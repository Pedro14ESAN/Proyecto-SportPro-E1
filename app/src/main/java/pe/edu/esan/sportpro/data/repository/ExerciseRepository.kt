package pe.edu.esan.sportpro.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import pe.edu.esan.sportpro.data.model.Exercise

class ExerciseRepository {

    private val firestore: FirebaseFirestore =
        FirebaseFirestore.getInstance()

    fun createExercise(
        academyId: String,
        createdBy: String,
        name: String,
        objective: String,
        description: String,
        durationMinutes: Long,
        onResult: (Result<Exercise>) -> Unit
    ) {

        val document = firestore
            .collection("exercises")
            .document()

        val exercise = Exercise(
            id = document.id,
            academyId = academyId,
            createdBy = createdBy,
            name = name,
            objective = objective,
            description = description,
            durationMinutes = durationMinutes,
            active = true,
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now()
        )

        document
            .set(exercise)
            .addOnSuccessListener {
                onResult(Result.success(exercise))
            }
            .addOnFailureListener { exception ->
                onResult(Result.failure(exception))
            }
    }

    fun getExercisesByAcademy(
        academyId: String,
        onResult: (Result<List<Exercise>>) -> Unit
    ) {

        firestore
            .collection("exercises")
            .whereEqualTo("academyId", academyId)
            .get()
            .addOnSuccessListener { querySnapshot ->

                val exercises =
                    querySnapshot.documents.mapNotNull { document ->
                        document.toObject(Exercise::class.java)
                    }

                onResult(Result.success(exercises))
            }
            .addOnFailureListener { exception ->
                onResult(Result.failure(exception))
            }
    }

    fun updateExercise(
        exercise: Exercise,
        onResult: (Result<Unit>) -> Unit
    ) {

        val updatedExercise = exercise.copy(
            updatedAt = Timestamp.now()
        )

        firestore
            .collection("exercises")
            .document(exercise.id)
            .set(updatedExercise)
            .addOnSuccessListener {
                onResult(Result.success(Unit))
            }
            .addOnFailureListener { exception ->
                onResult(Result.failure(exception))
            }
    }

    fun updateExerciseStatus(
        exerciseId: String,
        active: Boolean,
        onResult: (Result<Unit>) -> Unit
    ) {

        firestore
            .collection("exercises")
            .document(exerciseId)
            .update(
                mapOf(
                    "active" to active,
                    "updatedAt" to Timestamp.now()
                )
            )
            .addOnSuccessListener {
                onResult(Result.success(Unit))
            }
            .addOnFailureListener { exception ->
                onResult(Result.failure(exception))
            }
    }
}