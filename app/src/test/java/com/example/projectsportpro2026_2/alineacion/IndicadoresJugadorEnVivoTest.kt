package com.example.projectsportpro2026_2.alineacion

import com.example.projectsportpro2026_2.datos.alineacion.IndicadoresJugadorEnVivo
import com.example.projectsportpro2026_2.datos.alineacion.calcularIndicadoresJugadores
import com.example.projectsportpro2026_2.datos.modelo.evento.EquipoEvento
import com.example.projectsportpro2026_2.datos.modelo.evento.EstadoEventoPartido
import com.example.projectsportpro2026_2.datos.modelo.evento.EventoPartido
import com.example.projectsportpro2026_2.datos.modelo.evento.TipoTarjeta
import org.junit.Assert.*
import org.junit.Test

class IndicadoresJugadorEnVivoTest {
    private fun gol(id: String, jugador: String = "A") = EventoPartido(
        id = id, codigoTipoEvento = "GOL", idJugador = jugador,
        equipoEvento = EquipoEvento.PROPIO, afectaMarcador = true
    )
    private fun tarjeta(id: String, tipo: TipoTarjeta) = EventoPartido(
        id = id, codigoTipoEvento = "TARJETA", idJugador = "A",
        equipoEvento = EquipoEvento.PROPIO, tipoTarjeta = tipo
    )

    @Test fun acumulaGolesYDistingueAmbasTarjetas() {
        val eventos = listOf(gol("1"), gol("2"),
            tarjeta("3", TipoTarjeta.AMARILLA), tarjeta("4", TipoTarjeta.ROJA))
        assertEquals(mapOf("A" to IndicadoresJugadorEnVivo(2, true, true)),
            calcularIndicadoresJugadores(eventos))
    }

    @Test fun ignoraRivalAnuladosSinEquipoYSinId() {
        val eventos = listOf(
            gol("1").copy(equipoEvento = EquipoEvento.RIVAL),
            gol("2").copy(estado = EstadoEventoPartido.ANULADO),
            gol("3").copy(equipoEvento = EquipoEvento.NINGUNO),
            gol("4", ""),
            tarjeta("5", TipoTarjeta.NINGUNA).copy(observacion = "Tarjeta roja")
        )
        assertTrue(calcularIndicadoresJugadores(eventos).isEmpty())
    }

    @Test fun correccionDeJugadorTransfiereLosIndicadoresPorId() {
        val evento = gol("1")
        assertEquals(setOf("A"), calcularIndicadoresJugadores(listOf(evento)).keys)
        assertEquals(setOf("B"), calcularIndicadoresJugadores(listOf(
            evento.copy(idJugador = "B", version = 2)
        )).keys)
    }

    @Test fun correccionDeTarjetaYAnulacionReconstruyenElResultado() {
        val evento = tarjeta("1", TipoTarjeta.AMARILLA)
        assertEquals(IndicadoresJugadorEnVivo(0, true, false),
            calcularIndicadoresJugadores(listOf(evento))["A"])
        val corregido = evento.copy(tipoTarjeta = TipoTarjeta.ROJA, version = 2)
        assertEquals(IndicadoresJugadorEnVivo(0, false, true),
            calcularIndicadoresJugadores(listOf(corregido))["A"])
        assertTrue(calcularIndicadoresJugadores(listOf(
            corregido.copy(estado = EstadoEventoPartido.ANULADO)
        )).isEmpty())
    }

    @Test fun golesUsanLaReglaPersistidaDelMarcador() {
        val eventos = listOf(
            gol("1").copy(codigoTipoEvento = "PENAL"),
            gol("2").copy(afectaMarcador = false),
            tarjeta("3", TipoTarjeta.NINGUNA)
        )
        assertEquals(IndicadoresJugadorEnVivo(1, false, false),
            calcularIndicadoresJugadores(eventos)["A"])
    }

    @Test fun conflictosQuedanExcluidosHastaSerRevisados() {
        val original = gol("1").copy(fechaCaptura = 100, idDispositivo = "D1", minuto = 10)
        val duplicado = gol("2").copy(fechaCaptura = 200, idDispositivo = "D2", minuto = 10)
        assertEquals(1, calcularIndicadoresJugadores(listOf(original, duplicado))["A"]?.goles)
        assertEquals(2, calcularIndicadoresJugadores(listOf(
            original, duplicado.copy(conflictoRevisado = true)
        ))["A"]?.goles)
    }
}