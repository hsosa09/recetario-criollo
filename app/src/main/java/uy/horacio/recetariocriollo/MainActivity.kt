package uy.horacio.recetariocriollo

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import uy.horacio.recetariocriollo.dominio.modelo.Tema
import uy.horacio.recetariocriollo.ui.navegacion.Atajo
import uy.horacio.recetariocriollo.ui.navegacion.Atajos
import uy.horacio.recetariocriollo.ui.navegacion.NavegacionRecetario
import uy.horacio.recetariocriollo.ui.theme.TemaRecetario

class MainActivity : ComponentActivity() {

    /** Atajo pedido por un acceso directo o el widget, hasta que la navegación lo atiende. */
    private val atajoPendiente = MutableStateFlow<Atajo?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        // Al recrear la actividad (rotación) el intent es el mismo: no se repite el atajo.
        if (savedInstanceState == null) atajoPendiente.value = Atajos.leer(intent?.dataString)
        // Fuera de setContent: un operador de Flow dentro de la composición se recrea en cada recomposición.
        val temas = (application as RecetarioApp).contenedor.ajustes.ajustes
            .map { it.tema }
            .distinctUntilChanged()
        setContent {
            val tema by temas.collectAsStateWithLifecycle(initialValue = Tema.SISTEMA)
            val atajo by atajoPendiente.collectAsStateWithLifecycle()
            val oscuroSistema = isSystemInDarkTheme()
            TemaRecetario(
                oscuro = when (tema) {
                    Tema.SISTEMA -> oscuroSistema
                    Tema.CLARO -> false
                    Tema.OSCURO -> true
                }
            ) {
                NavegacionRecetario(atajo = atajo, alAtenderAtajo = { atajoPendiente.value = null })
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Atajos.leer(intent.dataString)?.let { atajoPendiente.value = it }
    }
}
