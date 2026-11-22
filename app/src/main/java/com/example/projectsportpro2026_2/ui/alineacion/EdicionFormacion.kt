package com.example.projectsportpro2026_2.ui.alineacion

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.projectsportpro2026_2.datos.alineacion.formacionesParaFormato
import com.example.projectsportpro2026_2.datos.modelo.alineacion.*
import com.example.projectsportpro2026_2.ui.partidos.SportProTextoSecundario

@Composable
internal fun SelectorFormacion(
    formato: FormatoPartido, formacion: String, personalizada: Boolean,
    tieneTitulares: Boolean, editable: Boolean, onSeleccionar: (String?) -> Unit
) {
    var abierto by remember { mutableStateOf(false) }
    var pendiente by remember { mutableStateOf<String?>(null) }
    Column {
        Text("FORMACIÓN", style = MaterialTheme.typography.labelSmall, color = SportProTextoSecundario)
        Box {
            OutlinedButton(onClick = { abierto = true }, enabled = editable) {
                Text(if (personalizada) "Personalizado" else formacion.ifBlank { "Distribución original" })
            }
            DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
                (formacionesParaFormato(formato) + "Personalizado").forEach { opcion ->
                    DropdownMenuItem(text = { Text(opcion) }, onClick = {
                        abierto = false
                        if (opcion == "Personalizado") onSeleccionar(null)
                        else if (personalizada && tieneTitulares) pendiente = opcion
                        else onSeleccionar(opcion)
                    })
                }
            }
        }
        if (personalizada && editable) Text(
            "Mantén pulsado un jugador y arrástralo. Se guarda al soltar.",
            style = MaterialTheme.typography.bodySmall, color = SportProTextoSecundario
        )
    }
    pendiente?.let { nueva ->
        AlertDialog(
            onDismissRequest = { pendiente = null },
            title = { Text("¿Redistribuir titulares?") },
            text = { Text("La formación $nueva reemplazará las posiciones personalizadas.") },
            confirmButton = { TextButton(onClick = { pendiente = null; onSeleccionar(nueva) }) { Text("Redistribuir") } },
            dismissButton = { TextButton(onClick = { pendiente = null }) { Text("Cancelar") } }
        )
    }
}

/** Centers are normalized against the same field bounds used to render them.
 * Local preview avoids database writes per pointer movement; cancellation discards it. */
@Composable
internal fun UbicacionEnCancha(
    slot: SlotCampo, alineado: JugadorAlineacion?, ancho: Dp, alto: Dp,
    editable: Boolean, onMover: (String, Float, Float) -> Unit,
    content: @Composable () -> Unit
) {
    val inicialX = alineado?.xCampo?.toFloat()?.takeIf { it.isFinite() } ?: slot.x
    val inicialY = alineado?.yCampo?.toFloat()?.takeIf { it.isFinite() } ?: slot.y
    var x by remember(alineado, slot) { mutableFloatStateOf(inicialX) }
    var y by remember(alineado, slot) { mutableFloatStateOf(inicialY) }
    val density = LocalDensity.current
    val anchoPx = with(density) { ancho.toPx() }
    val altoPx = with(density) { alto.toPx() }
    val minX = (30f / ancho.value).coerceAtMost(.5f)
    val maxX = 1f - minX
    val minY = (30f / alto.value).coerceAtMost(.5f)
    val maxY = (1f - 65f / alto.value).coerceAtLeast(minY)
    val moverActual by rememberUpdatedState(onMover)
    Box(Modifier
        .offset(x = (ancho.value * (if (alineado?.xCampo != null) x.coerceIn(minX, maxX) else x) - 27).dp,
                y = (alto.value * (if (alineado?.yCampo != null) y.coerceIn(minY, maxY) else y) - 27).dp)
        .testTag("posicion-${alineado?.idJugador ?: slot.id}")
        .pointerInput(editable, alineado, slot, anchoPx, altoPx) {
            if (editable && alineado != null) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { x = x.coerceIn(minX, maxX); y = y.coerceIn(minY, maxY) },
                    onDragCancel = { x = inicialX; y = inicialY },
                    onDragEnd = { moverActual(alineado.idJugador, x, y) },
                    onDrag = { change, delta ->
                        change.consume()
                        x = (x + delta.x / anchoPx).coerceIn(minX, maxX)
                        y = (y + delta.y / altoPx).coerceIn(minY, maxY)
                    }
                )
            }
        }
    ) { content() }
}