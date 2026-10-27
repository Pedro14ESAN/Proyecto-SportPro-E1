package com.example.projectsportpro2026_2.ui.entrenamientos

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectsportpro2026_2.datos.modelo.Ejercicio
import com.example.projectsportpro2026_2.datos.modelo.Entrenamiento
import com.example.projectsportpro2026_2.datos.modelo.EstadoEntrenamiento
import com.example.projectsportpro2026_2.datos.modelo.Rol
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val AzulSportPro =
    Color(0xFF0B3547)

private val VerdeSportPro =
    Color(0xFF18A668)

private val FondoSportPro =
    Color(0xFFF8FAF9)

private val RojoClaro =
    Color(0xFFFFE7E7)

private val AmarilloClaro =
    Color(0xFFFFF5D6)

@Composable
fun PantallaEntrenamientos(
    onVolver: () -> Unit,
    viewModel:
    EntrenamientosViewModel =
        viewModel()
) {

    val estado by
    viewModel.estado
        .collectAsState()

    var menuEquipo by
    remember {
        mutableStateOf(
            false
        )
    }

    var mostrarFormulario by
    remember {
        mutableStateOf(
            false
        )
    }

    var entrenamientoEditar by
    remember {
        mutableStateOf<Entrenamiento?>(
            null
        )
    }

    var entrenamientoCancelar by
    remember {
        mutableStateOf<Entrenamiento?>(
            null
        )
    }

    LaunchedEffect(Unit) {
        viewModel.iniciar()
    }

    val equipoSeleccionado =
        estado.equipos
            .firstOrNull {
                it.id ==
                        estado.idEquipoSeleccionado
            }

    val lista =
        if (
            estado.rol ==
            Rol.DT &&
            estado.idEquipoSeleccionado
                .isNotBlank()
        ) {

            estado.entrenamientos
                .filter {
                    it.idEquipo ==
                            estado.idEquipoSeleccionado
                }

        } else {

            estado.entrenamientos
        }

    Scaffold(
        containerColor =
            FondoSportPro
    ) {
            padding ->

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        padding
                    )
                    .padding(
                        horizontal =
                            18.dp
                    )
        ) {

            Spacer(
                Modifier.height(
                    12.dp
                )
            )

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth(),
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
                    Modifier.weight(
                        1f
                    )
                )

                Text(
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

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
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
                        "ENTRENAMIENTOS",
                        color =
                            VerdeSportPro,
                        fontSize =
                            11.sp,
                        fontWeight =
                            FontWeight.Bold
                    )

                    Text(
                        "Entrenamientos",
                        color =
                            AzulSportPro,
                        fontSize =
                            29.sp,
                        fontWeight =
                            FontWeight.Bold
                    )
                }

                if (
                    estado.rol ==
                    Rol.DT &&
                    estado.equipos
                        .isNotEmpty()
                ) {

                    Button(
                        onClick = {

                            entrenamientoEditar =
                                null

                            viewModel
                                .limpiarError()

                            mostrarFormulario =
                                true
                        },
                        contentPadding =
                            PaddingValues(
                                horizontal =
                                    16.dp
                            ),
                        colors =
                            ButtonDefaults
                                .buttonColors(
                                    containerColor =
                                        VerdeSportPro
                                )
                    ) {

                        Text(
                            "+ Nuevo"
                        )
                    }
                }
            }

            Spacer(
                Modifier.height(
                    14.dp
                )
            )

            if (
                estado.rol ==
                Rol.DT
            ) {

                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                ) {

                    OutlinedButton(
                        onClick = {
                            menuEquipo =
                                true
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                    ) {

                        Text(
                            equipoSeleccionado
                                ?.let {
                                    "${it.nombre} · ${it.categoria}"
                                }
                                ?: "Seleccionar equipo"
                        )
                    }

                    DropdownMenu(
                        expanded =
                            menuEquipo,
                        onDismissRequest = {
                            menuEquipo =
                                false
                        }
                    ) {

                        if (
                            estado.equipos
                                .isEmpty()
                        ) {

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "No tienes equipos activos asignados"
                                    )
                                },
                                enabled =
                                    false,
                                onClick = {}
                            )

                        } else {

                            estado.equipos
                                .forEach {
                                        equipo ->

                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                "${equipo.nombre} · ${equipo.categoria}"
                                            )
                                        },
                                        onClick = {

                                            viewModel
                                                .seleccionarEquipo(
                                                    equipo.id
                                                )

                                            menuEquipo =
                                                false
                                        }
                                    )
                                }
                        }
                    }
                }

                Spacer(
                    Modifier.height(
                        12.dp
                    )
                )
            }

            estado.error
                ?.let {

                    Text(
                        text =
                            it,
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

            estado.mensaje
                ?.let {

                    Text(
                        text =
                            it,
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

                estado.cargando -> {

                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .weight(
                                    1f
                                ),
                        contentAlignment =
                            Alignment.Center
                    ) {

                        CircularProgressIndicator(
                            color =
                                VerdeSportPro
                        )
                    }
                }

                estado.rol ==
                        Rol.DT &&
                        estado.equipos
                            .isEmpty() -> {

                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .weight(
                                    1f
                                ),
                        contentAlignment =
                            Alignment.Center
                    ) {

                        Text(
                            "No tienes un equipo activo asignado.",
                            color =
                                Color.Gray
                        )
                    }
                }

                lista.isEmpty() -> {

                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .weight(
                                    1f
                                ),
                        contentAlignment =
                            Alignment.Center
                    ) {

                        Text(
                            "No hay entrenamientos para mostrar.",
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
                                .weight(
                                    1f
                                ),
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
                                lista,
                            key = {
                                it.id
                            }
                        ) {
                                entrenamiento ->

                            TarjetaEntrenamiento(
                                entrenamiento =
                                    entrenamiento,
                                esDt =
                                    estado.rol ==
                                            Rol.DT,
                                onEditar = {

                                    entrenamientoEditar =
                                        entrenamiento

                                    mostrarFormulario =
                                        true
                                },
                                onCancelar = {

                                    entrenamientoCancelar =
                                        entrenamiento
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

        FormularioEntrenamiento(
            entrenamiento =
                entrenamientoEditar,
            ejerciciosActivos =
                estado.ejerciciosActivos,
            guardando =
                estado.guardando,
            error =
                estado.error,
            onCerrar = {

                mostrarFormulario =
                    false

                viewModel
                    .limpiarError()
            },
            onGuardar = {
                    fecha,
                    hora,
                    objetivo,
                    duracion,
                    ids ->

                viewModel
                    .guardar(
                        entrenamientoActual =
                            entrenamientoEditar,
                        fecha =
                            fecha,
                        hora =
                            hora,
                        objetivo =
                            objetivo,
                        duracionTexto =
                            duracion,
                        idsEjercicios =
                            ids
                    )

                mostrarFormulario =
                    false
            }
        )
    }

    entrenamientoCancelar
        ?.let {
                entrenamiento ->

            AlertDialog(
                onDismissRequest = {
                    entrenamientoCancelar =
                        null
                },
                title = {
                    Text(
                        "Cancelar entrenamiento"
                    )
                },
                text = {
                    Text(
                        "El entrenamiento permanecerá en el historial como cancelado."
                    )
                },
                confirmButton = {

                    Button(
                        onClick = {

                            viewModel
                                .cancelar(
                                    entrenamiento
                                )

                            entrenamientoCancelar =
                                null
                        }
                    ) {

                        Text(
                            "Cancelar entrenamiento"
                        )
                    }
                },
                dismissButton = {

                    TextButton(
                        onClick = {
                            entrenamientoCancelar =
                                null
                        }
                    ) {

                        Text(
                            "Volver"
                        )
                    }
                }
            )
        }
}

@Composable
private fun TarjetaEntrenamiento(
    entrenamiento:
    Entrenamiento,
    esDt: Boolean,
    onEditar: () -> Unit,
    onCancelar: () -> Unit
) {

    Card(
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
                        if (
                            entrenamiento.estado ==
                            EstadoEntrenamiento.CANCELADO
                        ) {
                            Color(
                                0xFFF3F3F3
                            )
                        } else {
                            Color.White
                        }
                )
    ) {

        Column(
            modifier =
                Modifier.padding(
                    16.dp
                )
        ) {

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
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
                        entrenamiento.nombreEquipo,
                        color =
                            AzulSportPro,
                        fontWeight =
                            FontWeight.Bold,
                        fontSize =
                            16.sp
                    )

                    Text(
                        "${entrenamiento.fecha} · ${entrenamiento.hora}",
                        color =
                            Color.Gray,
                        fontSize =
                            12.sp
                    )
                }

                Surface(
                    color =
                        if (
                            entrenamiento.estado ==
                            EstadoEntrenamiento.CANCELADO
                        ) {
                            RojoClaro
                        } else {
                            Color(
                                0xFFE9FFD8
                            )
                        },
                    shape =
                        RoundedCornerShape(
                            40.dp
                        )
                ) {

                    Text(
                        text =
                            if (
                                entrenamiento.estado ==
                                EstadoEntrenamiento.CANCELADO
                            ) {
                                "Cancelado"
                            } else {
                                "Publicado"
                            },
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
                            FontWeight.Bold
                    )
                }
            }

            Spacer(
                Modifier.height(
                    10.dp
                )
            )

            Text(
                entrenamiento.objetivo,
                fontWeight =
                    FontWeight.SemiBold,
                color =
                    AzulSportPro
            )

            Spacer(
                Modifier.height(
                    4.dp
                )
            )

            Text(
                "${entrenamiento.duracionTotalMinutos} min · ${entrenamiento.ejercicios.size} ejercicios",
                color =
                    Color.Gray,
                fontSize =
                    12.sp
            )

            entrenamiento.ejercicios
                .sortedBy {
                    it.orden
                }
                .forEachIndexed {
                        indice,
                        ejercicio ->

                    Text(
                        "${indice + 1}. ${ejercicio.nombre} · ${ejercicio.duracionMinutos} min",
                        fontSize =
                            12.sp,
                        color =
                            Color.DarkGray,
                        modifier =
                            Modifier.padding(
                                top =
                                    4.dp
                            )
                    )
                }

            if (
                esDt &&
                entrenamiento.estado ==
                EstadoEntrenamiento.PUBLICADO
            ) {

                Spacer(
                    Modifier.height(
                        8.dp
                    )
                )

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.End
                ) {

                    TextButton(
                        onClick =
                            onEditar
                    ) {

                        Text(
                            "Editar"
                        )
                    }

                    TextButton(
                        onClick =
                            onCancelar
                    ) {

                        Text(
                            "Cancelar"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FormularioEntrenamiento(
    entrenamiento:
    Entrenamiento?,
    ejerciciosActivos:
    List<Ejercicio>,
    guardando:
    Boolean,
    error:
    String?,
    onCerrar:
        () -> Unit,
    onGuardar:
        (
        String,
        String,
        String,
        String,
        List<String>
    ) -> Unit
) {

    val contexto =
        LocalContext.current

    var fecha by
    remember(
        entrenamiento
    ) {
        mutableStateOf(
            entrenamiento
                ?.fecha
                ?: LocalDate
                    .now()
                    .toString()
        )
    }

    var hora by
    remember(
        entrenamiento
    ) {
        mutableStateOf(
            entrenamiento
                ?.hora
                ?: LocalTime
                    .now()
                    .withSecond(0)
                    .withNano(0)
                    .format(
                        DateTimeFormatter
                            .ofPattern(
                                "HH:mm"
                            )
                    )
        )
    }

    var objetivo by
    remember(
        entrenamiento
    ) {
        mutableStateOf(
            entrenamiento
                ?.objetivo
                .orEmpty()
        )
    }

    var duracion by
    remember(
        entrenamiento
    ) {
        mutableStateOf(
            entrenamiento
                ?.duracionTotalMinutos
                ?.toString()
                .orEmpty()
        )
    }

    var seleccionados by remember(entrenamiento) {
        mutableStateOf<List<String>>(
            entrenamiento
                ?.ejercicios
                ?.sortedBy {
                    it.orden
                }
                ?.map {
                    it.idEjercicio
                }
                ?: emptyList()
        )
    }

    val duracionSesion =
        duracion
            .toIntOrNull()
            ?: 0

    fun duracionEjercicio(
        id: String
    ): Int {

        return ejerciciosActivos
            .firstOrNull {
                it.id ==
                        id
            }
            ?.duracionMinutos
            ?: entrenamiento
                ?.ejercicios
                ?.firstOrNull {
                    it.idEjercicio ==
                            id
                }
                ?.duracionMinutos
            ?: 0
    }

    fun nombreEjercicio(
        id: String
    ): String {

        return ejerciciosActivos
            .firstOrNull {
                it.id ==
                        id
            }
            ?.nombre
            ?: entrenamiento
                ?.ejercicios
                ?.firstOrNull {
                    it.idEjercicio ==
                            id
                }
                ?.nombre
            ?: "Ejercicio"
    }

    val sumaEjercicios =
        seleccionados
            .sumOf {
                duracionEjercicio(
                    it
                )
            }

    AlertDialog(
        onDismissRequest =
            onCerrar,
        title = {

            Text(
                if (
                    entrenamiento ==
                    null
                ) {
                    "Planificar entrenamiento"
                } else {
                    "Editar entrenamiento"
                }
            )
        },
        text = {

            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxWidth(),
                verticalArrangement =
                    Arrangement.spacedBy(
                        10.dp
                    )
            ) {

                item {

                    Text(
                        "Fecha *",
                        fontWeight =
                            FontWeight.SemiBold
                    )

                    OutlinedButton(
                        onClick = {

                            val actual =
                                try {
                                    LocalDate.parse(
                                        fecha
                                    )
                                } catch (
                                    e: Exception
                                ) {
                                    LocalDate.now()
                                }

                            DatePickerDialog(
                                contexto,
                                {
                                        _,
                                        anio,
                                        mes,
                                        dia ->

                                    fecha =
                                        LocalDate
                                            .of(
                                                anio,
                                                mes + 1,
                                                dia
                                            )
                                            .toString()
                                },
                                actual.year,
                                actual.monthValue - 1,
                                actual.dayOfMonth
                            ).show()
                        },
                        modifier =
                            Modifier.fillMaxWidth()
                    ) {

                        Text(
                            fecha
                        )
                    }
                }

                item {

                    Text(
                        "Hora *",
                        fontWeight =
                            FontWeight.SemiBold
                    )

                    OutlinedButton(
                        onClick = {

                            val actual =
                                try {
                                    LocalTime.parse(
                                        hora
                                    )
                                } catch (
                                    e: Exception
                                ) {
                                    LocalTime.now()
                                }

                            TimePickerDialog(
                                contexto,
                                {
                                        _,
                                        horaNueva,
                                        minutoNuevo ->

                                    hora =
                                        String.format(
                                            "%02d:%02d",
                                            horaNueva,
                                            minutoNuevo
                                        )
                                },
                                actual.hour,
                                actual.minute,
                                true
                            ).show()
                        },
                        modifier =
                            Modifier.fillMaxWidth()
                    ) {

                        Text(
                            hora
                        )
                    }
                }

                item {

                    OutlinedTextField(
                        value =
                            objetivo,
                        onValueChange = {
                            objetivo =
                                it
                        },
                        modifier =
                            Modifier.fillMaxWidth(),
                        label = {
                            Text(
                                "Objetivo de la sesión *"
                            )
                        }
                    )
                }

                item {

                    OutlinedTextField(
                        value =
                            duracion,
                        onValueChange = {

                            if (
                                it.all {
                                        caracter ->
                                    caracter.isDigit()
                                }
                            ) {
                                duracion =
                                    it
                            }
                        },
                        modifier =
                            Modifier.fillMaxWidth(),
                        label = {
                            Text(
                                "Duración total (minutos) *"
                            )
                        },
                        keyboardOptions =
                            KeyboardOptions(
                                keyboardType =
                                    KeyboardType.Number
                            ),
                        singleLine =
                            true
                    )
                }

                item {

                    Text(
                        "Ejercicios seleccionados",
                        fontWeight =
                            FontWeight.Bold
                    )

                    Text(
                        "$sumaEjercicios min de $duracionSesion min",
                        color =
                            Color.Gray,
                        fontSize =
                            12.sp
                    )

                    if (
                        duracionSesion >
                        0 &&
                        sumaEjercicios >
                        duracionSesion
                    ) {

                        Surface(
                            color =
                                AmarilloClaro,
                            shape =
                                RoundedCornerShape(
                                    10.dp
                                ),
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        top =
                                            6.dp
                                    )
                        ) {

                            Text(
                                "Advertencia: los ejercicios suman $sumaEjercicios min y superan los $duracionSesion min de la sesión.",
                                modifier =
                                    Modifier.padding(
                                        10.dp
                                    ),
                                color =
                                    Color(
                                        0xFF775D15
                                    ),
                                fontSize =
                                    12.sp
                            )
                        }
                    }
                }

                items(
                    seleccionados
                ) {
                        id ->

                    val indice =
                        seleccionados
                            .indexOf(
                                id
                            )

                    Card(
                        modifier =
                            Modifier.fillMaxWidth()
                    ) {

                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        10.dp
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
                                    "${indice + 1}. ${nombreEjercicio(id)}",
                                    fontWeight =
                                        FontWeight.SemiBold
                                )

                                Text(
                                    "${duracionEjercicio(id)} min",
                                    fontSize =
                                        11.sp,
                                    color =
                                        Color.Gray
                                )
                            }

                            TextButton(
                                enabled =
                                    indice >
                                            0,
                                onClick = {

                                    val nueva =
                                        seleccionados
                                            .toMutableList()

                                    val anterior =
                                        nueva[
                                            indice - 1
                                        ]

                                    nueva[
                                        indice - 1
                                    ] =
                                        nueva[
                                            indice
                                        ]

                                    nueva[
                                        indice
                                    ] =
                                        anterior

                                    seleccionados =
                                        nueva
                                }
                            ) {

                                Text(
                                    "↑"
                                )
                            }

                            TextButton(
                                enabled =
                                    indice <
                                            seleccionados
                                                .lastIndex,
                                onClick = {

                                    val nueva =
                                        seleccionados
                                            .toMutableList()

                                    val siguiente =
                                        nueva[
                                            indice + 1
                                        ]

                                    nueva[
                                        indice + 1
                                    ] =
                                        nueva[
                                            indice
                                        ]

                                    nueva[
                                        indice
                                    ] =
                                        siguiente

                                    seleccionados =
                                        nueva
                                }
                            ) {

                                Text(
                                    "↓"
                                )
                            }

                            TextButton(
                                onClick = {

                                    seleccionados =
                                        seleccionados
                                            .filterNot {
                                                it ==
                                                        id
                                            }
                                }
                            ) {

                                Text(
                                    "Quitar"
                                )
                            }
                        }
                    }
                }

                item {

                    Spacer(
                        Modifier.height(
                            4.dp
                        )
                    )

                    Text(
                        "Agregar ejercicios",
                        fontWeight =
                            FontWeight.Bold
                    )
                }

                items(
                    ejerciciosActivos
                        .filter {
                            it.id !in
                                    seleccionados
                        }
                ) {
                        ejercicio ->

                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(
                                    vertical =
                                        3.dp
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
                                ejercicio.nombre,
                                fontWeight =
                                    FontWeight.SemiBold
                            )

                            Text(
                                "${ejercicio.objetivo} · ${ejercicio.duracionMinutos} min",
                                color =
                                    Color.Gray,
                                fontSize =
                                    11.sp
                            )
                        }

                        TextButton(
                            onClick = {

                                seleccionados =
                                    seleccionados +
                                            ejercicio.id
                            }
                        ) {

                            Text(
                                "Agregar"
                            )
                        }
                    }
                }

                error
                    ?.let {

                        item {

                            Text(
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
            }
        },
        confirmButton = {

            Button(
                enabled =
                    !guardando &&
                            fecha.isNotBlank() &&
                            hora.isNotBlank() &&
                            objetivo.isNotBlank() &&
                            duracion.isNotBlank() &&
                            seleccionados.isNotEmpty(),
                onClick = {

                    onGuardar(
                        fecha,
                        hora,
                        objetivo,
                        duracion,
                        seleccionados
                    )
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
                    } else if (
                        entrenamiento ==
                        null
                    ) {
                        "Publicar"
                    } else {
                        "Guardar cambios"
                    }
                )
            }
        },
        dismissButton = {

            TextButton(
                onClick =
                    onCerrar
            ) {

                Text(
                    "Cerrar"
                )
            }
        }
    )
}