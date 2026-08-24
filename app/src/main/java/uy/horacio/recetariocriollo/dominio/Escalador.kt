package uy.horacio.recetariocriollo.dominio

import uy.horacio.recetariocriollo.dominio.modelo.Ingrediente
import uy.horacio.recetariocriollo.dominio.modelo.IngredienteDeReceta
import uy.horacio.recetariocriollo.dominio.modelo.Receta
import uy.horacio.recetariocriollo.dominio.modelo.ReglaEscalado
import uy.horacio.recetariocriollo.dominio.modelo.Unidad
import kotlin.math.abs
import kotlin.math.pow

/** Una cantidad ya recalculada para las porciones pedidas. */
data class CantidadEscalada(
    val ingrediente: IngredienteDeReceta,
    /** Lo que da la cuenta, sin redondear. Sirve para no arrastrar error si se vuelve a escalar. */
    val cantidadExacta: Double,
    /** Lo que se muestra: ya redondeado a algo medible en una cocina. */
    val cantidad: Double,
    val texto: String,
    /** true si hubo que mover la cantidad para que fuera medible. */
    val redondeada: Boolean
)

/**
 * Recalculo de cantidades al cambiar las porciones.
 *
 * No todo escala igual: la sal, las especias y la levadura suben menos que
 * proporcionalmente (si duplicas una masa no va el doble de levadura), y hay
 * ingredientes que directamente no escalan (la manteca para el molde).
 */
object Escalador {

    /** Exponente del escalado atenuado. Con factor 2 sube ~68%, no 100%. */
    const val EXPONENTE_ATENUADO = 0.75

    fun factor(porcionesBase: Int, porcionesDeseadas: Int): Double {
        require(porcionesBase > 0) { "Las porciones base tienen que ser mayores a cero" }
        if (porcionesDeseadas <= 0) return 0.0
        return porcionesDeseadas.toDouble() / porcionesBase.toDouble()
    }

    /** Aplica la regla del ingrediente al factor de escalado. */
    fun aplicarRegla(cantidad: Double, factor: Double, regla: ReglaEscalado): Double = when (regla) {
        ReglaEscalado.LINEAL -> cantidad * factor
        ReglaEscalado.ATENUADA -> if (factor <= 0.0) 0.0 else cantidad * factor.pow(EXPONENTE_ATENUADO)
        ReglaEscalado.FIJA -> cantidad
    }

    fun escalarIngrediente(ingrediente: IngredienteDeReceta, factor: Double): CantidadEscalada {
        val exacta = aplicarRegla(ingrediente.cantidad, factor, ingrediente.regla)
        val redondeada = Fracciones.redondearParaCocina(exacta, ingrediente.unidad)
        // Nunca dejar en cero algo que la receta si lleva: se muestra el minimo medible.
        val mostrada = if (redondeada <= 0.0 && exacta > 0.0) minimoMedible(ingrediente.unidad) else redondeada
        return CantidadEscalada(
            ingrediente = ingrediente,
            cantidadExacta = exacta,
            cantidad = mostrada,
            texto = Fracciones.formatearConUnidad(mostrada, ingrediente.unidad),
            redondeada = abs(mostrada - exacta) > 1e-6
        )
    }

    fun escalarReceta(receta: Receta, porcionesDeseadas: Int): List<CantidadEscalada> {
        val factor = factor(receta.porcionesBase, porcionesDeseadas)
        return receta.ingredientes.map { escalarIngrediente(it, factor) }
    }

    private fun minimoMedible(unidad: Unidad): Double = when {
        unidad.prefiereFracciones -> 0.25
        unidad.factorBase >= 1000 -> 0.05
        else -> 1.0
    }

    /** Regla que conviene proponer al cargar un ingrediente nuevo en una receta. */
    fun reglaSugerida(ingrediente: Ingrediente): ReglaEscalado =
        if (ingrediente.esSalOEspecia) ReglaEscalado.ATENUADA else ReglaEscalado.LINEAL
}
