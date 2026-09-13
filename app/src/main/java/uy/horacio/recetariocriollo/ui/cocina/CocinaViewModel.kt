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
import uy.horacio.recetariocriollo.datos.RecetaRepositorio
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

    private companion object {
        const val CLAVE_PASO = "paso_actual"
    }
}
