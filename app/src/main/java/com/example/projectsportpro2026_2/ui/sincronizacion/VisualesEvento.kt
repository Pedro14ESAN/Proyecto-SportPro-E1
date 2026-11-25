package com.example.projectsportpro2026_2.ui.sincronizacion

import com.example.projectsportpro2026_2.ui.theme.EstiloMarcador
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.projectsportpro2026_2.ui.theme.LineaDeCal
import com.example.projectsportpro2026_2.ui.theme.FamiliaEtiquetas
import com.example.projectsportpro2026_2.datos.partido.eventosValidos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectsportpro2026_2.datos.modelo.evento.EstadoEventoPartido
import com.example.projectsportpro2026_2.datos.modelo.evento.EventoPartido
import com.example.projectsportpro2026_2.datos.modelo.evento.TipoTarjeta
import com.example.projectsportpro2026_2.ui.partidos.*

@Composable
fun IconoEvento(codigo: String, tipoTarjeta: TipoTarjeta = TipoTarjeta.NINGUNA) {
    val estilo = estiloEventoEnVivo(codigo)
    val roja = codigo == "TARJETA" && tipoTarjeta == TipoTarjeta.ROJA
    Surface(
        modifier = Modifier.size(46.dp),
        shape = RoundedCornerShape(13.dp),
        color = if (roja) SportProRojoClaro else estilo.fondoIcono
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = iconoEvento(codigo, tipoTarjeta),
                contentDescription = null,
                tint = if (roja) SportProRojo else estilo.colorIcono,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
fun JugadoresEvento(evento: EventoPartido) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        if (evento.codigoTipoEvento == "CAMBIO" &&
            (evento.nombreJugadorSale.isNotBlank() || evento.nombreJugadorEntra.isNotBlank())) {
            if (evento.nombreJugadorSale.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.ArrowDownward, contentDescription = null, tint = SportProRojo,
                        modifier = Modifier.size(16.dp))
                    Text(" Sale: ${evento.nombreJugadorSale}", color = SportProRojo,
                        style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (evento.nombreJugadorEntra.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.ArrowUpward, contentDescription = null, tint = SportProVerde,
                        modifier = Modifier.size(16.dp))
                    Text(" Entra: ${evento.nombreJugadorEntra}", color = SportProVerde,
                        style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else if (evento.nombreJugador.isNotBlank()) {
            Text(evento.nombreJugador, color = SportProNavy,
                style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
    }
}
data class EstiloEventoEnVivo(
    val icono: ImageVector,
    val fondoIcono: Color,
    val colorIcono: Color,
    val borde: Color
)

@androidx.compose.runtime.Composable
fun estiloEventoEnVivo(
    codigo: String
): EstiloEventoEnVivo =
    when (codigo) {

        "INICIO" ->
            EstiloEventoEnVivo(
                icono = iconoEvento(codigo),
                fondoIcono =
                    SportProVerdeClaro,
                colorIcono =
                    SportProVerde,
                borde =
                    LineaDeCal.colores.okBorde /* antes 0xFFB9E0C3 */
            )

        "FIN" ->
            EstiloEventoEnVivo(
                icono = iconoEvento(codigo),
                fondoIcono =
                    LineaDeCal.colores.apagadoFondo /* antes 0xFFE8ECEF */,
                colorIcono =
                    SportProNavy,
                borde =
                    SportProBorde
            )

        "GOL" ->
            EstiloEventoEnVivo(
                icono = iconoEvento(codigo),
                fondoIcono =
                    SportProVerdeClaro,
                colorIcono =
                    SportProVerde,
                borde =
                    LineaDeCal.colores.okBorde /* antes 0xFFB9E0C3 */
            )

        "TARJETA" ->
            EstiloEventoEnVivo(
                icono = iconoEvento(codigo),
                fondoIcono =
                    LineaDeCal.colores.amarilloFondo /* antes 0xFFFFF3C4 */,
                colorIcono =
                    LineaDeCal.colores.amarilloTexto /* antes 0xFF9A7200 */,
                borde =
                    LineaDeCal.colores.amarilloBorde /* antes 0xFFE8D17A */
            )

        "CAMBIO" ->
            EstiloEventoEnVivo(
                icono = iconoEvento(codigo),
                fondoIcono =
                    LineaDeCal.colores.azulFondo /* antes 0xFFE7F0F8 */,
                colorIcono =
                    LineaDeCal.colores.azulTexto /* antes 0xFF28659A */,
                borde =
                    LineaDeCal.colores.borde /* antes 0xFFB9D2E7 */
            )

        "FUERA_JUEGO" ->
            EstiloEventoEnVivo(
                icono = iconoEvento(codigo),
                fondoIcono =
                    LineaDeCal.colores.lilaFondo /* antes 0xFFF0EBF8 */,
                colorIcono =
                    LineaDeCal.colores.lilaTexto /* antes 0xFF67508A */,
                borde =
                    LineaDeCal.colores.borde /* antes 0xFFD4C8E8 */
            )

        "FALTA" ->
            EstiloEventoEnVivo(
                icono = iconoEvento(codigo),
                fondoIcono =
                    SportProRojoClaro,
                colorIcono =
                    SportProRojo,
                borde =
                    LineaDeCal.colores.errorBorde /* antes 0xFFF0C6C2 */
            )

        "PENAL" ->
            EstiloEventoEnVivo(
                icono = iconoEvento(codigo),
                fondoIcono =
                    LineaDeCal.colores.pendienteFondo /* antes 0xFFFFEEDB */,
                colorIcono =
                    LineaDeCal.colores.pendienteTexto /* antes 0xFFA85D16 */,
                borde =
                    LineaDeCal.colores.pendienteBorde /* antes 0xFFF0D0A9 */
            )

        "TIRO_ESQUINA" ->
            EstiloEventoEnVivo(
                icono = iconoEvento(codigo),
                fondoIcono =
                    LineaDeCal.colores.azulFondo /* antes 0xFFE7F0F8 */,
                colorIcono =
                    LineaDeCal.colores.azulTexto /* antes 0xFF28659A */,
                borde =
                    LineaDeCal.colores.borde /* antes 0xFFB9D2E7 */
            )

        "SAQUE_META" ->
            EstiloEventoEnVivo(
                icono = iconoEvento(codigo),
                fondoIcono =
                    LineaDeCal.colores.apagadoFondo /* antes 0xFFEAF1EC */,
                colorIcono =
                    SportProNavy,
                borde =
                    SportProBorde
            )

        "SAQUE_LATERAL" ->
            EstiloEventoEnVivo(
                icono = iconoEvento(codigo),
                fondoIcono =
                    LineaDeCal.colores.apagadoFondo /* antes 0xFFEAF1EC */,
                colorIcono =
                    SportProNavy,
                borde =
                    SportProBorde
            )

        else ->
            EstiloEventoEnVivo(
                icono = iconoEvento(codigo),
                fondoIcono =
                    SportProVerdeClaro,
                colorIcono =
                    SportProVerde,
                borde =
                    SportProBorde
            )
    }

@Composable
internal fun ResumenEventosEnVivo(
    eventos: List<EventoPartido>,
    modifier: Modifier = Modifier
) {
    val activos = remember(eventos) {
        eventosValidos(eventos)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = LineaDeCal.colores.apagadoFondo /* antes 0xFFF3F6F5 */,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, LineaDeCal.colores.borde /* antes 0xFFE2E8E6 */)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Resumen de eventos",
                color = SportProTextoSecundario,
                fontFamily = FamiliaEtiquetas,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )

            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                CifraResumen(activos.count { it.afectaMarcador }, "Goles", Modifier.weight(1f))
                CifraResumen(
                    activos.count { it.codigoTipoEvento == "TARJETA" },
                    "Tarjetas",
                    Modifier.weight(1f)
                )
                CifraResumen(
                    activos.count { it.codigoTipoEvento == "CAMBIO" },
                    "Cambios",
                    Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CifraResumen(
    valor: Int,
    etiqueta: String,
    modifier: Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$valor",
            color = SportProNavy,
            style = EstiloMarcador.copy(fontSize = 26.sp, lineHeight = 28.sp)
        )

        Text(
            text = etiqueta,
            color = SportProTextoSecundario,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
