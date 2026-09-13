package uy.horacio.recetariocriollo.ui.busqueda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import uy.horacio.recetariocriollo.datos.IngredienteRepositorio
import uy.horacio.recetariocriollo.datos.RecetaRepositorio
import uy.horacio.recetariocriollo.dominio.BuscadorPorIngredientes
import uy.horacio.recetariocriollo.dominio.CoincidenciaReceta
import uy.horacio.recetariocriollo.dominio.modelo.Ingrediente

data class EstadoBusqueda(
    val catalogo: List<Ingrediente> = emptyList(),
    val seleccionados: Set<Long> = emptySet(),
    val asumirBasicos: Boolean = true,
    val resultados: List<CoincidenciaReceta> = emptyList(),
    val hayRecetas: Boolean = false,
    /** Lo que aparece en alguna receta: tildar algo que ninguna usa no cambia el resultado. */
    val ingredientesDeRecetas: List<Ingrediente> = emptyList()
) {
    val ingredientesElegidos: List<Ingrediente>
        get() = catalogo.filter { it.id in seleccionados }
}

private data class Preferencias(
    val seleccionados: Set<Long> = emptySet(),
    val asumirBasicos: Boolean = true
)

class BusquedaViewModel(
    recetas: RecetaRepositorio,
    ingredientes: IngredienteRepositorio
) : ViewModel() {

    private val preferencias = MutableStateFlow(Preferencias())

    val estado: StateFlow<EstadoBusqueda> = combine(
        ingredientes.observarCatalogo(),
        recetas.observarRecetas(),
        preferencias
    ) { catalogo, listaRecetas, elegido ->
        val resultados = if (elegido.seleccionados.isEmpty()) {
            emptyList()
        } else {
            BuscadorPorIngredientes.ordenarPorCoincidencia(
                recetas = listaRecetas,
                disponibles = elegido.seleccionados,
                asumirBasicosDeDespensa = elegido.asumirBasicos
            )
        }
        EstadoBusqueda(
            catalogo = catalogo,
            seleccionados = elegido.seleccionados,
            asumirBasicos = elegido.asumirBasicos,
            resultados = resultados,
            hayRecetas = listaRecetas.isNotEmpty(),
            ingredientesDeRecetas = listaRecetas
                .flatMap { receta -> receta.ingredientes.map { it.ingrediente } }
                .distinctBy { it.id }
                .sortedBy { it.nombre.lowercase() }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = EstadoBusqueda()
    )

    fun alternar(ingrediente: Ingrediente) = preferencias.update { actual ->
        val nuevos = if (ingrediente.id in actual.seleccionados) {
            actual.seleccionados - ingrediente.id
        } else {
            actual.seleccionados + ingrediente.id
        }
        actual.copy(seleccionados = nuevos)
    }

    fun alternarBasicos() = preferencias.update { it.copy(asumirBasicos = !it.asumirBasicos) }

    fun limpiar() = preferencias.update { it.copy(seleccionados = emptySet()) }
}
