package com.example.projectsportpro2026_2.datos.modelo

// Documento de Firestore: equipos/{id}
data class Equipo(
    val id: String = "",
    val nombre: String = "",
    val categoria: String = "",
    val idAcademia: String = "",
    val idEntrenador: String = "",
    val activo: Boolean = true,
    val actualizadoPor: String = "",
    val fechaActualizacion: Long = 0L
)