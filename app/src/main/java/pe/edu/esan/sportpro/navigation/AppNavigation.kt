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

import androidx.compose.runtime.remember
import pe.edu.esan.sportpro.ui.auth.AuthViewModel
import pe.edu.esan.sportpro.ui.auth.LoginScreen
import pe.edu.esan.sportpro.ui.auth.RegisterScreen

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
    val authViewModel = remember {
        AuthViewModel()
    }

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
            Text("Pantalla de Login asignada al INTEGRANTE 2")
        }

        composable(Screen.Register.route) {
            Text("Pantalla de Registro asignada al INTEGRANTE 2")
        }

        composable(Screen.Home.route) {
            val currentUser = FirebaseAuth.getInstance().currentUser

            val userName = currentUser?.displayName
                ?: currentUser?.email?.substringBefore("@")
                ?: "Entrenador"
            val role = "DT / Administrador"

            HomeScreen(
                userName = "Usuario SportPro",
                role = "DT",

                onNavigateToTeams = {
                    navController.navigate(Screen.Teams.route)
                },

                onNavigateToPlayers = {
                    navController.navigate(Screen.Players.route)
                },

                onNavigateToTrainings = {
                    navController.navigate(Screen.Trainings.route)
                },

                onLogout = {
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