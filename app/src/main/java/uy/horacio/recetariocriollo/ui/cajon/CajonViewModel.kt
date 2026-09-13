package uy.horacio.recetariocriollo.ui.cajon

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import uy.horacio.recetariocriollo.datos.CocinadaRepositorio
import uy.horacio.recetariocriollo.datos.IngredienteRepositorio
import uy.horacio.recetariocriollo.datos.RecetaRepositorio

/** Los números del cajón: cuánto hay en la biblioteca y cuánto se cocinó. */
data class ResumenBiblioteca(
    val recetas: Int = 0,
    val ingredientes: Int = 0,
    val cocinadas: Int = 0
)

class CajonViewModel(
    recetas: RecetaRepositorio,
    ingredientes: IngredienteRepositorio,
    cocinadas: CocinadaRepositorio
) : ViewModel() {

    val resumen: StateFlow<ResumenBiblioteca> = combine(
        recetas.observarRecetas().map { it.size },
        ingredientes.observarCatalogo().map { it.size },
        cocinadas.observarResumenes().map { mapa -> mapa.values.sumOf { it.veces } }
    ) { cantidadRecetas, cantidadIngredientes, vecesCocinado ->
        ResumenBiblioteca(cantidadRecetas, cantidadIngredientes, vecesCocinado)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ResumenBiblioteca())
}
