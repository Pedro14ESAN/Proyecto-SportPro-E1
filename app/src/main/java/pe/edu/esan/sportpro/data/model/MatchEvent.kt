package pe.edu.esan.sportpro.data.model

import com.google.firebase.Timestamp
//evento del partido en tiempo real
data class MatchEvent(
    val id: String = "",
    val matchId: String = "",
    val type: String = "",
    val minute: Long = 0,
    val teamId: String = "",
    val playerId: String = "",
    val relatedPlayerId: String = "", // Jugador a cargo (sustitucion jug10 por jug6)
    val observation: String = "",
    val status: String = STATUS_ACTIVE,
    val createdBy: String = "",
    val createdAt: Timestamp? = null,
    val updatedBy: String = "",
    val updatedAt: Timestamp? = null,
    val annulledBy: String = "",
    val annulledAt: Timestamp? = null,
    val syncStatus: String = SYNCED // Estado de sincronización
    // nos ayuda a diseñar la parte de pérdida temporal de conexión
) {

    companion object {

        // Tipos de evento
        const val TYPE_MATCH_START = "MATCH_START"
        const val TYPE_MATCH_END = "MATCH_END"

        const val TYPE_GOAL = "GOAL"
        const val TYPE_FOUL = "FOUL"

        const val TYPE_YELLOW_CARD = "YELLOW_CARD"
        const val TYPE_RED_CARD = "RED_CARD"

        const val TYPE_PENALTY = "PENALTY"
        const val TYPE_CORNER = "CORNER"

        const val TYPE_THROW_IN = "THROW_IN"
        const val TYPE_GOAL_KICK = "GOAL_KICK"

        const val TYPE_OFFSIDE = "OFFSIDE"
        const val TYPE_SUBSTITUTION = "SUBSTITUTION"

        // Estado lógico del evento
        const val STATUS_ACTIVE = "ACTIVE"
        const val STATUS_ANNULLED = "ANNULLED"

        // Estado de sincronización
        const val SYNC_PENDING = "PENDING"
        const val SYNCED = "SYNCED"
        const val SYNC_ERROR = "ERROR"
    }
}