package uy.horacio.recetariocriollo.datos

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import uy.horacio.recetariocriollo.dominio.modelo.CategoriaReceta

@RunWith(AndroidJUnit4::class)
class CocinadaDaoTest {

    private lateinit var base: RecetarioBaseDatos

    @Before
    fun crear() {
        base = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            RecetarioBaseDatos::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun cerrar() = base.close()

    private suspend fun receta(nombre: String): Long = base.recetaDao().insertarReceta(
        RecetaEntity(
            nombre = nombre, categoria = CategoriaReceta.POSTRE, porcionesBase = 4,
            tiempoMinutos = null, notas = null, fotoPath = null
        )
    )

    @Test
    fun elResumenPromediaCuentaYTomaLaUltimaFecha() = runBlocking {
        val flan = receta("Flan")
        val torta = receta("Torta")
        val dao = base.cocinadaDao()
        dao.insertar(CocinadaEntity(recetaId = flan, fechaMillis = 100, estrellas = 4, porciones = 8))
        dao.insertar(CocinadaEntity(recetaId = flan, fechaMillis = 300, estrellas = 5, porciones = 8))
        dao.insertar(CocinadaEntity(recetaId = torta, fechaMillis = 200, estrellas = 3, porciones = 4))

        val resumen = dao.observarResumenes().first().associateBy { it.recetaId }
        assertEquals(2, resumen[flan]!!.veces)
        assertEquals(4.5, resumen[flan]!!.promedioEstrellas!!, 0.001)
        assertEquals(300L, resumen[flan]!!.ultimaMillis)
        assertEquals(1, resumen[torta]!!.veces)

        val todas = dao.observarTodas().first()
        assertEquals(listOf(300L, 200L, 100L), todas.map { it.cocinada.fechaMillis })
        assertEquals("Torta", todas[1].nombreReceta)
    }

    @Test
    fun borrarLaRecetaBorraSuHistorial() = runBlocking {
        val flan = receta("Flan")
        base.cocinadaDao().insertar(CocinadaEntity(recetaId = flan, fechaMillis = 1, estrellas = 5, porciones = 8))
        base.recetaDao().borrarReceta(flan)
        assertTrue(base.cocinadaDao().observarTodas().first().isEmpty())
    }
}
