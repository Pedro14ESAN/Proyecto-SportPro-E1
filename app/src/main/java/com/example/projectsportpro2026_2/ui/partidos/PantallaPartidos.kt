package com.example.projectsportpro2026_2.ui.partidos

import com.example.projectsportpro2026_2.ui.comun.TipoEstado
import com.example.projectsportpro2026_2.ui.comun.ChipEstado
import com.example.projectsportpro2026_2.ui.comun.VarianteTicket
import com.example.projectsportpro2026_2.ui.comun.TarjetaTicket
import com.example.projectsportpro2026_2.ui.comun.anchoCompleto
import com.example.projectsportpro2026_2.ui.comun.CabeceraCancha
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import com.example.projectsportpro2026_2.ui.theme.LineaDeCal
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.produceState
import kotlinx.coroutines.flow.catch
import com.example.projectsportpro2026_2.datos.repositorio.sincronizacion.RepositorioSincronizacion
import com.example.projectsportpro2026_2.datos.repositorio.evento.RepositorioEventosPartido
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectsportpro2026_2.datos.modelo.Equipo
import com.example.projectsportpro2026_2.datos.modelo.partido.EstadoPartido
import com.example.projectsportpro2026_2.datos.modelo.partido.Partido
import com.example.projectsportpro2026_2.datos.repositorio.evento.MarcadorPartido

@Composable
fun PantallaPartidos(
    idAcademia: String,
    nombreAcademia: String,
    onVolver: () -> Unit,
    onCrearPartido: () -> Unit,
    onAbrirPartido: (String) -> Unit,
    viewModel: PartidosViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()

    LaunchedEffect(idAcademia) {
        viewModel.iniciar(idAcademia)
    }

    val enCurso = estado.partidos.filter {
        it.estado == EstadoPartido.EN_CURSO
    }

    val proximos = estado.partidos.filter {
        it.estado == EstadoPartido.PROGRAMADO
    }

    val finalizados = estado.partidos.filter {
        it.estado == EstadoPartido.FINALIZADO
    }

    val cancelados = estado.partidos.filter {
        it.estado == EstadoPartido.CANCELADO
    }

    Scaffold(
        containerColor = SportProFondo,

        floatingActionButton = {
            FloatingActionButton(
                onClick = onCrearPartido,
                containerColor = LineaDeCal.colores.lima,
                contentColor = LineaDeCal.colores.navy,
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Crear partido")
            }
        }
    ) { relleno ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(SportProFondo)
                .padding(relleno)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            item {
                EncabezadoPantallaPartidos(
                    nombreAcademia = nombreAcademia,
                    onVolver = onVolver
                )
            }

            estado.error?.let { mensaje ->
                item {
                    Text(
                        text = mensaje,
                        color = SportProRojo,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (estado.cargando && estado.partidos.isEmpty()) {

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = SportProVerde
                        )
                    }
                }

            } else {

                if (enCurso.isNotEmpty()) {
                    seccionPartidos(
                        titulo = "EN VIVO",
                        partidos = enCurso,
                        equipos = estado.equipos,
                        marcadores = estado.marcadoresPorPartido,
                        onAbrirPartido = onAbrirPartido
                    )
                }

                seccionPartidos(
                    titulo = "Próximos partidos",
                    partidos = proximos,
                    equipos = estado.equipos,
                    marcadores = estado.marcadoresPorPartido,
                    onAbrirPartido = onAbrirPartido
                )

                seccionPartidos(
                    titulo = "Partidos anteriores",
                    partidos = finalizados,
                    equipos = estado.equipos,
                    marcadores = estado.marcadoresPorPartido,
                    onAbrirPartido = onAbrirPartido
                )

                seccionPartidos(
                    titulo = "CANCELADOS",
                    partidos = cancelados,
                    equipos = estado.equipos,
                    marcadores = estado.marcadoresPorPartido,
                    onAbrirPartido = onAbrirPartido
                )
            }

            item {
                Spacer(
                    modifier = Modifier.height(90.dp)
                )
            }
        }
    }
}

@Composable
private fun EncabezadoPantallaPartidos(
    nombreAcademia: String,
    onVolver: () -> Unit
) {
    // Línea de cal (6.8, img 07): cabecera de césped (mismos textos y el mismo "Volver")
    CabeceraCancha(
        titulo = "Partidos",
        subtitulo = nombreAcademia.takeIf { it.isNotBlank() },
        etiqueta = "TEMPORADA 2026",
        onVolver = onVolver,
        modifier = Modifier.anchoCompleto(20.dp)
    )
}

private fun LazyListScope.seccionPartidos(
    titulo: String,
    partidos: List<Partido>,
    equipos: List<Equipo>,
    marcadores: Map<String, MarcadorPartido>,
    onAbrirPartido: (String) -> Unit
) {
    item {
        Text(
            text = titulo,
            style = MaterialTheme.typography.labelLarge,
            color = SportProTextoSecundario,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(
                top = 12.dp,
                bottom = 2.dp
            )
        )
    }

    if (partidos.isEmpty()) {

        item {
            Text(
                text = "No hay partidos en esta sección.",
                style = MaterialTheme.typography.bodySmall,
                color = SportProTextoSecundario,
                modifier = Modifier.padding(
                    vertical = 4.dp
                )
            )
        }

    } else {

        items(
            items = partidos,
            key = { it.id }
        ) { partido ->

            val nombreEquipo = equipos
                .firstOrNull {
                    it.id == partido.idEquipo
                }
                ?.nombre
                ?: "Equipo"

            TarjetaPartidoSportPro(
                partido = partido,
                nombreEquipo = nombreEquipo,
                marcador = marcadores[partido.id],
                onAbrir = {
                    onAbrirPartido(partido.id)
                }
            )
        }
    }
}

@Composable
private fun TarjetaPartidoSportPro(
    partido: Partido,
    nombreEquipo: String,
    marcador: MarcadorPartido?,
    onAbrir: () -> Unit
) {
    val enCurso =
        partido.estado == EstadoPartido.EN_CURSO

    val marcadorEnVivo by produceState<MarcadorPartido?>(null, partido.id, enCurso) {
        if (enCurso) {
            RepositorioSincronizacion().escucharEventos(partido.id)
                .catch { value = null }
                .collect { value = RepositorioEventosPartido().calcularMarcador(it.eventos) }
        }
    }
    val marcadorVisible = if (enCurso) marcadorEnVivo else marcador
    val fondo = when (partido.estado) {
        EstadoPartido.EN_CURSO ->
            SportProNavyFondo

        EstadoPartido.CANCELADO ->
            SportProRojoClaro

        else ->
            SportProSuperficie
    }

    val textoPrincipal = when (partido.estado) {
        EstadoPartido.EN_CURSO ->
            Color.White

        EstadoPartido.CANCELADO ->
            SportProRojo

        else ->
            SportProNavy
    }

    val textoSecundario = when (partido.estado) {
        EstadoPartido.EN_CURSO ->
            Color(0xFFD7E5EC)

        EstadoPartido.CANCELADO ->
            SportProRojo

        else ->
            SportProTextoSecundario
    }

    // Línea de cal (6.8): ticket con talón amarillo; el partido en curso va en un ticket oscuro
    TarjetaTicket(
        hora = partido.hora,
        fecha = fechaCortaPartido(partido.fecha),
        variante = when (partido.estado) {
            EstadoPartido.EN_CURSO -> VarianteTicket.OSCURA
            EstadoPartido.PROGRAMADO -> VarianteTicket.AMARILLA
            EstadoPartido.FINALIZADO -> VarianteTicket.NORMAL
            EstadoPartido.CANCELADO -> VarianteTicket.TACHADA
        },
        onClick = onAbrir
    ) {
        Column(
            modifier = Modifier,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                ChipEstadoPartido(
                    estado = partido.estado
                )

                TextButton(
                    onClick = onAbrir
                ) {
                    val colorAccion = if (enCurso) {
                        LineaDeCal.colores.lima /* antes 0xFFA7F36A */
                    } else {
                        SportProVerde
                    }
                    Text(
                        text = if (enCurso) {
                            "Ver detalles"
                        } else {
                            "Ver partido"
                        },
                        color = colorAccion,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        tint = colorAccion,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text = nombreEquipo,
                style = MaterialTheme.typography.bodySmall,
                color = textoSecundario
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = nombreEquipo,
                    style = MaterialTheme.typography.titleMedium,
                    color = textoPrincipal,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                if (
                    (enCurso || partido.estado == EstadoPartido.FINALIZADO) &&
                    marcadorVisible != null
                ) {

                    Text(
                        text = "${marcadorVisible.propio}–${marcadorVisible.rival}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = if (enCurso) Color.White else SportProVerde,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(
                            horizontal = 12.dp
                        )
                    )

                } else {

                    Text(
                        text = if (enCurso) "…" else "VS",
                        style = MaterialTheme.typography.titleLarge,
                        color = if (enCurso) Color.White else SportProVerde,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(
                            horizontal = 12.dp
                        )
                    )
                }

                Text(
                    text = partido.nombreRival,
                    style = MaterialTheme.typography.titleMedium,
                    color = textoPrincipal,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = "${partido.fecha} · ${partido.hora} · ${partido.ubicacion}",
                style = MaterialTheme.typography.bodySmall,
                color = textoSecundario
            )
        }
    }
}

@Composable
private fun ChipEstadoPartido(
    estado: EstadoPartido
) {
    val fondo = when (estado) {
        EstadoPartido.EN_CURSO ->
            LineaDeCal.colores.lima /* antes 0xFFCBFFA7 */

        EstadoPartido.PROGRAMADO ->
            SportProVerdeClaro

        EstadoPartido.FINALIZADO ->
            LineaDeCal.colores.apagadoFondo /* antes 0xFFE9ECEE */

        EstadoPartido.CANCELADO ->
            SportProRojoClaro
    }

    val texto = when (estado) {
        EstadoPartido.EN_CURSO ->
            SportProVerde

        EstadoPartido.PROGRAMADO ->
            SportProVerde

        EstadoPartido.FINALIZADO ->
            SportProTextoSecundario

        EstadoPartido.CANCELADO ->
            SportProRojo
    }

    // Línea de cal: ChipEstado con los 4 estados (Programado, En curso, Finalizado y Cancelado)
    ChipEstado(
        textoEstadoPartido(estado),
        when (estado) {
            EstadoPartido.EN_CURSO -> TipoEstado.OK
            EstadoPartido.PROGRAMADO -> TipoEstado.NEUTRO
            EstadoPartido.FINALIZADO -> TipoEstado.APAGADO
            EstadoPartido.CANCELADO -> TipoEstado.ERROR
        },
        conPunto = estado == EstadoPartido.EN_CURSO
    )
}

/** "2026-11-28" → "SÁB 28" para el talón del ticket; si no se puede leer, se deja igual. */
private fun fechaCortaPartido(fecha: String): String =
    try {
        java.time.LocalDate.parse(fecha.trim())
            .format(java.time.format.DateTimeFormatter.ofPattern("EEE d", java.util.Locale.forLanguageTag("es-PE")))
            .replace(".", "")
            .uppercase()
    } catch (e: Exception) {
        fecha
    }

private fun textoEstadoPartido(
    estado: EstadoPartido
): String =
    when (estado) {
        EstadoPartido.PROGRAMADO ->
            "PROGRAMADO"

        EstadoPartido.EN_CURSO ->
            "EN CURSO"

        EstadoPartido.FINALIZADO ->
            "FINALIZADO"

        EstadoPartido.CANCELADO ->
            "CANCELADO"
    }