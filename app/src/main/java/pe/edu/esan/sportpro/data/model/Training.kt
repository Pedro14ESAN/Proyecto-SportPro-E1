package pe.edu.esan.sportpro.data.model

import com.google.firebase.Timestamp

data class Training(
    val id: String = "",
    val teamId: String = "",
    val academyId: String = "",
    val coachUid: String = "",
    val title: String = "",
    val objective: String = "",
    val date: Timestamp? = null,
    val startTime: String = "",
    val durationMinutes: Long = 0,
    val location: String = "",
    val exerciseIds: List<String> = emptyList(),
    val status: String = STATUS_SCHEDULED,
    val createdAt: Timestamp? = null
) {
    companion object {
        const val STATUS_SCHEDULED = "SCHEDULED"
        const val STATUS_IN_PROGRESS = "IN_PROGRESS"
        const val STATUS_FINISHED = "FINISHED"
        const val STATUS_CANCELLED = "CANCELLED"
    }
}