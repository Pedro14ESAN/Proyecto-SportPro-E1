package com.example.projectsportpro2026_2.ui.autenticacion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectsportpro2026_2.datos.modelo.PerfilUsuario
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioAutenticacion
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EstadoLogin(
    val correo: String = "",
    val contrasena: String = "",
    val cargando: Boolean = false,
    val error: String? = null,
    val perfil: PerfilUsuario? = null
)

class LoginViewModel(
    private val repositorio: RepositorioAutenticacion = RepositorioAutenticacion()
) : ViewModel() {

    private val _estado = MutableStateFlow(EstadoLogin())
    val estado: StateFlow<EstadoLogin> = _estado.asStateFlow()

    fun cambiarCorreo(valor: String) = _estado.update { it.copy(correo = valor, error = null) }
    fun cambiarContrasena(valor: String) = _estado.update { it.copy(contrasena = valor, error = null) }

    fun iniciarSesion() {
        val actual = _estado.value
        if (!actual.correo.contains("@")) {
            _estado.update { it.copy(error = "Escribe un correo válido") }
            return
        }
        if (actual.contrasena.isEmpty()) {
            _estado.update { it.copy(error = "Escribe tu contraseña") }
            return
        }

        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }
            repositorio.iniciarSesion(actual.correo, actual.contrasena)
                .onSuccess { perfil ->
                    _estado.update { it.copy(cargando = false, perfil = perfil) }
                }
                .onFailure { excepcion ->
                    _estado.update { it.copy(cargando = false, error = traducirError(excepcion)) }
                }
        }
    }

    private fun traducirError(e: Throwable): String = when (e) {
        is FirebaseAuthInvalidUserException,
        is FirebaseAuthInvalidCredentialsException -> "Correo o contraseña incorrectos"
        is FirebaseTooManyRequestsException -> "Demasiados intentos. Espera un momento"
        is FirebaseNetworkException -> "Sin conexión a internet"
        is IllegalStateException -> e.message ?: "No se pudo iniciar sesión"
        else -> "No se pudo iniciar sesión. Inténtalo de nuevo"
    }
}