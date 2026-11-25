package com.example.projectsportpro2026_2.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

/*
 * Rediseño "Línea de cal" · tema (ESPECIFICACION.md, sección 4).
 *
 * dynamicColor = false: la app nunca toma colores del fondo de pantalla
 * del celular. Los componentes de Material (OutlinedTextField,
 * DatePickerDialog, TimePicker, FilterChip, Switch, Checkbox, RadioButton,
 * AlertDialog, menús, hojas y CircularProgressIndicator) toman sus colores
 * de estos esquemas:
 *  - primary (verde): indicador de progreso, Switch y Checkbox marcados,
 *    borde enfocado de los campos, día elegido del calendario.
 *  - secondaryContainer (lima / verde oscuro): FilterChip seleccionado.
 *  - surfaceContainerHigh: AlertDialog, DatePicker y TimePicker.
 *  - surfaceContainer: menús desplegables.
 *  - surfaceContainerLow: ModalBottomSheet y Card.
 *  - surfaceContainerHighest: pista del Switch apagado, campos rellenos.
 */

private val EsquemaClaro: ColorScheme = lightColorScheme(
    primary = TokenVerde,
    onPrimary = Color.White,
    primaryContainer = PaletaClara.okFondo,
    onPrimaryContainer = PaletaClara.okTexto,
    inversePrimary = TokenLima,
    secondary = TokenNavy,
    onSecondary = Color.White,
    secondaryContainer = TokenLima,
    onSecondaryContainer = TokenNavy,
    tertiary = TokenAmarillo,
    onTertiary = TokenNavy,
    tertiaryContainer = PaletaClara.amarilloFondo,
    onTertiaryContainer = PaletaClara.amarilloTexto,
    background = TokenCrema,
    onBackground = TokenNavy,
    surface = TokenCrema,
    onSurface = TokenNavy,
    surfaceVariant = Color(0xFFECEEEA),
    onSurfaceVariant = TokenGrisTexto,
    surfaceTint = TokenVerde,
    inverseSurface = TokenNavy,
    inverseOnSurface = Color.White,
    error = TokenRojo,
    onError = Color.White,
    errorContainer = PaletaClara.errorFondo,
    onErrorContainer = PaletaClara.errorTexto,
    outline = Color(0xFFB9C4C2),
    outlineVariant = PaletaClara.borde,
    scrim = Color.Black,
    surfaceBright = TokenBlanco,
    surfaceDim = Color(0xFFE6E8E3),
    surfaceContainerLowest = TokenBlanco,
    surfaceContainerLow = TokenBlanco,
    surfaceContainer = TokenBlanco,
    surfaceContainerHigh = TokenBlanco,
    surfaceContainerHighest = Color(0xFFECEEEA)
)

private val EsquemaOscuro: ColorScheme = darkColorScheme(
    primary = TokenVerde,
    onPrimary = Color.White,
    primaryContainer = PaletaOscura.okFondo,
    onPrimaryContainer = PaletaOscura.okTexto,
    inversePrimary = TokenVerde,
    secondary = TokenLima,
    onSecondary = TokenNavy,
    secondaryContainer = PaletaOscura.okFondo,
    onSecondaryContainer = TokenLima,
    tertiary = TokenAmarillo,
    onTertiary = TokenNavy,
    tertiaryContainer = PaletaOscura.amarilloFondo,
    onTertiaryContainer = PaletaOscura.amarilloTexto,
    background = TokenOscuroFondo,
    onBackground = TokenOscuroTexto,
    surface = TokenOscuroFondo,
    onSurface = TokenOscuroTexto,
    surfaceVariant = Color(0xFF1F4260),
    onSurfaceVariant = TokenOscuroTexto2,
    surfaceTint = TokenVerde,
    inverseSurface = TokenOscuroTexto,
    inverseOnSurface = TokenOscuroFondo,
    error = TokenRojo,
    onError = Color.White,
    errorContainer = PaletaOscura.errorFondo,
    onErrorContainer = PaletaOscura.errorTexto,
    outline = Color(0xFF557A94),
    outlineVariant = TokenOscuroBorde,
    scrim = Color.Black,
    surfaceBright = Color(0xFF1F4260),
    surfaceDim = TokenOscuroFondo,
    surfaceContainerLowest = TokenOscuroFondo,
    surfaceContainerLow = TokenOscuroSuperficie,
    surfaceContainer = TokenOscuroSuperficie,
    surfaceContainerHigh = TokenOscuroSuperficie,
    surfaceContainerHighest = Color(0xFF1F4260)
)

@Composable
fun ProjectSportPro2026_2Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Se conserva el parámetro para no romper llamadas existentes, pero el
    // rediseño no usa colores dinámicos (sección 4 de la especificación).
    @Suppress("UNUSED_PARAMETER") dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val paleta = if (darkTheme) PaletaOscura else PaletaClara
    val esquema = if (darkTheme) EsquemaOscuro else EsquemaClaro

    CompositionLocalProvider(LocalPaletaLineaDeCal provides paleta) {
        MaterialTheme(
            colorScheme = esquema,
            typography = Typography,
            content = content
        )
    }
}
