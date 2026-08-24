package uy.horacio.recetariocriollo.ui.recetas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uy.horacio.recetariocriollo.datos.RecetaRepositorio
import uy.horacio.recetariocriollo.dominio.modelo.CategoriaReceta
import uy.horacio.recetariocriollo.dominio.modelo.Receta

data class EstadoListaRecetas(
    val recetas: List<Receta> = emptyList(),
    val hayRecetasCargadas: Boolean = false,
    val texto: String = "",
    val categoria: CategoriaReceta? = null,
    val soloFavoritas: Boolean = false,
    val categoriasDisponibles: List<CategoriaReceta> = emptyList(),
    val cargando: Boolean = true
)

private data class FiltroLista(
    val texto: String = "",
    val categoria: CategoriaReceta? = null,
    val soloFavoritas: Boolean = false
)

class ListaRecetasViewModel(private val repositorio: RecetaRepositorio) : ViewModel() {

    private val filtro = MutableStateFlow(FiltroLista())

    val estado: StateFlow<EstadoListaRecetas> =
        combine(repositorio.observarRecetas(), filtro) { recetas, filtroActual ->
            val texto = filtroActual.texto.trim().lowercase()
            val filtradas = recetas.filter { receta ->
                (texto.isEmpty() || receta.nombre.lowercase().contains(texto)) &&
                    (filtroActual.categoria == null || receta.categoria == filtroActual.categoria) &&
                    (!filtroActual.soloFavoritas || receta.esFavorita)
            }
            EstadoListaRecetas(
                recetas = filtradas,
                hayRecetasCargadas = recetas.isNotEmpty(),
                texto = filtroActual.texto,
                categoria = filtroActual.categoria,
                soloFavoritas = filtroActual.soloFavoritas,
                categoriasDisponibles = recetas.map { it.categoria }.distinct().sortedBy { it.ordinal },
                cargando = false
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = EstadoListaRecetas()
        )

    fun cambiarTexto(nuevo: String) = filtro.update { it.copy(texto = nuevo) }

    fun cambiarCategoria(nueva: CategoriaReceta?) = filtro.update { it.copy(categoria = nueva) }

    fun alternarSoloFavoritas() = filtro.update { it.copy(soloFavoritas = !it.soloFavoritas) }

    fun alternarFavorita(receta: Receta) {
        viewModelScope.launch {
            repositorio.alternarFavorita(receta.id, !receta.esFavorita)
        }
    }
}
