package pe.edu.esan.sportpro.data.model

import com.google.firebase.Timestamp

data class Team(
    val id: String = "",
    val academyId: String = "",
    val name: String = "",
    val category: String = "",
    val coachUid: String = "",
    val season: String = "",
    val active: Boolean = true,
    val createdAt: Timestamp? = null
)