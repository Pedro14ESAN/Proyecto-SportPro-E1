package com.example.projectsportpro2026_2.ui.comunidad

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectsportpro2026_2.datos.modelo.DecisionModeracion
import com.example.projectsportpro2026_2.datos.modelo.MotivoReporte
import com.example.projectsportpro2026_2.datos.modelo.ObjetivoReporte
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioAutenticacion
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioBloqueos
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioModeracion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// El contenido sobre el que el usuario tocó "•••" (publicación, comentario o aviso)
data class ContenidoSeleccionado(
    val objetivo: ObjetivoReporte,
    val idAutor: String,
    val nombreAutor: String,
    val resumen: String
)

data class EstadoAccionesModeracion(
    val uid: String = "",
    val puedeModerar: Boolean = false,
    val procesando: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null
)

// Acciones sobre contenido ajeno: reportar, bloquear y moderar directamente (HU24)
class AccionesModeracionViewModel(
    private val moderacion: RepositorioModeracion = RepositorioModeracion(),
    private val bloqueos: RepositorioBloqueos = RepositorioBloqueos(),
    private val autenticacion: RepositorioAutenticacion = RepositorioAutenticacion()
) : ViewModel() {

    private val _estado = MutableStateFlow(EstadoAccionesModeracion())
    val estado: StateFlow<EstadoAccionesModeracion> = _estado.asStateFlow()

    private var iniciado = false

    fun iniciar() {
        if (iniciado) return
        iniciado = true

        viewModelScope.launch {
            val uid = autenticacion.uidActual().orEmpty()
            _estado.update { it.copy(uid = uid) }
            if (uid.isBlank()) return@launch

            moderacion.puedeModerar(uid)
                .onSuccess { puede -> _estado.update { it.copy(puedeModerar = puede) } }
        }
    }

    fun reportar(
        seleccionado: ContenidoSeleccionado,
        motivo: MotivoReporte,
        detalle: String,
        alTerminar: () -> Unit = {}
    ) = ejecutar("Reporte enviado. Un moderador lo revisará.", alTerminar) {
        moderacion.reportar(seleccionado.objetivo, motivo, detalle, _estado.value.uid)
    }

    fun bloquear(
        seleccionado: ContenidoSeleccionado,
        alTerminar: () -> Unit = {}
    ) = ejecutar("Bloqueaste a ${seleccionado.nombreAutor}", alTerminar) {
        bloqueos.bloquear(_estado.value.uid, seleccionado.idAutor, seleccionado.nombreAutor)
    }

    fun moderar(
        seleccionado: ContenidoSeleccionado,
        decision: DecisionModeracion,
        motivo: String,
        alTerminar: () -> Unit = {}
    ) = ejecutar(decision.texto, alTerminar) {
        moderacion.moderar(seleccionado.objetivo, decision, motivo, _estado.value.uid)
    }

    fun limpiarMensajes() {
        _estado.update { it.copy(error = null, mensaje = null) }
    }

    // Las 3 acciones hacen lo mismo: marcar "procesando", ejecutar, y mostrar éxito o error.
    // En vez de repetirlo 3 veces, se escribe una vez y se le pasa la acción.
    private fun ejecutar(
        textoExito: String,
        alTerminar: () -> Unit,
        accion: suspend () -> Result<Unit>
    ) {
        if (_estado.value.procesando) return

        viewModelScope.launch {
            _estado.update { it.copy(procesando = true, error = null) }

            accion()
                .onSuccess {
                    _estado.update { it.copy(procesando = false, mensaje = textoExito) }
                    alTerminar()
                }
                .onFailure { e ->
                    _estado.update {
                        it.copy(procesando = false, error = e.message ?: "No se pudo completar la operación")
                    }
                }
        }
    }
}