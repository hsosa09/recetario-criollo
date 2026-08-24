package uy.horacio.recetariocriollo.datos

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import uy.horacio.recetariocriollo.dominio.modelo.CategoriaIngrediente
import uy.horacio.recetariocriollo.dominio.modelo.CategoriaReceta
import uy.horacio.recetariocriollo.dominio.modelo.ReglaEscalado
import uy.horacio.recetariocriollo.dominio.modelo.Unidad

@Entity(tableName = "recetas")
data class RecetaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val categoria: CategoriaReceta,
    val porcionesBase: Int,
    val tiempoMinutos: Int?,
    val notas: String?,
    val fotoPath: String?,
    val esFavorita: Boolean = false,
    val creadaEn: Long = System.currentTimeMillis(),
    val modificadaEn: Long = System.currentTimeMillis()
)

/** Catalogo normalizado: los ingredientes viven una sola vez y se reusan entre recetas. */
@Entity(
    tableName = "ingredientes",
    indices = [Index(value = ["nombre"], unique = true)]
)
data class IngredienteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val categoria: CategoriaIngrediente,
    val densidadGramosPorTaza: Double?,
    val esSalOEspecia: Boolean = false,
    val esBasicoDeDespensa: Boolean = false,
    val unidadHabitual: Unidad = Unidad.GRAMO
)

@Entity(
    tableName = "receta_ingredientes",
    foreignKeys = [
        ForeignKey(
            entity = RecetaEntity::class,
            parentColumns = ["id"],
            childColumns = ["recetaId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = IngredienteEntity::class,
            parentColumns = ["id"],
            childColumns = ["ingredienteId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("recetaId"), Index("ingredienteId")]
)
data class RecetaIngredienteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recetaId: Long,
    val ingredienteId: Long,
    val cantidad: Double,
    val unidad: Unidad,
    val regla: ReglaEscalado = ReglaEscalado.LINEAL,
    val aclaracion: String? = null,
    val orden: Int = 0
)

@Entity(
    tableName = "pasos",
    foreignKeys = [
        ForeignKey(
            entity = RecetaEntity::class,
            parentColumns = ["id"],
            childColumns = ["recetaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("recetaId")]
)
data class PasoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recetaId: Long,
    val orden: Int,
    val texto: String,
    val timerSugeridoSegundos: Int? = null
)

/** Fila de receta_ingredientes junto con la ficha del catalogo. */
data class IngredienteDeRecetaConCatalogo(
    @Embedded val cruce: RecetaIngredienteEntity,
    @Relation(parentColumn = "ingredienteId", entityColumn = "id")
    val ingrediente: IngredienteEntity
)

/** Receta con todo lo que cuelga de ella. Es lo que consume la UI. */
data class RecetaCompletaEntity(
    @Embedded val receta: RecetaEntity,
    @Relation(
        entity = RecetaIngredienteEntity::class,
        parentColumn = "id",
        entityColumn = "recetaId"
    )
    val ingredientes: List<IngredienteDeRecetaConCatalogo>,
    @Relation(parentColumn = "id", entityColumn = "recetaId")
    val pasos: List<PasoEntity>
)
