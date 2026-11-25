package com.example.projectsportpro2026_2.ui.estadisticas

import com.example.projectsportpro2026_2.ui.theme.EstiloMarcador
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import com.example.projectsportpro2026_2.ui.comun.anchoCompleto
import com.example.projectsportpro2026_2.ui.comun.CabeceraCancha
import com.example.projectsportpro2026_2.ui.theme.FamiliaEtiquetas
import com.example.projectsportpro2026_2.ui.theme.FamiliaTitulos
import com.example.projectsportpro2026_2.ui.theme.LineaDeCal
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectsportpro2026_2.ui.partidos.FormaTarjetaSportPro
import com.example.projectsportpro2026_2.ui.partidos.SportProBorde
import com.example.projectsportpro2026_2.ui.partidos.SportProNavy
import com.example.projectsportpro2026_2.ui.partidos.SportProRojo
import com.example.projectsportpro2026_2.ui.partidos.SportProSuperficie
import com.example.projectsportpro2026_2.ui.partidos.SportProTextoSecundario
import com.example.projectsportpro2026_2.ui.partidos.SportProVerdeClaro

// Piezas visuales de HU18 y HU26/HU27 (Pedro), alineadas al Figma:
// overline verde monoespaciado, títulos serif, tarjetas blancas con borde suave.

val VerdeFigma: Color @Composable get() = LineaDeCal.colores.verde // antes Color(0xFF1E9B5F)
val AmarilloAviso: Color @Composable get() = LineaDeCal.colores.amarilloTexto // antes Color(0xFF8A6100)
val AmarilloAvisoFondo: Color @Composable get() = LineaDeCal.colores.amarilloFondo // antes Color(0xFFFFF6D9)
val AmarilloAvisoBorde: Color @Composable get() = LineaDeCal.colores.amarilloBorde // antes Color(0xFFF1D98A)

@Composable
fun EncabezadoSeccion(
    textoVolver: String,
    onVolver: () -> Unit,
    overline: String,
    titulo: String,
    subtitulo: String? = null
) {
    // Línea de cal: cabecera de césped a todo el ancho (las pantallas que la usan tienen 20 dp de margen).
    // Mismos textos: etiqueta, título, subtítulo y el botón de volver ("textoVolver" como descripción).
    CabeceraCancha(
        titulo = titulo,
        subtitulo = subtitulo?.takeIf { it.isNotBlank() },
        etiqueta = overline,
        onVolver = onVolver,
        descripcionVolver = textoVolver,
        modifier = Modifier.anchoCompleto(20.dp)
    )
}

// Tarjeta blanca con borde suave (como en el Figma)
@Composable
fun TarjetaBlanca(
    modifier: Modifier = Modifier,
    contenido: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = FormaTarjetaSportPro,
        color = SportProSuperficie,
        border = BorderStroke(1.dp, SportProBorde)
    ) {
        Column(Modifier.padding(16.dp)) { contenido() }
    }
}

/*
 * Indicador numérico. Si valor es null se muestra "Sin datos"
 * (excepción HU18: no mostrar 0 cuando confunda).
 */
@Composable
fun RowScope.TarjetaMetrica(
    titulo: String,
    valor: String?,
    detalle: String? = null,
    icono: ImageVector? = null,
    colorValor: Color = SportProNavy
) {
    Surface(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight(),
        shape = RoundedCornerShape(16.dp),
        color = SportProSuperficie,
        border = BorderStroke(1.dp, SportProBorde)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icono != null) {
                    Icon(icono, contentDescription = null, tint = SportProTextoSecundario, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                }
                Text(
                    text = titulo.uppercase(),
                    color = SportProTextoSecundario,
                    fontFamily = FamiliaEtiquetas,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    maxLines = 2
                )
            }
            if (valor == null) {
                Text("Sin datos", color = SportProTextoSecundario, fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold)
            } else {
                // Línea de cal: contadores en Doto (números de 18 sp o más)
                Text(
                    text = valor,
                    color = colorValor,
                    style = EstiloMarcador.copy(fontSize = 28.sp, lineHeight = 30.sp)
                )
            }
            if (!detalle.isNullOrBlank()) {
                Text(detalle, color = SportProTextoSecundario, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun FilaMetricas(contenido: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(androidx.compose.foundation.layout.IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        content = contenido
    )
}

// Barra de proporción (0f..1f)
@Composable
fun BarraProporcion(
    fraccion: Float,
    color: Color = VerdeFigma,
    fondo: Color = SportProVerdeClaro
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(fondo)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraccion.coerceIn(0f, 1f))
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )
    }
}

// Barra dividida a favor / en contra
@Composable
fun BarraBalance(favor: Int, contra: Int) {
    val total = favor + contra
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(5.dp))
    ) {
        if (total == 0) {
            Box(Modifier.fillMaxWidth().height(10.dp).background(SportProBorde))
        } else {
            if (favor > 0) {
                Box(Modifier.weight(favor.toFloat()).height(10.dp).background(VerdeFigma))
            }
            if (contra > 0) {
                Box(Modifier.weight(contra.toFloat()).height(10.dp).background(SportProRojo))
            }
        }
    }
}

// Aviso amarillo (borrador, datos protegidos, desactualizado…)
@Composable
fun AvisoAmarillo(titulo: String, texto: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = AmarilloAvisoFondo,
        border = BorderStroke(1.dp, AmarilloAvisoBorde)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Rounded.Lock, contentDescription = null, tint = AmarilloAviso, modifier = Modifier.size(18.dp))
            Column {
                Text(titulo, color = AmarilloAviso, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(texto, color = AmarilloAviso, fontSize = 12.sp)
            }
        }
    }
}

// Pie de pantalla "SOLO LECTURA · …"
@Composable
fun PieSoloLectura(texto: String) {
    Text(
        text = texto,
        color = SportProTextoSecundario,
        fontFamily = FamiliaEtiquetas,
        fontSize = 12.sp,
        letterSpacing = 1.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    )
}

// Pestañas tipo "pill" (Equipo / Jugador / Periodo)
@Composable
fun PestanasPill(
    opciones: List<String>,
    seleccionada: Int,
    onSeleccionar: (Int) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = SportProVerdeClaro,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(4.dp)) {
            opciones.forEachIndexed { i, texto ->
                val activa = i == seleccionada
                Surface(
                    onClick = { onSeleccionar(i) },
                    shape = RoundedCornerShape(10.dp),
                    color = if (activa) SportProSuperficie else Color.Transparent,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = texto,
                        color = if (activa) SportProNavy else SportProTextoSecundario,
                        fontWeight = if (activa) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun Separador() = Spacer(Modifier.width(4.dp))
