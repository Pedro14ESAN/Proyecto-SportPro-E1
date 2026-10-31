package com.example.projectsportpro2026_2.ui.convocatoria

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectsportpro2026_2.datos.modelo.Jugador
import com.example.projectsportpro2026_2.datos.modelo.convocatoria.JugadorConvocado
import com.example.projectsportpro2026_2.datos.modelo.partido.EstadoPartido
import com.example.projectsportpro2026_2.datos.modelo.partido.Partido
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioAutenticacion
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioJugadores
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioPartidos
import com.example.projectsportpro2026_2.datos.repositorio.convocatoria.RepositorioConvocatorias
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EstadoConvocatoria(
    val partido: Partido? = null,
    val jugadores: List<Jugador> = emptyList(),
    val idsSeleccionados: Set<String> = emptySet(),
    val convocados: Map<String, JugadorConvocado> = emptyMap(),
    val publicada: Boolean = false,
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null
)

class ConvocatoriaViewModel(
    private val repositorioConvocatorias: RepositorioConvocatorias =
        RepositorioConvocatorias(),

    private val repositorioJugadores: RepositorioJugadores =
        RepositorioJugadores(),

    private val repositorioPartidos: RepositorioPartidos =
        RepositorioPartidos(),

    private val autenticacion: RepositorioAutenticacion =
        RepositorioAutenticacion()
) : ViewModel() {

    private val _estado =
        MutableStateFlow(EstadoConvocatoria())

    val estado: StateFlow<EstadoConvocatoria> =
        _estado.asStateFlow()

    private var idPartidoActual = ""

    private val uid: String
        get() = autenticacion.uidActual().orEmpty()

    fun iniciar(
        idPartido: String
    ) {
        idPartidoActual = idPartido
        cargar()
    }

    fun cargar() {
        if (idPartidoActual.isBlank()) {
            return
        }

        viewModelScope.launch {

            _estado.update {
                it.copy(
                    cargando = true,
                    error = null,
                )
            }

            try {

                val partido = repositorioPartidos
                    .obtenerPartido(
                        idPartidoActual,
                        uid
                    )
                    .getOrThrow()

                val jugadores = repositorioJugadores
                    .listarJugadores(
                        partido.idEquipo
                    )
                    .getOrThrow()

                val convocatoria =
                    repositorioConvocatorias
                        .obtenerConvocatoria(
                            idPartidoActual,
                            uid
                        )
                        .getOrThrow()

                val listaConvocados =
                    repositorioConvocatorias
                        .listarConvocados(
                            idPartidoActual,
                            uid
                        )
                        .getOrThrow()
                    // Solo conservamos convocados que todavía pertenecen
                    // al equipo actual del partido.
                val idsJugadoresEquipo =
                    jugadores
                        .map { it.id }
                        .toSet()

                val convocadosValidos =
                    listaConvocados.filter {
                        it.idJugador in idsJugadoresEquipo
                    }

                val mapaConvocados =
                    convocadosValidos.associateBy {
                        it.idJugador
                    }

                _estado.update {
                    it.copy(
                        partido = partido,
                        jugadores = jugadores,
                        idsSeleccionados =
                            mapaConvocados.keys,
                        convocados =
                            mapaConvocados,
                        publicada =
                            convocatoria?.publicada == true,
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

    fun alternarJugador(
        jugador: Jugador
    ) {
        val estadoActual = _estado.value
        val partido = estadoActual.partido

        if (partido == null) {
            return
        }

        if (partido.estado != EstadoPartido.PROGRAMADO) {
            _estado.update {
                it.copy(
                    error = "La convocatoria ya no puede modificarse porque el partido comenzó"
                )
            }
            return
        }

        if (!jugador.activo) {
            _estado.update {
                it.copy(
                    error = "${jugador.nombreCompleto()} está inactivo y no puede ser convocado"
                )
            }
            return
        }

        _estado.update { actual ->

            val nuevaSeleccion =
                actual.idsSeleccionados.toMutableSet()

            if (jugador.id in nuevaSeleccion) {
                nuevaSeleccion.remove(
                    jugador.id
                )
            } else {
                nuevaSeleccion.add(
                    jugador.id
                )
            }

            actual.copy(
                idsSeleccionados = nuevaSeleccion,
                error = null,
                mensaje = null
            )
        }
    }

    fun publicar(
        alTerminar: () -> Unit = {}
    ) {
        val estadoActual = _estado.value

        if (estadoActual.idsSeleccionados.isEmpty()) {
            _estado.update {
                it.copy(
                    error = "Selecciona al menos un jugador"
                )
            }
            return
        }

        viewModelScope.launch {

            _estado.update {
                it.copy(
                    guardando = true,
                    error = null,
                    mensaje = null
                )
            }

            repositorioConvocatorias
                .publicarConvocatoria(
                    idPartido = idPartidoActual,
                    idsJugadores =
                        estadoActual.idsSeleccionados.toList(),
                    uid = uid
                )
                .onSuccess {

                    _estado.update {
                        it.copy(
                            guardando = false,
                            publicada = true,
                            mensaje = "Convocatoria publicada correctamente"
                        )
                    }

                    cargar()
                    alTerminar()
                }
                .onFailure { e ->

                    _estado.update {
                        it.copy(
                            guardando = false,
                            error = mensajeError(e)
                        )
                    }
                }
        }
    }

    fun limpiarMensaje() {
        _estado.update {
            it.copy(
                mensaje = null
            )
        }
    }

    fun limpiarError() {
        _estado.update {
            it.copy(
                error = null
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