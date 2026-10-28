package com.example.projectsportpro2026_2.ui.catalogoeventos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectsportpro2026_2.datos.modelo.evento.TipoEventoCatalogo
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioAutenticacion
import com.example.projectsportpro2026_2.datos.repositorio.evento.ActualizacionCatalogo
import com.example.projectsportpro2026_2.datos.repositorio.evento.RepositorioCatalogoEventos
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class EstadoSincronizacionCatalogo {
    SINCRONIZADO,
    SINCRONIZANDO,
    DATOS_LOCALES
}

data class FormularioEvento(
    val id: String? = null,
    val nombre: String = "",
    val activo: Boolean = true,

    val requiereEquipo: Boolean = false,
    val requiereJugador: Boolean = false,
    val requiereObservacion: Boolean = false,

    val afectaMarcador: Boolean = false,
    val afectaEstadoPartido: Boolean = false,

    val esTipoBase: Boolean = false
)

data class EstadoCatalogoEventos(
    val eventos: List<TipoEventoCatalogo> = emptyList(),

    val cargando: Boolean = false,
    val guardando: Boolean = false,

    val estadoSincronizacion:
    EstadoSincronizacionCatalogo =
        EstadoSincronizacionCatalogo.SINCRONIZADO,

    val mostrarFormulario: Boolean = false,
    val formulario: FormularioEvento =
        FormularioEvento(),

    val error: String? = null,
    val mensaje: String? = null
)

class CatalogoEventosViewModel(
    private val repositorio:
    RepositorioCatalogoEventos =
        RepositorioCatalogoEventos(),

    private val autenticacion:
    RepositorioAutenticacion =
        RepositorioAutenticacion()
) : ViewModel() {

    private val _estado =
        MutableStateFlow(
            EstadoCatalogoEventos()
        )

    val estado: StateFlow<EstadoCatalogoEventos> =
        _estado.asStateFlow()

    private var idAcademiaActual = ""

    private var trabajoObservacion: Job? = null

    private val uid: String
        get() =
            autenticacion
                .uidActual()
                .orEmpty()

    fun iniciar(
        idAcademia: String
    ) {
        if (
            idAcademia.isBlank() ||
            idAcademia == idAcademiaActual &&
            trabajoObservacion?.isActive == true
        ) {
            return
        }

        idAcademiaActual =
            idAcademia

        observarCatalogo()
    }

    private fun observarCatalogo() {

        trabajoObservacion?.cancel()

        trabajoObservacion =
            viewModelScope.launch {

                _estado.update {
                    it.copy(
                        cargando = true,
                        error = null
                    )
                }

                /*
                 * Si es la primera vez que esta academia
                 * abre HU13, crea los 11 eventos mínimos.
                 *
                 * Si ya existen, no los duplica.
                 */
                repositorio
                    .inicializarCatalogoBase(
                        idAcademia =
                            idAcademiaActual,
                        uid = uid
                    )
                    .onFailure { e ->

                        /*
                         * No detenemos necesariamente la
                         * observación, porque Firestore
                         * podría tener datos disponibles
                         * desde cache.
                         */
                        _estado.update {
                            it.copy(
                                error =
                                    mensajeError(e)
                            )
                        }
                    }

                repositorio
                    .observarCatalogo(
                        idAcademia =
                            idAcademiaActual,
                        uid = uid
                    )
                    .collect { resultado ->

                        resultado
                            .onSuccess {
                                    actualizacion ->

                                aplicarActualizacion(
                                    actualizacion
                                )
                            }
                            .onFailure { e ->

                                _estado.update {
                                    it.copy(
                                        cargando = false,
                                        error =
                                            mensajeError(e)
                                    )
                                }
                            }
                    }
            }
    }

    private fun aplicarActualizacion(
        actualizacion:
        ActualizacionCatalogo
    ) {
        val sincronizacion =
            when {

                actualizacion
                    .tieneEscriturasPendientes ->

                    EstadoSincronizacionCatalogo
                        .SINCRONIZANDO

                actualizacion.desdeCache ->

                    EstadoSincronizacionCatalogo
                        .DATOS_LOCALES

                else ->

                    EstadoSincronizacionCatalogo
                        .SINCRONIZADO
            }

        _estado.update {
            it.copy(
                eventos =
                    actualizacion.tipos,

                cargando = false,

                estadoSincronizacion =
                    sincronizacion
            )
        }
    }

    fun abrirNuevoEvento() {
        _estado.update {
            it.copy(
                mostrarFormulario = true,

                formulario =
                    FormularioEvento(),

                error = null,
                mensaje = null
            )
        }
    }

    fun abrirEditarEvento(
        evento: TipoEventoCatalogo
    ) {
        _estado.update {
            it.copy(
                mostrarFormulario = true,

                formulario =
                    FormularioEvento(
                        id = evento.id,
                        nombre = evento.nombre,
                        activo = evento.activo,

                        requiereEquipo =
                            evento.requiereEquipo,

                        requiereJugador =
                            evento.requiereJugador,

                        requiereObservacion =
                            evento.requiereObservacion,

                        afectaMarcador =
                            evento.afectaMarcador,

                        afectaEstadoPartido =
                            evento.afectaEstadoPartido,

                        esTipoBase =
                            evento.esTipoBase
                    ),

                error = null,
                mensaje = null
            )
        }
    }

    fun cerrarFormulario() {
        if (_estado.value.guardando) {
            return
        }

        _estado.update {
            it.copy(
                mostrarFormulario = false,
                formulario =
                    FormularioEvento()
            )
        }
    }

    fun cambiarNombre(
        valor: String
    ) {
        _estado.update {
            it.copy(
                formulario =
                    it.formulario.copy(
                        nombre = valor
                    )
            )
        }
    }

    fun cambiarActivo(
        valor: Boolean
    ) {
        _estado.update {
            it.copy(
                formulario =
                    it.formulario.copy(
                        activo = valor
                    )
            )
        }
    }

    fun cambiarRequiereEquipo(
        valor: Boolean
    ) {
        _estado.update {
            it.copy(
                formulario =
                    it.formulario.copy(
                        requiereEquipo = valor
                    )
            )
        }
    }

    fun cambiarRequiereJugador(
        valor: Boolean
    ) {
        _estado.update {
            it.copy(
                formulario =
                    it.formulario.copy(
                        requiereJugador = valor
                    )
            )
        }
    }

    fun cambiarRequiereObservacion(
        valor: Boolean
    ) {
        _estado.update {
            it.copy(
                formulario =
                    it.formulario.copy(
                        requiereObservacion =
                            valor
                    )
            )
        }
    }

    fun cambiarAfectaMarcador(
        valor: Boolean
    ) {
        _estado.update {
            it.copy(
                formulario =
                    it.formulario.copy(
                        afectaMarcador = valor
                    )
            )
        }
    }

    fun cambiarAfectaEstadoPartido(
        valor: Boolean
    ) {
        _estado.update {
            it.copy(
                formulario =
                    it.formulario.copy(
                        afectaEstadoPartido =
                            valor
                    )
            )
        }
    }

    fun guardarFormulario() {

        val actual =
            _estado.value

        val formulario =
            actual.formulario

        if (formulario.nombre.isBlank()) {
            _estado.update {
                it.copy(
                    error =
                        "El nombre del evento es obligatorio"
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

            val resultado =
                if (formulario.id == null) {

                    repositorio.crearTipo(
                        idAcademia =
                            idAcademiaActual,

                        nombre =
                            formulario.nombre,

                        activo =
                            formulario.activo,

                        requiereEquipo =
                            formulario.requiereEquipo,

                        requiereJugador =
                            formulario.requiereJugador,

                        requiereObservacion =
                            formulario
                                .requiereObservacion,

                        afectaMarcador =
                            formulario
                                .afectaMarcador,

                        afectaEstadoPartido =
                            formulario
                                .afectaEstadoPartido,

                        uid = uid
                    )

                } else {

                    repositorio.editarTipo(
                        idAcademia =
                            idAcademiaActual,

                        idTipo =
                            formulario.id,

                        nombre =
                            formulario.nombre,

                        activo =
                            formulario.activo,

                        requiereEquipo =
                            formulario.requiereEquipo,

                        requiereJugador =
                            formulario.requiereJugador,

                        requiereObservacion =
                            formulario
                                .requiereObservacion,

                        afectaMarcador =
                            formulario
                                .afectaMarcador,

                        afectaEstadoPartido =
                            formulario
                                .afectaEstadoPartido,

                        uid = uid
                    )
                }

            resultado
                .onSuccess {

                    _estado.update {
                        it.copy(
                            guardando = false,

                            mostrarFormulario =
                                false,

                            formulario =
                                FormularioEvento(),

                            mensaje =
                                if (
                                    formulario.id ==
                                    null
                                ) {
                                    "Evento creado correctamente"
                                } else {
                                    "Evento actualizado correctamente"
                                }
                        )
                    }
                }
                .onFailure { e ->

                    _estado.update {
                        it.copy(
                            guardando = false,
                            error =
                                mensajeError(e)
                        )
                    }
                }
        }
    }

    fun cambiarEstadoEvento(
        evento:
        TipoEventoCatalogo,

        activo: Boolean
    ) {
        viewModelScope.launch {

            repositorio
                .cambiarEstado(
                    idAcademia =
                        idAcademiaActual,

                    idTipo =
                        evento.id,

                    activo =
                        activo,

                    uid = uid
                )
                .onFailure { e ->

                    _estado.update {
                        it.copy(
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
    ): String =
        when (e) {

            is IllegalArgumentException,
            is IllegalStateException ->

                e.message
                    ?: "No se pudo completar la operación"

            else ->

                "No se pudo completar la operación. " +
                        "Revisa tu conexión e inténtalo nuevamente"
        }

    override fun onCleared() {
        trabajoObservacion?.cancel()
        super.onCleared()
    }
}