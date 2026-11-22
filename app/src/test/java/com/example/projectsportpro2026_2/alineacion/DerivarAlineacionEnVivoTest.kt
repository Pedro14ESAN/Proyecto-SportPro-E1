package com.example.projectsportpro2026_2.alineacion

import com.example.projectsportpro2026_2.datos.alineacion.calcularSustituciones
import com.example.projectsportpro2026_2.datos.alineacion.opcionesSustitucion
import com.example.projectsportpro2026_2.datos.modelo.alineacion.JugadorAlineacion
import com.example.projectsportpro2026_2.datos.modelo.alineacion.TipoJugadorAlineacion
import com.example.projectsportpro2026_2.datos.modelo.evento.EquipoEvento
import com.example.projectsportpro2026_2.datos.modelo.evento.EstadoEventoPartido
import com.example.projectsportpro2026_2.datos.modelo.evento.EventoPartido
import org.junit.Assert.*
import org.junit.Test

class DerivarAlineacionEnVivoTest {
    private val base = listOf(
        JugadorAlineacion("A", TipoJugadorAlineacion.TITULAR, "DEL_CEN", 1),
        JugadorAlineacion("Z", TipoJugadorAlineacion.TITULAR, "ARQ", 2),
        JugadorAlineacion("B"), JugadorAlineacion("C"), JugadorAlineacion("D")
    )
    private val iniciales = base.filter { it.tipo == TipoJugadorAlineacion.TITULAR }
    private val primero = cambio("1", "A", "B", 30, 100)
    private val segundo = cambio("2", "B", "C", 60, 200)

    private fun cambio(id: String, sale: String, entra: String, minuto: Int, captura: Long) =
        EventoPartido(id = id, codigoTipoEvento = "CAMBIO", equipoEvento = EquipoEvento.PROPIO,
            idJugadorSale = sale, idJugadorEntra = entra, minuto = minuto, fechaCaptura = captura)

    @Test fun procesaOrdenCronologicoConservaPosicionYBase() {
        val resultado = calcularSustituciones(base, listOf(segundo, primero))
        assertEquals(iniciales[0].copy(idJugador = "C"), resultado.titulares[0])
        assertEquals(iniciales[1], resultado.titulares[1])
        assertEquals("A", base[0].idJugador)
        assertEquals(setOf("C", "Z"), resultado.idsSalida)
        assertEquals(setOf("D"), resultado.idsEntrada)
    }

    @Test fun anularUltimoVuelveABYAnularPrimeroReconstruyeBase() {
        val anulado = EstadoEventoPartido.ANULADO
        val ultimoAnulado = calcularSustituciones(base, listOf(primero, segundo.copy(estado = anulado)))
        assertEquals("B", ultimoAnulado.titulares[0].idJugador)
        assertEquals(setOf("C", "D"), ultimoAnulado.idsEntrada)
        val primeroAnulado = calcularSustituciones(base, listOf(primero.copy(estado = anulado), segundo))
        assertEquals(iniciales, primeroAnulado.titulares)
        assertEquals(setOf("B", "C", "D"), primeroAnulado.idsEntrada)
    }

    @Test fun mismoMinutoOrdenaPorCaptura() {
        val resultado = calcularSustituciones(base, listOf(segundo.copy(minuto = 30), primero))
        assertEquals("C", resultado.titulares[0].idJugador)
    }

    @Test fun salienteDebeEstarEnCancha() {
        assertEquals(iniciales, calcularSustituciones(base, listOf(cambio("x", "B", "C", 10, 1))).titulares)
        assertThrows(IllegalArgumentException::class.java) {
            calcularSustituciones(base, emptyList()).validar("B", "C")
        }
    }

    @Test fun entradaDebeSerSuplenteYNoPuedeEstarEnCancha() {
        val estado = calcularSustituciones(base, emptyList())
        assertThrows(IllegalArgumentException::class.java) { estado.validar("A", "Z") }
        assertThrows(IllegalArgumentException::class.java) { estado.validar("A", "desconocido") }
    }

    @Test fun entrantePuedeSalirPeroNuncaReingresar() {
        val trasPrimero = calcularSustituciones(base, listOf(primero))
        assertTrue("B" in trasPrimero.idsSalida)
        assertFalse("B" in trasPrimero.idsEntrada)
        val trasSegundo = calcularSustituciones(base, listOf(primero, segundo))
        for (id in listOf("A", "B", "C")) {
            assertFalse(id in trasSegundo.idsEntrada)
            assertThrows(IllegalArgumentException::class.java) { trasSegundo.validar("C", id) }
        }
        val reingreso = cambio("3", "C", "A", 70, 300)
        assertEquals(trasSegundo, calcularSustituciones(base, listOf(primero, segundo, reingreso)))
    }

    @Test fun rivalesAnuladosIdsVaciosYObservacionesNoAlteranOpciones() {
        val eventos = listOf(
            primero.copy(equipoEvento = EquipoEvento.RIVAL),
            segundo.copy(estado = EstadoEventoPartido.ANULADO),
            cambio("3", "", "", 10, 1).copy(observacion = "A sale B entra")
        )
        assertEquals(calcularSustituciones(base, emptyList()), calcularSustituciones(base, eventos))
    }

    @Test fun ignoraConflictoHastaSerRevisado() {
        val fin = EventoPartido(id = "fin", codigoTipoEvento = "FIN", minuto = 20, fechaCaptura = 50)
        assertEquals(iniciales, calcularSustituciones(base, listOf(fin, primero)).titulares)
        assertEquals("B", calcularSustituciones(base, listOf(fin, primero.copy(conflictoRevisado = true))).titulares[0].idJugador)
    }

    @Test fun correccionRecalculaCadenaYLiberaEntranteAnterior() {
        val corregido = primero.copy(idJugadorEntra = "D", version = 2)
        val resultado = calcularSustituciones(base, listOf(corregido, segundo))
        assertEquals("D", resultado.titulares[0].idJugador)
        assertEquals(setOf("B", "C"), resultado.idsEntrada)
    }

    @Test fun correccionValidaEnElMomentoDelEventoExcluyendoVersionAnterior() {
        val opciones = opcionesSustitucion(base, listOf(primero, segundo), primero.copy(idJugadorEntra = "D"))
        assertEquals(setOf("A", "Z"), opciones.idsSalida)
        assertEquals(setOf("B", "C", "D"), opciones.idsEntrada)
        opciones.validar("A", "D")
        opcionesSustitucion(base, listOf(primero, segundo), segundo).validar("B", "C")
    }

    @Test fun rechazaReutilizarEntranteEnOtraPosicion() {
        val duplicado = cambio("3", "Z", "B", 70, 300)
        assertEquals(calcularSustituciones(base, listOf(primero, segundo)),
            calcularSustituciones(base, listOf(primero, segundo, duplicado)))
    }
}