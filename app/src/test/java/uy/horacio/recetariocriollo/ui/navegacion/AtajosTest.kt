package uy.horacio.recetariocriollo.ui.navegacion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AtajosTest {

    @Test
    fun `lee cada atajo`() {
        assertEquals(Atajo.NuevoTimer(600), Atajos.leer(Atajos.uriNuevoTimer(600)))
        assertEquals(Atajo.NuevaReceta, Atajos.leer(Atajos.URI_NUEVA_RECETA))
        assertEquals(Atajo.ConLoQueTengo, Atajos.leer(Atajos.URI_CON_LO_QUE_TENGO))
        assertEquals(Atajo.Timers, Atajos.leer(Atajos.URI_TIMERS))
    }

    @Test
    fun `rechaza lo que no es un atajo valido`() {
        assertNull(Atajos.leer(null))
        assertNull(Atajos.leer("https://atajo/timers"))
        assertNull(Atajos.leer("recetario://otro/timers"))
        assertNull(Atajos.leer("recetario://atajo/desconocido"))
        assertNull(Atajos.leer("recetario://atajo/timer"))
        assertNull(Atajos.leer("recetario://atajo/timer?segundos=0"))
        assertNull(Atajos.leer("recetario://atajo/timer?segundos=abc"))
        assertNull(Atajos.leer("recetario://atajo/timer?segundos=999999"))
        assertNull(Atajos.leer("no es un uri %%"))
    }
}
