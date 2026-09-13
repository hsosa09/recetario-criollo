package uy.horacio.recetariocriollo.ui.recetas

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import uy.horacio.recetariocriollo.dominio.modelo.Ingrediente
import uy.horacio.recetariocriollo.dominio.modelo.ReglaEscalado
import uy.horacio.recetariocriollo.dominio.modelo.Unidad

class LineaIngredienteTest {

    private fun linea(cantidad: String, unidad: Unidad = Unidad.GRAMO) = LineaIngrediente(
        idLocal = 1,
        ingrediente = Ingrediente(id = 1, nombre = "Harina 0000"),
        cantidad = cantidad,
        unidad = unidad,
        regla = ReglaEscalado.LINEAL,
        aclaracion = ""
    )

    @Test
    fun `acepta como escribe la gente`() {
        listOf("250", "2,5", "2.5", "1/2", "1 1/2").forEach { texto ->
            assertTrue(texto, linea(texto).tieneCantidadValida())
        }
    }

    @Test
    fun `rechaza lo ilegible en vez de guardarlo como cero`() {
        // Issue #4
        listOf("", "  ", "dos", "2..5", "1/0", "0", "-3").forEach { texto ->
            assertFalse(texto, linea(texto).tieneCantidadValida())
        }
    }

    @Test
    fun `a gusto puede quedar sin numero`() {
        assertTrue(linea("", Unidad.A_GUSTO).tieneCantidadValida())
    }
}
