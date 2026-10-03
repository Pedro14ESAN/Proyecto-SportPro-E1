package pe.edu.esan.sportpro.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import pe.edu.esan.sportpro.ui.home.HomeScreen
import pe.edu.esan.sportpro.ui.training.TrainingScreen

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
            Text("Biblioteca de ejercicios - Bruno")
        }

        composable(Screen.CreateExercise.route) {
            Text("Crear ejercicio - Bruno")
        }

        composable(Screen.CreateTraining.route) {
            Text("Crear entrenamiento - Bruno")
        }

        composable(Screen.TrainingDetail.route) {
            Text("Detalle del entrenamiento - Bruno")
        }

        composable(Screen.Attendance.route) {
            Text("Registro de asistencia - Bruno")
        }
    }
}