package com.example.projectsportpro2026_2.ui.autenticacion

import com.example.projectsportpro2026_2.ui.comun.BotonPrincipal
import com.example.projectsportpro2026_2.ui.comun.CampoSportPro
import com.example.projectsportpro2026_2.ui.comun.FondoCancha
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.Box
import com.example.projectsportpro2026_2.ui.theme.FamiliaEtiquetas
import com.example.projectsportpro2026_2.ui.theme.FamiliaTitulos
import com.example.projectsportpro2026_2.ui.theme.LineaDeCal
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
private val VerdePrincipal: Color @Composable get() = LineaDeCal.colores.verde // antes Color(0xFF1B9A5C)
private val VerdeOscuro: Color @Composable get() = LineaDeCal.colores.okTexto // antes Color(0xFF14804B)
private val FondoPantalla: Color @Composable get() = LineaDeCal.colores.fondo // antes Color(0xFFFAFAF7)
private val TituloOscuro: Color @Composable get() = LineaDeCal.colores.textoPrincipal // antes Color(0xFF0F2438)
private val TextoEtiqueta: Color @Composable get() = LineaDeCal.colores.textoSecundario // antes Color(0xFF5B6B73)
private val BordeCampo: Color @Composable get() = LineaDeCal.colores.borde // antes Color(0xFFD5E3DC)
private val TextoSecundario: Color @Composable get() = LineaDeCal.colores.textoSecundario // antes Color(0xFF6B7A80)

@Composable
fun PantallaLogin(
    onSesionIniciada: (PerfilUsuario) -> Unit,
    onIrARegistro: () -> Unit,
    onIrARecuperar: () -> Unit,
    // Regresa a la portada; null = sin flecha
    onVolverAlInicio: (() -> Unit)? = null,
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
        onIrARecuperar = onIrARecuperar,
        onVolverAlInicio = onVolverAlInicio
    )
}

@Composable
private fun EtiquetaCampo(texto: String) {
    Text(
        text = texto,
        style = TextStyle(
            fontFamily = FamiliaEtiquetas,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
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
    modifier: Modifier = Modifier,
    onVolverAlInicio: (() -> Unit)? = null
) {
    // Línea de cal (6.1, img 09): FondoCancha arriba, CampoSportPro y botón lima.
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FondoPantalla),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FondoCancha(alturaMinima = 220.dp) {
                if (onVolverAlInicio != null) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable(role = Role.Button, onClick = onVolverAlInicio)
                            .padding(vertical = 6.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Volver al inicio",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
                Text(
                    text = "Sport PRO",
                    style = TextStyle(
                        fontFamily = FamiliaEtiquetas,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        letterSpacing = 2.sp,
                        color = LineaDeCal.colores.lima
                    )
                )
                Text(
                    text = "Bienvenido",
                    style = TextStyle(
                        fontFamily = FamiliaTitulos,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 32.sp,
                        color = Color.White
                    )
                )
            }

            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CampoSportPro(
                    valor = estado.correo,
                    onValorChange = onCorreo,
                    etiqueta = "Correo electrónico",
                    placeholder = "nombre@correo.com",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                CampoSportPro(
                    valor = estado.contrasena,
                    onValorChange = onContrasena,
                    etiqueta = "Contraseña",
                    placeholder = "Tu contraseña",
                    // 7.5: CampoSportPro trae el botón para mostrar u ocultar la contraseña
                    esContrasena = true
                )

                estado.error?.let {
                    Text(it, color = LineaDeCal.colores.errorTexto, fontSize = 14.sp)
                }

                BotonPrincipal(
                    texto = "Iniciar sesión",
                    onClick = onIniciarSesion,
                    cargando = estado.cargando,
                    icono = Icons.AutoMirrored.Rounded.ArrowForward
                )

                Text(
                    text = "¿Olvidaste tu contraseña?",
                    color = VerdeOscuro,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onIrARecuperar)
                        .padding(top = 4.dp, bottom = 4.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
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