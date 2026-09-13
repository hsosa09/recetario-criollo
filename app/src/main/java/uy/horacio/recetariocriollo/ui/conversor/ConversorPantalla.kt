package uy.horacio.recetariocriollo.ui.conversor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uy.horacio.recetariocriollo.R
import uy.horacio.recetariocriollo.dominio.Conversor
import uy.horacio.recetariocriollo.dominio.Fracciones
import uy.horacio.recetariocriollo.dominio.NivelHorno
import uy.horacio.recetariocriollo.dominio.Texto
import uy.horacio.recetariocriollo.dominio.modelo.Unidad
import uy.horacio.recetariocriollo.ui.componentes.BarraSuperior
import uy.horacio.recetariocriollo.ui.componentes.BotonSecundario
import uy.horacio.recetariocriollo.ui.componentes.CampoTexto
import uy.horacio.recetariocriollo.ui.componentes.ChipRecto
import uy.horacio.recetariocriollo.ui.componentes.FilaPareja
import uy.horacio.recetariocriollo.ui.componentes.MARGEN
import uy.horacio.recetariocriollo.ui.componentes.Rotulo
import uy.horacio.recetariocriollo.ui.componentes.SelectorDesplegable
import uy.horacio.recetariocriollo.ui.componentes.TextoTenue
import uy.horacio.recetariocriollo.ui.componentes.fileteAbajo
import uy.horacio.recetariocriollo.ui.componentes.fileteArriba
import uy.horacio.recetariocriollo.ui.textoId

private val UNIDADES_CONVERTIBLES = Unidad.deVolumen + Unidad.dePeso

private val PESTANIAS = listOf(
    R.string.conversor_tab_medidas,
    R.string.conversor_tab_ingrediente,
    R.string.conversor_tab_horno,
    R.string.conversor_tab_levadura
)

private const val PESTANIA_INGREDIENTE = 1

@Composable
fun ConversorPantalla(
    vistaModelo: ConversorViewModel,
    modifier: Modifier = Modifier
) {
    val estado by vistaModelo.estado.collectAsStateWithLifecycle()
    var pestania by rememberSaveable { mutableIntStateOf(0) }
    val colores = MaterialTheme.colorScheme

    Column(modifier = modifier.fillMaxSize()) {
        BarraSuperior(titulo = stringResource(R.string.conversor_titulo))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = MARGEN, vertical = 14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fileteAbajo(colores.outline, 2.dp)
        ) {
            itemsIndexed(PESTANIAS) { indice, texto ->
                ChipRecto(
                    texto = stringResource(texto),
                    activo = pestania == indice,
                    alTocar = { pestania = indice }
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
        ) {
            when (pestania) {
                0 -> SeccionMedidas(estado, vistaModelo, alIrAIngrediente = { pestania = PESTANIA_INGREDIENTE })
                PESTANIA_INGREDIENTE -> SeccionIngrediente(estado, vistaModelo)
                2 -> SeccionHorno(estado, vistaModelo)
                else -> SeccionLevadura(estado, vistaModelo)
            }
        }
    }
}

/** Resultado grande con rotulo, separado por filete grueso. */
@Composable
private fun ColumnScope.BloqueResultado(rotulo: String, contenido: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fileteArriba(MaterialTheme.colorScheme.outline, 2.dp)
            .padding(top = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Rotulo(rotulo)
        contenido()
    }
}

@Composable
private fun SeccionMedidas(
    estado: EstadoConversor,
    vistaModelo: ConversorViewModel,
    alIrAIngrediente: () -> Unit
) {
    Column(
        modifier = Modifier.padding(MARGEN),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CampoTexto(
            valor = estado.cantidad,
            alCambiar = vistaModelo::cambiarCantidad,
            etiqueta = stringResource(R.string.conversor_cantidad),
            teclado = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            alto = 46.dp,
            estiloTexto = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp)
        )

        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SelectorDesplegable(
                etiqueta = stringResource(R.string.conversor_de),
                seleccion = estado.desde,
                opciones = UNIDADES_CONVERTIBLES,
                textoDe = { it.plural },
                alElegir = vistaModelo::cambiarDesde,
                alto = 46.dp,
                modifier = Modifier.weight(1f)
            )
            BotonSecundario(
                texto = stringResource(R.string.conversor_invertir),
                alTocar = vistaModelo::invertir,
                alto = 46.dp
            )
            SelectorDesplegable(
                etiqueta = stringResource(R.string.conversor_a),
                seleccion = estado.hasta,
                opciones = UNIDADES_CONVERTIBLES,
                textoDe = { it.plural },
                alElegir = vistaModelo::cambiarHasta,
                alto = 46.dp,
                modifier = Modifier.weight(1f)
            )
        }

        BloqueResultado(rotulo = stringResource(R.string.conversor_resultado)) {
            val cruza = Conversor.necesitaDensidad(estado.desde, estado.hasta)
            when {
                estado.faltaDensidad -> {
                    Text(
                        text = stringResource(R.string.conversor_necesita_densidad),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    BotonSecundario(
                        texto = stringResource(R.string.conversor_elegir_ingrediente),
                        alTocar = alIrAIngrediente
                    )
                }

                estado.resultado != null -> Text(
                    text = Fracciones.formatearConversion(estado.resultado, estado.hasta),
                    style = MaterialTheme.typography.displaySmall
                )

                else -> Text("—", style = MaterialTheme.typography.displaySmall)
            }
            val ingrediente = estado.ingrediente
            if (cruza && ingrediente != null) {
                TextoTenue(stringResource(R.string.conversor_cruza_con, ingrediente.nombre))
            }
        }
    }

    Column(modifier = Modifier.padding(start = MARGEN, end = MARGEN, bottom = 20.dp)) {
        Rotulo(
            texto = stringResource(R.string.conversor_equivalencias),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            modifier = Modifier.padding(bottom = 6.dp)
        )
        EQUIVALENCIAS.forEach { (izquierda, derecha) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .fileteAbajo(MaterialTheme.colorScheme.outline)
                    .padding(vertical = 9.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(izquierda, style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp))
                TextoTenue(derecha, estilo = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp))
            }
        }
    }
}

@Composable
private fun SeccionIngrediente(estado: EstadoConversor, vistaModelo: ConversorViewModel) {
    var filtro by rememberSaveable { mutableStateOf("") }
    val elegido = estado.ingrediente
    val visibles = estado.catalogoConDensidad.filter { Texto.contiene(it.nombre, filtro) }
    val (deTusRecetas, resto) = visibles.partition { it.id in estado.idsEnRecetas }

    Column(modifier = Modifier.padding(MARGEN)) {
        val densidad = elegido?.densidadGramosPorTaza
        Text(
            text = if (elegido != null && densidad != null) {
                stringResource(
                    R.string.conversor_densidad_dato,
                    elegido.nombre.lowercase(),
                    Fracciones.formatearDecimal(densidad, 0)
                )
            } else {
                stringResource(R.string.conversor_ingrediente_elegi)
            },
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        TextoTenue(
            texto = stringResource(R.string.conversor_ingrediente_ayuda),
            estilo = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 14.dp)
        )
        CampoTexto(
            valor = filtro,
            alCambiar = { filtro = it },
            marcador = stringResource(R.string.selector_filtro),
            modifier = Modifier.padding(bottom = 6.dp)
        )
        if (visibles.isEmpty()) {
            TextoTenue(
                texto = stringResource(R.string.selector_sin_resultados),
                estilo = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 14.dp)
            )
        }
        listOf(
            R.string.conversor_de_tus_recetas to deTusRecetas,
            R.string.conversor_resto_catalogo to resto
        ).filter { it.second.isNotEmpty() }.forEach { (rotulo, grupo) ->
            Rotulo(
                texto = stringResource(rotulo),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = 14.dp, bottom = 8.dp)
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                grupo.forEach { ingrediente ->
                    ChipRecto(
                        texto = ingrediente.nombre,
                        activo = ingrediente.id == elegido?.id,
                        alTocar = {
                            vistaModelo.elegirIngrediente(if (ingrediente.id == elegido?.id) null else ingrediente)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SeccionHorno(estado: EstadoConversor, vistaModelo: ConversorViewModel) {
    Column(
        modifier = Modifier.padding(MARGEN),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        FilaPareja(
            izquierda = {
                CampoTexto(
                    valor = estado.celsius,
                    alCambiar = vistaModelo::cambiarCelsius,
                    etiqueta = stringResource(R.string.conversor_horno_celsius),
                    teclado = KeyboardOptions(keyboardType = KeyboardType.Number),
                    alto = 46.dp,
                    estiloTexto = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp)
                )
            },
            derecha = {
                CampoTexto(
                    valor = estado.fahrenheit,
                    alCambiar = vistaModelo::cambiarFahrenheit,
                    etiqueta = stringResource(R.string.conversor_horno_fahrenheit),
                    teclado = KeyboardOptions(keyboardType = KeyboardType.Number),
                    alto = 46.dp,
                    estiloTexto = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp)
                )
            }
        )

        BloqueResultado(rotulo = stringResource(R.string.conversor_horno_referencia)) {
            val nivel = estado.nivelHorno
            if (nivel == null) {
                Text(stringResource(R.string.conversor_horno_fuera_escala), style = MaterialTheme.typography.titleMedium)
            } else {
                Text(stringResource(nivel.textoId), style = MaterialTheme.typography.displaySmall.copy(fontSize = 30.sp))
                TextoTenue("${nivel.rangoCelsius} · ${nivel.rangoFahrenheit}")
            }
        }
    }

    Column(modifier = Modifier.padding(start = MARGEN, end = MARGEN, bottom = 20.dp)) {
        Rotulo(
            texto = stringResource(R.string.conversor_horno_tabla),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            modifier = Modifier.padding(bottom = 6.dp)
        )
        NivelHorno.entries.forEach { nivel ->
            val elegido = estado.nivelHorno == nivel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .fileteArriba(MaterialTheme.colorScheme.outline)
                    .clickable(role = Role.Button) { vistaModelo.elegirNivelHorno(nivel) }
                    .padding(vertical = 11.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(nivel.textoId),
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                    color = if (elegido) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
                Text(nivel.rangoCelsius, style = MaterialTheme.typography.bodyMedium)
                TextoTenue(nivel.rangoFahrenheit, estilo = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun SeccionLevadura(estado: EstadoConversor, vistaModelo: ConversorViewModel) {
    Column(
        modifier = Modifier.padding(MARGEN),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        FilaPareja(
            izquierda = {
                CampoTexto(
                    valor = estado.levaduraFresca,
                    alCambiar = vistaModelo::cambiarLevaduraFresca,
                    etiqueta = stringResource(R.string.conversor_levadura_fresca),
                    teclado = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    alto = 46.dp,
                    estiloTexto = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp)
                )
            },
            derecha = {
                CampoTexto(
                    valor = estado.levaduraSeca,
                    alCambiar = vistaModelo::cambiarLevaduraSeca,
                    etiqueta = stringResource(R.string.conversor_levadura_seca),
                    teclado = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    alto = 46.dp,
                    estiloTexto = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp)
                )
            }
        )
        BloqueResultado(rotulo = stringResource(R.string.conversor_levadura_seca_rotulo)) {
            Text(
                text = stringResource(R.string.conversor_gramos, estado.levaduraSeca.ifBlank { "0" }),
                style = MaterialTheme.typography.displaySmall
            )
            TextoTenue(
                texto = stringResource(R.string.conversor_levadura_nota),
                estilo = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/** Las equivalencias que uno termina buscando siempre. */
private val EQUIVALENCIAS: List<Pair<String, String>> = listOf(
    "1 taza" to "240 ml · 16 cucharadas",
    "1 cucharada" to "3 cucharaditas · 15 ml",
    "1 cucharadita" to "5 ml",
    "1 kg" to "2,2 lb",
    "1 lb" to "454 g",
    "1 oz" to "28 g"
)
