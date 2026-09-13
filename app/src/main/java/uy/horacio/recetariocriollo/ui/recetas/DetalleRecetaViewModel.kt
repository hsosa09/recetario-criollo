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
import kotlinx.coroutines.flow.first
import uy.horacio.recetariocriollo.datos.AjustesRepositorio
import uy.horacio.recetariocriollo.datos.CocinadaRepositorio
import uy.horacio.recetariocriollo.datos.RecetaRepositorio
import uy.horacio.recetariocriollo.dominio.modelo.Cocinada
import uy.horacio.recetariocriollo.dominio.CantidadEscalada
import uy.horacio.recetariocriollo.dominio.Escalador
import uy.horacio.recetariocriollo.dominio.Variantes
import uy.horacio.recetariocriollo.dominio.modelo.Receta

data class EstadoDetalleReceta(
    val receta: Receta? = null,
    val porciones: Int = 0,
    val ingredientes: List<CantidadEscalada> = emptyList(),
    val modoCocina: Boolean = false,
    /** Historial de esta receta, de la más reciente a la más vieja. */
    val cocinadas: List<Cocinada> = emptyList(),
    /** La original y sus variantes, sin esta receta. Vacía si no tiene familia. */
    val familia: List<Receta> = emptyList(),
    val cargando: Boolean = true
) {
    val estaEscalada: Boolean
        get() = receta != null && porciones != receta.porcionesBase
}

class DetalleRecetaViewModel(
    private val repositorio: RecetaRepositorio,
    cocinadas: CocinadaRepositorio,
    ajustes: AjustesRepositorio,
    private val cronometros: GestorCronometros,
    estadoGuardado: SavedStateHandle
) : ViewModel() {

    private val recetaId: Long = estadoGuardado.get<Long>(CLAVE_ID) ?: 0L

    /** null = todavia no la tocaron, se usan las porciones originales de la receta. */
    private val porcionesElegidas = MutableStateFlow<Int?>(null)
    private val modoCocina = MutableStateFlow(false)

    init {
        // El ajuste decide con qué modo abre; después se alterna a mano.
        viewModelScope.launch { modoCocina.value = ajustes.ajustes.first().modoCocinaPorDefecto }
    }

    val estado: StateFlow<EstadoDetalleReceta> =
        combine(
            repositorio.observarReceta(recetaId),
            porcionesElegidas,
            modoCocina,
            cocinadas.observarDeReceta(recetaId),
            repositorio.observarRecetas()
        ) { receta, porciones, cocina, historial, todas ->
            if (receta == null) {
                EstadoDetalleReceta(cargando = false)
            } else {
                val objetivo = porciones ?: receta.porcionesBase
                EstadoDetalleReceta(
                    receta = receta,
                    porciones = objetivo,
                    ingredientes = Escalador.escalarReceta(receta, objetivo),
                    modoCocina = cocina,
                    cocinadas = historial,
                    familia = Variantes.familia(receta, todas).filter { it.id != receta.id },
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
