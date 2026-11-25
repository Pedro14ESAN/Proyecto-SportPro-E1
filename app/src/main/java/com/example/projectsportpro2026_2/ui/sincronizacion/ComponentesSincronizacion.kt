package com.example.projectsportpro2026_2.ui.sincronizacion

import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import com.example.projectsportpro2026_2.ui.theme.FamiliaEtiquetas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectsportpro2026_2.datos.modelo.sincronizacion.EstadoConexionPartido
import com.example.projectsportpro2026_2.datos.modelo.sincronizacion.EstadoSincronizacionEvento
import com.example.projectsportpro2026_2.datos.modelo.sincronizacion.EventoSincronizado
import com.example.projectsportpro2026_2.datos.sincronizacion.EventoRechazado
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.projectsportpro2026_2.ui.partidos.FormaBotonSportPro
import com.example.projectsportpro2026_2.ui.partidos.SportProBorde
import com.example.projectsportpro2026_2.ui.partidos.SportProFondo
import com.example.projectsportpro2026_2.ui.partidos.SportProNavy
import com.example.projectsportpro2026_2.ui.partidos.SportProSuperficie
import com.example.projectsportpro2026_2.ui.partidos.SportProTextoPrincipal
import com.example.projectsportpro2026_2.ui.partidos.SportProTextoSecundario

/*
 * HU16 - Componentes visuales de sincronización para la
 * pantalla del operador (PantallaPartidoEnVivo).
 */

private data class ColoresEstado(
    val fondo: Color,
    val borde: Color,
    val punto: Color,
    val texto: Color
)

@androidx.compose.runtime.Composable
private fun coloresGenerales(
    estado: EstadoConexionPartido
): ColoresEstado =
    when (estado) {
        EstadoConexionPartido.SINCRONIZADO ->
            ColoresEstado(SyncVerdeFondo, SyncVerdeBorde, SyncVerde, SyncVerdeTexto)

        EstadoConexionPartido.SINCRONIZANDO ->
            ColoresEstado(SyncNaranjaFondo, SyncNaranjaBorde, SyncNaranja, SyncNaranjaTexto)

        EstadoConexionPartido.SIN_CONEXION ->
            ColoresEstado(SyncRojoFondo, SyncRojoBorde, SyncRojo, SyncRojoTexto)
    }

/** Estado secundario del partido. La simulación permanece disponible en la lógica de pruebas. */
@Composable
fun BannerSincronizacion(
    estado: EstadoSincronizacionUi,
    onReintentar: () -> Unit
) {
    val general = estado.estadoGeneral
    val color = when (general) {
        EstadoConexionPartido.SINCRONIZADO -> SportProTextoSecundario
        EstadoConexionPartido.SINCRONIZANDO -> SyncNaranjaTexto
        EstadoConexionPartido.SIN_CONEXION -> SyncRojoTexto
    }
    val pendientes = if (estado.pendientes == 1) "1 pendiente" else "${estado.pendientes} pendientes"
    val titulo = when (general) {
        EstadoConexionPartido.SINCRONIZADO -> "Sincronizado"
        EstadoConexionPartido.SINCRONIZANDO ->
            if (estado.pendientes > 0) "Sincronizando · $pendientes"
            else "Conectando con el servidor…"
        EstadoConexionPartido.SIN_CONEXION -> "Sin conexión · $pendientes"
    }
    val confirmacion = if (estado.ultimaSincronizacion > 0L) {
        val hora = SimpleDateFormat("HH:mm", Locale.forLanguageTag("es-PE"))
            .format(Date(estado.ultimaSincronizacion))
        "Última confirmación $hora"
    } else {
        "Sin confirmación del servidor"
    }

    // Línea de cal (6.9): aviso naranja cuando no hay conexión o hay pendientes
    val fondoBanner = when (general) {
        EstadoConexionPartido.SINCRONIZADO -> Color.Transparent
        EstadoConexionPartido.SINCRONIZANDO, EstadoConexionPartido.SIN_CONEXION -> SyncNaranjaFondo
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(fondoBanner)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = when (general) {
                    EstadoConexionPartido.SINCRONIZADO -> Icons.Rounded.CloudDone
                    EstadoConexionPartido.SINCRONIZANDO -> Icons.Rounded.CloudSync
                    EstadoConexionPartido.SIN_CONEXION -> Icons.Rounded.CloudOff
                },
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
        Text(
            text = "$titulo · $confirmacion",
            color = color,
            fontSize = 12.sp,
            lineHeight = 14.sp
        )
        }

        when (general) {
            EstadoConexionPartido.SINCRONIZANDO -> Text(
                text = "Los eventos guardados se compartirán al confirmarse.",
                color = SportProTextoSecundario,
                fontSize = 12.sp,
                lineHeight = 14.sp
            )
            EstadoConexionPartido.SIN_CONEXION -> Text(
                text = "Los eventos se guardan en este dispositivo; los demás aún no los reciben.",
                color = SportProTextoSecundario,
                fontSize = 12.sp,
                lineHeight = 14.sp
            )
            EstadoConexionPartido.SINCRONIZADO -> Unit
        }

        if (!estado.sinConexion && (estado.pendientes > 0 || estado.rechazados.isNotEmpty())) {
            Row(
                modifier = Modifier
                    .clickable(enabled = !estado.reintentando, onClick = onReintentar)
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!estado.reintentando) {
                    Icon(Icons.Rounded.Refresh, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                }
                Text(
                    text = if (estado.reintentando) "Reintentando…" else "Reintentar sincronización",
                    color = color,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
/*
 * Caso alterno HU16: el partido nunca se cargó en el
 * dispositivo, no se puede registrar offline.
 */
@Composable
fun AvisoRegistroOfflineNoDisponible() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SyncRojoFondo,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, SyncRojoBorde)
    ) {
        Text(
            text = "No es posible iniciar un registro offline confiable: " +
                    "este dispositivo no tiene guardados el partido, su " +
                    "catálogo de eventos o la alineación. Conéctate para " +
                    "cargarlos al menos una vez.",
            modifier = Modifier.padding(12.dp),
            color = SyncRojoTexto,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

/*
 * Aviso dentro del marcador: "⚠ 1 gol pendiente revisión".
 */
fun textoAvisoMarcador(estado: EstadoSincronizacionUi): String? {
    val goles = estado.golesEnRevision

    if (goles <= 0) {
        return null
    }

    return if (goles == 1) {
        "1 gol pendiente de revisión · marcador provisional"
    } else {
        "$goles goles pendientes de revisión · marcador provisional"
    }
}

/*
 * Ícono redondo de la cabecera con punto de estado.
 */
@Composable
fun IndicadorNubeSincronizacion(
    estado: EstadoConexionPartido
) {
    Box {
        Surface(
            modifier = Modifier.size(38.dp),
            shape = CircleShape,
            color = SportProFondo,
            border = BorderStroke(1.dp, SportProBorde)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Rounded.Cloud,
                    contentDescription = null,
                    tint = SportProNavy,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(9.dp),
            shape = CircleShape,
            color = coloresGenerales(estado).punto
        ) {}
    }
}

/*
 * Píldoras "● 2 pendientes" y "● 1 con conflicto".
 */
@Composable
fun ContadoresSincronizacion(
    pendientes: Int,
    conflictos: Int
) {
    if (pendientes == 0 && conflictos == 0) {
        return
    }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (pendientes > 0) {
            PildoraContador(
                texto = "$pendientes " +
                        if (pendientes == 1) "pendiente" else "pendientes",
                fondo = SyncNaranjaFondo,
                punto = SyncNaranja,
                color = SyncNaranjaTexto
            )
        }

        if (conflictos > 0) {
            PildoraContador(
                texto = "$conflictos con conflicto",
                fondo = SyncRojoFondo,
                punto = SyncRojo,
                color = SyncRojoTexto
            )
        }
    }
}

@Composable
private fun PildoraContador(
    texto: String,
    fondo: Color,
    punto: Color,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = fondo
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(6.dp),
                shape = CircleShape,
                color = punto
            ) {}

            Spacer(Modifier.width(6.dp))

            Text(
                text = texto,
                color = color,
                fontFamily = FamiliaEtiquetas,
                fontSize = 12.sp
            )
        }
    }
}

/*
 * Chip pequeño bajo el nombre del evento.
 */
@Composable
fun ChipEstadoSincronizacion(
    estado: EstadoSincronizacionEvento
) {
    val (texto, fondo, color) =
        when (estado) {
            EstadoSincronizacionEvento.SINCRONIZADO ->
                Triple("Sincronizado", SyncVerdeFondo, SyncVerdeTexto)

            EstadoSincronizacionEvento.PENDIENTE ->
                Triple("Pendiente de sync", SyncNaranjaFondo, SyncNaranjaTexto)

            EstadoSincronizacionEvento.CONFLICTO ->
                Triple("Requiere revisión", SyncRojoFondo, SyncRojoTexto)
        }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = fondo
    ) {
        Text(
            text = texto,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
            color = color,
            fontFamily = FamiliaEtiquetas,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

/*
 * Tarjeta de la cronología del operador con estado de sync.
 * (Versión HU16 de la tarjeta de cronología de HU14.)
 */
@Composable
fun TarjetaEventoSincronizado(
    item: EventoSincronizado,
    onClick: () -> Unit
) {
    val evento = item.evento

    val (fondo, borde, punto) =
        when (item.estado) {
            EstadoSincronizacionEvento.SINCRONIZADO ->
                Triple(SportProSuperficie, SportProBorde, SyncVerde)

            EstadoSincronizacionEvento.PENDIENTE ->
                Triple(SyncNaranjaFondo, SyncNaranjaBorde, SyncNaranja)

            EstadoSincronizacionEvento.CONFLICTO ->
                Triple(SyncRojoFondo, SyncRojoBorde, SyncRojo)
        }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = fondo,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, borde)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(8.dp),
                shape = CircleShape,
                color = punto
            ) {}

            Text(
                text = "${evento.minuto}'",
                color = punto,
                fontFamily = FamiliaEtiquetas,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.labelLarge
            )

            IconoEvento(evento.codigoTipoEvento, evento.tipoTarjeta)

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = evento.nombreTipoEvento,
                    color = SportProTextoPrincipal,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                JugadoresEvento(evento)
                if (evento.observacion.isNotBlank()) {
                    Text(
                        text = evento.observacion,
                        color = SportProTextoSecundario,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                ChipEstadoSincronizacion(item.estado)

                if (item.estado == EstadoSincronizacionEvento.CONFLICTO) {
                    Text(
                        text = item.motivoConflicto,
                        color = SyncRojoTexto,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Hora local de captura (criterio HU16)
                Text(
                    text = "Capturado ${horaConSegundos(evento.fechaCaptura)}",
                    color = SportProTextoSecundario,
                    fontFamily = FamiliaEtiquetas,
                    fontSize = 12.sp
                )
            }

            Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = SportProTextoSecundario,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

/*
 * Evento que el servidor rechazó: se muestra como
 * "Requiere revisión" con acciones explícitas.
 */
@Composable
fun TarjetaEventoRechazado(
    rechazado: EventoRechazado,
    onReintentar: () -> Unit,
    onDescartar: () -> Unit
) {
    val evento = rechazado.evento

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SyncRojoFondo,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, SyncRojoBorde)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "${evento.minuto}'",
                    color = SyncRojo,
                    fontFamily = FamiliaEtiquetas,
                    fontWeight = FontWeight.Black
                )

                Icon(
                    imageVector = iconoEvento(
                        codigo =
                            evento.codigoTipoEvento,
                        tipoTarjeta =
                            evento.tipoTarjeta
                    ),
                    contentDescription = null,
                    tint = colorSimboloEvento(
                        codigo =
                            evento.codigoTipoEvento,
                        tipoTarjeta =
                            evento.tipoTarjeta
                    ),
                    modifier = Modifier.size(20.dp)
                )

                Text(
                    text = evento.nombreTipoEvento +
                            if (evento.nombreJugador.isNotBlank()) {
                                " · ${evento.nombreJugador}"
                            } else {
                                ""
                            },
                    modifier = Modifier.weight(1f),
                    color = SportProTextoPrincipal,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            ChipEstadoSincronizacion(EstadoSincronizacionEvento.CONFLICTO)

            Text(
                text = "No se pudo sincronizar: ${rechazado.motivo}",
                color = SyncRojoTexto,
                style = MaterialTheme.typography.bodySmall
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onReintentar,
                    shape = FormaBotonSportPro,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SyncVerde,
                        contentColor = Color.White
                    )
                ) {
                    Text("Reintentar", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onDescartar,
                    shape = FormaBotonSportPro,
                    border = BorderStroke(1.dp, SyncRojoBorde)
                ) {
                    Text("Descartar", color = SyncRojoTexto)
                }
            }
        }
    }
}

/*
 * Hoja inferior para resolver un conflicto.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalConflictoSincronizacion(
    item: EventoSincronizado,
    procesando: Boolean,
    sinConexion: Boolean,
    onCerrar: () -> Unit,
    onConservar: () -> Unit,
    onAnular: () -> Unit
) {
    val evento = item.evento

    val estadoHoja =
        rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onCerrar,
        sheetState = estadoHoja,
        containerColor = SportProSuperficie,
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "REQUIERE REVISIÓN",
                color = SyncRojoTexto,
                fontFamily = FamiliaEtiquetas,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.labelSmall
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = iconoEvento(
                        codigo =
                            evento.codigoTipoEvento,
                        tipoTarjeta =
                            evento.tipoTarjeta
                    ),
                    contentDescription = null,
                    tint = colorSimboloEvento(
                        codigo =
                            evento.codigoTipoEvento,
                        tipoTarjeta =
                            evento.tipoTarjeta
                    ),
                    modifier = Modifier.size(26.dp)
                )

                Text(
                    text = "${evento.nombreTipoEvento} · ${evento.minuto}'",
                    color = SportProNavy,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black
                )
            }

            if (evento.nombreJugador.isNotBlank()) {
                Text(
                    text = evento.nombreJugador,
                    color = SportProTextoSecundario
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SyncRojoFondo,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = item.motivoConflicto,
                    modifier = Modifier.padding(12.dp),
                    color = SyncRojoTexto,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text(
                text = "El evento no se descartó. Decide si se conserva " +
                        "(no es un duplicado) o si se anula con un motivo " +
                        "(quedará en la auditoría).",
                color = SportProTextoSecundario,
                style = MaterialTheme.typography.bodySmall
            )

            Button(
                onClick = onConservar,
                enabled = !procesando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = FormaBotonSportPro,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SyncVerde,
                    contentColor = Color.White
                )
            ) {
                if (procesando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Conservar evento", fontWeight = FontWeight.Bold)
                }
            }

            OutlinedButton(
                onClick = onAnular,
                enabled = !procesando && !sinConexion,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = FormaBotonSportPro,
                border = BorderStroke(1.dp, SyncRojoBorde)
            ) {
                Text(
                    text = "Anular evento",
                    color = SyncRojoTexto,
                    fontWeight = FontWeight.Bold
                )
            }

            if (sinConexion) {
                Text(
                    text = "La anulación requiere conexión porque valida la " +
                            "versión del evento (HU15).",
                    color = SportProTextoSecundario,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/*
 * Mensaje temporal de sincronización (éxito o error).
 */
@Composable
fun MensajeSincronizacion(
    mensaje: String,
    error: Boolean,
    onCerrar: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (error) SyncRojoFondo else SyncVerdeFondo,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = mensaje,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 12.dp),
                color = if (error) SyncRojoTexto else SyncVerdeTexto,
                style = MaterialTheme.typography.bodySmall
            )

            Icon(
                Icons.Rounded.Close,
                contentDescription = "Cerrar",
                tint = SportProTextoSecundario,
                modifier = Modifier
                    .clickable(onClick = onCerrar)
                    .padding(10.dp).size(18.dp)
            )
        }
    }
}
