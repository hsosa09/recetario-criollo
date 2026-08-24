package uy.horacio.recetariocriollo.dominio

import uy.horacio.recetariocriollo.dominio.modelo.Ingrediente
import uy.horacio.recetariocriollo.dominio.modelo.Receta
import kotlin.math.roundToInt

/** Que tan cocinable es una receta con lo que hay en la heladera. */
data class CoincidenciaReceta(
    val receta: Receta,
    val considerados: Int,
    val presentes: Int,
    val faltantes: List<Ingrediente>,
    val porcentaje: Int
) {
    val sePuedeCocinar: Boolean get() = faltantes.isEmpty() && considerados > 0
}

/**
 * Cruza las recetas contra los ingredientes tildados por el usuario.
 * Como todo sale del catalogo normalizado, el match es por id: sin sinonimos
 * ni errores de tipeo que resolver.
 */
object BuscadorPorIngredientes {

    fun evaluar(
        receta: Receta,
        disponibles: Set<Long>,
        asumirBasicosDeDespensa: Boolean = true
    ): CoincidenciaReceta {
        // Un mismo ingrediente puede aparecer dos veces en la receta (harina para la masa
        // y harina para estirar); para el calculo cuenta una sola vez.
        val distintos = receta.ingredientes
            .map { it.ingrediente }
            .distinctBy { it.id }

        val (presentes, faltantes) = distintos.partition { ingrediente ->
            ingrediente.id in disponibles ||
                (asumirBasicosDeDespensa && ingrediente.esBasicoDeDespensa)
        }

        val porcentaje = if (distintos.isEmpty()) 0
        else (presentes.size * 100.0 / distintos.size).roundToInt()

        return CoincidenciaReceta(
            receta = receta,
            considerados = distintos.size,
            presentes = presentes.size,
            faltantes = faltantes.sortedBy { it.nombre },
            porcentaje = porcentaje
        )
    }

    /**
     * Evalua todas las recetas y las ordena de mas cocinable a menos.
     * [porcentajeMinimo] saca de la lista lo que no vale la pena mostrar.
     */
    fun ordenarPorCoincidencia(
        recetas: List<Receta>,
        disponibles: Set<Long>,
        asumirBasicosDeDespensa: Boolean = true,
        porcentajeMinimo: Int = 1
    ): List<CoincidenciaReceta> = recetas
        .map { evaluar(it, disponibles, asumirBasicosDeDespensa) }
        .filter { it.porcentaje >= porcentajeMinimo }
        .sortedWith(
            compareByDescending<CoincidenciaReceta> { it.porcentaje }
                .thenBy { it.faltantes.size }
                .thenBy { it.receta.nombre.lowercase() }
        )
}
