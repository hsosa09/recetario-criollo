package uy.horacio.recetariocriollo.ui.conversor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uy.horacio.recetariocriollo.R
import uy.horacio.recetariocriollo.dominio.Fracciones
import uy.horacio.recetariocriollo.dominio.NivelHorno
import uy.horacio.recetariocriollo.dominio.modelo.Ingrediente
import uy.horacio.recetariocriollo.dominio.modelo.Unidad
import uy.horacio.recetariocriollo.ui.componentes.FilaPareja
import uy.horacio.recetariocriollo.ui.componentes.SelectorDesplegable
import uy.horacio.recetariocriollo.ui.textoId

private val UNIDADES_CONVERTIBLES = Unidad.deVolumen + Unidad.dePeso

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversorPantalla(
    vistaModelo: ConversorViewModel,
    modifier: Modifier = Modifier
) {
    val estado by vistaModelo.estado.collectAsStateWithLifecycle()
    var pestania by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.conversor_titulo)) }) }
    ) { relleno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno)
        ) {
            PrimaryTabRow(selectedTabIndex = pestania) {
                Tab(
                    selected = pestania == 0,
                    onClick = { pestania = 0 },
                    text = { Text(stringResource(R.string.conversor_tab_medidas)) }
                )
                Tab(
                    selected = pestania == 1,
                    onClick = { pestania = 1 },
                    text = { Text(stringResource(R.string.conversor_tab_horno)) }
                )
                Tab(
                    selected = pestania == 2,
                    onClick = { pestania = 2 },
                    text = { Text(stringResource(R.string.conversor_tab_levadura)) }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (pestania) {
                    0 -> SeccionMedidas(estado, vistaModelo)
                    1 -> SeccionHorno(estado, vistaModelo)
                    else -> SeccionLevadura(estado, vistaModelo)
                }
            }
        }
    }
}

@Composable
private fun SeccionMedidas(estado: EstadoConversor, vistaModelo: ConversorViewModel) {
    OutlinedTextField(
        value = estado.cantidad,
        onValueChange = vistaModelo::cambiarCantidad,
        label = { Text(stringResource(R.string.conversor_cantidad)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth()
    )

    FilaPareja(
        izquierda = {
            SelectorDesplegable(
                etiqueta = stringResource(R.string.conversor_de),
                seleccion = estado.desde,
                opciones = UNIDADES_CONVERTIBLES,
                textoDe = { it.plural },
                alElegir = vistaModelo::cambiarDesde
            )
        },
        derecha = {
            SelectorDesplegable(
                etiqueta = stringResource(R.string.conversor_a),
                seleccion = estado.hasta,
                opciones = UNIDADES_CONVERTIBLES,
                textoDe = { it.plural },
                alElegir = vistaModelo::cambiarHasta
            )
        }
    )

    FilledTonalButton(onClick = vistaModelo::invertir) {
        Icon(Icons.Default.SwapVert, contentDescription = null)
        Text(
            text = stringResource(R.string.conversor_invertir),
            modifier = Modifier.padding(start = 8.dp)
        )
    }

    // Pasar de tazas a gramos depende del ingrediente: una taza de harina y una de
    // azucar no pesan lo mismo.
    val sinIngrediente = stringResource(R.string.conversor_tab_ingrediente)
    SelectorDesplegable<Ingrediente?>(
        etiqueta = stringResource(R.string.conversor_ingrediente),
        seleccion = estado.ingrediente,
        opciones = listOf<Ingrediente?>(null) + estado.catalogoConDensidad,
        textoDe = { it?.nombre ?: sinIngrediente },
        alElegir = vistaModelo::elegirIngrediente
    )

    estado.ingrediente?.densidadGramosPorTaza?.let { densidad ->
        Text(
            text = stringResource(
                R.string.conversor_densidad_dato,
                estado.ingrediente.nombre,
                Fracciones.formatearDecimal(densidad, 0)
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.conversor_resultado),
                style = MaterialTheme.typography.titleMedium
            )
            when {
                estado.faltaDensidad -> Text(
                    text = stringResource(R.string.conversor_necesita_densidad),
                    style = MaterialTheme.typography.bodyLarge
                )

                estado.resultado != null -> Text(
                    text = Fracciones.formatearConUnidad(
                        Fracciones.redondearParaCocina(estado.resultado, estado.hasta),
                        estado.hasta
                    ).ifBlank { Fracciones.formatearDecimal(estado.resultado) },
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold
                )

                else -> Text("—", style = MaterialTheme.typography.displaySmall)
            }
        }
    }

    Text(
        text = stringResource(R.string.conversor_equivalencias),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
    )
    EQUIVALENCIAS.forEach { (izquierda, derecha) ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(izquierda, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = derecha,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun SeccionHorno(estado: EstadoConversor, vistaModelo: ConversorViewModel) {
    FilaPareja(
        izquierda = {
            OutlinedTextField(
                value = estado.celsius,
                onValueChange = vistaModelo::cambiarCelsius,
                label = { Text(stringResource(R.string.conversor_horno_celsius)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        },
        derecha = {
            OutlinedTextField(
                value = estado.fahrenheit,
                onValueChange = vistaModelo::cambiarFahrenheit,
                label = { Text(stringResource(R.string.conversor_horno_fahrenheit)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.conversor_horno_referencia),
                style = MaterialTheme.typography.titleMedium
            )
            val nivel = estado.nivelHorno
            if (nivel == null) {
                Text(stringResource(R.string.conversor_horno_fuera_escala))
            } else {
                Text(
                    text = stringResource(nivel.textoId),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(text = nivel.rangoCelsius, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }

    Text(
        text = stringResource(R.string.conversor_horno_tabla),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
    )
    NivelHorno.entries.forEach { nivel ->
        Card(
            onClick = { vistaModelo.elegirNivelHorno(nivel) },
            colors = CardDefaults.cardColors(
                containerColor = if (estado.nivelHorno == nivel) {
                    MaterialTheme.colorScheme.tertiaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(nivel.textoId),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text(nivel.rangoCelsius, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = nivel.rangoFahrenheit,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SeccionLevadura(estado: EstadoConversor, vistaModelo: ConversorViewModel) {
    OutlinedTextField(
        value = estado.levaduraFresca,
        onValueChange = vistaModelo::cambiarLevaduraFresca,
        label = { Text(stringResource(R.string.conversor_levadura_fresca)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = estado.levaduraSeca,
        onValueChange = vistaModelo::cambiarLevaduraSeca,
        label = { Text(stringResource(R.string.conversor_levadura_seca)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth()
    )
    Text(
        text = stringResource(R.string.conversor_levadura_nota),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/** Las equivalencias que uno termina buscando siempre. */
private val EQUIVALENCIAS: List<Pair<String, String>> = listOf(
    "1 taza" to "240 ml",
    "1 cucharada" to "15 ml",
    "1 cucharadita" to "5 ml",
    "1 taza" to "16 cucharadas",
    "1 cucharada" to "3 cucharaditas",
    "1 kg" to "2,2 lb",
    "1 lb" to "454 g",
    "1 oz" to "28 g"
)
