package com.example.projectsportpro2026_2.ui.jugadores

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectsportpro2026_2.datos.modelo.DatosPrivadosJugador
import com.example.projectsportpro2026_2.datos.modelo.Jugador
import com.example.projectsportpro2026_2.datos.modelo.PiesHabiles
import com.example.projectsportpro2026_2.datos.modelo.Posiciones

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
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.limpiarError()
                enEdicion = null
                privadosEdicion = null
                mostrarFormulario = true
            }) { Text("+") }
        }
    ) { relleno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onVolver) { Text("← Volver") }
                Text("Jugadores · $nombreEquipo", style = MaterialTheme.typography.titleLarge)
            }

            if (!mostrarFormulario) {
                estado.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }

            if (estado.cargando && estado.jugadores.isEmpty()) {
                CircularProgressIndicator()
            } else if (estado.jugadores.isEmpty()) {
                Text("Este equipo aún no tiene jugadores. Pulsa + para registrar uno.")
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(estado.jugadores, key = { it.id }) { jugador ->
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
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(jugador.nombreCompleto(), style = MaterialTheme.typography.titleMedium)
                Text(
                    if (jugador.activo) "Activo" else "Inactivo",
                    color = if (jugador.activo) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            val detalle = listOf(jugador.categoria, jugador.posicion)
                .filter { it.isNotBlank() }.joinToString(" · ")
            if (detalle.isNotBlank()) Text(detalle)
            if (jugador.esMenorDeEdad()) {
                Text("Menor de edad", style = MaterialTheme.typography.bodySmall)
            }
            Row {
                TextButton(onClick = onEditar) { Text("Editar") }
                TextButton(onClick = onCambiarEstado) {
                    Text(if (jugador.activo) "Inactivar" else "Activar")
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
        title = { Text(if (inicial == null) "Nuevo jugador" else "Editar jugador") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Datos personales", style = MaterialTheme.typography.titleSmall)
                OutlinedTextField(
                    value = nombres, onValueChange = { nombres = it },
                    label = { Text("Nombres *") }, singleLine = true
                )
                OutlinedTextField(
                    value = apellidos, onValueChange = { apellidos = it },
                    label = { Text("Apellidos *") }, singleLine = true
                )
                OutlinedTextField(
                    value = fecha, onValueChange = { fecha = it },
                    label = { Text("Fecha de nacimiento (aaaa-MM-dd)") }, singleLine = true
                )
                OutlinedTextField(
                    value = documento, onValueChange = { documento = it },
                    label = { Text("Documento de identidad") }, singleLine = true
                )

                Text("Datos deportivos", style = MaterialTheme.typography.titleSmall)
                Selector("Posición", posicion, Posiciones.lista) { posicion = it }
                Selector("Pie hábil", pie, PiesHabiles.lista) { pie = it }

                Text(
                    "Datos sensibles (solo personal autorizado)",
                    style = MaterialTheme.typography.titleSmall
                )
                OutlinedTextField(
                    value = estatura, onValueChange = { estatura = it },
                    label = { Text("Estatura (cm)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = peso, onValueChange = { peso = it },
                    label = { Text("Peso (kg)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = telefono, onValueChange = { telefono = it },
                    label = { Text("Teléfono del jugador") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
                OutlinedTextField(
                    value = contactoNombre, onValueChange = { contactoNombre = it },
                    label = { Text("Contacto de emergencia") }, singleLine = true
                )
                OutlinedTextField(
                    value = contactoTelefono, onValueChange = { contactoTelefono = it },
                    label = { Text("Teléfono de emergencia") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
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
                enabled = !cargando
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
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
        OutlinedButton(onClick = { abierto = true }, modifier = Modifier.fillMaxWidth()) {
            Text(if (seleccionado.isEmpty()) etiqueta else "$etiqueta: $seleccionado")
        }
        DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
            DropdownMenuItem(
                text = { Text("(Sin definir)") },
                onClick = { onElegir(""); abierto = false }
            )
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion) },
                    onClick = { onElegir(opcion); abierto = false }
                )
            }
        }
    }
}