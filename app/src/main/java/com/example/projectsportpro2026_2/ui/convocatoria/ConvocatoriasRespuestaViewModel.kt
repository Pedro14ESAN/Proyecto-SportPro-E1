package com.example.projectsportpro2026_2.ui.convocatoria

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectsportpro2026_2.datos.modelo.Rol
import com.example.projectsportpro2026_2.datos.modelo.convocatoria.EstadoDisponibilidad
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioAutenticacion
import com.example.projectsportpro2026_2.datos.repositorio.convocatoria.ConvocatoriaParaResponder
import com.example.projectsportpro2026_2.datos.repositorio.convocatoria.RepositorioConvocatorias
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EstadoConvocatoriasRespuesta(
    val convocatorias: List<ConvocatoriaParaResponder> = emptyList(),
    val rol: Rol? = null,
    val cargando: Boolean = false,
    val idRespondiendo: String? = null,
    val error: String? = null,
    val mensaje: String? = null
)

class ConvocatoriasRespuestaViewModel(
    private val repositorioConvocatorias: RepositorioConvocatorias =
        RepositorioConvocatorias(),

    private val autenticacion: RepositorioAutenticacion =
        RepositorioAutenticacion()
) : ViewModel() {

    private val _estado =
        MutableStateFlow(
            EstadoConvocatoriasRespuesta()
        )

    val estado: StateFlow<EstadoConvocatoriasRespuesta> =
        _estado.asStateFlow()

    private val uid: String
        get() = autenticacion.uidActual().orEmpty()

    fun iniciar() {
        cargar()
    }

    fun cargar(
        mantenerMensaje: Boolean = false
    ) {
        viewModelScope.launch {

            _estado.update {
                it.copy(
                    cargando = true,
                    error = null,
                    mensaje = if (mantenerMensaje) {
                        it.mensaje
                    } else {
                        null
                    }
                )
            }

            try {

                if (uid.isBlank()) {
                    throw IllegalStateException(
                        "No hay una sesión activa"
                    )
                }

                val perfil =
                    autenticacion.obtenerPerfil(uid)
                        ?: throw IllegalStateException(
                            "El usuario no tiene un perfil válido"
                        )

                val convocatorias =
                    when (perfil.rol) {

                        Rol.JUG ->
                            repositorioConvocatorias
                                .listarParaJugador(uid)
                                .getOrThrow()

                        Rol.PAD ->
                            repositorioConvocatorias
                                .listarParaApoderado(uid)
                                .getOrThrow()

                        else ->
                            throw IllegalStateException(
                                "Este usuario no puede responder convocatorias"
                            )
                    }

                _estado.update {
                    it.copy(
                        convocatorias = convocatorias,
                        rol = perfil.rol,
                        cargando = false
                    )
                }

            } catch (e: Exception) {

                _estado.update {
                    it.copy(
                        cargando = false,
                        error = mensajeError(e)
                    )
                }
            }
        }
    }

    fun responder(
        convocatoria: ConvocatoriaParaResponder,
        nuevoEstado: EstadoDisponibilidad
    ) {
        if (
            nuevoEstado != EstadoDisponibilidad.DISPONIBLE &&
            nuevoEstado != EstadoDisponibilidad.NO_DISPONIBLE
        ) {
            return
        }

        viewModelScope.launch {

            val clave =
                "${convocatoria.partido.id}-${convocatoria.jugador.id}"

            _estado.update {
                it.copy(
                    idRespondiendo = clave,
                    error = null,
                    mensaje = null
                )
            }

            repositorioConvocatorias
                .responderDisponibilidad(
                    idPartido = convocatoria.partido.id,
                    idJugador = convocatoria.jugador.id,
                    nuevoEstado = nuevoEstado,
                    uid = uid
                )
                .onSuccess {

                    val textoEstado =
                        when (nuevoEstado) {

                            EstadoDisponibilidad.DISPONIBLE ->
                                "Disponible"

                            EstadoDisponibilidad.NO_DISPONIBLE ->
                                "No disponible"

                            else ->
                                ""
                        }

                    _estado.update {
                        it.copy(
                            idRespondiendo = null,
                            mensaje =
                                "Disponibilidad actualizada: $textoEstado"
                        )
                    }

                    cargar(
                        mantenerMensaje = true
                    )
                }
                .onFailure { e ->

                    _estado.update {
                        it.copy(
                            idRespondiendo = null,
                            error = mensajeError(e)
                        )
                    }
                }
        }
    }

    fun limpiarError() {
        _estado.update {
            it.copy(
                error = null
            )
        }
    }

    fun limpiarMensaje() {
        _estado.update {
            it.copy(
                mensaje = null
            )
        }
    }

    private fun mensajeError(
        e: Throwable
    ): String =
        when (e) {

            is IllegalArgumentException,
            is IllegalStateException ->
                e.message
                    ?: "No se pudo completar la operación"

            else ->
                "No se pudo completar la operación. Revisa tu conexión e inténtalo nuevamente"
        }
}