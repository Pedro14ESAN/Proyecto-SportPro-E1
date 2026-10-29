package com.example.projectsportpro2026_2.ui.catalogoeventos

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectsportpro2026_2.datos.modelo.evento.TipoEventoCatalogo
import com.example.projectsportpro2026_2.ui.partidos.FormaBotonSportPro
import com.example.projectsportpro2026_2.ui.partidos.FormaTarjetaSportPro
import com.example.projectsportpro2026_2.ui.partidos.SportProBorde
import com.example.projectsportpro2026_2.ui.partidos.SportProFondo
import com.example.projectsportpro2026_2.ui.partidos.SportProNavy
import com.example.projectsportpro2026_2.ui.partidos.SportProRojo
import com.example.projectsportpro2026_2.ui.partidos.SportProRojoClaro
import com.example.projectsportpro2026_2.ui.partidos.SportProSuperficie
import com.example.projectsportpro2026_2.ui.partidos.SportProTextoSecundario
import com.example.projectsportpro2026_2.ui.partidos.SportProVerde
import com.example.projectsportpro2026_2.ui.partidos.SportProVerdeClaro

@Composable
fun PantallaCatalogoEventos(
    idAcademia: String,
    onVolver: () -> Unit,
    viewModel: CatalogoEventosViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()

    LaunchedEffect(idAcademia) {
        viewModel.iniciar(idAcademia)
    }

    Scaffold(
        containerColor = SportProFondo
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
                bottom = 28.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            item {
                CabeceraCatalogoSportPro()
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
                    text = "· ADM",
                    color = SportProVerde,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {

                        Text(
                            text = "Catálogo de eventos",
                            color = SportProNavy,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black
                        )

                        Spacer(
                            modifier = Modifier.height(4.dp)
                        )

                        Text(
                            text = "Configura los eventos disponibles para el registro en vivo.",
                            color = SportProTextoSecundario,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(
                        modifier = Modifier.size(10.dp)
                    )

                    IndicadorSincronizacion(
                        estado = estado.estadoSincronizacion
                    )
                }
            }

            estado.error?.let { mensaje ->

                item {
                    MensajeCatalogo(
                        mensaje = mensaje,
                        error = true
                    )
                }
            }

            estado.mensaje?.let { mensaje ->

                item {
                    MensajeCatalogo(
                        mensaje = mensaje,
                        error = false
                    )
                }
            }

            item {
                Button(
                    onClick = {
                        viewModel.abrirNuevoEvento()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = FormaBotonSportPro,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SportProVerde,
                        contentColor = Color.White
                    )
                ) {

                    Text(
                        text = "+  Nuevo evento",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = "EVENTOS CONFIGURADOS",
                        color = SportProTextoSecundario,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )

                    if (estado.eventos.isNotEmpty()) {
                        Text(
                            text = "${estado.eventos.count { it.activo }} activos",
                            color = SportProTextoSecundario,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            if (
                estado.cargando &&
                estado.eventos.isEmpty()
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
                            text = "Todavía no existen eventos configurados.",
                            modifier = Modifier.padding(20.dp),
                            color = SportProTextoSecundario,
                            textAlign = TextAlign.Center
                        )
                    }
                }

            } else {

                items(
                    items = estado.eventos,
                    key = {
                        it.id
                    }
                ) { evento ->

                    TarjetaEventoCatalogo(
                        evento = evento,

                        onCambiarActivo = { activo ->
                            viewModel.cambiarEstadoEvento(
                                evento = evento,
                                activo = activo
                            )
                        },

                        onEditar = {
                            viewModel.abrirEditarEvento(
                                evento
                            )
                        }
                    )
                }
            }
        }
    }

    if (estado.mostrarFormulario) {

        DialogoFormularioEvento(
            formulario = estado.formulario,
            guardando = estado.guardando,

            onCerrar = {
                viewModel.cerrarFormulario()
            },

            onCambiarNombre =
                viewModel::cambiarNombre,

            onCambiarActivo =
                viewModel::cambiarActivo,

            onCambiarRequiereEquipo =
                viewModel::cambiarRequiereEquipo,

            onCambiarRequiereJugador =
                viewModel::cambiarRequiereJugador,

            onCambiarRequiereObservacion =
                viewModel::cambiarRequiereObservacion,

            onCambiarAfectaMarcador =
                viewModel::cambiarAfectaMarcador,

            onCambiarAfectaEstado =
                viewModel::cambiarAfectaEstadoPartido,

            onGuardar = {
                viewModel.guardarFormulario()
            }
        )
    }
}

@Composable
private fun TarjetaEventoCatalogo(
    evento: TipoEventoCatalogo,
    onCambiarActivo: (Boolean) -> Unit,
    onEditar: () -> Unit
) {
    var menuAbierto by remember {
        mutableStateOf(false)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = SportProSuperficie
        ),
        shape = FormaTarjetaSportPro,
        border = BorderStroke(
            1.dp,
            SportProBorde
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Surface(
                modifier = Modifier.size(42.dp),
                shape = RoundedCornerShape(12.dp),
                color = if (evento.activo) {
                    SportProVerdeClaro
                } else {
                    SportProFondo
                }
            ) {

                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = inicialEvento(evento),
                        color = if (evento.activo) {
                            SportProVerde
                        } else {
                            SportProTextoSecundario
                        },
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = evento.nombre,
                        color = if (evento.activo) {
                            SportProNavy
                        } else {
                            SportProTextoSecundario
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (evento.afectaMarcador) {
                        ChipEvento(
                            texto = "marcador"
                        )
                    }

                    if (evento.afectaEstadoPartido) {
                        ChipEvento(
                            texto = "estado"
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                val campos =
                    camposRequeridos(evento)

                Text(
                    text =
                        if (campos.isEmpty()) {
                            "Sin campos adicionales"
                        } else {
                            campos.joinToString(
                                separator = " · "
                            )
                        },
                    color = SportProTextoSecundario,
                    style = MaterialTheme.typography.bodySmall
                )

                if (!evento.activo) {

                    Text(
                        text = "INACTIVO",
                        color = SportProRojo,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Switch(
                checked = evento.activo,
                onCheckedChange = onCambiarActivo,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = SportProVerde
                )
            )

            Box {

                Text(
                    text = "⋮",
                    modifier = Modifier
                        .clickable {
                            menuAbierto = true
                        }
                        .padding(
                            horizontal = 6.dp,
                            vertical = 10.dp
                        ),
                    color = SportProNavy,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                DropdownMenu(
                    expanded = menuAbierto,
                    onDismissRequest = {
                        menuAbierto = false
                    }
                ) {

                    DropdownMenuItem(
                        text = {
                            Text("Editar")
                        },
                        onClick = {
                            menuAbierto = false
                            onEditar()
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            Text(
                                if (evento.activo) {
                                    "Desactivar"
                                } else {
                                    "Activar"
                                }
                            )
                        },
                        onClick = {
                            menuAbierto = false

                            onCambiarActivo(
                                !evento.activo
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ChipEvento(
    texto: String
) {
    Surface(
        shape = CircleShape,
        color = SportProVerdeClaro
    ) {

        Text(
            text = texto,
            color = SportProVerde,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(
                horizontal = 7.dp,
                vertical = 3.dp
            )
        )
    }
}

@Composable
private fun IndicadorSincronizacion(
    estado: EstadoSincronizacionCatalogo
) {
    val texto =
        when (estado) {

            EstadoSincronizacionCatalogo.SINCRONIZADO ->
                "Sincronizado"

            EstadoSincronizacionCatalogo.SINCRONIZANDO ->
                "Sincronizando..."

            EstadoSincronizacionCatalogo.DATOS_LOCALES ->
                "Datos locales"
        }

    val color =
        when (estado) {

            EstadoSincronizacionCatalogo.SINCRONIZADO ->
                SportProVerde

            EstadoSincronizacionCatalogo.SINCRONIZANDO ->
                SportProNavy

            EstadoSincronizacionCatalogo.DATOS_LOCALES ->
                Color(0xFF9A6A00)
        }

    Surface(
        color = SportProSuperficie,
        shape = CircleShape,
        border = BorderStroke(
            1.dp,
            SportProBorde
        )
    ) {

        Row(
            modifier = Modifier.padding(
                horizontal = 9.dp,
                vertical = 6.dp
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {

            Surface(
                modifier = Modifier.size(7.dp),
                shape = CircleShape,
                color = color
            ) {}

            Text(
                text = texto,
                color = SportProTextoSecundario,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun DialogoFormularioEvento(
    formulario: FormularioEvento,
    guardando: Boolean,

    onCerrar: () -> Unit,
    onCambiarNombre: (String) -> Unit,
    onCambiarActivo: (Boolean) -> Unit,
    onCambiarRequiereEquipo: (Boolean) -> Unit,
    onCambiarRequiereJugador: (Boolean) -> Unit,
    onCambiarRequiereObservacion: (Boolean) -> Unit,
    onCambiarAfectaMarcador: (Boolean) -> Unit,
    onCambiarAfectaEstado: (Boolean) -> Unit,
    onGuardar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCerrar,

        title = {
            Column {

                Text(
                    text =
                        if (formulario.id == null) {
                            "NUEVO EVENTO"
                        } else {
                            "EDITAR EVENTO"
                        },
                    color = SportProVerde,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = "Configurar evento",
                    color = SportProNavy,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black
                )
            }
        },

        text = {

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                Text(
                    text = "Nombre del evento",
                    color = SportProTextoSecundario,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = formulario.nombre,
                    onValueChange = onCambiarNombre,
                    enabled = !formulario.esTipoBase,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = {
                        Text(
                            "Ej. Córner corto"
                        )
                    },
                    shape = RoundedCornerShape(12.dp)
                )

                if (formulario.esTipoBase) {

                    Text(
                        text = "Los eventos base conservan su nombre.",
                        color = SportProTextoSecundario,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                FilaSwitchFormulario(
                    titulo = "Evento activo",
                    valor = formulario.activo,
                    onCambiar = onCambiarActivo
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = "METADATOS DEL EVENTO",
                    color = SportProTextoSecundario,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black
                )

                FilaCheckFormulario(
                    titulo = "Requiere equipo",
                    valor =
                        formulario.requiereEquipo,
                    onCambiar =
                        onCambiarRequiereEquipo
                )

                FilaCheckFormulario(
                    titulo = "Requiere jugador",
                    valor =
                        formulario.requiereJugador,
                    onCambiar =
                        onCambiarRequiereJugador
                )

                FilaCheckFormulario(
                    titulo = "Requiere observación",
                    valor =
                        formulario.requiereObservacion,
                    onCambiar =
                        onCambiarRequiereObservacion
                )

                FilaCheckFormulario(
                    titulo = "Afecta marcador",
                    valor =
                        formulario.afectaMarcador,
                    onCambiar =
                        onCambiarAfectaMarcador
                )

                FilaCheckFormulario(
                    titulo = "Afecta estado del partido",
                    valor =
                        formulario.afectaEstadoPartido,
                    onCambiar =
                        onCambiarAfectaEstado
                )
            }
        },

        confirmButton = {

            Button(
                onClick = onGuardar,
                enabled =
                    !guardando &&
                            formulario.nombre
                                .isNotBlank(),
                shape = FormaBotonSportPro,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            SportProNavy,
                        contentColor =
                            Color.White
                    )
            ) {

                if (guardando) {

                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )

                } else {

                    Text(
                        text =
                            if (formulario.id == null) {
                                "Guardar evento"
                            } else {
                                "Guardar cambios"
                            },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },

        dismissButton = {

            OutlinedButton(
                onClick = onCerrar,
                enabled = !guardando,
                shape = FormaBotonSportPro
            ) {

                Text(
                    text = "Cancelar",
                    color = SportProNavy
                )
            }
        },

        containerColor = SportProSuperficie,
        shape = RoundedCornerShape(22.dp)
    )
}

@Composable
private fun FilaSwitchFormulario(
    titulo: String,
    valor: Boolean,
    onCambiar: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = titulo,
            color = SportProNavy,
            fontWeight = FontWeight.Bold
        )

        Switch(
            checked = valor,
            onCheckedChange = onCambiar,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = SportProVerde
            )
        )
    }
}

@Composable
private fun FilaCheckFormulario(
    titulo: String,
    valor: Boolean,
    onCambiar: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onCambiar(
                    !valor
                )
            },
        verticalAlignment = Alignment.CenterVertically
    ) {

        Checkbox(
            checked = valor,
            onCheckedChange = onCambiar
        )

        Text(
            text = titulo,
            color = SportProNavy,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun MensajeCatalogo(
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
            color =
                if (error) {
                    SportProRojo
                } else {
                    SportProVerde
                },
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun CabeceraCatalogoSportPro() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Surface(
                modifier = Modifier.size(38.dp),
                shape = RoundedCornerShape(10.dp),
                color = SportProNavy
            ) {

                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text("⚽")
                }
            }

            Column {

                Text(
                    text = "SPORTPRO",
                    color = SportProNavy,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = "ADMINISTRACIÓN",
                    color = SportProTextoSecundario,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        Surface(
            modifier = Modifier.size(38.dp),
            shape = CircleShape,
            color = SportProVerdeClaro
        ) {

            Box(
                contentAlignment = Alignment.Center
            ) {
                Text("🔔")
            }
        }
    }
}

private fun camposRequeridos(
    evento: TipoEventoCatalogo
): List<String> =
    buildList {

        if (evento.requiereEquipo) {
            add("Equipo")
        }

        if (evento.requiereJugador) {
            add("Jugador")
        }

        if (evento.requiereObservacion) {
            add("Observación")
        }
    }

private fun inicialEvento(
    evento: TipoEventoCatalogo
): String =
    evento.nombre
        .trim()
        .firstOrNull()
        ?.uppercaseChar()
        ?.toString()
        ?: "E"