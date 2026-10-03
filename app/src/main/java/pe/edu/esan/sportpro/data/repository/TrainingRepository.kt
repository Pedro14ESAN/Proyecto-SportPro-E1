package pe.edu.esan.sportpro.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import pe.edu.esan.sportpro.data.model.Training

class TrainingRepository {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Crear entrenamiento
    fun createTraining(
        teamId: String,
        academyId: String,
        coachUid: String,
        title: String,
        objective: String,
        date: Timestamp,
        startTime: String,
        durationMinutes: Long,
        location: String,
        onResult: (Result<Training>) -> Unit
    ) {

        val document = firestore.collection("trainings").document()

        val training = Training(
            id = document.id,
            teamId = teamId,
            academyId = academyId,
            coachUid = coachUid,
            title = title,
            objective = objective,
            date = date,
            startTime = startTime,
            durationMinutes = durationMinutes,
            location = location,
            status = Training.STATUS_SCHEDULED,
            createdAt = Timestamp.now()
        )

        document
            .set(training)
            .addOnSuccessListener {
                onResult(Result.success(training))
            }
            .addOnFailureListener { exception ->
                onResult(Result.failure(exception))
            }
    }

    // Obtener entrenamientos de un equipo
    fun getTrainingsByTeam(
        teamId: String,
        onResult: (Result<List<Training>>) -> Unit
    ) {

        firestore
            .collection("trainings")
            .whereEqualTo("teamId", teamId)
            .get()
            .addOnSuccessListener { querySnapshot ->

                val trainings = querySnapshot.documents.mapNotNull { document ->
                    document.toObject(Training::class.java)
                }

                onResult(Result.success(trainings))
            }
            .addOnFailureListener { exception ->
                onResult(Result.failure(exception))
            }
    }

    // Obtener entrenamientos creados por un entrenador
    fun getTrainingsByCoach(
        coachUid: String,
        onResult: (Result<List<Training>>) -> Unit
    ) {

        firestore
            .collection("trainings")
            .whereEqualTo("coachUid", coachUid)
            .get()
            .addOnSuccessListener { querySnapshot ->

                val trainings = querySnapshot.documents.mapNotNull { document ->
                    document.toObject(Training::class.java)
                }

                onResult(Result.success(trainings))
            }
            .addOnFailureListener { exception ->
                onResult(Result.failure(exception))
            }
    }

    // Obtener entrenamiento por ID
    fun getTrainingById(
        trainingId: String,
        onResult: (Result<Training>) -> Unit
    ) {

        firestore
            .collection("trainings")
            .document(trainingId)
            .get()
            .addOnSuccessListener { document ->

                val training = document.toObject(Training::class.java)

                if (training != null) {
                    onResult(Result.success(training))
                } else {
                    onResult(
                        Result.failure(
                            Exception("No se encontró el entrenamiento")
                        )
                    )
                }
            }
            .addOnFailureListener { exception ->
                onResult(Result.failure(exception))
            }
    }

    // Actualizar entrenamiento
    fun updateTraining(
        training: Training,
        onResult: (Result<Unit>) -> Unit
    ) {

        firestore
            .collection("trainings")
            .document(training.id)
            .set(training)
            .addOnSuccessListener {
                onResult(Result.success(Unit))
            }
            .addOnFailureListener { exception ->
                onResult(Result.failure(exception))
            }
    }

    // Cambiar estado del entrenamiento
    fun updateTrainingStatus(
        trainingId: String,
        status: String,
        onResult: (Result<Unit>) -> Unit
    ) {

        firestore
            .collection("trainings")
            .document(trainingId)
            .update("status", status)
            .addOnSuccessListener {
                onResult(Result.success(Unit))
            }
            .addOnFailureListener { exception ->
                onResult(Result.failure(exception))
            }
    }

    // Cancelar entrenamiento
    fun cancelTraining(
        trainingId: String,
        onResult: (Result<Unit>) -> Unit
    ) {

        updateTrainingStatus(
            trainingId = trainingId,
            status = Training.STATUS_CANCELLED,
            onResult = onResult
        )
    }
}