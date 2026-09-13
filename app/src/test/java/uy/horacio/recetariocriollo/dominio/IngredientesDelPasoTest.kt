package uy.horacio.recetariocriollo.dominio

import org.junit.Assert.assertEquals
import org.junit.Test
import uy.horacio.recetariocriollo.dominio.modelo.Ingrediente

class IngredientesDelPasoTest {

    private val harina = Ingrediente(id = 1, nombre = "Harina 0000")
    private val sal = Ingrediente(id = 2, nombre = "Sal fina")
    private val papa = Ingrediente(id = 3, nombre = "Papa")
    private val azucar = Ingrediente(id = 4, nombre = "Azúcar")
    private val salsa = Ingrediente(id = 5, nombre = "Salsa de tomate")
    private val todos = listOf(harina, sal, papa, azucar, salsa)

    @Test
    fun `reconoce por la primera palabra del nombre`() {
        val ids = IngredientesDelPaso.mencionados("Poner la harina en un bol con la sal.", todos)
        assertEquals(setOf(1L, 2L), ids)
    }

    @Test
    fun `admite plural y tildes`() {
        assertEquals(setOf(3L), IngredientesDelPaso.mencionados("Pelar y hervir las papas.", todos))
        assertEquals(setOf(4L), IngredientesDelPaso.mencionados("Batir con el AZUCAR.", todos))
    }

    @Test
    fun `no confunde palabras que empiezan igual`() {
        // "sal" no esta dentro de "salpimentar", ni "salsa" es "sal".
        assertEquals(emptySet<Long>(), IngredientesDelPaso.mencionados("Salpimentar a gusto.", listOf(sal)))
        assertEquals(setOf(5L), IngredientesDelPaso.mencionados("Sumar la salsa.", todos))
    }

    @Test
    fun `ordena primero lo mencionado y respeta el resto`() {
        val orden = IngredientesDelPaso.ordenar("Agregar la papa y el azucar", todos) { it }
        assertEquals(listOf(papa, azucar, harina, sal, salsa), orden)
    }
}
