package uy.horacio.recetariocriollo.dominio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import uy.horacio.recetariocriollo.dominio.modelo.CategoriaIngrediente
import uy.horacio.recetariocriollo.dominio.modelo.Ingrediente
import uy.horacio.recetariocriollo.dominio.modelo.IngredienteDeReceta
import uy.horacio.recetariocriollo.dominio.modelo.Receta
import uy.horacio.recetariocriollo.dominio.modelo.Unidad

class EstacionalidadTest {

    private fun ing(id: Long, nombre: String, categoria: CategoriaIngrediente, meses: Int = 0) =
        IngredienteDeReceta(ingrediente = Ingrediente(id, nombre, categoria, meses = meses), cantidad = 1.0, unidad = Unidad.UNIDAD)

    private val tomate = ing(1, "Tomate", CategoriaIngrediente.VERDURAS, Temporada.rango(12, 4))
    private val zapallo = ing(2, "Zapallo", CategoriaIngrediente.VERDURAS, Temporada.rango(2, 7))
    private val papa = ing(3, "Papa", CategoriaIngrediente.VERDURAS)
    private val carne = ing(4, "Carne", CategoriaIngrediente.CARNES)

    private fun receta(vararg items: IngredienteDeReceta) = Receta(nombre = "x", ingredientes = items.toList())

    @Test
    fun `todas las de temporada tienen que estar en el mes`() {
        val ensalada = receta(tomate, papa)
        assertTrue(Estacionalidad.esDeEstacion(ensalada, 1))
        assertFalse(Estacionalidad.esDeEstacion(ensalada, 7))
        val guiso = receta(tomate, zapallo)
        assertTrue(Estacionalidad.esDeEstacion(guiso, 3))
        assertFalse(Estacionalidad.esDeEstacion(guiso, 12))
        assertEquals(listOf("Zapallo"), Estacionalidad.fueraDeEstacion(guiso, 12).map { it.nombre })
    }

    @Test
    fun `sin verduras ni frutas con dato no es de estacion`() {
        assertFalse(Estacionalidad.esDeEstacion(receta(papa, carne), 1))
        assertFalse(Estacionalidad.esDeEstacion(receta(), 1))
    }

    @Test
    fun `solo cuentan verduras y frutas`() {
        val quesoConMeses = ing(5, "Queso", CategoriaIngrediente.LACTEOS, Temporada.rango(1, 1))
        assertTrue(Estacionalidad.esDeEstacion(receta(tomate, quesoConMeses), 3))
    }
}
