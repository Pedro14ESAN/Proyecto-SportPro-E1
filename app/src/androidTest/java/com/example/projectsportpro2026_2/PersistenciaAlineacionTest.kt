package com.example.projectsportpro2026_2

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.projectsportpro2026_2.datos.alineacion.*
import com.example.projectsportpro2026_2.datos.modelo.Jugador
import com.example.projectsportpro2026_2.datos.modelo.alineacion.*
import com.example.projectsportpro2026_2.datos.modelo.evento.*
import com.example.projectsportpro2026_2.datos.repositorio.alineacion.AlineacionEnVivo
import com.example.projectsportpro2026_2.ui.alineacion.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.util.CustomClassMapper
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PersistenciaAlineacionTest {
    @get:Rule val compose = createComposeRule()
    // Real SDK set/toObject mapper; no network access or production document writes.
    private val referencia = FirebaseFirestore.getInstance().document("pruebas/local")
    private val original = JugadorAlineacion("A", TipoJugadorAlineacion.TITULAR, "ARQ", 0, .31, .67)

    @Suppress("UNCHECKED_CAST")
    private fun guardar(valor: Any) = CustomClassMapper.convertToPlainJavaTypes(valor) as Map<String, Any?>

    private fun leer(campos: Map<String, Any?>) =
        CustomClassMapper.convertToCustomClass(campos, JugadorAlineacion::class.java, referencia)
            .recuperarCoordenadasGuardadas(campos)

    @Test fun guardarYLeerConMapperRealConservaCoordenadas() {
        val documento = guardar(original)
        assertEquals(.31, documento["xCampo"])
        assertEquals(.67, documento["yCampo"])
        assertFalse(documento.containsKey("xcampo"))
        assertFalse(documento.containsKey("ycampo"))
        assertEquals(original, leer(documento))
        // A second reload comes from the document, not from retained Compose/ViewModel state.
        assertEquals(original, leer(documento.toMap()))
    }

    @Test fun cabeceraConservaPersonalizadoFormacionYConfirmacion() {
        val cabecera = Alineacion(formato = FormatoPartido.CINCO_VS_CINCO,
            formacion = "1-2-1", personalizada = true, confirmada = true)
        val documento = guardar(cabecera)
        val recargada = CustomClassMapper.convertToCustomClass(documento, Alineacion::class.java, referencia)
        assertEquals(cabecera, recargada)
        assertEquals(true, documento["personalizada"])
        assertEquals("1-2-1", documento["formacion"])
    }

    @Test fun recuperaCoordenadasGuardadasPorLaVersionAnterior() {
        val documentoAnterior = guardar(original).toMutableMap().apply {
            put("xcampo", remove("xCampo"))
            put("ycampo", remove("yCampo"))
        }
        assertEquals(original, leer(documentoAnterior))
        val documentoNuevo = guardar(leer(documentoAnterior))
        assertEquals(.31, documentoNuevo["xCampo"])
        assertFalse(documentoNuevo.containsKey("xcampo"))
        // A canonical value must win if both names exist.
        assertEquals(.42, leer(documentoAnterior + ("xCampo" to .42)).xCampo)
    }

    @Test fun antiguaSinCoordenadasConservaFallbackOriginal() {
        val documento = mapOf<String, Any?>("idJugador" to "A", "tipo" to "TITULAR",
            "posicionCampo" to "ARQ", "orden" to 0L)
        val antigua = leer(documento)
        assertNull(antigua.xCampo)
        assertNull(antigua.yCampo)
        val cabecera = CustomClassMapper.convertToCustomClass(
            mapOf("formato" to "CINCO_VS_CINCO"), Alineacion::class.java, referencia)
        assertFalse(cabecera.personalizada)
        assertEquals("", cabecera.formacion)
        assertEquals(.88f, posicionesFormacion(cabecera.formato, cabecera.formacion).first().y)
    }

    @Test fun salirRecargarYMostrarEnVivoConservaUbicacionYSustitucion() {
        val documento = guardar(original)
        var etapa by mutableIntStateOf(0)
        val plantilla = listOf(Jugador(id = "A", nombres = "Ana"), Jugador(id = "B", nombres = "Bruno"))
        val cambio = EventoPartido(id = "cambio", codigoTipoEvento = "CAMBIO",
            equipoEvento = EquipoEvento.PROPIO, idJugadorSale = "A", idJugadorEntra = "B", minuto = 10)
        compose.setContent {
            MaterialTheme {
                when (etapa) {
                    0 -> CanchaAlineacion(slotsParaFormato(FormatoPartido.CINCO_VS_CINCO, "1-2-1"),
                        listOf(original), plantilla)
                    2 -> CanchaAlineacion(slotsParaFormato(FormatoPartido.CINCO_VS_CINCO, "1-2-1"),
                        listOf(leer(documento.toMap())), plantilla)
                    3, 4 -> {
                        val recargada = leer(documento.toMap())
                        val base = AlineacionEnVivo(FormatoPartido.CINCO_VS_CINCO,
                            listOf(recargada), listOf(recargada, JugadorAlineacion("B")), plantilla, "1-2-1")
                        CanchaAlineacionDerivada(base, if (etapa == 4) listOf(cambio) else emptyList(), plantilla)
                    }
                }
            }
        }
        val posicionOriginal = compose.onNodeWithTag("posicion-A").fetchSemanticsNode().boundsInRoot
        compose.runOnIdle { etapa = 1 }
        compose.onNodeWithTag("posicion-A").assertDoesNotExist()
        compose.runOnIdle { etapa = 2 }
        assertEquals(posicionOriginal, compose.onNodeWithTag("posicion-A").fetchSemanticsNode().boundsInRoot)
        compose.runOnIdle { etapa = 3 }
        assertEquals(posicionOriginal, compose.onNodeWithTag("posicion-A").fetchSemanticsNode().boundsInRoot)
        compose.runOnIdle { etapa = 4 }
        compose.onNodeWithTag("posicion-A").assertDoesNotExist()
        assertEquals(posicionOriginal, compose.onNodeWithTag("posicion-B").fetchSemanticsNode().boundsInRoot)
    }
}