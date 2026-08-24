package uy.horacio.recetariocriollo

import android.app.Application
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import uy.horacio.recetariocriollo.cronometro.GestorCronometros
import uy.horacio.recetariocriollo.datos.AlmacenFotos
import uy.horacio.recetariocriollo.datos.IngredienteRepositorio
import uy.horacio.recetariocriollo.datos.RecetaRepositorio
import uy.horacio.recetariocriollo.datos.RecetarioBaseDatos

/**
 * Contenedor de dependencias hecho a mano. La app es chica y 100% local:
 * no justifica traer un framework de inyeccion.
 */
class Contenedor(contexto: Context) {

    private val alcance = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val baseDatos = RecetarioBaseDatos.obtener(contexto, alcance)

    val almacenFotos = AlmacenFotos(contexto)

    val recetas = RecetaRepositorio(baseDatos.recetaDao(), almacenFotos)

    val ingredientes = IngredienteRepositorio(baseDatos.ingredienteDao(), baseDatos.recetaDao())

    val cronometros: GestorCronometros = GestorCronometros.obtener(contexto)
}

class RecetarioApp : Application() {
    lateinit var contenedor: Contenedor
        private set

    override fun onCreate() {
        super.onCreate()
        contenedor = Contenedor(this)
    }
}
