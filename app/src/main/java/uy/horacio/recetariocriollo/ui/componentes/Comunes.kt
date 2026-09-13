package uy.horacio.recetariocriollo.ui.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.layout.layout
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.offset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uy.horacio.recetariocriollo.ui.theme.RecetarioTema

/*
 * Kit de componentes del sistema "Modernist". Todo es recto, con filetes de tinta
 * al 40 %: 1 dp entre filas, 2 dp entre secciones. Las pantallas no deberian armar
 * bordes ni colores a mano; si falta una pieza, va aca.
 */

val MARGEN = 16.dp

/** Filete horizontal: fino entre filas, grueso entre secciones. */
@Composable
fun Filete(grueso: Boolean = false, modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier,
        thickness = if (grueso) 2.dp else 1.dp,
        color = MaterialTheme.colorScheme.outline
    )
}

/** Dibuja un filete arriba del elemento sin sumar un Composable aparte. */
fun Modifier.fileteArriba(color: Color, grosor: Dp = 1.dp): Modifier = drawBehind {
    val alto = grosor.toPx()
    drawRect(color = color, topLeft = Offset.Zero, size = size.copy(height = alto))
}

fun Modifier.fileteAbajo(color: Color, grosor: Dp = 1.dp): Modifier = drawBehind {
    val alto = grosor.toPx()
    drawRect(color = color, topLeft = Offset(0f, size.height - alto), size = size.copy(height = alto))
}

/** Rotulo de seccion: mayusculas chicas, espaciadas, en color acento. */
@Composable
fun Rotulo(
    texto: String,
    modifier: Modifier = Modifier,
    color: Color = RecetarioTema.extra.rotulo
) {
    Text(
        text = texto.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = color,
        modifier = modifier
    )
}

/** Cabecera de seccion con rotulo a la izquierda y accion de texto opcional a la derecha. */
@Composable
fun CabeceraSeccion(
    rotulo: String,
    modifier: Modifier = Modifier,
    accion: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MARGEN)
            .padding(top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Rotulo(rotulo, modifier = Modifier.weight(1f))
        accion?.invoke()
    }
}

/** Texto secundario: la tinta al 60 %. */
@Composable
fun TextoTenue(
    texto: String,
    modifier: Modifier = Modifier,
    estilo: TextStyle = MaterialTheme.typography.bodySmall,
    maxLineas: Int = Int.MAX_VALUE
) {
    Text(
        text = texto,
        style = estilo,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = maxLineas,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}

/** Barra superior de 56 dp con filete grueso abajo. */
@Composable
fun BarraSuperior(
    titulo: String,
    modifier: Modifier = Modifier,
    alVolver: (() -> Unit)? = null,
    descripcionVolver: String? = null,
    acciones: @Composable RowScope.() -> Unit = {}
) {
    val filete = MaterialTheme.colorScheme.outline
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .height(56.dp)
            .fileteAbajo(filete, 2.dp)
            .padding(start = if (alVolver == null) MARGEN else 6.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (alVolver != null) {
            BotonIcono(
                icono = Iconos.Volver,
                descripcion = descripcionVolver,
                alTocar = alVolver
            )
        }
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        acciones()
    }
}

/** Area tactil de 44 dp con un icono de trazo. */
@Composable
fun BotonIcono(
    icono: ImageVector,
    descripcion: String?,
    alTocar: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onBackground,
    tamanioIcono: Dp = 21.dp,
    habilitado: Boolean = true
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clickable(enabled = habilitado, role = Role.Button, onClick = alTocar)
            .semantics { if (descripcion != null) contentDescription = descripcion },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = if (habilitado) color else color.copy(alpha = 0.3f),
            modifier = Modifier.size(tamanioIcono)
        )
    }
}

/** Chip rectangular: contorno fino, o acento lleno cuando esta activo. */
@Composable
fun ChipRecto(
    texto: String,
    activo: Boolean,
    alTocar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colores = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .heightIn(min = 36.dp)
            .background(if (activo) colores.primary else Color.Transparent)
            .border(1.dp, if (activo) colores.primary else colores.outline)
            .clickable(role = Role.Checkbox, onClick = alTocar)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelMedium,
            color = if (activo) colores.onPrimary else colores.onBackground,
            maxLines = 1
        )
    }
}

/** Boton principal: bloque rojo, texto a la izquierda. */
@Composable
fun BotonPrimario(
    texto: String,
    alTocar: () -> Unit,
    modifier: Modifier = Modifier,
    alto: Dp = 52.dp,
    habilitado: Boolean = true,
    icono: ImageVector? = null
) {
    val colores = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .heightIn(min = alto)
            .background(if (habilitado) colores.primary else colores.outlineVariant)
            .clickable(enabled = habilitado, role = Role.Button, onClick = alTocar)
            .padding(horizontal = MARGEN),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val tinta = if (habilitado) colores.onPrimary else colores.onSurfaceVariant
        if (icono != null) Icon(icono, contentDescription = null, tint = tinta, modifier = Modifier.size(18.dp))
        Text(text = texto, style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp), color = tinta)
    }
}

/** Boton secundario: contorno fino de tinta. */
@Composable
fun BotonSecundario(
    texto: String,
    alTocar: () -> Unit,
    modifier: Modifier = Modifier,
    alto: Dp = 44.dp,
    habilitado: Boolean = true,
    icono: ImageVector? = null,
    centrado: Boolean = false
) {
    val colores = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .heightIn(min = alto)
            .border(1.dp, colores.outline)
            .clickable(enabled = habilitado, role = Role.Button, onClick = alTocar)
            .padding(horizontal = if (centrado) 4.dp else 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (centrado) Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
        else Arrangement.spacedBy(6.dp)
    ) {
        val tinta = if (habilitado) colores.onBackground else colores.onSurfaceVariant.copy(alpha = 0.4f)
        if (icono != null) Icon(icono, contentDescription = null, tint = tinta, modifier = Modifier.size(15.dp))
        Text(
            text = texto,
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
            color = tinta,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Casilleros contiguos: a partir del segundo, cada uno se estira 1 dp hacia atras y pisa
 * el filete del anterior, asi entre dos queda una sola linea y el borde final no se corre.
 */
fun Modifier.contiguo(indice: Int, vertical: Boolean = false): Modifier =
    if (indice == 0) this else layout { medible, restricciones ->
        val extra = 1.dp.roundToPx()
        // offset respeta las restricciones infinitas (una fila dentro de un LazyColumn).
        val medido = medible.measure(
            if (vertical) restricciones.offset(vertical = extra) else restricciones.offset(horizontal = extra)
        )
        val ancho = if (vertical) medido.width else medido.width - extra
        val alto = if (vertical) medido.height - extra else medido.height
        layout(ancho, alto) {
            medido.place(if (vertical) 0 else -extra, if (vertical) -extra else 0)
        }
    }

/** Accion de texto en color acento, sin caja. */
@Composable
fun BotonTexto(
    texto: String,
    alTocar: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Text(
        text = texto,
        style = MaterialTheme.typography.labelMedium,
        color = color,
        modifier = modifier
            .clickable(role = Role.Button, onClick = alTocar)
            .padding(vertical = 8.dp)
    )
}

/** Etiqueta chica de dato (categoria, tiempo). */
@Composable
fun Etiqueta(texto: String, acento: Boolean = false, modifier: Modifier = Modifier) {
    val extra = RecetarioTema.extra
    Text(
        text = texto,
        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 11.sp),
        color = if (acento) extra.textoAcentoTenue else MaterialTheme.colorScheme.onBackground,
        modifier = modifier
            .background(if (acento) extra.acentoTenue else MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 9.dp, vertical = 5.dp)
    )
}

/** Barra de progreso plana, sin animacion ni bordes. */
@Composable
fun BarraProgreso(
    fraccion: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    alto: Dp = 4.dp
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(alto)
            .background(RecetarioTema.extra.pista)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraccion.coerceIn(0f, 1f))
                .background(color)
        )
    }
}

/** Casilla cuadrada de 26 dp, como la de la lista de compras del prototipo. */
@Composable
fun Casilla(marcada: Boolean, modifier: Modifier = Modifier) {
    val colores = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .size(26.dp)
            .background(if (marcada) colores.primary else Color.Transparent)
            .border(2.dp, if (marcada) colores.primary else colores.outline),
        contentAlignment = Alignment.Center
    ) {
        if (marcada) {
            Icon(Iconos.Tilde, contentDescription = null, tint = colores.onPrimary, modifier = Modifier.size(14.dp))
        }
    }
}

/** Pantalla o lista sin contenido: dice que pasa y que hacer, alineado a la izquierda. */
@Composable
fun EstadoVacio(
    titulo: String,
    detalle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MARGEN, vertical = 36.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(text = titulo, style = MaterialTheme.typography.titleMedium)
        if (detalle != null) {
            TextoTenue(detalle, estilo = MaterialTheme.typography.bodyLarge)
        }
    }
}

/**
 * Campo de texto del sistema: rotulo arriba, caja hundida con filete fino y
 * cursor en acento. Reemplaza a OutlinedTextField en toda la app.
 */
@Composable
fun CampoTexto(
    valor: String,
    alCambiar: (String) -> Unit,
    modifier: Modifier = Modifier,
    etiqueta: String? = null,
    marcador: String? = null,
    ayuda: String? = null,
    unaLinea: Boolean = true,
    lineasMinimas: Int = 1,
    teclado: KeyboardOptions = KeyboardOptions.Default,
    alto: Dp = 44.dp,
    estiloTexto: TextStyle = MaterialTheme.typography.bodyLarge
) {
    val colores = MaterialTheme.colorScheme
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(5.dp)) {
        if (etiqueta != null) {
            TextoTenue(etiqueta, estilo = MaterialTheme.typography.bodySmall.copy(lineHeight = 12.sp))
        }
        BasicTextField(
            value = valor,
            onValueChange = alCambiar,
            singleLine = unaLinea,
            minLines = if (unaLinea) 1 else lineasMinimas,
            keyboardOptions = teclado,
            textStyle = estiloTexto.copy(color = colores.onBackground),
            cursorBrush = SolidColor(colores.primary),
            modifier = Modifier
                .fillMaxWidth()
                .semantics { if (etiqueta != null) contentDescription = etiqueta },
            decorationBox = { campo ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = alto)
                        .background(colores.surfaceVariant)
                        .border(1.dp, colores.outline)
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                    contentAlignment = if (unaLinea) Alignment.CenterStart else Alignment.TopStart
                ) {
                    if (valor.isEmpty() && marcador != null) {
                        Text(marcador, style = estiloTexto, color = colores.onSurfaceVariant.copy(alpha = 0.5f))
                    }
                    campo()
                }
            }
        )
        if (ayuda != null) TextoTenue(ayuda)
    }
}

/**
 * Desplegable para listas cortas (unidades, categorias, reglas de escalado),
 * con la misma caja que CampoTexto.
 */
@Composable
fun <T> SelectorDesplegable(
    etiqueta: String,
    seleccion: T,
    opciones: List<T>,
    textoDe: @Composable (T) -> String,
    alElegir: (T) -> Unit,
    modifier: Modifier = Modifier,
    detalleDe: (@Composable (T) -> String?)? = null,
    alto: Dp = 44.dp
) {
    var abierto by remember { mutableStateOf(false) }
    val colores = MaterialTheme.colorScheme
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(5.dp)) {
        TextoTenue(etiqueta, estilo = MaterialTheme.typography.bodySmall.copy(lineHeight = 12.sp))
        Box {
            val actual = textoDe(seleccion)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = alto)
                    .background(colores.surfaceVariant)
                    .border(1.dp, colores.outline)
                    .clickable(role = Role.DropdownList) { abierto = true }
                    .semantics { contentDescription = "$etiqueta: $actual" }
                    .padding(start = 10.dp, end = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = actual,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Icon(Iconos.Desplegar, contentDescription = null, modifier = Modifier.size(18.dp))
            }
            DropdownMenu(
                expanded = abierto,
                onDismissRequest = { abierto = false },
                containerColor = colores.background,
                shadowElevation = 6.dp,
                border = BorderStroke(1.dp, colores.outline),
                modifier = Modifier.widthIn(min = 180.dp)
            ) {
                opciones.forEach { opcion ->
                    val detalle = detalleDe?.invoke(opcion)
                    val marcada = opcion == seleccion
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = textoDe(opcion),
                                    style = if (marcada) MaterialTheme.typography.titleSmall
                                    else MaterialTheme.typography.bodyLarge,
                                    color = if (marcada) colores.primary else colores.onBackground
                                )
                                if (detalle != null) TextoTenue(detalle)
                            }
                        },
                        onClick = {
                            alElegir(opcion)
                            abierto = false
                        },
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

/** Stepper cuadrado: − valor +. */
@Composable
fun Stepper(
    valor: String,
    alRestar: () -> Unit,
    alSumar: () -> Unit,
    descripcionRestar: String,
    descripcionSumar: String,
    modifier: Modifier = Modifier,
    puedeRestar: Boolean = true
) {
    val filete = MaterialTheme.colorScheme.outline
    Row(modifier = modifier.height(52.dp), verticalAlignment = Alignment.CenterVertically) {
        BotonIcono(
            icono = Iconos.Menos,
            descripcion = descripcionRestar,
            alTocar = alRestar,
            habilitado = puedeRestar,
            tamanioIcono = 22.dp,
            modifier = Modifier
                .size(52.dp)
                .border(1.dp, filete)
        )
        Box(
            modifier = Modifier
                .widthIn(min = 76.dp)
                .fillMaxHeight()
                .fileteArriba(filete)
                .fileteAbajo(filete),
            contentAlignment = Alignment.Center
        ) {
            Text(valor, style = MaterialTheme.typography.headlineMedium.copy(fontSize = 26.sp))
        }
        BotonIcono(
            icono = Iconos.Mas,
            descripcion = descripcionSumar,
            alTocar = alSumar,
            tamanioIcono = 22.dp,
            modifier = Modifier
                .size(52.dp)
                .border(1.dp, filete)
        )
    }
}

/** Aviso flotante: bloque de tinta, texto claro, sin radios. */
@Composable
fun AvisoRecetario(datos: SnackbarData) {
    Snackbar(
        snackbarData = datos,
        shape = MaterialTheme.shapes.small,
        containerColor = MaterialTheme.colorScheme.inverseSurface,
        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
        actionColor = MaterialTheme.colorScheme.inversePrimary,
        modifier = Modifier.padding(12.dp)
    )
}

/** Bloque de seccion con margenes estandar y filete grueso abajo. */
@Composable
fun BloqueSeccion(
    modifier: Modifier = Modifier,
    fondo: Color = Color.Transparent,
    conFilete: Boolean = true,
    contenido: @Composable ColumnScope.() -> Unit
) {
    val filete = MaterialTheme.colorScheme.outline
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(fondo)
            .then(if (conFilete) Modifier.fileteAbajo(filete, 2.dp) else Modifier)
            .padding(horizontal = MARGEN, vertical = 14.dp),
        content = contenido
    )
}

/** Fila de dos elementos que reparten el ancho. */
@Composable
fun FilaPareja(
    izquierda: @Composable () -> Unit,
    derecha: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Column(modifier = Modifier.weight(1f)) { izquierda() }
        Column(modifier = Modifier.weight(1f)) { derecha() }
    }
}

@Composable
fun EspacioAlto(alto: Int = 12) {
    Spacer(modifier = Modifier.height(alto.dp))
}

@Composable
fun EspacioAncho(ancho: Int = 8) {
    Spacer(modifier = Modifier.width(ancho.dp))
}
