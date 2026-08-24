package uy.horacio.recetariocriollo.datos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredienteDao {

    @Query("SELECT * FROM ingredientes ORDER BY nombre COLLATE NOCASE ASC")
    fun observarTodos(): Flow<List<IngredienteEntity>>

    @Query("SELECT * FROM ingredientes WHERE densidadGramosPorTaza IS NOT NULL ORDER BY nombre COLLATE NOCASE ASC")
    fun observarConDensidad(): Flow<List<IngredienteEntity>>

    @Query("SELECT * FROM ingredientes WHERE id = :id")
    suspend fun obtenerPorId(id: Long): IngredienteEntity?

    @Query("SELECT * FROM ingredientes WHERE nombre = :nombre COLLATE NOCASE LIMIT 1")
    suspend fun buscarPorNombre(nombre: String): IngredienteEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(ingrediente: IngredienteEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarTodos(ingredientes: List<IngredienteEntity>)

    @Update
    suspend fun actualizar(ingrediente: IngredienteEntity)

    @Query("DELETE FROM ingredientes WHERE id = :id")
    suspend fun borrar(id: Long)

    @Query("SELECT COUNT(*) FROM ingredientes")
    suspend fun cantidad(): Int
}
