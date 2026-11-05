package com.example.projectsportpro2026_2.datos.modelo

// A quién va dirigido el anuncio (criterio HU19: academia, equipo o rol)
enum class TipoAudiencia { ACADEMIA, EQUIPO, ROL }

// Un anuncio no se borra: se archiva (criterio HU19)
enum class EstadoAnuncio { ACTIVO, ARCHIVADO }

// Documento de Firestore: anuncios/{id}
data class Anuncio(
    val id: String = "",
    val titulo: String = "",
    val contenido: String = "",

    // Audiencia
    val idAcademia: String = "",                  // siempre: a qué academia pertenece
    val tipoAudiencia: TipoAudiencia = TipoAudiencia.ACADEMIA,
    val idEquipo: String = "",                    // solo si tipoAudiencia == EQUIPO
    val nombreEquipo: String = "",
    val rolDestino: Rol = Rol.DT,                 // solo si tipoAudiencia == ROL

    // Autor
    val idAutor: String = "",
    val nombreAutor: String = "",
    val rolAutor: Rol = Rol.DT,

    // Estado y trazabilidad
    val estado: EstadoAnuncio = EstadoAnuncio.ACTIVO,
    val editado: Boolean = false,                 // para mostrar "Actualizado" si cambió el horario
    val fechaCreacion: Long = 0L,                 // milisegundos
    val actualizadoPor: String = "",
    val fechaActualizacion: Long = 0L
)