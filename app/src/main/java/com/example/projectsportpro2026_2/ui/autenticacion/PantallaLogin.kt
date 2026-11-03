package com.example.projectsportpro2026_2.ui.autenticacion

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectsportpro2026_2.datos.modelo.PerfilUsuario
import com.example.projectsportpro2026_2.ui.theme.ProjectSportPro2026_2Theme

// Colores locales de esta pantalla (no se modifica el tema global)
private val VerdePrincipal = Color(0xFF1B9A5C)
private val VerdeOscuro = Color(0xFF14804B)
private val FondoPantalla = Color(0xFFFAFAF7)
private val TituloOscuro = Color(0xFF0F2438)
private val TextoEtiqueta = Color(0xFF5B6B73)
private val BordeCampo = Color(0xFFD5E3DC)
private val TextoSecundario = Color(0xFF6B7A80)

@Composable
fun PantallaLogin(
    onSesionIniciada: (PerfilUsuario) -> Unit,
    onIrARegistro: () -> Unit,
    onIrARecuperar: () -> Unit,
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
        onIrARegistro = onIrARegistro,
        onIrARecuperar = onIrARecuperar
    )
}

@Composable
private fun EtiquetaCampo(texto: String) {
    Text(
        text = texto,
        style = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.sp,
            color = TextoEtiqueta
        )
    )
}

@Composable
fun ContenidoLogin(
    estado: EstadoLogin,
    onCorreo: (String) -> Unit,
    onContrasena: (String) -> Unit,
    onIniciarSesion: () -> Unit,
    onIrARegistro: () -> Unit,
    onIrARecuperar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formaCampo = RoundedCornerShape(16.dp)
    val coloresCampo = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        focusedBorderColor = VerdePrincipal,
        unfocusedBorderColor = BordeCampo,
        cursorColor = VerdePrincipal,
        focusedTextColor = TituloOscuro,
        unfocusedTextColor = TituloOscuro,
        focusedPlaceholderColor = Color(0xFFA5B1B7),
        unfocusedPlaceholderColor = Color(0xFFA5B1B7)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FondoPantalla)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Sport PRO",
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 2.sp,
                color = VerdePrincipal
            )
        )
        Text(
            text = "Bienvenido",
            style = TextStyle(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 32.sp,
                color = TituloOscuro
            )
        )

        Spacer(Modifier.height(12.dp))

        EtiquetaCampo("CORREO ELECTRÓNICO")
        OutlinedTextField(
            value = estado.correo,
            onValueChange = onCorreo,
            placeholder = { Text("nombre@correo.com") },
            singleLine = true,
            shape = formaCampo,
            colors = coloresCampo,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(4.dp))

        EtiquetaCampo("CONTRASEÑA")
        OutlinedTextField(
            value = estado.contrasena,
            onValueChange = onContrasena,
            placeholder = { Text("••••••••") },
            singleLine = true,
            shape = formaCampo,
            colors = coloresCampo,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        estado.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = onIniciarSesion,
            enabled = !estado.cargando,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = VerdePrincipal,
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (estado.cargando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
            } else {
                Text(
                    "Iniciar sesión",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        Text(
            text = "¿Olvidaste tu contraseña?",
            color = VerdeOscuro,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onIrARecuperar)
                .padding(top = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "¿Aún no tienes cuenta? ",
                color = TextoSecundario,
                fontSize = 14.sp
            )
            Text(
                text = "Crear cuenta",
                color = VerdeOscuro,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.clickable(onClick = onIrARegistro)
            )
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
            onIrARegistro = {},
            onIrARecuperar = {}
        )
    }
}