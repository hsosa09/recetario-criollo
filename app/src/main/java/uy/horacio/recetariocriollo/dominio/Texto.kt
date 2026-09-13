package uy.horacio.recetariocriollo.dominio

import java.text.Normalizer

/**
 * Comparacion de texto "como escribe la gente en el celular": sin distinguir
 * mayusculas, tildes ni dieresis. "azucar" encuentra "Azúcar" y "PURE" encuentra "puré".
 */
object Texto {

    private val MARCAS_DIACRITICAS = Regex("\\p{Mn}+")

    /** Minusculas, sin tildes y sin espacios repetidos ni en los bordes. */
    fun normalizar(texto: String): String =
        Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
            .replace(MARCAS_DIACRITICAS, "")
            .lowercase()
            .replace(Regex("\\s+"), " ")

    /** true si [consulta] aparece dentro de [texto]; una consulta vacia coincide con todo. */
    fun contiene(texto: String, consulta: String): Boolean {
        val buscado = normalizar(consulta)
        return buscado.isEmpty() || normalizar(texto).contains(buscado)
    }

    /** Mismo nombre salvo mayusculas, tildes y espacios. */
    fun mismoNombre(a: String, b: String): Boolean = normalizar(a) == normalizar(b)
}
