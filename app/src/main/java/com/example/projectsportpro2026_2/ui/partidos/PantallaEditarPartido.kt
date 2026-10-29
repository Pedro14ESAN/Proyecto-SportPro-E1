package com.example.projectsportpro2026_2.ui.partidos

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun PantallaEditarPartido(
    idPartido: String,
    onVolver: () -> Unit,
    onPartidoEditado: () -> Unit,
    viewModel: PartidosViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()
    val contexto = LocalContext.current

    var idEquipoSeleccionado by remember {
        mutableStateOf("")
    }

    var selectorEquipoAbierto by remember {
        mutableStateOf(false)
    }

    var rival by remember {
        mutableStateOf("")
    }

    var fecha by remember {
        mutableStateOf("")
    }

    var hora by remember {
        mutableStateOf("")
    }

    var ubicacion by remember {
        mutableStateOf("")
    }

    LaunchedEffect(idPartido) {
        viewModel.cargarPartido(idPartido)
    }

    val partido = estado.partidoSeleccionado

    LaunchedEffect(partido?.id) {
        partido?.let {
            idEquipoSeleccionado = it.idEquipo
            rival = it.nombreRival
            fecha = it.fecha
            hora = it.hora
            ubicacion = it.ubicacion
        }
    }

    val equipoSeleccionado = estado.equipos.firstOrNull {
        it.id == idEquipoSeleccionado
    }

    val hoy = LocalDate.now()

    val dialogoFecha = remember {
        DatePickerDialog(
            contexto,
            { _, anio, mes, dia ->
                fecha = LocalDate.of(
                    anio,
                    mes + 1,
                    dia
                ).toString()
            },
            hoy.year,
            hoy.monthValue - 1,
            hoy.dayOfMonth
        )
    }

    val ahora = LocalTime.now()

    val dialogoHora = remember {
        TimePickerDialog(
            contexto,
            { _, horas, minutos ->
                hora = String.format(
                    "%02d:%02d",
                    horas,
                    minutos
                )
            },
            ahora.hour,
            ahora.minute,
            true
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SportProFondo)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        Spacer(
            modifier = Modifier.height(18.dp)
        )

        CabeceraEditarSportPro()

        TextButton(
            onClick = onVolver
        ) {
            Text(
                text = "← Volver al partido",
                color = SportProVerde
            )
        }

        Text(
            text = "EDITAR PARTIDO",
            style = MaterialTheme.typography.labelMedium,
            color = SportProVerde,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Editar partido",
            style = MaterialTheme.typography.headlineMedium,
            color = SportProNavy,
            fontWeight = FontWeight.Black
        )

        if (estado.cargando && partido == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(40.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = SportProVerde
                )
            }

            return@Column
        }

        estado.error?.let { mensaje ->
            Text(
                text = mensaje,
                color = SportProRojo
            )
        }

        if (partido == null) {
            Text(
                text = "No se pudo cargar el partido.",
                color = SportProTextoSecundario
            )

            return@Column
        }

        // EQUIPO
        Text(
            text = "EQUIPO *",
            style = MaterialTheme.typography.labelMedium,
            color = SportProTextoSecundario,
            fontWeight = FontWeight.Bold
        )

        Box {
            OutlinedTextField(
                value = equipoSeleccionado?.nombre.orEmpty(),
                onValueChange = {},
                readOnly = true,
                placeholder = {
                    Text("Selecciona un equipo")
                },
                trailingIcon = {
                    Text(
                        text = "⌄",
                        color = SportProTextoSecundario,
                        fontWeight = FontWeight.Bold
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = FormaCampoSportPro,
                colors = coloresCampoEditarSportPro()
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable {
                        selectorEquipoAbierto = true
                    }
            )

            DropdownMenu(
                expanded = selectorEquipoAbierto,
                onDismissRequest = {
                    selectorEquipoAbierto = false
                },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                estado.equipos.forEach { equipo ->
                    DropdownMenuItem(
                        text = {
                            Text(equipo.nombre)
                        },
                        onClick = {
                            idEquipoSeleccionado = equipo.id
                            selectorEquipoAbierto = false
                        }
                    )
                }
            }
        }

        // RIVAL
        Text(
            text = "RIVAL *",
            style = MaterialTheme.typography.labelMedium,
            color = SportProTextoSecundario,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = rival,
            onValueChange = {
                rival = it
            },
            placeholder = {
                Text("Nombre del equipo rival")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = FormaCampoSportPro,
            colors = coloresCampoEditarSportPro()
        )

        Text(
            text = "Puedes escribir el nombre libremente. No es obligatorio que esté registrado en SportPro.",
            style = MaterialTheme.typography.bodySmall,
            color = SportProTextoSecundario
        )

        // FECHA Y HORA
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "FECHA *",
                    style = MaterialTheme.typography.labelMedium,
                    color = SportProTextoSecundario,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                SelectorEditarSportPro(
                    valor = mostrarFechaEditar(fecha),
                    placeholder = "dd/mm/aaaa",
                    icono = "▣",
                    onClick = {
                        dialogoFecha.show()
                    }
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "HORA *",
                    style = MaterialTheme.typography.labelMedium,
                    color = SportProTextoSecundario,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                SelectorEditarSportPro(
                    valor = hora,
                    placeholder = "--:--",
                    icono = "◷",
                    onClick = {
                        dialogoHora.show()
                    }
                )
            }
        }

        // UBICACIÓN
        Text(
            text = "UBICACIÓN *",
            style = MaterialTheme.typography.labelMedium,
            color = SportProTextoSecundario,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = ubicacion,
            onValueChange = {
                ubicacion = it
            },
            placeholder = {
                Text("Ej. Estadio Municipal, Cancha Norte")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = FormaCampoSportPro,
            colors = coloresCampoEditarSportPro()
        )

        estado.error?.let { mensaje ->
            Text(
                text = mensaje,
                color = SportProRojo,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(
            modifier = Modifier.height(6.dp)
        )

        Button(
            onClick = {
                viewModel.editarPartido(
                    idPartido = partido.id,
                    idEquipo = idEquipoSeleccionado,
                    nombreRival = rival,
                    fecha = fecha,
                    hora = hora,
                    ubicacion = ubicacion
                ) {
                    onPartidoEditado()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = !estado.cargando &&
                    idEquipoSeleccionado.isNotBlank(),
            shape = FormaBotonSportPro,
            colors = ButtonDefaults.buttonColors(
                containerColor = SportProNavy,
                contentColor = Color.White,
                disabledContainerColor =
                    SportProNavy.copy(alpha = 0.45f)
            )
        ) {

            if (estado.cargando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Guardar cambios",
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Text(
            text = "Solo los partidos en estado Programado pueden editarse.",
            style = MaterialTheme.typography.bodySmall,
            color = SportProTextoSecundario,
            modifier = Modifier.align(
                Alignment.CenterHorizontally
            )
        )

        Spacer(
            modifier = Modifier.height(32.dp)
        )
    }
}

@Composable
private fun SelectorEditarSportPro(
    valor: String,
    placeholder: String,
    icono: String,
    onClick: () -> Unit
) {
    Box {
        OutlinedTextField(
            value = valor,
            onValueChange = {},
            readOnly = true,
            placeholder = {
                Text(placeholder)
            },
            trailingIcon = {
                Text(
                    text = icono,
                    color = SportProNavy
                )
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = FormaCampoSportPro,
            colors = coloresCampoEditarSportPro()
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(
                    onClick = onClick
                )
        )
    }
}

@Composable
private fun CabeceraEditarSportPro() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Surface(
                modifier = Modifier.size(38.dp),
                shape = RoundedCornerShape(10.dp),
                color = SportProNavy
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text("⚽")
                }
            }

            Column {
                Text(
                    text = "SPORTPRO",
                    color = SportProNavy,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "",
                    color = SportProTextoSecundario,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        Surface(
            modifier = Modifier.size(38.dp),
            shape = CircleShape,
            color = SportProVerdeClaro
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Text("🔔")
            }
        }
    }
}

@Composable
private fun coloresCampoEditarSportPro() =
    OutlinedTextFieldDefaults.colors(
        focusedBorderColor = SportProVerde,
        unfocusedBorderColor = SportProBorde,
        focusedContainerColor = SportProSuperficie,
        unfocusedContainerColor = SportProSuperficie,
        cursorColor = SportProVerde,
        focusedTextColor = SportProTextoPrincipal,
        unfocusedTextColor = SportProTextoPrincipal,
        focusedPlaceholderColor = SportProTextoSecundario,
        unfocusedPlaceholderColor = SportProTextoSecundario
    )

private fun mostrarFechaEditar(
    fecha: String
): String {
    if (fecha.isBlank()) {
        return ""
    }

    return try {
        LocalDate.parse(fecha).format(
            DateTimeFormatter.ofPattern("dd/MM/yyyy")
        )
    } catch (_: Exception) {
        fecha
    }
}