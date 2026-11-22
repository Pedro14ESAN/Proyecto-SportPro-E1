package com.example.projectsportpro2026_2

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.projectsportpro2026_2.datos.modelo.evento.EventoPartido
import com.example.projectsportpro2026_2.datos.modelo.sincronizacion.EstadoSincronizacionEvento
import com.example.projectsportpro2026_2.datos.modelo.sincronizacion.EventoSincronizado
import com.example.projectsportpro2026_2.ui.sincronizacion.BannerSincronizacion
import com.example.projectsportpro2026_2.ui.sincronizacion.EstadoSincronizacionUi
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SincronizacionCompactaTest {
    @get:Rule val compose = createComposeRule()

    @Test fun sincronizadoMuestraConfirmacionSinAccionDeSimulacion() {
        compose.setContent {
            MaterialTheme {
                BannerSincronizacion(
                    EstadoSincronizacionUi(ultimaSincronizacion = 1_700_000_000_000),
                    onReintentar = {}
                )
            }
        }
        compose.onNodeWithText("✓ Sincronizado · Última confirmación", substring = true).assertIsDisplayed()
        compose.onNodeWithText("Simular sin conexión").assertDoesNotExist()
        compose.onNodeWithText("Reconectar").assertDoesNotExist()
        compose.onNodeWithText("↻ Reintentar sincronización").assertDoesNotExist()
    }

    @Test fun offlineConservaPendientesYExplicaAlmacenamientoLocal() {
        val pendiente = EventoSincronizado(EventoPartido(id = "1"), EstadoSincronizacionEvento.PENDIENTE)
        compose.setContent {
            MaterialTheme {
                BannerSincronizacion(
                    EstadoSincronizacionUi(hayRed = false, eventos = listOf(pendiente)),
                    onReintentar = {}
                )
            }
        }
        compose.onNodeWithText("Sin conexión · 1 pendiente", substring = true).assertIsDisplayed()
        compose.onNodeWithText("Sin confirmación del servidor", substring = true).assertIsDisplayed()
        compose.onNodeWithText("Los eventos se guardan en este dispositivo", substring = true).assertIsDisplayed()
        compose.onNodeWithText("↻ Reintentar sincronización").assertDoesNotExist()
    }

    @Test fun conRedConservaLaAccionDeReintento() {
        var reintentos = 0
        val pendiente = EventoSincronizado(EventoPartido(id = "1"), EstadoSincronizacionEvento.PENDIENTE)
        compose.setContent {
            MaterialTheme {
                BannerSincronizacion(
                    EstadoSincronizacionUi(eventos = listOf(pendiente)),
                    onReintentar = { reintentos++ }
                )
            }
        }
        compose.onNodeWithText("Sincronizando · 1 pendiente", substring = true).assertIsDisplayed()
        compose.onNodeWithText("↻ Reintentar sincronización").performClick()
        compose.runOnIdle { assertEquals(1, reintentos) }
    }
}