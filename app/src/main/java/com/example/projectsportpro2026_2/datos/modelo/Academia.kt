package com.example.projectsportpro2026_2.datos.modelo

// Documento de Firestore: academias/{id}
data class Academia(
    val id: String = "",
    val nombre: String = "",
    val direccion: String = "",
    val descripcion: String = "",
    val idAdministrador: String = "",
    val idsAutorizados: List<String> = emptyList(),  // quienes pueden gestionarla
    val activa: Boolean = true,
    val actualizadoPor: String = "",
    val fechaActualizacion: Long = 0L                // milisegundos
)