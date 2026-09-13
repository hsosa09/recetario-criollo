package uy.horacio.recetariocriollo.cronometro

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Recupera las alarmas de los cronometros cuando el sistema las perdio o cambio las reglas:
 * - al terminar de arrancar el telefono (Android borra todas las alarmas al reiniciar),
 * - al actualizar la app,
 * - al conceder o quitar el permiso de alarmas exactas desde Ajustes.
 */
class ReceptorArranque : BroadcastReceiver() {

    override fun onReceive(contexto: Context, intencion: Intent) {
        if (intencion.action !in ACCIONES) return
        GestorCronometros.obtener(contexto).recuperarAlarmas()
    }

    private companion object {
        val ACCIONES = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            // Literal y no AlarmManager.ACTION_...: la constante es de API 31 y minSdk es 26.
            "android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED"
        )
    }
}
