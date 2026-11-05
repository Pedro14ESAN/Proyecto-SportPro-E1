package com.example.projectsportpro2026_2.ui.notificaciones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectsportpro2026_2.datos.modelo.Notificacion
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioAutenticacion
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioNotificaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EstadoNotificaciones(
    val uid: String = "",
    val notificaciones: List<Notificacion> = emptyList(),
    val cargando: Boolean = false,
    val abriendo: Boolean = false,
    val error: String? = null
) {
    val noLeidas: Int
        get() = notificaciones.count { !it.leida }
}

class NotificacionesViewModel(
    private val repositorio: RepositorioNotificaciones = RepositorioNotificaciones(),
    private val autenticacion: RepositorioAutenticacion = RepositorioAutenticacion()
) : ViewModel() {

    private val _estado = MutableStateFlow(EstadoNotificaciones())
    val estado: StateFlow<EstadoNotificaciones> = _estado.asStateFlow()

    private var iniciado = false

    fun iniciar() {
        if (iniciado) return
        iniciado = true

        viewModelScope.launch {
            val uid = autenticacion.uidActual()
            if (uid.isNullOrBlank()) {
                _estado.update { it.copy(error = "No existe una sesión activa") }
                return@launch
            }
            _estado.update { it.copy(uid = uid) }
            cargar()
        }
    }

    fun recargar() {
        viewModelScope.launch { cargar() }
    }

    private suspend fun cargar() {
        val uid = _estado.value.uid
        if (uid.isBlank()) return

        _estado.update { it.copy(cargando = true, error = null) }

        repositorio.listar(uid)
            .onSuccess { lista -> _estado.update { it.copy(notificaciones = lista, cargando = false) } }
            .onFailure { e ->
                _estado.update { it.copy(cargando = false, error = e.message ?: "No se pudieron cargar las notificaciones") }
            }
    }

    fun marcarTodasLeidas() {
        viewModelScope.launch {
            repositorio.marcarTodasLeidas(_estado.value.uid)
                .onSuccess {
                    _estado.update { e ->
                        e.copy(notificaciones = e.notificaciones.map { it.copy(leida = true) })
                    }
                }
                .onFailure { e -> _estado.update { it.copy(error = e.message) } }
        }
    }

    // Criterio HU20: al tocar, solo navega si el contenido existe y el usuario mantiene el permiso
    fun abrir(notificacion: Notificacion, alPermitir: (Notificacion) -> Unit) {
        if (_estado.value.abriendo) return

        viewModelScope.launch {
            _estado.update { it.copy(abriendo = true, error = null) }

            if (!notificacion.leida) {
                repositorio.marcarLeida(notificacion.id)
                _estado.update { e ->
                    e.copy(notificaciones = e.notificaciones.map {
                        if (it.id == notificacion.id) it.copy(leida = true) else it
                    })
                }
            }

            repositorio.verificarAcceso(notificacion, _estado.value.uid)
                .onSuccess {
                    _estado.update { it.copy(abriendo = false) }
                    alPermitir(notificacion)
                }
                .onFailure { e ->
                    _estado.update { it.copy(abriendo = false, error = e.message) }
                }
        }
    }
}