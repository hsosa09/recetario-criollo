package uy.horacio.recetariocriollo.dominio

import uy.horacio.recetariocriollo.dominio.modelo.CategoriaReceta

/**
 * Plantillas del generador de recetas: dan el esqueleto de pasos tipico de cada
 * tipo de plato para no arrancar de la hoja en blanco. Todo se puede editar despues.
 */
data class PlantillaReceta(
    val id: String,
    val nombre: String,
    val categoria: CategoriaReceta,
    val porcionesSugeridas: Int,
    val tiempoSugeridoMinutos: Int,
    val pasos: List<String>
)

object Plantillas {

    val todas: List<PlantillaReceta> = listOf(
        PlantillaReceta(
            id = "entrada",
            nombre = "Entrada o picada",
            categoria = CategoriaReceta.ENTRADA,
            porcionesSugeridas = 4,
            tiempoSugeridoMinutos = 20,
            pasos = listOf(
                "Preparar y cortar los ingredientes.",
                "Armar la fuente o los platitos.",
                "Condimentar y servir."
            )
        ),
        PlantillaReceta(
            id = "horno",
            nombre = "Plato al horno",
            categoria = CategoriaReceta.PLATO_PRINCIPAL,
            porcionesSugeridas = 4,
            tiempoSugeridoMinutos = 60,
            pasos = listOf(
                "Precalentar el horno.",
                "Preparar los ingredientes: pelar, cortar, condimentar.",
                "Armar la fuente por capas.",
                "Llevar al horno y controlar la cocción.",
                "Dejar reposar unos minutos antes de servir."
            )
        ),
        PlantillaReceta(
            id = "olla",
            nombre = "Guiso u olla",
            categoria = CategoriaReceta.SOPA_Y_GUISO,
            porcionesSugeridas = 6,
            tiempoSugeridoMinutos = 75,
            pasos = listOf(
                "Rehogar la cebolla, el morrón y el ajo.",
                "Sellar la carne hasta que tome color.",
                "Sumar las verduras duras y el líquido.",
                "Cocinar a fuego bajo hasta que esté tierno.",
                "Corregir la sal y dejar espesar."
            )
        ),
        PlantillaReceta(
            id = "postre",
            nombre = "Postre",
            categoria = CategoriaReceta.POSTRE,
            porcionesSugeridas = 8,
            tiempoSugeridoMinutos = 60,
            pasos = listOf(
                "Preparar el molde.",
                "Mezclar los ingredientes secos por un lado y los húmedos por otro.",
                "Unir sin batir de más.",
                "Cocinar o enfriar según corresponda.",
                "Desmoldar y decorar."
            )
        ),
        PlantillaReceta(
            id = "panificado",
            nombre = "Panificado con levadura",
            categoria = CategoriaReceta.PANIFICADOS,
            porcionesSugeridas = 12,
            tiempoSugeridoMinutos = 150,
            pasos = listOf(
                "Activar la levadura en líquido tibio.",
                "Mezclar la harina con la sal y hacer un hueco en el centro.",
                "Unir e ir amasando hasta que la masa quede lisa.",
                "Dejar levar tapada hasta que doble el volumen.",
                "Armar las piezas y dejar levar de nuevo.",
                "Cocinar y dejar enfriar sobre rejilla."
            )
        )
    )

    fun porId(id: String): PlantillaReceta? = todas.firstOrNull { it.id == id }
}
