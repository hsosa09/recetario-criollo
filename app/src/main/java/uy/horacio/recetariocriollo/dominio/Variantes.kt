package uy.horacio.recetariocriollo.dominio

import uy.horacio.recetariocriollo.dominio.modelo.IngredienteDeReceta
import uy.horacio.recetariocriollo.dominio.modelo.Receta
import kotlin.math.abs

/** Un cambio entre la receta original y una variante. */
sealed interface Cambio<out T> {
    data class Igual<T>(val valor: T) : Cambio<T>
    data class Agregado<T>(val valor: T) : Cambio<T>
    data class Quitado<T>(val valor: T) : Cambio<T>
    data class Modificado<T>(val antes: T, val despues: T) : Cambio<T>
}

data class ComparacionRecetas(
    val original: Receta,
    val variante: Receta,
    val ingredientes: List<Cambio<IngredienteDeReceta>>,
    val pasos: List<Cambio<String>>,
    /** Datos generales que cambian: porciones, tiempo, dificultad, molde, categoría. */
    val datosDistintos: Set<Dato>
) {
    enum class Dato { PORCIONES, TIEMPO, DIFICULTAD, MOLDE, CATEGORIA }

    val hayDiferencias: Boolean
        get() = datosDistintos.isNotEmpty() ||
            ingredientes.any { it !is Cambio.Igual } ||
            pasos.any { it !is Cambio.Igual }
}

object Variantes {

    /** Las variantes cuelgan siempre de la raíz: una variante de una variante apunta a la misma original. */
    fun raiz(receta: Receta): Long = receta.origenId ?: receta.id

    /** La receta y todas las de su familia (original y variantes), la original primero. */
    fun familia(receta: Receta, todas: List<Receta>): List<Receta> {
        val raiz = raiz(receta)
        return todas
            .filter { it.id == raiz || it.origenId == raiz }
            .sortedWith(compareBy<Receta> { it.origenId != null }.thenBy { it.nombre.lowercase() })
    }

    fun comparar(original: Receta, variante: Receta): ComparacionRecetas {
        val ingredientes = diff(original.ingredientes, variante.ingredientes) { a, b -> a.ingrediente.id == b.ingrediente.id }
        return ComparacionRecetas(
            original = original,
            variante = variante,
            ingredientes = marcarModificados(ingredientes, original.ingredientes),
            pasos = emparejarPasosCambiados(
                diff(original.pasos.map { it.texto }, variante.pasos.map { it.texto }) { a, b ->
                    Texto.normalizar(a) == Texto.normalizar(b)
                }
            ),
            datosDistintos = buildSet {
                if (original.porcionesBase != variante.porcionesBase) add(ComparacionRecetas.Dato.PORCIONES)
                if (original.tiempoMinutos != variante.tiempoMinutos) add(ComparacionRecetas.Dato.TIEMPO)
                if (original.dificultad != variante.dificultad) add(ComparacionRecetas.Dato.DIFICULTAD)
                if (original.moldeCm != variante.moldeCm) add(ComparacionRecetas.Dato.MOLDE)
                if (original.categoria != variante.categoria) add(ComparacionRecetas.Dato.CATEGORIA)
            }
        )
    }

    /**
     * Diff por subsecuencia común más larga (LCS). Devuelve el recorrido completo en orden:
     * lo común como Igual (con el valor de [b]), lo que solo está en [a] como Quitado y lo
     * que solo está en [b] como Agregado.
     */
    fun <T> diff(a: List<T>, b: List<T>, igual: (T, T) -> Boolean): List<Cambio<T>> {
        val largo = Array(a.size + 1) { IntArray(b.size + 1) }
        for (i in a.indices.reversed()) for (j in b.indices.reversed()) {
            largo[i][j] = if (igual(a[i], b[j])) largo[i + 1][j + 1] + 1 else maxOf(largo[i + 1][j], largo[i][j + 1])
        }
        val salida = mutableListOf<Cambio<T>>()
        var i = 0
        var j = 0
        while (i < a.size && j < b.size) {
            when {
                igual(a[i], b[j]) -> { salida += Cambio.Igual(b[j]); i++; j++ }
                largo[i + 1][j] >= largo[i][j + 1] -> { salida += Cambio.Quitado(a[i]); i++ }
                else -> { salida += Cambio.Agregado(b[j]); j++ }
            }
        }
        while (i < a.size) salida += Cambio.Quitado(a[i++])
        while (j < b.size) salida += Cambio.Agregado(b[j++])
        return salida
    }

    /** Un ingrediente que está en las dos pero con otra cantidad, unidad, regla o aclaración. */
    private fun marcarModificados(
        cambios: List<Cambio<IngredienteDeReceta>>,
        originales: List<IngredienteDeReceta>
    ): List<Cambio<IngredienteDeReceta>> {
        val usados = mutableSetOf<Int>()
        return cambios.map { cambio ->
            if (cambio !is Cambio.Igual) return@map cambio
            val despues = cambio.valor
            // El mismo ingrediente puede aparecer dos veces (azúcar para la mezcla y para el caramelo).
            val indice = originales.indices.firstOrNull { it !in usados && originales[it].ingrediente.id == despues.ingrediente.id }
                ?: return@map cambio
            usados += indice
            val antes = originales[indice]
            val mismo = abs(antes.cantidad - despues.cantidad) < 1e-9 && antes.unidad == despues.unidad &&
                antes.regla == despues.regla && antes.aclaracion.orEmpty() == despues.aclaracion.orEmpty()
            if (mismo) cambio else Cambio.Modificado(antes, despues)
        }
    }

    /** Un paso quitado seguido de uno agregado en el mismo lugar se muestra como paso cambiado. */
    private fun emparejarPasosCambiados(cambios: List<Cambio<String>>): List<Cambio<String>> {
        val salida = mutableListOf<Cambio<String>>()
        var i = 0
        while (i < cambios.size) {
            val actual = cambios[i]
            val siguiente = cambios.getOrNull(i + 1)
            if (actual is Cambio.Quitado && siguiente is Cambio.Agregado) {
                salida += Cambio.Modificado(actual.valor, siguiente.valor)
                i += 2
            } else {
                salida += actual
                i++
            }
        }
        return salida
    }
}
