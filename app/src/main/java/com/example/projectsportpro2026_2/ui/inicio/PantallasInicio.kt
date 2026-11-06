package com.example.projectsportpro2026_2.ui.inicio

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectsportpro2026_2.ui.partidos.SportProNavy

// Colores locales de estas pantallas (no se modifica el tema global)
private val VerdePrincipal = Color(0xFF1B9A5C)
private val FondoPantalla = Color(0xFFFAFAF7)
private val TituloOscuro = Color(0xFF0F2438)
private val BordeCampo = Color(0xFFD5E3DC)
private val TextoSecundario = Color(0xFF6B7A80)

@Composable
private fun PantallaBaseInicio(
    titulo: String,
    descripcion: String,
    onCerrarSesion: () -> Unit,
    acciones: @Composable () -> Unit = {}
) {

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(FondoPantalla)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {

        // Logo y nombre de la app
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier =
                    Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SportProNavy),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⚽",
                    fontSize = 22.sp
                )
            }

            Spacer(Modifier.width(12.dp))

            Text(
                text = "SportPro",
                style = TextStyle(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    color = TituloOscuro
                )
            )
        }

        Spacer(Modifier.height(32.dp))

        Text(
            text = "PANEL PRINCIPAL",
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 2.sp,
                color = VerdePrincipal
            )
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = titulo,
            style = TextStyle(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 28.sp,
                color = TituloOscuro
            )
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = descripcion,
            color = TextoSecundario,
            fontSize = 14.sp
        )

        Spacer(Modifier.height(24.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            acciones()
        }

        Spacer(Modifier.height(24.dp))

        OutlinedButton(
            onClick =
                onCerrarSesion,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, BordeCampo),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                "Cerrar sesión",
                color = TextoSecundario,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// Botón de acción del panel: ancho completo, texto a la izquierda y flecha a la derecha
@Composable
private fun BotonInicio(
    texto: String,
    onClick: () -> Unit,
    destacado: Boolean = false
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (destacado) VerdePrincipal else Color.White,
            contentColor = if (destacado) Color.White else TituloOscuro
        ),
        border = if (destacado) null else BorderStroke(1.dp, BordeCampo),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = 20.dp,
            vertical = 0.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = texto,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
            Text(
                text = "→",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = if (destacado) Color.White else VerdePrincipal
            )
        }
    }
}

@Composable
fun InicioAdministrador(
    onGestionarAcademias: () -> Unit,
    onVinculaciones: () -> Unit,
    onMensualidades: () -> Unit,
    onEjercicios: () -> Unit,
    onAsistencias: () -> Unit,
    onPartidosEnVivo: () -> Unit = {},
    onAnuncios: () -> Unit = {},          // HU19
    onComunidad: () -> Unit = {},         // HU21
    onNotificaciones: () -> Unit = {},    // HU20
    onEstadisticas: () -> Unit = {},      // HU18
    onCerrarSesion: () -> Unit
) = PantallaBaseInicio(

    titulo =
        "Panel del administrador",

    descripcion =
        "Gestiona tus academias, equipos, jugadores y módulos administrativos.",

    onCerrarSesion =
        onCerrarSesion

) {

    // HU17: seguimiento de partidos en tiempo real
    BotonInicio("Partidos en vivo", onPartidosEnVivo, destacado = true)
    // HU18 (Pedro): rendimiento de equipo y jugadores
    BotonInicio("📊 Estadísticas", onEstadisticas)
    BotonInicio("Gestionar academias", onGestionarAcademias)
    BotonInicio("Vinculaciones", onVinculaciones)
    BotonInicio("Mensualidades", onMensualidades)
    BotonInicio("Biblioteca de ejercicios", onEjercicios)
    BotonInicio("Asistencias", onAsistencias)
    // HU19, HU21, HU20 (Cielo)
    BotonInicio("Anuncios del club", onAnuncios)
    BotonInicio("Comunidad", onComunidad)
    BotonInicio("🔔 Notificaciones", onNotificaciones)
}

@Composable
fun InicioEntrenador(
    onGestionarAcademias: () -> Unit,
    onEjercicios: () -> Unit,
    onEntrenamientos: () -> Unit,
    onAsistencias: () -> Unit,
    onPartidosEnVivo: () -> Unit = {},    onAnuncios: () -> Unit = {},          // HU19
    onComunidad: () -> Unit = {},         // HU21
    onNotificaciones: () -> Unit = {},    // HU20
    onEstadisticas: () -> Unit = {},      // HU18
    onCerrarSesion: () -> Unit
) = PantallaBaseInicio(

    titulo =
        "Panel del director técnico",

    descripcion =
        "Gestiona tus equipos, biblioteca, entrenamientos y asistencia.",

    onCerrarSesion =
        onCerrarSesion

) {

    // HU17: seguimiento de partidos en tiempo real
    BotonInicio("Partidos en vivo", onPartidosEnVivo, destacado = true)
    // HU18 (Pedro): rendimiento de equipo y jugadores
    BotonInicio("📊 Estadísticas", onEstadisticas)
    BotonInicio("Gestionar academias", onGestionarAcademias)
    BotonInicio("Biblioteca de ejercicios", onEjercicios)
    BotonInicio("Entrenamientos", onEntrenamientos)
    BotonInicio("Asistencias", onAsistencias)
    // HU19, HU21, HU20 (Cielo)
    BotonInicio("Anuncios del club", onAnuncios)
    BotonInicio("Comunidad", onComunidad)
    BotonInicio("🔔 Notificaciones", onNotificaciones)
}

@Composable
fun InicioJugador(
    onMensualidades: () -> Unit,
    onEntrenamientos: () -> Unit,
    onAsistencias: () -> Unit,
    onConvocatorias: () -> Unit,
    onPartidosEnVivo: () -> Unit = {},    onAnuncios: () -> Unit = {},          // HU19
    onComunidad: () -> Unit = {},         // HU21
    onNotificaciones: () -> Unit = {},    // HU20
    onEstadisticas: () -> Unit = {},      // HU18
    onCerrarSesion: () -> Unit
) = PantallaBaseInicio(

    titulo =
        "Panel del jugador",

    descripcion =
        "Consulta tu información deportiva y administrativa.",

    onCerrarSesion =
        onCerrarSesion

) {

    // HU17: seguimiento de partidos en tiempo real
    BotonInicio("Partidos en vivo", onPartidosEnVivo, destacado = true)
    // HU18 (Pedro): rendimiento de equipo y jugadores
    BotonInicio("📊 Estadísticas", onEstadisticas)
    BotonInicio("Mis mensualidades", onMensualidades)
    BotonInicio("Mis entrenamientos", onEntrenamientos)
    BotonInicio("Mi asistencia", onAsistencias)
    BotonInicio("Mis convocatorias", onConvocatorias)
    // HU19, HU21, HU20 (Cielo)
    BotonInicio("Anuncios del club", onAnuncios)
    BotonInicio("Comunidad", onComunidad)
    BotonInicio("🔔 Notificaciones", onNotificaciones)
}

@Composable
fun InicioApoderado(
    onMisJugadores: () -> Unit,
    onMensualidades: () -> Unit,
    onEntrenamientos: () -> Unit,
    onAsistencias: () -> Unit,
    onConvocatorias: () -> Unit,
    onPartidosEnVivo: () -> Unit = {},    onAnuncios: () -> Unit = {},          // HU19
    onComunidad: () -> Unit = {},         // HU21
    onNotificaciones: () -> Unit = {},    // HU20
    onEstadisticas: () -> Unit = {},      // HU18
    onCerrarSesion: () -> Unit
) = PantallaBaseInicio(

    titulo =
        "Panel del apoderado",

    descripcion =
        "Consulta a los jugadores bajo tu responsabilidad.",

    onCerrarSesion =
        onCerrarSesion

) {

    // HU17: seguimiento de partidos en tiempo real
    BotonInicio("Partidos en vivo", onPartidosEnVivo, destacado = true)
    // HU18 (Pedro): rendimiento de equipo y jugadores
    BotonInicio("📊 Estadísticas", onEstadisticas)
    BotonInicio("Mis jugadores", onMisJugadores)
    BotonInicio("Mensualidades", onMensualidades)
    BotonInicio("Entrenamientos", onEntrenamientos)
    BotonInicio("Historial de asistencia", onAsistencias)
    BotonInicio("Convocatorias", onConvocatorias)
    // HU19, HU21, HU20 (Cielo)
    BotonInicio("Anuncios del club", onAnuncios)
    BotonInicio("Comunidad", onComunidad)
    BotonInicio("🔔 Notificaciones", onNotificaciones)
}

@Composable
fun PantallaCuentaPendiente(
    onCerrarSesion: () -> Unit
) = PantallaBaseInicio(

    titulo =
        "Cuenta pendiente",

    descripcion =
        "Tu cuenta debe ser autorizada por tu apoderado y validada por la academia antes de usarla.",

    onCerrarSesion =
        onCerrarSesion
)