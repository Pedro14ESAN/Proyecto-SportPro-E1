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
        modifier =
            Modifier
                .fillMaxSize()
                .padding(
                    24.dp
                ),

        verticalArrangement =
            Arrangement.Center,

        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Text(
            text =
                titulo,

            style =
                MaterialTheme
                    .typography
                    .headlineMedium,

            textAlign =
                TextAlign.Center
        )

        Spacer(
            Modifier.height(
                8.dp
            )
        )

        Text(
            text =
                descripcion,

            textAlign =
                TextAlign.Center
        )

        Spacer(
            Modifier.height(
                24.dp
            )
        )

        acciones()

        OutlinedButton(
            onClick =
                onCerrarSesion
        ) {

            Text(
                "Cerrar sesión"
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
    onCerrarSesion: () -> Unit
) = PantallaBaseInicio(

    titulo =
        "Panel del administrador",

    descripcion =
        "Gestiona tus academias, equipos, jugadores y módulos administrativos.",

    onCerrarSesion =
        onCerrarSesion

) {

    Button(
        onClick =
            onGestionarAcademias
    ) {
        Text(
            "Gestionar academias"
        )
    }

    Spacer(
        Modifier.height(
            12.dp
        )
    )

    Button(
        onClick =
            onVinculaciones
    ) {
        Text(
            "Vinculaciones"
        )
    }

    Spacer(
        Modifier.height(
            12.dp
        )
    )

    Button(
        onClick =
            onMensualidades
    ) {
        Text(
            "Mensualidades"
        )
    }

    Spacer(
        Modifier.height(
            12.dp
        )
    )

    Button(
        onClick =
            onEjercicios
    ) {
        Text(
            "Biblioteca de ejercicios"
        )
    }

    Spacer(
        Modifier.height(
            12.dp
        )
    )

    Button(
        onClick =
            onAsistencias
    ) {
        Text(
            "Asistencias"
        )
    }

    Spacer(
        Modifier.height(
            12.dp
        )
    )
}

@Composable
fun InicioEntrenador(
    onGestionarAcademias: () -> Unit,
    onEjercicios: () -> Unit,
    onEntrenamientos: () -> Unit,
    onAsistencias: () -> Unit,
    onCerrarSesion: () -> Unit
) = PantallaBaseInicio(

    titulo =
        "Panel del director técnico",

    descripcion =
        "Gestiona tus equipos, biblioteca, entrenamientos y asistencia.",

    onCerrarSesion =
        onCerrarSesion

) {

    Button(
        onClick =
            onGestionarAcademias
    ) {
        Text(
            "Gestionar academias"
        )
    }

    Spacer(
        Modifier.height(
            12.dp
        )
    )

    Button(
        onClick =
            onEjercicios
    ) {
        Text(
            "Biblioteca de ejercicios"
        )
    }

    Spacer(
        Modifier.height(
            12.dp
        )
    )

    Button(
        onClick =
            onEntrenamientos
    ) {
        Text(
            "Entrenamientos"
        )
    }

    Spacer(
        Modifier.height(
            12.dp
        )
    )

    Button(
        onClick =
            onAsistencias
    ) {
        Text(
            "Asistencias"
        )
    }

    Spacer(
        Modifier.height(
            12.dp
        )
    )
}

@Composable
fun InicioJugador(
    onMensualidades: () -> Unit,
    onEntrenamientos: () -> Unit,
    onAsistencias: () -> Unit,
    onCerrarSesion: () -> Unit
) = PantallaBaseInicio(

    titulo =
        "Panel del jugador",

    descripcion =
        "Consulta tu información deportiva y administrativa.",

    onCerrarSesion =
        onCerrarSesion

) {

    Button(
        onClick =
            onMensualidades
    ) {
        Text(
            "Mis mensualidades"
        )
    }

    Spacer(
        Modifier.height(
            12.dp
        )
    )

    Button(
        onClick =
            onEntrenamientos
    ) {
        Text(
            "Mis entrenamientos"
        )
    }

    Spacer(
        Modifier.height(
            12.dp
        )
    )

    Button(
        onClick =
            onAsistencias
    ) {
        Text(
            "Mi asistencia"
        )
    }

    Spacer(
        Modifier.height(
            12.dp
        )
    )
}

@Composable
fun InicioApoderado(
    onMisJugadores: () -> Unit,
    onMensualidades: () -> Unit,
    onEntrenamientos: () -> Unit,
    onAsistencias: () -> Unit,
    onCerrarSesion: () -> Unit
) = PantallaBaseInicio(

    titulo =
        "Panel del apoderado",

    descripcion =
        "Consulta a los jugadores bajo tu responsabilidad.",

    onCerrarSesion =
        onCerrarSesion

) {

    Button(
        onClick =
            onMisJugadores
    ) {
        Text(
            "Mis jugadores"
        )
    }

    Spacer(
        Modifier.height(
            12.dp
        )
    )

    Button(
        onClick =
            onMensualidades
    ) {
        Text(
            "Mensualidades"
        )
    }

    Spacer(
        Modifier.height(
            12.dp
        )
    )

    Button(
        onClick =
            onEntrenamientos
    ) {
        Text(
            "Entrenamientos"
        )
    }

    Spacer(
        Modifier.height(
            12.dp
        )
    )

    Button(
        onClick =
            onAsistencias
    ) {
        Text(
            "Historial de asistencia"
        )
    }

    Spacer(
        Modifier.height(
            12.dp
        )
    )
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