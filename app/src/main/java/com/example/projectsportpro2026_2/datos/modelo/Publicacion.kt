package com.example.projectsportpro2026_2.datos.modelo

// Quién puede ver la publicación (HU21 / RC-03)
enum class VisibilidadPublicacion { EQUIPO, ACADEMIA, COMUNIDAD }

// VISIBLE: normal
// ELIMINADA: el autor la borró (eliminación lógica, HU21)
// OCULTA: un moderador la ocultó (HU24)
enum class EstadoPublicacion { VISIBLE, ELIMINADA, OCULTA }
// Reacciones disponibles (HU22). Cada usuario puede tener solo una, y cambiarla.
enum class TipoReaccion(val emoji: String) {
    ME_GUSTA("❤️"),
    APLAUSO("👏"),
    FUERZA("💪")
}

// Documento de Firestore: publicaciones/{id}
data class Publicacion(
    val id: String = "",
    val texto: String = "",
    val imagenUrl: String = "",                  // Cloud Storage, opcional

    // Visibilidad
    val visibilidad: VisibilidadPublicacion = VisibilidadPublicacion.ACADEMIA,
    val idAcademia: String = "",                 // academia del autor (vacío si no tiene)
    val nombreAcademia: String = "",
    val idEquipo: String = "",                   // solo si visibilidad == EQUIPO
    val nombreEquipo: String = "",

    // Autor: no hay publicaciones anónimas (RC-02)
    val idAutor: String = "",
    val nombreAutor: String = "",
    val rolAutor: Rol = Rol.JUG,

    // Interacciones (HU22)
    val reacciones: Map<String, String> = emptyMap(),   // uid → tipo de reacción (una por usuario)
    val cantidadComentarios: Int = 0,

    // Estado y trazabilidad
    val estado: EstadoPublicacion = EstadoPublicacion.VISIBLE,
    val editada: Boolean = false,
    val fechaCreacion: Long = 0L,
    val fechaActualizacion: Long = 0L
) {
    // La reacción que dejó este usuario (null si no reaccionó)
    fun reaccionDe(uid: String): TipoReaccion? =
        reacciones[uid]?.let { valor -> TipoReaccion.entries.find { it.name == valor } }

    fun totalReacciones(): Int = reacciones.size

    fun conteoDe(tipo: TipoReaccion): Int = reacciones.values.count { it == tipo.name }
}