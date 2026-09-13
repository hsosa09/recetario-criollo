package uy.horacio.recetariocriollo.datos

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

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

    val TODAS = arrayOf(DE_1_A_2)
}
