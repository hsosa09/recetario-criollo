package uy.horacio.recetariocriollo.datos

import uy.horacio.recetariocriollo.dominio.modelo.CategoriaIngrediente
import uy.horacio.recetariocriollo.dominio.modelo.CategoriaIngrediente.BEBIDAS
import uy.horacio.recetariocriollo.dominio.modelo.CategoriaIngrediente.CARNES
import uy.horacio.recetariocriollo.dominio.modelo.CategoriaIngrediente.CONDIMENTOS
import uy.horacio.recetariocriollo.dominio.modelo.CategoriaIngrediente.ENDULZANTES
import uy.horacio.recetariocriollo.dominio.modelo.CategoriaIngrediente.FRUTAS
import uy.horacio.recetariocriollo.dominio.modelo.CategoriaIngrediente.GRASAS
import uy.horacio.recetariocriollo.dominio.modelo.CategoriaIngrediente.HARINAS
import uy.horacio.recetariocriollo.dominio.modelo.CategoriaIngrediente.LACTEOS
import uy.horacio.recetariocriollo.dominio.modelo.CategoriaIngrediente.LEGUMBRES
import uy.horacio.recetariocriollo.dominio.modelo.CategoriaIngrediente.OTROS
import uy.horacio.recetariocriollo.dominio.modelo.CategoriaIngrediente.PESCADOS
import uy.horacio.recetariocriollo.dominio.modelo.CategoriaIngrediente.VERDURAS
import uy.horacio.recetariocriollo.dominio.Temporada
import uy.horacio.recetariocriollo.dominio.modelo.Unidad

/**
 * Catalogo de ingredientes con el que arranca la app y un par de recetas de ejemplo.
 *
 * Las densidades son gramos por taza de 240 ml, medidas al ras. Salen de tablas de
 * cocina; alcanzan para convertir de taza a gramos sin sacar la balanza.
 */
object CatalogoInicial {

    private fun ing(
        nombre: String,
        categoria: CategoriaIngrediente,
        densidad: Double? = null,
        especia: Boolean = false,
        basico: Boolean = false,
        unidad: Unidad = Unidad.GRAMO
    ) = IngredienteEntity(
        nombre = nombre,
        categoria = categoria,
        densidadGramosPorTaza = densidad,
        esSalOEspecia = especia,
        esBasicoDeDespensa = basico,
        unidadHabitual = unidad,
        meses = Temporada.CALENDARIO_URUGUAY[nombre] ?: Temporada.TODO_EL_ANIO
    )

    val ingredientes: List<IngredienteEntity> = listOf(
        // --- Verduras ---
        ing("Papa", VERDURAS, unidad = Unidad.UNIDAD),
        ing("Boniato", VERDURAS, unidad = Unidad.UNIDAD),
        ing("Zanahoria", VERDURAS, unidad = Unidad.UNIDAD),
        ing("Cebolla", VERDURAS, unidad = Unidad.UNIDAD),
        ing("Ajo", VERDURAS, unidad = Unidad.UNIDAD),
        ing("Morrón rojo", VERDURAS, unidad = Unidad.UNIDAD),
        ing("Morrón verde", VERDURAS, unidad = Unidad.UNIDAD),
        ing("Tomate", VERDURAS, unidad = Unidad.UNIDAD),
        ing("Zapallo", VERDURAS, 245.0),
        ing("Zapallito", VERDURAS, unidad = Unidad.UNIDAD),
        ing("Chaucha", VERDURAS, 125.0),
        ing("Arveja", VERDURAS, 145.0),
        ing("Choclo", VERDURAS, 165.0),
        ing("Lechuga", VERDURAS, unidad = Unidad.UNIDAD),
        ing("Espinaca", VERDURAS, 30.0),
        ing("Acelga", VERDURAS, 35.0),
        ing("Apio", VERDURAS, 100.0),
        ing("Puerro", VERDURAS, unidad = Unidad.UNIDAD),
        ing("Remolacha", VERDURAS, unidad = Unidad.UNIDAD),
        ing("Berenjena", VERDURAS, unidad = Unidad.UNIDAD),
        ing("Brócoli", VERDURAS, 90.0),
        ing("Coliflor", VERDURAS, 100.0),
        ing("Repollo", VERDURAS, 90.0),
        ing("Champiñones", VERDURAS, 70.0),
        ing("Calabaza", VERDURAS, 245.0),

        // --- Frutas ---
        ing("Manzana", FRUTAS, unidad = Unidad.UNIDAD),
        ing("Banana", FRUTAS, unidad = Unidad.UNIDAD),
        ing("Naranja", FRUTAS, unidad = Unidad.UNIDAD),
        ing("Limón", FRUTAS, unidad = Unidad.UNIDAD),
        ing("Frutilla", FRUTAS, 150.0),
        ing("Durazno", FRUTAS, unidad = Unidad.UNIDAD),
        ing("Pera", FRUTAS, unidad = Unidad.UNIDAD),
        ing("Membrillo", FRUTAS, unidad = Unidad.UNIDAD),
        ing("Ananá", FRUTAS, 165.0),
        ing("Pasas de uva", FRUTAS, 150.0),

        // --- Carnes ---
        ing("Carne picada", CARNES),
        ing("Nalga", CARNES),
        ing("Peceto", CARNES),
        ing("Asado de tira", CARNES),
        ing("Pulpa de cerdo", CARNES),
        ing("Panceta", CARNES),
        ing("Pollo entero", CARNES, unidad = Unidad.UNIDAD),
        ing("Pechuga de pollo", CARNES),
        ing("Muslo de pollo", CARNES, unidad = Unidad.UNIDAD),
        ing("Chorizo", CARNES, unidad = Unidad.UNIDAD),
        ing("Morcilla", CARNES, unidad = Unidad.UNIDAD),
        ing("Jamón", CARNES),
        ing("Mondongo", CARNES),
        ing("Cordero", CARNES),

        // --- Pescados ---
        ing("Merluza", PESCADOS),
        ing("Atún en lata", PESCADOS, unidad = Unidad.UNIDAD),
        ing("Pescadilla", PESCADOS),

        // --- Lacteos y huevos ---
        ing("Leche", LACTEOS, 245.0, unidad = Unidad.MILILITRO),
        ing("Huevo", LACTEOS, unidad = Unidad.UNIDAD),
        ing("Manteca", LACTEOS, 227.0),
        ing("Queso rallado", LACTEOS, 100.0),
        ing("Muzzarella", LACTEOS, 110.0),
        ing("Queso dambo", LACTEOS),
        ing("Crema de leche", LACTEOS, 240.0, unidad = Unidad.MILILITRO),
        ing("Ricota", LACTEOS, 250.0),
        ing("Yogur natural", LACTEOS, 245.0),
        ing("Dulce de leche", LACTEOS, 300.0),

        // --- Harinas y cereales ---
        ing("Harina 0000", HARINAS, 120.0),
        ing("Harina 000", HARINAS, 120.0),
        ing("Harina integral", HARINAS, 130.0),
        ing("Harina leudante", HARINAS, 120.0),
        ing("Fécula de maíz", HARINAS, 120.0),
        ing("Polenta", HARINAS, 160.0),
        ing("Semolín", HARINAS, 165.0),
        ing("Arroz", HARINAS, 195.0),
        ing("Fideos", HARINAS),
        ing("Avena", HARINAS, 90.0),
        ing("Pan rallado", HARINAS, 110.0),
        ing("Galletitas María", HARINAS, unidad = Unidad.UNIDAD),

        // --- Legumbres ---
        ing("Lentejas", LEGUMBRES, 200.0),
        ing("Porotos", LEGUMBRES, 190.0),
        ing("Garbanzos", LEGUMBRES, 200.0),
        ing("Arvejas secas", LEGUMBRES, 200.0),

        // --- Condimentos y especias ---
        ing("Sal fina", CONDIMENTOS, 290.0, especia = true, basico = true, unidad = Unidad.CUCHARADITA),
        ing("Sal gruesa", CONDIMENTOS, 250.0, especia = true, basico = true, unidad = Unidad.CUCHARADITA),
        ing("Pimienta negra", CONDIMENTOS, 130.0, especia = true, basico = true, unidad = Unidad.CUCHARADITA),
        ing("Ají molido", CONDIMENTOS, 100.0, especia = true, unidad = Unidad.CUCHARADITA),
        ing("Pimentón", CONDIMENTOS, 110.0, especia = true, unidad = Unidad.CUCHARADITA),
        ing("Comino", CONDIMENTOS, 105.0, especia = true, unidad = Unidad.CUCHARADITA),
        ing("Orégano", CONDIMENTOS, 45.0, especia = true, unidad = Unidad.CUCHARADITA),
        ing("Laurel", CONDIMENTOS, especia = true, unidad = Unidad.UNIDAD),
        ing("Romero", CONDIMENTOS, 45.0, especia = true, unidad = Unidad.CUCHARADITA),
        ing("Tomillo", CONDIMENTOS, 45.0, especia = true, unidad = Unidad.CUCHARADITA),
        ing("Nuez moscada", CONDIMENTOS, 110.0, especia = true, unidad = Unidad.PIZCA),
        ing("Canela", CONDIMENTOS, 125.0, especia = true, unidad = Unidad.CUCHARADITA),
        ing("Ajo en polvo", CONDIMENTOS, 150.0, especia = true, unidad = Unidad.CUCHARADITA),
        ing("Perejil", CONDIMENTOS, 60.0, unidad = Unidad.CUCHARADA),
        ing("Vinagre", CONDIMENTOS, 240.0, unidad = Unidad.CUCHARADA),
        ing("Salsa de soja", CONDIMENTOS, 250.0, unidad = Unidad.CUCHARADA),
        ing("Mostaza", CONDIMENTOS, 250.0, unidad = Unidad.CUCHARADA),
        ing("Mayonesa", CONDIMENTOS, 220.0, unidad = Unidad.CUCHARADA),
        ing("Ketchup", CONDIMENTOS, 240.0, unidad = Unidad.CUCHARADA),
        ing("Extracto de tomate", CONDIMENTOS, 260.0, unidad = Unidad.CUCHARADA),
        ing("Salsa de tomate", CONDIMENTOS, 245.0, unidad = Unidad.MILILITRO),

        // --- Endulzantes ---
        ing("Azúcar", ENDULZANTES, 200.0, basico = true),
        ing("Azúcar rubia", ENDULZANTES, 200.0),
        ing("Azúcar impalpable", ENDULZANTES, 120.0),
        ing("Miel", ENDULZANTES, 340.0, unidad = Unidad.CUCHARADA),
        ing("Edulcorante", ENDULZANTES, unidad = Unidad.CUCHARADITA),

        // --- Grasas y aceites ---
        ing("Aceite", GRASAS, 218.0, basico = true, unidad = Unidad.MILILITRO),
        ing("Aceite de oliva", GRASAS, 216.0, unidad = Unidad.CUCHARADA),
        ing("Grasa vacuna", GRASAS, 205.0),

        // --- Bebidas ---
        ing("Agua", BEBIDAS, 240.0, basico = true, unidad = Unidad.MILILITRO),
        ing("Vino blanco", BEBIDAS, 240.0, unidad = Unidad.MILILITRO),
        ing("Vino tinto", BEBIDAS, 240.0, unidad = Unidad.MILILITRO),
        ing("Caldo de verduras", BEBIDAS, 240.0, unidad = Unidad.MILILITRO),
        ing("Caldo de carne", BEBIDAS, 240.0, unidad = Unidad.MILILITRO),
        ing("Cerveza", BEBIDAS, 240.0, unidad = Unidad.MILILITRO),

        // --- Otros ---
        ing("Levadura fresca", OTROS, 250.0, especia = true),
        ing("Levadura seca", OTROS, 150.0, especia = true, unidad = Unidad.CUCHARADITA),
        ing("Polvo de hornear", OTROS, 200.0, especia = true, unidad = Unidad.CUCHARADITA),
        ing("Bicarbonato", OTROS, 220.0, especia = true, unidad = Unidad.CUCHARADITA),
        ing("Gelatina sin sabor", OTROS, 150.0, unidad = Unidad.CUCHARADITA),
        ing("Cacao amargo", OTROS, 85.0),
        ing("Chocolate cobertura", OTROS, 170.0),
        ing("Esencia de vainilla", OTROS, 240.0, especia = true, unidad = Unidad.CUCHARADITA),
        ing("Coco rallado", OTROS, 80.0),
        ing("Nueces", OTROS, 120.0),
        ing("Almendras", OTROS, 140.0),
        ing("Aceitunas", OTROS, 135.0),
        ing("Pan francés", OTROS, unidad = Unidad.UNIDAD)
    )
}
