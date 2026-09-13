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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import uy.horacio.recetariocriollo.R
import uy.horacio.recetariocriollo.dominio.Fracciones
import uy.horacio.recetariocriollo.dominio.ValidacionIngrediente
import uy.horacio.recetariocriollo.dominio.modelo.CategoriaIngrediente
import uy.horacio.recetariocriollo.dominio.modelo.Ingrediente
import uy.horacio.recetariocriollo.dominio.modelo.Unidad
import uy.horacio.recetariocriollo.ui.componentes.CampoTexto
import uy.horacio.recetariocriollo.ui.componentes.Casilla
import uy.horacio.recetariocriollo.ui.componentes.SelectorDesplegable
import uy.horacio.recetariocriollo.ui.componentes.TextoTenue
import uy.horacio.recetariocriollo.ui.textoId

/** Alta rapida de un ingrediente al catalogo desde el editor de recetas. */
@Composable
fun DialogoNuevoIngrediente(
    nombreInicial: String,
    alGuardar: (nombre: String, categoria: CategoriaIngrediente, densidad: Double?, especia: Boolean, unidad: Unidad) -> Unit,
    alCancelar: () -> Unit
) {
    DialogoIngrediente(
        inicial = Ingrediente(nombre = nombreInicial, categoria = CategoriaIngrediente.OTROS),
        catalogo = emptyList(),
        alGuardar = { nuevo ->
            alGuardar(nuevo.nombre, nuevo.categoria, nuevo.densidadGramosPorTaza, nuevo.esSalOEspecia, nuevo.unidadHabitual)
        },
        alCancelar = alCancelar,
        mostrarBasico = false
    )
}

/**
 * Alta o edicion de un ingrediente del catalogo. Valida nombre repetido (sin tildes)
 * contra [catalogo] y densidad razonable antes de dejar guardar.
 */
@Composable
fun DialogoIngrediente(
    inicial: Ingrediente,
    catalogo: List<Ingrediente>,
    alGuardar: (Ingrediente) -> Unit,
    alCancelar: () -> Unit,
    usos: Int = 0,
    alBorrar: (() -> Unit)? = null,
    mostrarBasico: Boolean = true
) {
    val esNuevo = inicial.id == 0L
    var nombre by rememberSaveable { mutableStateOf(inicial.nombre) }
    var categoria by rememberSaveable { mutableStateOf(inicial.categoria) }
    var unidad by rememberSaveable { mutableStateOf(inicial.unidadHabitual) }
    var densidad by rememberSaveable {
        mutableStateOf(inicial.densidadGramosPorTaza?.let { Fracciones.formatearDecimal(it, 1) }.orEmpty())
    }
    var especia by rememberSaveable { mutableStateOf(inicial.esSalOEspecia) }
    var basico by rememberSaveable { mutableStateOf(inicial.esBasicoDeDespensa) }
    val colores = MaterialTheme.colorScheme

    val problema = ValidacionIngrediente.validar(nombre, densidad, catalogo, inicial.id)
    // "Vacio" no se muestra como error: el boton deshabilitado alcanza.
    val mensajeProblema = when (problema) {
        ValidacionIngrediente.Problema.NOMBRE_REPETIDO -> stringResource(R.string.catalogo_nombre_repetido)
        ValidacionIngrediente.Problema.DENSIDAD_INVALIDA -> stringResource(R.string.catalogo_densidad_invalida)
        else -> null
    }

    AlertDialog(
        onDismissRequest = alCancelar,
        containerColor = colores.background,
        title = {
            Text(
                text = stringResource(if (esNuevo) R.string.selector_alta_titulo else R.string.catalogo_editar_titulo),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    alGuardar(
                        inicial.copy(
                            nombre = nombre.trim().replace(Regex("\\s+"), " "),
                            categoria = categoria,
                            densidadGramosPorTaza = Fracciones.parsear(densidad),
                            esSalOEspecia = especia,
                            esBasicoDeDespensa = basico,
                            unidadHabitual = unidad
                        )
                    )
                },
                enabled = problema == null
            ) {
                Text(
                    text = stringResource(if (esNuevo) R.string.selector_alta_guardar else R.string.accion_guardar),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (problema == null) colores.primary else colores.onSurfaceVariant
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
                    teclado = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done)
                )
                OpcionCasilla(
                    marcada = especia,
                    titulo = stringResource(R.string.selector_alta_especia),
                    detalle = stringResource(R.string.selector_alta_especia_ayuda),
                    alTocar = { especia = !especia }
                )
                if (mostrarBasico) {
                    OpcionCasilla(
                        marcada = basico,
                        titulo = stringResource(R.string.catalogo_basico),
                        detalle = stringResource(R.string.catalogo_basico_ayuda),
                        alTocar = { basico = !basico }
                    )
                }
                if (mensajeProblema != null) {
                    Text(mensajeProblema, style = MaterialTheme.typography.bodyMedium, color = colores.error)
                }
                if (alBorrar != null) {
                    if (usos > 0) {
                        TextoTenue(pluralStringResource(R.plurals.catalogo_no_se_puede_borrar, usos, usos))
                    } else {
                        TextButton(onClick = alBorrar) {
                            Text(stringResource(R.string.catalogo_borrar), color = colores.error)
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun OpcionCasilla(marcada: Boolean, titulo: String, detalle: String, alTocar: () -> Unit) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Checkbox, onClick = alTocar)
    ) {
        Casilla(marcada = marcada)
        Column {
            Text(titulo, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground)
            TextoTenue(detalle)
        }
    }
}
