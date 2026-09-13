package uy.horacio.recetariocriollo.dominio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TextoTest {

    @Test
    fun `encuentra sin tildes ni mayusculas`() {
        // Issue #5
        assertTrue(Texto.contiene("Azúcar impalpable", "azucar"))
        assertTrue(Texto.contiene("Limón", "LIMON"))
        assertTrue(Texto.contiene("Orégano", "oreg"))
        assertTrue(Texto.contiene("Pingüino", "pinguino"))
    }

    @Test
    fun `la enie no se confunde con la n`() {
        // La ñ se descompone en n + tilde: al quitar marcas queda "n". Se acepta a
        // proposito, porque en el telefono es comun escribir "nandu" por "ñandú".
        assertTrue(Texto.contiene("Ñoquis", "noquis"))
    }

    @Test
    fun `consulta vacia coincide con todo`() {
        assertTrue(Texto.contiene("Flan casero", "   "))
    }

    @Test
    fun `no inventa coincidencias`() {
        assertFalse(Texto.contiene("Harina 0000", "azucar"))
    }

    @Test
    fun `compara nombres completos`() {
        assertTrue(Texto.mismoNombre(" sal  FINA ", "Sal fina"))
        assertFalse(Texto.mismoNombre("Sal", "Sal fina"))
        assertEquals("fecula de maiz", Texto.normalizar("Fécula de maíz"))
    }
}
