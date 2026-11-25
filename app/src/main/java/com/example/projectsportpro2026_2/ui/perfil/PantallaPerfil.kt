package com.example.projectsportpro2026_2.ui.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.LockReset
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectsportpro2026_2.datos.modelo.EstadoCuenta
import com.example.projectsportpro2026_2.datos.modelo.PerfilUsuario
import com.example.projectsportpro2026_2.ui.autenticacion.RecuperarViewModel
import com.example.projectsportpro2026_2.ui.comun.BotonPrincipal
import com.example.projectsportpro2026_2.ui.comun.CabeceraCancha
import com.example.projectsportpro2026_2.ui.comun.ChipEstado
import com.example.projectsportpro2026_2.ui.comun.TipoEstado
import com.example.projectsportpro2026_2.ui.comun.VarianteBoton
import com.example.projectsportpro2026_2.ui.comun.nombreRol
import com.example.projectsportpro2026_2.ui.theme.EstiloEtiqueta
import com.example.projectsportpro2026_2.ui.theme.FamiliaCuerpo
import com.example.projectsportpro2026_2.ui.theme.FamiliaTitulos
import com.example.projectsportpro2026_2.ui.theme.LineaDeCal

/*
 * Línea de cal · Perfil (pestaña de la barra inferior). Solo lectura: nombre, correo, rol,
 * academia y estado de la cuenta.
 *  - "Cambiar contraseña" reutiliza RecuperarViewModel.enviar(), la misma función de envío
 *    de correo de la pantalla Recuperar, con el correo de la cuenta.
 *  - "Cerrar sesión" recibe el mismo callback que el inicio (onCerrarSesion de NavegacionApp).
 */
@Composable
fun PantallaPerfil(
    onVolver: () -> Unit,
    onCerrarSesion: () -> Unit,
    viewModel: PerfilViewModel = viewModel(),
    recuperar: RecuperarViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()
    val envio by recuperar.estado.collectAsState()
    val c = LineaDeCal.colores

    Scaffold(containerColor = c.fondo) { relleno ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CabeceraCancha(
                    titulo = estado.perfil?.nombre?.ifBlank { null } ?: "Perfil",
                    subtitulo = estado.perfil?.let { nombreRol(it.rol) },
                    etiqueta = "PERFIL",
                    onVolver = onVolver
                ) {
                    estado.perfil?.let { perfil ->
                        Spacer(Modifier.height(12.dp))
                        AvatarIniciales(perfil.nombre)
                    }
                }

                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val perfil = estado.perfil
                    when {
                        estado.cargando && perfil == null ->
                            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = c.verde)
                            }

                        perfil == null -> {
                            Text(
                                estado.error ?: "No se pudo cargar tu perfil",
                                color = c.errorTexto,
                                fontFamily = FamiliaCuerpo,
                                fontSize = 14.sp
                            )
                            BotonPrincipal(
                                texto = "Reintentar",
                                onClick = viewModel::cargar,
                                variante = VarianteBoton.NAVY,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        else -> {
                            TarjetaDatos(perfil, estado)

                            BotonPrincipal(
                                texto = "Cambiar contraseña",
                                onClick = {
                                    recuperar.cambiarCorreo(perfil.correo)
                                    recuperar.enviar()
                                },
                                variante = VarianteBoton.NAVY,
                                icono = Icons.Rounded.LockReset,
                                habilitado = !envio.cargando,
                                cargando = envio.cargando,
                                modifier = Modifier.fillMaxWidth()
                            )

                            envio.mensaje?.let {
                                AvisoEnvio(it, ok = true)
                            }
                            envio.error?.let {
                                AvisoEnvio(it, ok = false)
                            }
                        }
                    }

                    TextButton(
                        onClick = onCerrarSesion,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.Logout,
                            contentDescription = null,
                            tint = c.rojo,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Cerrar sesión",
                            color = c.rojo,
                            fontFamily = FamiliaCuerpo,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AvatarIniciales(nombre: String) {
    val c = LineaDeCal.colores
    val iniciales = nombre.trim().split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { "?" }
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(c.lima)
            .border(3.dp, Color.White.copy(alpha = 0.85f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            iniciales,
            color = c.navy,
            fontFamily = FamiliaTitulos,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp
        )
    }
}

@Composable
private fun TarjetaDatos(perfil: PerfilUsuario, estado: EstadoPerfil) {
    val c = LineaDeCal.colores
    val academia = when {
        !estado.academiasCargadas -> "Cargando…"
        estado.academias == null -> "No se pudo cargar"
        estado.academias.isEmpty() -> "Sin academia asignada"
        else -> estado.academias.joinToString(" · ")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(c.superficie)
            .border(1.dp, c.borde, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        FilaDato("Nombre", perfil.nombre.ifBlank { "—" })
        HorizontalDivider(color = c.borde)
        FilaDato("Correo", perfil.correo.ifBlank { "—" })
        HorizontalDivider(color = c.borde)
        FilaDato("Rol", nombreRol(perfil.rol))
        HorizontalDivider(color = c.borde)
        FilaDato("Academia", academia)
        HorizontalDivider(color = c.borde)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "ESTADO DE LA CUENTA",
                style = EstiloEtiqueta,
                color = c.textoSecundario,
                modifier = Modifier.weight(1f)
            )
            if (perfil.estado == EstadoCuenta.ACTIVA) {
                ChipEstado("Activa", TipoEstado.OK, conPunto = true)
            } else {
                ChipEstado("Pendiente", TipoEstado.PENDIENTE, conPunto = true)
            }
        }
    }
}

@Composable
private fun FilaDato(etiqueta: String, valor: String) {
    val c = LineaDeCal.colores
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(etiqueta.uppercase(), style = EstiloEtiqueta, color = c.textoSecundario)
        Text(
            valor,
            color = c.textoPrincipal,
            fontFamily = FamiliaCuerpo,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )
    }
}

@Composable
private fun AvisoEnvio(texto: String, ok: Boolean) {
    val c = LineaDeCal.colores
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (ok) c.okFondo else c.errorFondo)
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            if (ok) Icons.Rounded.CheckCircle else Icons.Rounded.ErrorOutline,
            contentDescription = null,
            tint = if (ok) c.okTexto else c.errorTexto,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            texto,
            color = if (ok) c.okTexto else c.errorTexto,
            fontFamily = FamiliaCuerpo,
            fontSize = 14.sp
        )
    }
}
