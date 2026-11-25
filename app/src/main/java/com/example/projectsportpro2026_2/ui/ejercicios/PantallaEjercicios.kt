package com.example.projectsportpro2026_2.ui.ejercicios

import com.example.projectsportpro2026_2.ui.theme.LineaDeCal
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
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectsportpro2026_2.datos.modelo.Ejercicio

private val AzulEjercicios: Color @Composable get() = LineaDeCal.colores.textoPrincipal // antes Color(0xFF0B3547)

private val VerdeEjercicios: Color @Composable get() = LineaDeCal.colores.verde // antes Color(0xFF168F5B)

private val RojoEjercicios: Color @Composable get() = LineaDeCal.colores.rojo // antes Color(0xFFB83232)

private val FondoEjercicios: Color @Composable get() = LineaDeCal.colores.fondo // antes Color(0xFFF7F9F8)

private val GrisEjercicios: Color @Composable get() = LineaDeCal.colores.textoSecundario // antes Color(0xFF5F666A)

@Composable
fun PantallaEjercicios(
    onVolver: () -> Unit,
    viewModel: EjerciciosViewModel =
        viewModel()
) {
    val estado by
    viewModel.estado.collectAsState()

    var busqueda by remember {
        mutableStateOf("")
    }

    var filtroActivo by remember {
        mutableStateOf<Boolean?>(null)
    }

    var menuAcademia by remember {
        mutableStateOf(false)
    }

    var mostrarFormulario by remember {
        mutableStateOf(false)
    }

    var ejercicioEditar by remember {
        mutableStateOf<Ejercicio?>(null)
    }

    LaunchedEffect(Unit) {
        viewModel.iniciar()
    }

    val academia =
        estado.academias
            .firstOrNull {
                it.id ==
                        estado.idAcademiaSeleccionada
            }

    val ejerciciosFiltrados =
        estado.ejercicios.filter {
                ejercicio ->

            val textoCoincide =
                ejercicio.nombre.contains(
                    busqueda,
                    ignoreCase = true
                ) ||
                        ejercicio.objetivo.contains(
                            busqueda,
                            ignoreCase = true
                        )

            val estadoCoincide =
                filtroActivo == null ||
                        ejercicio.activo ==
                        filtroActivo

            textoCoincide &&
                    estadoCoincide
        }

    Scaffold(
        containerColor =
            FondoEjercicios,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    ejercicioEditar = null
                    mostrarFormulario = true
                },
                containerColor =
                    VerdeEjercicios,
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
                                AzulEjercicios,
                            fontWeight =
                                FontWeight.SemiBold
                        )
                    }

                    Text(
                        "SPORTPRO",
                        color =
                            AzulEjercicios,
                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }

            item {
                Column {
                    Text(
                        "ENTRENAMIENTOS",
                        color =
                            VerdeEjercicios,
                        fontSize = 11.sp,
                        fontWeight =
                            FontWeight.Bold
                    )

                    Text(
                        "Biblioteca de ejercicios",
                        color =
                            AzulEjercicios,
                        fontSize = 26.sp,
                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }

            item {
                Box(
                    modifier =
                        Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = {
                            menuAcademia = true
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
                                        AzulEjercicios
                                )
                    ) {
                        Text(
                            academia?.nombre
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
                        estado.academias
                            .forEach {
                                    item ->

                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            item.nombre
                                        )
                                    },
                                    onClick = {
                                        viewModel
                                            .seleccionarAcademia(
                                                item.id
                                            )

                                        menuAcademia =
                                            false
                                    }
                                )
                            }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = busqueda,
                    onValueChange = {
                        busqueda = it
                    },
                    modifier =
                        Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "Buscar por nombre u objetivo"
                        )
                    },
                    singleLine = true
                )
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
                    FiltroEjercicio(
                        texto = "Todos",
                        seleccionado =
                            filtroActivo == null
                    ) {
                        filtroActivo = null
                    }

                    FiltroEjercicio(
                        texto = "Activos",
                        seleccionado =
                            filtroActivo == true
                    ) {
                        filtroActivo = true
                    }

                    FiltroEjercicio(
                        texto = "Inactivos",
                        seleccionado =
                            filtroActivo == false
                    ) {
                        filtroActivo = false
                    }
                }
            }

            estado.error?.let {
                    error ->

                item {
                    MensajeEjercicio(
                        texto = error,
                        fondo =
                            Color(0xFFFFE8E8),
                        color =
                            RojoEjercicios
                    )
                }
            }

            estado.mensaje?.let {
                    mensaje ->

                item {
                    MensajeEjercicio(
                        texto = mensaje,
                        fondo =
                            Color(0xFFE5F5EB),
                        color =
                            VerdeEjercicios
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
                                VerdeEjercicios
                        )
                    }
                }
            } else if (
                ejerciciosFiltrados
                    .isEmpty()
            ) {
                item {
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
                            "No hay ejercicios para mostrar.",
                            modifier =
                                Modifier.padding(
                                    16.dp
                                ),
                            color =
                                GrisEjercicios
                        )
                    }
                }
            } else {
                itemsIndexed(
                    items =
                        ejerciciosFiltrados,
                    key = {
                            _,
                            ejercicio ->

                        ejercicio.id
                    }
                ) {
                        indice,
                        ejercicio ->

                    TarjetaEjercicio(
                        numero =
                            indice + 1,
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

    if (mostrarFormulario) {
        FormularioEjercicio(
            ejercicio =
                ejercicioEditar,
            guardando =
                estado.guardando,
            onCerrar = {
                mostrarFormulario =
                    false
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
private fun FiltroEjercicio(
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
                        LineaDeCal.colores.superficieOscura
                    } else {
                        Color(0xFFEDEFF0)
                    },
                contentColor =
                    if (seleccionado) {
                        Color.White
                    } else {
                        AzulEjercicios
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
                14.dp
            ),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    LineaDeCal.colores.superficie,
                contentColor =
                    AzulEjercicios
            )
    ) {
        Column(
            modifier =
                Modifier.padding(
                    13.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    6.dp
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
                    "$numero. ${ejercicio.nombre}",
                    color =
                        AzulEjercicios,
                    fontSize = 15.sp,
                    fontWeight =
                        FontWeight.Bold
                )

                Surface(
                    color =
                        if (
                            ejercicio.activo
                        ) {
                            Color(
                                0xFFE5F5EB
                            )
                        } else {
                            Color(
                                0xFFFFE8E8
                            )
                        },
                    shape =
                        RoundedCornerShape(
                            50.dp
                        )
                ) {
                    Text(
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
                                    9.dp,
                                vertical =
                                    4.dp
                            ),
                        color =
                            if (
                                ejercicio.activo
                            ) {
                                VerdeEjercicios
                            } else {
                                RojoEjercicios
                            },
                        fontSize =
                            10.sp,
                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }

            Text(
                ejercicio.objetivo,
                color =
                    AzulEjercicios,
                fontSize = 12.sp,
                fontWeight =
                    FontWeight.SemiBold
            )

            if (
                ejercicio.descripcion
                    .isNotBlank()
            ) {
                Text(
                    ejercicio.descripcion,
                    color =
                        GrisEjercicios,
                    fontSize = 11.sp,
                    maxLines = 2
                )
            }

            Text(
                "${ejercicio.duracionMinutos} minutos",
                color =
                    GrisEjercicios,
                fontSize = 11.sp
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(
                        8.dp
                    )
            ) {
                OutlinedButton(
                    onClick = onEditar,
                    modifier =
                        Modifier
                            .width(120.dp)
                            .height(42.dp),
                    contentPadding =
                        PaddingValues(
                            horizontal = 4.dp
                        ),
                    colors =
                        ButtonDefaults
                            .outlinedButtonColors(
                                contentColor =
                                    AzulEjercicios
                            )
                ) {
                    Text(
                        "Editar",
                        fontSize = 11.sp
                    )
                }

                Button(
                    onClick =
                        onCambiarEstado,
                    modifier =
                        Modifier
                            .width(130.dp)
                            .height(42.dp),
                    contentPadding =
                        PaddingValues(
                            horizontal = 4.dp
                        ),
                    colors =
                        ButtonDefaults
                            .buttonColors(
                                containerColor =
                                    if (
                                        ejercicio.activo
                                    ) {
                                        RojoEjercicios
                                    } else {
                                        VerdeEjercicios
                                    },
                                contentColor =
                                    Color.White
                            )
                ) {
                    Text(
                        if (
                            ejercicio.activo
                        ) {
                            "Inactivar"
                        } else {
                            "Activar"
                        },
                        fontSize = 11.sp
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
    onCerrar: () -> Unit,
    onGuardar:
        (
        String,
        String,
        String,
        String
    ) -> Unit
) {
    var nombre by remember(ejercicio) {
        mutableStateOf(
            ejercicio
                ?.nombre
                .orEmpty()
        )
    }

    var objetivo by remember(ejercicio) {
        mutableStateOf(
            ejercicio
                ?.objetivo
                .orEmpty()
        )
    }

    var descripcion by remember(ejercicio) {
        mutableStateOf(
            ejercicio
                ?.descripcion
                .orEmpty()
        )
    }

    var duracion by remember(ejercicio) {
        mutableStateOf(
            ejercicio
                ?.duracionMinutos
                ?.toString()
                .orEmpty()
        )
    }

    AlertDialog(
        onDismissRequest = onCerrar,
        title = {
            Text(
                if (ejercicio == null) {
                    "Crear ejercicio"
                } else {
                    "Editar ejercicio"
                },
                color = AzulEjercicios
            )
        },
        text = {
            Column(
                verticalArrangement =
                    Arrangement.spacedBy(
                        8.dp
                    )
            ) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = {
                        nombre = it
                    },
                    modifier =
                        Modifier.fillMaxWidth(),
                    label = {
                        Text("Nombre")
                    },
                    singleLine = true
                )

                OutlinedTextField(
                    value = objetivo,
                    onValueChange = {
                        objetivo = it
                    },
                    modifier =
                        Modifier.fillMaxWidth(),
                    label = {
                        Text("Objetivo")
                    },
                    singleLine = true
                )

                OutlinedTextField(
                    value = descripcion,
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
                    minLines = 2,
                    maxLines = 3
                )

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
                            "Duración en minutos"
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
        },
        confirmButton = {
            Button(
                onClick = {
                    onGuardar(
                        nombre,
                        objetivo,
                        descripcion,
                        duracion
                    )
                },
                enabled =
                    nombre.isNotBlank() &&
                            objetivo.isNotBlank() &&
                            duracion.isNotBlank() &&
                            !guardando,
                colors =
                    ButtonDefaults
                        .buttonColors(
                            containerColor =
                                VerdeEjercicios,
                            contentColor =
                                Color.White
                        )
            ) {
                Text(
                    if (guardando) {
                        "Guardando..."
                    } else {
                        "Guardar"
                    }
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onCerrar
            ) {
                Text(
                    "Cancelar",
                    color =
                        AzulEjercicios
                )
            }
        }
    )
}

@Composable
private fun MensajeEjercicio(
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