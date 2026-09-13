package uy.horacio.recetariocriollo.ui.cocina

import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import uy.horacio.recetariocriollo.ui.ingredientes.HojaRecetario
import uy.horacio.recetariocriollo.ui.componentes.TextoTenue
import uy.horacio.recetariocriollo.ui.componentes.SelectorEstrellas
import uy.horacio.recetariocriollo.ui.componentes.MARGEN
import uy.horacio.recetariocriollo.ui.componentes.CampoTexto
import uy.horacio.recetariocriollo.ui.componentes.BotonTexto
import uy.horacio.recetariocriollo.ui.componentes.BotonPrimario
import uy.horacio.recetariocriollo.ui.componentes.BotonSecundario
import androidx.compose.foundation.layout.imePadding
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import uy.horacio.recetariocriollo.R
import uy.horacio.recetariocriollo.cronometro.Cronometro
import uy.horacio.recetariocriollo.dominio.modelo.Unidad
import uy.horacio.recetariocriollo.ui.componentes.AvisoRecetario
import uy.horacio.recetariocriollo.ui.componentes.BarraProgreso
import uy.horacio.recetariocriollo.ui.componentes.BotonIcono
import uy.horacio.recetariocriollo.ui.componentes.Iconos
import uy.horacio.recetariocriollo.ui.componentes.Rotulo
import uy.horacio.recetariocriollo.ui.componentes.fileteAbajo
import uy.horacio.recetariocriollo.ui.componentes.fileteArriba
import uy.horacio.recetariocriollo.ui.theme.RecetarioTema
import uy.horacio.recetariocriollo.ui.theme.TemaRecetario

/**
 * Cocinar paso a paso: pantalla completa, oscura, letra grande y la pantalla siempre
 * encendida. Se pasa de paso deslizando o con la botonera de abajo.
 */
@Composable
fun CocinaPantalla(
    vistaModelo: CocinaViewModel,
    alSalir: () -> Unit,
    alTerminar: () -> Unit
) {
    val estado by vistaModelo.estado.collectAsStateWithLifecycle()
    val claroAfuera = !androidx.compose.foundation.isSystemInDarkTheme()

    PantallaEncendidaYBarrasOscuras(claroAfuera)
    BackHandler(onBack = alSalir)

    TemaRecetario(oscuro = true, controlarBarras = false) {
        val receta = estado.receta
        val colores = MaterialTheme.colorScheme
        val acentoSuave = RecetarioTema.extra.rotulo
        val avisos = remember { SnackbarHostState() }
        val alcance = rememberCoroutineScope()
        val recursos = LocalResources.current
        var preguntando by rememberSaveable { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colores.background)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Barra: salir, nombre y "Paso N de M".
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 60.dp)
                        .fileteAbajo(colores.outline, 2.dp)
                        .padding(start = 6.dp, end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BotonIcono(
                        icono = Iconos.Cerrar,
                        descripcion = stringResource(R.string.cocina_salir),
                        alTocar = alSalir
                    )
                    Text(
                        text = receta?.nombre.orEmpty(),
                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (estado.totalPasos > 0) {
                        Text(
                            text = stringResource(R.string.cocina_paso_de, estado.pasoActual + 1, estado.totalPasos),
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
                            color = acentoSuave
                        )
                    }
                }
                BarraProgreso(fraccion = estado.progreso, alto = 3.dp)

                if (receta != null && estado.totalPasos == 0) {
                    Text(
                        text = stringResource(R.string.detalle_sin_pasos),
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(20.dp)
                    )
                }

                if (estado.totalPasos > 0 && receta != null) {
                    val pager = rememberPagerState(initialPage = estado.pasoActual) { estado.totalPasos }

                    // El pager y el ViewModel se siguen mutuamente: deslizar cambia el paso
                    // y los botones mueven el pager.
                    LaunchedEffect(pager) {
                        snapshotFlow { pager.settledPage }.collect { vistaModelo.irAPaso(it) }
                    }
                    LaunchedEffect(estado.pasoActual) {
                        if (pager.currentPage != estado.pasoActual) pager.animateScrollToPage(estado.pasoActual)
                    }

                    HorizontalPager(
                        state = pager,
                        modifier = Modifier.weight(1f),
                        key = { receta.pasos[it].id }
                    ) { indice ->
                        val paso = receta.pasos[indice]
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 20.dp, vertical = 24.dp)
                        ) {
                            Text(
                                text = (indice + 1).toString(),
                                style = MaterialTheme.typography.displaySmall.copy(fontSize = 76.sp, lineHeight = 70.sp),
                                color = colores.primary,
                                modifier = Modifier.padding(bottom = 18.dp)
                            )
                            Text(
                                text = paso.texto,
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 27.sp, lineHeight = 38.sp),
                                modifier = Modifier
                                    .padding(bottom = 24.dp)
                                    .semantics { heading() }
                            )
                            paso.fotoPath?.let { ruta ->
                                AsyncImage(
                                    model = ruta,
                                    contentDescription = stringResource(R.string.editor_foto_paso),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(220.dp)
                                        .padding(bottom = 24.dp)
                                )
                            }
                            paso.timerSugeridoSegundos?.let { segundos ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 56.dp)
                                        .background(colores.primary)
                                        .clickable(role = Role.Button) {
                                            val etiqueta = vistaModelo.arrancarTimerDelPaso()
                                            if (etiqueta != null) {
                                                alcance.launch {
                                                    avisos.showSnackbar(recursos.getString(R.string.detalle_timer_arrancado, etiqueta))
                                                }
                                            }
                                        }
                                        .padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Iconos.Timer, contentDescription = null, tint = colores.onPrimary, modifier = Modifier.size(18.dp))
                                    Text(
                                        text = stringResource(R.string.cocina_arrancar, Cronometro.describirDuracion(segundos)),
                                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 17.sp),
                                        color = colores.onPrimary
                                    )
                                }
                            }

                            estado.timersVivos.forEach { timer ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = if (timer == estado.timersVivos.first()) 24.dp else 0.dp)
                                        .fileteArriba(colores.outlineVariant)
                                        .padding(vertical = 10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = timer.etiqueta,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = colores.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = Cronometro.formatearSegundos(timer.restanteSegundos(estado.ahora)),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = acentoSuave
                                    )
                                }
                            }

                            if (estado.ingredientes.isNotEmpty()) {
                                Column(
                                    modifier = Modifier
                                        .padding(top = 28.dp)
                                        .fillMaxWidth()
                                        .fileteArriba(colores.outline, 2.dp)
                                        .padding(top = 16.dp)
                                ) {
                                    Rotulo(
                                        texto = stringResource(R.string.cocina_ingredientes),
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    )
                                    estado.ingredientes.forEach { item ->
                                        val destacado = item.ingrediente.id in estado.mencionados
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .fileteAbajo(colores.outlineVariant)
                                                .padding(vertical = 8.dp),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Text(
                                                text = if (item.ingrediente.unidad == Unidad.A_GUSTO) "—" else item.texto,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = if (destacado) colores.onBackground else colores.onSurfaceVariant,
                                                modifier = Modifier.widthIn(min = 92.dp, max = 170.dp)
                                            )
                                            Text(
                                                text = item.ingrediente.ingrediente.nombre,
                                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp),
                                                color = if (destacado) colores.onBackground else colores.onSurfaceVariant,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Botonera fija: Anterior | Siguiente o Terminé.
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .fileteArriba(colores.outline, 2.dp)
                    ) {
                        val puedeVolver = estado.pasoActual > 0
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable(enabled = puedeVolver, role = Role.Button, onClick = vistaModelo::anterior)
                                .padding(start = 18.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = stringResource(R.string.cocina_anterior),
                                style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp),
                                color = if (puedeVolver) colores.onBackground else colores.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                        }
                        Box(Modifier.width(1.dp).fillMaxHeight().background(colores.outline))
                        Box(
                            modifier = Modifier
                                .weight(1.4f)
                                .fillMaxHeight()
                                .background(colores.primary)
                                .clickable(role = Role.Button) {
                                    if (estado.esUltimo) preguntando = true else vistaModelo.siguiente()
                                }
                                .padding(start = 18.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = stringResource(if (estado.esUltimo) R.string.cocina_termine else R.string.cocina_siguiente),
                                style = MaterialTheme.typography.labelLarge.copy(fontSize = 17.sp),
                                color = colores.onPrimary
                            )
                        }
                    }
                }
            }

            if (preguntando) {
                val foto by vistaModelo.fotoResultado.collectAsStateWithLifecycle()
                HojaComoSalio(
                    foto = foto,
                    nuevoArchivoCamara = vistaModelo::archivoParaCamara,
                    alElegirFoto = { uri, alTerminar -> vistaModelo.usarFotoResultado(uri, alTerminar) },
                    alQuitarFoto = vistaModelo::descartarFotoResultado,
                    alGuardar = { estrellas, nota ->
                        preguntando = false
                        vistaModelo.registrarCocinada(estrellas, nota, alTerminar)
                    },
                    alSaltar = {
                        preguntando = false
                        vistaModelo.descartarFotoResultado()
                        alTerminar()
                    },
                    alCerrar = { preguntando = false }
                )
            }

            SnackbarHost(
                hostState = avisos,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 64.dp)
            ) { AvisoRecetario(it) }
        }
    }
}

/**
 * Mientras se cocina la pantalla no se apaga y los iconos del sistema van claros sobre
 * el fondo oscuro. Al salir se deja todo como estaba.
 */
@Composable
private fun PantallaEncendidaYBarrasOscuras(claroAfuera: Boolean) {
    val vista = LocalView.current
    val actividad = LocalActivity.current
    DisposableEffect(claroAfuera) {
        val ventana = actividad?.window
        ventana?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val barras = ventana?.let { WindowCompat.getInsetsController(it, vista) }
        barras?.isAppearanceLightStatusBars = false
        barras?.isAppearanceLightNavigationBars = false
        onDispose {
            ventana?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            barras?.isAppearanceLightStatusBars = claroAfuera
            barras?.isAppearanceLightNavigationBars = claroAfuera
        }
    }
}

/** «¿Cómo salió?»: estrellas y qué cambiar la próxima. Queda en el historial de la receta. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HojaComoSalio(
    foto: String?,
    nuevoArchivoCamara: () -> java.io.File,
    alElegirFoto: (android.net.Uri, alTerminar: () -> Unit) -> Unit,
    alQuitarFoto: () -> Unit,
    alGuardar: (estrellas: Int, nota: String) -> Unit,
    alSaltar: () -> Unit,
    alCerrar: () -> Unit
) {
    var estrellas by rememberSaveable { mutableIntStateOf(5) }
    var nota by rememberSaveable { mutableStateOf("") }
    val recursos = LocalResources.current
    val contexto = androidx.compose.ui.platform.LocalContext.current
    // Ruta del archivo que se le pasó a la cámara: sobrevive a que el sistema mate la app mientras se saca la foto.
    var tomaEnCurso by rememberSaveable { mutableStateOf<String?>(null) }
    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { sacada ->
        val ruta = tomaEnCurso
        tomaEnCurso = null
        if (ruta != null) {
            val archivo = java.io.File(ruta)
            // La toma se copia reducida a files/fotos; el original de la cámara se borra siempre.
            if (sacada && archivo.length() > 0) alElegirFoto(android.net.Uri.fromFile(archivo)) { archivo.delete() }
            else archivo.delete()
        }
    }
    val galeria = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { alElegirFoto(it) {} }
    }
    HojaRecetario(
        estado = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        alCerrar = alCerrar
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = MARGEN)
                .padding(top = 18.dp, bottom = 20.dp)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.como_salio_titulo), style = MaterialTheme.typography.headlineSmall)
                TextoTenue(stringResource(R.string.como_salio_detalle))
            }
            SelectorEstrellas(
                valor = estrellas,
                alElegir = { estrellas = it },
                descripcion = { n -> recursos.getQuantityString(R.plurals.estrellas_descripcion, n, n) }
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (foto != null) {
                    AsyncImage(
                        model = foto,
                        contentDescription = stringResource(R.string.como_salio_foto),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(64.dp)
                    )
                }
                BotonSecundario(
                    texto = stringResource(if (foto == null) R.string.como_salio_sacar_foto else R.string.como_salio_otra_foto),
                    icono = Iconos.Foto,
                    alto = 40.dp,
                    alTocar = {
                        val archivo = nuevoArchivoCamara()
                        tomaEnCurso = archivo.absolutePath
                        camara.launch(FileProvider.getUriForFile(contexto, "${contexto.packageName}.archivos", archivo))
                    }
                )
                BotonSecundario(
                    texto = stringResource(R.string.como_salio_elegir_foto),
                    alto = 40.dp,
                    alTocar = { galeria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                )
                if (foto != null) BotonTexto(texto = stringResource(R.string.accion_quitar), alTocar = alQuitarFoto)
            }
            CampoTexto(
                valor = nota,
                alCambiar = { nota = it },
                etiqueta = stringResource(R.string.como_salio_nota),
                marcador = stringResource(R.string.como_salio_nota_ejemplo),
                unaLinea = false,
                lineasMinimas = 3
            )
            BotonPrimario(
                texto = stringResource(R.string.como_salio_guardar),
                alTocar = { alGuardar(estrellas, nota) },
                modifier = Modifier.fillMaxWidth()
            )
            BotonTexto(texto = stringResource(R.string.como_salio_saltar), alTocar = alSaltar)
        }
    }
}
