package com.example.projectsportpro2026_2.ui.equipos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectsportpro2026_2.datos.modelo.Categorias
import com.example.projectsportpro2026_2.datos.modelo.EntrenadorAcademia
import com.example.projectsportpro2026_2.datos.modelo.Equipo

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
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.limpiarError()
                enEdicion = null
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
                Text("Equipos · $nombreAcademia", style = MaterialTheme.typography.titleLarge)
            }

            TextButton(onClick = {
                viewModel.limpiarError()
                mostrarEntrenadores = true
            }) { Text("Directores técnicos de la academia") }

            if (!mostrarFormulario && !mostrarEntrenadores) {
                estado.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }

            if (estado.cargando && estado.equipos.isEmpty()) {
                CircularProgressIndicator()
            } else if (estado.equipos.isEmpty()) {
                Text("Esta academia aún no tiene equipos. Pulsa + para crear uno.")
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
private fun TarjetaEquipo(
    equipo: Equipo,
    nombreEntrenador: String?,
    onJugadores: () -> Unit,
    onEditar: () -> Unit,
    onCambiarEstado: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(equipo.nombre, style = MaterialTheme.typography.titleMedium)
                Text(
                    if (equipo.activo) "Activo" else "Inactivo",
                    color = if (equipo.activo) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text("Categoría: ${equipo.categoria}")
            Text("Director técnico: ${nombreEntrenador ?: "Sin asignar"}")
            Row {
                TextButton(onClick = onJugadores) { Text("Jugadores") }
                TextButton(onClick = onEditar) { Text("Editar") }
                TextButton(onClick = onCambiarEstado) {
                    Text(if (equipo.activo) "Inactivar" else "Activar")
                }
            }
        }
    }
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
        title = { Text(if (inicial == null) "Nuevo equipo" else "Editar equipo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del equipo *") },
                    singleLine = true
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
            Button(onClick = { onGuardar(nombre, categoria, idEntrenador) }, enabled = !cargando) {
                Text("Guardar")
            }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}

@Composable
private fun SelectorCategoria(seleccionada: String, onElegir: (String) -> Unit) {
    var abierto by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { abierto = true }, modifier = Modifier.fillMaxWidth()) {
            Text(if (seleccionada.isEmpty()) "Elige la categoría *" else "Categoría: $seleccionada")
        }
        DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
            Categorias.lista.forEach { categoria ->
                DropdownMenuItem(
                    text = { Text(categoria) },
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
        OutlinedButton(onClick = { abierto = true }, modifier = Modifier.fillMaxWidth()) {
            Text(if (nombre == null) "Director técnico (opcional)" else "Director técnico: $nombre")
        }
        DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
            DropdownMenuItem(
                text = { Text("(Sin asignar)") },
                onClick = {
                    onElegir("")
                    abierto = false
                }
            )
            entrenadores.forEach { entrenador ->
                DropdownMenuItem(
                    text = { Text(entrenador.nombre) },
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
        title = { Text("Directores técnicos") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (entrenadores.isEmpty()) {
                    Text("Aún no hay directores técnicos en esta academia.")
                }
                entrenadores.forEach { Text("• ${it.nombre} (${it.correo})") }
                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it },
                    label = { Text("Correo del director técnico") },
                    singleLine = true
                )
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(onClick = { onAgregar(correo) { correo = "" } }, enabled = !cargando) {
                Text("Agregar")
            }
        },
        dismissButton = { TextButton(onClick = onCerrar) { Text("Cerrar") } }
    )
}