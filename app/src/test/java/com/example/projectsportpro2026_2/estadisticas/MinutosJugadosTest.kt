package com.example.projectsportpro2026_2.estadisticas

import com.example.projectsportpro2026_2.datos.estadisticas.CalculadoraEstadisticas
import com.example.projectsportpro2026_2.datos.estadisticas.DatosFuenteEquipo
import com.example.projectsportpro2026_2.datos.estadisticas.PeriodoEstadisticas
import com.example.projectsportpro2026_2.datos.modelo.evento.EquipoEvento
import com.example.projectsportpro2026_2.datos.modelo.evento.EstadoEventoPartido
import com.example.projectsportpro2026_2.datos.modelo.evento.EventoPartido
import com.example.projectsportpro2026_2.datos.modelo.partido.EstadoPartido
import com.example.projectsportpro2026_2.datos.modelo.partido.Partido
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/*
 * Rediseño "Línea de cal" · 7.3: minutos jugados, goles por fecha y último partido.
 * Titular desde el minuto 1 o desde que entra por un cambio, hasta que sale por un cambio
 * o hasta el FIN. Sin FIN: hasta el último evento válido (D-12). Sin alineación confirmada: null.
 */
class MinutosJugadosTest {

    private fun fin(minuto: Int) = EventoPartido(id = "fin", codigoTipoEvento = "FIN", minuto = minuto)

    private fun cambio(
        id: String,
        minuto: Int,
        sale: String,
        entra: String,
        equipo: EquipoEvento = EquipoEvento.PROPIO,
        estado: EstadoEventoPartido = EstadoEventoPartido.ACTIVO,
        captura: Long = minuto.toLong()
    ) = EventoPartido(
        id = id,
        codigoTipoEvento = "CAMBIO",
        minuto = minuto,
        equipoEvento = equipo,
        idJugadorSale = sale,
        idJugadorEntra = entra,
        estado = estado,
        fechaCaptura = captura
    )

    private fun gol(id: String, minuto: Int, jugador: String, equipo: EquipoEvento = EquipoEvento.PROPIO) =
        EventoPartido(
            id = id,
            codigoTipoEvento = "GOL",
            minuto = minuto,
            equipoEvento = equipo,
            idJugador = jugador,
            afectaMarcador = true
        )

    private val titulares = setOf("A", "B")

    @Test fun titularJuegaHastaElFin() {
        assertEquals(90, CalculadoraEstadisticas.minutosJugados("A", listOf(fin(90)), titulares))
    }

    @Test fun entraPorCambioJuegaDesdeElCambioHastaElFin() {
        val eventos = listOf(cambio("c1", 60, sale = "B", entra = "C"), fin(90))
        assertEquals(30, CalculadoraEstadisticas.minutosJugados("C", eventos, titulares))
    }

    @Test fun titularQueSalePorCambioJuegaHastaElCambio() {
        val eventos = listOf(cambio("c1", 70, sale = "A", entra = "C"), fin(90))
        assertEquals(70, CalculadoraEstadisticas.minutosJugados("A", eventos, titulares))
    }

    @Test fun sinFinCuentaHastaElUltimoEventoValido() {
        val eventos = listOf(gol("g1", 40, "A"), cambio("c1", 85, sale = "B", entra = "C"))
        assertEquals(85, CalculadoraEstadisticas.minutosJugados("A", eventos, titulares))
        // C entra en el 85 y el último evento es el 85: 0 minutos
        assertEquals(0, CalculadoraEstadisticas.minutosJugados("C", eventos, titulares))
    }

    @Test fun sinFinNiEventosElTitularSumaCero() {
        assertEquals(0, CalculadoraEstadisticas.minutosJugados("A", emptyList(), titulares))
    }

    @Test fun sinAlineacionConfirmadaEsNull() {
        assertNull(CalculadoraEstadisticas.minutosJugados("A", listOf(fin(90)), null))
    }

    @Test fun suplenteQueNoEntraSumaCero() {
        assertEquals(0, CalculadoraEstadisticas.minutosJugados("Z", listOf(fin(90)), titulares))
    }

    @Test fun ignoraCambiosAnuladosYDelRival() {
        val eventos = listOf(
            cambio("c1", 30, sale = "A", entra = "C", estado = EstadoEventoPartido.ANULADO),
            cambio("c2", 50, sale = "A", entra = "C", equipo = EquipoEvento.RIVAL),
            fin(90)
        )
        assertEquals(90, CalculadoraEstadisticas.minutosJugados("A", eventos, titulares))
        assertEquals(0, CalculadoraEstadisticas.minutosJugados("C", eventos, titulares))
    }

    @Test fun entraYVuelveASalirSumaSoloElTramoEnCancha() {
        val eventos = listOf(
            cambio("c1", 20, sale = "B", entra = "C"),
            cambio("c2", 65, sale = "C", entra = "D"),
            fin(90)
        )
        assertEquals(45, CalculadoraEstadisticas.minutosJugados("C", eventos, titulares))
        assertEquals(20, CalculadoraEstadisticas.minutosJugados("B", eventos, titulares))
        assertEquals(25, CalculadoraEstadisticas.minutosJugados("D", eventos, titulares))
    }

    @Test fun partidosDelJugadorDaGolesPorFechaYUltimoPartido() {
        val fuente = DatosFuenteEquipo(
            partidos = listOf(
                Partido(id = "p2", fecha = "2026-10-20", estado = EstadoPartido.FINALIZADO),
                Partido(id = "p1", fecha = "2026-10-10", estado = EstadoPartido.FINALIZADO),
                Partido(id = "p3", fecha = "2026-10-30", estado = EstadoPartido.PROGRAMADO)
            ),
            eventosPorPartido = mapOf(
                "p1" to listOf(gol("g1", 10, "A"), gol("g2", 50, "A"), gol("g3", 70, "X", EquipoEvento.RIVAL), fin(90)),
                "p2" to listOf(gol("g4", 30, "B"), fin(90))
            ),
            // p2 no tiene alineación confirmada
            titularesPorPartido = mapOf("p1" to setOf("A"))
        )

        val partidos = CalculadoraEstadisticas.partidosDelJugador("A", fuente, PeriodoEstadisticas.TODO)

        // Solo finalizados, del más antiguo al más reciente
        assertEquals(listOf("p1", "p2"), partidos.map { it.partido.id })
        assertEquals(2, partidos[0].goles)
        assertEquals(2, partidos[0].golesPropios)
        assertEquals(1, partidos[0].golesRival)
        assertEquals(90, partidos[0].minutos)
        assertEquals(true, partidos[0].titular)
        // Último partido: sin goles de A y sin alineación confirmada → minutos "—"
        assertEquals(0, partidos[1].goles)
        assertNull(partidos[1].minutos)
        assertNull(partidos[1].titular)
    }

    @Test fun calcularJugadorSumaSoloLosMinutosConAlineacion() {
        val fuente = DatosFuenteEquipo(
            partidos = listOf(
                Partido(id = "p1", fecha = "2026-10-10", estado = EstadoPartido.FINALIZADO),
                Partido(id = "p2", fecha = "2026-10-20", estado = EstadoPartido.FINALIZADO)
            ),
            eventosPorPartido = mapOf("p1" to listOf(fin(90)), "p2" to listOf(fin(90))),
            titularesPorPartido = mapOf("p1" to setOf("A"))
        )
        val r = CalculadoraEstadisticas.calcularJugador("A", fuente, PeriodoEstadisticas.TODO)
        assertEquals(90, r.minutosJugados)
        assertEquals(2, r.partidos.size)

        val sinAlineacion = CalculadoraEstadisticas.calcularJugador(
            "A", fuente.copy(titularesPorPartido = emptyMap()), PeriodoEstadisticas.TODO
        )
        assertNull(sinAlineacion.minutosJugados)
    }
}
