package com.example.projectsportpro2026_2.ui.convocatoria

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
import androidx.compose.material3.Scaffold
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
import com.example.projectsportpro2026_2.datos.modelo.Jugador
import com.example.projectsportpro2026_2.datos.modelo.convocatoria.EstadoDisponibilidad
import com.example.projectsportpro2026_2.datos.modelo.partido.EstadoPartido
import com.example.projectsportpro2026_2.ui.partidos.FormaBotonSportPro
import com.example.projectsportpro2026_2.ui.partidos.FormaTarjetaSportPro
import com.example.projectsportpro2026_2.ui.partidos.SportProBorde
import com.example.projectsportpro2026_2.ui.partidos.SportProFondo
import com.example.projectsportpro2026_2.ui.partidos.SportProNavy
import com.example.projectsportpro2026_2.ui.partidos.SportProRojo
import com.example.projectsportpro2026_2.ui.partidos.SportProRojoClaro
import com.example.projectsportpro2026_2.ui.partidos.SportProSuperficie
import com.example.projectsportpro2026_2.ui.partidos.SportProTextoPrincipal
import com.example.projectsportpro2026_2.ui.partidos.SportProTextoSecundario
import com.example.projectsportpro2026_2.ui.partidos.SportProVerde
import com.example.projectsportpro2026_2.ui.partidos.SportProVerdeClaro
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun PantallaConvocatoria(
    idPartido: String,
    onVolver: () -> Unit,
    viewModel: ConvocatoriaViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()

    LaunchedEffect(idPartido) {
        viewModel.iniciar(idPartido)
    }

    val partido = estado.partido

    val editable =
        partido?.estado == EstadoPartido.PROGRAMADO

    Scaffold(
        containerColor = SportProFondo,

        bottomBar = {
            if (partido != null) {
                BarraPublicarConvocatoria(
                    cantidad = estado.idsSeleccionados.size,
                    publicada = estado.publicada,
                    guardando = estado.guardando,
                    habilitado = editable &&
                            estado.idsSeleccionados.isNotEmpty(),
                    onPublicar = {
                        viewModel.publicar()
                    }
                )
            }
        }
    ) { relleno ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(SportProFondo)
                .padding(relleno),
            contentPadding = PaddingValues(
                start = 22.dp,
                end = 22.dp,
                top = 18.dp,
                bottom = 18.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            item {
                CabeceraConvocatoriaSportPro()
            }

            item {
                TextButton(
                    onClick = onVolver
                ) {
                    Text(
                        text = "← Partidos",
                        color = SportProVerde
                    )
                }
            }

            if (estado.cargando && partido == null) {

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

            } else if (partido != null) {

                item {
                    Text(
                        text = resumenPartido(
                            rival = partido.nombreRival,
                            fecha = partido.fecha,
                            hora = partido.hora
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = SportProVerde,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    Text(
                        text = "Convocatoria",
                        style = MaterialTheme.typography.headlineMedium,
                        color = SportProNavy,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(
                            bottom = 8.dp
                        )
                    )
                }

                estado.error?.let { mensaje ->

                    item {
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
                }

                estado.mensaje?.let { mensaje ->

                    item {
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
                }

                if (!editable) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = SportProVerdeClaro,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "La convocatoria es histórica porque el partido ya comenzó.",
                                color = SportProVerde,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                if (estado.jugadores.isEmpty()) {

                    item {
                        Text(
                            text = "Este equipo todavía no tiene jugadores registrados.",
                            color = SportProTextoSecundario,
                            modifier = Modifier.padding(
                                vertical = 20.dp
                            )
                        )
                    }

                } else {

                    items(
                        items = estado.jugadores,
                        key = { it.id }
                    ) { jugador ->

                        val seleccionado =
                            jugador.id in estado.idsSeleccionados

                        val disponibilidad =
                            estado.convocados[jugador.id]
                                ?.estado

                        TarjetaJugadorConvocatoria(
                            jugador = jugador,
                            seleccionado = seleccionado,
                            editable = editable,
                            disponibilidad = disponibilidad,
                            onClick = {
                                viewModel.alternarJugador(
                                    jugador
                                )
                            }
                        )
                    }
                }
            }

            item {
                Spacer(
                    modifier = Modifier.height(12.dp)
                )
            }
        }
    }
}

@Composable
private fun TarjetaJugadorConvocatoria(
    jugador: Jugador,
    seleccionado: Boolean,
    editable: Boolean,
    disponibilidad: EstadoDisponibilidad?,
    onClick: () -> Unit
) {
    val habilitado =
        jugador.activo && editable

    val fondo = if (jugador.activo) {
        SportProSuperficie
    } else {
        Color(0xFFF4F5F2)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = habilitado,
                onClick = onClick
            ),
        shape = FormaTarjetaSportPro,
        colors = CardDefaults.cardColors(
            containerColor = fondo
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (seleccionado && jugador.activo) {
                Color(0xFFB9E0C3)
            } else {
                SportProBorde
            }
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 12.dp,
                    vertical = 12.dp
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            CheckConvocatoria(
                marcado = seleccionado,
                habilitado = habilitado
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement =
                        Arrangement.SpaceBetween
                ) {

                    Text(
                        text = jugador.nombreCompleto(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (jugador.activo) {
                            SportProNavy
                        } else {
                            SportProTextoSecundario
                        },
                        fontWeight = FontWeight.Bold
                    )

                    if (!jugador.activo) {

                        ChipInactivo()

                    } else if (
                        seleccionado &&
                        disponibilidad != null
                    ) {

                        ChipDisponibilidad(
                            estado = disponibilidad
                        )
                    }
                }

                Text(
                    text = "${abreviarPosicion(jugador.posicion)} · ${edadJugador(jugador)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = SportProTextoSecundario
                )
            }
        }
    }
}

@Composable
private fun CheckConvocatoria(
    marcado: Boolean,
    habilitado: Boolean
) {
    val fondo = when {
        marcado && habilitado ->
            SportProVerde

        marcado ->
            SportProTextoSecundario

        else ->
            Color.Transparent
    }

    val borde = when {
        marcado ->
            fondo

        else ->
            Color(0xFFB7C5BC)
    }

    Surface(
        modifier = Modifier.size(20.dp),
        shape = RoundedCornerShape(4.dp),
        color = fondo,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            borde
        )
    ) {
        if (marcado) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✓",
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ChipInactivo() {
    Surface(
        color = SportProRojoClaro,
        shape = CircleShape
    ) {
        Text(
            text = "INACTIVO",
            modifier = Modifier.padding(
                horizontal = 8.dp,
                vertical = 4.dp
            ),
            color = SportProRojo,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ChipDisponibilidad(
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
            modifier = Modifier.padding(
                horizontal = 8.dp,
                vertical = 4.dp
            ),
            color = textoColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun BarraPublicarConvocatoria(
    cantidad: Int,
    publicada: Boolean,
    guardando: Boolean,
    habilitado: Boolean,
    onPublicar: () -> Unit
) {
    Surface(
        color = SportProFondo,
        shadowElevation = 6.dp
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 22.dp,
                    vertical = 10.dp
                )
        ) {

            Button(
                onClick = onPublicar,
                enabled = habilitado && !guardando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = FormaBotonSportPro,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SportProNavy,
                    contentColor = Color.White,
                    disabledContainerColor =
                        SportProNavy.copy(
                            alpha = 0.45f
                        )
                )
            ) {

                if (guardando) {

                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )

                } else {

                    val accion = if (publicada) {
                        "Actualizar convocatoria"
                    } else {
                        "Publicar convocatoria"
                    }

                    Text(
                        text = "$accion · $cantidad jugadores",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun CabeceraConvocatoriaSportPro() {
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
                Text("🔔")
            }
        }
    }
}

private fun abreviarPosicion(
    posicion: String
): String =
    when (posicion.trim().lowercase()) {

        "portero" ->
            "POR"

        "defensa" ->
            "DEF"

        "mediocampista" ->
            "MED"

        "delantero" ->
            "DEL"

        else ->
            posicion.take(3).uppercase()
    }

private fun edadJugador(
    jugador: Jugador
): String {

    if (jugador.fechaNacimiento.isBlank()) {
        return "Edad no registrada"
    }

    return try {

        val nacimiento =
            LocalDate.parse(
                jugador.fechaNacimiento
            )

        val edad =
            Period.between(
                nacimiento,
                LocalDate.now()
            ).years

        "$edad años"

    } catch (_: Exception) {

        "Edad no registrada"
    }
}

private fun resumenPartido(
    rival: String,
    fecha: String,
    hora: String
): String {

    val dia = try {

        LocalDate
            .parse(fecha)
            .format(
                DateTimeFormatter.ofPattern(
                    "EEE",
                    Locale.forLanguageTag("es-PE")
                )
            )
            .replace(".", "")
            .uppercase()

    } catch (_: Exception) {

        ""
    }

    return buildString {

        append("VS ")
        append(rival.uppercase())

        if (dia.isNotBlank()) {
            append(" · ")
            append(dia)
        }

        if (hora.isNotBlank()) {
            append(" ")
            append(hora)
        }
    }
}