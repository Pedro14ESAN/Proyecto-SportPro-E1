package com.example.projectsportpro2026_2.ui.comunidad

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectsportpro2026_2.datos.modelo.Academia
import com.example.projectsportpro2026_2.datos.modelo.AvisoPrueba
import com.example.projectsportpro2026_2.datos.modelo.Categorias
import com.example.projectsportpro2026_2.datos.modelo.EstadoAviso
import com.example.projectsportpro2026_2.datos.modelo.Rol
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioAutenticacion
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioAvisosPrueba
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val UN_DIA = 24L * 60 * 60 * 1000

// Opciones del filtro "Fecha ▾"
enum class FiltroFecha(val texto: String) {
    TODAS("Cualquier fecha"),
    PROXIMOS_7("Próximos 7 días"),
    PROXIMOS_30("Próximos 30 días")
}

// Los 4 filtros del Figma + mostrar cerrados/vencidos. null = sin filtro
data class FiltrosAvisos(
    val categoria: String? = null,
    val posicion: String? = null,
    val ubicacion: String? = null,
    val fecha: FiltroFecha = FiltroFecha.TODAS,
    val incluirNoVigentes: Boolean = false     // criterio HU23: cerrados y vencidos ocultos por defecto
) {
    val hayFiltros: Boolean
        get() = categoria != null || posicion != null || ubicacion != null || fecha != FiltroFecha.TODAS
}

data class EstadoAvisos(
    val uid: String = "",
    val rol: Rol? = null,
    val avisos: List<AvisoPrueba> = emptyList(),
    val academiasParaPublicar: List<Academia> = emptyList(),
    val filtros: FiltrosAvisos = FiltrosAvisos(),
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null
) {
    // Solo DT o ADM con al menos una academia (RC-05)
    val puedePublicar: Boolean
        get() = academiasParaPublicar.isNotEmpty()

    // Opciones de los filtros, sacadas de los avisos que existen
    val categoriasDisponibles: List<String>
        get() = Categorias.lista.filter { c -> avisos.any { it.categoria == c } }

    val ubicacionesDisponibles: List<String>
        get() = avisos.map { it.ubicacion.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
            .sorted()

    fun avisosFiltrados(ahora: Long = System.currentTimeMillis()): List<AvisoPrueba> =
        avisos.filter { aviso ->
            val vigente = aviso.estadoEfectivo(ahora) == EstadoAviso.ABIERTO

            val pasaVigencia = filtros.incluirNoVigentes || vigente
            val pasaCategoria = filtros.categoria == null || aviso.categoria == filtros.categoria
            // Un aviso con posición abierta sirve para cualquier posición buscada
            val pasaPosicion = filtros.posicion == null ||
                    aviso.posicionAbierta() ||
                    filtros.posicion in aviso.posiciones
            val pasaUbicacion = filtros.ubicacion == null ||
                    aviso.ubicacion.trim().equals(filtros.ubicacion, ignoreCase = true)
            val pasaFecha = when (filtros.fecha) {
                FiltroFecha.TODAS -> true
                FiltroFecha.PROXIMOS_7 -> aviso.fechaPrueba <= ahora + 7 * UN_DIA
                FiltroFecha.PROXIMOS_30 -> aviso.fechaPrueba <= ahora + 30 * UN_DIA
            }

            pasaVigencia && pasaCategoria && pasaPosicion && pasaUbicacion && pasaFecha
        }

    // Cerrar o reabrir: el autor, o el ADM de esa academia
    fun puedeGestionar(aviso: AvisoPrueba): Boolean =
        aviso.idAutor == uid ||
                (rol == Rol.ADM && academiasParaPublicar.any { it.id == aviso.idAcademia })
}

class AvisosPruebaViewModel(
    private val repositorio: RepositorioAvisosPrueba = RepositorioAvisosPrueba(),
    private val autenticacion: RepositorioAutenticacion = RepositorioAutenticacion()
) : ViewModel() {

    private val _estado = MutableStateFlow(EstadoAvisos())
    val estado: StateFlow<EstadoAvisos> = _estado.asStateFlow()

    private var iniciado = false

    // ---------- Carga ----------

    fun iniciar() {
        if (iniciado) return
        iniciado = true

        viewModelScope.launch {
            val uid = autenticacion.uidActual()
            if (uid.isNullOrBlank()) {
                _estado.update { it.copy(error = "No existe una sesión activa") }
                return@launch
            }

            val perfil = try {
                autenticacion.obtenerPerfil(uid)
            } catch (e: Exception) {
                null
            }
            _estado.update { it.copy(uid = uid, rol = perfil?.rol) }

            repositorio.academiasParaPublicar(uid)
                .onSuccess { lista -> _estado.update { it.copy(academiasParaPublicar = lista) } }

            cargar()
        }
    }

    fun recargar() {
        viewModelScope.launch { cargar() }
    }

    private suspend fun cargar() {
        _estado.update { it.copy(cargando = true, error = null) }

        repositorio.listar()
            .onSuccess { lista -> _estado.update { it.copy(avisos = lista, cargando = false) } }
            .onFailure { e -> _estado.update { it.copy(cargando = false, error = mensajeError(e)) } }
    }

    // ---------- Filtros ----------

    // Se usa así: actualizarFiltros { it.copy(categoria = "Sub-15") }
    fun actualizarFiltros(cambio: (FiltrosAvisos) -> FiltrosAvisos) {
        _estado.update { it.copy(filtros = cambio(it.filtros)) }
    }

    fun limpiarFiltros() {
        _estado.update {
            it.copy(filtros = FiltrosAvisos(incluirNoVigentes = it.filtros.incluirNoVigentes))
        }
    }

    // ---------- Publicar y cambiar estado ----------

    fun publicar(
        idAcademia: String,
        categoria: String,
        posiciones: List<String>,
        fechaPrueba: Long,
        ubicacion: String,
        descripcion: String,
        alTerminar: () -> Unit = {}
    ) {
        if (_estado.value.guardando) return

        viewModelScope.launch {
            _estado.update { it.copy(guardando = true, error = null) }

            repositorio.crear(
                uid = _estado.value.uid,
                idAcademia = idAcademia,
                categoria = categoria,
                posiciones = posiciones,
                fechaPrueba = fechaPrueba,
                ubicacion = ubicacion,
                descripcion = descripcion
            )
                .onSuccess { aviso ->
                    _estado.update { it.copy(guardando = false, mensaje = "Aviso publicado: ${aviso.titulo()}") }
                    alTerminar()
                    cargar()
                }
                .onFailure { e ->
                    _estado.update { it.copy(guardando = false, error = mensajeError(e)) }
                }
        }
    }

    fun cerrar(aviso: AvisoPrueba) = cambiarEstado(aviso, EstadoAviso.CERRADO, "Aviso cerrado")

    fun reabrir(aviso: AvisoPrueba) = cambiarEstado(aviso, EstadoAviso.ABIERTO, "Aviso reabierto")

    private fun cambiarEstado(aviso: AvisoPrueba, nuevo: EstadoAviso, textoExito: String) {
        viewModelScope.launch {
            repositorio.cambiarEstado(aviso.id, nuevo, _estado.value.uid)
                .onSuccess {
                    _estado.update { it.copy(mensaje = textoExito) }
                    cargar()
                }
                .onFailure { e -> _estado.update { it.copy(error = mensajeError(e)) } }
        }
    }

    fun limpiarMensajes() {
        _estado.update { it.copy(error = null, mensaje = null) }
    }

    private fun mensajeError(e: Throwable): String =
        e.message ?: "No se pudo completar la operación"
}