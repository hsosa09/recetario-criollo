package uy.horacio.recetariocriollo.ui.recetas

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import uy.horacio.recetariocriollo.R
import uy.horacio.recetariocriollo.dominio.Plantillas
import uy.horacio.recetariocriollo.dominio.modelo.CategoriaReceta
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
import uy.horacio.recetariocriollo.ui.componentes.CampoTexto
import uy.horacio.recetariocriollo.ui.componentes.ChipRecto
import uy.horacio.recetariocriollo.ui.componentes.FilaPareja
import uy.horacio.recetariocriollo.ui.componentes.Iconos
import uy.horacio.recetariocriollo.ui.componentes.MARGEN
import uy.horacio.recetariocriollo.ui.componentes.Rotulo
import uy.horacio.recetariocriollo.ui.componentes.SelectorDesplegable
import uy.horacio.recetariocriollo.ui.componentes.TextoTenue
import uy.horacio.recetariocriollo.ui.componentes.fileteArriba
import uy.horacio.recetariocriollo.ui.detalleId
import uy.horacio.recetariocriollo.ui.ingredientes.DialogoNuevoIngrediente
import uy.horacio.recetariocriollo.ui.ingredientes.HojaSelectorIngrediente
import uy.horacio.recetariocriollo.ui.textoId
import uy.horacio.recetariocriollo.ui.theme.RecetarioTema

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
    val colores = MaterialTheme.colorScheme

    val elegirFoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let(vistaModelo::elegirFoto) }

    LaunchedEffect(estado.guardadaConId) {
        estado.guardadaConId?.let(alGuardar)
    }

    val mensajeError = estado.error?.let { id ->
        estado.errorDetalle?.let { detalle -> stringResource(id, detalle) } ?: stringResource(id)
    }
    LaunchedEffect(mensajeError) {
        if (mensajeError != null) {
            avisos.showSnackbar(mensajeError)
            vistaModelo.limpiarError()
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = colores.background,
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(avisos) { AvisoRecetario(it) } },
        topBar = {
            BarraSuperior(
                titulo = stringResource(
                    if (estado.esNueva) R.string.editor_titulo_nueva else R.string.editor_titulo_editar
                ),
                alVolver = alVolver,
                descripcionVolver = stringResource(R.string.accion_volver)
            ) {
                BotonSecundario(
                    texto = stringResource(R.string.accion_guardar),
                    alTocar = vistaModelo::guardar,
                    habilitado = !estado.guardando,
                    alto = 36.dp
                )
            }
        }
    ) { relleno ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno)
                .imePadding()
        ) {
            if (estado.esNueva) {
                item(key = "plantillas") {
                    BloqueSeccion {
                        Rotulo(stringResource(R.string.editor_plantilla), modifier = Modifier.padding(bottom = 10.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            items(Plantillas.todas, key = { it.id }) { plantilla ->
                                ChipRecto(
                                    texto = plantilla.nombre,
                                    activo = false,
                                    alTocar = { vistaModelo.aplicarPlantilla(plantilla) }
                                )
                            }
                        }
                        TextoTenue(stringResource(R.string.editor_plantilla_ayuda))
                    }
                }
            }

            item(key = "datos") {
                BloqueSeccion(modifier = Modifier.padding(top = 2.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        CampoTexto(
                            valor = estado.nombre,
                            alCambiar = vistaModelo::cambiarNombre,
                            etiqueta = stringResource(R.string.editor_nombre)
                        )
                        FilaPareja(
                            izquierda = {
                                CampoTexto(
                                    valor = estado.porciones,
                                    alCambiar = vistaModelo::cambiarPorciones,
                                    etiqueta = stringResource(R.string.editor_porciones),
                                    teclado = KeyboardOptions(
                                        keyboardType = KeyboardType.Number,
                                        imeAction = ImeAction.Next
                                    )
                                )
                            },
                            derecha = {
                                CampoTexto(
                                    valor = estado.tiempo,
                                    alCambiar = vistaModelo::cambiarTiempo,
                                    etiqueta = stringResource(R.string.editor_tiempo),
                                    teclado = KeyboardOptions(
                                        keyboardType = KeyboardType.Number,
                                        imeAction = ImeAction.Next
                                    )
                                )
                            }
                        )
                        SelectorDesplegable(
                            etiqueta = stringResource(R.string.editor_categoria),
                            seleccion = estado.categoria,
                            opciones = CategoriaReceta.entries,
                            textoDe = { stringResource(it.textoId) },
                            alElegir = vistaModelo::cambiarCategoria
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            TextoTenue(stringResource(R.string.editor_foto))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                if (estado.fotoPath != null) {
                                    AsyncImage(
                                        model = estado.fotoPath,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.size(70.dp)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(70.dp)
                                            .background(RecetarioTema.extra.marcador)
                                    )
                                }
                                BotonSecundario(
                                    texto = stringResource(
                                        if (estado.fotoPath == null) R.string.editor_elegir_foto
                                        else R.string.editor_cambiar_foto
                                    ),
                                    icono = Iconos.Foto,
                                    alto = 42.dp,
                                    alTocar = {
                                        elegirFoto.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    }
                                )
                                if (estado.fotoPath != null) {
                                    BotonTexto(
                                        texto = stringResource(R.string.editor_quitar_foto),
                                        alTocar = vistaModelo::quitarFoto
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item(key = "rotulo_ingredientes") {
                CabeceraSeccion(stringResource(R.string.editor_ingredientes)) {
                    TextoTenue(stringResource(R.string.editor_ingredientes_ayuda))
                }
            }

            items(estado.ingredientes, key = { "ingrediente_${it.idLocal}" }) { linea ->
                FilaLineaIngrediente(
                    linea = linea,
                    alCambiarCantidad = { vistaModelo.cambiarCantidad(linea.idLocal, it) },
                    alCambiarUnidad = { vistaModelo.cambiarUnidad(linea.idLocal, it) },
                    alCambiarRegla = { vistaModelo.cambiarRegla(linea.idLocal, it) },
                    alCambiarAclaracion = { vistaModelo.cambiarAclaracion(linea.idLocal, it) },
                    alQuitar = { vistaModelo.quitarIngrediente(linea.idLocal) }
                )
            }

            item(key = "agregar_ingrediente") {
                BotonSecundario(
                    texto = stringResource(R.string.editor_agregar_ingrediente),
                    alTocar = { mostrandoSelector = true },
                    icono = Iconos.Mas,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fileteArriba(colores.outline)
                        .padding(horizontal = MARGEN, vertical = 12.dp)
                )
            }

            item(key = "rotulo_pasos") {
                CabeceraSeccion(
                    rotulo = stringResource(R.string.editor_pasos),
                    modifier = Modifier.fileteArriba(colores.outline, 2.dp)
                )
            }

            itemsIndexed(estado.pasos, key = { _, paso -> "paso_${paso.idLocal}" }) { indice, paso ->
                FilaLineaPaso(
                    numero = indice + 1,
                    linea = paso,
                    esPrimero = indice == 0,
                    esUltimo = indice == estado.pasos.lastIndex,
                    alCambiarTexto = { vistaModelo.cambiarTextoPaso(paso.idLocal, it) },
                    alCambiarTimer = { vistaModelo.cambiarTimerPaso(paso.idLocal, it) },
                    alSubir = { vistaModelo.moverPaso(paso.idLocal, true) },
                    alBajar = { vistaModelo.moverPaso(paso.idLocal, false) },
                    alQuitar = { vistaModelo.quitarPaso(paso.idLocal) }
                )
            }

            item(key = "agregar_paso") {
                BotonSecundario(
                    texto = stringResource(R.string.editor_agregar_paso),
                    alTocar = vistaModelo::agregarPaso,
                    icono = Iconos.Mas,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fileteArriba(colores.outline)
                        .padding(horizontal = MARGEN, vertical = 12.dp)
                )
            }

            item(key = "notas") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fileteArriba(colores.outline, 2.dp)
                        .padding(MARGEN),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CampoTexto(
                        valor = estado.notas,
                        alCambiar = vistaModelo::cambiarNotas,
                        etiqueta = stringResource(R.string.editor_notas),
                        unaLinea = false,
                        lineasMinimas = 3
                    )
                    BotonPrimario(
                        texto = stringResource(R.string.editor_guardar_receta),
                        alTocar = vistaModelo::guardar,
                        habilitado = !estado.guardando,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
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
private fun FilaLineaIngrediente(
    linea: LineaIngrediente,
    alCambiarCantidad: (String) -> Unit,
    alCambiarUnidad: (Unidad) -> Unit,
    alCambiarRegla: (ReglaEscalado) -> Unit,
    alCambiarAclaracion: (String) -> Unit,
    alQuitar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fileteArriba(MaterialTheme.colorScheme.outline)
            .padding(start = MARGEN, end = 6.dp, top = 6.dp, bottom = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = linea.ingrediente.nombre,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = stringResource(linea.regla.textoId),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.primary
            )
            BotonIcono(
                icono = Iconos.Cerrar,
                descripcion = stringResource(R.string.accion_quitar),
                alTocar = alQuitar,
                tamanioIcono = 18.dp
            )
        }
        Column(
            modifier = Modifier.padding(end = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FilaPareja(
                izquierda = {
                    CampoTexto(
                        valor = linea.cantidad,
                        alCambiar = alCambiarCantidad,
                        etiqueta = stringResource(R.string.editor_cantidad),
                        teclado = KeyboardOptions(keyboardType = KeyboardType.Decimal)
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
            CampoTexto(
                valor = linea.aclaracion,
                alCambiar = alCambiarAclaracion,
                etiqueta = stringResource(R.string.editor_aclaracion)
            )
        }
    }
}

@Composable
private fun FilaLineaPaso(
    numero: Int,
    linea: LineaPaso,
    esPrimero: Boolean,
    esUltimo: Boolean,
    alCambiarTexto: (String) -> Unit,
    alCambiarTimer: (String) -> Unit,
    alSubir: () -> Unit,
    alBajar: () -> Unit,
    alQuitar: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fileteArriba(MaterialTheme.colorScheme.outline)
            .padding(start = MARGEN, end = 6.dp, top = 12.dp, bottom = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = numero.toString(),
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .width(22.dp)
                .padding(top = 10.dp)
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CampoTexto(
                valor = linea.texto,
                alCambiar = alCambiarTexto,
                marcador = stringResource(R.string.editor_paso_texto),
                unaLinea = false,
                lineasMinimas = 2
            )
            CampoTexto(
                valor = linea.minutosTimer,
                alCambiar = alCambiarTimer,
                etiqueta = stringResource(R.string.editor_paso_timer),
                teclado = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
        Column {
            BotonIcono(
                icono = Iconos.Arriba,
                descripcion = stringResource(R.string.editor_subir),
                alTocar = alSubir,
                habilitado = !esPrimero,
                tamanioIcono = 18.dp
            )
            BotonIcono(
                icono = Iconos.Abajo,
                descripcion = stringResource(R.string.editor_bajar),
                alTocar = alBajar,
                habilitado = !esUltimo,
                tamanioIcono = 18.dp
            )
            BotonIcono(
                icono = Iconos.Cerrar,
                descripcion = stringResource(R.string.accion_quitar),
                alTocar = alQuitar,
                tamanioIcono = 18.dp
            )
        }
    }
}
