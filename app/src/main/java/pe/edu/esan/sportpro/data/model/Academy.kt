package pe.edu.esan.sportpro.data.model

import com.google.firebase.Timestamp

data class Academy(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val logoUrl: String = "",
    val administratorUid: String = "",
    val address: String = "",
    val active: Boolean = true,
    val createdAt: Timestamp? = null
)