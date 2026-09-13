package uy.horacio.recetariocriollo.dominio

import org.junit.Assert.assertEquals
import org.junit.Test
import uy.horacio.recetariocriollo.dominio.modelo.Cocinada
import uy.horacio.recetariocriollo.dominio.modelo.PasoPreparacion
import uy.horacio.recetariocriollo.dominio.modelo.Receta

class ArchivosTest {

    private val receta = Receta(
        id = 1, nombre = "Pan", fotoPath = "/f/portada.jpg",
        pasos = listOf(
            PasoPreparacion(orden = 0, texto = "Amasar", fotoPath = "/f/masa.jpg"),
            PasoPreparacion(orden = 1, texto = "Levar"),
            PasoPreparacion(orden = 2, texto = "Hornear", fotoPath = "/f/horno.jpg")
        )
    )

    @Test
    fun `archivos de una receta y de una cocinada`() {
        assertEquals(setOf("/f/portada.jpg", "/f/masa.jpg", "/f/horno.jpg"), Archivos.deReceta(receta))
        assertEquals(
            setOf("/f/resultado.jpg", "/n/nota.m4a"),
            Archivos.deCocinada(Cocinada(recetaId = 1, fechaMillis = 0, estrellas = 5, porciones = 4, fotoPath = "/f/resultado.jpg", audioPath = "/n/nota.m4a"))
        )
    }

    @Test
    fun `sobran las fotos cambiadas o de pasos quitados`() {
        val editada = receta.copy(
            fotoPath = "/f/portada2.jpg",
            pasos = listOf(receta.pasos[0], receta.pasos[1].copy(fotoPath = "/f/levado.jpg"))
        )
        assertEquals(setOf("/f/portada.jpg", "/f/horno.jpg"), Archivos.sobrantesAlGuardar(receta, editada))
    }

    @Test
    fun `una receta nueva no deja sobrantes`() {
        assertEquals(emptySet<String>(), Archivos.sobrantesAlGuardar(null, receta))
    }
}
