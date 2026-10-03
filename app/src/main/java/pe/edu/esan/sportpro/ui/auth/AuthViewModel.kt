package pe.edu.esan.sportpro.ui.auth

import androidx.lifecycle.ViewModel
import pe.edu.esan.sportpro.data.model.User
import pe.edu.esan.sportpro.data.repository.AuthRepository
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue


class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var currentUser by mutableStateOf<User?>(null)
        private set

    fun register(
        fullName: String,
        email: String,
        password: String,
        confirmPassword: String,
        role: String,
        acceptedTerms: Boolean,
        onSuccess: () -> Unit
    ) {

        errorMessage = null

        if (fullName.isBlank() ||
            email.isBlank() ||
            password.isBlank() ||
            confirmPassword.isBlank()
        ) {
            errorMessage = "Completa todos los campos obligatorios"
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errorMessage = "Ingresa un correo electrónico válido"
            return
        }

        if (password.length < 8) {
            errorMessage = "La contraseña debe tener al menos 8 caracteres"
            return
        }

        if (password != confirmPassword) {
            errorMessage = "Las contraseñas no coinciden"
            return
        }

        if (!acceptedTerms) {
            errorMessage = "Debes aceptar los términos y la política de privacidad"
            return
        }

        if (
            role != User.ROLE_PLAYER &&
            role != User.ROLE_PARENT &&
            role != User.ROLE_COACH
        ) {
            errorMessage = "Selecciona un rol válido"
            return
        }

        isLoading = true

        repository.registerUser(
            fullName = fullName.trim(),
            email = email.trim(),
            password = password,
            role = role
        ) { result ->

            isLoading = false

            result.onSuccess { user ->
                currentUser = user
                onSuccess()
            }

            result.onFailure {
                errorMessage = "No se pudo completar el registro. Inténtalo nuevamente."
            }
        }
    }
    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit
    ) {
        errorMessage = null

        val cleanEmail = email.trim()

        if (cleanEmail.isBlank() || password.isBlank()) {
            errorMessage = "Ingresa tu correo y contraseña"
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            errorMessage = "Ingresa un correo electrónico válido"
            return
        }

        isLoading = true

        repository.loginUser(
            email = cleanEmail,
            password = password
        ) { loginResult ->

            loginResult.fold(
                onSuccess = { firebaseUser ->

                    repository.getUserProfile(firebaseUser.uid) { profileResult ->

                        profileResult.fold(
                            onSuccess = { user ->

                                if (!user.active) {
                                    repository.logout()
                                    isLoading = false
                                    errorMessage = "No se pudo iniciar sesión"
                                } else {
                                    currentUser = user
                                    isLoading = false
                                    errorMessage = null
                                    onSuccess()
                                }
                            },

                            onFailure = {
                                repository.logout()
                                isLoading = false
                                errorMessage = "No se pudo iniciar sesión"
                            }
                        )
                    }
                },

                onFailure = {
                    isLoading = false
                    errorMessage = "Correo o contraseña incorrectos"
                }
            )
        }
    }

    fun logout() {
        repository.logout()
        currentUser = null
        errorMessage = null
    }

    fun clearError() {
        errorMessage = null
    }
    fun updateAcademy(academyId: String) {
        currentUser = currentUser?.copy(academyId = academyId)
    }
}