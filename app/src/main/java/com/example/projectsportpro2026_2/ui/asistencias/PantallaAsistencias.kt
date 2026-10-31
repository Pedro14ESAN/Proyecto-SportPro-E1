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
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.FilterChip
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
    Color(0xFF18A668)

private val VerdeClaro =
    Color(0xFFE8F8E9)

private val RojoClaro =
    Color(0xFFFFE7E7)

private val AmarilloClaro =
    Color(0xFFFFF5D6)

private val FondoSportPro =
    Color(0xFFF8FAF9)

@Composable
fun PantallaAsistencias(
    onVolver: () -> Unit,
    viewModel:
    AsistenciasViewModel =
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

    val presentes =
        estado.estados.values.count {
            it ==
                    EstadoAsistencia.PRESENTE
        }

    val ausentes =
        estado.estados.values.count {
            it ==
                    EstadoAsistencia.AUSENTE
        }

    val justificados =
        estado.estados.values.count {
            it ==
                    EstadoAsistencia.JUSTIFICADO
        }

    Scaffold(
        containerColor =
            FondoSportPro
    ) { padding ->

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(
                        horizontal =
                            18.dp
                    )
        ) {

            Spacer(
                Modifier.height(
                    10.dp
                )
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                TextButton(
                    onClick =
                        onVolver
                ) {

                    Text(
                        "← Entrenamientos",
                        color =
                            VerdeSportPro
                    )
                }

                Spacer(
                    Modifier.weight(1f)
                )

                Text(
                    text =
                        "SPORTPRO",
                    color =
                        AzulSportPro,
                    fontWeight =
                        FontWeight.Bold
                )
            }

            Spacer(
                Modifier.height(
                    12.dp
                )
            )

            Text(
                text =
                    "ASISTENCIA",
                color =
                    VerdeSportPro,
                fontSize =
                    11.sp,
                fontWeight =
                    FontWeight.Bold
            )

            Text(
                text =
                    if (
                        estado.rol ==
                        Rol.DT ||
                        estado.rol ==
                        Rol.ADM
                    ) {
                        "Asistencia"
                    } else {
                        "Historial de asistencia"
                    },
                color =
                    AzulSportPro,
                fontSize =
                    28.sp,
                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                Modifier.height(
                    16.dp
                )
            )

            if (
                estado.rol ==
                Rol.DT ||
                estado.rol ==
                Rol.ADM
            ) {

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
                            Modifier.fillMaxWidth()
                    ) {

                        val sesion =
                            estado.sesiones
                                .firstOrNull {
                                    it.id ==
                                            estado.idSesionSeleccionada
                                }

                        Text(
                            text =
                                sesion
                                    ?.let {
                                        "${it.nombreEquipo} · ${it.fecha} ${it.hora}"
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
                                                "${sesion.nombreEquipo} · ${sesion.fecha} ${sesion.hora}"
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

                Spacer(
                    Modifier.height(
                        14.dp
                    )
                )

                estado.error?.let {

                    Text(
                        text =
                            it,
                        color =
                            Color.Red,
                        fontSize =
                            12.sp
                    )

                    Spacer(
                        Modifier.height(
                            8.dp
                        )
                    )
                }

                estado.mensaje?.let {

                    Text(
                        text =
                            it,
                        color =
                            VerdeSportPro,
                        fontSize =
                            12.sp
                    )

                    Spacer(
                        Modifier.height(
                            8.dp
                        )
                    )
                }

                when {

                    estado.cargando -> {

                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                            contentAlignment =
                                Alignment.Center
                        ) {

                            CircularProgressIndicator(
                                color =
                                    VerdeSportPro
                            )
                        }
                    }

                    estado.idSesionSeleccionada
                        .isBlank() -> {

                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                            contentAlignment =
                                Alignment.Center
                        ) {

                            Text(
                                text =
                                    "Selecciona un entrenamiento.",
                                color =
                                    Color.Gray
                            )
                        }
                    }

                    estado.jugadores
                        .isEmpty() -> {

                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                            contentAlignment =
                                Alignment.Center
                        ) {

                            Text(
                                text =
                                    "No hay jugadores en este equipo.",
                                color =
                                    Color.Gray
                            )
                        }
                    }

                    estado.asistenciaRegistrada &&
                            !estado.modoCorreccion -> {

                        Text(
                            text =
                                "ASISTENCIA REGISTRADA",
                            color =
                                VerdeSportPro,
                            fontSize =
                                11.sp,
                            fontWeight =
                                FontWeight.Bold
                        )

                        Spacer(
                            Modifier.height(
                                10.dp
                            )
                        )

                        Row(
                            modifier =
                                Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.spacedBy(
                                    8.dp
                                )
                        ) {

                            TarjetaResumenAsistencia(
                                cantidad =
                                    presentes,
                                texto =
                                    "Presente",
                                fondo =
                                    VerdeClaro,
                                modifier =
                                    Modifier.weight(
                                        1f
                                    )
                            )

                            TarjetaResumenAsistencia(
                                cantidad =
                                    ausentes,
                                texto =
                                    "Ausente",
                                fondo =
                                    RojoClaro,
                                modifier =
                                    Modifier.weight(
                                        1f
                                    )
                            )

                            TarjetaResumenAsistencia(
                                cantidad =
                                    justificados,
                                texto =
                                    "Justificado",
                                fondo =
                                    AmarilloClaro,
                                modifier =
                                    Modifier.weight(
                                        1f
                                    )
                            )
                        }

                        Spacer(
                            Modifier.height(
                                18.dp
                            )
                        )

                        Text(
                            text =
                                "Jugadores",
                            color =
                                AzulSportPro,
                            fontWeight =
                                FontWeight.Bold,
                            fontSize =
                                16.sp
                        )

                        Spacer(
                            Modifier.height(
                                8.dp
                            )
                        )

                        LazyColumn(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                            verticalArrangement =
                                Arrangement.spacedBy(
                                    8.dp
                                ),
                            contentPadding =
                                PaddingValues(
                                    bottom =
                                        16.dp
                                )
                        ) {

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
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel
                                    .iniciarCorreccion()
                            },
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(
                                        50.dp
                                    )
                        ) {

                            Text(
                                text =
                                    "Corregir asistencia",
                                fontWeight =
                                    FontWeight.Bold
                            )
                        }

                        Spacer(
                            Modifier.height(
                                12.dp
                            )
                        )
                    }

                    else -> {

                        if (
                            estado.modoCorreccion
                        ) {

                            Surface(
                                modifier =
                                    Modifier.fillMaxWidth(),
                                color =
                                    AmarilloClaro,
                                shape =
                                    RoundedCornerShape(
                                        12.dp
                                    )
                            ) {

                                Text(
                                    text =
                                        "Modo corrección: modifica únicamente los estados necesarios y guarda los cambios.",
                                    modifier =
                                        Modifier.padding(
                                            12.dp
                                        ),
                                    color =
                                        Color(
                                            0xFF775D15
                                        ),
                                    fontSize =
                                        12.sp
                                )
                            }

                            Spacer(
                                Modifier.height(
                                    12.dp
                                )
                            )
                        }

                        LazyColumn(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                            verticalArrangement =
                                Arrangement.spacedBy(
                                    10.dp
                                ),
                            contentPadding =
                                PaddingValues(
                                    bottom =
                                        16.dp
                                )
                        ) {

                            items(
                                items =
                                    estado.jugadores,
                                key = {
                                    it.id
                                }
                            ) {
                                    jugador ->

                                FilaJugadorAsistencia(
                                    jugador =
                                        jugador,
                                    estado =
                                        estado.estados[
                                            jugador.id
                                        ],
                                    onCambiar = {
                                            nuevo ->

                                        viewModel
                                            .cambiarEstado(
                                                jugador.id,
                                                nuevo
                                            )
                                    }
                                )
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.guardar()
                            },
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(
                                        50.dp
                                    ),
                            enabled =
                                !estado.guardando,
                            colors =
                                ButtonDefaults
                                    .buttonColors(
                                        containerColor =
                                            AzulSportPro
                                    )
                        ) {

                            Text(
                                text =
                                    when {

                                        estado.guardando ->
                                            "Guardando..."

                                        estado.modoCorreccion ->
                                            "Guardar corrección"

                                        else ->
                                            "Guardar asistencia"
                                    }
                            )
                        }

                        if (
                            estado.modoCorreccion
                        ) {

                            Spacer(
                                Modifier.height(
                                    6.dp
                                )
                            )

                            TextButton(
                                onClick = {
                                    viewModel
                                        .cancelarCorreccion()
                                },
                                modifier =
                                    Modifier.fillMaxWidth()
                            ) {

                                Text(
                                    "Cancelar corrección"
                                )
                            }
                        }

                        Spacer(
                            Modifier.height(
                                12.dp
                            )
                        )
                    }
                }

            } else {

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
                            Modifier.fillMaxWidth()
                    ) {

                        val jugador =
                            estado
                                .jugadoresConsulta
                                .firstOrNull {
                                    it.id ==
                                            estado.idJugadorConsulta
                                }

                        Text(
                            text =
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

                        if (
                            estado
                                .jugadoresConsulta
                                .isEmpty()
                        ) {

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "No hay jugadores disponibles"
                                    )
                                },
                                enabled =
                                    false,
                                onClick = {}
                            )

                        } else {

                            estado
                                .jugadoresConsulta
                                .forEach {
                                        jugador ->

                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                jugador
                                                    .nombreCompleto()
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

                Spacer(
                    Modifier.height(
                        14.dp
                    )
                )

                estado.error?.let {

                    Text(
                        text =
                            it,
                        color =
                            Color.Red,
                        fontSize =
                            12.sp
                    )

                    Spacer(
                        Modifier.height(
                            8.dp
                        )
                    )
                }

                when {

                    estado.cargando -> {

                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                            contentAlignment =
                                Alignment.Center
                        ) {

                            CircularProgressIndicator(
                                color =
                                    VerdeSportPro
                            )
                        }
                    }

                    estado.historial
                        .isEmpty() -> {

                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                            contentAlignment =
                                Alignment.Center
                        ) {

                            Text(
                                text =
                                    "No hay registros de asistencia.",
                                color =
                                    Color.Gray
                            )
                        }
                    }

                    else -> {

                        LazyColumn(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                            verticalArrangement =
                                Arrangement.spacedBy(
                                    10.dp
                                ),
                            contentPadding =
                                PaddingValues(
                                    bottom =
                                        20.dp
                                )
                        ) {

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
    }
}

@Composable
private fun FilaJugadorAsistencia(
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
            CardDefaults
                .cardColors(
                    containerColor =
                        Color.White
                )
    ) {

        Column(
            modifier =
                Modifier.padding(
                    14.dp
                )
        ) {

            Text(
                text =
                    jugador.nombreCompleto(),
                fontWeight =
                    FontWeight.Bold,
                color =
                    AzulSportPro
            )

            if (
                jugador.categoria
                    .isNotBlank()
            ) {

                Text(
                    text =
                        jugador.categoria,
                    color =
                        Color.Gray,
                    fontSize =
                        11.sp
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
                    Arrangement.spacedBy(
                        8.dp
                    )
            ) {

                FilterChip(
                    selected =
                        estado ==
                                EstadoAsistencia.PRESENTE,
                    onClick = {
                        onCambiar(
                            EstadoAsistencia.PRESENTE
                        )
                    },
                    label = {
                        Text(
                            "P"
                        )
                    }
                )

                FilterChip(
                    selected =
                        estado ==
                                EstadoAsistencia.AUSENTE,
                    onClick = {
                        onCambiar(
                            EstadoAsistencia.AUSENTE
                        )
                    },
                    label = {
                        Text(
                            "A"
                        )
                    }
                )

                FilterChip(
                    selected =
                        estado ==
                                EstadoAsistencia.JUSTIFICADO,
                    onClick = {
                        onCambiar(
                            EstadoAsistencia.JUSTIFICADO
                        )
                    },
                    label = {
                        Text(
                            "J"
                        )
                    }
                )
            }
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
            CardDefaults
                .cardColors(
                    containerColor =
                        Color.White
                )
    ) {

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        14.dp
                    ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Column(
                modifier =
                    Modifier.weight(
                        1f
                    )
            ) {

                Text(
                    text =
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
                        text =
                            jugador.categoria,
                        color =
                            Color.Gray,
                        fontSize =
                            11.sp
                    )
                }
            }

            EtiquetaEstadoAsistencia(
                estado =
                    estado
            )
        }
    }
}

@Composable
private fun EtiquetaEstadoAsistencia(
    estado: EstadoAsistencia?
) {

    val texto =
        when (
            estado
        ) {

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
        when (
            estado
        ) {

            EstadoAsistencia.PRESENTE ->
                VerdeClaro

            EstadoAsistencia.AUSENTE ->
                RojoClaro

            EstadoAsistencia.JUSTIFICADO ->
                AmarilloClaro

            null ->
                Color(
                    0xFFEDEDED
                )
        }

    val color =
        when (
            estado
        ) {

            EstadoAsistencia.PRESENTE ->
                Color(
                    0xFF278A48
                )

            EstadoAsistencia.AUSENTE ->
                Color(
                    0xFFC43D3D
                )

            EstadoAsistencia.JUSTIFICADO ->
                Color(
                    0xFF9A741D
                )

            null ->
                Color.Gray
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
            text =
                texto,
            modifier =
                Modifier.padding(
                    horizontal =
                        10.dp,
                    vertical =
                        5.dp
                ),
            color =
                color,
            fontSize =
                11.sp,
            fontWeight =
                FontWeight.Bold
        )
    }
}

@Composable
private fun TarjetaResumenAsistencia(
    cantidad: Int,
    texto: String,
    fondo: Color,
    modifier: Modifier = Modifier
) {

    Surface(
        modifier =
            modifier,
        color =
            fondo,
        shape =
            RoundedCornerShape(
                14.dp
            )
    ) {

        Column(
            modifier =
                Modifier.padding(
                    14.dp
                ),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Text(
                text =
                    cantidad.toString(),
                color =
                    AzulSportPro,
                fontSize =
                    25.sp,
                fontWeight =
                    FontWeight.Bold
            )

            Text(
                text =
                    texto,
                fontSize =
                    11.sp,
                color =
                    Color.DarkGray
            )
        }
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
            CardDefaults
                .cardColors(
                    containerColor =
                        Color.White
                )
    ) {

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        14.dp
                    ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Column(
                modifier =
                    Modifier.weight(
                        1f
                    )
            ) {

                Text(
                    text =
                        asistencia.nombreEquipo,
                    color =
                        AzulSportPro,
                    fontWeight =
                        FontWeight.Bold
                )

                Text(
                    text =
                        asistencia.fechaEntrenamiento,
                    color =
                        Color.Gray,
                    fontSize =
                        12.sp
                )
            }

            EtiquetaEstadoAsistencia(
                estado =
                    asistencia.estado
            )
        }
    }
}