package com.example.projectsportpro2026_2.ui.vinculaciones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectsportpro2026_2.datos.repositorio.CuentaPorVincular
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioAutenticacion
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioCuentasJugador
import com.example.projectsportpro2026_2.datos.repositorio.ResultadoVinculo
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EstadoCuentasJugadores(
    val cuentas: List<CuentaPorVincular> = emptyList(),
    val cargando: Boolean = false,
    val aviso: String? = null,
    val error: String? = null
)

class CuentasJugadoresViewModel(
    private val repositorio: RepositorioCuentasJugador = RepositorioCuentasJugador(),
    private val autenticacion: RepositorioAutenticacion = RepositorioAutenticacion()
) : ViewModel() {

    private val _estado = MutableStateFlow(EstadoCuentasJugadores())
    val estado: StateFlow<EstadoCuentasJugadores> = _estado.asStateFlow()

    private val uid: String get() = autenticacion.uidActual().orEmpty()

    init {
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }
            repositorio.listar(uid)
                .onSuccess { lista -> _estado.update { it.copy(cargando = false, cuentas = lista) } }
                .onFailure { e -> _estado.update { it.copy(cargando = false, error = mensaje(e)) } }
        }
    }

    fun vincular(cuenta: CuentaPorVincular) {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, aviso = null, error = null) }
            repositorio.vincular(cuenta.perfil.uid, cuenta.ficha.id, uid)
                .onSuccess { resultado ->
                    val nombre = cuenta.perfil.nombre
                    val aviso = when (resultado) {
                        ResultadoVinculo.VINCULADA_Y_ACTIVADA ->
                            "Cuenta de $nombre vinculada y activada"
                        ResultadoVinculo.VINCULADA ->
                            "Cuenta de $nombre vinculada"
                        ResultadoVinculo.VINCULADA_PENDIENTE ->
                            "Cuenta de $nombre vinculada. Seguirá pendiente hasta que autorices a un apoderado"
                    }
                    _estado.update { it.copy(aviso = aviso) }
                    cargar()
                }
                .onFailure { e -> _estado.update { it.copy(cargando = false, error = mensaje(e)) } }
        }
    }

    private fun mensaje(e: Throwable): String = when (e) {
        is IllegalArgumentException, is IllegalStateException ->
            e.message ?: "No se pudo completar la operación"
        is FirebaseFirestoreException -> when (e.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                "No tienes permiso para realizar esta operación"
            FirebaseFirestoreException.Code.UNAVAILABLE ->
                "Sin conexión. Revisa tu internet e inténtalo de nuevo"
            else -> "No se pudo completar la operación (${e.code})"
        }
        else -> "No se pudo completar la operación. Revisa tu conexión e inténtalo de nuevo"
    }
}