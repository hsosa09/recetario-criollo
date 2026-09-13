package uy.horacio.recetariocriollo.datos

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import uy.horacio.recetariocriollo.dominio.Archivos
import uy.horacio.recetariocriollo.dominio.modelo.Receta

/** Unico punto de entrada a las recetas. La UI no toca DAOs ni entidades. */
class RecetaRepositorio(
    private val recetaDao: RecetaDao,
    private val cocinadaDao: CocinadaDao,
    private val almacenFotos: AlmacenFotos
) {

    fun observarRecetas(): Flow<List<Receta>> =
        recetaDao.observarTodas().map { lista -> lista.map { it.aDominio() } }

    fun observarReceta(id: Long): Flow<Receta?> =
        recetaDao.observarPorId(id).map { it?.aDominio() }

    suspend fun obtenerReceta(id: Long): Receta? = recetaDao.obtenerPorId(id)?.aDominio()

    /** Guarda alta o edicion. Devuelve el id de la receta guardada. */
    suspend fun guardar(receta: Receta): Long {
        val anterior = if (receta.id != 0L) recetaDao.obtenerPorId(receta.id)?.aDominio() else null
        val id = recetaDao.guardarCompleta(
            receta = receta.aEntidad(),
            ingredientes = receta.ingredientes.map { it.aEntidad(receta.id) },
            pasos = receta.pasos.mapIndexed { indice, paso -> paso.copy(orden = indice).aEntidad(receta.id) }
        )
        // Fotos de portada o de pasos que cambiaron o se quitaron ya no le sirven a nadie.
        Archivos.sobrantesAlGuardar(anterior, receta).forEach { almacenFotos.borrar(it) }
        return id
    }

    suspend fun borrar(id: Long) {
        // Portada, fotos de pasos y la foto y audio de cada cocinada (que se borran en cascada).
        val archivos = recetaDao.obtenerPorId(id)?.aDominio()?.let { Archivos.deReceta(it) }.orEmpty() +
            cocinadaDao.deReceta(id).flatMap { Archivos.deCocinada(it.aDominio()) }
        recetaDao.borrarConVariantesSueltas(id)
        archivos.forEach { almacenFotos.borrar(it) }
    }

    suspend fun alternarFavorita(id: Long, favorita: Boolean) =
        recetaDao.marcarFavorita(id, favorita)
}
