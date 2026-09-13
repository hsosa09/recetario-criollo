package uy.horacio.recetariocriollo.ui.cronometro

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uy.horacio.recetariocriollo.R
import uy.horacio.recetariocriollo.cronometro.Cronometro
import uy.horacio.recetariocriollo.cronometro.EstadoCronometro
import uy.horacio.recetariocriollo.cronometro.GestorCronometros
import uy.horacio.recetariocriollo.cronometro.Notificaciones
import uy.horacio.recetariocriollo.ui.componentes.BarraProgreso
import uy.horacio.recetariocriollo.ui.componentes.BarraSuperior
import uy.horacio.recetariocriollo.ui.componentes.BloqueSeccion
import uy.horacio.recetariocriollo.ui.componentes.BotonPrimario
import uy.horacio.recetariocriollo.ui.componentes.BotonSecundario
import uy.horacio.recetariocriollo.ui.componentes.CampoTexto
import uy.horacio.recetariocriollo.ui.componentes.EstadoVacio
import uy.horacio.recetariocriollo.ui.componentes.MARGEN
import uy.horacio.recetariocriollo.ui.componentes.TextoTenue
import uy.horacio.recetariocriollo.ui.componentes.contiguo
import uy.horacio.recetariocriollo.ui.componentes.fileteAbajo
import uy.horacio.recetariocriollo.ui.theme.RecetarioTema

@Composable
fun CronometrosPantalla(
    vistaModelo: CronometrosViewModel,
    modifier: Modifier = Modifier
) {
    val cronometros by vistaModelo.cronometros.collectAsStateWithLifecycle()
    val ahora by vistaModelo.ahora.collectAsStateWithLifecycle()
    val contexto = LocalContext.current
    val colores = MaterialTheme.colorScheme

    var etiqueta by rememberSaveable { mutableStateOf("") }
    var minutos by rememberSaveable { mutableStateOf("") }
    var segundos by rememberSaveable { mutableStateOf("") }
    var hayPermiso by remember { mutableStateOf(Notificaciones.hayPermiso(contexto)) }
    var alarmasExactas by remember { mutableStateOf(vistaModelo.alarmasExactasPermitidas()) }

    // El permiso de alarmas exactas se concede en Ajustes: se revisa cada vez que se vuelve.
    LifecycleResumeEffect(Unit) {
        alarmasExactas = vistaModelo.alarmasExactasPermitidas()
        vistaModelo.alVolverALaPantalla()
        onPauseOrDispose { }
    }

    val pedirPermiso = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido -> hayPermiso = concedido }

    Column(modifier = modifier.fillMaxSize()) {
        BarraSuperior(titulo = stringResource(R.string.timers_titulo))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {
            if (!hayPermiso && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                item(key = "permiso") {
                    BloqueSeccion(fondo = RecetarioTema.extra.acentoTenue) {
                        Text(
                            text = stringResource(R.string.timers_permiso),
                            style = MaterialTheme.typography.bodyMedium,
                            color = RecetarioTema.extra.textoAcentoTenue,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                        BotonSecundario(
                            texto = stringResource(R.string.timers_permiso_boton),
                            alTocar = { pedirPermiso.launch(Manifest.permission.POST_NOTIFICATIONS) },
                            alto = 40.dp
                        )
                    }
                }
            }

            if (!alarmasExactas) {
                item(key = "alarmas_exactas") {
                    BloqueSeccion(fondo = RecetarioTema.extra.acentoTenue) {
                        Text(
                            text = stringResource(R.string.timers_alarmas_exactas),
                            style = MaterialTheme.typography.bodyMedium,
                            color = RecetarioTema.extra.textoAcentoTenue,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                        BotonSecundario(
                            texto = stringResource(R.string.timers_alarmas_exactas_boton),
                            alTocar = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    contexto.startActivity(
                                        Intent(
                                            Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                                            Uri.parse("package:${contexto.packageName}")
                                        )
                                    )
                                }
                            },
                            alto = 40.dp
                        )
                    }
                }
            }

            item(key = "nuevo") {
                BloqueSeccion {
                    CampoTexto(
                        valor = etiqueta,
                        alCambiar = { etiqueta = it },
                        marcador = stringResource(R.string.timers_etiqueta),
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    // Atajos en grilla de 4 por fila, casilleros contiguos.
                    GestorCronometros.ATAJOS_SEGUNDOS.chunked(4).forEachIndexed { numeroFila, fila ->
                        Row(modifier = Modifier.fillMaxWidth().contiguo(numeroFila, vertical = true)) {
                            fila.forEachIndexed { columna, atajo ->
                                BotonSecundario(
                                    texto = if (atajo >= 3600) stringResource(R.string.timers_atajo_hora)
                                    else stringResource(R.string.timers_atajo_minutos, atajo / 60),
                                    alTocar = {
                                        vistaModelo.crear(etiqueta, atajo)
                                        etiqueta = ""
                                    },
                                    alto = 48.dp,
                                    modifier = Modifier.weight(1f).contiguo(columna)
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        CampoTexto(
                            valor = minutos,
                            alCambiar = { minutos = it },
                            marcador = stringResource(R.string.timers_minutos),
                            teclado = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        CampoTexto(
                            valor = segundos,
                            alCambiar = { segundos = it },
                            marcador = stringResource(R.string.timers_segundos),
                            teclado = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        val total = (minutos.trim().toIntOrNull() ?: 0) * 60 +
                            (segundos.trim().toIntOrNull() ?: 0)
                        BotonPrimario(
                            texto = stringResource(R.string.timers_arrancar),
                            alto = 44.dp,
                            // Sin tiempo el ViewModel no crea nada: mejor que el boton lo diga.
                            habilitado = total > 0,
                            alTocar = {
                                vistaModelo.crear(etiqueta, total)
                                etiqueta = ""
                                minutos = ""
                                segundos = ""
                            }
                        )
                    }
                }
            }

            if (cronometros.isEmpty()) {
                item(key = "vacio") {
                    EstadoVacio(
                        titulo = stringResource(R.string.timers_vacio),
                        detalle = stringResource(R.string.timers_vacio_detalle)
                    )
                }
            }

            items(cronometros, key = { it.id }) { cronometro ->
                FilaCronometro(
                    cronometro = cronometro,
                    ahora = ahora,
                    alPausar = { vistaModelo.pausar(cronometro.id) },
                    alReanudar = { vistaModelo.reanudar(cronometro.id) },
                    alReiniciar = { vistaModelo.reiniciar(cronometro.id) },
                    alQuitar = { vistaModelo.quitar(cronometro.id) },
                    alAjustar = { vistaModelo.ajustar(cronometro.id, it) }
                )
            }

            if (cronometros.any { it.estado == EstadoCronometro.TERMINADO }) {
                item(key = "limpiar") {
                    BotonSecundario(
                        texto = stringResource(R.string.timers_limpiar_terminados),
                        alTocar = vistaModelo::quitarTerminados,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(MARGEN)
                    )
                }
            }
        }
    }
}

@Composable
private fun FilaCronometro(
    cronometro: Cronometro,
    ahora: Long,
    alPausar: () -> Unit,
    alReanudar: () -> Unit,
    alReiniciar: () -> Unit,
    alQuitar: () -> Unit,
    alAjustar: (Int) -> Unit
) {
    val colores = MaterialTheme.colorScheme
    val terminado = cronometro.estado == EstadoCronometro.TERMINADO
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fileteAbajo(colores.outline)
            .padding(horizontal = MARGEN, vertical = 14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            TextoTenue(
                texto = cronometro.etiqueta,
                estilo = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                maxLineas = 2,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = if (terminado) stringResource(R.string.timers_terminado)
                else Cronometro.formatearSegundos(cronometro.restanteSegundos(ahora)),
                style = MaterialTheme.typography.displaySmall.copy(fontSize = 30.sp),
                color = if (terminado) colores.primary else colores.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Clip
            )
        }
        BarraProgreso(
            fraccion = cronometro.progreso(ahora),
            color = if (terminado) colores.primary else colores.onBackground,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            val acciones = buildList {
                when (cronometro.estado) {
                    EstadoCronometro.CORRIENDO -> add(stringResource(R.string.timers_pausar) to alPausar)
                    EstadoCronometro.PAUSADO -> add(stringResource(R.string.timers_reanudar) to alReanudar)
                    EstadoCronometro.TERMINADO -> Unit
                }
                add(stringResource(R.string.timers_reiniciar) to alReiniciar)
                add(stringResource(R.string.timers_menos_minuto) to { alAjustar(-60) })
                add(stringResource(R.string.timers_mas_minuto) to { alAjustar(60) })
                add(stringResource(R.string.timers_quitar) to alQuitar)
            }
            acciones.forEachIndexed { indice, (texto, accion) ->
                AccionCronometro(texto, accion, Modifier.weight(1f).contiguo(indice))
            }
        }
    }
}

@Composable
private fun AccionCronometro(texto: String, alTocar: () -> Unit, modifier: Modifier) {
    BotonSecundario(
        texto = texto,
        alTocar = alTocar,
        alto = 40.dp,
        centrado = true,
        modifier = modifier
    )
}
