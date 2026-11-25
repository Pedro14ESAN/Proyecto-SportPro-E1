package com.example.projectsportpro2026_2.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.projectsportpro2026_2.R

/*
 * Rediseño "Línea de cal" · tipografías (ESPECIFICACION.md, sección 3.3).
 * Fuentes OFL en res/font (ver docs/diseno/LICENCIA_FUENTES.md).
 *
 * - Títulos: Bricolage Grotesque.
 * - Marcador y números: Doto. SOLO números grandes (18 sp o más).
 * - Cuerpo: Instrument Sans (es la fuente por defecto de todo Text).
 * - Etiquetas: DM Mono, en MAYÚSCULAS con espaciado.
 *
 * Ningún estilo baja de 12 sp.
 */

val FamiliaTitulos = FontFamily(
    Font(R.font.bricolage_medium, FontWeight.Medium),
    Font(R.font.bricolage_bold, FontWeight.Bold),
    Font(R.font.bricolage_extrabold, FontWeight.ExtraBold)
)

val FamiliaMarcador = FontFamily(
    Font(R.font.doto_bold, FontWeight.Bold),
    Font(R.font.doto_black, FontWeight.Black)
)

val FamiliaCuerpo = FontFamily(
    Font(R.font.instrument_sans_regular, FontWeight.Normal),
    Font(R.font.instrument_sans_medium, FontWeight.Medium),
    Font(R.font.instrument_sans_semibold, FontWeight.SemiBold),
    Font(R.font.instrument_sans_bold, FontWeight.Bold)
)

val FamiliaEtiquetas = FontFamily(
    Font(R.font.dm_mono_regular, FontWeight.Normal),
    Font(R.font.dm_mono_medium, FontWeight.Medium)
)

/** Números del marcador LED, minutos y contadores (mínimo 18 sp). */
val EstiloMarcador = TextStyle(
    fontFamily = FamiliaMarcador,
    fontWeight = FontWeight.Black,
    fontSize = 32.sp,
    lineHeight = 34.sp
)

/** Etiqueta mono en mayúsculas ("MINUTO", "EN VIVO"). */
val EstiloEtiqueta = TextStyle(
    fontFamily = FamiliaEtiquetas,
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 1.2.sp
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = FamiliaTitulos, fontWeight = FontWeight.ExtraBold,
        fontSize = 52.sp, lineHeight = 56.sp, letterSpacing = (-1).sp
    ),
    displayMedium = TextStyle(
        fontFamily = FamiliaTitulos, fontWeight = FontWeight.ExtraBold,
        fontSize = 42.sp, lineHeight = 46.sp, letterSpacing = (-0.8).sp
    ),
    displaySmall = TextStyle(
        fontFamily = FamiliaTitulos, fontWeight = FontWeight.ExtraBold,
        fontSize = 34.sp, lineHeight = 38.sp, letterSpacing = (-0.5).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FamiliaTitulos, fontWeight = FontWeight.ExtraBold,
        fontSize = 30.sp, lineHeight = 34.sp, letterSpacing = (-0.4).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FamiliaTitulos, fontWeight = FontWeight.ExtraBold,
        fontSize = 26.sp, lineHeight = 30.sp, letterSpacing = (-0.3).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FamiliaTitulos, fontWeight = FontWeight.Bold,
        fontSize = 22.sp, lineHeight = 28.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FamiliaTitulos, fontWeight = FontWeight.Bold,
        fontSize = 20.sp, lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FamiliaCuerpo, fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp, lineHeight = 22.sp, letterSpacing = 0.1.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FamiliaCuerpo, fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FamiliaCuerpo, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.2.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FamiliaCuerpo, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.2.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FamiliaCuerpo, fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.2.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FamiliaCuerpo, fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FamiliaEtiquetas, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 1.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FamiliaEtiquetas, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 1.sp
    )
)
