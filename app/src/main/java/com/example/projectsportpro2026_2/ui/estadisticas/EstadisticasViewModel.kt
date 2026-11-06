package com.example.projectsportpro2026_2.ui.estadisticas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectsportpro2026_2.datos.estadisticas.CalculadoraEstadisticas
import com.example.projectsportpro2026_2.datos.estadisticas.DatosEstadisticasEquipo
import com.example.projectsportpro2026_2.datos.estadisticas.EstadisticasEquipo
import com.example.projectsportpro2026_2.datos.estadisticas.EstadisticasJugador
import com.example.projectsportpro2026_2.datos.estadisticas.JugadorEstadistico
import com.example.projectsportpro2026_2.datos.estadisticas.PeriodoEstadisticas
import com.example.projectsportpro2026_2.datos.estadisticas.RepositorioEstadisticas
import com.example.projectsportpro2026_2.datos.estadisticas.SesionPeriodo
import com.example.projectsportpro2026_2.datos.modelo.Equipo
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioAutenticacion
import com.example.projectsportpro2026_2.datos.seguridad.ContextoAcceso
import com.example.projectsportpro2026_2.datos.seguridad.MensajesPermiso
import com.example.projectsportpro2026_2.datos.seguridad.NivelAcceso
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PestanaEstadisticas { EQUIPO, JUGADOR, PERIODO }

data class EstadoEstadisticas(
    val cargando: Boolean = true,
    val cargandoEquipo: Boolean = false,
    val error: String? = null,

    val contexto: ContextoAcceso? = null,
    val equipos: List<Equipo> = emptyList(),
    val idEquipo: String = "",

    val datos: DatosEstadisticasEquipo? = null,
    val periodos: List<PeriodoEstadisticas> = listOf(PeriodoEstadisticas.TODO),
    val periodo: PeriodoEstadisticas = PeriodoEstadisticas.TODO,

    val pestana: PestanaEstadisticas = PestanaEstadisticas.EQUIPO,
    val idJugador: String = ""
) {
    val equipo: Equipo? get() = equipos.firstOrNull { it.id == idEquipo }

    val resumenEquipo: EstadisticasEquipo?
        get() = datos?.let { CalculadoraEstadisticas.calcularEquipo(it.fuente, periodo) }

    val jugadores: List<JugadorEstadistico> get() = datos?.jugadores.orEmpty()

    val jugadorSeleccionado: JugadorEstadistico?
        get() = jugadores.firstOrNull { it.jugador.id == idJugador } ?: jugadores.firstOrNull()

    val resumenJugador: EstadisticasJugador?
        get() {
            val d = datos ?: return null
            val j = jugadorSeleccionado ?: return null
            return CalculadoraEstadisticas.calcularJugador(j.jugador.id, d.fuente, periodo)
        }

    val sesionesPeriodo: List<SesionPeriodo>
        get() = datos?.let { CalculadoraEstadisticas.sesionesDelPeriodo(it.fuente, periodo) }
            .orEmpty()

    val verCompleto: Boolean
        get() = jugadorSeleccionado?.nivel == NivelAcceso.COMPLETO
}

class EstadisticasViewModel : ViewModel() {

    private val repositorio = RepositorioEstadisticas()
    private val autenticacion = RepositorioAutenticacion()

    private val _estado = MutableStateFlow(EstadoEstadisticas())
    val estado: StateFlow<EstadoEstadisticas> = _estado.asStateFlow()

    private var iniciado = false

    fun iniciar() {
        if (iniciado) return
        iniciado = true
        cargarEquipos()
    }

    fun reintentar() {
        if (_estado.value.contexto == null) cargarEquipos() else cargarEquipo()
    }

    private fun cargarEquipos() {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }

            repositorio.equiposVisibles(autenticacion.uidActual().orEmpty())
                .onSuccess { (contexto, equipos) ->
                    _estado.update {
                        it.copy(
                            cargando = false,
                            contexto = contexto,
                            equipos = equipos,
                            idEquipo = equipos.firstOrNull()?.id.orEmpty()
                        )
                    }
                    cargarEquipo()
                }
                .onFailure { e ->
                    _estado.update {
                        it.copy(cargando = false, error = MensajesPermiso.traducir(e))
                    }
                }
        }
    }

    private fun cargarEquipo() {
        val actual = _estado.value
        val contexto = actual.contexto ?: return
        val equipo = actual.equipo ?: return

        viewModelScope.launch {
            _estado.update { it.copy(cargandoEquipo = true, error = null) }

            repositorio.cargarEquipo(contexto, equipo)
                .onSuccess { datos ->
                    val periodos = CalculadoraEstadisticas.periodosDisponibles(datos.fuente)
                    _estado.update {
                        it.copy(
                            cargandoEquipo = false,
                            datos = datos,
                            periodos = periodos,
                            // Por defecto: la temporada más reciente
                            periodo = periodos.first(),
                            idJugador = datos.jugadores.firstOrNull()?.jugador?.id.orEmpty()
                        )
                    }
                }
                .onFailure { e ->
                    _estado.update {
                        it.copy(
                            cargandoEquipo = false,
                            datos = null,
                            error = MensajesPermiso.traducir(
                                e,
                                "No se pudieron calcular las estadísticas. Revisa tu conexión."
                            )
                        )
                    }
                }
        }
    }

    fun seleccionarEquipo(idEquipo: String) {
        if (idEquipo == _estado.value.idEquipo) return
        _estado.update { it.copy(idEquipo = idEquipo, datos = null) }
        cargarEquipo()
    }

    fun seleccionarPestana(pestana: PestanaEstadisticas) =
        _estado.update { it.copy(pestana = pestana) }

    fun seleccionarPeriodo(periodo: PeriodoEstadisticas) =
        _estado.update { it.copy(periodo = periodo) }

    fun seleccionarJugador(idJugador: String) =
        _estado.update { it.copy(idJugador = idJugador) }
}
