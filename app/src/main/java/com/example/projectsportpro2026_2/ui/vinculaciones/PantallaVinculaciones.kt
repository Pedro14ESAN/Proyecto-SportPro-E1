package com.example.projectsportpro2026_2.ui.vinculaciones

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectsportpro2026_2.datos.modelo.EstadoVinculacion
import com.example.projectsportpro2026_2.datos.modelo.Vinculacion

@Composable
fun PantallaVinculaciones(
    onVolver: () -> Unit,
    viewModel: VinculacionesViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onVolver) { Text("← Volver") }
            Text("Vinculaciones", style = MaterialTheme.typography.headlineSmall)
        }

        // Banner con el número de solicitudes pendientes
        if (estado.pendientes > 0) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Text(
                    text = if (estado.pendientes == 1) "1 solicitud esperando aprobación"
                    else "${estado.pendientes} solicitudes esperando aprobación",
                    modifier = Modifier.padding(16.dp),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        estado.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        if (estado.cargando && estado.vinculaciones.isEmpty()) {
            CircularProgressIndicator()
        } else if (estado.vinculaciones.isEmpty()) {
            Text("Aún no hay solicitudes de vinculación en tus academias.")
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
private fun TarjetaAdministrador(
    vinculacion: Vinculacion,
    onAprobar: () -> Unit,
    onRechazar: () -> Unit,
    onDesvincular: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(vinculacion.nombreJugador, style = MaterialTheme.typography.titleMedium)
                Text(etiquetaEstado(vinculacion.estado))
            }
            Text("Apoderado: ${vinculacion.nombreApoderado}")
            Text("Solicitud: ${formatoFecha(vinculacion.fechaSolicitud)}")

            when (vinculacion.estado) {
                EstadoVinculacion.PENDIENTE -> Row {
                    TextButton(onClick = onAprobar) { Text("Autorizar") }
                    TextButton(onClick = onRechazar) { Text("Rechazar") }
                }

                EstadoVinculacion.ACTIVA -> {
                    Text(
                        "Aprobado por ${vinculacion.nombreAprobador} " +
                                "el ${formatoFecha(vinculacion.fechaAprobacion)}"
                    )
                    if (vinculacion.desvinculacionSolicitada) {
                        Text("El apoderado solicitó la desvinculación")
                    }
                    TextButton(onClick = onDesvincular) { Text("Desvincular") }
                }

                EstadoVinculacion.INACTIVA -> Unit
            }
        }
    }
}