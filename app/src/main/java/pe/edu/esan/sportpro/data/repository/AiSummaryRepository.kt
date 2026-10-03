package pe.edu.esan.sportpro.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import pe.edu.esan.sportpro.data.model.AiSummary
// solo manejará en Firestore los resúmenes generados, revisados y aprobados
class AiSummaryRepository {

    private val firestore: FirebaseFirestore =
        FirebaseFirestore.getInstance()

    // =========================================================
    // GUARDAR RESUMEN GENERADO POR IA COMO BORRADOR
    // =========================================================

    fun createDraftSummary(
        matchId: String,
        generatedText: String,
        sourceEventCount: Long,
        sourceVersion: Long,
        onResult: (Result<AiSummary>) -> Unit
    ) {

        val document =
            firestore
                .collection("ai_summaries")
                .document()

        val summary = AiSummary(
            id = document.id,
            matchId = matchId,
            generatedText = generatedText,
            editedText = "",
            finalText = "",
            status = AiSummary.STATUS_DRAFT,
            generatedBy = "AI",
            generatedAt = Timestamp.now(),
            reviewedBy = "",
            reviewedAt = null,
            approvedBy = "",
            approvedAt = null,
            hasOmissions = false,
            hasErrors = false,
            hasInventedInfo = false,
            corrections = "",
            sourceEventCount = sourceEventCount,
            sourceVersion = sourceVersion,
            outdated = false
        )

        document
            .set(summary)
            .addOnSuccessListener {

                onResult(
                    Result.success(summary)
                )
            }
            .addOnFailureListener { exception ->

                onResult(
                    Result.failure(exception)
                )
            }
    }

    // =========================================================
    // OBTENER RESUMEN POR ID
    // =========================================================

    fun getSummaryById(
        summaryId: String,
        onResult: (Result<AiSummary>) -> Unit
    ) {

        firestore
            .collection("ai_summaries")
            .document(summaryId)
            .get()
            .addOnSuccessListener { document ->

                val summary =
                    document.toObject(
                        AiSummary::class.java
                    )

                if (summary != null) {

                    onResult(
                        Result.success(summary)
                    )

                } else {

                    onResult(
                        Result.failure(
                            Exception(
                                "No se encontró el resumen"
                            )
                        )
                    )
                }
            }
            .addOnFailureListener { exception ->

                onResult(
                    Result.failure(exception)
                )
            }
    }

    // =========================================================
    // OBTENER RESÚMENES DE UN PARTIDO
    // =========================================================

    fun getSummariesByMatch(
        matchId: String,
        onResult: (Result<List<AiSummary>>) -> Unit
    ) {

        firestore
            .collection("ai_summaries")
            .whereEqualTo(
                "matchId",
                matchId
            )
            .get()
            .addOnSuccessListener { querySnapshot ->

                val summaries =
                    querySnapshot
                        .documents
                        .mapNotNull { document ->

                            document.toObject(
                                AiSummary::class.java
                            )
                        }

                onResult(
                    Result.success(summaries)
                )
            }
            .addOnFailureListener { exception ->

                onResult(
                    Result.failure(exception)
                )
            }
    }

    // =========================================================
    // REVISAR / EDITAR RESUMEN
    // =========================================================

    fun reviewSummary(
        summaryId: String,
        editedText: String,
        reviewedBy: String,
        hasOmissions: Boolean,
        hasErrors: Boolean,
        hasInventedInfo: Boolean,
        corrections: String,
        onResult: (Result<Unit>) -> Unit
    ) {

        firestore
            .collection("ai_summaries")
            .document(summaryId)
            .update(
                mapOf(
                    "editedText" to editedText,
                    "status" to
                            AiSummary.STATUS_REVIEWED,
                    "reviewedBy" to reviewedBy,
                    "reviewedAt" to Timestamp.now(),
                    "hasOmissions" to hasOmissions,
                    "hasErrors" to hasErrors,
                    "hasInventedInfo" to
                            hasInventedInfo,
                    "corrections" to corrections
                )
            )
            .addOnSuccessListener {

                onResult(
                    Result.success(Unit)
                )
            }
            .addOnFailureListener { exception ->

                onResult(
                    Result.failure(exception)
                )
            }
    }

    // =========================================================
    // APROBAR RESUMEN
    // =========================================================

    fun approveSummary(
        summaryId: String,
        finalText: String,
        approvedBy: String,
        onResult: (Result<Unit>) -> Unit
    ) {

        firestore
            .collection("ai_summaries")
            .document(summaryId)
            .update(
                mapOf(
                    "finalText" to finalText,
                    "status" to
                            AiSummary.STATUS_APPROVED,
                    "approvedBy" to approvedBy,
                    "approvedAt" to Timestamp.now(),
                    "outdated" to false
                )
            )
            .addOnSuccessListener {

                onResult(
                    Result.success(Unit)
                )
            }
            .addOnFailureListener { exception ->

                onResult(
                    Result.failure(exception)
                )
            }
    }

    // =========================================================
    // MARCAR RESUMEN COMO DESACTUALIZADO
    // Si cambian eventos luego de generar/aprobar el resumen
    // =========================================================

    fun markSummaryOutdated(
        summaryId: String,
        newSourceVersion: Long,
        onResult: (Result<Unit>) -> Unit
    ) {

        firestore
            .collection("ai_summaries")
            .document(summaryId)
            .update(
                mapOf(
                    "status" to
                            AiSummary.STATUS_OUTDATED,
                    "outdated" to true,
                    "sourceVersion" to
                            newSourceVersion
                )
            )
            .addOnSuccessListener {

                onResult(
                    Result.success(Unit)
                )
            }
            .addOnFailureListener { exception ->

                onResult(
                    Result.failure(exception)
                )
            }
    }
}