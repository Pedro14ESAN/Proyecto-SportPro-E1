package com.example.projectsportpro2026_2.ui.academias

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectsportpro2026_2.datos.modelo.Academia

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
                Text("Mis academias", style = MaterialTheme.typography.headlineSmall)
            }

            if (!mostrarFormulario) {
                estado.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }

            if (estado.cargando && estado.academias.isEmpty()) {
                CircularProgressIndicator()
            } else if (estado.academias.isEmpty()) {
                Text("Aún no tienes academias. Pulsa + para crear una.")
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
private fun TarjetaAcademia(
    academia: Academia,
    onEquipos: () -> Unit,
    onPartidos: () -> Unit,
    onCatalogo: () -> Unit,
    onEditar: () -> Unit,
    onCambiarEstado: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(academia.nombre, style = MaterialTheme.typography.titleMedium)
                Text(
                    if (academia.activa) "Activa" else "Inactiva",
                    color = if (academia.activa) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (academia.direccion.isNotBlank()) Text(academia.direccion)
            if (academia.descripcion.isNotBlank()) Text(academia.descripcion)
            Row {
                TextButton(onClick = onEquipos) { Text("Equipos") }
                TextButton(onClick = onPartidos) { Text("Partidos") }
                TextButton(onClick = onCatalogo) { Text("Catálogo") }
                TextButton(onClick = onEditar) { Text("Editar") }
                TextButton(onClick = onCambiarEstado) {
                    Text(if (academia.activa) "Inactivar" else "Activar")
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

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(if (inicial == null) "Nueva academia" else "Editar academia") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre *") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = direccion,
                    onValueChange = { direccion = it },
                    label = { Text("Dirección") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") }
                )
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(
                onClick = { onGuardar(nombre, direccion, descripcion) },
                enabled = !cargando
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}