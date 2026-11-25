package com.example.projectsportpro2026_2.ui.vinculaciones

import com.example.projectsportpro2026_2.ui.comun.TipoEstado
import com.example.projectsportpro2026_2.ui.comun.ChipEstado
import com.example.projectsportpro2026_2.ui.comun.CabeceraCancha
import com.example.projectsportpro2026_2.ui.theme.FamiliaEtiquetas
import com.example.projectsportpro2026_2.ui.theme.FamiliaTitulos
import com.example.projectsportpro2026_2.ui.theme.LineaDeCal
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectsportpro2026_2.datos.repositorio.CuentaPorVincular

// Colores locales de esta pantalla (no se modifica el tema global)
private val VerdePrincipal: Color @Composable get() = LineaDeCal.colores.verde // antes Color(0xFF1B9A5C)
private val VerdeOscuro: Color @Composable get() = LineaDeCal.colores.okTexto // antes Color(0xFF14804B)
private val VerdeClaro: Color @Composable get() = LineaDeCal.colores.okFondo // antes Color(0xFFEAF7EF)
private val FondoPantalla: Color @Composable get() = LineaDeCal.colores.fondo // antes Color(0xFFFAFAF7)
private val TituloOscuro: Color @Composable get() = LineaDeCal.colores.textoPrincipal // antes Color(0xFF0F2438)
private val BordeCampo: Color @Composable get() = LineaDeCal.colores.borde // antes Color(0xFFD5E3DC)
private val TextoSecundario: Color @Composable get() = LineaDeCal.colores.textoSecundario // antes Color(0xFF6B7A80)
private val AvisoFondo: Color @Composable get() = LineaDeCal.colores.amarilloFondo // antes Color(0xFFFFF3DC)
private val AvisoTexto: Color @Composable get() = LineaDeCal.colores.amarilloTexto // antes Color(0xFF8A5A12)

@Composable
fun PantallaCuentasJugadores(
    onVolver: () -> Unit,
    viewModel: CuentasJugadoresViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoPantalla)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Línea de cal: cabecera de césped (mismos textos y el mismo "Volver")
        CabeceraCancha(
            titulo = "Cuentas de jugadores",
            subtitulo = "Cuentas cuyo documento coincide con una ficha que aún no tiene cuenta.",
            etiqueta = "GESTIÓN",
            onVolver = onVolver,
            descripcionVolver = "Volver"
        )

        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

        estado.aviso?.let {
            Text(it, color = VerdeOscuro, fontWeight = FontWeight.SemiBold)
        }
        estado.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        if (estado.cargando && estado.cuentas.isEmpty()) {
            CircularProgressIndicator(color = VerdePrincipal)
        } else if (estado.cuentas.isEmpty()) {
            Text(
                "No hay cuentas de jugadores por vincular.",
                color = TextoSecundario
            )
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(estado.cuentas, key = { it.perfil.uid + it.ficha.id }) { cuenta ->
                TarjetaCuenta(
                    cuenta = cuenta,
                    habilitado = !estado.cargando,
                    onVincular = { viewModel.vincular(cuenta) }
                )
            }
        }
        }
    }
}

@Composable
private fun TarjetaCuenta(
    cuenta: CuentaPorVincular,
    habilitado: Boolean,
    onVincular: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = LineaDeCal.colores.superficie),
        border = BorderStroke(1.dp, BordeCampo)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    cuenta.perfil.nombre,
                    color = TituloOscuro,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f)
                )
                ChipEstado(
                    if (cuenta.pendiente) "Pendiente" else "Activa",
                    if (cuenta.pendiente) TipoEstado.PENDIENTE else TipoEstado.OK
                )
            }
            Text(cuenta.perfil.correo, color = TextoSecundario, fontSize = 14.sp)
            Text(
                "Ficha: ${cuenta.ficha.nombreCompleto()} · ${cuenta.ficha.categoria}",
                color = TituloOscuro,
                fontSize = 14.sp
            )
            Text(
                "Documento: ${cuenta.ficha.documento}",
                color = TextoSecundario,
                fontSize = 13.sp
            )

            if (cuenta.pendiente) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AvisoFondo)
                        .padding(10.dp)
                ) {
                    Text(
                        text = if (cuenta.tieneApoderado) {
                            "La ficha ya tiene un apoderado con vinculación activa: se activará al vincular."
                        } else {
                            "La ficha aún no tiene apoderado con vinculación activa: se vinculará, pero seguirá pendiente hasta que autorices a un apoderado."
                        },
                        color = AvisoTexto,
                        fontSize = 12.sp
                    )
                }
            }

            Button(
                onClick = onVincular,
                enabled = habilitado,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VerdePrincipal,
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (cuenta.seActivara) "Vincular y activar" else "Vincular",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}