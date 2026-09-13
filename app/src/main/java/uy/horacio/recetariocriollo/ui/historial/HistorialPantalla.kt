package uy.horacio.recetariocriollo.ui.historial

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import androidx.compose.foundation.layout.size
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uy.horacio.recetariocriollo.R
import uy.horacio.recetariocriollo.dominio.Historial
import uy.horacio.recetariocriollo.dominio.modelo.CocinadaConReceta
import uy.horacio.recetariocriollo.ui.componentes.BarraProgreso
import uy.horacio.recetariocriollo.ui.componentes.BarraSuperior
import uy.horacio.recetariocriollo.ui.componentes.CabeceraSeccion
import uy.horacio.recetariocriollo.ui.componentes.EstadoVacio
import uy.horacio.recetariocriollo.ui.componentes.MARGEN
import uy.horacio.recetariocriollo.ui.componentes.ReproductorNota
import uy.horacio.recetariocriollo.ui.componentes.Rotulo
import uy.horacio.recetariocriollo.ui.componentes.TextoTenue
import uy.horacio.recetariocriollo.ui.componentes.fileteAbajo

@Composable
fun HistorialPantalla(
    vistaModelo: HistorialViewModel,
    alVolver: () -> Unit,
    alAbrirReceta: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val estado by vistaModelo.estado.collectAsStateWithLifecycle()
    val colores = MaterialTheme.colorScheme
    val ahora = remember { System.currentTimeMillis() }

    Column(modifier = modifier.fillMaxSize()) {
        BarraSuperior(
            titulo = stringResource(R.string.historial_titulo),
            alVolver = alVolver,
            descripcionVolver = stringResource(R.string.accion_volver)
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            if (estado.cocinadas.isEmpty() && !estado.cargando) {
                item(key = "vacio") {
                    EstadoVacio(
                        titulo = stringResource(R.string.historial_vacio_titulo),
                        detalle = stringResource(R.string.historial_vacio_detalle)
                    )
                }
            }

            if (estado.ranking.isNotEmpty()) {
                item(key = "ranking") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colores.surfaceVariant)
                            .fileteAbajo(colores.outline, 2.dp)
                            .padding(MARGEN)
                    ) {
                        Rotulo(stringResource(R.string.historial_mas_cocinado), modifier = Modifier.padding(bottom = 12.dp))
                        estado.ranking.forEach { puesto ->
                            Column(
                                modifier = Modifier
                                    .padding(bottom = 10.dp)
                                    .combinedClickable(role = Role.Button, onClick = { alAbrirReceta(puesto.recetaId) })
                            ) {
                                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
                                    Text(
                                        text = puesto.nombre,
                                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                                        modifier = Modifier.weight(1f)
                                    )
                                    TextoTenue(pluralStringResource(R.plurals.historial_veces, puesto.veces, puesto.veces))
                                }
                                BarraProgreso(fraccion = puesto.fraccion, alto = 8.dp)
                            }
                        }
                    }
                }
                item(key = "rotulo_todas") { CabeceraSeccion(stringResource(R.string.historial_todas)) }
            }

            items(estado.cocinadas, key = { it.cocinada.id }) { item ->
                FilaCocinada(
                    item = item,
                    ahora = ahora,
                    alAbrir = { alAbrirReceta(item.cocinada.recetaId) },
                    alBorrar = { vistaModelo.borrar(item.cocinada.id) }
                )
            }
        }
    }
}

@Composable
private fun FilaCocinada(
    item: CocinadaConReceta,
    ahora: Long,
    alAbrir: () -> Unit,
    alBorrar: () -> Unit
) {
    val colores = MaterialTheme.colorScheme
    var menu by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fileteAbajo(colores.outline)
            // Tocar abre la receta; mantener apretado ofrece borrar la anotación.
            .combinedClickable(role = Role.Button, onClick = alAbrir, onLongClick = { menu = true })
            .padding(horizontal = MARGEN, vertical = 13.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = item.nombreReceta,
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 16.sp),
                modifier = Modifier.weight(1f)
            )
            Text(
                text = Historial.estrellas(item.cocinada.estrellas.toDouble()),
                style = MaterialTheme.typography.labelMedium,
                color = colores.primary
            )
            TextoTenue(Historial.fechaCorta(item.cocinada.fechaMillis, ahora))
        }
        item.cocinada.nota?.let { nota ->
            TextoTenue(nota, estilo = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp))
        }
        item.cocinada.audioPath?.let { ruta -> ReproductorNota(ruta = ruta, modifier = Modifier.padding(top = 4.dp)) }
        item.cocinada.fotoPath?.let { ruta ->
            AsyncImage(
                model = ruta,
                contentDescription = stringResource(R.string.como_salio_foto),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(96.dp)
            )
        }
        DropdownMenu(expanded = menu, onDismissRequest = { menu = false }, containerColor = colores.background) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.historial_borrar)) },
                onClick = {
                    menu = false
                    alBorrar()
                }
            )
        }
    }
}
