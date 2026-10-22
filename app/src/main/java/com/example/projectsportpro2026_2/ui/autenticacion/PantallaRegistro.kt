package com.example.projectsportpro2026_2.ui.autenticacion

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.RadioButton
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
import com.example.projectsportpro2026_2.datos.modelo.Rol
import com.example.projectsportpro2026_2.ui.theme.ProjectSportPro2026_2Theme

@Composable
fun PantallaRegistro(
    onRegistrado: (PerfilUsuario) -> Unit,
    onIrALogin: () -> Unit,
    viewModel: RegistroViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()

    LaunchedEffect(estado.perfilCreado) {
        estado.perfilCreado?.let(onRegistrado)
    }

    ContenidoRegistro(
        estado = estado,
        onNombre = viewModel::cambiarNombre,
        onCorreo = viewModel::cambiarCorreo,
        onContrasena = viewModel::cambiarContrasena,
        onRol = viewModel::cambiarRol,
        onFecha = viewModel::cambiarFechaNacimiento,
        onRegistrar = viewModel::registrar,
        onIrALogin = onIrALogin
    )
}

@Composable
fun ContenidoRegistro(
    estado: EstadoRegistro,
    onNombre: (String) -> Unit,
    onCorreo: (String) -> Unit,
    onContrasena: (String) -> Unit,
    onRol: (Rol) -> Unit,
    onFecha: (String) -> Unit,
    onRegistrar: () -> Unit,
    onIrALogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Crear cuenta", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = estado.nombre,
            onValueChange = onNombre,
            label = { Text("Nombre completo") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
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
            label = { Text("Contraseña (mínimo 6 caracteres)") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        Text("Me registro como:", style = MaterialTheme.typography.titleMedium)
        listOf(Rol.JUG, Rol.PAD, Rol.DT).forEach { rol ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onRol(rol) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = estado.rol == rol, onClick = { onRol(rol) })
                Text(nombreDeRol(rol))
            }
        }

        if (estado.rol == Rol.JUG) {
            OutlinedTextField(
                value = estado.fechaNacimiento,
                onValueChange = onFecha,
                label = { Text("Fecha de nacimiento (aaaa-MM-dd)") },
                supportingText = {
                    Text("Menores de 12 años no crean cuenta. De 12 a 17 requieren autorización del apoderado.")
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        estado.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Button(
            onClick = onRegistrar,
            enabled = !estado.cargando,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (estado.cargando) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text("Registrarme")
            }
        }

        TextButton(
            onClick = onIrALogin,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("¿Ya tienes cuenta? Inicia sesión")
        }
    }
}

private fun nombreDeRol(rol: Rol): String = when (rol) {
    Rol.JUG -> "Jugador"
    Rol.PAD -> "Padre o apoderado"
    Rol.DT -> "Director técnico"
    Rol.ADM -> "Administrador"
}

@Preview(showBackground = true)
@Composable
fun VistaPreviaRegistro() {
    ProjectSportPro2026_2Theme {
        ContenidoRegistro(
            estado = EstadoRegistro(),
            onNombre = {},
            onCorreo = {},
            onContrasena = {},
            onRol = {},
            onFecha = {},
            onRegistrar = {},
            onIrALogin = {}
        )
    }
}