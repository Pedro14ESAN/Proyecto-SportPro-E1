package com.example.projectsportpro2026_2

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.projectsportpro2026_2.datos.modelo.Jugador
import com.example.projectsportpro2026_2.datos.modelo.alineacion.FormatoPartido
import com.example.projectsportpro2026_2.datos.modelo.alineacion.JugadorAlineacion
import com.example.projectsportpro2026_2.datos.modelo.alineacion.TipoJugadorAlineacion
import com.example.projectsportpro2026_2.datos.modelo.evento.TipoTarjeta
import com.example.projectsportpro2026_2.datos.modelo.evento.EquipoEvento
import com.example.projectsportpro2026_2.datos.modelo.evento.EstadoEventoPartido
import com.example.projectsportpro2026_2.datos.modelo.evento.EventoPartido
import com.example.projectsportpro2026_2.datos.modelo.sincronizacion.InstantaneaEventos
import com.example.projectsportpro2026_2.datos.repositorio.alineacion.AlineacionEnVivo
import com.example.projectsportpro2026_2.datos.repositorio.sincronizacion.RepositorioSincronizacion
import com.example.projectsportpro2026_2.ui.alineacion.CanchaAlineacionDerivada
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AlineacionReactivaTest {
    @get:Rule val compose = createComposeRule()
    private val eventos = MutableStateFlow<List<EventoPartido>>(emptyList())
    private val titulares = listOf(JugadorAlineacion("A", TipoJugadorAlineacion.TITULAR, "ARQ", 1))
    private val plantilla = listOf(
        Jugador(id = "A", nombres = "Ana", apellidos = "Uno"),
        Jugador(id = "B", nombres = "Bruno", apellidos = "Dos"),
        Jugador(id = "C", nombres = "Carla", apellidos = "Tres")
    )
    private val base = AlineacionEnVivo(FormatoPartido.CINCO_VS_CINCO, titulares,
        titulares + listOf(JugadorAlineacion("B"), JugadorAlineacion("C")), plantilla)
    private val primero = cambio("1", "A", "B", 30, 100)
    private val segundo = cambio("2", "B", "C", 60, 200)

    private fun cambio(id: String, sale: String, entra: String, minuto: Int, captura: Long) =
        EventoPartido(id = id, codigoTipoEvento = "CAMBIO", equipoEvento = EquipoEvento.PROPIO,
            idJugadorSale = sale, idJugadorEntra = entra, minuto = minuto, fechaCaptura = captura)

    @Test fun usuarioReconstruyeCanchaSinReabrirTrasCambiosYAnulaciones() = comprobar(false)
    @Test fun administradorReconstruyeCanchaConEventosClasificados() = comprobar(true)

    @Test fun indicadoresSeActualizanYNoSeTransfierenAlSustituto() {
        compose.setContent {
            val observados by eventos.collectAsState()
            MaterialTheme { CanchaAlineacionDerivada(base, observados, plantilla) }
        }
        val gol = EventoPartido(id = "gol1", codigoTipoEvento = "GOL",
            equipoEvento = EquipoEvento.PROPIO, idJugador = "A", afectaMarcador = true)
        val segundoGol = gol.copy(id = "gol2")
        val amarilla = gol.copy(id = "amarilla", codigoTipoEvento = "TARJETA",
            afectaMarcador = false, tipoTarjeta = TipoTarjeta.AMARILLA)
        val roja = amarilla.copy(id = "roja", tipoTarjeta = TipoTarjeta.ROJA)
        emitir(listOf(gol, segundoGol, amarilla, roja))
        mostrarIndicador("Ana Uno, 2 goles")
        mostrarIndicador("Ana Uno, tarjeta amarilla")
        mostrarIndicador("Ana Uno, tarjeta roja")
        // D-01 (Línea de cal): el balón ahora es un ícono; se comprueba la cantidad visible "×2"
        compose.onNodeWithText("×2", useUnmergedTree = true).assertIsDisplayed()

        // Al corregir un gol para B, los totales de A disminuyen.
        val corregido = segundoGol.copy(idJugador = "B", version = 2)
        emitir(listOf(gol, corregido, amarilla, roja))
        mostrarIndicador("Ana Uno, 1 gol")
        compose.onNodeWithContentDescription("Ana Uno, 2 goles", useUnmergedTree = true).assertDoesNotExist()

        // La tarjeta corregida pierde el indicador amarillo.
        val amarillaCorregida = amarilla.copy(tipoTarjeta = TipoTarjeta.ROJA, version = 2)
        emitir(listOf(gol, corregido, amarillaCorregida, roja))
        compose.waitForIdle()
        compose.onNodeWithContentDescription("Ana Uno, tarjeta amarilla", useUnmergedTree = true).assertDoesNotExist()

        // Entra B con sus propios goles, sin heredar las tarjetas de A.
        emitir(listOf(gol, corregido, amarillaCorregida, roja, primero))
        mostrar("Bruno", "Ana", "Carla")
        mostrarIndicador("Bruno Dos, 1 gol")
        compose.onNodeWithContentDescription("Bruno Dos, tarjeta roja", useUnmergedTree = true).assertDoesNotExist()

        emitir(listOf(gol, corregido.copy(estado = EstadoEventoPartido.ANULADO), primero))
        compose.waitForIdle()
        compose.onNodeWithContentDescription("Bruno Dos, 1 gol", useUnmergedTree = true).assertDoesNotExist()
        compose.onNodeWithText("BD", useUnmergedTree = true).assertIsDisplayed()
    }

    private fun mostrarIndicador(descripcion: String) {
        compose.waitUntil(5000) {
            compose.onAllNodes(
                androidx.compose.ui.test.hasContentDescription(descripcion),
                useUnmergedTree = true
            ).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithContentDescription(descripcion, useUnmergedTree = true).assertIsDisplayed()
    }

    private fun comprobar(admin: Boolean) {
        val sincronizacion = RepositorioSincronizacion()
        compose.setContent {
            val observados by eventos.collectAsState()
            val visibles = if (admin) {
                sincronizacion.clasificar(InstantaneaEventos(eventos = observados)).map { it.evento }
            } else observados
            MaterialTheme { CanchaAlineacionDerivada(base, visibles, plantilla) }
        }
        mostrar("Ana", "Bruno", "Carla")
        emitir(listOf(primero))
        mostrar("Bruno", "Ana", "Carla")
        emitir(listOf(segundo, primero))
        mostrar("Carla", "Ana", "Bruno")
        emitir(listOf(primero, segundo.copy(estado = EstadoEventoPartido.ANULADO)))
        mostrar("Bruno", "Ana", "Carla")
        emitir(listOf(primero.copy(estado = EstadoEventoPartido.ANULADO), segundo))
        mostrar("Ana", "Bruno", "Carla")
        // Las iniciales también pertenecen al jugador restaurado.
        compose.onNodeWithText("AU", useUnmergedTree = true).assertIsDisplayed()
    }

    private fun emitir(nuevos: List<EventoPartido>) {
        compose.runOnIdle { eventos.value = nuevos }
    }

    private fun mostrar(nombre: String, ausente1: String, ausente2: String) {
        compose.waitUntil(5000) {
            compose.onAllNodesWithText(nombre, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText(nombre, useUnmergedTree = true).assertIsDisplayed()
        compose.onNodeWithText(ausente1, useUnmergedTree = true).assertDoesNotExist()
        compose.onNodeWithText(ausente2, useUnmergedTree = true).assertDoesNotExist()
    }
}