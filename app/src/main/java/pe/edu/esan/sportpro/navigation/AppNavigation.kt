package pe.edu.esan.sportpro.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import pe.edu.esan.sportpro.ui.home.HomeScreen
import pe.edu.esan.sportpro.ui.training.TrainingScreen
import pe.edu.esan.sportpro.ui.training.ExerciseLibraryScreen
import android.widget.Toast
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import pe.edu.esan.sportpro.data.model.Exercise
import pe.edu.esan.sportpro.data.model.Training
import pe.edu.esan.sportpro.data.repository.ExerciseRepository
import pe.edu.esan.sportpro.data.repository.TrainingRepository
import pe.edu.esan.sportpro.ui.training.CreateExerciseScreen
import pe.edu.esan.sportpro.ui.training.CreateTrainingScreen
import java.text.SimpleDateFormat
import java.util.Locale
import com.google.firebase.Timestamp

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Teams : Screen("teams")
    object Players : Screen("players")
    object Trainings : Screen("trainings")
    object ExerciseLibrary : Screen("exercise_library")
    object CreateExercise : Screen("create_exercise")
    object CreateTraining : Screen("create_training")
    object TrainingDetail : Screen("training_detail")
    object Attendance : Screen("attendance")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Login.route) {
            Text("Pantalla de Login asignado al INTEGRANTE 2")
        }

        composable(Screen.Register.route) {
            Text("Pantalla de Registro asigndado al INTEGRANTE 2")
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToTeams = { navController.navigate(Screen.Teams.route) },
                onNavigateToPlayers = { navController.navigate(Screen.Players.route) },
                onNavigateToTrainings = { navController.navigate(Screen.Trainings.route) },
                onLogout = { navController.navigate(Screen.Login.route) }
            )
        }

        composable(Screen.Teams.route) {
            Text("Pantalla de Equipos (Asignada al Compañero 4)")
        }

        composable(Screen.Players.route) {
            Text("Pantalla de Jugadores (Asignada al Compañero 5)")
        }

        composable(Screen.Trainings.route) {

            val trainingRepository = remember {
                TrainingRepository()
            }

            val currentUser =
                FirebaseAuth
                    .getInstance()
                    .currentUser

            var trainings by remember {
                mutableStateOf<List<Training>>(
                    emptyList()
                )
            }

            var isLoadingTrainings by remember {
                mutableStateOf(true)
            }

            var trainingError by remember {
                mutableStateOf<String?>(null)
            }


            LaunchedEffect(currentUser?.uid) {

                if (currentUser == null) {

                    isLoadingTrainings = false

                    trainingError =
                        "No existe una sesión activa."

                } else {

                    trainingRepository
                        .getTrainingsByCoach(
                            coachUid =
                                currentUser.uid
                        ) { result ->

                            result.onSuccess {
                                    firebaseTrainings ->

                                trainings =
                                    firebaseTrainings
                                        .sortedByDescending {
                                            it.date
                                                ?.seconds
                                                ?: 0
                                        }

                                isLoadingTrainings =
                                    false

                                trainingError =
                                    null
                            }


                            result.onFailure {
                                    exception ->

                                trainings =
                                    emptyList()

                                isLoadingTrainings =
                                    false

                                trainingError =
                                    "No se pudieron cargar los entrenamientos: ${exception.message}"
                            }
                        }
                }
            }


            TrainingScreen(

                trainings = trainings,

                isLoading =
                    isLoadingTrainings,

                errorMessage =
                    trainingError,

                onBack = {
                    navController.popBackStack()
                },

                onNavigateToExerciseLibrary = {

                    navController.navigate(
                        Screen.ExerciseLibrary.route
                    )
                },

                onCreateTraining = {

                    navController.navigate(
                        Screen.CreateTraining.route
                    )
                }
            )
        }

        composable(Screen.ExerciseLibrary.route) {

            val exerciseRepository = remember {
                ExerciseRepository()
            }

            val currentUser =
                FirebaseAuth
                    .getInstance()
                    .currentUser

            var exercises by remember {
                mutableStateOf<List<Exercise>>(
                    emptyList()
                )
            }

            var isLoading by remember {
                mutableStateOf(true)
            }

            var errorMessage by remember {
                mutableStateOf<String?>(null)
            }


            LaunchedEffect(currentUser?.uid) {

                if (currentUser == null) {

                    isLoading = false

                    errorMessage =
                        "No existe una sesión activa."

                } else {

                    FirebaseFirestore
                        .getInstance()
                        .collection("users")
                        .document(currentUser.uid)
                        .get()

                        .addOnSuccessListener { userDocument ->

                            val academyId =
                                userDocument
                                    .getString("academyId")

                            if (
                                academyId
                                    .isNullOrBlank()
                            ) {

                                isLoading = false

                                errorMessage =
                                    "Tu usuario no tiene una academia asignada."

                            } else {

                                exerciseRepository
                                    .getExercisesByAcademy(
                                        academyId = academyId
                                    ) { result ->

                                        result.onSuccess {
                                                firebaseExercises ->

                                            exercises =
                                                firebaseExercises
                                                    .sortedBy {
                                                        it.name
                                                    }

                                            isLoading =
                                                false

                                            errorMessage =
                                                null
                                        }

                                        result.onFailure {
                                                exception ->

                                            exercises =
                                                emptyList()

                                            isLoading =
                                                false

                                            errorMessage =
                                                "No se pudieron cargar los ejercicios: ${exception.message}"
                                        }
                                    }
                            }
                        }

                        .addOnFailureListener {
                                exception ->

                            isLoading = false

                            errorMessage =
                                "No se pudo obtener tu academia: ${exception.message}"
                        }
                }
            }


            ExerciseLibraryScreen(

                exercises = exercises,

                isLoading = isLoading,

                errorMessage = errorMessage,

                onBack = {

                    navController.popBackStack()
                },

                onCreateExercise = {

                    navController.navigate(
                        Screen.CreateExercise.route
                    )
                }
            )
        }

        composable(Screen.CreateExercise.route) {

            val appContext =
                LocalContext.current

            val exerciseRepository =
                remember {
                    ExerciseRepository()
                }

            CreateExerciseScreen(

                onBack = {

                    navController
                        .popBackStack()
                },

                onExerciseCreated = {
                        name,
                        objective,
                        description,
                        durationMinutes ->

                    val currentUser =
                        FirebaseAuth
                            .getInstance()
                            .currentUser


                    if (currentUser == null) {

                        Toast.makeText(
                            appContext,
                            "Debes iniciar sesión.",
                            Toast.LENGTH_SHORT
                        ).show()

                    } else {

                        FirebaseFirestore
                            .getInstance()
                            .collection("users")
                            .document(currentUser.uid)
                            .get()

                            .addOnSuccessListener {
                                    document ->

                                val academyId =
                                    document
                                        .getString(
                                            "academyId"
                                        )

                                if (
                                    academyId
                                        .isNullOrBlank()
                                ) {

                                    Toast.makeText(
                                        appContext,
                                        "El usuario no tiene una academia asignada.",
                                        Toast.LENGTH_LONG
                                    ).show()

                                } else {

                                    exerciseRepository
                                        .createExercise(

                                            academyId =
                                                academyId,

                                            createdBy =
                                                currentUser.uid,

                                            name =
                                                name,

                                            objective =
                                                objective,

                                            description =
                                                description,

                                            durationMinutes =
                                                durationMinutes

                                        ) { result ->

                                            result
                                                .onSuccess {

                                                    Toast.makeText(
                                                        appContext,
                                                        "Ejercicio guardado en Firebase",
                                                        Toast.LENGTH_SHORT
                                                    ).show()

                                                    navController
                                                        .popBackStack()
                                                }

                                            result
                                                .onFailure {
                                                        exception ->

                                                    Toast.makeText(
                                                        appContext,
                                                        "Error: ${exception.message}",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                        }
                                }
                            }

                            .addOnFailureListener {
                                    exception ->

                                Toast.makeText(
                                    appContext,
                                    "No se pudo obtener la academia: ${exception.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }
                }
            )
        }

        composable(Screen.CreateTraining.route) {

            val appContext = LocalContext.current

            val exerciseRepository = remember {
                ExerciseRepository()
            }

            val trainingRepository = remember {
                TrainingRepository()
            }

            val currentUser =
                FirebaseAuth
                    .getInstance()
                    .currentUser

            var exercises by remember {
                mutableStateOf<List<Exercise>>(
                    emptyList()
                )
            }

            var isLoadingExercises by remember {
                mutableStateOf(true)
            }

            var exerciseError by remember {
                mutableStateOf<String?>(null)
            }

            var academyId by remember {
                mutableStateOf<String?>(null)
            }


            LaunchedEffect(currentUser?.uid) {

                if (currentUser == null) {

                    isLoadingExercises = false

                    exerciseError =
                        "No existe una sesión activa."

                } else {

                    FirebaseFirestore
                        .getInstance()
                        .collection("users")
                        .document(currentUser.uid)
                        .get()

                        .addOnSuccessListener { userDocument ->

                            val userAcademyId =
                                userDocument
                                    .getString("academyId")

                            if (userAcademyId.isNullOrBlank()) {

                                isLoadingExercises = false

                                exerciseError =
                                    "Tu usuario no tiene una academia asignada."

                            } else {

                                academyId =
                                    userAcademyId

                                exerciseRepository
                                    .getExercisesByAcademy(
                                        academyId =
                                            userAcademyId
                                    ) { result ->

                                        result.onSuccess {
                                                firebaseExercises ->

                                            exercises =
                                                firebaseExercises
                                                    .filter {
                                                        it.active
                                                    }
                                                    .sortedBy {
                                                        it.name
                                                    }

                                            isLoadingExercises =
                                                false

                                            exerciseError =
                                                null
                                        }

                                        result.onFailure {
                                                exception ->

                                            exercises =
                                                emptyList()

                                            isLoadingExercises =
                                                false

                                            exerciseError =
                                                "No se pudieron cargar los ejercicios: ${exception.message}"
                                        }
                                    }
                            }
                        }

                        .addOnFailureListener {
                                exception ->

                            isLoadingExercises =
                                false

                            exerciseError =
                                "No se pudo obtener tu academia: ${exception.message}"
                        }
                }
            }


            CreateTrainingScreen(

                availableExercises =
                    exercises,

                isLoadingExercises =
                    isLoadingExercises,

                exerciseError =
                    exerciseError,

                onBack = {

                    navController
                        .popBackStack()
                },

                onTrainingCreated = {
                        team,
                        date,
                        time,
                        objective,
                        durationMinutes,
                        exerciseIds ->

                    val user =
                        FirebaseAuth
                            .getInstance()
                            .currentUser

                    val currentAcademy =
                        academyId


                    if (user == null) {

                        Toast.makeText(
                            appContext,
                            "No existe una sesión activa.",
                            Toast.LENGTH_LONG
                        ).show()

                    } else if (
                        currentAcademy.isNullOrBlank()
                    ) {

                        Toast.makeText(
                            appContext,
                            "No tienes una academia asignada.",
                            Toast.LENGTH_LONG
                        ).show()

                    } else {

                        try {

                            val formatter =
                                SimpleDateFormat(
                                    "dd/MM/yyyy HH:mm",
                                    Locale.getDefault()
                                ).apply {
                                    isLenient = false
                                }

                            val parsedDate =
                                formatter.parse(
                                    "$date $time"
                                )

                            if (parsedDate == null) {

                                Toast.makeText(
                                    appContext,
                                    "Fecha u hora inválida.",
                                    Toast.LENGTH_LONG
                                ).show()

                            } else {

                                val firebaseDate =
                                    Timestamp(parsedDate)

                                trainingRepository
                                    .createTraining(

                                        teamId = team,

                                        academyId =
                                            currentAcademy,

                                        coachUid =
                                            user.uid,

                                        title =
                                            "Entrenamiento - $team",

                                        objective =
                                            objective,

                                        date =
                                            firebaseDate,

                                        startTime =
                                            time,

                                        durationMinutes =
                                            durationMinutes,

                                        location = "",

                                        exerciseIds =
                                            exerciseIds

                                    ) { result ->

                                        result.onSuccess {

                                            Toast.makeText(
                                                appContext,
                                                "Entrenamiento guardado en Firebase",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            navController
                                                .popBackStack()
                                        }

                                        result.onFailure {
                                                exception ->

                                            Toast.makeText(
                                                appContext,
                                                "Error al guardar: ${exception.message}",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                            }

                        } catch (
                            exception: Exception
                        ) {

                            Toast.makeText(
                                appContext,
                                "Usa la fecha dd/MM/yyyy y hora HH:mm",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            )
        }

        composable(Screen.TrainingDetail.route) {
            Text("Detalle del entrenamiento - Bruno")
        }

        composable(Screen.Attendance.route) {
            Text("Registro de asistencia - Bruno")
        }
    }
}