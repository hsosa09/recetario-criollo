package uy.horacio.recetariocriollo.dominio

import uy.horacio.recetariocriollo.dominio.modelo.Cocinada
import uy.horacio.recetariocriollo.dominio.modelo.ResumenCocinadas
import java.time.Instant
import java.time.ZoneId
import kotlin.math.floor

/** Formatos y cuentas del historial de cocinadas. Sin Android, para poder testearlo. */
object Historial {

    // Tabla propia: las abreviaturas de Locale cambian entre versiones de Android y de Java
    // ("sep" o "sept", con o sin punto) y la fecha tiene que verse igual en todos lados.
    private val MESES = listOf("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic")

    /** "★★★★½": estrellas llenas y media si el promedio llega a medio punto. */
    fun estrellas(promedio: Double): String {
        val acotado = promedio.coerceIn(0.0, 5.0)
        val llenas = floor(acotado).toInt()
        val media = acotado - llenas >= 0.5
        return "★".repeat(llenas) + if (media) "½" else ""
    }

    /** "30 ago" si es del año en curso; "24 dic 2025" si no. */
    fun fechaCorta(millis: Long, ahoraMillis: Long, zona: ZoneId = ZoneId.systemDefault()): String {
        val fecha = Instant.ofEpochMilli(millis).atZone(zona).toLocalDate()
        val hoy = Instant.ofEpochMilli(ahoraMillis).atZone(zona).toLocalDate()
        val mes = MESES[fecha.monthValue - 1]
        return if (fecha.year == hoy.year) "${fecha.dayOfMonth} $mes" else "${fecha.dayOfMonth} $mes ${fecha.year}"
    }

    /** Resumen de una sola receta a partir de su lista de cocinadas. */
    fun resumir(recetaId: Long, cocinadas: List<Cocinada>): ResumenCocinadas =
        if (cocinadas.isEmpty()) ResumenCocinadas.vacio(recetaId)
        else ResumenCocinadas(
            recetaId = recetaId,
            veces = cocinadas.size,
            promedioEstrellas = cocinadas.map { it.estrellas }.average(),
            ultimaMillis = cocinadas.maxOf { it.fechaMillis }
        )

    data class Puesto(val recetaId: Long, val veces: Int, val fraccion: Float)

    /**
     * Las [cuantas] recetas más cocinadas, con la fraccion respecto de la primera para
     * dibujar la barra. Empates: gana la cocinada más recientemente.
     */
    fun masCocinadas(resumenes: Collection<ResumenCocinadas>, cuantas: Int = 3): List<Puesto> {
        val orden = resumenes
            .filter { it.veces > 0 }
            .sortedWith(compareByDescending<ResumenCocinadas> { it.veces }.thenByDescending { it.ultimaMillis ?: 0L })
            .take(cuantas)
        val maximo = orden.firstOrNull()?.veces ?: return emptyList()
        return orden.map { Puesto(it.recetaId, it.veces, it.veces.toFloat() / maximo) }
    }
}
