package com.example.projectsportpro2026_2.ui.inicio

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.HowToReg
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.SportsSoccer
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectsportpro2026_2.datos.modelo.EstadoMensualidad
import com.example.projectsportpro2026_2.datos.modelo.Rol
import com.example.projectsportpro2026_2.ui.comun.BotonPrincipal
import com.example.projectsportpro2026_2.ui.comun.CabeceraCancha
import com.example.projectsportpro2026_2.ui.comun.ChipEstado
import com.example.projectsportpro2026_2.ui.comun.ColorFicha
import com.example.projectsportpro2026_2.ui.comun.CromoJugador
import com.example.projectsportpro2026_2.ui.comun.DatoFicha
import com.example.projectsportpro2026_2.ui.comun.MarcadorLed
import com.example.projectsportpro2026_2.ui.comun.PizarraFichas
import com.example.projectsportpro2026_2.ui.comun.TamanoCromo
import com.example.projectsportpro2026_2.ui.comun.TarjetaTicket
import com.example.projectsportpro2026_2.ui.comun.TipoEstado
import com.example.projectsportpro2026_2.ui.comun.VarianteBoton
import com.example.projectsportpro2026_2.ui.comun.VarianteTicket
import com.example.projectsportpro2026_2.ui.theme.EstiloEtiqueta
import com.example.projectsportpro2026_2.ui.theme.EstiloMarcador
import com.example.projectsportpro2026_2.ui.theme.FamiliaCuerpo
import com.example.projectsportpro2026_2.ui.theme.FamiliaTitulos
import com.example.projectsportpro2026_2.ui.theme.LineaDeCal
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/*
 * Rediseño "Línea de cal" · inicio de los 4 roles (ESPECIFICACION 6.2 y 7.1, img 03).
 *
 * Se conservan TODOS los destinos de cada rol (ADM 12, DT 9, JUG 9, PAD 10):
 *  - "Notificaciones" pasa a la campana de la cabecera (con las no leídas).
 *  - "Partidos en vivo" pasa al marcador LED (o "Sin partidos en vivo").
 *  - "Planes y ofertas" (ADM) queda como tarjeta debajo de la pizarra.
 *  - El resto son fichas de la pizarra, con el mismo texto y el mismo destino, salvo
 *    las que ya están en la barra inferior (no se repiten): ADM y DT "Gestionar
 *    academias" y "Comunidad"; JUG "Mis entrenamientos" y "Comunidad"; PAD "Mis
 *    jugadores" y "Comunidad". Siguen accesibles desde la barra.
 *  - "Cerrar sesión" está en el menú del avatar y en la pantalla Perfil.
 *
 * Lo nuevo (7.1) solo lee datos con InicioViewModel; si algo falla, esa sección
 * no se muestra. No hay vista de tablet: en pantallas anchas el mismo diseño va
 * centrado con widthIn(max = 600.dp). La barra inferior (NavegacionApp) es un atajo
 * adicional: todos los botones de la pizarra se mantienen.
 */

private val localePeru: Locale = Locale.forLanguageTag("es-PE")

// =====================================================================
// Estructura común
// =====================================================================

@Composable
private fun PantallaInicioRol(
    rol: Rol,
    titulo: String,
    descripcion: String,
    viewModel: InicioViewModel,
    onNotificaciones: () -> Unit,
    onPartidosEnVivo: () -> Unit,
    onCerrarSesion: () -> Unit,
    columnasPizarra: Int,
    onComunidad: () -> Unit = {},
    fichas: (EstadoInicio) -> List<DatoFicha>,
    seccionSuperior: @Composable ColumnScope.(EstadoInicio) -> Unit,
    debajoDePizarra: @Composable ColumnScope.(EstadoInicio) -> Unit = {}
) {
    LaunchedEffect(rol) { viewModel.iniciar(rol) }
    val estado by viewModel.estado.collectAsState()
    val c = LineaDeCal.colores

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(c.fondo),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            CabeceraCancha(
                titulo = saludo(estado.nombre),
                subtitulo = descripcion,
                etiqueta = titulo,
                acciones = {
                    BotonCampana(estado.noLeidas, onNotificaciones)
                    Spacer(Modifier.width(8.dp))
                    MenuAvatar(estado.nombre, titulo, onCerrarSesion)
                }
            ) {
                TarjetaEnVivo(estado, onPartidosEnVivo)
            }

            Column(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                seccionSuperior(estado)

                TituloSeccion("Pizarra")
                PizarraFichas(fichas = fichas(estado), columnas = columnasPizarra)

                debajoDePizarra(estado)

                // 7.1: vista previa de Comunidad (en el ADM, debajo de "Planes y ofertas")
                SeccionComunidadInicio(viewModel, onComunidad)

                // Cerrar sesión: en el menú del avatar y en la pantalla Perfil (barra inferior)
            }
        }
    }
}

private fun saludo(nombre: String): String {
    val primerNombre = nombre.trim().split(" ").firstOrNull().orEmpty()
    return if (primerNombre.isBlank()) "Hola." else "Hola, $primerNombre."
}

@Composable
private fun TituloSeccion(texto: String, accion: (@Composable () -> Unit)? = null) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = texto,
            modifier = Modifier.weight(1f),
            color = LineaDeCal.colores.textoPrincipal,
            fontFamily = FamiliaTitulos,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        accion?.invoke()
    }
}

// ---------- Campana y avatar ----------

@Composable
private fun BotonCampana(noLeidas: Int?, onClick: () -> Unit) {
    val c = LineaDeCal.colores
    val descripcion = if (noLeidas != null && noLeidas > 0) {
        "Notificaciones, $noLeidas sin leer"
    } else {
        "Notificaciones"
    }
    Box {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.18f))
                .clickable(role = Role.Button, onClick = onClick)
                .semantics { contentDescription = descripcion },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Notifications, contentDescription = null, tint = Color.White)
        }
        if (noLeidas != null && noLeidas > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
                    .heightIn(min = 20.dp)
                    .widthIn(min = 20.dp)
                    .clip(CircleShape)
                    .background(c.rojo)
                    .padding(horizontal = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (noLeidas > 99) "99+" else noLeidas.toString(),
                    color = Color.White,
                    fontFamily = FamiliaCuerpo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun MenuAvatar(nombre: String, titulo: String, onCerrarSesion: () -> Unit) {
    val c = LineaDeCal.colores
    var abierto by remember { mutableStateOf(false) }
    val iniciales = nombre.split(" ").filter { it.isNotBlank() }.take(2)
        .joinToString("") { it.first().uppercase() }

    Box {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(c.lima)
                .clickable(role = Role.Button) { abierto = true }
                .semantics { contentDescription = "Menú de la cuenta" },
            contentAlignment = Alignment.Center
        ) {
            if (iniciales.isNotBlank()) {
                Text(
                    text = iniciales,
                    color = c.navy,
                    fontFamily = FamiliaTitulos,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
            } else {
                Icon(Icons.Rounded.Badge, contentDescription = null, tint = c.navy)
            }
        }
        DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
            DropdownMenuItem(
                text = {
                    Column {
                        Text(
                            nombre.ifBlank { titulo },
                            fontFamily = FamiliaCuerpo,
                            fontWeight = FontWeight.Bold,
                            color = c.textoPrincipal
                        )
                        if (nombre.isNotBlank()) {
                            Text(titulo, fontSize = 12.sp, color = c.textoSecundario)
                        }
                    }
                },
                onClick = {},
                enabled = false
            )
            HorizontalDivider(color = c.borde)
            DropdownMenuItem(
                text = { Text("Cerrar sesión", fontFamily = FamiliaCuerpo, fontWeight = FontWeight.SemiBold) },
                leadingIcon = { Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null) },
                onClick = {
                    abierto = false
                    onCerrarSesion()
                }
            )
        }
    }
}

// ---------- Tarjeta EN VIVO ----------

@Composable
private fun TarjetaEnVivo(estado: EstadoInicio, onPartidosEnVivo: () -> Unit) {
    val vivo = estado.enVivo
    if (vivo != null) {
        MarcadorLed(
            local = vivo.partido.equipo.nombre,
            rival = vivo.partido.partido.nombreRival,
            golesLocal = vivo.marcador?.propio,
            golesRival = vivo.marcador?.rival,
            etiquetaSuperior = listOf("En vivo", vivo.partido.equipo.categoria)
                .filter { it.isNotBlank() }.joinToString(" · "),
            enVivo = true,
            compacto = true,
            onClick = onPartidosEnVivo
        )
        return
    }

    // Sin partido en curso: sigue abriendo "Partidos en vivo" (7.1)
    val c = LineaDeCal.colores
    val proximo = estado.proximoPartido
    val texto = when {
        !estado.partidosCargados -> null
        proximo != null -> "Sin partidos en vivo · próximo: " + textoFechaHora(proximo.partido.fecha, proximo.partido.hora)
        else -> "Sin partidos en vivo"
    }
    val forma = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(c.ledFondo)
            .clickable(role = Role.Button, onClick = onPartidosEnVivo)
            .semantics(mergeDescendants = true) {
                contentDescription = listOfNotNull("Partidos en vivo", texto).joinToString(". ")
            }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                "PARTIDOS EN VIVO",
                color = c.amarillo,
                style = EstiloEtiqueta
            )
            if (texto != null) {
                Text(
                    texto,
                    color = Color.White,
                    fontFamily = FamiliaCuerpo,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        }
        Icon(
            Icons.AutoMirrored.Rounded.ArrowForward,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}

private fun textoFechaHora(fecha: String, hora: String): String {
    val dia = try {
        etiquetaDia(LocalDate.parse(fecha.trim()))
    } catch (e: Exception) {
        fecha
    }
    return listOf(dia, hora).filter { it.isNotBlank() }.joinToString(" · ")
}

private fun etiquetaDia(fecha: LocalDate, hoy: LocalDate = LocalDate.now()): String =
    when (fecha) {
        hoy -> "hoy"
        hoy.plusDays(1) -> "mañana"
        else -> fecha.format(DateTimeFormatter.ofPattern("EEE d MMM", localePeru)).replace(".", "")
    }

private fun etiquetaDia(epochMillis: Long): String =
    etiquetaDia(Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate())

// ---------- Piezas de las secciones nuevas ----------

/** Tarjeta con número grande (Doto) y etiqueta; borde superior de color. */
@Composable
private fun TarjetaNumero(
    valor: String,
    etiqueta: String,
    acento: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = LineaDeCal.colores
    val numerico = valor.any { it.isDigit() }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(c.superficie)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics(mergeDescendants = true) {}
    ) {
        Box(Modifier.fillMaxWidth().height(3.dp).background(acento))
        Column(
            Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (numerico) {
                Text(valor, color = c.textoPrincipal, style = EstiloMarcador.copy(fontSize = 26.sp, lineHeight = 28.sp))
            } else {
                Text(
                    valor,
                    color = c.textoPrincipal,
                    fontFamily = FamiliaTitulos,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    lineHeight = 28.sp,
                    maxLines = 1
                )
            }
            Text(
                etiqueta,
                color = c.textoSecundario,
                fontFamily = FamiliaCuerpo,
                fontSize = 12.sp,
                lineHeight = 15.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TextoTicketTitulo(texto: String) {
    Text(
        texto,
        fontFamily = FamiliaCuerpo,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun TextoTicketDetalle(texto: String) {
    Text(
        texto,
        color = LineaDeCal.colores.textoSecundario,
        fontFamily = FamiliaCuerpo,
        fontSize = 13.sp,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )
}

private fun textoMensualidad(estado: EstadoMensualidad): String =
    when (estado) {
        EstadoMensualidad.PAGADO -> "Al día"
        EstadoMensualidad.PENDIENTE -> "Pendiente"
        EstadoMensualidad.VENCIDO -> "Vencida"
    }

private fun tipoMensualidad(estado: EstadoMensualidad): TipoEstado =
    when (estado) {
        EstadoMensualidad.PAGADO -> TipoEstado.OK
        EstadoMensualidad.PENDIENTE -> TipoEstado.PENDIENTE
        EstadoMensualidad.VENCIDO -> TipoEstado.ERROR
    }

// =====================================================================
// ADM · 12 destinos
// =====================================================================

@Composable
fun InicioAdministrador(
    onGestionarAcademias: () -> Unit,
    onVinculaciones: () -> Unit,
    onCuentasJugadores: () -> Unit = {},
    onMensualidades: () -> Unit,
    onEjercicios: () -> Unit,
    onAsistencias: () -> Unit,
    onPartidosEnVivo: () -> Unit = {},
    onAnuncios: () -> Unit = {},          // HU19
    onComunidad: () -> Unit = {},         // HU21
    onNotificaciones: () -> Unit = {},    // HU20
    onEstadisticas: () -> Unit = {},      // HU18
    onPlanes: () -> Unit = {},            // Planes y ofertas (demo)
    onCerrarSesion: () -> Unit,
    viewModel: InicioViewModel = viewModel()
) = PantallaInicioRol(
    rol = Rol.ADM,
    titulo = "Panel del administrador",
    descripcion = "Gestiona tus academias, equipos, jugadores y módulos administrativos.",
    viewModel = viewModel,
    onNotificaciones = onNotificaciones,       // 🔔 Notificaciones → campana
    onPartidosEnVivo = onPartidosEnVivo,       // Partidos en vivo → marcador LED
    onComunidad = onComunidad,
    onCerrarSesion = onCerrarSesion,
    columnasPizarra = 4,
    fichas = { estado ->
        listOf(
            DatoFicha(Icons.Rounded.Badge, "Cuentas de jugadores", onCuentasJugadores, ColorFicha.BLANCA,
                contador = estado.atencion?.cuentasPorAprobar),
            DatoFicha(Icons.Rounded.Link, "Vinculaciones", onVinculaciones, ColorFicha.AMARILLA,
                contador = estado.atencion?.vinculacionesPendientes),
            DatoFicha(Icons.Rounded.Payments, "Mensualidades", onMensualidades, ColorFicha.VERDE,
                contador = estado.atencion?.mensualidadesVencidas),
            DatoFicha(Icons.Rounded.FitnessCenter, "Biblioteca de ejercicios", onEjercicios, ColorFicha.BLANCA),
            DatoFicha(Icons.Rounded.HowToReg, "Asistencias", onAsistencias, ColorFicha.LIMA),
            DatoFicha(Icons.Rounded.BarChart, "Estadísticas", onEstadisticas, ColorFicha.AMARILLA),
            DatoFicha(Icons.Rounded.Campaign, "Anuncios del club", onAnuncios, ColorFicha.VERDE)
        )
    },
    seccionSuperior = { estado ->
        val atencion = estado.atencion
        if (atencion != null) {
            val c = LineaDeCal.colores
            TituloSeccion("Requiere tu atención")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                atencion.cuentasPorAprobar?.let {
                    TarjetaNumero("$it", "Cuentas por aprobar", c.naranja, onCuentasJugadores, Modifier.weight(1f))
                }
                atencion.vinculacionesPendientes?.let {
                    TarjetaNumero("$it", "Vinculaciones pendientes", c.naranja, onVinculaciones, Modifier.weight(1f))
                }
                atencion.mensualidadesVencidas?.let {
                    TarjetaNumero("$it", "Mensualidades vencidas", c.rojo, onMensualidades, Modifier.weight(1f))
                }
            }
        }
    },
    debajoDePizarra = {
        // 💎 Planes y ofertas: tarjeta debajo de la pizarra (6.2)
        TarjetaPlanes(onPlanes)
    }
)

@Composable
private fun TarjetaPlanes(onPlanes: () -> Unit) {
    val c = LineaDeCal.colores
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(c.superficie)
            .clickable(role = Role.Button, onClick = onPlanes)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(Icons.Rounded.WorkspacePremium, contentDescription = null, tint = c.amarillo)
        Text(
            "Planes y ofertas",
            modifier = Modifier.weight(1f),
            color = c.textoPrincipal,
            fontFamily = FamiliaCuerpo,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )
        Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, tint = c.textoSecundario)
    }
}

// =====================================================================
// DT · 9 destinos
// =====================================================================

@Composable
fun InicioEntrenador(
    onGestionarAcademias: () -> Unit,
    onEjercicios: () -> Unit,
    onEntrenamientos: () -> Unit,
    onAsistencias: () -> Unit,
    onPartidosEnVivo: () -> Unit = {},
    onAnuncios: () -> Unit = {},          // HU19
    onComunidad: () -> Unit = {},         // HU21
    onNotificaciones: () -> Unit = {},    // HU20
    onEstadisticas: () -> Unit = {},      // HU18
    onCerrarSesion: () -> Unit,
    viewModel: InicioViewModel = viewModel()
) = PantallaInicioRol(
    rol = Rol.DT,
    titulo = "Panel del director técnico",
    descripcion = "Gestiona tus equipos, biblioteca, entrenamientos y asistencia.",
    viewModel = viewModel,
    onNotificaciones = onNotificaciones,
    onPartidosEnVivo = onPartidosEnVivo,
    onComunidad = onComunidad,
    onCerrarSesion = onCerrarSesion,
    columnasPizarra = 3,
    fichas = {
        listOf(
            DatoFicha(Icons.Rounded.CalendarMonth, "Entrenamientos", onEntrenamientos, ColorFicha.BLANCA),
            DatoFicha(Icons.Rounded.HowToReg, "Asistencias", onAsistencias, ColorFicha.AMARILLA),
            DatoFicha(Icons.Rounded.FitnessCenter, "Biblioteca de ejercicios", onEjercicios, ColorFicha.VERDE),
            DatoFicha(Icons.Rounded.BarChart, "Estadísticas", onEstadisticas, ColorFicha.AMARILLA),
            DatoFicha(Icons.Rounded.Campaign, "Anuncios del club", onAnuncios, ColorFicha.VERDE)
        )
    },
    seccionSuperior = { estado ->
        val jornada = estado.jornada
        if (jornada != null) {
            TituloSeccion("Tu jornada")
            jornada.entrenamiento?.let { e ->
                // Sin lugar: Entrenamiento no lo guarda (7.1)
                TarjetaTicket(
                    hora = e.hora,
                    fecha = etiquetaDia(e.fechaHoraEpochMillis),
                    variante = VarianteTicket.VERDE,
                    onClick = onEntrenamientos
                ) {
                    TextoTicketTitulo(listOf("Entrenamiento", e.objetivo).filter { it.isNotBlank() }.joinToString(" · "))
                    TextoTicketDetalle(
                        listOf(
                            e.nombreEquipo,
                            "${e.duracionTotalMinutos} min",
                            "${e.ejercicios.size} ejercicios"
                        ).filter { it.isNotBlank() }.joinToString(" · ")
                    )
                }
            }
            jornada.partido?.let { p ->
                TarjetaTicket(
                    hora = p.partido.hora,
                    fecha = textoDiaPartido(p.partido.fecha),
                    variante = VarianteTicket.AMARILLA,
                    onClick = onPartidosEnVivo
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.weight(1f)) { TextoTicketTitulo("vs ${p.partido.nombreRival}") }
                        jornada.respuestas?.let { r ->
                            ChipEstado("${r.respondieron}/${r.total}", TipoEstado.NEUTRO)
                        }
                    }
                    TextoTicketDetalle(
                        listOf(
                            p.equipo.nombre,
                            jornada.respuestas?.let { r -> "respondieron ${r.respondieron}/${r.total}" }.orEmpty()
                        ).filter { it.isNotBlank() }.joinToString(" · ")
                    )
                }
            }
        }
    }
)

private fun textoDiaPartido(fecha: String): String =
    try {
        etiquetaDia(LocalDate.parse(fecha.trim()))
    } catch (e: Exception) {
        fecha
    }

// =====================================================================
// JUG · 9 destinos
// =====================================================================

@Composable
fun InicioJugador(
    onMensualidades: () -> Unit,
    onEntrenamientos: () -> Unit,
    onAsistencias: () -> Unit,
    onConvocatorias: () -> Unit,
    onPartidosEnVivo: () -> Unit = {},
    onAnuncios: () -> Unit = {},          // HU19
    onComunidad: () -> Unit = {},         // HU21
    onNotificaciones: () -> Unit = {},    // HU20
    onEstadisticas: () -> Unit = {},      // HU18
    onCerrarSesion: () -> Unit,
    viewModel: InicioViewModel = viewModel()
) = PantallaInicioRol(
    rol = Rol.JUG,
    titulo = "Panel del jugador",
    descripcion = "Consulta tu información deportiva y administrativa.",
    viewModel = viewModel,
    onNotificaciones = onNotificaciones,
    onPartidosEnVivo = onPartidosEnVivo,
    onComunidad = onComunidad,
    onCerrarSesion = onCerrarSesion,
    columnasPizarra = 3,
    fichas = { estado ->
        listOf(
            DatoFicha(Icons.Rounded.SportsSoccer, "Mis convocatorias", onConvocatorias, ColorFicha.LIMA,
                contador = estado.paraTi?.convocatoriasPendientes),
            DatoFicha(Icons.Rounded.EventAvailable, "Mi asistencia", onAsistencias, ColorFicha.AMARILLA),
            DatoFicha(Icons.Rounded.Payments, "Mis mensualidades", onMensualidades, ColorFicha.VERDE),
            DatoFicha(Icons.Rounded.BarChart, "Estadísticas", onEstadisticas, ColorFicha.AMARILLA),
            DatoFicha(Icons.Rounded.Campaign, "Anuncios del club", onAnuncios, ColorFicha.VERDE)
        )
    },
    seccionSuperior = { estado ->
        val paraTi = estado.paraTi
        val c = LineaDeCal.colores
        val datos = buildList {
            paraTi?.porcentajeAsistencia?.let { add(Triple("$it%", "Mi asistencia", onAsistencias)) }
            paraTi?.golesTemporada?.let { add(Triple("$it", "Goles en la temporada", onEstadisticas)) }
            paraTi?.mensualidad?.let { add(Triple(textoMensualidad(it), "Mensualidad", onMensualidades)) }
        }
        if (paraTi?.convocatoria != null || datos.isNotEmpty()) {
            TituloSeccion("Para ti")
            paraTi?.convocatoria?.let { conv ->
                TarjetaTicket(
                    hora = conv.partido.hora,
                    fecha = textoDiaPartido(conv.partido.fecha),
                    variante = VarianteTicket.AMARILLA,
                    onClick = onConvocatorias
                ) {
                    TextoTicketTitulo("Convocado vs ${conv.partido.nombreRival}")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.weight(1f)) { TextoTicketDetalle(conv.partido.ubicacion) }
                        Text(
                            "Responder",
                            color = c.verde,
                            fontFamily = FamiliaCuerpo,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            if (datos.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    datos.forEach { (valor, etiqueta, accion) ->
                        TarjetaNumero(valor, etiqueta, c.verde, accion, Modifier.weight(1f))
                    }
                }
            }
        }
    }
)

// =====================================================================
// PAD · 10 destinos
// =====================================================================

@Composable
fun InicioApoderado(
    onMisJugadores: () -> Unit,
    onMensualidades: () -> Unit,
    onEntrenamientos: () -> Unit,
    onAsistencias: () -> Unit,
    onConvocatorias: () -> Unit,
    onPartidosEnVivo: () -> Unit = {},
    onAnuncios: () -> Unit = {},          // HU19
    onComunidad: () -> Unit = {},         // HU21
    onNotificaciones: () -> Unit = {},    // HU20
    onEstadisticas: () -> Unit = {},      // HU18
    onCerrarSesion: () -> Unit,
    viewModel: InicioViewModel = viewModel()
) = PantallaInicioRol(
    rol = Rol.PAD,
    titulo = "Panel del apoderado",
    descripcion = "Consulta a los jugadores bajo tu responsabilidad.",
    viewModel = viewModel,
    onNotificaciones = onNotificaciones,
    onPartidosEnVivo = onPartidosEnVivo,
    onComunidad = onComunidad,
    onCerrarSesion = onCerrarSesion,
    columnasPizarra = 3,
    fichas = { estado ->
        listOf(
            DatoFicha(Icons.Rounded.Payments, "Mensualidades", onMensualidades, ColorFicha.VERDE,
                contador = estado.mensualidadesVencidasPad),
            DatoFicha(Icons.Rounded.CalendarMonth, "Entrenamientos", onEntrenamientos, ColorFicha.BLANCA),
            DatoFicha(Icons.Rounded.EventAvailable, "Historial de asistencia", onAsistencias, ColorFicha.AMARILLA),
            DatoFicha(Icons.Rounded.SportsSoccer, "Convocatorias", onConvocatorias, ColorFicha.BLANCA,
                contador = estado.convocatoriasPendientesPad),
            DatoFicha(Icons.Rounded.BarChart, "Estadísticas", onEstadisticas, ColorFicha.AMARILLA),
            DatoFicha(Icons.Rounded.Campaign, "Anuncios del club", onAnuncios, ColorFicha.VERDE)
        )
    },
    seccionSuperior = { estado ->
        val jugadores = estado.misJugadores
        if (!jugadores.isNullOrEmpty()) {
            TituloSeccion("Mis jugadores") {
                TextButton(onClick = onMisJugadores) {
                    Text(
                        "Ver todos",
                        color = LineaDeCal.colores.verde,
                        fontFamily = FamiliaCuerpo,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
            jugadores.chunked(2).forEach { fila ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    fila.forEach { item ->
                        val mensualidad = item.mensualidad
                        CromoJugador(
                            nombre = item.jugador.nombreCompleto(),
                            posicion = item.jugador.posicion.ifBlank { item.jugador.categoria },
                            fotoUrl = item.jugador.fotoUrl,
                            tamano = TamanoCromo.MINI,
                            insignia = if (mensualidad != null) {
                                @Composable { ChipEstado(textoMensualidad(mensualidad), tipoMensualidad(mensualidad)) }
                            } else {
                                null
                            },
                            onClick = onMisJugadores,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (fila.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
)

// =====================================================================
// Cuenta pendiente (mismo estilo, mismo texto)
// =====================================================================

@Composable
fun PantallaCuentaPendiente(
    onCerrarSesion: () -> Unit
) {
    val c = LineaDeCal.colores
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(c.fondo),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            CabeceraCancha(
                titulo = "Cuenta pendiente",
                etiqueta = "Panel principal",
                subtitulo = "Tu cuenta debe ser autorizada por tu apoderado y validada por la academia antes de usarla."
            )
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ChipEstado("Pendiente", TipoEstado.PENDIENTE, conPunto = true)
                BotonPrincipal(
                    texto = "Cerrar sesión",
                    onClick = onCerrarSesion,
                    variante = VarianteBoton.NAVY,
                    icono = Icons.AutoMirrored.Rounded.Logout
                )
            }
        }
    }
}
