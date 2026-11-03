package com.example.projectsportpro2026_2.ui.comunidad

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectsportpro2026_2.datos.modelo.Comentario
import com.example.projectsportpro2026_2.datos.modelo.Publicacion
import com.example.projectsportpro2026_2.ui.comun.AvatarIniciales
import com.example.projectsportpro2026_2.ui.comun.tiempoRelativo
import com.example.projectsportpro2026_2.ui.partidos.FormaCampoSportPro
import com.example.projectsportpro2026_2.ui.partidos.SportProBorde
import com.example.projectsportpro2026_2.ui.partidos.SportProNavy
import com.example.projectsportpro2026_2.ui.partidos.SportProRojo
import com.example.projectsportpro2026_2.ui.partidos.SportProSuperficie
import com.example.projectsportpro2026_2.ui.partidos.SportProTextoPrincipal
import com.example.projectsportpro2026_2.ui.partidos.SportProTextoSecundario
import com.example.projectsportpro2026_2.ui.partidos.SportProVerde
import com.example.projectsportpro2026_2.ui.partidos.SportProVerdeClaro

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HojaComentarios(
    publicacion: Publicacion,
    comentarios: List<Comentario>,
    uid: String,
    nombreUsuario: String,
    cargando: Boolean,
    enviando: Boolean,
    error: String?,
    onCerrar: () -> Unit,
    onComentar: (texto: String, alTerminar: () -> Unit) -> Unit,
    onEliminar: (Comentario) -> Unit
) {
    val estadoHoja = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var texto by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onCerrar,
        sheetState = estadoHoja,
        containerColor = SportProSuperficie
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 16.dp)
                .imePadding(),                     // sube el campo cuando aparece el teclado
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text(
                text = "Comentarios (${publicacion.cantidadComentarios})",
                color = SportProNavy,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // Resumen de la publicación original
            Surface(
                color = SportProVerdeClaro,
                shape = FormaCampoSportPro,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AvatarIniciales(publicacion.nombreAutor, tamano = 32.dp)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = publicacion.nombreAutor,
                            color = SportProNavy,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = publicacion.texto,
                            color = SportProTextoPrincipal,
                            fontSize = 13.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "♡ ${publicacion.totalReacciones()}",
                            color = SportProTextoSecundario,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Lista de comentarios
            when {
                cargando && comentarios.isEmpty() -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = SportProVerde)
                }

                error != null && comentarios.isEmpty() ->
                    Text(error, color = SportProRojo, fontSize = 13.sp)

                comentarios.isEmpty() ->
                    Text(
                        text = "Aún no hay comentarios. ¡Sé la primera persona en comentar!",
                        color = SportProTextoSecundario,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )

                else -> LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(comentarios, key = { it.id }) { comentario ->
                        FilaComentario(
                            comentario = comentario,
                            esMio = comentario.idAutor == uid,
                            onEliminar = { onEliminar(comentario) }
                        )
                    }
                }
            }

            // Error al enviar o eliminar (cuando ya hay comentarios en pantalla)
            if (error != null && comentarios.isNotEmpty()) {
                Text(error, color = SportProRojo, fontSize = 12.sp)
            }

            HorizontalDivider(color = SportProBorde)

            // Escribir comentario
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AvatarIniciales(nombreUsuario, oscuro = true, tamano = 32.dp)

                OutlinedTextField(
                    value = texto,
                    onValueChange = { if (it.length <= 300) texto = it },
                    placeholder = { Text("Escribe un comentario…", fontSize = 13.sp) },
                    shape = RoundedCornerShape(20.dp),
                    maxLines = 3,
                    modifier = Modifier.weight(1f)
                )

                TextButton(
                    onClick = { onComentar(texto) { texto = "" } },   // limpia el campo solo si se guardó
                    enabled = texto.isNotBlank() && !enviando
                ) {
                    if (enviando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = SportProVerde
                        )
                    } else {
                        Text(
                            text = "Publicar",
                            color = if (texto.isNotBlank()) SportProVerde else SportProTextoSecundario,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilaComentario(
    comentario: Comentario,
    esMio: Boolean,
    onEliminar: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AvatarIniciales(comentario.nombreAutor, tamano = 32.dp)

        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = comentario.nombreAutor,
                    color = SportProNavy,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = tiempoRelativo(comentario.fechaCreacion),
                    color = SportProTextoSecundario,
                    fontSize = 10.sp
                )
            }
            Text(
                text = comentario.texto,
                color = SportProTextoPrincipal,
                fontSize = 13.sp
            )
        }

        // Solo el autor ve el botón de eliminar su comentario
        if (esMio) {
            Text(
                text = "🗑",
                modifier = Modifier
                    .clickable { onEliminar() }
                    .padding(4.dp)
            )
        }
    }
}