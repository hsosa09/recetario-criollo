package uy.horacio.recetariocriollo.ui.navegacion

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.net.toUri
import uy.horacio.recetariocriollo.MainActivity
import uy.horacio.recetariocriollo.R

/**
 * Accesos directos al mantener apretado el ícono. Van por código y no en un XML estático
 * porque el XML necesita el paquete escrito a mano, y el debug (`.debug`) tiene otro.
 */
object AccesosDirectos {

    private const val SEGUNDOS_TIMER = 600

    fun publicar(contexto: Context) {
        val accesos = listOf(
            acceso(contexto, "timer_10", R.string.atajo_timer_corto, R.string.atajo_timer_largo, R.drawable.ic_atajo_timer, Atajos.uriNuevoTimer(SEGUNDOS_TIMER)),
            acceso(contexto, "nueva_receta", R.string.atajo_nueva_receta_corto, R.string.atajo_nueva_receta_largo, R.drawable.ic_atajo_receta, Atajos.URI_NUEVA_RECETA),
            acceso(contexto, "con_lo_que_tengo", R.string.atajo_tengo_corto, R.string.atajo_tengo_largo, R.drawable.ic_atajo_tengo, Atajos.URI_CON_LO_QUE_TENGO)
        )
        runCatching { ShortcutManagerCompat.setDynamicShortcuts(contexto, accesos) }
    }

    private fun acceso(contexto: Context, id: String, corto: Int, largo: Int, icono: Int, uri: String) =
        ShortcutInfoCompat.Builder(contexto, id)
            .setShortLabel(contexto.getString(corto))
            .setLongLabel(contexto.getString(largo))
            .setIcon(IconCompat.createWithResource(contexto, icono))
            .setIntent(
                Intent(contexto, MainActivity::class.java)
                    .setAction(Intent.ACTION_VIEW)
                    .setData(uri.toUri())
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            )
            .build()
}
