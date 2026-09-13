package uy.horacio.recetariocriollo.dominio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TemporadaTest {

    @Test
    fun `rango simple y cruzando fin de anio`() {
        assertEquals(listOf(3, 4, 5), Temporada.meses(Temporada.rango(3, 5)))
        assertEquals(listOf(1, 2, 11, 12), Temporada.meses(Temporada.rango(11, 2)))
        assertEquals((1..12).toList(), Temporada.meses(Temporada.rango(1, 12)))
    }

    @Test
    fun `sin dato cuenta como todo el anio`() {
        assertTrue(Temporada.incluye(Temporada.TODO_EL_ANIO, 7))
        assertTrue(Temporada.meses(Temporada.TODO_EL_ANIO).isEmpty())
    }

    @Test
    fun `incluye y alternar`() {
        val durazno = Temporada.rango(11, 2)
        assertTrue(Temporada.incluye(durazno, 1))
        assertFalse(Temporada.incluye(durazno, 7))
        val conMarzo = Temporada.alternar(durazno, 3)
        assertTrue(Temporada.incluye(conMarzo, 3))
        assertEquals(durazno, Temporada.alternar(conMarzo, 3))
    }

    @Test
    fun `el calendario no deja meses fuera de rango`() {
        Temporada.CALENDARIO_URUGUAY.values.forEach { assertTrue(it in 1 until (1 shl 12)) }
    }
}
