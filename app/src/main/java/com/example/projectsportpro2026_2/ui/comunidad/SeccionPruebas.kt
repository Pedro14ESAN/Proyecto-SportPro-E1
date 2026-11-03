package com.example.projectsportpro2026_2.ui.comunidad

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectsportpro2026_2.datos.modelo.AvisoPrueba
import com.example.projectsportpro2026_2.datos.modelo.EstadoAviso
import com.example.projectsportpro2026_2.datos.modelo.Posiciones
import com.example.projectsportpro2026_2.ui.comun.*
import com.example.projectsportpro2026_2.ui.partidos.FormaCampoSportPro
import com.example.projectsportpro2026_2.ui.partidos.FormaTarjetaSportPro
import com.example.projectsportpro2026_2.ui.partidos.SportProBorde
import com.example.projectsportpro2026_2.ui.partidos.SportProNavy
import com.example.projectsportpro2026_2.ui.partidos.SportProRojo
import com.example.projectsportpro2026_2.ui.partidos.SportProSuperficie
import com.example.projectsportpro2026_2.ui.partidos.SportProTextoPrincipal
import com.example.projectsportpro2026_2.ui.partidos.SportProTextoSecundario
import com.example.projectsportpro2026_2.ui.partidos.SportProVerde
import com.example.projectsportpro2026_2.ui.partidos.SportProVerdeClaro
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun LazyListScope.seccionPruebas(
    estado: EstadoAvisos,
    onActualizarFiltros: ((FiltrosAvisos) -> FiltrosAvisos) -> Unit,
    onLimpiarFiltros: () -> Unit,
    onReintentar: () -> Unit,
    onCerrar: (AvisoPrueba) -> Unit,
    onReabrir: (AvisoPrueba) -> Unit,
    onMasAviso: (AvisoPrueba) -> Unit = {}
) {
    item { FiltrosPruebas(estado, onActualizarFiltros, onLimpiarFiltros) }

    estado.error?.let { texto ->
        item { MensajeErrorSportPro(texto, onReintentar) }
    }

    val lista = estado.avisosFiltrados()

    when {
        estado.cargando && estado.avisos.isEmpty() -> item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = SportProVerde)
            }
        }

        lista.isEmpty() -> item {
            Text(
                text = if (estado.filtros.hayFiltros) "No hay pruebas con esos filtros."
                else "Aún no hay pruebas abiertas publicadas.",
                color = SportProTextoSecundario,
                modifier = Modifier.padding(vertical = 24.dp)
            )
        }

        else -> items(lista, key = { it.id }) { aviso ->
            TarjetaAvisoPrueba(
                aviso = aviso,
                puedeGestionar = estado.puedeGestionar(aviso),
                onCerrar = { onCerrar(aviso) },
                onReabrir = { onReabrir(aviso) },
                onMas = { onMasAviso(aviso) }
            )
        }
    }
}

@Composable
private fun FiltrosPruebas(
    estado: EstadoAvisos,
    onActualizar: ((FiltrosAvisos) -> FiltrosAvisos) -> Unit,
    onLimpiar: () -> Unit
) {
    val f = estado.filtros

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Desplegable(
                etiqueta = "Categoría",
                opciones = estado.categoriasDisponibles,
                seleccionado = f.categoria,
                modifier = Modifier.weight(1f)
            ) { valor -> onActualizar { it.copy(categoria = valor) } }

            Desplegable(
                etiqueta = "Posición",
                opciones = Posiciones.lista,
                seleccionado = f.posicion,
                modifier = Modifier.weight(1f)
            ) { valor -> onActualizar { it.copy(posicion = valor) } }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Desplegable(
                etiqueta = "Ubicación",
                opciones = estado.ubicacionesDisponibles,
                seleccionado = f.ubicacion,
                modifier = Modifier.weight(1f)
            ) { valor -> onActualizar { it.copy(ubicacion = valor) } }

            Desplegable(
                etiqueta = "Fecha",
                opciones = FiltroFecha.entries.drop(1).map { it.texto },
                seleccionado = f.fecha.takeIf { it != FiltroFecha.TODAS }?.texto,
                textoNinguno = FiltroFecha.TODAS.texto,
                modifier = Modifier.weight(1f)
            ) { valor ->
                onActualizar {
                    it.copy(fecha = FiltroFecha.entries.find { e -> e.texto == valor } ?: FiltroFecha.TODAS)
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(
                checked = f.incluirNoVigentes,
                onCheckedChange = { activo -> onActualizar { it.copy(incluirNoVigentes = activo) } },
                colors = SwitchDefaults.colors(checkedTrackColor = SportProVerde)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Mostrar cerradas y vencidas",
                color = SportProTextoSecundario,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f)
            )
            if (f.hayFiltros) {
                TextButton(onClick = onLimpiar) {
                    Text("Limpiar", color = SportProVerde, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
internal fun Desplegable(
    etiqueta: String,
    opciones: List<String>,
    seleccionado: String?,
    modifier: Modifier = Modifier,
    textoNinguno: String? = "Todas",
    onElegir: (String?) -> Unit
) {
    var abierto by remember { mutableStateOf(false) }

    Box(modifier) {
        Surface(
            onClick = { abierto = true },
            shape = FormaCampoSportPro,
            color = if (seleccionado != null) SportProVerdeClaro else SportProSuperficie,
            border = BorderStroke(1.dp, if (seleccionado != null) SportProVerde else SportProBorde),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = seleccionado ?: etiqueta,
                    color = if (seleccionado != null) SportProNavy else SportProTextoSecundario,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text("▾", color = SportProTextoSecundario)
            }
        }

        DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
            if (textoNinguno != null) {
                DropdownMenuItem(
                    text = { Text(textoNinguno) },
                    onClick = {
                        abierto = false
                        onElegir(null)
                    }
                )
            }
            if (opciones.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("Sin opciones todavía", color = SportProTextoSecundario) },
                    onClick = { abierto = false },
                    enabled = false
                )
            }
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = opcion,
                            fontWeight = if (opcion == seleccionado) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        abierto = false
                        onElegir(opcion)
                    }
                )
            }
        }
    }
}

private fun formatearFechaPrueba(millis: Long): String =
    SimpleDateFormat("EEE, dd MMM yyyy · HH:mm", Locale.forLanguageTag("es-PE")).format(Date(millis))

@Composable
private fun TarjetaAvisoPrueba(
    aviso: AvisoPrueba,
    puedeGestionar: Boolean,
    onCerrar: () -> Unit,
    onReabrir: () -> Unit,
    onMas: () -> Unit
) {
    val estadoVisible = aviso.estadoEfectivo()
    val vigente = estadoVisible == EstadoAviso.ABIERTO
    val puedeReabrir = aviso.estado == EstadoAviso.CERRADO && aviso.fechaPrueba > System.currentTimeMillis()
    val hayAcciones = puedeGestionar && (vigente || puedeReabrir)
    var menuAbierto by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (vigente) 1f else 0.6f),
        shape = FormaTarjetaSportPro,
        colors = CardDefaults.cardColors(containerColor = SportProSuperficie),
        border = BorderStroke(1.dp, if (vigente) SportProVerde else SportProBorde)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = SportProNavy
                ) {
                    Box(contentAlignment = Alignment.Center) { Text("⚽") }
                }
                Spacer(Modifier.width(10.dp))

                Column(Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = aviso.nombreAcademia,
                            color = SportProNavy,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        EtiquetaSportPro(aviso.rolAutor.name, EtiquetaNaranja, EtiquetaNaranjaFondo)
                    }
                    Text(
                        text = "Publicación oficial verificada",
                        color = SportProTextoSecundario,
                        fontSize = 10.sp
                    )
                }

                if (hayAcciones) {
                    Box {
                        Text(
                            text = "•••",
                            color = SportProTextoSecundario,
                            modifier = Modifier
                                .clickable { menuAbierto = true }
                                .padding(8.dp)
                        )
                        DropdownMenu(expanded = menuAbierto, onDismissRequest = { menuAbierto = false }) {
                            if (vigente) {
                                DropdownMenuItem(
                                    text = { Text("Cerrar convocatoria", color = SportProRojo) },
                                    onClick = {
                                        menuAbierto = false
                                        onCerrar()
                                    }
                                )
                            }
                            if (puedeReabrir) {
                                DropdownMenuItem(
                                    text = { Text("Reabrir convocatoria") },
                                    onClick = {
                                        menuAbierto = false
                                        onReabrir()
                                    }
                                )
                            }
                        }
                    }
                } else if (!puedeGestionar) {
                    Text(
                        text = "•••",
                        color = SportProTextoSecundario,
                        modifier = Modifier
                            .clickable { onMas() }
                            .padding(8.dp)
                    )
                }
            }

            when (estadoVisible) {
                EstadoAviso.ABIERTO -> EtiquetaSportPro("● PRUEBA ABIERTA", SportProVerde, SportProVerdeClaro)
                EstadoAviso.CERRADO -> EtiquetaSportPro("CERRADA", EtiquetaGris, EtiquetaGrisFondo)
                EstadoAviso.VENCIDO -> EtiquetaSportPro("VENCIDA", EtiquetaNaranja, EtiquetaNaranjaFondo)
                EstadoAviso.OCULTO -> Unit
            }

            Text(
                text = aviso.titulo(),
                color = SportProNavy,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                DatoAviso("🏆", "Categoría", aviso.categoria)
                DatoAviso(
                    "⚽", "Posición",
                    if (aviso.posicionAbierta()) "Abierta (todas)" else aviso.posiciones.joinToString(" / ")
                )
                DatoAviso("📅", "Fecha", formatearFechaPrueba(aviso.fechaPrueba))
                DatoAviso("📍", "Ubicación", aviso.ubicacion)
            }

            HorizontalDivider(color = SportProBorde)

            Text(
                text = aviso.descripcion,
                color = SportProTextoPrincipal,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun DatoAviso(emoji: String, etiqueta: String, valor: String) {
    Text(
        text = "$emoji $etiqueta: $valor",
        color = SportProTextoSecundario,
        fontSize = 13.sp
    )
}