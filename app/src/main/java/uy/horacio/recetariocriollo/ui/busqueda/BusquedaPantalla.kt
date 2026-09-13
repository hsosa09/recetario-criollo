package uy.horacio.recetariocriollo.ui.busqueda

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uy.horacio.recetariocriollo.R
import uy.horacio.recetariocriollo.dominio.CoincidenciaReceta
import uy.horacio.recetariocriollo.ui.componentes.BarraProgreso
import uy.horacio.recetariocriollo.ui.componentes.BarraSuperior
import uy.horacio.recetariocriollo.ui.componentes.BloqueSeccion
import uy.horacio.recetariocriollo.ui.componentes.BotonSecundario
import uy.horacio.recetariocriollo.ui.componentes.CabeceraSeccion
import uy.horacio.recetariocriollo.ui.componentes.ChipRecto
import uy.horacio.recetariocriollo.ui.componentes.EstadoVacio
import uy.horacio.recetariocriollo.ui.componentes.MARGEN
import uy.horacio.recetariocriollo.ui.componentes.Rotulo
import uy.horacio.recetariocriollo.ui.componentes.TextoTenue
import uy.horacio.recetariocriollo.ui.componentes.fileteAbajo
import uy.horacio.recetariocriollo.ui.componentes.fileteArriba
import uy.horacio.recetariocriollo.ui.textoId

@Composable
fun BusquedaPantalla(
    vistaModelo: BusquedaViewModel,
    alAbrirReceta: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val estado by vistaModelo.estado.collectAsStateWithLifecycle()
    val colores = MaterialTheme.colorScheme
    val porCategoria = estado.ingredientesDeRecetas
        .groupBy { it.categoria }
        .toSortedMap(compareBy { it.ordinal })

    Column(modifier = modifier.fillMaxSize()) {
        BarraSuperior(titulo = stringResource(R.string.busqueda_titulo))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item(key = "cabecera") {
                BloqueSeccion {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 10.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.busqueda_elegir),
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.weight(1f)
                        )
                        TextoTenue(pluralStringResource(R.plurals.busqueda_seleccionados, estado.seleccionados.size, estado.seleccionados.size))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ChipRecto(
                            texto = stringResource(R.string.busqueda_asumir_basicos),
                            activo = estado.asumirBasicos,
                            alTocar = vistaModelo::alternarBasicos,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        BotonSecundario(
                            texto = stringResource(R.string.busqueda_limpiar),
                            alTocar = vistaModelo::limpiar,
                            habilitado = estado.seleccionados.isNotEmpty(),
                            alto = 36.dp
                        )
                    }
                }
            }

            porCategoria.forEach { (categoria, ingredientes) ->
                item(key = "grupo_${categoria.name}") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fileteAbajo(colores.outline)
                            .padding(horizontal = MARGEN, vertical = 12.dp)
                    ) {
                        Rotulo(
                            texto = stringResource(categoria.textoId),
                            color = colores.onBackground.copy(alpha = 0.5f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ingredientes.forEach { ingrediente ->
                                ChipRecto(
                                    texto = ingrediente.nombre,
                                    activo = ingrediente.id in estado.seleccionados,
                                    alTocar = { vistaModelo.alternar(ingrediente) }
                                )
                            }
                        }
                    }
                }
            }

            item(key = "rotulo_resultados") {
                CabeceraSeccion(
                    rotulo = stringResource(R.string.busqueda_que_podes),
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .fileteArriba(colores.outline, 2.dp)
                )
            }

            when {
                estado.seleccionados.isEmpty() -> item(key = "sin_seleccion") {
                    EstadoVacio(
                        titulo = stringResource(R.string.busqueda_sin_seleccion),
                        modifier = Modifier.fileteArriba(colores.outline)
                    )
                }

                estado.resultados.isEmpty() -> item(key = "sin_resultados") {
                    EstadoVacio(
                        titulo = stringResource(R.string.busqueda_sin_resultados),
                        modifier = Modifier.fileteArriba(colores.outline)
                    )
                }

                else -> items(estado.resultados, key = { it.receta.id }) { coincidencia ->
                    FilaCoincidencia(
                        coincidencia = coincidencia,
                        alTocar = { alAbrirReceta(coincidencia.receta.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FilaCoincidencia(
    coincidencia: CoincidenciaReceta,
    alTocar: () -> Unit
) {
    val colores = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fileteArriba(colores.outline)
            .clickable(role = Role.Button, onClick = alTocar)
            .padding(horizontal = MARGEN, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = coincidencia.receta.nombre,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = stringResource(R.string.busqueda_coincidencia, coincidencia.porcentaje),
                style = MaterialTheme.typography.titleSmall,
                color = colores.primary
            )
        }
        BarraProgreso(fraccion = coincidencia.porcentaje / 100f)
        Text(
            text = when {
                coincidencia.sePuedeCocinar -> stringResource(R.string.busqueda_completa)
                else -> pluralStringResource(
                    R.plurals.busqueda_faltan,
                    coincidencia.faltantes.size,
                    coincidencia.faltantes.size
                )
            },
            style = MaterialTheme.typography.labelMedium,
            color = if (coincidencia.sePuedeCocinar) colores.primary else colores.onSurfaceVariant
        )
        if (coincidencia.faltantes.isNotEmpty()) {
            TextoTenue(
                stringResource(
                    R.string.busqueda_faltantes,
                    coincidencia.faltantes.joinToString { it.nombre }
                )
            )
        }
    }
}
