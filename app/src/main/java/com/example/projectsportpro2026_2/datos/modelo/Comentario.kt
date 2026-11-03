package com.example.projectsportpro2026_2.datos.modelo

// VISIBLE: normal | ELIMINADO: lo borró su autor | OCULTO: lo ocultó un moderador (HU24)
enum class EstadoComentario { VISIBLE, ELIMINADO, OCULTO }

// Documento de Firestore: publicaciones/{idPublicacion}/comentarios/{id}
data class Comentario(
    val id: String = "",
    val idPublicacion: String = "",
    val texto: String = "",

    // Autor (criterio HU22: autor y fecha/hora)
    val idAutor: String = "",
    val nombreAutor: String = "",
    val rolAutor: Rol = Rol.JUG,

    val estado: EstadoComentario = EstadoComentario.VISIBLE,
    val fechaCreacion: Long = 0L,
    val fechaActualizacion: Long = 0L
)