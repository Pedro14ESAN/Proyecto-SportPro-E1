package com.example.projectsportpro2026_2.ui.seguimiento

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectsportpro2026_2.datos.modelo.evento.EquipoEvento
import com.example.projectsportpro2026_2.datos.modelo.evento.EstadoEventoPartido
import com.example.projectsportpro2026_2.datos.modelo.evento.EventoPartido
import com.example.projectsportpro2026_2.datos.modelo.partido.EstadoPartido
import com.example.projectsportpro2026_2.ui.partidos.FormaBotonSportPro
import com.example.projectsportpro2026_2.ui.partidos.SportProBorde
import com.example.projectsportpro2026_2.ui.partidos.SportProFondo
import com.example.projectsportpro2026_2.ui.partidos.SportProNavy
import com.example.projectsportpro2026_2.ui.partidos.SportProSuperficie
import com.example.projectsportpro2026_2.ui.partidos.SportProTextoPrincipal
import com.example.projectsportpro2026_2.ui.partidos.SportProTextoSecundario
import com.example.projectsportpro2026_2.ui.sincronizacion.SyncNaranja
import com.example.projectsportpro2026_2.ui.sincronizacion.SyncNaranjaFondo
import com.example.projectsportpro2026_2.ui.sincronizacion.SyncNaranjaTexto
import com.example.projectsportpro2026_2.ui.sincronizacion.SyncRojo
import com.example.projectsportpro2026_2.ui.sincronizacion.SyncRojoBorde
import com.example.projectsportpro2026_2.ui.sincronizacion.SyncRojoFondo
import com.example.projectsportpro2026_2.ui.sincronizacion.SyncRojoTexto
import com.example.projectsportpro2026_2.ui.sincronizacion.SyncVerde
import com.example.projectsportpro2026_2.ui.sincronizacion.SyncVerdeBorde
import com.example.projectsportpro2026_2.ui.sincronizacion.SyncVerdeFondo
import com.example.projectsportpro2026_2.ui.sincronizacion.SyncVerdeTexto
import com.example.projectsportpro2026_2.ui.sincronizacion.VivoFondoOscuro
import com.example.projectsportpro2026_2.ui.sincronizacion.VivoFondoOscuroSuave
import com.example.projectsportpro2026_2.ui.sincronizacion.VivoTextoTenue
import com.example.projectsportpro2026_2.ui.sincronizacion.VivoVerdeBrillante
import com.example.projectsportpro2026_2.ui.sincronizacion.horaConSegundos
import com.example.projectsportpro2026_2.ui.sincronizacion.simboloEvento

/*
 * HU17 - Actualización en tiempo real de marcador y cronología.
 *
 * Vista SOLO LECTURA para DT / JUG / PAD / ADM. No tiene
 * botón "Actualizar": todo llega por suscripción.
 */
@Composable
fun PantallaSeguimientoPartido(
    idPartido: String,
    onVolver: () -> Unit,
    viewModel: SeguimientoPartidoViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()

    LaunchedEffect(idPartido) {
        viewModel.iniciar(idPartido)
    }

    val info = estado.info
    val partido = estado.partido

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SportProFondo),
        contentPadding = PaddingValues(bottom = 28.dp)
    ) {

        item {
            CabeceraVivo(
                nombreEquipo = info?.nombreEquipo.orEmpty(),
                nombreRival = partido?.nombreRival.orEmpty(),
                subtitulo = listOf(
                    info?.nombreAcademia.orEmpty(),
                    info?.categoria.orEmpty()
                ).filter { it.isNotBlank() }
                    .joinToString(" · "),
                estadoPartido = partido?.estado,
                golesPropios = estado.marcador.propio,
                golesRival = estado.marcador.rival,
                minuto = estado.minutoActual,
                provisional = estado.marcadorProvisional,
                onVolver = onVolver
            )
        }

        if (estado.cargando) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = SyncVerde)
                }
            }
            return@LazyColumn
        }

        if (info == null || partido == null) {
            item {
                ErrorSeguimiento(
                    mensaje = estado.error
                        ?: "No se pudo abrir el partido",
                    onReintentar = viewModel::reintentar
                )
            }
            return@LazyColumn
        }

        item {
            FranjaActualizacion(
                desactualizada = estado.informacionDesactualizada,
                ultimaActualizacion = estado.ultimaActualizacion,
                estadoPartido = partido.estado
            )
        }

        estado.error?.let { mensaje ->
            item {
                Text(
                    text = mensaje,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    color = SyncRojoTexto,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        when (partido.estado) {
            EstadoPartido.PROGRAMADO -> item {
                AvisoEstadoPartido(
                    texto = "El partido aún no comienza. Esta pantalla " +
                            "se actualizará sola cuando el operador lo inicie."
                )
            }

            EstadoPartido.CANCELADO -> item {
                AvisoEstadoPartido(
                    texto = "El partido fue cancelado."
                )
            }

            else -> Unit
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CRONOLOGÍA DE EVENTOS",
                    modifier = Modifier.weight(1f),
                    color = SportProTextoSecundario,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )

                if (estado.anulados > 0) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = SyncRojoFondo
                    ) {
                        Text(
                            text = "${estado.anulados} " +
                                    if (estado.anulados == 1) "anulado" else "anulados",
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                            color = SyncRojoTexto,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        if (estado.eventos.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    color = SportProSuperficie,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, SportProBorde)
                ) {
                    Text(
                        text = "Todavía no hay eventos registrados.",
                        modifier = Modifier.padding(20.dp),
                        color = SportProTextoSecundario
                    )
                }
            }
        } else {
            estado.eventos.forEachIndexed { indice, evento ->
                item(key = evento.id) {
                    Column(
                        modifier = Modifier
                            .animateItem()
                            .padding(horizontal = 20.dp)
                    ) {
                        if (indice > 0) {
                            // Línea vertical que une la cronología
                            Box(
                                modifier = Modifier
                                    .padding(start = 33.dp)
                                    .width(2.dp)
                                    .height(10.dp)
                                    .background(SportProBorde)
                            )
                        }

                        TarjetaEventoVivo(
                            evento = evento,
                            nombreEquipo = info.nombreEquipo,
                            nombreRival = partido.nombreRival,
                            esNuevo = evento.id in estado.idsNuevos
                        )
                    }
                }
            }
        }

        item {
            TarjetaResumen(
                goles = estado.totalGoles,
                tarjetas = estado.totalTarjetas,
                cambios = estado.totalCambios
            )
        }

        item {
            Text(
                text = "SOLO LECTURA · ACTUALIZACIÓN AUTOMÁTICA",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp),
                color = SportProTextoSecundario.copy(alpha = 0.7f),
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CabeceraVivo(
    nombreEquipo: String,
    nombreRival: String,
    subtitulo: String,
    estadoPartido: EstadoPartido?,
    golesPropios: Int,
    golesRival: Int,
    minuto: Int?,
    provisional: Boolean,
    onVolver: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(VivoFondoOscuro)
            .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 22.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "← Partido",
                modifier = Modifier
                    .clickable(onClick = onVolver)
                    .padding(vertical = 6.dp),
                color = Color.White,
                style = MaterialTheme.typography.labelLarge
            )

            Spacer(Modifier.weight(1f))

            ChipEstadoVivo(estadoPartido, minuto)
        }

        Spacer(Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            EscudoEquipo(
                nombre = nombreEquipo,
                colorInicial = VivoVerdeBrillante,
                modifier = Modifier.weight(1f)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$golesPropios",
                        color = VivoVerdeBrillante,
                        fontSize = 54.sp,
                        fontWeight = FontWeight.Black
                    )

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .size(width = 26.dp, height = 6.dp)
                            .background(
                                VivoFondoOscuroSuave,
                                RoundedCornerShape(3.dp)
                            )
                    )

                    Text(
                        text = "$golesRival",
                        color = Color.White,
                        fontSize = 54.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Text(
                    text = when {
                        provisional -> "MARCADOR PROVISIONAL"
                        estadoPartido == EstadoPartido.FINALIZADO -> "MARCADOR FINAL"
                        minuto != null -> "MIN $minuto'"
                        else -> " "
                    },
                    color = if (provisional) SyncNaranja else VivoTextoTenue,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
            }

            EscudoEquipo(
                nombre = nombreRival,
                colorInicial = Color(0xFFFF6B6B),
                modifier = Modifier.weight(1f)
            )
        }

        if (subtitulo.isNotBlank()) {
            Spacer(Modifier.height(14.dp))

            Text(
                text = subtitulo.uppercase(),
                modifier = Modifier.fillMaxWidth(),
                color = VivoTextoTenue,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ChipEstadoVivo(
    estadoPartido: EstadoPartido?,
    minuto: Int?
) {
    val (texto, colorPunto) =
        when (estadoPartido) {
            EstadoPartido.EN_CURSO ->
                (if (minuto != null) "EN VIVO · $minuto'" else "EN VIVO") to VivoVerdeBrillante

            EstadoPartido.FINALIZADO ->
                "FINALIZADO" to VivoTextoTenue

            EstadoPartido.CANCELADO ->
                "CANCELADO" to Color(0xFFFF6B6B)

            EstadoPartido.PROGRAMADO ->
                "POR INICIAR" to SyncNaranja

            null -> "CARGANDO" to VivoTextoTenue
        }

    val transicion = rememberInfiniteTransition(label = "pulso")
    val opacidad by transicion.animateFloat(
        initialValue = 1f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "opacidadPunto"
    )

    Surface(
        shape = RoundedCornerShape(50),
        color = VivoFondoOscuroSuave
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .alpha(
                        if (estadoPartido == EstadoPartido.EN_CURSO) opacidad else 1f
                    )
                    .background(colorPunto, CircleShape)
            )

            Spacer(Modifier.width(7.dp))

            Text(
                text = texto,
                color = colorPunto,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun EscudoEquipo(
    nombre: String,
    colorInicial: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(VivoFondoOscuroSuave, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = nombre.trim().firstOrNull()?.uppercase() ?: "?",
                color = colorInicial,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp
            )
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = nombre.uppercase(),
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun FranjaActualizacion(
    desactualizada: Boolean,
    ultimaActualizacion: Long,
    estadoPartido: EstadoPartido
) {
    val (texto, fondo, color) =
        when {
            desactualizada -> Triple(
                "Sin conexión · último estado conocido, puede estar desactualizado",
                SyncNaranjaFondo,
                SyncNaranjaTexto
            )

            estadoPartido == EstadoPartido.FINALIZADO -> Triple(
                "Partido finalizado · sincronizado",
                SyncVerdeFondo,
                SyncVerdeTexto
            )

            else -> Triple(
                "Actualizado en vivo",
                SyncVerdeFondo,
                SyncVerdeTexto
            )
        }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(fondo)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    if (desactualizada) SyncNaranja else SyncVerde,
                    CircleShape
                )
        )

        Spacer(Modifier.width(10.dp))

        Text(
            text = texto,
            modifier = Modifier.weight(1f),
            color = color,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
        )

        Spacer(Modifier.width(8.dp))

        Text(
            text = horaConSegundos(ultimaActualizacion),
            color = SportProTextoSecundario,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun AvisoEstadoPartido(texto: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 14.dp),
        color = SportProSuperficie,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, SportProBorde)
    ) {
        Text(
            text = texto,
            modifier = Modifier.padding(14.dp),
            color = SportProTextoSecundario,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun TarjetaEventoVivo(
    evento: EventoPartido,
    nombreEquipo: String,
    nombreRival: String,
    esNuevo: Boolean
) {
    val anulado = evento.estado == EstadoEventoPartido.ANULADO
    val esGol = evento.afectaMarcador && !anulado
    val corregido = !anulado && evento.version > 1

    val fondo =
        when {
            anulado -> SportProSuperficie
            esGol -> SyncVerdeFondo
            else -> SportProSuperficie
        }

    val borde =
        when {
            anulado -> SyncRojoBorde.copy(alpha = 0.5f)
            esGol -> SyncVerdeBorde
            else -> Color(0xFFE7ECEB)
        }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (anulado) 0.55f else 1f),
        color = fondo,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, borde)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = when {
                        anulado -> SyncRojoFondo
                        esGol -> SyncVerde
                        else -> Color(0xFFEEF2F1)
                    }
                ) {
                    Text(
                        text = "${evento.minuto}'",
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        color = when {
                            anulado -> SyncRojoTexto
                            esGol -> Color.White
                            else -> SportProTextoSecundario
                        },
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                if (esNuevo || corregido) {
                    Spacer(Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                if (esNuevo) SyncRojo else SyncNaranja,
                                CircleShape
                            )
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Text(
                text = simboloEvento(
                    evento.codigoTipoEvento,
                    evento.nombreTipoEvento,
                    evento.observacion
                ),
                fontSize = 18.sp
            )

            Spacer(Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = evento.nombreTipoEvento,
                    color = SportProTextoPrincipal,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (anulado) TextDecoration.LineThrough else null
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (evento.nombreJugador.isNotBlank()) {
                        Text(
                            text = evento.nombreJugador,
                            color = SportProTextoSecundario,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.width(6.dp))
                    }

                    when (evento.equipoEvento) {
                        EquipoEvento.PROPIO ->
                            ChipEquipo(nombreEquipo, SyncVerdeFondo, SyncVerdeTexto)

                        EquipoEvento.RIVAL ->
                            ChipEquipo(nombreRival, Color(0xFFEEF2F1), SportProTextoSecundario)

                        EquipoEvento.NINGUNO -> Unit
                    }
                }

                if (evento.observacion.isNotBlank() && !anulado) {
                    Text(
                        text = evento.observacion,
                        color = SportProTextoSecundario,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (anulado) {
                    Text(
                        text = "ANULADO" +
                                if (evento.motivoAnulacion.isNotBlank()) {
                                    " · ${evento.motivoAnulacion}"
                                } else {
                                    ""
                                },
                        color = SyncRojoTexto,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }

            if (esNuevo) {
                Insignia(texto = "NUEVO", fondo = SyncRojoFondo, color = SyncRojoTexto)
            } else if (corregido) {
                Insignia(texto = "✎ CORREGIDO", fondo = SyncNaranjaFondo, color = SyncNaranjaTexto)
            }
        }
    }
}

@Composable
private fun ChipEquipo(
    nombre: String,
    fondo: Color,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(5.dp),
        color = fondo
    ) {
        Text(
            text = nombre,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
            color = color,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun Insignia(
    texto: String,
    fondo: Color,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = fondo,
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Text(
            text = texto,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            color = color,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp
        )
    }
}

@Composable
private fun TarjetaResumen(
    goles: Int,
    tarjetas: Int,
    cambios: Int
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 20.dp),
        color = Color(0xFFF3F6F5),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8E6))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "RESUMEN",
                color = SportProTextoSecundario,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 1.sp
            )

            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                CifraResumen(goles, "Goles", Modifier.weight(1f))
                CifraResumen(tarjetas, "Tarjetas", Modifier.weight(1f))
                CifraResumen(cambios, "Cambios", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun CifraResumen(
    valor: Int,
    etiqueta: String,
    modifier: Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$valor",
            color = SportProNavy,
            fontWeight = FontWeight.Black,
            fontSize = 26.sp
        )

        Text(
            text = etiqueta,
            color = SportProTextoSecundario,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun ErrorSeguimiento(
    mensaje: String,
    onReintentar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SyncRojoFondo,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = mensaje,
                modifier = Modifier.padding(14.dp),
                color = SyncRojoTexto,
                style = MaterialTheme.typography.bodySmall
            )
        }

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
    }
}
