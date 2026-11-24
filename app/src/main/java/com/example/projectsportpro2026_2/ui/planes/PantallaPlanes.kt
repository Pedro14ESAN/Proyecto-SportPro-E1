package com.example.projectsportpro2026_2.ui.planes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectsportpro2026_2.ui.comun.CabeceraSportPro
import com.example.projectsportpro2026_2.ui.comun.EtiquetaSportPro
import com.example.projectsportpro2026_2.ui.estadisticas.EncabezadoSeccion
import com.example.projectsportpro2026_2.ui.partidos.FormaBotonSportPro
import com.example.projectsportpro2026_2.ui.partidos.SportProBorde
import com.example.projectsportpro2026_2.ui.partidos.SportProFondo
import com.example.projectsportpro2026_2.ui.partidos.SportProNavy
import com.example.projectsportpro2026_2.ui.partidos.SportProSuperficie
import com.example.projectsportpro2026_2.ui.partidos.SportProTextoSecundario
import com.example.projectsportpro2026_2.ui.partidos.SportProVerde
import com.example.projectsportpro2026_2.ui.partidos.SportProVerdeClaro

/*
 * Planes y ofertas de SportPro (DEMO).
 * Todo es ficticio: no se conecta ningún medio de pago.
 */

private data class Plan(
    val nombre: String,
    val precio: String,
    val detalle: String,
    val incluye: List<String>,
    val recomendado: Boolean = false
)

private data class Oferta(
    val emoji: String,
    val nombre: String,
    val descripcion: String
)

private val planes = listOf(
    Plan(
        "Básico", "S/ 49", "Hasta 2 equipos y 40 jugadores",
        listOf("Entrenamientos y convocatorias", "Asistencias", "Anuncios del club")
    ),
    Plan(
        "Pro", "S/ 129", "Equipos ilimitados · hasta 150 jugadores",
        listOf("Todo lo del Básico", "Partido en vivo", "Estadísticas y comunidad"),
        recomendado = true
    ),
    Plan(
        "Élite", "S/ 249", "Varias sedes · jugadores ilimitados",
        listOf("Todo lo del Pro", "Resumen del partido con IA", "Narración en audio y soporte")
    )
)

private val ofertas = listOf(
    Oferta("⏱️", "Primer Tiempo Gratis", "45 días del plan Pro gratis, sin tarjeta."),
    Oferta("🏅", "Academia Fundadora", "50 % de descuento de por vida para las primeras 20 academias."),
    Oferta("🏆", "Temporada Completa", "Paga 10 meses y usa 12, con el plan Élite gratis en el torneo de fin de año.")
)

@Composable
fun PantallaPlanes(onVolver: () -> Unit) {
    var elegido by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SportProFondo)
            .statusBarsPadding(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { CabeceraSportPro() }
        item {
            EncabezadoSeccion(
                textoVolver = "Inicio",
                onVolver = onVolver,
                overline = "SPORTPRO PARA ACADEMIAS",
                titulo = "Planes y ofertas"
            )
        }

        item { TituloBloque("OFERTAS DE LANZAMIENTO") }
        items(ofertas) { o -> TarjetaOferta(o) }

        item { TituloBloque("PLANES MENSUALES") }
        items(planes) { p -> TarjetaPlan(p) { elegido = p.nombre } }

        item {
            Text(
                "Versión demo: los precios son referenciales y no se realiza ningún cobro.",
                color = SportProTextoSecundario, fontSize = 12.sp
            )
        }
    }

    elegido?.let { nombre ->
        AlertDialog(
            onDismissRequest = { elegido = null },
            title = { Text("Plan $nombre") },
            text = {
                Text(
                    "¡Gracias por tu interés! Esta es una versión de demostración, " +
                            "así que no se realiza ningún pago.",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { elegido = null }) {
                    Text("Entendido", color = SportProVerde, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun TituloBloque(texto: String) {
    Text(
        texto, color = SportProTextoSecundario, fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.5.sp
    )
}

@Composable
private fun TarjetaOferta(o: Oferta) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SportProVerdeClaro
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(o.emoji, fontSize = 28.sp)
            Spacer(Modifier.padding(start = 12.dp))
            Column {
                Text(o.nombre, color = SportProNavy, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(o.descripcion, color = SportProNavy, fontSize = 13.sp, lineHeight = 18.sp)
            }
        }
    }
}

@Composable
private fun TarjetaPlan(p: Plan, onElegir: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = if (p.recomendado) SportProNavy else SportProSuperficie,
        border = if (p.recomendado) null else BorderStroke(1.dp, SportProBorde)
    ) {
        val texto = if (p.recomendado) Color.White else SportProNavy
        val secundario = if (p.recomendado) Color.White.copy(alpha = 0.75f) else SportProTextoSecundario

        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(p.nombre, color = texto, fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                if (p.recomendado) {
                    EtiquetaSportPro("RECOMENDADO", SportProNavy, Color(0xFFA8F06A))
                }
            }
            Row(verticalAlignment = Alignment.Bottom) {
                Text(p.precio, color = texto, fontWeight = FontWeight.ExtraBold, fontSize = 30.sp)
                Text(" / mes", color = secundario, fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 4.dp))
            }
            Text(p.detalle, color = secundario, fontSize = 13.sp)
            p.incluye.forEach { Text("✓ $it", color = texto, fontSize = 14.sp) }
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = onElegir,
                shape = FormaBotonSportPro,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (p.recomendado) Color(0xFFA8F06A) else SportProNavy,
                    contentColor = if (p.recomendado) SportProNavy else Color.White
                ),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Elegir ${p.nombre}", fontWeight = FontWeight.Bold)
            }
        }
    }
}
