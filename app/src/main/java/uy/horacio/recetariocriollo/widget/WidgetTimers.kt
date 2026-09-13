package uy.horacio.recetariocriollo.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.Action
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.semantics.contentDescription
import androidx.glance.semantics.semantics
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.compose.runtime.collectAsState
import uy.horacio.recetariocriollo.MainActivity
import uy.horacio.recetariocriollo.R
import uy.horacio.recetariocriollo.cronometro.ContenidoWidget
import uy.horacio.recetariocriollo.cronometro.Cronometro
import uy.horacio.recetariocriollo.cronometro.EstadoCronometro
import uy.horacio.recetariocriollo.cronometro.FilaWidget
import uy.horacio.recetariocriollo.cronometro.GestorCronometros
import uy.horacio.recetariocriollo.cronometro.WidgetTimers
import uy.horacio.recetariocriollo.ui.navegacion.Atajos
import java.text.DateFormat
import java.util.Date

/**
 * Widget de la pantalla de inicio con los timers andando. Glance no tiene cuenta regresiva
 * en vivo, así que cada fila dice a qué hora termina: es un dato que no envejece.
 * Se redibuja cada vez que [GestorCronometros] guarda un cambio.
 */
class WidgetTimersGlance : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val gestor = GestorCronometros.obtener(context)
        provideContent {
            val cronometros by gestor.cronometros.collectAsState()
            ContenidoWidgetTimers(WidgetTimers.contenido(cronometros, System.currentTimeMillis()))
        }
    }

    companion object {
        suspend fun actualizar(contexto: Context) = WidgetTimersGlance().updateAll(contexto)
    }
}

class ReceptorWidgetTimers : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = WidgetTimersGlance()
}

// Mismos tonos que la app: papel, tinta y acento, invertidos de noche.
private val PAPEL = ColorProvider(day = Color(0xFFF3F2F2), night = Color(0xFF201E1D))
private val TINTA = ColorProvider(day = Color(0xFF201E1D), night = Color(0xFFF3F2F2))
private val TENUE = ColorProvider(day = Color(0xFF6B6765), night = Color(0xFFA9A5A3))
private val ACENTO = ColorProvider(Color(0xFFEC3013))
private val SOBRE_ACENTO = ColorProvider(Color.White)

internal val CLAVE_ID = ActionParameters.Key<Long>("id_cronometro")
internal val CLAVE_SEGUNDOS = ActionParameters.Key<Int>("segundos")

private fun abrirApp(contexto: Context, uri: String): Action =
    actionStartActivity(
        Intent(contexto, MainActivity::class.java)
            .setAction(Intent.ACTION_VIEW)
            .setData(uri.toUri())
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
    )

@Composable
fun ContenidoWidgetTimers(contenido: ContenidoWidget) {
    val contexto = LocalContext.current
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(PAPEL)
            .padding(12.dp)
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth().clickable(abrirApp(contexto, Atajos.URI_TIMERS)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = GlanceModifier.width(4.dp).height(14.dp).background(ACENTO)) {}
            Spacer(GlanceModifier.width(6.dp))
            Text(
                text = contexto.getString(R.string.widget_timers_titulo).uppercase(),
                style = TextStyle(color = TINTA, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            )
        }
        Spacer(GlanceModifier.height(8.dp))
        if (contenido.vacio) {
            Text(
                text = contexto.getString(R.string.widget_timers_vacio),
                style = TextStyle(color = TENUE, fontSize = 13.sp)
            )
            Spacer(GlanceModifier.height(8.dp))
            Row(modifier = GlanceModifier.fillMaxWidth()) {
                WidgetTimers.ATAJOS_SEGUNDOS.forEachIndexed { i, segundos ->
                    if (i > 0) Spacer(GlanceModifier.width(6.dp))
                    BotonWidget(
                        texto = contexto.getString(R.string.timers_atajo_minutos, segundos / 60),
                        accion = actionRunCallback<AccionArrancarTimer>(actionParametersOf(CLAVE_SEGUNDOS to segundos)),
                        relleno = true
                    )
                }
            }
        } else {
            contenido.filas.forEach { fila -> FilaTimer(fila) }
            if (contenido.restantes > 0) {
                Text(
                    text = contexto.getString(R.string.widget_timers_y_mas, contenido.restantes),
                    style = TextStyle(color = TENUE, fontSize = 12.sp),
                    modifier = GlanceModifier.padding(top = 4.dp).clickable(abrirApp(contexto, Atajos.URI_TIMERS))
                )
            }
        }
    }
}

@Composable
private fun FilaTimer(fila: FilaWidget) {
    val contexto = LocalContext.current
    val detalle = when (fila.estado) {
        EstadoCronometro.CORRIENDO -> contexto.getString(
            R.string.widget_timers_listo_a_las,
            DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(fila.finEnMillis ?: 0L))
        )
        EstadoCronometro.PAUSADO -> contexto.getString(
            R.string.widget_timers_pausado,
            Cronometro.formatearSegundos(fila.restanteSegundos)
        )
        EstadoCronometro.TERMINADO -> contexto.getString(R.string.timers_terminado)
    }
    Row(
        modifier = GlanceModifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = GlanceModifier.defaultWeight().clickable(abrirApp(contexto, Atajos.URI_TIMERS))) {
            Text(
                text = fila.etiqueta,
                maxLines = 1,
                style = TextStyle(color = TINTA, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            )
            Text(
                text = detalle,
                maxLines = 1,
                style = TextStyle(
                    color = if (fila.estado == EstadoCronometro.TERMINADO) ACENTO else TENUE,
                    fontSize = 12.sp
                )
            )
        }
        val parametros = actionParametersOf(CLAVE_ID to fila.id)
        if (fila.estado != EstadoCronometro.TERMINADO) {
            BotonWidget(
                texto = contexto.getString(R.string.timers_mas_minuto),
                accion = actionRunCallback<AccionMasUnMinuto>(parametros)
            )
            Spacer(GlanceModifier.width(6.dp))
            BotonWidget(
                texto = contexto.getString(
                    if (fila.estado == EstadoCronometro.CORRIENDO) R.string.timers_pausar else R.string.timers_reanudar
                ),
                accion = actionRunCallback<AccionPausarOSeguir>(parametros),
                relleno = fila.estado == EstadoCronometro.PAUSADO
            )
        } else {
            BotonWidget(
                texto = contexto.getString(R.string.timers_quitar),
                accion = actionRunCallback<AccionQuitarTimer>(parametros)
            )
        }
    }
}

@Composable
private fun BotonWidget(texto: String, accion: Action, relleno: Boolean = false) {
    Box(
        modifier = GlanceModifier
            .background(if (relleno) ACENTO else TINTA)
            .padding(horizontal = 10.dp, vertical = 7.dp)
            .clickable(accion)
            .semantics { contentDescription = texto },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = texto,
            style = TextStyle(color = if (relleno) SOBRE_ACENTO else PAPEL, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        )
    }
}

class AccionMasUnMinuto : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val id = parameters[CLAVE_ID] ?: return
        GestorCronometros.obtener(context).ajustar(id, 60)
    }
}

class AccionPausarOSeguir : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val id = parameters[CLAVE_ID] ?: return
        val gestor = GestorCronometros.obtener(context)
        when (gestor.cronometros.value.firstOrNull { it.id == id }?.estado) {
            EstadoCronometro.CORRIENDO -> gestor.pausar(id)
            EstadoCronometro.PAUSADO -> gestor.reanudar(id)
            else -> Unit
        }
    }
}

class AccionQuitarTimer : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val id = parameters[CLAVE_ID] ?: return
        GestorCronometros.obtener(context).quitar(id)
    }
}

class AccionArrancarTimer : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val segundos = parameters[CLAVE_SEGUNDOS] ?: return
        GestorCronometros.obtener(context).crear(etiqueta = Cronometro.describirDuracion(segundos), segundos = segundos)
    }
}
