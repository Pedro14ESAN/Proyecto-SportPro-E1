package com.example.projectsportpro2026_2.datos.modelo

enum class EstadoMensualidad {
    PENDIENTE,
    PAGADO,
    VENCIDO
}

data class Mensualidad(
    val id: String = "",

    val idJugador: String = "",
    val nombreJugador: String = "",

    val idAcademia: String = "",
    val nombreAcademia: String = "",

    val idEquipo: String = "",

    val periodo: String = "",

    val estado: EstadoMensualidad =
        EstadoMensualidad.PENDIENTE,

    val actualizadoPor: String = "",

    val fechaActualizacion: Long = 0L
)