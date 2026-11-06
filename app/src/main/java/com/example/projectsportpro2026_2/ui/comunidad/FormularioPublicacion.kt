package com.example.projectsportpro2026_2.ui.comunidad

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.example.projectsportpro2026_2.datos.repositorio.ProcesadorImagen
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectsportpro2026_2.datos.modelo.Publicacion
import com.example.projectsportpro2026_2.datos.modelo.Rol
import com.example.projectsportpro2026_2.datos.modelo.VisibilidadPublicacion
import com.example.projectsportpro2026_2.datos.repositorio.OpcionesComunidad
import com.example.projectsportpro2026_2.ui.comun.*
import com.example.projectsportpro2026_2.ui.partidos.FormaBotonSportPro
import com.example.projectsportpro2026_2.ui.partidos.FormaCampoSportPro
import com.example.projectsportpro2026_2.ui.partidos.SportProBorde
import com.example.projectsportpro2026_2.ui.partidos.SportProFondo
import com.example.projectsportpro2026_2.ui.partidos.SportProNavy
import com.example.projectsportpro2026_2.ui.partidos.SportProRojo
import com.example.projectsportpro2026_2.ui.partidos.SportProSuperficie
import com.example.projectsportpro2026_2.ui.partidos.SportProTextoSecundario
import com.example.projectsportpro2026_2.ui.partidos.SportProVerde
import com.example.projectsportpro2026_2.ui.partidos.SportProVerdeClaro

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioPublicacion(
    publicacionEnEdicion: Publicacion?,
    nombreAutor: String,
    rolAutor: Rol?,
    opciones: OpcionesComunidad,
    guardando: Boolean,
    error: String?,
    onCerrar: () -> Unit,
    onPublicar: (
        texto: String,
        visibilidad: VisibilidadPublicacion,
        idAcademia: String,
        idEquipo: String,
        imagen: ByteArray?          // HU21: JPEG ya comprimido (null = solo texto)
    ) -> Unit,
    onGuardarEdicion: (texto: String) -> Unit,
    // HU21: progreso de subida (null = no se sube) y fallo de la imagen
    progresoImagen: Float? = null,
    falloImagen: Boolean = false,
    onDescartarErrorImagen: () -> Unit = {}
) {
    val esEdicion = publicacionEnEdicion != null
    val contexto = LocalContext.current
    val alcance = rememberCoroutineScope()

    // HU21: imagen elegida, ya reducida y sin metadatos EXIF
    var imagen by remember { mutableStateOf<ByteArray?>(null) }
    val vistaPrevia = remember(imagen) {
        imagen?.let { BitmapFactory.decodeByteArray(it, 0, it.size)?.asImageBitmap() }
    }
    var procesandoImagen by remember { mutableStateOf(false) }
    var errorImagenLocal by remember { mutableStateOf<String?>(null) }

    // Selector de fotos del sistema: no necesita permisos de almacenamiento
    val selectorFoto = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            procesandoImagen = true
            errorImagenLocal = null
            alcance.launch {
                try {
                    imagen = ProcesadorImagen.comprimir(contexto, uri)
                    onDescartarErrorImagen()
                } catch (e: Exception) {
                    errorImagenLocal = e.message ?: "No se pudo leer la imagen"
                } finally {
                    procesandoImagen = false
                }
            }
        }
    }
    val estadoHoja = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var texto by remember { mutableStateOf(publicacionEnEdicion?.texto.orEmpty()) }

    // Privacidad por defecto: si pertenece a una academia, empieza en "Academia"
    var visibilidad by remember {
        mutableStateOf(
            if (opciones.academias.isNotEmpty()) VisibilidadPublicacion.ACADEMIA
            else VisibilidadPublicacion.COMUNIDAD
        )
    }
    // Lo que el usuario eligió a mano (vacío = todavía no eligió)
    var idAcademiaElegida by remember { mutableStateOf("") }
    var idEquipoElegido by remember { mutableStateOf("") }

    // Si no eligió, se usa la primera opción disponible, aunque haya llegado después de abrir el formulario
    val idAcademia = idAcademiaElegida.ifBlank { opciones.academias.firstOrNull()?.id.orEmpty() }
    val idEquipo = idEquipoElegido.ifBlank { opciones.equipos.firstOrNull()?.id.orEmpty() }
    val destinoValido = when (visibilidad) {
        VisibilidadPublicacion.EQUIPO -> opciones.equipos.any { it.id == idEquipo }
        VisibilidadPublicacion.ACADEMIA -> opciones.academias.any { it.id == idAcademia }
        VisibilidadPublicacion.COMUNIDAD -> true
    }
    val formularioValido = texto.isNotBlank() && (esEdicion || destinoValido)

    // "Director Técnico · Club Deportivo Norte"
    val subtitulo = listOfNotNull(
        rolAutor?.let { nombreRol(it) },
        opciones.academias.firstOrNull()?.nombre
    ).joinToString(" · ")

    ModalBottomSheet(
        onDismissRequest = onCerrar,
        sheetState = estadoHoja,
        containerColor = SportProSuperficie
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = if (esEdicion) "Editar publicación" else "Crear publicación",
                color = SportProNavy,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.titleLarge
            )

            // "PUBLICANDO COMO" (RC-02: no hay publicaciones anónimas)
            Surface(
                color = SportProVerdeClaro,
                shape = FormaCampoSportPro,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AvatarIniciales(nombreAutor, oscuro = true)
                    Column {
                        TituloCampoSportPro("PUBLICANDO COMO")
                        Text(nombreAutor, color = SportProNavy, fontWeight = FontWeight.Bold)
                        if (subtitulo.isNotBlank()) {
                            Text(subtitulo, color = SportProTextoSecundario, fontSize = 12.sp)
                        }
                    }
                }
            }

            OutlinedTextField(
                value = texto,
                onValueChange = { if (it.length <= 500) texto = it },
                placeholder = { Text("¿Qué quieres compartir?") },
                minLines = 4,
                shape = FormaCampoSportPro,
                supportingText = {
                    Text(
                        text = "${texto.length} / 500",
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )

            if (esEdicion) {
                Surface(color = SportProVerdeClaro, shape = FormaCampoSportPro) {
                    Text(
                        text = "La visibilidad no se puede cambiar después de publicar.",
                        color = SportProNavy,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            } else {

                // HU21: imagen opcional, guardada en Cloud Storage
                TituloCampoSportPro("MULTIMEDIA")
                val previa = vistaPrevia
                if (previa != null) {
                    Box {
                        Image(
                            bitmap = previa,
                            contentDescription = "Vista previa de la imagen",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 220.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                        Surface(
                            onClick = {
                                imagen = null
                                onDescartarErrorImagen()
                            },
                            enabled = !guardando,
                            shape = RoundedCornerShape(50),
                            color = SportProNavy.copy(alpha = 0.8f),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                        ) {
                            Text(
                                "✕ Quitar",
                                color = Color.White,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                    progresoImagen?.let { p ->
                        LinearProgressIndicator(
                            progress = { p },
                            color = SportProVerde,
                            trackColor = SportProVerdeClaro,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            "Subiendo imagen… ${(p * 100).toInt()} %",
                            color = SportProTextoSecundario,
                            fontSize = 11.sp
                        )
                    }
                } else {
                    Surface(
                        onClick = {
                            selectorFoto.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        enabled = !procesandoImagen && !guardando,
                        shape = FormaCampoSportPro,
                        color = SportProFondo,
                        border = BorderStroke(1.dp, SportProBorde),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (procesandoImagen) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = SportProVerde
                                )
                            } else {
                                Text("📷", fontSize = 18.sp)
                            }
                            Column {
                                Text("Añadir imagen", color = SportProNavy, fontWeight = FontWeight.SemiBold)
                                Text(
                                    "Opcional · se quitan los datos de ubicación de la foto",
                                    color = SportProTextoSecundario,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
                errorImagenLocal?.let { Text(it, color = SportProRojo, fontSize = 12.sp) }

                // Caso alterno HU21: la imagen no se pudo subir
                if (falloImagen && imagen != null) {
                    Surface(color = SportProRojo.copy(alpha = 0.08f), shape = FormaCampoSportPro) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "No se pudo subir la imagen. ¿Qué quieres hacer?",
                                color = SportProRojo,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { onPublicar(texto, visibilidad, idAcademia, idEquipo, imagen) },
                                    enabled = !guardando && formularioValido,
                                    shape = FormaBotonSportPro,
                                    modifier = Modifier.weight(1f)
                                ) { Text("Reintentar", color = SportProNavy) }
                                OutlinedButton(
                                    onClick = {
                                        imagen = null
                                        onPublicar(texto, visibilidad, idAcademia, idEquipo, null)
                                    },
                                    enabled = !guardando && formularioValido,
                                    shape = FormaBotonSportPro,
                                    modifier = Modifier.weight(1f)
                                ) { Text("Solo texto", color = SportProNavy) }
                            }
                        }
                    }
                }

                TituloCampoSportPro("VISIBILIDAD")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OpcionVisibilidad(
                        emoji = "👥", titulo = "Equipo", subtitulo = "Solo tu equipo",
                        seleccionado = visibilidad == VisibilidadPublicacion.EQUIPO,
                        habilitado = opciones.equipos.isNotEmpty(),
                        modifier = Modifier.weight(1f)
                    ) { visibilidad = VisibilidadPublicacion.EQUIPO }

                    OpcionVisibilidad(
                        emoji = "🏛", titulo = "Academia", subtitulo = "Todo el club",
                        seleccionado = visibilidad == VisibilidadPublicacion.ACADEMIA,
                        habilitado = opciones.academias.isNotEmpty(),
                        modifier = Modifier.weight(1f)
                    ) { visibilidad = VisibilidadPublicacion.ACADEMIA }

                    OpcionVisibilidad(
                        emoji = "🌐", titulo = "Comunidad", subtitulo = "Red pública",
                        seleccionado = visibilidad == VisibilidadPublicacion.COMUNIDAD,
                        habilitado = true,
                        modifier = Modifier.weight(1f)
                    ) { visibilidad = VisibilidadPublicacion.COMUNIDAD }
                }
                // Muestra a dónde se publicará (útil cuando hay una sola opción y no aparecen chips)
                val destinoTexto = when (visibilidad) {
                    VisibilidadPublicacion.EQUIPO ->
                        opciones.equipos.find { it.id == idEquipo }?.let { "Se publicará solo para ${it.nombre}" }
                    VisibilidadPublicacion.ACADEMIA ->
                        opciones.academias.find { it.id == idAcademia }?.let { "Se publicará para todo ${it.nombre}" }
                    VisibilidadPublicacion.COMUNIDAD ->
                        "Se publicará para toda la Comunidad SportPro"
                }
                destinoTexto?.let {
                    Text(it, color = SportProTextoSecundario, fontSize = 12.sp)
                }

                // Si pertenece a varios equipos o academias, elige a cuál
                if (visibilidad == VisibilidadPublicacion.EQUIPO && opciones.equipos.size > 1) {
                    TituloCampoSportPro("EQUIPO")
                    FilaDeChipsSportPro {
                        opciones.equipos.forEach { equipo ->
                            ChipSportPro("${equipo.nombre} · ${equipo.categoria}", equipo.id == idEquipo) {
                                idEquipoElegido = equipo.id
                            }
                        }
                    }
                }
                if (visibilidad == VisibilidadPublicacion.ACADEMIA && opciones.academias.size > 1) {
                    TituloCampoSportPro("ACADEMIA")
                    FilaDeChipsSportPro {
                        opciones.academias.forEach { academia ->
                            ChipSportPro(academia.nombre, academia.id == idAcademia) {
                                idAcademiaElegida = academia.id
                            }
                        }
                    }
                }
            }

            if (error != null) {
                Text(error, color = SportProRojo, fontSize = 13.sp)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onCerrar,
                    shape = FormaBotonSportPro,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar", color = SportProNavy)
                }
                Button(
                    onClick = {
                        if (esEdicion) onGuardarEdicion(texto)
                        else onPublicar(texto, visibilidad, idAcademia, idEquipo, imagen)
                    },
                    enabled = formularioValido && !guardando && !procesandoImagen,
                    shape = FormaBotonSportPro,
                    colors = ButtonDefaults.buttonColors(containerColor = SportProVerde),
                    modifier = Modifier.weight(1f)
                ) {
                    if (guardando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Text(if (esEdicion) "Guardar" else "Publicar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Tarjeta de las 3 opciones de visibilidad (Equipo / Academia / Comunidad)
@Composable
private fun OpcionVisibilidad(
    emoji: String,
    titulo: String,
    subtitulo: String,
    seleccionado: Boolean,
    habilitado: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        enabled = habilitado,
        shape = RoundedCornerShape(12.dp),
        color = if (seleccionado) SportProVerdeClaro else SportProSuperficie,
        border = BorderStroke(
            width = if (seleccionado) 2.dp else 1.dp,
            color = if (seleccionado) SportProVerde else SportProBorde
        ),
        modifier = modifier.alpha(if (habilitado) 1f else 0.4f)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 18.sp)
            Text(
                text = titulo,
                color = SportProNavy,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitulo,
                color = SportProTextoSecundario,
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}