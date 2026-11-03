package com.example.projectsportpro2026_2.ui.comunidad

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectsportpro2026_2.datos.modelo.EstadoPublicacion
import com.example.projectsportpro2026_2.datos.modelo.Publicacion
import com.example.projectsportpro2026_2.datos.modelo.Rol
import com.example.projectsportpro2026_2.datos.modelo.VisibilidadPublicacion
import com.example.projectsportpro2026_2.datos.repositorio.OpcionesComunidad
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioAutenticacion
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioPublicaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Pestañas del Figma: "Para ti" / "Academias" / "Pruebas"
enum class PestanaComunidad { PARA_TI, ACADEMIAS, PRUEBAS }

data class EstadoComunidad(
    val uid: String = "",
    val rol: Rol? = null,
    val nombre: String="",
    val publicaciones: List<Publicacion> = emptyList(),
    val pestana: PestanaComunidad = PestanaComunidad.PARA_TI,
    val opciones: OpcionesComunidad = OpcionesComunidad(),
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null
) {
    // Lo que se muestra según la pestaña elegida
    val publicacionesVisibles: List<Publicacion>
        get() = when (pestana) {
            PestanaComunidad.PARA_TI -> publicaciones
            PestanaComunidad.ACADEMIAS -> publicaciones.filter {
                it.visibilidad != VisibilidadPublicacion.COMUNIDAD   // solo internas de mis academias/equipos
            }
            PestanaComunidad.PRUEBAS -> emptyList()                    // la llenará HU23
        }

    // Solo el autor puede editar o eliminar, y solo si no fue moderada
    fun puedeModificar(publicacion: Publicacion): Boolean =
        publicacion.idAutor == uid && publicacion.estado == EstadoPublicacion.VISIBLE
}

class ComunidadViewModel(
    private val repositorio: RepositorioPublicaciones = RepositorioPublicaciones(),
    private val autenticacion: RepositorioAutenticacion = RepositorioAutenticacion()
) : ViewModel() {

    private val _estado = MutableStateFlow(EstadoComunidad())
    val estado: StateFlow<EstadoComunidad> = _estado.asStateFlow()

    private var iniciado = false

    // ---------- Carga inicial ----------

    fun iniciar() {
        if (iniciado) return
        iniciado = true

        viewModelScope.launch {
            val uid = autenticacion.uidActual()
            if (uid.isNullOrBlank()) {
                _estado.update { it.copy(error = "No existe una sesión activa") }
                return@launch
            }

            val perfil = try {
                autenticacion.obtenerPerfil(uid)
            } catch (e: Exception) {
                null
            }
            _estado.update { it.copy(uid = uid, rol = perfil?.rol, nombre = perfil?.nombre.orEmpty()) }

            // Todos los roles pueden publicar; aquí se cargan sus academias y equipos.
            // Si falla, igual puede publicar en Comunidad SportPro.
            repositorio.opcionesPublicacion(uid)
                .onSuccess { opciones ->
                    _estado.update { it.copy(opciones = opciones) }
                }

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

        repositorio.listarFeed(uid)
            .onSuccess { lista ->
                _estado.update { it.copy(publicaciones = lista, cargando = false) }
            }
            .onFailure { e ->
                _estado.update { it.copy(cargando = false, error = mensajeError(e)) }
            }
    }

    // ---------- Acciones de la pantalla ----------

    fun cambiarPestana(pestana: PestanaComunidad) {
        _estado.update { it.copy(pestana = pestana) }
    }

    fun publicar(
        texto: String,
        visibilidad: VisibilidadPublicacion,
        idAcademia: String,
        idEquipo: String,
        alTerminar: () -> Unit = {}
    ) {
        if (_estado.value.guardando) return   // evita publicar dos veces

        viewModelScope.launch {
            _estado.update { it.copy(guardando = true, error = null) }

            repositorio.crear(
                uid = _estado.value.uid,
                texto = texto,
                visibilidad = visibilidad,
                idAcademia = idAcademia,
                idEquipo = idEquipo
            )
                .onSuccess { publicada ->
                    _estado.update {
                        it.copy(
                            guardando = false,
                            mensaje = "Publicación compartida con ${destino(publicada)}"
                        )
                    }
                    alTerminar()
                    cargar()
                }
                .onFailure { e ->
                    _estado.update { it.copy(guardando = false, error = mensajeError(e)) }
                }
        }
    }

    fun editar(
        idPublicacion: String,
        texto: String,
        alTerminar: () -> Unit = {}
    ) {
        if (_estado.value.guardando) return

        viewModelScope.launch {
            _estado.update { it.copy(guardando = true, error = null) }

            repositorio.editar(idPublicacion, texto, _estado.value.uid)
                .onSuccess {
                    _estado.update { it.copy(guardando = false, mensaje = "Publicación actualizada") }
                    alTerminar()
                    cargar()
                }
                .onFailure { e ->
                    _estado.update { it.copy(guardando = false, error = mensajeError(e)) }
                }
        }
    }

    fun eliminar(publicacion: Publicacion) {
        viewModelScope.launch {
            repositorio.eliminar(publicacion.id, _estado.value.uid)
                .onSuccess {
                    _estado.update { it.copy(mensaje = "Publicación eliminada") }
                    cargar()
                }
                .onFailure { e ->
                    _estado.update { it.copy(error = mensajeError(e)) }
                }
        }
    }

    fun limpiarMensajes() {
        _estado.update { it.copy(error = null, mensaje = null) }
    }

    // ---------- Ayudas ----------

    // Texto del mensaje de éxito, como en el Figma:
    // "Publicación compartida con Comunidad SportPro"
    private fun destino(publicacion: Publicacion): String =
        when (publicacion.visibilidad) {
            VisibilidadPublicacion.COMUNIDAD -> "Comunidad SportPro"
            VisibilidadPublicacion.ACADEMIA -> publicacion.nombreAcademia.ifBlank { "tu academia" }
            VisibilidadPublicacion.EQUIPO -> publicacion.nombreEquipo.ifBlank { "tu equipo" }
        }

    private fun mensajeError(e: Throwable): String =
        e.message ?: "No se pudo completar la operación"
}