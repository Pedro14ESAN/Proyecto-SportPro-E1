package com.example.projectsportpro2026_2.ui.comunidad

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectsportpro2026_2.datos.modelo.Publicacion
import com.example.projectsportpro2026_2.datos.modelo.TipoReaccion
import com.example.projectsportpro2026_2.datos.modelo.VisibilidadPublicacion
import com.example.projectsportpro2026_2.ui.comun.*
import com.example.projectsportpro2026_2.ui.partidos.FormaBotonSportPro
import com.example.projectsportpro2026_2.ui.partidos.FormaTarjetaSportPro
import com.example.projectsportpro2026_2.ui.partidos.SportProBorde
import com.example.projectsportpro2026_2.ui.partidos.SportProFondo
import com.example.projectsportpro2026_2.ui.partidos.SportProNavy
import com.example.projectsportpro2026_2.ui.partidos.SportProRojo
import com.example.projectsportpro2026_2.ui.partidos.SportProSuperficie
import com.example.projectsportpro2026_2.ui.partidos.SportProTextoPrincipal
import com.example.projectsportpro2026_2.ui.partidos.SportProTextoSecundario
import com.example.projectsportpro2026_2.ui.partidos.SportProVerde
import com.example.projectsportpro2026_2.ui.partidos.SportProVerdeClaro

@Composable
fun PantallaComunidad(
    onVolver: () -> Unit,
    viewModel: ComunidadViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()
    val avisos = remember { SnackbarHostState() }

    var formularioAbierto by remember { mutableStateOf(false) }
    var enEdicion by remember { mutableStateOf<Publicacion?>(null) }
    var porEliminar by remember { mutableStateOf<Publicacion?>(null) }

    LaunchedEffect(Unit) { viewModel.iniciar() }

    LaunchedEffect(estado.mensaje) {
        estado.mensaje?.let { texto ->
            avisos.showSnackbar("✓ $texto")
            viewModel.limpiarMensajes()
        }
    }

    Scaffold(
        containerColor = SportProFondo,
        snackbarHost = {
            SnackbarHost(avisos) { datos ->
                Snackbar(
                    snackbarData = datos,
                    containerColor = SportProNavy,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    ) { relleno ->
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

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Comunidad",
                        color = SportProNavy,
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Button(
                        onClick = {
                            enEdicion = null
                            viewModel.limpiarMensajes()
                            formularioAbierto = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SportProVerde),
                        shape = FormaBotonSportPro
                    ) {
                        Text("+ Publicar", fontWeight = FontWeight.Bold)
                    }
                }
            }

            item {
                FilaDeChipsSportPro {
                    ChipSportPro("Para ti", estado.pestana == PestanaComunidad.PARA_TI) {
                        viewModel.cambiarPestana(PestanaComunidad.PARA_TI)
                    }
                    ChipSportPro("Academias", estado.pestana == PestanaComunidad.ACADEMIAS) {
                        viewModel.cambiarPestana(PestanaComunidad.ACADEMIAS)
                    }
                    ChipSportPro("Pruebas", estado.pestana == PestanaComunidad.PRUEBAS) {
                        viewModel.cambiarPestana(PestanaComunidad.PRUEBAS)
                    }
                }
            }

            estado.error?.takeIf { !formularioAbierto }?.let { texto ->
                item { MensajeErrorSportPro(texto) { viewModel.recargar() } }
            }

            when {
                estado.pestana == PestanaComunidad.PRUEBAS -> item {
                    Text(
                        text = "Aún no hay pruebas abiertas publicadas.",
                        color = SportProTextoSecundario,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }

                estado.cargando && estado.publicaciones.isEmpty() -> item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = SportProVerde)
                    }
                }

                estado.publicacionesVisibles.isEmpty() && estado.error == null -> item {
                    Text(
                        text = "Todavía no hay publicaciones. ¡Sé la primera persona en compartir algo!",
                        color = SportProTextoSecundario,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }

                else -> items(estado.publicacionesVisibles, key = { it.id }) { publicacion ->
                    TarjetaPublicacion(
                        publicacion = publicacion,
                        miReaccion = publicacion.reaccionDe(estado.uid),
                        puedeModificar = estado.puedeModificar(publicacion),
                        onReaccionar = { tipo -> viewModel.reaccionar(publicacion, tipo) },
                        onComentarios = { viewModel.abrirComentarios(publicacion) },
                        onEditar = {
                            enEdicion = publicacion
                            viewModel.limpiarMensajes()
                            formularioAbierto = true
                        },
                        onEliminar = { porEliminar = publicacion }
                    )
                }
            }
        }
    }

    if (formularioAbierto) {
        FormularioPublicacion(
            publicacionEnEdicion = enEdicion,
            nombreAutor = estado.nombre,
            rolAutor = estado.rol,
            opciones = estado.opciones,
            guardando = estado.guardando,
            error = estado.error,
            onCerrar = { formularioAbierto = false },
            onPublicar = { texto, visibilidad, idAcademia, idEquipo ->
                viewModel.publicar(
                    texto, visibilidad, idAcademia, idEquipo,
                    alTerminar = { formularioAbierto = false }
                )
            },
            onGuardarEdicion = { texto ->
                enEdicion?.let {
                    viewModel.editar(it.id, texto, alTerminar = { formularioAbierto = false })
                }
            }
        )
    }

    // Hoja de comentarios (HU22): se abre cuando el ViewModel tiene una publicación seleccionada
    estado.publicacionComentarios?.let { publicacion ->
        HojaComentarios(
            publicacion = publicacion,
            comentarios = estado.comentarios,
            uid = estado.uid,
            nombreUsuario = estado.nombre,
            cargando = estado.cargandoComentarios,
            enviando = estado.enviandoComentario,
            error = estado.errorComentarios,
            onCerrar = { viewModel.cerrarComentarios() },
            onComentar = { texto, alTerminar -> viewModel.comentar(texto, alTerminar) },
            onEliminar = { comentario -> viewModel.eliminarComentario(comentario) }
        )
    }

    porEliminar?.let { publicacion ->
        AlertDialog(
            onDismissRequest = { porEliminar = null },
            title = { Text("¿Eliminar publicación?") },
            text = { Text("Dejará de verse en el feed de la comunidad.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminar(publicacion)
                    porEliminar = null
                }) {
                    Text("Eliminar", color = SportProRojo, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { porEliminar = null }) {
                    Text("Cancelar", color = SportProNavy)
                }
            }
        )
    }
}

// ---------- Tarjeta de publicación ----------

private data class EstiloVisibilidad(val texto: String, val color: Color, val fondo: Color)

private fun estiloVisibilidad(p: Publicacion): EstiloVisibilidad = when (p.visibilidad) {
    VisibilidadPublicacion.COMUNIDAD ->
        EstiloVisibilidad("🌐 Comunidad SportPro", EtiquetaAzul, EtiquetaAzulFondo)
    VisibilidadPublicacion.ACADEMIA ->
        EstiloVisibilidad("🏛 ${p.nombreAcademia.ifBlank { "Academia" }}", EtiquetaLila, EtiquetaLilaFondo)
    VisibilidadPublicacion.EQUIPO ->
        EstiloVisibilidad("👥 ${p.nombreEquipo.ifBlank { "Equipo" }}", SportProVerde, SportProVerdeClaro)
}

@Composable
private fun TarjetaPublicacion(
    publicacion: Publicacion,
    miReaccion: TipoReaccion?,
    puedeModificar: Boolean,
    onReaccionar: (TipoReaccion) -> Unit,
    onComentarios: () -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    var menuAbierto by remember { mutableStateOf(false) }
    val visibilidad = estiloVisibilidad(publicacion)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = FormaTarjetaSportPro,
        colors = CardDefaults.cardColors(containerColor = SportProSuperficie),
        border = BorderStroke(1.dp, SportProBorde)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarIniciales(publicacion.nombreAutor)
                Spacer(Modifier.width(10.dp))

                Column(Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = publicacion.nombreAutor,
                            color = SportProNavy,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        EtiquetaSportPro(publicacion.rolAutor.name, EtiquetaGris, EtiquetaGrisFondo)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = tiempoRelativo(publicacion.fechaCreacion) +
                                    if (publicacion.editada) " · editada" else "",
                            color = SportProTextoSecundario,
                            fontSize = 11.sp
                        )
                        EtiquetaSportPro(visibilidad.texto, visibilidad.color, visibilidad.fondo)
                    }
                }

                if (puedeModificar) {
                    Box {
                        Text(
                            text = "•••",
                            color = SportProTextoSecundario,
                            modifier = Modifier
                                .clickable { menuAbierto = true }
                                .padding(8.dp)
                        )
                        DropdownMenu(
                            expanded = menuAbierto,
                            onDismissRequest = { menuAbierto = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Editar") },
                                onClick = {
                                    menuAbierto = false
                                    onEditar()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Eliminar", color = SportProRojo) },
                                onClick = {
                                    menuAbierto = false
                                    onEliminar()
                                }
                            )
                        }
                    }
                }
            }

            Text(
                text = publicacion.texto,
                color = SportProTextoPrincipal,
                fontSize = 14.sp
            )

            HorizontalDivider(color = SportProBorde)

            // Interacciones (HU22)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                BotonReaccion(publicacion, miReaccion, onReaccionar)

                val n = publicacion.cantidadComentarios
                Text(
                    text = "💬 $n ${if (n == 1) "comentario" else "comentarios"}",
                    color = SportProTextoSecundario,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .clickable { onComentarios() }
                        .padding(vertical = 4.dp)
                )
            }
        }
    }
}

// Botón "♡ 14" que abre el selector ❤️ 👏 💪
@Composable
private fun BotonReaccion(
    publicacion: Publicacion,
    miReaccion: TipoReaccion?,
    onReaccionar: (TipoReaccion) -> Unit
) {
    var selectorAbierto by remember { mutableStateOf(false) }

    Box {
        Text(
            text = "${miReaccion?.emoji ?: "♡"} ${publicacion.totalReacciones()}",
            color = if (miReaccion != null) SportProVerde else SportProTextoSecundario,
            fontWeight = if (miReaccion != null) FontWeight.Bold else FontWeight.Normal,
            fontSize = 13.sp,
            modifier = Modifier
                .clickable { selectorAbierto = true }
                .padding(vertical = 4.dp)
        )

        DropdownMenu(
            expanded = selectorAbierto,
            onDismissRequest = { selectorAbierto = false }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TipoReaccion.entries.forEach { tipo ->
                    Surface(
                        onClick = {
                            selectorAbierto = false
                            onReaccionar(tipo)
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = if (tipo == miReaccion) SportProVerdeClaro else Color.Transparent
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(tipo.emoji, fontSize = 22.sp)
                            Text(
                                text = "${publicacion.conteoDe(tipo)}",
                                color = SportProTextoSecundario,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
            if (miReaccion != null) {
                Text(
                    text = "Toca tu reacción otra vez para quitarla",
                    color = SportProTextoSecundario,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
    }
}