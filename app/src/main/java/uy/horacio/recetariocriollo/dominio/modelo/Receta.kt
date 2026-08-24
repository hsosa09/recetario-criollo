package uy.horacio.recetariocriollo.dominio.modelo

/**
 * Ingrediente del catalogo normalizado. Se comparte entre recetas: toda la app
 * referencia ingredientes por id, nunca por texto tipeado a mano.
 */
data class Ingrediente(
    val id: Long = 0,
    val nombre: String,
    val categoria: CategoriaIngrediente = CategoriaIngrediente.OTROS,
    /** Cuanto pesa una taza de este ingrediente. Null = no se conoce, no se puede pasar de volumen a peso. */
    val densidadGramosPorTaza: Double? = null,
    /** Sal, especias y leudantes: por defecto escalan atenuado, no 1:1. */
    val esSalOEspecia: Boolean = false,
    /** Cosas que siempre hay en la despensa (sal, agua, aceite). Se pueden dar por disponibles al buscar. */
    val esBasicoDeDespensa: Boolean = false,
    val unidadHabitual: Unidad = Unidad.GRAMO
)

/** Un ingrediente dentro de una receta concreta, con su cantidad y su regla de escalado. */
data class IngredienteDeReceta(
    val id: Long = 0,
    val ingrediente: Ingrediente,
    val cantidad: Double,
    val unidad: Unidad,
    val regla: ReglaEscalado = ReglaEscalado.LINEAL,
    /** Detalle libre: "en cubos", "a temperatura ambiente". */
    val aclaracion: String? = null,
    val orden: Int = 0
)

data class PasoPreparacion(
    val id: Long = 0,
    val orden: Int,
    val texto: String,
    /** Si el paso tiene un tiempo tipico, se puede arrancar un cronometro desde el propio paso. */
    val timerSugeridoSegundos: Int? = null
)

data class Receta(
    val id: Long = 0,
    val nombre: String,
    val categoria: CategoriaReceta = CategoriaReceta.PLATO_PRINCIPAL,
    val porcionesBase: Int = 4,
    val tiempoMinutos: Int? = null,
    val notas: String? = null,
    val fotoPath: String? = null,
    val esFavorita: Boolean = false,
    val ingredientes: List<IngredienteDeReceta> = emptyList(),
    val pasos: List<PasoPreparacion> = emptyList()
) {
    val tiempoLegible: String?
        get() = tiempoMinutos?.let { minutos ->
            when {
                minutos < 60 -> "$minutos min"
                minutos % 60 == 0 -> "${minutos / 60} h"
                else -> "${minutos / 60} h ${minutos % 60} min"
            }
        }
}
