package uy.horacio.recetariocriollo.ui.navegacion

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import uy.horacio.recetariocriollo.R
import uy.horacio.recetariocriollo.cronometro.EstadoCronometro
import uy.horacio.recetariocriollo.ui.Fabricas
import uy.horacio.recetariocriollo.ui.busqueda.BusquedaPantalla
import uy.horacio.recetariocriollo.ui.ajustes.AjustesPantalla
import uy.horacio.recetariocriollo.ui.ajustes.AjustesViewModel
import uy.horacio.recetariocriollo.ui.busqueda.BusquedaViewModel
import uy.horacio.recetariocriollo.ui.estadisticas.EstadisticasPantalla
import uy.horacio.recetariocriollo.ui.estadisticas.EstadisticasViewModel
import uy.horacio.recetariocriollo.ui.cajon.CajonRecetario
import uy.horacio.recetariocriollo.ui.cajon.CajonViewModel
import uy.horacio.recetariocriollo.ui.componentes.LocalAbrirCajon
import kotlinx.coroutines.launch
import uy.horacio.recetariocriollo.ui.catalogo.CatalogoPantalla
import uy.horacio.recetariocriollo.ui.catalogo.CatalogoViewModel
import uy.horacio.recetariocriollo.ui.cocina.CocinaPantalla
import uy.horacio.recetariocriollo.ui.cocina.CocinaViewModel
import uy.horacio.recetariocriollo.ui.theme.PapelNoche
import uy.horacio.recetariocriollo.ui.componentes.Iconos
import uy.horacio.recetariocriollo.ui.componentes.fileteArriba
import uy.horacio.recetariocriollo.ui.conversor.ConversorPantalla
import uy.horacio.recetariocriollo.ui.conversor.ConversorViewModel
import uy.horacio.recetariocriollo.ui.cronometro.CronometrosPantalla
import uy.horacio.recetariocriollo.ui.cronometro.CronometrosViewModel
import uy.horacio.recetariocriollo.ui.historial.HistorialPantalla
import uy.horacio.recetariocriollo.ui.historial.HistorialViewModel
import uy.horacio.recetariocriollo.ui.recetas.DetalleRecetaPantalla
import uy.horacio.recetariocriollo.ui.recetas.DetalleRecetaViewModel
import uy.horacio.recetariocriollo.ui.recetas.EditorRecetaPantalla
import uy.horacio.recetariocriollo.ui.recetas.EditorRecetaViewModel
import uy.horacio.recetariocriollo.ui.recetas.ListaRecetasPantalla
import uy.horacio.recetariocriollo.ui.recetas.ListaRecetasViewModel
import kotlin.reflect.KClass

private data class Solapa(
    val ruta: Any,
    val clase: KClass<*>,
    val icono: ImageVector,
    val textoId: Int
)

private val SOLAPAS = listOf(
    Solapa(RutaRecetas, RutaRecetas::class, Iconos.Recetas, R.string.nav_recetas),
    Solapa(RutaBusqueda, RutaBusqueda::class, Iconos.Tengo, R.string.nav_buscar),
    Solapa(RutaConversor, RutaConversor::class, Iconos.Conversor, R.string.nav_conversor),
    Solapa(RutaCronometros, RutaCronometros::class, Iconos.Timer, R.string.nav_timers)
)

@Composable
fun NavegacionRecetario(
    controlador: NavHostController = rememberNavController()
) {
    val entradaActual by controlador.currentBackStackEntryAsState()
    val destino = entradaActual?.destination
    val enSolapa = SOLAPAS.any { solapa -> destino?.hasRoute(solapa.clase) == true }
    // El paso a paso es oscuro de punta a punta, incluida la franja de la barra de estado.
    val cocinando = destino?.hasRoute(RutaCocina::class) == true

    // El contador de timers andando se ve desde cualquier pantalla.
    val cronometrosVm: CronometrosViewModel = viewModel(factory = Fabricas.Factory)
    val cronometros by cronometrosVm.cronometros.collectAsStateWithLifecycle()
    val activos = cronometros.count { it.estado != EstadoCronometro.TERMINADO }
    val terminados = cronometros.count { it.estado == EstadoCronometro.TERMINADO }

    val cajon = rememberDrawerState(DrawerValue.Closed)
    val alcance = rememberCoroutineScope()
    val cajonVm: CajonViewModel = viewModel(factory = Fabricas.Factory)
    val resumen by cajonVm.resumen.collectAsStateWithLifecycle()
    BackHandler(enabled = cajon.isOpen) { alcance.launch { cajon.close() } }

    ModalNavigationDrawer(
        drawerState = cajon,
        gesturesEnabled = enSolapa || cajon.isOpen,
        scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f),
        drawerContent = {
            CajonRecetario(
                resumen = resumen,
                alIr = { ruta ->
                    alcance.launch { cajon.close() }
                    controlador.navigate(ruta) { launchSingleTop = true }
                }
            )
        }
    ) {
    CompositionLocalProvider(LocalAbrirCajon provides if (enSolapa) ({ alcance.launch { cajon.open() } }) else null) {
    Scaffold(
        containerColor = if (cocinando) PapelNoche else MaterialTheme.colorScheme.background,
        bottomBar = {
            if (enSolapa) {
                BarraSolapas(
                    alElegir = { solapa ->
                        controlador.navigate(solapa.ruta) {
                            popUpTo(RutaRecetas) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    esActual = { solapa -> destino?.hasRoute(solapa.clase) == true },
                    cantidadTimers = activos + terminados
                )
            }
        }
    ) { relleno ->
        NavHost(
            navController = controlador,
            startDestination = RutaRecetas,
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno)
                .consumeWindowInsets(relleno)
        ) {
            composable<RutaRecetas> {
                val vistaModelo: ListaRecetasViewModel = viewModel(factory = Fabricas.Factory)
                ListaRecetasPantalla(
                    vistaModelo = vistaModelo,
                    alAbrirReceta = { id -> controlador.navigate(RutaDetalleReceta(id)) },
                    alCrearReceta = { controlador.navigate(RutaEditorReceta()) }
                )
            }

            composable<RutaBusqueda> {
                val vistaModelo: BusquedaViewModel = viewModel(factory = Fabricas.Factory)
                BusquedaPantalla(
                    vistaModelo = vistaModelo,
                    alAbrirReceta = { id -> controlador.navigate(RutaDetalleReceta(id)) }
                )
            }

            composable<RutaConversor> {
                val vistaModelo: ConversorViewModel = viewModel(factory = Fabricas.Factory)
                ConversorPantalla(vistaModelo = vistaModelo)
            }

            composable<RutaCronometros> {
                CronometrosPantalla(vistaModelo = cronometrosVm)
            }

            composable<RutaDetalleReceta> {
                val vistaModelo: DetalleRecetaViewModel = viewModel(factory = Fabricas.Factory)
                DetalleRecetaPantalla(
                    vistaModelo = vistaModelo,
                    alVolver = { controlador.popBackStack() },
                    alEditar = { id -> controlador.navigate(RutaEditorReceta(id)) },
                    alCocinar = { id, porciones -> controlador.navigate(RutaCocina(id, porciones)) },
                    alVerHistorial = { controlador.navigate(RutaHistorial) }
                )
            }

            composable<RutaHistorial> {
                val vistaModelo: HistorialViewModel = viewModel(factory = Fabricas.Factory)
                HistorialPantalla(
                    vistaModelo = vistaModelo,
                    alVolver = { controlador.popBackStack() },
                    alAbrirReceta = { id -> controlador.navigate(RutaDetalleReceta(id)) }
                )
            }

            composable<RutaEstadisticas> {
                val vistaModelo: EstadisticasViewModel = viewModel(factory = Fabricas.Factory)
                EstadisticasPantalla(vistaModelo = vistaModelo, alVolver = { controlador.popBackStack() })
            }

            composable<RutaAjustes> {
                val vistaModelo: AjustesViewModel = viewModel(factory = Fabricas.Factory)
                AjustesPantalla(vistaModelo = vistaModelo, alVolver = { controlador.popBackStack() })
            }

            composable<RutaCatalogo> {
                val vistaModelo: CatalogoViewModel = viewModel(factory = Fabricas.Factory)
                CatalogoPantalla(vistaModelo = vistaModelo, alVolver = { controlador.popBackStack() })
            }

            composable<RutaCocina> {
                val vistaModelo: CocinaViewModel = viewModel(factory = Fabricas.Factory)
                CocinaPantalla(
                    vistaModelo = vistaModelo,
                    alSalir = { controlador.popBackStack() },
                    alTerminar = { controlador.popBackStack() }
                )
            }

            composable<RutaEditorReceta> { entrada ->
                val argumentos = entrada.toRoute<RutaEditorReceta>()
                val vistaModelo: EditorRecetaViewModel = viewModel(factory = Fabricas.Factory)
                EditorRecetaPantalla(
                    vistaModelo = vistaModelo,
                    alVolver = { controlador.popBackStack() },
                    alGuardar = { id ->
                        controlador.popBackStack()
                        // Una receta recien creada se abre para verla ya escalable.
                        if (argumentos.recetaId == 0L) {
                            controlador.navigate(RutaDetalleReceta(id)) { launchSingleTop = true }
                        }
                    }
                )
            }
        }
    }
    }
    }
}

/**
 * Barra inferior del prototipo: texto e icono alineados a la izquierda, filete de
 * 3 dp en acento sobre la solapa activa y globo cuadrado con los timers vivos.
 */
@Composable
private fun BarraSolapas(
    alElegir: (Solapa) -> Unit,
    esActual: (Solapa) -> Boolean,
    cantidadTimers: Int
) {
    val colores = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colores.background)
            .fileteArriba(colores.outline, 2.dp)
            .navigationBarsPadding()
            .height(62.dp)
    ) {
        SOLAPAS.forEachIndexed { indice, solapa ->
            val activa = esActual(solapa)
            val tinta = if (activa) colores.primary else colores.onBackground.copy(alpha = 0.6f)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .fileteArriba(if (activa) colores.primary else Color.Transparent, 3.dp)
                    .semantics { selected = activa }
                    .clickable(role = Role.Tab) { alElegir(solapa) }
                    .padding(horizontal = 10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Icon(solapa.icono, contentDescription = null, tint = tinta, modifier = Modifier.size(21.dp))
                    if (solapa.clase == RutaCronometros::class && cantidadTimers > 0) {
                        Box(
                            modifier = Modifier
                                .background(colores.primary)
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cantidadTimers.toString(),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 0.sp),
                                color = colores.onPrimary
                            )
                        }
                    }
                }
                Text(
                    text = stringResource(solapa.textoId),
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = (-0.01).sp),
                    color = tinta,
                    maxLines = 1
                )
            }
            if (indice < SOLAPAS.lastIndex) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(colores.outline)
                )
            }
        }
    }
}

