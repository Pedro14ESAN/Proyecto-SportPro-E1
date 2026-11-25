package com.example.projectsportpro2026_2.ui.navegacion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.SportsSoccer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectsportpro2026_2.datos.modelo.EstadoCuenta
import com.example.projectsportpro2026_2.datos.modelo.PerfilUsuario
import com.example.projectsportpro2026_2.datos.modelo.Rol
import com.example.projectsportpro2026_2.ui.theme.FamiliaCuerpo
import com.example.projectsportpro2026_2.ui.theme.LineaDeCal
import com.example.projectsportpro2026_2.ui.theme.ProjectSportPro2026_2Theme

/*
 * Línea de cal · barra de navegación inferior (cambio de alcance aprobado, img 01 y 05 a 09).
 * Es un atajo adicional: no reemplaza ningún botón del inicio ni ninguna ruta. Solo se
 * muestra en las pantallas principales de cada rol (las rutas de sus pestañas).
 */

/** Una pestaña de la barra inferior. */
data class PestanaNavegacion(
    val ruta: String,
    val etiqueta: String,
    val icono: ImageVector
)

/**
 * Pestañas según el rol. Lista vacía (sin barra) si no hay sesión o si la cuenta está
 * pendiente de aprobación.
 */
fun pestanasPara(perfil: PerfilUsuario?): List<PestanaNavegacion> {
    if (perfil == null || perfil.estado == EstadoCuenta.PENDIENTE) return emptyList()

    val inicio = PestanaNavegacion(rutaInicioPara(perfil), "Inicio", Icons.Rounded.Home)
    val partidos = PestanaNavegacion(Rutas.MIS_PARTIDOS, "Partidos", Icons.Rounded.SportsSoccer)
    val comunidad = PestanaNavegacion(Rutas.COMUNIDAD, "Comunidad", Icons.Rounded.Forum)
    val perfilPestana = PestanaNavegacion(Rutas.PERFIL, "Perfil", Icons.Rounded.Person)

    val segunda = when (perfil.rol) {
        Rol.ADM, Rol.DT -> PestanaNavegacion(Rutas.ACADEMIAS, "Academias", Icons.Rounded.Shield)
        Rol.JUG -> PestanaNavegacion(Rutas.ENTRENAMIENTOS, "Entrenamientos", Icons.Rounded.FitnessCenter)
        Rol.PAD -> PestanaNavegacion(Rutas.MIS_JUGADORES, "Mis jugadores", Icons.Rounded.Groups)
    }

    return listOf(inicio, segunda, partidos, comunidad, perfilPestana)
}

/**
 * Barra flotante navy con esquinas redondeadas y margen. El ítem activo va en lima con
 * una rayita debajo; los demás en blanco atenuado.
 */
@Composable
fun BarraNavegacionInferior(
    pestanas: List<PestanaNavegacion>,
    rutaActual: String?,
    onElegir: (PestanaNavegacion) -> Unit,
    modifier: Modifier = Modifier
) {
    val c = LineaDeCal.colores
    val forma = RoundedCornerShape(22.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxWidth()
                .shadow(10.dp, forma)
                .clip(forma)
                .background(c.superficieOscura)
                .padding(horizontal = 6.dp, vertical = 8.dp)
                .selectableGroup(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            pestanas.forEach { pestana ->
                ItemBarra(
                    pestana = pestana,
                    activo = pestana.ruta == rutaActual,
                    onClick = { onElegir(pestana) },
                    // Las etiquetas largas ("Entrenamientos", "Mis jugadores") reciben más ancho
                    modifier = Modifier.weight((pestana.etiqueta.length + 3).toFloat())
                )
            }
        }
    }
}

@Composable
private fun ItemBarra(
    pestana: PestanaNavegacion,
    activo: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = LineaDeCal.colores
    val color = if (activo) c.lima else Color.White.copy(alpha = 0.62f)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .selectable(selected = activo, onClick = onClick, role = Role.Tab)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = pestana.icono,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = pestana.etiqueta,
            color = color,
            fontFamily = FamiliaCuerpo,
            fontWeight = if (activo) FontWeight.SemiBold else FontWeight.Medium,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(3.dp))
        // Rayita lima debajo del ítem activo (transparente en los demás, para no mover el texto)
        Box(
            modifier = Modifier
                .width(18.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(if (activo) c.lima else Color.Transparent)
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun VistaBarraNavegacion() {
    ProjectSportPro2026_2Theme {
        val pestanas = pestanasPara(PerfilUsuario(nombre = "Daniel", rol = Rol.JUG))
        BarraNavegacionInferior(pestanas, Rutas.INICIO_JUG, onElegir = {})
    }
}
