package com.example.projectsportpro2026_2.ui.partidos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectsportpro2026_2.datos.modelo.Equipo
import com.example.projectsportpro2026_2.datos.modelo.partido.EstadoPartido
import com.example.projectsportpro2026_2.datos.modelo.partido.Partido
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioAcademias
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioAutenticacion
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioPartidos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.projectsportpro2026_2.datos.repositorio.evento.MarcadorPartido
import com.example.projectsportpro2026_2.datos.repositorio.evento.RepositorioEventosPartido

data class EstadoPartidos(
    val partidos: List<Partido> = emptyList(),
    val equipos: List<Equipo> = emptyList(),
    val partidoSeleccionado: Partido? = null,
    val marcadorSeleccionado: MarcadorPartido? = null,
    val marcadoresPorPartido: Map<String, MarcadorPartido> = emptyMap(),
    val cargando: Boolean = false,
    val error: String? = null
)

class PartidosViewModel(
    private val repositorio: RepositorioPartidos = RepositorioPartidos(),
    private val repositorioAcademias: RepositorioAcademias = RepositorioAcademias(),
    private val autenticacion: RepositorioAutenticacion = RepositorioAutenticacion(),
    private val repositorioEventos: RepositorioEventosPartido = RepositorioEventosPartido()
) : ViewModel() {

    private val _estado = MutableStateFlow(EstadoPartidos())
    val estado: StateFlow<EstadoPartidos> = _estado.asStateFlow()

    private var idAcademia = ""

    private val uid: String
        get() = autenticacion.uidActual().orEmpty()

    fun iniciar(id: String) {
        idAcademia = id
        cargar()
    }

    fun limpiarError() {
        _estado.update { it.copy(error = null) }
    }

    fun cargar() {
        viewModelScope.launch {
            _estado.update {
                it.copy(
                    cargando = true,
                    error = null
                )
            }

            try {
                val equipos = repositorioAcademias
                    .listarEquipos(idAcademia)
                    .getOrThrow()
                    .filter { it.activo }

                val partidos = repositorio
                    .listarPartidos(idAcademia, uid)
                    .getOrThrow()

                val marcadores = mutableMapOf<String, MarcadorPartido>()

                partidos
                    .filter {
                        it.estado == EstadoPartido.FINALIZADO
                    }
                    .forEach { partido ->

                        val eventos =
                            repositorioEventos
                                .listarEventos(
                                    idPartido = partido.id,
                                    uid = uid
                                )
                                .getOrThrow()

                        marcadores[partido.id] =
                            repositorioEventos
                                .calcularMarcador(eventos)
                    }

                _estado.update {
                    it.copy(
                        equipos = equipos,
                        partidos = partidos,
                        marcadoresPorPartido = marcadores,
                        cargando = false
                    )
                }
            } catch (e: Exception) {
                _estado.update {
                    it.copy(
                        cargando = false,
                        error = mensaje(e)
                    )
                }
            }
        }

    }

    fun crearPartido(
        idEquipo: String,
        nombreRival: String,
        fecha: String,
        hora: String,
        ubicacion: String,
        alTerminar: (Partido) -> Unit
    ) {
        viewModelScope.launch {

            _estado.update {
                it.copy(
                    cargando = true,
                    error = null
                )
            }

            repositorio.crearPartido(
                idEquipo = idEquipo,
                nombreRival = nombreRival,
                fecha = fecha,
                hora = hora,
                ubicacion = ubicacion,
                uid = uid
            )
                .onSuccess { partido ->
                    _estado.update {
                        it.copy(cargando = false)
                    }

                    alTerminar(partido)
                }
                .onFailure { e ->
                    _estado.update {
                        it.copy(
                            cargando = false,
                            error = mensaje(e)
                        )
                    }
                }
        }
    }

    fun cargarPartido(idPartido: String) {
        viewModelScope.launch {

            _estado.update {
                it.copy(
                    cargando = true,
                    error = null,
                    marcadorSeleccionado = null
                )
            }

            try {
                val partido = repositorio
                    .obtenerPartido(idPartido, uid)
                    .getOrThrow()

                val equipos = repositorioAcademias
                    .listarEquipos(partido.idAcademia)
                    .getOrThrow()

                val marcador =
                    if (
                        partido.estado == EstadoPartido.EN_CURSO ||
                        partido.estado == EstadoPartido.FINALIZADO
                    ) {

                        val eventos =
                            repositorioEventos
                                .listarEventos(
                                    idPartido = partido.id,
                                    uid = uid
                                )
                                .getOrThrow()

                        repositorioEventos
                            .calcularMarcador(eventos)

                    } else {
                        null
                    }

                _estado.update {
                    it.copy(
                        partidoSeleccionado = partido,
                        equipos = equipos,
                        marcadorSeleccionado = marcador,
                        cargando = false
                    )
                }

            } catch (e: Exception) {

                _estado.update {
                    it.copy(
                        cargando = false,
                        error = mensaje(e)
                    )
                }
            }
        }
    }

    fun editarPartido(
        idPartido: String,
        idEquipo: String,
        nombreRival: String,
        fecha: String,
        hora: String,
        ubicacion: String,
        alTerminar: () -> Unit
    ) {
        viewModelScope.launch {

            _estado.update {
                it.copy(
                    cargando = true,
                    error = null
                )
            }

            repositorio.editarPartido(
                idPartido = idPartido,
                idEquipo = idEquipo,
                nombreRival = nombreRival,
                fecha = fecha,
                hora = hora,
                ubicacion = ubicacion,
                uid = uid
            )
                .onSuccess {

                    _estado.update {
                        it.copy(cargando = false)
                    }

                    alTerminar()
                }
                .onFailure { e ->

                    _estado.update {
                        it.copy(
                            cargando = false,
                            error = mensaje(e)
                        )
                    }
                }
        }
    }

    fun cambiarEstado(
        partido: Partido,
        nuevoEstado: EstadoPartido,
        alTerminar: () -> Unit = {}
    ) {
        viewModelScope.launch {

            _estado.update {
                it.copy(
                    cargando = true,
                    error = null
                )
            }

            repositorio.cambiarEstadoPartido(
                idPartido = partido.id,
                nuevoEstado = nuevoEstado,
                uid = uid
            )
                .onSuccess {
                    cargarPartido(partido.id)
                    alTerminar()
                }
                .onFailure { e ->
                    _estado.update {
                        it.copy(
                            cargando = false,
                            error = mensaje(e)
                        )
                    }
                }
        }
    }

    private fun mensaje(e: Throwable): String =
        when (e) {
            is IllegalArgumentException,
            is IllegalStateException ->
                e.message ?: "No se pudo completar la operación"

            else ->
                "No se pudo completar la operación. Revisa tu conexión e inténtalo nuevamente"
        }
}