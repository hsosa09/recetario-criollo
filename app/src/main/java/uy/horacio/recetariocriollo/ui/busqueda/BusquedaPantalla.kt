package uy.horacio.recetariocriollo.ui.busqueda

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uy.horacio.recetariocriollo.R
import uy.horacio.recetariocriollo.dominio.CoincidenciaReceta
import uy.horacio.recetariocriollo.ui.componentes.EstadoVacio
import uy.horacio.recetariocriollo.ui.ingredientes.ListaCatalogoIngredientes
import uy.horacio.recetariocriollo.ui.textoId

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BusquedaPantalla(
    vistaModelo: BusquedaViewModel,
    alAbrirReceta: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val estado by vistaModelo.estado.collectAsStateWithLifecycle()
    var eligiendo by remember { mutableStateOf(false) }
    val hojaEstado = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.busqueda_titulo)) }) }
    ) { relleno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno)
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.busqueda_elegir),
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (estado.seleccionados.isNotEmpty()) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            estado.ingredientesElegidos.forEach { ingrediente ->
                                InputChip(
                                    selected = true,
                                    onClick = { vistaModelo.alternar(ingrediente) },
                                    label = { Text(ingrediente.nombre) }
                                )
                            }
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(onClick = { eligiendo = true }) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Text(
                                text = stringResource(R.string.busqueda_seleccionados, estado.seleccionados.size),
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                        if (estado.seleccionados.isNotEmpty()) {
                            TextButton(onClick = vistaModelo::limpiar) {
                                Text(stringResource(R.string.busqueda_limpiar))
                            }
                        }
                    }
                    FilterChip(
                        selected = estado.asumirBasicos,
                        onClick = vistaModelo::alternarBasicos,
                        label = { Text(stringResource(R.string.busqueda_asumir_basicos)) },
                        leadingIcon = {
                            if (estado.asumirBasicos) {
                                Icon(Icons.Default.Check, contentDescription = null)
                            }
                        }
                    )
                }
            }

            when {
                estado.seleccionados.isEmpty() -> EstadoVacio(
                    icono = Icons.Default.Kitchen,
                    titulo = stringResource(R.string.busqueda_sin_seleccion)
                )

                estado.resultados.isEmpty() -> EstadoVacio(
                    icono = Icons.Default.Kitchen,
                    titulo = stringResource(R.string.busqueda_sin_resultados)
                )

                else -> LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(estado.resultados, key = { it.receta.id }) { coincidencia ->
                        TarjetaCoincidencia(
                            coincidencia = coincidencia,
                            alTocar = { alAbrirReceta(coincidencia.receta.id) }
                        )
                    }
                }
            }
        }
    }

    if (eligiendo) {
        ModalBottomSheet(
            onDismissRequest = { eligiendo = false },
            sheetState = hojaEstado
        ) {
            ListaCatalogoIngredientes(
                catalogo = estado.catalogo,
                seleccionados = estado.seleccionados,
                alElegir = vistaModelo::alternar,
                conCasillas = true,
                modifier = Modifier.heightIn(max = 560.dp)
            )
            TextButton(
                onClick = { eligiendo = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) { Text(stringResource(R.string.accion_listo)) }
        }
    }
}

@Composable
private fun TarjetaCoincidencia(
    coincidencia: CoincidenciaReceta,
    alTocar: () -> Unit
) {
    Card(onClick = alTocar, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = coincidencia.receta.nombre,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = stringResource(R.string.busqueda_coincidencia, coincidencia.porcentaje),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            LinearProgressIndicator(
                progress = { coincidencia.porcentaje / 100f },
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = stringResource(coincidencia.receta.categoria.textoId),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            when {
                coincidencia.sePuedeCocinar -> AssistChip(
                    onClick = alTocar,
                    label = { Text(stringResource(R.string.busqueda_completa)) },
                    leadingIcon = { Icon(Icons.Default.Check, contentDescription = null) }
                )

                coincidencia.faltantes.size == 1 -> Text(
                    text = stringResource(R.string.busqueda_falta_uno),
                    style = MaterialTheme.typography.bodyLarge
                )

                else -> Text(
                    text = stringResource(R.string.busqueda_faltan, coincidencia.faltantes.size),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            if (coincidencia.faltantes.isNotEmpty()) {
                Text(
                    text = stringResource(
                        R.string.busqueda_faltantes,
                        coincidencia.faltantes.joinToString { it.nombre }
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
