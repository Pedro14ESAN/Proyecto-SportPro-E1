package com.example.projectsportpro2026_2.ui.partidos

import com.example.projectsportpro2026_2.ui.comun.TipoEstado
import com.example.projectsportpro2026_2.ui.comun.ChipEstado
import com.example.projectsportpro2026_2.ui.comun.MarcadorLed
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.SportsSoccer
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import com.example.projectsportpro2026_2.ui.theme.LineaDeCal
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
import java.time.LocalDateTime
import java.time.LocalTime

@Composable
fun PantallaDetallePartido(
    idPartido: String,
    onVolver: () -> Unit,
    onEditar: (String) -> Unit,
    onGestionarConvocatoria: (String) -> Unit,
    onVerAlineacion: (String) -> Unit,
    onAbrirPartidoEnVivo: (String) -> Unit,
    // HU26/HU27: resumen narrativo del partido finalizado
    onAbrirResumen: (String) -> Unit = {},
    viewModel: PartidosViewModel = viewModel()
){
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
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
            Text(
                text = "Partidos",
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
        estado.marcadorSeleccionado?.let { marcador ->

            if (
                partido.estado == EstadoPartido.EN_CURSO ||
                partido.estado == EstadoPartido.FINALIZADO
            ) {

                TarjetaMarcadorDetalle(
                    nombreEquipo = nombreEquipo,
                    nombreRival = partido.nombreRival,
                    golesPropios = marcador.propio,
                    golesRival = marcador.rival,
                    finalizado =
                        partido.estado ==
                                EstadoPartido.FINALIZADO
                )
            }
        }

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
                Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Editar partido",
                    fontWeight = FontWeight.Medium
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = LineaDeCal.colores.amarilloFondo /* antes 0xFFFFF5DF */,
                shape = FormaCampoSportPro
            ) {
                Text(
                    text = "El partido está listo para iniciarse cuando corresponda.",
                    modifier = Modifier.padding(14.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = LineaDeCal.colores.amarilloTexto /* antes 0xFF9A6514 */
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
                    containerColor = SportProNavyFondo,
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
                    onAbrirPartidoEnVivo(
                        partido.id
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = FormaBotonSportPro,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SportProVerde,
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Rounded.SportsSoccer, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Abrir partido en vivo",
                    fontWeight = FontWeight.Bold
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
                    containerColor = SportProNavyFondo,
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
                color = LineaDeCal.colores.apagadoFondo /* antes 0xFFECEFF1 */,
                shape = FormaCampoSportPro
            ) {
                Text(
                    text = "Este partido ya fue finalizado.",
                    modifier = Modifier.padding(14.dp),
                    color = SportProTextoSecundario
                )
            }
            Button(
                onClick = {
                    onAbrirPartidoEnVivo(
                        partido.id
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = FormaBotonSportPro,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SportProNavyFondo,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Ver cronología del partido",
                    fontWeight = FontWeight.Bold
                )
            }

            // HU26/HU27: generar y revisar el resumen narrativo con IA
            Button(
                onClick = {
                    onAbrirResumen(
                        partido.id
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = FormaBotonSportPro,
                colors = ButtonDefaults.buttonColors(
                    containerColor = LineaDeCal.colores.verde /* antes 0xFF1E9B5F */,
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Resumen del partido",
                    fontWeight = FontWeight.Bold
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
            partido = partido,

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
private fun TarjetaMarcadorDetalle(
    nombreEquipo: String,
    nombreRival: String,
    golesPropios: Int,
    golesRival: Int,
    finalizado: Boolean
) {
    // Línea de cal: marcador LED (mismas etiquetas "RESULTADO FINAL" / "MARCADOR ACTUAL")
    MarcadorLed(
        local = nombreEquipo,
        rival = nombreRival,
        golesLocal = golesPropios,
        golesRival = golesRival,
        etiquetaSuperior = if (finalizado) {
            "RESULTADO FINAL"
        } else {
            "MARCADOR ACTUAL"
        },
        enVivo = !finalizado
    )
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
            LineaDeCal.colores.lima /* antes 0xFFCBFFA7 */

        EstadoPartido.FINALIZADO ->
            LineaDeCal.colores.apagadoFondo /* antes 0xFFE9ECEE */

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

    // Línea de cal: ChipEstado
    ChipEstado(
        textoEstadoDetalle(estado),
        when (estado) {
            EstadoPartido.EN_CURSO -> TipoEstado.OK
            EstadoPartido.PROGRAMADO -> TipoEstado.NEUTRO
            EstadoPartido.FINALIZADO -> TipoEstado.APAGADO
            EstadoPartido.CANCELADO -> TipoEstado.ERROR
        },
        conPunto = estado == EstadoPartido.EN_CURSO
    )
}

@Composable
private fun CabeceraDetalleSportPro() {
    // Línea de cal: la cabecera decorativa (logo ⚽ y campana 🔔 sin acción) se quitó;
    // cada pantalla muestra su título y su botón de volver.
}

@Composable
private fun DialogoCambioEstadoSportPro(
    nuevoEstado: EstadoPartido,
    partido: Partido?,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {

    val inicioAnticipado =
        nuevoEstado == EstadoPartido.EN_CURSO &&
                partido?.let {
                    esInicioAnticipado(it)
                } == true
    val titulo = when (nuevoEstado) {
        EstadoPartido.EN_CURSO ->
            if (inicioAnticipado) {
                "¿Iniciar antes de lo previsto?"
            } else {
                "¿Iniciar partido?"
            }

        EstadoPartido.FINALIZADO ->
            "¿Finalizar partido?"

        EstadoPartido.CANCELADO ->
            "¿Cancelar partido?"

        EstadoPartido.PROGRAMADO ->
            "Cambiar estado"
    }

    val mensaje = when (nuevoEstado) {
        EstadoPartido.EN_CURSO ->
            if (inicioAnticipado
            ) {
                "Este partido está programado para ${
                    mostrarFechaLarga(partido.fecha)
                } a las ${partido.hora}. " +
                        "¿Deseas iniciarlo ahora de todas formas?"
            } else {
                "El partido pasará a estado EN CURSO."
            }

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
private fun esInicioAnticipado(
    partido: Partido
): Boolean {

    return try {

        val fecha =
            LocalDate.parse(
                partido.fecha
            )

        val hora =
            LocalTime.parse(
                partido.hora
            )

        val fechaHoraProgramada =
            LocalDateTime.of(
                fecha,
                hora
            )

        LocalDateTime.now()
            .isBefore(
                fechaHoraProgramada
            )

    } catch (e: Exception) {

        false
    }
}