package uy.horacio.recetariocriollo.ui.cronometro

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow
import uy.horacio.recetariocriollo.cronometro.Cronometro
import uy.horacio.recetariocriollo.cronometro.GestorCronometros

class CronometrosViewModel(private val gestor: GestorCronometros) : ViewModel() {

    val cronometros: StateFlow<List<Cronometro>> = gestor.cronometros
    val ahora: StateFlow<Long> = gestor.ahora

    fun crear(etiqueta: String, segundos: Int) {
        if (segundos <= 0) return
        gestor.crear(etiqueta, segundos)
    }

    fun pausar(id: Long) = gestor.pausar(id)
    fun reanudar(id: Long) = gestor.reanudar(id)
    fun reiniciar(id: Long) = gestor.reiniciar(id)
    fun quitar(id: Long) = gestor.quitar(id)
    fun quitarTerminados() = gestor.quitarTerminados()
    fun ajustar(id: Long, segundos: Int) = gestor.ajustar(id, segundos)

    fun alarmasExactasPermitidas(): Boolean = gestor.alarmasExactasPermitidas()

    /** Al volver de Ajustes con el permiso concedido, los timers en marcha pasan a exactos. */
    fun alVolverALaPantalla() {
        if (gestor.alarmasExactasPermitidas()) gestor.reprogramarCorriendo()
    }
}
