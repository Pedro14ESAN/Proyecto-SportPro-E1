package com.example.projectsportpro2026_2.ui.notificaciones

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.EventBusy
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.SportsSoccer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import com.example.projectsportpro2026_2.ui.theme.LineaDeCal
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectsportpro2026_2.datos.modelo.Notificacion
import com.example.projectsportpro2026_2.datos.modelo.TipoNotificacion
import com.example.projectsportpro2026_2.ui.comun.*
import com.example.projectsportpro2026_2.ui.partidos.FormaBotonSportPro
import com.example.projectsportpro2026_2.ui.partidos.FormaCampoSportPro
import com.example.projectsportpro2026_2.ui.partidos.SportProBorde
import com.example.projectsportpro2026_2.ui.partidos.SportProFondo
import com.example.projectsportpro2026_2.ui.partidos.SportProNavy
import com.example.projectsportpro2026_2.ui.partidos.SportProSuperficie
import com.example.projectsportpro2026_2.ui.partidos.SportProTextoPrincipal
import com.example.projectsportpro2026_2.ui.partidos.SportProTextoSecundario
import com.example.projectsportpro2026_2.ui.partidos.SportProVerde
import com.example.projectsportpro2026_2.ui.partidos.SportProVerdeClaro

@Composable
fun PantallaNotificaciones(
    onVolver: () -> Unit,
    abrirAlIniciar: String? = null,       // HU20 (deep link): id del aviso tocado en la barra
    onAbrirAnuncios: () -> Unit,
    onAbrirEntrenamientos: () -> Unit,
    onAbrirConvocatorias: () -> Unit,
    viewModel: NotificacionesViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()
    val contexto = LocalContext.current

    var permisoConcedido by remember { mutableStateOf(MonitorNotificaciones.tienePermiso(contexto)) }
    var permisoRechazado by remember { mutableStateOf(false) }

    val pedirPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        permisoConcedido = concedido
        permisoRechazado = !concedido
    }

    LaunchedEffect(Unit) { viewModel.iniciar() }

    // A dónde lleva cada tipo de aviso (se usa al tocar la fila y en el deep link)
    val irAlContenido: (Notificacion) -> Unit = { destino ->
        when (destino.tipo) {
            TipoNotificacion.ANUNCIO -> onAbrirAnuncios()

            TipoNotificacion.ENTRENAMIENTO_PUBLICADO,
            TipoNotificacion.ENTRENAMIENTO_CAMBIADO,
            TipoNotificacion.ENTRENAMIENTO_CANCELADO -> onAbrirEntrenamientos()

            TipoNotificacion.CONVOCATORIA,
            TipoNotificacion.CONVOCATORIA_RETIRADA -> onAbrirConvocatorias()
        }
    }

    // HU20 (deep link): cuando cargan los avisos, abre el que se tocó en la barra.
    // Pasa por la misma verificación de permisos que tocar la fila.
    var deepLinkUsado by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(estado.notificaciones, abrirAlIniciar) {
        if (deepLinkUsado || abrirAlIniciar.isNullOrBlank()) return@LaunchedEffect
        val aviso = estado.notificaciones.find { it.id == abrirAlIniciar } ?: return@LaunchedEffect
        deepLinkUsado = true
        viewModel.abrir(aviso, irAlContenido)
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
                Row(
                    modifier = Modifier.clickable { onVolver() }.padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null, tint = SportProVerde, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Inicio",
                        color = SportProVerde,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Notificaciones",
                        color = SportProNavy,
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    if (estado.noLeidas > 0) {
                        Spacer(Modifier.width(8.dp))
                        Surface(shape = CircleShape, color = LineaDeCal.colores.naranja) {
                            Text(
                                text = "${estado.noLeidas}",
                                color = androidx.compose.ui.graphics.Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    if (estado.noLeidas > 0) {
                        TextButton(onClick = { viewModel.marcarTodasLeidas() }) {
                            Text("Marcar como leídas", color = SportProVerde, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Criterio HU20: sin permiso, la app sigue funcionando
            if (!permisoConcedido) {
                item {
                    BannerPermiso(
                        rechazado = permisoRechazado,
                        onActivar = { pedirPermiso.launch(Manifest.permission.POST_NOTIFICATIONS) }
                    )
                }
            }

            estado.error?.let { texto ->
                item { MensajeErrorSportPro(texto) { viewModel.recargar() } }
            }

            when {
                estado.cargando && estado.notificaciones.isEmpty() -> item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = SportProVerde)
                    }
                }

                estado.notificaciones.isEmpty() && estado.error == null -> item {
                    Text(
                        text = "No tienes notificaciones todavía.",
                        color = SportProTextoSecundario,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }

                else -> items(estado.notificaciones, key = { it.id }) { notificacion ->
                    FilaNotificacion(notificacion) {
                        viewModel.abrir(notificacion, irAlContenido)
                    }
                }
            }
        }
    }
}

@Composable
private fun BannerPermiso(rechazado: Boolean, onActivar: () -> Unit) {
    Surface(color = EtiquetaAzulFondo, shape = FormaCampoSportPro, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = if (rechazado) {
                    "Sin el permiso no verás avisos en la barra del celular, pero igual quedan aquí. " +
                            "Puedes activarlo después en los ajustes del celular."
                } else {
                    "Activa las notificaciones para enterarte al instante de convocatorias, " +
                            "entrenamientos y anuncios."
                },
                color = EtiquetaAzul,
                fontSize = 12.sp
            )
            if (!rechazado) {
                Button(
                    onClick = onActivar,
                    shape = FormaBotonSportPro,
                    colors = ButtonDefaults.buttonColors(containerColor = LineaDeCal.colores.superficieOscura)
                ) {
                    Text("Activar notificaciones", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun iconoNotificacion(tipo: TipoNotificacion): ImageVector =
    when (tipo) {
        TipoNotificacion.CONVOCATORIA -> Icons.Rounded.SportsSoccer
        TipoNotificacion.CONVOCATORIA_RETIRADA -> Icons.AutoMirrored.Rounded.Undo
        TipoNotificacion.ENTRENAMIENTO_PUBLICADO -> Icons.AutoMirrored.Rounded.DirectionsRun
        TipoNotificacion.ENTRENAMIENTO_CAMBIADO -> Icons.Rounded.Schedule
        TipoNotificacion.ENTRENAMIENTO_CANCELADO -> Icons.Rounded.EventBusy
        TipoNotificacion.ANUNCIO -> Icons.Rounded.Campaign
    }

@Composable
private fun FilaNotificacion(notificacion: Notificacion, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = FormaCampoSportPro,
        color = if (notificacion.leida) SportProSuperficie else SportProVerdeClaro,
        border = androidx.compose.foundation.BorderStroke(1.dp, SportProBorde),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(38.dp),
                shape = RoundedCornerShape(10.dp),
                color = SportProFondo
            ) {
                Box(contentAlignment = Alignment.Center) {
                    // D-17: el modelo conserva TipoNotificacion.emoji; en la UI se muestra un ícono
                    Icon(
                        iconoNotificacion(notificacion.tipo),
                        contentDescription = null,
                        tint = SportProVerde,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = notificacion.titulo,
                    color = SportProNavy,
                    fontWeight = if (notificacion.leida) FontWeight.Normal else FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(notificacion.mensaje, color = SportProTextoPrincipal, fontSize = 12.sp)
                Text(
                    text = tiempoRelativo(notificacion.fechaCreacion),
                    color = SportProTextoSecundario,
                    fontSize = 12.sp
                )
            }

            if (!notificacion.leida) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(EtiquetaAzul, CircleShape)
                )
            }
        }
    }
}