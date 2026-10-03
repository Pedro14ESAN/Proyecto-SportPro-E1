package pe.edu.esan.sportpro.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import pe.edu.esan.sportpro.ui.home.HomeScreen

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

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {

        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) {
                            inclusive = true
                        }
                    }
                },
                onGoToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = {
                    authViewModel.logout()

                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) {
                            inclusive = true
                        }
                    }
                },
                onGoToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Home.route) {

            HomeScreen(
                userName = authViewModel.currentUser?.fullName
                    ?: "Usuario SportPro",
                role = authViewModel.currentUser?.role
                    ?: "",

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
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(Screen.Teams.route) {
            Text("Pantalla de Equipos - Integrante 4")
        }

        composable(Screen.Players.route) {
            Text("Pantalla de Jugadores - Integrante 5")
        }

        composable(Screen.Trainings.route) {
            Text("Pantalla de Entrenamientos - Bruno")
        }
    }
}