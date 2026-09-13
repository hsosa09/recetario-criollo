package uy.horacio.recetariocriollo.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import uy.horacio.recetariocriollo.R

/**
 * Archivo viene empaquetada (fuente variable, licencia OFL en docs/licencias):
 * la app no descarga nada, asi que no puede depender de Google Fonts en tiempo de uso.
 */
@OptIn(ExperimentalTextApi::class)
val Archivo = FontFamily(
    Font(
        R.font.archivo,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400))
    ),
    Font(
        R.font.archivo,
        weight = FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(600))
    ),
    Font(
        R.font.archivo,
        weight = FontWeight.ExtraBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(800))
    )
)

private fun estilo(
    tamanio: Int,
    peso: FontWeight,
    interlineado: Float,
    espaciado: Float = 0f
) = TextStyle(
    fontFamily = Archivo,
    fontWeight = peso,
    fontSize = tamanio.sp,
    lineHeight = (tamanio * interlineado).sp,
    letterSpacing = espaciado.em
)

// Titulos en 800 con tracking negativo, cuerpo en 400 con interlineado amplio.
// El cuerpo queda en 15 sp: se lee desde la mesada con el telefono apoyado.
val TipografiaRecetario = Typography(
    displaySmall = estilo(36, FontWeight.ExtraBold, 1.05f, -0.03f),
    headlineMedium = estilo(28, FontWeight.ExtraBold, 1.1f, -0.03f),
    headlineSmall = estilo(22, FontWeight.ExtraBold, 1.15f, -0.02f),
    titleLarge = estilo(19, FontWeight.ExtraBold, 1.15f, -0.02f),
    titleMedium = estilo(17, FontWeight.ExtraBold, 1.2f, -0.02f),
    titleSmall = estilo(15, FontWeight.ExtraBold, 1.3f),
    bodyLarge = estilo(15, FontWeight.Normal, 1.5f),
    bodyMedium = estilo(13, FontWeight.Normal, 1.5f),
    bodySmall = estilo(12, FontWeight.Normal, 1.4f),
    labelLarge = estilo(14, FontWeight.ExtraBold, 1.2f),
    labelMedium = estilo(12, FontWeight.ExtraBold, 1.2f),
    // Rotulos de seccion: se escriben en mayusculas desde el componente Rotulo.
    labelSmall = estilo(10, FontWeight.ExtraBold, 1.2f, 0.14f)
)
