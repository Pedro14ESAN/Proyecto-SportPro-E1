package com.example.projectsportpro2026_2.ui.autenticacion

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class EstadoRecuperar(
    val correo: String = "",
    val cargando: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null
)

class RecuperarViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _estado = MutableStateFlow(EstadoRecuperar())
    val estado: StateFlow<EstadoRecuperar> = _estado.asStateFlow()

    fun cambiarCorreo(valor: String) =
        _estado.update { it.copy(correo = valor, error = null, mensaje = null) }

    fun enviar() {
        val correo = _estado.value.correo.trim()
        if (correo.isBlank()) {
            _estado.update { it.copy(error = "Escribe tu correo electrónico") }
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            _estado.update { it.copy(error = "El correo no tiene un formato válido") }
            return
        }

        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null, mensaje = null) }
            try {
                auth.sendPasswordResetEmail(correo).await()
                exito()
            } catch (e: FirebaseAuthInvalidUserException) {
                // Mensaje neutro: no revela si el correo tiene cuenta o no
                exito()
            } catch (e: FirebaseNetworkException) {
                _estado.update {
                    it.copy(cargando = false, error = "Sin conexión. Revisa tu internet e inténtalo de nuevo")
                }
            } catch (e: Exception) {
                _estado.update {
                    it.copy(cargando = false, error = "No se pudo enviar el correo. Inténtalo de nuevo")
                }
            }
        }
    }

    private fun exito() = _estado.update {
        it.copy(
            cargando = false,
            mensaje = "Si el correo está registrado, te enviamos un enlace para crear una contraseña nueva. Revisa también spam."
        )
    }
}