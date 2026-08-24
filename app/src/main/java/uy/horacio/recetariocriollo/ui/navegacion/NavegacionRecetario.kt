package uy.horacio.recetariocriollo.ui.navegacion

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
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
import uy.horacio.recetariocriollo.ui.busqueda.BusquedaViewModel
import uy.horacio.recetariocriollo.ui.conversor.ConversorPantalla
import uy.horacio.recetariocriollo.ui.conversor.ConversorViewModel
import uy.horacio.recetariocriollo.ui.cronometro.CronometrosPantalla
import uy.horacio.recetariocriollo.ui.cronometro.CronometrosViewModel
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
    Solapa(RutaRecetas, RutaRecetas::class, Icons.AutoMirrored.Filled.MenuBook, R.string.nav_recetas),
    Solapa(RutaBusqueda, RutaBusqueda::class, Icons.Default.Kitchen, R.string.nav_buscar),
    Solapa(RutaConversor, RutaConversor::class, Icons.Default.Scale, R.string.nav_conversor),
    Solapa(RutaCronometros, RutaCronometros::class, Icons.Default.Timer, R.string.nav_timers)
)

@Composable
fun NavegacionRecetario(
    controlador: NavHostController = rememberNavController()
) {
    val entradaActual by controlador.currentBackStackEntryAsState()
    val destino = entradaActual?.destination
    val enSolapa = SOLAPAS.any { solapa -> destino?.hasRoute(solapa.clase) == true }

    // El contador de timers andando se ve desde cualquier pantalla.
    val cronometrosVm: CronometrosViewModel = viewModel(factory = Fabricas.Factory)
    val cronometros by cronometrosVm.cronometros.collectAsStateWithLifecycle()
    val activos = cronometros.count { it.estado != EstadoCronometro.TERMINADO }
    val terminados = cronometros.count { it.estado == EstadoCronometro.TERMINADO }

    Scaffold(
        bottomBar = {
            if (enSolapa) {
                NavigationBar {
                    SOLAPAS.forEach { solapa ->
                        val seleccionada = destino?.hasRoute(solapa.clase) == true
                        NavigationBarItem(
                            selected = seleccionada,
                            onClick = {
                                controlador.navigate(solapa.ruta) {
                                    popUpTo(RutaRecetas) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                if (solapa.clase == RutaCronometros::class && (activos + terminados) > 0) {
                                    BadgedBox(badge = { Badge { Text("${activos + terminados}") } }) {
                                        Icon(solapa.icono, contentDescription = null)
                                    }
                                } else {
                                    Icon(solapa.icono, contentDescription = null)
                                }
                            },
                            label = { Text(stringResource(solapa.textoId)) }
                        )
                    }
                }
            }
        }
    ) { relleno ->
        NavHost(
            navController = controlador,
            startDestination = RutaRecetas,
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno)
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
                    alEditar = { id -> controlador.navigate(RutaEditorReceta(id)) }
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
