package uy.horacio.recetariocriollo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import uy.horacio.recetariocriollo.dominio.modelo.Tema
import uy.horacio.recetariocriollo.ui.navegacion.NavegacionRecetario
import uy.horacio.recetariocriollo.ui.theme.TemaRecetario

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        // Fuera de setContent: un operador de Flow dentro de la composición se recrea en cada recomposición.
        val temas = (application as RecetarioApp).contenedor.ajustes.ajustes
            .map { it.tema }
            .distinctUntilChanged()
        setContent {
            val tema by temas.collectAsStateWithLifecycle(initialValue = Tema.SISTEMA)
            val oscuroSistema = isSystemInDarkTheme()
            TemaRecetario(
                oscuro = when (tema) {
                    Tema.SISTEMA -> oscuroSistema
                    Tema.CLARO -> false
                    Tema.OSCURO -> true
                }
            ) {
                NavegacionRecetario()
            }
        }
    }
}
