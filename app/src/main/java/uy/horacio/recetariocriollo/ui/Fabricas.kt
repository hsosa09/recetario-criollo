package uy.horacio.recetariocriollo.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import uy.horacio.recetariocriollo.RecetarioApp
import uy.horacio.recetariocriollo.ui.busqueda.BusquedaViewModel
import uy.horacio.recetariocriollo.ui.conversor.ConversorViewModel
import uy.horacio.recetariocriollo.ui.cronometro.CronometrosViewModel
import uy.horacio.recetariocriollo.ui.recetas.DetalleRecetaViewModel
import uy.horacio.recetariocriollo.ui.recetas.EditorRecetaViewModel
import uy.horacio.recetariocriollo.ui.recetas.ListaRecetasViewModel

/** Fabrica unica de ViewModels: toma las dependencias del contenedor de la app. */
object Fabricas {

    val Factory = viewModelFactory {
        initializer { ListaRecetasViewModel(app().contenedor.recetas) }

        initializer {
            DetalleRecetaViewModel(
                repositorio = app().contenedor.recetas,
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

        initializer { ConversorViewModel(app().contenedor.ingredientes) }

        initializer { CronometrosViewModel(app().contenedor.cronometros) }
    }
}

private fun CreationExtras.app(): RecetarioApp =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as RecetarioApp
