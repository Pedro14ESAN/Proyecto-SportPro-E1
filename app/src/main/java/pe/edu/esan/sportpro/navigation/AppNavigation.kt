package pe.edu.esan.sportpro.navigation

import android.widget.Toast

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import pe.edu.esan.sportpro.data.model.Exercise
import pe.edu.esan.sportpro.data.model.Training
import pe.edu.esan.sportpro.data.repository.ExerciseRepository
import pe.edu.esan.sportpro.data.repository.TrainingRepository

import pe.edu.esan.sportpro.ui.auth.AuthViewModel
import pe.edu.esan.sportpro.ui.auth.LoginScreen
import pe.edu.esan.sportpro.ui.auth.RegisterScreen

import pe.edu.esan.sportpro.ui.home.HomeScreen

import pe.edu.esan.sportpro.ui.onboarding.OrganizationIntroScreen
import pe.edu.esan.sportpro.ui.onboarding.WelcomeScreen

import pe.edu.esan.sportpro.ui.teams.TeamsScreen

import pe.edu.esan.sportpro.ui.training.CreateExerciseScreen
import pe.edu.esan.sportpro.ui.training.CreateTrainingScreen
import pe.edu.esan.sportpro.ui.training.ExerciseLibraryScreen
import pe.edu.esan.sportpro.ui.training.TrainingScreen

import java.text.SimpleDateFormat
import java.util.Locale


sealed class Screen(val route: String) {

    // Onboarding
    object Welcome : Screen("welcome")
    object IntroOrganization : Screen("intro_organization")

    // Autenticación
    object Login : Screen("login")
    object Register : Screen("register")

    // Principal
    object Home : Screen("home")
    object Teams : Screen("teams")
    object Players : Screen("players")
    object Trainings : Screen("trainings")

    // Entrenamientos
    object ExerciseLibrary : Screen("exercise_library")
    object CreateExercise : Screen("create_exercise")
    object CreateTraining : Screen("create_training")
    object TrainingDetail : Screen("training_detail")
    object Attendance : Screen("attendance")
}


@Composable
fun AppNavigation() {

    val navController = rememberNavController()
    val authViewModel = remember { AuthViewModel() }
    val context = LocalContext.current

    // Verificamos si Firebase mantiene una sesión activa.
    val currentUser = FirebaseAuth.getInstance().currentUser

    val initialDestination =
        if (currentUser != null) {
            Screen.Home.route
        } else {
            Screen.Welcome.route
        }


    NavHost(
        navController = navController,
        startDestination = initialDestination
    ) {

        // =====================================================
        // 0. PORTADA
        // =====================================================

        composable(Screen.Welcome.route) {

            WelcomeScreen(
                onStart = {
                    navController.navigate(
                        Screen.IntroOrganization.route
                    )
                }
            )
        }


        // =====================================================
        // 0.1 INTRODUCCIÓN
        // =====================================================

        composable(Screen.IntroOrganization.route) {

            OrganizationIntroScreen(

                onBack = {
                    navController.popBackStack()
                },

                onContinue = {

                    navController.navigate(
                        Screen.Login.route
                    ) {

                        popUpTo(Screen.Welcome.route) {
                            inclusive = true
                        }
                    }
                },

                onSkip = {

                    navController.navigate(
                        Screen.Login.route
                    ) {

                        popUpTo(Screen.Welcome.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }


        // =====================================================
        // 1. LOGIN
        // =====================================================

        composable(Screen.Login.route) {

            LoginScreen(

                onLoginClick = { email, password ->

                    if (
                        email.isBlank()
                        || password.isBlank()
                    ) {

                        Toast.makeText(
                            context,
                            "Completa el correo y la contraseña",
                            Toast.LENGTH_SHORT
                        ).show()

                    } else {

                        FirebaseAuth
                            .getInstance()
                            .signInWithEmailAndPassword(
                                email.trim(),
                                password
                            )
                            .addOnSuccessListener {

                                navController.navigate(
                                    Screen.Home.route
                                ) {

                                    popUpTo(
                                        Screen.Login.route
                                    ) {
                                        inclusive = true
                                    }
                                }
                            }
                            .addOnFailureListener { exception ->

                                Toast.makeText(
                                    context,
                                    "No se pudo iniciar sesión: ${exception.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }
                },

                onNavigateToRegister = {

                    navController.navigate(
                        Screen.Register.route
                    )
                },

                onForgotPassword = {

                    Toast.makeText(
                        context,
                        "Recuperación de contraseña próximamente",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }


        // =====================================================
        // 2. REGISTRO
        // =====================================================

        composable(Screen.Register.route) {

            RegisterScreen(
                viewModel = authViewModel,

                onRegisterSuccess = {

                    navController.navigate(
                        Screen.Home.route
                    ) {

                        popUpTo(Screen.Register.route) {
                            inclusive = true
                        }
                    }
                },

                onGoToLogin = {

                    navController.navigate(
                        Screen.Login.route
                    ) {

                        popUpTo(Screen.Register.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }


        // =====================================================
        // 3. HOME
        // =====================================================

        composable(Screen.Home.route) {

            HomeScreen(

                onNavigateToTeams = {

                    navController.navigate(
                        Screen.Teams.route
                    )
                },

                onNavigateToPlayers = {

                    navController.navigate(
                        Screen.Players.route
                    )
                },

                onNavigateToTrainings = {

                    navController.navigate(
                        Screen.Trainings.route
                    )
                },

                onLogout = {

                    FirebaseAuth
                        .getInstance()
                        .signOut()

                    navController.navigate(
                        Screen.Login.route
                    ) {

                        popUpTo(0) {
                            inclusive = true
                        }
                    }
                }
            )
        }


        // =====================================================
        // 4. EQUIPOS
        // =====================================================

        composable(Screen.Teams.route) {

            val user = authViewModel.currentUser

            TeamsScreen(
                academyId = user?.academyId.orEmpty(),
                role = user?.role.orEmpty(),

                onBack = {
                    navController.popBackStack()
                },

                onAcademyCreated = { academyId ->
                    authViewModel.updateAcademy(academyId)
                }
            )
        }


        // =====================================================
        // 5. JUGADORES
        // =====================================================

        composable(Screen.Players.route) {

            Text(
                "Pantalla de Jugadores - Integrante 5"
            )
        }


        // =====================================================
        // 6. ENTRENAMIENTOS
        // =====================================================

        composable(Screen.Trainings.route) {

            val trainingRepository =
                remember {
                    TrainingRepository()
                }

            val user =
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

            LaunchedEffect(user?.uid) {

                if (user == null) {

                    isLoadingTrainings = false

                    trainingError =
                        "No existe una sesión activa."

                } else {

                    trainingRepository
                        .getTrainingsByCoach(
                            coachUid = user.uid
                        ) { result ->

                            result.onSuccess {
                                    firebaseTrainings ->

                                trainings =
                                    firebaseTrainings
                                        .sortedByDescending {
                                            it.date?.seconds ?: 0
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
                isLoading = isLoadingTrainings,
                errorMessage = trainingError,

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


        // =====================================================
        // 7. BIBLIOTECA DE EJERCICIOS
        // =====================================================

        composable(Screen.ExerciseLibrary.route) {

            val exerciseRepository =
                remember {
                    ExerciseRepository()
                }

            val user =
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

            LaunchedEffect(user?.uid) {

                if (user == null) {

                    isLoading = false

                    errorMessage =
                        "No existe una sesión activa."

                } else {

                    FirebaseFirestore
                        .getInstance()
                        .collection("users")
                        .document(user.uid)
                        .get()
                        .addOnSuccessListener { userDocument ->

                            val academyId =
                                userDocument.getString(
                                    "academyId"
                                )

                            if (academyId.isNullOrBlank()) {

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
                        .addOnFailureListener { exception ->

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


        // =====================================================
        // 8. CREAR EJERCICIO
        // =====================================================

        composable(Screen.CreateExercise.route) {

            val appContext =
                LocalContext.current

            val exerciseRepository =
                remember {
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

                    val user =
                        FirebaseAuth
                            .getInstance()
                            .currentUser

                    if (user == null) {

                        Toast.makeText(
                            appContext,
                            "Debes iniciar sesión.",
                            Toast.LENGTH_SHORT
                        ).show()

                    } else {

                        FirebaseFirestore
                            .getInstance()
                            .collection("users")
                            .document(user.uid)
                            .get()
                            .addOnSuccessListener { document ->

                                val academyId =
                                    document.getString(
                                        "academyId"
                                    )

                                if (academyId.isNullOrBlank()) {

                                    Toast.makeText(
                                        appContext,
                                        "El usuario no tiene una academia asignada.",
                                        Toast.LENGTH_LONG
                                    ).show()

                                } else {

                                    exerciseRepository
                                        .createExercise(
                                            academyId = academyId,
                                            createdBy = user.uid,
                                            name = name,
                                            objective = objective,
                                            description = description,
                                            durationMinutes = durationMinutes
                                        ) { result ->

                                            result.onSuccess {

                                                Toast.makeText(
                                                    appContext,
                                                    "Ejercicio guardado en Firebase",
                                                    Toast.LENGTH_SHORT
                                                ).show()

                                                navController
                                                    .popBackStack()
                                            }

                                            result.onFailure {
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


        // =====================================================
        // 9. CREAR ENTRENAMIENTO
        // =====================================================

        composable(Screen.CreateTraining.route) {

            val appContext =
                LocalContext.current

            val exerciseRepository =
                remember {
                    ExerciseRepository()
                }

            val trainingRepository =
                remember {
                    TrainingRepository()
                }

            val user =
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


            LaunchedEffect(user?.uid) {

                if (user == null) {

                    isLoadingExercises =
                        false

                    exerciseError =
                        "No existe una sesión activa."

                } else {

                    FirebaseFirestore
                        .getInstance()
                        .collection("users")
                        .document(user.uid)
                        .get()
                        .addOnSuccessListener {
                                userDocument ->

                            val userAcademyId =
                                userDocument.getString(
                                    "academyId"
                                )

                            if (userAcademyId.isNullOrBlank()) {

                                isLoadingExercises =
                                    false

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
                                "No se pudo obtener la academia: ${exception.message}"
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
                    navController.popBackStack()
                },

                onTrainingCreated = {
                        team,
                        date,
                        time,
                        objective,
                        durationMinutes,
                        exerciseIds ->

                    val currentUser =
                        FirebaseAuth
                            .getInstance()
                            .currentUser

                    val currentAcademy =
                        academyId

                    if (currentUser == null) {

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
                                    Timestamp(
                                        parsedDate
                                    )

                                trainingRepository
                                    .createTraining(
                                        teamId = team,
                                        academyId =
                                            currentAcademy,
                                        coachUid =
                                            currentUser.uid,
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


        // =====================================================
        // 10. DETALLE DE ENTRENAMIENTO
        // =====================================================

        composable(Screen.TrainingDetail.route) {

            Text(
                "Detalle del entrenamiento - Bruno"
            )
        }


        // =====================================================
        // 11. REGISTRO DE ASISTENCIA
        // =====================================================

        composable(Screen.Attendance.route) {

            Text(
                "Registro de asistencia - Bruno"
            )
        }
    }
}