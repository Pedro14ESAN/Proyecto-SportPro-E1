package com.example.projectsportpro2026_2.estadisticas

import com.example.projectsportpro2026_2.datos.estadisticas.CalculadoraEstadisticas
import com.example.projectsportpro2026_2.datos.estadisticas.DatosFuenteEquipo
import com.example.projectsportpro2026_2.datos.estadisticas.PeriodoEstadisticas
import com.example.projectsportpro2026_2.datos.modelo.Asistencia
import com.example.projectsportpro2026_2.datos.modelo.Entrenamiento
import com.example.projectsportpro2026_2.datos.modelo.EstadoAsistencia
import com.example.projectsportpro2026_2.datos.modelo.evento.EquipoEvento
import com.example.projectsportpro2026_2.datos.modelo.evento.EstadoEventoPartido
import com.example.projectsportpro2026_2.datos.modelo.evento.EventoPartido
import com.example.projectsportpro2026_2.datos.modelo.partido.EstadoPartido
import com.example.projectsportpro2026_2.datos.modelo.partido.Partido
import com.example.projectsportpro2026_2.datos.resumen.FuenteResumen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

// HU18 / HU26: cálculos con eventos válidos (sin Firebase)
class CalculadoraEstadisticasTest {

    private val hoy = LocalDate.of(2026, 11, 6)

    private fun evento(
        id: String,
        codigo: String,
        equipo: EquipoEvento = EquipoEvento.PROPIO,
        jugador: String = "",
        estado: EstadoEventoPartido = EstadoEventoPartido.ACTIVO,
        version: Int = 1,
        observacion: String = ""
    ) = EventoPartido(
        id = id,
        idPartido = "p1",
        codigoTipoEvento = codigo,
        nombreTipoEvento = codigo,
        equipoEvento = equipo,
        idJugador = jugador,
        afectaMarcador = codigo == "GOL",
        estado = estado,
        version = version,
        observacion = observacion
    )

    private val fuente = DatosFuenteEquipo(
        partidos = listOf(
            Partido(id = "p1", fecha = "2026-10-10", estado = EstadoPartido.FINALIZADO),
            Partido(id = "p2", fecha = "2026-10-20", estado = EstadoPartido.PROGRAMADO)
        ),
        eventosPorPartido = mapOf(
            "p1" to listOf(
                evento("e1", "GOL", jugador = "j1"),
                evento("e2", "GOL", jugador = "j1", estado = EstadoEventoPartido.ANULADO),
                evento("e3", "GOL", equipo = EquipoEvento.RIVAL),
                evento("e4", "TARJETA", jugador = "j1", observacion = "Amarilla"),
                evento("e5", "TARJETA", jugador = "j2", observacion = "Roja directa"),
                evento("e6", "FALTA", jugador = "j2")
            )
        ),
        alineadosPorPartido = mapOf("p1" to setOf("j1", "j2", "j3")),
        entrenamientos = listOf(
            Entrenamiento(id = "s1", fecha = "2026-10-05"),
            Entrenamiento(id = "s2", fecha = "2026-12-01")  // futura: no cuenta
        ),
        asistencias = listOf(
            Asistencia(idEntrenamiento = "s1", idJugador = "j1", estado = EstadoAsistencia.PRESENTE),
            Asistencia(idEntrenamiento = "s1", idJugador = "j2", estado = EstadoAsistencia.AUSENTE)
        )
    )

    @Test
    fun equipo_soloPartidosFinalizadosYEventosValidos() {
        val r = CalculadoraEstadisticas.calcularEquipo(fuente, PeriodoEstadisticas.TODO, hoy)
        assertEquals(1, r.partidosJugados)
        assertEquals(1, r.golesFavor)          // el gol anulado no cuenta
        assertEquals(1, r.golesContra)
        assertEquals(1, r.empatados)
        assertEquals(1, r.tarjetasAmarillas)
        assertEquals(1, r.tarjetasRojas)
        assertEquals(1, r.faltas)
        assertEquals(1, r.sesionesEntrenamiento)
        assertEquals(1, r.eventosAnuladosExcluidos)
        assertEquals(50, r.porcentajeAsistencia)
    }

    @Test
    fun jugador_participacionesGolesYTarjetas() {
        val j1 = CalculadoraEstadisticas.calcularJugador("j1", fuente, PeriodoEstadisticas.TODO, hoy)
        assertEquals(1, j1.participaciones)
        assertEquals(1, j1.goles)
        assertEquals(1, j1.tarjetasAmarillas)
        assertEquals(0, j1.tarjetasRojas)
        assertEquals(100, j1.asistencia.porcentaje)

        val j3 = CalculadoraEstadisticas.calcularJugador("j3", fuente, PeriodoEstadisticas.TODO, hoy)
        assertEquals(1, j3.participaciones)   // estuvo en la alineación
        assertEquals(0, j3.goles)
        assertNull(j3.asistencia.porcentaje)  // "Sin datos", no 0 %
    }

    @Test
    fun periodo_filtraPorFecha() {
        val septiembre = PeriodoEstadisticas(
            "sep", "Sep", LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30)
        )
        val r = CalculadoraEstadisticas.calcularEquipo(fuente, septiembre, hoy)
        assertEquals(0, r.partidosJugados)
        assertFalse(r.hayPartidos)
    }

    @Test
    fun resumen_huellaIgualALaCloudFunction() {
        // Mismo caso que functions/test: "a:2|b:1|d:1"
        val eventos = listOf(
            evento("b", "INICIO"),
            evento("a", "GOL", version = 2),
            evento("c", "GOL", estado = EstadoEventoPartido.ANULADO, version = 2),
            evento("d", "TARJETA")
        )
        assertEquals("b5ae884eccfc70e4", FuenteResumen.huella(eventos))
        assertTrue(FuenteResumen.suficientes(eventos))
    }

    @Test
    fun resumen_insuficientesSinEventosDeJuego() {
        val eventos = listOf(evento("i", "INICIO"), evento("f", "FIN"), evento("g", "GOL"))
        assertFalse(FuenteResumen.suficientes(eventos))
    }
}
