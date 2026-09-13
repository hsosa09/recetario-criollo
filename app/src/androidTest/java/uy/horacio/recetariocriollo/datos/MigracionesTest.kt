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
}
