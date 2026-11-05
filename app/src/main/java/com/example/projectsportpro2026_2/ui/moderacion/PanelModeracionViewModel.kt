package com.example.projectsportpro2026_2.ui.moderacion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectsportpro2026_2.datos.modelo.DecisionModeracion
import com.example.projectsportpro2026_2.datos.modelo.EstadoReporte
import com.example.projectsportpro2026_2.datos.modelo.Reporte
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioAutenticacion
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioModeracion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class FiltroReportes(val texto: String) {
    PENDIENTES("Pendientes"),
    RESUELTOS("Resueltos"),
    TODOS("Todos")
}

data class EstadoPanelModeracion(
    val uid: String = "",
    val reportes: List<Reporte> = emptyList(),
    val filtro: FiltroReportes = FiltroReportes.PENDIENTES,
    val cargando: Boolean = false,
    val procesando: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null
) {
    val cantidadPendientes: Int
        get() = reportes.count { it.estado == EstadoReporte.PENDIENTE }

    val reportesVisibles: List<Reporte>
        get() = when (filtro) {
            FiltroReportes.PENDIENTES -> reportes.filter { it.estado == EstadoReporte.PENDIENTE }
            FiltroReportes.RESUELTOS -> reportes.filter { it.estado == EstadoReporte.RESUELTO }
            FiltroReportes.TODOS -> reportes
        }
}

class PanelModeracionViewModel(
    private val moderacion: RepositorioModeracion = RepositorioModeracion(),
    private val autenticacion: RepositorioAutenticacion = RepositorioAutenticacion()
) : ViewModel() {

    private val _estado = MutableStateFlow(EstadoPanelModeracion())
    val estado: StateFlow<EstadoPanelModeracion> = _estado.asStateFlow()

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

        moderacion.listarReportes(uid)
            .onSuccess { lista -> _estado.update { it.copy(reportes = lista, cargando = false) } }
            .onFailure { e ->
                _estado.update { it.copy(cargando = false, error = e.message ?: "No se pudieron cargar los reportes") }
            }
    }

    fun cambiarFiltro(filtro: FiltroReportes) {
        _estado.update { it.copy(filtro = filtro) }
    }

    fun resolver(
        reporte: Reporte,
        decision: DecisionModeracion,
        motivo: String,
        alTerminar: () -> Unit = {}
    ) {
        if (_estado.value.procesando) return

        viewModelScope.launch {
            _estado.update { it.copy(procesando = true, error = null) }

            moderacion.moderar(reporte.objetivo(), decision, motivo, _estado.value.uid)
                .onSuccess {
                    _estado.update { it.copy(procesando = false, mensaje = decision.texto) }
                    alTerminar()
                    cargar()
                }
                .onFailure { e ->
                    _estado.update {
                        it.copy(procesando = false, error = e.message ?: "No se pudo completar la operación")
                    }
                }
        }
    }

    fun limpiarMensajes() {
        _estado.update { it.copy(error = null, mensaje = null) }
    }
}