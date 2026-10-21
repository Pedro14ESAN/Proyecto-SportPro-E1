package com.example.projectsportpro2026_2.datos.modelo

enum class EstadoCuenta { ACTIVA, PENDIENTE }

// Documento que se guarda en Firestore: usuarios/{uid}
// Los valores por defecto son necesarios para que Firestore pueda leerlo.
data class PerfilUsuario(
    val uid: String = "",
    val nombre: String = "",
    val correo: String = "",
    val rol: Rol = Rol.JUG,
    val fechaNacimiento: String = "",   // formato aaaa-MM-dd (solo jugadores)
    val estado: EstadoCuenta = EstadoCuenta.ACTIVA
)