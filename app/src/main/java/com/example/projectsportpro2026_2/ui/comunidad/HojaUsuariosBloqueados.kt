package com.example.projectsportpro2026_2.ui.comunidad

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectsportpro2026_2.datos.modelo.Bloqueo
import com.example.projectsportpro2026_2.ui.comun.AvatarIniciales
import com.example.projectsportpro2026_2.ui.comun.formatearFecha
import com.example.projectsportpro2026_2.ui.partidos.FormaBotonSportPro
import com.example.projectsportpro2026_2.ui.partidos.SportProBorde
import com.example.projectsportpro2026_2.ui.partidos.SportProNavy
import com.example.projectsportpro2026_2.ui.partidos.SportProRojo
import com.example.projectsportpro2026_2.ui.partidos.SportProSuperficie
import com.example.projectsportpro2026_2.ui.partidos.SportProTextoSecundario
import com.example.projectsportpro2026_2.ui.partidos.SportProVerde

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HojaUsuariosBloqueados(
    bloqueados: List<Bloqueo>,
    cargando: Boolean,
    procesando: Boolean,
    error: String?,
    onCerrar: () -> Unit,
    onDesbloquear: (Bloqueo) -> Unit
) {
    val estadoHoja = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Usuarios bloqueados",
                color = SportProNavy,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "No ves sus publicaciones ni comentarios, y ellos no ven los tuyos.",
                color = SportProTextoSecundario,
                fontSize = 12.sp
            )

            when {
                cargando && bloqueados.isEmpty() -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = SportProVerde)
                }

                bloqueados.isEmpty() -> Text(
                    text = "No has bloqueado a nadie.",
                    color = SportProTextoSecundario,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                else -> bloqueados.forEach { bloqueo ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AvatarIniciales(bloqueo.nombreBloqueado, tamano = 36.dp)
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = bloqueo.nombreBloqueado.ifBlank { "Usuario" },
                                color = SportProNavy,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Desde ${formatearFecha(bloqueo.fecha)}",
                                color = SportProTextoSecundario,
                                fontSize = 11.sp
                            )
                        }
                        OutlinedButton(
                            onClick = { onDesbloquear(bloqueo) },
                            enabled = !procesando,
                            shape = FormaBotonSportPro
                        ) {
                            Text("Desbloquear", color = SportProNavy, fontSize = 12.sp)
                        }
                    }
                    HorizontalDivider(color = SportProBorde)
                }
            }

            if (error != null) {
                Text(error, color = SportProRojo, fontSize = 13.sp)
            }
        }
    }
}