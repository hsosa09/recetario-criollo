package uy.horacio.recetariocriollo.ui.componentes

import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import uy.horacio.recetariocriollo.R
import uy.horacio.recetariocriollo.dominio.NotaDeVoz
import java.io.File

/** Botón recto «▶ 0:07» que reproduce una nota de voz y muestra el avance. */
@Composable
fun ReproductorNota(ruta: String, modifier: Modifier = Modifier) {
    val colores = MaterialTheme.colorScheme
    var reproductor by remember(ruta) { mutableStateOf<MediaPlayer?>(null) }
    var sonando by remember(ruta) { mutableStateOf(false) }
    var posicion by remember(ruta) { mutableIntStateOf(0) }
    val duracion = remember(ruta) {
        val lector = MediaMetadataRetriever()
        try {
            lector.setDataSource(ruta)
            lector.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toIntOrNull() ?: 0
        } catch (_: Exception) {
            0
        } finally {
            lector.release()
        }
    }

    DisposableEffect(ruta) {
        onDispose {
            reproductor?.release()
            reproductor = null
        }
    }
    LaunchedEffect(sonando) {
        while (sonando) {
            posicion = reproductor?.currentPosition ?: 0
            delay(200)
        }
    }

    val existe = remember(ruta) { File(ruta).exists() }
    if (!existe) return
    val etiqueta = stringResource(if (sonando) R.string.nota_pausar else R.string.nota_escuchar)
    Row(
        modifier = modifier
            .heightIn(min = 36.dp)
            .border(1.dp, colores.outline)
            .clickable(role = Role.Button) {
                val actual = reproductor ?: MediaPlayer().apply {
                    setDataSource(ruta)
                    prepare()
                    setOnCompletionListener {
                        sonando = false
                        posicion = 0
                    }
                }.also { reproductor = it }
                if (sonando) {
                    actual.pause()
                    sonando = false
                } else {
                    actual.start()
                    sonando = true
                }
            }
            .semantics { contentDescription = "$etiqueta, ${NotaDeVoz.formatear(duracion.toLong())}" }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(if (sonando) "❚❚" else "▶", style = MaterialTheme.typography.labelMedium, color = colores.primary, modifier = Modifier.width(14.dp))
        Text(
            text = if (sonando || posicion > 0) "${NotaDeVoz.formatear(posicion.toLong())} / ${NotaDeVoz.formatear(duracion.toLong())}"
            else stringResource(R.string.nota_de_voz, NotaDeVoz.formatear(duracion.toLong())),
            style = MaterialTheme.typography.labelMedium
        )
    }
}
