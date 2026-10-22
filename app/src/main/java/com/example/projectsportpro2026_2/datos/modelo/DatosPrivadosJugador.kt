package com.example.projectsportpro2026_2.datos.modelo

// Documento de Firestore: jugadores/{id}/privado/datos  (información sensible)
data class DatosPrivadosJugador(
    val estaturaCm: Double? = null,            // null = pendiente
    val pesoKg: Double? = null,                // null = pendiente
    val telefono: String = "",
    val contactoEmergenciaNombre: String = "",
    val contactoEmergenciaTelefono: String = ""
)