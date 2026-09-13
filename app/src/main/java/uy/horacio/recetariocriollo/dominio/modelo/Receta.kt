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
    val dificultad: Dificultad? = null,
    /** Diametro del molde redondo en cm, si la receta usa uno. Base para ajustar por molde. */
    val moldeCm: Int? = null,
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

enum class Dificultad { FACIL, MEDIA, DIFICIL }

/** Una vez que se cocino la receta: como salio y que cambiar la proxima. */
data class Cocinada(
    val id: Long = 0,
    val recetaId: Long,
    val fechaMillis: Long,
    /** De 1 a 5. */
    val estrellas: Int,
    val porciones: Int,
    val nota: String? = null
)

/** Cocinada junto con el nombre de su receta, para el historial general. */
data class CocinadaConReceta(val cocinada: Cocinada, val nombreReceta: String)

/** Lo que se muestra de una receta sin recorrer todo su historial. */
data class ResumenCocinadas(
    val recetaId: Long,
    val veces: Int,
    /** Promedio de estrellas, o null si nunca se cocino. */
    val promedioEstrellas: Double?,
    val ultimaMillis: Long?
) {
    companion object {
        fun vacio(recetaId: Long) = ResumenCocinadas(recetaId, 0, null, null)
    }
}
