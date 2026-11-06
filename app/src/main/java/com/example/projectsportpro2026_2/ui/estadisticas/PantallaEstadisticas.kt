package com.example.projectsportpro2026_2.ui.estadisticas

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
                Text(nombre, color = SportProNavy, fontFamily = FontFamily.Serif,
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
                color = SportProTextoSecundario, fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun MetricasEquipo(r: EstadisticasEquipo) {
    val sinPartidos = !r.hayPartidos
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FilaMetricas {
            TarjetaMetrica("Partidos jugados", r.partidosJugados.toString(), icono = "⚽")
            TarjetaMetrica("Sesiones entrenamiento", r.sesionesEntrenamiento.toString(), icono = "🏃")
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
                icono = "🟨"
            )
        }
        TarjetaBlanca {
            Text("BALANCE DE GOLES", color = SportProTextoSecundario,
                fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
    fechaNacimiento: String,
    esMenor: Boolean,
    completo: Boolean,
    hayPartidos: Boolean,
    r: EstadisticasJugador
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TarjetaBlanca {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarIniciales(nombre, tamano = 46.dp)
                Spacer(Modifier.padding(6.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(nombre, color = SportProNavy, fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (posicion.isNotBlank()) {
                            EtiquetaSportPro(posicion.uppercase(), EtiquetaAzul, EtiquetaAzulFondo)
                        }
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
            }
        }

        FilaMetricas {
            TarjetaMetrica("Participaciones", if (hayPartidos) r.participaciones.toString() else null,
                icono = "👟")
            TarjetaMetrica("Goles", if (hayPartidos) r.goles.toString() else null,
                icono = "⚽", colorValor = SportProVerde)
        }

        if (completo) {
            TarjetaBlanca {
                Text("ASISTENCIA A ENTRENAMIENTOS", color = SportProTextoSecundario,
                    fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                val porcentaje = r.asistencia.porcentaje
                if (porcentaje == null) {
                    Text("Sin datos", color = SportProTextoSecundario, fontWeight = FontWeight.SemiBold)
                } else {
                    Text("$porcentaje %", color = SportProNavy, fontFamily = FontFamily.Serif,
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
                    icono = "🟨")
                TarjetaMetrica("T. rojas", if (hayPartidos) r.tarjetasRojas.toString() else null,
                    icono = "🟥")
                TarjetaMetrica("Faltas", if (hayPartidos) r.faltas.toString() else null, icono = "⚑")
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
    }
}

@Composable
private fun MetricasPeriodo(r: EstadisticasEquipo) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FilaMetricas {
            TarjetaMetrica("Sesiones", r.sesionesEntrenamiento.toString(), icono = "🏃")
            TarjetaMetrica("Partidos", r.partidosJugados.toString(), icono = "⚽")
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
