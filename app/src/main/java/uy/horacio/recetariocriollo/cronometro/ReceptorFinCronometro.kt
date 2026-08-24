package uy.horacio.recetariocriollo.cronometro

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Lo dispara la alarma del sistema cuando se cumple el tiempo de un cronometro. */
class ReceptorFinCronometro : BroadcastReceiver() {

    override fun onReceive(contexto: Context, intencion: Intent) {
        val id = intencion.getLongExtra(EXTRA_ID, -1L)
        val etiqueta = intencion.getStringExtra(EXTRA_ETIQUETA).orEmpty()
        if (id <= 0) return
        Notificaciones.avisarFin(contexto, id, etiqueta)
        // Si el proceso sigue vivo, la pantalla de cronometros tiene que verlo terminado.
        GestorCronometros.obtener(contexto).marcarTerminado(id)
    }

    companion object {
        const val EXTRA_ID = "id_cronometro"
        const val EXTRA_ETIQUETA = "etiqueta_cronometro"
    }
}
