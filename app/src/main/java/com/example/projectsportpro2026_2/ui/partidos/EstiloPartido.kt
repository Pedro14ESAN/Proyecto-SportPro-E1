package com.example.projectsportpro2026_2.ui.partidos

import com.example.projectsportpro2026_2.ui.theme.LineaDeCal
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Colores principales basados en el diseño de SportPro
val SportProNavy: Color @Composable get() = LineaDeCal.colores.textoPrincipal // antes Color(0xFF123047)
val SportProNavySuave: Color @Composable get() = LineaDeCal.colores.navy2 // antes Color(0xFF1C4059)

// Navy usado como FONDO (botones oscuros, cabeceras, chip seleccionado) con texto claro encima.
// SportProNavy es color de TEXTO y en modo oscuro se vuelve claro; por eso los fondos usan este.
val SportProNavyFondo: Color @Composable get() = LineaDeCal.colores.superficieOscura

val SportProVerde: Color @Composable get() = LineaDeCal.colores.verde // antes Color(0xFF2F7D4A)
val SportProVerdeClaro: Color @Composable get() = LineaDeCal.colores.okFondo // antes Color(0xFFE3F0E7)

val SportProFondo: Color @Composable get() = LineaDeCal.colores.fondo // antes Color(0xFFF7F7F2)
val SportProSuperficie: Color @Composable get() = LineaDeCal.colores.superficie // antes Color(0xFFFFFFFF)

val SportProTextoPrincipal: Color @Composable get() = LineaDeCal.colores.textoPrincipal // antes Color(0xFF17212B)
val SportProTextoSecundario: Color @Composable get() = LineaDeCal.colores.textoSecundario // antes Color(0xFF66737E)

val SportProBorde: Color @Composable get() = LineaDeCal.colores.borde // antes Color(0xFFD9DEDF)

val SportProRojo: Color @Composable get() = LineaDeCal.colores.rojo // antes Color(0xFFB3261E)
val SportProRojoClaro: Color @Composable get() = LineaDeCal.colores.errorFondo // antes Color(0xFFFCE8E6)

// Formas reutilizables
val FormaTarjetaSportPro = RoundedCornerShape(18.dp)
val FormaCampoSportPro = RoundedCornerShape(12.dp)
val FormaBotonSportPro = RoundedCornerShape(12.dp)