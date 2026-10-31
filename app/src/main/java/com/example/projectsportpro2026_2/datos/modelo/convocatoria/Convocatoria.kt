package com.example.projectsportpro2026_2.datos.modelo.convocatoria

// Documento de Firestore: convocatorias/{idPartido}
data class Convocatoria(
    val idPartido: String = "",
    val idEquipo: String = "",
    val idAcademia: String = "",
    val publicada: Boolean = false,
    val creadoPor: String = "",
    val fechaPublicacion: Long = 0L,
    val actualizadoPor: String = "",
    val fechaActualizacion: Long = 0L
)

// Documento de Firestore:
// convocatorias/{idPartido}/jugadores/{idJugador}
data class JugadorConvocado(
    val idJugador: String = "",
    val estado: EstadoDisponibilidad = EstadoDisponibilidad.PENDIENTE,
    val respondidoPor: String = "",
    val fechaRespuesta: Long = 0L
)

enum class EstadoDisponibilidad {
    PENDIENTE,
    DISPONIBLE,
    NO_DISPONIBLE
}