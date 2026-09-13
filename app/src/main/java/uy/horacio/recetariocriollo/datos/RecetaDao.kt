package uy.horacio.recetariocriollo.datos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RecetaDao {

    @Transaction
    @Query("SELECT * FROM recetas ORDER BY esFavorita DESC, nombre COLLATE NOCASE ASC")
    fun observarTodas(): Flow<List<RecetaCompletaEntity>>

    @Transaction
    @Query("SELECT * FROM recetas WHERE id = :id")
    fun observarPorId(id: Long): Flow<RecetaCompletaEntity?>

    @Transaction
    @Query("SELECT * FROM recetas WHERE id = :id")
    suspend fun obtenerPorId(id: Long): RecetaCompletaEntity?

    @Insert
    suspend fun insertarReceta(receta: RecetaEntity): Long

    @Update
    suspend fun actualizarReceta(receta: RecetaEntity)

    @Query("DELETE FROM recetas WHERE id = :id")
    suspend fun borrarReceta(id: Long)

    /** Sin FK en origenId (SQLite no deja agregarla con ALTER): las variantes se desvinculan a mano. */
    @Query("UPDATE recetas SET origenId = NULL WHERE origenId = :id")
    suspend fun desvincularVariantes(id: Long)

    @Transaction
    suspend fun borrarConVariantesSueltas(id: Long) {
        desvincularVariantes(id)
        borrarReceta(id)
    }

    @Query("UPDATE recetas SET esFavorita = :favorita, modificadaEn = :momento WHERE id = :id")
    suspend fun marcarFavorita(id: Long, favorita: Boolean, momento: Long = System.currentTimeMillis())

    @Insert
    suspend fun insertarIngredientes(filas: List<RecetaIngredienteEntity>)

    @Query("DELETE FROM receta_ingredientes WHERE recetaId = :recetaId")
    suspend fun borrarIngredientesDe(recetaId: Long)

    @Insert
    suspend fun insertarPasos(pasos: List<PasoEntity>)

    @Query("DELETE FROM pasos WHERE recetaId = :recetaId")
    suspend fun borrarPasosDe(recetaId: Long)

    @Query("SELECT COUNT(*) FROM receta_ingredientes WHERE ingredienteId = :ingredienteId")
    suspend fun vecesUsado(ingredienteId: Long): Int

    @Query("SELECT COUNT(*) FROM recetas")
    suspend fun cantidadRecetas(): Int

    /**
     * Guarda la receta entera de una: cabecera, ingredientes y pasos.
     * Reemplaza los hijos en vez de intentar un diff, que para el volumen de datos
     * de un recetario personal no aporta nada.
     */
    @Transaction
    suspend fun guardarCompleta(
        receta: RecetaEntity,
        ingredientes: List<RecetaIngredienteEntity>,
        pasos: List<PasoEntity>
    ): Long {
        val id = if (receta.id == 0L) {
            insertarReceta(receta)
        } else {
            actualizarReceta(receta.copy(modificadaEn = System.currentTimeMillis()))
            borrarIngredientesDe(receta.id)
            borrarPasosDe(receta.id)
            receta.id
        }
        insertarIngredientes(
            ingredientes.mapIndexed { indice, fila ->
                fila.copy(id = 0, recetaId = id, orden = indice)
            }
        )
        insertarPasos(
            pasos.mapIndexed { indice, paso ->
                paso.copy(id = 0, recetaId = id, orden = indice)
            }
        )
        return id
    }
}
