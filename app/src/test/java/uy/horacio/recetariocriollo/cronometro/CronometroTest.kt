package uy.horacio.recetariocriollo.cronometro

import org.junit.Assert.assertEquals
import org.junit.Test

class CronometroTest {

    @Test
    fun `describe minutos redondos`() {
        assertEquals("25 min", Cronometro.describirDuracion(1500))
    }

    @Test
    fun `describe horas y minutos`() {
        assertEquals("1 h", Cronometro.describirDuracion(3600))
        assertEquals("1 h 15 min", Cronometro.describirDuracion(4500))
    }

    @Test
    fun `describe segundos sueltos`() {
        assertEquals("1 min 30 s", Cronometro.describirDuracion(90))
        assertEquals("45 s", Cronometro.describirDuracion(45))
    }

    @Test
    fun `cero y negativos no rompen`() {
        assertEquals("0 s", Cronometro.describirDuracion(0))
        assertEquals("0 s", Cronometro.describirDuracion(-10))
    }

    @Test
    fun `el formato de reloj sigue igual`() {
        assertEquals("07:35", Cronometro.formatearSegundos(455))
        assertEquals("1:02:00", Cronometro.formatearSegundos(3720))
    }
}
