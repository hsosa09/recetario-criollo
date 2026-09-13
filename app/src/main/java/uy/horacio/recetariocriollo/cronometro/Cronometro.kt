package uy.horacio.recetariocriollo.cronometro

import kotlinx.serialization.Serializable

enum class EstadoCronometro { CORRIENDO, PAUSADO, TERMINADO }

/**
 * Un timer de cocina.
 *
 * Mientras corre se guarda [finEnMillis] (un instante absoluto), no los segundos
 * restantes: asi la cuenta sigue bien aunque se cierre la app o se apague la pantalla.
 */
@Serializable
data class Cronometro(
    val id: Long,
    val etiqueta: String,
    val duracionSegundos: Int,
    val finEnMillis: Long? = null,
    val restanteAlPausar: Int = duracionSegundos,
    val estado: EstadoCronometro = EstadoCronometro.CORRIENDO
) {
    fun restanteSegundos(ahoraMillis: Long): Int = when (estado) {
        EstadoCronometro.CORRIENDO -> {
            val fin = finEnMillis ?: return 0
            (((fin - ahoraMillis) + 999) / 1000).coerceAtLeast(0).toInt()
        }
        EstadoCronometro.PAUSADO -> restanteAlPausar
        EstadoCronometro.TERMINADO -> 0
    }

    fun progreso(ahoraMillis: Long): Float {
        if (duracionSegundos <= 0) return 1f
        val transcurrido = duracionSegundos - restanteSegundos(ahoraMillis)
        return (transcurrido.toFloat() / duracionSegundos).coerceIn(0f, 1f)
    }

    companion object {
        /** Duracion para leer de un vistazo: "25 min", "1 h 15 min", "1 min 30 s". */
        fun describirDuracion(segundos: Int): String {
            val seguros = segundos.coerceAtLeast(0)
            val horas = seguros / 3600
            val minutos = (seguros % 3600) / 60
            val resto = seguros % 60
            return buildList {
                if (horas > 0) add("$horas h")
                if (minutos > 0) add("$minutos min")
                if (resto > 0 || isEmpty()) add("$resto s")
            }.joinToString(" ")
        }

        /** "07:35" o "1:02:00" cuando pasa de la hora. */
        fun formatearSegundos(segundos: Int): String {
            val seguros = segundos.coerceAtLeast(0)
            val horas = seguros / 3600
            val minutos = (seguros % 3600) / 60
            val resto = seguros % 60
            return if (horas > 0) String.format(java.util.Locale.US, "%d:%02d:%02d", horas, minutos, resto)
            else String.format(java.util.Locale.US, "%02d:%02d", minutos, resto)
        }
    }
}
