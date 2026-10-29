package com.example.projectsportpro2026_2.ui.eventospartido

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectsportpro2026_2.datos.modelo.Jugador
import com.example.projectsportpro2026_2.datos.modelo.evento.EquipoEvento
import com.example.projectsportpro2026_2.datos.modelo.evento.EventoPartido
import com.example.projectsportpro2026_2.datos.modelo.evento.TipoEventoCatalogo
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPartidoEnVivo(
    idPartido: String,
    onVolver: () -> Unit,
    viewModel: PartidoEnVivoViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()

    LaunchedEffect(idPartido) {
        viewModel.iniciar(idPartido)
    }

    val contexto = estado.contexto

    Scaffold(
        containerColor = SportProFondo
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
                bottom = 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            item {
                CabeceraPartidoEnVivo()
            }

            item {
                TextButton(
                    onClick = onVolver,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "← Partidos",
                        color = SportProVerde,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (
                estado.cargando &&
                contexto == null
            ) {
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

                return@LazyColumn
            }

            estado.error?.let { mensaje ->
                item {
                    MensajePartidoEnVivo(
                        mensaje = mensaje,
                        error = true
                    )
                }
            }

            estado.mensaje?.let { mensaje ->
                item {
                    MensajePartidoEnVivo(
                        mensaje = mensaje,
                        error = false
                    )
                }
            }

            if (contexto == null) {
                item {
                    Text(
                        text = "No se pudo cargar el partido en vivo.",
                        color = SportProTextoSecundario
                    )
                }

                return@LazyColumn
            }

            item {
                TarjetaMarcador(
                    nombreEquipo =
                        contexto.equipo.nombre,

                    nombreRival =
                        contexto.partido.nombreRival,

                    golesPropios =
                        estado.marcador.propio,

                    golesRival =
                        estado.marcador.rival,

                    estadoPartido =
                        contexto.partido.estado
                )
            }

            if (
                contexto.partido.estado ==
                EstadoPartido.EN_CURSO
            ) {

                item {
                    Text(
                        text = "REGISTRO RÁPIDO",
                        color = SportProTextoSecundario,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black
                    )
                }

                item {
                    PanelEventosRapidos(
                        tipos =
                            contexto.tiposEvento,

                        onEvento = { tipo ->
                            viewModel.abrirEvento(
                                tipo
                            )
                        }
                    )
                }

            } else {

                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = SportProFondo,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            1.dp,
                            SportProBorde
                        )
                    ) {
                        Text(
                            text = "El registro de eventos está cerrado porque el partido ya no está en curso.",
                            modifier = Modifier.padding(14.dp),
                            color = SportProTextoSecundario,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.SpaceBetween,
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Text(
                        text = "Cronología",
                        color = SportProNavy,
                        style =
                            MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black
                    )

                    TextButton(
                        onClick = {
                            viewModel.abrirAuditoriaGeneral()
                        }
                    ) {
                        Text(
                            text = "Auditoría",
                            color = SportProVerde,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (estado.eventos.isEmpty()) {

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
                            text = "Todavía no se han registrado eventos.",
                            modifier = Modifier.padding(20.dp),
                            color = SportProTextoSecundario
                        )
                    }
                }

            } else {

                estado.eventos.forEach { evento ->

                    item(
                        key = evento.id
                    ) {
                        TarjetaCronologiaEvento(
                            evento = evento,

                            onClick = {
                                viewModel.abrirDetalleEvento(
                                    evento
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    if (
        estado.mostrarFormulario &&
        estado.formulario.tipo != null &&
        contexto != null
    ) {

        ModalRegistroEvento(
            formulario =
                estado.formulario,

            nombreEquipo =
                contexto.equipo.nombre,

            nombreRival =
                contexto.partido.nombreRival,

            jugadores =
                contexto.jugadoresAlineados,

            registrando =
                estado.registrando,

            error =
                estado.error,

            onCerrar = {
                viewModel.cerrarFormulario()
            },

            onCambiarMinuto =
                viewModel::cambiarMinuto,

            onCambiarEquipo =
                viewModel::cambiarEquipo,

            onCambiarJugador =
                viewModel::cambiarJugador,

            onCambiarJugadorRival =
                viewModel::cambiarNombreJugadorRival,

            onCambiarObservacion =
                viewModel::cambiarObservacion,

            onRegistrar = {
                viewModel.registrarEvento()
            }
        )
    }
        estado.eventoSeleccionado?.let {
                evento ->

            if (contexto != null) {

                ModalGestionEvento(
                    evento =
                        evento,

                    accion =
                        estado.accionEvento,

                    formulario =
                        estado.formulario,

                    motivo =
                        estado.motivoCambio,

                    jugadores =
                        contexto.jugadoresAlineados,

                    nombreEquipo =
                        contexto.equipo.nombre,

                    nombreRival =
                        contexto.partido.nombreRival,

                    auditoria =
                        estado.auditoriaEvento,

                    cargandoAuditoria =
                        estado.cargandoAuditoria,

                    procesando =
                        estado.procesandoCambio,

                    error =
                        estado.error,

                    onCerrar =
                        viewModel::cerrarDetalleEvento,

                    onCorregir =
                        viewModel::iniciarCorreccionEvento,

                    onAnular =
                        viewModel::iniciarAnulacionEvento,

                    onCancelarAccion =
                        viewModel::cancelarAccionEvento,

                    onMotivo =
                        viewModel::cambiarMotivoCambio,

                    onMinuto =
                        viewModel::cambiarMinuto,

                    onEquipo =
                        viewModel::cambiarEquipo,

                    onJugador =
                        viewModel::cambiarJugador,

                    onJugadorRival =
                        viewModel::cambiarNombreJugadorRival,

                    onObservacion =
                        viewModel::cambiarObservacion,

                    onGuardarCorreccion =
                        viewModel::guardarCorreccionEvento,

                    onConfirmarAnulacion =
                        viewModel::confirmarAnulacionEvento
                )
            }
        }
    if (
        estado.mostrarAuditoriaGeneral &&
        contexto != null
    ) {

        ModalAuditoriaGeneral(
            eventos =
                estado.eventosAuditoria,

            cargando =
                estado.cargandoAuditoriaGeneral,

            onCerrar = {
                viewModel.cerrarAuditoriaGeneral()
            },

            onAbrirEvento = { evento ->

                viewModel.cerrarAuditoriaGeneral()

                viewModel.abrirDetalleEvento(
                    evento
                )
            }
        )
    }
}

@Composable
private fun TarjetaMarcador(
    nombreEquipo: String,
    nombreRival: String,
    golesPropios: Int,
    golesRival: Int,
    estadoPartido: EstadoPartido
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = SportProNavy
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 18.dp,
                    vertical = 20.dp
                ),
            horizontalAlignment =
                Alignment.CenterHorizontally,
            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            Text(
                text =
                    if (
                        estadoPartido ==
                        EstadoPartido.EN_CURSO
                    ) {
                        "PARTIDO EN CURSO"
                    } else {
                        textoEstadoPartido(
                            estadoPartido
                        )
                    },
                color = SportProVerdeClaro,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically,
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                Text(
                    text =
                        nombreEquipo.uppercase(),
                    modifier = Modifier.weight(1f),
                    color = Color.White,
                    style =
                        MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text =
                        "$golesPropios–$golesRival",
                    modifier = Modifier.padding(
                        horizontal = 18.dp
                    ),
                    color = Color.White,
                    style =
                        MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text =
                        nombreRival.uppercase(),
                    modifier = Modifier.weight(1f),
                    color = Color.White,
                    style =
                        MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            if (
                estadoPartido ==
                EstadoPartido.EN_CURSO
            ) {
                Text(
                    text = "EN CURSO",
                    color = SportProVerdeClaro,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun PanelEventosRapidos(
    tipos: List<TipoEventoCatalogo>,
    onEvento: (TipoEventoCatalogo) -> Unit
) {
    Column(
        verticalArrangement =
            Arrangement.spacedBy(7.dp)
    ) {

        tipos
            .chunked(4)
            .forEach { fila ->

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(7.dp)
                ) {

                    fila.forEach { tipo ->

                        val habilitado =
                            tipo.codigo != "INICIO"

                        BotonEventoRapido(
                            tipo = tipo,
                            habilitado = habilitado,
                            modifier =
                                Modifier.weight(1f),
                            onClick = {
                                onEvento(tipo)
                            }
                        )
                    }

                    repeat(
                        4 - fila.size
                    ) {
                        Spacer(
                            modifier =
                                Modifier.weight(1f)
                        )
                    }
                }
            }
    }
}

@Composable
private fun BotonEventoRapido(
    tipo: TipoEventoCatalogo,
    habilitado: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val destacado =
        tipo.afectaMarcador

    Surface(
        modifier = modifier
            .height(66.dp)
            .clickable(
                enabled = habilitado,
                onClick = onClick
            ),
        shape = RoundedCornerShape(12.dp),
        color =
            when {
                !habilitado ->
                    Color(0xFFE8ECEA)

                destacado ->
                    Color(0xFF1CA364)

                else ->
                    Color(0xFFEAF1EC)
            }
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            horizontalAlignment =
                Alignment.CenterHorizontally,
            verticalArrangement =
                Arrangement.Center
        ) {

            Text(
                text =
                    iconoEvento(
                        tipo.codigo
                    ),
                style =
                    MaterialTheme.typography.titleMedium
            )

            Spacer(
                modifier = Modifier.height(3.dp)
            )

            Text(
                text =
                    nombreCortoEvento(
                        tipo.nombre
                    ),
                color =
                    if (destacado && habilitado) {
                        Color.White
                    } else {
                        SportProNavy
                    },
                style =
                    MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TarjetaCronologiaEvento(
    evento: EventoPartido,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
            .clickable(
            onClick = onClick
        ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = SportProSuperficie
        ),
        border = BorderStroke(
            1.dp,
            SportProBorde
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment =
                Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            Surface(
                modifier = Modifier.size(8.dp),
                shape = CircleShape,
                color = SportProVerde
            ) {}

            Text(
                text =
                    "${evento.minuto}'",
                color = SportProVerde,
                style =
                    MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black
            )

            Text(
                text =
                    iconoEvento(
                        evento.codigoTipoEvento
                    ),
                style =
                    MaterialTheme.typography.titleMedium
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Row(
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Text(
                        text =
                            evento.nombreTipoEvento,
                        color = SportProTextoPrincipal,
                        style =
                            MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (
                        evento.nombreJugador
                            .isNotBlank()
                    ) {

                        Text(
                            text =
                                " · ${evento.nombreJugador}",
                            color =
                                SportProTextoSecundario,
                            style =
                                MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow =
                                TextOverflow.Ellipsis
                        )
                    }
                }

                if (
                    evento.observacion
                        .isNotBlank()
                ) {
                    Text(
                        text =
                            evento.observacion,
                        color = SportProTextoSecundario,
                        style =
                            MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow =
                            TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModalRegistroEvento(
    formulario: FormularioEventoPartido,
    nombreEquipo: String,
    nombreRival: String,
    jugadores: List<Jugador>,
    registrando: Boolean,
    error: String?,
    onCerrar: () -> Unit,
    onCambiarMinuto: (String) -> Unit,
    onCambiarEquipo: (EquipoEvento) -> Unit,
    onCambiarJugador: (String) -> Unit,
    onCambiarJugadorRival: (String) -> Unit,
    onCambiarObservacion: (String) -> Unit,
    onRegistrar: () -> Unit

) {
    val tipo =
        formulario.tipo
            ?: return

    val estadoHoja =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )

    ModalBottomSheet(
        onDismissRequest = onCerrar,
        sheetState = estadoHoja,
        containerColor = SportProSuperficie,
        shape = RoundedCornerShape(
            topStart = 26.dp,
            topEnd = 26.dp
        ),
        dragHandle = {
            Surface(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .size(
                        width = 36.dp,
                        height = 4.dp
                    ),
                shape = CircleShape,
                color = SportProBorde
            ) {}
        }
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    bottom = 28.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween,
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Column {

                    Text(
                        text = "NUEVO EVENTO",
                        color = SportProVerde,
                        style =
                            MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black
                    )

                    Row(
                        verticalAlignment =
                            Alignment.CenterVertically,
                        horizontalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {

                        Text(
                            text =
                                iconoEvento(
                                    tipo.codigo
                                ),
                            style =
                                MaterialTheme.typography.headlineSmall
                        )

                        Text(
                            text = tipo.nombre,
                            color = SportProNavy,
                            style =
                                MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .size(34.dp)
                        .clickable(
                            enabled =
                                !registrando,
                            onClick =
                                onCerrar
                        ),
                    shape = CircleShape,
                    color = SportProFondo
                ) {
                    Box(
                        contentAlignment =
                            Alignment.Center
                    ) {
                        Text(
                            text = "×",
                            color = SportProNavy,
                            fontWeight =
                                FontWeight.Bold
                        )
                    }
                }
            }

            OutlinedTextField(
                value =
                    formulario.minuto,
                onValueChange =
                    onCambiarMinuto,
                modifier =
                    Modifier.fillMaxWidth(),
                enabled = !registrando,
                singleLine = true,
                label = {
                    Text("Minuto")
                },
                placeholder = {
                    Text("Ej. 37")
                },
                shape =
                    RoundedCornerShape(12.dp)
            )

            if (tipo.requiereEquipo) {

                Text(
                    text =
                        textoEquipoSegunEvento(
                            tipo
                        ),
                    color = SportProTextoSecundario,
                    style =
                        MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black
                )

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {

                    BotonSeleccionEquipo(
                        texto =
                            nombreEquipo,

                        seleccionado =
                            formulario.equipoEvento ==
                                    EquipoEvento.PROPIO,

                        modifier =
                            Modifier.weight(1f),

                        onClick = {
                            onCambiarEquipo(
                                EquipoEvento.PROPIO
                            )
                        }
                    )

                    BotonSeleccionEquipo(
                        texto =
                            nombreRival,

                        seleccionado =
                            formulario.equipoEvento ==
                                    EquipoEvento.RIVAL,

                        modifier =
                            Modifier.weight(1f),

                        onClick = {
                            onCambiarEquipo(
                                EquipoEvento.RIVAL
                            )
                        }
                    )
                }
            }

            if (tipo.requiereJugador) {

                if (
                    formulario.equipoEvento ==
                    EquipoEvento.RIVAL
                ) {

                    OutlinedTextField(
                        value =
                            formulario
                                .nombreJugadorRival,

                        onValueChange =
                            onCambiarJugadorRival,

                        modifier =
                            Modifier.fillMaxWidth(),

                        enabled =
                            !registrando,

                        singleLine = true,

                        label = {
                            Text(
                                "Jugador rival"
                            )
                        },

                        placeholder = {
                            Text(
                                "Nombre del jugador"
                            )
                        },

                        shape =
                            RoundedCornerShape(
                                12.dp
                            )
                    )

                } else {

                    SelectorJugador(
                        jugadores =
                            jugadores,

                        idSeleccionado =
                            formulario.idJugador,

                        habilitado =
                            !registrando,

                        onSeleccionar =
                            onCambiarJugador
                    )
                }
            }

            if (
                tipo.requiereObservacion
            ) {

                OutlinedTextField(
                    value =
                        formulario.observacion,

                    onValueChange =
                        onCambiarObservacion,

                    modifier =
                        Modifier.fillMaxWidth(),

                    enabled =
                        !registrando,

                    minLines = 2,

                    label = {
                        Text("Observación")
                    },

                    placeholder = {
                        Text(
                            "Describe brevemente el evento"
                        )
                    },

                    shape =
                        RoundedCornerShape(12.dp)
                )
            }

            error?.let { mensaje ->

                Surface(
                    modifier =
                        Modifier.fillMaxWidth(),
                    color =
                        SportProRojoClaro,
                    shape =
                        RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = mensaje,
                        modifier =
                            Modifier.padding(10.dp),
                        color = SportProRojo,
                        style =
                            MaterialTheme.typography.bodySmall
                    )
                }
            }

            Button(
                onClick = onRegistrar,
                enabled =
                    !registrando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = FormaBotonSportPro,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            SportProVerde,
                        contentColor =
                            Color.White
                    )
            ) {

                if (registrando) {

                    CircularProgressIndicator(
                        modifier =
                            Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )

                } else {

                    Text(
                        text =
                            if (
                                tipo.codigo ==
                                "FIN"
                            ) {
                                "Registrar y finalizar"
                            } else {
                                "Registrar ${tipo.nombre}"
                            },
                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun BotonSeleccionEquipo(
    texto: String,
    seleccionado: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    if (seleccionado) {

        Button(
            onClick = onClick,
            modifier = modifier,
            shape = RoundedCornerShape(12.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor =
                        SportProVerde,
                    contentColor =
                        Color.White
                )
        ) {
            Text(
                text = texto,
                maxLines = 1,
                overflow =
                    TextOverflow.Ellipsis,
                style =
                    MaterialTheme.typography.labelMedium
            )
        }

    } else {

        OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(
                1.dp,
                SportProBorde
            )
        ) {
            Text(
                text = texto,
                color = SportProTextoSecundario,
                maxLines = 1,
                overflow =
                    TextOverflow.Ellipsis,
                style =
                    MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun SelectorJugador(
    jugadores: List<Jugador>,
    idSeleccionado: String,
    habilitado: Boolean,
    onSeleccionar: (String) -> Unit
) {
    var abierto by remember {
        mutableStateOf(false)
    }

    val jugadorSeleccionado =
        jugadores.firstOrNull {
            it.id == idSeleccionado
        }

    Column {

        Text(
            text = "JUGADOR",
            color = SportProTextoSecundario,
            style =
                MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black
        )

        Spacer(
            modifier = Modifier.height(5.dp)
        )

        Box(
            modifier = Modifier.fillMaxWidth()
        ) {

            OutlinedButton(
                onClick = {
                    if (habilitado) {
                        abierto = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                border =
                    BorderStroke(
                        1.dp,
                        SportProBorde
                    )
            ) {

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.SpaceBetween
                ) {

                    Text(
                        text =
                            jugadorSeleccionado
                                ?.nombreCompleto()
                                ?: "Seleccionar jugador",
                        color =
                            if (
                                jugadorSeleccionado !=
                                null
                            ) {
                                SportProTextoPrincipal
                            } else {
                                SportProTextoSecundario
                            },
                        maxLines = 1,
                        overflow =
                            TextOverflow.Ellipsis
                    )

                    Text(
                        text = "⌄",
                        color = SportProNavy
                    )
                }
            }

            DropdownMenu(
                expanded = abierto,
                onDismissRequest = {
                    abierto = false
                }
            ) {

                jugadores.forEach { jugador ->

                    DropdownMenuItem(
                        text = {
                            Text(
                                jugador
                                    .nombreCompleto()
                            )
                        },
                        onClick = {
                            onSeleccionar(
                                jugador.id
                            )

                            abierto = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MensajePartidoEnVivo(
    mensaje: String,
    error: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color =
            if (error) {
                SportProRojoClaro
            } else {
                SportProVerdeClaro
            },
        shape = RoundedCornerShape(12.dp)
    ) {

        Text(
            text = mensaje,
            modifier = Modifier.padding(12.dp),
            color =
                if (error) {
                    SportProRojo
                } else {
                    SportProVerde
                },
            style =
                MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun CabeceraPartidoEnVivo() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.SpaceBetween,
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Row(
            verticalAlignment =
                Alignment.CenterVertically,
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
                    text = "PARTIDO EN VIVO",
                    color =
                        SportProTextoSecundario,
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
                Text("⚽")
            }
        }
    }
}

private fun textoEquipoSegunEvento(
    tipo: TipoEventoCatalogo
): String =
    if (
        tipo.afectaMarcador
    ) {
        "EQUIPO QUE ANOTA"
    } else {
        "EQUIPO DEL EVENTO"
    }

private fun iconoEvento(
    codigo: String
): String =
    when (codigo) {

        "INICIO" -> "▶"
        "FIN" -> "■"

        "SAQUE_META" -> "↑"

        "SAQUE_LATERAL" -> "↔"

        "TIRO_ESQUINA" -> "⌒"

        "FALTA" -> "⚑"

        "PENAL" -> "◎"

        "GOL" -> "⚽"

        "TARJETA" -> "▬"

        "FUERA_JUEGO" -> "⊘"

        "CAMBIO" -> "↕"

        else -> "•"
    }

private fun nombreCortoEvento(
    nombre: String
): String {

    val limpio =
        nombre.trim()

    return when {

        limpio.equals(
            "Saque de meta",
            ignoreCase = true
        ) ->
            "Sq. Meta"

        limpio.equals(
            "Saque lateral",
            ignoreCase = true
        ) ->
            "Sq. Lat."

        limpio.equals(
            "Tiro de esquina",
            ignoreCase = true
        ) ->
            "Esquina"

        limpio.equals(
            "Fuera de juego",
            ignoreCase = true
        ) ->
            "F. Juego"

        else ->
            limpio
    }
}

private fun textoEstadoPartido(
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