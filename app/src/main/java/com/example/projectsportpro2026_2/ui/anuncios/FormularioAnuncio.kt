package com.example.projectsportpro2026_2.ui.anuncios

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectsportpro2026_2.datos.modelo.Anuncio
import com.example.projectsportpro2026_2.datos.modelo.Rol
import com.example.projectsportpro2026_2.datos.modelo.TipoAudiencia
import com.example.projectsportpro2026_2.datos.repositorio.OpcionesPublicacion
import com.example.projectsportpro2026_2.ui.partidos.FormaBotonSportPro
import com.example.projectsportpro2026_2.ui.partidos.FormaCampoSportPro
import com.example.projectsportpro2026_2.ui.partidos.SportProNavy
import com.example.projectsportpro2026_2.ui.partidos.SportProRojo
import com.example.projectsportpro2026_2.ui.partidos.SportProSuperficie
import com.example.projectsportpro2026_2.ui.partidos.SportProTextoSecundario
import com.example.projectsportpro2026_2.ui.partidos.SportProVerde
import com.example.projectsportpro2026_2.ui.partidos.SportProVerdeClaro

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioAnuncio(
    anuncioEnEdicion: Anuncio?,
    opciones: List<OpcionesPublicacion>,
    guardando: Boolean,
    error: String?,
    onCerrar: () -> Unit,
    onPublicar: (
        titulo: String,
        contenido: String,
        idAcademia: String,
        tipo: TipoAudiencia,
        idEquipo: String,
        rol: Rol
    ) -> Unit,
    onGuardarEdicion: (titulo: String, contenido: String) -> Unit
) {
    val esEdicion = anuncioEnEdicion != null
    val estadoHoja = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Lo que el usuario va escribiendo/eligiendo
    var titulo by remember { mutableStateOf(anuncioEnEdicion?.titulo.orEmpty()) }
    var contenido by remember { mutableStateOf(anuncioEnEdicion?.contenido.orEmpty()) }
    var idAcademia by remember { mutableStateOf(opciones.firstOrNull()?.academia?.id.orEmpty()) }

    val opcionActual = opciones.find { it.academia.id == idAcademia }
    val equiposDisponibles = opcionActual?.equipos.orEmpty()

    var tipo by remember { mutableStateOf(audienciaPorDefecto(opcionActual)) }
    var idEquipo by remember { mutableStateOf("") }
    var rolDestino by remember { mutableStateOf(Rol.DT) }

    // Validación en pantalla: el botón se activa solo si todo está completo
    val audienciaValida = when (tipo) {
        TipoAudiencia.EQUIPO -> idEquipo.isNotBlank()
        else -> opcionActual?.puedePublicarATodos == true
    }
    val formularioValido = titulo.isNotBlank() && contenido.isNotBlank() &&
            (esEdicion || (opcionActual != null && audienciaValida))

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
                text = if (esEdicion) "EDITAR ANUNCIO" else "NUEVO ANUNCIO",
                color = SportProVerde,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = if (esEdicion) "Actualizar anuncio" else "Crear anuncio",
                color = SportProNavy,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.headlineSmall
            )

            if (esEdicion) {
                Surface(color = SportProVerdeClaro, shape = FormaCampoSportPro) {
                    Text(
                        text = "La audiencia no se puede cambiar. Si te equivocaste, " +
                                "archiva este anuncio y crea uno nuevo.",
                        color = SportProNavy,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            } else {

                // Selector de academia (solo si gestiona más de una)
                if (opciones.size > 1) {
                    TituloCampo("ACADEMIA")
                    FilaDeChips {
                        opciones.forEach { opcion ->
                            ChipAnuncio(opcion.academia.nombre, opcion.academia.id == idAcademia) {
                                idAcademia = opcion.academia.id
                                tipo = audienciaPorDefecto(opcion)
                                idEquipo = ""
                            }
                        }
                    }
                }

                TituloCampo("AUDIENCIA")
                FilaDeChips {
                    if (opcionActual?.puedePublicarATodos == true) {
                        ChipAnuncio("Toda la academia", tipo == TipoAudiencia.ACADEMIA) {
                            tipo = TipoAudiencia.ACADEMIA
                        }
                        ChipAnuncio("Por rol", tipo == TipoAudiencia.ROL) {
                            tipo = TipoAudiencia.ROL
                        }
                    }
                    ChipAnuncio("Un equipo", tipo == TipoAudiencia.EQUIPO) {
                        tipo = TipoAudiencia.EQUIPO
                    }
                }

                if (tipo == TipoAudiencia.EQUIPO) {
                    TituloCampo("EQUIPO")
                    if (equiposDisponibles.isEmpty()) {
                        Text(
                            text = "No hay equipos activos disponibles.",
                            color = SportProTextoSecundario,
                            fontSize = 13.sp
                        )
                    } else {
                        FilaDeChips {
                            equiposDisponibles.forEach { equipo ->
                                ChipAnuncio("${equipo.nombre} · ${equipo.categoria}", equipo.id == idEquipo) {
                                    idEquipo = equipo.id
                                }
                            }
                        }
                    }
                }

                if (tipo == TipoAudiencia.ROL) {
                    TituloCampo("ROL DESTINO")
                    FilaDeChips {
                        listOf(Rol.DT, Rol.JUG, Rol.PAD).forEach { rol ->
                            ChipAnuncio(textoRol(rol), rolDestino == rol) { rolDestino = rol }
                        }
                    }
                }
            }

            TituloCampo("TÍTULO")
            OutlinedTextField(
                value = titulo,
                onValueChange = { if (it.length <= 80) titulo = it },
                placeholder = { Text("Ej. Cambio de horario — Entrenamientos Sub-15") },
                singleLine = true,
                shape = FormaCampoSportPro,
                supportingText = { Text("${titulo.length} / 80") },
                modifier = Modifier.fillMaxWidth()
            )

            TituloCampo("CONTENIDO")
            OutlinedTextField(
                value = contenido,
                onValueChange = { if (it.length <= 1000) contenido = it },
                placeholder = { Text("Escribe el comunicado...") },
                minLines = 4,
                shape = FormaCampoSportPro,
                supportingText = { Text("${contenido.length} / 1000") },
                modifier = Modifier.fillMaxWidth()
            )

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
                        if (esEdicion) onGuardarEdicion(titulo, contenido)
                        else onPublicar(titulo, contenido, idAcademia, tipo, idEquipo, rolDestino)
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
                        Text(
                            text = if (esEdicion) "Guardar" else "Publicar",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private fun audienciaPorDefecto(opcion: OpcionesPublicacion?): TipoAudiencia =
    if (opcion?.puedePublicarATodos == true) TipoAudiencia.ACADEMIA else TipoAudiencia.EQUIPO

@Composable
private fun TituloCampo(texto: String) {
    Text(
        text = texto,
        color = SportProTextoSecundario,
        fontFamily = FontFamily.Monospace,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
    )
}

// Fila de chips que se desliza horizontalmente si no caben
@Composable
private fun FilaDeChips(contenido: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        content = contenido
    )
}