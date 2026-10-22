package com.example.projectsportpro2026_2.ui.vinculaciones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectsportpro2026_2.datos.modelo.EstadoVinculacion
import com.example.projectsportpro2026_2.datos.modelo.Vinculacion
import com.example.projectsportpro2026_2.datos.repositorio.DatosSensiblesVinculo
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioAutenticacion
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioVinculaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class FiltroVinculacion(val etiqueta: String) {
    ACTIVO("Activo"), PENDIENTE("Pendiente"), INACTIVO("Inactivo"), TODOS("Todos")
}

data class EstadoMisJugadores(
    val vinculaciones: List<Vinculacion> = emptyList(),
    val datos: Map<String, DatosSensiblesVinculo> = emptyMap(),
    val filtro: FiltroVinculacion = FiltroVinculacion.TODOS,
    val cargando: Boolean = false,
    val error: String? = null
) {
    val visibles: List<Vinculacion>
        get() = when (filtro) {
            FiltroVinculacion.ACTIVO -> vinculaciones.filter { it.estado == EstadoVinculacion.ACTIVA }
            FiltroVinculacion.PENDIENTE -> vinculaciones.filter { it.estado == EstadoVinculacion.PENDIENTE }
            FiltroVinculacion.INACTIVO -> vinculaciones.filter { it.estado == EstadoVinculacion.INACTIVA }
            FiltroVinculacion.TODOS -> vinculaciones
        }
}

class MisJugadoresViewModel(
    private val repositorio: RepositorioVinculaciones = RepositorioVinculaciones(),
    private val autenticacion: RepositorioAutenticacion = RepositorioAutenticacion()
) : ViewModel() {

    private val _estado = MutableStateFlow(EstadoMisJugadores())
    val estado: StateFlow<EstadoMisJugadores> = _estado.asStateFlow()

    private val uid: String get() = autenticacion.uidActual().orEmpty()

    init {
        cargar()
    }

    fun limpiarError() = _estado.update { it.copy(error = null) }
    fun cambiarFiltro(filtro: FiltroVinculacion) = _estado.update { it.copy(filtro = filtro) }

    fun cargar() {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }
            repositorio.listarMias(uid)
                .onSuccess { lista ->
                    // Los datos sensibles solo se piden para vinculaciones activas
                    val datos = mutableMapOf<String, DatosSensiblesVinculo>()
                    lista.filter { it.estado == EstadoVinculacion.ACTIVA }.forEach { vinculacion ->
                        repositorio.datosSensibles(vinculacion.id, uid)
                            .onSuccess { datos[vinculacion.id] = it }
                    }
                    _estado.update { it.copy(cargando = false, vinculaciones = lista, datos = datos) }
                }
                .onFailure { e -> _estado.update { it.copy(cargando = false, error = mensaje(e)) } }
        }
    }

    fun solicitar(documentoJugador: String, alTerminar: () -> Unit) {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }
            repositorio.solicitar(documentoJugador, uid)
                .onSuccess {
                    alTerminar()
                    cargar()
                }
                .onFailure { e -> _estado.update { it.copy(cargando = false, error = mensaje(e)) } }
        }
    }

    fun solicitarDesvinculacion(vinculacion: Vinculacion) {
        viewModelScope.launch {
            _estado.update { it.copy(error = null) }
            repositorio.solicitarDesvinculacion(vinculacion.id, uid)
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