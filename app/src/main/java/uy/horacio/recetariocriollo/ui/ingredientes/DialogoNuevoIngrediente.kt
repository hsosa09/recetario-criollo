package uy.horacio.recetariocriollo.ui.ingredientes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import uy.horacio.recetariocriollo.R
import uy.horacio.recetariocriollo.dominio.Fracciones
import uy.horacio.recetariocriollo.dominio.modelo.CategoriaIngrediente
import uy.horacio.recetariocriollo.dominio.modelo.Unidad
import uy.horacio.recetariocriollo.ui.componentes.SelectorDesplegable
import uy.horacio.recetariocriollo.ui.textoId

/** Alta rapida de un ingrediente al catalogo, sin salir de donde estabas. */
@Composable
fun DialogoNuevoIngrediente(
    nombreInicial: String,
    alGuardar: (nombre: String, categoria: CategoriaIngrediente, densidad: Double?, especia: Boolean, unidad: Unidad) -> Unit,
    alCancelar: () -> Unit
) {
    var nombre by rememberSaveable { mutableStateOf(nombreInicial) }
    var categoria by remember { mutableStateOf(CategoriaIngrediente.OTROS) }
    var unidad by remember { mutableStateOf(Unidad.GRAMO) }
    var densidad by rememberSaveable { mutableStateOf("") }
    var especia by rememberSaveable { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = alCancelar,
        title = { Text(stringResource(R.string.selector_alta_titulo)) },
        confirmButton = {
            TextButton(
                onClick = {
                    alGuardar(
                        nombre.trim(),
                        categoria,
                        Fracciones.parsear(densidad),
                        especia,
                        unidad
                    )
                },
                enabled = nombre.isNotBlank()
            ) { Text(stringResource(R.string.selector_alta_guardar)) }
        },
        dismissButton = {
            TextButton(onClick = alCancelar) { Text(stringResource(R.string.accion_cancelar)) }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text(stringResource(R.string.selector_alta_nombre)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                SelectorDesplegable(
                    etiqueta = stringResource(R.string.selector_alta_categoria),
                    seleccion = categoria,
                    opciones = CategoriaIngrediente.entries,
                    textoDe = { stringResource(it.textoId) },
                    alElegir = { categoria = it }
                )
                SelectorDesplegable(
                    etiqueta = stringResource(R.string.editor_unidad),
                    seleccion = unidad,
                    opciones = Unidad.entries,
                    textoDe = { it.plural },
                    alElegir = { unidad = it }
                )
                OutlinedTextField(
                    value = densidad,
                    onValueChange = { densidad = it },
                    label = { Text(stringResource(R.string.selector_alta_densidad)) },
                    supportingText = { Text(stringResource(R.string.selector_alta_densidad_ayuda)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Switch(checked = especia, onCheckedChange = { especia = it })
                    Column {
                        Text(stringResource(R.string.selector_alta_especia))
                        Text(
                            text = stringResource(R.string.selector_alta_especia_ayuda),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    )
}
