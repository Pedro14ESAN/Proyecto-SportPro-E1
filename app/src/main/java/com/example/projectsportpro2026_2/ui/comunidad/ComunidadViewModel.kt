package com.example.projectsportpro2026_2.ui.comunidad

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectsportpro2026_2.datos.modelo.Comentario
import com.example.projectsportpro2026_2.datos.modelo.EstadoPublicacion
import com.example.projectsportpro2026_2.datos.modelo.Publicacion
import com.example.projectsportpro2026_2.datos.modelo.Rol
import com.example.projectsportpro2026_2.datos.modelo.TipoReaccion
import com.example.projectsportpro2026_2.datos.modelo.VisibilidadPublicacion
import com.example.projectsportpro2026_2.datos.repositorio.OpcionesComunidad
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioAutenticacion
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioInteracciones
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
    val nombre: String = "",
    val publicaciones: List<Publicacion> = emptyList(),
    val pestana: PestanaComunidad = PestanaComunidad.PARA_TI,
    val opciones: OpcionesComunidad = OpcionesComunidad(),
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null,

    // Hoja de comentarios (HU22)
    val publicacionComentarios: Publicacion? = null,   // null = hoja cerrada
    val comentarios: List<Comentario> = emptyList(),
    val cargandoComentarios: Boolean = false,
    val enviandoComentario: Boolean = false,
    val errorComentarios: String? = null
) {
    val publicacionesVisibles: List<Publicacion>
        get() = when (pestana) {
            PestanaComunidad.PARA_TI -> publicaciones
            PestanaComunidad.ACADEMIAS -> publicaciones.filter {
                it.visibilidad != VisibilidadPublicacion.COMUNIDAD
            }
            PestanaComunidad.PRUEBAS -> emptyList()   // la llenará HU23
        }

    fun puedeModificar(publicacion: Publicacion): Boolean =
        publicacion.idAutor == uid && publicacion.estado == EstadoPublicacion.VISIBLE
}

class ComunidadViewModel(
    private val repositorio: RepositorioPublicaciones = RepositorioPublicaciones(),
    private val interacciones: RepositorioInteracciones = RepositorioInteracciones(),
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

            repositorio.opcionesPublicacion(uid)
                .onSuccess { opciones -> _estado.update { it.copy(opciones = opciones) } }

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
            .onSuccess { lista -> _estado.update { it.copy(publicaciones = lista, cargando = false) } }
            .onFailure { e -> _estado.update { it.copy(cargando = false, error = mensajeError(e)) } }
    }

    // ---------- Publicaciones (HU21) ----------

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
        if (_estado.value.guardando) return

        viewModelScope.launch {
            _estado.update { it.copy(guardando = true, error = null) }

            repositorio.crear(_estado.value.uid, texto, visibilidad, idAcademia, idEquipo)
                .onSuccess { publicada ->
                    _estado.update {
                        it.copy(guardando = false, mensaje = "Publicación compartida con ${destino(publicada)}")
                    }
                    alTerminar()
                    cargar()
                }
                .onFailure { e -> _estado.update { it.copy(guardando = false, error = mensajeError(e)) } }
        }
    }

    fun editar(idPublicacion: String, texto: String, alTerminar: () -> Unit = {}) {
        if (_estado.value.guardando) return

        viewModelScope.launch {
            _estado.update { it.copy(guardando = true, error = null) }

            repositorio.editar(idPublicacion, texto, _estado.value.uid)
                .onSuccess {
                    _estado.update { it.copy(guardando = false, mensaje = "Publicación actualizada") }
                    alTerminar()
                    cargar()
                }
                .onFailure { e -> _estado.update { it.copy(guardando = false, error = mensajeError(e)) } }
        }
    }

    fun eliminar(publicacion: Publicacion) {
        viewModelScope.launch {
            repositorio.eliminar(publicacion.id, _estado.value.uid)
                .onSuccess {
                    _estado.update { it.copy(mensaje = "Publicación eliminada") }
                    cargar()
                }
                .onFailure { e -> _estado.update { it.copy(error = mensajeError(e)) } }
        }
    }

    // ---------- Reacciones (HU22) ----------

    fun reaccionar(publicacion: Publicacion, tipo: TipoReaccion) {
        val uid = _estado.value.uid
        if (uid.isBlank()) return

        val anterior = publicacion.reaccionDe(uid)
        val nueva = if (anterior == tipo) null else tipo   // tocar la misma reacción la quita

        // 1) Actualización optimista: la pantalla cambia al instante
        reemplazarPublicacion(publicacion.id) {
            it.copy(reacciones = conReaccion(it.reacciones, uid, nueva))
        }

        // 2) Se guarda en Firestore; si falla, se revierte
        viewModelScope.launch {
            interacciones.reaccionar(publicacion.id, uid, nueva)
                .onFailure { e ->
                    reemplazarPublicacion(publicacion.id) {
                        it.copy(reacciones = conReaccion(it.reacciones, uid, anterior))
                    }
                    _estado.update { it.copy(error = mensajeError(e)) }
                }
        }
    }

    // ---------- Comentarios (HU22) ----------

    fun abrirComentarios(publicacion: Publicacion) {
        _estado.update {
            it.copy(
                publicacionComentarios = publicacion,
                comentarios = emptyList(),
                errorComentarios = null
            )
        }
        cargarComentarios(publicacion.id)
    }

    fun cerrarComentarios() {
        _estado.update { it.copy(publicacionComentarios = null, comentarios = emptyList()) }
    }

    private fun cargarComentarios(idPublicacion: String) {
        viewModelScope.launch {
            _estado.update { it.copy(cargandoComentarios = true, errorComentarios = null) }

            val resultado = interacciones.listarComentarios(idPublicacion, _estado.value.uid)

            // Si mientras cargaba el usuario cerró la hoja o abrió otra publicación, se descarta
            if (_estado.value.publicacionComentarios?.id != idPublicacion) return@launch

            resultado
                .onSuccess { lista ->
                    _estado.update { it.copy(comentarios = lista, cargandoComentarios = false) }
                }
                .onFailure { e ->
                    _estado.update { it.copy(cargandoComentarios = false, errorComentarios = mensajeError(e)) }
                }
        }
    }

    fun comentar(texto: String, alTerminar: () -> Unit = {}) {
        val publicacion = _estado.value.publicacionComentarios ?: return
        if (_estado.value.enviandoComentario) return

        viewModelScope.launch {
            _estado.update { it.copy(enviandoComentario = true, errorComentarios = null) }

            interacciones.comentar(publicacion.id, texto, _estado.value.uid)
                .onSuccess { nuevo ->
                    _estado.update {
                        it.copy(comentarios = it.comentarios + nuevo, enviandoComentario = false)
                    }
                    reemplazarPublicacion(publicacion.id) {
                        it.copy(cantidadComentarios = it.cantidadComentarios + 1)
                    }
                    alTerminar()
                }
                .onFailure { e ->
                    _estado.update { it.copy(enviandoComentario = false, errorComentarios = mensajeError(e)) }
                }
        }
    }

    fun eliminarComentario(comentario: Comentario) {
        viewModelScope.launch {
            interacciones.eliminarComentario(comentario.idPublicacion, comentario.id, _estado.value.uid)
                .onSuccess {
                    _estado.update { it.copy(comentarios = it.comentarios.filter { c -> c.id != comentario.id }) }
                    reemplazarPublicacion(comentario.idPublicacion) {
                        it.copy(cantidadComentarios = (it.cantidadComentarios - 1).coerceAtLeast(0))
                    }
                }
                .onFailure { e -> _estado.update { it.copy(errorComentarios = mensajeError(e)) } }
        }
    }

    fun limpiarMensajes() {
        _estado.update { it.copy(error = null, mensaje = null) }
    }

    // ---------- Ayudas ----------

    // Cambia una publicación en la lista del feed y en la hoja de comentarios (si está abierta)
    private fun reemplazarPublicacion(id: String, cambio: (Publicacion) -> Publicacion) {
        _estado.update { e ->
            e.copy(
                publicaciones = e.publicaciones.map { if (it.id == id) cambio(it) else it },
                publicacionComentarios = e.publicacionComentarios?.let { if (it.id == id) cambio(it) else it }
            )
        }
    }

    private fun conReaccion(
        actuales: Map<String, String>,
        uid: String,
        tipo: TipoReaccion?
    ): Map<String, String> =
        if (tipo == null) actuales - uid else actuales + (uid to tipo.name)

    private fun destino(publicacion: Publicacion): String =
        when (publicacion.visibilidad) {
            VisibilidadPublicacion.COMUNIDAD -> "Comunidad SportPro"
            VisibilidadPublicacion.ACADEMIA -> publicacion.nombreAcademia.ifBlank { "tu academia" }
            VisibilidadPublicacion.EQUIPO -> publicacion.nombreEquipo.ifBlank { "tu equipo" }
        }

    private fun mensajeError(e: Throwable): String =
        e.message ?: "No se pudo completar la operación"
}