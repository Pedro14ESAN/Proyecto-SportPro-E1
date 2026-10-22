package com.example.projectsportpro2026_2.ui.jugadores

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectsportpro2026_2.datos.modelo.DatosPrivadosJugador
import com.example.projectsportpro2026_2.datos.modelo.Jugador
import com.example.projectsportpro2026_2.datos.repositorio.DatosJugador
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioAutenticacion
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioJugadores
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Lo que escribe el usuario en el formulario (todo como texto)
data class CamposJugador(
    val nombres: String = "",
    val apellidos: String = "",
    val fechaNacimiento: String = "",
    val documento: String = "",
    val posicion: String = "",
    val piePreferido: String = "",
    val estatura: String = "",
    val peso: String = "",
    val telefono: String = "",
    val contactoNombre: String = "",
    val contactoTelefono: String = ""
)

data class EstadoJugadores(
    val jugadores: List<Jugador> = emptyList(),
    val cargando: Boolean = false,
    val error: String? = null
)

class JugadoresViewModel(
    private val repositorio: RepositorioJugadores = RepositorioJugadores(),
    private val autenticacion: RepositorioAutenticacion = RepositorioAutenticacion()
) : ViewModel() {

    private val _estado = MutableStateFlow(EstadoJugadores())
    val estado: StateFlow<EstadoJugadores> = _estado.asStateFlow()

    private var idEquipo = ""
    private val uid: String get() = autenticacion.uidActual().orEmpty()

    fun iniciar(id: String) {
        if (id != idEquipo) {
            idEquipo = id
            cargar()
        }
    }

    fun limpiarError() = _estado.update { it.copy(error = null) }

    fun cargar() {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }
            repositorio.listarJugadores(idEquipo)
                .onSuccess { lista -> _estado.update { it.copy(cargando = false, jugadores = lista) } }
                .onFailure { e -> _estado.update { it.copy(cargando = false, error = mensaje(e)) } }
        }
    }

    // Trae los datos sensibles antes de abrir el formulario de edición
    fun cargarPrivados(jugador: Jugador, alCargar: (DatosPrivadosJugador) -> Unit) {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }
            repositorio.obtenerPrivados(jugador.id, uid)
                .onSuccess { privados ->
                    _estado.update { it.copy(cargando = false) }
                    alCargar(privados)
                }
                .onFailure { e -> _estado.update { it.copy(cargando = false, error = mensaje(e)) } }
        }
    }

    // jugadorActual == null  ->  crear; si no  ->  editar
    fun guardar(jugadorActual: Jugador?, campos: CamposJugador, alTerminar: () -> Unit) {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }
            val datos = try {
                aDatos(campos)
            } catch (e: IllegalArgumentException) {
                _estado.update { it.copy(cargando = false, error = e.message) }
                return@launch
            }
            val resultado = if (jugadorActual == null) {
                repositorio.crearJugador(idEquipo, datos, uid).map { }
            } else {
                repositorio.editarJugador(jugadorActual.id, datos, uid)
            }
            resultado
                .onSuccess {
                    alTerminar()
                    cargar()
                }
                .onFailure { e -> _estado.update { it.copy(cargando = false, error = mensaje(e)) } }
        }
    }

    fun cambiarEstado(jugador: Jugador) {
        viewModelScope.launch {
            _estado.update { it.copy(error = null) }
            repositorio.cambiarEstadoJugador(jugador.id, !jugador.activo, uid)
                .onSuccess { cargar() }
                .onFailure { e -> _estado.update { it.copy(error = mensaje(e)) } }
        }
    }

    private fun aDatos(c: CamposJugador) = DatosJugador(
        nombres = c.nombres,
        apellidos = c.apellidos,
        fechaNacimiento = c.fechaNacimiento,
        documento = c.documento,
        posicion = c.posicion,
        piePreferido = c.piePreferido,
        estaturaCm = numero(c.estatura, "La estatura"),
        pesoKg = numero(c.peso, "El peso"),
        telefono = c.telefono,
        contactoEmergenciaNombre = c.contactoNombre,
        contactoEmergenciaTelefono = c.contactoTelefono
    )

    // Vacío = pendiente (null). Texto no numérico = error.
    private fun numero(texto: String, etiqueta: String): Double? {
        val limpio = texto.trim().replace(',', '.')
        if (limpio.isEmpty()) return null
        val valor = limpio.toDoubleOrNull()
        if (valor == null || valor.isNaN() || valor.isInfinite()) {
            throw IllegalArgumentException("$etiqueta debe ser un número")
        }
        return valor
    }

    private fun mensaje(e: Throwable): String = when (e) {
        is IllegalArgumentException, is IllegalStateException ->
            e.message ?: "No se pudo completar la operación"
        else -> "No se pudo completar la operación. Revisa tu conexión e inténtalo de nuevo"
    }
}