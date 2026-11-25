package com.example.projectsportpro2026_2.ui.alineacion

import com.example.projectsportpro2026_2.ui.sincronizacion.colorSimboloEvento
import com.example.projectsportpro2026_2.ui.sincronizacion.iconoEvento
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Icon
import com.example.projectsportpro2026_2.ui.theme.LineaDeCal
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectsportpro2026_2.datos.alineacion.IndicadoresJugadorEnVivo
import com.example.projectsportpro2026_2.datos.modelo.evento.TipoTarjeta
import com.example.projectsportpro2026_2.ui.partidos.SportProNavy
import com.example.projectsportpro2026_2.ui.sincronizacion.simboloEvento

@Composable
internal fun IndicadoresJugadorEnCancha(
    indicadores: IndicadoresJugadorEnVivo,
    nombreJugador: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = LineaDeCal.colores.superficie,
        shape = RoundedCornerShape(6.dp),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            if (indicadores.goles > 0) {
                val cantidad = if (indicadores.goles > 1) "×${indicadores.goles}" else ""
                InsigniaJugador(
                    icono = iconoEvento("GOL"),
                    tinte = colorSimboloEvento("GOL"),
                    texto = cantidad,
                    descripcion = "$nombreJugador, ${indicadores.goles} " +
                        if (indicadores.goles == 1) "gol" else "goles"
                )
            }
            if (indicadores.amarilla) {
                InsigniaJugador(
                    icono = iconoEvento("TARJETA", TipoTarjeta.AMARILLA),
                    tinte = colorSimboloEvento("TARJETA", TipoTarjeta.AMARILLA),
                    texto = "",
                    descripcion = "$nombreJugador, tarjeta amarilla"
                )
            }
            if (indicadores.roja) {
                InsigniaJugador(
                    icono = iconoEvento("TARJETA", TipoTarjeta.ROJA),
                    tinte = colorSimboloEvento("TARJETA", TipoTarjeta.ROJA),
                    texto = "",
                    descripcion = "$nombreJugador, tarjeta roja"
                )
            }
        }
    }
}

@Composable
private fun InsigniaJugador(icono: ImageVector, tinte: Color, texto: String, descripcion: String) {
    // Línea de cal: ícono en lugar del emoji. La descripción sigue diciendo "N goles" o la tarjeta,
    // y el texto visible es la cantidad ("×2") cuando hay más de un gol.
    Row(
        modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = descripcion },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icono, contentDescription = null, tint = tinte, modifier = Modifier.size(13.dp))
        if (texto.isNotEmpty()) {
            Text(
                text = texto,
                color = SportProNavy,
                fontSize = 12.sp,
                lineHeight = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
