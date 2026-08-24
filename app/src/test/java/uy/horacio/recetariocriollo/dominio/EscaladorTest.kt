package uy.horacio.recetariocriollo.dominio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import uy.horacio.recetariocriollo.dominio.modelo.CategoriaIngrediente
import uy.horacio.recetariocriollo.dominio.modelo.Ingrediente
import uy.horacio.recetariocriollo.dominio.modelo.IngredienteDeReceta
import uy.horacio.recetariocriollo.dominio.modelo.Receta
import uy.horacio.recetariocriollo.dominio.modelo.ReglaEscalado
import uy.horacio.recetariocriollo.dominio.modelo.Unidad

class EscaladorTest {

    private val harina = Ingrediente(
        id = 1,
        nombre = "Harina 0000",
        categoria = CategoriaIngrediente.HARINAS,
        densidadGramosPorTaza = 120.0
    )
    private val sal = Ingrediente(
        id = 2,
        nombre = "Sal fina",
        categoria = CategoriaIngrediente.CONDIMENTOS,
        esSalOEspecia = true
    )
    private val aceite = Ingrediente(
        id = 3,
        nombre = "Aceite",
        categoria = CategoriaIngrediente.GRASAS
    )

    private fun receta() = Receta(
        id = 1,
        nombre = "Tortas fritas",
        porcionesBase = 12,
        ingredientes = listOf(
            IngredienteDeReceta(id = 1, ingrediente = harina, cantidad = 500.0, unidad = Unidad.GRAMO),
            IngredienteDeReceta(
                id = 2,
                ingrediente = sal,
                cantidad = 2.0,
                unidad = Unidad.CUCHARADITA,
                regla = ReglaEscalado.ATENUADA
            ),
            IngredienteDeReceta(
                id = 3,
                ingrediente = aceite,
                cantidad = 1.0,
                unidad = Unidad.LITRO,
                regla = ReglaEscalado.FIJA
            )
        )
    )

    @Test
    fun `el factor sale de las porciones`() {
        assertEquals(2.0, Escalador.factor(6, 12), 0.0001)
        assertEquals(0.5, Escalador.factor(8, 4), 0.0001)
    }

    @Test
    fun `lo proporcional se multiplica igual que las porciones`() {
        val escalada = Escalador.escalarReceta(receta(), 24).first()
        assertEquals(1000.0, escalada.cantidad, 0.001)
    }

    @Test
    fun `la sal sube menos que proporcional al doblar la receta`() {
        val escalada = Escalador.escalarReceta(receta(), 24)[1]
        // 2 cdta * 2^0,75 = 3,36 -> se muestra como 3 1/3 cucharaditas, no 4.
        assertTrue(escalada.cantidad > 2.0)
        assertTrue(escalada.cantidad < 4.0)
        assertEquals(3.3636, escalada.cantidadExacta, 0.01)
    }

    @Test
    fun `lo marcado como fijo no se toca`() {
        val escalada = Escalador.escalarReceta(receta(), 36)[2]
        assertEquals(1.0, escalada.cantidad, 0.0001)
    }

    @Test
    fun `achicar la receta tambien funciona`() {
        val escalada = Escalador.escalarReceta(receta(), 6).first()
        assertEquals(250.0, escalada.cantidad, 0.001)
    }

    @Test
    fun `un ingrediente nunca queda en cero si la receta lo lleva`() {
        val receta = Receta(
            nombre = "Prueba",
            porcionesBase = 100,
            ingredientes = listOf(
                IngredienteDeReceta(ingrediente = sal, cantidad = 1.0, unidad = Unidad.CUCHARADITA)
            )
        )
        val escalada = Escalador.escalarReceta(receta, 1).first()
        assertTrue(escalada.cantidad > 0.0)
    }

    @Test
    fun `sugiere ajuste suave para sal y especias`() {
        assertEquals(ReglaEscalado.ATENUADA, Escalador.reglaSugerida(sal))
        assertEquals(ReglaEscalado.LINEAL, Escalador.reglaSugerida(harina))
    }

    @Test
    fun `las cantidades escaladas se muestran medibles`() {
        val receta = Receta(
            nombre = "Flan",
            porcionesBase = 8,
            ingredientes = listOf(
                IngredienteDeReceta(
                    ingrediente = Ingrediente(id = 9, nombre = "Huevo", unidadHabitual = Unidad.UNIDAD),
                    cantidad = 6.0,
                    unidad = Unidad.UNIDAD
                )
            )
        )
        // 6 huevos para 8 porciones -> 3 porciones piden 2,25 huevos.
        assertEquals("2 1/4", Escalador.escalarReceta(receta, 3).first().texto)
    }
}
