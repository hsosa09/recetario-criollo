package uy.horacio.recetariocriollo.dominio

import uy.horacio.recetariocriollo.dominio.modelo.Cocinada
import uy.horacio.recetariocriollo.dominio.modelo.Ingrediente
import uy.horacio.recetariocriollo.dominio.modelo.Receta
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** Lo que muestra la pantalla de estadísticas para un año. */
data class EstadisticasAnio(
    val anio: Int,
    /** Veces cocinado en cada mes, de enero (0) a diciembre (11). */
    val porMes: List<Int>,
    val total: Int,
    /** Días distintos en que se cocinó. */
    val diasCocinando: Int,
    /** Máxima cantidad de días seguidos con al menos una cocinada. */
    val rachaMasLarga: Int,
    val masCocinada: Destacado?,
    val mejorCalificada: Destacado?,
    val ingredienteMasUsado: Destacado?,
    /** Recetas existentes que no se cocinaron en el año, por nombre. */
    val sinCocinar: List<Receta>
)

/** Una receta o ingrediente destacado con su número (veces, o promedio de estrellas). */
data class Destacado(val id: Long, val nombre: String, val valor: Double)

object Estadisticas {

    fun calcular(
        anio: Int,
        cocinadas: List<Cocinada>,
        recetas: List<Receta>,
        zona: ZoneId = ZoneId.systemDefault()
    ): EstadisticasAnio {
        val delAnio = cocinadas
            .map { it to fecha(it.fechaMillis, zona) }
            .filter { (_, dia) -> dia.year == anio }
        val porReceta = recetas.associateBy { it.id }

        val porMes = MutableList(12) { 0 }
        delAnio.forEach { (_, dia) -> porMes[dia.monthValue - 1]++ }

        val dias = delAnio.map { it.second }.toSortedSet()

        val masCocinada = delAnio.groupingBy { it.first.recetaId }.eachCount()
            .filterKeys { it in porReceta }
            .maxWithOrNull(compareBy<Map.Entry<Long, Int>> { it.value }.thenBy { -it.key })
            ?.let { Destacado(it.key, porReceta.getValue(it.key).nombre, it.value.toDouble()) }

        // Mejor calificada: promedio de estrellas; a igual promedio gana la que se hizo más veces.
        val mejorCalificada = delAnio.groupBy { it.first.recetaId }
            .filterKeys { it in porReceta }
            .map { (id, lista) -> Triple(id, lista.map { it.first.estrellas }.average(), lista.size) }
            .maxWithOrNull(compareBy<Triple<Long, Double, Int>> { it.second }.thenBy { it.third })
            ?.let { (id, promedio, _) -> Destacado(id, porReceta.getValue(id).nombre, promedio) }

        // Ingrediente más usado: en cuántas cocinadas estuvo, sin contar los básicos de despensa.
        val ingredienteMasUsado = delAnio
            .mapNotNull { porReceta[it.first.recetaId] }
            .flatMap { receta -> receta.ingredientes.map { it.ingrediente }.distinctBy { it.id } }
            .filterNot { it.esBasicoDeDespensa }
            .groupBy { it.id }
            .maxWithOrNull(compareBy<Map.Entry<Long, List<Ingrediente>>> { it.value.size }.thenBy { -it.key })
            ?.let { Destacado(it.key, it.value.first().nombre, it.value.size.toDouble()) }

        val cocinadasIds = delAnio.map { it.first.recetaId }.toSet()
        return EstadisticasAnio(
            anio = anio,
            porMes = porMes,
            total = delAnio.size,
            diasCocinando = dias.size,
            rachaMasLarga = rachaMasLarga(dias.toList()),
            masCocinada = masCocinada,
            mejorCalificada = mejorCalificada,
            ingredienteMasUsado = ingredienteMasUsado,
            sinCocinar = recetas.filter { it.id !in cocinadasIds }.sortedBy { it.nombre.lowercase() }
        )
    }

    /** Años con cocinadas, del más reciente al más viejo, siempre incluyendo el actual. */
    fun anios(cocinadas: List<Cocinada>, anioActual: Int, zona: ZoneId = ZoneId.systemDefault()): List<Int> =
        (cocinadas.map { fecha(it.fechaMillis, zona).year } + anioActual).distinct().sortedDescending()

    /** Días seguidos: [dias] ordenados y sin repetir. */
    fun rachaMasLarga(dias: List<LocalDate>): Int {
        if (dias.isEmpty()) return 0
        var mejor = 1
        var actual = 1
        for (i in 1 until dias.size) {
            actual = if (dias[i - 1].plusDays(1) == dias[i]) actual + 1 else 1
            mejor = maxOf(mejor, actual)
        }
        return mejor
    }

    private fun fecha(millis: Long, zona: ZoneId): LocalDate = Instant.ofEpochMilli(millis).atZone(zona).toLocalDate()
}
