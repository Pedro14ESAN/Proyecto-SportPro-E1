package com.example.projectsportpro2026_2.comunidad

import com.example.projectsportpro2026_2.datos.modelo.Academia
import com.example.projectsportpro2026_2.datos.modelo.Equipo
import com.example.projectsportpro2026_2.datos.modelo.EstadoPublicacion
import com.example.projectsportpro2026_2.datos.modelo.Publicacion
import com.example.projectsportpro2026_2.datos.modelo.TipoReaccion
import com.example.projectsportpro2026_2.datos.modelo.VisibilidadPublicacion
import com.example.projectsportpro2026_2.datos.repositorio.Pertenencia
import com.example.projectsportpro2026_2.datos.repositorio.filtrarFeedComunidad
import com.example.projectsportpro2026_2.ui.inicio.LogicaInicio
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Filtro del feed "Para ti" (HU21 / RC-03 / HU24), extraído de RepositorioPublicaciones.listarFeed
 * y compartido con la vista previa de Comunidad del inicio (7.1).
 */
class FiltroFeedComunidadTest {

    // Usuario de la academia A1 y del equipo E1; administra la academia A3
    private val pertenencia = Pertenencia(
        academias = listOf(Academia(id = "A1"), Academia(id = "A3")),
        equipos = listOf(Equipo(id = "E1")),
        idsAcademiasGestionadas = setOf("A3")
    )

    private fun pub(
        id: String,
        visibilidad: VisibilidadPublicacion = VisibilidadPublicacion.COMUNIDAD,
        idAcademia: String = "",
        idEquipo: String = "",
        idAutor: String = "autor",
        estado: EstadoPublicacion = EstadoPublicacion.VISIBLE,
        fecha: Long = 0L
    ) = Publicacion(
        id = id,
        visibilidad = visibilidad,
        idAcademia = idAcademia,
        idEquipo = idEquipo,
        idAutor = idAutor,
        estado = estado,
        fechaCreacion = fecha
    )

    private fun ids(lista: List<Publicacion>) = lista.map { it.id }

    @Test
    fun `comunidad se ve aunque sea de otra academia`() {
        val r = filtrarFeedComunidad(listOf(pub("1", idAcademia = "OTRA")), pertenencia, emptySet())
        assertEquals(listOf("1"), ids(r))
    }

    @Test
    fun `academia solo si el usuario pertenece a ella`() {
        val r = filtrarFeedComunidad(
            listOf(
                pub("mia", VisibilidadPublicacion.ACADEMIA, idAcademia = "A1"),
                pub("ajena", VisibilidadPublicacion.ACADEMIA, idAcademia = "A2")
            ),
            pertenencia, emptySet()
        )
        assertEquals(listOf("mia"), ids(r))
    }

    @Test
    fun `equipo si es su equipo o si administra la academia`() {
        val r = filtrarFeedComunidad(
            listOf(
                pub("miEquipo", VisibilidadPublicacion.EQUIPO, idAcademia = "A1", idEquipo = "E1", fecha = 3),
                pub("otroEquipo", VisibilidadPublicacion.EQUIPO, idAcademia = "A1", idEquipo = "E2", fecha = 2),
                pub("gestionada", VisibilidadPublicacion.EQUIPO, idAcademia = "A3", idEquipo = "E9", fecha = 1)
            ),
            pertenencia, emptySet()
        )
        assertEquals(listOf("miEquipo", "gestionada"), ids(r))
    }

    @Test
    fun `sin eliminadas ni ocultas por moderacion`() {
        val r = filtrarFeedComunidad(
            listOf(
                pub("visible", fecha = 3),
                pub("eliminada", estado = EstadoPublicacion.ELIMINADA, fecha = 2),
                pub("oculta", estado = EstadoPublicacion.OCULTA, fecha = 1)
            ),
            pertenencia, emptySet()
        )
        assertEquals(listOf("visible"), ids(r))
    }

    @Test
    fun `sin publicaciones de usuarios bloqueados`() {
        val r = filtrarFeedComunidad(
            listOf(pub("1", idAutor = "bloqueado"), pub("2", idAutor = "amigo")),
            pertenencia, setOf("bloqueado")
        )
        assertEquals(listOf("2"), ids(r))
    }

    @Test
    fun `repetidas quedan una vez y se conserva la primera version`() {
        val nueva = pub("1", fecha = 5).copy(texto = "nueva")
        val vieja = pub("1", fecha = 5).copy(texto = "vieja")
        val r = filtrarFeedComunidad(listOf(nueva, vieja), pertenencia, emptySet())
        assertEquals(1, r.size)
        assertEquals("nueva", r.first().texto)
    }

    @Test
    fun `una repetida oculta en la version nueva no aparece`() {
        // La versión en tiempo real (primera) manda: si la ocultaron, no se muestra la vieja
        val r = filtrarFeedComunidad(
            listOf(pub("1", estado = EstadoPublicacion.OCULTA), pub("1")),
            pertenencia, emptySet()
        )
        assertTrue(r.isEmpty())
    }

    @Test
    fun `ordenadas de la mas reciente a la mas antigua`() {
        val r = filtrarFeedComunidad(
            listOf(pub("a", fecha = 1), pub("c", fecha = 3), pub("b", fecha = 2)),
            pertenencia, emptySet()
        )
        assertEquals(listOf("c", "b", "a"), ids(r))
    }

    @Test
    fun `lista vacia`() {
        assertTrue(filtrarFeedComunidad(emptyList(), pertenencia, emptySet()).isEmpty())
    }

    // ---------- Inicio: las 3 más recientes y los emojis ----------

    @Test
    fun `el inicio toma las 3 mas recientes`() {
        val lista = (1..5).map { pub("p$it", fecha = it.toLong()) }
        assertEquals(listOf("p5", "p4", "p3"), ids(LogicaInicio.publicacionesRecientes(lista)))
    }

    @Test
    fun `con menos de 3 publicaciones el inicio muestra las que hay`() {
        assertEquals(1, LogicaInicio.publicacionesRecientes(listOf(pub("1"))).size)
        assertTrue(LogicaInicio.publicacionesRecientes(emptyList()).isEmpty())
    }

    @Test
    fun `emojis presentes en el orden de las reacciones`() {
        val p = pub("1").copy(
            reacciones = mapOf(
                "u1" to TipoReaccion.FUERZA.name,
                "u2" to TipoReaccion.ME_GUSTA.name,
                "u3" to TipoReaccion.FUERZA.name
            )
        )
        assertEquals("❤️💪", LogicaInicio.emojisReacciones(p))
        assertEquals("", LogicaInicio.emojisReacciones(pub("2")))
    }
}
