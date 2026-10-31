package com.example.projectsportpro2026_2.datos.modelo

enum class EstadoAsistencia {
    PRESENTE,
    AUSENTE,
    JUSTIFICADO
}

data class Asistencia(
    val id: String = "",
    val idEntrenamiento: String = "",
    val idJugador: String = "",
    val nombreJugador: String = "",
    val idEquipo: String = "",
    val nombreEquipo: String = "",
    val fechaEntrenamiento: String = "",
    val estado: EstadoAsistencia = EstadoAsistencia.PRESENTE,
    val registradoPor: String = "",
    val fechaRegistro: Long = 0L,
    val actualizadoPor: String = "",
    val fechaActualizacion: Long = 0L
)