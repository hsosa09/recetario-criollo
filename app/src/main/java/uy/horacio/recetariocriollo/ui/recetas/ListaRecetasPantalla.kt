package uy.horacio.recetariocriollo.ui.recetas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import uy.horacio.recetariocriollo.R
import uy.horacio.recetariocriollo.dominio.modelo.Receta
import uy.horacio.recetariocriollo.ui.textoId

@Composable
fun ListaRecetasPantalla(
    vistaModelo: ListaRecetasViewModel,
    alAbrirReceta: (Long) -> Unit,
    alCrearReceta: () -> Unit,
    modifier: Modifier = Modifier
) {
    val estado by vistaModelo.estado.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = alCrearReceta,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.lista_nueva_receta)) }
            )
        }
    ) { relleno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno)
        ) {
            OutlinedTextField(
                value = estado.texto,
                onValueChange = vistaModelo::cambiarTexto,
                label = { Text(stringResource(R.string.lista_buscar)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = estado.soloFavoritas,
                        onClick = vistaModelo::alternarSoloFavoritas,
                        label = { Text(stringResource(R.string.lista_solo_favoritas)) },
                        leadingIcon = {
                            Icon(
                                imageVector = if (estado.soloFavoritas) Icons.Default.Favorite
                                else Icons.Default.FavoriteBorder,
                                contentDescription = null
                            )
                        }
                    )
                }
                item {
                    FilterChip(
                        selected = estado.categoria == null,
                        onClick = { vistaModelo.cambiarCategoria(null) },
                        label = { Text(stringResource(R.string.filtro_todas)) }
                    )
                }
                items(estado.categoriasDisponibles) { categoria ->
                    FilterChip(
                        selected = estado.categoria == categoria,
                        onClick = {
                            vistaModelo.cambiarCategoria(
                                if (estado.categoria == categoria) null else categoria
                            )
                        },
                        label = { Text(stringResource(categoria.textoId)) }
                    )
                }
            }

            if (estado.recetas.isEmpty() && !estado.cargando) {
                uy.horacio.recetariocriollo.ui.componentes.EstadoVacio(
                    icono = Icons.AutoMirrored.Filled.MenuBook,
                    titulo = if (estado.hayRecetasCargadas) {
                        stringResource(R.string.lista_sin_resultados)
                    } else {
                        stringResource(R.string.lista_vacia_titulo)
                    },
                    detalle = if (estado.hayRecetasCargadas) null
                    else stringResource(R.string.lista_vacia_detalle)
                )
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(estado.recetas, key = { it.id }) { receta ->
                    TarjetaReceta(
                        receta = receta,
                        alTocar = { alAbrirReceta(receta.id) },
                        alAlternarFavorita = { vistaModelo.alternarFavorita(receta) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaReceta(
    receta: Receta,
    alTocar: () -> Unit,
    alAlternarFavorita: () -> Unit
) {
    Card(
        onClick = alTocar,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (receta.fotoPath != null) {
                AsyncImage(
                    model = receta.fotoPath,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = alTocar),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = receta.nombre,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = stringResource(receta.categoria.textoId),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.receta_porciones, receta.porcionesBase),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    receta.tiempoLegible?.let { tiempo ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(text = tiempo, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            IconButton(onClick = alAlternarFavorita) {
                Icon(
                    imageVector = if (receta.esFavorita) Icons.Default.Favorite
                    else Icons.Default.FavoriteBorder,
                    contentDescription = stringResource(
                        if (receta.esFavorita) R.string.receta_quitar_favorita
                        else R.string.receta_marcar_favorita
                    ),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
