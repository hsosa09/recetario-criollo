package uy.horacio.recetariocriollo.datos

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.os.SystemClock
import uy.horacio.recetariocriollo.dominio.NotaDeVoz
import java.io.File
import java.util.UUID

/**
 * Graba notas de voz cortas en files/notas (AAC en MP4). El audio no sale del teléfono.
 * Una instancia por grabación en curso; hay que llamar a [detener] o [cancelar].
 */
class GrabadorNotas(private val contexto: Context) {

    private var grabador: MediaRecorder? = null
    private var archivo: File? = null
    private var inicio = 0L

    val grabando: Boolean get() = grabador != null

    fun milisegundos(): Long = if (grabando) SystemClock.elapsedRealtime() - inicio else 0L

    /** Empieza a grabar. [alLlegarAlMaximo] avisa cuando se cumple el minuto. Devuelve false si no se pudo. */
    fun empezar(alLlegarAlMaximo: () -> Unit): Boolean {
        cancelar()
        val destino = File(File(contexto.filesDir, CARPETA).apply { mkdirs() }, "nota_${UUID.randomUUID()}.m4a")
        val nuevo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(contexto) else @Suppress("DEPRECATION") MediaRecorder()
        return try {
            nuevo.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioChannels(1)
                setAudioSamplingRate(44_100)
                setAudioEncodingBitRate(64_000)
                setMaxDuration(NotaDeVoz.DURACION_MAXIMA_MS)
                setOnInfoListener { _, que, _ ->
                    if (que == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) alLlegarAlMaximo()
                }
                setOutputFile(destino.absolutePath)
                prepare()
                start()
            }
            grabador = nuevo
            archivo = destino
            inicio = SystemClock.elapsedRealtime()
            true
        } catch (_: Exception) {
            nuevo.release()
            destino.delete()
            false
        }
    }

    /** Termina y devuelve la ruta, o null si fue muy corta o falló (el archivo se borra). */
    fun detener(): String? {
        val actual = grabador ?: return null
        val destino = archivo
        val duracion = milisegundos()
        val bien = runCatching { actual.stop() }.isSuccess
        actual.release()
        grabador = null
        archivo = null
        if (!bien || destino == null || !NotaDeVoz.esValida(duracion)) {
            destino?.delete()
            return null
        }
        return destino.absolutePath
    }

    fun cancelar() {
        grabador?.let { runCatching { it.stop() }; it.release() }
        grabador = null
        archivo?.delete()
        archivo = null
    }

    private companion object {
        const val CARPETA = "notas"
    }
}
