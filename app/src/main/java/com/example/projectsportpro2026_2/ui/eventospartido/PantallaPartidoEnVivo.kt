package com.example.projectsportpro2026_2.ui.eventospartido

import com.example.projectsportpro2026_2.ui.partidos.SportProNavyFondo
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import com.example.projectsportpro2026_2.datos.alineacion.calcularSustituciones
import com.example.projectsportpro2026_2.datos.alineacion.opcionesSustitucion
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.example.projectsportpro2026_2.ui.alineacion.AlineacionPartidoEnVivo
import com.example.projectsportpro2026_2.ui.sincronizacion.ResumenEventosEnVivo
import com.example.projectsportpro2026_2.ui.sincronizacion.IconoEvento
import com.example.projectsportpro2026_2.ui.sincronizacion.JugadoresEvento
import com.example.projectsportpro2026_2.ui.sincronizacion.estiloEventoEnVivo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.example.projectsportpro2026_2.datos.modelo.Jugador
import com.example.projectsportpro2026_2.datos.modelo.evento.TipoTarjeta
import com.example.projectsportpro2026_2.datos.modelo.evento.EquipoEvento
import com.example.projectsportpro2026_2.datos.modelo.evento.EventoPartido
import com.example.projectsportpro2026_2.datos.modelo.evento.TipoEventoCatalogo
import com.example.projectsportpro2026_2.datos.modelo.partido.EstadoPartido
import com.example.projectsportpro2026_2.datos.modelo.sincronizacion.EstadoConexionPartido
import com.example.projectsportpro2026_2.datos.modelo.sincronizacion.EstadoSincronizacionEvento
import com.example.projectsportpro2026_2.ui.partidos.FormaBotonSportPro
import com.example.projectsportpro2026_2.ui.sincronizacion.AvisoRegistroOfflineNoDisponible
import com.example.projectsportpro2026_2.ui.sincronizacion.BannerSincronizacion
import com.example.projectsportpro2026_2.ui.sincronizacion.ContadoresSincronizacion
import com.example.projectsportpro2026_2.ui.sincronizacion.IndicadorNubeSincronizacion
import com.example.projectsportpro2026_2.ui.sincronizacion.MensajeSincronizacion
import com.example.projectsportpro2026_2.ui.sincronizacion.ModalConflictoSincronizacion
import com.example.projectsportpro2026_2.ui.sincronizacion.SincronizacionViewModel
import com.example.projectsportpro2026_2.ui.sincronizacion.SyncNaranja
import com.example.projectsportpro2026_2.ui.sincronizacion.TarjetaEventoRechazado
import com.example.projectsportpro2026_2.ui.sincronizacion.TarjetaEventoSincronizado
import com.example.projectsportpro2026_2.ui.sincronizacion.textoAvisoMarcador
import com.example.projectsportpro2026_2.ui.partidos.FormaTarjetaSportPro
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPartidoEnVivo(
    idPartido: String,
    onVolver: () -> Unit,
    viewModel: PartidoEnVivoViewModel = viewModel(),
    // HU16: sincronización y registro sin conexión
    sincronizacion: SincronizacionViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()
    val sync by sincronizacion.estado.collectAsState()

    LaunchedEffect(idPartido) {
        viewModel.iniciar(idPartido)
        sincronizacion.iniciar(idPartido)
    }

    val contexto = estado.contexto
    val eventosActuales = remember(sync.cargado, sync.eventos, estado.eventos) {
        if (sync.cargado) sync.eventos.map { it.evento } else estado.eventos
    }
    val candidato = EventoPartido(
        id = estado.eventoSeleccionado?.id ?: "~nuevo",
        minuto = estado.formulario.minuto.toIntOrNull() ?: Int.MAX_VALUE,
        fechaCaptura = estado.eventoSeleccionado?.fechaCaptura ?: Long.MAX_VALUE
    )
    val opcionesCambio = remember(contexto, eventosActuales, candidato) {
        if (estado.eventoSeleccionado == null) {
            calcularSustituciones(contexto?.alineacionInicial.orEmpty(), eventosActuales)
        } else {
            opcionesSustitucion(contexto?.alineacionInicial.orEmpty(), eventosActuales, candidato)
        }
    }

    /*
     * HU16: el estado del partido llega en tiempo real
     * (otro dispositivo pudo finalizarlo).
     */
    val estadoPartidoActual =
        sync.partido?.estado
            ?: contexto?.partido?.estado

    /*
     * HU16: sin conexión y sin datos locales no se permite
     * un registro offline que no sea confiable.
     */
    val registroBloqueado =
        sync.sinConexion &&
                !sync.registroOfflineDisponible

    Scaffold(
        containerColor = SportProFondo,
        bottomBar = {
            Surface(color = SportProFondo, modifier = Modifier.navigationBarsPadding()) {
                BannerSincronizacion(
                    estado = sync,
                    onReintentar = sincronizacion::reintentarSincronizacion
                )
            }
        }
    ) { relleno ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(SportProFondo)
                .padding(relleno),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 18.dp,
                bottom = 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            item {
                CabeceraPartidoEnVivo(
                    estadoSincronizacion = sync.estadoGeneral
                )
            }

            item {
                TextButton(
                    onClick = onVolver,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "← Partidos",
                        color = SportProVerde,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (registroBloqueado) {
                item {
                    AvisoRegistroOfflineNoDisponible()
                }
            }

            sync.error?.let { mensaje ->
                item {
                    MensajeSincronizacion(
                        mensaje = mensaje,
                        error = true,
                        onCerrar = sincronizacion::limpiarError
                    )
                }
            }

            sync.mensaje?.let { mensaje ->
                item {
                    MensajeSincronizacion(
                        mensaje = mensaje,
                        error = false,
                        onCerrar = sincronizacion::limpiarMensaje
                    )
                }
            }

            if (
                estado.cargando &&
                contexto == null
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = SportProVerde
                        )
                    }
                }

                return@LazyColumn
            }

            estado.error?.let { mensaje ->
                item {
                    MensajePartidoEnVivo(
                        mensaje = mensaje,
                        error = true
                    )
                }
            }

            estado.mensaje?.let { mensaje ->
                item {
                    MensajePartidoEnVivo(
                        mensaje = mensaje,
                        error = false
                    )
                }
            }

            if (contexto == null) {
                item {
                    Text(
                        text = if (sync.sinConexion) {
                            "No se pudo abrir el partido sin conexión: " +
                                    "este dispositivo nunca lo había cargado."
                        } else {
                            "No se pudo cargar el partido en vivo."
                        },
                        color = SportProTextoSecundario
                    )
                }

                return@LazyColumn
            }

            item {
                TarjetaMarcador(
                    nombreEquipo =
                        contexto.equipo.nombre,

                    nombreRival =
                        contexto.partido.nombreRival,

                    // HU16: marcador desde el listener en tiempo real
                    golesPropios =
                        if (sync.cargado) {
                            sync.marcador.propio
                        } else {
                            estado.marcador.propio
                        },

                    golesRival =
                        if (sync.cargado) {
                            sync.marcador.rival
                        } else {
                            estado.marcador.rival
                        },

                    estadoPartido =
                        estadoPartidoActual
                            ?: contexto.partido.estado,

                    avisoRevision =
                        textoAvisoMarcador(sync)
                )
            }

            if (estadoPartidoActual == EstadoPartido.EN_CURSO) {
                item {
                    AlineacionPartidoEnVivo(
                        idPartido = idPartido,
                        idEquipo = contexto.partido.idEquipo,
                        nombreEquipo = contexto.equipo.nombre,
                        eventos = eventosActuales
                    )
                }
            }

            item {
                val eventosResumen = remember(sync.cargado, sync.eventos, estado.eventos) {
                    if (sync.cargado) {
                        sync.eventos
                            .filter { it.estado != EstadoSincronizacionEvento.CONFLICTO }
                            .map { it.evento }
                    } else {
                        estado.eventos
                    }
                }
                ResumenEventosEnVivo(eventos = eventosResumen)
            }

            if (
                estadoPartidoActual ==
                EstadoPartido.EN_CURSO &&
                !registroBloqueado
            ) {

                item {
                    Text(
                        text = "REGISTRO RÁPIDO",
                        color = SportProTextoSecundario,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black
                    )
                }

                item {
                    PanelEventosRapidos(
                        tipos =
                            contexto.tiposEvento,

                        onEvento = { tipo ->
                            viewModel.abrirEvento(
                                tipo
                            )
                        }
                    )
                }

            } else {

                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = SportProFondo,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            1.dp,
                            SportProBorde
                        )
                    ) {
                        Text(
                            text = "El registro de eventos está cerrado porque el partido ya no está en curso.",
                            modifier = Modifier.padding(14.dp),
                            color = SportProTextoSecundario,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.SpaceBetween,
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Text(
                        text = "Cronología",
                        color = SportProNavy,
                        style =
                            MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black
                    )

                    TextButton(
                        onClick = {
                            viewModel.abrirAuditoriaGeneral()
                        }
                    ) {
                        Text(
                            text = "Auditoría",
                            color = SportProVerde,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // HU16: contadores de pendientes / conflictos
            if (sync.pendientes > 0 || sync.conflictos > 0) {
                item {
                    ContadoresSincronizacion(
                        pendientes = sync.pendientes,
                        conflictos = sync.conflictos
                    )
                }
            }

            // HU16: eventos rechazados por el servidor
            sync.rechazados.forEach { rechazado ->
                item(
                    key = "rechazado-${rechazado.evento.id}"
                ) {
                    TarjetaEventoRechazado(
                        rechazado = rechazado,
                        onReintentar = {
                            sincronizacion.reintentarRechazado(rechazado)
                        },
                        onDescartar = {
                            sincronizacion.descartarRechazado(rechazado)
                        }
                    )
                }
            }

            if (sync.cargado && sync.eventos.isNotEmpty()) {

                // HU16: cronología en tiempo real con estado de sync
                sync.eventos.forEach { eventoSync ->
                    item(
                        key = eventoSync.evento.id
                    ) {
                        TarjetaEventoSincronizado(
                            item = eventoSync,
                            onClick = {
                                if (
                                    eventoSync.estado ==
                                    EstadoSincronizacionEvento.CONFLICTO
                                ) {
                                    sincronizacion.abrirConflicto(eventoSync)
                                } else {
                                    viewModel.abrirDetalleEvento(
                                        eventoSync.evento
                                    )
                                }
                            }
                        )
                    }
                }

            } else if (estado.eventos.isEmpty()) {

                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = SportProSuperficie,
                        shape = FormaTarjetaSportPro,
                        border = BorderStroke(
                            1.dp,
                            SportProBorde
                        )
                    ) {

                        Text(
                            text = "Todavía no se han registrado eventos.",
                            modifier = Modifier.padding(20.dp),
                            color = SportProTextoSecundario
                        )
                    }
                }

            } else {

                estado.eventos.forEach { evento ->

                    item(
                        key = evento.id
                    ) {
                        TarjetaCronologiaEvento(
                            evento = evento,

                            onClick = {
                                viewModel.abrirDetalleEvento(
                                    evento
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    if (
        estado.mostrarFormulario &&
        estado.formulario.tipo != null &&
        contexto != null
    ) {

        ModalRegistroEvento(
            idsSalida = opcionesCambio.idsSalida,
            idsEntrada = opcionesCambio.idsEntrada,
            formulario =
                estado.formulario,

            nombreEquipo =
                contexto.equipo.nombre,

            nombreRival =
                contexto.partido.nombreRival,

            jugadores =
                contexto.jugadoresAlineados,

            registrando =
                estado.registrando,

            error =
                estado.error,

            onCerrar = {
                viewModel.cerrarFormulario()
            },

            onCambiarMinuto =
                viewModel::cambiarMinuto,

            onCambiarEquipo =
                viewModel::cambiarEquipo,

            onCambiarJugador =
                viewModel::cambiarJugador,

            onCambiarJugadorRival =
                viewModel::cambiarNombreJugadorRival,

            onCambiarTipoTarjeta =
                viewModel::cambiarTipoTarjeta,

            onCambiarObservacion =
                viewModel::cambiarObservacion,

            onRegistrar = {
                viewModel.registrarEvento()
            },
            onCambiarJugadorSale =
                viewModel::cambiarJugadorSale,

            onCambiarJugadorEntra =
                viewModel::cambiarJugadorEntra,

            onCambiarJugadorSaleRival =
                viewModel::cambiarJugadorSaleRival,

            onCambiarJugadorEntraRival =
                viewModel::cambiarJugadorEntraRival,
        )
    }
    estado.eventoSeleccionado?.let {
            evento ->

        if (contexto != null) {

            ModalGestionEvento(
                idsSalida = opcionesCambio.idsSalida,
                idsEntrada = opcionesCambio.idsEntrada,
                evento =
                    evento,

                accion =
                    estado.accionEvento,

                formulario =
                    estado.formulario,

                motivo =
                    estado.motivoCambio,

                jugadores =
                    contexto.jugadoresAlineados,

                nombreEquipo =
                    contexto.equipo.nombre,

                nombreRival =
                    contexto.partido.nombreRival,

                auditoria =
                    estado.auditoriaEvento,

                cargandoAuditoria =
                    estado.cargandoAuditoria,

                procesando =
                    estado.procesandoCambio,

                error =
                    estado.error,

                onCerrar =
                    viewModel::cerrarDetalleEvento,

                onCorregir =
                    viewModel::iniciarCorreccionEvento,

                onAnular =
                    viewModel::iniciarAnulacionEvento,

                onCancelarAccion =
                    viewModel::cancelarAccionEvento,

                onMotivo =
                    viewModel::cambiarMotivoCambio,

                onMinuto =
                    viewModel::cambiarMinuto,

                onEquipo =
                    viewModel::cambiarEquipo,

                onJugador =
                    viewModel::cambiarJugador,

                onJugadorRival =
                    viewModel::cambiarNombreJugadorRival,

                onJugadorSale =
                    viewModel::cambiarJugadorSale,

                onJugadorEntra =
                    viewModel::cambiarJugadorEntra,

                onJugadorSaleRival =
                    viewModel::cambiarJugadorSaleRival,

                onJugadorEntraRival =
                    viewModel::cambiarJugadorEntraRival,

                onTipoTarjeta =
                    viewModel::cambiarTipoTarjeta,

                onObservacion =
                    viewModel::cambiarObservacion,

                onGuardarCorreccion =
                    viewModel::guardarCorreccionEvento,

                onConfirmarAnulacion =
                    viewModel::confirmarAnulacionEvento
            )
        }
    }
    if (
        estado.mostrarAuditoriaGeneral &&
        contexto != null
    ) {

        ModalAuditoriaGeneral(
            eventos =
                estado.eventosAuditoria,

            cargando =
                estado.cargandoAuditoriaGeneral,

            onCerrar = {
                viewModel.cerrarAuditoriaGeneral()
            },

            onAbrirEvento = { evento ->

                viewModel.cerrarAuditoriaGeneral()

                viewModel.abrirDetalleEvento(
                    evento
                )
            }
        )
    }

    // HU16: resolución de conflictos de sincronización
    sync.eventoConflicto?.let { conflicto ->
        ModalConflictoSincronizacion(
            item = conflicto,
            procesando = sync.resolviendoConflicto,
            sinConexion = sync.sinConexion,
            onCerrar = sincronizacion::cerrarConflicto,
            onConservar = sincronizacion::conservarEventoEnConflicto,
            onAnular = {
                sincronizacion.cerrarConflicto()
                viewModel.abrirDetalleEvento(conflicto.evento)
                viewModel.iniciarAnulacionEvento()
            }
        )
    }
}

@Composable
private fun TarjetaMarcador(
    nombreEquipo: String,
    nombreRival: String,
    golesPropios: Int,
    golesRival: Int,
    estadoPartido: EstadoPartido,
    // HU16: advertencia de información no sincronizada
    avisoRevision: String? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = SportProNavyFondo
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 18.dp,
                    vertical = 20.dp
                ),
            horizontalAlignment =
                Alignment.CenterHorizontally,
            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            Text(
                text =
                    if (
                        estadoPartido ==
                        EstadoPartido.EN_CURSO
                    ) {
                        "PARTIDO EN CURSO"
                    } else {
                        textoEstadoPartido(
                            estadoPartido
                        )
                    },
                color = SportProVerdeClaro,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically,
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                Text(
                    text =
                        nombreEquipo.uppercase(),
                    modifier = Modifier.weight(1f),
                    color = Color.White,
                    style =
                        MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text =
                        "$golesPropios–$golesRival",
                    modifier = Modifier.padding(
                        horizontal = 18.dp
                    ),
                    color = Color.White,
                    style =
                        MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text =
                        nombreRival.uppercase(),
                    modifier = Modifier.weight(1f),
                    color = Color.White,
                    style =
                        MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            if (
                estadoPartido ==
                EstadoPartido.EN_CURSO
            ) {
                Text(
                    text = "EN CURSO",
                    color = SportProVerdeClaro,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // HU16: el marcador no es definitivo
            avisoRevision?.let { aviso ->
                Text(
                    text = aviso,
                    color = SyncNaranja,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun PanelEventosRapidos(
    tipos: List<TipoEventoCatalogo>,
    onEvento: (TipoEventoCatalogo) -> Unit
) {
    Column(
        verticalArrangement =
            Arrangement.spacedBy(7.dp)
    ) {

        tipos
            .chunked(4)
            .forEach { fila ->

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(7.dp)
                ) {

                    fila.forEach { tipo ->

                        val habilitado =
                            tipo.codigo != "INICIO"

                        BotonEventoRapido(
                            tipo = tipo,
                            habilitado = habilitado,
                            modifier =
                                Modifier.weight(1f),
                            onClick = {
                                onEvento(tipo)
                            }
                        )
                    }

                    repeat(
                        4 - fila.size
                    ) {
                        Spacer(
                            modifier =
                                Modifier.weight(1f)
                        )
                    }
                }
            }
    }
}

@Composable
private fun BotonEventoRapido(
    tipo: TipoEventoCatalogo,
    habilitado: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    val estilo =
        estiloEventoEnVivo(
            tipo.codigo
        )

    val destacado =
        tipo.afectaMarcador

    Surface(
        modifier = modifier
            .height(76.dp)
            .clickable(
                enabled = habilitado,
                onClick = onClick
            ),
        shape = RoundedCornerShape(14.dp),
        color =
            if (habilitado) {
                if (destacado) {
                    SportProVerde
                } else {
                    SportProSuperficie
                }
            } else {
                Color(0xFFE8ECEA)
            },
        border =
            BorderStroke(
                1.dp,
                if (
                    habilitado &&
                    !destacado
                ) {
                    estilo.borde
                } else {
                    Color.Transparent
                }
            )
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(7.dp),
            horizontalAlignment =
                Alignment.CenterHorizontally,
            verticalArrangement =
                Arrangement.Center
        ) {

            Surface(
                modifier =
                    Modifier.size(32.dp),
                shape =
                    RoundedCornerShape(9.dp),
                color =
                    if (
                        destacado &&
                        habilitado
                    ) {
                        Color.White.copy(
                            alpha = 0.18f
                        )
                    } else {
                        estilo.fondoIcono
                    }
            ) {

                Box(
                    contentAlignment =
                        Alignment.Center
                ) {

                    Text(
                        text =
                            estilo.icono,
                        color =
                            if (
                                destacado &&
                                habilitado
                            ) {
                                Color.White
                            } else {
                                estilo.colorIcono
                            },
                        style =
                            MaterialTheme.typography.titleMedium,
                        fontWeight =
                            FontWeight.Black
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(4.dp)
            )

            Text(
                text =
                    nombreCortoEvento(
                        tipo.nombre
                    ),
                color =
                    if (
                        destacado &&
                        habilitado
                    ) {
                        Color.White
                    } else {
                        SportProNavy
                    },
                style =
                    MaterialTheme.typography.labelSmall,
                fontWeight =
                    FontWeight.Bold,
                maxLines = 1,
                overflow =
                    TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TarjetaCronologiaEvento(
    evento: EventoPartido,
    onClick: () -> Unit
) {

    val estilo =
        estiloEventoEnVivo(
            evento.codigoTipoEvento
        )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = SportProSuperficie
        ),
        border = BorderStroke(
            1.dp,
            estilo.borde
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            IconoEvento(evento.codigoTipoEvento, evento.tipoTarjeta)

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement =
                    Arrangement.spacedBy(3.dp)
            ) {

                Row(
                    verticalAlignment =
                        Alignment.CenterVertically,
                    horizontalArrangement =
                        Arrangement.spacedBy(7.dp)
                ) {

                    Text(
                        text =
                            evento.nombreTipoEvento,
                        color =
                            SportProTextoPrincipal,
                        style =
                            MaterialTheme.typography.bodyLarge,
                        fontWeight =
                            FontWeight.Bold
                    )

                    Surface(
                        shape = CircleShape,
                        color = SportProVerdeClaro
                    ) {

                        Text(
                            text =
                                "${evento.minuto}'",
                            modifier =
                                Modifier.padding(
                                    horizontal = 8.dp,
                                    vertical = 3.dp
                                ),
                            color = SportProVerde,
                            style =
                                MaterialTheme.typography.labelSmall,
                            fontWeight =
                                FontWeight.Black
                        )
                    }
                }

                JugadoresEvento(evento)

                if (
                    evento.observacion
                        .isNotBlank()
                ) {

                    Text(
                        text =
                            evento.observacion,
                        color =
                            SportProTextoSecundario,
                        style =
                            MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow =
                            TextOverflow.Ellipsis
                    )
                }
            }

            Text(
                text = "›",
                color =
                    SportProTextoSecundario,
                style =
                    MaterialTheme.typography.titleLarge,
                fontWeight =
                    FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModalRegistroEvento(
    idsSalida: Set<String>,
    idsEntrada: Set<String>,
    formulario: FormularioEventoPartido,
    nombreEquipo: String,
    nombreRival: String,
    jugadores: List<Jugador>,
    registrando: Boolean,
    error: String?,
    onCerrar: () -> Unit,
    onCambiarMinuto: (String) -> Unit,
    onCambiarEquipo: (EquipoEvento) -> Unit,
    onCambiarJugador: (String) -> Unit,
    onCambiarJugadorRival: (String) -> Unit,
    onCambiarTipoTarjeta: (TipoTarjeta) -> Unit,
    onCambiarObservacion: (String) -> Unit,
    onRegistrar: () -> Unit,
    onCambiarJugadorSale: (String) -> Unit,
    onCambiarJugadorEntra: (String) -> Unit,
    onCambiarJugadorSaleRival: (String) -> Unit,
    onCambiarJugadorEntraRival: (String) -> Unit,


) {
    val tipo =
        formulario.tipo
            ?: return

    val estadoHoja =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )

    ModalBottomSheet(
        onDismissRequest = onCerrar,
        sheetState = estadoHoja,
        containerColor = SportProSuperficie,
        shape = RoundedCornerShape(
            topStart = 26.dp,
            topEnd = 26.dp
        ),
        dragHandle = {
            Surface(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .size(
                        width = 36.dp,
                        height = 4.dp
                    ),
                shape = CircleShape,
                color = SportProBorde
            ) {}
        }
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    bottom = 28.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween,
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Column {

                    Text(
                        text = "NUEVO EVENTO",
                        color = SportProVerde,
                        style =
                            MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black
                    )

                    Row(
                        verticalAlignment =
                            Alignment.CenterVertically,
                        horizontalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {

                        val estilo =
                            estiloEventoEnVivo(
                                tipo.codigo
                            )

                        Surface(
                            modifier = Modifier.size(42.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = estilo.fondoIcono
                        ) {

                            Box(
                                contentAlignment = Alignment.Center
                            ) {

                                Text(
                                    text = estilo.icono,
                        fontSize = 28.sp,
                                    color = estilo.colorIcono,
                                    style =
                                        MaterialTheme.typography.titleLarge,
                                    fontWeight =
                                        FontWeight.Black
                                )
                            }
                        }

                        Text(
                            text = tipo.nombre,
                            color = SportProNavy,
                            style =
                                MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .size(34.dp)
                        .clickable(
                            enabled =
                                !registrando,
                            onClick =
                                onCerrar
                        ),
                    shape = CircleShape,
                    color = SportProFondo
                ) {
                    Box(
                        contentAlignment =
                            Alignment.Center
                    ) {
                        Text(
                            text = "×",
                            color = SportProNavy,
                            fontWeight =
                                FontWeight.Bold
                        )
                    }
                }
            }

            OutlinedTextField(
                value =
                    formulario.minuto,
                onValueChange =
                    onCambiarMinuto,
                modifier =
                    Modifier.fillMaxWidth(),
                enabled = !registrando,
                singleLine = true,
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType =
                            KeyboardType.Number
                    ),
                label = {
                    Text("Minuto")
                },
                placeholder = {
                    Text("Ej. 37")
                },
                shape =
                    RoundedCornerShape(12.dp)
            )

            if (tipo.requiereEquipo || tipo.codigo == "CAMBIO") {

                Text(
                    text =
                        textoEquipoSegunEvento(
                            tipo
                        ),
                    color = SportProTextoSecundario,
                    style =
                        MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black
                )

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {

                    BotonSeleccionEquipo(
                        texto =
                            nombreEquipo,

                        seleccionado =
                            formulario.equipoEvento ==
                                    EquipoEvento.PROPIO,

                        modifier =
                            Modifier.weight(1f),

                        onClick = {
                            onCambiarEquipo(
                                EquipoEvento.PROPIO
                            )
                        }
                    )

                    BotonSeleccionEquipo(
                        texto =
                            nombreRival,

                        seleccionado =
                            formulario.equipoEvento ==
                                    EquipoEvento.RIVAL,

                        modifier =
                            Modifier.weight(1f),

                        onClick = {
                            onCambiarEquipo(
                                EquipoEvento.RIVAL
                            )
                        }
                    )
                }
            }

            if (tipo.codigo == "CAMBIO") {

                Text(
                    text = "SUSTITUCIÓN",
                    color = SportProTextoSecundario,
                    style =
                        MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black
                )

                if (
                    formulario.equipoEvento ==
                    EquipoEvento.RIVAL
                ) {

                    OutlinedTextField(
                        value =
                            formulario.nombreJugadorSaleRival,
                        onValueChange =
                            onCambiarJugadorSaleRival,
                        modifier =
                            Modifier.fillMaxWidth(),
                        enabled = !registrando,
                        singleLine = true,
                        label = {
                            Text("Jugador que sale")
                        }
                    )

                    OutlinedTextField(
                        value =
                            formulario.nombreJugadorEntraRival,
                        onValueChange =
                            onCambiarJugadorEntraRival,
                        modifier =
                            Modifier.fillMaxWidth(),
                        enabled = !registrando,
                        singleLine = true,
                        label = {
                            Text("Jugador que entra")
                        }
                    )

                } else {

                    Text(
                        text = "↓ JUGADOR QUE SALE",
                        color = SportProRojo,
                        fontWeight = FontWeight.Bold
                    )

                    SelectorJugador(
                        jugadores = jugadores.filter { it.id in idsSalida },
                        idSeleccionado =
                            formulario.idJugadorSale,
                        habilitado = !registrando,
                        onSeleccionar =
                            onCambiarJugadorSale
                    )

                    Text(
                        text = "↑ JUGADOR QUE ENTRA",
                        color = SportProVerde,
                        fontWeight = FontWeight.Bold
                    )

                    SelectorJugador(
                        jugadores =
                            jugadores.filter { it.id in idsEntrada },
                        idSeleccionado =
                            formulario.idJugadorEntra,
                        habilitado = !registrando,
                        onSeleccionar =
                            onCambiarJugadorEntra
                    )
                }
            }
            if (
                tipo.requiereJugador &&
                tipo.codigo != "CAMBIO"
            ) {

                if (
                    formulario.equipoEvento ==
                    EquipoEvento.RIVAL
                ) {

                    OutlinedTextField(
                        value =
                            formulario
                                .nombreJugadorRival,

                        onValueChange =
                            onCambiarJugadorRival,

                        modifier =
                            Modifier.fillMaxWidth(),

                        enabled =
                            !registrando,

                        singleLine = true,

                        label = {
                            Text(
                                "Jugador rival"
                            )
                        },

                        placeholder = {
                            Text(
                                "Nombre del jugador"
                            )
                        },

                        shape =
                            RoundedCornerShape(
                                12.dp
                            )
                    )

                } else {

                    SelectorJugador(
                        jugadores =
                            jugadores,

                        idSeleccionado =
                            formulario.idJugador,

                        habilitado =
                            !registrando,

                        onSeleccionar =
                            onCambiarJugador
                    )
                }
            }
        }

        if (tipo.codigo == "TARJETA") {

            Text(
                text = "TIPO DE TARJETA",
                color = SportProTextoSecundario,
                style =
                    MaterialTheme.typography.labelSmall,
                fontWeight =
                    FontWeight.Black
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                BotonTipoTarjeta(
                    texto = "Amarilla",
                    simbolo = "▰",
                    seleccionado =
                        formulario.tipoTarjeta ==
                                TipoTarjeta.AMARILLA,
                    habilitado = !registrando,
                    color = Color(0xFFD9A900),
                    modifier =
                        Modifier.weight(1f),
                    onClick = {
                        onCambiarTipoTarjeta(
                            TipoTarjeta.AMARILLA
                        )
                    }
                )

                BotonTipoTarjeta(
                    texto = "Roja",
                    simbolo = "▰",
                    seleccionado =
                        formulario.tipoTarjeta ==
                                TipoTarjeta.ROJA,
                    habilitado = !registrando,
                    color = Color(0xFFC62828),
                    modifier =
                        Modifier.weight(1f),
                    onClick = {
                        onCambiarTipoTarjeta(
                            TipoTarjeta.ROJA
                        )
                    }
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
                    onCambiarObservacion,

                modifier =
                    Modifier.fillMaxWidth(),

                enabled =
                    !registrando,

                minLines = 2,

                label = {
                    Text("Observación")
                },

                placeholder = {
                    Text(
                        "Describe brevemente el evento"
                    )
                },

                shape =
                    RoundedCornerShape(12.dp)
            )
        }

        error?.let { mensaje ->

            Surface(
                modifier =
                    Modifier.fillMaxWidth(),
                color =
                    SportProRojoClaro,
                shape =
                    RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = mensaje,
                    modifier =
                        Modifier.padding(10.dp),
                    color = SportProRojo,
                    style =
                        MaterialTheme.typography.bodySmall
                )
            }
        }

        Button(
            onClick = onRegistrar,
            enabled = !registrando && (
                tipo.codigo != "CAMBIO" || formulario.equipoEvento == EquipoEvento.RIVAL ||
                    (formulario.idJugadorSale in idsSalida && formulario.idJugadorEntra in idsEntrada)
                ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = FormaBotonSportPro,
            colors =
                ButtonDefaults.buttonColors(
                    containerColor =
                        SportProVerde,
                    contentColor =
                        Color.White
                )
        ) {

            if (registrando) {

                CircularProgressIndicator(
                    modifier =
                        Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )

            } else {

                Text(
                    text =
                        if (
                            tipo.codigo ==
                            "FIN"
                        ) {
                            "Registrar y finalizar"
                        } else {
                            "Registrar ${tipo.nombre}"
                        },
                    fontWeight =
                        FontWeight.Bold
                )
            }
        }
    }

}


@Composable
private fun BotonTipoTarjeta(
    texto: String,
    simbolo: String,
    seleccionado: Boolean,
    habilitado: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    Surface(
        modifier = modifier
            .height(50.dp)
            .clickable(
                enabled = habilitado,
                onClick = onClick
            ),
        shape = RoundedCornerShape(12.dp),
        color =
            if (seleccionado) {
                color.copy(alpha = 0.14f)
            } else {
                SportProSuperficie
            },
        border = BorderStroke(
            width =
                if (seleccionado) {
                    2.dp
                } else {
                    1.dp
                },
            color =
                if (seleccionado) {
                    color
                } else {
                    SportProBorde
                }
        )
    ) {

        Row(
            modifier =
                Modifier.padding(
                    horizontal = 12.dp
                ),
            verticalAlignment =
                Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.Center
        ) {

            Text(
                text = simbolo,
                color = color,
                fontWeight =
                    FontWeight.Black
            )

            Spacer(
                modifier =
                    Modifier.size(7.dp)
            )

            Text(
                text = texto,
                color = SportProNavy,
                fontWeight =
                    FontWeight.Bold
            )
        }
    }
}

@Composable
private fun BotonSeleccionEquipo(
    texto: String,
    seleccionado: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    if (seleccionado) {

        Button(
            onClick = onClick,
            modifier = modifier,
            shape = RoundedCornerShape(12.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor =
                        SportProVerde,
                    contentColor =
                        Color.White
                )
        ) {
            Text(
                text = texto,
                maxLines = 1,
                overflow =
                    TextOverflow.Ellipsis,
                style =
                    MaterialTheme.typography.labelMedium
            )
        }

    } else {

        OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(
                1.dp,
                SportProBorde
            )
        ) {
            Text(
                text = texto,
                color = SportProTextoSecundario,
                maxLines = 1,
                overflow =
                    TextOverflow.Ellipsis,
                style =
                    MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun SelectorJugador(
    jugadores: List<Jugador>,
    idSeleccionado: String,
    habilitado: Boolean,
    onSeleccionar: (String) -> Unit
) {
    var abierto by remember {
        mutableStateOf(false)
    }

    val jugadorSeleccionado =
        jugadores.firstOrNull {
            it.id == idSeleccionado
        }

    Column {

        Text(
            text = "JUGADOR",
            color = SportProTextoSecundario,
            style =
                MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black
        )

        Spacer(
            modifier = Modifier.height(5.dp)
        )

        Box(
            modifier = Modifier.fillMaxWidth()
        ) {

            OutlinedButton(
                onClick = {
                    if (habilitado) {
                        abierto = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                border =
                    BorderStroke(
                        1.dp,
                        SportProBorde
                    )
            ) {

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.SpaceBetween
                ) {

                    Text(
                        text =
                            jugadorSeleccionado
                                ?.nombreCompleto()
                                ?: "Seleccionar jugador",
                        color =
                            if (
                                jugadorSeleccionado !=
                                null
                            ) {
                                SportProTextoPrincipal
                            } else {
                                SportProTextoSecundario
                            },
                        maxLines = 1,
                        overflow =
                            TextOverflow.Ellipsis
                    )

                    Text(
                        text = "⌄",
                        color = SportProNavy
                    )
                }
            }

            DropdownMenu(
                expanded = abierto,
                onDismissRequest = {
                    abierto = false
                }
            ) {

                jugadores.forEach { jugador ->

                    DropdownMenuItem(
                        text = {
                            Text(
                                jugador
                                    .nombreCompleto()
                            )
                        },
                        onClick = {
                            onSeleccionar(
                                jugador.id
                            )

                            abierto = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MensajePartidoEnVivo(
    mensaje: String,
    error: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color =
            if (error) {
                SportProRojoClaro
            } else {
                SportProVerdeClaro
            },
        shape = RoundedCornerShape(12.dp)
    ) {

        Text(
            text = mensaje,
            modifier = Modifier.padding(12.dp),
            color =
                if (error) {
                    SportProRojo
                } else {
                    SportProVerde
                },
            style =
                MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun CabeceraPartidoEnVivo(
    estadoSincronizacion: EstadoConexionPartido
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.SpaceBetween,
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Row(
            verticalAlignment =
                Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {

            Surface(
                modifier = Modifier.size(38.dp),
                shape = RoundedCornerShape(10.dp),
                color = SportProNavyFondo
            ) {

                Box(
                    contentAlignment =
                        Alignment.Center
                ) {
                    Text("⚽")
                }
            }

            Column {

                Text(
                    text = "SPORTPRO",
                    color = SportProNavy,
                    style =
                        MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = "PARTIDO EN VIVO",
                    color =
                        SportProTextoSecundario,
                    style =
                        MaterialTheme.typography.labelSmall
                )
            }
        }

        // HU16: indicador de sincronización
        IndicadorNubeSincronizacion(
            estado = estadoSincronizacion
        )
    }
}

private fun textoEquipoSegunEvento(
    tipo: TipoEventoCatalogo
): String =
    if (
        tipo.afectaMarcador
    ) {
        "EQUIPO QUE ANOTA"
    } else {
        "EQUIPO DEL EVENTO"
    }

private fun nombreCortoEvento(
    nombre: String
): String {

    val limpio =
        nombre.trim()

    return when {

        limpio.equals(
            "Saque de meta",
            ignoreCase = true
        ) ->
            "Sq. Meta"

        limpio.equals(
            "Saque lateral",
            ignoreCase = true
        ) ->
            "Sq. Lat."

        limpio.equals(
            "Tiro de esquina",
            ignoreCase = true
        ) ->
            "Esquina"

        limpio.equals(
            "Fuera de juego",
            ignoreCase = true
        ) ->
            "F. Juego"

        else ->
            limpio
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
