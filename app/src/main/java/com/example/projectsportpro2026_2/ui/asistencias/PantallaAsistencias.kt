package com.example.projectsportpro2026_2.ui.asistencias

import com.example.projectsportpro2026_2.ui.comun.ContadorLed
import com.example.projectsportpro2026_2.ui.comun.TipoEstado
import com.example.projectsportpro2026_2.ui.comun.ChipEstado
import com.example.projectsportpro2026_2.ui.theme.FamiliaTitulos
import androidx.compose.foundation.background
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import com.example.projectsportpro2026_2.ui.comun.anchoCompleto
import com.example.projectsportpro2026_2.ui.comun.CabeceraCancha
import com.example.projectsportpro2026_2.ui.theme.LineaDeCal
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectsportpro2026_2.datos.modelo.Asistencia
import com.example.projectsportpro2026_2.datos.modelo.EstadoAsistencia
import com.example.projectsportpro2026_2.datos.modelo.Jugador
import com.example.projectsportpro2026_2.datos.modelo.Rol

private val AzulSportPro: Color @Composable get() = LineaDeCal.colores.textoPrincipal // antes Color(0xFF0B3547)

private val VerdeSportPro: Color @Composable get() = LineaDeCal.colores.verde // antes Color(0xFF168F5B)

private val RojoSportPro: Color @Composable get() = LineaDeCal.colores.rojo // antes Color(0xFFB83232)

private val AmarilloSportPro: Color @Composable get() = LineaDeCal.colores.amarilloTexto // antes Color(0xFF8A6814)

private val VerdeClaro: Color @Composable get() = LineaDeCal.colores.okFondo // antes Color(0xFFE6F5EC)

private val RojoClaro: Color @Composable get() = LineaDeCal.colores.errorFondo // antes Color(0xFFFFE9E9)

private val AmarilloClaro: Color @Composable get() = LineaDeCal.colores.amarilloFondo // antes Color(0xFFFFF4D6)

private val GrisClaro: Color @Composable get() = LineaDeCal.colores.apagadoFondo // antes Color(0xFFF0F2F3)

private val FondoSportPro: Color @Composable get() = LineaDeCal.colores.fondo // antes Color(0xFFF7F9F8)

@Composable
fun PantallaAsistencias(
    onVolver: () -> Unit,
    viewModel: AsistenciasViewModel =
        viewModel()
) {
    val estado by
    viewModel.estado
        .collectAsState()

    var menuSesion by
    remember {
        mutableStateOf(false)
    }

    var menuJugador by
    remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        viewModel.iniciar()
    }

    val puedeGestionar =
        estado.rol == Rol.DT ||
                estado.rol == Rol.ADM

    Scaffold(
        containerColor =
            FondoSportPro
    ) {
            padding ->

        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(
                        horizontal = 16.dp
                    ),
            verticalArrangement =
                Arrangement.spacedBy(
                    8.dp
                ),
            contentPadding =
                PaddingValues(
                    bottom = 20.dp
                )
        ) {
            // Línea de cal: cabecera de césped (mismos textos y el mismo "Volver")
            item {
                CabeceraCancha(
                    titulo = if (puedeGestionar) {
                        "Registro de asistencia"
                    } else {
                        "Historial de asistencia"
                    },
                    etiqueta = "ASISTENCIA",
                    onVolver = onVolver,
                    descripcionVolver = "Volver",
                    modifier = Modifier.anchoCompleto(16.dp)
                )
            }

            if (puedeGestionar) {
                item {
                    Box(
                        modifier =
                            Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = {
                                menuSesion =
                                    true
                            },
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .heightIn(
                                        min = 48.dp
                                    ),
                            colors =
                                ButtonDefaults
                                    .outlinedButtonColors(
                                        contentColor =
                                            AzulSportPro
                                    )
                        ) {
                            val sesion =
                                estado.sesiones
                                    .firstOrNull {
                                        it.id ==
                                                estado
                                                    .idSesionSeleccionada
                                    }

                            Text(
                                sesion?.let {
                                    "${it.nombreEquipo} · ${it.fecha} · ${it.hora}"
                                }
                                    ?: "Seleccionar entrenamiento"
                            )
                        }

                        DropdownMenu(
                            expanded =
                                menuSesion,
                            onDismissRequest = {
                                menuSesion =
                                    false
                            }
                        ) {
                            if (
                                estado.sesiones
                                    .isEmpty()
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "No hay entrenamientos disponibles"
                                        )
                                    },
                                    enabled =
                                        false,
                                    onClick = {}
                                )
                            } else {
                                estado.sesiones
                                    .forEach {
                                            sesion ->

                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    "${sesion.nombreEquipo} · ${sesion.fecha} · ${sesion.hora}",
                                                    color =
                                                        AzulSportPro
                                                )
                                            },
                                            onClick = {
                                                viewModel
                                                    .seleccionarSesion(
                                                        sesion.id
                                                    )

                                                menuSesion =
                                                    false
                                            }
                                        )
                                    }
                            }
                        }
                    }
                }

                estado.error?.let {
                        error ->

                    item {
                        MensajeAsistencia(
                            texto =
                                error,
                            fondo =
                                RojoClaro,
                            color =
                                RojoSportPro
                        )
                    }
                }

                estado.mensaje?.let {
                        mensaje ->

                    item {
                        MensajeAsistencia(
                            texto =
                                mensaje,
                            fondo =
                                VerdeClaro,
                            color =
                                VerdeSportPro
                        )
                    }
                }

                when {
                    estado.cargando -> {
                        item {
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(140.dp),
                                contentAlignment =
                                    Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color =
                                        VerdeSportPro
                                )
                            }
                        }
                    }

                    estado.idSesionSeleccionada
                        .isBlank() -> {
                        item {
                            TextoVacio(
                                "Selecciona un entrenamiento."
                            )
                        }
                    }

                    estado.jugadores
                        .isEmpty() -> {
                        item {
                            TextoVacio(
                                "No hay jugadores en este equipo."
                            )
                        }
                    }

                    estado.asistenciaRegistrada &&
                            !estado.modoCorreccion -> {

                        item {
                            Text(
                                "ASISTENCIA REGISTRADA",
                                color =
                                    VerdeSportPro,
                                fontSize = 12.sp,
                                fontWeight =
                                    FontWeight.Bold
                            )
                        }

                        item {
                            val presentes =
                                estado.estados.values
                                    .count {
                                        it ==
                                                EstadoAsistencia.PRESENTE
                                    }

                            val ausentes =
                                estado.estados.values
                                    .count {
                                        it ==
                                                EstadoAsistencia.AUSENTE
                                    }

                            val justificados =
                                estado.estados.values
                                    .count {
                                        it ==
                                                EstadoAsistencia.JUSTIFICADO
                                    }

                            Row(
                                modifier =
                                    Modifier.fillMaxWidth(),
                                horizontalArrangement =
                                    Arrangement.SpaceBetween
                            ) {
                                TarjetaResumen(
                                    cantidad =
                                        presentes,
                                    texto =
                                        "Presentes",
                                    fondo =
                                        VerdeClaro,
                                    color =
                                        VerdeSportPro
                                )

                                TarjetaResumen(
                                    cantidad =
                                        ausentes,
                                    texto =
                                        "Ausentes",
                                    fondo =
                                        RojoClaro,
                                    color =
                                        RojoSportPro
                                )

                                TarjetaResumen(
                                    cantidad =
                                        justificados,
                                    texto =
                                        "Justificados",
                                    fondo =
                                        AmarilloClaro,
                                    color =
                                        AmarilloSportPro
                                )
                            }
                        }

                        items(
                            items =
                                estado.jugadores,
                            key = {
                                it.id
                            }
                        ) {
                                jugador ->

                            TarjetaJugadorRegistrado(
                                jugador =
                                    jugador,
                                estado =
                                    estado.estados[
                                        jugador.id
                                    ]
                            )
                        }

                        item {
                            Button(
                                onClick = {
                                    viewModel
                                        .iniciarCorreccion()
                                },
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                colors =
                                    ButtonDefaults
                                        .buttonColors(
                                            containerColor =
                                                LineaDeCal.colores.superficieOscura,
                                            contentColor =
                                                Color.White
                                        )
                            ) {
                                Text(
                                    "Corregir asistencia",
                                    fontWeight =
                                        FontWeight.Bold
                                )
                            }
                        }
                    }

                    else -> {
                        if (
                            estado.modoCorreccion
                        ) {
                            item {
                                MensajeAsistencia(
                                    texto =
                                        "Modo corrección: modifica únicamente los jugadores necesarios.",
                                    fondo =
                                        AmarilloClaro,
                                    color =
                                        AmarilloSportPro
                                )
                            }
                        }

                        item {
                            Text(
                                "Registro rápido",
                                color =
                                    AzulSportPro,
                                fontWeight =
                                    FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        item {
                            Button(
                                onClick = {
                                    viewModel
                                        .marcarTodosPresentes()
                                },
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                colors =
                                    ButtonDefaults
                                        .buttonColors(
                                            containerColor =
                                                VerdeSportPro,
                                            contentColor =
                                                Color.White
                                        )
                            ) {
                                Text(
                                    "Marcar todos presentes",
                                    fontWeight =
                                        FontWeight.Bold
                                )
                            }
                        }

                        item {
                            Button(
                                onClick = {
                                    viewModel
                                        .marcarTodosAusentes()
                                },
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                colors =
                                    ButtonDefaults
                                        .buttonColors(
                                            containerColor =
                                                RojoSportPro,
                                            contentColor =
                                                Color.White
                                        )
                            ) {
                                Text(
                                    "Marcar todos ausentes",
                                    fontWeight =
                                        FontWeight.Bold
                                )
                            }
                        }

                        item {
                            Text(
                                "Jugadores",
                                color =
                                    AzulSportPro,
                                fontSize = 15.sp,
                                fontWeight =
                                    FontWeight.Bold
                            )
                        }

                        items(
                            items =
                                estado.jugadores,
                            key = {
                                it.id
                            }
                        ) {
                                jugador ->

                            TarjetaJugadorEditable(
                                jugador =
                                    jugador,
                                estado =
                                    estado.estados[
                                        jugador.id
                                    ],
                                onCambiar = {
                                        nuevoEstado ->

                                    viewModel
                                        .cambiarEstado(
                                            jugador.id,
                                            nuevoEstado
                                        )
                                }
                            )
                        }

                        item {
                            Button(
                                onClick = {
                                    viewModel
                                        .guardar()
                                },
                                enabled =
                                    !estado.guardando,
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(52.dp),
                                colors =
                                    ButtonDefaults
                                        .buttonColors(
                                            containerColor =
                                                LineaDeCal.colores.superficieOscura,
                                            contentColor =
                                                Color.White
                                        )
                            ) {
                                Text(
                                    when {
                                        estado.guardando ->
                                            "Guardando..."

                                        estado.modoCorreccion ->
                                            "Guardar corrección"

                                        else ->
                                            "Guardar asistencia"
                                    },
                                    fontWeight =
                                        FontWeight.Bold
                                )
                            }
                        }

                        if (
                            estado.modoCorreccion
                        ) {
                            item {
                                OutlinedButton(
                                    onClick = {
                                        viewModel
                                            .cancelarCorreccion()
                                    },
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .height(48.dp),
                                    colors =
                                        ButtonDefaults
                                            .outlinedButtonColors(
                                                contentColor =
                                                    RojoSportPro
                                            )
                                ) {
                                    Text(
                                        "Cancelar corrección"
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                item {
                    Box(
                        modifier =
                            Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = {
                                menuJugador =
                                    true
                            },
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                            colors =
                                ButtonDefaults
                                    .outlinedButtonColors(
                                        contentColor =
                                            AzulSportPro
                                    )
                        ) {
                            val jugador =
                                estado
                                    .jugadoresConsulta
                                    .firstOrNull {
                                        it.id ==
                                                estado
                                                    .idJugadorConsulta
                                    }

                            Text(
                                jugador
                                    ?.nombreCompleto()
                                    ?: "Seleccionar jugador"
                            )
                        }

                        DropdownMenu(
                            expanded =
                                menuJugador,
                            onDismissRequest = {
                                menuJugador =
                                    false
                            }
                        ) {
                            estado.jugadoresConsulta
                                .forEach {
                                        jugador ->

                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                jugador
                                                    .nombreCompleto(),
                                                color =
                                                    AzulSportPro
                                            )
                                        },
                                        onClick = {
                                            viewModel
                                                .seleccionarJugadorConsulta(
                                                    jugador.id
                                                )

                                            menuJugador =
                                                false
                                        }
                                    )
                                }
                        }
                    }
                }

                estado.error?.let {
                        error ->

                    item {
                        MensajeAsistencia(
                            texto =
                                error,
                            fondo =
                                RojoClaro,
                            color =
                                RojoSportPro
                        )
                    }
                }

                if (
                    estado.cargando
                ) {
                    item {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(140.dp),
                            contentAlignment =
                                Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color =
                                    VerdeSportPro
                            )
                        }
                    }
                } else if (
                    estado.historial
                        .isEmpty()
                ) {
                    item {
                        TextoVacio(
                            "No hay registros de asistencia."
                        )
                    }
                } else {
                    items(
                        items =
                            estado.historial,
                        key = {
                            it.id
                        }
                    ) {
                            asistencia ->

                        TarjetaHistorial(
                            asistencia =
                                asistencia
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaJugadorEditable(
    jugador: Jugador,
    estado: EstadoAsistencia?,
    onCambiar:
        (EstadoAsistencia) -> Unit
) {
    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                14.dp
            ),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    LineaDeCal.colores.superficie,
                contentColor =
                    AzulSportPro
            )
    ) {
        Column(
            modifier =
                Modifier.padding(
                    12.dp
                )
        ) {
            Text(
                jugador.nombreCompleto(),
                color =
                    AzulSportPro,
                fontWeight =
                    FontWeight.Bold,
                fontSize = 15.sp
            )

            if (
                jugador.categoria
                    .isNotBlank()
            ) {
                Text(
                    jugador.categoria,
                    color =
                        LineaDeCal.colores.textoSecundario /* antes 0xFF5B6266 */,
                    fontSize = 12.sp
                )
            }

            Spacer(
                Modifier.height(
                    8.dp
                )
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {
                BotonEstado(
                    texto =
                        "Presente",
                    seleccionado =
                        estado ==
                                EstadoAsistencia.PRESENTE,
                    colorSeleccionado =
                        VerdeSportPro,
                    ancho =
                        96.dp,
                    onClick = {
                        onCambiar(
                            EstadoAsistencia.PRESENTE
                        )
                    }
                )

                BotonEstado(
                    texto =
                        "Ausente",
                    seleccionado =
                        estado ==
                                EstadoAsistencia.AUSENTE,
                    colorSeleccionado =
                        RojoSportPro,
                    ancho =
                        96.dp,
                    onClick = {
                        onCambiar(
                            EstadoAsistencia.AUSENTE
                        )
                    }
                )

                BotonEstado(
                    texto =
                        "Justificado",
                    seleccionado =
                        estado ==
                                EstadoAsistencia.JUSTIFICADO,
                    colorSeleccionado =
                        AmarilloSportPro,
                    ancho =
                        112.dp,
                    onClick = {
                        onCambiar(
                            EstadoAsistencia.JUSTIFICADO
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun BotonEstado(
    texto: String,
    seleccionado: Boolean,
    colorSeleccionado: Color,
    ancho: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    // Línea de cal (6.7, img 06): ficha redonda P / A / J. TalkBack lee el estado completo.
    Box(
        modifier =
            Modifier
                .width(ancho)
                .height(46.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (seleccionado) colorSeleccionado else GrisClaro)
                .border(
                    1.dp,
                    if (seleccionado) colorSeleccionado else LineaDeCal.colores.borde,
                    CircleShape
                )
                .clickable(role = Role.RadioButton, onClick = onClick)
                .semantics {
                    contentDescription = texto
                    selected = seleccionado
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                texto.take(1).uppercase(),
                color = if (seleccionado) Color.White else AzulSportPro,
                fontFamily = FamiliaTitulos,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun TarjetaJugadorRegistrado(
    jugador: Jugador,
    estado: EstadoAsistencia?
) {
    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                14.dp
            ),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    LineaDeCal.colores.superficie
            )
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        12.dp
                    ),
            verticalAlignment =
                Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {
            Column(
                modifier =
                    Modifier.width(
                        190.dp
                    )
            ) {
                Text(
                    jugador.nombreCompleto(),
                    color =
                        AzulSportPro,
                    fontWeight =
                        FontWeight.Bold
                )

                if (
                    jugador.categoria
                        .isNotBlank()
                ) {
                    Text(
                        jugador.categoria,
                        color =
                            LineaDeCal.colores.textoSecundario /* antes 0xFF5B6266 */,
                        fontSize = 12.sp
                    )
                }
            }

            EtiquetaEstado(
                estado
            )
        }
    }
}

@Composable
private fun TarjetaResumen(
    cantidad: Int,
    texto: String,
    fondo: Color,
    color: Color
) {
    // Línea de cal (6.7): ContadorLed de presentes, ausentes y justificados
    ContadorLed(
        valor = cantidad.toString(),
        etiqueta = texto,
        modifier = Modifier.width(104.dp),
        color = when (fondo) {
            RojoClaro -> LineaDeCal.colores.naranja
            AmarilloClaro -> LineaDeCal.colores.amarillo
            else -> LineaDeCal.colores.lima
        }
    )
}

@Composable
private fun EtiquetaEstado(
    estado: EstadoAsistencia?
) {
    val texto =
        when (estado) {
            EstadoAsistencia.PRESENTE ->
                "Presente"

            EstadoAsistencia.AUSENTE ->
                "Ausente"

            EstadoAsistencia.JUSTIFICADO ->
                "Justificado"

            null ->
                "Sin registrar"
        }

    val fondo =
        when (estado) {
            EstadoAsistencia.PRESENTE ->
                VerdeClaro

            EstadoAsistencia.AUSENTE ->
                RojoClaro

            EstadoAsistencia.JUSTIFICADO ->
                AmarilloClaro

            null ->
                GrisClaro
        }

    val color =
        when (estado) {
            EstadoAsistencia.PRESENTE ->
                VerdeSportPro

            EstadoAsistencia.AUSENTE ->
                RojoSportPro

            EstadoAsistencia.JUSTIFICADO ->
                AmarilloSportPro

            null ->
                LineaDeCal.colores.textoSecundario /* antes 0xFF555555 */
        }

    // Línea de cal: ChipEstado con los estados reales
    ChipEstado(
        texto,
        when (estado) {
            EstadoAsistencia.PRESENTE -> TipoEstado.OK
            EstadoAsistencia.AUSENTE -> TipoEstado.ERROR
            EstadoAsistencia.JUSTIFICADO -> TipoEstado.AMARILLO
            null -> TipoEstado.APAGADO
        }
    )
}

@Composable
private fun TarjetaHistorial(
    asistencia: Asistencia
) {
    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                14.dp
            ),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    LineaDeCal.colores.superficie
            )
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        12.dp
                    ),
            verticalAlignment =
                Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {
            Column(
                modifier =
                    Modifier.width(
                        190.dp
                    )
            ) {
                Text(
                    asistencia.nombreEquipo,
                    color =
                        AzulSportPro,
                    fontWeight =
                        FontWeight.Bold
                )

                Text(
                    asistencia.fechaEntrenamiento,
                    color =
                        LineaDeCal.colores.textoSecundario /* antes 0xFF5B6266 */,
                    fontSize = 12.sp
                )
            }

            EtiquetaEstado(
                asistencia.estado
            )
        }
    }
}

@Composable
private fun MensajeAsistencia(
    texto: String,
    fondo: Color,
    color: Color
) {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        color =
            fondo,
        shape =
            RoundedCornerShape(
                10.dp
            )
    ) {
        Text(
            texto,
            modifier =
                Modifier.padding(
                    10.dp
                ),
            color =
                color,
            fontSize = 12.sp,
            fontWeight =
                FontWeight.Medium
        )
    }
}

@Composable
private fun TextoVacio(
    texto: String
) {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        color =
            LineaDeCal.colores.superficie,
        shape =
            RoundedCornerShape(
                12.dp
            )
    ) {
        Text(
            texto,
            modifier =
                Modifier.padding(
                    16.dp
                ),
            color =
                LineaDeCal.colores.textoSecundario /* antes 0xFF5B6266 */,
            fontSize = 13.sp
        )
    }
}