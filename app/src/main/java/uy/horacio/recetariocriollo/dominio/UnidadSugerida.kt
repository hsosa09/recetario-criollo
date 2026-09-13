package uy.horacio.recetariocriollo.dominio

import uy.horacio.recetariocriollo.dominio.modelo.Ingrediente
import uy.horacio.recetariocriollo.dominio.modelo.PreferenciaUnidades
import uy.horacio.recetariocriollo.dominio.modelo.Unidad

/** La unidad que el editor propone al agregar un ingrediente, según el ajuste de unidades. */
object UnidadSugerida {

    fun para(ingrediente: Ingrediente, preferencia: PreferenciaUnidades): Unidad {
        val habitual = ingrediente.unidadHabitual
        if (preferencia == PreferenciaUnidades.METRICAS) return habitual
        return when (habitual) {
            // Pasar gramos a tazas solo tiene sentido si se sabe cuánto pesa una taza.
            Unidad.GRAMO -> if (ingrediente.densidadGramosPorTaza != null) Unidad.TAZA else Unidad.GRAMO
            Unidad.MILILITRO -> Unidad.TAZA
            else -> habitual
        }
    }
}
