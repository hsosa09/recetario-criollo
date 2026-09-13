package uy.horacio.recetariocriollo.ui.navegacion

import java.net.URI

/** Lo que pide un acceso directo o el widget al abrir la app. */
sealed interface Atajo {
    data class NuevoTimer(val segundos: Int) : Atajo
    data object NuevaReceta : Atajo
    data object ConLoQueTengo : Atajo
    data object Timers : Atajo
}

/**
 * Los accesos directos y el widget abren la app con un URI propio, sin exportar nada:
 * `recetario://atajo/timer?segundos=600`, `/nueva-receta`, `/con-lo-que-tengo`, `/timers`.
 */
object Atajos {

    const val ESQUEMA = "recetario"
    private const val ANFITRION = "atajo"
    private const val SEGUNDOS_MAXIMOS = 24 * 3600

    fun uriNuevoTimer(segundos: Int) = "$ESQUEMA://$ANFITRION/timer?segundos=$segundos"
    const val URI_NUEVA_RECETA = "$ESQUEMA://$ANFITRION/nueva-receta"
    const val URI_CON_LO_QUE_TENGO = "$ESQUEMA://$ANFITRION/con-lo-que-tengo"
    const val URI_TIMERS = "$ESQUEMA://$ANFITRION/timers"

    /** Null si no es un atajo de la app o está mal formado. */
    fun leer(texto: String?): Atajo? {
        val uri = runCatching { URI(texto ?: return null) }.getOrNull() ?: return null
        if (uri.scheme != ESQUEMA || uri.host != ANFITRION) return null
        return when (uri.path) {
            "/timer" -> {
                val segundos = uri.rawQuery.orEmpty().split("&")
                    .map { it.split("=", limit = 2) }
                    .firstOrNull { it.size == 2 && it[0] == "segundos" }
                    ?.get(1)?.toIntOrNull()
                    ?.takeIf { it in 1..SEGUNDOS_MAXIMOS }
                    ?: return null
                Atajo.NuevoTimer(segundos)
            }
            "/nueva-receta" -> Atajo.NuevaReceta
            "/con-lo-que-tengo" -> Atajo.ConLoQueTengo
            "/timers" -> Atajo.Timers
            else -> null
        }
    }
}
