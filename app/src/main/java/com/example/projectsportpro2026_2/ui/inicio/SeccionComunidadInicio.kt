package com.example.projectsportpro2026_2.ui.inicio

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.projectsportpro2026_2.datos.modelo.Publicacion
import com.example.projectsportpro2026_2.ui.comun.AvatarIniciales
import com.example.projectsportpro2026_2.ui.comun.BotonPrincipal
import com.example.projectsportpro2026_2.ui.comun.ChipEstado
import com.example.projectsportpro2026_2.ui.comun.ImagenRemota
import com.example.projectsportpro2026_2.ui.comun.TipoEstado
import com.example.projectsportpro2026_2.ui.comun.VarianteBoton
import com.example.projectsportpro2026_2.ui.comun.tiempoRelativo
import com.example.projectsportpro2026_2.ui.theme.FamiliaCuerpo
import com.example.projectsportpro2026_2.ui.theme.FamiliaTitulos
import com.example.projectsportpro2026_2.ui.theme.LineaDeCal

/*
 * Línea de cal · 7.1: vista previa de Comunidad en el inicio de los 4 roles (no en Cuenta
 * pendiente). Muestra las 3 publicaciones más recientes que el usuario puede ver, con el MISMO
 * filtro del feed "Para ti" (filtrarFeedComunidad), en tiempo real.
 *  - Desde aquí no se reacciona ni se comenta: tocar una tarjeta, "Ver todo" o "Publicar"
 *    abre Comunidad.
 *  - Si la consulta falla, la sección no se muestra y el resto del inicio sigue igual.
 *  - collectAsStateWithLifecycle: con la app en segundo plano o fuera del inicio no hay
 *    suscriptores y el ViewModel cierra el listener (WhileSubscribed).
 */
@Composable
fun SeccionComunidadInicio(
    viewModel: InicioViewModel,
    onComunidad: () -> Unit
) {
    val comunidad by viewModel.comunidad.collectAsStateWithLifecycle()
    // Cargando u Oculta: no se muestra nada
    val publicaciones = (comunidad as? ComunidadInicio.Lista)?.publicaciones ?: return
    val c = LineaDeCal.colores

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Comunidad",
                modifier = Modifier.weight(1f),
                color = c.textoPrincipal,
                fontFamily = FamiliaTitulos,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            TextButton(onClick = onComunidad) {
                Text(
                    "Ver todo",
                    color = c.verde,
                    fontFamily = FamiliaCuerpo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = c.verde,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        if (publicaciones.isEmpty()) {
            ComunidadVacia(onComunidad)
        } else {
            publicaciones.forEach { publicacion ->
                key(publicacion.id) {
                    // Las tarjetas nuevas entran con una animación suave; las que ya estaban no se mueven
                    val aparicion = remember { MutableTransitionState(false).apply { targetState = true } }
                    AnimatedVisibility(
                        visibleState = aparicion,
                        enter = fadeIn(tween(300)) + expandVertically(tween(300))
                    ) {
                        TarjetaComunidadInicio(publicacion, onComunidad)
                    }
                }
            }
        }
    }
}

@Composable
private fun ComunidadVacia(onComunidad: () -> Unit) {
    val c = LineaDeCal.colores
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(c.superficie)
            .border(1.dp, c.borde, RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Aún no hay publicaciones",
            color = c.textoSecundario,
            fontFamily = FamiliaCuerpo,
            fontSize = 14.sp
        )
        BotonPrincipal(
            texto = "Publicar",
            onClick = onComunidad,
            variante = VarianteBoton.LIMA,
            icono = Icons.Rounded.Edit
        )
    }
}

@Composable
private fun TarjetaComunidadInicio(publicacion: Publicacion, onComunidad: () -> Unit) {
    val c = LineaDeCal.colores
    val forma = RoundedCornerShape(16.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(c.superficie)
            .border(1.dp, c.borde, forma)
            .clickable(role = Role.Button, onClickLabel = "Abrir Comunidad", onClick = onComunidad)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AvatarIniciales(publicacion.nombreAutor, tamano = 36.dp)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        publicacion.nombreAutor,
                        color = c.textoPrincipal,
                        fontFamily = FamiliaCuerpo,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    // Mismo texto de rol que la tarjeta de Comunidad (ADM, DT, JUG, PAD)
                    ChipEstado(publicacion.rolAutor.name, TipoEstado.NEUTRO)
                }
                Text(
                    tiempoRelativo(publicacion.fechaCreacion),
                    color = c.textoSecundario,
                    fontFamily = FamiliaCuerpo,
                    fontSize = 12.sp
                )
            }
        }

        Row(verticalAlignment = Alignment.Top) {
            Text(
                publicacion.texto,
                color = c.textoPrincipal,
                fontFamily = FamiliaCuerpo,
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (publicacion.imagenUrl.isNotBlank()) {
                Spacer(Modifier.width(10.dp))
                ImagenRemota(
                    url = publicacion.imagenUrl,
                    descripcion = "Imagen de la publicación de ${publicacion.nombreAutor}",
                    modifier = Modifier.size(56.dp)
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            val total = publicacion.totalReacciones()
            val emojis = LogicaInicio.emojisReacciones(publicacion)
            if (emojis.isNotEmpty()) {
                Text(emojis, fontSize = 14.sp)
            } else {
                Icon(
                    Icons.Rounded.FavoriteBorder,
                    contentDescription = null,
                    tint = c.textoSecundario,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(Modifier.width(4.dp))
            Text(
                "$total",
                color = c.textoSecundario,
                fontFamily = FamiliaCuerpo,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )

            Spacer(Modifier.width(16.dp))

            val n = publicacion.cantidadComentarios
            Icon(
                Icons.Rounded.ChatBubbleOutline,
                contentDescription = null,
                tint = c.textoSecundario,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                "$n ${if (n == 1) "comentario" else "comentarios"}",
                color = c.textoSecundario,
                fontFamily = FamiliaCuerpo,
                fontSize = 13.sp
            )
        }
    }
}
