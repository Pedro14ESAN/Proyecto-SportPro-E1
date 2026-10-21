package com.example.projectsportpro2026_2.ui.autenticacion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectsportpro2026_2.datos.modelo.PerfilUsuario
import com.example.projectsportpro2026_2.ui.theme.ProjectSportPro2026_2Theme

@Composable
fun PantallaLogin(
    onSesionIniciada: (PerfilUsuario) -> Unit,
    onIrARegistro: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()

    LaunchedEffect(estado.perfil) {
        estado.perfil?.let(onSesionIniciada)
    }

    ContenidoLogin(
        estado = estado,
        onCorreo = viewModel::cambiarCorreo,
        onContrasena = viewModel::cambiarContrasena,
        onIniciarSesion = viewModel::iniciarSesion,
        onIrARegistro = onIrARegistro
    )
}

@Composable
fun ContenidoLogin(
    estado: EstadoLogin,
    onCorreo: (String) -> Unit,
    onContrasena: (String) -> Unit,
    onIniciarSesion: () -> Unit,
    onIrARegistro: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("SportPro", style = MaterialTheme.typography.headlineLarge)
        Text("Inicia sesión", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = estado.correo,
            onValueChange = onCorreo,
            label = { Text("Correo electrónico") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = estado.contrasena,
            onValueChange = onContrasena,
            label = { Text("Contraseña") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        estado.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Button(
            onClick = onIniciarSesion,
            enabled = !estado.cargando,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (estado.cargando) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text("Entrar")
            }
        }

        TextButton(
            onClick = onIrARegistro,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("¿No tienes cuenta? Regístrate")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VistaPreviaLogin() {
    ProjectSportPro2026_2Theme {
        ContenidoLogin(
            estado = EstadoLogin(),
            onCorreo = {},
            onContrasena = {},
            onIniciarSesion = {},
            onIrARegistro = {}
        )
    }
}