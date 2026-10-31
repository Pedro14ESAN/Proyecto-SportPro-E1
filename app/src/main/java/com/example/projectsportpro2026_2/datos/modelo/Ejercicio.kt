package com.example.projectsportpro2026_2.datos.modelo

data class Ejercicio(
    val id: String = "",
    val nombre: String = "",
    val objetivo: String = "",
    val descripcion: String = "",
    val duracionMinutos: Int = 0,
    val idAcademia: String = "",
    val activo: Boolean = true,
    val creadoPor: String = "",
    val fechaCreacion: Long = 0L,
    val actualizadoPor: String = "",
    val fechaActualizacion: Long = 0L
)