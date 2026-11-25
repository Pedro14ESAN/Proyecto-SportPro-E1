package com.example.projectsportpro2026_2.ui.resumen

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import com.example.projectsportpro2026_2.ui.theme.PaletaClara
import com.example.projectsportpro2026_2.ui.theme.PaletaLineaDeCal

/*
 * HU27 - Narración resaltada.
 *
 * El texto de la narración se guarda plano (así lo lee también el
 * audio), y al mostrarlo se resaltan en negrita las partes clave:
 * minutos ("minuto 45", "45'"), goles, tarjetas amarillas y rojas,
 * expulsiones, penales, sustituciones y los títulos de sección.
 */
object ResaltadoNarracion {

    private val opciones = setOf(RegexOption.IGNORE_CASE)

    // Línea de cal: los colores salen de la paleta (clara u oscura) que pasa la pantalla.
    private class Estilos(p: PaletaLineaDeCal) {
        val minuto = SpanStyle(fontWeight = FontWeight.Bold, color = p.textoPrincipal)
        val gol = SpanStyle(fontWeight = FontWeight.ExtraBold, color = p.verde)
        val amarilla = SpanStyle(
            fontWeight = FontWeight.Bold,
            color = p.amarilloTexto,
            background = p.amarilloFondo // antes 0xFFFFF1C2
        )
        val roja = SpanStyle(
            fontWeight = FontWeight.Bold,
            color = p.errorTexto,
            background = p.errorFondo // antes 0xFFFDE2E1
        )
        val destacado = SpanStyle(fontWeight = FontWeight.Bold, color = p.textoPrincipal)
        val titulo = SpanStyle(fontWeight = FontWeight.ExtraBold, color = p.textoPrincipal)
    }

    // En orden de prioridad: si dos patrones se cruzan gana el primero
    private fun reglas(e: Estilos): List<Pair<Regex, SpanStyle>> = listOf(
        // Títulos de sección de la IA y de la plantilla sin IA
        Regex("^\\s*(HECHOS|LECTURA DEL PARTIDO[^\\n]*|Cronología registrada:)\\s*$",
            setOf(RegexOption.MULTILINE)) to e.titulo,

        Regex("\\btarjetas?\\s+amarillas?\\b|\\bamonestad[oa]s?\\b|\\bamonestaci[oó]n(es)?\\b", opciones)
                to e.amarilla,
        Regex("\\btarjetas?\\s+rojas?\\b|\\bexpulsad[oa]s?\\b|\\bexpulsi[oó]n(es)?\\b|\\bdoble\\s+amarilla\\b", opciones)
                to e.roja,

        Regex("\\b(auto)?gol(es|azo)?\\b", opciones) to e.gol,

        // "minuto 45", "minutos 10 y 20", "min 45'", "45'", "45+2'"
        Regex("\\bminutos?\\s+\\d{1,3}(\\s*\\+\\s*\\d{1,2})?'?|\\bmin\\.?\\s*\\d{1,3}(\\s*\\+\\s*\\d{1,2})?'?", opciones)
                to e.minuto,
        Regex("\\b\\d{1,3}(\\s*\\+\\s*\\d{1,2})?\\s*'") to e.minuto,

        Regex("\\bpenal(es)?\\b|\\bpenaltis?\\b|\\bsustituci[oó]n(es)?\\b|\\bcambios?\\b", opciones)
                to e.destacado
    )

    fun resaltar(texto: String, paleta: PaletaLineaDeCal = PaletaClara): AnnotatedString {
        val e = Estilos(paleta)
        // Se toman todas las coincidencias y se descartan las que se cruzan
        // con una de mayor prioridad (por ejemplo "gol" dentro de un título)
        val elegidas = mutableListOf<Pair<IntRange, SpanStyle>>()
        reglas(e).forEach { (regex, estilo) ->
            regex.findAll(texto).forEach { m ->
                val rango = m.groups[1]?.takeIf { estilo == e.titulo }?.range ?: m.range
                if (rango.isEmpty()) return@forEach
                val cruza = elegidas.any { (r, _) -> rango.first <= r.last && r.first <= rango.last }
                if (!cruza) elegidas += rango to estilo
            }
        }

        return buildAnnotatedString {
            append(texto)
            elegidas.forEach { (rango, estilo) ->
                addStyle(estilo, rango.first, rango.last + 1)
            }
        }
    }
}
