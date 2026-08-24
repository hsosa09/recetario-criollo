package uy.horacio.recetariocriollo.cronometro

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import uy.horacio.recetariocriollo.MainActivity
import uy.horacio.recetariocriollo.R

/** Aviso local cuando se termina un cronometro. Sin internet, sin nada raro. */
object Notificaciones {

    const val CANAL_CRONOMETROS = "cronometros_cocina"

    fun crearCanal(contexto: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val gestor = contexto.getSystemService(NotificationManager::class.java) ?: return
        if (gestor.getNotificationChannel(CANAL_CRONOMETROS) != null) return
        val canal = NotificationChannel(
            CANAL_CRONOMETROS,
            contexto.getString(R.string.canal_cronometros),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = contexto.getString(R.string.canal_cronometros_descripcion)
            enableVibration(true)
            setSound(
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
        }
        gestor.createNotificationChannel(canal)
    }

    fun avisarFin(contexto: Context, idCronometro: Long, etiqueta: String) {
        crearCanal(contexto)
        if (!hayPermiso(contexto)) return

        val intencionAbrir = Intent(contexto, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendiente = PendingIntent.getActivity(
            contexto,
            idCronometro.toInt(),
            intencionAbrir,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val aviso = NotificationCompat.Builder(contexto, CANAL_CRONOMETROS)
            .setSmallIcon(R.drawable.ic_cronometro)
            .setContentTitle(contexto.getString(R.string.cronometro_listo, etiqueta))
            .setContentText(contexto.getString(R.string.cronometro_listo_detalle))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendiente)
            .setVibrate(longArrayOf(0, 400, 200, 400, 200, 600))
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
            .build()

        runCatching {
            NotificationManagerCompat.from(contexto).notify(idCronometro.toInt(), aviso)
        }
    }

    fun hayPermiso(contexto: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            contexto,
            android.Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }
}
