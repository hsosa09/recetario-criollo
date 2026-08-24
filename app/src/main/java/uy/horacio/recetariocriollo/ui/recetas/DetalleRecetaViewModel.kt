package uy.horacio.recetariocriollo.ui.recetas

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uy.horacio.recetariocriollo.cronometro.GestorCronometros
import uy.horacio.recetariocriollo.datos.RecetaRepositorio
import uy.horacio.recetariocriollo.dominio.CantidadEscalada
import uy.horacio.recetariocriollo.dominio.Escalador
import uy.horacio.recetariocriollo.dominio.modelo.Receta

data class EstadoDetalleReceta(
    val receta: Receta? = null,
    val porciones: Int = 0,
    val ingredientes: List<CantidadEscalada> = emptyList(),
    val modoCocina: Boolean = false,
    val cargando: Boolean = true
) {
    val estaEscalada: Boolean
        get() = receta != null && porciones != receta.porcionesBase
}

class DetalleRecetaViewModel(
    private val repositorio: RecetaRepositorio,
    private val cronometros: GestorCronometros,
    estadoGuardado: SavedStateHandle
) : ViewModel() {

    private val recetaId: Long = estadoGuardado.get<Long>(CLAVE_ID) ?: 0L

    /** null = todavia no la tocaron, se usan las porciones originales de la receta. */
    private val porcionesElegidas = MutableStateFlow<Int?>(null)
    private val modoCocina = MutableStateFlow(false)

    val estado: StateFlow<EstadoDetalleReceta> =
        combine(
            repositorio.observarReceta(recetaId),
            porcionesElegidas,
            modoCocina
        ) { receta, porciones, cocina ->
            if (receta == null) {
                EstadoDetalleReceta(cargando = false)
            } else {
                val objetivo = porciones ?: receta.porcionesBase
                EstadoDetalleReceta(
                    receta = receta,
                    porciones = objetivo,
                    ingredientes = Escalador.escalarReceta(receta, objetivo),
                    modoCocina = cocina,
                    cargando = false
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = EstadoDetalleReceta()
        )

    fun cambiarPorciones(nuevas: Int) {
        porcionesElegidas.value = nuevas.coerceIn(1, MAXIMO_PORCIONES)
    }

    fun restaurarPorciones() {
        porcionesElegidas.value = null
    }

    fun alternarModoCocina() {
        modoCocina.value = !modoCocina.value
    }

    fun alternarFavorita() {
        val receta = estado.value.receta ?: return
        viewModelScope.launch {
            repositorio.alternarFavorita(receta.id, !receta.esFavorita)
        }
    }

    fun borrar(alTerminar: () -> Unit) {
        viewModelScope.launch {
            repositorio.borrar(recetaId)
            alTerminar()
        }
    }

    /** Arranca un cronometro con el nombre de la receta y el numero de paso. */
    fun arrancarTimerDePaso(numeroPaso: Int, segundos: Int): String {
        val nombre = estado.value.receta?.nombre.orEmpty()
        val etiqueta = if (nombre.isBlank()) "Paso $numeroPaso" else "$nombre · paso $numeroPaso"
        cronometros.crear(etiqueta, segundos)
        return etiqueta
    }

    companion object {
        const val CLAVE_ID = "recetaId"
        const val MAXIMO_PORCIONES = 99
    }
}
