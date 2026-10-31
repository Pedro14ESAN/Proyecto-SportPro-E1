package com.example.projectsportpro2026_2.ui.mensualidades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectsportpro2026_2.datos.modelo.EstadoMensualidad
import com.example.projectsportpro2026_2.datos.modelo.Jugador
import com.example.projectsportpro2026_2.datos.modelo.Mensualidad
import com.example.projectsportpro2026_2.datos.modelo.Rol
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioAutenticacion
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioMensualidades
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EstadoMensualidades(
    val mensualidades: List<Mensualidad> = emptyList(),
    val jugadoresAdministrables: List<Jugador> = emptyList(),

    val rol: Rol? = null,

    val cargando: Boolean = false,
    val guardando: Boolean = false,

    val error: String? = null,
    val mensaje: String? = null
)

class MensualidadesViewModel(
    private val repositorio:
    RepositorioMensualidades =
        RepositorioMensualidades(),

    private val autenticacion:
    RepositorioAutenticacion =
        RepositorioAutenticacion()
) : ViewModel() {

    private val _estado =
        MutableStateFlow(
            EstadoMensualidades()
        )

    val estado:
            StateFlow<EstadoMensualidades> =
        _estado.asStateFlow()

    private var iniciado = false


    fun iniciar() {

        if (iniciado) return

        iniciado = true

        cargar()
    }


    fun cargar() {

        viewModelScope.launch {

            _estado.update {
                it.copy(
                    cargando = true,
                    error = null
                )
            }

            val uid =
                autenticacion
                    .uidActual()

            if (uid.isNullOrBlank()) {

                _estado.update {
                    it.copy(
                        cargando = false,
                        error =
                            "No existe una sesión activa"
                    )
                }

                return@launch
            }

            try {

                val perfil =
                    autenticacion
                        .obtenerPerfil(uid)
                        ?: throw IllegalStateException(
                            "Tu cuenta no tiene perfil"
                        )


                val mensualidades =
                    repositorio
                        .listarParaUsuario(uid)
                        .getOrThrow()


                val jugadores =
                    if (
                        perfil.rol ==
                        Rol.ADM
                    ) {

                        repositorio
                            .listarJugadoresAdministrables(
                                uid
                            )
                            .getOrThrow()

                    } else {

                        emptyList()
                    }


                _estado.update {
                    it.copy(
                        mensualidades =
                            mensualidades,

                        jugadoresAdministrables =
                            jugadores,

                        rol =
                            perfil.rol,

                        cargando =
                            false,

                        error =
                            null
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
                            mensajeError(e)
                    )
                }
            }
        }
    }


    fun guardarMensualidad(
        idJugador: String,
        periodo: String,
        estadoMensualidad:
        EstadoMensualidad
    ) {

        viewModelScope.launch {

            val uid =
                autenticacion
                    .uidActual()

            if (uid.isNullOrBlank()) {

                _estado.update {
                    it.copy(
                        error =
                            "No existe una sesión activa"
                    )
                }

                return@launch
            }


            if (idJugador.isBlank()) {

                _estado.update {
                    it.copy(
                        error =
                            "Selecciona un jugador"
                    )
                }

                return@launch
            }


            _estado.update {
                it.copy(
                    guardando = true,
                    error = null,
                    mensaje = null
                )
            }


            repositorio
                .registrarOActualizar(
                    idJugador =
                        idJugador,

                    periodo =
                        periodo,

                    estado =
                        estadoMensualidad,

                    uid =
                        uid
                )
                .onSuccess {

                    _estado.update {
                        it.copy(
                            guardando =
                                false,

                            mensaje =
                                "Mensualidad guardada correctamente"
                        )
                    }

                    cargar()
                }
                .onFailure { e ->

                    _estado.update {
                        it.copy(
                            guardando =
                                false,

                            error =
                                mensajeError(e)
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
    ): String {

        return when (e) {

            is IllegalArgumentException,
            is IllegalStateException ->
                e.message
                    ?: "No se pudo completar la operación"

            else ->
                "No se pudo completar la operación. Revisa tu conexión e inténtalo nuevamente."
        }
    }
}