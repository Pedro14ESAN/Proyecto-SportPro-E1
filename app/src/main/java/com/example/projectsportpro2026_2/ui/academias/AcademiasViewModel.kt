package com.example.projectsportpro2026_2.ui.academias

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectsportpro2026_2.datos.modelo.Academia
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioAcademias
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioAutenticacion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EstadoAcademias(
    val academias: List<Academia> = emptyList(),
    val cargando: Boolean = false,
    val error: String? = null
)

class AcademiasViewModel(
    private val repositorio: RepositorioAcademias = RepositorioAcademias(),
    private val autenticacion: RepositorioAutenticacion = RepositorioAutenticacion()
) : ViewModel() {

    private val _estado = MutableStateFlow(EstadoAcademias())
    val estado: StateFlow<EstadoAcademias> = _estado.asStateFlow()

    private val uid: String get() = autenticacion.uidActual().orEmpty()

    init {
        cargar()
    }

    fun limpiarError() = _estado.update { it.copy(error = null) }

    fun cargar() {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }
            repositorio.listarAcademias(uid)
                .onSuccess { lista -> _estado.update { it.copy(cargando = false, academias = lista) } }
                .onFailure { e -> _estado.update { it.copy(cargando = false, error = mensaje(e)) } }
        }
    }

    // idAcademia == null  ->  crear; si no  ->  editar
    fun guardar(
        idAcademia: String?,
        nombre: String,
        direccion: String,
        descripcion: String,
        alTerminar: () -> Unit
    ) {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }
            val resultado = if (idAcademia == null) {
                repositorio.crearAcademia(nombre, direccion, descripcion, uid).map { }
            } else {
                repositorio.editarAcademia(idAcademia, nombre, direccion, descripcion, uid)
            }
            resultado
                .onSuccess {
                    alTerminar()
                    cargar()
                }
                .onFailure { e -> _estado.update { it.copy(cargando = false, error = mensaje(e)) } }
        }
    }

    fun cambiarEstado(academia: Academia) {
        viewModelScope.launch {
            _estado.update { it.copy(error = null) }
            repositorio.cambiarEstadoAcademia(academia.id, !academia.activa, uid)
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