package com.example.projectsportpro2026_2.ui.equipos

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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.projectsportpro2026_2.datos.modelo.Categorias
import com.example.projectsportpro2026_2.datos.modelo.EntrenadorAcademia
import com.example.projectsportpro2026_2.datos.modelo.Equipo

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
fun PantallaEquipos(
    idAcademia: String,
    nombreAcademia: String,
    onVolver: () -> Unit,
    onAbrirJugadores: (idEquipo: String, nombre: String) -> Unit,
    viewModel: EquiposViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()
    var mostrarFormulario by remember { mutableStateOf(false) }
    var mostrarEntrenadores by remember { mutableStateOf(false) }
    var enEdicion by remember { mutableStateOf<Equipo?>(null) }

    LaunchedEffect(idAcademia) { viewModel.iniciar(idAcademia) }

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
                    text = "EQUIPOS",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 2.sp,
                        color = VerdePrincipal
                    )
                )
                Text(
                    text = nombreAcademia,
                    style = TextStyle(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 30.sp,
                        color = TituloOscuro
                    )
                )
                Text(
                    text = "Equipos y directores técnicos de la academia.",
                    color = TextoSecundario,
                    fontSize = 14.sp
                )
            }

            BotonSeccion(
                texto = "Directores técnicos de la academia",
                onClick = {
                    viewModel.limpiarError()
                    mostrarEntrenadores = true
                },
                modifier = Modifier.fillMaxWidth()
            )

            if (!mostrarFormulario && !mostrarEntrenadores) {
                estado.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }

            if (estado.cargando && estado.equipos.isEmpty()) {
                CircularProgressIndicator(color = VerdePrincipal)
            } else if (estado.equipos.isEmpty()) {
                Text(
                    "Esta academia aún no tiene equipos. Pulsa + para crear uno.",
                    color = TextoSecundario
                )
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 88.dp)
            ) {
                items(estado.equipos, key = { it.id }) { equipo ->
                    TarjetaEquipo(
                        equipo = equipo,
                        nombreEntrenador = estado.entrenadores
                            .firstOrNull { it.uid == equipo.idEntrenador }?.nombre,
                        onJugadores = { onAbrirJugadores(equipo.id, equipo.nombre) },
                        onEditar = {
                            viewModel.limpiarError()
                            enEdicion = equipo
                            mostrarFormulario = true
                        },
                        onCambiarEstado = { viewModel.cambiarEstado(equipo) }
                    )
                }
            }
        }
    }

    if (mostrarFormulario) {
        FormularioEquipo(
            inicial = enEdicion,
            entrenadores = estado.entrenadores,
            cargando = estado.cargando,
            error = estado.error,
            onGuardar = { nombre, categoria, idEntrenador ->
                viewModel.guardar(enEdicion, nombre, categoria, idEntrenador) {
                    mostrarFormulario = false
                }
            },
            onCancelar = { mostrarFormulario = false }
        )
    }

    if (mostrarEntrenadores) {
        DialogoEntrenadores(
            entrenadores = estado.entrenadores,
            cargando = estado.cargando,
            error = estado.error,
            onAgregar = { correo, alTerminar -> viewModel.agregarEntrenador(correo, alTerminar) },
            onCerrar = { mostrarEntrenadores = false }
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
        contentPadding = PaddingValues(horizontal = 12.dp),
        modifier = modifier.height(44.dp)
    ) {
        Text(texto, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}

@Composable
private fun TarjetaEquipo(
    equipo: Equipo,
    nombreEntrenador: String?,
    onJugadores: () -> Unit,
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
                    equipo.nombre,
                    color = TituloOscuro,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (equipo.activo) VerdeClaro else GrisInactivo)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        if (equipo.activo) "Activo" else "Inactivo",
                        color = if (equipo.activo) VerdeOscuro else TextoSecundario,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
            Text("Categoría: ${equipo.categoria}", color = TextoSecundario, fontSize = 14.sp)
            Text(
                "Director técnico: ${nombreEntrenador ?: "Sin asignar"}",
                color = TextoSecundario,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(6.dp))

            BotonSeccion("Jugadores", onJugadores, Modifier.fillMaxWidth())

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onEditar) {
                    Text("Editar", color = VerdeOscuro, fontWeight = FontWeight.SemiBold)
                }
                TextButton(onClick = onCambiarEstado) {
                    Text(
                        if (equipo.activo) "Inactivar" else "Activar",
                        color = if (equipo.activo) TextoSecundario else VerdeOscuro,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun coloresCampo() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    focusedBorderColor = VerdePrincipal,
    unfocusedBorderColor = BordeCampo,
    focusedLabelColor = VerdeOscuro,
    cursorColor = VerdePrincipal,
    focusedTextColor = TituloOscuro,
    unfocusedTextColor = TituloOscuro
)

@Composable
private fun TituloDialogo(texto: String) {
    Text(
        texto,
        style = TextStyle(
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 22.sp,
            color = TituloOscuro
        )
    )
}

@Composable
private fun FormularioEquipo(
    inicial: Equipo?,
    entrenadores: List<EntrenadorAcademia>,
    cargando: Boolean,
    error: String?,
    onGuardar: (nombre: String, categoria: String, idEntrenador: String) -> Unit,
    onCancelar: () -> Unit
) {
    var nombre by remember { mutableStateOf(inicial?.nombre.orEmpty()) }
    var categoria by remember { mutableStateOf(inicial?.categoria.orEmpty()) }
    var idEntrenador by remember { mutableStateOf(inicial?.idEntrenador.orEmpty()) }

    AlertDialog(
        onDismissRequest = onCancelar,
        containerColor = FondoPantalla,
        shape = RoundedCornerShape(24.dp),
        title = { TituloDialogo(if (inicial == null) "Nuevo equipo" else "Editar equipo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del equipo *") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = coloresCampo()
                )
                SelectorCategoria(seleccionada = categoria, onElegir = { categoria = it })
                SelectorEntrenador(
                    seleccionado = idEntrenador,
                    entrenadores = entrenadores,
                    onElegir = { idEntrenador = it }
                )
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(
                onClick = { onGuardar(nombre, categoria, idEntrenador) },
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

@Composable
private fun BotonSelector(texto: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BordeCampo),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = TituloOscuro
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        Text(texto)
    }
}

@Composable
private fun SelectorCategoria(seleccionada: String, onElegir: (String) -> Unit) {
    var abierto by remember { mutableStateOf(false) }
    Box {
        BotonSelector(
            texto = if (seleccionada.isEmpty()) "Elige la categoría *" else "Categoría: $seleccionada",
            onClick = { abierto = true }
        )
        DropdownMenu(
            expanded = abierto,
            onDismissRequest = { abierto = false },
            modifier = Modifier.background(Color.White)
        ) {
            Categorias.lista.forEach { categoria ->
                DropdownMenuItem(
                    text = { Text(categoria, color = TituloOscuro) },
                    onClick = {
                        onElegir(categoria)
                        abierto = false
                    }
                )
            }
        }
    }
}

@Composable
private fun SelectorEntrenador(
    seleccionado: String,
    entrenadores: List<EntrenadorAcademia>,
    onElegir: (String) -> Unit
) {
    var abierto by remember { mutableStateOf(false) }
    val nombre = entrenadores.firstOrNull { it.uid == seleccionado }?.nombre
    Box {
        BotonSelector(
            texto = if (nombre == null) "Director técnico (opcional)" else "Director técnico: $nombre",
            onClick = { abierto = true }
        )
        DropdownMenu(
            expanded = abierto,
            onDismissRequest = { abierto = false },
            modifier = Modifier.background(Color.White)
        ) {
            DropdownMenuItem(
                text = { Text("(Sin asignar)", color = TextoSecundario) },
                onClick = {
                    onElegir("")
                    abierto = false
                }
            )
            entrenadores.forEach { entrenador ->
                DropdownMenuItem(
                    text = { Text(entrenador.nombre, color = TituloOscuro) },
                    onClick = {
                        onElegir(entrenador.uid)
                        abierto = false
                    }
                )
            }
        }
    }
}

@Composable
private fun DialogoEntrenadores(
    entrenadores: List<EntrenadorAcademia>,
    cargando: Boolean,
    error: String?,
    onAgregar: (correo: String, alTerminar: () -> Unit) -> Unit,
    onCerrar: () -> Unit
) {
    var correo by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onCerrar,
        containerColor = FondoPantalla,
        shape = RoundedCornerShape(24.dp),
        title = { TituloDialogo("Directores técnicos") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (entrenadores.isEmpty()) {
                    Text(
                        "Aún no hay directores técnicos en esta academia.",
                        color = TextoSecundario
                    )
                }
                entrenadores.forEach {
                    Text("• ${it.nombre} (${it.correo})", color = TituloOscuro, fontSize = 14.sp)
                }
                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it },
                    label = { Text("Correo del director técnico") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = coloresCampo()
                )
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(
                onClick = { onAgregar(correo) { correo = "" } },
                enabled = !cargando,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VerdePrincipal,
                    contentColor = Color.White
                )
            ) { Text("Agregar", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onCerrar) {
                Text("Cerrar", color = TextoSecundario, fontWeight = FontWeight.SemiBold)
            }
        }
    )
}