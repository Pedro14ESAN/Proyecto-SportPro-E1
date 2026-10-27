package com.example.projectsportpro2026_2.ui.alineacion

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectsportpro2026_2.datos.modelo.Jugador
import com.example.projectsportpro2026_2.datos.modelo.alineacion.FormatoPartido
import com.example.projectsportpro2026_2.datos.modelo.alineacion.JugadorAlineacion
import com.example.projectsportpro2026_2.datos.modelo.alineacion.TipoJugadorAlineacion
import com.example.projectsportpro2026_2.datos.modelo.partido.EstadoPartido
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

private val VerdeCancha = Color(0xFF328653)
private val VerdeCanchaOscuro = Color(0xFF287344)

@Composable
fun PantallaAlineacion(
    idPartido: String,
    onVolver: () -> Unit,
    viewModel: AlineacionViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()

    LaunchedEffect(idPartido) {
        viewModel.iniciar(idPartido)
    }

    val partido = estado.partido

    val editable =
        partido?.estado == EstadoPartido.PROGRAMADO

    val titulares =
        estado.jugadoresAlineacion.filter {
            it.tipo == TipoJugadorAlineacion.TITULAR
        }

    val suplentes =
        estado.jugadoresAlineacion.filter {
            it.tipo == TipoJugadorAlineacion.SUPLENTE
        }

    val slots =
        slotsParaFormato(estado.formato)

    val idsDisponibles =
        estado.disponibles.map {
            it.id
        }.toSet()

    val posicionesTitulares =
        titulares.map {
            it.posicionCampo
        }.toSet()

    val puedeConfirmar =
        editable &&
                titulares.size == estado.formato.maximoTitulares &&
                posicionesTitulares == slots.map { it.id }.toSet() &&
                titulares.all {
                    it.idJugador in idsDisponibles
                }

    Scaffold(
        containerColor = SportProFondo,

        bottomBar = {
            if (partido != null) {
                BarraConfirmarAlineacion(
                    titulares = titulares.size,
                    maximo = estado.formato.maximoTitulares,
                    editable = editable,
                    puedeConfirmar = puedeConfirmar,
                    guardando = estado.guardando,
                    confirmada = estado.confirmada,
                    onConfirmar = {
                        viewModel.confirmarAlineacion()
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
                start = 20.dp,
                end = 20.dp,
                top = 18.dp,
                bottom = 20.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            item {
                CabeceraAlineacionSportPro()
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
                        text = "VS ${partido.nombreRival.uppercase()}",
                        style = MaterialTheme.typography.labelMedium,
                        color = SportProVerde,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            text = "Alineación",
                            style = MaterialTheme.typography.headlineMedium,
                            color = SportProNavy,
                            fontWeight = FontWeight.Black
                        )

                        Surface(
                            color = SportProVerdeClaro,
                            shape = CircleShape
                        ) {
                            Text(
                                text =
                                    "${titulares.size}/${estado.formato.maximoTitulares} titulares",
                                color = SportProVerde,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(
                                    horizontal = 10.dp,
                                    vertical = 6.dp
                                )
                            )
                        }
                    }
                }

                estado.error?.let { mensaje ->
                    item {
                        MensajeAlineacion(
                            mensaje = mensaje,
                            error = true
                        )
                    }
                }

                estado.mensaje?.let { mensaje ->
                    item {
                        MensajeAlineacion(
                            mensaje = mensaje,
                            error = false
                        )
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
                                text =
                                    "La alineación es histórica porque el partido ya comenzó.",
                                color = SportProVerde,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "FORMATO DE PARTIDO",
                        color = SportProTextoSecundario,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    SelectorFormato(
                        formatoActual = estado.formato,
                        editable = editable,
                        onCambiar = viewModel::cambiarFormato
                    )
                }

                item {
                    CanchaAlineacion(
                        slots = slots,
                        titulares = titulares,
                        plantilla = estado.plantilla,
                        editable = editable,
                        onQuitarTitular = viewModel::pasarASuplente
                    )
                }

                item {
                    Text(
                        text = "SUPLENTES",
                        color = SportProNavy,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black
                    )
                }

                item {
                    Text(
                        text = if (editable) {
                            "Toca + para pasar un jugador a titular."
                        } else {
                            "Jugadores suplentes del partido."
                        },
                        color = SportProTextoSecundario,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (suplentes.isEmpty()) {

                    item {
                        TextoVacioAlineacion(
                            "No hay suplentes disponibles."
                        )
                    }

                } else {

                    items(
                        items = suplentes,
                        key = {
                            "SUP-${it.idJugador}"
                        }
                    ) { alineado ->

                        val jugador =
                            estado.plantilla.firstOrNull {
                                it.id == alineado.idJugador
                            }

                        if (jugador != null) {
                            TarjetaSuplente(
                                jugador = jugador,
                                editable = editable,
                                onAgregar = {
                                    viewModel.pasarATitular(
                                        jugador
                                    )
                                }
                            )
                        }
                    }
                }

                item {
                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )
                }

                item {
                    Text(
                        text = "TITULARES",
                        color = SportProNavy,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black
                    )
                }

                item {
                    Text(
                        text = if (editable) {
                            "Puedes cambiar su posición o devolverlos a suplentes."
                        } else {
                            "Alineación titular registrada."
                        },
                        color = SportProTextoSecundario,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (titulares.isEmpty()) {

                    item {
                        TextoVacioAlineacion(
                            "Todavía no has agregado titulares."
                        )
                    }

                } else {

                    items(
                        items = titulares,
                        key = {
                            "TIT-${it.idJugador}"
                        }
                    ) { alineado ->

                        val jugador =
                            estado.plantilla.firstOrNull {
                                it.id == alineado.idJugador
                            }

                        if (jugador != null) {

                            TarjetaTitular(
                                jugador = jugador,
                                alineado = alineado,
                                slots = slots,
                                titulares = titulares,
                                editable = editable,
                                onCambiarPosicion = { idSlot ->
                                    viewModel.asignarASlot(
                                        jugador.id,
                                        idSlot
                                    )
                                },
                                onQuitar = {
                                    viewModel.pasarASuplente(
                                        jugador
                                    )
                                }
                            )
                        }
                    }
                }

                if (
                    estado.disponibles.isEmpty() &&
                    titulares.isEmpty()
                ) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = SportProSuperficie,
                            shape = FormaTarjetaSportPro,
                            border = BorderStroke(
                                1.dp,
                                SportProBorde
                            )
                        ) {
                            Text(
                                text =
                                    "No hay jugadores con disponibilidad confirmada para este partido.",
                                color = SportProTextoSecundario,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(18.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }

    val jugadorNoDisponible =
        estado.jugadorNoDisponible

    if (
        jugadorNoDisponible != null &&
        editable
    ) {
        DialogoJugadorNoDisponible(
            jugador = jugadorNoDisponible,
            onQuitar = {
                viewModel.quitarJugadorNoDisponible()
            },
            onIgnorar = {
                viewModel.ignorarJugadorNoDisponible()
            }
        )
    }
}

@Composable
private fun SelectorFormato(
    formatoActual: FormatoPartido,
    editable: Boolean,
    onCambiar: (FormatoPartido) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {

        FormatoPartido.entries.forEach { formato ->

            val seleccionado =
                formato == formatoActual

            if (seleccionado) {

                Button(
                    onClick = {},
                    enabled = editable,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(
                        horizontal = 4.dp
                    ),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SportProNavy,
                        contentColor = Color.White,
                        disabledContainerColor = SportProNavy,
                        disabledContentColor = Color.White
                    )
                ) {
                    Text(
                        text = formato.etiqueta,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

            } else {

                OutlinedButton(
                    onClick = {
                        onCambiar(formato)
                    },
                    enabled = editable,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(
                        horizontal = 4.dp
                    ),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(
                        1.dp,
                        SportProBorde
                    )
                ) {
                    Text(
                        text = formato.etiqueta,
                        style = MaterialTheme.typography.labelMedium,
                        color = SportProTextoSecundario
                    )
                }
            }
        }
    }
}

@Composable
private fun CanchaAlineacion(
    slots: List<SlotCampo>,
    titulares: List<JugadorAlineacion>,
    plantilla: List<Jugador>,
    editable: Boolean,
    onQuitarTitular: (Jugador) -> Unit
) {
    val titularesPorSlot =
        titulares.associateBy {
            it.posicionCampo
        }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(390.dp)
            .background(
                VerdeCancha,
                RoundedCornerShape(18.dp)
            )
    ) {

        Canvas(
            modifier = Modifier
                .matchParentSize()
                .padding(12.dp)
        ) {
            val ancho = size.width
            val alto = size.height
            val linea =
                Color.White.copy(alpha = 0.78f)

            drawRect(
                color = linea,
                style = Stroke(width = 3f)
            )

            drawLine(
                color = linea,
                start = Offset(
                    0f,
                    alto / 2f
                ),
                end = Offset(
                    ancho,
                    alto / 2f
                ),
                strokeWidth = 3f
            )

            drawCircle(
                color = linea,
                center = Offset(
                    ancho / 2f,
                    alto / 2f
                ),
                radius = ancho * 0.15f,
                style = Stroke(width = 3f)
            )

            drawCircle(
                color = linea,
                center = Offset(
                    ancho / 2f,
                    alto / 2f
                ),
                radius = 4f
            )

            // Área inferior
            drawRect(
                color = linea,
                topLeft = Offset(
                    ancho * 0.22f,
                    alto * 0.78f
                ),
                size = androidx.compose.ui.geometry.Size(
                    ancho * 0.56f,
                    alto * 0.22f
                ),
                style = Stroke(width = 3f)
            )

            // Área superior
            drawRect(
                color = linea,
                topLeft = Offset(
                    ancho * 0.22f,
                    0f
                ),
                size = androidx.compose.ui.geometry.Size(
                    ancho * 0.56f,
                    alto * 0.22f
                ),
                style = Stroke(width = 3f)
            )
        }

        slots.forEach { slot ->

            val alineado =
                titularesPorSlot[slot.id]

            val jugador =
                alineado?.let {
                        titular ->
                    plantilla.firstOrNull {
                        it.id ==
                                titular.idJugador
                    }
                }

            val x =
                (maxWidth.value * slot.x).dp -
                        27.dp

            val y =
                (390f * slot.y).dp -
                        27.dp

            Box(
                modifier = Modifier.offset(
                    x = x,
                    y = y
                )
            ) {

                if (jugador == null) {

                    SlotVacio(
                        etiqueta = slot.id
                    )

                } else {

                    JugadorEnCancha(
                        jugador = jugador,
                        etiqueta = slot.id,
                        editable = editable,
                        onClick = {
                            if (editable) {
                                onQuitarTitular(
                                    jugador
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SlotVacio(
    etiqueta: String
) {
    Column(
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Surface(
            modifier = Modifier.size(54.dp),
            shape = CircleShape,
            color = VerdeCanchaOscuro.copy(
                alpha = 0.9f
            ),
            border = BorderStroke(
                1.dp,
                Color.White.copy(
                    alpha = 0.75f
                )
            )
        ) {

            Box(
                contentAlignment =
                    Alignment.Center
            ) {
                Text(
                    text = "+",
                    color = Color.White,
                    style =
                        MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Light
                )
            }
        }

        Text(
            text = etiquetaCortaSlot(
                etiqueta
            ),
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun JugadorEnCancha(
    jugador: Jugador,
    etiqueta: String,
    editable: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.clickable(
            enabled = editable,
            onClick = onClick
        ),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Surface(
            modifier = Modifier.size(54.dp),
            shape = CircleShape,
            color = SportProNavy,
            border = BorderStroke(
                2.dp,
                Color.White
            ),
            shadowElevation = 4.dp
        ) {

            Box(
                contentAlignment =
                    Alignment.Center
            ) {

                Text(
                    text =
                        inicialesJugador(jugador),
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
            }
        }

        Text(
            text =
                jugador.nombres
                    .substringBefore(" ")
                    .take(9),
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text =
                etiquetaCortaSlot(
                    etiqueta
                ),
            color =
                Color.White.copy(
                    alpha = 0.82f
                ),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun TarjetaSuplente(
    jugador: Jugador,
    editable: Boolean,
    onAgregar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = SportProSuperficie
        ),
        shape = FormaTarjetaSportPro,
        border = BorderStroke(
            1.dp,
            SportProBorde
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            AvatarJugador(jugador)

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = jugador.nombreCompleto(),
                    color = SportProNavy,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text =
                        abreviarPosicionAlineacion(
                            jugador.posicion
                        ),
                    color = SportProTextoSecundario,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Surface(
                modifier = Modifier
                    .size(38.dp)
                    .clickable(
                        enabled = editable,
                        onClick = onAgregar
                    ),
                shape = CircleShape,
                color = if (editable) {
                    SportProVerdeClaro
                } else {
                    SportProFondo
                }
            ) {

                Box(
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = "+",
                        color = if (editable) {
                            SportProVerde
                        } else {
                            SportProTextoSecundario
                        },
                        fontWeight = FontWeight.Black,
                        style =
                            MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaTitular(
    jugador: Jugador,
    alineado: JugadorAlineacion,
    slots: List<SlotCampo>,
    titulares: List<JugadorAlineacion>,
    editable: Boolean,
    onCambiarPosicion: (String) -> Unit,
    onQuitar: () -> Unit
) {
    var menuAbierto by remember {
        mutableStateOf(false)
    }

    val posicionesOcupadas =
        titulares
            .filter {
                it.idJugador != jugador.id
            }
            .map {
                it.posicionCampo
            }
            .toSet()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = SportProSuperficie
        ),
        shape = FormaTarjetaSportPro,
        border = BorderStroke(
            1.dp,
            Color(0xFFB9E0C3)
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            AvatarJugador(jugador)

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = jugador.nombreCompleto(),
                    color = SportProNavy,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text =
                        "Habitual: ${
                            abreviarPosicionAlineacion(
                                jugador.posicion
                            )
                        }",
                    color = SportProTextoSecundario,
                    style =
                        MaterialTheme.typography.bodySmall
                )
            }

            Box {

                OutlinedButton(
                    onClick = {
                        menuAbierto = true
                    },
                    enabled = editable,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(
                        horizontal = 9.dp
                    )
                ) {

                    Text(
                        text =
                            etiquetaCortaSlot(
                                alineado.posicionCampo
                            ),
                        style =
                            MaterialTheme.typography.labelMedium
                    )
                }

                DropdownMenu(
                    expanded = menuAbierto,
                    onDismissRequest = {
                        menuAbierto = false
                    }
                ) {

                    slots.forEach { slot ->

                        val ocupado =
                            slot.id in
                                    posicionesOcupadas

                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (ocupado) {
                                        "${slot.etiqueta} · ocupado"
                                    } else {
                                        slot.etiqueta
                                    }
                                )
                            },
                            enabled = !ocupado,
                            onClick = {
                                menuAbierto = false
                                onCambiarPosicion(
                                    slot.id
                                )
                            }
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .size(36.dp)
                    .clickable(
                        enabled = editable,
                        onClick = onQuitar
                    ),
                shape = CircleShape,
                color = SportProRojoClaro
            ) {

                Box(
                    contentAlignment =
                        Alignment.Center
                ) {

                    Text(
                        text = "−",
                        color = SportProRojo,
                        fontWeight = FontWeight.Black,
                        style =
                            MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun AvatarJugador(
    jugador: Jugador
) {
    Surface(
        modifier = Modifier.size(42.dp),
        shape = CircleShape,
        color = SportProNavy
    ) {

        Box(
            contentAlignment = Alignment.Center
        ) {

            Text(
                text =
                    inicialesJugador(jugador),
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun BarraConfirmarAlineacion(
    titulares: Int,
    maximo: Int,
    editable: Boolean,
    puedeConfirmar: Boolean,
    guardando: Boolean,
    confirmada: Boolean,
    onConfirmar: () -> Unit
) {
    Surface(
        color = SportProFondo,
        shadowElevation = 6.dp
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 20.dp,
                    vertical = 10.dp
                )
        ) {

            if (
                editable &&
                titulares < maximo
            ) {
                Text(
                    text =
                        "Completa los $maximo titulares y todas las posiciones para confirmar.",
                    color = SportProTextoSecundario,
                    style =
                        MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(
                        bottom = 6.dp
                    )
                )
            }

            Button(
                onClick = onConfirmar,
                enabled =
                    puedeConfirmar &&
                            !guardando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = FormaBotonSportPro,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = SportProNavy,
                        contentColor = Color.White,
                        disabledContainerColor =
                            SportProNavy.copy(
                                alpha = 0.4f
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

                    Text(
                        text =
                            if (
                                confirmada &&
                                puedeConfirmar
                            ) {
                                "Confirmar cambios"
                            } else {
                                "Confirmar alineación"
                            },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun DialogoJugadorNoDisponible(
    jugador: Jugador,
    onQuitar: () -> Unit,
    onIgnorar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onIgnorar,

        icon = {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = CircleShape,
                color = SportProRojoClaro
            ) {

                Box(
                    contentAlignment =
                        Alignment.Center
                ) {
                    Text(
                        text = "!",
                        color = SportProRojo,
                        style =
                            MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        },

        title = {
            Column(
                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {

                Text(
                    text =
                        "JUGADOR NO DISPONIBLE",
                    color = SportProRojo,
                    style =
                        MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text =
                        jugador.nombreCompleto(),
                    color = SportProNavy,
                    style =
                        MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black
                )
            }
        },

        text = {
            Text(
                text =
                    "Este jugador ya no está disponible para el partido. Puedes quitarlo de titulares o revisar la situación antes de continuar.",
                color = SportProTextoSecundario,
                textAlign = TextAlign.Center
            )
        },

        confirmButton = {
            Button(
                onClick = onQuitar,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            SportProRojo,
                        contentColor =
                            Color.White
                    ),
                shape = FormaBotonSportPro
            ) {
                Text(
                    "Quitar de titulares"
                )
            }
        },

        dismissButton = {
            TextButton(
                onClick = onIgnorar
            ) {
                Text(
                    text = "Ignorar",
                    color = SportProNavy
                )
            }
        }
    )
}

@Composable
private fun MensajeAlineacion(
    mensaje: String,
    error: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (error) {
            SportProRojoClaro
        } else {
            SportProVerdeClaro
        },
        shape = RoundedCornerShape(12.dp)
    ) {

        Text(
            text = mensaje,
            color = if (error) {
                SportProRojo
            } else {
                SportProVerde
            },
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun TextoVacioAlineacion(
    texto: String
) {
    Text(
        text = texto,
        color = SportProTextoSecundario,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.padding(
            vertical = 6.dp
        )
    )
}

@Composable
private fun CabeceraAlineacionSportPro() {
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
                    contentAlignment =
                        Alignment.Center
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
                contentAlignment =
                    Alignment.Center
            ) {
                Text("🔔")
            }
        }
    }
}

private fun inicialesJugador(
    jugador: Jugador
): String {
    val primera =
        jugador.nombres
            .trim()
            .firstOrNull()
            ?.uppercaseChar()
            ?.toString()
            .orEmpty()

    val segunda =
        jugador.apellidos
            .trim()
            .firstOrNull()
            ?.uppercaseChar()
            ?.toString()
            .orEmpty()

    return "$primera$segunda"
        .ifBlank {
            "JU"
        }
}

private fun abreviarPosicionAlineacion(
    posicion: String
): String =
    when (
        posicion
            .trim()
            .lowercase()
    ) {

        "portero" ->
            "POR"

        "defensa" ->
            "DEF"

        "mediocampista" ->
            "MED"

        "delantero" ->
            "DEL"

        else ->
            posicion
                .take(3)
                .uppercase()
                .ifBlank {
                    "N/D"
                }
    }

private fun etiquetaCortaSlot(
    id: String
): String =
    when {
        id == "ARQ" ->
            "POR"

        id.startsWith("DEF_IZQ") ->
            "DEF IZQ"

        id.startsWith("DEF_DER") ->
            "DEF DER"

        id.startsWith("DEF") ->
            "DEF"

        id.startsWith("MED_IZQ") ->
            "MED IZQ"

        id.startsWith("MED_DER") ->
            "MED DER"

        id.startsWith("MED") ->
            "MED"

        id.startsWith("DEL_IZQ") ->
            "DEL IZQ"

        id.startsWith("DEL_DER") ->
            "DEL DER"

        id.startsWith("DEL") ->
            "DEL"

        else ->
            id
    }