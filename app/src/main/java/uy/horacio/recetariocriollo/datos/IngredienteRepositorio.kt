package uy.horacio.recetariocriollo.datos

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import uy.horacio.recetariocriollo.dominio.Texto
import uy.horacio.recetariocriollo.dominio.modelo.CategoriaIngrediente
import uy.horacio.recetariocriollo.dominio.modelo.Ingrediente
import uy.horacio.recetariocriollo.dominio.modelo.Unidad

/**
 * Catalogo de ingredientes. Es la unica forma de crear ingredientes en la app:
 * nada de texto libre suelto en las recetas.
 */
class IngredienteRepositorio(
    private val ingredienteDao: IngredienteDao,
    private val recetaDao: RecetaDao
) {

    fun observarCatalogo(): Flow<List<Ingrediente>> =
        ingredienteDao.observarTodos().map { lista -> lista.map { it.aDominio() } }

    fun observarConDensidad(): Flow<List<Ingrediente>> =
        ingredienteDao.observarConDensidad().map { lista -> lista.map { it.aDominio() } }

    /**
     * Da de alta un ingrediente. Si ya existe uno con el mismo nombre (sin distinguir
     * mayusculas ni tildes) devuelve ese, asi el catalogo no se llena de duplicados.
     */
    suspend fun crearSiNoExiste(
        nombre: String,
        categoria: CategoriaIngrediente,
        densidadGramosPorTaza: Double? = null,
        esSalOEspecia: Boolean = false,
        unidadHabitual: Unidad = Unidad.GRAMO
    ): Ingrediente {
        val limpio = nombre.trim().replace(Regex("\\s+"), " ")
        ingredienteDao.buscarPorNombre(limpio)?.let { return it.aDominio() }
        // "Azucar" y "azúcar" son el mismo ingrediente que "Azúcar": no se duplica.
        ingredienteDao.observarTodos().first()
            .firstOrNull { Texto.mismoNombre(it.nombre, limpio) }
            ?.let { return it.aDominio() }
        val nuevo = IngredienteEntity(
            nombre = limpio,
            categoria = categoria,
            densidadGramosPorTaza = densidadGramosPorTaza,
            esSalOEspecia = esSalOEspecia,
            unidadHabitual = unidadHabitual
        )
        val id = ingredienteDao.insertar(nuevo)
        val guardado = if (id > 0) nuevo.copy(id = id) else ingredienteDao.buscarPorNombre(limpio)!!
        return guardado.aDominio()
    }

    suspend fun actualizar(ingrediente: Ingrediente) =
        ingredienteDao.actualizar(ingrediente.aEntidad())

    /** No se puede borrar algo que alguna receta usa: devuelve false y no toca nada. */
    suspend fun borrarSiNoSeUsa(id: Long): Boolean {
        if (recetaDao.vecesUsado(id) > 0) return false
        ingredienteDao.borrar(id)
        return true
    }

    suspend fun vecesUsado(id: Long): Int = recetaDao.vecesUsado(id)
}
