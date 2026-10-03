package pe.edu.esan.sportpro.data.model

import com.google.firebase.Timestamp

data class Attendance(
    val id: String = "",
    val trainingId: String = "",
    val playerId: String = "",
    val status: String = "",
    val markedBy: String = "",
    val markedAt: Timestamp? = null,
    val observation: String = ""
) {
    companion object {
        const val STATUS_PRESENT = "PRESENT"
        const val STATUS_ABSENT = "ABSENT"
        const val STATUS_JUSTIFIED = "JUSTIFIED"
    }
}