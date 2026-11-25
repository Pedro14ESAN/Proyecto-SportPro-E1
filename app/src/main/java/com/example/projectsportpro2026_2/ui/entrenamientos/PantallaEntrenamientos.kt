package com.example.projectsportpro2026_2.ui.entrenamientos

import com.example.projectsportpro2026_2.ui.theme.EstiloMarcador
import com.example.projectsportpro2026_2.ui.theme.EstiloEtiqueta
import com.example.projectsportpro2026_2.ui.comun.TipoEstado
import com.example.projectsportpro2026_2.ui.comun.ChipEstado
import com.example.projectsportpro2026_2.ui.comun.VarianteTicket
import com.example.projectsportpro2026_2.ui.comun.TarjetaTicket
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.Icons
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Icon
import com.example.projectsportpro2026_2.ui.comun.anchoCompleto
import com.example.projectsportpro2026_2.ui.comun.CabeceraCancha
import com.example.projectsportpro2026_2.ui.theme.LineaDeCal
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

private val AzulEntrenamiento: Color @Composable get() = LineaDeCal.colores.textoPrincipal // antes Color(0xFF0B3547)

private val VerdeEntrenamiento: Color @Composable get() = LineaDeCal.colores.verde // antes Color(0xFF168F5B)

private val RojoEntrenamiento: Color @Composable get() = LineaDeCal.colores.rojo // antes Color(0xFFB83232)

private val AmarilloEntrenamiento: Color @Composable get() = LineaDeCal.colores.amarilloTexto // antes Color(0xFF8A6814)

private val FondoEntrenamiento: Color @Composable get() = LineaDeCal.colores.fondo // antes Color(0xFFF7F9F8)

private val GrisEntrenamiento: Color @Composable get() = LineaDeCal.colores.textoSecundario // antes Color(0xFF5F666A)

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
                        LineaDeCal.colores.lima,
                    contentColor =
                        LineaDeCal.colores.navy
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Planificar entrenamiento")
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
            // Línea de cal: cabecera de césped (mismos textos y el mismo "Volver")
            item {
                CabeceraCancha(
                    titulo = if (esDt) {
                        "Entrenamientos"
                    } else {
                        "Mis entrenamientos"
                    },
                    etiqueta = "PLANIFICACIÓN",
                    onVolver = onVolver,
                    descripcionVolver = "Volver",
                    modifier = Modifier.anchoCompleto(16.dp)
                )
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
                            LineaDeCal.colores.errorFondo /* antes 0xFFFFE8E8 */,
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
                            LineaDeCal.colores.okFondo /* antes 0xFFE5F5EB */,
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
                            LineaDeCal.colores.superficie,
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
                        LineaDeCal.colores.superficieOscura
                    } else {
                        LineaDeCal.colores.apagadoFondo /* antes 0xFFEDEFF0 */
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
            fontSize = 12.sp,
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
    // Línea de cal (6.6, img 05): ticket con hora y fecha en el talón.
    // Los cancelados se ven apagados y tachados y se conservan en el historial.
    TarjetaTicket(
        hora = entrenamiento.hora,
        fecha = fechaCortaEntrenamiento(entrenamiento.fecha),
        variante = if (entrenamiento.estado == EstadoEntrenamiento.CANCELADO) {
            VarianteTicket.TACHADA
        } else {
            VarianteTicket.VERDE
        }
    ) {
        Column(
            modifier =
                Modifier,
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
                        Modifier.weight(1f),
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
                fontSize = 12.sp
            )

            // 6.6: el cambio de horario se muestra como nota naranja (historial que ya existe)
            entrenamiento.historialCambiosHorario.lastOrNull()?.let { cambio ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Rounded.Schedule,
                        contentDescription = null,
                        tint = LineaDeCal.colores.pendienteTexto,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        "Cambió de ${cambio.fechaAnterior} ${cambio.horaAnterior} a ${cambio.fechaNueva} ${cambio.horaNueva}",
                        color = LineaDeCal.colores.pendienteTexto,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

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
                    fontSize = 12.sp,
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
                            fontSize = 12.sp
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
                            fontSize = 12.sp
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

    ChipEstado(
        if (publicado) {
            "Publicado"
        } else {
            "Cancelado"
        },
        if (publicado) TipoEstado.OK else TipoEstado.APAGADO
    )
}

/** "2026-11-24" → "LUN 24" para el talón del ticket; si no se puede leer, se deja igual. */
private fun fechaCortaEntrenamiento(fecha: String): String =
    try {
        java.time.LocalDate.parse(fecha.trim())
            .format(java.time.format.DateTimeFormatter.ofPattern("EEE d", java.util.Locale.forLanguageTag("es-PE")))
            .replace(".", "")
            .uppercase()
    } catch (e: Exception) {
        fecha
    }

/** Caja LED de Planificar: minutos de los ejercicios sobre la duración total, con barra. */
@Composable
private fun CajaLedMinutos(usados: Int, total: Int) {
    val c = LineaDeCal.colores
    val excede = total > 0 && usados > total
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.ledFondo, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "EJERCICIOS",
                color = c.amarillo,
                style = EstiloEtiqueta,
                modifier = Modifier.weight(1f)
            )
            Text(
                "$usados/$total",
                color = if (excede) c.naranja else c.lima,
                style = EstiloMarcador.copy(fontSize = 24.sp, lineHeight = 26.sp)
            )
        }
        LinearProgressIndicator(
            progress = { if (total > 0) (usados.toFloat() / total).coerceIn(0f, 1f) else 0f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = if (excede) c.naranja else c.lima,
            trackColor = Color.White.copy(alpha = 0.15f)
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
                                LineaDeCal.colores.amarilloFondo /* antes 0xFFFFF4D6 */
                            } else {
                                LineaDeCal.colores.neutroFondo /* antes 0xFFE8F2F5 */
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

                            // 6.6: caja LED "EJERCICIOS x/y" con barra
                            CajaLedMinutos(
                                usados = minutosEjercicios,
                                total = minutosSesion
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
                                fontSize = 12.sp
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
                                    fontSize = 12.sp,
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
                                    LineaDeCal.colores.apagadoFondo /* antes 0xFFF0F2F3 */
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
                                fontSize = 12.sp
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
                                    Icon(Icons.Rounded.KeyboardArrowUp, contentDescription = "Subir ejercicio")
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
                                    Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = "Bajar ejercicio")
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
                                        fontSize = 12.sp
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
                                    LineaDeCal.colores.superficie
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
                                fontSize = 12.sp
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
                            fontSize = 12.sp
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