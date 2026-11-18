package com.example.projectsportpro2026_2.ui.resumen

/*
 * Prepara la narración escrita para que suene natural en voz alta.
 *
 * El texto aprobado está pensado para leerse (viñetas, "Min 37'",
 * marcadores "1-0", "gol(es)", títulos en mayúsculas…). Leído tal
 * cual, el motor de voz pronuncia símbolos o junta frases. Aquí se
 * convierte en frases completas, y se separa en oraciones para
 * hablarlas con pausas naturales. No cambia el texto guardado.
 */
object TextoParaVoz {

    // Un párrafo es una lista de oraciones
    fun preparar(texto: String): List<List<String>> =
        texto.split("\n")
            .map { limpiarLinea(it) }
            .filter { it.isNotBlank() }
            .map { linea -> dividirOraciones(linea) }
            .filter { it.isNotEmpty() }

    fun limpiarLinea(original: String): String {
        var t = original.trim()
        if (t.isBlank()) return ""

        // Viñetas y marcas de formato
        t = t.replace(Regex("""^[•\-*·>#]+\s*"""), "")
        t = t.replace(Regex("""[*_#`~|]"""), "")
        // Emojis y otros símbolos gráficos
        t = t.replace(Regex("""[\p{So}\p{Sk}\x{FE0F}\x{200D}]"""), "")

        // Títulos en MAYÚSCULAS ("HECHOS", "LECTURA DEL PARTIDO (interpretación)")
        if (esTitulo(t)) {
            t = t.replace(Regex("""\(.*?\)"""), "").trim()
            t = t.lowercase().replaceFirstChar { it.uppercase() }
        }

        // Minutos: "Min 37'", "min. 37", "37'" → "minuto 37"
        t = t.replace(Regex("""\b[Mm]in\.?\s*(\d{1,3})\s*['’´]?"""), "minuto $1")
        t = t.replace(Regex("""(?<![\w])(\d{1,3})\s*['’´]"""), "minuto $1")
        t = t.replace(Regex("""\bminuto minuto\b"""), "minuto")

        // Marcadores "2-1", "1 – 0" → "2 a 1"
        t = t.replace(Regex("""(\d+)\s*[-–—]\s*(\d+)"""), "$1 a $2")

        // Plurales opcionales: "gol(es)" → "goles", "tarjeta(s)" → "tarjetas"
        t = t.replace(Regex("""(\p{L}+)\((es|s)\)"""), "$1$2")

        // Singular correcto: "se registraron 1 goles" → "se registró un gol"
        t = t.replace(Regex("""\bregistraron 1 goles\b"""), "registró un gol")
        t = t.replace(Regex("""\b1 goles\b"""), "un gol")

        // Abreviaturas frecuentes
        t = t.replace(Regex("""\bvs\.?\b""", RegexOption.IGNORE_CASE), "contra")
        t = t.replace(Regex("""\bDT\b"""), "director técnico")
        t = t.replace(Regex("""\bN[°º]\s*"""), "número ")

        // Separadores "—" o "–" y paréntesis: se leen como pausas
        t = t.replace(Regex("""\s*[—–]\s*"""), ", ")
        t = t.replace(Regex("""\s+-\s+"""), ", ")
        t = t.replace(Regex("""\s*\(\s*"""), ", ")
        t = t.replace(Regex("""\s*\)\s*"""), ", ")
        t = t.replace(Regex("""\s*[:;]\s*"""), ", ")

        // Limpieza de comas y espacios repetidos
        t = t.replace(Regex("""\s+"""), " ")
        t = t.replace(Regex("""(,\s*)+"""), ", ")
        t = t.replace(Regex(""",\s*([.!?])"""), "$1")
        t = t.trim().trim(',').trim()

        // "minuto 37, Gol…" al inicio de una línea → "En el minuto 37, gol…"
        t = t.replace(Regex("""^minuto (\d+), (\p{L})""")) { m ->
            "En el minuto ${m.groupValues[1]}, ${m.groupValues[2].lowercase()}"
        }
        t = t.replaceFirstChar { it.uppercase() }

        // Cada línea termina en punto para que haya pausa
        if (t.isNotEmpty() && t.last() !in ".!?") t += "."
        return t
    }

    private fun esTitulo(t: String): Boolean {
        val sinParentesis = t.replace(Regex("""\(.*?\)"""), "")
        val letras = sinParentesis.filter { it.isLetter() }
        return letras.length >= 4 && letras.all { it.isUpperCase() }
    }

    private fun dividirOraciones(linea: String): List<String> =
        linea.split(Regex("""(?<=[.!?])\s+(?=\p{Lu}|¿|¡|\d)"""))
            .map { it.trim() }
            .filter { it.any { c -> c.isLetterOrDigit() } }
}
