package uy.horacio.recetariocriollo.datos

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import uy.horacio.recetariocriollo.dominio.modelo.Ajustes
import uy.horacio.recetariocriollo.dominio.modelo.PreferenciaUnidades
import uy.horacio.recetariocriollo.dominio.modelo.Tema

/**
 * Ajustes del usuario en DataStore Preferences. Recibe el DataStore para poder
 * testearlo en la JVM con un archivo temporal.
 */
class AjustesRepositorio(private val almacen: DataStore<Preferences>) {

    val ajustes: Flow<Ajustes> = almacen.data.map { preferencias ->
        Ajustes(
            tema = enumONulo<Tema>(preferencias[TEMA]) ?: Tema.SISTEMA,
            modoCocinaPorDefecto = preferencias[MODO_COCINA] ?: false,
            unidades = enumONulo<PreferenciaUnidades>(preferencias[UNIDADES]) ?: PreferenciaUnidades.METRICAS
        )
    }

    suspend fun cambiarTema(tema: Tema) = almacen.edit { it[TEMA] = tema.name }

    suspend fun cambiarModoCocinaPorDefecto(activo: Boolean) = almacen.edit { it[MODO_COCINA] = activo }

    suspend fun cambiarUnidades(unidades: PreferenciaUnidades) = almacen.edit { it[UNIDADES] = unidades.name }

    private companion object {
        val TEMA = stringPreferencesKey("tema")
        val MODO_COCINA = booleanPreferencesKey("modo_cocina_por_defecto")
        val UNIDADES = stringPreferencesKey("unidades")

        // Por nombre, y un valor desconocido (de una versión futura) vuelve al por defecto.
        inline fun <reified T : Enum<T>> enumONulo(nombre: String?): T? =
            nombre?.let { valor -> enumValues<T>().firstOrNull { it.name == valor } }
    }
}
