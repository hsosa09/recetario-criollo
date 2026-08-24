package uy.horacio.recetariocriollo.datos

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import uy.horacio.recetariocriollo.dominio.modelo.Receta

/** Unico punto de entrada a las recetas. La UI no toca DAOs ni entidades. */
class RecetaRepositorio(
    private val recetaDao: RecetaDao,
    private val almacenFotos: AlmacenFotos
) {

    fun observarRecetas(): Flow<List<Receta>> =
        recetaDao.observarTodas().map { lista -> lista.map { it.aDominio() } }

    fun observarReceta(id: Long): Flow<Receta?> =
        recetaDao.observarPorId(id).map { it?.aDominio() }

    suspend fun obtenerReceta(id: Long): Receta? = recetaDao.obtenerPorId(id)?.aDominio()

    /** Guarda alta o edicion. Devuelve el id de la receta guardada. */
    suspend fun guardar(receta: Receta): Long {
        val fotoAnterior = if (receta.id != 0L) recetaDao.obtenerPorId(receta.id)?.receta?.fotoPath else null
        val id = recetaDao.guardarCompleta(
            receta = receta.aEntidad(),
            ingredientes = receta.ingredientes.map { it.aEntidad(receta.id) },
            pasos = receta.pasos.mapIndexed { indice, paso -> paso.copy(orden = indice).aEntidad(receta.id) }
        )
        // Si se cambio la foto, la vieja ya no le sirve a nadie.
        if (fotoAnterior != null && fotoAnterior != receta.fotoPath) {
            almacenFotos.borrar(fotoAnterior)
        }
        return id
    }

    suspend fun borrar(id: Long) {
        val foto = recetaDao.obtenerPorId(id)?.receta?.fotoPath
        recetaDao.borrarReceta(id)
        foto?.let { almacenFotos.borrar(it) }
    }

    suspend fun alternarFavorita(id: Long, favorita: Boolean) =
        recetaDao.marcarFavorita(id, favorita)
}
