package uy.horacio.recetariocriollo.datos

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(
    entities = [
        RecetaEntity::class,
        IngredienteEntity::class,
        RecetaIngredienteEntity::class,
        PasoEntity::class,
        CocinadaEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(Convertidores::class)
abstract class RecetarioBaseDatos : RoomDatabase() {

    abstract fun recetaDao(): RecetaDao
    abstract fun ingredienteDao(): IngredienteDao
    abstract fun cocinadaDao(): CocinadaDao

    companion object {
        private const val NOMBRE_ARCHIVO = "recetario.db"

        @Volatile
        private var instancia: RecetarioBaseDatos? = null

        fun obtener(contexto: Context, alcance: CoroutineScope): RecetarioBaseDatos =
            instancia ?: synchronized(this) {
                instancia ?: construir(contexto.applicationContext, alcance).also { instancia = it }
            }

        private fun construir(contexto: Context, alcance: CoroutineScope): RecetarioBaseDatos =
            Room.databaseBuilder(contexto, RecetarioBaseDatos::class.java, NOMBRE_ARCHIVO)
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        // La primera vez se carga el catalogo de ingredientes y unas recetas de muestra.
                        alcance.launch {
                            instancia?.let { Sembrador.sembrar(it) }
                        }
                    }
                })
                .addMigrations(*Migraciones.TODAS)
                .build()
    }
}

/** Carga inicial del catalogo normalizado y de las recetas de ejemplo. */
object Sembrador {

    suspend fun sembrar(baseDatos: RecetarioBaseDatos) {
        val ingredienteDao = baseDatos.ingredienteDao()
        val recetaDao = baseDatos.recetaDao()

        if (ingredienteDao.cantidad() == 0) {
            ingredienteDao.insertarTodos(CatalogoInicial.ingredientes)
        }
        if (recetaDao.cantidadRecetas() > 0) return

        val porNombre = mutableMapOf<String, Long>()
        RecetasIniciales.recetas.forEach { semilla ->
            val cabecera = RecetaEntity(
                nombre = semilla.nombre,
                categoria = semilla.categoria,
                porcionesBase = semilla.porciones,
                tiempoMinutos = semilla.tiempoMinutos,
                notas = semilla.notas,
                fotoPath = null
            )
            val filas = semilla.ingredientes.mapNotNull { item ->
                val id = porNombre.getOrPut(item.nombre) {
                    ingredienteDao.buscarPorNombre(item.nombre)?.id ?: -1L
                }
                if (id <= 0) return@mapNotNull null
                RecetaIngredienteEntity(
                    recetaId = 0,
                    ingredienteId = id,
                    cantidad = item.cantidad,
                    unidad = item.unidad,
                    regla = item.regla,
                    aclaracion = item.aclaracion
                )
            }
            val pasos = semilla.pasos.mapIndexed { indice, paso ->
                PasoEntity(
                    recetaId = 0,
                    orden = indice,
                    texto = paso.texto,
                    timerSugeridoSegundos = paso.timerSegundos
                )
            }
            recetaDao.guardarCompleta(cabecera, filas, pasos)
        }
    }
}
