package uy.horacio.recetariocriollo.datos

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import uy.horacio.recetariocriollo.dominio.modelo.Ajustes
import uy.horacio.recetariocriollo.dominio.modelo.PreferenciaUnidades
import uy.horacio.recetariocriollo.dominio.modelo.Tema

@OptIn(ExperimentalCoroutinesApi::class)
class AjustesRepositorioTest {

    @get:Rule
    val carpeta = TemporaryFolder()

    private fun conAlmacen(bloque: suspend (AjustesRepositorio, androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences>) -> Unit) =
        runTest(UnconfinedTestDispatcher()) {
            val alcance = CoroutineScope(coroutineContext + SupervisorJob())
            val almacen = PreferenceDataStoreFactory.create(scope = alcance) { carpeta.newFile("ajustes.preferences_pb") }
            try {
                bloque(AjustesRepositorio(almacen), almacen)
            } finally {
                alcance.cancel()
            }
        }

    @Test
    fun `sin nada guardado usa los valores por defecto`() = conAlmacen { repositorio, _ ->
        assertEquals(Ajustes(), repositorio.ajustes.first())
    }

    @Test
    fun `guarda y lee cada ajuste`() = conAlmacen { repositorio, _ ->
        repositorio.cambiarTema(Tema.OSCURO)
        repositorio.cambiarModoCocinaPorDefecto(true)
        repositorio.cambiarUnidades(PreferenciaUnidades.DE_COCINA)
        assertEquals(Ajustes(Tema.OSCURO, true, PreferenciaUnidades.DE_COCINA), repositorio.ajustes.first())
    }

    @Test
    fun `un valor desconocido vuelve al por defecto`() = conAlmacen { repositorio, almacen ->
        almacen.edit { it[stringPreferencesKey("tema")] = "SEPIA" }
        assertEquals(Tema.SISTEMA, repositorio.ajustes.first().tema)
    }
}
