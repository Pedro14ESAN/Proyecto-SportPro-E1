package com.example.projectsportpro2026_2.ui.vinculaciones

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.OutlinedButton
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
import com.example.projectsportpro2026_2.datos.modelo.EstadoVinculacion
import com.example.projectsportpro2026_2.datos.modelo.Vinculacion
import com.example.projectsportpro2026_2.datos.repositorio.DatosSensiblesVinculo

@Composable
fun PantallaMisJugadores(
    onVolver: () -> Unit,
    viewModel: MisJugadoresViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()
    var mostrarDialogo by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.limpiarError()
                mostrarDialogo = true
            }) { Text("+ Solicitar", modifier = Modifier.padding(horizontal = 16.dp)) }
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
                Text("Mis jugadores", style = MaterialTheme.typography.headlineSmall)
            }

            // Pestañas de filtro: Activo, Pendiente, Inactivo, Todos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FiltroVinculacion.values().forEach { filtro ->
                    val modificador = Modifier.weight(1f)
                    val relleno2 = PaddingValues(horizontal = 2.dp)
                    if (estado.filtro == filtro) {
                        Button(
                            onClick = { viewModel.cambiarFiltro(filtro) },
                            modifier = modificador,
                            contentPadding = relleno2
                        ) { Text(filtro.etiqueta, style = MaterialTheme.typography.labelMedium) }
                    } else {
                        OutlinedButton(
                            onClick = { viewModel.cambiarFiltro(filtro) },
                            modifier = modificador,
                            contentPadding = relleno2
                        ) { Text(filtro.etiqueta, style = MaterialTheme.typography.labelMedium) }
                    }
                }
            }

            if (!mostrarDialogo) {
                estado.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }

            if (estado.cargando && estado.vinculaciones.isEmpty()) {
                CircularProgressIndicator()
            } else if (estado.visibles.isEmpty()) {
                Text("No hay jugadores en esta vista. Usa + Solicitar para vincular uno.")
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
private fun TarjetaVinculacion(
    vinculacion: Vinculacion,
    datos: DatosSensiblesVinculo?,
    onDesvincular: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(vinculacion.nombreJugador, style = MaterialTheme.typography.titleMedium)
                Text(
                    etiquetaEstado(vinculacion.estado),
                    color = if (vinculacion.estado == EstadoVinculacion.ACTIVA)
                        MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text("Solicitud: ${formatoFecha(vinculacion.fechaSolicitud)}")

            when (vinculacion.estado) {
                EstadoVinculacion.PENDIENTE ->
                    // En estado pendiente NO se muestran datos sensibles
                    Text("Esperando la aprobación de la academia")

                EstadoVinculacion.ACTIVA -> {
                    Text(
                        "Aprobado por ${vinculacion.nombreAprobador} " +
                                "el ${formatoFecha(vinculacion.fechaAprobacion)}"
                    )
                    if (datos != null) {
                        Text("Fecha de nacimiento: ${datos.fechaNacimiento.ifBlank { "Pendiente" }}")
                        Text("Posición: ${datos.posicion.ifBlank { "Pendiente" }}")
                        Text("Teléfono: ${datos.telefono.ifBlank { "Pendiente" }}")
                    }
                    if (vinculacion.desvinculacionSolicitada) {
                        Text("Desvinculación solicitada. Esperando a la academia.")
                    } else {
                        TextButton(onClick = onDesvincular) { Text("Solicitar desvinculación") }
                    }
                }

                EstadoVinculacion.INACTIVA -> Text("Vinculación inactiva")
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
        title = { Text("Solicitar vinculación") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Escribe el documento de identidad del jugador. La academia revisará tu solicitud.")
                OutlinedTextField(
                    value = documento,
                    onValueChange = { documento = it },
                    label = { Text("Documento del jugador") },
                    singleLine = true
                )
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(onClick = { onEnviar(documento) }, enabled = !cargando) { Text("Solicitar") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}