package com.example.projectsportpro2026_2

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.projectsportpro2026_2.datos.modelo.alineacion.*
import com.example.projectsportpro2026_2.ui.alineacion.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FormacionPersonalizadaTest {
    @get:Rule val compose = createComposeRule()

    @Test fun arrastreNormalizadoGuardaAlSoltarYConservaPosicionAlRemontar() {
        var guardado = JugadorAlineacion("A", TipoJugadorAlineacion.TITULAR, "ARQ", xCampo = .5, yCampo = .5)
        var llamadas = 0
        var version by mutableIntStateOf(0)
        var ancho by mutableStateOf(300.dp)
        compose.setContent {
            MaterialTheme {
                key(version) {
                    BoxWithConstraints(androidx.compose.ui.Modifier.width(ancho).height(390.dp)) {
                        UbicacionEnCancha(SlotCampo("ARQ", .5f, .5f, "Arquero"), guardado,
                            maxWidth, maxHeight, true, { _, x, y ->
                                llamadas++
                                guardado = guardado.copy(xCampo = x.toDouble(), yCampo = y.toDouble())
                            }) { Text("AA", modifier = androidx.compose.ui.Modifier.size(54.dp)) }
                    }
                }
            }
        }
        val inicial = compose.onNodeWithTag("posicion-A").fetchSemanticsNode().boundsInRoot
        compose.onNodeWithTag("posicion-A").performTouchInput {
            down(center)
            advanceEventTime(700)
            moveBy(Offset(45f, -65f))
            advanceEventTime(50)
            up()
        }
        compose.runOnIdle {
            assertEquals(1, llamadas)
            assertTrue(guardado.xCampo!! > .5)
            assertTrue(guardado.yCampo!! < .5)
            version++
        }
        val final = compose.onNodeWithTag("posicion-A").fetchSemanticsNode().boundsInRoot
        assertTrue(final.left > inicial.left)
        assertTrue(final.top < inicial.top)
        compose.runOnIdle { ancho = 240.dp }
        val reducido = compose.onNodeWithTag("posicion-A").fetchSemanticsNode().boundsInRoot
        assertTrue(reducido.left < final.left)
        compose.runOnIdle { assertEquals(1, llamadas) }
    }

    @Test fun cancelarConfirmacionConservaDistribucionPersonalizada() {
        var seleccion: String? = "sin cambios"
        compose.setContent {
            MaterialTheme {
                SelectorFormacion(FormatoPartido.ONCE_VS_ONCE, "4-3-3", true, true, true) { seleccion = it }
            }
        }
        compose.onNodeWithText("Personalizado").performClick()
        compose.onNodeWithText("4-4-2").performClick()
        compose.onNodeWithText("Cancelar").performClick()
        compose.runOnIdle { assertEquals("sin cambios", seleccion) }
        compose.onNodeWithText("Personalizado").performClick()
        compose.onNodeWithText("4-4-2").performClick()
        compose.onNodeWithText("Redistribuir").performClick()
        compose.runOnIdle { assertEquals("4-4-2", seleccion) }
    }
}
