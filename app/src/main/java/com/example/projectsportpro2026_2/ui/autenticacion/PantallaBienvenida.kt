package com.example.projectsportpro2026_2.ui.autenticacion

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectsportpro2026_2.ui.partidos.SportProFondo
import com.example.projectsportpro2026_2.ui.partidos.SportProNavy
import com.example.projectsportpro2026_2.ui.partidos.SportProTextoSecundario
import com.example.projectsportpro2026_2.ui.partidos.SportProVerde
import kotlinx.coroutines.launch

/*
 * Portada e introducción que se muestran al abrir la app sin sesión:
 * Portada → Introducción (3 pasos) → Login.
 */

private val VerdeLima = Color(0xFFA8F06A)
private val VerdeLimaSuave = Color(0xFFE3F5D9)
private val NavyPortada = Color(0xFF122C3D)

// ---------------- 1. Portada ----------------

@Composable
fun PantallaBienvenida(onComenzar: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyPortada)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Balón grande de fondo, apenas visible
        Text(
            "⚽",
            fontSize = 220.sp,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = 70.dp, y = (-40).dp)
                .alpha(0.12f)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 32.dp)
        ) {
            Text(
                "SPORTPRO / 2026",
                color = VerdeLima,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 2.sp
            )

            Spacer(Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(VerdeLima),
                contentAlignment = Alignment.Center
            ) {
                Text("⚽", fontSize = 28.sp)
            }
            Spacer(Modifier.height(18.dp))
            Text(
                "SPORTPRO",
                color = Color.White,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 44.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "La operación de tu academia,\nsiempre en juego.",
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 15.sp,
                lineHeight = 22.sp
            )

            Spacer(Modifier.weight(1.2f))

            Button(
                onClick = onComenzar,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VerdeLima,
                    contentColor = NavyPortada
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text("Comenzar →", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

// ---------------- 2. Introducción ----------------

private data class PasoIntro(
    val etiqueta: String,
    val emoji: String,
    val titulo: String,
    val texto: String
)

private val pasos = listOf(
    PasoIntro(
        "ORGANIZACIÓN", "⚽",
        "Todo tu equipo, en la misma cancha.",
        "Entrenamientos, convocatorias y partidos organizados en un solo lugar."
    ),
    PasoIntro(
        "PARTIDO EN VIVO", "⏱️",
        "Cada jugada, al minuto.",
        "Registra goles, tarjetas y cambios en tiempo real, aunque te quedes sin señal."
    ),
    PasoIntro(
        "COMUNIDAD", "📣",
        "Familias y jugadores siempre al tanto.",
        "Avisos, estadísticas y el resumen de cada partido llegan a quien corresponde."
    )
)

@Composable
fun PantallaIntroduccion(
    onVolver: () -> Unit,
    onTerminar: () -> Unit
) {
    val estadoPager = rememberPagerState(pageCount = { pasos.size })
    val alcance = rememberCoroutineScope()
    val pagina = estadoPager.currentPage
    val esUltima = pagina == pasos.lastIndex

    // Atrás: vuelve al paso anterior o a la portada
    BackHandler {
        if (pagina > 0) alcance.launch { estadoPager.animateScrollToPage(pagina - 1) }
        else onVolver()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SportProFondo)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Text(
            "← Volver",
            color = SportProVerde,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            modifier = Modifier.clickable {
                if (pagina > 0) alcance.launch { estadoPager.animateScrollToPage(pagina - 1) }
                else onVolver()
            }
        )
        Spacer(Modifier.height(24.dp))

        HorizontalPager(
            state = estadoPager,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { i ->
            PaginaIntro(numero = i + 1, total = pasos.size, paso = pasos[i])
        }

        // Indicador de páginas
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            pasos.indices.forEach { i ->
                Box(
                    modifier = Modifier
                        .animateContentSize()
                        .height(6.dp)
                        .width(if (i == pagina) 26.dp else 6.dp)
                        .clip(CircleShape)
                        .background(if (i == pagina) SportProVerde else Color(0xFFD5DBD3))
                )
            }
        }
        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                if (esUltima) onTerminar()
                else alcance.launch { estadoPager.animateScrollToPage(pagina + 1) }
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SportProNavy),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            Text(
                if (esUltima) "Ir a iniciar sesión" else "Continuar",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
        Spacer(Modifier.height(14.dp))
        Text(
            "Saltar introducción",
            color = SportProVerde,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clickable { onTerminar() }
                .padding(6.dp)
        )
    }
}

@Composable
private fun PaginaIntro(numero: Int, total: Int, paso: PasoIntro) {
    Column(Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(VerdeLimaSuave),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .background(VerdeLima),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(118.dp)
                        .clip(CircleShape)
                        .background(VerdeLimaSuave),
                    contentAlignment = Alignment.Center
                ) {
                    Text(paso.emoji, fontSize = 48.sp)
                }
            }
        }
        Spacer(Modifier.height(28.dp))
        Text(
            "%02d / %02d · %s".format(numero, total, paso.etiqueta),
            color = SportProVerde,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.5.sp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            paso.titulo,
            color = SportProNavy,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 28.sp,
            lineHeight = 32.sp
        )
        Spacer(Modifier.height(12.dp))
        Text(
            paso.texto,
            color = SportProTextoSecundario,
            fontSize = 15.sp,
            lineHeight = 22.sp
        )
    }
}
