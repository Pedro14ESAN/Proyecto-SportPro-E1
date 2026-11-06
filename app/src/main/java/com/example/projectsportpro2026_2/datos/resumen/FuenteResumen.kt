package com.example.projectsportpro2026_2.datos.resumen

import com.example.projectsportpro2026_2.datos.estadisticas.CalculadoraEstadisticas
import com.example.projectsportpro2026_2.datos.modelo.evento.EquipoEvento
import com.example.projectsportpro2026_2.datos.modelo.evento.EstadoEventoPartido
import com.example.projectsportpro2026_2.datos.modelo.evento.EventoPartido
import java.security.MessageDigest

/*
 * HU26 - Fuente del resumen: solo eventos válidos.
 *
 * - Se excluyen los ANULADOS.
 * - De los corregidos se usa la versión vigente (el documento
 *   del evento ya es la última versión; su campo "version"
 *   entra en la huella).
 * - La huella identifica el conjunto exacto de eventos usado.
 *   Si después se corrige o anula un evento, la huella cambia y
 *   el resumen aprobado queda DESACTUALIZADO (RN-13).
 *
 * IMPORTANTE: la Cloud Function (functions/index.js) calcula la
 * huella con el mismo algoritmo. Si se cambia aquí, cambiar allá.
 */
object FuenteResumen {

    // Mínimo de eventos de juego (sin contar INICIO/FIN) para un resumen fiable
    const val MINIMO_EVENTOS_JUEGO = 2

    private val CODIGOS_CONTROL = setOf("INICIO", "FIN")

    fun eventosValidos(eventos: List<EventoPartido>): List<EventoPartido> =
        eventos
            .filter { it.estado == EstadoEventoPartido.ACTIVO }
            .sortedWith(compareBy<EventoPartido> { it.minuto }.thenBy { it.fechaCaptura })

    fun eventosDeJuego(eventos: List<EventoPartido>): List<EventoPartido> =
        eventosValidos(eventos).filter { it.codigoTipoEvento !in CODIGOS_CONTROL }

    fun suficientes(eventos: List<EventoPartido>): Boolean =
        eventosDeJuego(eventos).size >= MINIMO_EVENTOS_JUEGO

    // sha256("id1:v1|id2:v2|…") ordenado por id; primeros 16 caracteres hex
    fun huella(eventos: List<EventoPartido>): String {
        val base = eventos
            .filter { it.estado == EstadoEventoPartido.ACTIVO }
            .sortedBy { it.id }
            .joinToString("|") { "${it.id}:${it.version}" }

        val bytes = MessageDigest.getInstance("SHA-256").digest(base.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }.take(16)
    }

    data class Marcador(val propio: Int, val rival: Int)

    fun marcador(eventos: List<EventoPartido>): Marcador {
        val validos = eventosValidos(eventos).filter { it.afectaMarcador }
        return Marcador(
            propio = validos.count { it.equipoEvento == EquipoEvento.PROPIO },
            rival = validos.count { it.equipoEvento == EquipoEvento.RIVAL }
        )
    }

    /*
     * Respaldo sin IA: redacta SOLO hechos a partir de los eventos,
     * sin interpretaciones. Se usa cuando la Cloud Function no está
     * disponible, para que el DT no pierda el trabajo; queda
     * igualmente como BORRADOR y marcado como "PLANTILLA".
     */
    fun redactarPlantilla(
        eventos: List<EventoPartido>,
        nombreEquipo: String,
        nombreRival: String
    ): String {
        val validos = eventosValidos(eventos)
        val m = marcador(eventos)

        fun equipo(e: EventoPartido): String = when (e.equipoEvento) {
            EquipoEvento.PROPIO -> nombreEquipo
            EquipoEvento.RIVAL -> nombreRival
            EquipoEvento.NINGUNO -> ""
        }

        fun jugador(e: EventoPartido): String =
            e.nombreJugador.ifBlank {
                if (e.equipoEvento == EquipoEvento.NINGUNO) "" else "jugador no identificado"
            }

        val lineas = validos.map { e ->
            val partes = buildList {
                add(e.nombreTipoEvento.ifBlank { e.codigoTipoEvento })
                jugador(e).takeIf { it.isNotBlank() }?.let { add(it) }
                equipo(e).takeIf { it.isNotBlank() }?.let { add("($it)") }
            }
            val obs = e.observacion.takeIf { it.isNotBlank() }?.let { " — $it" }.orEmpty()
            "• Min ${e.minuto}': ${partes.joinToString(" ")}$obs."
        }

        val goles = validos.filter { CalculadoraEstadisticas.esGol(it) }
        val tarjetas = validos.count {
            CalculadoraEstadisticas.esTarjetaAmarilla(it) || CalculadoraEstadisticas.esTarjetaRoja(it)
        }

        return buildString {
            appendLine("$nombreEquipo ${m.propio} – ${m.rival} $nombreRival")
            appendLine()
            append("El partido terminó ${m.propio}-${m.rival}. ")
            if (goles.isEmpty()) {
                append("No se registraron goles. ")
            } else {
                append("Se registraron ${goles.size} gol(es). ")
            }
            if (tarjetas > 0) append("Hubo $tarjetas tarjeta(s). ")
            appendLine()
            appendLine()
            appendLine("Cronología registrada:")
            lineas.forEach { appendLine(it) }
        }.trim()
    }
}
