package uy.horacio.recetariocriollo.dominio

import org.junit.Assert.assertEquals
import org.junit.Test
import uy.horacio.recetariocriollo.dominio.modelo.Ingrediente
import uy.horacio.recetariocriollo.dominio.modelo.PreferenciaUnidades.DE_COCINA
import uy.horacio.recetariocriollo.dominio.modelo.PreferenciaUnidades.METRICAS
import uy.horacio.recetariocriollo.dominio.modelo.Unidad

class UnidadSugeridaTest {

    private val harina = Ingrediente(nombre = "Harina", densidadGramosPorTaza = 120.0, unidadHabitual = Unidad.GRAMO)
    private val carne = Ingrediente(nombre = "Carne picada", unidadHabitual = Unidad.GRAMO)
    private val leche = Ingrediente(nombre = "Leche", unidadHabitual = Unidad.MILILITRO)
    private val huevo = Ingrediente(nombre = "Huevo", unidadHabitual = Unidad.UNIDAD)

    @Test
    fun `metricas respeta la unidad habitual`() {
        assertEquals(Unidad.GRAMO, UnidadSugerida.para(harina, METRICAS))
        assertEquals(Unidad.MILILITRO, UnidadSugerida.para(leche, METRICAS))
    }

    @Test
    fun `de cocina pasa a tazas solo si se puede`() {
        assertEquals(Unidad.TAZA, UnidadSugerida.para(harina, DE_COCINA))
        assertEquals(Unidad.GRAMO, UnidadSugerida.para(carne, DE_COCINA))
        assertEquals(Unidad.TAZA, UnidadSugerida.para(leche, DE_COCINA))
        assertEquals(Unidad.UNIDAD, UnidadSugerida.para(huevo, DE_COCINA))
    }
}
