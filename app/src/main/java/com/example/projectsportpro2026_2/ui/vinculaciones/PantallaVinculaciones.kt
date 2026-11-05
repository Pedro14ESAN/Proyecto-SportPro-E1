package com.example.projectsportpro2026_2.ui.vinculaciones

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectsportpro2026_2.datos.modelo.EstadoVinculacion
import com.example.projectsportpro2026_2.datos.modelo.Vinculacion

// Colores locales de esta pantalla (no se modifica el tema global)
private val VerdePrincipal = Color(0xFF1B9A5C)
private val VerdeOscuro = Color(0xFF14804B)
private val VerdeClaro = Color(0xFFEAF7EF)
private val FondoPantalla = Color(0xFFFAFAF7)
private val TituloOscuro = Color(0xFF0F2438)
private val BordeCampo = Color(0xFFD5E3DC)
private val TextoSecundario = Color(0xFF6B7A80)
private val GrisInactivo = Color(0xFFEEF1F0)
private val AvisoFondo = Color(0xFFFFF3DC)
private val AvisoTexto = Color(0xFF8A5A12)

@Composable
fun PantallaVinculaciones(
    onVolver: () -> Unit,
    viewModel: VinculacionesViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoPantalla)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "← Volver",
            color = VerdeOscuro,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            modifier = Modifier.clickable(onClick = onVolver)
        )

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "GESTIÓN",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 2.sp,
                    color = VerdePrincipal
                )
            )
            Text(
                text = "Vinculaciones",
                style = TextStyle(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 30.sp,
                    color = TituloOscuro
                )
            )
            Text(
                text = "Solicitudes de apoderados para vincularse a un jugador.",
                color = TextoSecundario,
                fontSize = 14.sp
            )
        }

        // Banner con el número de solicitudes pendientes
        if (estado.pendientes > 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(AvisoFondo)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("ⓘ", color = AvisoTexto, fontSize = 18.sp)
                Spacer(Modifier.padding(horizontal = 5.dp))
                Text(
                    text = if (estado.pendientes == 1) "1 solicitud esperando aprobación"
                    else "${estado.pendientes} solicitudes esperando aprobación",
                    color = AvisoTexto,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        estado.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        if (estado.cargando && estado.vinculaciones.isEmpty()) {
            CircularProgressIndicator(color = VerdePrincipal)
        } else if (estado.vinculaciones.isEmpty()) {
            Text(
                "Aún no hay solicitudes de vinculación en tus academias.",
                color = TextoSecundario
            )
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(estado.vinculaciones, key = { it.id }) { vinculacion ->
                TarjetaAdministrador(
                    vinculacion = vinculacion,
                    onAprobar = { viewModel.aprobar(vinculacion) },
                    onRechazar = { viewModel.rechazar(vinculacion) },
                    onDesvincular = { viewModel.desvincular(vinculacion) }
                )
            }
        }
    }
}

@Composable
private fun EtiquetaEstadoVinculacion(estado: EstadoVinculacion) {
    val fondo = when (estado) {
        EstadoVinculacion.PENDIENTE -> AvisoFondo
        EstadoVinculacion.ACTIVA -> VerdeClaro
        EstadoVinculacion.INACTIVA -> GrisInactivo
    }
    val texto = when (estado) {
        EstadoVinculacion.PENDIENTE -> AvisoTexto
        EstadoVinculacion.ACTIVA -> VerdeOscuro
        EstadoVinculacion.INACTIVA -> TextoSecundario
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(fondo)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            etiquetaEstado(estado),
            color = texto,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun TarjetaAdministrador(
    vinculacion: Vinculacion,
    onAprobar: () -> Unit,
    onRechazar: () -> Unit,
    onDesvincular: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BordeCampo)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    vinculacion.nombreJugador,
                    color = TituloOscuro,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f)
                )
                EtiquetaEstadoVinculacion(vinculacion.estado)
            }
            Text(
                "Apoderado: ${vinculacion.nombreApoderado}",
                color = TextoSecundario,
                fontSize = 14.sp
            )
            Text(
                "Solicitud: ${formatoFecha(vinculacion.fechaSolicitud)}",
                color = TextoSecundario,
                fontSize = 14.sp
            )

            when (vinculacion.estado) {
                EstadoVinculacion.PENDIENTE -> {
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onAprobar,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VerdePrincipal,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) { Text("Autorizar", fontWeight = FontWeight.Bold) }
                        OutlinedButton(
                            onClick = onRechazar,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, BordeCampo),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = TextoSecundario
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) { Text("Rechazar", fontWeight = FontWeight.SemiBold) }
                    }
                }

                EstadoVinculacion.ACTIVA -> {
                    Text(
                        "Aprobado por ${vinculacion.nombreAprobador} " +
                                "el ${formatoFecha(vinculacion.fechaAprobacion)}",
                        color = TextoSecundario,
                        fontSize = 14.sp
                    )
                    if (vinculacion.desvinculacionSolicitada) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(AvisoFondo)
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                "El apoderado solicitó la desvinculación",
                                color = AvisoTexto,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDesvincular) {
                            Text(
                                "Desvincular",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                EstadoVinculacion.INACTIVA -> Unit
            }
        }
    }
}