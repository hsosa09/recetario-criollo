package uy.horacio.recetariocriollo.datos

import uy.horacio.recetariocriollo.dominio.modelo.Cocinada
import uy.horacio.recetariocriollo.dominio.modelo.CocinadaConReceta
import uy.horacio.recetariocriollo.dominio.modelo.Ingrediente
import uy.horacio.recetariocriollo.dominio.modelo.IngredienteDeReceta
import uy.horacio.recetariocriollo.dominio.modelo.PasoPreparacion
import uy.horacio.recetariocriollo.dominio.modelo.Receta
import uy.horacio.recetariocriollo.dominio.modelo.ResumenCocinadas

/** Traduccion entre lo que guarda Room y los modelos con los que trabaja el dominio. */

fun IngredienteEntity.aDominio(): Ingrediente = Ingrediente(
    id = id,
    nombre = nombre,
    categoria = categoria,
    densidadGramosPorTaza = densidadGramosPorTaza,
    esSalOEspecia = esSalOEspecia,
    esBasicoDeDespensa = esBasicoDeDespensa,
    unidadHabitual = unidadHabitual,
    meses = meses
)

fun Ingrediente.aEntidad(): IngredienteEntity = IngredienteEntity(
    id = id,
    nombre = nombre,
    categoria = categoria,
    densidadGramosPorTaza = densidadGramosPorTaza,
    esSalOEspecia = esSalOEspecia,
    esBasicoDeDespensa = esBasicoDeDespensa,
    unidadHabitual = unidadHabitual,
    meses = meses
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
    timerSugeridoSegundos = timerSugeridoSegundos,
    fotoPath = fotoPath
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
    dificultad = receta.dificultad,
    moldeCm = receta.moldeCm,
    origenId = receta.origenId,
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
    esFavorita = esFavorita,
    dificultad = dificultad,
    moldeCm = moldeCm,
    origenId = origenId
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
    timerSugeridoSegundos = timerSugeridoSegundos,
    fotoPath = fotoPath
)

fun CocinadaEntity.aDominio(): Cocinada = Cocinada(
    id = id,
    recetaId = recetaId,
    fechaMillis = fechaMillis,
    estrellas = estrellas,
    porciones = porciones,
    nota = nota,
    fotoPath = fotoPath,
    audioPath = audioPath
)

fun Cocinada.aEntidad(): CocinadaEntity = CocinadaEntity(
    id = id,
    recetaId = recetaId,
    fechaMillis = fechaMillis,
    estrellas = estrellas.coerceIn(1, 5),
    porciones = porciones,
    nota = nota?.trim()?.takeIf { it.isNotEmpty() },
    fotoPath = fotoPath,
    audioPath = audioPath
)

fun CocinadaConNombreFila.aDominio(): CocinadaConReceta =
    CocinadaConReceta(cocinada = cocinada.aDominio(), nombreReceta = nombreReceta)

fun ResumenCocinadasFila.aDominio(): ResumenCocinadas =
    ResumenCocinadas(recetaId, veces, promedioEstrellas, ultimaMillis)
