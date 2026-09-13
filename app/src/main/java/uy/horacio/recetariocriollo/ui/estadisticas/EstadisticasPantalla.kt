package uy.horacio.recetariocriollo.ui.estadisticas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uy.horacio.recetariocriollo.R
import uy.horacio.recetariocriollo.dominio.Destacado
import uy.horacio.recetariocriollo.dominio.EstadisticasAnio
import uy.horacio.recetariocriollo.dominio.Fracciones
import uy.horacio.recetariocriollo.dominio.Historial
import uy.horacio.recetariocriollo.ui.componentes.BarraSuperior
import uy.horacio.recetariocriollo.ui.componentes.BloqueSeccion
import uy.horacio.recetariocriollo.ui.componentes.ChipRecto
import uy.horacio.recetariocriollo.ui.componentes.MARGEN
import uy.horacio.recetariocriollo.ui.componentes.Rotulo
import uy.horacio.recetariocriollo.ui.componentes.TextoTenue
import uy.horacio.recetariocriollo.ui.componentes.fileteAbajo

private val INICIALES_MES = listOf("E", "F", "M", "A", "M", "J", "J", "A", "S", "O", "N", "D")

@Composable
fun EstadisticasPantalla(
    vistaModelo: EstadisticasViewModel,
    alVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val estado by vistaModelo.estado.collectAsStateWithLifecycle()
    Column(modifier = modifier.fillMaxSize()) {
        BarraSuperior(
            titulo = stringResource(R.string.estadisticas_titulo),
            alVolver = alVolver,
            descripcionVolver = stringResource(R.string.accion_volver)
        )
        val datos = estado.datos ?: return@Column
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            if (estado.anios.size > 1) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = MARGEN, vertical = 12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .fileteAbajo(MaterialTheme.colorScheme.outline, 2.dp)
                ) {
                    items(estado.anios) { anio ->
                        ChipRecto(texto = anio.toString(), activo = anio == datos.anio, alTocar = { vistaModelo.elegirAnio(anio) })
                    }
                }
            }
            Resumen(datos)
            BarrasPorMes(datos)
            Destacados(datos)
            if (datos.sinCocinar.isNotEmpty()) {
                BloqueSeccion(conFilete = false) {
                    Rotulo(stringResource(R.string.estadisticas_sin_cocinar), modifier = Modifier.padding(bottom = 8.dp))
                    TextoTenue(
                        texto = datos.sinCocinar.joinToString(" · ") { it.nombre },
                        estilo = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun Resumen(datos: EstadisticasAnio) {
    BloqueSeccion(fondo = MaterialTheme.colorScheme.surfaceVariant) {
        Rotulo(stringResource(R.string.estadisticas_en_el_anio, datos.anio), modifier = Modifier.padding(bottom = 8.dp))
        Text(
            text = pluralStringResource(R.plurals.estadisticas_veces, datos.total, datos.total),
            style = MaterialTheme.typography.displaySmall
        )
        TextoTenue(
            texto = pluralStringResource(R.plurals.estadisticas_dias, datos.diasCocinando, datos.diasCocinando) + " · " +
                pluralStringResource(R.plurals.estadisticas_racha, datos.rachaMasLarga, datos.rachaMasLarga),
            estilo = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

/** Doce barras rectas en acento, con la cantidad arriba y la inicial del mes abajo. */
@Composable
private fun BarrasPorMes(datos: EstadisticasAnio) {
    val colores = MaterialTheme.colorScheme
    val maximo = (datos.porMes.maxOrNull() ?: 0).coerceAtLeast(1)
    val descripcion = datos.porMes.mapIndexed { i, n -> "${INICIALES_MES[i]} $n" }.joinToString(", ")
    BloqueSeccion {
        Rotulo(stringResource(R.string.estadisticas_por_mes), modifier = Modifier.padding(bottom = 12.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            datos.porMes.forEach { n ->
                Text(
                    text = if (n > 0) n.toString() else "",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.sp),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(vertical = 4.dp)
                .semantics { contentDescription = descripcion }
        ) {
            val ancho = size.width / 12f
            val grosor = ancho * 0.62f
            datos.porMes.forEachIndexed { i, n ->
                val x = i * ancho + (ancho - grosor) / 2
                // Pista de fondo para que se vea el mes vacío.
                drawRect(colores.outlineVariant, topLeft = Offset(x, size.height - 2.dp.toPx()), size = Size(grosor, 2.dp.toPx()))
                if (n > 0) {
                    val alto = size.height * n / maximo
                    drawRect(colores.primary, topLeft = Offset(x, size.height - alto), size = Size(grosor, alto))
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            INICIALES_MES.forEach { inicial ->
                TextoTenue(
                    texto = inicial,
                    estilo = MaterialTheme.typography.bodySmall.copy(textAlign = TextAlign.Center),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun Destacados(datos: EstadisticasAnio) {
    BloqueSeccion {
        Rotulo(stringResource(R.string.estadisticas_destacados), modifier = Modifier.padding(bottom = 4.dp))
        FilaDestacado(
            titulo = stringResource(R.string.estadisticas_mas_cocinada),
            destacado = datos.masCocinada,
            detalle = { pluralStringResource(R.plurals.historial_veces, it.valor.toInt(), it.valor.toInt()) }
        )
        FilaDestacado(
            titulo = stringResource(R.string.estadisticas_mejor_calificada),
            destacado = datos.mejorCalificada,
            detalle = { "${Historial.estrellas(it.valor)} ${Fracciones.formatearDecimal(it.valor, 1)}" }
        )
        FilaDestacado(
            titulo = stringResource(R.string.estadisticas_ingrediente),
            destacado = datos.ingredienteMasUsado,
            detalle = { pluralStringResource(R.plurals.estadisticas_en_cocinadas, it.valor.toInt(), it.valor.toInt()) }
        )
    }
}

@Composable
private fun FilaDestacado(titulo: String, destacado: Destacado?, detalle: @Composable (Destacado) -> String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fileteAbajo(MaterialTheme.colorScheme.outlineVariant)
            .padding(vertical = 10.dp)
    ) {
        TextoTenue(titulo)
        if (destacado == null) {
            Text(stringResource(R.string.estadisticas_sin_datos), style = MaterialTheme.typography.titleSmall)
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(destacado.nombre, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text(detalle(destacado), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
