package com.example.projectsportpro2026_2.ui.sincronizacion

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectsportpro2026_2.datos.modelo.evento.EquipoEvento
import com.example.projectsportpro2026_2.datos.modelo.partido.Partido
import com.example.projectsportpro2026_2.datos.modelo.sincronizacion.EstadoConexionPartido
import com.example.projectsportpro2026_2.datos.modelo.sincronizacion.EstadoSincronizacionEvento
import com.example.projectsportpro2026_2.datos.modelo.sincronizacion.EventoSincronizado
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioAutenticacion
import com.example.projectsportpro2026_2.datos.repositorio.evento.MarcadorPartido
import com.example.projectsportpro2026_2.datos.repositorio.sincronizacion.RepositorioSincronizacion
import com.example.projectsportpro2026_2.datos.sincronizacion.EventoRechazado
import com.example.projectsportpro2026_2.datos.sincronizacion.IdentificadorDispositivo
import com.example.projectsportpro2026_2.datos.sincronizacion.MonitorConexion
import com.example.projectsportpro2026_2.datos.sincronizacion.RegistroRechazos
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/*
 * HU16 - Estado de sincronización del partido en vivo
 * (pantalla del operador ADM/DT).
 */
data class EstadoSincronizacionUi(
    val cargado: Boolean = false,

    // Eventos activos con su estado de sync
    val eventos: List<EventoSincronizado> = emptyList(),

    // Eventos rechazados por el servidor (requieren revisión)
    val rechazados: List<EventoRechazado> = emptyList(),

    // Partido actualizado en tiempo real
    val partido: Partido? = null,

    val hayRed: Boolean = true,
    val simulandoSinConexion: Boolean = false,
    val cambiandoConexion: Boolean = false,
    val reintentando: Boolean = false,

    /*
     * Caso alterno: false si el partido/catálogo/alineación
     * nunca se cargaron en este dispositivo y por eso no se
     * puede registrar sin conexión de forma confiable.
     */
    val registroOfflineDisponible: Boolean = true,

    // true mientras los datos no estén confirmados por el servidor
    val datosDesdeCache: Boolean = false,

    // momento de la última confirmación completa del servidor
    val ultimaSincronizacion: Long = 0L,

    // Evento en conflicto abierto por el operador
    val eventoConflicto: EventoSincronizado? = null,
    val resolviendoConflicto: Boolean = false,

    val error: String? = null,
    val mensaje: String? = null
) {

    val sinConexion: Boolean
        get() = simulandoSinConexion || !hayRed

    val pendientes: Int
        get() = eventos.count {
            it.estado == EstadoSincronizacionEvento.PENDIENTE
        }

    // "Requiere revisión": conflictos detectados + rechazados
    val conflictos: Int
        get() = eventos.count {
            it.estado == EstadoSincronizacionEvento.CONFLICTO
        } + rechazados.size

    /*
     * Goles que todavía no están confirmados por todos
     * los dispositivos (pendientes, en conflicto o
     * rechazados por el servidor).
     */
    val golesEnRevision: Int
        get() = eventos.count {
            it.evento.afectaMarcador &&
                    it.estado != EstadoSincronizacionEvento.SINCRONIZADO
        } + rechazados.count {
            it.evento.afectaMarcador
        }

    /*
     * Hay información no sincronizada que puede cambiar
     * el resultado → el marcador NO es definitivo.
     */
    val marcadorProvisional: Boolean
        get() = golesEnRevision > 0

    /*
     * Marcador calculado con los eventos activos que
     * conoce este dispositivo (incluye los pendientes,
     * excluye los que están en conflicto hasta revisarlos).
     */
    val marcador: MarcadorPartido
        get() {
            val validos =
                eventos.filter {
                    it.evento.afectaMarcador &&
                            it.estado != EstadoSincronizacionEvento.CONFLICTO
                }

            return MarcadorPartido(
                propio = validos.count {
                    it.evento.equipoEvento == EquipoEvento.PROPIO
                },
                rival = validos.count {
                    it.evento.equipoEvento == EquipoEvento.RIVAL
                }
            )
        }

    val estadoGeneral: EstadoConexionPartido
        get() = when {
            sinConexion ->
                EstadoConexionPartido.SIN_CONEXION

            pendientes > 0 || datosDesdeCache ->
                EstadoConexionPartido.SINCRONIZANDO

            else ->
                EstadoConexionPartido.SINCRONIZADO
        }
}

class SincronizacionViewModel(
    aplicacion: Application
) : AndroidViewModel(aplicacion) {

    private val repositorio =
        RepositorioSincronizacion()

    private val autenticacion =
        RepositorioAutenticacion()

    private val monitor =
        MonitorConexion(aplicacion)

    private val _estado =
        MutableStateFlow(EstadoSincronizacionUi())

    val estado: StateFlow<EstadoSincronizacionUi> =
        _estado.asStateFlow()

    private var idPartidoActual = ""

    private var trabajos: List<Job> = emptyList()

    init {
        IdentificadorDispositivo.inicializar(aplicacion)
        RegistroRechazos.inicializar(aplicacion)

        viewModelScope.launch {
            monitor.conectado.collect { conectado ->
                _estado.update {
                    it.copy(hayRed = conectado)
                }

                verificarRegistroOffline()
            }
        }

        viewModelScope.launch {
            RegistroRechazos.rechazados.collect { lista ->
                _estado.update {
                    it.copy(
                        rechazados = RegistroRechazos.delPartido(
                            lista,
                            idPartidoActual
                        )
                    )
                }
            }
        }
    }

    fun iniciar(idPartido: String) {
        if (
            idPartido.isBlank() ||
            idPartido == idPartidoActual
        ) {
            return
        }

        idPartidoActual = idPartido

        _estado.update {
            it.copy(
                rechazados = RegistroRechazos.delPartido(
                    RegistroRechazos.rechazados.value,
                    idPartido
                )
            )
        }

        trabajos.forEach { it.cancel() }

        trabajos = listOf(
            viewModelScope.launch {
                repositorio
                    .escucharEventos(idPartido)
                    .catch { e ->
                        _estado.update {
                            it.copy(
                                error = "No se pudo escuchar la cronología: " +
                                        (e.message ?: "error desconocido")
                            )
                        }
                    }
                    .collect { instantanea ->

                        val eventos =
                            repositorio.clasificar(instantanea)

                        val confirmado =
                            !instantanea.desdeCache &&
                                    !instantanea.escriturasPendientes

                        _estado.update { actual ->
                            actual.copy(
                                cargado = true,
                                eventos = eventos,
                                datosDesdeCache = instantanea.desdeCache,
                                ultimaSincronizacion =
                                    if (confirmado) {
                                        System.currentTimeMillis()
                                    } else {
                                        actual.ultimaSincronizacion
                                    },
                                // Si el evento abierto cambió, lo refrescamos
                                eventoConflicto =
                                    actual.eventoConflicto?.let { abierto ->
                                        eventos.firstOrNull {
                                            it.evento.id == abierto.evento.id &&
                                                    it.estado ==
                                                    EstadoSincronizacionEvento.CONFLICTO
                                        }
                                    }
                            )
                        }
                    }
            },

            viewModelScope.launch {
                repositorio
                    .escucharPartido(idPartido)
                    .catch { /* el error ya se muestra por la cronología */ }
                    .collect { partido ->
                        _estado.update {
                            it.copy(partido = partido)
                        }
                    }
            }
        )

        verificarRegistroOffline()
    }

    /*
     * Solo importa cuando no hay conexión: comprueba en la
     * caché si el partido, el catálogo y la alineación
     * están disponibles para registrar offline.
     */
    private fun verificarRegistroOffline() {
        if (idPartidoActual.isBlank()) {
            return
        }

        viewModelScope.launch {
            val disponible =
                repositorio.registroOfflineDisponible(
                    idPartidoActual
                )

            _estado.update {
                it.copy(registroOfflineDisponible = disponible)
            }
        }
    }

    /*
     * Botón "Simular sin conexión" / "Reconectar".
     */
    fun alternarConexionSimulada() {
        val actual = _estado.value

        if (actual.cambiandoConexion) {
            return
        }

        val nuevoValor =
            !actual.simulandoSinConexion

        viewModelScope.launch {
            _estado.update {
                it.copy(
                    cambiandoConexion = true,
                    error = null
                )
            }

            try {
                repositorio.cambiarConexionSimulada(nuevoValor)

                _estado.update {
                    it.copy(
                        simulandoSinConexion = nuevoValor,
                        cambiandoConexion = false,
                        mensaje = if (nuevoValor) {
                            "Modo sin conexión: los eventos se guardarán en este dispositivo"
                        } else {
                            "Conexión restablecida: enviando eventos pendientes"
                        }
                    )
                }

                verificarRegistroOffline()
            } catch (e: Exception) {
                _estado.update {
                    it.copy(
                        cambiandoConexion = false,
                        error = "No se pudo cambiar el modo de conexión"
                    )
                }
            }
        }
    }

    /*
     * Acción clara de reintento (criterio HU16).
     * La sincronización también ocurre sola al volver la red.
     */
    fun reintentarSincronizacion() {
        val actual = _estado.value

        if (actual.reintentando) {
            return
        }

        if (actual.sinConexion) {
            _estado.update {
                it.copy(
                    error = if (actual.simulandoSinConexion) {
                        "Desactiva la simulación sin conexión para sincronizar"
                    } else {
                        "Todavía no hay conexión. Los eventos siguen guardados en el dispositivo"
                    }
                )
            }
            return
        }

        viewModelScope.launch {
            _estado.update {
                it.copy(
                    reintentando = true,
                    error = null
                )
            }

            try {
                repositorio.reintentarSincronizacion()

                _estado.update {
                    it.copy(
                        reintentando = false,
                        mensaje = "Reintentando enviar los eventos pendientes"
                    )
                }
            } catch (e: Exception) {
                _estado.update {
                    it.copy(
                        reintentando = false,
                        error = "No se pudo reintentar la sincronización"
                    )
                }
            }
        }
    }

    fun reintentarRechazado(rechazado: EventoRechazado) {
        viewModelScope.launch {
            repositorio
                .reintentarEventoRechazado(rechazado.evento)
                .onSuccess { confirmado ->
                    _estado.update {
                        it.copy(
                            mensaje = if (confirmado) {
                                "${rechazado.evento.nombreTipoEvento} sincronizado"
                            } else {
                                "${rechazado.evento.nombreTipoEvento} quedó pendiente de sincronizar"
                            }
                        )
                    }
                }
                .onFailure { e ->
                    _estado.update {
                        it.copy(
                            error = "El servidor volvió a rechazar el evento: " +
                                    (e.message ?: "error desconocido")
                        )
                    }
                }
        }
    }

    /*
     * Descarte EXPLÍCITO de un evento rechazado: lo decide
     * el operador, nunca el sistema en silencio.
     */
    fun descartarRechazado(rechazado: EventoRechazado) {
        RegistroRechazos.quitar(rechazado.evento.id)

        _estado.update {
            it.copy(
                mensaje = "Evento descartado por el operador"
            )
        }
    }

    fun abrirConflicto(evento: EventoSincronizado) {
        _estado.update {
            it.copy(
                eventoConflicto = evento,
                error = null
            )
        }
    }

    fun cerrarConflicto() {
        if (_estado.value.resolviendoConflicto) {
            return
        }

        _estado.update {
            it.copy(eventoConflicto = null)
        }
    }

    /*
     * "Conservar evento": el operador confirma que no es
     * un duplicado / error y el conflicto desaparece en
     * todos los dispositivos.
     */
    fun conservarEventoEnConflicto() {
        val conflicto =
            _estado.value.eventoConflicto
                ?: return

        viewModelScope.launch {
            _estado.update {
                it.copy(resolviendoConflicto = true)
            }

            repositorio
                .marcarConflictoRevisado(
                    idPartido = idPartidoActual,
                    idEvento = conflicto.evento.id,
                    uid = autenticacion.uidActual().orEmpty()
                )
                .onSuccess { confirmado ->
                    _estado.update {
                        it.copy(
                            resolviendoConflicto = false,
                            eventoConflicto = null,
                            mensaje = if (confirmado) {
                                "Conflicto revisado: se conserva el evento"
                            } else {
                                "Revisión guardada en el dispositivo. Se sincronizará al reconectar"
                            }
                        )
                    }
                }
                .onFailure { e ->
                    _estado.update {
                        it.copy(
                            resolviendoConflicto = false,
                            error = e.message
                                ?: "No se pudo revisar el conflicto"
                        )
                    }
                }
        }
    }

    fun limpiarError() {
        _estado.update { it.copy(error = null) }
    }

    fun limpiarMensaje() {
        _estado.update { it.copy(mensaje = null) }
    }

    override fun onCleared() {
        /*
         * Si el operador sale de la pantalla con la simulación
         * activa, devolvemos la conexión para no dejar toda la
         * app sin sincronizar.
         */
        if (_estado.value.simulandoSinConexion) {
            repositorio.restaurarConexion()
        }

        super.onCleared()
    }
}
