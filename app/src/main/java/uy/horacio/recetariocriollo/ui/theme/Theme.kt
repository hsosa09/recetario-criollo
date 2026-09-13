package uy.horacio.recetariocriollo.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val EsquemaClaro = lightColorScheme(
    primary = Acento,
    onPrimary = Papel,
    primaryContainer = Acento100,
    onPrimaryContainer = Acento800,
    secondary = Tinta,
    onSecondary = Papel,
    secondaryContainer = Neutro300,
    onSecondaryContainer = Tinta,
    tertiary = AcentoSecundario,
    onTertiary = Papel,
    tertiaryContainer = Acento100,
    onTertiaryContainer = Acento800,
    background = Papel,
    onBackground = Tinta,
    surface = Papel,
    onSurface = Tinta,
    surfaceVariant = PapelHundido,
    onSurfaceVariant = Tinta.copy(alpha = 0.62f),
    surfaceContainerLowest = Papel,
    surfaceContainerLow = Papel,
    surfaceContainer = PapelHundido,
    surfaceContainerHigh = PapelHundido,
    surfaceContainerHighest = PapelHundido,
    inverseSurface = Tinta,
    inverseOnSurface = Papel,
    inversePrimary = Acento400,
    outline = Tinta.copy(alpha = 0.4f),
    outlineVariant = Tinta.copy(alpha = 0.18f),
    error = Acento700,
    onError = Papel,
    errorContainer = Acento100,
    onErrorContainer = Acento800,
    scrim = Neutro900
)

private val EsquemaOscuro = darkColorScheme(
    primary = Acento,
    onPrimary = TintaNoche,
    primaryContainer = Acento900,
    onPrimaryContainer = Acento400,
    secondary = TintaNoche,
    onSecondary = PapelNoche,
    secondaryContainer = Neutro800,
    onSecondaryContainer = TintaNoche,
    tertiary = Acento400,
    onTertiary = PapelNoche,
    tertiaryContainer = Acento900,
    onTertiaryContainer = Acento400,
    background = PapelNoche,
    onBackground = TintaNoche,
    surface = PapelNoche,
    onSurface = TintaNoche,
    surfaceVariant = PapelNocheElevado,
    onSurfaceVariant = TintaNoche.copy(alpha = 0.66f),
    surfaceContainerLowest = PapelNoche,
    surfaceContainerLow = PapelNoche,
    surfaceContainer = PapelNocheElevado,
    surfaceContainerHigh = PapelNocheElevado,
    surfaceContainerHighest = PapelNocheElevado,
    inverseSurface = TintaNoche,
    inverseOnSurface = PapelNoche,
    inversePrimary = Acento700,
    outline = TintaNoche.copy(alpha = 0.35f),
    outlineVariant = TintaNoche.copy(alpha = 0.18f),
    error = Acento400,
    onError = PapelNoche,
    errorContainer = Acento900,
    onErrorContainer = Acento400,
    scrim = Color.Black
)

/** Colores del sistema que Material no tiene como rol propio. */
@Immutable
data class ColoresExtra(
    /** Rotulos de seccion: el acento en claro, el acento suave en oscuro. */
    val rotulo: Color,
    /** Relleno de imagenes ausentes (la inicial de la receta). */
    val marcador: Color,
    val textoMarcador: Color,
    /** Pista de las barras de progreso. */
    val pista: Color,
    /** Fondo de lo elegido sin llegar a acento lleno (tag, opcion marcada). */
    val acentoTenue: Color,
    val textoAcentoTenue: Color
)

private val ExtraClaro = ColoresExtra(
    rotulo = Acento,
    marcador = Neutro300,
    textoMarcador = Neutro500,
    pista = Neutro300,
    acentoTenue = Acento100,
    textoAcentoTenue = Acento800
)

private val ExtraOscuro = ColoresExtra(
    rotulo = Acento400,
    marcador = Neutro800,
    textoMarcador = Neutro500,
    pista = Neutro800,
    acentoTenue = Acento900,
    textoAcentoTenue = Acento400
)

val LocalColoresExtra = staticCompositionLocalOf { ExtraClaro }

/** Sin radios: el sistema es de filetes y bloques rectos. */
private val FormasRectas = RoundedCornerShape(0)
private val FormasRecetario = Shapes(
    extraSmall = FormasRectas,
    small = FormasRectas,
    medium = FormasRectas,
    large = FormasRectas,
    extraLarge = FormasRectas
)

@Composable
fun TemaRecetario(
    oscuro: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val esquema: ColorScheme = if (oscuro) EsquemaOscuro else EsquemaClaro
    val vista = LocalView.current
    if (!vista.isInEditMode) {
        SideEffect {
            val ventana = (vista.context as Activity).window
            WindowCompat.getInsetsController(ventana, vista).apply {
                isAppearanceLightStatusBars = !oscuro
                isAppearanceLightNavigationBars = !oscuro
            }
        }
    }
    androidx.compose.runtime.CompositionLocalProvider(
        LocalColoresExtra provides if (oscuro) ExtraOscuro else ExtraClaro
    ) {
        MaterialTheme(
            colorScheme = esquema,
            typography = TipografiaRecetario,
            shapes = FormasRecetario,
            content = content
        )
    }
}

/** Atajo para leer los colores extra desde cualquier Composable. */
object RecetarioTema {
    val extra: ColoresExtra
        @Composable get() = LocalColoresExtra.current
}
