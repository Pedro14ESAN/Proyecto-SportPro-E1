package com.example.projectsportpro2026_2.ui.inicio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
    onCerrarSesion: () -> Unit,
    acciones: @Composable () -> Unit = {}
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
        acciones()
        OutlinedButton(onClick = onCerrarSesion) { Text("Cerrar sesión") }
    }
}

@Composable
fun InicioAdministrador(onGestionarAcademias: () -> Unit, onCerrarSesion: () -> Unit) =
    PantallaBaseInicio(
        "Panel del administrador", "Gestiona tus academias, equipos y categorías.", onCerrarSesion
    ) {
        Button(onClick = onGestionarAcademias) { Text("Gestionar academias") }
        Spacer(Modifier.height(12.dp))
    }

@Composable
fun InicioEntrenador(onGestionarAcademias: () -> Unit, onCerrarSesion: () -> Unit) =
    PantallaBaseInicio(
        "Panel del director técnico", "Gestiona tus academias y equipos.", onCerrarSesion
    ) {
        Button(onClick = onGestionarAcademias) { Text("Gestionar academias") }
        Spacer(Modifier.height(12.dp))
    }

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