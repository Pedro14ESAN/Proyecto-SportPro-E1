package com.example.projectsportpro2026_2.inicio

import com.example.projectsportpro2026_2.datos.modelo.Asistencia
import com.example.projectsportpro2026_2.datos.modelo.Entrenamiento
import com.example.projectsportpro2026_2.datos.modelo.EstadoAsistencia
import com.example.projectsportpro2026_2.datos.modelo.EstadoCuenta
import com.example.projectsportpro2026_2.datos.modelo.EstadoEntrenamiento
import com.example.projectsportpro2026_2.datos.modelo.EstadoMensualidad
import com.example.projectsportpro2026_2.datos.modelo.EstadoVinculacion
import com.example.projectsportpro2026_2.datos.modelo.Jugador
import com.example.projectsportpro2026_2.datos.modelo.Mensualidad
import com.example.projectsportpro2026_2.datos.modelo.Notificacion
import com.example.projectsportpro2026_2.datos.modelo.PerfilUsuario
import com.example.projectsportpro2026_2.datos.modelo.Vinculacion
import com.example.projectsportpro2026_2.datos.modelo.convocatoria.EstadoDisponibilidad
import com.example.projectsportpro2026_2.datos.modelo.convocatoria.JugadorConvocado
import com.example.projectsportpro2026_2.datos.modelo.partido.EstadoPartido
import com.example.projectsportpro2026_2.datos.modelo.partido.Partido
import com.example.projectsportpro2026_2.datos.repositorio.CuentaPorVincular
import com.example.projectsportpro2026_2.datos.repositorio.convocatoria.ConvocatoriaParaResponder
import com.example.projectsportpro2026_2.ui.inicio.LogicaInicio
import com.example.projectsportpro2026_2.ui.inicio.Respuestas
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime

/*
 * Rediseño "Línea de cal" · 7.1: contadores y selecciones del inicio.
 * Lógica pura: no usa Firestore ni Android.
 */
class LogicaInicioTest {

    // ---------- Campana ----------

    @Test fun noLeidasCuentaSoloLasNotificacionesSinLeer() {
        val lista = listOf(
            Notificacion(id = "1", leida = false),
            Notificacion(id = "2", leida = true),
            Notificacion(id = "3", leida = false)
        )
        assertEquals(2, LogicaInicio.noLeidas(lista))
    }

    @Test fun noLeidasEsCeroSinNotificaciones() {
        assertEquals(0, LogicaInicio.noLeidas(emptyList()))
    }

    // ---------- ADM · Requiere tu atención ----------

    @Test fun cuentasPorAprobarCuentaSoloLasPendientes() {
        // D-10: pendiente == true (perfil en estado PENDIENTE), igual que la pantalla Cuentas de jugadores
        val lista = listOf(
            cuenta(EstadoCuenta.PENDIENTE),
            cuenta(EstadoCuenta.ACTIVA),
            cuenta(EstadoCuenta.PENDIENTE)
        )
        assertEquals(2, LogicaInicio.cuentasPorAprobar(lista))
    }

    @Test fun vinculacionesPendientesIgnoraActivasEInactivas() {
        val lista = listOf(
            Vinculacion(id = "1", estado = EstadoVinculacion.PENDIENTE),
            Vinculacion(id = "2", estado = EstadoVinculacion.ACTIVA),
            Vinculacion(id = "3", estado = EstadoVinculacion.INACTIVA),
            Vinculacion(id = "4", estado = EstadoVinculacion.PENDIENTE)
        )
        assertEquals(2, LogicaInicio.vinculacionesPendientes(lista))
    }

    @Test fun mensualidadesVencidasCuentaSoloVencido() {
        val lista = listOf(
            Mensualidad(id = "1", estado = EstadoMensualidad.VENCIDO),
            Mensualidad(id = "2", estado = EstadoMensualidad.PENDIENTE),
            Mensualidad(id = "3", estado = EstadoMensualidad.PAGADO),
            Mensualidad(id = "4", estado = EstadoMensualidad.VENCIDO)
        )
        assertEquals(2, LogicaInicio.mensualidadesVencidas(lista))
    }

    // ---------- DT · respondieron x/y ----------

    @Test fun respuestasConvocatoriaCuentaLasDistintasDePendiente() {
        val convocados = listOf(
            JugadorConvocado("a", EstadoDisponibilidad.DISPONIBLE),
            JugadorConvocado("b", EstadoDisponibilidad.NO_DISPONIBLE),
            JugadorConvocado("c", EstadoDisponibilidad.PENDIENTE),
            JugadorConvocado("d", EstadoDisponibilidad.PENDIENTE)
        )
        assertEquals(Respuestas(respondieron = 2, total = 4), LogicaInicio.respuestasConvocatoria(convocados))
    }

    @Test fun respuestasConvocatoriaSinConvocadosEsCeroDeCero() {
        assertEquals(Respuestas(0, 0), LogicaInicio.respuestasConvocatoria(emptyList()))
    }

    // ---------- JUG / PAD · convocatorias por responder ----------

    @Test fun convocatoriasPorResponderSoloPendientesDePartidosProgramados() {
        val lista = listOf(
            convocatoria("p1", EstadoDisponibilidad.PENDIENTE, EstadoPartido.PROGRAMADO),
            convocatoria("p2", EstadoDisponibilidad.DISPONIBLE, EstadoPartido.PROGRAMADO),
            // El partido ya comenzó: la convocatoria es histórica y no se puede responder
            convocatoria("p3", EstadoDisponibilidad.PENDIENTE, EstadoPartido.EN_CURSO),
            convocatoria("p4", EstadoDisponibilidad.PENDIENTE, EstadoPartido.PROGRAMADO)
        )
        assertEquals(2, LogicaInicio.convocatoriasPorResponder(lista))
    }

    @Test fun convocatoriaPorResponderEligeElPartidoMasCercano() {
        val lista = listOf(
            convocatoria("tarde", EstadoDisponibilidad.PENDIENTE, EstadoPartido.PROGRAMADO, "2026-12-05", "10:00"),
            convocatoria("pronto", EstadoDisponibilidad.PENDIENTE, EstadoPartido.PROGRAMADO, "2026-11-28", "16:00"),
            convocatoria("respondida", EstadoDisponibilidad.DISPONIBLE, EstadoPartido.PROGRAMADO, "2026-11-26", "09:00")
        )
        assertEquals("pronto", LogicaInicio.convocatoriaPorResponder(lista)?.partido?.id)
    }

    @Test fun convocatoriaPorResponderEsNullSiNoHayPendientes() {
        val lista = listOf(convocatoria("p1", EstadoDisponibilidad.DISPONIBLE, EstadoPartido.PROGRAMADO))
        assertNull(LogicaInicio.convocatoriaPorResponder(lista))
    }

    // ---------- JUG · asistencia ----------

    @Test fun porcentajeAsistenciaRedondeaPresentesSobreRegistros() {
        val lista = listOf(
            asistencia(EstadoAsistencia.PRESENTE),
            asistencia(EstadoAsistencia.PRESENTE),
            asistencia(EstadoAsistencia.AUSENTE)
        )
        // 2 de 3 = 66,67 % → 67
        assertEquals(67, LogicaInicio.porcentajeAsistencia(lista))
    }

    @Test fun porcentajeAsistenciaCuentaJustificadosComoRegistro() {
        val lista = listOf(asistencia(EstadoAsistencia.PRESENTE), asistencia(EstadoAsistencia.JUSTIFICADO))
        assertEquals(50, LogicaInicio.porcentajeAsistencia(lista))
    }

    @Test fun porcentajeAsistenciaEsNullSinRegistros() {
        assertNull(LogicaInicio.porcentajeAsistencia(emptyList()))
    }

    // ---------- Mensualidad del periodo ----------

    @Test fun estadoMensualidadTomaLaDelJugadorYPeriodo() {
        val lista = listOf(
            Mensualidad(id = "1", idJugador = "j1", periodo = "2026-10", estado = EstadoMensualidad.VENCIDO),
            Mensualidad(id = "2", idJugador = "j1", periodo = "2026-11", estado = EstadoMensualidad.PAGADO),
            Mensualidad(id = "3", idJugador = "j2", periodo = "2026-11", estado = EstadoMensualidad.PENDIENTE)
        )
        assertEquals(EstadoMensualidad.PAGADO, LogicaInicio.estadoMensualidad(lista, "j1", "2026-11"))
        assertEquals(EstadoMensualidad.PENDIENTE, LogicaInicio.estadoMensualidad(lista, "j2", "2026-11"))
    }

    @Test fun estadoMensualidadEsNullSinRegistroDelPeriodo() {
        val lista = listOf(Mensualidad(id = "1", idJugador = "j1", periodo = "2026-10", estado = EstadoMensualidad.PAGADO))
        assertNull(LogicaInicio.estadoMensualidad(lista, "j1", "2026-11"))
    }

    // ---------- Partidos: en curso y próximo ----------

    @Test fun partidoEnCursoDevuelveElPrimeroEnCurso() {
        val lista = listOf(
            partido("p1", EstadoPartido.PROGRAMADO),
            partido("p2", EstadoPartido.EN_CURSO),
            partido("p3", EstadoPartido.EN_CURSO)
        )
        assertEquals("p2", LogicaInicio.partidoEnCurso(lista) { it }?.id)
    }

    @Test fun partidoEnCursoEsNullSiNingunoEstaEnCurso() {
        val lista = listOf(partido("p1", EstadoPartido.FINALIZADO), partido("p2", EstadoPartido.CANCELADO))
        assertNull(LogicaInicio.partidoEnCurso(lista) { it })
    }

    @Test fun proximoPartidoIgnoraPasadosCanceladosYFinalizados() {
        val ahora = LocalDateTime.of(2026, 11, 25, 12, 0)
        val lista = listOf(
            partido("pasado", EstadoPartido.PROGRAMADO, "2026-11-24", "10:00"),
            partido("cancelado", EstadoPartido.CANCELADO, "2026-11-26", "10:00"),
            partido("lejano", EstadoPartido.PROGRAMADO, "2026-12-10", "10:00"),
            partido("cercano", EstadoPartido.PROGRAMADO, "2026-11-28", "10:00"),
            partido("hoyMasTarde", EstadoPartido.PROGRAMADO, "2026-11-25", "18:30")
        )
        assertEquals("hoyMasTarde", LogicaInicio.proximoPartido(lista, ahora) { it }?.id)
    }

    @Test fun proximoPartidoIgnoraFechasMalFormadas() {
        val ahora = LocalDateTime.of(2026, 11, 25, 12, 0)
        val lista = listOf(partido("raro", EstadoPartido.PROGRAMADO, "28/11/2026", "10:00"))
        assertNull(LogicaInicio.proximoPartido(lista, ahora) { it })
    }

    // ---------- DT · próximo entrenamiento ----------

    @Test fun proximoEntrenamientoSoloPublicadosFuturos() {
        val ahora = 1_000L
        val lista = listOf(
            Entrenamiento(id = "pasado", estado = EstadoEntrenamiento.PUBLICADO, fechaHoraEpochMillis = 500L),
            Entrenamiento(id = "cancelado", estado = EstadoEntrenamiento.CANCELADO, fechaHoraEpochMillis = 1_500L),
            Entrenamiento(id = "lejano", estado = EstadoEntrenamiento.PUBLICADO, fechaHoraEpochMillis = 9_000L),
            Entrenamiento(id = "proximo", estado = EstadoEntrenamiento.PUBLICADO, fechaHoraEpochMillis = 2_000L)
        )
        assertEquals("proximo", LogicaInicio.proximoEntrenamiento(lista, ahora)?.id)
    }

    @Test fun proximoEntrenamientoEsNullSinFuturos() {
        val lista = listOf(Entrenamiento(id = "pasado", estado = EstadoEntrenamiento.PUBLICADO, fechaHoraEpochMillis = 10L))
        assertNull(LogicaInicio.proximoEntrenamiento(lista, 1_000L))
    }

    // ---------- Ayudas ----------

    private fun cuenta(estado: EstadoCuenta) =
        CuentaPorVincular(perfil = PerfilUsuario(uid = estado.name, estado = estado), ficha = Jugador())

    private fun asistencia(estado: EstadoAsistencia) = Asistencia(estado = estado)

    private fun partido(
        id: String,
        estado: EstadoPartido,
        fecha: String = "2026-11-28",
        hora: String = "10:00"
    ) = Partido(id = id, estado = estado, fecha = fecha, hora = hora)

    private fun convocatoria(
        idPartido: String,
        disponibilidad: EstadoDisponibilidad,
        estadoPartido: EstadoPartido,
        fecha: String = "2026-11-28",
        hora: String = "10:00"
    ) = ConvocatoriaParaResponder(
        partido = partido(idPartido, estadoPartido, fecha, hora),
        jugador = Jugador(id = "j1"),
        convocado = JugadorConvocado(idJugador = "j1", estado = disponibilidad)
    )
}
