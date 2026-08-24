package uy.horacio.recetariocriollo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import uy.horacio.recetariocriollo.ui.navegacion.NavegacionRecetario
import uy.horacio.recetariocriollo.ui.theme.TemaRecetario

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            TemaRecetario {
                NavegacionRecetario()
            }
        }
    }
}
