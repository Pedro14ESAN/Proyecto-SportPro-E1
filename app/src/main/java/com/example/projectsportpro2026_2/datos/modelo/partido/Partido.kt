package com.example.projectsportpro2026_2.datos.modelo.partido

import com.example.projectsportpro2026_2.datos.modelo.Academia


data class Partido(
    val id: String = "",
    val idEquipo: String = "",
    val idAcademia: String = "",
    val nombreRival: String = "",
    val fecha: String = "",
    val hora: String = "",
    val ubicacion: String = "",
    val estado: EstadoPartido = EstadoPartido.PROGRAMADO,
    val creadoPor: String = "",
    val fechaCreacion: Long = 0L,
    val actualizadoPor: String = "",
    val fechaActualizacion: Long = 0L
)

enum class EstadoPartido{
    PROGRAMADO,
    EN_CURSO,
    FINALIZADO,
    CANCELADO
}