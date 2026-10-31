package com.example.projectsportpro2026_2.ui.sincronizacion

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

/*
 * Banner superior: "SINCRONIZADO · TODOS LOS DISPOSITIVOS"
 * con el enlace "Simular sin conexión".
 */
@Composable
fun BannerSincronizacion(
    estado: EstadoSincronizacionUi,
    onSimularConexion: () -> Unit,
    onReintentar: () -> Unit
) {
    val general = estado.estadoGeneral
    val colores = coloresGenerales(general)

    val titulo =
        when (general) {
            EstadoConexionPartido.SINCRONIZADO ->
                "SINCRONIZADO · TODOS LOS DISPOSITIVOS"

            EstadoConexionPartido.SINCRONIZANDO ->
                if (estado.pendientes > 0) {
                    "SINCRONIZANDO · ${estado.pendientes} " +
                            if (estado.pendientes == 1) "PENDIENTE" else "PENDIENTES"
                } else {
                    "CONECTANDO CON EL SERVIDOR…"
                }

            EstadoConexionPartido.SIN_CONEXION ->
                "SIN CONEXIÓN · ${estado.pendientes} " +
                        if (estado.pendientes == 1) {
                            "OPERACIÓN PENDIENTE"
                        } else {
                            "OPERACIONES PENDIENTES"
                        }
        }

    val detalle =
        when (general) {
            EstadoConexionPartido.SINCRONIZADO ->
                "Última confirmación: ${horaConSegundos(estado.ultimaSincronizacion)}"

            EstadoConexionPartido.SINCRONIZANDO ->
                "Enviando eventos guardados. Los demás dispositivos los verán al confirmarse."

            EstadoConexionPartido.SIN_CONEXION ->
                "Los eventos se guardan solo en este dispositivo. " +
                        "Los demás dispositivos aún no los reciben."
        }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = colores.fondo,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, colores.borde)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(10.dp),
                    shape = CircleShape,
                    color = colores.punto
                ) {}

                Spacer(Modifier.width(10.dp))

                Text(
                    text = titulo,
                    modifier = Modifier.weight(1f),
                    color = colores.texto,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(Modifier.width(8.dp))

                if (estado.cambiandoConexion) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = colores.texto
                    )
                } else {
                    Text(
                        text = if (estado.simulandoSinConexion) {
                            "Reconectar"
                        } else {
                            "Simular sin conexión"
                        },
                        modifier = Modifier
                            .clickable(onClick = onSimularConexion)
                            .padding(4.dp),
                        color = if (estado.simulandoSinConexion) {
                            colores.texto
                        } else {
                            SportProTextoSecundario
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (estado.simulandoSinConexion) {
                            FontWeight.Bold
                        } else {
                            FontWeight.Normal
                        }
                    )
                }
            }

            Text(
                text = detalle,
                color = colores.texto.copy(alpha = 0.85f),
                style = MaterialTheme.typography.bodySmall
            )

            // Acción clara de reintento cuando hay pendientes y red
            if (
                !estado.sinConexion &&
                (estado.pendientes > 0 || estado.rechazados.isNotEmpty())
            ) {
                Text(
                    text = if (estado.reintentando) {
                        "Reintentando…"
                    } else {
                        "↻ Reintentar sincronización"
                    },
                    modifier = Modifier
                        .clickable(
                            enabled = !estado.reintentando,
                            onClick = onReintentar
                        )
                        .padding(vertical = 2.dp),
                    color = colores.texto,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
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
        "⚠ 1 gol pendiente de revisión · marcador provisional"
    } else {
        "⚠ $goles goles pendientes de revisión · marcador provisional"
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
                Text(
                    text = "☁",
                    color = SportProNavy,
                    fontSize = 16.sp
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
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
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
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
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
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.labelLarge
            )

            Text(
                text = simboloEvento(
                    evento.codigoTipoEvento,
                    evento.nombreTipoEvento,
                    evento.observacion
                ),
                style = MaterialTheme.typography.titleMedium
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = evento.nombreTipoEvento,
                        color = SportProTextoPrincipal,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (evento.nombreJugador.isNotBlank()) {
                        Text(
                            text = " · ${evento.nombreJugador}",
                            color = SportProTextoSecundario,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

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
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                )
            }

            Text(
                text = "›",
                color = SportProTextoSecundario,
                fontSize = 20.sp
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
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = simboloEvento(
                        evento.codigoTipoEvento,
                        evento.nombreTipoEvento,
                        evento.observacion
                    )
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
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.labelSmall
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = simboloEvento(
                        evento.codigoTipoEvento,
                        evento.nombreTipoEvento,
                        evento.observacion
                    ),
                    style = MaterialTheme.typography.headlineSmall
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

            Text(
                text = "×",
                modifier = Modifier
                    .clickable(onClick = onCerrar)
                    .padding(10.dp),
                color = SportProTextoSecundario,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
