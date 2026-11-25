package com.example.projectsportpro2026_2.ui.vinculaciones

import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import com.example.projectsportpro2026_2.ui.comun.TipoEstado
import com.example.projectsportpro2026_2.ui.comun.ChipEstado
import com.example.projectsportpro2026_2.ui.comun.CabeceraCancha
import com.example.projectsportpro2026_2.ui.theme.FamiliaEtiquetas
import com.example.projectsportpro2026_2.ui.theme.FamiliaTitulos
import com.example.projectsportpro2026_2.ui.theme.LineaDeCal
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
import com.example.projectsportpro2026_2.ui.comun.fechaLatina

// Colores locales de esta pantalla (no se modifica el tema global)
private val VerdePrincipal: Color @Composable get() = LineaDeCal.colores.verde // antes Color(0xFF1B9A5C)
private val VerdeOscuro: Color @Composable get() = LineaDeCal.colores.okTexto // antes Color(0xFF14804B)
private val VerdeClaro: Color @Composable get() = LineaDeCal.colores.okFondo // antes Color(0xFFEAF7EF)
private val FondoPantalla: Color @Composable get() = LineaDeCal.colores.fondo // antes Color(0xFFFAFAF7)
private val TituloOscuro: Color @Composable get() = LineaDeCal.colores.textoPrincipal // antes Color(0xFF0F2438)
private val BordeCampo: Color @Composable get() = LineaDeCal.colores.borde // antes Color(0xFFD5E3DC)
private val TextoSecundario: Color @Composable get() = LineaDeCal.colores.textoSecundario // antes Color(0xFF6B7A80)
private val TextoEtiqueta: Color @Composable get() = LineaDeCal.colores.textoSecundario // antes Color(0xFF5B6B73)
private val GrisInactivo: Color @Composable get() = LineaDeCal.colores.apagadoFondo // antes Color(0xFFEEF1F0)
private val AvisoFondo: Color @Composable get() = LineaDeCal.colores.amarilloFondo // antes Color(0xFFFFF3DC)
private val AvisoTexto: Color @Composable get() = LineaDeCal.colores.amarilloTexto // antes Color(0xFF8A5A12)

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
                containerColor = LineaDeCal.colores.lima,
                contentColor = LineaDeCal.colores.navy,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null)
                    Text(" Solicitar", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { relleno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Línea de cal: cabecera de césped (mismos textos y el mismo "Volver")
            CabeceraCancha(
                titulo = "Mis jugadores",
                subtitulo = "Jugadores vinculados a tu cuenta y tus solicitudes.",
                etiqueta = "APODERADO",
                onVolver = onVolver,
                descripcionVolver = "Volver"
            )

            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

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
                                containerColor = LineaDeCal.colores.superficie,
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
    // Línea de cal: ChipEstado con los mismos estados y textos
    ChipEstado(
        etiquetaEstado(estado),
        when (estado) {
            EstadoVinculacion.PENDIENTE -> TipoEstado.PENDIENTE
            EstadoVinculacion.ACTIVA -> TipoEstado.OK
            EstadoVinculacion.INACTIVA -> TipoEstado.APAGADO
        }
    )
}

@Composable
private fun FilaDato(etiqueta: String, valor: String) {
    Column {
        Text(
            text = etiqueta.uppercase(),
            style = TextStyle(
                fontFamily = FamiliaEtiquetas,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
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
        colors = CardDefaults.cardColors(containerColor = LineaDeCal.colores.superficie),
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
                                fechaLatina(datos.fechaNacimiento).ifBlank { "Pendiente" }
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
                    fontFamily = FamiliaTitulos,
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
                        focusedContainerColor = LineaDeCal.colores.superficie,
                        unfocusedContainerColor = LineaDeCal.colores.superficie,
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