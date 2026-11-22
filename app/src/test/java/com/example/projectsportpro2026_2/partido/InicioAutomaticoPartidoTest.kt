package com.example.projectsportpro2026_2.partido

import com.example.projectsportpro2026_2.datos.modelo.evento.EquipoEvento
import com.example.projectsportpro2026_2.datos.modelo.evento.EstadoEventoPartido
import com.example.projectsportpro2026_2.datos.modelo.evento.EventoPartido
import com.example.projectsportpro2026_2.datos.modelo.evento.TipoEventoCatalogo
import com.example.projectsportpro2026_2.datos.modelo.partido.EstadoPartido
import com.example.projectsportpro2026_2.datos.modelo.partido.Partido
import com.example.projectsportpro2026_2.datos.partido.ID_INICIO_AUTOMATICO
import com.example.projectsportpro2026_2.datos.partido.planificarCambioEstado
import org.junit.Assert.*
import org.junit.Test

class InicioAutomaticoPartidoTest {
    private val programado = Partido(id = "p1", idAcademia = "a1", estado = EstadoPartido.PROGRAMADO)
    private val tipo = TipoEventoCatalogo(id = "catalogo-personalizado", codigo = "INICIO", nombre = "Inicio")

    private fun plan(
        partido: Partido = programado,
        destino: EstadoPartido = EstadoPartido.EN_CURSO,
        existe: Boolean = false
    ) = planificarCambioEstado(partido, destino, existe, tipo, "autor", 1000, "dispositivo")

    @Test fun creaEventoEstructuradoConMinutoUnoYTipoExistente() {
        val evento = checkNotNull(plan()?.eventoInicio)
        assertEquals(ID_INICIO_AUTOMATICO, evento.id)
        assertEquals("p1", evento.idPartido)
        assertEquals("a1", evento.idAcademia)
        assertEquals(tipo.id, evento.idTipoEvento)
        assertEquals("INICIO", evento.codigoTipoEvento)
        assertEquals("Inicio del partido", evento.nombreTipoEvento)
        assertEquals(1, evento.minuto)
        assertEquals("autor", evento.creadoPor)
        assertEquals(1000L, evento.fechaCaptura)
        assertEquals("dispositivo", evento.idDispositivo)
        assertEquals(EquipoEvento.NINGUNO, evento.equipoEvento)
        assertEquals(EstadoEventoPartido.ACTIVO, evento.estado)
        assertTrue(evento.afectaEstadoPartido)
        assertFalse(evento.afectaMarcador)
    }

    @Test fun repetirInicioCienVecesSoloProduceUnEvento() {
        var partido = programado
        val eventos = mutableMapOf<String, EventoPartido>()
        repeat(100) {
            val cambio = plan(partido, existe = eventos.isNotEmpty())
            if (cambio != null) {
                cambio.eventoInicio?.let { evento -> eventos[evento.id] = evento }
                partido = partido.copy(estado = EstadoPartido.EN_CURSO)
            }
        }
        assertEquals(1, eventos.size)
        assertEquals(1000L, eventos.values.single().fechaCaptura)
    }

    @Test fun reintentoDeTransaccionMantieneIdentidadYContenido() {
        assertEquals(plan(), plan())
        val ganador = checkNotNull(plan())
        // Tras confirmar otro cliente, la transacción vuelve a leer el estado EN_CURSO.
        assertNull(plan(programado.copy(estado = EstadoPartido.EN_CURSO)))
        assertNotNull(ganador.eventoInicio)
    }

    @Test fun existenteEnCursoNoRecibeEventoHistoricoAunqueNoTengaInicio() {
        assertNull(plan(programado.copy(estado = EstadoPartido.EN_CURSO), existe = false))
    }

    @Test fun respetaInicioExistenteSinSobrescribirloInclusoSiFueAnulado() {
        // El repositorio usa existencia del documento, independientemente de su estado o versión.
        val cambio = checkNotNull(plan(existe = true))
        assertNull(cambio.eventoInicio)
    }

    @Test fun otrosCambiosDeEstadoNoCreanInicio() {
        assertNull(plan(destino = EstadoPartido.CANCELADO)?.eventoInicio)
        assertNull(plan(programado.copy(estado = EstadoPartido.EN_CURSO),
            EstadoPartido.FINALIZADO)?.eventoInicio)
    }

    @Test fun finalizadoYCanceladoNoPuedenReiniciarse() {
        for (estado in listOf(EstadoPartido.FINALIZADO, EstadoPartido.CANCELADO)) {
            assertThrows(IllegalStateException::class.java) {
                plan(programado.copy(estado = estado))
            }
        }
    }

    @Test fun partidosDistintosTienenSuPropioInicio() {
        val primero = checkNotNull(plan()?.eventoInicio)
        val segundo = checkNotNull(plan(programado.copy(id = "p2"))?.eventoInicio)
        assertEquals(primero.id, segundo.id)
        assertNotEquals(primero.idPartido, segundo.idPartido)
    }
}