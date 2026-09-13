package uy.horacio.recetariocriollo.datos

import uy.horacio.recetariocriollo.dominio.modelo.CategoriaReceta
import uy.horacio.recetariocriollo.dominio.modelo.ReglaEscalado
import uy.horacio.recetariocriollo.dominio.modelo.Unidad

data class IngredienteSemilla(
    val nombre: String,
    val cantidad: Double,
    val unidad: Unidad,
    val regla: ReglaEscalado = ReglaEscalado.LINEAL,
    val aclaracion: String? = null
)

data class PasoSemilla(val texto: String, val timerSegundos: Int? = null)

data class RecetaSemilla(
    val nombre: String,
    val categoria: CategoriaReceta,
    val porciones: Int,
    val tiempoMinutos: Int,
    val notas: String?,
    val ingredientes: List<IngredienteSemilla>,
    val pasos: List<PasoSemilla>
)

/** Tres clasicas para que la app no arranque vacia y se vea como funciona el escalado. */
object RecetasIniciales {

    val recetas: List<RecetaSemilla> = listOf(
        RecetaSemilla(
            nombre = "Tortas fritas",
            categoria = CategoriaReceta.PANIFICADOS,
            porciones = 12,
            tiempoMinutos = 60,
            notas = "Las de la lluvia. La grasa se puede cambiar por manteca, pero no es lo mismo.",
            ingredientes = listOf(
                IngredienteSemilla("Harina 0000", 500.0, Unidad.GRAMO),
                IngredienteSemilla("Agua", 250.0, Unidad.MILILITRO, aclaracion = "tibia"),
                IngredienteSemilla("Sal fina", 1.0, Unidad.CUCHARADITA, ReglaEscalado.ATENUADA),
                IngredienteSemilla("Grasa vacuna", 50.0, Unidad.GRAMO, aclaracion = "derretida"),
                IngredienteSemilla("Polvo de hornear", 1.0, Unidad.CUCHARADITA, ReglaEscalado.ATENUADA),
                IngredienteSemilla("Aceite", 1.0, Unidad.LITRO, ReglaEscalado.FIJA, "para freír")
            ),
            pasos = listOf(
                PasoSemilla("Poner la harina en un bol con la sal y el polvo de hornear. Hacer un hueco en el medio."),
                PasoSemilla("Agregar la grasa derretida y el agua tibia de a poco, uniendo con la mano."),
                PasoSemilla("Amasar hasta que quede lisa y no se pegue, unos 8 minutos.", 480),
                PasoSemilla("Tapar con un repasador y dejar descansar 20 minutos.", 1200),
                PasoSemilla("Estirar de 1 cm, cortar discos y hacerles el agujerito del medio."),
                PasoSemilla("Freír en aceite caliente hasta que estén doradas de los dos lados.", 180)
            )
        ),
        RecetaSemilla(
            nombre = "Pastel de papa",
            categoria = CategoriaReceta.PLATO_PRINCIPAL,
            porciones = 6,
            tiempoMinutos = 75,
            notas = "Si sobra puré, va arriba más grueso y listo.",
            ingredientes = listOf(
                IngredienteSemilla("Papa", 8.0, Unidad.UNIDAD, aclaracion = "grandes"),
                IngredienteSemilla("Carne picada", 700.0, Unidad.GRAMO),
                IngredienteSemilla("Cebolla", 2.0, Unidad.UNIDAD),
                IngredienteSemilla("Morrón rojo", 1.0, Unidad.UNIDAD),
                IngredienteSemilla("Huevo", 2.0, Unidad.UNIDAD),
                IngredienteSemilla("Leche", 100.0, Unidad.MILILITRO),
                IngredienteSemilla("Manteca", 50.0, Unidad.GRAMO),
                IngredienteSemilla("Queso rallado", 60.0, Unidad.GRAMO),
                IngredienteSemilla("Aceite", 2.0, Unidad.CUCHARADA),
                IngredienteSemilla("Sal fina", 1.0, Unidad.CUCHARADITA, ReglaEscalado.ATENUADA),
                IngredienteSemilla("Pimienta negra", 0.5, Unidad.CUCHARADITA, ReglaEscalado.ATENUADA),
                IngredienteSemilla("Nuez moscada", 1.0, Unidad.PIZCA, ReglaEscalado.ATENUADA)
            ),
            pasos = listOf(
                PasoSemilla("Pelar y hervir las papas en agua con sal hasta que se pinchen fácil.", 1500),
                PasoSemilla("Mientras tanto, rehogar la cebolla y el morrón picados en el aceite.", 420),
                PasoSemilla("Sumar la carne picada, salpimentar y cocinar hasta que pierda el rojo.", 600),
                PasoSemilla("Pisar las papas con la manteca, la leche y la nuez moscada hasta hacer un puré."),
                PasoSemilla("Mezclar los huevos con la mitad del puré."),
                PasoSemilla("Armar en fuente: una capa de puré, el relleno de carne y otra capa de puré."),
                PasoSemilla("Espolvorear con queso rallado y gratinar en horno fuerte.", 900)
            )
        ),
        RecetaSemilla(
            nombre = "Flan casero",
            categoria = CategoriaReceta.POSTRE,
            porciones = 8,
            tiempoMinutos = 90,
            notas = "El baño María no puede hervir fuerte o el flan sale con agujeritos.",
            ingredientes = listOf(
                IngredienteSemilla("Huevo", 6.0, Unidad.UNIDAD),
                IngredienteSemilla("Leche", 1.0, Unidad.LITRO),
                IngredienteSemilla("Azúcar", 200.0, Unidad.GRAMO, aclaracion = "para la mezcla"),
                IngredienteSemilla("Azúcar", 150.0, Unidad.GRAMO, ReglaEscalado.FIJA, "para el caramelo"),
                IngredienteSemilla("Esencia de vainilla", 1.0, Unidad.CUCHARADITA, ReglaEscalado.ATENUADA)
            ),
            pasos = listOf(
                PasoSemilla("Hacer el caramelo con el azúcar y un chorrito de agua. Volcarlo en la budinera y girarla para que cubra."),
                PasoSemilla("Batir los huevos con el azúcar sin hacer espuma."),
                PasoSemilla("Agregar la leche tibia y la vainilla, mezclando despacio."),
                PasoSemilla("Colar la mezcla sobre el caramelo."),
                PasoSemilla("Cocinar a baño María en horno moderado hasta que esté firme al medio.", 3600),
                PasoSemilla("Enfriar en la heladera al menos 4 horas antes de desmoldar.")
            )
        )
    )
}
