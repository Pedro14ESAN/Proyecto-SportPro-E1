package com.example.projectsportpro2026_2.ui.moderacion

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectsportpro2026_2.datos.modelo.DecisionModeracion
import com.example.projectsportpro2026_2.datos.modelo.EstadoReporte
import com.example.projectsportpro2026_2.datos.modelo.MotivoReporte
import com.example.projectsportpro2026_2.datos.modelo.Reporte
import com.example.projectsportpro2026_2.datos.modelo.TipoContenido
import com.example.projectsportpro2026_2.ui.comun.*
import com.example.projectsportpro2026_2.ui.partidos.FormaBotonSportPro
import com.example.projectsportpro2026_2.ui.partidos.FormaCampoSportPro
import com.example.projectsportpro2026_2.ui.partidos.FormaTarjetaSportPro
import com.example.projectsportpro2026_2.ui.partidos.SportProBorde
import com.example.projectsportpro2026_2.ui.partidos.SportProFondo
import com.example.projectsportpro2026_2.ui.partidos.SportProNavy
import com.example.projectsportpro2026_2.ui.partidos.SportProRojo
import com.example.projectsportpro2026_2.ui.partidos.SportProSuperficie
import com.example.projectsportpro2026_2.ui.partidos.SportProTextoPrincipal
import com.example.projectsportpro2026_2.ui.partidos.SportProTextoSecundario
import com.example.projectsportpro2026_2.ui.partidos.SportProVerde

@Composable
fun PantallaPanelModeracion(
    onVolver: () -> Unit,
    viewModel: PanelModeracionViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()
    val avisos = remember { SnackbarHostState() }

    // Reporte y decisión que el ADM está confirmando (abre el diálogo de motivo)
    var decidiendo by remember { mutableStateOf<Pair<Reporte, DecisionModeracion>?>(null) }
    var motivo by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.iniciar() }

    LaunchedEffect(estado.mensaje) {
        estado.mensaje?.let { texto ->
            avisos.showSnackbar("✓ $texto")
            viewModel.limpiarMensajes()
        }
    }

    Scaffold(
        containerColor = SportProFondo,
        snackbarHost = {
            SnackbarHost(avisos) { datos ->
                Snackbar(
                    snackbarData = datos,
                    containerColor = SportProNavy,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    ) { relleno ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            item { CabeceraSportPro() }

            item {
                Text(
                    text = "← Comunidad",
                    color = SportProVerde,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onVolver() }
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "PANEL ADMINISTRADOR",
                            color = SportProTextoSecundario,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Moderación",
                            color = SportProNavy,
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                    if (estado.cantidadPendientes > 0) {
                        EtiquetaSportPro(
                            "${estado.cantidadPendientes} pendientes",
                            EtiquetaNaranja,
                            EtiquetaNaranjaFondo
                        )
                    }
                }
            }

            item {
                FilaDeChipsSportPro {
                    FiltroReportes.entries.forEach { f ->
                        ChipSportPro(f.texto, estado.filtro == f) { viewModel.cambiarFiltro(f) }
                    }
                }
            }

            estado.error?.takeIf { decidiendo == null }?.let { texto ->
                item { MensajeErrorSportPro(texto) { viewModel.recargar() } }
            }

            when {
                estado.cargando && estado.reportes.isEmpty() -> item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = SportProVerde)
                    }
                }

                estado.reportesVisibles.isEmpty() && estado.error == null -> item {
                    Text(
                        text = if (estado.filtro == FiltroReportes.PENDIENTES) "No hay reportes pendientes 🎉"
                        else "No hay reportes en esta sección.",
                        color = SportProTextoSecundario,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }

                else -> items(estado.reportesVisibles, key = { it.id }) { reporte ->
                    TarjetaReporte(
                        reporte = reporte,
                        onDecidir = { decision ->
                            motivo = ""
                            viewModel.limpiarMensajes()
                            decidiendo = reporte to decision
                        }
                    )
                }
            }
        }
    }

    // Diálogo para confirmar la decisión con motivo obligatorio
    decidiendo?.let { (reporte, decision) ->
        AlertDialog(
            onDismissRequest = { if (!estado.procesando) decidiendo = null },
            title = { Text(decision.texto) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Escribe el motivo. Quedará registrado con tu nombre y la fecha.",
                        fontSize = 13.sp,
                        color = SportProTextoPrincipal
                    )
                    OutlinedTextField(
                        value = motivo,
                        onValueChange = { if (it.length <= 300) motivo = it },
                        placeholder = { Text("Motivo de la decisión") },
                        minLines = 2,
                        shape = FormaCampoSportPro,
                        modifier = Modifier.fillMaxWidth()
                    )
                    estado.error?.let { Text(it, color = SportProRojo, fontSize = 12.sp) }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.resolver(reporte, decision, motivo) { decidiendo = null } },
                    enabled = motivo.isNotBlank() && !estado.procesando
                ) {
                    Text("Confirmar", color = SportProVerde, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { decidiendo = null }, enabled = !estado.procesando) {
                    Text("Cancelar", color = SportProNavy)
                }
            }
        )
    }
}

// ---------- Tarjeta de reporte ----------

private fun textoTipo(tipo: TipoContenido): String = when (tipo) {
    TipoContenido.PUBLICACION -> "Publicación"
    TipoContenido.COMENTARIO -> "Comentario"
    TipoContenido.AVISO -> "Aviso de prueba"
}

private fun textoMotivos(reporte: Reporte): String =
    reporte.motivos.entries
        .sortedByDescending { it.value }
        .joinToString(" · ") { (clave, cantidad) ->
            val nombre = MotivoReporte.entries.find { it.name == clave }?.texto ?: clave
            "$nombre ×$cantidad"
        }

@Composable
private fun TarjetaReporte(
    reporte: Reporte,
    onDecidir: (DecisionModeracion) -> Unit
) {
    val pendiente = reporte.estado == EstadoReporte.PENDIENTE
    val n = reporte.cantidadReportes

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = FormaTarjetaSportPro,
        colors = CardDefaults.cardColors(containerColor = SportProSuperficie),
        border = BorderStroke(1.dp, if (pendiente) EtiquetaNaranja else SportProBorde)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                EtiquetaSportPro(textoTipo(reporte.tipoContenido), EtiquetaAzul, EtiquetaAzulFondo)
                if (pendiente) EtiquetaSportPro("PENDIENTE", EtiquetaNaranja, EtiquetaNaranjaFondo)
                else EtiquetaSportPro("RESUELTO", EtiquetaGris, EtiquetaGrisFondo)
                Spacer(Modifier.weight(1f))
                Text(
                    text = if (n == 0) "Moderación directa" else "🚩 $n ${if (n == 1) "reporte" else "reportes"}",
                    color = SportProRojo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            if (reporte.motivos.isNotEmpty()) {
                Text(textoMotivos(reporte), color = SportProTextoSecundario, fontSize = 12.sp)
            }

            // Evidencia: copia del contenido al momento del reporte
            Surface(color = SportProFondo, shape = FormaCampoSportPro, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Autor: ${reporte.nombreAutorContenido}",
                        color = SportProNavy,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(reporte.resumenContenido, color = SportProTextoPrincipal, fontSize = 13.sp)
                }
            }

            if (n > 0) {
                Text(
                    text = "Primer reporte: ${formatearFecha(reporte.fechaPrimerReporte)} · " +
                            "Último: ${formatearFecha(reporte.fechaUltimoReporte)}",
                    color = SportProTextoSecundario,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                )
            }

            // Última decisión registrada
            reporte.decision?.let { d ->
                Surface(color = EtiquetaGrisFondo, shape = FormaCampoSportPro, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "${d.texto} por ${reporte.nombreModerador}",
                            color = SportProNavy,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text("Motivo: ${reporte.motivoDecision}", color = SportProTextoPrincipal, fontSize = 12.sp)
                        Text(formatearFecha(reporte.fechaDecision), color = SportProTextoSecundario, fontSize = 10.sp)
                    }
                }
            }

            if (pendiente) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { onDecidir(DecisionModeracion.DESCARTADO) },
                        shape = FormaBotonSportPro,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Descartar", color = SportProNavy, fontSize = 12.sp)
                    }
                    Button(
                        onClick = { onDecidir(DecisionModeracion.OCULTADO) },
                        shape = FormaBotonSportPro,
                        colors = ButtonDefaults.buttonColors(containerColor = SportProRojo),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Ocultar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    if (reporte.tipoContenido == TipoContenido.AVISO) {
                        Button(
                            onClick = { onDecidir(DecisionModeracion.CERRADO) },
                            shape = FormaBotonSportPro,
                            colors = ButtonDefaults.buttonColors(containerColor = EtiquetaNaranja),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cerrar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}