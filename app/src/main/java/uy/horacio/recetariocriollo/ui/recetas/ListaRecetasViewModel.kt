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
import uy.horacio.recetariocriollo.datos.CocinadaRepositorio
import uy.horacio.recetariocriollo.datos.RecetaRepositorio
import uy.horacio.recetariocriollo.dominio.Estacionalidad
import java.time.LocalDate
import uy.horacio.recetariocriollo.dominio.modelo.ResumenCocinadas
import uy.horacio.recetariocriollo.dominio.Texto
import uy.horacio.recetariocriollo.dominio.modelo.CategoriaReceta
import uy.horacio.recetariocriollo.dominio.modelo.Receta

data class EstadoListaRecetas(
    val recetas: List<Receta> = emptyList(),
    val hayRecetasCargadas: Boolean = false,
    val texto: String = "",
    val categoria: CategoriaReceta? = null,
    val soloFavoritas: Boolean = false,
    val soloDeEstacion: Boolean = false,
    /** Nombre de la original para cada variante. */
    val nombresOriginales: Map<Long, String> = emptyMap(),
    /** Ids de las recetas de estación en el mes actual. */
    val deEstacion: Set<Long> = emptySet(),
    val categoriasDisponibles: List<CategoriaReceta> = emptyList(),
    /** Veces cocinada y estrellas por receta; las nunca cocinadas no están. */
    val resumenes: Map<Long, ResumenCocinadas> = emptyMap(),
    val cargando: Boolean = true
)

private data class FiltroLista(
    val texto: String = "",
    val categoria: CategoriaReceta? = null,
    val soloFavoritas: Boolean = false,
    val soloDeEstacion: Boolean = false
)

class ListaRecetasViewModel(
    private val repositorio: RecetaRepositorio,
    cocinadas: CocinadaRepositorio,
    /** Inyectable para los tests; en la app es el mes de hoy en la zona del teléfono. */
    private val mesActual: () -> Int = { LocalDate.now().monthValue }
) : ViewModel() {

    private val filtro = MutableStateFlow(FiltroLista())

    val estado: StateFlow<EstadoListaRecetas> =
        combine(repositorio.observarRecetas(), filtro, cocinadas.observarResumenes()) { recetas, filtroActual, resumenes ->
            val mes = mesActual()
            val deEstacion = recetas.filter { Estacionalidad.esDeEstacion(it, mes) }.map { it.id }.toSet()
            val filtradas = recetas.filter { receta ->
                Texto.contiene(receta.nombre, filtroActual.texto) &&
                    (filtroActual.categoria == null || receta.categoria == filtroActual.categoria) &&
                    (!filtroActual.soloFavoritas || receta.esFavorita) &&
                    (!filtroActual.soloDeEstacion || receta.id in deEstacion)
            }
            EstadoListaRecetas(
                recetas = filtradas,
                hayRecetasCargadas = recetas.isNotEmpty(),
                texto = filtroActual.texto,
                categoria = filtroActual.categoria,
                soloFavoritas = filtroActual.soloFavoritas,
                soloDeEstacion = filtroActual.soloDeEstacion,
                deEstacion = deEstacion,
                nombresOriginales = recetas.mapNotNull { r -> r.origenId?.let { o -> recetas.firstOrNull { it.id == o }?.let { r.id to it.nombre } } }.toMap(),
                categoriasDisponibles = recetas.map { it.categoria }.distinct().sortedBy { it.ordinal },
                resumenes = resumenes,
                cargando = false
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = EstadoListaRecetas()
        )

    fun cambiarTexto(nuevo: String) = filtro.update { it.copy(texto = nuevo) }

    fun cambiarCategoria(nueva: CategoriaReceta?) = filtro.update { it.copy(categoria = nueva) }

    fun alternarSoloDeEstacion() = filtro.update { it.copy(soloDeEstacion = !it.soloDeEstacion) }

    fun alternarSoloFavoritas() = filtro.update { it.copy(soloFavoritas = !it.soloFavoritas) }

    fun alternarFavorita(receta: Receta) {
        viewModelScope.launch {
            repositorio.alternarFavorita(receta.id, !receta.esFavorita)
        }
    }
}
