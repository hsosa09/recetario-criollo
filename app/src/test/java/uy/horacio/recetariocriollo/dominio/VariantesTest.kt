package uy.horacio.recetariocriollo.dominio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import uy.horacio.recetariocriollo.dominio.modelo.Dificultad
import uy.horacio.recetariocriollo.dominio.modelo.Ingrediente
import uy.horacio.recetariocriollo.dominio.modelo.IngredienteDeReceta
import uy.horacio.recetariocriollo.dominio.modelo.PasoPreparacion
import uy.horacio.recetariocriollo.dominio.modelo.Receta
import uy.horacio.recetariocriollo.dominio.modelo.Unidad

class VariantesTest {

    private val huevo = Ingrediente(1, "Huevo")
    private val leche = Ingrediente(2, "Leche")
    private val azucar = Ingrediente(3, "Azúcar")
    private val dulce = Ingrediente(4, "Dulce de leche")

    private fun linea(i: Ingrediente, cantidad: Double, unidad: Unidad = Unidad.GRAMO, aclaracion: String? = null) =
        IngredienteDeReceta(ingrediente = i, cantidad = cantidad, unidad = unidad, aclaracion = aclaracion)

    private fun pasos(vararg textos: String) = textos.mapIndexed { i, t -> PasoPreparacion(orden = i, texto = t) }

    private val flan = Receta(
        id = 1, nombre = "Flan", porcionesBase = 8, dificultad = Dificultad.MEDIA,
        ingredientes = listOf(linea(huevo, 6.0, Unidad.UNIDAD), linea(leche, 1.0, Unidad.LITRO), linea(azucar, 200.0), linea(azucar, 150.0, aclaracion = "caramelo")),
        pasos = pasos("Hacer el caramelo.", "Batir los huevos.", "Cocinar a baño María.")
    )

    @Test
    fun `diff generico por LCS`() {
        val cambios = Variantes.diff(listOf("a", "b", "c", "d"), listOf("a", "c", "d", "e")) { x, y -> x == y }
        assertEquals(
            listOf(Cambio.Igual("a"), Cambio.Quitado("b"), Cambio.Igual("c"), Cambio.Igual("d"), Cambio.Agregado("e")),
            cambios
        )
    }

    @Test
    fun `detecta agregados quitados y modificados`() {
        val conDulce = flan.copy(
            id = 2, origenId = 1, porcionesBase = 10,
            ingredientes = listOf(linea(huevo, 6.0, Unidad.UNIDAD), linea(azucar, 150.0), linea(azucar, 150.0, aclaracion = "caramelo"), linea(dulce, 250.0)),
            pasos = pasos("Hacer el caramelo.", "Batir los huevos con el dulce de leche.", "Cocinar a baño maría.")
        )
        val c = Variantes.comparar(flan, conDulce)
        assertTrue(c.hayDiferencias)
        assertEquals(setOf(ComparacionRecetas.Dato.PORCIONES), c.datosDistintos)

        val tipos = c.ingredientes.map { it::class.simpleName to nombre(it) }
        assertEquals(
            listOf("Igual" to "Huevo", "Quitado" to "Leche", "Modificado" to "Azúcar", "Igual" to "Azúcar", "Agregado" to "Dulce de leche"),
            tipos
        )
        // Mayúsculas y tildes no cuentan como cambio de paso; el texto distinto sí.
        assertEquals(listOf("Igual", "Modificado", "Igual"), c.pasos.map { it::class.simpleName })
    }

    @Test
    fun `una copia exacta no tiene diferencias`() {
        assertFalse(Variantes.comparar(flan, flan.copy(id = 2, origenId = 1)).hayDiferencias)
    }

    @Test
    fun `familia y raiz`() {
        val v1 = flan.copy(id = 2, nombre = "Flan de dulce", origenId = 1)
        val v2 = flan.copy(id = 3, nombre = "Flan de coco", origenId = 1)
        val otra = flan.copy(id = 4, nombre = "Budín", origenId = null)
        assertEquals(1L, Variantes.raiz(v2))
        assertEquals(listOf("Flan", "Flan de coco", "Flan de dulce"), Variantes.familia(v1, listOf(otra, v1, flan, v2)).map { it.nombre })
        assertEquals(listOf("Budín"), Variantes.familia(otra, listOf(otra, v1)).map { it.nombre })
    }

    private fun nombre(c: Cambio<IngredienteDeReceta>) = when (c) {
        is Cambio.Igual -> c.valor.ingrediente.nombre
        is Cambio.Agregado -> c.valor.ingrediente.nombre
        is Cambio.Quitado -> c.valor.ingrediente.nombre
        is Cambio.Modificado -> c.despues.ingrediente.nombre
    }
}
