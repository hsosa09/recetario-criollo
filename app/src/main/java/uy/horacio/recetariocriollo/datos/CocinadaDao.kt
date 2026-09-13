package uy.horacio.recetariocriollo.datos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CocinadaDao {

    @Insert
    suspend fun insertar(cocinada: CocinadaEntity): Long

    @Query("DELETE FROM cocinadas WHERE id = :id")
    suspend fun borrar(id: Long)

    @Query("SELECT * FROM cocinadas WHERE recetaId = :recetaId ORDER BY fechaMillis DESC")
    fun observarDeReceta(recetaId: Long): Flow<List<CocinadaEntity>>

    @Query(
        """
        SELECT c.*, r.nombre AS nombreReceta
        FROM cocinadas c INNER JOIN recetas r ON r.id = c.recetaId
        ORDER BY c.fechaMillis DESC
        """
    )
    fun observarTodas(): Flow<List<CocinadaConNombreFila>>

    /** Veces, promedio y ultima fecha por receta, resuelto en SQLite. */
    @Query(
        """
        SELECT recetaId, COUNT(*) AS veces, AVG(estrellas) AS promedioEstrellas,
               MAX(fechaMillis) AS ultimaMillis
        FROM cocinadas GROUP BY recetaId
        """
    )
    fun observarResumenes(): Flow<List<ResumenCocinadasFila>>
}
