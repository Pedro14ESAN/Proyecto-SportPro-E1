package com.example.projectsportpro2026_2.ui.jugadores

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectsportpro2026_2.datos.modelo.DatosPrivadosJugador
import com.example.projectsportpro2026_2.datos.modelo.Jugador
import com.example.projectsportpro2026_2.datos.modelo.PiesHabiles
import com.example.projectsportpro2026_2.datos.modelo.Posiciones
import androidx.compose.foundation.lazy.itemsIndexed

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
fun PantallaJugadores(
    idEquipo: String,
    nombreEquipo: String,
    onVolver: () -> Unit,
    viewModel: JugadoresViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()
    var mostrarFormulario by remember { mutableStateOf(false) }
    var enEdicion by remember { mutableStateOf<Jugador?>(null) }
    var privadosEdicion by remember { mutableStateOf<DatosPrivadosJugador?>(null) }

    LaunchedEffect(idEquipo) { viewModel.iniciar(idEquipo) }

    Scaffold(
        containerColor = FondoPantalla,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.limpiarError()
                    enEdicion = null
                    privadosEdicion = null
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
                    text = "JUGADORES",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 2.sp,
                        color = VerdePrincipal
                    )
                )
                Text(
                    text = nombreEquipo,
                    style = TextStyle(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 30.sp,
                        color = TituloOscuro
                    )
                )
                Text(
                    text = "Jugadores registrados en este equipo.",
                    color = TextoSecundario,
                    fontSize = 14.sp
                )
            }

            if (!mostrarFormulario) {
                estado.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }

            if (estado.cargando && estado.jugadores.isEmpty()) {
                CircularProgressIndicator(color = VerdePrincipal)
            } else if (estado.jugadores.isEmpty()) {
                Text(
                    "Este equipo aún no tiene jugadores. Pulsa + para registrar uno.",
                    color = TextoSecundario
                )
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 88.dp)
            ) {
                itemsIndexed(
                    estado.jugadores,
                    key = { indice, jugador -> jugador.id.ifBlank { "sin-id-$indice" } }
                ) { _, jugador ->
                    TarjetaJugador(
                        jugador = jugador,
                        onEditar = {
                            viewModel.limpiarError()
                            viewModel.cargarPrivados(jugador) { privados ->
                                enEdicion = jugador
                                privadosEdicion = privados
                                mostrarFormulario = true
                            }
                        },
                        onCambiarEstado = { viewModel.cambiarEstado(jugador) }
                    )
                }
            }
        }
    }

    if (mostrarFormulario) {
        FormularioJugador(
            inicial = enEdicion,
            privados = privadosEdicion,
            cargando = estado.cargando,
            error = estado.error,
            onGuardar = { campos ->
                viewModel.guardar(enEdicion, campos) { mostrarFormulario = false }
            },
            onCancelar = { mostrarFormulario = false }
        )
    }
}

@Composable
private fun TarjetaJugador(
    jugador: Jugador,
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
                    jugador.nombreCompleto(),
                    color = TituloOscuro,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (jugador.activo) VerdeClaro else GrisInactivo)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        if (jugador.activo) "Activo" else "Inactivo",
                        color = if (jugador.activo) VerdeOscuro else TextoSecundario,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
            val detalle = listOf(jugador.categoria, jugador.posicion)
                .filter { it.isNotBlank() }.joinToString(" · ")
            if (detalle.isNotBlank()) {
                Text(detalle, color = TextoSecundario, fontSize = 14.sp)
            }
            if (jugador.esMenorDeEdad()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(AvisoFondo)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        "Menor de edad",
                        color = AvisoTexto,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
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
                        if (jugador.activo) "Inactivar" else "Activar",
                        color = if (jugador.activo) TextoSecundario else VerdeOscuro,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

private fun numeroATexto(valor: Double?): String = when {
    valor == null -> ""
    valor % 1.0 == 0.0 -> valor.toInt().toString()
    else -> valor.toString()
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
private fun TituloSeccion(texto: String) {
    Text(
        text = texto.uppercase(),
        style = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.sp,
            color = VerdeOscuro
        ),
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun Campo(
    valor: String,
    onCambio: (String) -> Unit,
    etiqueta: String,
    teclado: KeyboardOptions = KeyboardOptions.Default
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = coloresCampo(),
        keyboardOptions = teclado,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun FormularioJugador(
    inicial: Jugador?,
    privados: DatosPrivadosJugador?,
    cargando: Boolean,
    error: String?,
    onGuardar: (CamposJugador) -> Unit,
    onCancelar: () -> Unit
) {
    var nombres by remember { mutableStateOf(inicial?.nombres.orEmpty()) }
    var apellidos by remember { mutableStateOf(inicial?.apellidos.orEmpty()) }
    var fecha by remember { mutableStateOf(inicial?.fechaNacimiento.orEmpty()) }
    var documento by remember { mutableStateOf(inicial?.documento.orEmpty()) }
    var posicion by remember { mutableStateOf(inicial?.posicion.orEmpty()) }
    var pie by remember { mutableStateOf(inicial?.piePreferido.orEmpty()) }
    var estatura by remember { mutableStateOf(numeroATexto(privados?.estaturaCm)) }
    var peso by remember { mutableStateOf(numeroATexto(privados?.pesoKg)) }
    var telefono by remember { mutableStateOf(privados?.telefono.orEmpty()) }
    var contactoNombre by remember { mutableStateOf(privados?.contactoEmergenciaNombre.orEmpty()) }
    var contactoTelefono by remember { mutableStateOf(privados?.contactoEmergenciaTelefono.orEmpty()) }

    AlertDialog(
        onDismissRequest = onCancelar,
        containerColor = FondoPantalla,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                if (inicial == null) "Nuevo jugador" else "Editar jugador",
                style = TextStyle(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    color = TituloOscuro
                )
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TituloSeccion("Datos personales")
                Campo(nombres, { nombres = it }, "Nombres *")
                Campo(apellidos, { apellidos = it }, "Apellidos *")
                Campo(fecha, { fecha = it }, "Fecha de nacimiento (aaaa-MM-dd)")
                Campo(documento, { documento = it }, "Documento de identidad")

                TituloSeccion("Datos deportivos")
                Selector("Posición", posicion, Posiciones.lista) { posicion = it }
                Selector("Pie hábil", pie, PiesHabiles.lista) { pie = it }

                TituloSeccion("Datos sensibles (solo personal autorizado)")
                Campo(
                    estatura, { estatura = it }, "Estatura (cm)",
                    KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                Campo(
                    peso, { peso = it }, "Peso (kg)",
                    KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                Campo(
                    telefono, { telefono = it }, "Teléfono del jugador",
                    KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
                Campo(contactoNombre, { contactoNombre = it }, "Contacto de emergencia")
                Campo(
                    contactoTelefono, { contactoTelefono = it }, "Teléfono de emergencia",
                    KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onGuardar(
                        CamposJugador(
                            nombres, apellidos, fecha, documento, posicion, pie,
                            estatura, peso, telefono, contactoNombre, contactoTelefono
                        )
                    )
                },
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
private fun Selector(
    etiqueta: String,
    seleccionado: String,
    opciones: List<String>,
    onElegir: (String) -> Unit
) {
    var abierto by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(
            onClick = { abierto = true },
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
            Text(if (seleccionado.isEmpty()) etiqueta else "$etiqueta: $seleccionado")
        }
        DropdownMenu(
            expanded = abierto,
            onDismissRequest = { abierto = false },
            modifier = Modifier.background(Color.White)
        ) {
            DropdownMenuItem(
                text = { Text("(Sin definir)", color = TextoSecundario) },
                onClick = { onElegir(""); abierto = false }
            )
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion, color = TituloOscuro) },
                    onClick = { onElegir(opcion); abierto = false }
                )
            }
        }
    }
}