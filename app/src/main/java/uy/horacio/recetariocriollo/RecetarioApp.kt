package uy.horacio.recetariocriollo

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import uy.horacio.recetariocriollo.cronometro.GestorCronometros
import uy.horacio.recetariocriollo.datos.AjustesRepositorio
import uy.horacio.recetariocriollo.datos.AlmacenFotos
import uy.horacio.recetariocriollo.datos.CocinadaRepositorio
import uy.horacio.recetariocriollo.datos.IngredienteRepositorio
import uy.horacio.recetariocriollo.datos.RecetaRepositorio
import uy.horacio.recetariocriollo.datos.RecetarioBaseDatos

/** Un solo DataStore por proceso: el delegado garantiza la instancia única. */
private val Context.almacenAjustes by preferencesDataStore(name = "ajustes")

/**
 * Contenedor de dependencias hecho a mano. La app es chica y 100% local:
 * no justifica traer un framework de inyeccion.
 */
class Contenedor(contexto: Context) {

    private val alcance = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val baseDatos = RecetarioBaseDatos.obtener(contexto, alcance)

    val almacenFotos = AlmacenFotos(contexto)

    val recetas = RecetaRepositorio(baseDatos.recetaDao(), baseDatos.cocinadaDao(), almacenFotos)

    val ingredientes = IngredienteRepositorio(baseDatos.ingredienteDao(), baseDatos.recetaDao())

    val cocinadas = CocinadaRepositorio(baseDatos.cocinadaDao(), almacenFotos)

    val ajustes = AjustesRepositorio(contexto.almacenAjustes)

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
