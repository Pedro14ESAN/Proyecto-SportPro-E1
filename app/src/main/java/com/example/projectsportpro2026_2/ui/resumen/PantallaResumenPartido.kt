package com.example.projectsportpro2026_2.ui.resumen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectsportpro2026_2.datos.modelo.evento.EquipoEvento
import com.example.projectsportpro2026_2.datos.modelo.evento.EventoPartido
import com.example.projectsportpro2026_2.datos.modelo.partido.EstadoPartido
import com.example.projectsportpro2026_2.datos.modelo.resumen.EstadoResumen
import com.example.projectsportpro2026_2.datos.modelo.resumen.EvaluacionResumen
import com.example.projectsportpro2026_2.datos.modelo.resumen.OrigenResumen
import com.example.projectsportpro2026_2.datos.modelo.resumen.ResumenPartido
import com.example.projectsportpro2026_2.datos.modelo.resumen.TipoVersionResumen
import com.example.projectsportpro2026_2.datos.modelo.resumen.VersionResumen
import com.example.projectsportpro2026_2.ui.comun.CabeceraSportPro
import com.example.projectsportpro2026_2.ui.comun.EtiquetaGris
import com.example.projectsportpro2026_2.ui.comun.EtiquetaGrisFondo
import com.example.projectsportpro2026_2.ui.comun.EtiquetaSportPro
import com.example.projectsportpro2026_2.ui.comun.MensajeErrorSportPro
import com.example.projectsportpro2026_2.ui.comun.TituloCampoSportPro
import com.example.projectsportpro2026_2.ui.comun.formatearFecha
import com.example.projectsportpro2026_2.ui.estadisticas.AmarilloAviso
import com.example.projectsportpro2026_2.ui.estadisticas.AmarilloAvisoBorde
import com.example.projectsportpro2026_2.ui.estadisticas.AmarilloAvisoFondo
import com.example.projectsportpro2026_2.ui.estadisticas.EncabezadoSeccion
import com.example.projectsportpro2026_2.ui.estadisticas.TarjetaBlanca
import com.example.projectsportpro2026_2.ui.estadisticas.VerdeFigma
import com.example.projectsportpro2026_2.ui.partidos.FormaBotonSportPro
import com.example.projectsportpro2026_2.ui.partidos.FormaCampoSportPro
import com.example.projectsportpro2026_2.ui.partidos.SportProBorde
import com.example.projectsportpro2026_2.ui.partidos.SportProFondo
import com.example.projectsportpro2026_2.ui.partidos.SportProNavy
import com.example.projectsportpro2026_2.ui.partidos.SportProRojo
import com.example.projectsportpro2026_2.ui.partidos.SportProRojoClaro
import com.example.projectsportpro2026_2.ui.partidos.SportProTextoSecundario
import com.example.projectsportpro2026_2.ui.partidos.SportProVerde
import com.example.projectsportpro2026_2.ui.partidos.SportProVerdeClaro
import com.example.projectsportpro2026_2.ui.sincronizacion.simboloEvento

/*
 * HU26 / HU27 - "Resumen del partido".
 *
 * DT autorizado: genera el borrador, lo revisa junto a la
 * cronología fuente, lo edita, completa la evaluación y lo
 * aprueba o rechaza. Los demás roles solo ven el resumen
 * APROBADO (nunca un borrador).
 */
@Composable
fun PantallaResumenPartido(
    idPartido: String,
    onVolver: () -> Unit,
    viewModel: ResumenPartidoViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()
    val avisos = remember { SnackbarHostState() }

    var verFuentes by remember { mutableStateOf(false) }
    var verHistorial by remember { mutableStateOf(false) }
    var rechazando by remember { mutableStateOf(false) }
    var motivoRechazo by remember { mutableStateOf("") }

    LaunchedEffect(idPartido) { viewModel.iniciar(idPartido) }

    LaunchedEffect(estado.mensaje) {
        estado.mensaje?.let {
            avisos.showSnackbar(it)
            viewModel.limpiarMensaje()
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { CabeceraSportPro() }

            val contexto = estado.contexto
            val m = estado.marcador
            item {
                EncabezadoSeccion(
                    textoVolver = "Partido",
                    onVolver = onVolver,
                    overline = if (contexto == null) "RESUMEN NARRATIVO"
                    else "${contexto.equipo.nombre.uppercase()} ${m.propio} — ${m.rival} " +
                            contexto.partido.nombreRival.uppercase(),
                    titulo = "Resumen del partido"
                )
            }

            estado.error?.let { texto ->
                item {
                    MensajeErrorSportPro(texto) {
                        if (contexto == null) viewModel.reintentar() else viewModel.generar()
                    }
                }
                if (estado.iaNoDisponible && estado.puedeRevisar) {
                    item {
                        OutlinedButton(
                            onClick = { viewModel.generarSinIA() },
                            enabled = !estado.procesando,
                            shape = FormaBotonSportPro,
                            border = BorderStroke(1.dp, SportProBorde),
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
                            Text("Generar borrador sin IA (solo hechos)", color = SportProNavy,
                                fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            if (estado.cargando) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = SportProVerde)
                    }
                }
                return@LazyColumn
            }
            if (contexto == null) return@LazyColumn

            val resumen = estado.resumen
            val puedeRevisar = estado.puedeRevisar

            when {
                // ---------- Lectores (JUG / PAD / ADM sin ser DT) ----------
                !puedeRevisar -> item {
                    if (resumen?.estado == EstadoResumen.APROBADO) {
                        TarjetaAprobado(resumen)
                    } else {
                        TarjetaBlanca {
                            Text("Resumen aún no disponible", color = SportProNavy,
                                fontWeight = FontWeight.Bold)
                            Text(
                                if (contexto.puedeVerBorrador && resumen != null)
                                    "Hay un resumen en estado ${nombreEstado(resumen.estado)}. " +
                                            "Solo se publica cuando el DT lo aprueba."
                                else
                                    "El resumen se muestra cuando el director técnico lo revisa y aprueba.",
                                color = SportProTextoSecundario, fontSize = 13.sp
                            )
                        }
                    }
                }

                // ---------- DT: sin resumen, rechazado o desactualizado ----------
                resumen == null ||
                        resumen.estado == EstadoResumen.RECHAZADO ||
                        resumen.estado == EstadoResumen.DESACTUALIZADO -> {
                    if (resumen?.estado == EstadoResumen.DESACTUALIZADO) {
                        item {
                            AvisoEstado(
                                titulo = "Resumen desactualizado",
                                texto = resumen.motivoDesactualizado.ifBlank {
                                    "Se corrigió o anuló un evento después de la aprobación."
                                } + " Genera un nuevo borrador y vuelve a revisarlo.",
                                fondo = SportProRojoClaro, color = SportProRojo
                            )
                        }
                    }
                    if (resumen?.estado == EstadoResumen.RECHAZADO) {
                        item {
                            AvisoEstado(
                                titulo = "Borrador v${resumen.version} rechazado",
                                texto = "Motivo: ${resumen.motivoRechazo}",
                                fondo = SportProRojoClaro, color = SportProRojo
                            )
                        }
                    }
                    item { BloqueGenerar(estado, onGenerar = viewModel::generar) }
                }

                // ---------- DT: aprobado ----------
                resumen.estado == EstadoResumen.APROBADO -> {
                    item { TarjetaAprobado(resumen) }
                    item {
                        Text(
                            "Al aprobar confirmaste que revisaste los eventos fuente. " +
                                    "Si se corrige un evento, el resumen pasará a desactualizado.",
                            color = SportProTextoSecundario, fontSize = 12.sp
                        )
                    }
                }

                // ---------- DT: borrador en revisión (HU27) ----------
                else -> {
                    item {
                        TarjetaBorrador(
                            resumen = resumen,
                            totalValidos = estado.eventosValidos.size,
                            onVerFuentes = { verFuentes = !verFuentes }
                        )
                    }
                    if (estado.borradorDesfasado) {
                        item {
                            AvisoEstado(
                                titulo = "Los eventos cambiaron",
                                texto = "Se registró, corrigió o anuló un evento después de generar " +
                                        "este borrador. Recházalo y genera uno nuevo.",
                                fondo = SportProRojoClaro, color = SportProRojo
                            )
                        }
                    }
                    if (verFuentes) {
                        item { TituloCampoSportPro("CRONOLOGÍA FUENTE · EVENTOS VÁLIDOS") }
                        items(estado.eventosValidos, key = { it.id }) { e ->
                            FilaEventoFuente(e, contexto.equipo.nombre, contexto.partido.nombreRival)
                        }
                    }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            TituloCampoSportPro("TEXTO FINAL (EDITABLE)")
                            OutlinedTextField(
                                value = estado.textoEditado,
                                onValueChange = { if (it.length <= 4000) viewModel.editarTexto(it) },
                                minLines = 8,
                                shape = FormaCampoSportPro,
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (estado.hayCambiosSinGuardar) {
                                TextButton(onClick = { viewModel.guardarBorrador() },
                                    enabled = !estado.procesando) {
                                    Text("Guardar cambios del borrador", color = SportProVerde,
                                        fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    item {
                        FormularioEvaluacion(
                            evaluacion = estado.evaluacion,
                            onCambiar = viewModel::editarEvaluacion
                        )
                    }
                    item {
                        Button(
                            onClick = { viewModel.aprobar() },
                            enabled = !estado.procesando && !estado.borradorDesfasado &&
                                    estado.evaluacion.utilidad in 1..5,
                            shape = FormaBotonSportPro,
                            colors = ButtonDefaults.buttonColors(containerColor = VerdeFigma),
                            modifier = Modifier.fillMaxWidth().height(54.dp)
                        ) {
                            if (estado.procesando) {
                                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp,
                                    modifier = Modifier.size(20.dp))
                            } else {
                                Text("Aprobar resumen", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                    item {
                        Text(
                            "Al aprobar confirmas que revisaste los eventos fuente.",
                            color = SportProTextoSecundario, fontSize = 12.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedButton(
                            onClick = { rechazando = true },
                            enabled = !estado.procesando,
                            shape = FormaBotonSportPro,
                            border = BorderStroke(1.dp, SportProRojo),
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
                            Text("Rechazar y pedir nueva generación", color = SportProRojo,
                                fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Historial de versiones (DT o ADM)
            if (contexto.puedeVerBorrador && estado.historial.isNotEmpty()) {
                item {
                    Text(
                        text = (if (verHistorial) "▾ " else "▸ ") +
                                "Historial de versiones (${estado.historial.size})",
                        color = SportProNavy, fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { verHistorial = !verHistorial }
                    )
                }
                if (verHistorial) {
                    items(estado.historial, key = { it.id }) { v -> FilaVersion(v) }
                }
            }

            if (contexto.partido.estado != EstadoPartido.FINALIZADO) {
                item {
                    Text(
                        "El resumen se habilita cuando el partido está FINALIZADO.",
                        color = SportProTextoSecundario, fontSize = 12.sp
                    )
                }
            }
        }
    }

    if (rechazando) {
        AlertDialog(
            onDismissRequest = { rechazando = false },
            title = { Text("Rechazar borrador") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Se conserva en el historial. Indica qué estaba mal:", fontSize = 13.sp)
                    OutlinedTextField(
                        value = motivoRechazo,
                        onValueChange = { if (it.length <= 300) motivoRechazo = it },
                        minLines = 3,
                        shape = FormaCampoSportPro
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.rechazar(motivoRechazo)
                        rechazando = false
                        motivoRechazo = ""
                    },
                    enabled = motivoRechazo.trim().length >= 5
                ) { Text("Rechazar", color = SportProRojo, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { rechazando = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun BloqueGenerar(estado: EstadoResumenPartido, onGenerar: () -> Unit) {
    val contexto = estado.contexto ?: return
    val finalizado = contexto.partido.estado == EstadoPartido.FINALIZADO
    val juego = estado.eventosValidos.count { it.codigoTipoEvento != "INICIO" && it.codigoTipoEvento != "FIN" }

    TarjetaBlanca {
        Text("Genera el borrador con IA", color = SportProNavy, fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            "La IA solo usa los ${estado.eventosValidos.size} eventos válidos registrados " +
                    "(sin anulados y con la versión vigente de los corregidos). " +
                    "No recibe datos personales sensibles y no puede inventar jugadas.",
            color = SportProTextoSecundario, fontSize = 13.sp
        )
        if (estado.pendientesSync) {
            Spacer(Modifier.height(8.dp))
            Text("⚠ Hay eventos pendientes de sincronizar: espera la sincronización.",
                color = SportProRojo, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        if (finalizado && juego < 2) {
            Spacer(Modifier.height(8.dp))
            Text("⚠ Eventos insuficientes: no se puede generar un resumen fiable.",
                color = SportProRojo, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onGenerar,
            enabled = finalizado && !estado.procesando && !estado.pendientesSync && juego >= 2,
            shape = FormaBotonSportPro,
            colors = ButtonDefaults.buttonColors(containerColor = VerdeFigma),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            if (estado.procesando) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text("Generando…")
            } else {
                Text("✨ Generar resumen", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun TarjetaBorrador(
    resumen: ResumenPartido,
    totalValidos: Int,
    onVerFuentes: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = AmarilloAvisoFondo,
        border = BorderStroke(1.dp, AmarilloAvisoBorde)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                EtiquetaSportPro("BORRADOR · REQUIERE REVISIÓN DT", AmarilloAviso, Color(0xFFFFE7A3))
                EtiquetaSportPro(
                    if (resumen.origen == OrigenResumen.IA) "IA · v${resumen.version}"
                    else "SIN IA · v${resumen.version}",
                    EtiquetaGris, EtiquetaGrisFondo
                )
            }
            Text(
                resumen.textoOriginalIA,
                color = SportProNavy, fontSize = 14.sp, lineHeight = 20.sp
            )
            Text(
                text = "Eventos utilizados: ${resumen.totalEventos} · Ver fuentes",
                color = AmarilloAviso, fontWeight = FontWeight.Bold, fontSize = 12.sp,
                modifier = Modifier.clickable { onVerFuentes() }
            )
            Text(
                "Generado ${formatearFecha(resumen.fechaGeneracion)}" +
                        (if (resumen.modeloIA.isNotBlank()) " · ${resumen.modeloIA}" else "") +
                        " · eventos válidos ahora: $totalValidos",
                color = AmarilloAviso, fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun TarjetaAprobado(resumen: ResumenPartido) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = SportProVerdeClaro,
        border = BorderStroke(1.dp, VerdeFigma.copy(alpha = 0.4f))
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("✓ Resumen aprobado", color = VerdeFigma, fontWeight = FontWeight.ExtraBold)
            Text(resumen.textoFinal, color = SportProNavy, fontSize = 14.sp, lineHeight = 20.sp)
            Text(
                "Aprobado por ${resumen.nombreAprobador.ifBlank { "el DT" }} · " +
                        formatearFecha(resumen.fechaAprobacion) +
                        " · ${resumen.totalEventos} eventos fuente",
                color = SportProTextoSecundario, fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun AvisoEstado(titulo: String, texto: String, fondo: Color, color: Color) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = fondo) {
        Column(Modifier.padding(14.dp)) {
            Text(titulo, color = color, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(texto, color = color, fontSize = 12.sp)
        }
    }
}

@Composable
private fun FilaEventoFuente(e: EventoPartido, nombreEquipo: String, nombreRival: String) {
    TarjetaBlanca {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("${e.minuto}'", color = SportProNavy, fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold, modifier = Modifier.width(44.dp))
            Text(simboloEvento(e.codigoTipoEvento, e.nombreTipoEvento, e.observacion),
                modifier = Modifier.width(28.dp))
            Column(Modifier.weight(1f)) {
                Text(e.nombreTipoEvento.ifBlank { e.codigoTipoEvento }, color = SportProNavy,
                    fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                val detalle = listOfNotNull(
                    e.nombreJugador.takeIf { it.isNotBlank() },
                    when (e.equipoEvento) {
                        EquipoEvento.PROPIO -> nombreEquipo
                        EquipoEvento.RIVAL -> nombreRival
                        EquipoEvento.NINGUNO -> null
                    },
                    e.observacion.takeIf { it.isNotBlank() }
                ).joinToString(" · ")
                if (detalle.isNotBlank()) {
                    Text(detalle, color = SportProTextoSecundario, fontSize = 12.sp)
                }
            }
            if (e.version > 1) {
                EtiquetaSportPro("CORREGIDO v${e.version}", EtiquetaGris, EtiquetaGrisFondo)
            }
        }
    }
}

@Composable
private fun FormularioEvaluacion(
    evaluacion: EvaluacionResumen,
    onCambiar: (EvaluacionResumen) -> Unit
) {
    TarjetaBlanca {
        Text("EVALUACIÓN DEL RESUMEN", color = SportProTextoSecundario,
            fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Utilidad percibida", color = SportProNavy, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            (1..5).forEach { n ->
                val activo = n == evaluacion.utilidad
                Surface(
                    onClick = { onCambiar(evaluacion.copy(utilidad = n)) },
                    shape = RoundedCornerShape(10.dp),
                    color = if (activo) SportProNavy else SportProVerdeClaro,
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("$n", color = if (activo) Color.White else SportProNavy,
                            fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        CampoEvaluacion("Omisiones (eventos importantes que faltaron)", evaluacion.omisiones) {
            onCambiar(evaluacion.copy(omisiones = it))
        }
        CampoEvaluacion("Errores o información inventada", evaluacion.erroresOInvenciones) {
            onCambiar(evaluacion.copy(erroresOInvenciones = it))
        }
        CampoEvaluacion("Correcciones realizadas", evaluacion.correccionesRealizadas) {
            onCambiar(evaluacion.copy(correccionesRealizadas = it))
        }
    }
}

@Composable
private fun CampoEvaluacion(etiqueta: String, valor: String, onCambiar: (String) -> Unit) {
    OutlinedTextField(
        value = valor,
        onValueChange = { if (it.length <= 500) onCambiar(it) },
        label = { Text(etiqueta, fontSize = 12.sp) },
        minLines = 2,
        shape = FormaCampoSportPro,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )
}

@Composable
private fun FilaVersion(v: VersionResumen) {
    TarjetaBlanca {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("v${v.version} · ${nombreTipoVersion(v.tipo)}", color = SportProNavy,
                fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(formatearFecha(v.fecha), color = SportProTextoSecundario, fontSize = 11.sp)
        }
        if (v.nota.isNotBlank()) {
            Text(v.nota, color = SportProTextoSecundario, fontSize = 12.sp)
        }
        Text(v.texto, color = SportProNavy, fontSize = 12.sp, maxLines = 4)
    }
}

private fun nombreEstado(e: EstadoResumen) = when (e) {
    EstadoResumen.BORRADOR -> "borrador"
    EstadoResumen.APROBADO -> "aprobado"
    EstadoResumen.RECHAZADO -> "rechazado"
    EstadoResumen.DESACTUALIZADO -> "desactualizado"
}

private fun nombreTipoVersion(t: TipoVersionResumen) = when (t) {
    TipoVersionResumen.GENERADA -> "Generada"
    TipoVersionResumen.EDITADA -> "Editada"
    TipoVersionResumen.APROBADA -> "Aprobada"
    TipoVersionResumen.RECHAZADA -> "Rechazada"
    TipoVersionResumen.DESACTUALIZADA -> "Desactualizada"
}
