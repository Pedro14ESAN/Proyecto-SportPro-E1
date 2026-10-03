package pe.edu.esan.sportpro.data.model

import com.google.firebase.Timestamp

data class Exercise(
    val id: String = "",
    val academyId: String = "",
    val createdBy: String = "",
    val name: String = "",
    val objective: String = "",
    val description: String = "",
    val durationMinutes: Long = 0,
    val active: Boolean = true,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)