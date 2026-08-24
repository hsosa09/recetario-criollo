package uy.horacio.recetariocriollo.dominio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import uy.horacio.recetariocriollo.dominio.modelo.Unidad

class ConversorTest {

    @Test
    fun `convierte dentro del volumen`() {
        assertEquals(240.0, Conversor.convertir(1.0, Unidad.TAZA, Unidad.MILILITRO)!!, 0.001)
        assertEquals(3.0, Conversor.convertir(1.0, Unidad.CUCHARADA, Unidad.CUCHARADITA)!!, 0.001)
        assertEquals(16.0, Conversor.convertir(1.0, Unidad.TAZA, Unidad.CUCHARADA)!!, 0.001)
        assertEquals(0.5, Conversor.convertir(500.0, Unidad.MILILITRO, Unidad.LITRO)!!, 0.001)
    }

    @Test
    fun `convierte dentro del peso`() {
        assertEquals(1000.0, Conversor.convertir(1.0, Unidad.KILOGRAMO, Unidad.GRAMO)!!, 0.001)
        assertEquals(453.592, Conversor.convertir(1.0, Unidad.LIBRA, Unidad.GRAMO)!!, 0.01)
        assertEquals(16.0, Conversor.convertir(1.0, Unidad.LIBRA, Unidad.ONZA)!!, 0.01)
    }

    @Test
    fun `una taza de harina y una de azucar no pesan lo mismo`() {
        val harina = Conversor.convertir(1.0, Unidad.TAZA, Unidad.GRAMO, gramosPorTaza = 120.0)!!
        val azucar = Conversor.convertir(1.0, Unidad.TAZA, Unidad.GRAMO, gramosPorTaza = 200.0)!!
        assertEquals(120.0, harina, 0.001)
        assertEquals(200.0, azucar, 0.001)
    }

    @Test
    fun `sin densidad no se puede cruzar volumen y peso`() {
        assertNull(Conversor.convertir(1.0, Unidad.TAZA, Unidad.GRAMO))
        assertTrue(Conversor.necesitaDensidad(Unidad.TAZA, Unidad.GRAMO))
        assertTrue(!Conversor.necesitaDensidad(Unidad.TAZA, Unidad.MILILITRO))
    }

    @Test
    fun `de gramos a tazas usando la densidad`() {
        val tazas = Conversor.convertir(240.0, Unidad.GRAMO, Unidad.TAZA, gramosPorTaza = 120.0)!!
        assertEquals(2.0, tazas, 0.001)
    }

    @Test
    fun `temperaturas de horno`() {
        assertEquals(356.0, Conversor.celsiusAFahrenheit(180.0), 0.001)
        assertEquals(180.0, Conversor.fahrenheitACelsius(356.0), 0.001)
        assertEquals(32.0, Conversor.celsiusAFahrenheit(0.0), 0.001)
    }

    @Test
    fun `traduce los hornos de las recetas viejas`() {
        assertEquals(NivelHorno.MODERADO, Conversor.nivelDeHorno(180.0))
        assertEquals(NivelHorno.SUAVE, Conversor.nivelDeHorno(150.0))
        assertEquals(NivelHorno.FUERTE, Conversor.nivelDeHorno(210.0))
        assertEquals(NivelHorno.MUY_FUERTE, Conversor.nivelDeHorno(250.0))
        assertNull(Conversor.nivelDeHorno(40.0))
    }

    @Test
    fun `la levadura seca es un tercio de la fresca`() {
        assertEquals(10.0, Conversor.levaduraFrescaASeca(30.0), 0.001)
        assertEquals(30.0, Conversor.levaduraSecaAFresca(10.0), 0.001)
    }

    @Test
    fun `las unidades de conteo no se convierten`() {
        assertNull(Conversor.convertir(2.0, Unidad.UNIDAD, Unidad.GRAMO, gramosPorTaza = 100.0))
        assertEquals(2.0, Conversor.convertir(2.0, Unidad.UNIDAD, Unidad.UNIDAD)!!, 0.001)
    }
}
