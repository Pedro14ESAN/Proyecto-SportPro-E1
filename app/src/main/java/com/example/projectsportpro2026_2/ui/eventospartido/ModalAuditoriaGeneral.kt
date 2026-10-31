package com.example.projectsportpro2026_2.ui.eventospartido

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.projectsportpro2026_2.datos.modelo.evento.EstadoEventoPartido
import com.example.projectsportpro2026_2.datos.modelo.evento.EventoPartido
import com.example.projectsportpro2026_2.ui.partidos.SportProBorde
import com.example.projectsportpro2026_2.ui.partidos.SportProNavy
import com.example.projectsportpro2026_2.ui.partidos.SportProRojo
import com.example.projectsportpro2026_2.ui.partidos.SportProRojoClaro
import com.example.projectsportpro2026_2.ui.partidos.SportProSuperficie
import com.example.projectsportpro2026_2.ui.partidos.SportProTextoPrincipal
import com.example.projectsportpro2026_2.ui.partidos.SportProTextoSecundario
import com.example.projectsportpro2026_2.ui.partidos.SportProVerde
import com.example.projectsportpro2026_2.ui.partidos.SportProVerdeClaro

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalAuditoriaGeneral(
    eventos: List<EventoPartido>,
    cargando: Boolean,
    onCerrar: () -> Unit,
    onAbrirEvento: (EventoPartido) -> Unit
) {

    val hoja =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )

    ModalBottomSheet(
        onDismissRequest =
            onCerrar,

        sheetState =
            hoja,

        containerColor =
            SportProSuperficie,

        shape =
            RoundedCornerShape(
                topStart = 26.dp,
                topEnd = 26.dp
            )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    bottom = 30.dp
                ),

            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween,

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Column {

                    Text(
                        text = "AUDITORÍA",
                        color = SportProVerde,
                        fontWeight = FontWeight.Black
                    )

                    Text(
                        text = "Historial de eventos",
                        color = SportProNavy,
                        fontWeight = FontWeight.Black
                    )
                }

                Surface(
                    modifier = Modifier
                        .size(34.dp)
                        .clickable {
                            onCerrar()
                        },

                    shape =
                        CircleShape,

                    color =
                        Color(0xFFF2F3F1)
                ) {

                    Box(
                        contentAlignment =
                            Alignment.Center
                    ) {
                        Text(
                            text = "×",
                            color =
                                SportProNavy,

                            fontWeight =
                                FontWeight.Bold
                        )
                    }
                }
            }

            Text(
                text =
                    "Aquí se conservan también los eventos anulados.",
                color =
                    SportProTextoSecundario
            )

            if (cargando) {

                Box(
                    modifier =
                        Modifier.fillMaxWidth(),

                    contentAlignment =
                        Alignment.Center
                ) {

                    CircularProgressIndicator(
                        color =
                            SportProVerde
                    )
                }

            } else if (
                eventos.isEmpty()
            ) {

                Text(
                    text =
                        "No existen eventos para auditar.",
                    color =
                        SportProTextoSecundario
                )

            } else {

                eventos.forEach {
                        evento ->

                    TarjetaEventoAuditoria(
                        evento =
                            evento,

                        onClick = {
                            onAbrirEvento(
                                evento
                            )
                        }
                    )
                }
            }

            TextButton(
                onClick =
                    onCerrar,

                modifier =
                    Modifier.fillMaxWidth()
            ) {
                Text("Cerrar")
            }
        }
    }
}

@Composable
private fun TarjetaEventoAuditoria(
    evento: EventoPartido,
    onClick: () -> Unit
) {

    val anulado =
        evento.estado ==
                EstadoEventoPartido.ANULADO

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick
            ),

        color =
            if (anulado) {
                SportProRojoClaro
            } else {
                SportProSuperficie
            },

        shape =
            RoundedCornerShape(14.dp),

        border =
            BorderStroke(
                1.dp,
                if (anulado) {
                    SportProRojo
                } else {
                    SportProBorde
                }
            )
    ) {

        Row(
            modifier =
                Modifier.padding(14.dp),

            horizontalArrangement =
                Arrangement.spacedBy(12.dp),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Column(
                modifier =
                    Modifier.weight(1f)
            ) {

                Text(
                    text =
                        "${evento.minuto}' · ${evento.nombreTipoEvento}",
                    color =
                        SportProTextoPrincipal,
                    fontWeight =
                        FontWeight.Bold
                )

                if (
                    evento.nombreJugador
                        .isNotBlank()
                ) {

                    Text(
                        text =
                            evento.nombreJugador,
                        color =
                            SportProTextoSecundario,
                        maxLines = 1,
                        overflow =
                            TextOverflow.Ellipsis
                    )
                }

                Text(
                    text =
                        "Versión ${evento.version}",
                    color =
                        SportProTextoSecundario
                )
            }

            Surface(
                color =
                    if (anulado) {
                        SportProRojoClaro
                    } else {
                        SportProVerdeClaro
                    },

                shape =
                    RoundedCornerShape(20.dp)
            ) {

                Text(
                    text =
                        if (anulado) {
                            "ANULADO"
                        } else {
                            "ACTIVO"
                        },

                    modifier =
                        Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),

                    color =
                        if (anulado) {
                            SportProRojo
                        } else {
                            SportProVerde
                        },

                    fontWeight =
                        FontWeight.Bold
                )
            }
        }
    }
}