package uy.horacio.recetariocriollo.ui.cronometro

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uy.horacio.recetariocriollo.R
import uy.horacio.recetariocriollo.cronometro.Cronometro
import uy.horacio.recetariocriollo.cronometro.EstadoCronometro
import uy.horacio.recetariocriollo.cronometro.GestorCronometros
import uy.horacio.recetariocriollo.cronometro.Notificaciones
import uy.horacio.recetariocriollo.ui.componentes.EstadoVacio
import uy.horacio.recetariocriollo.ui.componentes.FilaPareja

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CronometrosPantalla(
    vistaModelo: CronometrosViewModel,
    modifier: Modifier = Modifier
) {
    val cronometros by vistaModelo.cronometros.collectAsStateWithLifecycle()
    val ahora by vistaModelo.ahora.collectAsStateWithLifecycle()
    val contexto = LocalContext.current

    var etiqueta by rememberSaveable { mutableStateOf("") }
    var minutos by rememberSaveable { mutableStateOf("") }
    var segundos by rememberSaveable { mutableStateOf("") }
    var hayPermiso by remember { mutableStateOf(Notificaciones.hayPermiso(contexto)) }

    val pedirPermiso = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido -> hayPermiso = concedido }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.timers_titulo)) },
                actions = {
                    if (cronometros.any { it.estado == EstadoCronometro.TERMINADO }) {
                        TextButton(onClick = vistaModelo::quitarTerminados) {
                            Text(stringResource(R.string.timers_limpiar_terminados))
                        }
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
            if (!hayPermiso && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(stringResource(R.string.timers_permiso))
                            TextButton(
                                onClick = { pedirPermiso.launch(Manifest.permission.POST_NOTIFICATIONS) }
                            ) { Text(stringResource(R.string.timers_permiso_boton)) }
                        }
                    }
                }
            }

            item {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GestorCronometros.ATAJOS_SEGUNDOS.forEach { atajo ->
                        AssistChip(
                            onClick = {
                                vistaModelo.crear(etiqueta.ifBlank { "" }, atajo)
                                etiqueta = ""
                            },
                            label = {
                                Text(
                                    if (atajo >= 3600) stringResource(R.string.timers_atajo_hora)
                                    else stringResource(R.string.timers_atajo_minutos, atajo / 60)
                                )
                            }
                        )
                    }
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.timers_nuevo),
                            style = MaterialTheme.typography.titleMedium
                        )
                        OutlinedTextField(
                            value = etiqueta,
                            onValueChange = { etiqueta = it },
                            label = { Text(stringResource(R.string.timers_etiqueta)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        FilaPareja(
                            izquierda = {
                                OutlinedTextField(
                                    value = minutos,
                                    onValueChange = { minutos = it },
                                    label = { Text(stringResource(R.string.timers_minutos)) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            },
                            derecha = {
                                OutlinedTextField(
                                    value = segundos,
                                    onValueChange = { segundos = it },
                                    label = { Text(stringResource(R.string.timers_segundos)) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        )
                        Button(
                            onClick = {
                                val total = (minutos.trim().toIntOrNull() ?: 0) * 60 +
                                    (segundos.trim().toIntOrNull() ?: 0)
                                vistaModelo.crear(etiqueta, total)
                                etiqueta = ""
                                minutos = ""
                                segundos = ""
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(stringResource(R.string.timers_arrancar)) }
                    }
                }
            }

            if (cronometros.isEmpty()) {
                item {
                    EstadoVacio(
                        icono = Icons.Default.Timer,
                        titulo = stringResource(R.string.timers_vacio),
                        detalle = stringResource(R.string.timers_vacio_detalle)
                    )
                }
            }

            items(cronometros, key = { it.id }) { cronometro ->
                TarjetaCronometro(
                    cronometro = cronometro,
                    ahora = ahora,
                    alPausar = { vistaModelo.pausar(cronometro.id) },
                    alReanudar = { vistaModelo.reanudar(cronometro.id) },
                    alReiniciar = { vistaModelo.reiniciar(cronometro.id) },
                    alQuitar = { vistaModelo.quitar(cronometro.id) },
                    alAjustar = { vistaModelo.ajustar(cronometro.id, it) }
                )
            }
        }
    }
}

@Composable
private fun TarjetaCronometro(
    cronometro: Cronometro,
    ahora: Long,
    alPausar: () -> Unit,
    alReanudar: () -> Unit,
    alReiniciar: () -> Unit,
    alQuitar: () -> Unit,
    alAjustar: (Int) -> Unit
) {
    val terminado = cronometro.estado == EstadoCronometro.TERMINADO
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (terminado) MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = cronometro.etiqueta,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = alQuitar) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(R.string.timers_quitar)
                    )
                }
            }

            Text(
                text = if (terminado) stringResource(R.string.timers_terminado)
                else Cronometro.formatearSegundos(cronometro.restanteSegundos(ahora)),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )

            LinearProgressIndicator(
                progress = { cronometro.progreso(ahora) },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (cronometro.estado == EstadoCronometro.CORRIENDO) {
                    TextButton(onClick = alPausar) {
                        Icon(Icons.Default.Pause, contentDescription = null)
                        Text(
                            text = stringResource(R.string.timers_pausar),
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                } else {
                    TextButton(onClick = alReanudar) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Text(
                            text = stringResource(R.string.timers_reanudar),
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
                TextButton(onClick = alReiniciar) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Text(
                        text = stringResource(R.string.timers_reiniciar),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                TextButton(onClick = { alAjustar(60) }) {
                    Text(stringResource(R.string.timers_mas_minuto))
                }
                TextButton(onClick = { alAjustar(-60) }) {
                    Text(stringResource(R.string.timers_menos_minuto))
                }
            }
        }
    }
}
