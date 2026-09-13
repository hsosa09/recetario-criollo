package uy.horacio.recetariocriollo.dominio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotaDeVozTest {

    @Test
    fun `formato de minutos y segundos redondeando`() {
        assertEquals("0:00", NotaDeVoz.formatear(0))
        assertEquals("0:07", NotaDeVoz.formatear(6_600))
        assertEquals("1:00", NotaDeVoz.formatear(60_000))
        assertEquals("0:00", NotaDeVoz.formatear(-5))
    }

    @Test
    fun `un toque sin querer no es una nota`() {
        assertFalse(NotaDeVoz.esValida(300))
        assertTrue(NotaDeVoz.esValida(1_200))
    }

    @Test
    fun `progreso acotado al maximo`() {
        assertEquals(0.5f, NotaDeVoz.progreso(30_000))
        assertEquals(1f, NotaDeVoz.progreso(90_000))
    }
}
