package com.example.projectsportpro2026_2.ui.mensualidades

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.example.projectsportpro2026_2.datos.modelo.EstadoMensualidad
import com.example.projectsportpro2026_2.datos.modelo.Jugador
import com.example.projectsportpro2026_2.datos.modelo.Mensualidad
import com.example.projectsportpro2026_2.datos.modelo.Rol
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val AzulSportPro =
    Color(0xFF0B3547)

private val VerdeSportPro =
    Color(0xFF18A668)

private val Fondo =
    Color(0xFFF8FAF9)

private val VerdeClaro =
    Color(0xFFE8F8E9)

private val AmarilloClaro =
    Color(0xFFFFF6DC)

private val RojoClaro =
    Color(0xFFFFE7E7)

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun PantallaMensualidades(
    onVolver: () -> Unit,
    viewModel: MensualidadesViewModel =
        viewModel()
) {

    val estado by
    viewModel.estado
        .collectAsState()

    var busqueda by remember {
        mutableStateOf("")
    }

    var periodoSeleccionado by remember {
        mutableStateOf("TODOS")
    }

    var estadoSeleccionado by remember {
        mutableStateOf<EstadoMensualidad?>(
            null
        )
    }

    var mostrarFormulario by remember {
        mutableStateOf(false)
    }

    var mensualidadEdicion by remember {
        mutableStateOf<Mensualidad?>(
            null
        )
    }

    var mensualidadDetalle by remember {
        mutableStateOf<Mensualidad?>(
            null
        )
    }

    LaunchedEffect(Unit) {
        viewModel.iniciar()
    }

    val periodos =
        remember(
            estado.mensualidades
        ) {

            val actuales =
                (0..2)
                    .map {
                        YearMonth
                            .now()
                            .minusMonths(
                                it.toLong()
                            )
                            .toString()
                    }

            (
                    actuales +
                            estado.mensualidades
                                .map {
                                    it.periodo
                                }
                    )
                .distinct()
                .sortedDescending()
        }

    val mensualidadesFiltradas =
        estado.mensualidades
            .filter { mensualidad ->

                val coincideBusqueda =
                    mensualidad
                        .nombreJugador
                        .contains(
                            busqueda,
                            ignoreCase = true
                        )

                val coincidePeriodo =
                    periodoSeleccionado ==
                            "TODOS" ||
                            mensualidad.periodo ==
                            periodoSeleccionado

                val coincideEstado =
                    estadoSeleccionado ==
                            null ||
                            mensualidad.estado ==
                            estadoSeleccionado

                coincideBusqueda &&
                        coincidePeriodo &&
                        coincideEstado
            }

    val paraContadores =
        estado.mensualidades
            .filter {
                periodoSeleccionado ==
                        "TODOS" ||
                        it.periodo ==
                        periodoSeleccionado
            }

    val pagadas =
        paraContadores.count {
            it.estado ==
                    EstadoMensualidad.PAGADO
        }

    val pendientes =
        paraContadores.count {
            it.estado ==
                    EstadoMensualidad.PENDIENTE
        }

    val vencidas =
        paraContadores.count {
            it.estado ==
                    EstadoMensualidad.VENCIDO
        }

    Scaffold(
        containerColor =
            Fondo
    ) { padding ->

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(
                        horizontal = 18.dp
                    )
        ) {

            Spacer(
                Modifier.height(
                    14.dp
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
                        "← Inicio",
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
                    fontWeight =
                        FontWeight.Bold,
                    color =
                        AzulSportPro
                )
            }

            Spacer(
                Modifier.height(
                    12.dp
                )
            )

            Text(
                text =
                    "CLUB DEPORTIVO",
                color =
                    VerdeSportPro,
                fontSize =
                    11.sp,
                fontWeight =
                    FontWeight.Bold
            )

            Text(
                text =
                    "Mensualidades",
                color =
                    AzulSportPro,
                fontSize =
                    30.sp,
                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                Modifier.height(
                    16.dp
                )
            )

            OutlinedTextField(
                value =
                    busqueda,
                onValueChange = {
                    busqueda = it
                },
                modifier =
                    Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        "Buscar jugador..."
                    )
                },
                singleLine =
                    true,
                shape =
                    RoundedCornerShape(
                        14.dp
                    )
            )

            Spacer(
                Modifier.height(
                    14.dp
                )
            )

            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(
                        8.dp
                    )
            ) {

                FilterChip(
                    selected =
                        periodoSeleccionado ==
                                "TODOS",
                    onClick = {
                        periodoSeleccionado =
                            "TODOS"
                    },
                    label = {
                        Text(
                            "Todos"
                        )
                    }
                )

                periodos
                    .take(2)
                    .forEach { periodo ->

                        FilterChip(
                            selected =
                                periodoSeleccionado ==
                                        periodo,
                            onClick = {
                                periodoSeleccionado =
                                    periodo
                            },
                            label = {
                                Text(
                                    textoPeriodo(
                                        periodo
                                    )
                                )
                            }
                        )
                    }
            }

            Spacer(
                Modifier.height(
                    8.dp
                )
            )

            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(
                        6.dp
                    )
            ) {

                FilterChip(
                    selected =
                        estadoSeleccionado ==
                                null,
                    onClick = {
                        estadoSeleccionado =
                            null
                    },
                    label = {
                        Text(
                            "Todos"
                        )
                    }
                )

                FilterChip(
                    selected =
                        estadoSeleccionado ==
                                EstadoMensualidad.PAGADO,
                    onClick = {
                        estadoSeleccionado =
                            EstadoMensualidad.PAGADO
                    },
                    label = {
                        Text(
                            "Pagado"
                        )
                    }
                )

                FilterChip(
                    selected =
                        estadoSeleccionado ==
                                EstadoMensualidad.PENDIENTE,
                    onClick = {
                        estadoSeleccionado =
                            EstadoMensualidad.PENDIENTE
                    },
                    label = {
                        Text(
                            "Pendiente"
                        )
                    }
                )

                FilterChip(
                    selected =
                        estadoSeleccionado ==
                                EstadoMensualidad.VENCIDO,
                    onClick = {
                        estadoSeleccionado =
                            EstadoMensualidad.VENCIDO
                    },
                    label = {
                        Text(
                            "Vencido"
                        )
                    }
                )
            }

            Spacer(
                Modifier.height(
                    14.dp
                )
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(
                        10.dp
                    )
            ) {

                TarjetaContador(
                    numero =
                        pagadas,
                    texto =
                        "Pagadas",
                    fondo =
                        VerdeClaro,
                    modifier =
                        Modifier.weight(1f)
                )

                TarjetaContador(
                    numero =
                        pendientes,
                    texto =
                        "Pendientes",
                    fondo =
                        AmarilloClaro,
                    modifier =
                        Modifier.weight(1f)
                )

                TarjetaContador(
                    numero =
                        vencidas,
                    texto =
                        "Vencidas",
                    fondo =
                        RojoClaro,
                    modifier =
                        Modifier.weight(1f)
                )
            }

            Spacer(
                Modifier.height(
                    14.dp
                )
            )

            if (
                estado.rol ==
                Rol.ADM
            ) {

                Button(
                    onClick = {
                        mensualidadEdicion =
                            null

                        viewModel
                            .limpiarError()

                        mostrarFormulario =
                            true
                    },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(
                                48.dp
                            ),
                    colors =
                        ButtonDefaults
                            .buttonColors(
                                containerColor =
                                    VerdeSportPro
                            ),
                    shape =
                        RoundedCornerShape(
                            14.dp
                        )
                ) {
                    Text(
                        "+ Registrar mensualidad",
                        fontWeight =
                            FontWeight.Bold
                    )
                }

            } else {

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
                            "Solo consulta · Solo el administrador autorizado puede modificar mensualidades.",
                        modifier =
                            Modifier.padding(
                                12.dp
                            ),
                        fontSize =
                            12.sp,
                        color =
                            Color(
                                0xFF715B21
                            )
                    )
                }
            }

            Spacer(
                Modifier.height(
                    12.dp
                )
            )

            estado.error?.let {

                Text(
                    text = it,
                    color =
                        MaterialTheme
                            .colorScheme
                            .error,
                    fontSize =
                        13.sp
                )

                Spacer(
                    Modifier.height(
                        8.dp
                    )
                )
            }

            estado.mensaje?.let {

                Text(
                    text = it,
                    color =
                        VerdeSportPro,
                    fontSize =
                        13.sp
                )

                Spacer(
                    Modifier.height(
                        8.dp
                    )
                )
            }

            when {

                estado.cargando &&
                        estado.mensualidades
                            .isEmpty() -> {

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

                mensualidadesFiltradas
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
                                "No hay mensualidades para mostrar.",
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
                                    24.dp
                            )
                    ) {

                        items(
                            items =
                                mensualidadesFiltradas,
                            key = {
                                it.id
                            }
                        ) { mensualidad ->

                            TarjetaMensualidad(
                                mensualidad =
                                    mensualidad,
                                onClick = {
                                    mensualidadDetalle =
                                        mensualidad
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (
        mostrarFormulario
    ) {

        FormularioMensualidad(
            jugadores =
                estado
                    .jugadoresAdministrables,
            inicial =
                mensualidadEdicion,
            guardando =
                estado.guardando,
            error =
                estado.error,
            onCancelar = {
                mostrarFormulario =
                    false

                viewModel
                    .limpiarError()
            },
            onGuardar = {
                    jugador,
                    periodo,
                    estadoMensualidad ->

                viewModel
                    .guardarMensualidad(
                        idJugador =
                            jugador.id,
                        periodo =
                            periodo,
                        estadoMensualidad =
                            estadoMensualidad
                    )

                mostrarFormulario =
                    false
            }
        )
    }

    mensualidadDetalle?.let { mensualidad ->

        ModalBottomSheet(
            onDismissRequest = {
                mensualidadDetalle =
                    null
            }
        ) {

            DetalleMensualidad(
                mensualidad =
                    mensualidad,
                esAdministrador =
                    estado.rol ==
                            Rol.ADM,
                onEditar = {
                    mensualidadEdicion =
                        mensualidad

                    mensualidadDetalle =
                        null

                    mostrarFormulario =
                        true
                }
            )
        }
    }
}

@Composable
private fun TarjetaContador(
    numero: Int,
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
                    numero.toString(),
                color =
                    AzulSportPro,
                fontSize =
                    23.sp,
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
private fun TarjetaMensualidad(
    mensualidad: Mensualidad,
    onClick: () -> Unit
) {

    Card(
        onClick =
            onClick,
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                16.dp
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
                        16.dp
                    ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Surface(
                color =
                    colorEstadoClaro(
                        mensualidad.estado
                    ),
                shape =
                    RoundedCornerShape(
                        100.dp
                    ),
                modifier =
                    Modifier.size(
                        12.dp
                    )
            ) {}

            Spacer(
                Modifier.width(
                    12.dp
                )
            )

            Column(
                modifier =
                    Modifier.weight(
                        1f
                    )
            ) {

                Text(
                    text =
                        mensualidad
                            .nombreJugador,
                    color =
                        AzulSportPro,
                    fontWeight =
                        FontWeight.Bold,
                    fontSize =
                        15.sp
                )

                Text(
                    text =
                        "${mensualidad.nombreAcademia} · ${textoPeriodo(mensualidad.periodo)}",
                    color =
                        Color.Gray,
                    fontSize =
                        12.sp
                )
            }

            EtiquetaEstado(
                estado =
                    mensualidad.estado
            )
        }
    }
}

@Composable
private fun EtiquetaEstado(
    estado: EstadoMensualidad
) {

    Surface(
        color =
            colorEstadoClaro(
                estado
            ),
        shape =
            RoundedCornerShape(
                50.dp
            )
    ) {

        Text(
            text =
                textoEstado(
                    estado
                ),
            modifier =
                Modifier.padding(
                    horizontal =
                        10.dp,
                    vertical =
                        5.dp
                ),
            fontSize =
                11.sp,
            fontWeight =
                FontWeight.Bold,
            color =
                colorEstadoOscuro(
                    estado
                )
        )
    }
}

@Composable
private fun DetalleMensualidad(
    mensualidad: Mensualidad,
    esAdministrador: Boolean,
    onEditar: () -> Unit
) {

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal =
                        22.dp
                )
                .padding(
                    bottom =
                        34.dp
                )
    ) {

        Text(
            text =
                "MENSUALIDAD · ${textoPeriodo(mensualidad.periodo)}",
            fontSize =
                11.sp,
            color =
                VerdeSportPro,
            fontWeight =
                FontWeight.Bold
        )

        Spacer(
            Modifier.height(
                5.dp
            )
        )

        Text(
            text =
                mensualidad
                    .nombreJugador,
            fontSize =
                26.sp,
            fontWeight =
                FontWeight.Bold,
            color =
                AzulSportPro
        )

        Spacer(
            Modifier.height(
                4.dp
            )
        )

        Text(
            text =
                mensualidad
                    .nombreAcademia,
            color =
                Color.Gray
        )

        Spacer(
            Modifier.height(
                18.dp
            )
        )

        Surface(
            color =
                Color(
                    0xFFF5F7F6
                ),
            shape =
                RoundedCornerShape(
                    15.dp
                ),
            modifier =
                Modifier.fillMaxWidth()
        ) {

            Column(
                modifier =
                    Modifier.padding(
                        16.dp
                    ),
                verticalArrangement =
                    Arrangement.spacedBy(
                        10.dp
                    )
            ) {

                FilaDetalle(
                    "Academia",
                    mensualidad.nombreAcademia
                )

                FilaDetalle(
                    "Periodo",
                    textoPeriodo(
                        mensualidad.periodo
                    )
                )

                FilaDetalle(
                    "Última actualización",
                    textoFecha(
                        mensualidad.fechaActualizacion
                    )
                )

                FilaDetalle(
                    "Estado actual",
                    textoEstado(
                        mensualidad.estado
                    )
                )
            }
        }

        Spacer(
            Modifier.height(
                16.dp
            )
        )

        if (
            esAdministrador
        ) {

            Button(
                onClick =
                    onEditar,
                modifier =
                    Modifier.fillMaxWidth(),
                colors =
                    ButtonDefaults
                        .buttonColors(
                            containerColor =
                                VerdeSportPro
                        )
            ) {
                Text(
                    "Modificar estado"
                )
            }

        } else {

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
                        "Solo consulta. Solo el administrador autorizado puede modificar el estado de la mensualidad.",
                    modifier =
                        Modifier.padding(
                            14.dp
                        ),
                    fontSize =
                        12.sp,
                    color =
                        Color(
                            0xFF715B21
                        )
                )
            }
        }
    }
}

@Composable
private fun FilaDetalle(
    titulo: String,
    valor: String
) {

    Row(
        modifier =
            Modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.SpaceBetween
    ) {

        Text(
            text =
                titulo,
            color =
                Color.Gray,
            fontSize =
                12.sp
        )

        Text(
            text =
                valor,
            color =
                AzulSportPro,
            fontWeight =
                FontWeight.SemiBold,
            fontSize =
                12.sp
        )
    }
}

@Composable
private fun FormularioMensualidad(
    jugadores: List<Jugador>,
    inicial: Mensualidad?,
    guardando: Boolean,
    error: String?,
    onCancelar: () -> Unit,
    onGuardar: (
        Jugador,
        String,
        EstadoMensualidad
    ) -> Unit
) {

    var idJugador by remember(
        inicial
    ) {
        mutableStateOf(
            inicial
                ?.idJugador
                .orEmpty()
        )
    }

    var periodo by remember(
        inicial
    ) {
        mutableStateOf(
            inicial
                ?.periodo
                ?: YearMonth
                    .now()
                    .toString()
        )
    }

    var estado by remember(
        inicial
    ) {
        mutableStateOf(
            inicial
                ?.estado
                ?: EstadoMensualidad.PENDIENTE
        )
    }

    var menuJugador by remember {
        mutableStateOf(false)
    }

    var menuPeriodo by remember {
        mutableStateOf(false)
    }

    val jugadorSeleccionado =
        jugadores
            .firstOrNull {
                it.id ==
                        idJugador
            }

    val periodos =
        remember {
            (0..11)
                .map {
                    YearMonth
                        .now()
                        .minusMonths(
                            it.toLong()
                        )
                        .toString()
                }
        }

    AlertDialog(
        onDismissRequest =
            onCancelar,
        title = {
            Text(
                if (
                    inicial ==
                    null
                ) {
                    "Registrar mensualidad"
                } else {
                    "Actualizar mensualidad"
                }
            )
        },
        text = {

            Column(
                verticalArrangement =
                    Arrangement.spacedBy(
                        12.dp
                    )
            ) {

                if (
                    inicial ==
                    null
                ) {

                    Text(
                        "Jugador *",
                        fontWeight =
                            FontWeight.SemiBold
                    )

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
                            Text(
                                jugadorSeleccionado
                                    ?.nombreCompleto()
                                    ?: if (
                                        jugadores.isEmpty()
                                    ) {
                                        "No hay jugadores disponibles"
                                    } else {
                                        "Seleccionar jugador"
                                    }
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
                                jugadores.isEmpty()
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

                                jugadores
                                    .forEach { jugador ->

                                        DropdownMenuItem(
                                            text = {
                                                Column {

                                                    Text(
                                                        text =
                                                            jugador.nombreCompleto(),
                                                        fontWeight =
                                                            FontWeight.SemiBold
                                                    )

                                                    if (
                                                        jugador.categoria
                                                            .isNotBlank()
                                                    ) {

                                                        Text(
                                                            text =
                                                                jugador.categoria,
                                                            fontSize =
                                                                11.sp,
                                                            color =
                                                                Color.Gray
                                                        )
                                                    }
                                                }
                                            },
                                            onClick = {
                                                idJugador =
                                                    jugador.id

                                                menuJugador =
                                                    false
                                            }
                                        )
                                    }
                            }
                        }
                    }

                    Text(
                        "Periodo *",
                        fontWeight =
                            FontWeight.SemiBold
                    )

                    Box(
                        modifier =
                            Modifier.fillMaxWidth()
                    ) {

                        OutlinedButton(
                            onClick = {
                                menuPeriodo =
                                    true
                            },
                            modifier =
                                Modifier.fillMaxWidth()
                        ) {
                            Text(
                                textoPeriodo(
                                    periodo
                                )
                            )
                        }

                        DropdownMenu(
                            expanded =
                                menuPeriodo,
                            onDismissRequest = {
                                menuPeriodo =
                                    false
                            }
                        ) {

                            periodos
                                .forEach { opcion ->

                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                textoPeriodo(
                                                    opcion
                                                )
                                            )
                                        },
                                        onClick = {
                                            periodo =
                                                opcion

                                            menuPeriodo =
                                                false
                                        }
                                    )
                                }
                        }
                    }

                } else {

                    Text(
                        text =
                            inicial
                                .nombreJugador,
                        fontWeight =
                            FontWeight.Bold,
                        color =
                            AzulSportPro
                    )

                    Text(
                        text =
                            "Periodo: ${textoPeriodo(periodo)}",
                        color =
                            Color.Gray
                    )
                }

                Text(
                    "Estado *",
                    fontWeight =
                        FontWeight.SemiBold
                )

                EstadoMensualidad
                    .entries
                    .forEach { opcion ->

                        FilterChip(
                            selected =
                                estado ==
                                        opcion,
                            onClick = {
                                estado =
                                    opcion
                            },
                            label = {
                                Text(
                                    textoEstado(
                                        opcion
                                    )
                                )
                            }
                        )
                    }

                error?.let {

                    Text(
                        text =
                            it,
                        color =
                            MaterialTheme
                                .colorScheme
                                .error,
                        fontSize =
                            12.sp
                    )
                }
            }
        },
        confirmButton = {

            Button(
                enabled =
                    !guardando &&
                            (
                                    inicial !=
                                            null ||
                                            jugadorSeleccionado !=
                                            null
                                    ),
                onClick = {

                    val jugador =
                        jugadorSeleccionado
                            ?: jugadores
                                .firstOrNull {
                                    it.id ==
                                            inicial
                                                ?.idJugador
                                }

                    if (
                        jugador !=
                        null
                    ) {
                        onGuardar(
                            jugador,
                            periodo,
                            estado
                        )
                    }
                },
                colors =
                    ButtonDefaults
                        .buttonColors(
                            containerColor =
                                VerdeSportPro
                        )
            ) {
                Text(
                    if (
                        guardando
                    ) {
                        "Guardando..."
                    } else {
                        "Guardar"
                    }
                )
            }
        },
        dismissButton = {

            TextButton(
                onClick =
                    onCancelar
            ) {
                Text(
                    "Cancelar"
                )
            }
        }
    )
}

private fun textoEstado(
    estado: EstadoMensualidad
): String {

    return when (
        estado
    ) {

        EstadoMensualidad.PAGADO ->
            "Pagado"

        EstadoMensualidad.PENDIENTE ->
            "Pendiente"

        EstadoMensualidad.VENCIDO ->
            "Vencido"
    }
}

private fun colorEstadoClaro(
    estado: EstadoMensualidad
): Color {

    return when (
        estado
    ) {

        EstadoMensualidad.PAGADO ->
            VerdeClaro

        EstadoMensualidad.PENDIENTE ->
            AmarilloClaro

        EstadoMensualidad.VENCIDO ->
            RojoClaro
    }
}

private fun colorEstadoOscuro(
    estado: EstadoMensualidad
): Color {

    return when (
        estado
    ) {

        EstadoMensualidad.PAGADO ->
            Color(
                0xFF278A48
            )

        EstadoMensualidad.PENDIENTE ->
            Color(
                0xFF8A7024
            )

        EstadoMensualidad.VENCIDO ->
            Color(
                0xFFC43D3D
            )
    }
}

private fun textoPeriodo(
    periodo: String
): String {

    return try {

        val fecha =
            YearMonth.parse(
                periodo
            )

        val formato =
            DateTimeFormatter
                .ofPattern(
                    "MMM yyyy",
                    Locale(
                        "es",
                        "PE"
                    )
                )

        fecha
            .format(
                formato
            )
            .replaceFirstChar {
                it.uppercase()
            }

    } catch (e: Exception) {
        periodo
    }
}

private fun textoFecha(
    milisegundos: Long
): String {

    if (
        milisegundos <=
        0
    ) {
        return "-"
    }

    return try {

        Instant
            .ofEpochMilli(
                milisegundos
            )
            .atZone(
                ZoneId
                    .systemDefault()
            )
            .format(
                DateTimeFormatter
                    .ofPattern(
                        "dd MMM yyyy",
                        Locale(
                            "es",
                            "PE"
                        )
                    )
            )

    } catch (e: Exception) {
        "-"
    }
}