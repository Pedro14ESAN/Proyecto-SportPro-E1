package com.example.projectsportpro2026_2.ui.resumen

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import com.example.projectsportpro2026_2.ui.estadisticas.AmarilloAviso
import com.example.projectsportpro2026_2.ui.partidos.SportProNavy
import com.example.projectsportpro2026_2.ui.partidos.SportProRojo
import com.example.projectsportpro2026_2.ui.partidos.SportProVerde

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

    private val estiloMinuto = SpanStyle(fontWeight = FontWeight.Bold, color = SportProNavy)
    private val estiloGol = SpanStyle(fontWeight = FontWeight.ExtraBold, color = SportProVerde)
    private val estiloAmarilla = SpanStyle(
        fontWeight = FontWeight.Bold,
        color = AmarilloAviso,
        background = Color(0xFFFFF1C2)
    )
    private val estiloRoja = SpanStyle(
        fontWeight = FontWeight.Bold,
        color = SportProRojo,
        background = Color(0xFFFDE2E1)
    )
    private val estiloDestacado = SpanStyle(fontWeight = FontWeight.Bold, color = SportProNavy)
    private val estiloTitulo = SpanStyle(fontWeight = FontWeight.ExtraBold, color = SportProNavy)

    // En orden de prioridad: si dos patrones se cruzan gana el primero
    private val reglas: List<Pair<Regex, SpanStyle>> = listOf(
        // Títulos de sección de la IA y de la plantilla sin IA
        Regex("^\\s*(HECHOS|LECTURA DEL PARTIDO[^\\n]*|Cronología registrada:)\\s*$",
            setOf(RegexOption.MULTILINE)) to estiloTitulo,

        Regex("\\btarjetas?\\s+amarillas?\\b|\\bamonestad[oa]s?\\b|\\bamonestaci[oó]n(es)?\\b", opciones)
                to estiloAmarilla,
        Regex("\\btarjetas?\\s+rojas?\\b|\\bexpulsad[oa]s?\\b|\\bexpulsi[oó]n(es)?\\b|\\bdoble\\s+amarilla\\b", opciones)
                to estiloRoja,

        Regex("\\b(auto)?gol(es|azo)?\\b", opciones) to estiloGol,

        // "minuto 45", "minutos 10 y 20", "min 45'", "45'", "45+2'"
        Regex("\\bminutos?\\s+\\d{1,3}(\\s*\\+\\s*\\d{1,2})?'?|\\bmin\\.?\\s*\\d{1,3}(\\s*\\+\\s*\\d{1,2})?'?", opciones)
                to estiloMinuto,
        Regex("\\b\\d{1,3}(\\s*\\+\\s*\\d{1,2})?\\s*'") to estiloMinuto,

        Regex("\\bpenal(es)?\\b|\\bpenaltis?\\b|\\bsustituci[oó]n(es)?\\b|\\bcambios?\\b", opciones)
                to estiloDestacado
    )

    fun resaltar(texto: String): AnnotatedString {
        // Se toman todas las coincidencias y se descartan las que se cruzan
        // con una de mayor prioridad (por ejemplo "gol" dentro de un título)
        val elegidas = mutableListOf<Pair<IntRange, SpanStyle>>()
        reglas.forEach { (regex, estilo) ->
            regex.findAll(texto).forEach { m ->
                val rango = m.groups[1]?.takeIf { estilo == estiloTitulo }?.range ?: m.range
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
