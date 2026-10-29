package com.example.projectsportpro2026_2.ui.eventospartido

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.projectsportpro2026_2.datos.modelo.Jugador
import com.example.projectsportpro2026_2.datos.modelo.evento.AuditoriaEventoPartido
import com.example.projectsportpro2026_2.datos.modelo.evento.EquipoEvento
import com.example.projectsportpro2026_2.datos.modelo.evento.EstadoEventoPartido
import com.example.projectsportpro2026_2.datos.modelo.evento.EventoPartido
import com.example.projectsportpro2026_2.datos.modelo.evento.TipoCambioEvento
import com.example.projectsportpro2026_2.ui.partidos.FormaBotonSportPro
import com.example.projectsportpro2026_2.ui.partidos.SportProBorde
import com.example.projectsportpro2026_2.ui.partidos.SportProFondo
import com.example.projectsportpro2026_2.ui.partidos.SportProNavy
import com.example.projectsportpro2026_2.ui.partidos.SportProRojo
import com.example.projectsportpro2026_2.ui.partidos.SportProRojoClaro
import com.example.projectsportpro2026_2.ui.partidos.SportProSuperficie
import com.example.projectsportpro2026_2.ui.partidos.SportProTextoPrincipal
import com.example.projectsportpro2026_2.ui.partidos.SportProTextoSecundario
import com.example.projectsportpro2026_2.ui.partidos.SportProVerde
import com.example.projectsportpro2026_2.ui.partidos.SportProVerdeClaro
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalGestionEvento(
    evento: EventoPartido,
    accion: AccionEventoPartido?,
    formulario: FormularioEventoPartido,
    motivo: String,
    jugadores: List<Jugador>,
    nombreEquipo: String,
    nombreRival: String,
    auditoria: List<AuditoriaEventoPartido>,
    cargandoAuditoria: Boolean,
    procesando: Boolean,
    error: String?,
    onCerrar: () -> Unit,
    onCorregir: () -> Unit,
    onAnular: () -> Unit,
    onCancelarAccion: () -> Unit,
    onMotivo: (String) -> Unit,
    onMinuto: (String) -> Unit,
    onEquipo: (EquipoEvento) -> Unit,
    onJugador: (String) -> Unit,
    onJugadorRival: (String) -> Unit,
    onObservacion: (String) -> Unit,
    onGuardarCorreccion: () -> Unit,
    onConfirmarAnulacion: () -> Unit
) {
    val hoja =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )

    ModalBottomSheet(
        onDismissRequest = onCerrar,
        sheetState = hoja,
        containerColor = SportProSuperficie,
        shape = RoundedCornerShape(
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
                Arrangement.spacedBy(14.dp)
        ) {

            Text(
                text = "DETALLE DEL EVENTO",
                color = SportProVerde,
                fontWeight = FontWeight.Black
            )

            Text(
                text =
                    "${evento.minuto}' · ${evento.nombreTipoEvento}",
                color = SportProNavy,
                fontWeight = FontWeight.Black
            )

            if (accion == null) {

                DetalleEvento(
                    evento = evento,
                    auditoria = auditoria,
                    cargandoAuditoria =
                        cargandoAuditoria
                )

                if (
                    evento.estado ==
                    EstadoEventoPartido.ACTIVO
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        OutlinedButton(
                            onClick = onCorregir,
                            modifier = Modifier.weight(1f),
                            shape = FormaBotonSportPro
                        ) {
                            Text("Corregir")
                        }

                        OutlinedButton(
                            onClick = onAnular,
                            modifier = Modifier.weight(1f),
                            shape = FormaBotonSportPro,
                            colors =
                                ButtonDefaults.outlinedButtonColors(
                                    contentColor = SportProRojo
                                )
                        ) {
                            Text("Anular")
                        }
                    }

                } else {

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = SportProRojoClaro,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Este evento está anulado y se conserva únicamente para auditoría.",
                            modifier = Modifier.padding(12.dp),
                            color = SportProRojo
                        )
                    }
                }

                TextButton(
                    onClick = onCerrar,
                    modifier =
                        Modifier.fillMaxWidth()
                ) {
                    Text("Cerrar")
                }
            }

            if (
                accion ==
                AccionEventoPartido.CORREGIR
            ) {

                FormularioCorreccion(
                    formulario =
                        formulario,

                    motivo =
                        motivo,

                    jugadores =
                        jugadores,

                    nombreEquipo =
                        nombreEquipo,

                    nombreRival =
                        nombreRival,

                    procesando =
                        procesando,

                    error =
                        error,

                    onMotivo =
                        onMotivo,

                    onMinuto =
                        onMinuto,

                    onEquipo =
                        onEquipo,

                    onJugador =
                        onJugador,

                    onJugadorRival =
                        onJugadorRival,

                    onObservacion =
                        onObservacion,

                    onGuardar =
                        onGuardarCorreccion,

                    onCancelar =
                        onCancelarAccion
                )
            }

            if (
                accion ==
                AccionEventoPartido.ANULAR
            ) {

                Surface(
                    modifier =
                        Modifier.fillMaxWidth(),
                    color =
                        SportProRojoClaro,
                    shape =
                        RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text =
                            "El evento no se eliminará. Quedará registrado como ANULADO para mantener la trazabilidad.",
                        modifier =
                            Modifier.padding(12.dp),
                        color =
                            SportProRojo
                    )
                }

                OutlinedTextField(
                    value = motivo,
                    onValueChange =
                        onMotivo,
                    modifier =
                        Modifier.fillMaxWidth(),
                    enabled =
                        !procesando,
                    minLines = 2,
                    label = {
                        Text(
                            "Motivo de anulación *"
                        )
                    }
                )

                error?.let {
                    Text(
                        text = it,
                        color = SportProRojo
                    )
                }

                Button(
                    onClick =
                        onConfirmarAnulacion,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled =
                        !procesando,
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor =
                                SportProRojo
                        )
                ) {
                    if (procesando) {
                        CircularProgressIndicator(
                            modifier =
                                Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "Confirmar anulación"
                        )
                    }
                }

                TextButton(
                    onClick =
                        onCancelarAccion,
                    modifier =
                        Modifier.fillMaxWidth(),
                    enabled =
                        !procesando
                ) {
                    Text("Cancelar")
                }
            }
        }
    }
}

@Composable
private fun DetalleEvento(
    evento: EventoPartido,
    auditoria:
    List<AuditoriaEventoPartido>,
    cargandoAuditoria: Boolean
) {

    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        color = SportProFondo,
        shape =
            RoundedCornerShape(14.dp)
    ) {

        Column(
            modifier =
                Modifier.padding(14.dp),
            verticalArrangement =
                Arrangement.spacedBy(7.dp)
        ) {

            DatoDetalle(
                "Estado",
                evento.estado.name
            )

            DatoDetalle(
                "Versión",
                evento.version.toString()
            )

            if (
                evento.nombreJugador
                    .isNotBlank()
            ) {
                DatoDetalle(
                    "Jugador",
                    evento.nombreJugador
                )
            }

            DatoDetalle(
                "Equipo",
                textoEquipo(
                    evento.equipoEvento
                )
            )

            if (
                evento.observacion
                    .isNotBlank()
            ) {
                DatoDetalle(
                    "Observación",
                    evento.observacion
                )
            }

            DatoDetalle(
                "Creado por",
                evento.creadoPor
            )

            DatoDetalle(
                "Fecha de captura",
                fechaHora(
                    evento.fechaCaptura
                )
            )

            if (
                evento.modificadoPor
                    .isNotBlank()
            ) {
                DatoDetalle(
                    "Última modificación",
                    evento.modificadoPor
                )
            }

            if (
                evento.anuladoPor
                    .isNotBlank()
            ) {
                DatoDetalle(
                    "Anulado por",
                    evento.anuladoPor
                )
            }
        }
    }

    Text(
        text = "AUDITORÍA",
        color = SportProTextoSecundario,
        fontWeight = FontWeight.Black
    )

    if (cargandoAuditoria) {

        CircularProgressIndicator(
            modifier =
                Modifier.size(24.dp),
            color = SportProVerde
        )

    } else if (auditoria.isEmpty()) {

        Text(
            text =
                "Este evento todavía no tiene correcciones ni anulaciones.",
            color =
                SportProTextoSecundario
        )

    } else {

        auditoria.forEach {
                movimiento ->

            TarjetaAuditoria(
                movimiento =
                    movimiento
            )
        }
    }
}

@Composable
private fun TarjetaAuditoria(
    movimiento:
    AuditoriaEventoPartido
) {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        color =
            if (
                movimiento.tipoCambio ==
                TipoCambioEvento.ANULACION
            ) {
                SportProRojoClaro
            } else {
                SportProVerdeClaro
            },
        shape =
            RoundedCornerShape(12.dp)
    ) {

        Column(
            modifier =
                Modifier.padding(12.dp),
            verticalArrangement =
                Arrangement.spacedBy(4.dp)
        ) {

            Text(
                text =
                    if (
                        movimiento.tipoCambio ==
                        TipoCambioEvento.ANULACION
                    ) {
                        "ANULACIÓN"
                    } else {
                        "CORRECCIÓN"
                    },
                color =
                    if (
                        movimiento.tipoCambio ==
                        TipoCambioEvento.ANULACION
                    ) {
                        SportProRojo
                    } else {
                        SportProVerde
                    },
                fontWeight =
                    FontWeight.Black
            )

            Text(
                text =
                    "Versión ${movimiento.versionAnterior} → ${movimiento.versionNueva}",
                color =
                    SportProTextoPrincipal
            )

            Text(
                text =
                    "Motivo: ${movimiento.motivo}",
                color =
                    SportProTextoPrincipal
            )

            Text(
                text =
                    "Autor: ${movimiento.autor}",
                color =
                    SportProTextoSecundario
            )

            Text(
                text =
                    fechaHora(
                        movimiento.fechaCambio
                    ),
                color =
                    SportProTextoSecundario
            )

            if (
                movimiento.tipoCambio ==
                TipoCambioEvento.CORRECCION
            ) {

                Spacer(
                    Modifier.height(4.dp)
                )

                Text(
                    text =
                        "Antes: ${resumenDatos(movimiento.valoresAnteriores)}",
                    color =
                        SportProTextoSecundario
                )

                Text(
                    text =
                        "Después: ${resumenDatos(movimiento.valoresNuevos)}",
                    color =
                        SportProTextoPrincipal
                )
            }
        }
    }
}

@Composable
private fun FormularioCorreccion(
    formulario:
    FormularioEventoPartido,
    motivo: String,
    jugadores: List<Jugador>,
    nombreEquipo: String,
    nombreRival: String,
    procesando: Boolean,
    error: String?,
    onMotivo: (String) -> Unit,
    onMinuto: (String) -> Unit,
    onEquipo: (EquipoEvento) -> Unit,
    onJugador: (String) -> Unit,
    onJugadorRival: (String) -> Unit,
    onObservacion: (String) -> Unit,
    onGuardar: () -> Unit,
    onCancelar: () -> Unit
) {
    val tipo =
        formulario.tipo
            ?: return

    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        color = SportProFondo,
        shape =
            RoundedCornerShape(12.dp)
    ) {
        Text(
            text =
                "CORRIGIENDO · ${tipo.nombre}",
            modifier =
                Modifier.padding(12.dp),
            color =
                SportProNavy,
            fontWeight =
                FontWeight.Bold
        )
    }

    OutlinedTextField(
        value =
            formulario.minuto,
        onValueChange =
            onMinuto,
        modifier =
            Modifier.fillMaxWidth(),
        enabled =
            !procesando,
        label = {
            Text("Minuto")
        },
        singleLine = true
    )

    if (tipo.requiereEquipo) {

        Text(
            text = "EQUIPO",
            color =
                SportProTextoSecundario,
            fontWeight =
                FontWeight.Bold
        )

        Row(
            modifier =
                Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {

            BotonEquipo(
                texto =
                    nombreEquipo,

                seleccionado =
                    formulario.equipoEvento ==
                            EquipoEvento.PROPIO,

                modifier =
                    Modifier.weight(1f),

                onClick = {
                    onEquipo(
                        EquipoEvento.PROPIO
                    )
                }
            )

            BotonEquipo(
                texto =
                    nombreRival,

                seleccionado =
                    formulario.equipoEvento ==
                            EquipoEvento.RIVAL,

                modifier =
                    Modifier.weight(1f),

                onClick = {
                    onEquipo(
                        EquipoEvento.RIVAL
                    )
                }
            )
        }
    }

    if (tipo.requiereJugador) {

        if (
            formulario.equipoEvento ==
            EquipoEvento.RIVAL
        ) {

            OutlinedTextField(
                value =
                    formulario
                        .nombreJugadorRival,

                onValueChange =
                    onJugadorRival,

                modifier =
                    Modifier.fillMaxWidth(),

                enabled =
                    !procesando,

                label = {
                    Text("Jugador rival")
                },

                singleLine = true
            )

        } else {

            SelectorJugadorCorreccion(
                jugadores =
                    jugadores,

                idSeleccionado =
                    formulario.idJugador,

                habilitado =
                    !procesando,

                onSeleccionar =
                    onJugador
            )
        }
    }

    if (
        tipo.requiereObservacion
    ) {

        OutlinedTextField(
            value =
                formulario.observacion,

            onValueChange =
                onObservacion,

            modifier =
                Modifier.fillMaxWidth(),

            enabled =
                !procesando,

            minLines = 2,

            label = {
                Text("Observación")
            }
        )
    }

    OutlinedTextField(
        value = motivo,
        onValueChange =
            onMotivo,
        modifier =
            Modifier.fillMaxWidth(),
        enabled =
            !procesando,
        minLines = 2,
        label = {
            Text(
                "Motivo de corrección *"
            )
        }
    )

    error?.let {
        Text(
            text = it,
            color = SportProRojo
        )
    }

    Button(
        onClick = onGuardar,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        enabled =
            !procesando,
        shape =
            FormaBotonSportPro,
        colors =
            ButtonDefaults.buttonColors(
                containerColor =
                    SportProVerde
            )
    ) {

        if (procesando) {

            CircularProgressIndicator(
                modifier =
                    Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )

        } else {

            Text(
                text =
                    "Guardar corrección",
                fontWeight =
                    FontWeight.Bold
            )
        }
    }

    TextButton(
        onClick = onCancelar,
        modifier =
            Modifier.fillMaxWidth(),
        enabled =
            !procesando
    ) {
        Text("Cancelar")
    }
}

@Composable
private fun BotonEquipo(
    texto: String,
    seleccionado: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    if (seleccionado) {

        Button(
            onClick = onClick,
            modifier = modifier,
            colors =
                ButtonDefaults.buttonColors(
                    containerColor =
                        SportProVerde
                )
        ) {
            Text(
                text = texto,
                maxLines = 1,
                overflow =
                    TextOverflow.Ellipsis
            )
        }

    } else {

        OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            border =
                BorderStroke(
                    1.dp,
                    SportProBorde
                )
        ) {
            Text(
                text = texto,
                maxLines = 1,
                overflow =
                    TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SelectorJugadorCorreccion(
    jugadores: List<Jugador>,
    idSeleccionado: String,
    habilitado: Boolean,
    onSeleccionar: (String) -> Unit
) {
    var abierto by remember {
        mutableStateOf(false)
    }

    val jugador =
        jugadores.firstOrNull {
            it.id ==
                    idSeleccionado
        }

    Box(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        OutlinedButton(
            onClick = {
                if (habilitado) {
                    abierto = true
                }
            },
            modifier =
                Modifier.fillMaxWidth()
        ) {

            Text(
                text =
                    jugador
                        ?.nombreCompleto()
                        ?: "Seleccionar jugador",
                maxLines = 1,
                overflow =
                    TextOverflow.Ellipsis
            )
        }

        DropdownMenu(
            expanded = abierto,
            onDismissRequest = {
                abierto = false
            }
        ) {

            jugadores.forEach {
                    actual ->

                DropdownMenuItem(
                    text = {
                        Text(
                            actual
                                .nombreCompleto()
                        )
                    },

                    onClick = {
                        onSeleccionar(
                            actual.id
                        )

                        abierto =
                            false
                    }
                )
            }
        }
    }
}

@Composable
private fun DatoDetalle(
    etiqueta: String,
    valor: String
) {
    Row(
        modifier =
            Modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.SpaceBetween
    ) {

        Text(
            text = etiqueta,
            color =
                SportProTextoSecundario
        )

        Text(
            text = valor,
            color =
                SportProTextoPrincipal,
            fontWeight =
                FontWeight.Medium
        )
    }
}

private fun textoEquipo(
    equipo: EquipoEvento
): String =
    when (equipo) {
        EquipoEvento.PROPIO ->
            "Propio"

        EquipoEvento.RIVAL ->
            "Rival"

        EquipoEvento.NINGUNO ->
            "No aplica"
    }

private fun fechaHora(
    milisegundos: Long
): String {

    if (milisegundos <= 0L) {
        return "-"
    }

    return SimpleDateFormat(
        "dd/MM/yyyy HH:mm",
        Locale.getDefault()
    ).format(
        Date(milisegundos)
    )
}

private fun resumenDatos(
    datos:
    com.example.projectsportpro2026_2.datos.modelo.evento.DatosEventoAuditoria
): String {

    val partes =
        mutableListOf<String>()

    partes +=
        "${datos.minuto}'"

    partes +=
        datos.nombreTipoEvento

    if (
        datos.nombreJugador
            .isNotBlank()
    ) {
        partes +=
            datos.nombreJugador
    }

    if (
        datos.equipoEvento !=
        EquipoEvento.NINGUNO
    ) {
        partes +=
            textoEquipo(
                datos.equipoEvento
            )
    }

    return partes.joinToString(
        " · "
    )
}
