package com.example.projectsportpro2026_2.datos.alineacion

import com.example.projectsportpro2026_2.datos.modelo.alineacion.JugadorAlineacion
import com.example.projectsportpro2026_2.datos.modelo.alineacion.TipoJugadorAlineacion
import com.example.projectsportpro2026_2.datos.modelo.evento.EquipoEvento
import com.example.projectsportpro2026_2.datos.modelo.evento.EstadoEventoPartido
import com.example.projectsportpro2026_2.datos.modelo.evento.EventoPartido
import com.example.projectsportpro2026_2.datos.sincronizacion.DetectorConflictos

data class EstadoSustituciones(
    val titulares: List<JugadorAlineacion>,
    val idsEntrada: Set<String>,
    val idsEntraron: Set<String>,
    val idsSalieron: Set<String>
) {
    val idsSalida: Set<String> get() = titulares.map { it.idJugador }.toSet()

    fun validar(sale: String, entra: String) {
        require(sale in idsSalida) { "El jugador que sale debe estar actualmente en cancha" }
        require(entra in idsEntrada) {
            "El jugador que entra debe ser un suplente disponible que todavía no haya participado. No se permiten reingresos"
        }
    }
}

private val ordenCambios = compareBy<EventoPartido> { it.minuto }
    .thenBy { it.fechaCaptura }.thenBy { it.id }

/** Solo reproduce cambios aplicables. No escribe en la alineación histórica. */
fun calcularSustituciones(
    alineacionInicial: List<JugadorAlineacion>,
    eventos: List<EventoPartido>
): EstadoSustituciones {
    val conflictos = DetectorConflictos.detectar(eventos)
    val actuales = alineacionInicial.filter { it.tipo == TipoJugadorAlineacion.TITULAR }.toMutableList()
    val disponibles = alineacionInicial.filter { it.tipo == TipoJugadorAlineacion.SUPLENTE }
        .map { it.idJugador }.filter { it.isNotBlank() }.toMutableSet()
    disponibles.removeAll(actuales.map { it.idJugador }.toSet())
    val entraron = mutableSetOf<String>()
    val salieron = mutableSetOf<String>()

    eventos.filter {
        it.codigoTipoEvento == "CAMBIO" && it.estado == EstadoEventoPartido.ACTIVO &&
            it.equipoEvento == EquipoEvento.PROPIO && it.id !in conflictos
    }.sortedWith(ordenCambios).forEach { cambio ->
        val indice = actuales.indexOfFirst { it.idJugador == cambio.idJugadorSale }
        if (indice >= 0 && cambio.idJugadorEntra in disponibles &&
            cambio.idJugadorEntra !in entraron && cambio.idJugadorEntra !in salieron) {
            actuales[indice] = actuales[indice].copy(idJugador = cambio.idJugadorEntra)
            disponibles.remove(cambio.idJugadorEntra)
            entraron.add(cambio.idJugadorEntra)
            salieron.add(cambio.idJugadorSale)
        }
    }
    return EstadoSustituciones(actuales.toList(), disponibles.toSet(), entraron.toSet(), salieron.toSet())
}

/** Estado inmediatamente anterior al evento que se crea o corrige, según el orden futbolístico. */
fun opcionesSustitucion(
    alineacionInicial: List<JugadorAlineacion>,
    eventos: List<EventoPartido>,
    candidato: EventoPartido
): EstadoSustituciones = calcularSustituciones(
    alineacionInicial,
    eventos.filter { it.id != candidato.id && ordenCambios.compare(it, candidato) < 0 }
)
