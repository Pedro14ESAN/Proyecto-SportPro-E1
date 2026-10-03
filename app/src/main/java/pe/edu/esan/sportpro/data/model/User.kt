package pe.edu.esan.sportpro.data.model

import com.google.firebase.Timestamp

data class User(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val role: String = "",
    val academyId: String = "",
    val photoUrl: String = "",
    val active: Boolean = true,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    companion object {
        const val ROLE_ADMIN = "ADM"
        const val ROLE_COACH = "DT"
        const val ROLE_PLAYER = "JUG"
        const val ROLE_PARENT = "PAD"
    }
}