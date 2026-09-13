package uy.horacio.recetariocriollo.datos

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Cada migracion se corre contra el esquema exportado en app/schemas. */
@RunWith(AndroidJUnit4::class)
class MigracionesTest {

    private val nombreBase = "migracion-test.db"

    @get:Rule
    val ayudante = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        RecetarioBaseDatos::class.java
    )

    @Test
    fun de1a2ConservaLasRecetasYSumaElHistorial() {
        ayudante.createDatabase(nombreBase, 1).apply {
            execSQL(
                "INSERT INTO recetas (id, nombre, categoria, porcionesBase, tiempoMinutos, notas, fotoPath, esFavorita, creadaEn, modificadaEn) " +
                    "VALUES (7, 'Flan casero', 'POSTRE', 8, 90, 'A baño María', NULL, 1, 0, 0)"
            )
            execSQL(
                "INSERT INTO pasos (recetaId, orden, texto, timerSugeridoSegundos) VALUES (7, 0, 'Hacer el caramelo', 420)"
            )
            close()
        }

        // Valida que la tabla migrada coincida exactamente con el esquema 2 de Room.
        ayudante.runMigrationsAndValidate(nombreBase, 2, true, Migraciones.DE_1_A_2).close()

        val contexto = InstrumentationRegistry.getInstrumentation().targetContext
        val base = Room.databaseBuilder(contexto, RecetarioBaseDatos::class.java, nombreBase)
            .addMigrations(*Migraciones.TODAS)
            .build()
        try {
            runBlocking {
                val receta = base.recetaDao().obtenerPorId(7)!!.aDominio()
                assertEquals("Flan casero", receta.nombre)
                assertEquals(true, receta.esFavorita)
                assertEquals(1, receta.pasos.size)
                assertNull(receta.dificultad)
                assertNull(receta.moldeCm)

                base.cocinadaDao().insertar(CocinadaEntity(recetaId = 7, fechaMillis = 1, estrellas = 5, porciones = 8))
                assertEquals(1, base.cocinadaDao().observarDeReceta(7).first().size)
            }
        } finally {
            base.close()
        }
    }

    @Test
    fun de2a3CompletaLasEstacionesYNoPisaLoEditado() {
        ayudante.createDatabase(nombreBase, 2).apply {
            execSQL("INSERT INTO ingredientes (id, nombre, categoria, densidadGramosPorTaza, esSalOEspecia, esBasicoDeDespensa, unidadHabitual) VALUES (1, 'Zapallito', 'VERDURAS', NULL, 0, 0, 'UNIDAD')")
            execSQL("INSERT INTO ingredientes (id, nombre, categoria, densidadGramosPorTaza, esSalOEspecia, esBasicoDeDespensa, unidadHabitual) VALUES (2, 'Queso rallado', 'LACTEOS', 100, 0, 0, 'GRAMO')")
            execSQL(
                "INSERT INTO recetas (id, nombre, categoria, porcionesBase, tiempoMinutos, notas, fotoPath, esFavorita, dificultad, moldeCm, creadaEn, modificadaEn) " +
                    "VALUES (3, 'Tarta', 'PLATO_PRINCIPAL', 6, 50, NULL, NULL, 0, 'MEDIA', 24, 0, 0)"
            )
            execSQL("INSERT INTO cocinadas (recetaId, fechaMillis, estrellas, porciones, nota) VALUES (3, 10, 4, 6, 'rica')")
            close()
        }

        ayudante.runMigrationsAndValidate(nombreBase, 3, true, Migraciones.DE_2_A_3).use { db ->
            db.query("SELECT meses FROM ingredientes WHERE nombre = 'Zapallito'").use {
                it.moveToFirst()
                assertEquals(uy.horacio.recetariocriollo.dominio.Temporada.rango(11, 3), it.getInt(0))
            }
            db.query("SELECT meses FROM ingredientes WHERE nombre = 'Queso rallado'").use {
                it.moveToFirst()
                assertEquals(0, it.getInt(0))
            }
            db.query("SELECT origenId, dificultad FROM recetas WHERE id = 3").use {
                it.moveToFirst()
                assertEquals(true, it.isNull(0))
                assertEquals("MEDIA", it.getString(1))
            }
            db.query("SELECT nota, fotoPath, audioPath FROM cocinadas").use {
                it.moveToFirst()
                assertEquals("rica", it.getString(0))
                assertEquals(true, it.isNull(1) && it.isNull(2))
            }
        }
    }

    @Test
    fun cadenaCompletaDe1a3() {
        ayudante.createDatabase(nombreBase, 1).apply {
            execSQL(
                "INSERT INTO recetas (id, nombre, categoria, porcionesBase, tiempoMinutos, notas, fotoPath, esFavorita, creadaEn, modificadaEn) " +
                    "VALUES (1, 'Tortas fritas', 'PANIFICADOS', 12, 60, NULL, NULL, 0, 0, 0)"
            )
            close()
        }
        ayudante.runMigrationsAndValidate(nombreBase, 3, true, *Migraciones.TODAS).use { db ->
            db.query("SELECT nombre FROM recetas").use {
                it.moveToFirst()
                assertEquals("Tortas fritas", it.getString(0))
            }
        }
    }

    @Test
    fun borrarLaOriginalDejaLasVariantesSueltas() {
        val contexto = InstrumentationRegistry.getInstrumentation().targetContext
        val base = Room.inMemoryDatabaseBuilder(contexto, RecetarioBaseDatos::class.java).allowMainThreadQueries().build()
        try {
            runBlocking {
                val dao = base.recetaDao()
                fun receta(nombre: String, origen: Long?) = RecetaEntity(
                    nombre = nombre, categoria = uy.horacio.recetariocriollo.dominio.modelo.CategoriaReceta.POSTRE,
                    porcionesBase = 8, tiempoMinutos = null, notas = null, fotoPath = null, origenId = origen
                )
                val original = dao.insertarReceta(receta("Flan", null))
                val variante = dao.insertarReceta(receta("Flan con dulce de leche", original))
                dao.borrarConVariantesSueltas(original)
                val quedó = dao.obtenerPorId(variante)!!.receta
                assertNull(quedó.origenId)
                assertNull(dao.obtenerPorId(original))
            }
        } finally {
            base.close()
        }
    }
}
