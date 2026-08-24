package uy.horacio.recetariocriollo.ui.navegacion

import kotlinx.serialization.Serializable

/** Rutas de navegacion con tipos: los argumentos viajan como propiedades, sin strings sueltos. */

@Serializable
data object RutaRecetas

@Serializable
data object RutaBusqueda

@Serializable
data object RutaConversor

@Serializable
data object RutaCronometros

@Serializable
data class RutaDetalleReceta(val recetaId: Long)

@Serializable
data class RutaEditorReceta(val recetaId: Long = 0L)
