package com.example.projectsportpro2026_2.navegacion

import com.example.projectsportpro2026_2.ui.navegacion.Rutas
import com.example.projectsportpro2026_2.ui.navegacion.barraVisibleEn
import com.example.projectsportpro2026_2.ui.navegacion.rutaPestanaActiva
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Reglas puras de la barra inferior: dónde se muestra y qué pestaña se resalta. */
class NavegacionBarraTest {

    private val adm = listOf(Rutas.INICIO_ADM, Rutas.ACADEMIAS, Rutas.MIS_PARTIDOS, Rutas.COMUNIDAD, Rutas.PERFIL)
    private val dt = listOf(Rutas.INICIO_DT, Rutas.ACADEMIAS, Rutas.MIS_PARTIDOS, Rutas.COMUNIDAD, Rutas.PERFIL)
    private val jug = listOf(Rutas.INICIO_JUG, Rutas.ENTRENAMIENTOS, Rutas.MIS_PARTIDOS, Rutas.COMUNIDAD, Rutas.PERFIL)
    private val pad = listOf(Rutas.INICIO_PAD, Rutas.MIS_JUGADORES, Rutas.MIS_PARTIDOS, Rutas.COMUNIDAD, Rutas.PERFIL)

    @Test
    fun `la barra se oculta en autenticacion y cuenta pendiente`() {
        listOf(
            Rutas.BIENVENIDA, Rutas.INTRODUCCION, Rutas.LOGIN,
            Rutas.REGISTRO, Rutas.RECUPERAR, Rutas.CUENTA_PENDIENTE
        ).forEach { ruta ->
            assertFalse(ruta, barraVisibleEn(ruta, hayPestanas = true))
        }
    }

    @Test
    fun `la barra se muestra en pantallas principales e internas de la sesion`() {
        listOf(
            Rutas.INICIO_DT, Rutas.ACADEMIAS, Rutas.DETALLE_PARTIDO, Rutas.PARTIDO_EN_VIVO,
            Rutas.CONVOCATORIA, Rutas.ALINEACION, Rutas.RESUMEN_PARTIDO, Rutas.ESTADISTICAS,
            Rutas.CREAR_PARTIDO, Rutas.EDITAR_PARTIDO, Rutas.NOTIFICACIONES_ABRIR, Rutas.PERFIL
        ).forEach { ruta ->
            assertTrue(ruta, barraVisibleEn(ruta, hayPestanas = true))
        }
    }

    @Test
    fun `sin pestanas (sin sesion o cuenta pendiente) no hay barra`() {
        assertFalse(barraVisibleEn(Rutas.INICIO_ADM, hayPestanas = false))
        assertFalse(barraVisibleEn(null, hayPestanas = true))
    }

    @Test
    fun `una pestana se resalta a si misma`() {
        adm.forEach { assertEquals(it, rutaPestanaActiva(it, adm)) }
        jug.forEach { assertEquals(it, rutaPestanaActiva(it, jug)) }
        pad.forEach { assertEquals(it, rutaPestanaActiva(it, pad)) }
    }

    @Test
    fun `pantallas de un partido resaltan Partidos`() {
        listOf(
            Rutas.DETALLE_PARTIDO, Rutas.EDITAR_PARTIDO, Rutas.CONVOCATORIA, Rutas.ALINEACION,
            Rutas.PARTIDO_EN_VIVO, Rutas.SEGUIMIENTO_PARTIDO, Rutas.RESUMEN_PARTIDO
        ).forEach { ruta ->
            assertEquals(ruta, Rutas.MIS_PARTIDOS, rutaPestanaActiva(ruta, dt))
            assertEquals(ruta, Rutas.MIS_PARTIDOS, rutaPestanaActiva(ruta, jug))
        }
    }

    @Test
    fun `equipos, jugadores, partidos de academia y catalogo resaltan Academias`() {
        listOf(Rutas.EQUIPOS, Rutas.JUGADORES, Rutas.PARTIDOS, Rutas.CREAR_PARTIDO, Rutas.CATALOGO_EVENTOS)
            .forEach { ruta -> assertEquals(ruta, Rutas.ACADEMIAS, rutaPestanaActiva(ruta, adm)) }
    }

    @Test
    fun `moderacion resalta Comunidad`() {
        assertEquals(Rutas.COMUNIDAD, rutaPestanaActiva(Rutas.MODERACION, adm))
    }

    @Test
    fun `el resto resalta Inicio`() {
        listOf(
            Rutas.MENSUALIDADES, Rutas.EJERCICIOS, Rutas.ASISTENCIAS, Rutas.ANUNCIOS,
            Rutas.ESTADISTICAS, Rutas.NOTIFICACIONES_ABRIR, Rutas.PLANES, Rutas.VINCULACIONES,
            Rutas.CUENTAS_JUGADORES, Rutas.MIS_CONVOCATORIAS
        ).forEach { ruta -> assertEquals(ruta, Rutas.INICIO_ADM, rutaPestanaActiva(ruta, adm)) }
    }

    @Test
    fun `entrenamientos es pestana solo para JUG y mis jugadores solo para PAD`() {
        assertEquals(Rutas.ENTRENAMIENTOS, rutaPestanaActiva(Rutas.ENTRENAMIENTOS, jug))
        assertEquals(Rutas.INICIO_DT, rutaPestanaActiva(Rutas.ENTRENAMIENTOS, dt))
        assertEquals(Rutas.INICIO_PAD, rutaPestanaActiva(Rutas.ENTRENAMIENTOS, pad))
        assertEquals(Rutas.MIS_JUGADORES, rutaPestanaActiva(Rutas.MIS_JUGADORES, pad))
    }

    @Test
    fun `si el rol no tiene la pestana de la seccion se resalta Inicio`() {
        // JUG no tiene Academias
        assertEquals(Rutas.INICIO_JUG, rutaPestanaActiva(Rutas.EQUIPOS, jug))
    }

    @Test
    fun `sin pestanas no se resalta nada`() {
        assertEquals(null, rutaPestanaActiva(Rutas.INICIO_ADM, emptyList()))
    }
}
