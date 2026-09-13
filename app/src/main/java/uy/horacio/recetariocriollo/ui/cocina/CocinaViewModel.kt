package uy.horacio.recetariocriollo.ui.cocina

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import uy.horacio.recetariocriollo.cronometro.Cronometro
import uy.horacio.recetariocriollo.cronometro.EstadoCronometro
import uy.horacio.recetariocriollo.cronometro.GestorCronometros
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import uy.horacio.recetariocriollo.datos.AlmacenFotos
import uy.horacio.recetariocriollo.datos.GrabadorNotas
import uy.horacio.recetariocriollo.datos.CocinadaRepositorio
import uy.horacio.recetariocriollo.datos.RecetaRepositorio
import uy.horacio.recetariocriollo.dominio.modelo.Cocinada
import kotlinx.coroutines.launch
import uy.horacio.recetariocriollo.dominio.CantidadEscalada
import uy.horacio.recetariocriollo.dominio.Escalador
import uy.horacio.recetariocriollo.dominio.IngredientesDelPaso
import uy.horacio.recetariocriollo.dominio.modelo.Receta

data class EstadoCocina(
    val receta: Receta? = null,
    val porciones: Int = 0,
    val pasoActual: Int = 0,
    /** Ingredientes escalados, con los que nombra el paso actual primero. */
    val ingredientes: List<CantidadEscalada> = emptyList(),
    /** Ids de receta_ingredientes que nombra el paso actual, para resaltarlos. */
    val mencionados: Set<Long> = emptySet(),
    val timersVivos: List<Cronometro> = emptyList(),
    val ahora: Long = 0L,
    val cargando: Boolean = true
) {
    val totalPasos: Int get() = receta?.pasos?.size ?: 0
    val esUltimo: Boolean get() = pasoActual >= totalPasos - 1
    val progreso: Float get() = if (totalPasos == 0) 0f else (pasoActual + 1f) / totalPasos
}

class CocinaViewModel(
    repositorio: RecetaRepositorio,
    private val cocinadas: CocinadaRepositorio,
    private val almacenFotos: AlmacenFotos,
    private val grabador: GrabadorNotas,
    private val cronometros: GestorCronometros,
    private val estadoGuardado: SavedStateHandle
) : ViewModel() {

    private val recetaId: Long = estadoGuardado.get<Long>("recetaId") ?: 0L
    private val porcionesPedidas: Int = estadoGuardado.get<Int>("porciones") ?: 0

    // El paso vive en el SavedStateHandle: sobrevive a la rotacion y a la muerte del proceso.
    private val paso = estadoGuardado.getStateFlow(CLAVE_PASO, 0)

    val estado: StateFlow<EstadoCocina> = combine(
        repositorio.observarReceta(recetaId),
        paso,
        cronometros.cronometros,
        cronometros.ahora
    ) { receta, indice, timers, ahora ->
        if (receta == null) return@combine EstadoCocina(cargando = false)
        val porciones = porcionesPedidas.takeIf { it > 0 } ?: receta.porcionesBase
        val pasoActual = indice.coerceIn(0, (receta.pasos.size - 1).coerceAtLeast(0))
        val textoPaso = receta.pasos.getOrNull(pasoActual)?.texto.orEmpty()
        val escalados = Escalador.escalarReceta(receta, porciones)
        val idsCatalogo = IngredientesDelPaso.mencionados(textoPaso, escalados.map { it.ingrediente.ingrediente })
        EstadoCocina(
            receta = receta,
            porciones = porciones,
            pasoActual = pasoActual,
            ingredientes = IngredientesDelPaso.ordenar(textoPaso, escalados) { it.ingrediente.ingrediente },
            mencionados = escalados
                .filter { it.ingrediente.ingrediente.id in idsCatalogo }
                .map { it.ingrediente.id }
                .toSet(),
            timersVivos = timers.filter { it.estado != EstadoCronometro.TERMINADO },
            ahora = ahora,
            cargando = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EstadoCocina())

    fun irAPaso(indice: Int) {
        val total = estado.value.totalPasos
        if (total == 0) return
        estadoGuardado[CLAVE_PASO] = indice.coerceIn(0, total - 1)
    }

    fun siguiente() = irAPaso(estado.value.pasoActual + 1)

    fun anterior() = irAPaso(estado.value.pasoActual - 1)

    /** Arranca el timer sugerido del paso actual. Devuelve la etiqueta, o null si no tiene. */
    fun arrancarTimerDelPaso(): String? {
        val actual = estado.value
        val receta = actual.receta ?: return null
        val segundos = receta.pasos.getOrNull(actual.pasoActual)?.timerSugeridoSegundos ?: return null
        val etiqueta = "${receta.nombre} · paso ${actual.pasoActual + 1}"
        cronometros.crear(etiqueta, segundos)
        return etiqueta
    }

    private val _fotoResultado = MutableStateFlow<String?>(estadoGuardado[CLAVE_FOTO])
    /** Foto de cómo quedó, ya copiada a la app, mientras se completa «¿Cómo salió?». */
    val fotoResultado: StateFlow<String?> = _fotoResultado.asStateFlow()

    fun usarFotoResultado(origen: Uri, alTerminar: () -> Unit = {}) {
        viewModelScope.launch {
            val ruta = almacenFotos.guardarDesde(origen)
            if (ruta != null) {
                _fotoResultado.value?.let { almacenFotos.borrar(it) }
                _fotoResultado.value = ruta
                estadoGuardado[CLAVE_FOTO] = ruta
            }
            alTerminar()
        }
    }

    /** Saca la foto elegida y la borra: todavía no la usa ninguna cocinada. */
    fun descartarFotoResultado() {
        _fotoResultado.value?.let { almacenFotos.descartar(listOf(it)) }
        _fotoResultado.value = null
        estadoGuardado[CLAVE_FOTO] = null
    }

    fun archivoParaCamara() = almacenFotos.archivoParaCamara()

    private val _audioResultado = MutableStateFlow<String?>(estadoGuardado[CLAVE_AUDIO])
    /** Nota de voz grabada para «¿Cómo salió?». */
    val audioResultado: StateFlow<String?> = _audioResultado.asStateFlow()

    private val _msGrabando = MutableStateFlow<Long?>(null)
    /** Milisegundos grabados, o null si no se está grabando. */
    val msGrabando: StateFlow<Long?> = _msGrabando.asStateFlow()

    /** Empieza o termina la grabación. Devuelve false si no se pudo empezar (micrófono ocupado). */
    fun alternarGrabacion(): Boolean {
        if (grabador.grabando) {
            terminarGrabacion()
            return true
        }
        val empezo = grabador.empezar(alLlegarAlMaximo = { viewModelScope.launch { terminarGrabacion() } })
        if (empezo) {
            viewModelScope.launch {
                while (grabador.grabando) {
                    _msGrabando.value = grabador.milisegundos()
                    delay(200)
                }
                _msGrabando.value = null
            }
        }
        return empezo
    }

    private fun terminarGrabacion() {
        val ruta = grabador.detener() ?: return
        _audioResultado.value?.let { almacenFotos.descartar(listOf(it)) }
        _audioResultado.value = ruta
        estadoGuardado[CLAVE_AUDIO] = ruta
    }

    fun descartarAudio() {
        grabador.cancelar()
        _audioResultado.value?.let { almacenFotos.descartar(listOf(it)) }
        _audioResultado.value = null
        estadoGuardado[CLAVE_AUDIO] = null
    }

    override fun onCleared() {
        // Si se salió sin guardar, la foto y el audio quedaron huérfanos.
        grabador.cancelar()
        almacenFotos.descartar(listOfNotNull(_fotoResultado.value, _audioResultado.value))
    }

    /** Anota en el historial como salio. Llama a [alTerminar] cuando quedo guardado. */
    fun registrarCocinada(estrellas: Int, nota: String, alTerminar: () -> Unit) {
        val actual = estado.value
        val receta = actual.receta ?: return alTerminar()
        // Si todavía estaba grabando, lo grabado hasta ahora también cuenta.
        if (grabador.grabando) terminarGrabacion()
        viewModelScope.launch {
            cocinadas.registrar(
                Cocinada(
                    recetaId = receta.id,
                    fechaMillis = System.currentTimeMillis(),
                    estrellas = estrellas,
                    porciones = actual.porciones,
                    nota = nota,
                    fotoPath = _fotoResultado.value,
                    audioPath = _audioResultado.value
                )
            )
            _audioResultado.value = null
            estadoGuardado[CLAVE_AUDIO] = null
            // Ya es de la cocinada: que onCleared no la borre.
            _fotoResultado.value = null
            estadoGuardado[CLAVE_FOTO] = null
            alTerminar()
        }
    }

    private companion object {
        const val CLAVE_PASO = "paso_actual"
        const val CLAVE_FOTO = "foto_resultado"
        const val CLAVE_AUDIO = "audio_resultado"
    }
}
