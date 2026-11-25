package com.example.projectsportpro2026_2.partido

import com.example.projectsportpro2026_2.datos.modelo.evento.EstadoEventoPartido
import com.example.projectsportpro2026_2.datos.modelo.evento.EventoPartido
import com.example.projectsportpro2026_2.datos.partido.RelojPartido
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/*
 * Rediseño "Línea de cal" · 7.2 (HU-14): minuto sugerido y reloj, calculados sin red.
 * 1T: ((ahora - INICIO) / 60000) + 1. 2T (marca local, D-02): 45 + ((ahora - marca) / 60000) + 1.
 */
class RelojPartidoTest {

    private val inicio = 1_000_000L
    private fun min(m: Long) = m * 60_000L
    private fun seg(s: Long) = s * 1_000L

    // ---------- Minuto sugerido ----------

    @Test fun sinInicioNoHaySugerencia() {
        assertNull(RelojPartido.minutoSugerido(null, null, inicio + min(10)))
        assertNull(RelojPartido.minutoSugerido(0L, null, inicio + min(10)))
    }

    @Test fun alEmpezarSugiereElMinutoUno() {
        assertEquals(1, RelojPartido.minutoSugerido(inicio, null, inicio))
        assertEquals(1, RelojPartido.minutoSugerido(inicio, null, inicio + seg(59)))
    }

    @Test fun primerTiempoSumaUnoAlosMinutosTranscurridos() {
        // 36 minutos y 30 segundos después del INICIO → minuto 37
        assertEquals(37, RelojPartido.minutoSugerido(inicio, null, inicio + min(36) + seg(30)))
    }

    @Test fun relojDelCelularAtrasadoNoDaMinutosNegativos() {
        assertEquals(1, RelojPartido.minutoSugerido(inicio, null, inicio - min(3)))
    }

    @Test fun segundoTiempoContinuaDesdeElMinuto46() {
        val marca = inicio + min(60)
        assertEquals(46, RelojPartido.minutoSugerido(inicio, marca, marca))
        assertEquals(56, RelojPartido.minutoSugerido(inicio, marca, marca + min(10) + seg(5)))
    }

    @Test fun marcaAnteriorAlInicioSeIgnora() {
        assertEquals(11, RelojPartido.minutoSugerido(inicio, inicio - min(5), inicio + min(10)))
    }

    @Test fun duracionDelPrimerTiempoSePuedeAjustar() {
        val marca = inicio + min(40)
        assertEquals(31, RelojPartido.minutoSugerido(inicio, marca, marca + min(5), minutosPrimerTiempo = 25))
    }

    // ---------- Reloj del LED ----------

    @Test fun relojPrimerTiempo() {
        val s = RelojPartido.segundosDeJuego(inicio, null, inicio + min(12) + seg(34))
        assertEquals("12:34", RelojPartido.textoReloj(s!!))
    }

    @Test fun relojSegundoTiempoArrancaEn45() {
        val marca = inicio + min(62)
        val s = RelojPartido.segundosDeJuego(inicio, marca, marca + seg(78))
        assertEquals("46:18", RelojPartido.textoReloj(s!!))
    }

    @Test fun relojSinInicioEsNull() {
        assertNull(RelojPartido.segundosDeJuego(null, null, inicio))
    }

    @Test fun relojPasaDe99Minutos() {
        assertEquals("120:05", RelojPartido.textoReloj(120 * 60L + 5))
    }

    // ---------- INICIO en la caché ----------

    @Test fun capturaInicioTomaElInicioValidoMasAntiguo() {
        val eventos = listOf(
            EventoPartido(id = "a", codigoTipoEvento = "INICIO", fechaCaptura = 500L, estado = EstadoEventoPartido.ANULADO),
            EventoPartido(id = "b", codigoTipoEvento = "INICIO", fechaCaptura = 900L),
            EventoPartido(id = "c", codigoTipoEvento = "INICIO", fechaCaptura = 700L),
            EventoPartido(id = "d", codigoTipoEvento = "GOL", fechaCaptura = 100L)
        )
        assertEquals(700L, RelojPartido.capturaInicio(eventos))
    }

    @Test fun capturaInicioSinInicioEsNull() {
        val eventos = listOf(EventoPartido(id = "d", codigoTipoEvento = "GOL", fechaCaptura = 100L))
        assertNull(RelojPartido.capturaInicio(eventos))
    }
}
