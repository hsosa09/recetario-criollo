package uy.horacio.recetariocriollo.ui.ingredientes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import uy.horacio.recetariocriollo.R
import uy.horacio.recetariocriollo.dominio.modelo.CategoriaIngrediente
import uy.horacio.recetariocriollo.dominio.modelo.Ingrediente
import uy.horacio.recetariocriollo.ui.textoId

/**
 * Selector por lista del catalogo normalizado.
 *
 * Es el unico camino para meter un ingrediente en una receta o en la busqueda:
 * el buscador de arriba filtra la lista, pero lo que se elige siempre es una
 * ficha existente del catalogo (o una nueva que se da de alta en el momento).
 */
@Composable
fun ListaCatalogoIngredientes(
    catalogo: List<Ingrediente>,
    seleccionados: Set<Long>,
    alElegir: (Ingrediente) -> Unit,
    modifier: Modifier = Modifier,
    conCasillas: Boolean = true,
    alCrearNuevo: ((String) -> Unit)? = null
) {
    var filtro by remember { mutableStateOf("") }

    val visibles = remember(catalogo, filtro) {
        val texto = filtro.trim().lowercase()
        if (texto.isEmpty()) catalogo
        else catalogo.filter { it.nombre.lowercase().contains(texto) }
    }
    val porCategoria = remember(visibles) {
        visibles.groupBy { it.categoria }
            .toSortedMap(compareBy { it.ordinal })
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = filtro,
            onValueChange = { filtro = it },
            label = { Text(stringResource(R.string.selector_filtro)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            if (visibles.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.selector_sin_resultados),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
            porCategoria.forEach { (categoria, ingredientes) ->
                stickyHeader(key = "cabecera_${categoria.name}") {
                    CabeceraCategoria(categoria)
                }
                items(ingredientes, key = { it.id }) { ingrediente ->
                    FilaIngrediente(
                        ingrediente = ingrediente,
                        marcado = ingrediente.id in seleccionados,
                        conCasilla = conCasillas,
                        alTocar = { alElegir(ingrediente) }
                    )
                }
            }
            if (alCrearNuevo != null) {
                item {
                    TextButton(
                        onClick = { alCrearNuevo(filtro.trim()) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Text(
                            text = stringResource(R.string.selector_crear, filtro.trim()),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CabeceraCategoria(categoria: CategoriaIngrediente) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(
            text = stringResource(categoria.textoId),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FilaIngrediente(
    ingrediente: Ingrediente,
    marcado: Boolean,
    conCasilla: Boolean,
    alTocar: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = alTocar)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (conCasilla) {
            Checkbox(checked = marcado, onCheckedChange = { alTocar() })
        }
        Text(
            text = ingrediente.nombre,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
    }
}

/** El mismo selector, pero en hoja inferior y para elegir de a uno. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HojaSelectorIngrediente(
    catalogo: List<Ingrediente>,
    alElegir: (Ingrediente) -> Unit,
    alCerrar: () -> Unit,
    alCrearNuevo: (String) -> Unit
) {
    val estado = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = alCerrar, sheetState = estado) {
        Text(
            text = stringResource(R.string.selector_titulo),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        ListaCatalogoIngredientes(
            catalogo = catalogo,
            seleccionados = emptySet(),
            alElegir = alElegir,
            conCasillas = false,
            alCrearNuevo = alCrearNuevo,
            modifier = Modifier.heightIn(max = 560.dp)
        )
    }
}
