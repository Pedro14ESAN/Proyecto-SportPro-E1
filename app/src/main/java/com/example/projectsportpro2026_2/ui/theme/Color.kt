package com.example.projectsportpro2026_2.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/*
 * Rediseño "Línea de cal" · tokens de color (ESPECIFICACION.md, sección 3).
 *
 * Hay dos paletas (clara y oscura) con los mismos campos. La activa se
 * elige en ProjectSportPro2026_2Theme según isSystemInDarkTheme() y se
 * lee con LineaDeCal.colores desde cualquier @Composable.
 *
 * Los tokens "fijos" (navy, lima, amarillo, naranja, rojo, ledFondo)
 * valen lo mismo en los dos modos, como pide la sección 3.2.
 * Los semánticos (fondo, superficie, texto…) cambian con el modo.
 */

// ---------- 3.1 · Valores base (modo claro) ----------
val TokenNavy = Color(0xFF0F2438)
val TokenNavy2 = Color(0xFF16354D)
val TokenVerde = Color(0xFF1B9A5C)
val TokenVerdeCesped = Color(0xFF178A52)
val TokenLima = Color(0xFFA8F06A)
val TokenCrema = Color(0xFFF7F7F2)
val TokenBlanco = Color(0xFFFFFFFF)
val TokenGrisTexto = Color(0xFF5B6B73)
val TokenAmarillo = Color(0xFFF2C94C)
val TokenRojo = Color(0xFFD9534F)
val TokenNaranja = Color(0xFFE8913A)
val TokenLedFondo = Color(0xFF0B1A28)

// ---------- 3.2 · Valores del modo oscuro ----------
val TokenOscuroFondo = Color(0xFF0D1E2D)
val TokenOscuroSuperficie = Color(0xFF16354D)
val TokenOscuroBorde = Color(0xFF2B4D66)
val TokenOscuroCesped = Color(0xFF146B42)
val TokenOscuroCespedAlterno = Color(0xFF115C39)
val TokenOscuroTexto = Color(0xFFE8EEF2)
val TokenOscuroTexto2 = Color(0xFF9FB3C1)

@Immutable
data class PaletaLineaDeCal(
    val esOscura: Boolean,

    // Fijos en los dos modos
    val navy: Color,
    val navy2: Color,
    val verde: Color,
    val lima: Color,
    val amarillo: Color,
    val rojo: Color,
    val naranja: Color,
    val ledFondo: Color,

    // Semánticos
    val fondo: Color,              // fondo de pantalla (crema)
    val superficie: Color,         // tarjetas, campos, hojas
    val superficieOscura: Color,   // botones oscuros, pizarra, cabeceras navy (texto claro encima)
    val borde: Color,
    val textoPrincipal: Color,
    val textoSecundario: Color,
    val cesped: Color,
    val cespedAlterno: Color,

    // Chips de estado: fondo / texto
    val okFondo: Color,
    val okTexto: Color,
    val pendienteFondo: Color,
    val pendienteTexto: Color,
    val errorFondo: Color,
    val errorTexto: Color,
    val apagadoFondo: Color,
    val apagadoTexto: Color,
    val amarilloFondo: Color,
    val amarilloTexto: Color,
    val neutroFondo: Color,
    val neutroTexto: Color,

    // Etiquetas de visibilidad que ya existían (Comunidad SportPro y Academia, img 10)
    val azulFondo: Color,
    val azulTexto: Color,
    val lilaFondo: Color,
    val lilaTexto: Color,

    // Bordes suaves de los avisos de sincronización y estadísticas
    val okBorde: Color,
    val pendienteBorde: Color,
    val errorBorde: Color,
    val amarilloBorde: Color
)

val PaletaClara = PaletaLineaDeCal(
    esOscura = false,
    navy = TokenNavy,
    navy2 = TokenNavy2,
    verde = TokenVerde,
    lima = TokenLima,
    amarillo = TokenAmarillo,
    rojo = TokenRojo,
    naranja = TokenNaranja,
    ledFondo = TokenLedFondo,
    fondo = TokenCrema,
    superficie = TokenBlanco,
    superficieOscura = TokenNavy,
    borde = Color(0xFFD9DEDF),
    textoPrincipal = TokenNavy,
    textoSecundario = TokenGrisTexto,
    cesped = TokenVerde,
    cespedAlterno = TokenVerdeCesped,
    okFondo = Color(0xFFE1F3E8),
    okTexto = Color(0xFF16794A),
    pendienteFondo = Color(0xFFFDE9D4),
    pendienteTexto = Color(0xFFA55A12),
    errorFondo = Color(0xFFFBE3E2),
    errorTexto = Color(0xFFB23A36),
    apagadoFondo = Color(0xFFECEEEA),
    apagadoTexto = Color(0xFF6B7A80),
    amarilloFondo = Color(0xFFFFF3CC),
    amarilloTexto = Color(0xFF8A6A00),
    neutroFondo = Color(0xFFE3EAF0),
    neutroTexto = TokenNavy,
    azulFondo = Color(0xFFE8EEFF),
    azulTexto = Color(0xFF3550A0),
    lilaFondo = Color(0xFFF1ECFA),
    lilaTexto = Color(0xFF6B4FA0),
    okBorde = Color(0xFFA6DFBF),
    pendienteBorde = Color(0xFFF7C78E),
    errorBorde = Color(0xFFF5B5B5),
    amarilloBorde = Color(0xFFF1D98A)
)

val PaletaOscura = PaletaLineaDeCal(
    esOscura = true,
    navy = TokenNavy,
    navy2 = TokenNavy2,
    verde = TokenVerde,
    lima = TokenLima,
    amarillo = TokenAmarillo,
    rojo = TokenRojo,
    naranja = TokenNaranja,
    ledFondo = TokenLedFondo,
    fondo = TokenOscuroFondo,
    superficie = TokenOscuroSuperficie,
    superficieOscura = TokenOscuroBorde,
    borde = TokenOscuroBorde,
    textoPrincipal = TokenOscuroTexto,
    textoSecundario = TokenOscuroTexto2,
    cesped = TokenOscuroCesped,
    cespedAlterno = TokenOscuroCespedAlterno,
    okFondo = Color(0xFF1E4A3A),
    okTexto = TokenLima,
    pendienteFondo = Color(0xFF4A3420),
    pendienteTexto = Color(0xFFFFC58A),
    // Derivados (la espec solo fija OK y pendiente en oscuro): mismo criterio,
    // fondo oscuro del tono y texto claro del mismo tono.
    errorFondo = Color(0xFF4A2426),
    errorTexto = Color(0xFFFFB3AE),
    apagadoFondo = Color(0xFF1F3A50),
    apagadoTexto = TokenOscuroTexto2,
    amarilloFondo = Color(0xFF4A3F1A),
    amarilloTexto = TokenAmarillo,
    neutroFondo = Color(0xFF1F3A50),
    neutroTexto = TokenOscuroTexto,
    azulFondo = Color(0xFF22335E),
    azulTexto = Color(0xFFB9C8FF),
    lilaFondo = Color(0xFF3A2D55),
    lilaTexto = Color(0xFFD7C6F5),
    okBorde = Color(0xFF2F6B52),
    pendienteBorde = Color(0xFF7A5530),
    errorBorde = Color(0xFF7A3A3A),
    amarilloBorde = Color(0xFF6B5A22)
)

val LocalPaletaLineaDeCal = staticCompositionLocalOf { PaletaClara }

object LineaDeCal {
    val colores: PaletaLineaDeCal
        @Composable
        @ReadOnlyComposable
        get() = LocalPaletaLineaDeCal.current
}
