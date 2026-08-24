package uy.horacio.recetariocriollo.ui.recetas

import android.net.Uri
import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uy.horacio.recetariocriollo.R
import uy.horacio.recetariocriollo.datos.AlmacenFotos
import uy.horacio.recetariocriollo.datos.IngredienteRepositorio
import uy.horacio.recetariocriollo.datos.RecetaRepositorio
import uy.horacio.recetariocriollo.dominio.Escalador
import uy.horacio.recetariocriollo.dominio.Fracciones
import uy.horacio.recetariocriollo.dominio.PlantillaReceta
import uy.horacio.recetariocriollo.dominio.modelo.CategoriaIngrediente
import uy.horacio.recetariocriollo.dominio.modelo.CategoriaReceta
import uy.horacio.recetariocriollo.dominio.modelo.Ingrediente
import uy.horacio.recetariocriollo.dominio.modelo.IngredienteDeReceta
import uy.horacio.recetariocriollo.dominio.modelo.PasoPreparacion
import uy.horacio.recetariocriollo.dominio.modelo.Receta
import uy.horacio.recetariocriollo.dominio.modelo.ReglaEscalado
import uy.horacio.recetariocriollo.dominio.modelo.Unidad

/** Una linea de ingrediente mientras se edita: los numeros viven como texto. */
data class LineaIngrediente(
    val idLocal: Long,
    val ingrediente: Ingrediente,
    val cantidad: String,
    val unidad: Unidad,
    val regla: ReglaEscalado,
    val aclaracion: String
)

data class LineaPaso(
    val idLocal: Long,
    val texto: String,
    val minutosTimer: String
)

data class EstadoEditorReceta(
    val recetaId: Long = 0L,
    val nombre: String = "",
    val categoria: CategoriaReceta = CategoriaReceta.PLATO_PRINCIPAL,
    val porciones: String = "4",
    val tiempo: String = "",
    val notas: String = "",
    val fotoPath: String? = null,
    val ingredientes: List<LineaIngrediente> = emptyList(),
    val pasos: List<LineaPaso> = emptyList(),
    val catalogo: List<Ingrediente> = emptyList(),
    val esFavorita: Boolean = false,
    val guardando: Boolean = false,
    val guardadaConId: Long? = null,
    @get:StringRes val error: Int? = null
) {
    val esNueva: Boolean get() = recetaId == 0L
}

class EditorRecetaViewModel(
    private val recetas: RecetaRepositorio,
    private val ingredientes: IngredienteRepositorio,
    private val almacenFotos: AlmacenFotos,
    estadoGuardado: SavedStateHandle
) : ViewModel() {

    private val recetaId: Long = estadoGuardado.get<Long>(CLAVE_ID) ?: 0L

    private val _estado = MutableStateFlow(EstadoEditorReceta(recetaId = recetaId))
    val estado: StateFlow<EstadoEditorReceta> = _estado.asStateFlow()

    private var siguienteIdLocal = 1L

    init {
        viewModelScope.launch {
            ingredientes.observarCatalogo().collect { catalogo ->
                _estado.update { it.copy(catalogo = catalogo) }
            }
        }
        if (recetaId != 0L) cargarReceta()
    }

    private fun cargarReceta() {
        viewModelScope.launch {
            val receta = recetas.obtenerReceta(recetaId) ?: return@launch
            _estado.update { actual ->
                actual.copy(
                    nombre = receta.nombre,
                    categoria = receta.categoria,
                    porciones = receta.porcionesBase.toString(),
                    tiempo = receta.tiempoMinutos?.toString().orEmpty(),
                    notas = receta.notas.orEmpty(),
                    fotoPath = receta.fotoPath,
                    esFavorita = receta.esFavorita,
                    ingredientes = receta.ingredientes.map { item ->
                        LineaIngrediente(
                            idLocal = siguienteIdLocal++,
                            ingrediente = item.ingrediente,
                            cantidad = Fracciones.formatearCantidad(item.cantidad, item.unidad),
                            unidad = item.unidad,
                            regla = item.regla,
                            aclaracion = item.aclaracion.orEmpty()
                        )
                    },
                    pasos = receta.pasos.map { paso ->
                        LineaPaso(
                            idLocal = siguienteIdLocal++,
                            texto = paso.texto,
                            minutosTimer = paso.timerSugeridoSegundos
                                ?.let { (it / 60).toString() }
                                .orEmpty()
                        )
                    }
                )
            }
        }
    }

    fun cambiarNombre(valor: String) = _estado.update { it.copy(nombre = valor, error = null) }
    fun cambiarCategoria(valor: CategoriaReceta) = _estado.update { it.copy(categoria = valor) }
    fun cambiarPorciones(valor: String) = _estado.update { it.copy(porciones = valor, error = null) }
    fun cambiarTiempo(valor: String) = _estado.update { it.copy(tiempo = valor) }
    fun cambiarNotas(valor: String) = _estado.update { it.copy(notas = valor) }

    /** Carga el esqueleto de la plantilla sin pisar lo que ya haya escrito. */
    fun aplicarPlantilla(plantilla: PlantillaReceta) {
        _estado.update { actual ->
            actual.copy(
                categoria = plantilla.categoria,
                porciones = if (actual.porciones.isBlank()) plantilla.porcionesSugeridas.toString()
                else actual.porciones,
                tiempo = if (actual.tiempo.isBlank()) plantilla.tiempoSugeridoMinutos.toString()
                else actual.tiempo,
                pasos = actual.pasos + plantilla.pasos.map { texto ->
                    LineaPaso(idLocal = siguienteIdLocal++, texto = texto, minutosTimer = "")
                }
            )
        }
    }

    fun agregarIngrediente(ingrediente: Ingrediente) {
        _estado.update { actual ->
            actual.copy(
                ingredientes = actual.ingredientes + LineaIngrediente(
                    idLocal = siguienteIdLocal++,
                    ingrediente = ingrediente,
                    cantidad = "",
                    unidad = ingrediente.unidadHabitual,
                    regla = Escalador.reglaSugerida(ingrediente),
                    aclaracion = ""
                ),
                error = null
            )
        }
    }

    /** Da de alta el ingrediente en el catalogo y lo suma a la receta de una. */
    fun crearIngredienteYAgregar(
        nombre: String,
        categoria: CategoriaIngrediente,
        densidad: Double?,
        especia: Boolean,
        unidad: Unidad
    ) {
        viewModelScope.launch {
            val creado = ingredientes.crearSiNoExiste(nombre, categoria, densidad, especia, unidad)
            agregarIngrediente(creado)
        }
    }

    fun cambiarCantidad(idLocal: Long, valor: String) = modificarIngrediente(idLocal) {
        it.copy(cantidad = valor)
    }

    fun cambiarUnidad(idLocal: Long, valor: Unidad) = modificarIngrediente(idLocal) {
        it.copy(unidad = valor)
    }

    fun cambiarRegla(idLocal: Long, valor: ReglaEscalado) = modificarIngrediente(idLocal) {
        it.copy(regla = valor)
    }

    fun cambiarAclaracion(idLocal: Long, valor: String) = modificarIngrediente(idLocal) {
        it.copy(aclaracion = valor)
    }

    fun quitarIngrediente(idLocal: Long) = _estado.update { actual ->
        actual.copy(ingredientes = actual.ingredientes.filterNot { it.idLocal == idLocal })
    }

    fun agregarPaso() = _estado.update { actual ->
        actual.copy(
            pasos = actual.pasos + LineaPaso(
                idLocal = siguienteIdLocal++,
                texto = "",
                minutosTimer = ""
            )
        )
    }

    fun cambiarTextoPaso(idLocal: Long, valor: String) = modificarPaso(idLocal) {
        it.copy(texto = valor)
    }

    fun cambiarTimerPaso(idLocal: Long, valor: String) = modificarPaso(idLocal) {
        it.copy(minutosTimer = valor)
    }

    fun quitarPaso(idLocal: Long) = _estado.update { actual ->
        actual.copy(pasos = actual.pasos.filterNot { it.idLocal == idLocal })
    }

    fun moverPaso(idLocal: Long, haciaArriba: Boolean) = _estado.update { actual ->
        val lista = actual.pasos.toMutableList()
        val posicion = lista.indexOfFirst { it.idLocal == idLocal }
        val destino = if (haciaArriba) posicion - 1 else posicion + 1
        if (posicion < 0 || destino !in lista.indices) return@update actual
        lista.add(destino, lista.removeAt(posicion))
        actual.copy(pasos = lista)
    }

    fun elegirFoto(origen: Uri) {
        viewModelScope.launch {
            val ruta = almacenFotos.guardarDesde(origen) ?: return@launch
            _estado.update { it.copy(fotoPath = ruta) }
        }
    }

    fun quitarFoto() = _estado.update { it.copy(fotoPath = null) }

    fun guardar() {
        val actual = _estado.value
        val porciones = actual.porciones.trim().toIntOrNull()
        when {
            actual.nombre.isBlank() -> {
                _estado.update { it.copy(error = R.string.editor_falta_nombre) }
                return
            }
            actual.ingredientes.isEmpty() -> {
                _estado.update { it.copy(error = R.string.editor_falta_ingredientes) }
                return
            }
            porciones == null || porciones < 1 -> {
                _estado.update { it.copy(error = R.string.editor_porciones_invalidas) }
                return
            }
        }

        _estado.update { it.copy(guardando = true, error = null) }
        viewModelScope.launch {
            val receta = Receta(
                id = actual.recetaId,
                nombre = actual.nombre.trim(),
                categoria = actual.categoria,
                porcionesBase = porciones ?: 1,
                tiempoMinutos = actual.tiempo.trim().toIntOrNull(),
                notas = actual.notas.trim().ifBlank { null },
                fotoPath = actual.fotoPath,
                esFavorita = actual.esFavorita,
                ingredientes = actual.ingredientes.mapIndexed { indice, linea ->
                    IngredienteDeReceta(
                        ingrediente = linea.ingrediente,
                        cantidad = Fracciones.parsear(linea.cantidad) ?: 0.0,
                        unidad = linea.unidad,
                        regla = linea.regla,
                        aclaracion = linea.aclaracion.trim().ifBlank { null },
                        orden = indice
                    )
                },
                pasos = actual.pasos
                    .filter { it.texto.isNotBlank() }
                    .mapIndexed { indice, linea ->
                        PasoPreparacion(
                            orden = indice,
                            texto = linea.texto.trim(),
                            timerSugeridoSegundos = linea.minutosTimer.trim().toIntOrNull()
                                ?.takeIf { it > 0 }
                                ?.times(60)
                        )
                    }
            )
            val id = recetas.guardar(receta)
            _estado.update { it.copy(guardando = false, guardadaConId = id) }
        }
    }

    fun limpiarError() = _estado.update { it.copy(error = null) }

    private inline fun modificarIngrediente(idLocal: Long, transformar: (LineaIngrediente) -> LineaIngrediente) {
        _estado.update { actual ->
            actual.copy(
                ingredientes = actual.ingredientes.map {
                    if (it.idLocal == idLocal) transformar(it) else it
                }
            )
        }
    }

    private inline fun modificarPaso(idLocal: Long, transformar: (LineaPaso) -> LineaPaso) {
        _estado.update { actual ->
            actual.copy(
                pasos = actual.pasos.map { if (it.idLocal == idLocal) transformar(it) else it }
            )
        }
    }

    companion object {
        const val CLAVE_ID = "recetaId"
    }
}
