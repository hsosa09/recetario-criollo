package uy.horacio.recetariocriollo.dominio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import uy.horacio.recetariocriollo.dominio.modelo.Ingrediente
import uy.horacio.recetariocriollo.dominio.modelo.IngredienteDeReceta
import uy.horacio.recetariocriollo.dominio.modelo.Receta
import uy.horacio.recetariocriollo.dominio.modelo.Unidad

class BuscadorPorIngredientesTest {

    private val papa = Ingrediente(id = 1, nombre = "Papa")
    private val carne = Ingrediente(id = 2, nombre = "Carne picada")
    private val cebolla = Ingrediente(id = 3, nombre = "Cebolla")
    private val sal = Ingrediente(id = 4, nombre = "Sal fina", esSalOEspecia = true, esBasicoDeDespensa = true)
    private val huevo = Ingrediente(id = 5, nombre = "Huevo")

    private fun receta(id: Long, nombre: String, ingredientes: List<Ingrediente>) = Receta(
        id = id,
        nombre = nombre,
        ingredientes = ingredientes.mapIndexed { indice, ingrediente ->
            IngredienteDeReceta(
                id = indice.toLong(),
                ingrediente = ingrediente,
                cantidad = 1.0,
                unidad = Unidad.UNIDAD
            )
        }
    )

    private val pastel = receta(1, "Pastel de papa", listOf(papa, carne, cebolla, sal))
    private val tortilla = receta(2, "Tortilla", listOf(papa, huevo, sal))

    @Test
    fun `marca cocinable lo que tiene todo`() {
        val resultado = BuscadorPorIngredientes.evaluar(tortilla, setOf(papa.id, huevo.id))
        assertTrue(resultado.sePuedeCocinar)
        assertEquals(100, resultado.porcentaje)
        assertTrue(resultado.faltantes.isEmpty())
    }

    @Test
    fun `dice cuantos y cuales faltan`() {
        val resultado = BuscadorPorIngredientes.evaluar(pastel, setOf(papa.id, cebolla.id))
        assertFalse(resultado.sePuedeCocinar)
        assertEquals(listOf("Carne picada"), resultado.faltantes.map { it.nombre })
        assertEquals(75, resultado.porcentaje)
    }

    @Test
    fun `los basicos de despensa se pueden dar por hechos`() {
        val conBasicos = BuscadorPorIngredientes.evaluar(tortilla, setOf(papa.id, huevo.id), true)
        val sinBasicos = BuscadorPorIngredientes.evaluar(tortilla, setOf(papa.id, huevo.id), false)
        assertEquals(100, conBasicos.porcentaje)
        assertEquals(67, sinBasicos.porcentaje)
    }

    @Test
    fun `ordena primero lo que se puede cocinar`() {
        val resultados = BuscadorPorIngredientes.ordenarPorCoincidencia(
            recetas = listOf(pastel, tortilla),
            disponibles = setOf(papa.id, huevo.id)
        )
        assertEquals("Tortilla", resultados.first().receta.nombre)
        assertTrue(resultados.first().porcentaje > resultados.last().porcentaje)
    }

    @Test
    fun `un ingrediente repetido en la receta cuenta una sola vez`() {
        val conRepetido = Receta(
            id = 3,
            nombre = "Flan",
            ingredientes = listOf(
                IngredienteDeReceta(id = 1, ingrediente = huevo, cantidad = 6.0, unidad = Unidad.UNIDAD),
                IngredienteDeReceta(id = 2, ingrediente = huevo, cantidad = 1.0, unidad = Unidad.UNIDAD)
            )
        )
        val resultado = BuscadorPorIngredientes.evaluar(conRepetido, setOf(huevo.id))
        assertEquals(1, resultado.considerados)
        assertEquals(100, resultado.porcentaje)
    }
}
