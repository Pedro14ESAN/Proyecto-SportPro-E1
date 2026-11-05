package com.example.projectsportpro2026_2.ui.comunidad

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectsportpro2026_2.datos.modelo.DecisionModeracion
import com.example.projectsportpro2026_2.datos.modelo.MotivoReporte
import com.example.projectsportpro2026_2.datos.modelo.TipoContenido
import com.example.projectsportpro2026_2.ui.comun.*
import com.example.projectsportpro2026_2.ui.partidos.FormaBotonSportPro
import com.example.projectsportpro2026_2.ui.partidos.FormaCampoSportPro
import com.example.projectsportpro2026_2.ui.partidos.SportProFondo
import com.example.projectsportpro2026_2.ui.partidos.SportProNavy
import com.example.projectsportpro2026_2.ui.partidos.SportProRojo
import com.example.projectsportpro2026_2.ui.partidos.SportProSuperficie
import com.example.projectsportpro2026_2.ui.partidos.SportProTextoPrincipal
import com.example.projectsportpro2026_2.ui.partidos.SportProTextoSecundario
import com.example.projectsportpro2026_2.ui.partidos.SportProVerde

private enum class PasoAcciones { MENU, REPORTAR, BLOQUEAR, MODERAR }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HojaAccionesContenido(
    seleccionado: ContenidoSeleccionado,
    puedeModerar: Boolean,
    procesando: Boolean,
    error: String?,
    onCerrar: () -> Unit,
    onReportar: (MotivoReporte, String) -> Unit,
    onBloquear: () -> Unit,
    onModerar: (DecisionModeracion, String) -> Unit
) {
    val estadoHoja = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var paso by remember { mutableStateOf(PasoAcciones.MENU) }
    var motivo by remember { mutableStateOf<MotivoReporte?>(null) }
    var detalle by remember { mutableStateOf("") }
    var decision by remember { mutableStateOf(DecisionModeracion.OCULTADO) }
    var motivoDecision by remember { mutableStateOf("") }

    val esAviso = seleccionado.objetivo.tipo == TipoContenido.AVISO
    val tipoTexto = when (seleccionado.objetivo.tipo) {
        TipoContenido.PUBLICACION -> "publicación"
        TipoContenido.COMENTARIO -> "comentario"
        TipoContenido.AVISO -> "aviso"
    }

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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Contexto: qué contenido se está reportando o moderando
            Surface(color = SportProFondo, shape = FormaCampoSportPro, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AvatarIniciales(seleccionado.nombreAutor, tamano = 32.dp)
                    Column {
                        Text(seleccionado.nombreAutor, color = SportProNavy, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            text = seleccionado.resumen,
                            color = SportProTextoSecundario,
                            fontSize = 12.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            when (paso) {

                PasoAcciones.MENU -> {
                    OpcionAccion("🚩", "Reportar $tipoTexto", SportProRojo) { paso = PasoAcciones.REPORTAR }
                    // Los avisos son de la academia, no de una persona: no se bloquean
                    if (!esAviso) {
                        OpcionAccion("⊘", "Bloquear a ${seleccionado.nombreAutor}", SportProNavy) {
                            paso = PasoAcciones.BLOQUEAR
                        }
                    }
                    if (puedeModerar) {
                        OpcionAccion("🛡", "Moderar directamente", SportProVerde) { paso = PasoAcciones.MODERAR }
                    }
                }

                PasoAcciones.REPORTAR -> {
                    TituloHoja("Reportar $tipoTexto")
                    TituloCampoSportPro("MOTIVO *")
                    MotivoReporte.entries.forEach { m ->
                        FilaRadio(m.texto, motivo == m) { motivo = m }
                    }
                    OutlinedTextField(
                        value = detalle,
                        onValueChange = { if (it.length <= 300) detalle = it },
                        placeholder = { Text("Detalle (opcional)") },
                        minLines = 2,
                        shape = FormaCampoSportPro,
                        modifier = Modifier.fillMaxWidth()
                    )
                    NotaInfo("🔒 Tu identidad no se mostrará a la persona reportada.")
                    MensajeErrorHoja(error)
                    BotonesHoja(
                        textoConfirmar = "Enviar reporte",
                        colorConfirmar = SportProRojo,
                        habilitado = motivo != null && !procesando,
                        procesando = procesando,
                        onVolver = { paso = PasoAcciones.MENU },
                        onConfirmar = { motivo?.let { onReportar(it, detalle) } }
                    )
                }

                PasoAcciones.BLOQUEAR -> {
                    TituloHoja("¿Bloquear a ${seleccionado.nombreAutor}?")
                    Text(
                        text = "Ninguno de los dos verá las publicaciones ni los comentarios del otro, " +
                                "y no podrán reaccionar ni comentar en lo del otro. No se borra nada " +
                                "y puedes desbloquear cuando quieras.",
                        color = SportProTextoPrincipal,
                        fontSize = 13.sp
                    )
                    MensajeErrorHoja(error)
                    BotonesHoja(
                        textoConfirmar = "Bloquear",
                        colorConfirmar = SportProRojo,
                        habilitado = !procesando,
                        procesando = procesando,
                        onVolver = { paso = PasoAcciones.MENU },
                        onConfirmar = onBloquear
                    )
                }

                PasoAcciones.MODERAR -> {
                    TituloHoja("Moderar $tipoTexto")
                    TituloCampoSportPro("DECISIÓN *")
                    FilaRadio("Ocultar contenido", decision == DecisionModeracion.OCULTADO) {
                        decision = DecisionModeracion.OCULTADO
                    }
                    if (esAviso) {
                        FilaRadio("Cerrar aviso", decision == DecisionModeracion.CERRADO) {
                            decision = DecisionModeracion.CERRADO
                        }
                    }
                    TituloCampoSportPro("MOTIVO *")
                    OutlinedTextField(
                        value = motivoDecision,
                        onValueChange = { if (it.length <= 300) motivoDecision = it },
                        placeholder = { Text("¿Por qué tomas esta decisión?") },
                        minLines = 2,
                        shape = FormaCampoSportPro,
                        modifier = Modifier.fillMaxWidth()
                    )
                    NotaInfo("Quedará registrado con tu nombre, la fecha y el motivo.")
                    MensajeErrorHoja(error)
                    BotonesHoja(
                        textoConfirmar = "Aplicar",
                        colorConfirmar = SportProVerde,
                        habilitado = motivoDecision.isNotBlank() && !procesando,
                        procesando = procesando,
                        onVolver = { paso = PasoAcciones.MENU },
                        onConfirmar = { onModerar(decision, motivoDecision) }
                    )
                }
            }
        }
    }
}

// ---------- Piezas internas de la hoja ----------

@Composable
private fun TituloHoja(texto: String) {
    Text(texto, color = SportProNavy, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleLarge)
}

@Composable
private fun OpcionAccion(emoji: String, texto: String, color: Color, onClick: () -> Unit) {
    Surface(onClick = onClick, color = SportProFondo, shape = FormaCampoSportPro, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(emoji, fontSize = 18.sp)
            Text(texto, color = color, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun FilaRadio(texto: String, seleccionado: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = seleccionado,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = SportProVerde)
        )
        Text(texto, color = SportProTextoPrincipal, fontSize = 14.sp)
    }
}

@Composable
private fun NotaInfo(texto: String) {
    Surface(color = EtiquetaAzulFondo, shape = FormaCampoSportPro, modifier = Modifier.fillMaxWidth()) {
        Text(texto, color = EtiquetaAzul, fontSize = 12.sp, modifier = Modifier.padding(12.dp))
    }
}

@Composable
private fun MensajeErrorHoja(error: String?) {
    if (error != null) Text(error, color = SportProRojo, fontSize = 13.sp)
}

@Composable
private fun BotonesHoja(
    textoConfirmar: String,
    colorConfirmar: Color,
    habilitado: Boolean,
    procesando: Boolean,
    onVolver: () -> Unit,
    onConfirmar: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedButton(onClick = onVolver, shape = FormaBotonSportPro, modifier = Modifier.weight(1f)) {
            Text("Volver", color = SportProNavy)
        }
        Button(
            onClick = onConfirmar,
            enabled = habilitado,
            shape = FormaBotonSportPro,
            colors = ButtonDefaults.buttonColors(containerColor = colorConfirmar),
            modifier = Modifier.weight(1f)
        ) {
            if (procesando) {
                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
            } else {
                Text(textoConfirmar, fontWeight = FontWeight.Bold)
            }
        }
    }
}