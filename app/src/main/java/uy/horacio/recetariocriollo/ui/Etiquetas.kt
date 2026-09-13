package uy.horacio.recetariocriollo.ui

import androidx.annotation.StringRes
import uy.horacio.recetariocriollo.R
import uy.horacio.recetariocriollo.dominio.NivelHorno
import uy.horacio.recetariocriollo.dominio.modelo.CategoriaIngrediente
import uy.horacio.recetariocriollo.dominio.modelo.CategoriaReceta
import uy.horacio.recetariocriollo.dominio.modelo.Dificultad
import uy.horacio.recetariocriollo.dominio.modelo.ReglaEscalado

/**
 * Textos visibles de los enums del dominio. Viven aca para que el dominio
 * quede sin dependencias de Android y los textos queden en strings.xml.
 */

@get:StringRes
val CategoriaReceta.textoId: Int
    get() = when (this) {
        CategoriaReceta.ENTRADA -> R.string.categoria_entrada
        CategoriaReceta.PLATO_PRINCIPAL -> R.string.categoria_plato_principal
        CategoriaReceta.GUARNICION -> R.string.categoria_guarnicion
        CategoriaReceta.SOPA_Y_GUISO -> R.string.categoria_sopa_guiso
        CategoriaReceta.POSTRE -> R.string.categoria_postre
        CategoriaReceta.PANIFICADOS -> R.string.categoria_panificados
        CategoriaReceta.SALSA_Y_ADEREZO -> R.string.categoria_salsa
        CategoriaReceta.BEBIDA -> R.string.categoria_bebida
        CategoriaReceta.CONSERVA -> R.string.categoria_conserva
        CategoriaReceta.OTRA -> R.string.categoria_otra
    }

@get:StringRes
val CategoriaIngrediente.textoId: Int
    get() = when (this) {
        CategoriaIngrediente.VERDURAS -> R.string.categoria_ing_verduras
        CategoriaIngrediente.FRUTAS -> R.string.categoria_ing_frutas
        CategoriaIngrediente.CARNES -> R.string.categoria_ing_carnes
        CategoriaIngrediente.PESCADOS -> R.string.categoria_ing_pescados
        CategoriaIngrediente.LACTEOS -> R.string.categoria_ing_lacteos
        CategoriaIngrediente.HARINAS -> R.string.categoria_ing_harinas
        CategoriaIngrediente.LEGUMBRES -> R.string.categoria_ing_legumbres
        CategoriaIngrediente.CONDIMENTOS -> R.string.categoria_ing_condimentos
        CategoriaIngrediente.ENDULZANTES -> R.string.categoria_ing_endulzantes
        CategoriaIngrediente.GRASAS -> R.string.categoria_ing_grasas
        CategoriaIngrediente.BEBIDAS -> R.string.categoria_ing_bebidas
        CategoriaIngrediente.OTROS -> R.string.categoria_ing_otros
    }

@get:StringRes
val ReglaEscalado.textoId: Int
    get() = when (this) {
        ReglaEscalado.LINEAL -> R.string.regla_lineal
        ReglaEscalado.ATENUADA -> R.string.regla_atenuada
        ReglaEscalado.FIJA -> R.string.regla_fija
    }

@get:StringRes
val ReglaEscalado.detalleId: Int
    get() = when (this) {
        ReglaEscalado.LINEAL -> R.string.regla_lineal_detalle
        ReglaEscalado.ATENUADA -> R.string.regla_atenuada_detalle
        ReglaEscalado.FIJA -> R.string.regla_fija_detalle
    }

@get:StringRes
val NivelHorno.textoId: Int
    get() = when (this) {
        NivelHorno.MUY_SUAVE -> R.string.horno_muy_suave
        NivelHorno.SUAVE -> R.string.horno_suave
        NivelHorno.MODERADO -> R.string.horno_moderado
        NivelHorno.FUERTE -> R.string.horno_fuerte
        NivelHorno.MUY_FUERTE -> R.string.horno_muy_fuerte
    }

@get:StringRes
val Dificultad.textoId: Int
    get() = when (this) {
        Dificultad.FACIL -> R.string.dificultad_facil
        Dificultad.MEDIA -> R.string.dificultad_media
        Dificultad.DIFICIL -> R.string.dificultad_dificil
    }
