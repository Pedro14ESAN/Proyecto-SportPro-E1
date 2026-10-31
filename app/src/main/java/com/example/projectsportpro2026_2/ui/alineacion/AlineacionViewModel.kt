package com.example.projectsportpro2026_2.ui.alineacion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectsportpro2026_2.datos.modelo.Jugador
import com.example.projectsportpro2026_2.datos.modelo.alineacion.FormatoPartido
import com.example.projectsportpro2026_2.datos.modelo.alineacion.JugadorAlineacion
import com.example.projectsportpro2026_2.datos.modelo.alineacion.TipoJugadorAlineacion
import com.example.projectsportpro2026_2.datos.modelo.partido.EstadoPartido
import com.example.projectsportpro2026_2.datos.modelo.partido.Partido
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioAutenticacion
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioJugadores
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioPartidos
import com.example.projectsportpro2026_2.datos.repositorio.alineacion.RepositorioAlineaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SlotCampo(
    val id: String,
    val x: Float,
    val y: Float,
    val etiqueta: String
)

data class EstadoAlineacion(
    val partido: Partido? = null,

    // Todos los jugadores registrados en el equipo.
    val plantilla: List<Jugador> = emptyList(),

    // Solo convocados cuyo estado actual es DISPONIBLE.
    val disponibles: List<Jugador> = emptyList(),

    // Titulares + suplentes que forman la alineación actual.
    val jugadoresAlineacion: List<JugadorAlineacion> = emptyList(),

    val formato: FormatoPartido = FormatoPartido.ONCE_VS_ONCE,
    val confirmada: Boolean = false,

    val cargando: Boolean = false,
    val guardando: Boolean = false,

    val error: String? = null,
    val mensaje: String? = null,

    // Se usa para mostrar el diálogo del Figma cuando
    // un titular deja de estar DISPONIBLE.
    val jugadorNoDisponible: Jugador? = null
)

class AlineacionViewModel(
    private val repositorioAlineaciones: RepositorioAlineaciones =
        RepositorioAlineaciones(),

    private val repositorioPartidos: RepositorioPartidos =
        RepositorioPartidos(),

    private val repositorioJugadores: RepositorioJugadores =
        RepositorioJugadores(),

    private val autenticacion: RepositorioAutenticacion =
        RepositorioAutenticacion()
) : ViewModel() {

    private val _estado =
        MutableStateFlow(
            EstadoAlineacion()
        )

    val estado: StateFlow<EstadoAlineacion> =
        _estado.asStateFlow()

    private var idPartidoActual = ""

    private val idsAdvertenciasIgnoradas =
        mutableSetOf<String>()

    private val uid: String
        get() = autenticacion
            .uidActual()
            .orEmpty()

    private fun guardarBorrador() {
        val actual = _estado.value

        val partido = actual.partido
            ?: return

        if (
            partido.estado !=
            EstadoPartido.PROGRAMADO
        ) {
            return
        }

        viewModelScope.launch {

            repositorioAlineaciones
                .guardarAlineacion(
                    idPartido = idPartidoActual,
                    formato = actual.formato,
                    jugadoresAlineacion =
                        actual.jugadoresAlineacion,
                    confirmada = false,
                    uid = uid
                )
                .onFailure { e ->

                    _estado.update {
                        it.copy(
                            error = mensajeError(e)
                        )
                    }
                }
        }
    }

    fun iniciar(
        idPartido: String
    ) {
        idPartidoActual = idPartido
        idsAdvertenciasIgnoradas.clear()
        cargar()
    }

    fun cargar() {
        if (idPartidoActual.isBlank()) {
            return
        }

        viewModelScope.launch {

            _estado.update {
                it.copy(
                    cargando = true,
                    error = null,
                    mensaje = null
                )
            }

            try {

                val partido =
                    repositorioPartidos
                        .obtenerPartido(
                            idPartidoActual,
                            uid
                        )
                        .getOrThrow()

                val plantilla =
                    repositorioJugadores
                        .listarJugadores(
                            partido.idEquipo
                        )
                        .getOrThrow()

                val disponibles =
                    repositorioAlineaciones
                        .listarJugadoresDisponibles(
                            idPartidoActual,
                            uid
                        )
                        .getOrThrow()

                val alineacionGuardada =
                    repositorioAlineaciones
                        .obtenerAlineacion(
                            idPartidoActual,
                            uid
                        )
                        .getOrThrow()

                val jugadoresGuardados =
                    repositorioAlineaciones
                        .listarJugadoresAlineacion(
                            idPartidoActual,
                            uid
                        )
                        .getOrThrow()

                val formato =
                    alineacionGuardada?.formato
                        ?: FormatoPartido.ONCE_VS_ONCE

                val idsDisponibles =
                    disponibles
                        .map { it.id }
                        .toSet()

                /*
                 * Los titulares guardados se conservan incluso si
                 * dejaron de estar disponibles.
                 *
                 * Así podemos mostrar la advertencia del Figma
                 * en vez de quitarlos silenciosamente.
                 */
                val titularesGuardados =
                    jugadoresGuardados.filter {
                        it.tipo ==
                                TipoJugadorAlineacion.TITULAR
                    }

                /*
                 * Un suplente que dejó de estar disponible ya no
                 * debe aparecer como candidato.
                 */
                val suplentesValidos =
                    jugadoresGuardados.filter {
                        it.tipo ==
                                TipoJugadorAlineacion.SUPLENTE &&
                                it.idJugador in idsDisponibles
                    }

                val idsYaPresentes =
                    (titularesGuardados + suplentesValidos)
                        .map {
                            it.idJugador
                        }
                        .toSet()

                var siguienteOrden =
                    jugadoresGuardados
                        .maxOfOrNull {
                            it.orden
                        }
                        ?.plus(1)
                        ?: 0

                val nuevosDisponibles =
                    disponibles
                        .filter {
                            it.id !in idsYaPresentes
                        }
                        .map { jugador ->

                            JugadorAlineacion(
                                idJugador = jugador.id,
                                tipo =
                                    TipoJugadorAlineacion.SUPLENTE,
                                posicionCampo = "",
                                orden = siguienteOrden++
                            )
                        }

                val jugadoresAlineacion =
                    (
                            titularesGuardados +
                                    suplentesValidos +
                                    nuevosDisponibles
                            ).distinctBy {
                            it.idJugador
                        }

                _estado.update {
                    it.copy(
                        partido = partido,
                        plantilla = plantilla,
                        disponibles = disponibles,
                        jugadoresAlineacion =
                            jugadoresAlineacion,
                        formato = formato,
                        confirmada =
                            alineacionGuardada
                                ?.confirmada == true,
                        cargando = false
                    )
                }

                detectarJugadorNoDisponible()

            } catch (e: Exception) {

                _estado.update {
                    it.copy(
                        cargando = false,
                        error = mensajeError(e)
                    )
                }
            }
        }
    }

    fun cambiarFormato(
        nuevoFormato: FormatoPartido
    ) {
        if (!verificarEditable()) {
            return
        }

        val actual =
            _estado.value

        if (actual.formato == nuevoFormato) {
            return
        }

        val titulares =
            actual.jugadoresAlineacion.filter {
                it.tipo ==
                        TipoJugadorAlineacion.TITULAR
            }

        if (
            titulares.size >
            nuevoFormato.maximoTitulares
        ) {
            _estado.update {
                it.copy(
                    error =
                        "Primero quita titulares. " +
                                "El formato ${nuevoFormato.etiqueta} " +
                                "permite máximo " +
                                "${nuevoFormato.maximoTitulares}."
                )
            }
            return
        }

        val nuevosSlots =
            slotsParaFormato(
                nuevoFormato
            )

        val usados =
            mutableSetOf<String>()

        val plantillaPorId =
            actual.plantilla
                .associateBy {
                    it.id
                }

        val reajustados =
            actual.jugadoresAlineacion.map {
                    jugadorAlineacion ->

                if (
                    jugadorAlineacion.tipo !=
                    TipoJugadorAlineacion.TITULAR
                ) {
                    return@map jugadorAlineacion
                }

                val posicionActual =
                    jugadorAlineacion.posicionCampo

                val posicionSigueExistiendo =
                    nuevosSlots.any {
                        it.id == posicionActual
                    } &&
                            posicionActual !in usados

                if (posicionSigueExistiendo) {

                    usados.add(
                        posicionActual
                    )

                    jugadorAlineacion

                } else {

                    val jugador =
                        plantillaPorId[
                            jugadorAlineacion.idJugador
                        ]

                    val nuevoSlot =
                        encontrarSlotPreferido(
                            jugador = jugador,
                            slots = nuevosSlots,
                            ocupados = usados
                        )

                    if (nuevoSlot != null) {
                        usados.add(
                            nuevoSlot.id
                        )
                    }

                    jugadorAlineacion.copy(
                        posicionCampo =
                            nuevoSlot?.id.orEmpty()
                    )
                }
            }

        _estado.update {
            it.copy(
                formato = nuevoFormato,
                jugadoresAlineacion =
                    reajustados,
                confirmada = false,
                error = null,
                mensaje = null
            )
        }
        guardarBorrador()
    }

    fun pasarATitular(
        jugador: Jugador
    ) {
        if (!verificarEditable()) {
            return
        }

        val actual =
            _estado.value

        if (
            jugador.id !in
            actual.disponibles.map {
                it.id
            }
        ) {
            _estado.update {
                it.copy(
                    error =
                        "${jugador.nombreCompleto()} " +
                                "ya no está disponible para el partido"
                )
            }
            return
        }

        val titulares =
            actual.jugadoresAlineacion.filter {
                it.tipo ==
                        TipoJugadorAlineacion.TITULAR
            }

        if (
            titulares.any {
                it.idJugador == jugador.id
            }
        ) {
            return
        }

        if (
            titulares.size >=
            actual.formato.maximoTitulares
        ) {
            _estado.update {
                it.copy(
                    error =
                        "Ya completaste los " +
                                "${actual.formato.maximoTitulares} " +
                                "titulares del formato " +
                                actual.formato.etiqueta
                )
            }
            return
        }

        val slots =
            slotsParaFormato(
                actual.formato
            )

        val ocupados =
            titulares
                .map {
                    it.posicionCampo
                }
                .filter {
                    it.isNotBlank()
                }
                .toSet()

        val slot =
            encontrarSlotPreferido(
                jugador = jugador,
                slots = slots,
                ocupados = ocupados
            )

        if (slot == null) {
            _estado.update {
                it.copy(
                    error =
                        "No hay una posición libre en el campo"
                )
            }
            return
        }

        val yaExiste =
            actual.jugadoresAlineacion.any {
                it.idJugador == jugador.id
            }

        val nuevaLista =
            if (yaExiste) {

                actual.jugadoresAlineacion.map {

                    if (
                        it.idJugador ==
                        jugador.id
                    ) {
                        it.copy(
                            tipo =
                                TipoJugadorAlineacion.TITULAR,
                            posicionCampo =
                                slot.id
                        )
                    } else {
                        it
                    }
                }

            } else {

                actual.jugadoresAlineacion +
                        JugadorAlineacion(
                            idJugador = jugador.id,
                            tipo =
                                TipoJugadorAlineacion.TITULAR,
                            posicionCampo =
                                slot.id,
                            orden =
                                actual.jugadoresAlineacion.size
                        )
            }

        _estado.update {
            it.copy(
                jugadoresAlineacion =
                    nuevaLista,
                confirmada = false,
                error = null,
                mensaje = null
            )
        }
        guardarBorrador()
    }

    fun pasarASuplente(
        jugador: Jugador
    ) {
        if (!verificarEditable()) {
            return
        }

        _estado.update { actual ->

            actual.copy(
                jugadoresAlineacion =
                    actual.jugadoresAlineacion.map {

                        if (
                            it.idJugador ==
                            jugador.id
                        ) {
                            it.copy(
                                tipo =
                                    TipoJugadorAlineacion.SUPLENTE,
                                posicionCampo = ""
                            )
                        } else {
                            it
                        }
                    },
                confirmada = false,
                error = null,
                mensaje = null
            )
        }
        guardarBorrador()
    }

    fun asignarASlot(
        idJugador: String,
        idSlot: String
    ) {
        if (!verificarEditable()) {
            return
        }

        val actual =
            _estado.value

        val slotValido =
            slotsParaFormato(
                actual.formato
            ).any {
                it.id == idSlot
            }

        if (!slotValido) {
            return
        }

        val otroEnEseSlot =
            actual.jugadoresAlineacion
                .firstOrNull {
                    it.tipo ==
                            TipoJugadorAlineacion.TITULAR &&
                            it.posicionCampo == idSlot &&
                            it.idJugador != idJugador
                }

        if (otroEnEseSlot != null) {
            _estado.update {
                it.copy(
                    error =
                        "Esa posición ya está ocupada"
                )
            }
            return
        }

        _estado.update {
            it.copy(
                jugadoresAlineacion =
                    it.jugadoresAlineacion.map {
                            jugador ->

                        if (
                            jugador.idJugador ==
                            idJugador &&
                            jugador.tipo ==
                            TipoJugadorAlineacion.TITULAR
                        ) {
                            jugador.copy(
                                posicionCampo =
                                    idSlot
                            )
                        } else {
                            jugador
                        }
                    },
                confirmada = false,
                error = null
            )
        }
        guardarBorrador()
    }

    fun quitarJugadorNoDisponible() {
        val jugador =
            _estado.value
                .jugadorNoDisponible
                ?: return

        idsAdvertenciasIgnoradas.remove(
            jugador.id
        )

        _estado.update {
            it.copy(
                jugadoresAlineacion =
                    it.jugadoresAlineacion.filter {
                            alineado ->
                        alineado.idJugador !=
                                jugador.id
                    },
                jugadorNoDisponible = null,
                confirmada = false
            )
        }

        guardarBorrador()

        detectarJugadorNoDisponible()
    }

    fun ignorarJugadorNoDisponible() {
        val jugador =
            _estado.value
                .jugadorNoDisponible
                ?: return

        idsAdvertenciasIgnoradas.add(
            jugador.id
        )

        _estado.update {
            it.copy(
                jugadorNoDisponible = null
            )
        }

        detectarJugadorNoDisponible()
    }

    fun confirmarAlineacion() {
        if (!verificarEditable()) {
            return
        }

        val actual =
            _estado.value

        val titulares =
            actual.jugadoresAlineacion.filter {
                it.tipo ==
                        TipoJugadorAlineacion.TITULAR
            }

        if (
            titulares.size !=
            actual.formato.maximoTitulares
        ) {
            _estado.update {
                it.copy(
                    error =
                        "Debes completar " +
                                "${actual.formato.maximoTitulares} " +
                                "titulares para confirmar " +
                                actual.formato.etiqueta
                )
            }
            return
        }

        val idsDisponibles =
            actual.disponibles
                .map {
                    it.id
                }
                .toSet()

        val titularNoDisponible =
            titulares.firstOrNull {
                it.idJugador !in idsDisponibles
            }

        if (titularNoDisponible != null) {

            val jugador =
                actual.plantilla
                    .firstOrNull {
                        it.id ==
                                titularNoDisponible.idJugador
                    }

            _estado.update {
                it.copy(
                    jugadorNoDisponible =
                        jugador,
                    error =
                        "Hay un titular que ya no está disponible"
                )
            }

            return
        }

        val posicionesActuales =
            titulares
                .map {
                    it.posicionCampo
                }
                .toSet()

        val posicionesRequeridas =
            slotsParaFormato(
                actual.formato
            )
                .map {
                    it.id
                }
                .toSet()

        if (
            posicionesActuales !=
            posicionesRequeridas
        ) {
            _estado.update {
                it.copy(
                    error =
                        "Debes completar todas las posiciones del campo antes de confirmar"
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

            repositorioAlineaciones
                .guardarAlineacion(
                    idPartido =
                        idPartidoActual,
                    formato =
                        actual.formato,
                    jugadoresAlineacion =
                        actual.jugadoresAlineacion,
                    confirmada = true,
                    uid = uid
                )
                .onSuccess {

                    _estado.update {
                        it.copy(
                            guardando = false,
                            confirmada = true,
                            mensaje =
                                "Alineación confirmada correctamente"
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

    fun limpiarError() {
        _estado.update {
            it.copy(
                error = null
            )
        }
    }

    private fun verificarEditable(): Boolean {
        val partido =
            _estado.value.partido

        if (
            partido?.estado !=
            EstadoPartido.PROGRAMADO
        ) {
            _estado.update {
                it.copy(
                    error =
                        "La alineación ya no puede modificarse porque el partido comenzó"
                )
            }

            return false
        }

        return true
    }

    private fun detectarJugadorNoDisponible() {
        val actual =
            _estado.value

        val idsDisponibles =
            actual.disponibles
                .map {
                    it.id
                }
                .toSet()

        val titularInvalido =
            actual.jugadoresAlineacion
                .firstOrNull {
                    it.tipo ==
                            TipoJugadorAlineacion.TITULAR &&
                            it.idJugador !in
                            idsDisponibles &&
                            it.idJugador !in
                            idsAdvertenciasIgnoradas
                }

        val jugador =
            titularInvalido?.let {
                    invalido ->

                actual.plantilla
                    .firstOrNull {
                        it.id ==
                                invalido.idJugador
                    }
            }

        _estado.update {
            it.copy(
                jugadorNoDisponible =
                    jugador
            )
        }
    }

    private fun encontrarSlotPreferido(
        jugador: Jugador?,
        slots: List<SlotCampo>,
        ocupados: Set<String>
    ): SlotCampo? {

        val libres =
            slots.filter {
                it.id !in ocupados
            }

        if (libres.isEmpty()) {
            return null
        }

        val prefijo =
            when (
                jugador
                    ?.posicion
                    ?.trim()
                    ?.lowercase()
            ) {

                "portero" ->
                    "ARQ"

                "defensa" ->
                    "DEF"

                "mediocampista" ->
                    "MED"

                "delantero" ->
                    "DEL"

                else ->
                    ""
            }

        if (prefijo.isNotBlank()) {

            libres.firstOrNull {
                it.id.startsWith(
                    prefijo
                )
            }?.let {
                return it
            }
        }

        return libres.first()
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
                "No se pudo completar la operación. Revisa tu conexión e inténtalo nuevamente"
        }
}

fun slotsParaFormato(
    formato: FormatoPartido
): List<SlotCampo> =
    when (formato) {

        FormatoPartido.ONCE_VS_ONCE ->
            listOf(
                SlotCampo(
                    "ARQ",
                    0.50f,
                    0.88f,
                    "Arquero"
                ),

                SlotCampo(
                    "DEF_IZQ",
                    0.18f,
                    0.70f,
                    "Defensa izquierdo"
                ),
                SlotCampo(
                    "DEF_CEN_IZQ",
                    0.38f,
                    0.72f,
                    "Defensa central"
                ),
                SlotCampo(
                    "DEF_CEN_DER",
                    0.62f,
                    0.72f,
                    "Defensa central"
                ),
                SlotCampo(
                    "DEF_DER",
                    0.82f,
                    0.70f,
                    "Defensa derecho"
                ),

                SlotCampo(
                    "MED_IZQ",
                    0.25f,
                    0.48f,
                    "Mediocampista izquierdo"
                ),
                SlotCampo(
                    "MED_CEN",
                    0.50f,
                    0.52f,
                    "Mediocampista central"
                ),
                SlotCampo(
                    "MED_DER",
                    0.75f,
                    0.48f,
                    "Mediocampista derecho"
                ),

                SlotCampo(
                    "DEL_IZQ",
                    0.25f,
                    0.25f,
                    "Delantero izquierdo"
                ),
                SlotCampo(
                    "DEL_CEN",
                    0.50f,
                    0.18f,
                    "Delantero centro"
                ),
                SlotCampo(
                    "DEL_DER",
                    0.75f,
                    0.25f,
                    "Delantero derecho"
                )
            )

        FormatoPartido.OCHO_VS_OCHO ->
            listOf(
                SlotCampo(
                    "ARQ",
                    0.50f,
                    0.88f,
                    "Arquero"
                ),

                SlotCampo(
                    "DEF_IZQ",
                    0.25f,
                    0.70f,
                    "Defensa izquierdo"
                ),
                SlotCampo(
                    "DEF_CEN",
                    0.50f,
                    0.68f,
                    "Defensa central"
                ),
                SlotCampo(
                    "DEF_DER",
                    0.75f,
                    0.70f,
                    "Defensa derecho"
                ),

                SlotCampo(
                    "MED_IZQ",
                    0.32f,
                    0.47f,
                    "Mediocampista izquierdo"
                ),
                SlotCampo(
                    "MED_DER",
                    0.68f,
                    0.47f,
                    "Mediocampista derecho"
                ),

                SlotCampo(
                    "DEL_IZQ",
                    0.35f,
                    0.23f,
                    "Delantero izquierdo"
                ),
                SlotCampo(
                    "DEL_DER",
                    0.65f,
                    0.23f,
                    "Delantero derecho"
                )
            )

        FormatoPartido.SIETE_VS_SIETE ->
            listOf(
                SlotCampo(
                    "ARQ",
                    0.50f,
                    0.88f,
                    "Arquero"
                ),

                SlotCampo(
                    "DEF_IZQ",
                    0.25f,
                    0.70f,
                    "Defensa izquierdo"
                ),
                SlotCampo(
                    "DEF_CEN",
                    0.50f,
                    0.68f,
                    "Defensa central"
                ),
                SlotCampo(
                    "DEF_DER",
                    0.75f,
                    0.70f,
                    "Defensa derecho"
                ),

                SlotCampo(
                    "MED_IZQ",
                    0.35f,
                    0.45f,
                    "Mediocampista izquierdo"
                ),
                SlotCampo(
                    "MED_DER",
                    0.65f,
                    0.45f,
                    "Mediocampista derecho"
                ),

                SlotCampo(
                    "DEL_CEN",
                    0.50f,
                    0.20f,
                    "Delantero centro"
                )
            )

        FormatoPartido.CINCO_VS_CINCO ->
            listOf(
                SlotCampo(
                    "ARQ",
                    0.50f,
                    0.88f,
                    "Arquero"
                ),

                SlotCampo(
                    "DEF_IZQ",
                    0.30f,
                    0.65f,
                    "Defensa izquierdo"
                ),
                SlotCampo(
                    "DEF_DER",
                    0.70f,
                    0.65f,
                    "Defensa derecho"
                ),

                SlotCampo(
                    "MED_CEN",
                    0.50f,
                    0.42f,
                    "Mediocampista central"
                ),

                SlotCampo(
                    "DEL_CEN",
                    0.50f,
                    0.18f,
                    "Delantero centro"
                )
            )

    }