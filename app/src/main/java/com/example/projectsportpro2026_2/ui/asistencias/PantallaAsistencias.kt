package com.example.projectsportpro2026_2.ui.asistencias

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

private val AzulSportPro =
    Color(0xFF0B3547)

private val VerdeSportPro =
    Color(0xFF168F5B)

private val RojoSportPro =
    Color(0xFFB83232)

private val AmarilloSportPro =
    Color(0xFF8A6814)

private val VerdeClaro =
    Color(0xFFE6F5EC)

private val RojoClaro =
    Color(0xFFFFE9E9)

private val AmarilloClaro =
    Color(0xFFFFF4D6)

private val GrisClaro =
    Color(0xFFF0F2F3)

private val FondoSportPro =
    Color(0xFFF7F9F8)

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
            item {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                    verticalAlignment =
                        Alignment.CenterVertically,
                    horizontalArrangement =
                        Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick =
                            onVolver
                    ) {
                        Text(
                            "← Volver",
                            color =
                                AzulSportPro,
                            fontWeight =
                                FontWeight.SemiBold
                        )
                    }

                    Text(
                        "SPORTPRO",
                        color =
                            AzulSportPro,
                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }

            item {
                Column {
                    Text(
                        "ASISTENCIA",
                        color =
                            VerdeSportPro,
                        fontSize = 11.sp,
                        fontWeight =
                            FontWeight.Bold
                    )

                    Text(
                        if (
                            puedeGestionar
                        ) {
                            "Registro de asistencia"
                        } else {
                            "Historial de asistencia"
                        },
                        color =
                            AzulSportPro,
                        fontSize = 26.sp,
                        fontWeight =
                            FontWeight.Bold
                    )
                }
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
                                                AzulSportPro,
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
                                                AzulSportPro,
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
                    Color.White,
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
                        Color(0xFF5B6266),
                    fontSize = 11.sp
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
    Button(
        onClick =
            onClick,
        modifier =
            Modifier
                .width(ancho)
                .height(46.dp),
        shape =
            RoundedCornerShape(
                10.dp
            ),
        colors =
            ButtonDefaults.buttonColors(
                containerColor =
                    if (
                        seleccionado
                    ) {
                        colorSeleccionado
                    } else {
                        GrisClaro
                    },
                contentColor =
                    if (
                        seleccionado
                    ) {
                        Color.White
                    } else {
                        AzulSportPro
                    }
            ),
        contentPadding =
            PaddingValues(
                horizontal = 2.dp
            )
    ) {
        Text(
            texto,
            fontSize = 10.sp,
            maxLines = 1,
            fontWeight =
                FontWeight.SemiBold
        )
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
                    Color.White
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
                            Color(0xFF5B6266),
                        fontSize = 11.sp
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
    Surface(
        modifier =
            Modifier
                .width(102.dp)
                .height(72.dp),
        color =
            fondo,
        shape =
            RoundedCornerShape(
                12.dp
            )
    ) {
        Column(
            modifier =
                Modifier.padding(
                    vertical = 8.dp
                ),
            horizontalAlignment =
                Alignment.CenterHorizontally,
            verticalArrangement =
                Arrangement.Center
        ) {
            Text(
                cantidad.toString(),
                color =
                    color,
                fontSize = 21.sp,
                fontWeight =
                    FontWeight.Bold
            )

            Text(
                texto,
                color =
                    Color(0xFF454B4E),
                fontSize = 9.sp,
                maxLines = 1
            )
        }
    }
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
                Color(0xFF555555)
        }

    Surface(
        color =
            fondo,
        shape =
            RoundedCornerShape(
                50.dp
            )
    ) {
        Text(
            texto,
            modifier =
                Modifier.padding(
                    horizontal = 10.dp,
                    vertical = 5.dp
                ),
            color =
                color,
            fontSize = 11.sp,
            fontWeight =
                FontWeight.Bold
        )
    }
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
                    Color.White
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
                        Color(0xFF5B6266),
                    fontSize = 11.sp
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
            Color.White,
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
                Color(0xFF5B6266),
            fontSize = 13.sp
        )
    }
}