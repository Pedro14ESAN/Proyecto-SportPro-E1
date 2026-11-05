package com.example.projectsportpro2026_2.ui.anuncios

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectsportpro2026_2.datos.modelo.Anuncio
import com.example.projectsportpro2026_2.datos.modelo.EstadoAnuncio
import com.example.projectsportpro2026_2.datos.modelo.Rol
import com.example.projectsportpro2026_2.datos.modelo.TipoAudiencia
import com.example.projectsportpro2026_2.ui.partidos.FormaBotonSportPro
import com.example.projectsportpro2026_2.ui.partidos.FormaCampoSportPro
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Colores de las etiquetas de audiencia (tomados del Figma)
private val AzulEtiqueta = Color(0xFF3550A0)
private val AzulEtiquetaFondo = Color(0xFFE8EEFF)
private val NaranjaEtiqueta = Color(0xFFA35F00)
private val NaranjaEtiquetaFondo = Color(0xFFFFF1DD)
private val GrisEtiqueta = Color(0xFF66737E)
private val GrisEtiquetaFondo = Color(0xFFEEF0F1)

@Composable
fun PantallaAnuncios(
    onVolver: () -> Unit,
    viewModel: AnunciosViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()
    val contexto = LocalContext.current

    // Estado propio de la pantalla: si el formulario está abierto y qué anuncio se edita
    var formularioAbierto by remember { mutableStateOf(false) }
    var anuncioEnEdicion by remember { mutableStateOf<Anuncio?>(null) }

    LaunchedEffect(Unit) { viewModel.iniciar() }

    // Muestra el mensaje de éxito una sola vez y lo limpia
    LaunchedEffect(estado.mensaje) {
        estado.mensaje?.let {
            Toast.makeText(contexto, it, Toast.LENGTH_SHORT).show()
            viewModel.limpiarMensajes()
        }
    }

    Scaffold(containerColor = SportProFondo) { relleno ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            item { CabeceraSportPro() }

            item {
                Text(
                    text = "← Inicio",
                    color = SportProVerde,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onVolver() }
                )
            }

            // Título + botón "+ Nuevo"
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "COMUNICADOS DEL CLUB",
                            color = SportProTextoSecundario,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Anuncios",
                            color = SportProNavy,
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }

                    if (estado.puedePublicar) {
                        Button(
                            onClick = {
                                anuncioEnEdicion = null
                                viewModel.limpiarMensajes()
                                formularioAbierto = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SportProNavy),
                            shape = FormaBotonSportPro
                        ) {
                            Text("+ Nuevo", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Pestañas: solo para quien puede publicar y archivar
            if (estado.puedePublicar) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ChipAnuncio("Todos", estado.filtro == FiltroAnuncios.TODOS) {
                            viewModel.cambiarFiltro(FiltroAnuncios.TODOS)
                        }
                        ChipAnuncio("Activos", estado.filtro == FiltroAnuncios.ACTIVOS) {
                            viewModel.cambiarFiltro(FiltroAnuncios.ACTIVOS)
                        }
                        ChipAnuncio("Archivados", estado.filtro == FiltroAnuncios.ARCHIVADOS) {
                            viewModel.cambiarFiltro(FiltroAnuncios.ARCHIVADOS)
                        }
                    }
                }
            }

            // Error de carga (el error del formulario se muestra dentro del formulario)
            estado.error?.takeIf { !formularioAbierto }?.let { texto ->
                item { MensajeError(texto) { viewModel.recargar() } }
            }

            when {
                estado.cargando && estado.anuncios.isEmpty() -> item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = SportProVerde)
                    }
                }

                estado.anunciosFiltrados.isEmpty() && estado.error == null -> item {
                    Text(
                        text = "No hay anuncios por ahora.",
                        color = SportProTextoSecundario,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }

                else -> items(estado.anunciosFiltrados, key = { it.id }) { anuncio ->
                    TarjetaAnuncio(
                        anuncio = anuncio,
                        puedeGestionar = estado.puedeGestionar(anuncio),
                        onEditar = {
                            anuncioEnEdicion = anuncio
                            viewModel.limpiarMensajes()
                            formularioAbierto = true
                        },
                        onArchivar = { viewModel.archivar(anuncio) },
                        onReactivar = { viewModel.reactivar(anuncio) }
                    )
                }
            }
        }
    }

    if (formularioAbierto) {
        FormularioAnuncio(
            anuncioEnEdicion = anuncioEnEdicion,
            opciones = estado.opcionesPublicacion,
            guardando = estado.guardando,
            error = estado.error,
            onCerrar = { formularioAbierto = false },
            onPublicar = { titulo, contenido, idAcademia, tipo, idEquipo, rol ->
                viewModel.crear(
                    titulo, contenido, idAcademia, tipo, idEquipo, rol,
                    alTerminar = { formularioAbierto = false }
                )
            },
            onGuardarEdicion = { titulo, contenido ->
                anuncioEnEdicion?.let {
                    viewModel.editar(
                        it.id, titulo, contenido,
                        alTerminar = { formularioAbierto = false }
                    )
                }
            }
        )
    }
}

// ---------- Componentes pequeños ----------

@Composable
private fun CabeceraSportPro() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            modifier = Modifier.size(38.dp),
            shape = RoundedCornerShape(10.dp),
            color = SportProNavy
        ) {
            Box(contentAlignment = Alignment.Center) { Text("⚽") }
        }
        Column {
            Text(
                text = "SPORTPRO",
                color = SportProNavy,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "CLUB DEPORTIVO",
                color = SportProTextoSecundario,
                fontSize = 10.sp,
                letterSpacing = 1.sp
            )
        }
    }
}

// "internal": se puede usar desde FormularioAnuncio.kt, pero no desde fuera del módulo
@Composable
internal fun ChipAnuncio(
    texto: String,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (seleccionado) SportProNavy else SportProVerdeClaro
    ) {
        Text(
            text = texto,
            color = if (seleccionado) Color.White else SportProNavy,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun Etiqueta(texto: String, color: Color, fondo: Color) {
    Surface(shape = RoundedCornerShape(6.dp), color = fondo) {
        Text(
            text = texto,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

private data class EstiloAudiencia(
    val texto: String,
    val emoji: String,
    val color: Color,
    val fondo: Color
)

private fun estiloAudiencia(anuncio: Anuncio): EstiloAudiencia =
    when (anuncio.tipoAudiencia) {
        TipoAudiencia.ACADEMIA ->
            EstiloAudiencia("Toda la academia", "🏟️", AzulEtiqueta, AzulEtiquetaFondo)
        TipoAudiencia.EQUIPO ->
            EstiloAudiencia(
                "Equipo · ${anuncio.nombreEquipo}".trimEnd(' ', '·'),
                "👥", SportProVerde, SportProVerdeClaro
            )
        TipoAudiencia.ROL ->
            EstiloAudiencia(textoRol(anuncio.rolDestino), "🎯", NaranjaEtiqueta, NaranjaEtiquetaFondo)
    }

internal fun textoRol(rol: Rol): String = when (rol) {
    Rol.DT -> "Cuerpo técnico"
    Rol.JUG -> "Jugadores"
    Rol.PAD -> "Apoderados"
    Rol.ADM -> "Administradores"
}

internal fun formatearFecha(millis: Long): String =
    SimpleDateFormat("dd MMM yyyy · HH:mm", Locale.forLanguageTag("es-PE")).format(Date(millis))

@Composable
private fun TarjetaAnuncio(
    anuncio: Anuncio,
    puedeGestionar: Boolean,
    onEditar: () -> Unit,
    onArchivar: () -> Unit,
    onReactivar: () -> Unit
) {
    val archivado = anuncio.estado == EstadoAnuncio.ARCHIVADO
    val estilo = estiloAudiencia(anuncio)
    var expandido by remember { mutableStateOf(false) }   // tocar la tarjeta muestra el texto completo

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (archivado) 0.6f else 1f)
            .clickable { expandido = !expandido },
        shape = FormaTarjetaSportPro,
        colors = CardDefaults.cardColors(containerColor = SportProSuperficie),
        border = BorderStroke(1.dp, SportProBorde)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(12.dp),
                color = estilo.fondo
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(estilo.emoji, fontSize = 18.sp)
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Etiqueta(estilo.texto, estilo.color, estilo.fondo)
                    if (archivado) Etiqueta("ARCHIVADO", GrisEtiqueta, GrisEtiquetaFondo)
                    if (anuncio.editado && !archivado) {
                        Etiqueta("ACTUALIZADO", NaranjaEtiqueta, NaranjaEtiquetaFondo)
                    }
                }

                Text(
                    text = anuncio.titulo,
                    color = SportProTextoPrincipal,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = anuncio.contenido,
                    color = SportProTextoSecundario,
                    fontSize = 13.sp,
                    maxLines = if (expandido) Int.MAX_VALUE else 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${anuncio.nombreAutor} · ${anuncio.rolAutor.name} · " +
                            formatearFecha(anuncio.fechaCreacion),
                    color = SportProTextoSecundario,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                )

                if (anuncio.editado) {
                    Text(
                        text = "Actualizado: ${formatearFecha(anuncio.fechaActualizacion)}",
                        color = NaranjaEtiqueta,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )
                }

                if (puedeGestionar) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (archivado) {
                            TextButton(onClick = onReactivar) {
                                Text("Reactivar", color = SportProVerde)
                            }
                        } else {
                            TextButton(onClick = onEditar) {
                                Text("Editar", color = SportProVerde)
                            }
                            TextButton(onClick = onArchivar) {
                                Text("Archivar", color = SportProRojo)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MensajeError(texto: String, onReintentar: () -> Unit) {
    Surface(
        shape = FormaCampoSportPro,
        color = SportProRojoClaro,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(texto, color = SportProRojo, fontSize = 13.sp)
            TextButton(onClick = onReintentar) {
                Text("Reintentar", color = SportProRojo, fontWeight = FontWeight.Bold)
            }
        }
    }
}