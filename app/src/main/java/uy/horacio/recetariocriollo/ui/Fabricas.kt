package uy.horacio.recetariocriollo.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import uy.horacio.recetariocriollo.RecetarioApp
import uy.horacio.recetariocriollo.ui.busqueda.BusquedaViewModel
import uy.horacio.recetariocriollo.ui.cajon.CajonViewModel
import uy.horacio.recetariocriollo.ui.catalogo.CatalogoViewModel
import uy.horacio.recetariocriollo.ui.cocina.CocinaViewModel
import uy.horacio.recetariocriollo.ui.conversor.ConversorViewModel
import uy.horacio.recetariocriollo.ui.cronometro.CronometrosViewModel
import uy.horacio.recetariocriollo.ui.historial.HistorialViewModel
import uy.horacio.recetariocriollo.ui.recetas.DetalleRecetaViewModel
import uy.horacio.recetariocriollo.ui.recetas.EditorRecetaViewModel
import uy.horacio.recetariocriollo.ui.recetas.ListaRecetasViewModel

/** Fabrica unica de ViewModels: toma las dependencias del contenedor de la app. */
object Fabricas {

    val Factory = viewModelFactory {
        initializer { ListaRecetasViewModel(app().contenedor.recetas, app().contenedor.cocinadas) }

        initializer {
            DetalleRecetaViewModel(
                repositorio = app().contenedor.recetas,
                cocinadas = app().contenedor.cocinadas,
                cronometros = app().contenedor.cronometros,
                estadoGuardado = createSavedStateHandle()
            )
        }

        initializer {
            EditorRecetaViewModel(
                recetas = app().contenedor.recetas,
                ingredientes = app().contenedor.ingredientes,
                almacenFotos = app().contenedor.almacenFotos,
                estadoGuardado = createSavedStateHandle()
            )
        }

        initializer {
            BusquedaViewModel(
                recetas = app().contenedor.recetas,
                ingredientes = app().contenedor.ingredientes
            )
        }

        initializer { ConversorViewModel(app().contenedor.ingredientes, app().contenedor.recetas) }

        initializer { CronometrosViewModel(app().contenedor.cronometros) }

        initializer {
            CajonViewModel(
                recetas = app().contenedor.recetas,
                ingredientes = app().contenedor.ingredientes,
                cocinadas = app().contenedor.cocinadas
            )
        }

        initializer {
            CatalogoViewModel(
                ingredientes = app().contenedor.ingredientes,
                recetas = app().contenedor.recetas
            )
        }

        initializer {
            HistorialViewModel(
                cocinadas = app().contenedor.cocinadas,
                recetas = app().contenedor.recetas
            )
        }

        initializer {
            CocinaViewModel(
                repositorio = app().contenedor.recetas,
                cocinadas = app().contenedor.cocinadas,
                cronometros = app().contenedor.cronometros,
                estadoGuardado = createSavedStateHandle()
            )
        }
    }
}

private fun CreationExtras.app(): RecetarioApp =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as RecetarioApp
