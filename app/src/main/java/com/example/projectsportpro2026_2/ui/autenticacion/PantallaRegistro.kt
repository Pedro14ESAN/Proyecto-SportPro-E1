package com.example.projectsportpro2026_2.ui.autenticacion

import com.example.projectsportpro2026_2.ui.comun.BotonPrincipal
import com.example.projectsportpro2026_2.ui.comun.CampoSportPro
import com.example.projectsportpro2026_2.ui.comun.FondoCancha
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.SportsKabaddi
import androidx.compose.material.icons.rounded.FamilyRestroom
import androidx.compose.material.icons.rounded.SportsSoccer
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.layout.widthIn
import com.example.projectsportpro2026_2.ui.theme.FamiliaEtiquetas
import com.example.projectsportpro2026_2.ui.theme.FamiliaTitulos
import com.example.projectsportpro2026_2.ui.theme.LineaDeCal
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
import com.example.projectsportpro2026_2.ui.comun.CampoFecha
import com.example.projectsportpro2026_2.ui.theme.ProjectSportPro2026_2Theme

// Colores locales de esta pantalla (no se modifica el tema global)
private val VerdePrincipal: Color @Composable get() = LineaDeCal.colores.verde // antes Color(0xFF1B9A5C)
private val VerdeOscuro: Color @Composable get() = LineaDeCal.colores.okTexto // antes Color(0xFF14804B)
private val VerdeClaro: Color @Composable get() = LineaDeCal.colores.okFondo // antes Color(0xFFEAF7EF)
private val FondoPantalla: Color @Composable get() = LineaDeCal.colores.fondo // antes Color(0xFFFAFAF7)
private val TituloOscuro: Color @Composable get() = LineaDeCal.colores.textoPrincipal // antes Color(0xFF0F2438)
private val TextoEtiqueta: Color @Composable get() = LineaDeCal.colores.textoSecundario // antes Color(0xFF5B6B73)
private val BordeCampo: Color @Composable get() = LineaDeCal.colores.borde // antes Color(0xFFD5E3DC)
private val TextoSecundario: Color @Composable get() = LineaDeCal.colores.textoSecundario // antes Color(0xFF6B7A80)
private val AvisoFondo: Color @Composable get() = LineaDeCal.colores.amarilloFondo // antes Color(0xFFFFF3DC)
private val AvisoTexto: Color @Composable get() = LineaDeCal.colores.amarilloTexto // antes Color(0xFF8A5A12)

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
        onDocumento = viewModel::cambiarDocumento,
        onRegistrar = viewModel::registrar,
        onIrALogin = onIrALogin
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
private fun TarjetaRol(
    icono: ImageVector,
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
            .background(if (seleccionado) VerdeClaro else LineaDeCal.colores.superficie)
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
                .background(if (seleccionado) LineaDeCal.colores.lima else LineaDeCal.colores.okFondo),
            contentAlignment = Alignment.Center
        ) {
            Icon(icono, contentDescription = null, tint = if (seleccionado) LineaDeCal.colores.navy else VerdePrincipal)
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
                unselectedColor = LineaDeCal.colores.textoSecundario
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
    onDocumento: (String) -> Unit,
    onRegistrar: () -> Unit,
    onIrALogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formaCampo = RoundedCornerShape(16.dp)
    val coloresCampo = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = LineaDeCal.colores.superficie,
        unfocusedContainerColor = LineaDeCal.colores.superficie,
        focusedBorderColor = VerdePrincipal,
        unfocusedBorderColor = BordeCampo,
        cursorColor = VerdePrincipal,
        focusedTextColor = TituloOscuro,
        unfocusedTextColor = TituloOscuro,
        focusedPlaceholderColor = TextoSecundario,
        unfocusedPlaceholderColor = TextoSecundario
    )

    val rolEnTexto = when (estado.rol) {
        Rol.JUG -> "jugador"
        Rol.PAD -> "apoderado"
        Rol.DT -> "entrenador"
        Rol.ADM -> "administrador"
    }

    // Línea de cal (6.1): mismo estilo que el login, con todos los campos y validaciones actuales
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
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FondoCancha(alturaMinima = 200.dp) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable(role = Role.Button, onClick = onIrALogin)
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
                    text = "Volver",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
            Text(
                text = "CREAR CUENTA",
                style = TextStyle(
                    fontFamily = FamiliaEtiquetas,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    letterSpacing = 2.sp,
                    color = LineaDeCal.colores.lima
                )
            )
            Text(
                text = "¿Cómo participas?",
                style = TextStyle(
                    fontFamily = FamiliaTitulos,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 30.sp,
                    color = Color.White
                )
            )
            Text(
                text = "Tu experiencia se adapta a tu relación con la academia.",
                color = Color.White.copy(alpha = 0.88f),
                fontSize = 14.sp
            )
        }

        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

        TarjetaRol(
            icono = Icons.Rounded.SportsSoccer,
            titulo = "Jugador",
            descripcion = "Sigo mi rendimiento y respondo convocatorias.",
            seleccionado = estado.rol == Rol.JUG,
            onClick = { onRol(Rol.JUG) }
        )
        TarjetaRol(
            icono = Icons.Rounded.FamilyRestroom,
            titulo = "Padre / Apoderado",
            descripcion = "Acompaño y gestiono a mi jugador vinculado.",
            seleccionado = estado.rol == Rol.PAD,
            onClick = { onRol(Rol.PAD) }
        )
        TarjetaRol(
            icono = Icons.Rounded.SportsKabaddi,
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
            Icon(Icons.Rounded.Info, contentDescription = null, tint = AvisoTexto, modifier = Modifier.size(20.dp))
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

        CampoSportPro(
            valor = estado.nombre,
            onValorChange = onNombre,
            etiqueta = "Nombre completo",
            placeholder = "Tu nombre y apellido"
        )

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
            etiqueta = "Contraseña (mínimo 6 caracteres)",
            placeholder = "Tu contraseña",
            // 7.5: botón para mostrar u ocultar la contraseña
            esContrasena = true
        )

        if (estado.rol == Rol.JUG) {
            Spacer(Modifier.height(4.dp))

            EtiquetaCampo("FECHA DE NACIMIENTO")
            CampoFecha(
                valor = estado.fechaNacimiento,
                onCambio = onFecha,
                textoAyuda = "Menores de 12 años no crean cuenta. De 12 a 17 requieren autorización del apoderado.",
                shape = formaCampo,
                colors = coloresCampo
            )
            Spacer(Modifier.height(4.dp))

            CampoSportPro(
                valor = estado.documento,
                onValorChange = onDocumento,
                etiqueta = "Documento de identidad",
                placeholder = "DNI o carné de extranjería",
                ayuda = "Sirve para que la academia vincule tu cuenta con tu ficha."
            )
        }

        estado.error?.let {
            Text(it, color = LineaDeCal.colores.errorTexto, fontSize = 14.sp)
        }

        Spacer(Modifier.height(8.dp))

        BotonPrincipal(
            texto = "Registrarme como $rolEnTexto",
            onClick = onRegistrar,
            cargando = estado.cargando,
            icono = Icons.AutoMirrored.Rounded.ArrowForward
        )

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
            onDocumento = {},
            onRegistrar = {},
            onIrALogin = {}
        )
    }
}