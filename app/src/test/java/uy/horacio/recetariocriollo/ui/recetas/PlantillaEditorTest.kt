package uy.horacio.recetariocriollo.ui.recetas

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import uy.horacio.recetariocriollo.dominio.Plantillas
import uy.horacio.recetariocriollo.dominio.modelo.CategoriaReceta

class PlantillaEditorTest {

    private var id = 0L
    private val nuevoId = { ++id }
    private val horno = Plantillas.porId("horno")!!
    private val postre = Plantillas.porId("postre")!!

    @Test
    fun `tocar dos veces la misma plantilla no duplica pasos`() {
        // Issue #7
        val una = EstadoEditorReceta().conPlantilla(horno, nuevoId)
        val dos = una.conPlantilla(horno, nuevoId)
        assertEquals(horno.pasos.size, dos.pasos.size)
        assertEquals(una, dos)
    }

    @Test
    fun `cambiar de plantilla reemplaza sus pasos y sugerencias`() {
        val estado = EstadoEditorReceta()
            .conPlantilla(horno, nuevoId)
            .conPlantilla(postre, nuevoId)
        assertEquals(postre.pasos, estado.pasos.map { it.texto })
        assertEquals(CategoriaReceta.POSTRE, estado.categoria)
        assertEquals(postre.porcionesSugeridas.toString(), estado.porciones)
        assertEquals(postre.tiempoSugeridoMinutos.toString(), estado.tiempo)
    }

    @Test
    fun `lo que escribio el usuario sobrevive al cambio`() {
        val conHorno = EstadoEditorReceta().conPlantilla(horno, nuevoId)
        val editado = conHorno.copy(
            porciones = "10",
            pasos = conHorno.pasos.mapIndexed { i, paso ->
                if (i == 0) paso.copy(texto = "Precalentar a 200 °C") else paso
            } + LineaPaso(idLocal = nuevoId(), texto = "Servir con ensalada", minutosTimer = "")
        )
        val cambiado = editado.conPlantilla(postre, nuevoId)
        val textos = cambiado.pasos.map { it.texto }
        assertTrue(textos.containsAll(listOf("Precalentar a 200 °C", "Servir con ensalada")))
        assertTrue(textos.containsAll(postre.pasos))
        assertEquals(2 + postre.pasos.size, textos.size)
        assertEquals("10", cambiado.porciones)
    }

    @Test
    fun `empezar en blanco saca lo que puso la plantilla`() {
        val estado = EstadoEditorReceta()
            .conPlantilla(postre, nuevoId)
            .conPlantilla(null, nuevoId)
        assertNull(estado.plantillaId)
        assertTrue(estado.pasos.isEmpty())
        assertEquals(PORCIONES_POR_DEFECTO, estado.porciones)
        assertEquals("", estado.tiempo)
    }
}
