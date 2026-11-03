package com.example.projectsportpro2026_2.ui.comunidad

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectsportpro2026_2.datos.modelo.Academia
import com.example.projectsportpro2026_2.datos.modelo.Categorias
import com.example.projectsportpro2026_2.datos.modelo.Posiciones
import com.example.projectsportpro2026_2.datos.modelo.Rol
import com.example.projectsportpro2026_2.ui.comun.*
import com.example.projectsportpro2026_2.ui.partidos.FormaBotonSportPro
import com.example.projectsportpro2026_2.ui.partidos.FormaCampoSportPro
import com.example.projectsportpro2026_2.ui.partidos.SportProNavy
import com.example.projectsportpro2026_2.ui.partidos.SportProRojo
import com.example.projectsportpro2026_2.ui.partidos.SportProSuperficie
import com.example.projectsportpro2026_2.ui.partidos.SportProVerde
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioAvisoPrueba(
    academias: List<Academia>,
    nombreAutor: String,
    rolAutor: Rol?,
    guardando: Boolean,
    error: String?,
    onCerrar: () -> Unit,
    onPublicar: (
        idAcademia: String,
        categoria: String,
        posiciones: List<String>,
        fechaPrueba: Long,
        ubicacion: String,
        descripcion: String
    ) -> Unit
) {
    val contexto = LocalContext.current
    val estadoHoja = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var idAcademia by remember { mutableStateOf(academias.firstOrNull()?.id.orEmpty()) }
    var categoria by remember { mutableStateOf<String?>(null) }
    var posicionAbierta by remember { mutableStateOf(true) }
    var posiciones by remember { mutableStateOf(setOf<String>()) }
    var fecha by remember { mutableStateOf<LocalDate?>(null) }
    var hora by remember { mutableStateOf(LocalTime.of(9, 0)) }
    var ubicacion by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }

    // Fecha + hora convertidas a milisegundos (como se guarda en Firestore)
    val fechaPrueba: Long? = fecha?.let {
        LocalDateTime.of(it, hora).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
    val fechaEnElPasado = fechaPrueba != null && fechaPrueba <= System.currentTimeMillis()

    val formularioValido = idAcademia.isNotBlank() &&
            categoria != null &&
            fechaPrueba != null && !fechaEnElPasado &&
            ubicacion.isNotBlank() &&
            descripcion.isNotBlank() &&
            (posicionAbierta || posiciones.isNotEmpty())

    ModalBottomSheet(
        onDismissRequest = onCerrar,
        sheetState = estadoHoja,
        containerColor = SportProSuperficie
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Nuevo aviso de prueba",
                color = SportProNavy,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.titleLarge
            )

            if (academias.size > 1) {
                TituloCampoSportPro("ACADEMIA *")
                FilaDeChipsSportPro {
                    academias.forEach { academia ->
                        ChipSportPro(academia.nombre, academia.id == idAcademia) { idAcademia = academia.id }
                    }
                }
            }

            TituloCampoSportPro("CATEGORÍA *")
            Desplegable(
                etiqueta = "Selecciona una categoría…",
                opciones = Categorias.lista,
                seleccionado = categoria,
                textoNinguno = null                    // obligatorio
            ) { categoria = it }

            TituloCampoSportPro("POSICIÓN *")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChipSportPro("Abierta (todas)", posicionAbierta) { posicionAbierta = true }
                ChipSportPro("Específica", !posicionAbierta) { posicionAbierta = false }
            }
            if (!posicionAbierta) {
                FilaDeChipsSportPro {
                    Posiciones.lista.forEach { posicion ->
                        ChipSportPro(posicion, posicion in posiciones) {
                            // Selección múltiple: toca para agregar o quitar
                            posiciones = if (posicion in posiciones) posiciones - posicion else posiciones + posicion
                        }
                    }
                }
            }

            TituloCampoSportPro("FECHA Y HORA *")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        val inicial = fecha ?: LocalDate.now()
                        DatePickerDialog(
                            contexto,
                            { _, anio, mes, dia -> fecha = LocalDate.of(anio, mes + 1, dia) },
                            inicial.year,
                            inicial.monthValue - 1,
                            inicial.dayOfMonth
                        ).apply {
                            // Caso alterno HU23: el calendario no deja elegir días pasados
                            datePicker.minDate = System.currentTimeMillis() - 1000
                        }.show()
                    },
                    shape = FormaCampoSportPro,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = fecha?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "dd/mm/aaaa",
                        color = SportProNavy
                    )
                }
                OutlinedButton(
                    onClick = {
                        TimePickerDialog(
                            contexto,
                            { _, h, m -> hora = LocalTime.of(h, m) },
                            hora.hour,
                            hora.minute,
                            true
                        ).show()
                    },
                    shape = FormaCampoSportPro,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(hora.format(DateTimeFormatter.ofPattern("HH:mm")), color = SportProNavy)
                }
            }
            Text(
                text = if (fechaEnElPasado) "⚠ La fecha y hora deben ser posteriores a este momento"
                else "La fecha debe ser igual o posterior a hoy",
                color = if (fechaEnElPasado) SportProRojo else EtiquetaAzul,
                fontSize = 11.sp
            )

            TituloCampoSportPro("UBICACIÓN *")
            OutlinedTextField(
                value = ubicacion,
                onValueChange = { if (it.length <= 120) ubicacion = it },
                placeholder = { Text("Ej. Estadio Municipal de Surco") },
                singleLine = true,
                shape = FormaCampoSportPro,
                modifier = Modifier.fillMaxWidth()
            )

            TituloCampoSportPro("DESCRIPCIÓN *")
            OutlinedTextField(
                value = descripcion,
                onValueChange = { if (it.length <= 600) descripcion = it },
                placeholder = { Text("Detalles de la prueba, requisitos, documentos a presentar…") },
                minLines = 3,
                shape = FormaCampoSportPro,
                supportingText = { Text("${descripcion.length} / 600") },
                modifier = Modifier.fillMaxWidth()
            )

            // Aviso oficial: se muestra quién publica (RC-05)
            Surface(color = EtiquetaAzulFondo, shape = FormaCampoSportPro, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "✔ Se publicará como $nombreAutor con badge oficial [${rolAutor?.name ?: ""}]",
                    color = EtiquetaAzul,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }

            if (error != null) {
                Text(error, color = SportProRojo, fontSize = 13.sp)
            }

            Button(
                onClick = {
                    val c = categoria ?: return@Button
                    val f = fechaPrueba ?: return@Button
                    onPublicar(
                        idAcademia, c,
                        if (posicionAbierta) emptyList() else posiciones.toList(),
                        f, ubicacion, descripcion
                    )
                },
                enabled = formularioValido && !guardando,
                shape = FormaBotonSportPro,
                colors = ButtonDefaults.buttonColors(containerColor = SportProVerde),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (guardando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text("Publicar aviso", fontWeight = FontWeight.Bold)
                }
            }

            TextButton(onClick = onCerrar, modifier = Modifier.fillMaxWidth()) {
                Text("← Volver", color = SportProNavy)
            }
        }
    }
}