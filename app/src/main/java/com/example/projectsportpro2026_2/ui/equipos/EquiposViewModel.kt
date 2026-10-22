package com.example.projectsportpro2026_2.ui.equipos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectsportpro2026_2.datos.modelo.Equipo
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioAcademias
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioAutenticacion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EstadoEquipos(
    val equipos: List<Equipo> = emptyList(),
    val cargando: Boolean = false,
    val error: String? = null
)

class EquiposViewModel(
    private val repositorio: RepositorioAcademias = RepositorioAcademias(),
    private val autenticacion: RepositorioAutenticacion = RepositorioAutenticacion()
) : ViewModel() {

    private val _estado = MutableStateFlow(EstadoEquipos())
    val estado: StateFlow<EstadoEquipos> = _estado.asStateFlow()

    private var idAcademia = ""
    private val uid: String get() = autenticacion.uidActual().orEmpty()

    fun iniciar(id: String) {
        if (id != idAcademia) {
            idAcademia = id
            cargar()
        }
    }

    fun limpiarError() = _estado.update { it.copy(error = null) }

    fun cargar() {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }
            repositorio.listarEquipos(idAcademia)
                .onSuccess { lista -> _estado.update { it.copy(cargando = false, equipos = lista) } }
                .onFailure { e -> _estado.update { it.copy(cargando = false, error = mensaje(e)) } }
        }
    }

    // equipoActual == null  ->  crear; si no  ->  editar
    fun guardar(
        equipoActual: Equipo?,
        nombre: String,
        categoria: String,
        alTerminar: () -> Unit
    ) {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }
            val resultado = if (equipoActual == null) {
                repositorio.crearEquipo(nombre, categoria, idAcademia, "", uid).map { }
            } else {
                repositorio.editarEquipo(
                    equipoActual.id, nombre, categoria, equipoActual.idEntrenador, uid
                )
            }
            resultado
                .onSuccess {
                    alTerminar()
                    cargar()
                }
                .onFailure { e -> _estado.update { it.copy(cargando = false, error = mensaje(e)) } }
        }
    }

    fun cambiarEstado(equipo: Equipo) {
        viewModelScope.launch {
            _estado.update { it.copy(error = null) }
            repositorio.cambiarEstadoEquipo(equipo.id, !equipo.activo, uid)
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