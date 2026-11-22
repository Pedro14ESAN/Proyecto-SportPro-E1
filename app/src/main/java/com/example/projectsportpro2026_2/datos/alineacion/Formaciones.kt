package com.example.projectsportpro2026_2.datos.alineacion

import com.example.projectsportpro2026_2.datos.modelo.Jugador
import com.example.projectsportpro2026_2.datos.modelo.alineacion.*

data class PosicionFormacion(val id: String, val x: Float, val y: Float, val etiqueta: String)

fun formacionesParaFormato(formato: FormatoPartido): List<String> = when (formato) {
    FormatoPartido.ONCE_VS_ONCE -> listOf("4-4-2", "4-3-3", "4-2-3-1", "3-5-2", "3-4-3")
    FormatoPartido.OCHO_VS_OCHO -> listOf("3-2-2", "3-3-1", "2-3-2")
    FormatoPartido.SIETE_VS_SIETE -> listOf("3-2-1", "2-3-1", "2-2-2")
    FormatoPartido.CINCO_VS_CINCO -> listOf("2-1-1", "1-2-1", "2-2")
}

fun posicionesFormacion(formato: FormatoPartido, formacion: String = ""): List<PosicionFormacion> {
    // Missing/unknown metadata never migrates a historical lineup on read.
    if (formacion !in formacionesParaFormato(formato)) return slotsHistoricos(formato)
    val filas = formacion.split("-").map { it.toInt() }
    return listOf(PosicionFormacion("ARQ", .5f, .82f, "Arquero")) +
        filas.flatMapIndexed { fila, cantidad ->
            val prefijo = when (fila) { 0 -> "DEF"; filas.lastIndex -> "DEL"; else -> "MED" }
            (0 until cantidad).map { columna ->
                PosicionFormacion(
                    "${prefijo}_${fila}_${columna}",
                    if (cantidad == 1) .5f else .14f + .72f * columna / (cantidad - 1),
                    .68f - .53f * fila / (filas.size - 1),
                    "${when (prefijo) { "DEF" -> "Defensa"; "DEL" -> "Delantero"; else -> "Medio" }} ${columna + 1}"
                )
            }
        }
}

fun distribuirFormacion(
    jugadores: List<JugadorAlineacion>,
    plantilla: List<Jugador>,
    formato: FormatoPartido,
    formacion: String
): List<JugadorAlineacion> {
    require(formacion in formacionesParaFormato(formato))
    val titulares = jugadores.filter { it.tipo == TipoJugadorAlineacion.TITULAR }.sortedBy { it.orden }
    require(titulares.size <= formato.maximoTitulares)
    val arquero = titulares.firstOrNull { it.posicionCampo == "ARQ" }
        ?: titulares.firstOrNull { a -> plantilla.any { it.id == a.idJugador && it.posicion.equals("portero", true) } }
    val libres = posicionesFormacion(formato, formacion).toMutableList()
    val asignados = (listOfNotNull(arquero) + titulares.filter { it != arquero }).associate { jugador ->
        val preferida = jugador.posicionCampo.substringBefore("_")
        val slot = if (jugador == arquero) libres.first() else
            libres.firstOrNull { it.id != "ARQ" && it.id.startsWith(preferida) }
                ?: libres.firstOrNull { it.id != "ARQ" } ?: libres.first()
        libres.remove(slot)
        jugador.idJugador to jugador.copy(posicionCampo = slot.id, xCampo = null, yCampo = null)
    }
    return jugadores.map { asignados[it.idJugador] ?: it }
}

fun coordenadasValidas(jugador: JugadorAlineacion): Boolean =
    (jugador.xCampo == null && jugador.yCampo == null) ||
        (jugador.xCampo?.let { it.isFinite() && it in 0.0..1.0 } == true &&
         jugador.yCampo?.let { it.isFinite() && it in 0.0..1.0 } == true)
private fun slotsHistoricos(
    formato: FormatoPartido
): List<PosicionFormacion> =
    when (formato) {

        FormatoPartido.ONCE_VS_ONCE ->
            listOf(
                PosicionFormacion(
                    "ARQ",
                    0.50f,
                    0.88f,
                    "Arquero"
                ),

                PosicionFormacion(
                    "DEF_IZQ",
                    0.18f,
                    0.70f,
                    "Defensa izquierdo"
                ),
                PosicionFormacion(
                    "DEF_CEN_IZQ",
                    0.38f,
                    0.72f,
                    "Defensa central"
                ),
                PosicionFormacion(
                    "DEF_CEN_DER",
                    0.62f,
                    0.72f,
                    "Defensa central"
                ),
                PosicionFormacion(
                    "DEF_DER",
                    0.82f,
                    0.70f,
                    "Defensa derecho"
                ),

                PosicionFormacion(
                    "MED_IZQ",
                    0.25f,
                    0.48f,
                    "Mediocampista izquierdo"
                ),
                PosicionFormacion(
                    "MED_CEN",
                    0.50f,
                    0.52f,
                    "Mediocampista central"
                ),
                PosicionFormacion(
                    "MED_DER",
                    0.75f,
                    0.48f,
                    "Mediocampista derecho"
                ),

                PosicionFormacion(
                    "DEL_IZQ",
                    0.25f,
                    0.25f,
                    "Delantero izquierdo"
                ),
                PosicionFormacion(
                    "DEL_CEN",
                    0.50f,
                    0.18f,
                    "Delantero centro"
                ),
                PosicionFormacion(
                    "DEL_DER",
                    0.75f,
                    0.25f,
                    "Delantero derecho"
                )
            )

        FormatoPartido.OCHO_VS_OCHO ->
            listOf(
                PosicionFormacion(
                    "ARQ",
                    0.50f,
                    0.88f,
                    "Arquero"
                ),

                PosicionFormacion(
                    "DEF_IZQ",
                    0.25f,
                    0.70f,
                    "Defensa izquierdo"
                ),
                PosicionFormacion(
                    "DEF_CEN",
                    0.50f,
                    0.68f,
                    "Defensa central"
                ),
                PosicionFormacion(
                    "DEF_DER",
                    0.75f,
                    0.70f,
                    "Defensa derecho"
                ),

                PosicionFormacion(
                    "MED_IZQ",
                    0.32f,
                    0.47f,
                    "Mediocampista izquierdo"
                ),
                PosicionFormacion(
                    "MED_DER",
                    0.68f,
                    0.47f,
                    "Mediocampista derecho"
                ),

                PosicionFormacion(
                    "DEL_IZQ",
                    0.35f,
                    0.23f,
                    "Delantero izquierdo"
                ),
                PosicionFormacion(
                    "DEL_DER",
                    0.65f,
                    0.23f,
                    "Delantero derecho"
                )
            )

        FormatoPartido.SIETE_VS_SIETE ->
            listOf(
                PosicionFormacion(
                    "ARQ",
                    0.50f,
                    0.88f,
                    "Arquero"
                ),

                PosicionFormacion(
                    "DEF_IZQ",
                    0.25f,
                    0.70f,
                    "Defensa izquierdo"
                ),
                PosicionFormacion(
                    "DEF_CEN",
                    0.50f,
                    0.68f,
                    "Defensa central"
                ),
                PosicionFormacion(
                    "DEF_DER",
                    0.75f,
                    0.70f,
                    "Defensa derecho"
                ),

                PosicionFormacion(
                    "MED_IZQ",
                    0.35f,
                    0.45f,
                    "Mediocampista izquierdo"
                ),
                PosicionFormacion(
                    "MED_DER",
                    0.65f,
                    0.45f,
                    "Mediocampista derecho"
                ),

                PosicionFormacion(
                    "DEL_CEN",
                    0.50f,
                    0.20f,
                    "Delantero centro"
                )
            )

        FormatoPartido.CINCO_VS_CINCO ->
            listOf(
                PosicionFormacion(
                    "ARQ",
                    0.50f,
                    0.88f,
                    "Arquero"
                ),

                PosicionFormacion(
                    "DEF_IZQ",
                    0.30f,
                    0.65f,
                    "Defensa izquierdo"
                ),
                PosicionFormacion(
                    "DEF_DER",
                    0.70f,
                    0.65f,
                    "Defensa derecho"
                ),

                PosicionFormacion(
                    "MED_CEN",
                    0.50f,
                    0.42f,
                    "Mediocampista central"
                ),

                PosicionFormacion(
                    "DEL_CEN",
                    0.50f,
                    0.18f,
                    "Delantero centro"
                )
            )

    }