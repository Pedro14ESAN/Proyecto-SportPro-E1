package com.example.projectsportpro2026_2.partido

import com.example.projectsportpro2026_2.datos.partido.eventosValidos
import com.example.projectsportpro2026_2.datos.modelo.evento.*
import org.junit.Assert.assertEquals
import org.junit.Test

class EventosValidosTest {
    @Test fun excluyeAnuladosYDuplicadosPendientesPeroAdmiteResolucion() {
        val gol = EventoPartido(id = "a", codigoTipoEvento = "GOL", idJugador = "J",
            equipoEvento = EquipoEvento.PROPIO, afectaMarcador = true, minuto = 10,
            fechaCaptura = 100, idDispositivo = "uno")
        val duplicado = gol.copy(id = "b", fechaCaptura = 200, idDispositivo = "dos")
        val anulado = gol.copy(id = "c", estado = EstadoEventoPartido.ANULADO)
        assertEquals(listOf(gol), eventosValidos(listOf(gol, duplicado, anulado)))
        assertEquals(2, eventosValidos(listOf(gol, duplicado.copy(conflictoRevisado = true), anulado)).size)
        assertEquals(listOf(duplicado), eventosValidos(listOf(gol.copy(estado = EstadoEventoPartido.ANULADO), duplicado)))
    }
}