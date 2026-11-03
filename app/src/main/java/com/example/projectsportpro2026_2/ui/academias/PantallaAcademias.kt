package com.example.projectsportpro2026_2.ui.academias

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
import com.example.projectsportpro2026_2.datos.modelo.Academia

// Colores locales de esta pantalla (no se modifica el tema global)
private val VerdePrincipal = Color(0xFF1B9A5C)
private val VerdeOscuro = Color(0xFF14804B)
private val VerdeClaro = Color(0xFFEAF7EF)
private val FondoPantalla = Color(0xFFFAFAF7)
private val TituloOscuro = Color(0xFF0F2438)
private val BordeCampo = Color(0xFFD5E3DC)
private val TextoSecundario = Color(0xFF6B7A80)
private val GrisInactivo = Color(0xFFEEF1F0)

@Composable
fun PantallaAcademias(
    onVolver: () -> Unit,
    onAbrirEquipos: (idAcademia: String, nombre: String) -> Unit,
    onAbrirPartidos: (idAcademia: String, nombre: String) -> Unit,
    onAbrirCatalogo: (idAcademia: String) -> Unit,
    viewModel: AcademiasViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()
    var mostrarFormulario by remember { mutableStateOf(false) }
    var enEdicion by remember { mutableStateOf<Academia?>(null) }

    Scaffold(
        containerColor = FondoPantalla,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.limpiarError()
                    enEdicion = null
                    mostrarFormulario = true
                },
                containerColor = VerdePrincipal,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) { Text("+", fontSize = 26.sp, fontWeight = FontWeight.Bold) }
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
                    text = "Mis academias",
                    style = TextStyle(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 30.sp,
                        color = TituloOscuro
                    )
                )
                Text(
                    text = "Administra tus academias, equipos y partidos.",
                    color = TextoSecundario,
                    fontSize = 14.sp
                )
            }

            Spacer(Modifier.height(4.dp))

            if (!mostrarFormulario) {
                estado.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }

            if (estado.cargando && estado.academias.isEmpty()) {
                CircularProgressIndicator(color = VerdePrincipal)
            } else if (estado.academias.isEmpty()) {
                Text(
                    "Aún no tienes academias. Pulsa + para crear una.",
                    color = TextoSecundario
                )
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 88.dp)
            ) {
                items(estado.academias, key = { it.id }) { academia ->
                    TarjetaAcademia(
                        academia = academia,
                        onEquipos = { onAbrirEquipos(academia.id, academia.nombre) },
                        onPartidos = {
                            onAbrirPartidos(
                                academia.id,
                                academia.nombre
                            )
                        },
                        onCatalogo = {
                            onAbrirCatalogo(
                                academia.id
                            )
                        },
                        onEditar = {
                            viewModel.limpiarError()
                            enEdicion = academia
                            mostrarFormulario = true
                        },
                        onCambiarEstado = { viewModel.cambiarEstado(academia) }
                    )
                }
            }
        }
    }

    if (mostrarFormulario) {
        FormularioAcademia(
            inicial = enEdicion,
            cargando = estado.cargando,
            error = estado.error,
            onGuardar = { nombre, direccion, descripcion ->
                viewModel.guardar(enEdicion?.id, nombre, direccion, descripcion) {
                    mostrarFormulario = false
                }
            },
            onCancelar = { mostrarFormulario = false }
        )
    }
}

@Composable
private fun BotonSeccion(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = VerdeClaro,
            contentColor = VerdeOscuro
        ),
        contentPadding = PaddingValues(horizontal = 4.dp),
        modifier = modifier.height(40.dp)
    ) {
        Text(texto, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}

@Composable
private fun TarjetaAcademia(
    academia: Academia,
    onEquipos: () -> Unit,
    onPartidos: () -> Unit,
    onCatalogo: () -> Unit,
    onEditar: () -> Unit,
    onCambiarEstado: () -> Unit
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
                    academia.nombre,
                    color = TituloOscuro,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (academia.activa) VerdeClaro else GrisInactivo)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        if (academia.activa) "Activa" else "Inactiva",
                        color = if (academia.activa) VerdeOscuro else TextoSecundario,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
            if (academia.direccion.isNotBlank()) {
                Text(academia.direccion, color = TextoSecundario, fontSize = 14.sp)
            }
            if (academia.descripcion.isNotBlank()) {
                Text(academia.descripcion, color = TextoSecundario, fontSize = 14.sp)
            }

            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BotonSeccion("Equipos", onEquipos, Modifier.weight(1f))
                BotonSeccion("Partidos", onPartidos, Modifier.weight(1f))
                BotonSeccion("Catálogo", onCatalogo, Modifier.weight(1f))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onEditar) {
                    Text("Editar", color = VerdeOscuro, fontWeight = FontWeight.SemiBold)
                }
                TextButton(onClick = onCambiarEstado) {
                    Text(
                        if (academia.activa) "Inactivar" else "Activar",
                        color = if (academia.activa) TextoSecundario else VerdeOscuro,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun FormularioAcademia(
    inicial: Academia?,
    cargando: Boolean,
    error: String?,
    onGuardar: (nombre: String, direccion: String, descripcion: String) -> Unit,
    onCancelar: () -> Unit
) {
    var nombre by remember { mutableStateOf(inicial?.nombre.orEmpty()) }
    var direccion by remember { mutableStateOf(inicial?.direccion.orEmpty()) }
    var descripcion by remember { mutableStateOf(inicial?.descripcion.orEmpty()) }

    val formaCampo = RoundedCornerShape(16.dp)
    val coloresCampo = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        focusedBorderColor = VerdePrincipal,
        unfocusedBorderColor = BordeCampo,
        focusedLabelColor = VerdeOscuro,
        cursorColor = VerdePrincipal,
        focusedTextColor = TituloOscuro,
        unfocusedTextColor = TituloOscuro
    )

    AlertDialog(
        onDismissRequest = onCancelar,
        containerColor = FondoPantalla,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                if (inicial == null) "Nueva academia" else "Editar academia",
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
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre *") },
                    singleLine = true,
                    shape = formaCampo,
                    colors = coloresCampo
                )
                OutlinedTextField(
                    value = direccion,
                    onValueChange = { direccion = it },
                    label = { Text("Dirección") },
                    singleLine = true,
                    shape = formaCampo,
                    colors = coloresCampo
                )
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    shape = formaCampo,
                    colors = coloresCampo
                )
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(
                onClick = { onGuardar(nombre, direccion, descripcion) },
                enabled = !cargando,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VerdePrincipal,
                    contentColor = Color.White
                )
            ) { Text("Guardar", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text("Cancelar", color = TextoSecundario, fontWeight = FontWeight.SemiBold)
            }
        }
    )
}