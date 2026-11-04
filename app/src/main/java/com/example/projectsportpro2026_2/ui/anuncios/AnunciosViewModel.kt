package com.example.projectsportpro2026_2.ui.anuncios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectsportpro2026_2.datos.modelo.Anuncio
import com.example.projectsportpro2026_2.datos.modelo.EstadoAnuncio
import com.example.projectsportpro2026_2.datos.modelo.Rol
import com.example.projectsportpro2026_2.datos.modelo.TipoAudiencia
import com.example.projectsportpro2026_2.datos.repositorio.OpcionesPublicacion
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioAnuncios
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioAutenticacion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioNotificaciones
// Pestañas del Figma: Todos / Activos / Archivados
enum class FiltroAnuncios { TODOS, ACTIVOS, ARCHIVADOS }

// Todo lo que la pantalla necesita saber para dibujarse
data class EstadoAnuncios(
    val uid: String = "",
    val rol: Rol? = null,
    val anuncios: List<Anuncio> = emptyList(),
    val filtro: FiltroAnuncios = FiltroAnuncios.TODOS,
    val opcionesPublicacion: List<OpcionesPublicacion> = emptyList(),
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null
) {
    // Si tiene al menos una academia donde publicar, se muestra el botón "+ Nuevo"
    val puedePublicar: Boolean
        get() = opcionesPublicacion.isNotEmpty()

    // La lista que realmente se muestra, según la pestaña elegida
    val anunciosFiltrados: List<Anuncio>
        get() = when (filtro) {
            FiltroAnuncios.TODOS -> anuncios
            FiltroAnuncios.ACTIVOS -> anuncios.filter { it.estado == EstadoAnuncio.ACTIVO }
            FiltroAnuncios.ARCHIVADOS -> anuncios.filter { it.estado == EstadoAnuncio.ARCHIVADO }
        }

    // Para mostrar u ocultar los botones Editar / Archivar en cada tarjeta
    fun puedeGestionar(anuncio: Anuncio): Boolean =
        anuncio.idAutor == uid ||
                opcionesPublicacion.any {
                    it.academia.id == anuncio.idAcademia && it.puedePublicarATodos
                }
}

class AnunciosViewModel(
    private val repositorio: RepositorioAnuncios = RepositorioAnuncios(),
    private val autenticacion: RepositorioAutenticacion = RepositorioAutenticacion(),
    private val notificaciones: RepositorioNotificaciones = RepositorioNotificaciones()
) : ViewModel() {

    private val _estado = MutableStateFlow(EstadoAnuncios())
    val estado: StateFlow<EstadoAnuncios> = _estado.asStateFlow()

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
            _estado.update { it.copy(uid = uid, rol = perfil?.rol) }

            // Solo DT y ADM pueden publicar: se cargan sus opciones de audiencia
            if (perfil?.rol == Rol.ADM || perfil?.rol == Rol.DT) {
                repositorio.opcionesPublicacion(uid)
                    .onSuccess { opciones ->
                        _estado.update { it.copy(opcionesPublicacion = opciones) }
                    }
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

        repositorio.listarParaUsuario(uid)
            .onSuccess { lista ->
                _estado.update { it.copy(anuncios = lista, cargando = false) }
            }
            .onFailure { e ->
                _estado.update { it.copy(cargando = false, error = mensajeError(e)) }
            }
    }

    // ---------- Acciones de la pantalla ----------

    fun cambiarFiltro(filtro: FiltroAnuncios) {
        _estado.update { it.copy(filtro = filtro) }
    }

    fun crear(
        titulo: String,
        contenido: String,
        idAcademia: String,
        tipoAudiencia: TipoAudiencia,
        idEquipo: String,
        rolDestino: Rol,
        alTerminar: () -> Unit = {}
    ) {
        if (_estado.value.guardando) return

        viewModelScope.launch {
            _estado.update { it.copy(guardando = true, error = null) }

            repositorio.crear(
                uid = _estado.value.uid,
                titulo = titulo,
                contenido = contenido,
                idAcademia = idAcademia,
                tipoAudiencia = tipoAudiencia,
                idEquipo = idEquipo,
                rolDestino = rolDestino
            )
                .onSuccess { anuncio ->
                    _estado.update { it.copy(guardando = false, mensaje = "Anuncio publicado") }
                    alTerminar()
                    cargar()
                    // HU20: avisar a la audiencia del anuncio
                    notificaciones.notificarAnuncio(anuncio)
                }
                .onFailure { e ->
                    _estado.update { it.copy(guardando = false, error = mensajeError(e)) }
                }
        }
    }

    fun editar(
        idAnuncio: String,
        titulo: String,
        contenido: String,
        alTerminar: () -> Unit = {}
    ) {
        if (_estado.value.guardando) return

        viewModelScope.launch {
            _estado.update { it.copy(guardando = true, error = null) }

            repositorio.editar(idAnuncio, titulo, contenido, _estado.value.uid)
                .onSuccess {
                    _estado.update { it.copy(guardando = false, mensaje = "Anuncio actualizado") }
                    alTerminar()
                    cargar()
                }
                .onFailure { e ->
                    _estado.update { it.copy(guardando = false, error = mensajeError(e)) }
                }
        }
    }

    fun archivar(anuncio: Anuncio) =
        cambiarEstado(anuncio, EstadoAnuncio.ARCHIVADO, "Anuncio archivado")

    fun reactivar(anuncio: Anuncio) =
        cambiarEstado(anuncio, EstadoAnuncio.ACTIVO, "Anuncio reactivado")

    private fun cambiarEstado(anuncio: Anuncio, nuevo: EstadoAnuncio, textoExito: String) {
        viewModelScope.launch {
            repositorio.cambiarEstado(anuncio.id, nuevo, _estado.value.uid)
                .onSuccess {
                    _estado.update { it.copy(mensaje = textoExito) }
                    cargar()
                }
                .onFailure { e ->
                    _estado.update { it.copy(error = mensajeError(e)) }
                }
        }
    }

    // La pantalla llama esto después de mostrar el mensaje, para que no se repita
    fun limpiarMensajes() {
        _estado.update { it.copy(error = null, mensaje = null) }
    }

    private fun mensajeError(e: Throwable): String =
        e.message ?: "No se pudo completar la operación"
}