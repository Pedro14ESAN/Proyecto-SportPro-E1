package pe.edu.esan.sportpro.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import pe.edu.esan.sportpro.ui.auth.AuthViewModel
import pe.edu.esan.sportpro.ui.auth.LoginScreen
import pe.edu.esan.sportpro.ui.auth.RegisterScreen
import pe.edu.esan.sportpro.ui.home.HomeScreen

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
    val authViewModel = remember { AuthViewModel() }

    // Verificamos si hay un usuario logueado en Firebase
    val currentUser = FirebaseAuth.getInstance().currentUser

    // Definimos la ruta inicial según si hay sesión activa
    val initialDestination = if (currentUser != null) {
        Screen.Home.route
    } else {
        Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = initialDestination // Usamos la variable dinámica
    ) {
        // 1. Pantalla de Login (Corregido)
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onGoToRegister = { // <-- Cambiado a onGoToRegister
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        // 2. Pantalla de Registro (Corregido)
        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onGoToLogin = { // <-- Cambiado a onGoToLogin
                    navController.popBackStack()
                }
            )
        }

        // 3. Tu Pantalla de Home
        composable(Screen.Home.route) {
            val user = FirebaseAuth.getInstance().currentUser
            val userName = user?.displayName
                ?: user?.email?.substringBefore("@")
                ?: "Usuario SportPro"
            val role = "DT"

            HomeScreen(
                userName = userName,
                role = role,
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
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // 4. Modulos pendientes de conectar cuando los suban los demás
        composable(Screen.Teams.route) {
            Text("Pantalla de Equipos en construcción")
        }

        composable(Screen.Players.route) {
            Text("Pantalla de Jugadores en construcción")
        }

        composable(Screen.Trainings.route) {
            Text("Pantalla de Entrenamientos en construcción")
        }
    }
}