package uy.horacio.recetariocriollo.ui.componentes

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * Iconos de trazo del prototipo: linea de 2 dp con puntas cuadradas, en grilla de 24.
 * Se pintan en negro y el color real lo pone el tint del Icon.
 */
object Iconos {

    private fun trazo(nombre: String, grosor: Float, vararg trazos: String): ImageVector =
        ImageVector.Builder(
            name = nombre,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            trazos.forEach { datos ->
                addPath(
                    pathData = addPathNodes(datos),
                    fill = null,
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = grosor,
                    strokeLineCap = StrokeCap.Square,
                    strokeLineJoin = StrokeJoin.Miter
                )
            }
        }.build()

    private const val CORAZON = "M12 21s-8-5.3-8-10.4A4.6 4.6 0 0 1 12 7a4.6 4.6 0 0 1 8 3.6C20 15.7 12 21 12 21z"
    private const val CIRCULO_RELOJ = "M4 13a8 8 0 1 0 16 0a8 8 0 1 0 -16 0"

    val Recetas = trazo("recetas", 2f, "M4 4h7v16H4zM13 4h7v16h-7zM13 9h7")
    val Tengo = trazo("tengo", 2f, "M3 8h18l-2 12H5L3 8zM8 8V5h8v3")
    val Conversor = trazo("conversor", 2f, "M12 3v18M4 8h16M4 8l-2 6h6zM20 8l2 6h-6z")
    val Timer = trazo("timer", 2.2f, CIRCULO_RELOJ, "M12 9v4l3 2M9 2h6")
    val Volver = trazo("volver", 2.2f, "M19 12H5M11 18l-6-6 6-6")
    val Cerrar = trazo("cerrar", 2.2f, "M6 6l12 12M18 6L6 18")
    val Editar = trazo("editar", 2f, "M4 20h4L19 9l-4-4L4 16v4zM14 6l4 4")
    val Mas = trazo("mas", 2.2f, "M12 5v14M5 12h14")
    val Menos = trazo("menos", 2.2f, "M5 12h14")
    val Arriba = trazo("arriba", 2.2f, "M12 19V5M6 11l6-6 6 6")
    val Abajo = trazo("abajo", 2.2f, "M12 5v14M6 13l6 6 6-6")
    val Tilde = trazo("tilde", 3.5f, "M4 12l6 6L20 6")
    val Desplegar = trazo("desplegar", 2.2f, "M6 9l6 6 6-6")
    val Foto = trazo("foto", 2f, "M3 7h4l2-3h6l2 3h4v13H3z", "M8.5 13a3.5 3.5 0 1 0 7 0a3.5 3.5 0 1 0 -7 0")
    val Llama = trazo("llama", 2f, "M12 21c-4 0-7-2.7-7-6.5C5 10 9 8 9 3c3 1.5 4.5 4 4.5 6.5C15 8.5 15.5 7 15.5 6c2 2 3.5 5 3.5 8.5C19 18.3 16 21 12 21z")

    val Historial = trazo("historial", 2f, "M3 12a9 9 0 1 0 3-6.7", "M3 4v5h5", "M12 7v5l3 2")

    val CorazonVacio = trazo("corazon", 2f, CORAZON)

    val CorazonLleno: ImageVector = ImageVector.Builder(
        name = "corazon_lleno",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).addPath(
        pathData = addPathNodes(CORAZON),
        fill = SolidColor(Color.Black),
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f
    ).build()
}
