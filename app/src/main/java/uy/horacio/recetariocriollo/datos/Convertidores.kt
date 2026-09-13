package uy.horacio.recetariocriollo.datos

import androidx.room.TypeConverter
import uy.horacio.recetariocriollo.dominio.modelo.CategoriaIngrediente
import uy.horacio.recetariocriollo.dominio.modelo.CategoriaReceta
import uy.horacio.recetariocriollo.dominio.modelo.Dificultad
import uy.horacio.recetariocriollo.dominio.modelo.ReglaEscalado
import uy.horacio.recetariocriollo.dominio.modelo.Unidad

/** Los enums se guardan por nombre: sobreviven a que se agregue un valor en el medio. */
class Convertidores {

    @TypeConverter
    fun deCategoriaReceta(valor: CategoriaReceta): String = valor.name

    @TypeConverter
    fun aCategoriaReceta(valor: String): CategoriaReceta =
        runCatching { CategoriaReceta.valueOf(valor) }.getOrDefault(CategoriaReceta.OTRA)

    @TypeConverter
    fun deCategoriaIngrediente(valor: CategoriaIngrediente): String = valor.name

    @TypeConverter
    fun aCategoriaIngrediente(valor: String): CategoriaIngrediente =
        runCatching { CategoriaIngrediente.valueOf(valor) }.getOrDefault(CategoriaIngrediente.OTROS)

    @TypeConverter
    fun deUnidad(valor: Unidad): String = valor.name

    @TypeConverter
    fun aUnidad(valor: String): Unidad =
        runCatching { Unidad.valueOf(valor) }.getOrDefault(Unidad.GRAMO)

    @TypeConverter
    fun deDificultad(valor: Dificultad?): String? = valor?.name

    @TypeConverter
    fun aDificultad(valor: String?): Dificultad? =
        valor?.let { runCatching { Dificultad.valueOf(it) }.getOrNull() }

    @TypeConverter
    fun deRegla(valor: ReglaEscalado): String = valor.name

    @TypeConverter
    fun aRegla(valor: String): ReglaEscalado =
        runCatching { ReglaEscalado.valueOf(valor) }.getOrDefault(ReglaEscalado.LINEAL)
}
