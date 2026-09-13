package uy.horacio.recetariocriollo.dominio

import uy.horacio.recetariocriollo.dominio.modelo.Ingrediente

/**
 * Para el modo paso a paso: que ingredientes nombra el texto de un paso, asi se
 * muestran primero en «Ingredientes a mano».
 *
 * Un ingrediente se considera mencionado si aparece su nombre completo o su primera
 * palabra como palabra entera ("Harina 0000" en "poner la harina en un bol",
 * "Sal fina" en "con la sal"). Sin tildes ni mayusculas, y admite el plural simple
 * ("papas" menciona a "Papa").
 */
object IngredientesDelPaso {

    fun mencionados(textoPaso: String, ingredientes: List<Ingrediente>): Set<Long> {
        val palabras = Texto.normalizar(textoPaso)
            .split(Regex("[^a-z0-9]+"))
            .filter { it.isNotBlank() }
        val texto = " " + palabras.joinToString(" ") + " "
        return ingredientes
            .filter { ingrediente -> menciona(texto, palabras, ingrediente.nombre) }
            .map { it.id }
            .toSet()
    }

    /** Ordena poniendo primero lo que nombra el paso, sin cambiar el orden relativo. */
    fun <T> ordenar(textoPaso: String, items: List<T>, ingredienteDe: (T) -> Ingrediente): List<T> {
        val ids = mencionados(textoPaso, items.map(ingredienteDe))
        val (primero, resto) = items.partition { ingredienteDe(it).id in ids }
        return primero + resto
    }

    private fun menciona(texto: String, palabras: List<String>, nombre: String): Boolean {
        val normalizado = Texto.normalizar(nombre).split(Regex("[^a-z0-9]+")).filter { it.isNotBlank() }
        if (normalizado.isEmpty()) return false
        if (texto.contains(" " + normalizado.joinToString(" ") + " ")) return true
        val principal = normalizado.first()
        return palabras.any { it == principal || it == principal + "s" || it == principal + "es" }
    }
}
