package com.example.projectsportpro2026_2.ui.autenticacion

import com.example.projectsportpro2026_2.ui.comun.VarianteBoton
import com.example.projectsportpro2026_2.ui.comun.BotonPrincipal
import com.example.projectsportpro2026_2.ui.comun.FondoCancha
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.SportsSoccer
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.widthIn
import com.example.projectsportpro2026_2.ui.theme.FamiliaEtiquetas
import com.example.projectsportpro2026_2.ui.theme.FamiliaTitulos
import com.example.projectsportpro2026_2.ui.partidos.SportProNavyFondo
import com.example.projectsportpro2026_2.ui.theme.LineaDeCal
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

private val VerdeLima: Color @Composable get() = LineaDeCal.colores.lima // antes Color(0xFFA8F06A)
private val VerdeLimaSuave: Color @Composable get() = LineaDeCal.colores.okFondo // antes Color(0xFFE3F5D9)
private val NavyPortada: Color @Composable get() = LineaDeCal.colores.navy // antes Color(0xFF122C3D)

// ---------------- 1. Portada ----------------

@Composable
fun PantallaBienvenida(onComenzar: () -> Unit) {
    // Línea de cal (6.1, img 09): portada con FondoCancha y botón lima
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SportProFondo)
            .navigationBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
        ) {
            FondoCancha(
                modifier = Modifier.weight(1f),
                alturaMinima = 420.dp
            ) {
                Text(
                    "SPORTPRO / 2026",
                    color = VerdeLima,
                    fontFamily = FamiliaEtiquetas,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
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
                    Icon(
                        Icons.Rounded.SportsSoccer,
                        contentDescription = null,
                        tint = NavyPortada,
                        modifier = Modifier.size(34.dp)
                    )
                }
                Text(
                    "SPORTPRO",
                    color = Color.White,
                    fontFamily = FamiliaTitulos,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 44.sp
                )
                Text(
                    "La operación de tu academia,\nsiempre en juego.",
                    color = Color.White.copy(alpha = 0.9f),
                    fontFamily = FamiliaTitulos,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    lineHeight = 28.sp
                )
                Spacer(Modifier.height(24.dp))
            }

            Box(Modifier.padding(horizontal = 24.dp, vertical = 28.dp)) {
                BotonPrincipal(
                    texto = "Comenzar",
                    onClick = onComenzar,
                    icono = Icons.AutoMirrored.Rounded.ArrowForward
                )
            }
        }
    }
}


// ---------------- 2. Introducción ----------------

private data class PasoIntro(
    val etiqueta: String,
    val icono: ImageVector,
    val titulo: String,
    val texto: String
)

private val pasos = listOf(
    PasoIntro(
        "ORGANIZACIÓN", Icons.Rounded.SportsSoccer,
        "Todo tu equipo, en la misma cancha.",
        "Entrenamientos, convocatorias y partidos organizados en un solo lugar."
    ),
    PasoIntro(
        "PARTIDO EN VIVO", Icons.Rounded.Timer,
        "Cada jugada, al minuto.",
        "Registra goles, tarjetas y cambios en tiempo real, aunque te quedes sin señal."
    ),
    PasoIntro(
        "COMUNIDAD", Icons.Rounded.Campaign,
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
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .clickable(role = Role.Button) {
                    if (pagina > 0) alcance.launch { estadoPager.animateScrollToPage(pagina - 1) }
                    else onVolver()
                }
                .padding(vertical = 6.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = null,
                tint = SportProVerde,
                modifier = Modifier.size(20.dp)
            )
            Text(
                "Volver",
                color = SportProVerde,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }
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
                        .background(if (i == pagina) SportProVerde else LineaDeCal.colores.borde) // antes Color(0xFFD5DBD3)
                )
            }
        }
        Spacer(Modifier.height(20.dp))

        BotonPrincipal(
            texto = if (esUltima) "Ir a iniciar sesión" else "Continuar",
            onClick = {
                if (esUltima) onTerminar()
                else alcance.launch { estadoPager.animateScrollToPage(pagina + 1) }
            },
            variante = if (esUltima) VarianteBoton.LIMA else VarianteBoton.NAVY
        )
        Spacer(Modifier.height(14.dp))
        Text(
            "Saltar introducción",
            color = SportProVerde,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
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
                    Icon(paso.icono, contentDescription = null, tint = NavyPortada, modifier = Modifier.size(52.dp))
                }
            }
        }
        Spacer(Modifier.height(28.dp))
        Text(
            "%02d / %02d · %s".format(numero, total, paso.etiqueta),
            color = SportProVerde,
            fontFamily = FamiliaEtiquetas,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            letterSpacing = 1.5.sp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            paso.titulo,
            color = SportProNavy,
            fontFamily = FamiliaTitulos,
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
