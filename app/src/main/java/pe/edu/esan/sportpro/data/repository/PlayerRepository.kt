package pe.edu.esan.sportpro.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import pe.edu.esan.sportpro.data.model.Player

class PlayerRepository {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Crear jugador
    fun createPlayer(
        player: Player,
        onResult: (Result<Player>) -> Unit
    ) {

        val document = firestore.collection("players").document()

        val newPlayer = player.copy(
            id = document.id
        )

        document
            .set(newPlayer)
            .addOnSuccessListener {
                onResult(Result.success(newPlayer))
            }
            .addOnFailureListener { exception ->
                onResult(Result.failure(exception))
            }
    }

    // Obtener jugadores de una academia
    fun getPlayersByAcademy(
        academyId: String,
        onResult: (Result<List<Player>>) -> Unit
    ) {

        firestore
            .collection("players")
            .whereEqualTo("academyId", academyId)
            .whereEqualTo("active", true)
            .get()
            .addOnSuccessListener { querySnapshot ->

                val players = querySnapshot.documents.mapNotNull { document ->
                    document.toObject(Player::class.java)
                }

                onResult(Result.success(players))
            }
            .addOnFailureListener { exception ->
                onResult(Result.failure(exception))
            }
    }

    // Obtener jugadores de un equipo
    fun getPlayersByTeam(
        teamId: String,
        onResult: (Result<List<Player>>) -> Unit
    ) {

        firestore
            .collection("players")
            .whereEqualTo("teamId", teamId)
            .whereEqualTo("active", true)
            .get()
            .addOnSuccessListener { querySnapshot ->

                val players = querySnapshot.documents.mapNotNull { document ->
                    document.toObject(Player::class.java)
                }

                onResult(Result.success(players))
            }
            .addOnFailureListener { exception ->
                onResult(Result.failure(exception))
            }
    }

    // Obtener jugador por ID
    fun getPlayerById(
        playerId: String,
        onResult: (Result<Player>) -> Unit
    ) {

        firestore
            .collection("players")
            .document(playerId)
            .get()
            .addOnSuccessListener { document ->

                val player = document.toObject(Player::class.java)

                if (player != null) {
                    onResult(Result.success(player))
                } else {
                    onResult(
                        Result.failure(
                            Exception("No se encontró el jugador")
                        )
                    )
                }
            }
            .addOnFailureListener { exception ->
                onResult(Result.failure(exception))
            }
    }

    // Actualizar jugador
    fun updatePlayer(
        player: Player,
        onResult: (Result<Unit>) -> Unit
    ) {

        firestore
            .collection("players")
            .document(player.id)
            .set(player)
            .addOnSuccessListener {
                onResult(Result.success(Unit))
            }
            .addOnFailureListener { exception ->
                onResult(Result.failure(exception))
            }
    }

    // Desactivar jugador
    fun deactivatePlayer(
        playerId: String,
        onResult: (Result<Unit>) -> Unit
    ) {

        firestore
            .collection("players")
            .document(playerId)
            .update("active", false)
            .addOnSuccessListener {
                onResult(Result.success(Unit))
            }
            .addOnFailureListener { exception ->
                onResult(Result.failure(exception))
            }
    }
}