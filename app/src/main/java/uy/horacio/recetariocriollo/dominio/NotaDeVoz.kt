package uy.horacio.recetariocriollo.dominio

/** Reglas de las notas de voz del historial. */
object NotaDeVoz {

    /** Una nota es para «qué cambiarías la próxima», no un podcast. */
    const val DURACION_MAXIMA_MS = 60_000

    /** Menos que esto es un toque sin querer: no se guarda. */
    const val DURACION_MINIMA_MS = 700

    fun esValida(duracionMs: Long): Boolean = duracionMs >= DURACION_MINIMA_MS

    /** "0:07", "1:00". */
    fun formatear(ms: Long): String {
        val segundos = (ms.coerceAtLeast(0) + 500) / 1000
        return "%d:%02d".format(segundos / 60, segundos % 60)
    }

    /** Fracción del máximo, para la barra mientras se graba. */
    fun progreso(ms: Long): Float = (ms.toFloat() / DURACION_MAXIMA_MS).coerceIn(0f, 1f)
}
