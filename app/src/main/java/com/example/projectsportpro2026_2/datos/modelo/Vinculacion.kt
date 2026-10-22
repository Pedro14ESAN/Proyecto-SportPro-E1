package com.example.projectsportpro2026_2.datos.modelo

enum class EstadoVinculacion { PENDIENTE, ACTIVA, INACTIVA }

// Documento de Firestore: vinculaciones/{id}
data class Vinculacion(
    val id: String = "",
    val idApoderado: String = "",
    val nombreApoderado: String = "",
    val idJugador: String = "",
    val nombreJugador: String = "",
    val idAcademia: String = "",
    val estado: EstadoVinculacion = EstadoVinculacion.PENDIENTE,
    val fechaSolicitud: Long = 0L,
    val desvinculacionSolicitada: Boolean = false,
    val idAprobador: String = "",
    val nombreAprobador: String = "",
    val fechaAprobacion: Long = 0L,
    val actualizadoPor: String = "",
    val fechaActualizacion: Long = 0L
)