package com.example.projectsportpro2026_2.ui.sincronizacion

import androidx.compose.ui.graphics.Color
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/*
 * Colores de estado tomados del diseño en Figma
 * (HU16 sincronización y HU17 vista en vivo).
 */
val SyncVerde = Color(0xFF1E9E5A)
val SyncVerdeTexto = Color(0xFF13693C)
val SyncVerdeFondo = Color(0xFFE8F7EE)
val SyncVerdeBorde = Color(0xFFA6DFBF)

val SyncNaranja = Color(0xFFF08A24)
val SyncNaranjaTexto = Color(0xFFB4530A)
val SyncNaranjaFondo = Color(0xFFFFF4E6)
val SyncNaranjaBorde = Color(0xFFF7C78E)

val SyncRojo = Color(0xFFE5484D)
val SyncRojoTexto = Color(0xFFB42318)
val SyncRojoFondo = Color(0xFFFDECEC)
val SyncRojoBorde = Color(0xFFF5B5B5)

// Cabecera oscura de la vista en vivo (HU17)
val VivoFondoOscuro = Color(0xFF0E2A33)
val VivoFondoOscuroSuave = Color(0xFF173A45)
val VivoVerdeBrillante = Color(0xFF4ADE80)
val VivoTextoTenue = Color(0xFF6E8E98)

private val localePeru: Locale =
    Locale.forLanguageTag("es-PE")

/*
 * "02:32:38 p. m."
 */
fun horaConSegundos(milisegundos: Long): String =
    if (milisegundos <= 0L) {
        "--:--:--"
    } else {
        SimpleDateFormat("hh:mm:ss a", localePeru)
            .format(Date(milisegundos))
    }

/*
 * Símbolo de cada tipo de evento (mismo criterio que
 * la botonera de registro rápido de HU14).
 */
fun simboloEvento(
    codigo: String,
    nombre: String = "",
    observacion: String = ""
): String =
    when (codigo) {
        "INICIO" -> "▶"
        "FIN" -> "■"
        "SAQUE_META" -> "↑"
        "SAQUE_LATERAL" -> "↔"
        "TIRO_ESQUINA" -> "⌒"
        "FALTA" -> "⚑"
        "PENAL" -> "◎"
        "GOL" -> "⚽"
        "TARJETA" ->
            if (
                nombre.contains("roja", ignoreCase = true) ||
                observacion.contains("roja", ignoreCase = true)
            ) {
                "🟥"
            } else {
                "🟨"
            }
        "FUERA_JUEGO" -> "⊘"
        "CAMBIO" -> "🔄"
        else -> "•"
    }
