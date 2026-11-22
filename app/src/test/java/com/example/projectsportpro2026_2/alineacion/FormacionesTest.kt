package com.example.projectsportpro2026_2.alineacion

import com.example.projectsportpro2026_2.datos.alineacion.*
import com.example.projectsportpro2026_2.datos.modelo.Jugador
import com.example.projectsportpro2026_2.datos.modelo.alineacion.*
import com.example.projectsportpro2026_2.datos.modelo.evento.*
import org.junit.Assert.*
import org.junit.Test

class FormacionesTest {
    @Test fun todasLasFormacionesTienenElNumeroExactoDePuestosUnicosYUnArquero() {
        FormatoPartido.entries.forEach { formato ->
            formacionesParaFormato(formato).forEach { nombre ->
                val slots = posicionesFormacion(formato, nombre)
                assertEquals(formato.maximoTitulares, slots.size)
                assertEquals(slots.size, slots.map { it.id }.distinct().size)
                assertEquals(1, slots.count { it.id == "ARQ" })
                assertTrue(slots.all { it.x in 0f..1f && it.y in 0f..1f })
                assertEquals(slots.maxOf { it.y }, slots.first().y)
            }
        }
    }

    @Test fun formatoSinMetadatosConservaLasPosicionesHistoricas() {
        val original = posicionesFormacion(FormatoPartido.ONCE_VS_ONCE)
        assertEquals("DEF_IZQ", original[1].id)
        assertEquals(.18f, original[1].x)
        assertEquals(.88f, original.first().y)
        assertEquals(original, posicionesFormacion(FormatoPartido.ONCE_VS_ONCE, "desconocida"))
        assertNull(JugadorAlineacion().xCampo)
        assertEquals("", Alineacion().formacion)
    }

    @Test fun redistribuirConservaArqueroJugadoresSuplentesYPerfil() {
        val jugadores = posicionesFormacion(FormatoPartido.ONCE_VS_ONCE).mapIndexed { index, slot ->
            JugadorAlineacion("$index", TipoJugadorAlineacion.TITULAR, slot.id, index, .3, .4)
        } + JugadorAlineacion("suplente")
        val plantilla = listOf(Jugador(id = "0", posicion = "portero"))
        formacionesParaFormato(FormatoPartido.ONCE_VS_ONCE).forEach {
            val nuevos = distribuirFormacion(jugadores, plantilla, FormatoPartido.ONCE_VS_ONCE, it)
            assertEquals("ARQ", nuevos.first().posicionCampo)
            assertEquals(jugadores.map { it.idJugador }, nuevos.map { it.idJugador })
            assertEquals(jugadores.last(), nuevos.last())
            assertEquals(11, nuevos.dropLast(1).map { it.posicionCampo }.distinct().size)
            assertTrue(nuevos.dropLast(1).all { it.xCampo == null && it.yCampo == null })
        }
        assertEquals("portero", plantilla.first().posicion)
        assertEquals(.3, jugadores.first().xCampo)
    }

    @Test fun alineacionIncompletaNoInventaTitularesYRespetaArqueroDePerfil() {
        val jugadores = listOf(JugadorAlineacion("A", TipoJugadorAlineacion.TITULAR, "DEF_IZQ"))
        val resultado = distribuirFormacion(jugadores, listOf(Jugador(id = "A", posicion = "portero")),
            FormatoPartido.CINCO_VS_CINCO, "1-2-1")
        assertEquals(1, resultado.size)
        assertEquals("ARQ", resultado.single().posicionCampo)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rechazaFormacionDeOtroFormato() {
        distribuirFormacion(emptyList(), emptyList(), FormatoPartido.CINCO_VS_CINCO, "4-4-2")
    }

    @Test fun coordenadasRechazanParesIncompletosInfinitosYFueraDeRango() {
        assertTrue(coordenadasValidas(JugadorAlineacion()))
        assertTrue(coordenadasValidas(JugadorAlineacion(xCampo = .25, yCampo = .75)))
        assertFalse(coordenadasValidas(JugadorAlineacion(xCampo = .25)))
        assertFalse(coordenadasValidas(JugadorAlineacion(xCampo = Double.NaN, yCampo = .75)))
        assertFalse(coordenadasValidas(JugadorAlineacion(xCampo = .25, yCampo = 1.1)))
    }

    @Test fun sustitucionesYAnulacionesHeredanCoordenadasSinModificarInicial() {
        val inicial = listOf(JugadorAlineacion("A", TipoJugadorAlineacion.TITULAR, "ARQ", 0, .3, .7),
            JugadorAlineacion("B"), JugadorAlineacion("C"))
        val ab = EventoPartido(id = "ab", codigoTipoEvento = "CAMBIO", equipoEvento = EquipoEvento.PROPIO,
            idJugadorSale = "A", idJugadorEntra = "B", minuto = 10)
        val bc = ab.copy(id = "bc", idJugadorSale = "B", idJugadorEntra = "C", minuto = 20)
        val c = calcularSustituciones(inicial, listOf(ab, bc)).titulares.single()
        assertEquals("C", c.idJugador)
        assertEquals(.3, c.xCampo)
        assertEquals(.7, c.yCampo)
        assertEquals("ARQ", c.posicionCampo)
        val b = calcularSustituciones(inicial, listOf(ab, bc.copy(estado = EstadoEventoPartido.ANULADO))).titulares.single()
        assertEquals(inicial.first().copy(idJugador = "B"), b)
        assertEquals(inicial.first(), calcularSustituciones(inicial, listOf(ab.copy(estado = EstadoEventoPartido.ANULADO), bc)).titulares.single())
    }
}