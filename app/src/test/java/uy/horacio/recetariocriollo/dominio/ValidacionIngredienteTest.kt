package uy.horacio.recetariocriollo.dominio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import uy.horacio.recetariocriollo.dominio.ValidacionIngrediente.Problema
import uy.horacio.recetariocriollo.dominio.modelo.Ingrediente

class ValidacionIngredienteTest {

    private val catalogo = listOf(
        Ingrediente(id = 1, nombre = "Azúcar"),
        Ingrediente(id = 2, nombre = "Sal fina")
    )

    @Test
    fun `nombre vacio o repetido sin importar tildes`() {
        assertEquals(Problema.NOMBRE_VACIO, ValidacionIngrediente.validar("  ", "", catalogo))
        assertEquals(Problema.NOMBRE_REPETIDO, ValidacionIngrediente.validar("azucar", "", catalogo))
        assertEquals(Problema.NOMBRE_REPETIDO, ValidacionIngrediente.validar(" SAL  FINA ", "", catalogo))
    }

    @Test
    fun `editar sin cambiar el nombre no choca consigo mismo`() {
        assertNull(ValidacionIngrediente.validar("Azúcar", "200", catalogo, idPropio = 1))
        assertEquals(Problema.NOMBRE_REPETIDO, ValidacionIngrediente.validar("Sal fina", "", catalogo, idPropio = 1))
    }

    @Test
    fun `la densidad es opcional pero tiene que ser razonable`() {
        assertNull(ValidacionIngrediente.validar("Harina 000", "", catalogo))
        assertNull(ValidacionIngrediente.validar("Harina 000", "120,5", catalogo))
        assertEquals(Problema.DENSIDAD_INVALIDA, ValidacionIngrediente.validar("Harina 000", "mucha", catalogo))
        assertEquals(Problema.DENSIDAD_INVALIDA, ValidacionIngrediente.validar("Harina 000", "0", catalogo))
        assertEquals(Problema.DENSIDAD_INVALIDA, ValidacionIngrediente.validar("Harina 000", "5000", catalogo))
    }

    @Test
    fun `usos cuenta recetas distintas`() {
        val usos = ValidacionIngrediente.usos(listOf(listOf(1L, 1L, 2L), listOf(1L)))
        assertEquals(2, usos[1L])
        assertEquals(1, usos[2L])
    }
}
