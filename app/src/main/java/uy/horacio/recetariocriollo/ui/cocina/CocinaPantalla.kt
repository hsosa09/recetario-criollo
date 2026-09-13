package uy.horacio.recetariocriollo.ui.cocina

import android.view.WindowManager
import androidx.activity.compose.BackHandler
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    alTerminar: (recetaId: Long, porciones: Int) -> Unit
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
                                    if (estado.esUltimo) alTerminar(receta.id, estado.porciones) else vistaModelo.siguiente()
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
