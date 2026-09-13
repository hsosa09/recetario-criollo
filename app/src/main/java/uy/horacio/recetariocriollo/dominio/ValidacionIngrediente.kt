package uy.horacio.recetariocriollo.dominio

import uy.horacio.recetariocriollo.dominio.modelo.Ingrediente

/** Reglas para dar de alta o editar un ingrediente del catálogo. */
object ValidacionIngrediente {

    enum class Problema { NOMBRE_VACIO, NOMBRE_REPETIDO, DENSIDAD_INVALIDA }

    /** Una taza de lo más pesado de una cocina (miel, sal gruesa) no llega a 400 g. */
    private const val DENSIDAD_MAXIMA = 1000.0

    /**
     * Devuelve el primer problema, o null si se puede guardar.
     * [idPropio] es el id del ingrediente que se edita (0 si es nuevo): no choca consigo mismo.
     */
    fun validar(
        nombre: String,
        densidadTexto: String,
        catalogo: List<Ingrediente>,
        idPropio: Long = 0
    ): Problema? {
        val limpio = nombre.trim()
        if (limpio.isEmpty()) return Problema.NOMBRE_VACIO
        if (catalogo.any { it.id != idPropio && Texto.mismoNombre(it.nombre, limpio) }) {
            return Problema.NOMBRE_REPETIDO
        }
        if (densidadTexto.isNotBlank()) {
            val densidad = Fracciones.parsear(densidadTexto) ?: return Problema.DENSIDAD_INVALIDA
            if (densidad <= 0.0 || densidad > DENSIDAD_MAXIMA) return Problema.DENSIDAD_INVALIDA
        }
        return null
    }

    /** Cuántas recetas distintas usan cada ingrediente. */
    fun usos(idsPorReceta: List<Collection<Long>>): Map<Long, Int> =
        idsPorReceta.flatMap { it.toSet() }.groupingBy { it }.eachCount()
}
