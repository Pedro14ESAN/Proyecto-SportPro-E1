package com.example.projectsportpro2026_2.ui.sincronizacion

import com.example.projectsportpro2026_2.ui.theme.LineaDeCal
import com.example.projectsportpro2026_2.ui.theme.TokenOscuroTexto2
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.projectsportpro2026_2.datos.modelo.evento.TipoTarjeta

/*
 * Colores de estado tomados del diseño en Figma
 * (HU16 sincronización y HU17 vista en vivo).
 */
val SyncVerde: Color @Composable get() = LineaDeCal.colores.verde // antes Color(0xFF1E9E5A)
val SyncVerdeTexto: Color @Composable get() = LineaDeCal.colores.okTexto // antes Color(0xFF13693C)
val SyncVerdeFondo: Color @Composable get() = LineaDeCal.colores.okFondo // antes Color(0xFFE8F7EE)
val SyncVerdeBorde: Color @Composable get() = LineaDeCal.colores.okBorde // antes Color(0xFFA6DFBF)

val SyncNaranja: Color @Composable get() = LineaDeCal.colores.naranja // antes Color(0xFFF08A24)
val SyncNaranjaTexto: Color @Composable get() = LineaDeCal.colores.pendienteTexto // antes Color(0xFFB4530A)
val SyncNaranjaFondo: Color @Composable get() = LineaDeCal.colores.pendienteFondo // antes Color(0xFFFFF4E6)
val SyncNaranjaBorde: Color @Composable get() = LineaDeCal.colores.pendienteBorde // antes Color(0xFFF7C78E)

val SyncRojo: Color @Composable get() = LineaDeCal.colores.rojo // antes Color(0xFFE5484D)
val SyncRojoTexto: Color @Composable get() = LineaDeCal.colores.errorTexto // antes Color(0xFFB42318)
val SyncRojoFondo: Color @Composable get() = LineaDeCal.colores.errorFondo // antes Color(0xFFFDECEC)
val SyncRojoBorde: Color @Composable get() = LineaDeCal.colores.errorBorde // antes Color(0xFFF5B5B5)

// Cabecera oscura de la vista en vivo (HU17)
val VivoFondoOscuro: Color @Composable get() = if (LineaDeCal.colores.esOscura) LineaDeCal.colores.ledFondo else LineaDeCal.colores.navy // antes Color(0xFF0E2A33)
val VivoFondoOscuroSuave: Color @Composable get() = LineaDeCal.colores.navy2 // antes Color(0xFF173A45)
val VivoVerdeBrillante: Color @Composable get() = LineaDeCal.colores.lima // antes Color(0xFF4ADE80)
val VivoTextoTenue: Color @Composable get() = TokenOscuroTexto2 // antes Color(0xFF6E8E98) · siempre sobre fondo oscuro

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
    tipoTarjeta: TipoTarjeta =
        TipoTarjeta.NINGUNA
): String =
    when (codigo) {

        "INICIO" -> "▶️"
        "FIN" -> "⏹️"
        "SAQUE_META" -> "🥅"
        "SAQUE_LATERAL" -> "↔️"
        "TIRO_ESQUINA" -> "🚩"
        "FALTA" -> "⚠️"
        "PENAL" -> "🎯"
        "GOL" -> "⚽"

        "TARJETA" ->
            if (tipoTarjeta == TipoTarjeta.ROJA) "🟥" else "🟨"

        "FUERA_JUEGO" -> "🚩"
        "CAMBIO" -> "🔄"

        else -> "📋"
    }

// Línea de cal: @Composable porque los colores salen del tema (claro u oscuro).
@Composable
fun colorSimboloEvento(
    codigo: String,
    tipoTarjeta: TipoTarjeta =
        TipoTarjeta.NINGUNA
): Color =
    when {

        codigo == "TARJETA" &&
                tipoTarjeta ==
                TipoTarjeta.AMARILLA ->
            LineaDeCal.colores.amarillo // antes Color(0xFFE0B000)

        codigo == "TARJETA" &&
                tipoTarjeta ==
                TipoTarjeta.ROJA ->
            LineaDeCal.colores.rojo // antes Color(0xFFC62828)

        codigo == "TARJETA" ->
            LineaDeCal.colores.amarilloTexto // antes Color(0xFF9A7200)

        codigo == "GOL" ->
            SyncVerde

        codigo == "FALTA" ->
            SyncRojo

        else ->
            LineaDeCal.colores.textoPrincipal // antes Color(0xFF123047)
    }