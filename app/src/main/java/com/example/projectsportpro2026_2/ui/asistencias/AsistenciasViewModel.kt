package com.example.projectsportpro2026_2.ui.asistencias

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectsportpro2026_2.datos.modelo.Asistencia
import com.example.projectsportpro2026_2.datos.modelo.Entrenamiento
import com.example.projectsportpro2026_2.datos.modelo.EstadoAsistencia
import com.example.projectsportpro2026_2.datos.modelo.Jugador
import com.example.projectsportpro2026_2.datos.modelo.Rol
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioAsistencias
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioAutenticacion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EstadoAsistenciasUi(
    val rol: Rol? = null,
    val sesiones: List<Entrenamiento> = emptyList(),
    val idSesionSeleccionada: String = "",
    val jugadores: List<Jugador> = emptyList(),
    val estados: Map<String, EstadoAsistencia> = emptyMap(),
    val asistenciaRegistrada: Boolean = false,
    val modoCorreccion: Boolean = false,
    val jugadoresConsulta: List<Jugador> = emptyList(),
    val idJugadorConsulta: String = "",
    val historial: List<Asistencia> = emptyList(),
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null
)

class AsistenciasViewModel(
    private val repositorio:
    RepositorioAsistencias =
        RepositorioAsistencias(),

    private val autenticacion:
    RepositorioAutenticacion =
        RepositorioAutenticacion()
) : ViewModel() {

    private val _estado =
        MutableStateFlow(
            EstadoAsistenciasUi()
        )

    val estado:
            StateFlow<EstadoAsistenciasUi> =
        _estado.asStateFlow()

    private var iniciado =
        false

    fun iniciar() {

        if (iniciado) return

        iniciado =
            true

        viewModelScope.launch {

            val uid =
                autenticacion.uidActual()
                    ?: return@launch

            _estado.update {
                it.copy(
                    cargando = true,
                    error = null
                )
            }

            try {

                val perfil =
                    autenticacion
                        .obtenerPerfil(uid)
                        ?: throw IllegalStateException(
                            "Tu cuenta no tiene perfil"
                        )

                if (
                    perfil.rol ==
                    Rol.DT ||
                    perfil.rol ==
                    Rol.ADM
                ) {

                    val sesiones =
                        repositorio
                            .listarSesionesGestionables(
                                uid
                            )
                            .getOrThrow()

                    val primera =
                        sesiones
                            .firstOrNull()
                            ?.id
                            .orEmpty()

                    _estado.update {
                        it.copy(
                            rol =
                                perfil.rol,
                            sesiones =
                                sesiones,
                            idSesionSeleccionada =
                                primera,
                            cargando =
                                false
                        )
                    }

                    if (
                        primera.isNotBlank()
                    ) {
                        seleccionarSesion(
                            primera
                        )
                    }

                } else {

                    val jugadores =
                        repositorio
                            .listarJugadoresConsulta(
                                uid
                            )
                            .getOrThrow()

                    val primero =
                        jugadores
                            .firstOrNull()
                            ?.id
                            .orEmpty()

                    _estado.update {
                        it.copy(
                            rol =
                                perfil.rol,
                            jugadoresConsulta =
                                jugadores,
                            idJugadorConsulta =
                                primero,
                            cargando =
                                false
                        )
                    }

                    if (
                        primero.isNotBlank()
                    ) {
                        seleccionarJugadorConsulta(
                            primero
                        )
                    }
                }

            } catch (
                e: Exception
            ) {

                _estado.update {
                    it.copy(
                        cargando =
                            false,
                        error =
                            e.message
                    )
                }
            }
        }
    }

    fun seleccionarSesion(
        idSesion: String
    ) {

        _estado.update {
            it.copy(
                idSesionSeleccionada =
                    idSesion,
                cargando =
                    true,
                modoCorreccion =
                    false,
                error =
                    null,
                mensaje =
                    null
            )
        }

        viewModelScope.launch {

            val uid =
                autenticacion.uidActual()
                    ?: return@launch

            try {

                val jugadores =
                    repositorio
                        .listarJugadoresSesion(
                            idSesion,
                            uid
                        )
                        .getOrThrow()

                val registros =
                    repositorio
                        .listarAsistenciasSesion(
                            idSesion,
                            uid
                        )
                        .getOrThrow()

                _estado.update {
                    it.copy(
                        jugadores =
                            jugadores,
                        estados =
                            registros.associate {
                                    asistencia ->

                                asistencia.idJugador to
                                        asistencia.estado
                            },
                        asistenciaRegistrada =
                            registros.isNotEmpty(),
                        modoCorreccion =
                            false,
                        cargando =
                            false
                    )
                }

            } catch (
                e: Exception
            ) {

                _estado.update {
                    it.copy(
                        cargando =
                            false,
                        error =
                            e.message
                    )
                }
            }
        }
    }

    fun cambiarEstado(
        idJugador: String,
        nuevoEstado: EstadoAsistencia
    ) {

        val actual =
            _estado.value

        if (
            actual.asistenciaRegistrada &&
            !actual.modoCorreccion
        ) {
            return
        }

        _estado.update {
            it.copy(
                estados =
                    it.estados +
                            (
                                    idJugador to
                                            nuevoEstado
                                    ),
                error =
                    null
            )
        }
    }

    fun iniciarCorreccion() {

        if (
            !_estado.value
                .asistenciaRegistrada
        ) {
            return
        }

        _estado.update {
            it.copy(
                modoCorreccion =
                    true,
                error =
                    null,
                mensaje =
                    null
            )
        }
    }

    fun cancelarCorreccion() {

        val id =
            _estado.value
                .idSesionSeleccionada

        if (
            id.isNotBlank()
        ) {
            seleccionarSesion(id)
        }
    }

    fun guardar() {

        val actual =
            _estado.value

        if (
            actual.idSesionSeleccionada
                .isBlank()
        ) {
            return
        }

        if (
            actual.asistenciaRegistrada &&
            !actual.modoCorreccion
        ) {

            _estado.update {
                it.copy(
                    error =
                        "La asistencia ya fue registrada. Usa Corregir asistencia."
                )
            }

            return
        }

        if (
            actual.estados.size <
            actual.jugadores.size
        ) {

            _estado.update {
                it.copy(
                    error =
                        "Registra la asistencia de todos los jugadores"
                )
            }

            return
        }

        viewModelScope.launch {

            val uid =
                autenticacion.uidActual()
                    ?: return@launch

            _estado.update {
                it.copy(
                    guardando =
                        true,
                    error =
                        null
                )
            }

            repositorio
                .guardarAsistencias(
                    idEntrenamiento =
                        actual.idSesionSeleccionada,
                    estados =
                        actual.estados,
                    uid =
                        uid,
                    permitirCorreccion =
                        actual.modoCorreccion
                )
                .onSuccess {

                    val mensaje =
                        if (
                            actual.modoCorreccion
                        ) {
                            "Asistencia corregida correctamente"
                        } else {
                            "Asistencia registrada correctamente"
                        }

                    _estado.update {
                        it.copy(
                            guardando =
                                false,
                            mensaje =
                                mensaje
                        )
                    }

                    seleccionarSesion(
                        actual.idSesionSeleccionada
                    )
                }
                .onFailure { error ->

                    _estado.update {
                        it.copy(
                            guardando =
                                false,
                            error =
                                error.message
                        )
                    }
                }
        }
    }

    fun seleccionarJugadorConsulta(
        idJugador: String
    ) {

        _estado.update {
            it.copy(
                idJugadorConsulta =
                    idJugador,
                cargando =
                    true
            )
        }

        viewModelScope.launch {

            val uid =
                autenticacion.uidActual()
                    ?: return@launch

            repositorio
                .listarHistorialJugador(
                    idJugador =
                        idJugador,
                    uid =
                        uid
                )
                .onSuccess { historial ->

                    _estado.update {
                        it.copy(
                            historial =
                                historial,
                            cargando =
                                false,
                            error =
                                null
                        )
                    }
                }
                .onFailure { error ->

                    _estado.update {
                        it.copy(
                            cargando =
                                false,
                            error =
                                error.message
                        )
                    }
                }
        }
    }
}