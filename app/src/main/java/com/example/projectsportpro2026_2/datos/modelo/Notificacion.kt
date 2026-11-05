package com.example.projectsportpro2026_2.datos.modelo

enum class TipoNotificacion(val emoji: String) {
    CONVOCATORIA("⚽"),
    CONVOCATORIA_RETIRADA("↩️"),
    ENTRENAMIENTO_PUBLICADO("🏃"),
    ENTRENAMIENTO_CAMBIADO("⏰"),
    ENTRENAMIENTO_CANCELADO("🚫"),
    ANUNCIO("📢")
}

// Aviso interno: notificaciones/{id}
// El texto NUNCA incluye datos de menores (nombres, teléfonos, etc.):
// puede verse en la pantalla bloqueada (criterio HU20).
data class Notificacion(
    val id: String = "",
    val idDestinatario: String = "",
    val tipo: TipoNotificacion = TipoNotificacion.ANUNCIO,
    val titulo: String = "",
    val mensaje: String = "",
    val idReferencia: String = "",          // id del anuncio, entrenamiento o partido
    val idEmisor: String = "",
    val leida: Boolean = false,
    val mostradaEnSistema: Boolean = false, // ya apareció en la barra del celular
    val fechaCreacion: Long = 0L
)