package pe.edu.esan.sportpro.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import pe.edu.esan.sportpro.data.model.Academy

/**
 * HU-03 · Creación de la academia por el administrador.
 *
 * La academia y el vínculo con el usuario (users/{uid}.academyId) se guardan
 * en un solo batch: o se guardan las dos cosas o no se guarda ninguna.
 * Las reglas de Firestore solo permiten crear academias al rol ADM.
 */
class AcademyRepository {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun createAcademy(
        name: String,
        description: String,
        address: String,
        onResult: (Result<Academy>) -> Unit
    ) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onResult(Result.failure(IllegalStateException("Tu sesión expiró. Inicia sesión otra vez")))
            return
        }

        val cleanName = name.trim().replace(Regex("\\s+"), " ")
        if (cleanName.isEmpty()) {
            onResult(Result.failure(IllegalArgumentException("El nombre de la academia es obligatorio")))
            return
        }

        val academyRef = firestore.collection("academies").document()
        val userRef = firestore.collection("users").document(uid)

        val academy = Academy(
            id = academyRef.id,
            name = cleanName,
            description = description.trim(),
            logoUrl = "",
            administratorUid = uid,
            address = address.trim(),
            active = true,
            createdAt = Timestamp.now()
        )

        firestore.runBatch { batch ->
            batch.set(academyRef, academy)
            batch.update(
                userRef,
                mapOf(
                    "academyId" to academyRef.id,
                    "updatedAt" to Timestamp.now()
                )
            )
        }
            .addOnSuccessListener { onResult(Result.success(academy)) }
            .addOnFailureListener { exception -> onResult(Result.failure(exception)) }
    }
}