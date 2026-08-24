package uy.horacio.recetariocriollo.ui.recetas

import android.view.WindowManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import uy.horacio.recetariocriollo.R
import uy.horacio.recetariocriollo.cronometro.Cronometro
import uy.horacio.recetariocriollo.dominio.CantidadEscalada
import uy.horacio.recetariocriollo.dominio.modelo.PasoPreparacion
import uy.horacio.recetariocriollo.dominio.modelo.ReglaEscalado
import uy.horacio.recetariocriollo.dominio.modelo.Unidad
import uy.horacio.recetariocriollo.ui.textoId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleRecetaPantalla(
    vistaModelo: DetalleRecetaViewModel,
    alVolver: () -> Unit,
    alEditar: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val estado by vistaModelo.estado.collectAsStateWithLifecycle()
    val receta = estado.receta
    var pidiendoBorrar by remember { mutableStateOf(false) }
    val avisos = remember { SnackbarHostState() }
    val alcance = rememberCoroutineScope()

    // Modo cocina: la pantalla no se apaga mientras se cocina con las manos ocupadas.
    val contexto = LocalContext.current
    DisposableEffect(estado.modoCocina) {
        val ventana = (contexto as? android.app.Activity)?.window
        if (estado.modoCocina) {
            ventana?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            ventana?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(avisos) },
        topBar = {
            TopAppBar(
                title = { Text(receta?.nombre.orEmpty(), maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = alVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.accion_volver))
                    }
                },
                actions = {
                    IconButton(onClick = vistaModelo::alternarModoCocina) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = stringResource(R.string.detalle_modo_cocina),
                            tint = if (estado.modoCocina) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = vistaModelo::alternarFavorita) {
                        Icon(
                            imageVector = if (receta?.esFavorita == true) Icons.Default.Favorite
                            else Icons.Default.FavoriteBorder,
                            contentDescription = stringResource(R.string.receta_marcar_favorita)
                        )
                    }
                    IconButton(onClick = { receta?.let { alEditar(it.id) } }) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.accion_editar))
                    }
                    IconButton(onClick = { pidiendoBorrar = true }) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.accion_borrar))
                    }
                }
            )
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

        val escalaTexto = if (estado.modoCocina) 1.25f else 1f

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            receta.fotoPath?.let { ruta ->
                item {
                    AsyncImage(
                        model = ruta,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(
                        onClick = {},
                        label = { Text(stringResource(receta.categoria.textoId)) },
                        leadingIcon = { Icon(Icons.Default.Restaurant, contentDescription = null) }
                    )
                    receta.tiempoLegible?.let { tiempo ->
                        AssistChip(
                            onClick = {},
                            label = { Text(tiempo) },
                            leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null) }
                        )
                    }
                }
            }

            item {
                SelectorPorciones(
                    porciones = estado.porciones,
                    porcionesBase = receta.porcionesBase,
                    estaEscalada = estado.estaEscalada,
                    alCambiar = vistaModelo::cambiarPorciones,
                    alRestaurar = vistaModelo::restaurarPorciones
                )
            }

            item {
                Text(
                    text = stringResource(R.string.detalle_ingredientes),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (estado.ingredientes.isEmpty()) {
                item { Text(stringResource(R.string.detalle_sin_ingredientes)) }
            }

            items(estado.ingredientes, key = { "ingrediente_${it.ingrediente.id}" }) { escalado ->
                FilaIngredienteEscalado(escalado = escalado, escalaTexto = escalaTexto)
            }

            item {
                Text(
                    text = stringResource(R.string.detalle_preparacion),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (receta.pasos.isEmpty()) {
                item { Text(stringResource(R.string.detalle_sin_pasos)) }
            }

            itemsIndexed(receta.pasos, key = { _, paso -> "paso_${paso.id}" }) { indice, paso ->
                TarjetaPaso(
                    numero = indice + 1,
                    paso = paso,
                    escalaTexto = escalaTexto,
                    alArrancarTimer = { segundos ->
                        val etiqueta = vistaModelo.arrancarTimerDePaso(indice + 1, segundos)
                        alcance.launch {
                            avisos.showSnackbar(
                                contexto.getString(R.string.detalle_timer_arrancado, etiqueta)
                            )
                        }
                    }
                )
            }

            receta.notas?.takeIf { it.isNotBlank() }?.let { notas ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = stringResource(R.string.detalle_notas),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = notas,
                                style = MaterialTheme.typography.bodyLarge,
                                fontSize = MaterialTheme.typography.bodyLarge.fontSize * escalaTexto
                            )
                        }
                    }
                }
            }
        }
    }

    if (pidiendoBorrar && receta != null) {
        AlertDialog(
            onDismissRequest = { pidiendoBorrar = false },
            title = { Text(stringResource(R.string.detalle_borrar_titulo)) },
            text = { Text(stringResource(R.string.detalle_borrar_mensaje, receta.nombre)) },
            confirmButton = {
                TextButton(onClick = {
                    pidiendoBorrar = false
                    vistaModelo.borrar(alVolver)
                }) { Text(stringResource(R.string.accion_borrar)) }
            },
            dismissButton = {
                TextButton(onClick = { pidiendoBorrar = false }) {
                    Text(stringResource(R.string.accion_cancelar))
                }
            }
        )
    }
}

@Composable
private fun SelectorPorciones(
    porciones: Int,
    porcionesBase: Int,
    estaEscalada: Boolean,
    alCambiar: (Int) -> Unit,
    alRestaurar: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.detalle_porciones),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                FilledTonalIconButton(
                    onClick = { alCambiar(porciones - 1) },
                    enabled = porciones > 1,
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(
                        Icons.Default.Remove,
                        contentDescription = stringResource(R.string.detalle_menos_porciones)
                    )
                }
                Text(
                    text = porciones.toString(),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold
                )
                FilledTonalIconButton(
                    onClick = { alCambiar(porciones + 1) },
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.detalle_mas_porciones)
                    )
                }
            }
            if (estaEscalada) {
                Text(
                    text = stringResource(R.string.detalle_receta_original, porcionesBase),
                    style = MaterialTheme.typography.bodyMedium
                )
                TextButton(onClick = alRestaurar) {
                    Text(stringResource(R.string.detalle_restaurar))
                }
            }
        }
    }
}

@Composable
private fun FilaIngredienteEscalado(escalado: CantidadEscalada, escalaTexto: Float) {
    val item = escalado.ingrediente
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = if (item.unidad == Unidad.A_GUSTO) "" else escalado.texto,
            style = MaterialTheme.typography.titleMedium,
            fontSize = MaterialTheme.typography.titleMedium.fontSize * escalaTexto,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.widthIn(min = 96.dp, max = 168.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = buildString {
                    append(item.ingrediente.nombre)
                    if (item.unidad == Unidad.A_GUSTO) append(" (a gusto)")
                    item.aclaracion?.let { append(", $it") }
                },
                style = MaterialTheme.typography.bodyLarge,
                fontSize = MaterialTheme.typography.bodyLarge.fontSize * escalaTexto
            )
            val nota = when {
                item.regla == ReglaEscalado.FIJA -> stringResource(R.string.detalle_no_escala)
                item.regla == ReglaEscalado.ATENUADA -> stringResource(R.string.detalle_ajuste_suave)
                else -> null
            }
            if (nota != null) {
                Text(
                    text = nota,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TarjetaPaso(
    numero: Int,
    paso: PasoPreparacion,
    escalaTexto: Float,
    alArrancarTimer: (Int) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.detalle_paso_numero, numero),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = paso.texto,
                style = MaterialTheme.typography.bodyLarge,
                fontSize = MaterialTheme.typography.bodyLarge.fontSize * escalaTexto
            )
            paso.timerSugeridoSegundos?.let { segundos ->
                TextButton(onClick = { alArrancarTimer(segundos) }) {
                    Icon(Icons.Default.Timer, contentDescription = null)
                    Text(
                        text = stringResource(
                            R.string.detalle_arrancar_timer,
                            Cronometro.formatearSegundos(segundos)
                        ),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}
