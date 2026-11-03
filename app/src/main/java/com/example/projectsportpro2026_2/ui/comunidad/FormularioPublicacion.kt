package com.example.projectsportpro2026_2.ui.comunidad

import androidx.compose.foundation.BorderStroke
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
        idEquipo: String
    ) -> Unit,
    onGuardarEdicion: (texto: String) -> Unit
) {
    val esEdicion = publicacionEnEdicion != null
    val estadoHoja = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var texto by remember { mutableStateOf(publicacionEnEdicion?.texto.orEmpty()) }

    // Privacidad por defecto: si pertenece a una academia, empieza en "Academia"
    var visibilidad by remember {
        mutableStateOf(
            if (opciones.academias.isNotEmpty()) VisibilidadPublicacion.ACADEMIA
            else VisibilidadPublicacion.COMUNIDAD
        )
    }
    var idAcademia by remember { mutableStateOf(opciones.academias.firstOrNull()?.id.orEmpty()) }
    var idEquipo by remember { mutableStateOf(opciones.equipos.firstOrNull()?.id.orEmpty()) }

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

                // Imagen: se activa cuando el equipo habilite Cloud Storage
                TituloCampoSportPro("MULTIMEDIA")
                Surface(
                    shape = FormaCampoSportPro,
                    color = SportProFondo,
                    border = BorderStroke(1.dp, SportProBorde),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text("📷 Añadir imagen", color = SportProTextoSecundario, fontWeight = FontWeight.SemiBold)
                        Text("Disponible cuando se active Cloud Storage", color = SportProTextoSecundario, fontSize = 11.sp)
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

                // Si pertenece a varios equipos o academias, elige a cuál
                if (visibilidad == VisibilidadPublicacion.EQUIPO && opciones.equipos.size > 1) {
                    TituloCampoSportPro("EQUIPO")
                    FilaDeChipsSportPro {
                        opciones.equipos.forEach { equipo ->
                            ChipSportPro("${equipo.nombre} · ${equipo.categoria}", equipo.id == idEquipo) {
                                idEquipo = equipo.id
                            }
                        }
                    }
                }
                if (visibilidad == VisibilidadPublicacion.ACADEMIA && opciones.academias.size > 1) {
                    TituloCampoSportPro("ACADEMIA")
                    FilaDeChipsSportPro {
                        opciones.academias.forEach { academia ->
                            ChipSportPro(academia.nombre, academia.id == idAcademia) {
                                idAcademia = academia.id
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
                        else onPublicar(texto, visibilidad, idAcademia, idEquipo)
                    },
                    enabled = formularioValido && !guardando,
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