package uy.horacio.recetariocriollo.dominio.modelo

enum class Tema { SISTEMA, CLARO, OSCURO }

/** Qué unidad se propone al cargar un ingrediente en el editor. */
enum class PreferenciaUnidades { METRICAS, DE_COCINA }

data class Ajustes(
    val tema: Tema = Tema.SISTEMA,
    /** El detalle de receta abre con letra grande y la pantalla encendida. */
    val modoCocinaPorDefecto: Boolean = false,
    val unidades: PreferenciaUnidades = PreferenciaUnidades.METRICAS
)
