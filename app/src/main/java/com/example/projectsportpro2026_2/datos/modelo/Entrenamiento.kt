package com.example.projectsportpro2026_2.datos.modelo

enum class EstadoEntrenamiento {
    PUBLICADO,
    CANCELADO
}

data class EjercicioEntrenamiento(
    val idEjercicio: String = "",
    val nombre: String = "",
    val objetivo: String = "",
    val duracionMinutos: Int = 0,
    val orden: Int = 0
)

data class CambioHorarioEntrenamiento(
    val fechaAnterior: String = "",
    val horaAnterior: String = "",
    val fechaNueva: String = "",
    val horaNueva: String = "",
    val actualizadoPor: String = "",
    val fechaCambio: Long = 0L
)

data class Entrenamiento(
    val id: String = "",
    val idAcademia: String = "",
    val idEquipo: String = "",
    val nombreEquipo: String = "",
    val idEntrenador: String = "",
    val fecha: String = "",
    val hora: String = "",
    val fechaHoraEpochMillis: Long = 0L,
    val objetivo: String = "",
    val duracionTotalMinutos: Int = 0,
    val ejercicios: List<EjercicioEntrenamiento> = emptyList(),
    val estado: EstadoEntrenamiento = EstadoEntrenamiento.PUBLICADO,
    val fechaCreacion: Long = 0L,
    val creadoPor: String = "",
    val actualizadoPor: String = "",
    val fechaActualizacion: Long = 0L,
    val historialCambiosHorario: List<CambioHorarioEntrenamiento> = emptyList()
)