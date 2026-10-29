package com.example.projectsportpro2026_2.datos.modelo.evento

data class TipoEventoCatalogo(
    val id: String = "",
    val codigo: String = "",
    val nombre: String = "",
    val activo: Boolean = true,

    val requiereEquipo: Boolean = false,
    val requiereJugador: Boolean = false,
    val requiereObservacion: Boolean = false,

    val afectaMarcador: Boolean = false,
    val afectaEstadoPartido: Boolean = false,

    val esTipoBase: Boolean = false,
    val orden: Int = 0,

    val creadoPor: String = "",
    val fechaCreacion: Long = 0L,
    val actualizadoPor: String = "",
    val fechaActualizacion: Long = 0L
)