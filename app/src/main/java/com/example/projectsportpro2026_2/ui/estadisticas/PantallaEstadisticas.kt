package com.example.projectsportpro2026_2.ui.estadisticas

import com.example.projectsportpro2026_2.ui.comun.TarjetaTicket
import com.example.projectsportpro2026_2.datos.estadisticas.PartidoJugador
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import androidx.core.content.FileProvider
import java.io.File
import android.graphics.Bitmap
import android.content.Intent
import android.content.Context
import com.example.projectsportpro2026_2.ui.comun.CromoJugador
import androidx.compose.material.icons.rounded.PanTool
import androidx.compose.material.icons.rounded.HowToReg
import androidx.compose.material.icons.rounded.CropPortrait
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.SportsSoccer
import androidx.compose.material.icons.Icons
import com.example.projectsportpro2026_2.ui.theme.FamiliaEtiquetas
import com.example.projectsportpro2026_2.ui.theme.FamiliaTitulos
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectsportpro2026_2.datos.estadisticas.EstadisticasEquipo
import com.example.projectsportpro2026_2.datos.estadisticas.EstadisticasJugador
import com.example.projectsportpro2026_2.datos.estadisticas.SesionPeriodo
import com.example.projectsportpro2026_2.datos.modelo.PoliticaEdad
import com.example.projectsportpro2026_2.datos.seguridad.NivelAcceso
import com.example.projectsportpro2026_2.ui.comun.AvatarIniciales
import com.example.projectsportpro2026_2.ui.comun.CabeceraSportPro
import com.example.projectsportpro2026_2.ui.comun.ChipSportPro
import com.example.projectsportpro2026_2.ui.comun.EtiquetaAzul
import com.example.projectsportpro2026_2.ui.comun.EtiquetaAzulFondo
import com.example.projectsportpro2026_2.ui.comun.EtiquetaNaranja
import com.example.projectsportpro2026_2.ui.comun.EtiquetaNaranjaFondo
import com.example.projectsportpro2026_2.ui.comun.EtiquetaSportPro
import com.example.projectsportpro2026_2.ui.comun.FilaDeChipsSportPro
import com.example.projectsportpro2026_2.ui.comun.MensajeErrorSportPro
import com.example.projectsportpro2026_2.ui.comun.TituloCampoSportPro
import com.example.projectsportpro2026_2.ui.partidos.SportProFondo
import com.example.projectsportpro2026_2.ui.partidos.SportProNavy
import com.example.projectsportpro2026_2.ui.partidos.SportProRojo
import com.example.projectsportpro2026_2.ui.partidos.SportProTextoSecundario
import com.example.projectsportpro2026_2.ui.partidos.SportProVerde
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/*
 * HU18 - Pantalla "Estadísticas" (DT / JUG / PAD / ADM).
 * Pestañas Equipo, Jugador y Periodo, como en el Figma.
 * Solo lectura: todo se calcula con eventos válidos.
 */
@Composable
fun PantallaEstadisticas(
    onVolver: () -> Unit,
    viewModel: EstadisticasViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()

    LaunchedEffect(Unit) { viewModel.iniciar() }

    Scaffold(containerColor = SportProFondo) { relleno ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            item { CabeceraSportPro() }

            item {
                EncabezadoSeccion(
                    textoVolver = "Inicio",
                    onVolver = onVolver,
                    overline = "RENDIMIENTO",
                    titulo = "Estadísticas",
                    subtitulo = "Acumulados de equipo y jugadores con los partidos registrados en SportPro."
                )
            }

            item {
                PestanasPill(
                    opciones = listOf("Equipo", "Jugador", "Periodo"),
                    seleccionada = estado.pestana.ordinal,
                    onSeleccionar = { viewModel.seleccionarPestana(PestanaEstadisticas.entries[it]) }
                )
            }

            estado.error?.let { texto ->
                item { MensajeErrorSportPro(texto) { viewModel.reintentar() } }
            }

            if (estado.cargando) {
                item { Cargando() }
                return@LazyColumn
            }

            if (estado.equipos.isEmpty() && estado.error == null) {
                item {
                    TarjetaBlanca {
                        Text("Sin equipos", color = SportProNavy, fontWeight = FontWeight.Bold)
                        Text(
                            "Aún no estás relacionado con un equipo. Las estadísticas aparecen " +
                                    "cuando perteneces a un equipo o academia.",
                            color = SportProTextoSecundario,
                            fontSize = 13.sp
                        )
                    }
                }
                return@LazyColumn
            }

            // Selector de equipo
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TituloCampoSportPro("EQUIPO")
                    FilaDeChipsSportPro {
                        estado.equipos.forEach { equipo ->
                            ChipSportPro(
                                texto = if (equipo.categoria.isBlank()) equipo.nombre
                                else "${equipo.nombre} · ${equipo.categoria}",
                                seleccionado = equipo.id == estado.idEquipo
                            ) { viewModel.seleccionarEquipo(equipo.id) }
                        }
                    }
                }
            }

            // Selector de periodo (todas las pestañas lo respetan)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TituloCampoSportPro("PERIODO")
                    FilaDeChipsSportPro {
                        estado.periodos.forEach { p ->
                            ChipSportPro(p.etiqueta, p.clave == estado.periodo.clave) {
                                viewModel.seleccionarPeriodo(p)
                            }
                        }
                    }
                }
            }

            if (estado.cargandoEquipo || estado.datos == null) {
                if (estado.error == null) item { Cargando() }
                return@LazyColumn
            }

            when (estado.pestana) {
                PestanaEstadisticas.EQUIPO -> {
                    val r = estado.resumenEquipo ?: EstadisticasEquipo()
                    item {
                        TarjetaEquipo(
                            nombre = estado.equipo?.nombre.orEmpty(),
                            categoria = estado.equipo?.categoria.orEmpty(),
                            r = r
                        )
                    }
                    item { MetricasEquipo(r) }
                }

                PestanaEstadisticas.JUGADOR -> {
                    if (estado.jugadores.isEmpty()) {
                        item {
                            TarjetaBlanca {
                                Text("Sin jugadores visibles", color = SportProNavy,
                                    fontWeight = FontWeight.Bold)
                                Text(
                                    "No hay jugadores activos en este equipo que puedas consultar.",
                                    color = SportProTextoSecundario, fontSize = 13.sp
                                )
                            }
                        }
                    } else {
                        item {
                            FilaDeChipsSportPro {
                                estado.jugadores.forEach { j ->
                                    ChipSportPro(
                                        texto = j.jugador.nombres.ifBlank { j.jugador.nombreCompleto() },
                                        seleccionado = j.jugador.id == estado.jugadorSeleccionado?.jugador?.id
                                    ) { viewModel.seleccionarJugador(j.jugador.id) }
                                }
                            }
                        }
                        val seleccionado = estado.jugadorSeleccionado
                        val r = estado.resumenJugador
                        if (seleccionado != null && r != null) {
                            item {
                                SeccionJugador(
                                    nombre = seleccionado.jugador.nombreCompleto(),
                                    posicion = seleccionado.jugador.posicion,
                                    fotoUrl = seleccionado.jugador.fotoUrl,
                                    nombreEquipo = estado.equipo?.nombre.orEmpty(),
                                    etiquetaPeriodo = estado.periodo.etiqueta,
                                    fechaNacimiento = seleccionado.jugador.fechaNacimiento,
                                    esMenor = seleccionado.jugador.esMenorDeEdad(),
                                    completo = seleccionado.nivel == NivelAcceso.COMPLETO,
                                    hayPartidos = (estado.resumenEquipo?.partidosJugados ?: 0) > 0,
                                    r = r
                                )
                            }
                        }
                    }
                }

                PestanaEstadisticas.PERIODO -> {
                    val r = estado.resumenEquipo ?: EstadisticasEquipo()
                    item { MetricasPeriodo(r) }
                    item { TituloCampoSportPro("SESIONES DEL PERIODO") }
                    val sesiones = estado.sesionesPeriodo
                    if (sesiones.isEmpty()) {
                        item {
                            Text("Sin sesiones de entrenamiento en este periodo.",
                                color = SportProTextoSecundario, fontSize = 13.sp)
                        }
                    } else {
                        items(sesiones, key = { it.idEntrenamiento }) { s ->
                            FilaSesion(s, mostrarAsistencia = estado.datos?.asistenciaCompleta == true)
                        }
                    }
                }
            }

            item { PieSoloLectura("SOLO LECTURA · DATOS BASADOS EN EVENTOS VÁLIDOS") }
        }
    }
}

@Composable
private fun Cargando() {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) { CircularProgressIndicator(color = SportProVerde) }
}

@Composable
private fun TarjetaEquipo(nombre: String, categoria: String, r: EstadisticasEquipo) {
    TarjetaBlanca {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AvatarIniciales(nombre, oscuro = true, tamano = 46.dp)
            Spacer(Modifier.padding(6.dp))
            Column(Modifier.weight(1f)) {
                Text(nombre, color = SportProNavy, fontFamily = FamiliaTitulos,
                    fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                if (categoria.isNotBlank()) {
                    Text(categoria, color = SportProTextoSecundario, fontSize = 12.sp)
                }
            }
            if (r.hayPartidos) {
                EtiquetaSportPro("${r.ganados}G · ${r.empatados}E · ${r.perdidos}P",
                    EtiquetaAzul, EtiquetaAzulFondo)
            }
        }
        if (r.eventosAnuladosExcluidos > 0) {
            Spacer(Modifier.height(8.dp))
            Text(
                "${r.eventosAnuladosExcluidos} evento(s) anulado(s) excluido(s) del cálculo",
                color = SportProTextoSecundario, fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun MetricasEquipo(r: EstadisticasEquipo) {
    val sinPartidos = !r.hayPartidos
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FilaMetricas {
            TarjetaMetrica("Partidos jugados", r.partidosJugados.toString(), icono = Icons.Rounded.SportsSoccer)
            TarjetaMetrica("Sesiones entrenamiento", r.sesionesEntrenamiento.toString(), icono = Icons.AutoMirrored.Rounded.DirectionsRun)
        }
        FilaMetricas {
            TarjetaMetrica(
                titulo = "Goles a favor",
                valor = if (sinPartidos) null else r.golesFavor.toString(),
                detalle = if (sinPartidos) null else "${r.golesContra} en contra",
                colorValor = SportProVerde
            )
            TarjetaMetrica(
                titulo = "Tarjetas amarillas",
                valor = if (sinPartidos) null else r.tarjetasAmarillas.toString(),
                detalle = if (sinPartidos) null
                else "${r.tarjetasRojas} rojas · ${r.cambios} cambios",
                icono = Icons.Rounded.CropPortrait
            )
        }
        TarjetaBlanca {
            Text("BALANCE DE GOLES", color = SportProTextoSecundario,
                fontFamily = FamiliaEtiquetas, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            if (sinPartidos) {
                Text("Sin datos: aún no hay partidos finalizados en este periodo.",
                    color = SportProTextoSecundario, fontSize = 13.sp)
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("A favor ${r.golesFavor}", color = SportProVerde, fontWeight = FontWeight.Bold)
                    Text(
                        (if (r.balanceGoles > 0) "+" else "") + r.balanceGoles,
                        color = SportProNavy, fontWeight = FontWeight.ExtraBold
                    )
                    Text("En contra ${r.golesContra}", color = SportProRojo, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
                BarraBalance(r.golesFavor, r.golesContra)
            }
        }
    }
}

@Composable
private fun SeccionJugador(
    nombre: String,
    posicion: String,
    fotoUrl: String,
    nombreEquipo: String,
    etiquetaPeriodo: String,
    fechaNacimiento: String,
    esMenor: Boolean,
    completo: Boolean,
    hayPartidos: Boolean,
    r: EstadisticasJugador
) {
    // 7.3 (opcional): el cromo se dibuja también en una capa para poder compartirlo como imagen
    val capaCromo = rememberGraphicsLayer()
    val alcance = rememberCoroutineScope()
    val contexto = LocalContext.current

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Línea de cal (6.11, img 01): cromo grande con la foto (fotoUrl) o la silueta (7.3)
        CromoJugador(
            nombre = nombre,
            posicion = posicion,
            fotoUrl = fotoUrl,
            equipo = nombreEquipo,
            estado = etiquetaPeriodo,
            modifier = Modifier
                .fillMaxWidth()
                .drawWithContent {
                    capaCromo.record { this@drawWithContent.drawContent() }
                    drawLayer(capaCromo)
                },
            insignia = {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // La edad es dato sensible: solo con acceso completo
                    if (completo) {
                        edad(fechaNacimiento)?.let {
                            EtiquetaSportPro("$it AÑOS", EtiquetaAzul, EtiquetaAzulFondo)
                        }
                    }
                    if (esMenor) {
                        EtiquetaSportPro("MENOR", EtiquetaNaranja, EtiquetaNaranjaFondo)
                    }
                }
            }
        )

        FilaMetricas {
            TarjetaMetrica("Participaciones", if (hayPartidos) r.participaciones.toString() else null,
                icono = Icons.Rounded.HowToReg)
            TarjetaMetrica("Goles", if (hayPartidos) r.goles.toString() else null,
                icono = Icons.Rounded.SportsSoccer, colorValor = SportProVerde)
            // 7.3: minutos jugados (solo partidos con alineación confirmada; "—" si no hay)
            TarjetaMetrica("Minutos", if (hayPartidos) (r.minutosJugados?.toString() ?: "—") else null,
                icono = Icons.Rounded.Timer)
        }

        if (r.partidos.isNotEmpty()) {
            GolesPorFecha(r.partidos)
            UltimoPartido(r.partidos.size, r.partidos.last())
        }

        if (completo) {
            TarjetaBlanca {
                Text("ASISTENCIA A ENTRENAMIENTOS", color = SportProTextoSecundario,
                    fontFamily = FamiliaEtiquetas, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                val porcentaje = r.asistencia.porcentaje
                if (porcentaje == null) {
                    Text("Sin datos", color = SportProTextoSecundario, fontWeight = FontWeight.SemiBold)
                } else {
                    Text("$porcentaje %", color = SportProNavy, fontFamily = FamiliaTitulos,
                        fontWeight = FontWeight.ExtraBold, fontSize = 26.sp)
                    Spacer(Modifier.height(6.dp))
                    BarraProporcion(porcentaje / 100f)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Presente ${r.asistencia.presente}", color = SportProVerde, fontSize = 12.sp)
                        Text("Ausente ${r.asistencia.ausente}", color = SportProRojo, fontSize = 12.sp)
                        Text("Justificado ${r.asistencia.justificado}", color = SportProTextoSecundario,
                            fontSize = 12.sp)
                    }
                }
            }

            FilaMetricas {
                TarjetaMetrica("T. amarillas", if (hayPartidos) r.tarjetasAmarillas.toString() else null,
                    icono = Icons.Rounded.CropPortrait)
                TarjetaMetrica("T. rojas", if (hayPartidos) r.tarjetasRojas.toString() else null,
                    icono = Icons.Rounded.CropPortrait)
                TarjetaMetrica("Faltas", if (hayPartidos) r.faltas.toString() else null, icono = Icons.Rounded.PanTool)
            }
        }

        if (esMenor || !completo) {
            AvisoAmarillo(
                titulo = if (esMenor) "Datos protegidos: menor de edad" else "Datos protegidos",
                texto = if (completo)
                    "Ves la información completa porque eres su DT, apoderado, administrador o el propio jugador."
                else
                    "Solo el DT, el apoderado vinculado y el propio jugador ven asistencia, disciplina y edad."
            )
        }

        // 7.3 (opcional): compartir el cromo como imagen. Solo lo ve quien ya puede ver al jugador.
        OutlinedButton(
            onClick = {
                alcance.launch {
                    runCatching {
                        val imagen = capaCromo.toImageBitmap().asAndroidBitmap()
                        compartirCromo(contexto, imagen, nombre)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Rounded.Share, contentDescription = null, tint = SportProNavy, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Compartir cromo", color = SportProNavy, fontWeight = FontWeight.SemiBold)
        }
    }
}

/** 7.3 · Un balón por gol en cada partido finalizado y "—" cuando no hubo. */
@Composable
private fun GolesPorFecha(partidos: List<PartidoJugador>) {
    TarjetaBlanca {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Goles por fecha",
                color = SportProNavy,
                fontFamily = FamiliaTitulos,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
            Text("— SIN GOLES", color = SportProTextoSecundario, fontFamily = FamiliaEtiquetas, fontSize = 12.sp)
        }
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            partidos.forEachIndexed { i, p ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.semantics(mergeDescendants = true) {
                        contentDescription = "Fecha ${i + 1}, vs ${p.partido.nombreRival}: " +
                            if (p.goles == 0) "sin goles" else "${p.goles} " + if (p.goles == 1) "gol" else "goles"
                    }
                ) {
                    if (p.goles == 0) {
                        Text("—", color = SportProTextoSecundario, fontSize = 16.sp)
                    } else {
                        repeat(p.goles) {
                            Icon(
                                Icons.Rounded.SportsSoccer,
                                contentDescription = null,
                                tint = SportProVerde,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("F${i + 1}", color = SportProTextoSecundario, fontFamily = FamiliaEtiquetas, fontSize = 12.sp)
                }
            }
        }
    }
}

/** 7.3 · Último partido: resultado, goles, minutos y si fue titular. */
@Composable
private fun UltimoPartido(numero: Int, p: PartidoJugador) {
    TarjetaTicket(
        hora = "${p.golesPropios}:${p.golesRival}",
        fecha = "F$numero"
    ) {
        Text(
            "vs ${p.partido.nombreRival}" + when (p.goles) {
                0 -> ""
                1 -> " · 1 gol"
                else -> " · ${p.goles} goles"
            },
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
        Text(
            listOf(
                p.minutos?.let { "$it min" } ?: "— min",
                when (p.titular) {
                    true -> "titular"
                    false -> "suplente"
                    null -> "sin alineación confirmada"
                }
            ).joinToString(" · "),
            color = SportProTextoSecundario,
            fontSize = 13.sp
        )
    }
}

/** Guarda el cromo en la caché de la app y abre el selector de Android para compartirlo. */
private suspend fun compartirCromo(contexto: Context, imagen: Bitmap, nombre: String) {
    val archivo = withContext(Dispatchers.IO) {
        val carpeta = File(contexto.cacheDir, "compartidos").apply { mkdirs() }
        File(carpeta, "cromo.png").also { destino ->
            destino.outputStream().use { imagen.compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
    }
    val uri = FileProvider.getUriForFile(contexto, "${contexto.packageName}.archivos", archivo)
    val intento = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_TEXT, "Cromo de $nombre · SportPro")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    contexto.startActivity(Intent.createChooser(intento, "Compartir cromo"))
}

@Composable
private fun MetricasPeriodo(r: EstadisticasEquipo) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FilaMetricas {
            TarjetaMetrica("Sesiones", r.sesionesEntrenamiento.toString(), icono = Icons.AutoMirrored.Rounded.DirectionsRun)
            TarjetaMetrica("Partidos", r.partidosJugados.toString(), icono = Icons.Rounded.SportsSoccer)
        }
        FilaMetricas {
            TarjetaMetrica("% asistencia", r.porcentajeAsistencia?.let { "$it %" })
            TarjetaMetrica("Eventos registrados", if (r.hayPartidos) r.eventosRegistrados.toString() else null)
        }
    }
}

@Composable
private fun FilaSesion(s: SesionPeriodo, mostrarAsistencia: Boolean) {
    TarjetaBlanca {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(formatearFechaCorta(s.fecha), color = SportProNavy, fontWeight = FontWeight.Bold)
                if (s.objetivo.isNotBlank()) {
                    Text(s.objetivo, color = SportProTextoSecundario, fontSize = 12.sp, maxLines = 2)
                }
            }
            val p = s.asistencia.porcentaje
            Text(
                if (!mostrarAsistencia || p == null) "—" else "$p %",
                color = SportProNavy, fontWeight = FontWeight.ExtraBold
            )
        }
        if (mostrarAsistencia) {
            s.asistencia.porcentaje?.let {
                Spacer(Modifier.height(8.dp))
                BarraProporcion(it / 100f)
            }
        }
    }
}

private fun edad(fechaNacimiento: String): Int? =
    try {
        PoliticaEdad.calcularEdad(LocalDate.parse(fechaNacimiento))
    } catch (e: Exception) {
        null
    }

private fun formatearFechaCorta(fecha: String): String =
    try {
        LocalDate.parse(fecha).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    } catch (e: Exception) {
        fecha
    }
