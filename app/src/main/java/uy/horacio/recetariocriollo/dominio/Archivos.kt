package uy.horacio.recetariocriollo.dominio

import uy.horacio.recetariocriollo.dominio.modelo.Cocinada
import uy.horacio.recetariocriollo.dominio.modelo.Receta

/** Qué archivos (fotos y audios) usa cada cosa, para borrar los que quedan sin dueño. */
object Archivos {

    fun deReceta(receta: Receta): Set<String> =
        (listOfNotNull(receta.fotoPath) + receta.pasos.mapNotNull { it.fotoPath }).toSet()

    fun deCocinada(cocinada: Cocinada): Set<String> = setOfNotNull(cocinada.fotoPath, cocinada.audioPath)

    /** Al guardar una edición: lo que tenía la versión anterior y la nueva ya no usa. */
    fun sobrantesAlGuardar(anterior: Receta?, nueva: Receta): Set<String> =
        if (anterior == null) emptySet() else deReceta(anterior) - deReceta(nueva)
}
