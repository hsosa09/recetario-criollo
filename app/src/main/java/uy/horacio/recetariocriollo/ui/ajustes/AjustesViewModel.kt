package uy.horacio.recetariocriollo.ui.ajustes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uy.horacio.recetariocriollo.cronometro.GestorCronometros
import uy.horacio.recetariocriollo.datos.AjustesRepositorio
import uy.horacio.recetariocriollo.dominio.modelo.Ajustes
import uy.horacio.recetariocriollo.dominio.modelo.PreferenciaUnidades
import uy.horacio.recetariocriollo.dominio.modelo.Tema

class AjustesViewModel(
    private val repositorio: AjustesRepositorio,
    private val cronometros: GestorCronometros
) : ViewModel() {

    val ajustes: StateFlow<Ajustes> =
        repositorio.ajustes.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Ajustes())

    fun cambiarTema(tema: Tema) {
        viewModelScope.launch { repositorio.cambiarTema(tema) }
    }

    fun cambiarModoCocina(activo: Boolean) {
        viewModelScope.launch { repositorio.cambiarModoCocinaPorDefecto(activo) }
    }

    fun cambiarUnidades(unidades: PreferenciaUnidades) {
        viewModelScope.launch { repositorio.cambiarUnidades(unidades) }
    }

    fun alarmasExactasPermitidas(): Boolean = cronometros.alarmasExactasPermitidas()
}
