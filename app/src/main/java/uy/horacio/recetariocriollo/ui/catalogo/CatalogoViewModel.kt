package uy.horacio.recetariocriollo.ui.catalogo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uy.horacio.recetariocriollo.datos.IngredienteRepositorio
import uy.horacio.recetariocriollo.datos.RecetaRepositorio
import uy.horacio.recetariocriollo.dominio.ValidacionIngrediente
import uy.horacio.recetariocriollo.dominio.modelo.Ingrediente

data class EstadoCatalogo(
    val catalogo: List<Ingrediente> = emptyList(),
    /** Cuántas recetas usan cada ingrediente. */
    val usos: Map<Long, Int> = emptyMap(),
    val cargando: Boolean = true
)

class CatalogoViewModel(
    private val ingredientes: IngredienteRepositorio,
    recetas: RecetaRepositorio
) : ViewModel() {

    val estado: StateFlow<EstadoCatalogo> = combine(
        ingredientes.observarCatalogo(),
        recetas.observarRecetas()
    ) { catalogo, listaRecetas ->
        EstadoCatalogo(
            catalogo = catalogo,
            usos = ValidacionIngrediente.usos(listaRecetas.map { receta -> receta.ingredientes.map { it.ingrediente.id } }),
            cargando = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EstadoCatalogo())

    fun guardar(ingrediente: Ingrediente) {
        // La validación ya corrió en el diálogo; se repite por si el catálogo cambió en el medio.
        val problema = ValidacionIngrediente.validar(
            ingrediente.nombre,
            ingrediente.densidadGramosPorTaza?.toString().orEmpty(),
            estado.value.catalogo,
            ingrediente.id
        )
        if (problema != null) return
        viewModelScope.launch {
            if (ingrediente.id == 0L) ingredientes.crear(ingrediente) else ingredientes.actualizar(ingrediente)
        }
    }

    /** Solo borra si ninguna receta lo usa; [alTerminar] recibe si se pudo. */
    fun borrar(id: Long, alTerminar: (Boolean) -> Unit) {
        viewModelScope.launch { alTerminar(ingredientes.borrarSiNoSeUsa(id)) }
    }
}
