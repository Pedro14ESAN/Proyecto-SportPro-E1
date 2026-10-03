package pe.edu.esan.sportpro.data.model

import com.google.firebase.Timestamp
//partido
data class Match(
    val id: String = "",
    val academyId: String = "",
    val teamId: String = "",
    val rivalName: String = "",
    val date: Timestamp? = null,
    val startTime: String = "",
    val location: String = "",
    val status: String = STATUS_SCHEDULED,
    val homeScore: Long = 0,
    val awayScore: Long = 0,
    val createdBy: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {

    companion object {

        const val STATUS_SCHEDULED = "SCHEDULED"
        const val STATUS_IN_PROGRESS = "IN_PROGRESS"
        const val STATUS_HALFTIME = "HALFTIME"
        const val STATUS_FINISHED = "FINISHED"
        const val STATUS_CANCELLED = "CANCELLED"
    }
}