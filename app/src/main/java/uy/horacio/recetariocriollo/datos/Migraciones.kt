package uy.horacio.recetariocriollo.datos

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import uy.horacio.recetariocriollo.dominio.Temporada

/**
 * Migraciones escritas a mano. Nunca fallbackToDestructiveMigration: son recetas de la
 * gente. Cada una se prueba en MigracionesTest (androidTest) contra el esquema exportado.
 */
object Migraciones {

    /** v2: dificultad y molde en recetas, y el historial de cocinadas. */
    val DE_1_A_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `recetas` ADD COLUMN `dificultad` TEXT")
            db.execSQL("ALTER TABLE `recetas` ADD COLUMN `moldeCm` INTEGER")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `cocinadas` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `recetaId` INTEGER NOT NULL,
                    `fechaMillis` INTEGER NOT NULL,
                    `estrellas` INTEGER NOT NULL,
                    `porciones` INTEGER NOT NULL,
                    `nota` TEXT,
                    FOREIGN KEY(`recetaId`) REFERENCES `recetas`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_cocinadas_recetaId` ON `cocinadas` (`recetaId`)")
        }
    }

    /** v3: estaciones, variantes, foto por paso y foto y audio de cada cocinada. */
    val DE_2_A_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `ingredientes` ADD COLUMN `meses` INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE `recetas` ADD COLUMN `origenId` INTEGER")
            db.execSQL("ALTER TABLE `pasos` ADD COLUMN `fotoPath` TEXT")
            db.execSQL("ALTER TABLE `cocinadas` ADD COLUMN `fotoPath` TEXT")
            db.execSQL("ALTER TABLE `cocinadas` ADD COLUMN `audioPath` TEXT")
            // El catálogo inicial ya viene con meses; a los que instalaron antes se les completa.
            // Solo si siguen en 0, por si alguien ya editó ese ingrediente a mano en el futuro.
            Temporada.CALENDARIO_URUGUAY.forEach { (nombre, meses) ->
                db.execSQL("UPDATE `ingredientes` SET `meses` = ? WHERE `nombre` = ? AND `meses` = 0", arrayOf<Any>(meses, nombre))
            }
        }
    }

    val TODAS = arrayOf(DE_1_A_2, DE_2_A_3)
}
