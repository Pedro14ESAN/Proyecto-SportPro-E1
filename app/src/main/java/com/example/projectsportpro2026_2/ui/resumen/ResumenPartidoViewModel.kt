package com.example.projectsportpro2026_2.ui.resumen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectsportpro2026_2.datos.modelo.evento.EventoPartido
import com.example.projectsportpro2026_2.datos.modelo.resumen.EstadoResumen
import com.example.projectsportpro2026_2.datos.modelo.resumen.EvaluacionResumen
import com.example.projectsportpro2026_2.datos.modelo.resumen.ResumenPartido
import com.example.projectsportpro2026_2.datos.modelo.resumen.VersionResumen
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioAutenticacion
import com.example.projectsportpro2026_2.datos.repositorio.sincronizacion.RepositorioSincronizacion
import com.example.projectsportpro2026_2.datos.resumen.ContextoResumen
import com.example.projectsportpro2026_2.datos.resumen.FuenteResumen
import com.example.projectsportpro2026_2.datos.resumen.RepositorioResumenPartido
import com.example.projectsportpro2026_2.datos.resumen.ServicioIANoDisponibleException
import com.example.projectsportpro2026_2.datos.seguridad.MensajesPermiso
import com.example.projectsportpro2026_2.datos.sincronizacion.RegistroRechazos
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EstadoResumenPartido(
    val cargando: Boolean = true,
    val procesando: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null,

    val contexto: ContextoResumen? = null,
    val resumen: ResumenPartido? = null,
    val historial: List<VersionResumen> = emptyList(),

    // Todos los eventos (activos y anulados) en tiempo real
    val eventos: List<EventoPartido> = emptyList(),
    val pendientesSync: Boolean = false,

    // El último intento con IA falló: se ofrece el borrador sin IA
    val iaNoDisponible: Boolean = false,

    // Texto que el DT está editando (HU27)
    val textoEditado: String = "",
    val evaluacion: EvaluacionResumen = EvaluacionResumen()
) {
    val eventosValidos: List<EventoPartido> get() = FuenteResumen.eventosValidos(eventos)

    val huellaActual: String get() = FuenteResumen.huella(eventos)

    // El borrador se generó con otro conjunto de eventos
    val borradorDesfasado: Boolean
        get() = resumen?.estado == EstadoResumen.BORRADOR &&
                eventos.isNotEmpty() &&
                resumen.huellaEventos != huellaActual

    val marcador: FuenteResumen.Marcador get() = FuenteResumen.marcador(eventos)

    val puedeRevisar: Boolean get() = contexto?.puedeRevisar == true

    val hayCambiosSinGuardar: Boolean
        get() = resumen?.estado == EstadoResumen.BORRADOR &&
                textoEditado.trim() != resumen.textoEditado.trim()
}

class ResumenPartidoViewModel : ViewModel() {

    private val repositorio = RepositorioResumenPartido()
    private val tiempoReal = RepositorioSincronizacion()
    private val autenticacion = RepositorioAutenticacion()

    private val _estado = MutableStateFlow(EstadoResumenPartido())
    val estado: StateFlow<EstadoResumenPartido> = _estado.asStateFlow()

    private var idPartido = ""
    private var trabajos: List<Job> = emptyList()

    fun iniciar(id: String) {
        if (id.isBlank() || id == idPartido) return
        idPartido = id
        cargar()
    }

    fun reintentar() = cargar()

    private fun cargar() {
        trabajos.forEach { it.cancel() }

        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }

            repositorio.prepararContexto(idPartido, autenticacion.uidActual().orEmpty())
                .onSuccess { contexto ->
                    _estado.update { it.copy(cargando = false, contexto = contexto) }
                    suscribirse()
                    cargarHistorial()
                }
                .onFailure { e ->
                    _estado.update { it.copy(cargando = false, error = MensajesPermiso.traducir(e)) }
                }
        }
    }

    private fun suscribirse() {
        trabajos = listOf(
            viewModelScope.launch {
                tiempoReal.escucharEventos(idPartido)
                    .catch { e -> _estado.update { it.copy(error = MensajesPermiso.traducir(e)) } }
                    .collect { instantanea ->
                        val rechazados = RegistroRechazos.rechazados.value
                            .any { it.evento.idPartido == idPartido }
                        _estado.update {
                            it.copy(
                                eventos = instantanea.eventos,
                                pendientesSync = instantanea.escriturasPendientes || rechazados
                            )
                        }
                        revisarDesactualizado()
                    }
            },
            viewModelScope.launch {
                repositorio.escuchar(idPartido)
                    .catch { e -> _estado.update { it.copy(error = MensajesPermiso.traducir(e)) } }
                    .collect { resumen ->
                        _estado.update { actual ->
                            // Si llega una versión nueva, el editor parte de ella
                            val cambioVersion = resumen?.version != actual.resumen?.version ||
                                    resumen?.estado != actual.resumen?.estado
                            actual.copy(
                                resumen = resumen,
                                textoEditado = if (cambioVersion || actual.textoEditado.isBlank())
                                    resumen?.let { r -> r.textoEditado.ifBlank { r.textoOriginalIA } }
                                        .orEmpty()
                                else actual.textoEditado,
                                evaluacion = if (cambioVersion)
                                    resumen?.evaluacion ?: EvaluacionResumen()
                                else actual.evaluacion
                            )
                        }
                        revisarDesactualizado()
                    }
            }
        )
    }

    private fun cargarHistorial() {
        viewModelScope.launch {
            repositorio.historial(idPartido).onSuccess { lista ->
                _estado.update { it.copy(historial = lista) }
            }
        }
    }

    /*
     * RN-13 al abrir la pantalla: si el resumen aprobado ya no
     * coincide con los eventos vigentes, el DT lo marca como
     * desactualizado (por si la corrección se hizo sin conexión).
     */
    private fun revisarDesactualizado() {
        val e = _estado.value
        val contexto = e.contexto ?: return
        val resumen = e.resumen ?: return
        if (!contexto.puedeRevisar || e.eventos.isEmpty() || e.pendientesSync) return
        if (resumen.estado != EstadoResumen.APROBADO || resumen.huellaEventos == e.huellaActual) return

        viewModelScope.launch {
            repositorio.marcarDesactualizadoSiCorresponde(
                idPartido = idPartido,
                uid = contexto.acceso.uid,
                rol = contexto.acceso.rol,
                motivo = "Los eventos del partido cambiaron después de la aprobación",
                huellaActual = e.huellaActual
            ).onSuccess { cambio -> if (cambio) cargarHistorial() }
        }
    }

    fun generar() {
        val e = _estado.value
        val contexto = e.contexto ?: return
        viewModelScope.launch {
            _estado.update { it.copy(procesando = true, error = null, iaNoDisponible = false) }
            repositorio.generarConIA(contexto, e.eventos, e.pendientesSync, e.resumen)
                .onSuccess { version ->
                    _estado.update {
                        it.copy(procesando = false, mensaje = "Borrador v$version generado. Revísalo antes de aprobar.")
                    }
                    cargarHistorial()
                }
                .onFailure { error ->
                    _estado.update {
                        it.copy(
                            procesando = false,
                            iaNoDisponible = error is ServicioIANoDisponibleException,
                            error = MensajesPermiso.traducir(error)
                        )
                    }
                }
        }
    }

    fun generarSinIA() {
        val e = _estado.value
        val contexto = e.contexto ?: return
        viewModelScope.launch {
            _estado.update { it.copy(procesando = true, error = null) }
            repositorio.generarPlantilla(contexto, e.eventos, e.pendientesSync, e.resumen)
                .onSuccess { version ->
                    _estado.update {
                        it.copy(
                            procesando = false,
                            iaNoDisponible = false,
                            mensaje = "Borrador v$version (sin IA) generado. Revísalo antes de aprobar."
                        )
                    }
                    cargarHistorial()
                }
                .onFailure { error ->
                    _estado.update { it.copy(procesando = false, error = MensajesPermiso.traducir(error)) }
                }
        }
    }

    fun editarTexto(texto: String) = _estado.update { it.copy(textoEditado = texto) }

    fun editarEvaluacion(evaluacion: EvaluacionResumen) =
        _estado.update { it.copy(evaluacion = evaluacion) }

    fun guardarBorrador() {
        val e = _estado.value
        val contexto = e.contexto ?: return
        val resumen = e.resumen ?: return
        viewModelScope.launch {
            _estado.update { it.copy(procesando = true, error = null) }
            repositorio.guardarEdicion(contexto, resumen, e.textoEditado)
                .onSuccess { _estado.update { it.copy(procesando = false, mensaje = "Cambios guardados") } }
                .onFailure { error ->
                    _estado.update { it.copy(procesando = false, error = MensajesPermiso.traducir(error)) }
                }
        }
    }

    fun aprobar() {
        val e = _estado.value
        val contexto = e.contexto ?: return
        val resumen = e.resumen ?: return
        viewModelScope.launch {
            _estado.update { it.copy(procesando = true, error = null) }
            repositorio.aprobar(contexto, resumen, e.textoEditado, e.evaluacion, e.eventos)
                .onSuccess {
                    _estado.update { it.copy(procesando = false, mensaje = "✓ Resumen aprobado") }
                    cargarHistorial()
                }
                .onFailure { error ->
                    _estado.update { it.copy(procesando = false, error = MensajesPermiso.traducir(error)) }
                }
        }
    }

    fun rechazar(motivo: String) {
        val e = _estado.value
        val contexto = e.contexto ?: return
        val resumen = e.resumen ?: return
        viewModelScope.launch {
            _estado.update { it.copy(procesando = true, error = null) }
            repositorio.rechazar(contexto, resumen, motivo)
                .onSuccess {
                    _estado.update {
                        it.copy(procesando = false, mensaje = "Borrador rechazado. Puedes generar uno nuevo.")
                    }
                    cargarHistorial()
                }
                .onFailure { error ->
                    _estado.update { it.copy(procesando = false, error = MensajesPermiso.traducir(error)) }
                }
        }
    }

    fun limpiarMensaje() = _estado.update { it.copy(mensaje = null) }
}
