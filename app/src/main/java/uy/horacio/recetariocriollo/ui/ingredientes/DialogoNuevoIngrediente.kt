package uy.horacio.recetariocriollo.ui.ingredientes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import uy.horacio.recetariocriollo.R
import uy.horacio.recetariocriollo.dominio.Fracciones
import uy.horacio.recetariocriollo.dominio.modelo.CategoriaIngrediente
import uy.horacio.recetariocriollo.dominio.modelo.Unidad
import uy.horacio.recetariocriollo.ui.componentes.CampoTexto
import uy.horacio.recetariocriollo.ui.componentes.Casilla
import uy.horacio.recetariocriollo.ui.componentes.SelectorDesplegable
import uy.horacio.recetariocriollo.ui.componentes.TextoTenue
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
    val colores = MaterialTheme.colorScheme

    AlertDialog(
        onDismissRequest = alCancelar,
        containerColor = colores.background,
        title = { Text(stringResource(R.string.selector_alta_titulo), style = MaterialTheme.typography.headlineSmall) },
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
            ) {
                Text(
                    text = stringResource(R.string.selector_alta_guardar),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (nombre.isNotBlank()) colores.primary else colores.onSurfaceVariant
                )
            }
        },
        dismissButton = {
            TextButton(onClick = alCancelar) {
                Text(
                    text = stringResource(R.string.accion_cancelar),
                    style = MaterialTheme.typography.labelLarge,
                    color = colores.onBackground
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                CampoTexto(
                    valor = nombre,
                    alCambiar = { nombre = it },
                    etiqueta = stringResource(R.string.selector_alta_nombre)
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
                CampoTexto(
                    valor = densidad,
                    alCambiar = { densidad = it },
                    etiqueta = stringResource(R.string.selector_alta_densidad),
                    ayuda = stringResource(R.string.selector_alta_densidad_ayuda),
                    teclado = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    )
                )
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(role = Role.Checkbox) { especia = !especia }
                ) {
                    Casilla(marcada = especia)
                    Column {
                        Text(stringResource(R.string.selector_alta_especia), style = MaterialTheme.typography.titleSmall)
                        TextoTenue(stringResource(R.string.selector_alta_especia_ayuda))
                    }
                }
            }
        }
    )
}
