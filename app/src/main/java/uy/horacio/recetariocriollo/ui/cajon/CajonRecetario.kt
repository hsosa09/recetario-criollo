package uy.horacio.recetariocriollo.ui.cajon

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uy.horacio.recetariocriollo.R
import uy.horacio.recetariocriollo.ui.componentes.MARGEN
import uy.horacio.recetariocriollo.ui.componentes.TextoTenue
import uy.horacio.recetariocriollo.ui.componentes.fileteAbajo
import uy.horacio.recetariocriollo.ui.navegacion.RutaAjustes
import uy.horacio.recetariocriollo.ui.navegacion.RutaCatalogo
import uy.horacio.recetariocriollo.ui.navegacion.RutaEstadisticas
import uy.horacio.recetariocriollo.ui.navegacion.RutaHistorial

/**
 * Cajón lateral del prototipo: resumen de la biblioteca y lo secundario de la app.
 * Solo lista pantallas que existen; las del prototipo se suman a medida que se hacen.
 */
@Composable
fun CajonRecetario(
    resumen: ResumenBiblioteca,
    alIr: (Any) -> Unit,
    modifier: Modifier = Modifier
) {
    val colores = MaterialTheme.colorScheme
    val filete = colores.outline
    Column(
        modifier = modifier
            .width(300.dp)
            .fillMaxHeight()
            .background(colores.background)
            .drawBehind {
                val grosor = 2.dp.toPx()
                drawRect(filete, topLeft = Offset(size.width - grosor, 0f), size = size.copy(width = grosor))
            }
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fileteAbajo(filete, 2.dp)
                .padding(start = MARGEN, end = MARGEN, top = 18.dp, bottom = 14.dp)
        ) {
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp))
            TextoTenue(
                texto = stringResource(
                    R.string.cajon_resumen,
                    pluralStringResource(R.plurals.cajon_recetas, resumen.recetas, resumen.recetas),
                    pluralStringResource(R.plurals.cajon_ingredientes, resumen.ingredientes, resumen.ingredientes)
                ),
                modifier = Modifier.padding(top = 3.dp)
            )
        }
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            EntradaCajon(
                nombre = stringResource(R.string.historial_titulo_largo),
                detalle = if (resumen.cocinadas == 0) stringResource(R.string.cajon_sin_cocinadas)
                else pluralStringResource(R.plurals.cajon_cocinadas, resumen.cocinadas, resumen.cocinadas),
                alTocar = { alIr(RutaHistorial) }
            )
            EntradaCajon(
                nombre = stringResource(R.string.estadisticas_titulo),
                detalle = stringResource(R.string.cajon_estadisticas_detalle),
                alTocar = { alIr(RutaEstadisticas) }
            )
            EntradaCajon(
                nombre = stringResource(R.string.catalogo_titulo),
                detalle = pluralStringResource(R.plurals.cajon_ingredientes, resumen.ingredientes, resumen.ingredientes),
                alTocar = { alIr(RutaCatalogo) }
            )
            EntradaCajon(
                nombre = stringResource(R.string.ajustes_titulo),
                detalle = stringResource(R.string.cajon_ajustes_detalle),
                alTocar = { alIr(RutaAjustes) }
            )
        }
    }
}

@Composable
private fun EntradaCajon(nombre: String, detalle: String, alTocar: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fileteAbajo(MaterialTheme.colorScheme.outline)
            .clickable(role = Role.Button, onClick = alTocar)
            .padding(horizontal = MARGEN, vertical = 14.dp)
    ) {
        Text(nombre, style = MaterialTheme.typography.titleSmall)
        TextoTenue(detalle, estilo = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp))
    }
}
