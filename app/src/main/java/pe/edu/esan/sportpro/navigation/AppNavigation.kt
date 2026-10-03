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
import pe.edu.esan.sportpro.data.model.Exercise
import pe.edu.esan.sportpro.data.repository.ExerciseRepository
import pe.edu.esan.sportpro.ui.training.CreateExerciseScreen
import pe.edu.esan.sportpro.ui.training.CreateTrainingScreen

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

            TrainingScreen(
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

            ExerciseLibraryScreen(

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

            val context = LocalContext.current

            val exerciseRepository = remember {
                ExerciseRepository()
            }

            CreateExerciseScreen(

                onBack = {
                    navController.popBackStack()
                },

                onExerciseCreated = {
                        name,
                        objective,
                        description,
                        durationMinutes ->

                    val currentUser =
                        FirebaseAuth.getInstance().currentUser

                    if (currentUser == null) {

                        Toast.makeText(
                            context,
                            "Debes iniciar sesión para crear ejercicios",
                            Toast.LENGTH_SHORT
                        ).show()

                    } else {

                        exerciseRepository.createExercise(
                            academyId = "club_deportivo_norte",
                            createdBy = currentUser.uid,
                            name = name,
                            objective = objective,
                            description = description,
                            durationMinutes = durationMinutes
                        ) { result ->

                            result
                                .onSuccess {

                                    Toast.makeText(
                                        context,
                                        "Ejercicio creado correctamente",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    navController.popBackStack()
                                }
                                .onFailure { exception ->

                                    Toast.makeText(
                                        context,
                                        "Error: ${exception.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
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

            var exercises by remember {
                mutableStateOf<List<Exercise>>(emptyList())
            }

            var isLoadingExercises by remember {
                mutableStateOf(true)
            }

            var exerciseError by remember {
                mutableStateOf<String?>(null)
            }

            LaunchedEffect(Unit) {

                exerciseRepository.getExercisesByAcademy(
                    academyId = "club_deportivo_norte"
                ) { result ->

                    result
                        .onSuccess { firebaseExercises ->

                            exercises = firebaseExercises.filter {
                                it.active
                            }

                            isLoadingExercises = false
                            exerciseError = null
                        }
                        .onFailure { exception ->

                            isLoadingExercises = false

                            exerciseError =
                                "No se pudieron cargar los ejercicios: ${exception.message}"
                        }
                }
            }

            CreateTrainingScreen(
                availableExercises = exercises,
                isLoadingExercises = isLoadingExercises,
                exerciseError = exerciseError,

                onBack = {
                    navController.popBackStack()
                },

                onTrainingCreated = {

                    Toast.makeText(
                        appContext,
                        "Entrenamiento creado correctamente",
                        Toast.LENGTH_SHORT
                    ).show()

                    navController.popBackStack()
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