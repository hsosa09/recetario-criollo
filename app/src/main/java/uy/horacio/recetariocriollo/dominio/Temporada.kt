package uy.horacio.recetariocriollo.dominio

/**
 * Meses de estación como máscara de 12 bits: bit 0 = enero … bit 11 = diciembre.
 * 0 significa «todo el año o sin dato»: no cuenta a la hora de decidir si una receta es de estación.
 */
object Temporada {

    const val TODO_EL_ANIO = 0

    /** Máscara de un rango de meses (1 a 12), que puede cruzar fin de año: rango(11, 2) = nov a feb. */
    fun rango(desde: Int, hasta: Int): Int {
        require(desde in 1..12 && hasta in 1..12) { "Los meses van de 1 a 12" }
        var mascara = 0
        var mes = desde
        while (true) {
            mascara = mascara or bit(mes)
            if (mes == hasta) break
            mes = if (mes == 12) 1 else mes + 1
        }
        return mascara
    }

    fun bit(mes: Int): Int = 1 shl (mes - 1)

    fun incluye(mascara: Int, mes: Int): Boolean = mascara == TODO_EL_ANIO || (mascara and bit(mes)) != 0

    fun alternar(mascara: Int, mes: Int): Int = mascara xor bit(mes)

    fun meses(mascara: Int): List<Int> = (1..12).filter { mascara != 0 && (mascara and bit(it)) != 0 }

    /**
     * Calendario aproximado de Uruguay (hemisferio sur) para las verduras y frutas del catálogo
     * inicial. Lo que está todo el año, o se consigue importado, queda sin dato.
     */
    val CALENDARIO_URUGUAY: Map<String, Int> = mapOf(
        "Boniato" to rango(3, 8),
        "Zapallo" to rango(2, 7),
        "Calabaza" to rango(2, 7),
        "Zapallito" to rango(11, 3),
        "Tomate" to rango(12, 4),
        "Morrón rojo" to rango(12, 4),
        "Morrón verde" to rango(12, 4),
        "Berenjena" to rango(12, 4),
        "Chaucha" to rango(11, 4),
        "Choclo" to rango(12, 3),
        "Arveja" to rango(8, 11),
        "Espinaca" to rango(4, 10),
        "Apio" to rango(5, 11),
        "Puerro" to rango(5, 10),
        "Brócoli" to rango(5, 9),
        "Coliflor" to rango(5, 9),
        "Repollo" to rango(4, 9),
        "Manzana" to rango(2, 6),
        "Naranja" to rango(5, 9),
        "Limón" to rango(4, 10),
        "Frutilla" to rango(9, 12),
        "Durazno" to rango(11, 2),
        "Pera" to rango(1, 5),
        "Membrillo" to rango(3, 5)
    )
}
