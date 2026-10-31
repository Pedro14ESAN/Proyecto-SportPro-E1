package com.example.projectsportpro2026_2.sincronizacion

import com.example.projectsportpro2026_2.datos.modelo.evento.EquipoEvento
import com.example.projectsportpro2026_2.datos.modelo.evento.EstadoEventoPartido
import com.example.projectsportpro2026_2.datos.modelo.evento.EventoPartido
import com.example.projectsportpro2026_2.datos.sincronizacion.DetectorConflictos
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/*
 * HU16 - Pruebas del detector de conflictos de sincronización.
 */
class DetectorConflictosTest {

    private fun gol(
        id: String,
        minuto: Int,
        dispositivo: String,
        captura: Long,
        jugador: String = "J1"
    ) = EventoPartido(
        id = id,
        idPartido = "P1",
        codigoTipoEvento = "GOL",
        nombreTipoEvento = "Gol",
        minuto = minuto,
        equipoEvento = EquipoEvento.PROPIO,
        idJugador = jugador,
        afectaMarcador = true,
        creadoPor = "U1",
        fechaCaptura = captura,
        idDispositivo = dispositivo
    )

    @Test
    fun mismoGolEnDosDispositivos_marcaElPosterior() {
        val conflictos = DetectorConflictos.detectar(
            listOf(
                gol("a", 12, "D1", 1_000),
                gol("b", 13, "D2", 2_000)
            )
        )

        assertEquals(setOf("b"), conflictos.keys)
    }

    @Test
    fun mismoDispositivo_noEsConflicto() {
        val conflictos = DetectorConflictos.detectar(
            listOf(
                gol("a", 12, "D1", 1_000),
                gol("b", 12, "D1", 2_000)
            )
        )

        assertTrue(conflictos.isEmpty())
    }

    @Test
    fun minutosLejanos_noEsConflicto() {
        val conflictos = DetectorConflictos.detectar(
            listOf(
                gol("a", 12, "D1", 1_000),
                gol("b", 30, "D2", 2_000)
            )
        )

        assertTrue(conflictos.isEmpty())
    }

    @Test
    fun jugadorDistinto_noEsConflicto() {
        val conflictos = DetectorConflictos.detectar(
            listOf(
                gol("a", 12, "D1", 1_000, jugador = "J1"),
                gol("b", 12, "D2", 2_000, jugador = "J2")
            )
        )

        assertTrue(conflictos.isEmpty())
    }

    @Test
    fun conflictoRevisado_noSeVuelveAMarcar() {
        val conflictos = DetectorConflictos.detectar(
            listOf(
                gol("a", 12, "D1", 1_000),
                gol("b", 12, "D2", 2_000).copy(conflictoRevisado = true)
            )
        )

        assertTrue(conflictos.isEmpty())
    }

    @Test
    fun eventoAnulado_seIgnora() {
        val conflictos = DetectorConflictos.detectar(
            listOf(
                gol("a", 12, "D1", 1_000)
                    .copy(estado = EstadoEventoPartido.ANULADO),
                gol("b", 12, "D2", 2_000)
            )
        )

        assertTrue(conflictos.isEmpty())
    }

    @Test
    fun eventoDespuesDelFin_requiereRevision() {
        val fin = EventoPartido(
            id = "fin",
            codigoTipoEvento = "FIN",
            nombreTipoEvento = "Fin",
            minuto = 90,
            fechaCaptura = 5_000,
            idDispositivo = "D1"
        )

        val conflictos = DetectorConflictos.detectar(
            listOf(
                gol("a", 40, "D2", 1_000),
                fin,
                gol("tarde", 88, "D2", 6_000)
            )
        )

        assertEquals(setOf("tarde"), conflictos.keys)
    }
}
