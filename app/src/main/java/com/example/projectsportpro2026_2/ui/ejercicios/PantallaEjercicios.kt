package com.example.projectsportpro2026_2.ui.ejercicios

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
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectsportpro2026_2.datos.modelo.Ejercicio

private val AzulSportPro =
    Color(0xFF0B3547)

private val VerdeSportPro =
    Color(0xFF18A668)

private val VerdeClaro =
    Color(0xFFE9FFD8)

private val FondoPantalla =
    Color(0xFFF8FAF9)

private enum class FiltroEstadoEjercicio {
    TODOS,
    ACTIVOS,
    INACTIVOS
}

@Composable
fun PantallaEjercicios(
    onVolver: () -> Unit,
    viewModel: EjerciciosViewModel =
        viewModel()
) {

    val estado by
    viewModel.estado
        .collectAsState()

    var busqueda by remember {
        mutableStateOf("")
    }

    var filtroEstado by remember {
        mutableStateOf(
            FiltroEstadoEjercicio.TODOS
        )
    }

    var objetivoSeleccionado by remember {
        mutableStateOf("TODOS")
    }

    var menuAcademia by remember {
        mutableStateOf(false)
    }

    var menuObjetivo by remember {
        mutableStateOf(false)
    }

    var mostrarFormulario by remember {
        mutableStateOf(false)
    }

    var ejercicioEditar by remember {
        mutableStateOf<Ejercicio?>(
            null
        )
    }

    LaunchedEffect(Unit) {
        viewModel.iniciar()
    }

    val academiaSeleccionada =
        estado.academias
            .firstOrNull {
                it.id ==
                        estado.idAcademiaSeleccionada
            }

    val objetivos =
        estado.ejercicios
            .map {
                it.objetivo.trim()
            }
            .filter {
                it.isNotBlank()
            }
            .distinct()
            .sorted()

    val ejerciciosFiltrados =
        estado.ejercicios
            .filter { ejercicio ->

                val coincideBusqueda =
                    ejercicio.nombre.contains(
                        busqueda,
                        ignoreCase = true
                    ) ||
                            ejercicio.objetivo.contains(
                                busqueda,
                                ignoreCase = true
                            )

                val coincideEstado =
                    when (
                        filtroEstado
                    ) {

                        FiltroEstadoEjercicio.TODOS ->
                            true

                        FiltroEstadoEjercicio.ACTIVOS ->
                            ejercicio.activo

                        FiltroEstadoEjercicio.INACTIVOS ->
                            !ejercicio.activo
                    }

                val coincideObjetivo =
                    objetivoSeleccionado ==
                            "TODOS" ||
                            ejercicio.objetivo ==
                            objetivoSeleccionado

                coincideBusqueda &&
                        coincideEstado &&
                        coincideObjetivo
            }

    Scaffold(
        containerColor =
            FondoPantalla
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
                    12.dp
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

            Box(
                modifier =
                    Modifier.fillMaxWidth()
            ) {

                OutlinedButton(
                    onClick = {
                        menuAcademia =
                            true
                    },
                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    Text(
                        academiaSeleccionada
                            ?.nombre
                            ?: "Seleccionar academia"
                    )
                }

                DropdownMenu(
                    expanded =
                        menuAcademia,
                    onDismissRequest = {
                        menuAcademia =
                            false
                    }
                ) {

                    if (
                        estado.academias
                            .isEmpty()
                    ) {

                        DropdownMenuItem(
                            text = {
                                Text(
                                    "No hay academias disponibles"
                                )
                            },
                            enabled =
                                false,
                            onClick = {}
                        )

                    } else {

                        estado.academias
                            .forEach { academia ->

                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            academia.nombre
                                        )
                                    },
                                    onClick = {

                                        viewModel
                                            .seleccionarAcademia(
                                                academia.id
                                            )

                                        menuAcademia =
                                            false

                                        objetivoSeleccionado =
                                            "TODOS"
                                    }
                                )
                            }
                    }
                }
            }

            Spacer(
                Modifier.height(
                    16.dp
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
                        text =
                            "BIBLIOTECA",
                        color =
                            VerdeSportPro,
                        fontSize =
                            11.sp,
                        fontWeight =
                            FontWeight.Bold
                    )

                    Text(
                        text =
                            "Biblioteca",
                        color =
                            AzulSportPro,
                        fontSize =
                            30.sp,
                        fontWeight =
                            FontWeight.Bold
                    )
                }

                Button(
                    onClick = {

                        ejercicioEditar =
                            null

                        viewModel
                            .limpiarError()

                        mostrarFormulario =
                            true
                    },
                    modifier =
                        Modifier.size(
                            48.dp
                        ),
                    contentPadding =
                        PaddingValues(0.dp),
                    shape =
                        RoundedCornerShape(
                            14.dp
                        ),
                    colors =
                        ButtonDefaults
                            .buttonColors(
                                containerColor =
                                    VerdeSportPro
                            )
                ) {

                    Text(
                        text =
                            "+",
                        fontSize =
                            25.sp
                    )
                }
            }

            Spacer(
                Modifier.height(
                    14.dp
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
                        "Buscar por nombre u objetivo..."
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
                    10.dp
                )
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(
                        6.dp
                    )
            ) {

                FilterChip(
                    selected =
                        filtroEstado ==
                                FiltroEstadoEjercicio.TODOS,
                    onClick = {
                        filtroEstado =
                            FiltroEstadoEjercicio.TODOS
                    },
                    label = {
                        Text(
                            "Todos"
                        )
                    }
                )

                FilterChip(
                    selected =
                        filtroEstado ==
                                FiltroEstadoEjercicio.ACTIVOS,
                    onClick = {
                        filtroEstado =
                            FiltroEstadoEjercicio.ACTIVOS
                    },
                    label = {
                        Text(
                            "Activos"
                        )
                    }
                )

                FilterChip(
                    selected =
                        filtroEstado ==
                                FiltroEstadoEjercicio.INACTIVOS,
                    onClick = {
                        filtroEstado =
                            FiltroEstadoEjercicio.INACTIVOS
                    },
                    label = {
                        Text(
                            "Inactivos"
                        )
                    }
                )
            }

            Spacer(
                Modifier.height(
                    6.dp
                )
            )

            Box(
                modifier =
                    Modifier.fillMaxWidth()
            ) {

                OutlinedButton(
                    onClick = {
                        menuObjetivo =
                            true
                    },
                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    Text(
                        if (
                            objetivoSeleccionado ==
                            "TODOS"
                        ) {
                            "Todos los objetivos"
                        } else {
                            objetivoSeleccionado
                        }
                    )
                }

                DropdownMenu(
                    expanded =
                        menuObjetivo,
                    onDismissRequest = {
                        menuObjetivo =
                            false
                    }
                ) {

                    DropdownMenuItem(
                        text = {
                            Text(
                                "Todos los objetivos"
                            )
                        },
                        onClick = {
                            objetivoSeleccionado =
                                "TODOS"

                            menuObjetivo =
                                false
                        }
                    )

                    objetivos
                        .forEach { objetivo ->

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        objetivo
                                    )
                                },
                                onClick = {
                                    objetivoSeleccionado =
                                        objetivo

                                    menuObjetivo =
                                        false
                                }
                            )
                        }
                }
            }

            estado.error?.let {

                Spacer(
                    Modifier.height(
                        8.dp
                    )
                )

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
            }

            estado.mensaje?.let {

                Spacer(
                    Modifier.height(
                        8.dp
                    )
                )

                Text(
                    text =
                        it,
                    color =
                        VerdeSportPro,
                    fontSize =
                        13.sp
                )
            }

            Spacer(
                Modifier.height(
                    12.dp
                )
            )

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

                estado.academias
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
                                "No tienes academias disponibles.",
                            color =
                                Color.Gray
                        )
                    }
                }

                ejerciciosFiltrados
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
                                "No hay ejercicios para mostrar.",
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

                        itemsIndexed(
                            items =
                                ejerciciosFiltrados,
                            key = {
                                    _,
                                    ejercicio ->
                                ejercicio.id
                            }
                        ) {
                                index,
                                ejercicio ->

                            TarjetaEjercicio(
                                numero =
                                    index + 1,
                                ejercicio =
                                    ejercicio,
                                onEditar = {

                                    ejercicioEditar =
                                        ejercicio

                                    mostrarFormulario =
                                        true
                                },
                                onCambiarEstado = {

                                    viewModel
                                        .cambiarEstado(
                                            ejercicio
                                        )
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

        FormularioEjercicio(
            ejercicio =
                ejercicioEditar,
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
                    nombre,
                    objetivo,
                    descripcion,
                    duracion ->

                viewModel
                    .guardarEjercicio(
                        ejercicioActual =
                            ejercicioEditar,
                        nombre =
                            nombre,
                        objetivo =
                            objetivo,
                        descripcion =
                            descripcion,
                        duracionTexto =
                            duracion
                    )

                mostrarFormulario =
                    false
            }
        )
    }
}

@Composable
private fun TarjetaEjercicio(
    numero: Int,
    ejercicio: Ejercicio,
    onEditar: () -> Unit,
    onCambiarEstado: () -> Unit
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
                            ejercicio.activo
                        ) {
                            Color.White
                        } else {
                            Color(
                                0xFFF2F2F2
                            )
                        }
                )
    ) {

        Column(
            modifier =
                Modifier.padding(
                    14.dp
                )
        ) {

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Surface(
                    modifier =
                        Modifier.size(
                            48.dp
                        ),
                    color =
                        if (
                            ejercicio.activo
                        ) {
                            VerdeClaro
                        } else {
                            Color(
                                0xFFE5E5E5
                            )
                        },
                    shape =
                        RoundedCornerShape(
                            14.dp
                        )
                ) {

                    Box(
                        contentAlignment =
                            Alignment.Center
                    ) {

                        Text(
                            text =
                                numero.toString(),
                            fontWeight =
                                FontWeight.Bold,
                            fontSize =
                                18.sp,
                            color =
                                AzulSportPro
                        )
                    }
                }

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
                            ejercicio.nombre,
                        fontWeight =
                            FontWeight.Bold,
                        color =
                            AzulSportPro,
                        fontSize =
                            15.sp
                    )

                    Text(
                        text =
                            ejercicio.objetivo,
                        color =
                            Color.Gray,
                        fontSize =
                            12.sp
                    )

                    Text(
                        text =
                            "${ejercicio.duracionMinutos} min",
                        color =
                            Color.Gray,
                        fontSize =
                            11.sp
                    )
                }

                Surface(
                    color =
                        if (
                            ejercicio.activo
                        ) {
                            VerdeClaro
                        } else {
                            Color(
                                0xFFFFE7E7
                            )
                        },
                    shape =
                        RoundedCornerShape(
                            50.dp
                        )
                ) {

                    Text(
                        text =
                            if (
                                ejercicio.activo
                            ) {
                                "Activo"
                            } else {
                                "Inactivo"
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

            if (
                ejercicio.descripcion
                    .isNotBlank()
            ) {

                Spacer(
                    Modifier.height(
                        8.dp
                    )
                )

                Text(
                    text =
                        ejercicio.descripcion,
                    color =
                        Color.DarkGray,
                    fontSize =
                        12.sp
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
                        onCambiarEstado
                ) {

                    Text(
                        if (
                            ejercicio.activo
                        ) {
                            "Inactivar"
                        } else {
                            "Activar"
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FormularioEjercicio(
    ejercicio: Ejercicio?,
    guardando: Boolean,
    error: String?,
    onCancelar: () -> Unit,
    onGuardar: (
        String,
        String,
        String,
        String
    ) -> Unit
) {

    var nombre by remember(
        ejercicio
    ) {
        mutableStateOf(
            ejercicio
                ?.nombre
                .orEmpty()
        )
    }

    var objetivo by remember(
        ejercicio
    ) {
        mutableStateOf(
            ejercicio
                ?.objetivo
                .orEmpty()
        )
    }

    var descripcion by remember(
        ejercicio
    ) {
        mutableStateOf(
            ejercicio
                ?.descripcion
                .orEmpty()
        )
    }

    var duracion by remember(
        ejercicio
    ) {
        mutableStateOf(
            ejercicio
                ?.duracionMinutos
                ?.toString()
                .orEmpty()
        )
    }

    AlertDialog(
        onDismissRequest =
            onCancelar,
        title = {

            Text(
                if (
                    ejercicio ==
                    null
                ) {
                    "Crear ejercicio"
                } else {
                    "Editar ejercicio"
                }
            )
        },
        text = {

            Column(
                verticalArrangement =
                    Arrangement.spacedBy(
                        10.dp
                    )
            ) {

                OutlinedTextField(
                    value =
                        nombre,
                    onValueChange = {
                        nombre = it
                    },
                    modifier =
                        Modifier.fillMaxWidth(),
                    label = {
                        Text(
                            "Nombre *"
                        )
                    },
                    singleLine =
                        true
                )

                OutlinedTextField(
                    value =
                        objetivo,
                    onValueChange = {
                        objetivo = it
                    },
                    modifier =
                        Modifier.fillMaxWidth(),
                    label = {
                        Text(
                            "Objetivo *"
                        )
                    },
                    singleLine =
                        true
                )

                OutlinedTextField(
                    value =
                        descripcion,
                    onValueChange = {
                        descripcion = it
                    },
                    modifier =
                        Modifier.fillMaxWidth(),
                    label = {
                        Text(
                            "Descripción"
                        )
                    },
                    minLines =
                        3
                )

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
                            "Duración estimada (minutos) *"
                        )
                    },
                    singleLine =
                        true,
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType =
                                KeyboardType.Number
                        )
                )

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
                            nombre.isNotBlank() &&
                            objetivo.isNotBlank() &&
                            duracion.isNotBlank(),
                onClick = {

                    onGuardar(
                        nombre,
                        objetivo,
                        descripcion,
                        duracion
                    )
                },
                colors =
                    ButtonDefaults
                        .buttonColors(
                            containerColor =
                                AzulSportPro
                        )
            ) {

                Text(
                    if (
                        guardando
                    ) {
                        "Guardando..."
                    } else if (
                        ejercicio ==
                        null
                    ) {
                        "Crear ejercicio"
                    } else {
                        "Guardar cambios"
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