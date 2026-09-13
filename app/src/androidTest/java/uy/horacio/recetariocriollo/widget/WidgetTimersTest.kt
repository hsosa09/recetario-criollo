package uy.horacio.recetariocriollo.widget

import androidx.glance.appwidget.testing.unit.runGlanceAppWidgetUnitTest
import androidx.glance.testing.unit.hasAnyDescendant
import androidx.glance.testing.unit.hasClickAction
import androidx.glance.testing.unit.hasText
import androidx.glance.testing.unit.hasTextEqualTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.runner.RunWith
import uy.horacio.recetariocriollo.cronometro.Cronometro
import uy.horacio.recetariocriollo.cronometro.EstadoCronometro
import uy.horacio.recetariocriollo.cronometro.WidgetTimers

@RunWith(AndroidJUnit4::class)
class WidgetTimersTest {

    private val contexto = InstrumentationRegistry.getInstrumentation().targetContext
    private val ahora = 1_000_000L

    /** Los botones son una caja tocable con el texto adentro. */
    private fun boton(texto: String) = hasClickAction().and(hasAnyDescendant(hasTextEqualTo(texto)))

    @Test
    fun sinTimersMuestraLosAtajos() = runGlanceAppWidgetUnitTest {
        setContext(contexto)
        provideComposable { ContenidoWidgetTimers(WidgetTimers.contenido(emptyList(), ahora)) }
        onNode(hasText("Nada andando")).assertExists()
        listOf("5 min", "10 min", "15 min").forEach { onNode(boton(it)).assertExists() }
    }

    @Test
    fun cadaEstadoTieneSusBotones() = runGlanceAppWidgetUnitTest {
        setContext(contexto)
        val timers = listOf(
            Cronometro(1, "Torta", 600, finEnMillis = ahora + 300_000),
            Cronometro(2, "Salsa", 600, restanteAlPausar = 200, estado = EstadoCronometro.PAUSADO),
            Cronometro(3, "Arroz", 600, estado = EstadoCronometro.TERMINADO)
        )
        provideComposable { ContenidoWidgetTimers(WidgetTimers.contenido(timers, ahora)) }
        onNode(hasTextEqualTo("Torta")).assertExists()
        onNode(hasText("listo a las")).assertExists()
        onNode(hasTextEqualTo("pausado · 03:20")).assertExists()
        onNode(hasTextEqualTo("¡Listo!")).assertExists()
        onNode(boton("Pausar")).assertExists()
        onNode(boton("Seguir")).assertExists()
        onNode(boton("Quitar")).assertExists()
        onAllNodes(hasTextEqualTo("+1 min")).assertCountEquals(2)
    }

    @Test
    fun resumeLosQueNoEntran() = runGlanceAppWidgetUnitTest {
        setContext(contexto)
        val timers = (1L..5L).map { Cronometro(it, "T$it", 600, finEnMillis = ahora + it * 60_000) }
        provideComposable { ContenidoWidgetTimers(WidgetTimers.contenido(timers, ahora)) }
        onNode(hasTextEqualTo("y 2 más")).assertExists()
        onNode(hasTextEqualTo("T4")).assertDoesNotExist()
    }
}
