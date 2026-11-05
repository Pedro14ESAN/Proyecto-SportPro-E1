package com.example.projectsportpro2026_2.ui.autenticacion

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectsportpro2026_2.ui.theme.ProjectSportPro2026_2Theme

private val VerdePrincipal = Color(0xFF1B9A5C)
private val VerdeOscuro = Color(0xFF14804B)
private val FondoPantalla = Color(0xFFFAFAF7)
private val TituloOscuro = Color(0xFF0F2438)
private val TextoEtiqueta = Color(0xFF5B6B73)
private val BordeCampo = Color(0xFFD5E3DC)
private val TextoSecundario = Color(0xFF6B7A80)

@Composable
fun PantallaRecuperar(
    onVolver: () -> Unit,
    viewModel: RecuperarViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()

    ContenidoRecuperar(
        estado = estado,
        onCorreo = viewModel::cambiarCorreo,
        onEnviar = viewModel::enviar,
        onVolver = onVolver
    )
}

@Composable
fun ContenidoRecuperar(
    estado: EstadoRecuperar,
    onCorreo: (String) -> Unit,
    onEnviar: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FondoPantalla)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "← Volver",
            color = VerdeOscuro,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            modifier = Modifier.clickable(onClick = onVolver)
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "RECUPERAR ACCESO",
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 2.sp,
                color = VerdePrincipal
            )
        )
        Text(
            text = "¿Olvidaste tu contraseña?",
            style = TextStyle(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 30.sp,
                color = TituloOscuro
            )
        )
        Text(
            text = "Escribe el correo de tu cuenta y te enviaremos un enlace para crear una contraseña nueva.",
            color = TextoSecundario,
            fontSize = 14.sp
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "CORREO ELECTRÓNICO",
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 1.sp,
                color = TextoEtiqueta
            )
        )
        OutlinedTextField(
            value = estado.correo,
            onValueChange = onCorreo,
            placeholder = { Text("nombre@correo.com") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = VerdePrincipal,
                unfocusedBorderColor = BordeCampo,
                cursorColor = VerdePrincipal,
                focusedTextColor = TituloOscuro,
                unfocusedTextColor = TituloOscuro,
                focusedPlaceholderColor = Color(0xFFA5B1B7),
                unfocusedPlaceholderColor = Color(0xFFA5B1B7)
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        estado.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        estado.mensaje?.let { Text(it, color = VerdeOscuro, fontSize = 14.sp) }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = onEnviar,
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
                Text("Enviar enlace", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VistaPreviaRecuperar() {
    ProjectSportPro2026_2Theme {
        ContenidoRecuperar(
            estado = EstadoRecuperar(),
            onCorreo = {},
            onEnviar = {},
            onVolver = {}
        )
    }
}