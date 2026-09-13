package uy.horacio.recetariocriollo.dominio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import uy.horacio.recetariocriollo.dominio.modelo.Cocinada
import uy.horacio.recetariocriollo.dominio.modelo.Ingrediente
import uy.horacio.recetariocriollo.dominio.modelo.IngredienteDeReceta
import uy.horacio.recetariocriollo.dominio.modelo.Receta
import uy.horacio.recetariocriollo.dominio.modelo.Unidad
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class EstadisticasTest {

    private val zona = ZoneId.of("America/Montevideo")
    private val huevo = Ingrediente(1, "Huevo")
    private val sal = Ingrediente(2, "Sal fina", esBasicoDeDespensa = true)
    private val harina = Ingrediente(3, "Harina 0000")

    private fun receta(id: Long, nombre: String, vararg ingredientes: Ingrediente) = Receta(
        id = id, nombre = nombre,
        ingredientes = ingredientes.map { IngredienteDeReceta(ingrediente = it, cantidad = 1.0, unidad = Unidad.UNIDAD) }
    )

    private val flan = receta(1, "Flan", huevo, sal)
    private val tortas = receta(2, "Tortas fritas", harina, sal)
    private val pastel = receta(3, "Pastel de papa", huevo)

    // 23:30 del 31/12 en Montevideo ya es 1/1 en UTC: la zona tiene que mandar.
    private fun cocinada(receta: Receta, anio: Int, mes: Int, dia: Int, estrellas: Int, hora: Int = 20) = Cocinada(
        recetaId = receta.id,
        fechaMillis = LocalDateTime.of(anio, mes, dia, hora, 30).atZone(zona).toInstant().toEpochMilli(),
        estrellas = estrellas, porciones = 4
    )

    private val cocinadas = listOf(
        cocinada(flan, 2026, 3, 1, 4),
        cocinada(flan, 2026, 3, 2, 5),
        cocinada(tortas, 2026, 3, 3, 5),
        cocinada(flan, 2026, 7, 10, 3),
        cocinada(tortas, 2026, 12, 31, 5, hora = 23),
        cocinada(pastel, 2025, 5, 5, 5)
    )

    @Test
    fun `cuenta por mes solo el anio pedido`() {
        val e = Estadisticas.calcular(2026, cocinadas, listOf(flan, tortas, pastel), zona)
        assertEquals(5, e.total)
        assertEquals(3, e.porMes[2])
        assertEquals(1, e.porMes[6])
        assertEquals(1, e.porMes[11])
        assertEquals(5, e.diasCocinando)
    }

    @Test
    fun `racha de dias seguidos`() {
        assertEquals(3, Estadisticas.calcular(2026, cocinadas, listOf(flan, tortas), zona).rachaMasLarga)
        val dias = listOf(LocalDate.of(2026, 2, 27), LocalDate.of(2026, 2, 28), LocalDate.of(2026, 3, 1))
        assertEquals(3, Estadisticas.rachaMasLarga(dias))
        assertEquals(0, Estadisticas.rachaMasLarga(emptyList()))
    }

    @Test
    fun `destacados del anio`() {
        val e = Estadisticas.calcular(2026, cocinadas, listOf(flan, tortas, pastel), zona)
        assertEquals("Flan", e.masCocinada!!.nombre)
        assertEquals(3.0, e.masCocinada!!.valor, 0.0)
        assertEquals("Tortas fritas", e.mejorCalificada!!.nombre)
        assertEquals(5.0, e.mejorCalificada!!.valor, 0.0)
        // Huevo en 3 cocinadas (flan); harina en 2; la sal es básico y no cuenta.
        assertEquals("Huevo", e.ingredienteMasUsado!!.nombre)
        assertEquals(listOf("Pastel de papa"), e.sinCocinar.map { it.nombre })
    }

    @Test
    fun `anio sin cocinadas`() {
        val e = Estadisticas.calcular(2024, cocinadas, listOf(flan), zona)
        assertEquals(0, e.total)
        assertNull(e.masCocinada)
        assertNull(e.ingredienteMasUsado)
        assertEquals(listOf(2026, 2025), Estadisticas.anios(cocinadas, 2026, zona))
    }
}
