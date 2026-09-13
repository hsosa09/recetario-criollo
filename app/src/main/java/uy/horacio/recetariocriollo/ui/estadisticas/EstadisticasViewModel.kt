package uy.horacio.recetariocriollo.ui.estadisticas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import uy.horacio.recetariocriollo.datos.CocinadaRepositorio
import uy.horacio.recetariocriollo.datos.RecetaRepositorio
import uy.horacio.recetariocriollo.dominio.Estadisticas
import uy.horacio.recetariocriollo.dominio.EstadisticasAnio
import java.time.LocalDate

data class EstadoEstadisticas(
    val anios: List<Int> = emptyList(),
    val datos: EstadisticasAnio? = null
)

class EstadisticasViewModel(
    cocinadas: CocinadaRepositorio,
    recetas: RecetaRepositorio,
    private val anioActual: Int = LocalDate.now().year
) : ViewModel() {

    private val anioElegido = MutableStateFlow(anioActual)

    val estado: StateFlow<EstadoEstadisticas> = combine(
        cocinadas.observarTodas(),
        recetas.observarRecetas(),
        anioElegido
    ) { todas, listaRecetas, anio ->
        val lista = todas.map { it.cocinada }
        EstadoEstadisticas(
            anios = Estadisticas.anios(lista, anioActual),
            datos = Estadisticas.calcular(anio, lista, listaRecetas)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EstadoEstadisticas())

    fun elegirAnio(anio: Int) {
        anioElegido.value = anio
    }
}
