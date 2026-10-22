package com.example.projectsportpro2026_2.ui.navegacion

import android.net.Uri
import com.example.projectsportpro2026_2.datos.modelo.EstadoCuenta
import com.example.projectsportpro2026_2.datos.modelo.PerfilUsuario
import com.example.projectsportpro2026_2.datos.modelo.Rol

object Rutas {
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val INICIO_ADM = "inicio_adm"
    const val INICIO_DT = "inicio_dt"
    const val INICIO_JUG = "inicio_jug"
    const val INICIO_PAD = "inicio_pad"
    const val CUENTA_PENDIENTE = "cuenta_pendiente"
    const val ACADEMIAS = "academias"
    const val EQUIPOS = "equipos/{idAcademia}/{nombre}"
    const val JUGADORES = "jugadores/{idEquipo}/{nombre}"
}

fun rutaEquipos(idAcademia: String, nombre: String): String =
    "equipos/$idAcademia/${Uri.encode(nombre)}"

fun rutaJugadores(idEquipo: String, nombre: String): String =
    "jugadores/$idEquipo/${Uri.encode(nombre)}"

// Decide a qué pantalla va cada usuario según su estado y su rol
fun rutaInicioPara(perfil: PerfilUsuario): String =
    if (perfil.estado == EstadoCuenta.PENDIENTE) {
        Rutas.CUENTA_PENDIENTE
    } else {
        when (perfil.rol) {
            Rol.ADM -> Rutas.INICIO_ADM
            Rol.DT -> Rutas.INICIO_DT
            Rol.JUG -> Rutas.INICIO_JUG
            Rol.PAD -> Rutas.INICIO_PAD
        }
    }