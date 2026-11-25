package com.example.projectsportpro2026_2.ui.inicio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectsportpro2026_2.datos.estadisticas.CalculadoraEstadisticas
import com.example.projectsportpro2026_2.datos.estadisticas.PeriodoEstadisticas
import com.example.projectsportpro2026_2.datos.estadisticas.RepositorioEstadisticas
import com.example.projectsportpro2026_2.datos.modelo.Entrenamiento
import com.example.projectsportpro2026_2.datos.modelo.EstadoMensualidad
import com.example.projectsportpro2026_2.datos.modelo.Jugador
import com.example.projectsportpro2026_2.datos.modelo.Mensualidad
import com.example.projectsportpro2026_2.datos.modelo.Rol
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioAsistencias
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioAutenticacion
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioCuentasJugador
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioEntrenamientos
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioMensualidades
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioVinculaciones
import com.example.projectsportpro2026_2.datos.repositorio.convocatoria.ConvocatoriaParaResponder
import com.example.projectsportpro2026_2.datos.repositorio.convocatoria.RepositorioConvocatorias
import com.example.projectsportpro2026_2.datos.repositorio.evento.MarcadorPartido
import com.example.projectsportpro2026_2.datos.repositorio.evento.RepositorioEventosPartido
import com.example.projectsportpro2026_2.datos.repositorio.seguimiento.PartidoSeguible
import com.example.projectsportpro2026_2.datos.repositorio.seguimiento.RepositorioSeguimientoPartido
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioNotificaciones
import com.example.projectsportpro2026_2.datos.repositorio.sincronizacion.RepositorioSincronizacion
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

/*
 * Rediseño "Línea de cal" · HU-02, 7.1: datos del inicio de los 4 roles.
 *
 * SOLO LECTURA (decisión del usuario): este ViewModel solo llama a funciones
 * de lectura de los repositorios (revisadas en la etapa 3: ninguna escribe).
 * Nunca llama a generarMensualidadesMesActual ni a nada que escriba en Firestore.
 *
 * Cada sección se carga por separado. Si una consulta falla, esa sección queda
 * en null y la pantalla la oculta; el resto del inicio sigue funcionando.
 */

/** Tarjeta EN VIVO del inicio. */
data class EnVivoInicio(
    val partido: PartidoSeguible,
    val marcador: MarcadorPartido? = null
)

/** ADM · "Requiere tu atención". Cada número es null si su consulta falló. */
data class AtencionAdm(
    val cuentasPorAprobar: Int? = null,
    val vinculacionesPendientes: Int? = null,
    val mensualidadesVencidas: Int? = null
) {
    val hayDatos: Boolean
        get() = cuentasPorAprobar != null || vinculacionesPendientes != null || mensualidadesVencidas != null
}

/** DT · "Tu jornada". */
data class JornadaDt(
    val entrenamiento: Entrenamiento? = null,
    val partido: PartidoSeguible? = null,
    val respuestas: Respuestas? = null
) {
    val hayDatos: Boolean get() = entrenamiento != null || partido != null
}

/** JUG · "Para ti". */
data class ParaTiJug(
    val convocatoria: ConvocatoriaParaResponder? = null,
    val convocatoriasPendientes: Int? = null,
    val porcentajeAsistencia: Int? = null,
    val golesTemporada: Int? = null,
    val mensualidad: EstadoMensualidad? = null,
    val mensualidadLeida: Boolean = false
)

/** PAD · un jugador vinculado con el estado de su mensualidad del periodo actual. */
data class JugadorInicio(
    val jugador: Jugador,
    val mensualidad: EstadoMensualidad? = null
)

data class EstadoInicio(
    val nombre: String = "",
    val noLeidas: Int? = null,

    // Tarjeta EN VIVO: partidosCargados = ya llegó la primera lista de partidos
    val partidosCargados: Boolean = false,
    val enVivo: EnVivoInicio? = null,
    val proximoPartido: PartidoSeguible? = null,

    val atencion: AtencionAdm? = null,
    val jornada: JornadaDt? = null,
    val paraTi: ParaTiJug? = null,
    val misJugadores: List<JugadorInicio>? = null,
    val mensualidadesVencidasPad: Int? = null,
    val convocatoriasPendientesPad: Int? = null
)

class InicioViewModel(
    private val autenticacion: RepositorioAutenticacion = RepositorioAutenticacion(),
    private val notificaciones: RepositorioNotificaciones = RepositorioNotificaciones(),
    private val seguimiento: RepositorioSeguimientoPartido = RepositorioSeguimientoPartido(),
    private val cuentas: RepositorioCuentasJugador = RepositorioCuentasJugador(),
    private val vinculaciones: RepositorioVinculaciones = RepositorioVinculaciones(),
    private val mensualidades: RepositorioMensualidades = RepositorioMensualidades(),
    private val entrenamientos: RepositorioEntrenamientos = RepositorioEntrenamientos(),
    private val convocatorias: RepositorioConvocatorias = RepositorioConvocatorias(),
    private val asistencias: RepositorioAsistencias = RepositorioAsistencias(),
    private val estadisticas: RepositorioEstadisticas = RepositorioEstadisticas()
) : ViewModel() {

    private val _estado = MutableStateFlow(EstadoInicio())
    val estado: StateFlow<EstadoInicio> = _estado.asStateFlow()

    private var iniciado = false
    private var escuchaPartidos: Job? = null
    private var escuchaMarcador: Job? = null
    private var idPartidoEscuchado: String? = null

    /** Se llama una vez desde la pantalla con el rol de su ruta. */
    fun iniciar(rol: Rol) {
        if (iniciado) return
        iniciado = true

        val uid = autenticacion.uidActual() ?: return

        viewModelScope.launch {
            val perfil = try {
                autenticacion.obtenerPerfil(uid)
            } catch (e: Exception) {
                null
            }
            _estado.update { it.copy(nombre = perfil?.nombre.orEmpty()) }
        }

        viewModelScope.launch {
            notificaciones.listar(uid).onSuccess { lista ->
                _estado.update { it.copy(noLeidas = LogicaInicio.noLeidas(lista)) }
            }
        }

        escucharPartidos(uid, rol)

        when (rol) {
            Rol.ADM -> cargarAtencion(uid)
            Rol.DT -> cargarJornadaEntrenamiento(uid)
            Rol.JUG -> cargarParaTi(uid)
            Rol.PAD -> cargarMisJugadores(uid)
        }
    }

    // ---------- Tarjeta EN VIVO (lógica existente de HU-17) ----------

    private fun escucharPartidos(uid: String, rol: Rol) {
        escuchaPartidos?.cancel()
        escuchaPartidos = viewModelScope.launch {
            val equipos = seguimiento.equiposVisibles(uid).getOrNull()?.second
            if (equipos == null) {
                // Sin datos: la tarjeta muestra "Sin partidos en vivo" y sigue abriendo Partidos en vivo
                _estado.update { it.copy(partidosCargados = true) }
                return@launch
            }
            seguimiento.escucharPartidos(equipos)
                .catch { _estado.update { it.copy(partidosCargados = true) } }
                .collect { lista ->
                    val enCurso = LogicaInicio.partidoEnCurso(lista.partidos) { it.partido }
                    val proximo = LogicaInicio.proximoPartido(lista.partidos, LocalDateTime.now()) { it.partido }
                    _estado.update { actual ->
                        actual.copy(
                            partidosCargados = true,
                            enVivo = enCurso?.let { EnVivoInicio(it, actual.enVivo?.takeIf { v -> v.partido.partido.id == it.partido.id }?.marcador) },
                            proximoPartido = proximo
                        )
                    }
                    escucharMarcador(enCurso)
                    if (rol == Rol.DT) {
                        cargarJornadaPartido(uid, proximo)
                    }
                }
        }
    }

    private fun escucharMarcador(enCurso: PartidoSeguible?) {
        val id = enCurso?.partido?.id
        if (id == idPartidoEscuchado) return
        idPartidoEscuchado = id
        escuchaMarcador?.cancel()
        if (id == null) return
        escuchaMarcador = viewModelScope.launch {
            RepositorioSincronizacion().escucharEventos(id)
                .catch { /* sin marcador: la tarjeta muestra "–" */ }
                .collect { datos ->
                    val marcador = RepositorioEventosPartido().calcularMarcador(datos.eventos)
                    _estado.update { actual ->
                        val vivo = actual.enVivo
                        if (vivo != null && vivo.partido.partido.id == id) {
                            actual.copy(enVivo = vivo.copy(marcador = marcador))
                        } else {
                            actual
                        }
                    }
                }
        }
    }

    // ---------- ADM · Requiere tu atención ----------

    private fun cargarAtencion(uid: String) {
        viewModelScope.launch {
            val cuentasPorAprobar = cuentas.listar(uid).getOrNull()?.let(LogicaInicio::cuentasPorAprobar)
            val pendientes = vinculaciones.listarParaAdministrador(uid).getOrNull()
                ?.let(LogicaInicio::vinculacionesPendientes)
            val vencidas = mensualidades.listarParaUsuario(uid).getOrNull()
                ?.let(LogicaInicio::mensualidadesVencidas)
            val atencion = AtencionAdm(cuentasPorAprobar, pendientes, vencidas)
            _estado.update { it.copy(atencion = atencion.takeIf { a -> a.hayDatos }) }
        }
    }

    // ---------- DT · Tu jornada ----------

    private fun cargarJornadaEntrenamiento(uid: String) {
        viewModelScope.launch {
            val proximo = entrenamientos.listarParaUsuario(uid).getOrNull()
                ?.let { LogicaInicio.proximoEntrenamiento(it, System.currentTimeMillis()) }
            _estado.update { actual ->
                val jornada = (actual.jornada ?: JornadaDt()).copy(entrenamiento = proximo)
                actual.copy(jornada = jornada.takeIf { it.hayDatos })
            }
        }
    }

    private var idPartidoJornada: String? = null

    private fun cargarJornadaPartido(uid: String, proximo: PartidoSeguible?) {
        val id = proximo?.partido?.id
        if (id == idPartidoJornada) return
        idPartidoJornada = id
        viewModelScope.launch {
            val respuestas = if (id == null) {
                null
            } else {
                convocatorias.listarConvocados(id, uid).getOrNull()
                    ?.let(LogicaInicio::respuestasConvocatoria)
                    ?.takeIf { it.total > 0 }
            }
            _estado.update { actual ->
                val jornada = (actual.jornada ?: JornadaDt()).copy(partido = proximo, respuestas = respuestas)
                actual.copy(jornada = jornada.takeIf { it.hayDatos })
            }
        }
    }

    // ---------- JUG · Para ti ----------

    private fun cargarParaTi(uid: String) {
        viewModelScope.launch {
            val lista = convocatorias.listarParaJugador(uid).getOrNull()
            actualizarParaTi {
                it.copy(
                    convocatoria = lista?.let(LogicaInicio::convocatoriaPorResponder),
                    convocatoriasPendientes = lista?.let(LogicaInicio::convocatoriasPorResponder)
                )
            }
        }

        viewModelScope.launch {
            val jugador = asistencias.listarJugadoresConsulta(uid).getOrNull()?.firstOrNull() ?: return@launch

            val porcentaje = asistencias.listarHistorialJugador(jugador.id, uid).getOrNull()
                ?.let(LogicaInicio::porcentajeAsistencia)
            actualizarParaTi { it.copy(porcentajeAsistencia = porcentaje) }

            val propias = mensualidades.listarParaUsuario(uid).getOrNull()
            if (propias != null) {
                actualizarParaTi {
                    it.copy(
                        mensualidad = LogicaInicio.estadoMensualidad(propias, jugador.id),
                        mensualidadLeida = true
                    )
                }
            }

            // Goles de la temporada: la lectura más pesada, va al final y se oculta si falla
            val goles = golesDeLaTemporada(uid, jugador)
            actualizarParaTi { it.copy(golesTemporada = goles) }
        }
    }

    private suspend fun golesDeLaTemporada(uid: String, jugador: Jugador): Int? {
        val (contexto, equipos) = estadisticas.equiposVisibles(uid).getOrNull() ?: return null
        val equipo = equipos.firstOrNull { it.id == jugador.idEquipo } ?: equipos.firstOrNull() ?: return null
        val datos = estadisticas.cargarEquipo(contexto, equipo).getOrNull() ?: return null
        return CalculadoraEstadisticas.calcularJugador(
            idJugador = jugador.id,
            fuente = datos.fuente,
            periodo = PeriodoEstadisticas.temporada(LocalDate.now().year)
        ).goles
    }

    private fun actualizarParaTi(cambio: (ParaTiJug) -> ParaTiJug) {
        _estado.update { it.copy(paraTi = cambio(it.paraTi ?: ParaTiJug())) }
    }

    // ---------- PAD · Mis jugadores ----------

    private fun cargarMisJugadores(uid: String) {
        viewModelScope.launch {
            val jugadores = asistencias.listarJugadoresConsulta(uid).getOrNull() ?: return@launch
            val lista: List<Mensualidad>? = mensualidades.listarParaUsuario(uid).getOrNull()
            _estado.update { actual ->
                actual.copy(
                    misJugadores = jugadores.map { j ->
                        JugadorInicio(j, lista?.let { LogicaInicio.estadoMensualidad(it, j.id) })
                    },
                    mensualidadesVencidasPad = lista?.let(LogicaInicio::mensualidadesVencidas)
                )
            }
        }
        viewModelScope.launch {
            val lista = convocatorias.listarParaApoderado(uid).getOrNull()
            _estado.update {
                it.copy(convocatoriasPendientesPad = lista?.let(LogicaInicio::convocatoriasPorResponder))
            }
        }
    }
}
