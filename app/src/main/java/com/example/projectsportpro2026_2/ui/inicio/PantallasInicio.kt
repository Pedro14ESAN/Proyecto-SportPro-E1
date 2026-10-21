package com.example.projectsportpro2026_2.ui.inicio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
private fun PantallaBaseInicio(
    titulo: String,
    descripcion: String,
    onCerrarSesion: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(titulo, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(descripcion, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onCerrarSesion) { Text("Cerrar sesión") }
    }
}

@Composable
fun InicioAdministrador(onCerrarSesion: () -> Unit) = PantallaBaseInicio(
    "Panel del administrador", "Aquí irá la gestión de la academia.", onCerrarSesion
)

@Composable
fun InicioEntrenador(onCerrarSesion: () -> Unit) = PantallaBaseInicio(
    "Panel del director técnico", "Aquí irán equipos, entrenamientos y partidos.", onCerrarSesion
)

@Composable
fun InicioJugador(onCerrarSesion: () -> Unit) = PantallaBaseInicio(
    "Panel del jugador", "Aquí irá tu información y tus convocatorias.", onCerrarSesion
)

@Composable
fun InicioApoderado(onCerrarSesion: () -> Unit) = PantallaBaseInicio(
    "Panel del apoderado", "Aquí irá la información de tu jugador.", onCerrarSesion
)

@Composable
fun PantallaCuentaPendiente(onCerrarSesion: () -> Unit) = PantallaBaseInicio(
    "Cuenta pendiente",
    "Tu cuenta debe ser autorizada por tu apoderado y validada por la academia antes de usarla.",
    onCerrarSesion
)