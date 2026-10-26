package com.example.projectsportpro2026_2.ui.partidos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectsportpro2026_2.datos.modelo.partido.EstadoPartido
import com.example.projectsportpro2026_2.datos.modelo.partido.Partido
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun PantallaDetallePartido(
    idPartido: String,
    onVolver: () -> Unit,
    onEditar: (String) -> Unit,
    onGestionarConvocatoria: (String) -> Unit,
    onVerAlineacion: (String) -> Unit,
    viewModel: PartidosViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()

    var accionPendiente by remember {
        mutableStateOf<EstadoPartido?>(null)
    }

    LaunchedEffect(idPartido) {
        viewModel.cargarPartido(idPartido)
    }

    val partido = estado.partidoSeleccionado

    val nombreEquipo = partido?.let { actual ->
        estado.equipos
            .firstOrNull { it.id == actual.idEquipo }
            ?.nombre
    } ?: "Equipo"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SportProFondo)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        Spacer(Modifier.height(18.dp))

        CabeceraDetalleSportPro()

        TextButton(
            onClick = onVolver
        ) {
            Text(
                text = "← Partidos",
                color = SportProVerde
            )
        }

        if (estado.cargando && partido == null) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(48.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = SportProVerde
                )
            }

            return@Column
        }

        estado.error?.let { mensaje ->
            Text(
                text = mensaje,
                color = SportProRojo
            )
        }

        if (partido == null) {
            Text(
                text = "No se pudo cargar el partido.",
                color = SportProTextoSecundario
            )
            return@Column
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = nombreEquipo,
                style = MaterialTheme.typography.labelLarge,
                color = SportProVerde,
                fontWeight = FontWeight.Bold
            )

            ChipEstadoDetalle(
                estado = partido.estado
            )
        }

        Text(
            text = "$nombreEquipo vs ${partido.nombreRival}",
            style = MaterialTheme.typography.headlineMedium,
            color = SportProNavy,
            fontWeight = FontWeight.Black
        )

        TarjetaInformacionPartido(
            partido = partido
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            OutlinedButton(
                onClick = {
                    onGestionarConvocatoria(partido.id)
                },
                modifier = Modifier.weight(1f),
                shape = FormaBotonSportPro,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = SportProNavy
                )
            ) {
                Text(
                    text = if (
                        partido.estado == EstadoPartido.PROGRAMADO
                    ) {
                        "Gestionar convocatoria"
                    } else {
                        "Ver convocatoria"
                    },
                    style = MaterialTheme.typography.labelMedium
                )
            }

            OutlinedButton(
                onClick = {
                    onVerAlineacion(partido.id)
                },
                modifier = Modifier.weight(1f),
                shape = FormaBotonSportPro,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = SportProNavy
                )
            ) {
                Text(
                    text = "Ver alineación",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        if (partido.estado == EstadoPartido.PROGRAMADO) {

            OutlinedButton(
                onClick = {
                    onEditar(partido.id)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = FormaBotonSportPro,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = SportProNavy
                )
            ) {
                Text(
                    text = "✎  Editar partido",
                    fontWeight = FontWeight.Medium
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFFFF5DF),
                shape = FormaCampoSportPro
            ) {
                Text(
                    text = "El partido está listo para iniciarse cuando corresponda.",
                    modifier = Modifier.padding(14.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF9A6514)
                )
            }

            Button(
                onClick = {
                    accionPendiente = EstadoPartido.EN_CURSO
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = FormaBotonSportPro,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SportProNavy,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Iniciar partido",
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (partido.estado == EstadoPartido.EN_CURSO) {

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SportProVerdeClaro,
                shape = FormaCampoSportPro
            ) {
                Text(
                    text = "El partido está actualmente EN CURSO.",
                    modifier = Modifier.padding(14.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = SportProVerde,
                    fontWeight = FontWeight.Medium
                )
            }

            Button(
                onClick = {
                    accionPendiente = EstadoPartido.FINALIZADO
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = FormaBotonSportPro,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SportProNavy,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Finalizar partido",
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (
            partido.estado == EstadoPartido.PROGRAMADO ||
            partido.estado == EstadoPartido.EN_CURSO
        ) {

            OutlinedButton(
                onClick = {
                    accionPendiente = EstadoPartido.CANCELADO
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = FormaBotonSportPro,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = SportProRojo
                )
            ) {
                Text(
                    text = "Cancelar partido",
                    fontWeight = FontWeight.Medium
                )
            }
        }

        if (partido.estado == EstadoPartido.FINALIZADO) {

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFECEFF1),
                shape = FormaCampoSportPro
            ) {
                Text(
                    text = "Este partido ya fue finalizado.",
                    modifier = Modifier.padding(14.dp),
                    color = SportProTextoSecundario
                )
            }
        }

        if (partido.estado == EstadoPartido.CANCELADO) {

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SportProRojoClaro,
                shape = FormaCampoSportPro
            ) {
                Text(
                    text = "Este partido fue cancelado y se conserva en el historial.",
                    modifier = Modifier.padding(14.dp),
                    color = SportProRojo
                )
            }
        }

        Spacer(Modifier.height(32.dp))
    }

    accionPendiente?.let { nuevoEstado ->

        DialogoCambioEstadoSportPro(
            nuevoEstado = nuevoEstado,

            onConfirmar = {

                partido?.let { partidoActual ->

                    viewModel.cambiarEstado(
                        partido = partidoActual,
                        nuevoEstado = nuevoEstado
                    )
                }

                accionPendiente = null
            },

            onCancelar = {
                accionPendiente = null
            }
        )
    }
}

@Composable
private fun TarjetaInformacionPartido(
    partido: Partido
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = FormaTarjetaSportPro,
        colors = CardDefaults.cardColors(
            containerColor = SportProSuperficie
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            FilaInformacionSportPro(
                titulo = "Rival",
                valor = partido.nombreRival
            )

            FilaInformacionSportPro(
                titulo = "Fecha",
                valor = mostrarFechaLarga(partido.fecha)
            )

            FilaInformacionSportPro(
                titulo = "Hora",
                valor = partido.hora
            )

            FilaInformacionSportPro(
                titulo = "Ubicación",
                valor = partido.ubicacion
            )

            FilaInformacionSportPro(
                titulo = "Estado",
                valor = textoEstadoDetalle(partido.estado)
            )
        }
    }
}

@Composable
private fun FilaInformacionSportPro(
    titulo: String,
    valor: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {

        Text(
            text = titulo,
            style = MaterialTheme.typography.bodyMedium,
            color = SportProTextoSecundario,
            modifier = Modifier.weight(0.4f)
        )

        Text(
            text = valor,
            style = MaterialTheme.typography.bodyMedium,
            color = SportProTextoPrincipal,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(0.6f)
        )
    }
}

@Composable
private fun ChipEstadoDetalle(
    estado: EstadoPartido
) {
    val fondo = when (estado) {
        EstadoPartido.PROGRAMADO ->
            SportProVerdeClaro

        EstadoPartido.EN_CURSO ->
            Color(0xFFCBFFA7)

        EstadoPartido.FINALIZADO ->
            Color(0xFFE9ECEE)

        EstadoPartido.CANCELADO ->
            SportProRojoClaro
    }

    val texto = when (estado) {
        EstadoPartido.PROGRAMADO,
        EstadoPartido.EN_CURSO ->
            SportProVerde

        EstadoPartido.FINALIZADO ->
            SportProTextoSecundario

        EstadoPartido.CANCELADO ->
            SportProRojo
    }

    Surface(
        color = fondo,
        shape = CircleShape
    ) {
        Text(
            text = textoEstadoDetalle(estado),
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 6.dp
            ),
            style = MaterialTheme.typography.labelSmall,
            color = texto,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CabeceraDetalleSportPro() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Surface(
                modifier = Modifier.size(38.dp),
                shape = RoundedCornerShape(10.dp),
                color = SportProNavy
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text("⚽")
                }
            }

            Column {

                Text(
                    text = "SPORTPRO",
                    color = SportProNavy,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "",
                    color = SportProTextoSecundario,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        Surface(
            modifier = Modifier.size(38.dp),
            color = SportProVerdeClaro,
            shape = CircleShape
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Text("🔔 (editar)")
            }
        }
    }
}

@Composable
private fun DialogoCambioEstadoSportPro(
    nuevoEstado: EstadoPartido,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    val titulo = when (nuevoEstado) {
        EstadoPartido.EN_CURSO ->
            "¿Iniciar partido?"

        EstadoPartido.FINALIZADO ->
            "¿Finalizar partido?"

        EstadoPartido.CANCELADO ->
            "¿Cancelar partido?"

        EstadoPartido.PROGRAMADO ->
            "Cambiar estado"
    }

    val mensaje = when (nuevoEstado) {
        EstadoPartido.EN_CURSO ->
            "El partido pasará a estado EN CURSO."

        EstadoPartido.FINALIZADO ->
            "El partido pasará a estado FINALIZADO."

        EstadoPartido.CANCELADO ->
            "El partido quedará CANCELADO, pero seguirá conservado en el historial."

        EstadoPartido.PROGRAMADO ->
            ""
    }

    AlertDialog(
        onDismissRequest = onCancelar,

        title = {
            Text(
                text = titulo,
                color = SportProNavy,
                fontWeight = FontWeight.Bold
            )
        },

        text = {
            Text(
                text = mensaje,
                color = SportProTextoSecundario
            )
        },

        confirmButton = {
            Button(
                onClick = onConfirmar,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (
                        nuevoEstado == EstadoPartido.CANCELADO
                    ) {
                        SportProRojo
                    } else {
                        SportProNavy
                    }
                )
            ) {
                Text("Confirmar")
            }
        },

        dismissButton = {
            TextButton(
                onClick = onCancelar
            ) {
                Text(
                    text = "Volver",
                    color = SportProTextoSecundario
                )
            }
        }
    )
}

private fun textoEstadoDetalle(
    estado: EstadoPartido
): String =
    when (estado) {
        EstadoPartido.PROGRAMADO ->
            "PROGRAMADO"

        EstadoPartido.EN_CURSO ->
            "EN CURSO"

        EstadoPartido.FINALIZADO ->
            "FINALIZADO"

        EstadoPartido.CANCELADO ->
            "CANCELADO"
    }

private fun mostrarFechaLarga(
    fecha: String
): String {

    if (fecha.isBlank()) {
        return ""
    }

    return try {

        val fechaLocal = LocalDate.parse(fecha)

        fechaLocal.format(
            DateTimeFormatter.ofPattern(
                "EEEE, d 'de' MMMM 'de' yyyy",
                Locale("es", "PE")
            )
        )

    } catch (e: Exception) {

        fecha
    }
}