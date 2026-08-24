package uy.horacio.recetariocriollo.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val EsquemaClaro = lightColorScheme(
    primary = Terracota,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = TerracotaClaro,
    onPrimaryContainer = TerracotaOscuro,
    secondary = Oliva,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    secondaryContainer = OlivaClaro,
    onSecondaryContainer = OlivaOscuro,
    tertiary = Dorado,
    onTertiary = androidx.compose.ui.graphics.Color.White,
    tertiaryContainer = DoradoClaro,
    onTertiaryContainer = DoradoOscuro,
    background = FondoCrema,
    onBackground = TextoPrincipal,
    surface = SuperficieCrema,
    onSurface = TextoPrincipal,
    surfaceVariant = SuperficieVariante,
    onSurfaceVariant = TextoVariante,
    error = RojoError,
    errorContainer = RojoErrorClaro
)

private val EsquemaOscuro = darkColorScheme(
    primary = TerracotaSuave,
    onPrimary = TerracotaOscuro,
    primaryContainer = Terracota,
    onPrimaryContainer = TerracotaClaro,
    secondary = OlivaSuave,
    onSecondary = OlivaOscuro,
    secondaryContainer = Oliva,
    onSecondaryContainer = OlivaClaro,
    tertiary = DoradoSuave,
    onTertiary = DoradoOscuro,
    tertiaryContainer = Dorado,
    onTertiaryContainer = DoradoClaro,
    background = FondoOscuro,
    onBackground = TextoClaro,
    surface = SuperficieOscura,
    onSurface = TextoClaro,
    surfaceVariant = SuperficieVarianteOscura,
    onSurfaceVariant = TextoVarianteClaro,
    error = RojoErrorOscuro,
    errorContainer = RojoErrorContenedorOscuro
)

@Composable
fun TemaRecetario(
    oscuro: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val esquema = if (oscuro) EsquemaOscuro else EsquemaClaro
    val vista = LocalView.current
    if (!vista.isInEditMode) {
        SideEffect {
            val ventana = (vista.context as Activity).window
            WindowCompat.getInsetsController(ventana, vista).isAppearanceLightStatusBars = !oscuro
        }
    }
    MaterialTheme(
        colorScheme = esquema,
        typography = TipografiaRecetario,
        content = content
    )
}
