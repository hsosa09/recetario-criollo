package uy.horacio.recetariocriollo.cronometro

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

/**
 * Los cronometros de la app, todos en un solo lugar para que se puedan ver y
 * manejar desde cualquier pantalla.
 *
 * Cada uno programa una alarma del sistema para avisar aunque la app este cerrada, y el
 * estado se persiste para sobrevivir al cierre. La alarma es exacta ([AlarmManager.setAlarmClock])
 * solo si el sistema lo permite: desde Android 12 hace falta SCHEDULE_EXACT_ALARM y desde
 * Android 13 ese permiso arranca denegado. Sin el, se programa inexacta y, con la app viva,
 * el aviso sale igual desde el latido.
 */
class GestorCronometros private constructor(private val contexto: Context) {

    private val preferencias =
        contexto.getSharedPreferences("cronometros", Context.MODE_PRIVATE)

    private val json = Json { ignoreUnknownKeys = true }

    private val _cronometros = MutableStateFlow<List<Cronometro>>(emptyList())
    val cronometros: StateFlow<List<Cronometro>> = _cronometros.asStateFlow()

    /** Reloj interno: se mueve una vez por segundo mientras haya algo corriendo. */
    private val _ahora = MutableStateFlow(System.currentTimeMillis())
    val ahora: StateFlow<Long> = _ahora.asStateFlow()

    private val alcance = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    init {
        _cronometros.value = leerGuardados()
        Notificaciones.crearCanal(contexto)
        alcance.launch { latir() }
    }

    private suspend fun latir() {
        while (true) {
            _ahora.value = System.currentTimeMillis()
            revisarVencidos()
            delay(500)
        }
    }

    private fun revisarVencidos() {
        val ahoraMillis = _ahora.value
        val actualizados = _cronometros.value.map { cronometro ->
            if (cronometro.estado == EstadoCronometro.CORRIENDO &&
                cronometro.restanteSegundos(ahoraMillis) <= 0
            ) {
                // Con la app viva se avisa aca y se cancela la alarma pendiente: si era
                // inexacta podria llegar minutos tarde, y si era exacta no hace falta dos veces.
                cancelarAlarma(cronometro)
                Notificaciones.avisarFin(contexto, cronometro.id, cronometro.etiqueta)
                cronometro.copy(estado = EstadoCronometro.TERMINADO, finEnMillis = null)
            } else {
                cronometro
            }
        }
        if (actualizados != _cronometros.value) guardar(actualizados)
    }

    /** true si el sistema deja programar la alarma en el segundo justo. */
    fun alarmasExactasPermitidas(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        return contexto.getSystemService(AlarmManager::class.java)?.canScheduleExactAlarms() == true
    }

    /** Vuelve a programar los que corren, por ejemplo al conceder el permiso de alarmas exactas. */
    fun reprogramarCorriendo() {
        _cronometros.value
            .filter { it.estado == EstadoCronometro.CORRIENDO }
            .forEach { programarAlarma(it) }
    }

    fun crear(etiqueta: String, segundos: Int): Cronometro {
        val nuevo = Cronometro(
            id = System.currentTimeMillis(),
            etiqueta = etiqueta.trim().ifBlank { ETIQUETA_POR_DEFECTO },
            duracionSegundos = segundos,
            finEnMillis = System.currentTimeMillis() + segundos * 1000L,
            restanteAlPausar = segundos,
            estado = EstadoCronometro.CORRIENDO
        )
        guardar(_cronometros.value + nuevo)
        programarAlarma(nuevo)
        return nuevo
    }

    fun pausar(id: Long) = modificar(id) { cronometro ->
        if (cronometro.estado != EstadoCronometro.CORRIENDO) return@modificar cronometro
        cancelarAlarma(cronometro)
        cronometro.copy(
            estado = EstadoCronometro.PAUSADO,
            restanteAlPausar = cronometro.restanteSegundos(System.currentTimeMillis()),
            finEnMillis = null
        )
    }

    fun reanudar(id: Long) = modificar(id) { cronometro ->
        if (cronometro.estado == EstadoCronometro.CORRIENDO) return@modificar cronometro
        val restante = if (cronometro.estado == EstadoCronometro.TERMINADO) {
            cronometro.duracionSegundos
        } else {
            cronometro.restanteAlPausar
        }
        cronometro.copy(
            estado = EstadoCronometro.CORRIENDO,
            finEnMillis = System.currentTimeMillis() + restante * 1000L,
            restanteAlPausar = restante
        ).also { programarAlarma(it) }
    }

    fun reiniciar(id: Long) = modificar(id) { cronometro ->
        cancelarAlarma(cronometro)
        cronometro.copy(
            estado = EstadoCronometro.CORRIENDO,
            finEnMillis = System.currentTimeMillis() + cronometro.duracionSegundos * 1000L,
            restanteAlPausar = cronometro.duracionSegundos
        ).also { programarAlarma(it) }
    }

    /** Suma o resta tiempo al vuelo, para cuando falta "un ratito mas". */
    fun ajustar(id: Long, segundos: Int) = modificar(id) { cronometro ->
        cancelarAlarma(cronometro)
        val restante = (cronometro.restanteSegundos(System.currentTimeMillis()) + segundos)
            .coerceAtLeast(0)
        val duracion = (cronometro.duracionSegundos + segundos).coerceAtLeast(restante)
        when (cronometro.estado) {
            EstadoCronometro.CORRIENDO, EstadoCronometro.TERMINADO -> cronometro.copy(
                estado = EstadoCronometro.CORRIENDO,
                duracionSegundos = duracion,
                finEnMillis = System.currentTimeMillis() + restante * 1000L,
                restanteAlPausar = restante
            ).also { programarAlarma(it) }
            EstadoCronometro.PAUSADO -> cronometro.copy(
                duracionSegundos = duracion,
                restanteAlPausar = restante
            )
        }
    }

    fun quitar(id: Long) {
        val cronometro = _cronometros.value.firstOrNull { it.id == id } ?: return
        cancelarAlarma(cronometro)
        guardar(_cronometros.value.filterNot { it.id == id })
    }

    fun quitarTerminados() {
        guardar(_cronometros.value.filterNot { it.estado == EstadoCronometro.TERMINADO })
    }

    fun marcarTerminado(id: Long) = modificar(id) { cronometro ->
        cronometro.copy(estado = EstadoCronometro.TERMINADO, finEnMillis = null)
    }

    private inline fun modificar(id: Long, transformar: (Cronometro) -> Cronometro) {
        val actualizados = _cronometros.value.map { if (it.id == id) transformar(it) else it }
        guardar(actualizados)
    }

    private fun guardar(lista: List<Cronometro>) {
        _cronometros.value = lista
        preferencias.edit()
            .putString(CLAVE_LISTA, json.encodeToString(lista))
            .apply()
    }

    private fun leerGuardados(): List<Cronometro> {
        val texto = preferencias.getString(CLAVE_LISTA, null) ?: return emptyList()
        val guardados = runCatching { json.decodeFromString<List<Cronometro>>(texto) }
            .getOrDefault(emptyList())
        val ahoraMillis = System.currentTimeMillis()
        // Los que vencieron con la app cerrada ya avisaron por notificacion; aca solo se reflejan.
        return guardados.map { cronometro ->
            if (cronometro.estado == EstadoCronometro.CORRIENDO &&
                cronometro.restanteSegundos(ahoraMillis) <= 0
            ) {
                cronometro.copy(estado = EstadoCronometro.TERMINADO, finEnMillis = null)
            } else {
                cronometro
            }
        }
    }

    private fun programarAlarma(cronometro: Cronometro) {
        val fin = cronometro.finEnMillis ?: return
        val gestorAlarmas = contexto.getSystemService(AlarmManager::class.java) ?: return
        val pendiente = pendienteDe(cronometro)
        if (alarmasExactasPermitidas()) {
            try {
                gestorAlarmas.setAlarmClock(AlarmManager.AlarmClockInfo(fin, pendiente), pendiente)
                return
            } catch (_: SecurityException) {
                // El permiso se revoco entre el chequeo y el uso: se sigue con la inexacta.
            }
        }
        // Inexacta pero permitida siempre: en reposo Android puede demorarla unos minutos.
        gestorAlarmas.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, fin, pendiente)
    }

    private fun cancelarAlarma(cronometro: Cronometro) {
        val gestorAlarmas = contexto.getSystemService(AlarmManager::class.java) ?: return
        gestorAlarmas.cancel(pendienteDe(cronometro))
    }

    private fun pendienteDe(cronometro: Cronometro): PendingIntent {
        val intencion = Intent(contexto, ReceptorFinCronometro::class.java).apply {
            putExtra(ReceptorFinCronometro.EXTRA_ID, cronometro.id)
            putExtra(ReceptorFinCronometro.EXTRA_ETIQUETA, cronometro.etiqueta)
        }
        return PendingIntent.getBroadcast(
            contexto,
            cronometro.id.toInt(),
            intencion,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        private const val CLAVE_LISTA = "lista_cronometros"
        private const val ETIQUETA_POR_DEFECTO = "Cronómetro"

        /** Duraciones que se usan todo el tiempo en la cocina. */
        val ATAJOS_SEGUNDOS = listOf(60, 180, 300, 600, 900, 1800, 2700, 3600)

        @Volatile
        private var instancia: GestorCronometros? = null

        fun obtener(contexto: Context): GestorCronometros =
            instancia ?: synchronized(this) {
                instancia ?: GestorCronometros(contexto.applicationContext).also { instancia = it }
            }
    }
}
