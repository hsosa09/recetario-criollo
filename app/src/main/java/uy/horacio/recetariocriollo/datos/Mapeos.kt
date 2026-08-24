package uy.horacio.recetariocriollo.datos

import uy.horacio.recetariocriollo.dominio.modelo.Ingrediente
import uy.horacio.recetariocriollo.dominio.modelo.IngredienteDeReceta
import uy.horacio.recetariocriollo.dominio.modelo.PasoPreparacion
import uy.horacio.recetariocriollo.dominio.modelo.Receta

/** Traduccion entre lo que guarda Room y los modelos con los que trabaja el dominio. */

fun IngredienteEntity.aDominio(): Ingrediente = Ingrediente(
    id = id,
    nombre = nombre,
    categoria = categoria,
    densidadGramosPorTaza = densidadGramosPorTaza,
    esSalOEspecia = esSalOEspecia,
    esBasicoDeDespensa = esBasicoDeDespensa,
    unidadHabitual = unidadHabitual
)

fun Ingrediente.aEntidad(): IngredienteEntity = IngredienteEntity(
    id = id,
    nombre = nombre,
    categoria = categoria,
    densidadGramosPorTaza = densidadGramosPorTaza,
    esSalOEspecia = esSalOEspecia,
    esBasicoDeDespensa = esBasicoDeDespensa,
    unidadHabitual = unidadHabitual
)

fun IngredienteDeRecetaConCatalogo.aDominio(): IngredienteDeReceta = IngredienteDeReceta(
    id = cruce.id,
    ingrediente = ingrediente.aDominio(),
    cantidad = cruce.cantidad,
    unidad = cruce.unidad,
    regla = cruce.regla,
    aclaracion = cruce.aclaracion,
    orden = cruce.orden
)

fun PasoEntity.aDominio(): PasoPreparacion = PasoPreparacion(
    id = id,
    orden = orden,
    texto = texto,
    timerSugeridoSegundos = timerSugeridoSegundos
)

fun RecetaCompletaEntity.aDominio(): Receta = Receta(
    id = receta.id,
    nombre = receta.nombre,
    categoria = receta.categoria,
    porcionesBase = receta.porcionesBase,
    tiempoMinutos = receta.tiempoMinutos,
    notas = receta.notas,
    fotoPath = receta.fotoPath,
    esFavorita = receta.esFavorita,
    ingredientes = ingredientes.sortedBy { it.cruce.orden }.map { it.aDominio() },
    pasos = pasos.sortedBy { it.orden }.map { it.aDominio() }
)

fun Receta.aEntidad(): RecetaEntity = RecetaEntity(
    id = id,
    nombre = nombre.trim(),
    categoria = categoria,
    porcionesBase = porcionesBase,
    tiempoMinutos = tiempoMinutos,
    notas = notas?.takeIf { it.isNotBlank() },
    fotoPath = fotoPath,
    esFavorita = esFavorita
)

fun IngredienteDeReceta.aEntidad(recetaId: Long): RecetaIngredienteEntity = RecetaIngredienteEntity(
    id = 0,
    recetaId = recetaId,
    ingredienteId = ingrediente.id,
    cantidad = cantidad,
    unidad = unidad,
    regla = regla,
    aclaracion = aclaracion?.takeIf { it.isNotBlank() },
    orden = orden
)

fun PasoPreparacion.aEntidad(recetaId: Long): PasoEntity = PasoEntity(
    id = 0,
    recetaId = recetaId,
    orden = orden,
    texto = texto.trim(),
    timerSugeridoSegundos = timerSugeridoSegundos
)
