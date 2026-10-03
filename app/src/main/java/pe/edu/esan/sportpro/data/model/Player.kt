package pe.edu.esan.sportpro.data.model

import com.google.firebase.Timestamp

data class Player(
    val id: String = "",
    val userId: String = "",
    val academyId: String = "",
    val teamId: String = "",
    val fullName: String = "",
    val position: String = "",
    val number: Long = 0,
    val photoUrl: String = "",
    val birthDate: Timestamp? = null,
    val active: Boolean = true
) {
    companion object {
        const val POSITION_GOALKEEPER = "POR"
        const val POSITION_DEFENDER = "DEF"
        const val POSITION_MIDFIELDER = "MED"
        const val POSITION_FORWARD = "DEL"
    }
}