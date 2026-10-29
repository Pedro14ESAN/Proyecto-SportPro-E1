package com.example.projectsportpro2026_2.ui.eventospartido

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectsportpro2026_2.datos.modelo.evento.EquipoEvento
import com.example.projectsportpro2026_2.datos.modelo.evento.EventoPartido
import com.example.projectsportpro2026_2.datos.modelo.evento.TipoEventoCatalogo
import com.example.projectsportpro2026_2.datos.modelo.partido.EstadoPartido
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioAutenticacion
import com.example.projectsportpro2026_2.datos.repositorio.evento.ContextoPartidoEnVivo
import com.example.projectsportpro2026_2.datos.repositorio.evento.MarcadorPartido
import com.example.projectsportpro2026_2.datos.repositorio.evento.RepositorioEventosPartido
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.projectsportpro2026_2.datos.modelo.evento.AuditoriaEventoPartido

data class FormularioEventoPartido(
    val tipo: TipoEventoCatalogo? = null,

    val minuto: String = "",

    val equipoEvento:
    EquipoEvento =
        EquipoEvento.NINGUNO,

    val idJugador: String = "",

    val nombreJugadorRival: String = "",

    val observacion: String = ""
)


enum class AccionEventoPartido {
    CORREGIR,
    ANULAR
}

data class EstadoPartidoEnVivo(
    val contexto:
    ContextoPartidoEnVivo? = null,

    val eventos:
    List<EventoPartido> = emptyList(),

    val marcador:
    MarcadorPartido = MarcadorPartido(),

    val cargando: Boolean = false,

    val registrando: Boolean = false,

    val mostrarFormulario: Boolean = false,

    val formulario: FormularioEventoPartido = FormularioEventoPartido(),

    val eventoSeleccionado: EventoPartido? = null,

    val accionEvento:
    AccionEventoPartido? = null,

    val motivoCambio: String = "",

    val procesandoCambio: Boolean = false,

    val auditoriaEvento:
    List<AuditoriaEventoPartido> = emptyList(),

    val cargandoAuditoria: Boolean = false,

    val mostrarAuditoriaGeneral: Boolean = false,

    val eventosAuditoria:
    List<EventoPartido> = emptyList(),

    val cargandoAuditoriaGeneral: Boolean = false,

    val error: String? = null,

    val mensaje: String? = null
)

class PartidoEnVivoViewModel(
    private val repositorio:
    RepositorioEventosPartido =
        RepositorioEventosPartido(),

    private val autenticacion:
    RepositorioAutenticacion =
        RepositorioAutenticacion()
) : ViewModel() {

    private val _estado =
        MutableStateFlow(
            EstadoPartidoEnVivo()
        )

    val estado:
            StateFlow<EstadoPartidoEnVivo> =
        _estado.asStateFlow()

    private var idPartidoActual = ""

    private val uidActual: String
        get() =
            autenticacion
                .uidActual()
                .orEmpty()

    fun iniciar(
        idPartido: String
    ) {
        if (
            idPartido.isBlank() ||
            idPartido ==
            idPartidoActual
        ) {
            return
        }

        idPartidoActual =
            idPartido

        cargarDatos()
    }

    fun cargarDatos() {

        if (idPartidoActual.isBlank()) {
            return
        }

        viewModelScope.launch {

            _estado.update {
                it.copy(
                    cargando = true,
                    error = null
                )
            }

            val resultadoContexto =
                repositorio.obtenerContexto(
                    idPartido =
                        idPartidoActual,
                    uid =
                        uidActual
                )

            resultadoContexto
                .onFailure { e ->

                    _estado.update {
                        it.copy(
                            cargando = false,
                            error =
                                mensajeError(e)
                        )
                    }

                    return@launch
                }

            val contexto =
                resultadoContexto
                    .getOrNull()
                    ?: return@launch

            val resultadoEventos =
                repositorio.listarEventos(
                    idPartido =
                        idPartidoActual,
                    uid =
                        uidActual
                )

            resultadoEventos
                .onSuccess { eventos ->

                    _estado.update {
                        it.copy(
                            contexto =
                                contexto,

                            eventos =
                                eventos,

                            marcador =
                                repositorio
                                    .calcularMarcador(
                                        eventos
                                    ),

                            cargando = false,
                            error = null
                        )
                    }
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

    private fun cargarAuditoriaEvento(
        idEvento: String
    ) {

        if (
            idPartidoActual.isBlank() ||
            idEvento.isBlank()
        ) {
            return
        }

        viewModelScope.launch {

            _estado.update {
                it.copy(
                    cargandoAuditoria = true
                )
            }

            repositorio
                .listarAuditoriaEvento(
                    idPartido =
                        idPartidoActual,

                    idEvento =
                        idEvento,

                    uid =
                        uidActual
                )
                .onSuccess { auditoria ->

                    _estado.update {
                        it.copy(
                            auditoriaEvento =
                                auditoria,

                            cargandoAuditoria =
                                false
                        )
                    }
                }
                .onFailure { e ->

                    _estado.update {
                        it.copy(
                            cargandoAuditoria =
                                false,
                            error =
                                mensajeError(e)
                        )
                    }
                }
        }
    }
    fun abrirEvento(
        tipo: TipoEventoCatalogo
    ) {

        val contexto =
            _estado.value.contexto
                ?: return

        if (
            contexto.partido.estado !=
            EstadoPartido.EN_CURSO
        ) {
            _estado.update {
                it.copy(
                    error =
                        "El partido ya no está en curso"
                )
            }
            return
        }

        /*
         * HU14 empieza cuando el partido ya está EN_CURSO,
         * por lo que Inicio no debe registrarse nuevamente.
         */
        if (
            tipo.codigo ==
            "INICIO"
        ) {
            _estado.update {
                it.copy(
                    error =
                        "El partido ya se encuentra en curso"
                )
            }
            return
        }

        val equipoInicial =
            if (tipo.requiereEquipo) {
                EquipoEvento.PROPIO
            } else {
                EquipoEvento.NINGUNO
            }

        _estado.update {
            it.copy(
                mostrarFormulario = true,

                formulario =
                    FormularioEventoPartido(
                        tipo = tipo,
                        equipoEvento =
                            equipoInicial
                    ),

                error = null,
                mensaje = null
            )
        }
    }

    fun cerrarFormulario() {

        if (_estado.value.registrando) {
            return
        }

        _estado.update {
            it.copy(
                mostrarFormulario = false,
                formulario =
                    FormularioEventoPartido()
            )
        }
    }

    fun iniciarCorreccionEvento() {

        val actual =
            _estado.value

        val evento =
            actual.eventoSeleccionado
                ?: return

        val contexto =
            actual.contexto
                ?: return

        /*
         * Primero intentamos utilizar la configuración
         * actual de HU13.
         */
        val tipoActual =
            contexto.tiposEvento
                .firstOrNull {
                    it.id ==
                            evento.idTipoEvento
                }
            /*
             * Si el tipo fue desactivado después de
             * registrarse, construimos la información
             * necesaria desde la fotografía histórica.
             */
                ?: TipoEventoCatalogo(
                    id =
                        evento.idTipoEvento,

                    codigo =
                        evento.codigoTipoEvento,

                    nombre =
                        evento.nombreTipoEvento,

                    activo = false,

                    requiereEquipo =
                        evento.equipoEvento !=
                                EquipoEvento.NINGUNO,

                    requiereJugador =
                        evento.idJugador.isNotBlank() ||
                                evento.nombreJugador.isNotBlank(),

                    requiereObservacion =
                        evento.observacion.isNotBlank(),

                    afectaMarcador =
                        evento.afectaMarcador,

                    afectaEstadoPartido =
                        evento.afectaEstadoPartido
                )

        _estado.update {
            it.copy(
                accionEvento =
                    AccionEventoPartido.CORREGIR,

                motivoCambio = "",

                formulario =
                    FormularioEventoPartido(
                        tipo =
                            tipoActual,

                        minuto =
                            evento.minuto
                                .toString(),

                        equipoEvento =
                            evento.equipoEvento,

                        idJugador =
                            evento.idJugador,

                        nombreJugadorRival =
                            if (
                                evento.equipoEvento ==
                                EquipoEvento.RIVAL
                            ) {
                                evento.nombreJugador
                            } else {
                                ""
                            },

                        observacion =
                            evento.observacion
                    ),

                error = null
            )
        }
    }
    fun abrirAuditoriaGeneral() {

        if (idPartidoActual.isBlank()) {
            return
        }

        _estado.update {
            it.copy(
                mostrarAuditoriaGeneral = true,
                cargandoAuditoriaGeneral = true,
                error = null
            )
        }

        viewModelScope.launch {

            repositorio
                .listarEventosAuditoria(
                    idPartido =
                        idPartidoActual,

                    uid =
                        uidActual
                )
                .onSuccess { eventos ->

                    _estado.update {
                        it.copy(
                            eventosAuditoria =
                                eventos,

                            cargandoAuditoriaGeneral =
                                false
                        )
                    }
                }
                .onFailure { e ->

                    _estado.update {
                        it.copy(
                            cargandoAuditoriaGeneral =
                                false,

                            error =
                                mensajeError(e)
                        )
                    }
                }
        }
    }

    fun cerrarAuditoriaGeneral() {

        _estado.update {
            it.copy(
                mostrarAuditoriaGeneral =
                    false
            )
        }
    }

    fun iniciarAnulacionEvento() {

        if (
            _estado.value.eventoSeleccionado ==
            null
        ) {
            return
        }

        _estado.update {
            it.copy(
                accionEvento =
                    AccionEventoPartido.ANULAR,

                motivoCambio = "",

                error = null
            )
        }
    }

    fun cambiarMotivoCambio(
        valor: String
    ) {

        _estado.update {
            it.copy(
                motivoCambio =
                    valor
            )
        }
    }

    fun cancelarAccionEvento() {

        if (_estado.value.procesandoCambio) {
            return
        }

        _estado.update {
            it.copy(
                accionEvento = null,
                motivoCambio = "",
                formulario =
                    FormularioEventoPartido(),
                error = null
            )
        }
    }

    fun guardarCorreccionEvento() {

        val actual =
            _estado.value

        val evento =
            actual.eventoSeleccionado
                ?: return

        val formulario =
            actual.formulario

        val tipo =
            formulario.tipo
                ?: return

        val motivo =
            actual.motivoCambio
                .trim()

        if (motivo.isBlank()) {

            _estado.update {
                it.copy(
                    error =
                        "El motivo de la corrección es obligatorio"
                )
            }

            return
        }

        val minuto =
            formulario.minuto
                .toIntOrNull()

        if (minuto == null || minuto < 0) {

            _estado.update {
                it.copy(
                    error =
                        "Debes indicar un minuto válido"
                )
            }

            return
        }

        if (
            tipo.requiereEquipo &&
            formulario.equipoEvento ==
            EquipoEvento.NINGUNO
        ) {

            _estado.update {
                it.copy(
                    error =
                        "Selecciona el equipo del evento"
                )
            }

            return
        }

        if (
            tipo.requiereJugador &&
            formulario.equipoEvento !=
            EquipoEvento.RIVAL &&
            formulario.idJugador.isBlank()
        ) {

            _estado.update {
                it.copy(
                    error =
                        "Selecciona un jugador"
                )
            }

            return
        }

        if (
            tipo.requiereJugador &&
            formulario.equipoEvento ==
            EquipoEvento.RIVAL &&
            formulario.nombreJugadorRival
                .isBlank()
        ) {

            _estado.update {
                it.copy(
                    error =
                        "Indica el jugador rival"
                )
            }

            return
        }

        if (
            tipo.requiereObservacion &&
            formulario.observacion
                .isBlank()
        ) {

            _estado.update {
                it.copy(
                    error =
                        "Este evento requiere una observación"
                )
            }

            return
        }

        viewModelScope.launch {

            _estado.update {
                it.copy(
                    procesandoCambio = true,
                    error = null,
                    mensaje = null
                )
            }

            repositorio
                .corregirEvento(
                    idPartido =
                        idPartidoActual,

                    idEvento =
                        evento.id,

                    versionEsperada =
                        evento.version,

                    /*
                     * Conservamos el mismo tipo.
                     */
                    idTipoEvento =
                        evento.idTipoEvento,

                    minuto =
                        minuto,

                    equipoEvento =
                        formulario.equipoEvento,

                    idJugador =
                        formulario.idJugador,

                    nombreJugadorRival =
                        formulario
                            .nombreJugadorRival,

                    observacion =
                        formulario.observacion,

                    motivo =
                        motivo,

                    uid =
                        uidActual
                )
                .onSuccess { corregido ->

                    refrescarDespuesCambio()

                    _estado.update {
                        it.copy(
                            eventoSeleccionado =
                                corregido,

                            accionEvento = null,

                            motivoCambio = "",

                            formulario =
                                FormularioEventoPartido(),

                            procesandoCambio =
                                false,

                            mensaje =
                                "Evento corregido correctamente"
                        )
                    }

                    cargarAuditoriaEvento(
                        corregido.id
                    )
                }
                .onFailure { e ->

                    _estado.update {
                        it.copy(
                            procesandoCambio =
                                false,

                            error =
                                mensajeError(e)
                        )
                    }
                }
        }
    }

    fun confirmarAnulacionEvento() {

        val actual =
            _estado.value

        val evento =
            actual.eventoSeleccionado
                ?: return

        val motivo =
            actual.motivoCambio
                .trim()

        if (motivo.isBlank()) {

            _estado.update {
                it.copy(
                    error =
                        "El motivo de la anulación es obligatorio"
                )
            }

            return
        }

        viewModelScope.launch {

            _estado.update {
                it.copy(
                    procesandoCambio = true,
                    error = null,
                    mensaje = null
                )
            }

            repositorio
                .anularEvento(
                    idPartido =
                        idPartidoActual,

                    idEvento =
                        evento.id,

                    versionEsperada =
                        evento.version,

                    motivo =
                        motivo,

                    uid =
                        uidActual
                )
                .onSuccess {

                    /*
                     * listarEventos() ya excluye ANULADO.
                     * Por eso desaparecerá automáticamente
                     * de la cronología activa.
                     */
                    refrescarDespuesCambio()

                    _estado.update {
                        it.copy(
                            eventoSeleccionado = null,
                            accionEvento = null,
                            motivoCambio = "",
                            auditoriaEvento =
                                emptyList(),

                            procesandoCambio =
                                false,

                            mensaje =
                                "Evento anulado correctamente"
                        )
                    }
                }
                .onFailure { e ->

                    _estado.update {
                        it.copy(
                            procesandoCambio =
                                false,

                            error =
                                mensajeError(e)
                        )
                    }
                }
        }
    }
    fun cambiarMinuto(
        valor: String
    ) {

        /*
         * Permitimos únicamente números.
         */
        if (
            valor.isNotEmpty() &&
            !valor.all {
                it.isDigit()
            }
        ) {
            return
        }

        _estado.update {
            it.copy(
                formulario =
                    it.formulario.copy(
                        minuto = valor
                    )
            )
        }
    }

    private suspend fun refrescarDespuesCambio() {

        repositorio
            .listarEventos(
                idPartido =
                    idPartidoActual,

                uid =
                    uidActual
            )
            .onSuccess { eventos ->

                _estado.update {
                    it.copy(
                        eventos =
                            eventos,

                        marcador =
                            repositorio
                                .calcularMarcador(
                                    eventos
                                )
                    )
                }
            }
    }

    fun cambiarEquipo(
        equipo:
        EquipoEvento
    ) {

        _estado.update {
            it.copy(
                formulario =
                    it.formulario.copy(
                        equipoEvento =
                            equipo,

                        /*
                         * Al cambiar de equipo limpiamos
                         * el jugador anterior para evitar
                         * mezclar un jugador propio con
                         * el rival.
                         */
                        idJugador = "",

                        nombreJugadorRival = ""
                    )
            )
        }
    }

    fun cambiarJugador(
        idJugador: String
    ) {

        _estado.update {
            it.copy(
                formulario =
                    it.formulario.copy(
                        idJugador =
                            idJugador
                    )
            )
        }
    }

    fun cambiarNombreJugadorRival(
        valor: String
    ) {

        _estado.update {
            it.copy(
                formulario =
                    it.formulario.copy(
                        nombreJugadorRival =
                            valor
                    )
            )
        }
    }

    fun cambiarObservacion(
        valor: String
    ) {

        _estado.update {
            it.copy(
                formulario =
                    it.formulario.copy(
                        observacion =
                            valor
                    )
            )
        }
    }

    fun registrarEvento() {

        val actual =
            _estado.value

        val formulario =
            actual.formulario

        val tipo =
            formulario.tipo

        if (tipo == null) {

            _estado.update {
                it.copy(
                    error =
                        "Debes seleccionar un tipo de evento"
                )
            }

            return
        }

        val minuto =
            formulario.minuto
                .toIntOrNull()

        if (minuto == null) {

            _estado.update {
                it.copy(
                    error =
                        "Debes indicar el minuto del evento"
                )
            }

            return
        }

        if (minuto < 0) {

            _estado.update {
                it.copy(
                    error =
                        "El minuto no puede ser negativo"
                )
            }

            return
        }

        /*
         * Estas validaciones hacen más agradable la UI,
         * pero el repositorio volverá a comprobarlas.
         */
        if (
            tipo.requiereEquipo &&
            formulario.equipoEvento ==
            EquipoEvento.NINGUNO
        ) {

            _estado.update {
                it.copy(
                    error =
                        "Selecciona el equipo del evento"
                )
            }

            return
        }

        if (
            tipo.requiereJugador &&
            formulario.equipoEvento != EquipoEvento.RIVAL &&
            formulario.idJugador.isBlank()
        ) {

            _estado.update {
                it.copy(
                    error =
                        "Selecciona un jugador"
                )
            }

            return
        }

        if (
            tipo.requiereJugador &&
            formulario.equipoEvento ==
            EquipoEvento.RIVAL &&
            formulario
                .nombreJugadorRival
                .isBlank()
        ) {

            _estado.update {
                it.copy(
                    error =
                        "Indica el jugador rival"
                )
            }

            return
        }

        if (
            tipo.requiereObservacion &&
            formulario.observacion
                .isBlank()
        ) {

            _estado.update {
                it.copy(
                    error =
                        "Este evento requiere una observación"
                )
            }

            return
        }

        viewModelScope.launch {

            _estado.update {
                it.copy(
                    registrando = true,
                    error = null,
                    mensaje = null
                )
            }

            repositorio
                .registrarEvento(
                    idPartido =
                        idPartidoActual,

                    idTipoEvento =
                        tipo.id,

                    minuto =
                        minuto,

                    equipoEvento =
                        formulario
                            .equipoEvento,

                    idJugador =
                        formulario
                            .idJugador,

                    nombreJugadorRival =
                        formulario
                            .nombreJugadorRival,

                    observacion =
                        formulario
                            .observacion,

                    uid =
                        uidActual
                )
                .onSuccess { evento ->

                    /*
                     * Después de registrar volvemos a leer
                     * nuestra cronología.
                     *
                     * Esto refresca ESTA pantalla.
                     * No implementa HU17 de sincronización
                     * entre dispositivos.
                     */
                    val eventos =
                        repositorio
                            .listarEventos(
                                idPartido =
                                    idPartidoActual,

                                uid =
                                    uidActual
                            )
                            .getOrElse {
                                actual.eventos +
                                        evento
                            }

                    var contextoActualizado =
                        _estado.value.contexto

                    /*
                     * FIN cambia el estado del partido
                     * dentro del repositorio.
                     *
                     * Actualizamos también nuestra copia
                     * local para bloquear nuevos registros.
                     */
                    if (
                        evento.codigoTipoEvento ==
                        "FIN"
                    ) {

                        contextoActualizado =
                            contextoActualizado
                                ?.copy(
                                    partido =
                                        contextoActualizado
                                            .partido
                                            .copy(
                                                estado =
                                                    EstadoPartido
                                                        .FINALIZADO
                                            )
                                )
                    }

                    _estado.update {
                        it.copy(
                            contexto =
                                contextoActualizado,

                            eventos =
                                eventos,

                            marcador =
                                repositorio
                                    .calcularMarcador(
                                        eventos
                                    ),

                            registrando = false,

                            mostrarFormulario =
                                false,

                            formulario =
                                FormularioEventoPartido(),

                            mensaje =
                                if (
                                    evento.codigoTipoEvento ==
                                    "FIN"
                                ) {
                                    "Partido finalizado correctamente"
                                } else {
                                    "${evento.nombreTipoEvento} registrado correctamente"
                                }
                        )
                    }
                }
                .onFailure { e ->

                    _estado.update {
                        it.copy(
                            registrando = false,
                            error =
                                mensajeError(e)
                        )
                    }
                }
        }
    }
    fun abrirDetalleEvento(
        evento: EventoPartido
    ) {

        _estado.update {
            it.copy(
                eventoSeleccionado = evento,
                accionEvento = null,
                motivoCambio = "",
                auditoriaEvento = emptyList(),
                error = null,
                mensaje = null
            )
        }

        cargarAuditoriaEvento(
            evento.id
        )
    }

    fun cerrarDetalleEvento() {

        if (_estado.value.procesandoCambio) {
            return
        }

        _estado.update {
            it.copy(
                eventoSeleccionado = null,
                accionEvento = null,
                motivoCambio = "",
                auditoriaEvento = emptyList()
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

                "No se pudo completar la operación. Revisa tu conexión."
        }
}