package pe.edu.esan.sportpro.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import pe.edu.esan.sportpro.data.model.Team
import java.text.Normalizer

class DuplicateTeamException :
    Exception("Ya existe un equipo con ese nombre y categoría en esta academia")

class TeamNotFoundException : Exception("El equipo ya no existe")

/**
 * HU-03 · Gestión de academia, equipos y categorías.
 *
 * Se conservan las funciones originales (getTeamsByAcademy, getTeamsByCoach, getTeamById)
 * con la misma firma. Cambios:
 *  - createTeam / updateTeam: validan datos, impiden duplicados (CA-02, CA-03) y guardan auditoría (CA-07).
 *  - deactivateTeam / activateTeam: nunca se elimina un equipo, se inactiva (CA-05).
 *  - observeTeamsByAcademy: lista en tiempo real con activos e inactivos (CA-06).
 *
 * Unicidad (CA-03): Firestore no tiene índices únicos, así que cada equipo reserva un documento
 * en `team_keys` (academia + nombre + categoría) dentro de la misma transacción que lo crea.
 */
class TeamRepository {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private enum class Outcome { OK, DUPLICATE, NOT_FOUND }

    // Crear equipo
    fun createTeam(
        academyId: String,
        name: String,
        category: String,
        coachUid: String,
        season: String,
        onResult: (Result<Team>) -> Unit
    ) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onResult(Result.failure(IllegalStateException(SESSION_EXPIRED)))
            return
        }

        val cleanName = normalizeName(name)
        validate(cleanName, category)?.let {
            onResult(Result.failure(IllegalArgumentException(it)))
            return
        }

        val document = firestore.collection("teams").document()
        val keyRef = firestore.collection("team_keys")
            .document(uniqueKey(academyId, cleanName, category))
        val now = Timestamp.now()

        val team = Team(
            id = document.id,
            academyId = academyId,
            name = cleanName,
            category = category,
            coachUid = coachUid,
            season = season,
            active = true,
            createdAt = now,
            createdBy = uid,
            updatedBy = uid,
            updatedAt = now
        )

        firestore.runTransaction { tx ->
            if (tx.get(keyRef).exists()) {
                false
            } else {
                tx.set(document, team)
                tx.set(keyRef, mapOf("teamId" to document.id, "academyId" to academyId))
                true
            }
        }
            .addOnSuccessListener { created ->
                if (created) onResult(Result.success(team))
                else onResult(Result.failure(DuplicateTeamException()))
            }
            .addOnFailureListener { exception ->
                onResult(Result.failure(exception))
            }
    }

    // Obtener todos los equipos ACTIVOS de una academia
    fun getTeamsByAcademy(
        academyId: String,
        onResult: (Result<List<Team>>) -> Unit
    ) {
        firestore
            .collection("teams")
            .whereEqualTo("academyId", academyId)
            .whereEqualTo("active", true)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val teams = querySnapshot.documents.mapNotNull { document ->
                    document.toObject(Team::class.java)
                }
                onResult(Result.success(teams))
            }
            .addOnFailureListener { exception ->
                onResult(Result.failure(exception))
            }
    }

    // NUEVO: equipos de una academia en tiempo real, activos E inactivos (CA-06).
    // Devuelve el listener: quien lo use debe llamar a remove() cuando ya no lo necesite.
    fun observeTeamsByAcademy(
        academyId: String,
        onResult: (Result<List<Team>>) -> Unit
    ): ListenerRegistration {
        return firestore
            .collection("teams")
            .whereEqualTo("academyId", academyId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onResult(Result.failure(error))
                    return@addSnapshotListener
                }
                val teams = snapshot?.documents
                    ?.mapNotNull { it.toObject(Team::class.java) }
                    .orEmpty()
                    .sortedBy { it.name.lowercase() }
                onResult(Result.success(teams))
            }
    }

    // Obtener equipos de un entrenador
    fun getTeamsByCoach(
        coachUid: String,
        onResult: (Result<List<Team>>) -> Unit
    ) {
        firestore
            .collection("teams")
            .whereEqualTo("coachUid", coachUid)
            .whereEqualTo("active", true)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val teams = querySnapshot.documents.mapNotNull { document ->
                    document.toObject(Team::class.java)
                }
                onResult(Result.success(teams))
            }
            .addOnFailureListener { exception ->
                onResult(Result.failure(exception))
            }
    }

    // Obtener equipo por ID
    fun getTeamById(
        teamId: String,
        onResult: (Result<Team>) -> Unit
    ) {
        firestore
            .collection("teams")
            .document(teamId)
            .get()
            .addOnSuccessListener { document ->
                val team = document.toObject(Team::class.java)
                if (team != null) {
                    onResult(Result.success(team))
                } else {
                    onResult(Result.failure(TeamNotFoundException()))
                }
            }
            .addOnFailureListener { exception ->
                onResult(Result.failure(exception))
            }
    }

    // Actualizar equipo (misma firma que antes).
    // Ya no usa set(): actualiza solo los campos editables para no perder createdAt/createdBy.
    fun updateTeam(
        team: Team,
        onResult: (Result<Unit>) -> Unit
    ) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onResult(Result.failure(IllegalStateException(SESSION_EXPIRED)))
            return
        }

        val cleanName = normalizeName(team.name)
        validate(cleanName, team.category)?.let {
            onResult(Result.failure(IllegalArgumentException(it)))
            return
        }

        val teamRef = firestore.collection("teams").document(team.id)

        firestore.runTransaction { tx ->
            // En una transacción todas las lecturas van antes de las escrituras
            val current = tx.get(teamRef).toObject(Team::class.java)
                ?: return@runTransaction Outcome.NOT_FOUND

            val oldKey = firestore.collection("team_keys")
                .document(uniqueKey(current.academyId, current.name, current.category))
            val newKey = firestore.collection("team_keys")
                .document(uniqueKey(current.academyId, cleanName, team.category))
            val keyChanged = oldKey.id != newKey.id

            if (keyChanged && tx.get(newKey).exists()) {
                return@runTransaction Outcome.DUPLICATE
            }

            if (keyChanged) {
                tx.delete(oldKey)
                tx.set(newKey, mapOf("teamId" to team.id, "academyId" to current.academyId))
            }

            tx.update(
                teamRef,
                mapOf(
                    "name" to cleanName,
                    "category" to team.category,
                    "coachUid" to team.coachUid,
                    "season" to team.season,
                    "updatedBy" to uid,
                    "updatedAt" to Timestamp.now()
                )
            )
            Outcome.OK
        }
            .addOnSuccessListener { outcome ->
                when (outcome) {
                    Outcome.OK -> onResult(Result.success(Unit))
                    Outcome.DUPLICATE -> onResult(Result.failure(DuplicateTeamException()))
                    Outcome.NOT_FOUND -> onResult(Result.failure(TeamNotFoundException()))
                }
            }
            .addOnFailureListener { exception ->
                onResult(Result.failure(exception))
            }
    }

    // Desactivar equipo
    // No borramos físicamente para conservar historial.
    fun deactivateTeam(
        teamId: String,
        onResult: (Result<Unit>) -> Unit
    ) = setActive(teamId, false, onResult)

    // NUEVO: reactivar un equipo inactivo
    fun activateTeam(
        teamId: String,
        onResult: (Result<Unit>) -> Unit
    ) = setActive(teamId, true, onResult)

    private fun setActive(
        teamId: String,
        active: Boolean,
        onResult: (Result<Unit>) -> Unit
    ) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onResult(Result.failure(IllegalStateException(SESSION_EXPIRED)))
            return
        }

        firestore
            .collection("teams")
            .document(teamId)
            .update(
                mapOf(
                    "active" to active,
                    "updatedBy" to uid,
                    "updatedAt" to Timestamp.now()
                )
            )
            .addOnSuccessListener {
                onResult(Result.success(Unit))
            }
            .addOnFailureListener { exception ->
                onResult(Result.failure(exception))
            }
    }

    // ---------- helpers ----------

    private fun normalizeName(name: String): String =
        name.trim().replace(Regex("\\s+"), " ")

    // Devuelve el mensaje de error, o null si los datos son válidos (CA-02)
    private fun validate(name: String, category: String): String? = when {
        slug(name).isEmpty() -> "El nombre del equipo es obligatorio"
        category.isBlank() -> "La categoría es obligatoria"
        else -> null
    }

    private fun slug(text: String): String =
        Normalizer.normalize(text.trim().lowercase(), Normalizer.Form.NFD)
            .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')

    private fun uniqueKey(academyId: String, name: String, category: String): String =
        "${academyId}_${slug(name)}_${slug(category)}"

    private companion object {
        const val SESSION_EXPIRED = "Tu sesión expiró. Inicia sesión otra vez"
    }
}