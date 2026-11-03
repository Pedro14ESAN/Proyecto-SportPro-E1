package com.example.projectsportpro2026_2.ui.comun

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectsportpro2026_2.datos.modelo.Rol
import com.example.projectsportpro2026_2.ui.partidos.FormaCampoSportPro
import com.example.projectsportpro2026_2.ui.partidos.SportProNavy
import com.example.projectsportpro2026_2.ui.partidos.SportProRojo
import com.example.projectsportpro2026_2.ui.partidos.SportProRojoClaro
import com.example.projectsportpro2026_2.ui.partidos.SportProTextoSecundario
import com.example.projectsportpro2026_2.ui.partidos.SportProVerde
import com.example.projectsportpro2026_2.ui.partidos.SportProVerdeClaro
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Piezas visuales reutilizables del bloque Comunicación y Comunidad (HU19–HU24).
// La paleta base viene de ui/partidos/EstiloPartido.kt (Ítalo).

// ---------- Colores de etiquetas (Figma) ----------

val EtiquetaAzul = Color(0xFF3550A0)
val EtiquetaAzulFondo = Color(0xFFE8EEFF)
val EtiquetaLila = Color(0xFF6B4FA0)
val EtiquetaLilaFondo = Color(0xFFF1ECFA)
val EtiquetaNaranja = Color(0xFFA35F00)
val EtiquetaNaranjaFondo = Color(0xFFFFF1DD)
val EtiquetaGris = Color(0xFF66737E)
val EtiquetaGrisFondo = Color(0xFFEEF0F1)

// ---------- Componentes ----------

@Composable
fun CabeceraSportPro() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            modifier = Modifier.size(38.dp),
            shape = RoundedCornerShape(10.dp),
            color = SportProNavy
        ) {
            Box(contentAlignment = Alignment.Center) { Text("⚽") }
        }
        Column {
            Text(
                text = "SPORTPRO",
                color = SportProNavy,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "CLUB DEPORTIVO",
                color = SportProTextoSecundario,
                fontSize = 10.sp,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun ChipSportPro(
    texto: String,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (seleccionado) SportProNavy else SportProVerdeClaro
    ) {
        Text(
            text = texto,
            color = if (seleccionado) Color.White else SportProNavy,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun EtiquetaSportPro(texto: String, color: Color, fondo: Color) {
    Surface(shape = RoundedCornerShape(6.dp), color = fondo) {
        Text(
            text = texto,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun MensajeErrorSportPro(texto: String, onReintentar: () -> Unit) {
    Surface(
        shape = FormaCampoSportPro,
        color = SportProRojoClaro,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(texto, color = SportProRojo, fontSize = 13.sp)
            TextButton(onClick = onReintentar) {
                Text("Reintentar", color = SportProRojo, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Círculo con iniciales, como "DM" o "CN" en el Figma
@Composable
fun AvatarIniciales(
    nombre: String,
    oscuro: Boolean = false,
    tamano: Dp = 40.dp
) {
    val iniciales = nombre.trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { "?" }

    Surface(
        modifier = Modifier.size(tamano),
        shape = CircleShape,
        color = if (oscuro) SportProNavy else SportProVerdeClaro
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = iniciales,
                color = if (oscuro) Color(0xFFB8F28B) else SportProVerde,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun TituloCampoSportPro(texto: String) {
    Text(
        text = texto,
        color = SportProTextoSecundario,
        fontFamily = FontFamily.Monospace,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
    )
}

// Fila de chips que se desliza horizontalmente si no caben
@Composable
fun FilaDeChipsSportPro(contenido: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        content = contenido
    )
}

// ---------- Textos ----------

fun nombreRol(rol: Rol): String = when (rol) {
    Rol.DT -> "Director Técnico"
    Rol.JUG -> "Jugador"
    Rol.PAD -> "Apoderado"
    Rol.ADM -> "Administrador"
}

fun formatearFecha(millis: Long): String =
    SimpleDateFormat("dd MMM yyyy · HH:mm", Locale.forLanguageTag("es-PE")).format(Date(millis))

// "Ahora mismo", "Hace 5 min", "Hace 2 horas", "Ayer"... como en el Figma
fun tiempoRelativo(millis: Long, ahora: Long = System.currentTimeMillis()): String {
    val minutos = (ahora - millis) / 60_000
    val horas = minutos / 60
    return when {
        minutos < 1 -> "Ahora mismo"
        minutos < 60 -> "Hace $minutos min"
        horas < 24 -> "Hace $horas ${if (horas == 1L) "hora" else "horas"}"
        horas < 48 -> "Ayer"
        else -> SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("es-PE")).format(Date(millis))
    }
}