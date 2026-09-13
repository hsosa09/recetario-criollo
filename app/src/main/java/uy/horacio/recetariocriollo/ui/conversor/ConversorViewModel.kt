package uy.horacio.recetariocriollo.ui.conversor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uy.horacio.recetariocriollo.datos.IngredienteRepositorio
import uy.horacio.recetariocriollo.datos.RecetaRepositorio
import uy.horacio.recetariocriollo.dominio.Conversor
import uy.horacio.recetariocriollo.dominio.Fracciones
import uy.horacio.recetariocriollo.dominio.NivelHorno
import uy.horacio.recetariocriollo.dominio.modelo.Ingrediente
import uy.horacio.recetariocriollo.dominio.modelo.Unidad

data class EstadoConversor(
    val cantidad: String = "1",
    val desde: Unidad = Unidad.TAZA,
    val hasta: Unidad = Unidad.GRAMO,
    val ingrediente: Ingrediente? = null,
    val catalogoConDensidad: List<Ingrediente> = emptyList(),
    /** Ingredientes que usa alguna receta: van primero en «Por ingrediente». */
    val idsEnRecetas: Set<Long> = emptySet(),
    val resultado: Double? = null,
    val faltaDensidad: Boolean = false,
    val celsius: String = "180",
    val fahrenheit: String = "356",
    val nivelHorno: NivelHorno? = NivelHorno.MODERADO,
    val levaduraFresca: String = "30",
    val levaduraSeca: String = "10"
)

class ConversorViewModel(
    ingredientes: IngredienteRepositorio,
    recetas: RecetaRepositorio
) : ViewModel() {

    private val _estado = MutableStateFlow(EstadoConversor())
    val estado: StateFlow<EstadoConversor> = _estado.asStateFlow()

    init {
        viewModelScope.launch {
            ingredientes.observarConDensidad().collect { lista ->
                _estado.update { it.copy(catalogoConDensidad = lista) }
            }
        }
        viewModelScope.launch {
            recetas.observarRecetas().collect { lista ->
                val ids = lista.flatMap { receta -> receta.ingredientes.map { it.ingrediente.id } }.toSet()
                _estado.update { it.copy(idsEnRecetas = ids) }
            }
        }
        recalcular()
    }

    fun cambiarCantidad(valor: String) {
        _estado.update { it.copy(cantidad = valor) }
        recalcular()
    }

    fun cambiarDesde(unidad: Unidad) {
        _estado.update { it.copy(desde = unidad) }
        recalcular()
    }

    fun cambiarHasta(unidad: Unidad) {
        _estado.update { it.copy(hasta = unidad) }
        recalcular()
    }

    fun invertir() {
        _estado.update { it.copy(desde = it.hasta, hasta = it.desde) }
        recalcular()
    }

    fun elegirIngrediente(ingrediente: Ingrediente?) {
        _estado.update { it.copy(ingrediente = ingrediente) }
        recalcular()
    }

    private fun recalcular() {
        _estado.update { actual ->
            val cantidad = Fracciones.parsear(actual.cantidad)
            val densidad = actual.ingrediente?.densidadGramosPorTaza
            val necesita = Conversor.necesitaDensidad(actual.desde, actual.hasta)
            val resultado = cantidad?.let {
                Conversor.convertir(it, actual.desde, actual.hasta, densidad)
            }
            actual.copy(
                resultado = resultado,
                faltaDensidad = necesita && densidad == null
            )
        }
    }

    fun cambiarCelsius(valor: String) {
        val grados = Fracciones.parsear(valor)
        _estado.update { actual ->
            actual.copy(
                celsius = valor,
                fahrenheit = grados?.let {
                    Fracciones.formatearDecimal(Conversor.celsiusAFahrenheit(it), 0)
                } ?: actual.fahrenheit,
                nivelHorno = grados?.let { Conversor.nivelDeHorno(it) }
            )
        }
    }

    fun cambiarFahrenheit(valor: String) {
        val grados = Fracciones.parsear(valor)
        _estado.update { actual ->
            val enCelsius = grados?.let { Conversor.fahrenheitACelsius(it) }
            actual.copy(
                fahrenheit = valor,
                celsius = enCelsius?.let { Fracciones.formatearDecimal(it, 0) } ?: actual.celsius,
                nivelHorno = enCelsius?.let { Conversor.nivelDeHorno(it) }
            )
        }
    }

    /** Al tocar "horno moderado" se completa con la temperatura del medio del rango. */
    fun elegirNivelHorno(nivel: NivelHorno) {
        val celsius = nivel.sugeridaCelsius.toDouble()
        _estado.update { actual ->
            actual.copy(
                celsius = Fracciones.formatearDecimal(celsius, 0),
                fahrenheit = Fracciones.formatearDecimal(Conversor.celsiusAFahrenheit(celsius), 0),
                nivelHorno = nivel
            )
        }
    }

    fun cambiarLevaduraFresca(valor: String) {
        val gramos = Fracciones.parsear(valor)
        _estado.update { actual ->
            actual.copy(
                levaduraFresca = valor,
                levaduraSeca = gramos?.let {
                    Fracciones.formatearDecimal(Conversor.levaduraFrescaASeca(it), 1)
                } ?: actual.levaduraSeca
            )
        }
    }

    fun cambiarLevaduraSeca(valor: String) {
        val gramos = Fracciones.parsear(valor)
        _estado.update { actual ->
            actual.copy(
                levaduraSeca = valor,
                levaduraFresca = gramos?.let {
                    Fracciones.formatearDecimal(Conversor.levaduraSecaAFresca(it), 1)
                } ?: actual.levaduraFresca
            )
        }
    }
}
