package uy.horacio.recetariocriollo.dominio.modelo

/** Familia de la unidad. Solo se puede convertir libremente dentro de la misma familia;
 *  cruzar volumen y peso exige conocer la densidad del ingrediente. */
enum class TipoUnidad { VOLUMEN, PESO, CONTEO }

/**
 * Unidades de cocina usadas en la app.
 *
 * [factorBase] expresa cuanto vale una unidad en la unidad base de su familia:
 * mililitros para VOLUMEN y gramos para PESO. Las de CONTEO no se convierten.
 */
enum class Unidad(
    val abreviatura: String,
    val singular: String,
    val plural: String,
    val tipo: TipoUnidad,
    val factorBase: Double,
    /** Las cantidades de esta unidad se muestran mejor como fraccion (1/2 taza) que como decimal. */
    val prefiereFracciones: Boolean = false
) {
    MILILITRO("ml", "mililitro", "mililitros", TipoUnidad.VOLUMEN, 1.0),
    LITRO("l", "litro", "litros", TipoUnidad.VOLUMEN, 1000.0, prefiereFracciones = true),
    CUCHARADITA("cdta", "cucharadita", "cucharaditas", TipoUnidad.VOLUMEN, 5.0, prefiereFracciones = true),
    CUCHARADA("cda", "cucharada", "cucharadas", TipoUnidad.VOLUMEN, 15.0, prefiereFracciones = true),
    TAZA("taza", "taza", "tazas", TipoUnidad.VOLUMEN, 240.0, prefiereFracciones = true),
    GRAMO("g", "gramo", "gramos", TipoUnidad.PESO, 1.0),
    KILOGRAMO("kg", "kilogramo", "kilogramos", TipoUnidad.PESO, 1000.0),
    ONZA("oz", "onza", "onzas", TipoUnidad.PESO, 28.349523125),
    LIBRA("lb", "libra", "libras", TipoUnidad.PESO, 453.59237),
    UNIDAD("u", "unidad", "unidades", TipoUnidad.CONTEO, 1.0, prefiereFracciones = true),
    PIZCA("pizca", "pizca", "pizcas", TipoUnidad.CONTEO, 1.0),
    A_GUSTO("a gusto", "a gusto", "a gusto", TipoUnidad.CONTEO, 0.0);

    /** "a gusto" no lleva numero adelante. */
    val sinCantidad: Boolean get() = this == A_GUSTO

    companion object {
        val deVolumen: List<Unidad> get() = entries.filter { it.tipo == TipoUnidad.VOLUMEN }
        val dePeso: List<Unidad> get() = entries.filter { it.tipo == TipoUnidad.PESO }
    }
}

/**
 * Como se comporta un ingrediente cuando se cambian las porciones.
 * Los textos visibles estan en strings.xml (ver ui.Etiquetas).
 */
enum class ReglaEscalado { LINEAL, ATENUADA, FIJA }

/** Categorias de receta. */
enum class CategoriaReceta {
    ENTRADA,
    PLATO_PRINCIPAL,
    GUARNICION,
    SOPA_Y_GUISO,
    POSTRE,
    PANIFICADOS,
    SALSA_Y_ADEREZO,
    BEBIDA,
    CONSERVA,
    OTRA
}

/** Categorias del catalogo de ingredientes, usadas para agrupar el selector por lista. */
enum class CategoriaIngrediente {
    VERDURAS,
    FRUTAS,
    CARNES,
    PESCADOS,
    LACTEOS,
    HARINAS,
    LEGUMBRES,
    CONDIMENTOS,
    ENDULZANTES,
    GRASAS,
    BEBIDAS,
    OTROS
}
