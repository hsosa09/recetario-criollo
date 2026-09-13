package uy.horacio.recetariocriollo.datos

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import uy.horacio.recetariocriollo.dominio.Archivos
import uy.horacio.recetariocriollo.dominio.modelo.Cocinada
import uy.horacio.recetariocriollo.dominio.modelo.CocinadaConReceta
import uy.horacio.recetariocriollo.dominio.modelo.ResumenCocinadas

/** Historial de cocinadas: cuantas veces, como salio y que cambiar la proxima. */
class CocinadaRepositorio(
    private val dao: CocinadaDao,
    private val almacenFotos: AlmacenFotos
) {

    suspend fun registrar(cocinada: Cocinada): Long = dao.insertar(cocinada.aEntidad())

    /** Borra la anotación y su foto y nota de voz. */
    suspend fun borrar(id: Long) {
        val archivos = dao.obtener(id)?.aDominio()?.let { Archivos.deCocinada(it) }.orEmpty()
        dao.borrar(id)
        archivos.forEach { almacenFotos.borrar(it) }
    }

    fun observarDeReceta(recetaId: Long): Flow<List<Cocinada>> =
        dao.observarDeReceta(recetaId).map { lista -> lista.map { it.aDominio() } }

    fun observarTodas(): Flow<List<CocinadaConReceta>> =
        dao.observarTodas().map { lista -> lista.map { it.aDominio() } }

    /** Resumen por receta, indexado por id. Las recetas nunca cocinadas no aparecen. */
    fun observarResumenes(): Flow<Map<Long, ResumenCocinadas>> =
        dao.observarResumenes().map { filas -> filas.associate { it.recetaId to it.aDominio() } }
}
