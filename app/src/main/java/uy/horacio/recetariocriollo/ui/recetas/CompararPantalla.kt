package uy.horacio.recetariocriollo.ui.recetas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import uy.horacio.recetariocriollo.R
import uy.horacio.recetariocriollo.datos.RecetaRepositorio
import uy.horacio.recetariocriollo.dominio.Cambio
import uy.horacio.recetariocriollo.dominio.ComparacionRecetas
import uy.horacio.recetariocriollo.dominio.Fracciones
import uy.horacio.recetariocriollo.dominio.Variantes
import uy.horacio.recetariocriollo.dominio.modelo.IngredienteDeReceta
import uy.horacio.recetariocriollo.dominio.modelo.Receta
import uy.horacio.recetariocriollo.ui.componentes.BarraSuperior
import uy.horacio.recetariocriollo.ui.componentes.BloqueSeccion
import uy.horacio.recetariocriollo.ui.componentes.EstadoVacio
import uy.horacio.recetariocriollo.ui.componentes.MARGEN
import uy.horacio.recetariocriollo.ui.componentes.Rotulo
import uy.horacio.recetariocriollo.ui.componentes.TextoTenue
import uy.horacio.recetariocriollo.ui.componentes.fileteAbajo
import uy.horacio.recetariocriollo.ui.textoId
import uy.horacio.recetariocriollo.ui.theme.RecetarioTema

class CompararViewModel(repositorio: RecetaRepositorio, estadoGuardado: SavedStateHandle) : ViewModel() {

    private val originalId: Long = estadoGuardado.get<Long>("originalId") ?: 0L
    private val varianteId: Long = estadoGuardado.get<Long>("varianteId") ?: 0L

    val comparacion: StateFlow<ComparacionRecetas?> = combine(
        repositorio.observarReceta(originalId),
        repositorio.observarReceta(varianteId)
    ) { original, variante ->
        if (original != null && variante != null) Variantes.comparar(original, variante) else null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}

/** Original contra variante: qué ingredientes y pasos cambian. */
@Composable
fun CompararPantalla(vistaModelo: CompararViewModel, alVolver: () -> Unit, modifier: Modifier = Modifier) {
    val comparacion by vistaModelo.comparacion.collectAsStateWithLifecycle()
    Column(modifier = modifier.fillMaxSize()) {
        BarraSuperior(
            titulo = stringResource(R.string.comparar_titulo),
            alVolver = alVolver,
            descripcionVolver = stringResource(R.string.accion_volver)
        )
        val c = comparacion ?: return@Column
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            BloqueSeccion(fondo = MaterialTheme.colorScheme.surfaceVariant) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Rotulo(stringResource(R.string.comparar_original))
                        Text(c.original.nombre, style = MaterialTheme.typography.titleMedium)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Rotulo(stringResource(R.string.comparar_variante))
                        Text(c.variante.nombre, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
            if (!c.hayDiferencias) {
                EstadoVacio(titulo = stringResource(R.string.comparar_sin_diferencias))
                return@Column
            }
            if (c.datosDistintos.isNotEmpty()) {
                BloqueSeccion {
                    Rotulo(stringResource(R.string.comparar_datos), modifier = Modifier.padding(bottom = 6.dp))
                    c.datosDistintos.forEach { dato ->
                        val (titulo, antes, despues) = datoLegible(dato, c.original, c.variante)
                        FilaCambio(marca = "~", color = MaterialTheme.colorScheme.primary, texto = "$titulo: $antes → $despues")
                    }
                }
            }
            BloqueSeccion {
                Rotulo(stringResource(R.string.detalle_ingredientes), modifier = Modifier.padding(bottom = 6.dp))
                c.ingredientes.forEach { cambio -> FilaIngredienteCambio(cambio) }
            }
            BloqueSeccion(conFilete = false) {
                Rotulo(stringResource(R.string.detalle_preparacion), modifier = Modifier.padding(bottom = 6.dp))
                c.pasos.forEach { cambio ->
                    when (cambio) {
                        is Cambio.Igual -> FilaCambio(" ", MaterialTheme.colorScheme.onSurfaceVariant, cambio.valor, tenue = true)
                        is Cambio.Agregado -> FilaCambio("+", MaterialTheme.colorScheme.primary, cambio.valor)
                        is Cambio.Quitado -> FilaCambio("−", MaterialTheme.colorScheme.onSurfaceVariant, cambio.valor, tachado = true)
                        is Cambio.Modificado -> {
                            FilaCambio("−", MaterialTheme.colorScheme.onSurfaceVariant, cambio.antes, tachado = true)
                            FilaCambio("+", MaterialTheme.colorScheme.primary, cambio.despues)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilaIngredienteCambio(cambio: Cambio<IngredienteDeReceta>) {
    val colores = MaterialTheme.colorScheme
    when (cambio) {
        is Cambio.Igual -> FilaCambio(" ", colores.onSurfaceVariant, textoIngrediente(cambio.valor), tenue = true)
        is Cambio.Agregado -> FilaCambio("+", colores.primary, textoIngrediente(cambio.valor), etiqueta = stringResource(R.string.comparar_agregado))
        is Cambio.Quitado -> FilaCambio("−", colores.onSurfaceVariant, textoIngrediente(cambio.valor), tachado = true, etiqueta = stringResource(R.string.comparar_quitado))
        is Cambio.Modificado -> FilaCambio(
            "~", colores.primary,
            "${cambio.despues.ingrediente.nombre}: ${cantidad(cambio.antes)} → ${cantidad(cambio.despues)}",
            etiqueta = stringResource(R.string.comparar_cambiado)
        )
    }
}

private fun cantidad(item: IngredienteDeReceta): String =
    Fracciones.formatearConUnidad(item.cantidad, item.unidad) + (item.aclaracion?.let { " ($it)" } ?: "")

private fun textoIngrediente(item: IngredienteDeReceta) = "${cantidad(item)} · ${item.ingrediente.nombre}"

@Composable
private fun FilaCambio(
    marca: String,
    color: Color,
    texto: String,
    tachado: Boolean = false,
    tenue: Boolean = false,
    etiqueta: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fileteAbajo(MaterialTheme.colorScheme.outlineVariant)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(marca, style = MaterialTheme.typography.titleMedium, color = color, modifier = Modifier.width(14.dp))
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyLarge.copy(textDecoration = if (tachado) TextDecoration.LineThrough else null),
            color = if (tenue || tachado) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        if (etiqueta != null) {
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.bodySmall,
                color = RecetarioTema.extra.textoAcentoTenue,
                modifier = Modifier
                    .background(RecetarioTema.extra.acentoTenue)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun datoLegible(dato: ComparacionRecetas.Dato, a: Receta, b: Receta): Triple<String, String, String> = when (dato) {
    ComparacionRecetas.Dato.PORCIONES -> Triple(stringResource(R.string.comparar_dato_porciones), "${a.porcionesBase}", "${b.porcionesBase}")
    ComparacionRecetas.Dato.TIEMPO -> Triple(stringResource(R.string.comparar_dato_tiempo), a.tiempoLegible ?: "—", b.tiempoLegible ?: "—")
    ComparacionRecetas.Dato.DIFICULTAD -> Triple(
        stringResource(R.string.comparar_dato_dificultad),
        a.dificultad?.let { stringResource(it.textoId) } ?: "—",
        b.dificultad?.let { stringResource(it.textoId) } ?: "—"
    )
    ComparacionRecetas.Dato.MOLDE -> Triple(stringResource(R.string.comparar_dato_molde), a.moldeCm?.let { "$it cm" } ?: "—", b.moldeCm?.let { "$it cm" } ?: "—")
    ComparacionRecetas.Dato.CATEGORIA -> Triple(stringResource(R.string.comparar_dato_categoria), stringResource(a.categoria.textoId), stringResource(b.categoria.textoId))
}
