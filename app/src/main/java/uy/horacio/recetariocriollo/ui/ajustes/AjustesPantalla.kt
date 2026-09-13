package uy.horacio.recetariocriollo.ui.ajustes

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uy.horacio.recetariocriollo.BuildConfig
import uy.horacio.recetariocriollo.R
import uy.horacio.recetariocriollo.cronometro.Notificaciones
import uy.horacio.recetariocriollo.dominio.modelo.PreferenciaUnidades
import uy.horacio.recetariocriollo.dominio.modelo.Tema
import uy.horacio.recetariocriollo.ui.componentes.BarraSuperior
import uy.horacio.recetariocriollo.ui.componentes.BloqueSeccion
import uy.horacio.recetariocriollo.ui.componentes.BotonSecundario
import uy.horacio.recetariocriollo.ui.componentes.Casilla
import uy.horacio.recetariocriollo.ui.componentes.ChipRecto
import uy.horacio.recetariocriollo.ui.componentes.Rotulo
import uy.horacio.recetariocriollo.ui.componentes.TextoTenue

@Composable
fun AjustesPantalla(
    vistaModelo: AjustesViewModel,
    alVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ajustes by vistaModelo.ajustes.collectAsStateWithLifecycle()
    val contexto = LocalContext.current
    var notificaciones by remember { mutableStateOf(Notificaciones.hayPermiso(contexto)) }
    var alarmasExactas by remember { mutableStateOf(vistaModelo.alarmasExactasPermitidas()) }

    // Los permisos se cambian en los ajustes del sistema: se revisan al volver.
    LifecycleResumeEffect(Unit) {
        notificaciones = Notificaciones.hayPermiso(contexto)
        alarmasExactas = vistaModelo.alarmasExactasPermitidas()
        onPauseOrDispose { }
    }

    Column(modifier = modifier.fillMaxSize()) {
        BarraSuperior(
            titulo = stringResource(R.string.ajustes_titulo),
            alVolver = alVolver,
            descripcionVolver = stringResource(R.string.accion_volver)
        )
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Seccion(stringResource(R.string.ajustes_tema)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        Tema.SISTEMA to R.string.ajustes_tema_sistema,
                        Tema.CLARO to R.string.ajustes_tema_claro,
                        Tema.OSCURO to R.string.ajustes_tema_oscuro
                    ).forEach { (tema, texto) ->
                        ChipRecto(
                            texto = stringResource(texto),
                            activo = ajustes.tema == tema,
                            alTocar = { vistaModelo.cambiarTema(tema) }
                        )
                    }
                }
            }

            Seccion(stringResource(R.string.ajustes_cocina)) {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(role = Role.Checkbox) { vistaModelo.cambiarModoCocina(!ajustes.modoCocinaPorDefecto) }
                ) {
                    Casilla(marcada = ajustes.modoCocinaPorDefecto)
                    Column {
                        Text(stringResource(R.string.ajustes_modo_cocina), style = MaterialTheme.typography.titleSmall)
                        TextoTenue(stringResource(R.string.ajustes_modo_cocina_detalle))
                    }
                }
            }

            Seccion(stringResource(R.string.ajustes_unidades)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        PreferenciaUnidades.METRICAS to R.string.ajustes_unidades_metricas,
                        PreferenciaUnidades.DE_COCINA to R.string.ajustes_unidades_cocina
                    ).forEach { (unidades, texto) ->
                        ChipRecto(
                            texto = stringResource(texto),
                            activo = ajustes.unidades == unidades,
                            alTocar = { vistaModelo.cambiarUnidades(unidades) }
                        )
                    }
                }
                TextoTenue(stringResource(R.string.ajustes_unidades_detalle), modifier = Modifier.padding(top = 8.dp))
            }

            Seccion(stringResource(R.string.ajustes_avisos)) {
                EstadoPermiso(
                    titulo = stringResource(R.string.ajustes_notificaciones),
                    concedido = notificaciones,
                    alAbrir = {
                        contexto.startActivity(
                            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                .putExtra(Settings.EXTRA_APP_PACKAGE, contexto.packageName)
                        )
                    }
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    EstadoPermiso(
                        titulo = stringResource(R.string.ajustes_alarmas_exactas),
                        concedido = alarmasExactas,
                        alAbrir = {
                            contexto.startActivity(
                                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, "package:${contexto.packageName}".toUri())
                            )
                        }
                    )
                }
            }

            Seccion(stringResource(R.string.ajustes_acerca), conFilete = false) {
                Text(
                    stringResource(R.string.ajustes_version, BuildConfig.VERSION_NAME),
                    style = MaterialTheme.typography.titleSmall
                )
                TextoTenue(stringResource(R.string.ajustes_acerca_detalle), modifier = Modifier.padding(top = 4.dp))
                TextoTenue(stringResource(R.string.ajustes_licencia_fuente), modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}

@Composable
private fun Seccion(rotulo: String, conFilete: Boolean = true, contenido: @Composable ColumnScope.() -> Unit) {
    BloqueSeccion(conFilete = conFilete) {
        Rotulo(rotulo, modifier = Modifier.padding(bottom = 10.dp))
        contenido()
    }
}

@Composable
private fun EstadoPermiso(titulo: String, concedido: Boolean, alAbrir: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(titulo, style = MaterialTheme.typography.titleSmall)
            TextoTenue(
                stringResource(if (concedido) R.string.ajustes_permiso_si else R.string.ajustes_permiso_no),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        BotonSecundario(texto = stringResource(R.string.ajustes_abrir_sistema), alTocar = alAbrir, alto = 36.dp)
    }
}
