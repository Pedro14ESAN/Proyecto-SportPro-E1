package pe.edu.esan.sportpro.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import pe.edu.esan.sportpro.data.model.Team

class TeamRepository {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Crear equipo
    fun createTeam(
        academyId: String,
        name: String,
        category: String,
        coachUid: String,
        season: String,
        onResult: (Result<Team>) -> Unit
    ) {

        val document = firestore.collection("teams").document()

        val team = Team(
            id = document.id,
            academyId = academyId,
            name = name,
            category = category,
            coachUid = coachUid,
            season = season,
            active = true,
            createdAt = Timestamp.now()
        )

        document
            .set(team)
            .addOnSuccessListener {
                onResult(Result.success(team))
            }
            .addOnFailureListener { exception ->
                onResult(Result.failure(exception))
            }
    }

    // Obtener todos los equipos de una academia
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
                    onResult(
                        Result.failure(
                            Exception("No se encontró el equipo")
                        )
                    )
                }
            }
            .addOnFailureListener { exception ->
                onResult(Result.failure(exception))
            }
    }

    // Actualizar equipo
    fun updateTeam(
        team: Team,
        onResult: (Result<Unit>) -> Unit
    ) {

        firestore
            .collection("teams")
            .document(team.id)
            .set(team)
            .addOnSuccessListener {
                onResult(Result.success(Unit))
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
    ) {

        firestore
            .collection("teams")
            .document(teamId)
            .update("active", false)
            .addOnSuccessListener {
                onResult(Result.success(Unit))
            }
            .addOnFailureListener { exception ->
                onResult(Result.failure(exception))
            }
    }
}