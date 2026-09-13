package uy.horacio.recetariocriollo.ui.catalogo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import uy.horacio.recetariocriollo.R
import uy.horacio.recetariocriollo.dominio.Fracciones
import uy.horacio.recetariocriollo.dominio.Texto
import uy.horacio.recetariocriollo.dominio.modelo.CategoriaIngrediente
import uy.horacio.recetariocriollo.dominio.modelo.Ingrediente
import uy.horacio.recetariocriollo.ui.componentes.AvisoRecetario
import uy.horacio.recetariocriollo.ui.componentes.BarraSuperior
import uy.horacio.recetariocriollo.ui.componentes.BotonSecundario
import uy.horacio.recetariocriollo.ui.componentes.CampoTexto
import uy.horacio.recetariocriollo.ui.componentes.EstadoVacio
import uy.horacio.recetariocriollo.ui.componentes.Iconos
import uy.horacio.recetariocriollo.ui.componentes.MARGEN
import uy.horacio.recetariocriollo.ui.componentes.Rotulo
import uy.horacio.recetariocriollo.ui.componentes.TextoTenue
import uy.horacio.recetariocriollo.ui.componentes.fileteAbajo
import uy.horacio.recetariocriollo.ui.ingredientes.DialogoIngrediente
import uy.horacio.recetariocriollo.ui.textoId

/** Los ingredientes del catálogo, agrupados por categoría y editables. */
@Composable
fun CatalogoPantalla(
    vistaModelo: CatalogoViewModel,
    alVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val estado by vistaModelo.estado.collectAsStateWithLifecycle()
    var filtro by rememberSaveable { mutableStateOf("") }
    var editando by remember { mutableStateOf<Ingrediente?>(null) }
    val avisos = remember { SnackbarHostState() }
    val alcance = rememberCoroutineScope()
    val recursos = LocalResources.current
    val colores = MaterialTheme.colorScheme

    val grupos = estado.catalogo
        .filter { Texto.contiene(it.nombre, filtro) }
        .groupBy { it.categoria }
        .toSortedMap(compareBy { it.ordinal })

    Scaffold(
        modifier = modifier,
        containerColor = colores.background,
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(avisos) { AvisoRecetario(it) } },
        topBar = {
            BarraSuperior(
                titulo = stringResource(R.string.catalogo_titulo),
                alVolver = alVolver,
                descripcionVolver = stringResource(R.string.accion_volver)
            ) {
                BotonSecundario(
                    texto = stringResource(R.string.catalogo_nuevo),
                    alTocar = { editando = Ingrediente(nombre = filtro.trim(), categoria = CategoriaIngrediente.OTROS) },
                    icono = Iconos.Mas,
                    alto = 36.dp
                )
            }
        }
    ) { relleno ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno)
                .imePadding()
        ) {
            item(key = "filtro") {
                Column(modifier = Modifier.padding(MARGEN)) {
                    CampoTexto(
                        valor = filtro,
                        alCambiar = { filtro = it },
                        marcador = stringResource(R.string.selector_filtro)
                    )
                    TextoTenue(
                        texto = pluralStringResource(R.plurals.catalogo_resumen, estado.catalogo.size, estado.catalogo.size),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
            if (grupos.isEmpty() && !estado.cargando) {
                item(key = "vacio") { EstadoVacio(titulo = stringResource(R.string.selector_sin_resultados)) }
            }
            grupos.forEach { (categoria, lista) ->
                stickyHeader(key = "cabecera_${categoria.name}") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colores.surfaceVariant)
                            .fileteAbajo(colores.outline)
                            .padding(horizontal = MARGEN)
                            .padding(top = 14.dp, bottom = 8.dp)
                    ) {
                        Rotulo(stringResource(categoria.textoId))
                    }
                }
                items(lista, key = { it.id }) { ingrediente ->
                    FilaCatalogo(
                        ingrediente = ingrediente,
                        usos = estado.usos[ingrediente.id] ?: 0,
                        alTocar = { editando = ingrediente }
                    )
                }
            }
        }
    }

    editando?.let { ingrediente ->
        val usos = estado.usos[ingrediente.id] ?: 0
        DialogoIngrediente(
            inicial = ingrediente,
            catalogo = estado.catalogo,
            usos = usos,
            alGuardar = { cambiado ->
                vistaModelo.guardar(cambiado)
                editando = null
            },
            alCancelar = { editando = null },
            alBorrar = if (ingrediente.id == 0L) null else {
                {
                    editando = null
                    vistaModelo.borrar(ingrediente.id) { borrado ->
                        val texto = if (borrado) recursos.getString(R.string.catalogo_borrado, ingrediente.nombre)
                        else recursos.getQuantityString(R.plurals.catalogo_no_se_puede_borrar, usos, usos)
                        alcance.launch { avisos.showSnackbar(texto) }
                    }
                }
            }
        )
    }
}

@Composable
private fun FilaCatalogo(ingrediente: Ingrediente, usos: Int, alTocar: () -> Unit) {
    val colores = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fileteAbajo(colores.outline)
            .clickable(role = Role.Button, onClick = alTocar)
            .padding(horizontal = MARGEN, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(ingrediente.nombre, style = MaterialTheme.typography.bodyLarge)
            if (usos > 0) {
                TextoTenue(pluralStringResource(R.plurals.catalogo_en_recetas, usos, usos))
            }
        }
        TextoTenue(
            ingrediente.densidadGramosPorTaza?.let {
                stringResource(R.string.catalogo_densidad, Fracciones.formatearDecimal(it, 0))
            } ?: stringResource(R.string.catalogo_sin_densidad)
        )
    }
}
