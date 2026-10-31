package com.example.projectsportpro2026_2.ui.seguimiento

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectsportpro2026_2.datos.modelo.Rol
import com.example.projectsportpro2026_2.datos.modelo.partido.EstadoPartido
import com.example.projectsportpro2026_2.datos.repositorio.seguimiento.PartidoSeguible
import com.example.projectsportpro2026_2.ui.partidos.SportProBorde
import com.example.projectsportpro2026_2.ui.partidos.SportProFondo
import com.example.projectsportpro2026_2.ui.partidos.SportProNavy
import com.example.projectsportpro2026_2.ui.partidos.SportProSuperficie
import com.example.projectsportpro2026_2.ui.partidos.SportProTextoPrincipal
import com.example.projectsportpro2026_2.ui.partidos.SportProTextoSecundario
import com.example.projectsportpro2026_2.ui.partidos.SportProVerde
import com.example.projectsportpro2026_2.ui.sincronizacion.SyncNaranjaFondo
import com.example.projectsportpro2026_2.ui.sincronizacion.SyncNaranjaTexto
import com.example.projectsportpro2026_2.ui.sincronizacion.SyncRojoFondo
import com.example.projectsportpro2026_2.ui.sincronizacion.SyncRojoTexto
import com.example.projectsportpro2026_2.ui.sincronizacion.SyncVerde
import com.example.projectsportpro2026_2.ui.sincronizacion.SyncVerdeBorde
import com.example.projectsportpro2026_2.ui.sincronizacion.SyncVerdeFondo
import com.example.projectsportpro2026_2.ui.sincronizacion.SyncVerdeTexto
import com.example.projectsportpro2026_2.ui.sincronizacion.VivoFondoOscuro

/*
 * HU17 - Partidos que el usuario puede seguir en vivo.
 * La lista también se actualiza en tiempo real.
 */
@Composable
fun PantallaMisPartidos(
    onVolver: () -> Unit,
    onAbrirPartido: (String) -> Unit,
    viewModel: MisPartidosViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.iniciar()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SportProFondo),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 18.dp,
            bottom = 32.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(VivoFondoOscuro, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("⚽")
                }

                Spacer(Modifier.width(10.dp))

                Column {
                    Text(
                        text = "SPORTPRO",
                        color = SportProNavy,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "PARTIDOS EN VIVO",
                        color = SportProTextoSecundario,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        item {
            TextButton(
                onClick = onVolver,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "← Inicio",
                    color = SportProVerde,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        item {
            Text(
                text = when (estado.rol) {
                    Rol.PAD -> "Partidos de tus jugadores"
                    Rol.JUG -> "Partidos de tu equipo"
                    else -> "Partidos de tus academias"
                },
                color = SportProNavy,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black
            )
        }

        if (estado.desdeCache && estado.partidos.isNotEmpty()) {
            item {
                Text(
                    text = "Sin conexión · la lista puede estar desactualizada",
                    color = SyncNaranjaTexto,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
            }
        }

        if (estado.cargando) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = SyncVerde)
                }
            }
            return@LazyColumn
        }

        estado.error?.let { mensaje ->
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = SyncRojoFondo,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = mensaje,
                        modifier = Modifier.padding(12.dp),
                        color = SyncRojoTexto,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        if (estado.partidos.isEmpty() && estado.error == null) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = SportProSuperficie,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, SportProBorde)
                ) {
                    Text(
                        text = "Todavía no hay partidos para mostrar.",
                        modifier = Modifier.padding(20.dp),
                        color = SportProTextoSecundario
                    )
                }
            }
        }

        estado.partidos.forEach { seguible ->
            item(key = seguible.partido.id) {
                TarjetaPartidoSeguible(
                    seguible = seguible,
                    onClick = {
                        onAbrirPartido(seguible.partido.id)
                    }
                )
            }
        }
    }
}

@Composable
private fun TarjetaPartidoSeguible(
    seguible: PartidoSeguible,
    onClick: () -> Unit
) {
    val partido = seguible.partido
    val enVivo = partido.estado == EstadoPartido.EN_CURSO
    val habilitado = partido.estado != EstadoPartido.CANCELADO

    val (textoEstado, fondoEstado, colorEstado) =
        when (partido.estado) {
            EstadoPartido.EN_CURSO -> Triple("● EN VIVO", SyncVerdeFondo, SyncVerdeTexto)
            EstadoPartido.PROGRAMADO -> Triple("PROGRAMADO", SyncNaranjaFondo, SyncNaranjaTexto)
            EstadoPartido.FINALIZADO -> Triple("FINALIZADO", Color(0xFFEEF2F1), SportProTextoSecundario)
            EstadoPartido.CANCELADO -> Triple("CANCELADO", SyncRojoFondo, SyncRojoTexto)
        }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = habilitado, onClick = onClick),
        color = if (enVivo) SyncVerdeFondo else SportProSuperficie,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (enVivo) SyncVerdeBorde else SportProBorde)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${seguible.equipo.nombre} vs ${partido.nombreRival}",
                    modifier = Modifier.weight(1f),
                    color = SportProTextoPrincipal,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Surface(
                    shape = RoundedCornerShape(50),
                    color = fondoEstado
                ) {
                    Text(
                        text = textoEstado,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                        color = colorEstado,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }

            Text(
                text = listOf(partido.fecha, partido.hora, partido.ubicacion)
                    .filter { it.isNotBlank() }
                    .joinToString(" · "),
                color = SportProTextoSecundario,
                style = MaterialTheme.typography.bodySmall
            )

            if (seguible.equipo.jugadores.isNotEmpty()) {
                Text(
                    text = "Juega: ${seguible.equipo.jugadores.joinToString(", ")}",
                    color = SportProTextoSecundario,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (habilitado) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (enVivo) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .background(SyncVerde, CircleShape)
                        )
                        Spacer(Modifier.width(6.dp))
                    }

                    Text(
                        text = if (enVivo) "Seguir en vivo →" else "Ver partido →",
                        color = SportProVerde,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
