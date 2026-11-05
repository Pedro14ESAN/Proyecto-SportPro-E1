package com.example.projectsportpro2026_2.datos.modelo

// ---------- Qué se puede reportar ----------

enum class TipoContenido { PUBLICACION, COMENTARIO, AVISO }

enum class MotivoReporte(val texto: String) {
    INAPROPIADO("Contenido inapropiado"),
    ACOSO("Acoso o bullying"),
    DATOS_PERSONALES("Expone datos personales"),
    ENGANOSO("Información falsa o engañosa"),
    SPAM("Spam"),
    OTRO("Otro")
}

enum class EstadoReporte { PENDIENTE, RESUELTO }

enum class DecisionModeracion(val texto: String) {
    DESCARTADO("Reporte descartado"),
    OCULTADO("Contenido ocultado"),
    CERRADO("Aviso cerrado")          // solo para avisos de prueba
}

// Identifica un contenido concreto. No se guarda en Firestore: viaja de la pantalla al repositorio.
data class ObjetivoReporte(
    val tipo: TipoContenido,
    val idContenido: String,
    val idPublicacion: String = ""    // solo comentarios: en qué publicación vive
) {
    // Mismo contenido = mismo id de reporte → así se agrupan los reportes repetidos
    fun idReporte(): String =
        if (tipo == TipoContenido.COMENTARIO) "COMENTARIO_${idPublicacion}_$idContenido"
        else "${tipo.name}_$idContenido"
}

// ---------- Documentos de Firestore ----------

// Un documento por CONTENIDO reportado, que agrupa todos sus reportes: reportes/{idReporte}
data class Reporte(
    val id: String = "",
    val tipoContenido: TipoContenido = TipoContenido.PUBLICACION,
    val idContenido: String = "",
    val idPublicacion: String = "",
    val idAcademia: String = "",                   // define qué ADM lo modera

    // Evidencia: copia del contenido tal como estaba al ser reportado (RC-10)
    val resumenContenido: String = "",
    val idAutorContenido: String = "",
    val nombreAutorContenido: String = "",

    // Agrupación (caso alterno HU24)
    val cantidadReportes: Int = 0,
    val motivos: Map<String, Long> = emptyMap(),   // "ACOSO" → 3 veces
    val estado: EstadoReporte = EstadoReporte.PENDIENTE,
    val fechaPrimerReporte: Long = 0L,
    val fechaUltimoReporte: Long = 0L,

    // Última decisión (el historial completo está en "moderaciones")
    val decision: DecisionModeracion? = null,
    val motivoDecision: String = "",
    val idModerador: String = "",
    val nombreModerador: String = "",
    val fechaDecision: Long = 0L
) {
    fun objetivo(): ObjetivoReporte = ObjetivoReporte(tipoContenido, idContenido, idPublicacion)
}

// Reporte individual de una persona: reportes/{idReporte}/denuncias/{uidReportante}
// El id del documento ES el uid: una persona no puede reportar dos veces el mismo contenido.
data class Denuncia(
    val uidReportante: String = "",
    val motivo: MotivoReporte = MotivoReporte.OTRO,
    val detalle: String = "",
    val fecha: Long = 0L
)

// Auditoría: un registro por cada decisión. Solo se agregan, nunca se editan: moderaciones/{id}
data class RegistroModeracion(
    val id: String = "",
    val idReporte: String = "",
    val tipoContenido: TipoContenido = TipoContenido.PUBLICACION,
    val idContenido: String = "",
    val decision: DecisionModeracion = DecisionModeracion.DESCARTADO,
    val motivo: String = "",
    val idModerador: String = "",
    val nombreModerador: String = "",
    val cantidadReportesAlDecidir: Int = 0,
    val fecha: Long = 0L
)

// Bloqueo entre dos usuarios: bloqueos/{idBloqueador}_{idBloqueado}
data class Bloqueo(
    val idBloqueador: String = "",
    val idBloqueado: String = "",
    val nombreBloqueado: String = "",
    val activo: Boolean = true,        // desbloquear = activo false (no se borra)
    val fecha: Long = 0L,
    val fechaActualizacion: Long = 0L
)