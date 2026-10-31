package com.example.projectsportpro2026_2.ui.convocatoria

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectsportpro2026_2.datos.modelo.Rol
import com.example.projectsportpro2026_2.datos.modelo.convocatoria.EstadoDisponibilidad
import com.example.projectsportpro2026_2.datos.modelo.partido.EstadoPartido
import com.example.projectsportpro2026_2.datos.repositorio.convocatoria.ConvocatoriaParaResponder
import com.example.projectsportpro2026_2.ui.partidos.FormaBotonSportPro
import com.example.projectsportpro2026_2.ui.partidos.FormaTarjetaSportPro
import com.example.projectsportpro2026_2.ui.partidos.SportProBorde
import com.example.projectsportpro2026_2.ui.partidos.SportProFondo
import com.example.projectsportpro2026_2.ui.partidos.SportProNavy
import com.example.projectsportpro2026_2.ui.partidos.SportProRojo
import com.example.projectsportpro2026_2.ui.partidos.SportProRojoClaro
import com.example.projectsportpro2026_2.ui.partidos.SportProSuperficie
import com.example.projectsportpro2026_2.ui.partidos.SportProTextoSecundario
import com.example.projectsportpro2026_2.ui.partidos.SportProVerde
import com.example.projectsportpro2026_2.ui.partidos.SportProVerdeClaro
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun PantallaMisConvocatorias(
    onVolver: () -> Unit,
    viewModel: ConvocatoriasRespuestaViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.iniciar()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SportProFondo),
        contentPadding = PaddingValues(
            start = 22.dp,
            end = 22.dp,
            top = 18.dp,
            bottom = 28.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        item {
            CabeceraMisConvocatorias()
        }

        item {
            TextButton(
                onClick = onVolver
            ) {
                Text(
                    text = "← Inicio",
                    color = SportProVerde
                )
            }
        }

        item {
            Text(
                text = if (estado.rol == Rol.PAD) {
                    "CONVOCATORIAS DE TUS JUGADORES"
                } else {
                    "MIS PARTIDOS"
                },
                style = MaterialTheme.typography.labelSmall,
                color = SportProVerde,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Text(
                text = "Convocatorias",
                style = MaterialTheme.typography.headlineMedium,
                color = SportProNavy,
                fontWeight = FontWeight.Black
            )
        }

        item {
            Text(
                text = if (estado.rol == Rol.PAD) {
                    "Confirma la disponibilidad de los jugadores bajo tu responsabilidad."
                } else {
                    "Confirma si podrás participar en los próximos partidos."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = SportProTextoSecundario
            )
        }

        estado.error?.let { mensaje ->
            item {
                MensajeErrorConvocatoria(
                    mensaje = mensaje
                )
            }
        }

        estado.mensaje?.let { mensaje ->
            item {
                MensajeExitoConvocatoria(
                    mensaje = mensaje
                )
            }
        }

        if (estado.cargando && estado.convocatorias.isEmpty()) {

            item {
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
            }

        } else if (estado.convocatorias.isEmpty()) {

            item {
                EstadoVacioConvocatorias()
            }

        } else {

            items(
                items = estado.convocatorias,
                key = {
                    "${it.partido.id}-${it.jugador.id}"
                }
            ) { convocatoria ->

                val clave =
                    "${convocatoria.partido.id}-${convocatoria.jugador.id}"

                TarjetaConvocatoriaRespuesta(
                    convocatoria = convocatoria,
                    respondiendo =
                        estado.idRespondiendo == clave,
                    onDisponible = {
                        viewModel.responder(
                            convocatoria,
                            EstadoDisponibilidad.DISPONIBLE
                        )
                    },
                    onNoDisponible = {
                        viewModel.responder(
                            convocatoria,
                            EstadoDisponibilidad.NO_DISPONIBLE
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun TarjetaConvocatoriaRespuesta(
    convocatoria: ConvocatoriaParaResponder,
    respondiendo: Boolean,
    onDisponible: () -> Unit,
    onNoDisponible: () -> Unit
) {
    val partido = convocatoria.partido
    val jugador = convocatoria.jugador
    val convocado = convocatoria.convocado

    val editable =
        partido.estado == EstadoPartido.PROGRAMADO

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = FormaTarjetaSportPro,
        colors = CardDefaults.cardColors(
            containerColor = SportProSuperficie
        ),
        border = BorderStroke(
            1.dp,
            SportProBorde
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = "VS ${partido.nombreRival.uppercase()}",
                        style = MaterialTheme.typography.titleMedium,
                        color = SportProNavy,
                        fontWeight = FontWeight.Black
                    )

                    Text(
                        text = informacionPartido(
                            fecha = partido.fecha,
                            hora = partido.hora,
                            ubicacion = partido.ubicacion
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = SportProTextoSecundario
                    )
                }

                ChipRespuesta(
                    estado = convocado.estado
                )
            }

            Surface(
                color = SportProFondo,
                shape = RoundedCornerShape(12.dp)
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {

                    Text(
                        text = jugador.nombreCompleto(),
                        color = SportProNavy,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = jugador.posicion.ifBlank {
                            "Posición no registrada"
                        },
                        color = SportProTextoSecundario,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (editable) {

                Text(
                    text = if (
                        convocado.estado ==
                        EstadoDisponibilidad.PENDIENTE
                    ) {
                        "¿Podrás asistir a este partido?"
                    } else {
                        "Puedes cambiar tu respuesta mientras el partido no haya comenzado."
                    },
                    color = SportProTextoSecundario,
                    style = MaterialTheme.typography.bodySmall
                )

                if (respondiendo) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = SportProVerde,
                            strokeWidth = 2.dp
                        )
                    }

                } else {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.spacedBy(10.dp)
                    ) {

                        Button(
                            onClick = onDisponible,
                            modifier = Modifier.weight(1f),
                            shape = FormaBotonSportPro,
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor =
                                        SportProVerde,
                                    contentColor =
                                        Color.White
                                )
                        ) {
                            Text(
                                text = "✓ Disponible",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = onNoDisponible,
                            modifier = Modifier.weight(1f),
                            shape = FormaBotonSportPro,
                            border = BorderStroke(
                                1.dp,
                                SportProRojo
                            ),
                            colors =
                                ButtonDefaults.outlinedButtonColors(
                                    contentColor = SportProRojo
                                )
                        ) {
                            Text(
                                text = "No disponible",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

            } else {

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = SportProFondo,
                    shape = RoundedCornerShape(10.dp)
                ) {

                    Text(
                        text = "Esta convocatoria ya forma parte del historial del partido.",
                        color = SportProTextoSecundario,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChipRespuesta(
    estado: EstadoDisponibilidad
) {
    val fondo = when (estado) {

        EstadoDisponibilidad.PENDIENTE ->
            Color(0xFFFFF2D9)

        EstadoDisponibilidad.DISPONIBLE ->
            SportProVerdeClaro

        EstadoDisponibilidad.NO_DISPONIBLE ->
            SportProRojoClaro
    }

    val textoColor = when (estado) {

        EstadoDisponibilidad.PENDIENTE ->
            Color(0xFF9A6514)

        EstadoDisponibilidad.DISPONIBLE ->
            SportProVerde

        EstadoDisponibilidad.NO_DISPONIBLE ->
            SportProRojo
    }

    val texto = when (estado) {

        EstadoDisponibilidad.PENDIENTE ->
            "PENDIENTE"

        EstadoDisponibilidad.DISPONIBLE ->
            "DISPONIBLE"

        EstadoDisponibilidad.NO_DISPONIBLE ->
            "NO DISP."
    }

    Surface(
        color = fondo,
        shape = CircleShape
    ) {

        Text(
            text = texto,
            color = textoColor,
            modifier = Modifier.padding(
                horizontal = 9.dp,
                vertical = 5.dp
            ),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun EstadoVacioConvocatorias() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp),
        color = SportProSuperficie,
        shape = FormaTarjetaSportPro,
        border = BorderStroke(
            1.dp,
            SportProBorde
        )
    ) {

        Column(
            modifier = Modifier.padding(22.dp),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Text(
                text = "⚽",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "No tienes convocatorias",
                color = SportProNavy,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Cuando seas convocado a un partido, aparecerá aquí.",
                color = SportProTextoSecundario,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun MensajeErrorConvocatoria(
    mensaje: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SportProRojoClaro,
        shape = RoundedCornerShape(12.dp)
    ) {

        Text(
            text = mensaje,
            color = SportProRojo,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun MensajeExitoConvocatoria(
    mensaje: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SportProVerdeClaro,
        shape = RoundedCornerShape(12.dp)
    ) {

        Text(
            text = mensaje,
            color = SportProVerde,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun CabeceraMisConvocatorias() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement =
            Arrangement.SpaceBetween
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.spacedBy(10.dp)
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
                    style =
                        MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = "",
                    color = SportProTextoSecundario,
                    style =
                        MaterialTheme.typography.labelSmall
                )
            }
        }

        Surface(
            modifier = Modifier.size(38.dp),
            shape = CircleShape,
            color = SportProVerdeClaro
        ) {

            Box(
                contentAlignment = Alignment.Center
            ) {
                Text("🔔")
            }
        }
    }
}

private fun informacionPartido(
    fecha: String,
    hora: String,
    ubicacion: String
): String {

    val fechaBonita =
        try {

            LocalDate
                .parse(fecha)
                .format(
                    DateTimeFormatter.ofPattern(
                        "EEE, d MMM",
                        Locale.forLanguageTag(
                            "es-PE"
                        )
                    )
                )

        } catch (_: Exception) {

            fecha
        }

    return buildString {

        append(fechaBonita)

        if (hora.isNotBlank()) {
            append(" · ")
            append(hora)
        }

        if (ubicacion.isNotBlank()) {
            append(" · ")
            append(ubicacion)
        }
    }
}