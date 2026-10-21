package com.example.projectsportpro2026_2.ui.autenticacion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectsportpro2026_2.datos.modelo.PerfilUsuario
import com.example.projectsportpro2026_2.datos.modelo.Rol
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioAutenticacion
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EstadoRegistro(
    val nombre: String = "",
    val correo: String = "",
    val contrasena: String = "",
    val rol: Rol = Rol.JUG,
    val fechaNacimiento: String = "",
    val cargando: Boolean = false,
    val error: String? = null,
    val perfilCreado: PerfilUsuario? = null
)

class RegistroViewModel(
    private val repositorio: RepositorioAutenticacion = RepositorioAutenticacion()
) : ViewModel() {

    private val _estado = MutableStateFlow(EstadoRegistro())
    val estado: StateFlow<EstadoRegistro> = _estado.asStateFlow()

    fun cambiarNombre(valor: String) = _estado.update { it.copy(nombre = valor, error = null) }
    fun cambiarCorreo(valor: String) = _estado.update { it.copy(correo = valor, error = null) }
    fun cambiarContrasena(valor: String) = _estado.update { it.copy(contrasena = valor, error = null) }
    fun cambiarRol(valor: Rol) = _estado.update { it.copy(rol = valor, error = null) }
    fun cambiarFechaNacimiento(valor: String) =
        _estado.update { it.copy(fechaNacimiento = valor, error = null) }

    fun registrar() {
        val actual = _estado.value
        val errorDeValidacion = validar(actual)
        if (errorDeValidacion != null) {
            _estado.update { it.copy(error = errorDeValidacion) }
            return
        }

        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }
            repositorio.registrar(
                nombre = actual.nombre,
                correo = actual.correo,
                contrasena = actual.contrasena,
                rol = actual.rol,
                fechaNacimiento = actual.fechaNacimiento
            ).onSuccess { perfil ->
                _estado.update { it.copy(cargando = false, perfilCreado = perfil) }
            }.onFailure { excepcion ->
                _estado.update { it.copy(cargando = false, error = traducirError(excepcion)) }
            }
        }
    }

    private fun validar(e: EstadoRegistro): String? = when {
        e.nombre.isBlank() -> "Escribe tu nombre completo"
        !e.correo.contains("@") -> "Escribe un correo válido"
        e.contrasena.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
        e.rol == Rol.JUG && e.fechaNacimiento.isBlank() -> "Escribe tu fecha de nacimiento"
        else -> null
    }

    private fun traducirError(e: Throwable): String = when (e) {
        is FirebaseAuthUserCollisionException -> "Ya existe una cuenta con ese correo"
        is FirebaseAuthWeakPasswordException -> "La contraseña es muy débil"
        is FirebaseAuthInvalidCredentialsException -> "El correo no es válido"
        is FirebaseNetworkException -> "Sin conexión a internet"
        is IllegalArgumentException, is IllegalStateException ->
            e.message ?: "No se pudo completar el registro"
        else -> "No se pudo registrar. Inténtalo de nuevo"
    }
}