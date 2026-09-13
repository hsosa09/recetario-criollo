package uy.horacio.recetariocriollo.cronometro

/** Una fila del widget de timers. */
data class FilaWidget(
    val id: Long,
    val etiqueta: String,
    val estado: EstadoCronometro,
    /** Instante de fin si corre (para la cuenta regresiva nativa del widget). */
    val finEnMillis: Long?,
    val restanteSegundos: Int
)

data class ContenidoWidget(
    val filas: List<FilaWidget>,
    /** Timers que no entran en el widget. */
    val restantes: Int
) {
    val vacio: Boolean get() = filas.isEmpty()
}

/** Qué muestra el widget: sin Android, para testearlo. */
object WidgetTimers {

    /** Duraciones de los atajos cuando no hay timers. */
    val ATAJOS_SEGUNDOS = listOf(300, 600, 900)

    /**
     * Primero los que corren (el que termina antes arriba), después los pausados y al final
     * los terminados. Entran [maximo]; el resto se resume.
     */
    fun contenido(cronometros: List<Cronometro>, ahoraMillis: Long, maximo: Int = 3): ContenidoWidget {
        val ordenados = cronometros.sortedWith(
            compareBy<Cronometro> {
                when (it.estado) {
                    EstadoCronometro.CORRIENDO -> 0
                    EstadoCronometro.PAUSADO -> 1
                    EstadoCronometro.TERMINADO -> 2
                }
            }.thenBy { it.restanteSegundos(ahoraMillis) }
        )
        return ContenidoWidget(
            filas = ordenados.take(maximo).map {
                FilaWidget(it.id, it.etiqueta, it.estado, it.finEnMillis, it.restanteSegundos(ahoraMillis))
            },
            restantes = (ordenados.size - maximo).coerceAtLeast(0)
        )
    }
}
