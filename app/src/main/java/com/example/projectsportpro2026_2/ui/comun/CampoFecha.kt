package com.example.projectsportpro2026_2.ui.comun

import android.app.DatePickerDialog
import android.content.DialogInterface
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

private val formatoLatino = DateTimeFormatter.ofPattern("dd/MM/yyyy")

// Convierte "2012-03-05" (lo que se guarda) en "05/03/2012" (lo que se ve)
fun fechaLatina(iso: String): String {
    if (iso.isBlank()) return ""
    return try {
        LocalDate.parse(iso.trim()).format(formatoLatino)
    } catch (e: DateTimeParseException) {
        iso
    }
}

private fun leerFecha(iso: String): LocalDate? =
    try {
        if (iso.isBlank()) null else LocalDate.parse(iso.trim())
    } catch (e: DateTimeParseException) {
        null
    }

// Campo de solo lectura que abre un calendario.
// valor y onCambio usan siempre aaaa-MM-dd (el formato que se guarda en Firestore).
@Composable
fun CampoFecha(
    valor: String,
    onCambio: (String) -> Unit,
    modifier: Modifier = Modifier,
    etiqueta: String? = null,
    placeholder: String = "dd/mm/aaaa",
    textoAyuda: String? = null,
    soloPasado: Boolean = true,
    permitirQuitar: Boolean = false,
    fechaInicial: LocalDate = LocalDate.now().minusYears(15),
    shape: Shape = RoundedCornerShape(16.dp),
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors()
) {
    val contexto = LocalContext.current

    val abrirCalendario = {
        val base = leerFecha(valor) ?: fechaInicial
        val dialogo = DatePickerDialog(
            contexto,
            { _, anio, mes, dia -> onCambio(LocalDate.of(anio, mes + 1, dia).toString()) },
            base.year,
            base.monthValue - 1,
            base.dayOfMonth
        )
        if (soloPasado) {
            dialogo.datePicker.maxDate = System.currentTimeMillis()
        }
        if (permitirQuitar && valor.isNotBlank()) {
            dialogo.setButton(DialogInterface.BUTTON_NEUTRAL, "Quitar") { _, _ -> onCambio("") }
        }
        dialogo.show()
    }

    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = fechaLatina(valor),
            onValueChange = {},
            readOnly = true,
            label = if (etiqueta != null) { { Text(etiqueta) } } else null,
            placeholder = { Text(placeholder) },
            supportingText = if (textoAyuda != null) { { Text(textoAyuda) } } else null,
            trailingIcon = { Text("📅") },
            singleLine = true,
            shape = shape,
            colors = colors,
            modifier = Modifier.fillMaxWidth()
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(onClick = abrirCalendario)
        )
    }
}