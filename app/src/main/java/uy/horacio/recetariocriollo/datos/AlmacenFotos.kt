package uy.horacio.recetariocriollo.datos

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.math.max

/**
 * Las fotos de las recetas se copian al almacenamiento interno de la app.
 * Nada sale del telefono y la receta no queda apuntando a una foto que el usuario
 * despues borra de la galeria.
 */
class AlmacenFotos(private val contexto: Context) {

    private val carpeta: File
        get() = File(contexto.filesDir, "fotos").apply { if (!exists()) mkdirs() }

    /** Copia y reescala la imagen elegida. Devuelve la ruta absoluta o null si fallo. */
    suspend fun guardarDesde(origen: Uri): String? = withContext(Dispatchers.IO) {
        runCatching {
            val bitmap = contexto.contentResolver.openInputStream(origen).use { entrada ->
                BitmapFactory.decodeStream(entrada)
            } ?: return@runCatching null

            val escalado = reducir(bitmap, LADO_MAXIMO)
            val destino = File(carpeta, "receta_${UUID.randomUUID()}.jpg")
            FileOutputStream(destino).use { salida ->
                escalado.compress(Bitmap.CompressFormat.JPEG, CALIDAD, salida)
            }
            if (escalado !== bitmap) escalado.recycle()
            bitmap.recycle()
            destino.absolutePath
        }.getOrNull()
    }

    suspend fun borrar(ruta: String) {
        withContext(Dispatchers.IO) {
            runCatching { File(ruta).takeIf { it.exists() }?.delete() }
        }
    }

    /**
     * Borra en segundo plano, sin atarse a quien lo pide. Sirve para limpiar desde
     * ViewModel.onCleared, cuando su viewModelScope ya esta cancelado.
     */
    fun descartar(rutas: Collection<String>) {
        if (rutas.isEmpty()) return
        val copia = rutas.toList()
        alcanceLimpieza.launch { copia.forEach { borrar(it) } }
    }

    private val alcanceLimpieza = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private fun reducir(bitmap: Bitmap, ladoMaximo: Int): Bitmap {
        val lado = max(bitmap.width, bitmap.height)
        if (lado <= ladoMaximo) return bitmap
        val proporcion = ladoMaximo.toFloat() / lado
        return Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * proporcion).toInt(),
            (bitmap.height * proporcion).toInt(),
            true
        )
    }

    private companion object {
        const val LADO_MAXIMO = 1440
        const val CALIDAD = 85
    }
}
