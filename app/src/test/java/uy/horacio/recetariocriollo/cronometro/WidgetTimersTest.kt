package uy.horacio.recetariocriollo.cronometro

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetTimersTest {

    private val ahora = 1_000_000L

    private fun corriendo(id: Long, faltanSeg: Int) = Cronometro(id, "t$id", 600, finEnMillis = ahora + faltanSeg * 1000L)
    private fun pausado(id: Long, quedan: Int) = Cronometro(id, "p$id", 600, restanteAlPausar = quedan, estado = EstadoCronometro.PAUSADO)
    private fun terminado(id: Long) = Cronometro(id, "f$id", 600, estado = EstadoCronometro.TERMINADO)

    @Test
    fun `corriendo primero por el que termina antes, despues pausados y terminados`() {
        val c = WidgetTimers.contenido(listOf(terminado(1), pausado(2, 30), corriendo(3, 300), corriendo(4, 60)), ahora, maximo = 4)
        assertEquals(listOf(4L, 3L, 2L, 1L), c.filas.map { it.id })
        assertEquals(60, c.filas.first().restanteSegundos)
        assertEquals(0, c.restantes)
    }

    @Test
    fun `resume los que no entran`() {
        val c = WidgetTimers.contenido((1L..5L).map { corriendo(it, it.toInt() * 10) }, ahora, maximo = 3)
        assertEquals(3, c.filas.size)
        assertEquals(2, c.restantes)
    }

    @Test
    fun `sin timers esta vacio`() {
        assertTrue(WidgetTimers.contenido(emptyList(), ahora).vacio)
    }
}
