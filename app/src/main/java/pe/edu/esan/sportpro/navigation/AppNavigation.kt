package pe.edu.esan.sportpro.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import pe.edu.esan.sportpro.ui.home.HomeScreen
import com.google.firebase.auth.FirebaseAuth
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Teams : Screen("teams")
    object Players : Screen("players")
    object Trainings : Screen("trainings")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // Verificamos si hay un usuario logueado en Firebase
    val currentUser = FirebaseAuth.getInstance().currentUser

    // Definimos la ruta inicial según el estado del usuario
    val startDestination = if (currentUser != null) {
        Screen.Home.route
    } else {
        Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Login.route) {
            Text("Pantalla de Login")
        }

        composable(Screen.Home.route) {
            val currentUser = FirebaseAuth.getInstance().currentUser

            val userName = currentUser?.displayName
                ?: currentUser?.email?.substringBefore("@")
                ?: "Entrenador"
            val role = "DT / Administrador"

            HomeScreen(
                userName = userName,
                role = role,
                onNavigateToTeams = { navController.navigate(Screen.Teams.route) },
                onNavigateToPlayers = { navController.navigate(Screen.Players.route) },
                onNavigateToTrainings = { navController.navigate(Screen.Trainings.route) },
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Rutas que faltaban registrar:
        composable(Screen.Teams.route) {
            Text("Pantalla de Equipos")
        }

        composable(Screen.Players.route) {
            Text("Pantalla de Jugadores")
        }

        composable(Screen.Trainings.route) {
            Text("Pantalla de Entrenamientos")
        }
    }
}