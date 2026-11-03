package com.example.projectsportpro2026_2.ui.vinculaciones

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.projectsportpro2026_2.datos.repositorio.DatosSensiblesVinculo

// Colores locales de esta pantalla (no se modifica el tema global)
private val VerdePrincipal = Color(0xFF1B9A5C)
private val VerdeOscuro = Color(0xFF14804B)
private val VerdeClaro = Color(0xFFEAF7EF)
private val FondoPantalla = Color(0xFFFAFAF7)
private val TituloOscuro = Color(0xFF0F2438)
private val BordeCampo = Color(0xFFD5E3DC)
private val TextoSecundario = Color(0xFF6B7A80)
private val TextoEtiqueta = Color(0xFF5B6B73)
private val GrisInactivo = Color(0xFFEEF1F0)
private val AvisoFondo = Color(0xFFFFF3DC)
private val AvisoTexto = Color(0xFF8A5A12)

@Composable
fun PantallaMisJugadores(
    onVolver: () -> Unit,
    viewModel: MisJugadoresViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()
    var mostrarDialogo by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = FondoPantalla,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.limpiarError()
                    mostrarDialogo = true
                },
                containerColor = VerdePrincipal,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    "+ Solicitar",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    ) { relleno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno)
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
                    text = "APODERADO",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 2.sp,
                        color = VerdePrincipal
                    )
                )
                Text(
                    text = "Mis jugadores",
                    style = TextStyle(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 30.sp,
                        color = TituloOscuro
                    )
                )
                Text(
                    text = "Jugadores vinculados a tu cuenta y tus solicitudes.",
                    color = TextoSecundario,
                    fontSize = 14.sp
                )
            }

            // Pestañas de filtro: Activo, Pendiente, Inactivo, Todos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FiltroVinculacion.values().forEach { filtro ->
                    val seleccionado = estado.filtro == filtro
                    val modificador = Modifier
                        .weight(1f)
                        .height(40.dp)
                    val relleno2 = PaddingValues(horizontal = 2.dp)
                    if (seleccionado) {
                        Button(
                            onClick = { viewModel.cambiarFiltro(filtro) },
                            modifier = modificador,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VerdePrincipal,
                                contentColor = Color.White
                            ),
                            contentPadding = relleno2
                        ) {
                            Text(
                                filtro.etiqueta,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        OutlinedButton(
                            onClick = { viewModel.cambiarFiltro(filtro) },
                            modifier = modificador,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, BordeCampo),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.White,
                                contentColor = TextoSecundario
                            ),
                            contentPadding = relleno2
                        ) {
                            Text(
                                filtro.etiqueta,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            if (!mostrarDialogo) {
                estado.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }

            if (estado.cargando && estado.vinculaciones.isEmpty()) {
                CircularProgressIndicator(color = VerdePrincipal)
            } else if (estado.visibles.isEmpty()) {
                Text(
                    "No hay jugadores en esta vista. Usa + Solicitar para vincular uno.",
                    color = TextoSecundario
                )
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 88.dp)
            ) {
                items(estado.visibles, key = { it.id }) { vinculacion ->
                    TarjetaVinculacion(
                        vinculacion = vinculacion,
                        datos = estado.datos[vinculacion.id],
                        onDesvincular = { viewModel.solicitarDesvinculacion(vinculacion) }
                    )
                }
            }
        }
    }

    if (mostrarDialogo) {
        DialogoSolicitud(
            cargando = estado.cargando,
            error = estado.error,
            onEnviar = { documento ->
                viewModel.solicitar(documento) { mostrarDialogo = false }
            },
            onCancelar = { mostrarDialogo = false }
        )
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
private fun FilaDato(etiqueta: String, valor: String) {
    Column {
        Text(
            text = etiqueta.uppercase(),
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                color = TextoEtiqueta
            )
        )
        Text(valor, color = TituloOscuro, fontSize = 15.sp)
    }
}

@Composable
private fun TarjetaVinculacion(
    vinculacion: Vinculacion,
    datos: DatosSensiblesVinculo?,
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
                "Solicitud: ${formatoFecha(vinculacion.fechaSolicitud)}",
                color = TextoSecundario,
                fontSize = 14.sp
            )

            when (vinculacion.estado) {
                EstadoVinculacion.PENDIENTE ->
                    // En estado pendiente NO se muestran datos sensibles
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(AvisoFondo)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            "Esperando la aprobación de la academia",
                            color = AvisoTexto,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }

                EstadoVinculacion.ACTIVA -> {
                    Text(
                        "Aprobado por ${vinculacion.nombreAprobador} " +
                                "el ${formatoFecha(vinculacion.fechaAprobacion)}",
                        color = TextoSecundario,
                        fontSize = 14.sp
                    )
                    if (datos != null) {
                        Spacer(Modifier.height(4.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(VerdeClaro)
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilaDato(
                                "Fecha de nacimiento",
                                datos.fechaNacimiento.ifBlank { "Pendiente" }
                            )
                            FilaDato("Posición", datos.posicion.ifBlank { "Pendiente" })
                            FilaDato("Teléfono", datos.telefono.ifBlank { "Pendiente" })
                        }
                    }
                    if (vinculacion.desvinculacionSolicitada) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(AvisoFondo)
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                "Desvinculación solicitada. Esperando a la academia.",
                                color = AvisoTexto,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = onDesvincular) {
                                Text(
                                    "Solicitar desvinculación",
                                    color = TextoSecundario,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                EstadoVinculacion.INACTIVA ->
                    Text("Vinculación inactiva", color = TextoSecundario, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun DialogoSolicitud(
    cargando: Boolean,
    error: String?,
    onEnviar: (String) -> Unit,
    onCancelar: () -> Unit
) {
    var documento by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onCancelar,
        containerColor = FondoPantalla,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                "Solicitar vinculación",
                style = TextStyle(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    color = TituloOscuro
                )
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Escribe el documento de identidad del jugador. La academia revisará tu solicitud.",
                    color = TextoSecundario,
                    fontSize = 14.sp
                )
                OutlinedTextField(
                    value = documento,
                    onValueChange = { documento = it },
                    label = { Text("Documento del jugador") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = VerdePrincipal,
                        unfocusedBorderColor = BordeCampo,
                        focusedLabelColor = VerdeOscuro,
                        cursorColor = VerdePrincipal,
                        focusedTextColor = TituloOscuro,
                        unfocusedTextColor = TituloOscuro
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(
                onClick = { onEnviar(documento) },
                enabled = !cargando,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VerdePrincipal,
                    contentColor = Color.White
                )
            ) { Text("Solicitar", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text("Cancelar", color = TextoSecundario, fontWeight = FontWeight.SemiBold)
            }
        }
    )
}