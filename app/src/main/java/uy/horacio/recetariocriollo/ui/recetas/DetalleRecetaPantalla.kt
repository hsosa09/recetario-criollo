package uy.horacio.recetariocriollo.ui.recetas

import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.activity.compose.LocalActivity
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import uy.horacio.recetariocriollo.R
import uy.horacio.recetariocriollo.cronometro.Cronometro
import uy.horacio.recetariocriollo.dominio.CantidadEscalada
import uy.horacio.recetariocriollo.dominio.modelo.PasoPreparacion
import uy.horacio.recetariocriollo.dominio.modelo.ReglaEscalado
import uy.horacio.recetariocriollo.dominio.modelo.Unidad
import uy.horacio.recetariocriollo.ui.componentes.AvisoRecetario
import uy.horacio.recetariocriollo.ui.componentes.BarraSuperior
import uy.horacio.recetariocriollo.ui.componentes.BloqueSeccion
import uy.horacio.recetariocriollo.ui.componentes.BotonIcono
import uy.horacio.recetariocriollo.ui.componentes.BotonPrimario
import uy.horacio.recetariocriollo.ui.componentes.BotonSecundario
import uy.horacio.recetariocriollo.ui.componentes.BotonTexto
import uy.horacio.recetariocriollo.ui.componentes.CabeceraSeccion
import uy.horacio.recetariocriollo.ui.componentes.Etiqueta
import uy.horacio.recetariocriollo.ui.componentes.Iconos
import uy.horacio.recetariocriollo.ui.componentes.MARGEN
import uy.horacio.recetariocriollo.ui.componentes.Rotulo
import uy.horacio.recetariocriollo.ui.componentes.Stepper
import uy.horacio.recetariocriollo.ui.componentes.TextoTenue
import uy.horacio.recetariocriollo.ui.componentes.fileteAbajo
import uy.horacio.recetariocriollo.ui.componentes.fileteArriba
import uy.horacio.recetariocriollo.ui.textoId
import uy.horacio.recetariocriollo.ui.theme.RecetarioTema

@Composable
fun DetalleRecetaPantalla(
    vistaModelo: DetalleRecetaViewModel,
    alVolver: () -> Unit,
    alEditar: (Long) -> Unit,
    alCocinar: (recetaId: Long, porciones: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val estado by vistaModelo.estado.collectAsStateWithLifecycle()
    val receta = estado.receta
    var pidiendoBorrar by remember { mutableStateOf(false) }
    val avisos = remember { SnackbarHostState() }
    val alcance = rememberCoroutineScope()
    val colores = MaterialTheme.colorScheme

    // Modo cocina: la pantalla no se apaga mientras se cocina con las manos ocupadas.
    val actividad = LocalActivity.current
    val recursos = LocalResources.current
    DisposableEffect(estado.modoCocina) {
        val ventana = actividad?.window
        if (estado.modoCocina) {
            ventana?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            ventana?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = colores.background,
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(avisos) { AvisoRecetario(it) } },
        topBar = {
            BarraSuperior(
                titulo = receta?.nombre.orEmpty(),
                alVolver = alVolver,
                descripcionVolver = stringResource(R.string.accion_volver)
            ) {
                BotonIcono(
                    icono = Iconos.Llama,
                    descripcion = stringResource(R.string.detalle_modo_cocina),
                    alTocar = vistaModelo::alternarModoCocina,
                    color = if (estado.modoCocina) colores.primary else colores.onBackground
                )
                BotonIcono(
                    icono = if (receta?.esFavorita == true) Iconos.CorazonLleno else Iconos.CorazonVacio,
                    descripcion = stringResource(
                        if (receta?.esFavorita == true) R.string.receta_quitar_favorita
                        else R.string.receta_marcar_favorita
                    ),
                    alTocar = vistaModelo::alternarFavorita,
                    color = if (receta?.esFavorita == true) colores.primary else colores.onBackground
                )
                BotonIcono(
                    icono = Iconos.Editar,
                    descripcion = stringResource(R.string.accion_editar),
                    alTocar = { receta?.let { alEditar(it.id) } },
                    tamanioIcono = 20.dp
                )
            }
        }
    ) { relleno ->
        if (receta == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(relleno),
                contentAlignment = Alignment.Center
            ) {
                if (!estado.cargando) Text(stringResource(R.string.detalle_no_encontrada))
            }
            return@Scaffold
        }

        // En modo cocina todo el texto de lectura crece un 25 %.
        val escala = if (estado.modoCocina) 1.25f else 1f

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno)
        ) {
            item(key = "portada") {
                Portada(nombre = receta.nombre, fotoPath = receta.fotoPath)
            }

            item(key = "cabecera") {
                BloqueSeccion {
                    Text(
                        text = receta.nombre,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Etiqueta(stringResource(receta.categoria.textoId), acento = true)
                        receta.tiempoLegible?.let { Etiqueta(it) }
                        Etiqueta(pluralStringResource(R.plurals.receta_porciones, receta.porcionesBase, receta.porcionesBase))
                    }
                    if (estado.modoCocina) {
                        TextoTenue(
                            texto = stringResource(R.string.detalle_modo_cocina_activo),
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }
                }
            }

            item(key = "porciones") {
                BloqueSeccion {
                    Rotulo(stringResource(R.string.detalle_porciones), modifier = Modifier.padding(bottom = 10.dp))
                    Stepper(
                        valor = estado.porciones.toString(),
                        alRestar = { vistaModelo.cambiarPorciones(estado.porciones - 1) },
                        alSumar = { vistaModelo.cambiarPorciones(estado.porciones + 1) },
                        descripcionRestar = stringResource(R.string.detalle_menos_porciones),
                        descripcionSumar = stringResource(R.string.detalle_mas_porciones),
                        puedeRestar = estado.porciones > 1
                    )
                    TextoTenue(
                        texto = if (estado.estaEscalada) {
                            pluralStringResource(R.plurals.detalle_cantidades_ajustadas, estado.porciones, estado.porciones)
                        } else {
                            pluralStringResource(R.plurals.detalle_receta_original, receta.porcionesBase, receta.porcionesBase)
                        },
                        modifier = Modifier.padding(top = 10.dp)
                    )
                    if (estado.estaEscalada) {
                        BotonTexto(
                            texto = stringResource(R.string.detalle_restaurar),
                            alTocar = vistaModelo::restaurarPorciones
                        )
                    }
                }
            }

            item(key = "rotulo_ingredientes") {
                CabeceraSeccion(stringResource(R.string.detalle_ingredientes))
            }

            if (estado.ingredientes.isEmpty()) {
                item { TextoVacioSeccion(stringResource(R.string.detalle_sin_ingredientes)) }
            }

            items(estado.ingredientes, key = { "ingrediente_${it.ingrediente.id}" }) { escalado ->
                FilaIngredienteEscalado(
                    escalado = escalado,
                    estaEscalada = estado.estaEscalada,
                    escala = escala
                )
            }

            item(key = "rotulo_pasos") {
                CabeceraSeccion(
                    rotulo = stringResource(R.string.detalle_preparacion),
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .fileteArriba(colores.outline, 2.dp)
                        .padding(top = 2.dp)
                )
            }

            if (receta.pasos.isEmpty()) {
                item { TextoVacioSeccion(stringResource(R.string.detalle_sin_pasos)) }
            }

            itemsIndexed(receta.pasos, key = { _, paso -> "paso_${paso.id}" }) { indice, paso ->
                FilaPaso(
                    numero = indice + 1,
                    paso = paso,
                    escala = escala,
                    alArrancarTimer = { segundos ->
                        val etiqueta = vistaModelo.arrancarTimerDePaso(indice + 1, segundos)
                        alcance.launch {
                            avisos.showSnackbar(
                                recursos.getString(R.string.detalle_timer_arrancado, etiqueta)
                            )
                        }
                    }
                )
            }

            receta.notas?.takeIf { it.isNotBlank() }?.let { notas ->
                item(key = "notas") {
                    Column(
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .fillMaxWidth()
                            .fileteArriba(colores.outline, 2.dp)
                            .padding(horizontal = MARGEN, vertical = 18.dp)
                    ) {
                        Rotulo(stringResource(R.string.detalle_notas), modifier = Modifier.padding(bottom = 8.dp))
                        Text(
                            text = notas,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 15.sp * escala,
                                lineHeight = 24.sp * escala
                            ),
                            color = colores.onBackground.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            item(key = "acciones") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fileteArriba(colores.outline, 2.dp)
                        .padding(MARGEN),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BotonPrimario(
                        texto = stringResource(R.string.detalle_cocinar_paso_a_paso),
                        alTocar = { alCocinar(receta.id, estado.porciones) },
                        habilitado = receta.pasos.isNotEmpty(),
                        icono = Iconos.Llama,
                        modifier = Modifier.fillMaxWidth()
                    )
                    BotonSecundario(
                        texto = stringResource(R.string.detalle_borrar_receta),
                        alTocar = { pidiendoBorrar = true },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    if (pidiendoBorrar && receta != null) {
        AlertDialog(
            onDismissRequest = { pidiendoBorrar = false },
            containerColor = colores.background,
            title = { Text(stringResource(R.string.detalle_borrar_titulo), style = MaterialTheme.typography.headlineSmall) },
            text = { Text(stringResource(R.string.detalle_borrar_mensaje, receta.nombre)) },
            confirmButton = {
                TextButton(onClick = {
                    pidiendoBorrar = false
                    vistaModelo.borrar(alVolver)
                }) { Text(stringResource(R.string.accion_borrar), color = colores.primary) }
            },
            dismissButton = {
                TextButton(onClick = { pidiendoBorrar = false }) {
                    Text(stringResource(R.string.accion_cancelar), color = colores.onBackground)
                }
            }
        )
    }
}

/** Foto a todo el ancho, o la inicial grande sobre bloque neutro. */
@Composable
private fun Portada(nombre: String, fotoPath: String?) {
    val extra = RecetarioTema.extra
    val filete = MaterialTheme.colorScheme.outline
    if (fotoPath != null) {
        AsyncImage(
            model = fotoPath,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .fileteAbajo(filete, 2.dp)
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(172.dp)
                .background(extra.marcador)
                .fileteAbajo(filete, 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = nombre.take(1).uppercase(),
                style = MaterialTheme.typography.displaySmall.copy(fontSize = 76.sp, lineHeight = 76.sp),
                color = extra.textoMarcador
            )
        }
    }
}

@Composable
private fun TextoVacioSeccion(texto: String) {
    TextoTenue(
        texto = texto,
        estilo = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.padding(horizontal = MARGEN, vertical = 10.dp)
    )
}

@Composable
private fun FilaIngredienteEscalado(
    escalado: CantidadEscalada,
    estaEscalada: Boolean,
    escala: Float
) {
    val item = escalado.ingrediente
    val detalle = buildList {
        item.aclaracion?.let { add(it) }
        if (item.unidad == Unidad.A_GUSTO) add(stringResource(R.string.detalle_a_gusto))
        if (estaEscalada && item.regla == ReglaEscalado.FIJA) add(stringResource(R.string.detalle_no_escala))
        if (estaEscalada && item.regla == ReglaEscalado.ATENUADA) add(stringResource(R.string.detalle_ajuste_suave))
        if (estaEscalada && escalado.redondeada) add(stringResource(R.string.detalle_redondeado_corto))
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fileteArriba(MaterialTheme.colorScheme.outline)
            .padding(horizontal = MARGEN, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = if (item.unidad == Unidad.A_GUSTO) "—" else escalado.texto,
            style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp * escala),
            modifier = Modifier.widthIn(min = 86.dp * escala, max = 150.dp * escala)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.ingrediente.nombre,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp * escala, lineHeight = 20.sp * escala)
            )
            if (detalle.isNotEmpty()) {
                TextoTenue(detalle.joinToString(" · "))
            }
        }
    }
}

@Composable
private fun FilaPaso(
    numero: Int,
    paso: PasoPreparacion,
    escala: Float,
    alArrancarTimer: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fileteArriba(MaterialTheme.colorScheme.outline)
            .padding(horizontal = MARGEN, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = numero.toString(),
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp * escala),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(26.dp * escala)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = paso.texto,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 15.sp * escala,
                    lineHeight = 22.5.sp * escala
                )
            )
            paso.timerSugeridoSegundos?.let { segundos ->
                Spacer(Modifier.height(8.dp))
                BotonSecundario(
                    texto = stringResource(R.string.detalle_arrancar_timer, Cronometro.describirDuracion(segundos)),
                    alTocar = { alArrancarTimer(segundos) },
                    icono = Iconos.Timer,
                    alto = 36.dp
                )
            }
        }
    }
}
