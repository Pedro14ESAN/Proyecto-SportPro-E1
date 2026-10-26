package com.example.projectsportpro2026_2.ui.partidos

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
                containerColor = SportProVerde,
                contentColor = Color.White,
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "+",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
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
                        titulo = "EN CURSO",
                        partidos = enCurso,
                        equipos = estado.equipos,
                        onAbrirPartido = onAbrirPartido
                    )
                }

                seccionPartidos(
                    titulo = "PRÓXIMOS",
                    partidos = proximos,
                    equipos = estado.equipos,
                    onAbrirPartido = onAbrirPartido
                )

                seccionPartidos(
                    titulo = "FINALIZADOS",
                    partidos = finalizados,
                    equipos = estado.equipos,
                    onAbrirPartido = onAbrirPartido
                )

                seccionPartidos(
                    titulo = "CANCELADOS",
                    partidos = cancelados,
                    equipos = estado.equipos,
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 18.dp, bottom = 4.dp)
    ) {

        TextButton(
            onClick = onVolver
        ) {
            Text(
                text = "← Volver",
                color = SportProVerde
            )
        }

        Text(
            text = "TEMPORADA 2026",
            style = MaterialTheme.typography.labelMedium,
            color = SportProVerde,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        Text(
            text = "Partidos",
            style = MaterialTheme.typography.headlineLarge,
            color = SportProNavy,
            fontWeight = FontWeight.Black
        )

        if (nombreAcademia.isNotBlank()) {
            Text(
                text = nombreAcademia,
                style = MaterialTheme.typography.bodySmall,
                color = SportProTextoSecundario
            )
        }
    }
}

private fun LazyListScope.seccionPartidos(
    titulo: String,
    partidos: List<Partido>,
    equipos: List<Equipo>,
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
    onAbrir: () -> Unit
) {
    val enCurso =
        partido.estado == EstadoPartido.EN_CURSO

    val fondo = when (partido.estado) {
        EstadoPartido.EN_CURSO ->
            SportProNavy

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

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = FormaTarjetaSportPro,
        colors = CardDefaults.cardColors(
            containerColor = fondo
        ),
        border = if (partido.estado == EstadoPartido.EN_CURSO) {
            null
        } else {
            CardDefaults.outlinedCardBorder()
        }
    ) {

        Column(
            modifier = Modifier.padding(16.dp),
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
                    Text(
                        text = if (enCurso) {
                            "Ver detalles →"
                        } else {
                            "Ver partido →"
                        },
                        color = if (enCurso) {
                            Color(0xFFA7F36A)
                        } else {
                            SportProVerde
                        },
                        fontWeight = FontWeight.Bold
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

                Text(
                    text = "VS",
                    style = MaterialTheme.typography.titleLarge,
                    color = SportProVerde,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(
                        horizontal = 12.dp
                    )
                )

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
            Color(0xFFCBFFA7)

        EstadoPartido.PROGRAMADO ->
            SportProVerdeClaro

        EstadoPartido.FINALIZADO ->
            Color(0xFFE9ECEE)

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

    Surface(
        color = fondo,
        shape = CircleShape
    ) {
        Text(
            text = textoEstadoPartido(estado),
            style = MaterialTheme.typography.labelSmall,
            color = texto,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 6.dp
            )
        )
    }
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