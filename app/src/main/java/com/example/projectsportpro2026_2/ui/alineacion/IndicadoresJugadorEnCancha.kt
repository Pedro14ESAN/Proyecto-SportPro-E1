package com.example.projectsportpro2026_2.ui.alineacion

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
        color = Color.White,
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
                    texto = simboloEvento("GOL") + cantidad,
                    descripcion = "$nombreJugador, ${indicadores.goles} " +
                        if (indicadores.goles == 1) "gol" else "goles"
                )
            }
            if (indicadores.amarilla) {
                InsigniaJugador(
                    texto = simboloEvento("TARJETA", TipoTarjeta.AMARILLA),
                    descripcion = "$nombreJugador, tarjeta amarilla"
                )
            }
            if (indicadores.roja) {
                InsigniaJugador(
                    texto = simboloEvento("TARJETA", TipoTarjeta.ROJA),
                    descripcion = "$nombreJugador, tarjeta roja"
                )
            }
        }
    }
}

@Composable
private fun InsigniaJugador(texto: String, descripcion: String) {
    Text(
        text = texto,
        modifier = Modifier.semantics { contentDescription = descripcion },
        color = SportProNavy,
        fontSize = 10.sp,
        lineHeight = 12.sp,
        fontWeight = FontWeight.Bold
    )
}
