package com.example.projectsportpro2026_2.ui.navegacion

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioAutenticacion
import com.example.projectsportpro2026_2.ui.autenticacion.PantallaLogin
import com.example.projectsportpro2026_2.ui.autenticacion.PantallaRegistro
import com.example.projectsportpro2026_2.ui.inicio.InicioAdministrador
import com.example.projectsportpro2026_2.ui.inicio.InicioApoderado
import com.example.projectsportpro2026_2.ui.inicio.InicioEntrenador
import com.example.projectsportpro2026_2.ui.inicio.InicioJugador
import com.example.projectsportpro2026_2.ui.inicio.PantallaCuentaPendiente

@Composable
fun NavegacionApp() {
    val repositorio = remember { RepositorioAutenticacion() }
    val navController = rememberNavController()
    var destinoInicial by remember { mutableStateOf<String?>(null) }

    // Al abrir la app: si ya hay sesión guardada, va directo a su pantalla de inicio
    LaunchedEffect(Unit) {
        val uid = repositorio.uidActual()
        destinoInicial = if (uid == null) {
            Rutas.LOGIN
        } else {
            val perfil = try {
                repositorio.obtenerPerfil(uid)
            } catch (e: Exception) {
                null
            }
            if (perfil != null) rutaInicioPara(perfil) else Rutas.LOGIN
        }
    }

    val inicio = destinoInicial
    if (inicio == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Va a una ruta y limpia el historial, para que "atrás" no regrese al login
    val irA: (String) -> Unit = { ruta ->
        navController.navigate(ruta) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }
    val cerrarSesion: () -> Unit = {
        repositorio.cerrarSesion()
        irA(Rutas.LOGIN)
    }

    NavHost(navController = navController, startDestination = inicio) {
        composable(Rutas.LOGIN) {
            PantallaLogin(
                onSesionIniciada = { perfil -> irA(rutaInicioPara(perfil)) },
                onIrARegistro = { navController.navigate(Rutas.REGISTRO) }
            )
        }
        composable(Rutas.REGISTRO) {
            PantallaRegistro(
                onRegistrado = { perfil -> irA(rutaInicioPara(perfil)) },
                onIrALogin = { navController.popBackStack() }
            )
        }
        composable(Rutas.INICIO_ADM) { InicioAdministrador(cerrarSesion) }
        composable(Rutas.INICIO_DT) { InicioEntrenador(cerrarSesion) }
        composable(Rutas.INICIO_JUG) { InicioJugador(cerrarSesion) }
        composable(Rutas.INICIO_PAD) { InicioApoderado(cerrarSesion) }
        composable(Rutas.CUENTA_PENDIENTE) { PantallaCuentaPendiente(cerrarSesion) }
    }
}