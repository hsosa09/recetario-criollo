package uy.horacio.recetariocriollo.ui.ingredientes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import uy.horacio.recetariocriollo.R
import uy.horacio.recetariocriollo.dominio.Texto
import uy.horacio.recetariocriollo.dominio.modelo.CategoriaIngrediente
import uy.horacio.recetariocriollo.dominio.modelo.Ingrediente
import uy.horacio.recetariocriollo.ui.componentes.BotonSecundario
import uy.horacio.recetariocriollo.ui.componentes.CampoTexto
import uy.horacio.recetariocriollo.ui.componentes.Casilla
import uy.horacio.recetariocriollo.ui.componentes.Iconos
import uy.horacio.recetariocriollo.ui.componentes.MARGEN
import uy.horacio.recetariocriollo.ui.componentes.Rotulo
import uy.horacio.recetariocriollo.ui.componentes.TextoTenue
import uy.horacio.recetariocriollo.ui.componentes.fileteAbajo
import uy.horacio.recetariocriollo.ui.componentes.fileteArriba
import uy.horacio.recetariocriollo.ui.textoId

/**
 * Selector por lista del catalogo normalizado.
 *
 * Es el unico camino para meter un ingrediente en una receta o en la busqueda:
 * el buscador de arriba filtra la lista, pero lo que se elige siempre es una
 * ficha existente del catalogo (o una nueva que se da de alta en el momento).
 */
@Composable
fun ListaCatalogoIngredientes(
    catalogo: List<Ingrediente>,
    seleccionados: Set<Long>,
    alElegir: (Ingrediente) -> Unit,
    modifier: Modifier = Modifier,
    conCasillas: Boolean = true,
    alCrearNuevo: ((String) -> Unit)? = null
) {
    var filtro by remember { mutableStateOf("") }

    val visibles = remember(catalogo, filtro) {
        catalogo.filter { Texto.contiene(it.nombre, filtro) }
    }
    val porCategoria = remember(visibles) {
        visibles.groupBy { it.categoria }
            .toSortedMap(compareBy { it.ordinal })
    }

    Column(modifier = modifier) {
        CampoTexto(
            valor = filtro,
            alCambiar = { filtro = it },
            marcador = stringResource(R.string.selector_filtro),
            modifier = Modifier.padding(horizontal = MARGEN, vertical = 10.dp)
        )

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            if (visibles.isEmpty()) {
                item {
                    TextoTenue(
                        texto = stringResource(R.string.selector_sin_resultados),
                        estilo = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(horizontal = MARGEN, vertical = 20.dp)
                    )
                }
            }
            porCategoria.forEach { (categoria, ingredientes) ->
                stickyHeader(key = "cabecera_${categoria.name}") {
                    CabeceraCategoria(categoria)
                }
                items(ingredientes, key = { it.id }) { ingrediente ->
                    FilaIngrediente(
                        ingrediente = ingrediente,
                        marcado = ingrediente.id in seleccionados,
                        conCasilla = conCasillas,
                        alTocar = { alElegir(ingrediente) }
                    )
                }
            }
            if (alCrearNuevo != null) {
                item {
                    BotonSecundario(
                        texto = stringResource(R.string.selector_crear, filtro.trim()),
                        alTocar = { alCrearNuevo(filtro.trim()) },
                        icono = Iconos.Mas,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(MARGEN)
                    )
                }
            }
        }
    }
}

@Composable
private fun CabeceraCategoria(categoria: CategoriaIngrediente) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .fileteAbajo(MaterialTheme.colorScheme.outline)
            .padding(horizontal = MARGEN)
            .padding(top = 12.dp, bottom = 8.dp)
    ) {
        Rotulo(stringResource(categoria.textoId))
    }
}

@Composable
private fun FilaIngrediente(
    ingrediente: Ingrediente,
    marcado: Boolean,
    conCasilla: Boolean,
    alTocar: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fileteAbajo(MaterialTheme.colorScheme.outline)
            .clickable(role = if (conCasilla) Role.Checkbox else Role.Button, onClick = alTocar)
            .heightIn(min = 48.dp)
            .padding(horizontal = MARGEN, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (conCasilla) Casilla(marcada = marcado)
        Text(
            text = ingrediente.nombre,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        ingrediente.densidadGramosPorTaza?.let { gramos ->
            TextoTenue(stringResource(R.string.selector_densidad_corta, gramos.toInt()))
        }
    }
}

/** El mismo selector, pero en hoja inferior y para elegir de a uno. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HojaSelectorIngrediente(
    catalogo: List<Ingrediente>,
    alElegir: (Ingrediente) -> Unit,
    alCerrar: () -> Unit,
    alCrearNuevo: (String) -> Unit
) {
    val estado = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    HojaRecetario(estado = estado, alCerrar = alCerrar) {
        Text(
            text = stringResource(R.string.selector_titulo),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(horizontal = MARGEN).padding(top = 18.dp)
        )
        ListaCatalogoIngredientes(
            catalogo = catalogo,
            seleccionados = emptySet(),
            alElegir = alElegir,
            conCasillas = false,
            alCrearNuevo = alCrearNuevo,
            modifier = Modifier.heightIn(max = 560.dp)
        )
    }
}

/** Hoja inferior del sistema: recta, con filete de tinta arriba y sin manija. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HojaRecetario(
    estado: androidx.compose.material3.SheetState,
    alCerrar: () -> Unit,
    contenido: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = alCerrar,
        sheetState = estado,
        shape = MaterialTheme.shapes.large,
        containerColor = MaterialTheme.colorScheme.background,
        scrimColor = BottomSheetDefaults.ScrimColor,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier.fileteArriba(MaterialTheme.colorScheme.onBackground, 2.dp),
            content = contenido
        )
    }
}
