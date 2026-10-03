package pe.edu.esan.sportpro.data.model

import com.google.firebase.Timestamp
// el resumen generado por IA y su posterior revisión por el DT.
data class AiSummary(
    val id: String = "",
    val matchId: String = "",
    val generatedText: String = "",
    val editedText: String = "",
    val finalText: String = "",
    val status: String = STATUS_DRAFT,
    val generatedBy: String = "AI",
    val generatedAt: Timestamp? = null,
    val reviewedBy: String = "",
    val reviewedAt: Timestamp? = null,
    val approvedBy: String = "",
    val approvedAt: Timestamp? = null,

    // Evaluación del DT sobre el resultado de IA
    val hasOmissions: Boolean = false,
    val hasErrors: Boolean = false,
    val hasInventedInfo: Boolean = false,
    val corrections: String = "",

    // Trazabilidad con los eventos del partido
    val sourceEventCount: Long = 0,
    val sourceVersion: Long = 1,

    // Si cambian eventos luego de generar el resumen
    val outdated: Boolean = false
) {

    companion object {

        const val STATUS_DRAFT = "DRAFT"
        const val STATUS_REVIEWED = "REVIEWED"
        const val STATUS_APPROVED = "APPROVED"
        const val STATUS_OUTDATED = "OUTDATED"
    }
}