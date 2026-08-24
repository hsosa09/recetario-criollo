package uy.horacio.recetariocriollo.ui.recetas

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import uy.horacio.recetariocriollo.R
import uy.horacio.recetariocriollo.dominio.Plantillas
import uy.horacio.recetariocriollo.dominio.modelo.CategoriaReceta
import uy.horacio.recetariocriollo.dominio.modelo.ReglaEscalado
import uy.horacio.recetariocriollo.dominio.modelo.Unidad
import uy.horacio.recetariocriollo.ui.componentes.FilaPareja
import uy.horacio.recetariocriollo.ui.componentes.SelectorDesplegable
import uy.horacio.recetariocriollo.ui.detalleId
import uy.horacio.recetariocriollo.ui.ingredientes.DialogoNuevoIngrediente
import uy.horacio.recetariocriollo.ui.ingredientes.HojaSelectorIngrediente
import uy.horacio.recetariocriollo.ui.textoId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorRecetaPantalla(
    vistaModelo: EditorRecetaViewModel,
    alVolver: () -> Unit,
    alGuardar: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val estado by vistaModelo.estado.collectAsStateWithLifecycle()
    var mostrandoSelector by remember { mutableStateOf(false) }
    var nombreParaAlta by remember { mutableStateOf<String?>(null) }
    val avisos = remember { SnackbarHostState() }

    val elegirFoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let(vistaModelo::elegirFoto) }

    LaunchedEffect(estado.guardadaConId) {
        estado.guardadaConId?.let(alGuardar)
    }

    val mensajeError = estado.error?.let { stringResource(it) }
    LaunchedEffect(mensajeError) {
        if (mensajeError != null) {
            avisos.showSnackbar(mensajeError)
            vistaModelo.limpiarError()
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(avisos) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (estado.esNueva) R.string.editor_titulo_nueva
                            else R.string.editor_titulo_editar
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = alVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.accion_volver))
                    }
                },
                actions = {
                    IconButton(onClick = vistaModelo::guardar, enabled = !estado.guardando) {
                        Icon(Icons.Default.Save, contentDescription = stringResource(R.string.accion_guardar))
                    }
                }
            )
        }
    ) { relleno ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (estado.esNueva) {
                item {
                    Column {
                        Text(
                            text = stringResource(R.string.editor_plantilla),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = stringResource(R.string.editor_plantilla_ayuda),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            items(Plantillas.todas, key = { it.id }) { plantilla ->
                                AssistChip(
                                    onClick = { vistaModelo.aplicarPlantilla(plantilla) },
                                    label = { Text(plantilla.nombre) }
                                )
                            }
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = estado.nombre,
                    onValueChange = vistaModelo::cambiarNombre,
                    label = { Text(stringResource(R.string.editor_nombre)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                SelectorDesplegable(
                    etiqueta = stringResource(R.string.editor_categoria),
                    seleccion = estado.categoria,
                    opciones = CategoriaReceta.entries,
                    textoDe = { stringResource(it.textoId) },
                    alElegir = vistaModelo::cambiarCategoria
                )
            }

            item {
                FilaPareja(
                    izquierda = {
                        OutlinedTextField(
                            value = estado.porciones,
                            onValueChange = vistaModelo::cambiarPorciones,
                            label = { Text(stringResource(R.string.editor_porciones)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    derecha = {
                        OutlinedTextField(
                            value = estado.tiempo,
                            onValueChange = vistaModelo::cambiarTiempo,
                            label = { Text(stringResource(R.string.editor_tiempo)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                )
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = stringResource(R.string.editor_foto),
                            style = MaterialTheme.typography.titleMedium
                        )
                        estado.fotoPath?.let { ruta ->
                            AsyncImage(
                                model = ruta,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .padding(vertical = 8.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    elegirFoto.launch(
                                        PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                }
                            ) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null)
                                Text(
                                    text = stringResource(
                                        if (estado.fotoPath == null) R.string.editor_elegir_foto
                                        else R.string.editor_cambiar_foto
                                    ),
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                            if (estado.fotoPath != null) {
                                TextButton(onClick = vistaModelo::quitarFoto) {
                                    Text(stringResource(R.string.editor_quitar_foto))
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = stringResource(R.string.editor_ingredientes),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(estado.ingredientes, key = { "ingrediente_${it.idLocal}" }) { linea ->
                TarjetaLineaIngrediente(
                    linea = linea,
                    alCambiarCantidad = { vistaModelo.cambiarCantidad(linea.idLocal, it) },
                    alCambiarUnidad = { vistaModelo.cambiarUnidad(linea.idLocal, it) },
                    alCambiarRegla = { vistaModelo.cambiarRegla(linea.idLocal, it) },
                    alCambiarAclaracion = { vistaModelo.cambiarAclaracion(linea.idLocal, it) },
                    alQuitar = { vistaModelo.quitarIngrediente(linea.idLocal) }
                )
            }

            item {
                OutlinedButton(
                    onClick = { mostrandoSelector = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text(
                        text = stringResource(R.string.editor_agregar_ingrediente),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            item {
                Text(
                    text = stringResource(R.string.editor_pasos),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            itemsIndexed(estado.pasos, key = { _, paso -> "paso_${paso.idLocal}" }) { indice, paso ->
                TarjetaLineaPaso(
                    numero = indice + 1,
                    linea = paso,
                    alCambiarTexto = { vistaModelo.cambiarTextoPaso(paso.idLocal, it) },
                    alCambiarTimer = { vistaModelo.cambiarTimerPaso(paso.idLocal, it) },
                    alSubir = { vistaModelo.moverPaso(paso.idLocal, true) },
                    alBajar = { vistaModelo.moverPaso(paso.idLocal, false) },
                    alQuitar = { vistaModelo.quitarPaso(paso.idLocal) }
                )
            }

            item {
                OutlinedButton(
                    onClick = vistaModelo::agregarPaso,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text(
                        text = stringResource(R.string.editor_agregar_paso),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = estado.notas,
                    onValueChange = vistaModelo::cambiarNotas,
                    label = { Text(stringResource(R.string.editor_notas)) },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (mostrandoSelector) {
        HojaSelectorIngrediente(
            catalogo = estado.catalogo,
            alElegir = { ingrediente ->
                vistaModelo.agregarIngrediente(ingrediente)
                mostrandoSelector = false
            },
            alCerrar = { mostrandoSelector = false },
            alCrearNuevo = { nombre ->
                mostrandoSelector = false
                nombreParaAlta = nombre
            }
        )
    }

    nombreParaAlta?.let { nombre ->
        DialogoNuevoIngrediente(
            nombreInicial = nombre,
            alGuardar = { nuevoNombre, categoria, densidad, especia, unidad ->
                vistaModelo.crearIngredienteYAgregar(nuevoNombre, categoria, densidad, especia, unidad)
                nombreParaAlta = null
            },
            alCancelar = { nombreParaAlta = null }
        )
    }
}

@Composable
private fun TarjetaLineaIngrediente(
    linea: LineaIngrediente,
    alCambiarCantidad: (String) -> Unit,
    alCambiarUnidad: (Unidad) -> Unit,
    alCambiarRegla: (ReglaEscalado) -> Unit,
    alCambiarAclaracion: (String) -> Unit,
    alQuitar: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = linea.ingrediente.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = alQuitar) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.accion_quitar))
                }
            }
            FilaPareja(
                izquierda = {
                    OutlinedTextField(
                        value = linea.cantidad,
                        onValueChange = alCambiarCantidad,
                        label = { Text(stringResource(R.string.editor_cantidad)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                derecha = {
                    SelectorDesplegable(
                        etiqueta = stringResource(R.string.editor_unidad),
                        seleccion = linea.unidad,
                        opciones = Unidad.entries,
                        textoDe = { it.plural },
                        alElegir = alCambiarUnidad
                    )
                }
            )
            SelectorDesplegable(
                etiqueta = stringResource(R.string.editor_regla),
                seleccion = linea.regla,
                opciones = ReglaEscalado.entries,
                textoDe = { stringResource(it.textoId) },
                detalleDe = { stringResource(it.detalleId) },
                alElegir = alCambiarRegla
            )
            OutlinedTextField(
                value = linea.aclaracion,
                onValueChange = alCambiarAclaracion,
                label = { Text(stringResource(R.string.editor_aclaracion)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun TarjetaLineaPaso(
    numero: Int,
    linea: LineaPaso,
    alCambiarTexto: (String) -> Unit,
    alCambiarTimer: (String) -> Unit,
    alSubir: () -> Unit,
    alBajar: () -> Unit,
    alQuitar: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.detalle_paso_numero, numero),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = alSubir) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = stringResource(R.string.editor_subir))
                }
                IconButton(onClick = alBajar) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = stringResource(R.string.editor_bajar))
                }
                IconButton(onClick = alQuitar) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.accion_quitar))
                }
            }
            OutlinedTextField(
                value = linea.texto,
                onValueChange = alCambiarTexto,
                label = { Text(stringResource(R.string.editor_paso_texto)) },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = linea.minutosTimer,
                onValueChange = alCambiarTimer,
                label = { Text(stringResource(R.string.editor_paso_timer)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
