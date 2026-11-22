package com.example.projectsportpro2026_2.ui.seguimiento

import com.example.projectsportpro2026_2.datos.modelo.partido.EstadoPartido
import com.example.projectsportpro2026_2.datos.repositorio.evento.MarcadorPartido
import com.example.projectsportpro2026_2.datos.repositorio.evento.RepositorioEventosPartido
import com.example.projectsportpro2026_2.datos.repositorio.sincronizacion.RepositorioSincronizacion
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.coroutineScope
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectsportpro2026_2.datos.modelo.Rol
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioAutenticacion
import com.example.projectsportpro2026_2.datos.repositorio.seguimiento.PartidoSeguible
import com.example.projectsportpro2026_2.datos.repositorio.seguimiento.RepositorioSeguimientoPartido
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/*
 * HU17 - Lista de partidos que el usuario puede seguir en vivo.
 */
data class EstadoMisPartidos(
    val cargando: Boolean = true,
    val rol: Rol? = null,
    val partidos: List<PartidoSeguible> = emptyList(),
    val marcadores: Map<String, MarcadorPartido> = emptyMap(),
    val desdeCache: Boolean = false,
    val error: String? = null
)

class MisPartidosViewModel(
    private val repositorio: RepositorioSeguimientoPartido =
        RepositorioSeguimientoPartido(),
    private val autenticacion: RepositorioAutenticacion =
        RepositorioAutenticacion()
) : ViewModel() {

    private val _estado = MutableStateFlow(EstadoMisPartidos())
    val estado: StateFlow<EstadoMisPartidos> = _estado.asStateFlow()

    private var escucha: Job? = null
    private var iniciado = false

    fun iniciar() {
        if (iniciado) {
            return
        }
        iniciado = true
        cargar()
    }

    fun cargar() {
        escucha?.cancel()

        escucha = viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }

            val resultado =
                repositorio.equiposVisibles(
                    autenticacion.uidActual().orEmpty()
                )

            val (rol, equipos) =
                resultado.getOrElse { e ->
                    _estado.update {
                        it.copy(
                            cargando = false,
                            error = e.message
                                ?: "No se pudieron cargar tus partidos"
                        )
                    }
                    return@launch
                }

            _estado.update { it.copy(rol = rol) }

            repositorio
                .escucharPartidos(equipos)
                .catch { e ->
                    _estado.update {
                        it.copy(
                            cargando = false,
                            error = e.message
                                ?: "No se pudieron escuchar los partidos"
                        )
                    }
                }
                .collectLatest { lista ->
                    _estado.update {
                        it.copy(
                            cargando = false,
                            partidos = lista.partidos,
                            desdeCache = lista.desdeCache
                        )
                    }
                    coroutineScope {
                        lista.partidos.filter { it.partido.estado == EstadoPartido.EN_CURSO }.forEach { seguible ->
                            launch {
                                RepositorioSincronizacion().escucharEventos(seguible.partido.id)
                                    .catch { e -> _estado.update { it.copy(error = e.message) } }
                                    .collect { datos ->
                                        val marcador = RepositorioEventosPartido().calcularMarcador(datos.eventos)
                                        _estado.update { it.copy(marcadores = it.marcadores + (seguible.partido.id to marcador)) }
                                    }
                            }
                        }
                    }
                }
        }
    }
}
