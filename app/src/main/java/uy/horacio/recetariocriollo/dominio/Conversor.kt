package uy.horacio.recetariocriollo.dominio

import uy.horacio.recetariocriollo.dominio.modelo.TipoUnidad
import uy.horacio.recetariocriollo.dominio.modelo.Unidad

/**
 * Referencias de horno como aparecen en los recetarios criollos viejos,
 * que casi nunca dan grados sino "horno moderado".
 */
enum class NivelHorno(
    val desdeCelsius: Int,
    val hastaCelsius: Int
) {
    MUY_SUAVE(90, 139),
    SUAVE(140, 169),
    MODERADO(170, 199),
    FUERTE(200, 229),
    MUY_FUERTE(230, 300);

    /** Temperatura del medio del rango, para cuando hay que poner un numero en el horno. */
    val sugeridaCelsius: Int get() = (desdeCelsius + hastaCelsius) / 2
    val rangoCelsius: String get() = "$desdeCelsius - $hastaCelsius °C"
    val rangoFahrenheit: String
        get() = "${Conversor.celsiusAFahrenheit(desdeCelsius.toDouble()).toInt()} - " +
                "${Conversor.celsiusAFahrenheit(hastaCelsius.toDouble()).toInt()} °F"
}

/**
 * Conversiones de cocina. Todo lo que cruza volumen y peso necesita la densidad
 * del ingrediente: una taza de harina y una de azucar no pesan lo mismo.
 */
object Conversor {

    /** Proporcion clasica levadura fresca : levadura seca. */
    const val PROPORCION_LEVADURA = 3.0

    /**
     * Convierte [cantidad] de [desde] a [hasta].
     * [gramosPorTaza] solo hace falta cuando se cruza volumen con peso; si se necesita
     * y no se pasa, devuelve null porque la conversion no tiene respuesta unica.
     */
    fun convertir(
        cantidad: Double,
        desde: Unidad,
        hasta: Unidad,
        gramosPorTaza: Double? = null
    ): Double? {
        if (desde.tipo == TipoUnidad.CONTEO || hasta.tipo == TipoUnidad.CONTEO) {
            return if (desde == hasta) cantidad else null
        }
        val enBase = cantidad * desde.factorBase
        return when {
            desde.tipo == hasta.tipo -> enBase / hasta.factorBase
            desde.tipo == TipoUnidad.VOLUMEN -> {
                val densidad = gramosPorTaza ?: return null
                mililitrosAGramos(enBase, densidad) / hasta.factorBase
            }
            else -> {
                val densidad = gramosPorTaza ?: return null
                gramosAMililitros(enBase, densidad) / hasta.factorBase
            }
        }
    }

    /** true si la conversion entre estas dos unidades exige conocer la densidad. */
    fun necesitaDensidad(desde: Unidad, hasta: Unidad): Boolean =
        desde.tipo != hasta.tipo &&
            desde.tipo != TipoUnidad.CONTEO &&
            hasta.tipo != TipoUnidad.CONTEO

    fun mililitrosAGramos(mililitros: Double, gramosPorTaza: Double): Double =
        mililitros * gramosPorTaza / Unidad.TAZA.factorBase

    fun gramosAMililitros(gramos: Double, gramosPorTaza: Double): Double {
        if (gramosPorTaza <= 0) return 0.0
        return gramos * Unidad.TAZA.factorBase / gramosPorTaza
    }

    fun celsiusAFahrenheit(celsius: Double): Double = celsius * 9.0 / 5.0 + 32.0

    fun fahrenheitACelsius(fahrenheit: Double): Double = (fahrenheit - 32.0) * 5.0 / 9.0

    /** El nivel de horno que corresponde a esa temperatura, o null si esta fuera de escala. */
    fun nivelDeHorno(celsius: Double): NivelHorno? =
        NivelHorno.entries.firstOrNull { celsius >= it.desdeCelsius && celsius <= it.hastaCelsius }

    /** Levadura fresca -> levadura seca (se usa un tercio). */
    fun levaduraFrescaASeca(gramosFrescos: Double): Double = gramosFrescos / PROPORCION_LEVADURA

    /** Levadura seca -> levadura fresca (se usa el triple). */
    fun levaduraSecaAFresca(gramosSecos: Double): Double = gramosSecos * PROPORCION_LEVADURA
}
