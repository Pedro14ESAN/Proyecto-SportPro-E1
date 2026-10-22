package com.example.projectsportpro2026_2.ui.vinculaciones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectsportpro2026_2.datos.modelo.EstadoVinculacion
import com.example.projectsportpro2026_2.datos.modelo.Vinculacion
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioAutenticacion
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioVinculaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EstadoVinculaciones(
    val vinculaciones: List<Vinculacion> = emptyList(),
    val cargando: Boolean = false,
    val error: String? = null
) {
    val pendientes: Int get() = vinculaciones.count { it.estado == EstadoVinculacion.PENDIENTE }
}

class VinculacionesViewModel(
    private val repositorio: RepositorioVinculaciones = RepositorioVinculaciones(),
    private val autenticacion: RepositorioAutenticacion = RepositorioAutenticacion()
) : ViewModel() {

    private val _estado = MutableStateFlow(EstadoVinculaciones())
    val estado: StateFlow<EstadoVinculaciones> = _estado.asStateFlow()

    private val uid: String get() = autenticacion.uidActual().orEmpty()

    init {
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }
            repositorio.listarParaAdministrador(uid)
                .onSuccess { lista ->
                    // Primero pendientes, luego activas, luego inactivas
                    val ordenada = lista.sortedWith(
                        compareBy<Vinculacion> { it.estado.ordinal }
                            .thenByDescending { it.fechaSolicitud }
                    )
                    _estado.update { it.copy(cargando = false, vinculaciones = ordenada) }
                }
                .onFailure { e -> _estado.update { it.copy(cargando = false, error = mensaje(e)) } }
        }
    }

    fun aprobar(v: Vinculacion) = ejecutar { repositorio.aprobar(v.id, uid) }
    fun rechazar(v: Vinculacion) = ejecutar { repositorio.rechazar(v.id, uid) }
    fun desvincular(v: Vinculacion) = ejecutar { repositorio.desvincular(v.id, uid) }

    private fun ejecutar(accion: suspend () -> Result<Unit>) {
        viewModelScope.launch {
            _estado.update { it.copy(error = null) }
            accion()
                .onSuccess { cargar() }
                .onFailure { e -> _estado.update { it.copy(error = mensaje(e)) } }
        }
    }

    private fun mensaje(e: Throwable): String = when (e) {
        is IllegalArgumentException, is IllegalStateException ->
            e.message ?: "No se pudo completar la operación"
        else -> "No se pudo completar la operación. Revisa tu conexión e inténtalo de nuevo"
    }
}