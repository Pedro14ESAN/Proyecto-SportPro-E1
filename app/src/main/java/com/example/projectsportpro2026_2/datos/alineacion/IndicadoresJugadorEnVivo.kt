package com.example.projectsportpro2026_2.datos.alineacion

import com.example.projectsportpro2026_2.datos.modelo.evento.EquipoEvento
import com.example.projectsportpro2026_2.datos.modelo.evento.EstadoEventoPartido
import com.example.projectsportpro2026_2.datos.modelo.evento.EventoPartido
import com.example.projectsportpro2026_2.datos.modelo.evento.TipoTarjeta
import com.example.projectsportpro2026_2.datos.sincronizacion.DetectorConflictos

data class IndicadoresJugadorEnVivo(
    val goles: Int,
    val amarilla: Boolean,
    val roja: Boolean
)

fun calcularIndicadoresJugadores(eventos: List<EventoPartido>): Map<String, IndicadoresJugadorEnVivo> {
    val conflictos = DetectorConflictos.detectar(eventos)
    return eventos.filter {
        it.estado == EstadoEventoPartido.ACTIVO &&
            it.equipoEvento == EquipoEvento.PROPIO &&
            it.idJugador.isNotBlank() &&
            it.id !in conflictos &&
            (it.afectaMarcador ||
                (it.codigoTipoEvento == "TARJETA" && it.tipoTarjeta != TipoTarjeta.NINGUNA))
    }.groupBy { it.idJugador }.mapValues { (_, delJugador) ->
        IndicadoresJugadorEnVivo(
            goles = delJugador.count { it.afectaMarcador },
            amarilla = delJugador.any {
                it.codigoTipoEvento == "TARJETA" && it.tipoTarjeta == TipoTarjeta.AMARILLA
            },
            roja = delJugador.any {
                it.codigoTipoEvento == "TARJETA" && it.tipoTarjeta == TipoTarjeta.ROJA
            }
        )
    }
}
