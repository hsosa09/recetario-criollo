package uy.horacio.recetariocriollo.dominio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import uy.horacio.recetariocriollo.dominio.modelo.Unidad

class FraccionesTest {

    @Test
    fun `redondea a fracciones utiles las unidades de cocina`() {
        assertEquals(2.0 + 1.0 / 3, Fracciones.redondearAFraccion(2.3333), 0.001)
        assertEquals(0.5, Fracciones.redondearAFraccion(0.48), 0.001)
        assertEquals(2.0 / 3, Fracciones.redondearAFraccion(0.7), 0.001)
        assertEquals(0.75, Fracciones.redondearAFraccion(0.73), 0.001)
        assertEquals(3.0, Fracciones.redondearAFraccion(2.97), 0.001)
    }

    @Test
    fun `no muestra 2,3333 huevos`() {
        val texto = Fracciones.formatearCantidad(2.3333, Unidad.UNIDAD)
        assertEquals("2 1/3", texto)
    }

    @Test
    fun `los gramos van a escalones redondos`() {
        // Entre 100 y 1000 g se redondea de a 5: nadie pesa 213,4 g de harina.
        assertEquals(215.0, Fracciones.redondearParaCocina(213.4, Unidad.GRAMO), 0.001)
        assertEquals(215.0, Fracciones.redondearParaCocina(216.0, Unidad.GRAMO), 0.001)
        assertEquals(1225.0, Fracciones.redondearParaCocina(1233.0, Unidad.GRAMO), 0.001)
        assertEquals(7.5, Fracciones.redondearParaCocina(7.6, Unidad.GRAMO), 0.001)
    }

    @Test
    fun `formatea decimales con coma y sin ceros de mas`() {
        assertEquals("1,5", Fracciones.formatearDecimal(1.5))
        assertEquals("250", Fracciones.formatearDecimal(250.0))
        assertEquals("0,25", Fracciones.formatearDecimal(0.25))
    }

    @Test
    fun `arma el texto con la unidad en singular o plural`() {
        assertEquals("1 taza", Fracciones.formatearConUnidad(1.0, Unidad.TAZA))
        assertEquals("2 1/2 tazas", Fracciones.formatearConUnidad(2.5, Unidad.TAZA))
        assertEquals("1/2 cucharadita", Fracciones.formatearConUnidad(0.5, Unidad.CUCHARADITA))
        assertEquals("250 g", Fracciones.formatearConUnidad(250.0, Unidad.GRAMO))
        assertEquals("a gusto", Fracciones.formatearConUnidad(0.0, Unidad.A_GUSTO))
    }

    @Test
    fun `lee cantidades escritas a mano`() {
        assertEquals(250.0, Fracciones.parsear("250")!!, 0.001)
        assertEquals(2.5, Fracciones.parsear("2,5")!!, 0.001)
        assertEquals(0.5, Fracciones.parsear("1/2")!!, 0.001)
        assertEquals(1.5, Fracciones.parsear("1 1/2")!!, 0.001)
        assertNull(Fracciones.parsear("una pizca"))
        assertNull(Fracciones.parsear(""))
    }

    @Test
    fun `la conversion no muestra cero para cantidades chicas`() {
        // Issue #2: 1 cucharadita a litros decia "0 l".
        assertEquals("0,005 l", Fracciones.formatearConversion(0.005, Unidad.LITRO))
        assertEquals("0,0025 kg", Fracciones.formatearConversion(0.0025, Unidad.KILOGRAMO))
        assertEquals("0,0353 oz", Fracciones.formatearConversion(0.035274, Unidad.ONZA))
    }

    @Test
    fun `la conversion usa fracciones solo cuando caen justas`() {
        assertEquals("1/2 taza", Fracciones.formatearConversion(0.5, Unidad.TAZA))
        assertEquals("16 cucharadas", Fracciones.formatearConversion(16.0, Unidad.CUCHARADA))
        assertEquals("0,417 taza", Fracciones.formatearConversion(0.4167, Unidad.TAZA))
    }

    @Test
    fun `la conversion no redondea a escalones de cocina`() {
        assertEquals("454 g", Fracciones.formatearConversion(453.59237, Unidad.GRAMO))
        assertEquals("2,2 lb", Fracciones.formatearConversion(2.2046, Unidad.LIBRA))
        assertEquals("7,6 g", Fracciones.formatearConversion(7.6, Unidad.GRAMO))
        assertEquals("120 g", Fracciones.formatearConversion(120.0, Unidad.GRAMO))
    }
}
