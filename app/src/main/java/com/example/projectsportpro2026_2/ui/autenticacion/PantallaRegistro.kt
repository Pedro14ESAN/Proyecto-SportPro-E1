package com.example.projectsportpro2026_2.ui.autenticacion

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectsportpro2026_2.datos.modelo.PerfilUsuario
import com.example.projectsportpro2026_2.datos.modelo.Rol
import com.example.projectsportpro2026_2.ui.theme.ProjectSportPro2026_2Theme

// Colores locales de esta pantalla (no se modifica el tema global)
private val VerdePrincipal = Color(0xFF1B9A5C)
private val VerdeOscuro = Color(0xFF14804B)
private val VerdeClaro = Color(0xFFEAF7EF)
private val FondoPantalla = Color(0xFFFAFAF7)
private val TituloOscuro = Color(0xFF0F2438)
private val TextoEtiqueta = Color(0xFF5B6B73)
private val BordeCampo = Color(0xFFD5E3DC)
private val TextoSecundario = Color(0xFF6B7A80)
private val AvisoFondo = Color(0xFFFFF3DC)
private val AvisoTexto = Color(0xFF8A5A12)

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
private fun TarjetaRol(
    icono: String,
    titulo: String,
    descripcion: String,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    val forma = RoundedCornerShape(18.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(if (seleccionado) VerdeClaro else Color.White)
            .border(
                width = if (seleccionado) 1.5.dp else 1.dp,
                color = if (seleccionado) VerdePrincipal else BordeCampo,
                shape = forma
            )
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (seleccionado) Color(0xFFCDEFDA) else Color(0xFFE3F1E3)),
            contentAlignment = Alignment.Center
        ) {
            Text(icono, fontSize = 20.sp)
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                titulo,
                color = TituloOscuro,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Text(descripcion, color = TextoSecundario, fontSize = 12.sp)
        }
        RadioButton(
            selected = seleccionado,
            onClick = null,
            colors = RadioButtonDefaults.colors(
                selectedColor = VerdePrincipal,
                unselectedColor = Color(0xFFB8C4C9)
            )
        )
    }
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

    val rolEnTexto = when (estado.rol) {
        Rol.JUG -> "jugador"
        Rol.PAD -> "apoderado"
        Rol.DT -> "entrenador"
        Rol.ADM -> "administrador"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FondoPantalla)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "← Volver",
            color = VerdeOscuro,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            modifier = Modifier.clickable(onClick = onIrALogin)
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "CREAR CUENTA",
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 2.sp,
                color = VerdePrincipal
            )
        )
        Text(
            text = "¿Cómo participas?",
            style = TextStyle(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 30.sp,
                color = TituloOscuro
            )
        )
        Text(
            text = "Tu experiencia se adapta a tu relación con la academia.",
            color = TextoSecundario,
            fontSize = 14.sp
        )

        Spacer(Modifier.height(8.dp))

        TarjetaRol(
            icono = "⚽",
            titulo = "Jugador",
            descripcion = "Sigo mi rendimiento y respondo convocatorias.",
            seleccionado = estado.rol == Rol.JUG,
            onClick = { onRol(Rol.JUG) }
        )
        TarjetaRol(
            icono = "🤍",
            titulo = "Padre / Apoderado",
            descripcion = "Acompaño y gestiono a mi jugador vinculado.",
            seleccionado = estado.rol == Rol.PAD,
            onClick = { onRol(Rol.PAD) }
        )
        TarjetaRol(
            icono = "♟",
            titulo = "Entrenador / DT",
            descripcion = "Dirijo equipo, sesiones y partidos.",
            seleccionado = estado.rol == Rol.DT,
            onClick = { onRol(Rol.DT) }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(AvisoFondo)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("ⓘ", color = AvisoTexto, fontSize = 18.sp)
            Spacer(Modifier.width(10.dp))
            Text(
                text = buildAnnotatedString {
                    append("El rol ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Administrador") }
                    append(" es asignado por la academia y no está disponible para registro público.")
                },
                color = AvisoTexto,
                fontSize = 12.sp
            )
        }

        Spacer(Modifier.height(12.dp))

        EtiquetaCampo("NOMBRE COMPLETO")
        OutlinedTextField(
            value = estado.nombre,
            onValueChange = onNombre,
            placeholder = { Text("Tu nombre y apellido") },
            singleLine = true,
            shape = formaCampo,
            colors = coloresCampo,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(4.dp))

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

        EtiquetaCampo("CONTRASEÑA (MÍNIMO 6 CARACTERES)")
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

        if (estado.rol == Rol.JUG) {
            Spacer(Modifier.height(4.dp))

            EtiquetaCampo("FECHA DE NACIMIENTO")
            OutlinedTextField(
                value = estado.fechaNacimiento,
                onValueChange = onFecha,
                placeholder = { Text("aaaa-MM-dd") },
                supportingText = {
                    Text("Menores de 12 años no crean cuenta. De 12 a 17 requieren autorización del apoderado.")
                },
                singleLine = true,
                shape = formaCampo,
                colors = coloresCampo,
                modifier = Modifier.fillMaxWidth()
            )
        }

        estado.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = onRegistrar,
            enabled = !estado.cargando,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = TituloOscuro,
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
                    "Registrarme como $rolEnTexto →",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "¿Ya tienes cuenta? ",
                color = TextoSecundario,
                fontSize = 14.sp
            )
            Text(
                text = "Iniciar sesión",
                color = VerdeOscuro,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.clickable(onClick = onIrALogin)
            )
        }
    }
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