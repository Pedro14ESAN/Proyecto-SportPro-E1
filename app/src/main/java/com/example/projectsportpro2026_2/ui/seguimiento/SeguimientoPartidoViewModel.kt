package com.example.projectsportpro2026_2.ui.seguimiento

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectsportpro2026_2.datos.modelo.evento.EquipoEvento
import com.example.projectsportpro2026_2.datos.modelo.evento.EstadoEventoPartido
import com.example.projectsportpro2026_2.datos.modelo.evento.EventoPartido
import com.example.projectsportpro2026_2.datos.modelo.partido.Partido
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioAutenticacion
import com.example.projectsportpro2026_2.datos.repositorio.evento.MarcadorPartido
import com.example.projectsportpro2026_2.datos.repositorio.seguimiento.InfoSeguimiento
import com.example.projectsportpro2026_2.datos.repositorio.seguimiento.RepositorioSeguimientoPartido
import com.example.projectsportpro2026_2.datos.repositorio.sincronizacion.RepositorioSincronizacion
import com.example.projectsportpro2026_2.datos.sincronizacion.MonitorConexion
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/*
 * HU17 - Estado de la vista en vivo (solo lectura).
 */
data class EstadoSeguimiento(
    val cargando: Boolean = true,
    val info: InfoSeguimiento? = null,

    // Partido en tiempo real (estado sincronizado entre dispositivos)
    val partido: Partido? = null,

    // Todos los eventos (activos y anulados), ya ordenados
    val eventos: List<EventoPartido> = emptyList(),

    // Eventos que llegaron mientras la pantalla estaba abierta
    val idsNuevos: Set<String> = emptySet(),

    val hayRed: Boolean = true,
    val desdeCache: Boolean = false,
    val escriturasPendientes: Boolean = false,
    val ultimaActualizacion: Long = 0L,

    val error: String? = null
) {
    val activos: List<EventoPartido>
        get() = eventos.filter {
            it.estado == EstadoEventoPartido.ACTIVO
        }

    val anulados: Int
        get() = eventos.count {
            it.estado == EstadoEventoPartido.ANULADO
        }

    /*
     * Criterio HU17: el marcador se calcula SOLO con eventos
     * válidos (activos) que afectan el resultado. Una
     * corrección o anulación se refleja automáticamente.
     */
    val marcador: MarcadorPartido
        get() {
            val goles = activos.filter { it.afectaMarcador }

            return MarcadorPartido(
                propio = goles.count {
                    it.equipoEvento == EquipoEvento.PROPIO
                },
                rival = goles.count {
                    it.equipoEvento == EquipoEvento.RIVAL
                }
            )
        }

    // Minuto del último evento válido registrado
    val minutoActual: Int?
        get() = activos.maxOfOrNull { it.minuto }

    val totalGoles: Int
        get() = activos.count { it.afectaMarcador }

    val totalTarjetas: Int
        get() = activos.count { it.codigoTipoEvento == "TARJETA" }

    val totalCambios: Int
        get() = activos.count { it.codigoTipoEvento == "CAMBIO" }

    /*
     * Sin conexión o con datos que el servidor aún no
     * confirmó → la información puede estar desactualizada.
     */
    val informacionDesactualizada: Boolean
        get() = !hayRed || desdeCache

    /*
     * Caso alterno HU17: si este dispositivo tiene
     * operaciones pendientes de sincronizar, el marcador
     * local no se muestra como definitivo.
     */
    val marcadorProvisional: Boolean
        get() = escriturasPendientes
}

class SeguimientoPartidoViewModel(
    aplicacion: Application
) : AndroidViewModel(aplicacion) {

    private val repositorio =
        RepositorioSeguimientoPartido()

    private val tiempoReal =
        RepositorioSincronizacion()

    private val autenticacion =
        RepositorioAutenticacion()

    private val monitor =
        MonitorConexion(aplicacion)

    private val _estado =
        MutableStateFlow(EstadoSeguimiento())

    val estado: StateFlow<EstadoSeguimiento> =
        _estado.asStateFlow()

    private var idPartidoActual = ""

    private var trabajos: List<Job> = emptyList()

    // ids presentes en la primera carga (no se marcan como nuevos)
    private var idsConocidos: Set<String>? = null

    init {
        viewModelScope.launch {
            monitor.conectado.collect { conectado ->
                _estado.update { it.copy(hayRed = conectado) }
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
        cargar()
    }

    fun reintentar() {
        if (idPartidoActual.isNotBlank()) {
            cargar()
        }
    }

    private fun cargar() {
        trabajos.forEach { it.cancel() }
        idsConocidos = null

        viewModelScope.launch {
            _estado.update {
                it.copy(cargando = true, error = null)
            }

            repositorio
                .prepararSeguimiento(
                    idPartido = idPartidoActual,
                    uid = autenticacion.uidActual().orEmpty()
                )
                .onSuccess { info ->
                    _estado.update {
                        it.copy(
                            cargando = false,
                            info = info,
                            partido = info.partido
                        )
                    }
                    suscribirse()
                }
                .onFailure { e ->
                    _estado.update {
                        it.copy(
                            cargando = false,
                            error = when (e) {
                                is IllegalArgumentException,
                                is IllegalStateException ->
                                    e.message ?: "No se pudo abrir el partido"

                                else ->
                                    "No se pudo abrir el partido. Revisa tu conexión."
                            }
                        )
                    }
                }
        }
    }

    /*
     * Flujo principal HU17 (paso 2): la pantalla se suscribe
     * al partido y a su cronología. Cada registro, corrección
     * o anulación publicado por el backend llega solo, sin
     * pulsar "Actualizar".
     */
    private fun suscribirse() {
        trabajos = listOf(
            viewModelScope.launch {
                tiempoReal
                    .escucharPartido(idPartidoActual)
                    .catch { e ->
                        _estado.update {
                            it.copy(error = e.message)
                        }
                    }
                    .collect { partido ->
                        _estado.update {
                            it.copy(partido = partido)
                        }
                    }
            },

            viewModelScope.launch {
                tiempoReal
                    .escucharEventos(idPartidoActual)
                    .catch { e ->
                        _estado.update {
                            it.copy(error = e.message)
                        }
                    }
                    .collect { instantanea ->

                        /*
                         * Orden: minuto del partido y, como
                         * criterio secundario, hora de registro.
                         */
                        val ordenados =
                            instantanea.eventos.sortedWith(
                                compareBy<EventoPartido> {
                                    it.minuto
                                }.thenBy {
                                    it.fechaCaptura
                                }
                            )

                        val ids = ordenados.map { it.id }.toSet()

                        val conocidos = idsConocidos
                        val nuevos =
                            if (conocidos == null) {
                                idsConocidos = ids
                                emptySet()
                            } else {
                                ids - conocidos
                            }

                        if (conocidos != null) {
                            idsConocidos = conocidos + ids
                        }

                        val confirmado =
                            !instantanea.desdeCache &&
                                    !instantanea.escriturasPendientes

                        _estado.update { actual ->
                            actual.copy(
                                eventos = ordenados,
                                idsNuevos = actual.idsNuevos + nuevos,
                                desdeCache = instantanea.desdeCache,
                                escriturasPendientes =
                                    instantanea.escriturasPendientes,
                                ultimaActualizacion =
                                    if (confirmado) {
                                        System.currentTimeMillis()
                                    } else {
                                        actual.ultimaActualizacion
                                    }
                            )
                        }

                        if (nuevos.isNotEmpty()) {
                            quitarMarcaNuevoDespues(nuevos)
                        }
                    }
            }
        )
    }

    private fun quitarMarcaNuevoDespues(ids: Set<String>) {
        viewModelScope.launch {
            delay(12_000L)
            _estado.update {
                it.copy(idsNuevos = it.idsNuevos - ids)
            }
        }
    }
}
