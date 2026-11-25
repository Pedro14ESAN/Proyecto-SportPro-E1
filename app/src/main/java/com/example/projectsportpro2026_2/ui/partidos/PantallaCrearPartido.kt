package com.example.projectsportpro2026_2.ui.partidos

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
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
fun PantallaCrearPartido(
    idAcademia: String,
    onVolver: () -> Unit,
    onPartidoCreado: (String) -> Unit,
    viewModel: PartidosViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()
    val contexto = LocalContext.current

    var idEquipoSeleccionado by remember { mutableStateOf("") }
    var rival by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var hora by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("") }

    var selectorEquipoAbierto by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(idAcademia) {
        viewModel.iniciar(idAcademia)
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

        Spacer(Modifier.height(18.dp))

        CabeceraSportProSimple()

        TextButton(
            onClick = onVolver
        ) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
            Text(
                text = "Partidos",
                color = SportProVerde,
                fontWeight = FontWeight.Medium
            )
        }

        Text(
            text = "NUEVO PARTIDO",
            style = MaterialTheme.typography.labelMedium,
            color = SportProVerde,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Crear partido",
            style = MaterialTheme.typography.headlineMedium,
            color = SportProNavy,
            fontWeight = FontWeight.Black
        )

        Spacer(Modifier.height(4.dp))

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
                    Icon(
                        imageVector = Icons.Rounded.ExpandMore,
                        contentDescription = null,
                        tint = SportProTextoSecundario
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = FormaCampoSportPro,
                colors = coloresCampoSportPro()
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
            colors = coloresCampoSportPro()
        )

        Text(
            text = "Puedes escribir el nombre libremente. " +
                    "No es obligatorio que esté registrado en SportPro.",
            style = MaterialTheme.typography.bodySmall,
            color = SportProTextoSecundario
        )

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

                Spacer(Modifier.height(6.dp))

                SelectorLecturaSportPro(
                    valor = mostrarFecha(fecha),
                    placeholder = "dd/mm/aaaa",
                    icono = Icons.Rounded.CalendarMonth,
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

                Spacer(Modifier.height(6.dp))

                SelectorLecturaSportPro(
                    valor = hora,
                    placeholder = "--:--",
                    icono = Icons.Rounded.Schedule,
                    onClick = {
                        dialogoHora.show()
                    }
                )
            }
        }

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
                Text(
                    "Ej. Estadio Municipal, Cancha Norte"
                )
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = FormaCampoSportPro,
            colors = coloresCampoSportPro()
        )

        estado.error?.let { mensaje ->

            Text(
                text = mensaje,
                color = SportProRojo,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(Modifier.height(4.dp))

        Button(
            onClick = {

                viewModel.crearPartido(
                    idEquipo = idEquipoSeleccionado,
                    nombreRival = rival,
                    fecha = fecha,
                    hora = hora,
                    ubicacion = ubicacion
                ) { partido ->

                    onPartidoCreado(partido.id)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = !estado.cargando &&
                    idEquipoSeleccionado.isNotBlank(),
            shape = FormaBotonSportPro,
            colors = ButtonDefaults.buttonColors(
                containerColor = SportProNavyFondo,
                contentColor = Color.White,
                disabledContainerColor =
                    SportProNavyFondo.copy(alpha = 0.45f)
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
                    text = "Crear partido",
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Text(
            text = "El partido se creará con estado Programado",
            style = MaterialTheme.typography.bodySmall,
            color = SportProTextoSecundario,
            modifier = Modifier.align(
                Alignment.CenterHorizontally
            )
        )

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun SelectorLecturaSportPro(
    valor: String,
    placeholder: String,
    icono: ImageVector,
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
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = SportProNavy
                    )
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = FormaCampoSportPro,
            colors = coloresCampoSportPro()
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
private fun CabeceraSportProSimple() {
    // Línea de cal: la cabecera decorativa (logo ⚽ y campana 🔔 sin acción) se quitó;
    // cada pantalla muestra su título y su botón de volver.
}

@Composable
private fun coloresCampoSportPro() =
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

private fun mostrarFecha(
    fecha: String
): String {

    if (fecha.isBlank()) {
        return ""
    }

    return try {

        LocalDate.parse(fecha).format(
            DateTimeFormatter.ofPattern("dd/MM/yyyy")
        )

    } catch (e: Exception) {

        fecha
    }
}