package pe.edu.esan.sportpro.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import pe.edu.esan.sportpro.data.model.Match
import pe.edu.esan.sportpro.data.model.MatchEvent
// repositorio de partidos
// crear partidos, obtener por ID, escuchar cambios en tiempo real,
class MatchRepository {

    private val firestore: FirebaseFirestore =
        FirebaseFirestore.getInstance()

    // =========================================================
    // CREAR PARTIDO
    // =========================================================

    fun createMatch(
        academyId: String,
        teamId: String,
        rivalName: String,
        date: Timestamp,
        startTime: String,
        location: String,
        createdBy: String,
        onResult: (Result<Match>) -> Unit
    ) {

        val document =
            firestore.collection("matches").document()

        val match = Match(
            id = document.id,
            academyId = academyId,
            teamId = teamId,
            rivalName = rivalName,
            date = date,
            startTime = startTime,
            location = location,
            status = Match.STATUS_SCHEDULED,
            homeScore = 0,
            awayScore = 0,
            createdBy = createdBy,
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now()
        )

        document
            .set(match)
            .addOnSuccessListener {
                onResult(
                    Result.success(match)
                )
            }
            .addOnFailureListener { exception ->

                onResult(
                    Result.failure(exception)
                )
            }
    }

    // =========================================================
    // OBTENER PARTIDO POR ID
    // =========================================================

    fun getMatchById(
        matchId: String,
        onResult: (Result<Match>) -> Unit
    ) {

        firestore
            .collection("matches")
            .document(matchId)
            .get()
            .addOnSuccessListener { document ->

                val match =
                    document.toObject(
                        Match::class.java
                    )

                if (match != null) {

                    onResult(
                        Result.success(match)
                    )

                } else {

                    onResult(
                        Result.failure(
                            Exception(
                                "No se encontró el partido"
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
    // ESCUCHAR PARTIDO EN TIEMPO REAL
    // =========================================================

    fun listenMatch(
        matchId: String,
        onResult: (Result<Match>) -> Unit
    ): ListenerRegistration {

        return firestore
            .collection("matches")
            .document(matchId)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {

                    onResult(
                        Result.failure(error)
                    )

                    return@addSnapshotListener
                }

                val match =
                    snapshot?.toObject(
                        Match::class.java
                    )

                if (match != null) {

                    onResult(
                        Result.success(match)
                    )
                }
            }
    }

    // =========================================================
    // CAMBIAR ESTADO DEL PARTIDO
    // =========================================================

    fun updateMatchStatus(
        matchId: String,
        status: String,
        onResult: (Result<Unit>) -> Unit
    ) {

        firestore
            .collection("matches")
            .document(matchId)
            .update(
                mapOf(
                    "status" to status,
                    "updatedAt" to Timestamp.now()
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
    // ACTUALIZAR MARCADOR
    // =========================================================

    fun updateScore(
        matchId: String,
        homeScore: Long,
        awayScore: Long,
        onResult: (Result<Unit>) -> Unit
    ) {

        firestore
            .collection("matches")
            .document(matchId)
            .update(
                mapOf(
                    "homeScore" to homeScore,
                    "awayScore" to awayScore,
                    "updatedAt" to Timestamp.now()
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
    // REGISTRAR EVENTO
    // =========================================================

    fun addEvent(
        matchId: String,
        event: MatchEvent,
        onResult: (Result<MatchEvent>) -> Unit
    ) {

        val eventDocument =
            firestore
                .collection("matches")
                .document(matchId)
                .collection("events")
                .document()

        val newEvent = event.copy(
            id = eventDocument.id,
            matchId = matchId,
            status = MatchEvent.STATUS_ACTIVE,
            createdAt = Timestamp.now(),
            syncStatus = MatchEvent.SYNC_PENDING
        )

        eventDocument
            .set(newEvent)
            .addOnSuccessListener {

                eventDocument
                    .update(
                        "syncStatus",
                        MatchEvent.SYNCED
                    )
                    .addOnSuccessListener {

                        val syncedEvent =
                            newEvent.copy(
                                syncStatus =
                                    MatchEvent.SYNCED
                            )

                        onResult(
                            Result.success(
                                syncedEvent
                            )
                        )
                    }
                    .addOnFailureListener { exception ->

                        onResult(
                            Result.failure(
                                exception
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
    // ESCUCHAR EVENTOS EN TIEMPO REAL
    // =========================================================

    fun listenEvents(
        matchId: String,
        onResult: (Result<List<MatchEvent>>) -> Unit
    ): ListenerRegistration {

        return firestore
            .collection("matches")
            .document(matchId)
            .collection("events")
            .orderBy("minute")
            .addSnapshotListener { snapshot, error ->

                if (error != null) {

                    onResult(
                        Result.failure(error)
                    )

                    return@addSnapshotListener
                }

                val events =
                    snapshot
                        ?.documents
                        ?.mapNotNull { document ->

                            document.toObject(
                                MatchEvent::class.java
                            )
                        }
                        ?: emptyList()

                onResult(
                    Result.success(events)
                )
            }
    }

    // =========================================================
    // ACTUALIZAR EVENTO
    // =========================================================

    fun updateEvent(
        matchId: String,
        event: MatchEvent,
        updatedBy: String,
        onResult: (Result<Unit>) -> Unit
    ) {

        val updatedEvent =
            event.copy(
                updatedBy = updatedBy,
                updatedAt = Timestamp.now()
            )

        firestore
            .collection("matches")
            .document(matchId)
            .collection("events")
            .document(event.id)
            .set(updatedEvent)
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
    // ANULAR EVENTO
    // NO SE ELIMINA FÍSICAMENTE
    // =========================================================

    fun annulEvent(
        matchId: String,
        eventId: String,
        annulledBy: String,
        onResult: (Result<Unit>) -> Unit
    ) {

        firestore
            .collection("matches")
            .document(matchId)
            .collection("events")
            .document(eventId)
            .update(
                mapOf(
                    "status" to
                            MatchEvent.STATUS_ANNULLED,

                    "annulledBy" to
                            annulledBy,

                    "annulledAt" to
                            Timestamp.now(),

                    "updatedBy" to
                            annulledBy,

                    "updatedAt" to
                            Timestamp.now()
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