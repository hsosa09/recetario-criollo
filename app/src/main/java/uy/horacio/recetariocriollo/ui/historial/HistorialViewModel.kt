package uy.horacio.recetariocriollo.ui.historial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uy.horacio.recetariocriollo.datos.CocinadaRepositorio
import uy.horacio.recetariocriollo.datos.RecetaRepositorio
import uy.horacio.recetariocriollo.dominio.Historial
import uy.horacio.recetariocriollo.dominio.modelo.CocinadaConReceta

data class PuestoHistorial(val recetaId: Long, val nombre: String, val veces: Int, val fraccion: Float)

data class EstadoHistorial(
    val ranking: List<PuestoHistorial> = emptyList(),
    val cocinadas: List<CocinadaConReceta> = emptyList(),
    val cargando: Boolean = true
)

class HistorialViewModel(
    private val cocinadas: CocinadaRepositorio,
    recetas: RecetaRepositorio
) : ViewModel() {

    val estado: StateFlow<EstadoHistorial> = combine(
        cocinadas.observarTodas(),
        cocinadas.observarResumenes(),
        recetas.observarRecetas()
    ) { todas, resumenes, listaRecetas ->
        val nombres = listaRecetas.associate { it.id to it.nombre }
        EstadoHistorial(
            ranking = Historial.masCocinadas(resumenes.values).mapNotNull { puesto ->
                nombres[puesto.recetaId]?.let { PuestoHistorial(puesto.recetaId, it, puesto.veces, puesto.fraccion) }
            },
            cocinadas = todas,
            cargando = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EstadoHistorial())

    fun borrar(id: Long) {
        viewModelScope.launch { cocinadas.borrar(id) }
    }
}
