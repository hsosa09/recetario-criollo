package uy.horacio.recetariocriollo.ui.recetas

import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import uy.horacio.recetariocriollo.R
import uy.horacio.recetariocriollo.dominio.Historial
import uy.horacio.recetariocriollo.dominio.modelo.Receta
import uy.horacio.recetariocriollo.dominio.modelo.ResumenCocinadas
import uy.horacio.recetariocriollo.ui.componentes.BarraSuperior
import uy.horacio.recetariocriollo.ui.componentes.BotonIcono
import uy.horacio.recetariocriollo.ui.componentes.BotonSecundario
import uy.horacio.recetariocriollo.ui.componentes.CampoTexto
import uy.horacio.recetariocriollo.ui.componentes.ChipRecto
import uy.horacio.recetariocriollo.ui.componentes.EstadoVacio
import uy.horacio.recetariocriollo.ui.componentes.Etiqueta
import uy.horacio.recetariocriollo.ui.componentes.Iconos
import uy.horacio.recetariocriollo.ui.componentes.MARGEN
import uy.horacio.recetariocriollo.ui.componentes.TextoTenue
import uy.horacio.recetariocriollo.ui.componentes.fileteAbajo
import uy.horacio.recetariocriollo.ui.textoId
import uy.horacio.recetariocriollo.ui.theme.RecetarioTema

@Composable
fun ListaRecetasPantalla(
    vistaModelo: ListaRecetasViewModel,
    alAbrirReceta: (Long) -> Unit,
    alCrearReceta: () -> Unit,
    modifier: Modifier = Modifier
) {
    val estado by vistaModelo.estado.collectAsStateWithLifecycle()
    val filete = MaterialTheme.colorScheme.outline

    Column(modifier = modifier.fillMaxSize()) {
        BarraSuperior(titulo = stringResource(R.string.lista_titulo)) {
            BotonSecundario(
                texto = stringResource(R.string.lista_nueva_receta),
                alTocar = alCrearReceta,
                icono = Iconos.Mas,
                alto = 36.dp
            )
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item(key = "buscador") {
                CampoTexto(
                    valor = estado.texto,
                    alCambiar = vistaModelo::cambiarTexto,
                    marcador = stringResource(R.string.lista_buscar),
                    alto = 42.dp,
                    modifier = Modifier.padding(start = MARGEN, end = MARGEN, top = 14.dp, bottom = 10.dp)
                )
            }

            item(key = "filtros") {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = MARGEN),
                    modifier = Modifier
                        .fillMaxWidth()
                        .fileteAbajo(filete, 2.dp)
                        .padding(bottom = 12.dp)
                ) {
                    item {
                        ChipRecto(
                            texto = stringResource(R.string.lista_solo_favoritas),
                            activo = estado.soloFavoritas,
                            alTocar = vistaModelo::alternarSoloFavoritas
                        )
                    }
                    item {
                        ChipRecto(
                            texto = stringResource(R.string.filtro_todas),
                            activo = estado.categoria == null,
                            alTocar = { vistaModelo.cambiarCategoria(null) }
                        )
                    }
                    items(estado.categoriasDisponibles) { categoria ->
                        ChipRecto(
                            texto = stringResource(categoria.textoId),
                            activo = estado.categoria == categoria,
                            alTocar = {
                                vistaModelo.cambiarCategoria(
                                    if (estado.categoria == categoria) null else categoria
                                )
                            }
                        )
                    }
                }
            }

            if (estado.recetas.isEmpty() && !estado.cargando) {
                item(key = "vacio") {
                    EstadoVacio(
                        titulo = if (estado.hayRecetasCargadas) {
                            stringResource(R.string.lista_sin_resultados)
                        } else {
                            stringResource(R.string.lista_vacia_titulo)
                        },
                        detalle = if (estado.hayRecetasCargadas) stringResource(R.string.lista_sin_resultados_detalle)
                        else stringResource(R.string.lista_vacia_detalle)
                    )
                }
            }

            items(estado.recetas, key = { it.id }) { receta ->
                FilaReceta(
                    receta = receta,
                    resumen = estado.resumenes[receta.id],
                    alTocar = { alAbrirReceta(receta.id) },
                    alAlternarFavorita = { vistaModelo.alternarFavorita(receta) }
                )
            }
        }
    }
}

@Composable
private fun FilaReceta(
    receta: Receta,
    resumen: ResumenCocinadas?,
    alTocar: () -> Unit,
    alAlternarFavorita: () -> Unit
) {
    val colores = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fileteAbajo(colores.outline)
            .clickable(role = Role.Button, onClick = alTocar)
            .padding(start = MARGEN, end = 8.dp, top = 12.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        MiniaturaReceta(receta = receta, lado = 78.dp)

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = receta.nombre,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                modifier = Modifier.padding(bottom = 5.dp)
            )
            TextoTenue(texto = metaDeReceta(receta), modifier = Modifier.padding(bottom = 7.dp))
            LineaDificultadYVeces(receta = receta, resumen = resumen)
        }

        BotonIcono(
            icono = if (receta.esFavorita) Iconos.CorazonLleno else Iconos.CorazonVacio,
            descripcion = stringResource(
                if (receta.esFavorita) R.string.receta_quitar_favorita
                else R.string.receta_marcar_favorita
            ),
            alTocar = alAlternarFavorita,
            color = if (receta.esFavorita) colores.primary else colores.onBackground.copy(alpha = 0.45f),
            tamanioIcono = 19.dp
        )
    }
}

/** "Postres · 8 porciones · 1 h", la linea de datos que acompania al nombre. */
@Composable
fun metaDeReceta(receta: Receta): String {
    val categoria = stringResource(receta.categoria.textoId)
    val porciones = pluralStringResource(R.plurals.receta_porciones, receta.porcionesBase, receta.porcionesBase)
    return listOfNotNull(categoria, porciones, receta.tiempoLegible).joinToString(" · ")
}

/** Foto de la receta, o su inicial sobre un bloque neutro cuando no tiene. */
@Composable
fun MiniaturaReceta(receta: Receta, lado: Dp, modifier: Modifier = Modifier) {
    val extra = RecetarioTema.extra
    if (receta.fotoPath != null) {
        AsyncImage(
            model = receta.fotoPath,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier.size(lado)
        )
    } else {
        Box(
            modifier = modifier
                .size(lado)
                .background(extra.marcador),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = receta.nombre.take(1).uppercase(),
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = (lado.value * 0.44f).sp),
                color = extra.textoMarcador
            )
        }
    }
}

/** "Media · ★★★★½ · cocinada 5 veces", la línea chica de la fila del prototipo. */
@Composable
private fun LineaDificultadYVeces(receta: Receta, resumen: ResumenCocinadas?) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        receta.dificultad?.let { Etiqueta(stringResource(it.textoId)) }
        resumen?.promedioEstrellas?.let { promedio ->
            Text(
                text = Historial.estrellas(promedio),
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.primary
            )
        }
        TextoTenue(
            texto = if (resumen == null || resumen.veces == 0) stringResource(R.string.receta_sin_cocinar)
            else pluralStringResource(R.plurals.receta_cocinada_veces, resumen.veces, resumen.veces),
            estilo = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
        )
    }
}
