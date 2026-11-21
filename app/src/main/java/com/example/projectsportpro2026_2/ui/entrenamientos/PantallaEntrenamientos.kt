package com.example.projectsportpro2026_2.ui.entrenamientos

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
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

private val AzulEntrenamiento =
    Color(0xFF0B3547)

private val VerdeEntrenamiento =
    Color(0xFF168F5B)

private val RojoEntrenamiento =
    Color(0xFFB83232)

private val AmarilloEntrenamiento =
    Color(0xFF8A6814)

private val FondoEntrenamiento =
    Color(0xFFF7F9F8)

private val GrisEntrenamiento =
    Color(0xFF5F666A)

@Composable
fun PantallaEntrenamientos(
    onVolver: () -> Unit,
    viewModel: EntrenamientosViewModel =
        viewModel()
) {
    val estado by
    viewModel.estado.collectAsState()

    var filtro by remember {
        mutableStateOf<EstadoEntrenamiento?>(
            null
        )
    }

    var menuEquipo by remember {
        mutableStateOf(false)
    }

    var mostrarFormulario by remember {
        mutableStateOf(false)
    }

    var entrenamientoEditar by remember {
        mutableStateOf<Entrenamiento?>(
            null
        )
    }

    LaunchedEffect(Unit) {
        viewModel.iniciar()
    }

    val esDt =
        estado.rol == Rol.DT

    val equipo =
        estado.equipos
            .firstOrNull {
                it.id ==
                        estado.idEquipoSeleccionado
            }

    val entrenamientosFiltrados =
        estado.entrenamientos
            .filter {
                    entrenamiento ->

                val equipoCoincide =
                    !esDt ||
                            estado
                                .idEquipoSeleccionado
                                .isBlank() ||
                            entrenamiento.idEquipo ==
                            estado
                                .idEquipoSeleccionado

                val estadoCoincide =
                    filtro == null ||
                            entrenamiento.estado ==
                            filtro

                equipoCoincide &&
                        estadoCoincide
            }

    Scaffold(
        containerColor =
            FondoEntrenamiento,
        floatingActionButton = {
            if (esDt) {
                FloatingActionButton(
                    onClick = {
                        entrenamientoEditar =
                            null
                        mostrarFormulario =
                            true
                    },
                    containerColor =
                        VerdeEntrenamiento,
                    contentColor =
                        Color.White
                ) {
                    Text(
                        "+",
                        fontSize = 25.sp,
                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }
        }
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
                Arrangement.spacedBy(8.dp),
            contentPadding =
                PaddingValues(
                    bottom = 90.dp
                )
        ) {
            item {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                    horizontalArrangement =
                        Arrangement.SpaceBetween,
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onVolver
                    ) {
                        Text(
                            "← Volver",
                            color =
                                AzulEntrenamiento,
                            fontWeight =
                                FontWeight.SemiBold
                        )
                    }

                    Text(
                        "SPORTPRO",
                        color =
                            AzulEntrenamiento,
                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }

            item {
                Column {
                    Text(
                        "PLANIFICACIÓN",
                        color =
                            VerdeEntrenamiento,
                        fontSize = 11.sp,
                        fontWeight =
                            FontWeight.Bold
                    )

                    Text(
                        if (esDt) {
                            "Entrenamientos"
                        } else {
                            "Mis entrenamientos"
                        },
                        color =
                            AzulEntrenamiento,
                        fontSize = 26.sp,
                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }

            if (esDt) {
                item {
                    Box(
                        modifier =
                            Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = {
                                menuEquipo =
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
                                            AzulEntrenamiento
                                    )
                        ) {
                            Text(
                                equipo?.let {
                                    if (
                                        it.categoria
                                            .isNotBlank()
                                    ) {
                                        "${it.nombre} · ${it.categoria}"
                                    } else {
                                        it.nombre
                                    }
                                } ?: "Seleccionar equipo"
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
                            estado.equipos
                                .forEach {
                                        equipoItem ->

                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                equipoItem
                                                    .nombre
                                            )
                                        },
                                        onClick = {
                                            viewModel
                                                .seleccionarEquipo(
                                                    equipoItem.id
                                                )

                                            menuEquipo =
                                                false
                                        }
                                    )
                                }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .horizontalScroll(
                                rememberScrollState()
                            ),
                    horizontalArrangement =
                        Arrangement.spacedBy(
                            6.dp
                        )
                ) {
                    FiltroEntrenamiento(
                        texto = "Todos",
                        seleccionado =
                            filtro == null
                    ) {
                        filtro = null
                    }

                    FiltroEntrenamiento(
                        texto =
                            "Publicados",
                        seleccionado =
                            filtro ==
                                    EstadoEntrenamiento.PUBLICADO
                    ) {
                        filtro =
                            EstadoEntrenamiento.PUBLICADO
                    }

                    FiltroEntrenamiento(
                        texto =
                            "Cancelados",
                        seleccionado =
                            filtro ==
                                    EstadoEntrenamiento.CANCELADO
                    ) {
                        filtro =
                            EstadoEntrenamiento.CANCELADO
                    }
                }
            }

            estado.error?.let {
                    error ->

                item {
                    MensajeEntrenamiento(
                        texto = error,
                        fondo =
                            Color(0xFFFFE8E8),
                        color =
                            RojoEntrenamiento
                    )
                }
            }

            estado.mensaje?.let {
                    mensaje ->

                item {
                    MensajeEntrenamiento(
                        texto = mensaje,
                        fondo =
                            Color(0xFFE5F5EB),
                        color =
                            VerdeEntrenamiento
                    )
                }
            }

            if (estado.cargando) {
                item {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                        contentAlignment =
                            Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color =
                                VerdeEntrenamiento
                        )
                    }
                }
            } else if (
                entrenamientosFiltrados
                    .isEmpty()
            ) {
                item {
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
                            "No hay entrenamientos para mostrar.",
                            modifier =
                                Modifier.padding(
                                    16.dp
                                ),
                            color =
                                GrisEntrenamiento
                        )
                    }
                }
            } else {
                items(
                    items =
                        entrenamientosFiltrados,
                    key = {
                        it.id
                    }
                ) {
                        entrenamiento ->

                    TarjetaEntrenamiento(
                        entrenamiento =
                            entrenamiento,
                        puedeGestionar =
                            esDt,
                        onEditar = {
                            entrenamientoEditar =
                                entrenamiento

                            mostrarFormulario =
                                true
                        },
                        onCancelar = {
                            viewModel.cancelar(
                                entrenamiento
                            )
                        }
                    )
                }
            }
        }
    }

    if (mostrarFormulario) {
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
            },
            onGuardar = {
                    fecha,
                    hora,
                    objetivo,
                    duracion,
                    ejercicios ->

                viewModel.guardar(
                    entrenamientoActual =
                        entrenamientoEditar,
                    fecha = fecha,
                    hora = hora,
                    objetivo = objetivo,
                    duracionTexto =
                        duracion,
                    idsEjercicios =
                        ejercicios
                )

                mostrarFormulario =
                    false
            }
        )
    }
}

@Composable
private fun FiltroEntrenamiento(
    texto: String,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier =
            Modifier.height(40.dp),
        shape =
            RoundedCornerShape(
                10.dp
            ),
        contentPadding =
            PaddingValues(
                horizontal = 16.dp
            ),
        colors =
            ButtonDefaults.buttonColors(
                containerColor =
                    if (seleccionado) {
                        AzulEntrenamiento
                    } else {
                        Color(0xFFEDEFF0)
                    },
                contentColor =
                    if (seleccionado) {
                        Color.White
                    } else {
                        AzulEntrenamiento
                    }
            )
    ) {
        Text(
            texto,
            fontSize = 11.sp,
            fontWeight =
                FontWeight.SemiBold
        )
    }
}

@Composable
private fun TarjetaEntrenamiento(
    entrenamiento: Entrenamiento,
    puedeGestionar: Boolean,
    onEditar: () -> Unit,
    onCancelar: () -> Unit
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
                    AzulEntrenamiento
            )
    ) {
        Column(
            modifier =
                Modifier.padding(
                    13.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    5.dp
                )
        ) {
            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween,
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Text(
                    entrenamiento.nombreEquipo,
                    modifier =
                        Modifier.width(
                            210.dp
                        ),
                    color =
                        AzulEntrenamiento,
                    fontSize = 15.sp,
                    fontWeight =
                        FontWeight.Bold
                )

                EstadoEntrenamientoEtiqueta(
                    entrenamiento.estado
                )
            }

            Text(
                "${entrenamiento.fecha} · ${entrenamiento.hora}",
                color =
                    GrisEntrenamiento,
                fontSize = 12.sp
            )

            Text(
                entrenamiento.objetivo,
                color =
                    AzulEntrenamiento,
                fontSize = 13.sp,
                fontWeight =
                    FontWeight.SemiBold
            )

            Text(
                "${entrenamiento.duracionTotalMinutos} min · ${entrenamiento.ejercicios.size} ejercicios",
                color =
                    GrisEntrenamiento,
                fontSize = 11.sp
            )

            if (
                entrenamiento.ejercicios
                    .isNotEmpty()
            ) {
                Text(
                    entrenamiento.ejercicios
                        .sortedBy {
                            it.orden
                        }
                        .joinToString(
                            " · "
                        ) {
                            it.nombre
                        },
                    color =
                        GrisEntrenamiento,
                    fontSize = 10.sp,
                    maxLines = 2
                )
            }

            if (
                puedeGestionar &&
                entrenamiento.estado ==
                EstadoEntrenamiento.PUBLICADO
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .horizontalScroll(
                                rememberScrollState()
                            ),
                    horizontalArrangement =
                        Arrangement.spacedBy(
                            8.dp
                        )
                ) {
                    OutlinedButton(
                        onClick =
                            onEditar,
                        modifier =
                            Modifier
                                .width(130.dp)
                                .height(42.dp),
                        colors =
                            ButtonDefaults
                                .outlinedButtonColors(
                                    contentColor =
                                        AzulEntrenamiento
                                )
                    ) {
                        Text(
                            "Editar sesión",
                            fontSize = 11.sp
                        )
                    }

                    Button(
                        onClick =
                            onCancelar,
                        modifier =
                            Modifier
                                .width(145.dp)
                                .height(42.dp),
                        colors =
                            ButtonDefaults
                                .buttonColors(
                                    containerColor =
                                        RojoEntrenamiento,
                                    contentColor =
                                        Color.White
                                )
                    ) {
                        Text(
                            "Cancelar sesión",
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EstadoEntrenamientoEtiqueta(
    estado: EstadoEntrenamiento
) {
    val publicado =
        estado ==
                EstadoEntrenamiento.PUBLICADO

    Surface(
        color =
            if (publicado) {
                Color(0xFFE5F5EB)
            } else {
                Color(0xFFFFE8E8)
            },
        shape =
            RoundedCornerShape(
                50.dp
            )
    ) {
        Text(
            if (publicado) {
                "Publicado"
            } else {
                "Cancelado"
            },
            modifier =
                Modifier.padding(
                    horizontal = 9.dp,
                    vertical = 4.dp
                ),
            color =
                if (publicado) {
                    VerdeEntrenamiento
                } else {
                    RojoEntrenamiento
                },
            fontSize = 10.sp,
            fontWeight =
                FontWeight.Bold
        )
    }
}

@Composable
private fun FormularioEntrenamiento(
    entrenamiento: Entrenamiento?,
    ejerciciosActivos: List<Ejercicio>,
    guardando: Boolean,
    error: String?,
    onCerrar: () -> Unit,
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

    var fecha by remember(entrenamiento) {
        mutableStateOf(
            entrenamiento?.fecha
                ?: LocalDate.now()
                    .toString()
        )
    }

    var hora by remember(entrenamiento) {
        mutableStateOf(
            entrenamiento?.hora
                ?: LocalTime.now()
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

    var objetivo by remember(entrenamiento) {
        mutableStateOf(
            entrenamiento
                ?.objetivo
                .orEmpty()
        )
    }

    var duracion by remember(entrenamiento) {
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

    fun nombreEjercicio(
        id: String
    ): String {
        return ejerciciosActivos
            .firstOrNull {
                it.id == id
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

    fun duracionEjercicio(
        id: String
    ): Int {
        return ejerciciosActivos
            .firstOrNull {
                it.id == id
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

    val minutosEjercicios =
        seleccionados.sumOf {
            duracionEjercicio(
                it
            )
        }

    val minutosSesion =
        duracion.toIntOrNull()
            ?: 0

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
                },
                color =
                    AzulEntrenamiento
            )
        },
        text = {
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(
                            max = 520.dp
                        ),
                verticalArrangement =
                    Arrangement.spacedBy(
                        8.dp
                    )
            ) {
                item {
                    Text(
                        "Fecha",
                        color =
                            AzulEntrenamiento,
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
                                        LocalDate.of(
                                            anio,
                                            mes + 1,
                                            dia
                                        ).toString()
                                },
                                actual.year,
                                actual.monthValue - 1,
                                actual.dayOfMonth
                            ).show()
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                        colors =
                            ButtonDefaults
                                .outlinedButtonColors(
                                    contentColor =
                                        AzulEntrenamiento
                                )
                    ) {
                        Text(fecha)
                    }
                }

                item {
                    Text(
                        "Hora",
                        color =
                            AzulEntrenamiento,
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
                                        nuevaHora,
                                        nuevoMinuto ->

                                    hora =
                                        String.format(
                                            "%02d:%02d",
                                            nuevaHora,
                                            nuevoMinuto
                                        )
                                },
                                actual.hour,
                                actual.minute,
                                true
                            ).show()
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                        colors =
                            ButtonDefaults
                                .outlinedButtonColors(
                                    contentColor =
                                        AzulEntrenamiento
                                )
                    ) {
                        Text(hora)
                    }
                }

                item {
                    OutlinedTextField(
                        value = objetivo,
                        onValueChange = {
                            objetivo = it
                        },
                        modifier =
                            Modifier.fillMaxWidth(),
                        label = {
                            Text(
                                "Objetivo de la sesión"
                            )
                        },
                        maxLines = 2
                    )
                }

                item {
                    OutlinedTextField(
                        value = duracion,
                        onValueChange = {
                            if (
                                it.all {
                                        caracter ->
                                    caracter.isDigit()
                                }
                            ) {
                                duracion = it
                            }
                        },
                        modifier =
                            Modifier.fillMaxWidth(),
                        label = {
                            Text(
                                "Duración total en minutos"
                            )
                        },
                        keyboardOptions =
                            KeyboardOptions(
                                keyboardType =
                                    KeyboardType.Number
                            ),
                        singleLine = true
                    )
                }

                item {
                    Surface(
                        modifier =
                            Modifier.fillMaxWidth(),
                        color =
                            if (
                                minutosSesion >
                                0 &&
                                minutosEjercicios >
                                minutosSesion
                            ) {
                                Color(
                                    0xFFFFF4D6
                                )
                            } else {
                                Color(
                                    0xFFE8F2F5
                                )
                            },
                        shape =
                            RoundedCornerShape(
                                10.dp
                            )
                    ) {
                        Column(
                            modifier =
                                Modifier.padding(
                                    10.dp
                                )
                        ) {
                            Text(
                                "Ejercicios seleccionados",
                                color =
                                    AzulEntrenamiento,
                                fontWeight =
                                    FontWeight.Bold
                            )

                            Text(
                                "$minutosEjercicios min de $minutosSesion min",
                                color =
                                    if (
                                        minutosSesion >
                                        0 &&
                                        minutosEjercicios >
                                        minutosSesion
                                    ) {
                                        AmarilloEntrenamiento
                                    } else {
                                        GrisEntrenamiento
                                    },
                                fontSize = 11.sp
                            )

                            if (
                                minutosSesion >
                                0 &&
                                minutosEjercicios >
                                minutosSesion
                            ) {
                                Text(
                                    "Los ejercicios superan la duración total.",
                                    color =
                                        AmarilloEntrenamiento,
                                    fontSize = 11.sp,
                                    fontWeight =
                                        FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                items(
                    items =
                        seleccionados
                ) {
                        id ->

                    val indice =
                        seleccionados
                            .indexOf(id)

                    Card(
                        modifier =
                            Modifier.fillMaxWidth(),
                        colors =
                            CardDefaults.cardColors(
                                containerColor =
                                    Color(
                                        0xFFF0F2F3
                                    )
                            )
                    ) {
                        Column(
                            modifier =
                                Modifier.padding(
                                    9.dp
                                ),
                            verticalArrangement =
                                Arrangement.spacedBy(
                                    4.dp
                                )
                        ) {
                            Text(
                                "${indice + 1}. ${nombreEjercicio(id)}",
                                color =
                                    AzulEntrenamiento,
                                fontWeight =
                                    FontWeight.SemiBold
                            )

                            Text(
                                "${duracionEjercicio(id)} min",
                                color =
                                    GrisEntrenamiento,
                                fontSize = 10.sp
                            )

                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(
                                            rememberScrollState()
                                        ),
                                horizontalArrangement =
                                    Arrangement.spacedBy(
                                        5.dp
                                    )
                            ) {
                                OutlinedButton(
                                    enabled =
                                        indice > 0,
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
                                            nueva[indice]

                                        nueva[indice] =
                                            anterior

                                        seleccionados =
                                            nueva
                                    },
                                    modifier =
                                        Modifier
                                            .width(
                                                70.dp
                                            )
                                            .height(
                                                38.dp
                                            ),
                                    contentPadding =
                                        PaddingValues(
                                            0.dp
                                        )
                                ) {
                                    Text("↑")
                                }

                                OutlinedButton(
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
                                            nueva[indice]

                                        nueva[indice] =
                                            siguiente

                                        seleccionados =
                                            nueva
                                    },
                                    modifier =
                                        Modifier
                                            .width(
                                                70.dp
                                            )
                                            .height(
                                                38.dp
                                            ),
                                    contentPadding =
                                        PaddingValues(
                                            0.dp
                                        )
                                ) {
                                    Text("↓")
                                }

                                Button(
                                    onClick = {
                                        seleccionados =
                                            seleccionados
                                                .filterNot {
                                                    it ==
                                                            id
                                                }
                                    },
                                    modifier =
                                        Modifier
                                            .width(
                                                100.dp
                                            )
                                            .height(
                                                38.dp
                                            ),
                                    colors =
                                        ButtonDefaults
                                            .buttonColors(
                                                containerColor =
                                                    RojoEntrenamiento,
                                                contentColor =
                                                    Color.White
                                            ),
                                    contentPadding =
                                        PaddingValues(
                                            horizontal =
                                                4.dp
                                        )
                                ) {
                                    Text(
                                        "Quitar",
                                        fontSize =
                                            10.sp
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        "Agregar ejercicios",
                        color =
                            AzulEntrenamiento,
                        fontWeight =
                            FontWeight.Bold
                    )
                }

                items(
                    items =
                        ejerciciosActivos
                            .filter {
                                it.id !in
                                        seleccionados
                            }
                ) {
                        ejercicio ->

                    Card(
                        modifier =
                            Modifier.fillMaxWidth(),
                        colors =
                            CardDefaults.cardColors(
                                containerColor =
                                    Color.White
                            )
                    ) {
                        Column(
                            modifier =
                                Modifier.padding(
                                    9.dp
                                ),
                            verticalArrangement =
                                Arrangement.spacedBy(
                                    4.dp
                                )
                        ) {
                            Text(
                                ejercicio.nombre,
                                color =
                                    AzulEntrenamiento,
                                fontWeight =
                                    FontWeight.SemiBold
                            )

                            Text(
                                "${ejercicio.objetivo} · ${ejercicio.duracionMinutos} min",
                                color =
                                    GrisEntrenamiento,
                                fontSize = 10.sp
                            )

                            Button(
                                onClick = {
                                    seleccionados =
                                        seleccionados +
                                                ejercicio.id
                                },
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(40.dp),
                                colors =
                                    ButtonDefaults
                                        .buttonColors(
                                            containerColor =
                                                VerdeEntrenamiento,
                                            contentColor =
                                                Color.White
                                        )
                            ) {
                                Text(
                                    "Agregar ejercicio"
                                )
                            }
                        }
                    }
                }

                error?.let {
                        mensaje ->

                    item {
                        Text(
                            mensaje,
                            color =
                                RojoEntrenamiento,
                            fontSize = 11.sp
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
                                VerdeEntrenamiento,
                            contentColor =
                                Color.White
                        )
            ) {
                Text(
                    if (guardando) {
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
                    "Cerrar",
                    color =
                        AzulEntrenamiento
                )
            }
        }
    )
}

@Composable
private fun MensajeEntrenamiento(
    texto: String,
    fondo: Color,
    color: Color
) {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        color = fondo,
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
            color = color,
            fontSize = 12.sp,
            fontWeight =
                FontWeight.Medium
        )
    }
}