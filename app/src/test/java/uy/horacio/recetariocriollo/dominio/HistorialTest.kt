package uy.horacio.recetariocriollo.dominio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import uy.horacio.recetariocriollo.dominio.modelo.ResumenCocinadas
import java.time.LocalDateTime
import java.time.ZoneId

class HistorialTest {

    private val zona = ZoneId.of("America/Montevideo")
    private fun millis(anio: Int, mes: Int, dia: Int) =
        LocalDateTime.of(anio, mes, dia, 21, 30).atZone(zona).toInstant().toEpochMilli()

    @Test
    fun `estrellas con media`() {
        assertEquals("★★★★½", Historial.estrellas(4.5))
        assertEquals("★★★★", Historial.estrellas(4.4))
        assertEquals("★★★★★", Historial.estrellas(5.0))
        assertEquals("★", Historial.estrellas(1.2))
        assertEquals("★★★★★", Historial.estrellas(7.0))
    }

    @Test
    fun `fecha corta en espanol`() {
        val hoy = millis(2026, 9, 13)
        assertEquals("30 ago", Historial.fechaCorta(millis(2026, 8, 30), hoy, zona))
        assertEquals("5 sep", Historial.fechaCorta(millis(2026, 9, 5), hoy, zona))
        assertEquals("24 dic 2025", Historial.fechaCorta(millis(2025, 12, 24), hoy, zona))
    }

    @Test
    fun `ranking por veces y desempate por la mas reciente`() {
        val puestos = Historial.masCocinadas(
            listOf(
                ResumenCocinadas(1, 7, 4.5, 100),
                ResumenCocinadas(2, 3, 4.0, 900),
                ResumenCocinadas(3, 3, 5.0, 500),
                ResumenCocinadas(4, 1, 4.0, 50),
                ResumenCocinadas.vacio(5)
            )
        )
        assertEquals(listOf(1L, 2L, 3L), puestos.map { it.recetaId })
        assertEquals(1f, puestos[0].fraccion)
        assertEquals(3f / 7, puestos[1].fraccion, 0.001f)
    }

    @Test
    fun `sin cocinadas no hay ranking`() {
        assertTrue(Historial.masCocinadas(listOf(ResumenCocinadas.vacio(1))).isEmpty())
    }
}
