package uy.horacio.recetariocriollo.dominio

import uy.horacio.recetariocriollo.dominio.modelo.TipoUnidad
import uy.horacio.recetariocriollo.dominio.modelo.Unidad
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/**
 * Redondeo y formateo de cantidades pensados para cocina real:
 * nadie mide "2,3333 huevos" ni "213,7 g de harina".
 */
object Fracciones {

    /** Fracciones que se pueden medir de verdad con taza, cuchara o a ojo. */
    private val FRACCIONES_UTILES = listOf(
        0.0 to "",
        1.0 / 4 to "1/4",
        1.0 / 3 to "1/3",
        1.0 / 2 to "1/2",
        2.0 / 3 to "2/3",
        3.0 / 4 to "3/4",
        1.0 to ""
    )

    /**
     * Lleva [valor] al numero medible mas cercano segun la [unidad].
     * Metrico -> escalones redondos; taza/cuchara/unidad -> fracciones de cocina.
     */
    fun redondearParaCocina(valor: Double, unidad: Unidad): Double {
        if (valor <= 0.0) return 0.0
        return if (unidad.prefiereFracciones) redondearAFraccion(valor)
        else redondearMetrico(valor, unidad)
    }

    /** Ajusta a la fraccion util mas cercana (1/4, 1/3, 1/2, 2/3, 3/4). */
    fun redondearAFraccion(valor: Double): Double {
        if (valor <= 0.0) return 0.0
        // Arriba de 10 las fracciones dejan de aportar: 12 1/3 tazas no se mide asi.
        if (valor >= 10) return redondearAMultiplo(valor, 0.5)
        val entero = kotlin.math.floor(valor)
        val resto = valor - entero
        val mejor = FRACCIONES_UTILES.minByOrNull { abs(it.first - resto) }!!
        return entero + mejor.first
    }

    /** Escalones redondos para gramos, mililitros y compania. */
    private fun redondearMetrico(valor: Double, unidad: Unidad): Double {
        val paso = when {
            unidad.factorBase >= 1000 -> 0.05                                // kg y litros
            unidad.tipo == TipoUnidad.PESO && unidad.factorBase > 1 -> 0.25  // onzas y libras
            valor < 10 -> 0.5
            valor < 100 -> 1.0
            valor < 1000 -> 5.0
            else -> 25.0
        }
        return redondearAMultiplo(valor, paso)
    }

    fun redondearAMultiplo(valor: Double, paso: Double): Double {
        if (paso <= 0) return valor
        return (valor / paso).roundToLong() * paso
    }

    /** Texto de la cantidad, sin unidad. Ej: 2.3333 -> "2 1/3", 250.0 -> "250", 1.5 -> "1,5". */
    fun formatearCantidad(valor: Double, unidad: Unidad): String {
        if (unidad.sinCantidad) return ""
        if (valor <= 0.0) return "0"
        if (unidad.prefiereFracciones) {
            val texto = comoFraccion(valor)
            if (texto != null) return texto
        }
        return formatearDecimal(valor)
    }

    /** Devuelve "1 1/2" si el valor cae sobre una fraccion util; null si no. */
    fun comoFraccion(valor: Double): String? {
        if (valor <= 0.0) return null
        val entero = kotlin.math.floor(valor).toInt()
        val resto = valor - entero
        val encaje = FRACCIONES_UTILES.firstOrNull { abs(it.first - resto) < 0.02 } ?: return null
        val etiqueta = encaje.second
        val enteroFinal = if (abs(encaje.first - 1.0) < 0.02) entero + 1 else entero
        return when {
            etiqueta.isEmpty() -> enteroFinal.toString()
            enteroFinal == 0 -> etiqueta
            else -> "$enteroFinal $etiqueta"
        }
    }

    /** Decimal con coma, como se escribe en Uruguay, y sin ceros al pedo. */
    fun formatearDecimal(valor: Double, decimalesMaximos: Int = 2): String {
        val redondeado = redondearAMultiplo(valor, Math.pow(10.0, -decimalesMaximos.toDouble()))
        if (abs(redondeado - redondeado.roundToLong()) < 1e-9) {
            return redondeado.roundToLong().toString()
        }
        var texto = String.format(java.util.Locale.US, "%.${decimalesMaximos}f", redondeado)
        texto = texto.trimEnd('0').trimEnd('.')
        return texto.replace('.', ',')
    }

    /** Cantidad + unidad ya en singular o plural. Ej: "1 taza", "2 1/2 tazas", "a gusto". */
    fun formatearConUnidad(valor: Double, unidad: Unidad): String {
        if (unidad.sinCantidad) return unidad.singular
        val cantidad = formatearCantidad(valor, unidad)
        // Las unidades de conteo van solas ("2 huevos" lo arma la pantalla con el nombre del ingrediente).
        if (unidad == Unidad.UNIDAD) return cantidad
        val nombre = when (unidad) {
            Unidad.TAZA, Unidad.CUCHARADA, Unidad.CUCHARADITA, Unidad.PIZCA ->
                if (esSingular(valor)) unidad.singular else unidad.plural
            else -> unidad.abreviatura
        }
        return "$cantidad $nombre"
    }

    /** Hasta una unidad va en singular: "1/2 cucharadita", "1 taza". */
    private fun esSingular(valor: Double) = valor <= 1.0 + 0.02

    /**
     * Lee una cantidad escrita a mano. Acepta lo que la gente escribe de verdad:
     * "250", "2,5", "2.5", "1/2" y "1 1/2".
     */
    fun parsear(texto: String): Double? {
        val limpio = texto.trim().replace(',', '.')
        if (limpio.isEmpty()) return null
        val partes = limpio.split(" ").filter { it.isNotBlank() }
        return when (partes.size) {
            1 -> unaParte(partes[0])
            2 -> {
                val entero = partes[0].toDoubleOrNull() ?: return null
                val fraccion = comoQuebrado(partes[1]) ?: return null
                entero + fraccion
            }
            else -> null
        }
    }

    private fun unaParte(texto: String): Double? =
        if (texto.contains('/')) comoQuebrado(texto) else texto.toDoubleOrNull()

    private fun comoQuebrado(texto: String): Double? {
        val lados = texto.split("/")
        if (lados.size != 2) return null
        val numerador = lados[0].toDoubleOrNull() ?: return null
        val denominador = lados[1].toDoubleOrNull() ?: return null
        if (denominador == 0.0) return null
        return numerador / denominador
    }

    /** Redondeo entero simple, util para porciones y tiempos. */
    fun redondearEntero(valor: Double): Int = valor.roundToInt()
}
