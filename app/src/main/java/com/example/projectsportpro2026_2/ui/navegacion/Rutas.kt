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

    const val PARTIDOS = "partidos/{idAcademia}/{nombre}"
    const val CREAR_PARTIDO = "partidos/{idAcademia}/{nombre}/crear"
    const val DETALLE_PARTIDO = "partido/{idPartido}"
    const val EDITAR_PARTIDO = "partido/{idPartido}/editar"
    const val CONVOCATORIA = "partido/{idPartido}/convocatoria"
    const val MIS_CONVOCATORIAS = "mis_convocatorias"
    const val ALINEACION = "partido/{idPartido}/alineacion"

    const val MIS_JUGADORES = "mis_jugadores"
    const val VINCULACIONES = "vinculaciones"

    const val MENSUALIDADES = "mensualidades"
    const val EJERCICIOS = "ejercicios"
    const val ENTRENAMIENTOS = "entrenamientos"
    const val ASISTENCIAS = "asistencias"

    const val CATALOGO_EVENTOS =
        "academias/{idAcademia}/catalogo_eventos"

    const val PARTIDO_EN_VIVO =
        "partido/{idPartido}/en_vivo"
}

fun rutaEquipos(
    idAcademia: String,
    nombre: String
): String =
    "equipos/$idAcademia/${Uri.encode(nombre)}"

fun rutaJugadores(
    idEquipo: String,
    nombre: String
): String =
    "jugadores/$idEquipo/${Uri.encode(nombre)}"

fun rutaPartidos(
    idAcademia: String,
    nombre: String
): String =
    "partidos/$idAcademia/${Uri.encode(nombre)}"

fun rutaCrearPartido(
    idAcademia: String,
    nombre: String
): String =
    "partidos/$idAcademia/${Uri.encode(nombre)}/crear"

fun rutaDetallePartido(
    idPartido: String
): String =
    "partido/$idPartido"

fun rutaConvocatoria(
    idPartido: String
): String =
    "partido/$idPartido/convocatoria"

fun rutaEditarPartido(
    idPartido: String
): String =
    "partido/$idPartido/editar"

fun rutaAlineacion(
    idPartido: String
): String =
    "partido/$idPartido/alineacion"

fun rutaCatalogoEventos(
    idAcademia: String
): String =
    "academias/$idAcademia/catalogo_eventos"

fun rutaPartidoEnVivo(
    idPartido: String
): String =
    "partido/$idPartido/en_vivo"

fun rutaInicioPara(
    perfil: PerfilUsuario
): String =
    if (
        perfil.estado ==
        EstadoCuenta.PENDIENTE
    ) {
        Rutas.CUENTA_PENDIENTE
    } else {
        when (perfil.rol) {
            Rol.ADM -> Rutas.INICIO_ADM
            Rol.DT -> Rutas.INICIO_DT
            Rol.JUG -> Rutas.INICIO_JUG
            Rol.PAD -> Rutas.INICIO_PAD
        }
    }