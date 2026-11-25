package com.example.projectsportpro2026_2.ui.comun

import androidx.compose.ui.layout.layout
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.HowToReg
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.SportsSoccer
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectsportpro2026_2.ui.theme.EstiloEtiqueta
import com.example.projectsportpro2026_2.ui.theme.EstiloMarcador
import com.example.projectsportpro2026_2.ui.theme.FamiliaCuerpo
import com.example.projectsportpro2026_2.ui.theme.FamiliaTitulos
import com.example.projectsportpro2026_2.ui.theme.LineaDeCal
import com.example.projectsportpro2026_2.ui.theme.ProjectSportPro2026_2Theme

/*
 * Rediseño "Línea de cal" · kit de componentes (ESPECIFICACION.md, sección 5).
 *
 * Todo sale de los tokens del tema (LineaDeCal.colores y las familias de Type.kt),
 * así que cada componente se ve bien en claro y en oscuro.
 *
 * Reglas que respeta el kit:
 *  - Doto SOLO en números de 18 sp o más (marcador, horas del talón, contadores).
 *    Los nombres de equipo y las etiquetas del LED van en DM Mono (sección 3.3 y
 *    PLAN.md, etapa 2): la sección 5 decía "Doto" para ellos, pero 3.3 lo prohíbe
 *    en texto.
 *  - Ningún texto mide menos de 12 sp.
 *  - Íconos de Material (Rounded); ningún emoji hace de ícono.
 */

// =====================================================================
// Utilidades internas
// =====================================================================

private val Blanco = Color.White

/** true si el texto es un número "de marcador" (16:00, 92%, 14/18, 37'). */
private fun esNumeroMarcador(texto: String): Boolean =
    texto == ":" || (
            texto.isNotBlank() &&
                    texto.any { it.isDigit() } &&
                    texto.all { it.isDigit() || it in ":%/'+-–., " }
            )

/**
 * Número grande: Doto si es numérico y mide 18 sp o más; si no, DM Mono.
 * Así nunca se usa Doto en texto corrido ni en tamaños chicos.
 */
@Composable
private fun TextoNumero(
    texto: String,
    tamanoSp: Float,
    color: Color,
    modifier: Modifier = Modifier,
    brillo: Boolean = false,
    tachado: Boolean = false,
    alineacion: TextAlign? = null
) {
    val usaDoto = tamanoSp >= 18f && esNumeroMarcador(texto)
    val base = if (usaDoto) {
        EstiloMarcador.copy(fontSize = tamanoSp.sp, lineHeight = (tamanoSp * 1.08f).sp)
    } else {
        EstiloEtiqueta.copy(
            fontSize = maxOf(12f, tamanoSp * 0.62f).sp,
            lineHeight = maxOf(16f, tamanoSp * 0.8f).sp
        )
    }
    Text(
        text = texto,
        modifier = modifier,
        color = color,
        textAlign = alineacion,
        maxLines = 1,
        style = base.copy(
            textDecoration = if (tachado) TextDecoration.LineThrough else null,
            shadow = if (brillo) Shadow(color = color.copy(alpha = 0.65f), blurRadius = 18f) else null
        )
    )
}

/** Etiqueta mono en mayúsculas (12 sp como mínimo). */
@Composable
private fun EtiquetaMono(
    texto: String,
    color: Color,
    modifier: Modifier = Modifier,
    tamanoSp: Float = 12f,
    alineacion: TextAlign? = null
) {
    Text(
        text = texto.uppercase(),
        modifier = modifier,
        color = color,
        textAlign = alineacion,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = EstiloEtiqueta.copy(fontSize = maxOf(12f, tamanoSp).sp)
    )
}

/** Franjas horizontales de césped. */
private fun DrawScope.dibujarCesped(a: Color, b: Color, franja: Float) {
    var y = 0f
    var i = 0
    while (y < size.height) {
        drawRect(
            color = if (i % 2 == 0) a else b,
            topLeft = Offset(0f, y),
            size = Size(size.width, franja)
        )
        y += franja
        i++
    }
}

/** Rectángulo de la cancha y círculo central sobre la línea de abajo ("líneas de cal"). */
private fun DrawScope.dibujarLineasDeCal(color: Color, margen: Float, grosor: Float, radio: Float) {
    drawRect(
        color = color,
        topLeft = Offset(margen, margen),
        size = Size(size.width - margen * 2, size.height - margen * 2),
        style = Stroke(width = grosor)
    )
    drawCircle(
        color = color,
        radius = radio,
        center = Offset(size.width / 2f, size.height - margen),
        style = Stroke(width = grosor)
    )
}

/** Trama de puntos del marcador LED. */
private fun DrawScope.dibujarTramaLed(color: Color, paso: Float, radio: Float) {
    var y = paso / 2f
    while (y < size.height) {
        var x = paso / 2f
        while (x < size.width) {
            drawCircle(color = color, radius = radio, center = Offset(x, y))
            x += paso
        }
        y += paso
    }
}

private fun Modifier.cespedConLineas(
    cesped: Color,
    alterno: Color,
    linea: Color,
    franja: Dp,
    margen: Dp,
    radioCirculo: Dp
): Modifier = drawBehind {
    dibujarCesped(cesped, alterno, franja.toPx())
    dibujarLineasDeCal(linea, margen.toPx(), 1.5.dp.toPx(), radioCirculo.toPx())
}

private fun Modifier.tramaLed(): Modifier = drawBehind {
    dibujarTramaLed(Color.White.copy(alpha = 0.06f), 4.dp.toPx(), 0.8.dp.toPx())
}

/** Botón redondo de 40 dp (atrás y acciones de las barras). */
@Composable
private fun BotonRedondo(
    icono: ImageVector,
    descripcion: String,
    onClick: () -> Unit,
    fondo: Color,
    contenido: Color,
    modifier: Modifier = Modifier,
    contador: Int? = null
) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(fondo)
                .clickable(role = Role.Button, onClickLabel = descripcion, onClick = onClick)
                .semantics { contentDescription = descripcion },
            contentAlignment = Alignment.Center
        ) {
            Icon(icono, contentDescription = null, tint = contenido, modifier = Modifier.size(22.dp))
        }
        if (contador != null && contador > 0) {
            InsigniaContador(contador, Modifier.align(Alignment.TopEnd).offset(x = 4.dp, y = (-4).dp))
        }
    }
}

/** Círculo naranja con un número (pendientes). */
@Composable
private fun InsigniaContador(valor: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .heightIn(min = 20.dp)
            .widthIn(min = 20.dp)
            .clip(CircleShape)
            .background(LineaDeCal.colores.naranja)
            .padding(horizontal = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (valor > 99) "99+" else valor.toString(),
            color = Blanco,
            fontFamily = FamiliaCuerpo,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            lineHeight = 14.sp
        )
    }
}

// =====================================================================
// 1 · CabeceraCancha
// =====================================================================

/**
 * Cabecera de césped con franjas horizontales y líneas de cal (blanco 85 %),
 * esquinas inferiores de 24 dp. Recibe título, subtítulo y contenido extra
 * (por ejemplo una fila de ContadorLed o el MarcadorLed).
 */
@Composable
fun CabeceraCancha(
    titulo: String,
    modifier: Modifier = Modifier,
    subtitulo: String? = null,
    etiqueta: String? = null,
    onVolver: (() -> Unit)? = null,
    descripcionVolver: String = "Volver",
    acciones: @Composable RowScope.() -> Unit = {},
    contenido: @Composable ColumnScope.() -> Unit = {}
) {
    val c = LineaDeCal.colores
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .cespedConLineas(
                cesped = c.cesped,
                alterno = c.cespedAlterno,
                linea = Blanco.copy(alpha = 0.85f),
                franja = 26.dp,
                margen = 10.dp,
                radioCirculo = 46.dp
            )
            .padding(start = 22.dp, end = 22.dp, top = 18.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (onVolver != null) {
                BotonRedondo(
                    icono = Icons.AutoMirrored.Rounded.ArrowBack,
                    descripcion = descripcionVolver,
                    onClick = onVolver,
                    fondo = Blanco.copy(alpha = 0.18f),
                    contenido = Blanco
                )
            }
            if (etiqueta != null) {
                EtiquetaMono(etiqueta, Blanco.copy(alpha = 0.9f), Modifier.weight(1f, fill = false))
            }
            Spacer(Modifier.weight(1f))
            acciones()
        }
        Text(
            text = titulo,
            color = Blanco,
            fontFamily = FamiliaTitulos,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 28.sp,
            lineHeight = 32.sp
        )
        if (subtitulo != null) {
            Text(
                text = subtitulo,
                color = Blanco.copy(alpha = 0.88f),
                fontFamily = FamiliaCuerpo,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
        Spacer(Modifier.height(6.dp))
        contenido()
    }
}

/**
 * Píldora translúcida de la cabecera ("Halcones Sub-12 ▾"). Con onClick muestra la
 * flecha y sirve de selector; sin onClick es una etiqueta fija (sección 7.4).
 */
@Composable
fun PildoraCabecera(
    texto: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(Blanco.copy(alpha = 0.18f))
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = texto,
            color = Blanco,
            fontFamily = FamiliaCuerpo,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (onClick != null) {
            Icon(Icons.Rounded.ExpandMore, contentDescription = null, tint = Blanco, modifier = Modifier.size(18.dp))
        }
    }
}

// =====================================================================
// 2 · MarcadorLed
// =====================================================================

/**
 * Marcador de estadio: fondo ledFondo con trama de puntos, goles en Doto lima con
 * resplandor, nombres de equipo en mono blanco y etiquetas en mono amarillo.
 * "PROVISIONAL" en naranja cuando el marcador tiene goles pendientes de revisión.
 * Con onClick muestra la flecha (abre "Partidos en vivo" desde el inicio).
 */
@Composable
fun MarcadorLed(
    local: String,
    rival: String,
    golesLocal: Int?,
    golesRival: Int?,
    modifier: Modifier = Modifier,
    etiquetaSuperior: String? = null,
    etiquetaInferior: String? = null,
    enVivo: Boolean = false,
    provisional: Boolean = false,
    compacto: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val c = LineaDeCal.colores
    val forma = RoundedCornerShape(16.dp)
    val tamanoGol = if (compacto) 34f else 46f
    val textoLocal = golesLocal?.toString() ?: "–"
    val textoRival = golesRival?.toString() ?: "–"
    val resplandor = if (c.esOscura) c.lima.copy(alpha = 0.45f) else Color.Black.copy(alpha = 0.25f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, forma, ambientColor = resplandor, spotColor = resplandor)
            .clip(forma)
            .background(c.ledFondo)
            .border(1.dp, Blanco.copy(alpha = 0.08f), forma)
            .tramaLed()
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .semantics(mergeDescendants = true) {
                contentDescription = buildString {
                    if (etiquetaSuperior != null) append("$etiquetaSuperior. ")
                    append("$local $textoLocal, $rival $textoRival")
                    if (provisional) append(". Marcador provisional")
                }
            }
            .padding(horizontal = 16.dp, vertical = if (compacto) 10.dp else 14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (etiquetaSuperior != null || onClick != null || enVivo) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (enVivo) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(c.rojo))
                    Spacer(Modifier.width(6.dp))
                }
                EtiquetaMono(etiquetaSuperior ?: "", c.amarillo, Modifier.weight(1f))
                if (onClick != null) {
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        tint = Blanco,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            EtiquetaMono(local, Blanco, Modifier.weight(1f), tamanoSp = 13f)
            TextoNumero(textoLocal, tamanoGol, c.lima, brillo = true)
            TextoNumero(":", tamanoGol * 0.8f, c.lima.copy(alpha = 0.55f), Modifier.padding(horizontal = 6.dp))
            TextoNumero(textoRival, tamanoGol, c.lima, brillo = true)
            EtiquetaMono(rival, Blanco, Modifier.weight(1f), tamanoSp = 13f, alineacion = TextAlign.End)
        }
        if (etiquetaInferior != null || provisional) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                EtiquetaMono(etiquetaInferior ?: "", c.amarillo, Modifier.weight(1f))
                if (provisional) {
                    EtiquetaMono("Provisional", c.naranja)
                }
            }
        }
    }
}

// =====================================================================
// 3 · ContadorLed
// =====================================================================

/** Caja LED pequeña con un número y una etiqueta mono (fila de contadores de las cabeceras). */
@Composable
fun ContadorLed(
    valor: String,
    etiqueta: String,
    modifier: Modifier = Modifier,
    color: Color = LineaDeCal.colores.lima
) {
    val c = LineaDeCal.colores
    val forma = RoundedCornerShape(12.dp)
    Column(
        modifier = modifier
            .clip(forma)
            .background(c.ledFondo)
            .border(1.dp, Blanco.copy(alpha = 0.08f), forma)
            .tramaLed()
            .semantics(mergeDescendants = true) { contentDescription = "$etiqueta: $valor" }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TextoNumero(valor, 26f, color, brillo = true)
        EtiquetaMono(etiqueta, Blanco.copy(alpha = 0.75f))
    }
}

// =====================================================================
// 4 · TarjetaTicket
// =====================================================================

enum class VarianteTicket {
    NORMAL,

    /** Entrenamiento: talón lima. */
    VERDE,

    /** Partido: talón amarillo. */
    AMARILLA,

    /** Inactivo o histórico. */
    APAGADA,

    /** Cancelado: apagado y con la hora tachada (se conserva en el historial). */
    TACHADA,

    /** Partido en curso (sección 6.8): ticket oscuro. */
    OSCURA
}

/**
 * Tarjeta tipo "entrada": talón izquierdo con hora (Doto) y fecha (mono), separado por
 * una línea discontinua con muescas circulares arriba y abajo.
 * colorMuesca debe ser el color del fondo sobre el que va la tarjeta.
 */
@Composable
fun TarjetaTicket(
    hora: String,
    fecha: String,
    modifier: Modifier = Modifier,
    variante: VarianteTicket = VarianteTicket.NORMAL,
    colorMuesca: Color = LineaDeCal.colores.fondo,
    anchoTalon: Dp = 76.dp,
    onClick: (() -> Unit)? = null,
    contenido: @Composable ColumnScope.() -> Unit
) {
    val c = LineaDeCal.colores
    val forma = RoundedCornerShape(14.dp)
    val apagada = variante == VarianteTicket.APAGADA || variante == VarianteTicket.TACHADA

    val cuerpo = if (variante == VarianteTicket.OSCURA) c.navy else c.superficie
    val talonFondo = when (variante) {
        VarianteTicket.VERDE -> c.lima
        VarianteTicket.AMARILLA -> c.amarillo
        VarianteTicket.OSCURA -> c.ledFondo
        else -> c.superficie
    }
    val talonTexto = when (variante) {
        VarianteTicket.VERDE, VarianteTicket.AMARILLA -> c.navy
        VarianteTicket.OSCURA -> c.lima
        VarianteTicket.APAGADA, VarianteTicket.TACHADA -> c.textoSecundario
        VarianteTicket.NORMAL -> c.textoPrincipal
    }
    val textoCuerpo = if (variante == VarianteTicket.OSCURA) Blanco else c.textoPrincipal
    val linea = if (variante == VarianteTicket.OSCURA) Blanco.copy(alpha = 0.3f) else c.borde
    val sombra = c.navy.copy(alpha = if (c.esOscura) 0.5f else 0.14f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .shadow(3.dp, forma, ambientColor = sombra, spotColor = sombra)
            .clip(forma)
            .background(cuerpo)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .drawWithContent {
                drawContent()
                val x = anchoTalon.toPx()
                val r = 7.dp.toPx()
                drawCircle(colorMuesca, r, Offset(x, 0f))
                drawCircle(colorMuesca, r, Offset(x, size.height))
            }
    ) {
        Column(
            modifier = Modifier
                .width(anchoTalon)
                .fillMaxHeight()
                .background(talonFondo)
                .padding(horizontal = 6.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            TextoNumero(
                texto = hora,
                tamanoSp = 20f,
                color = talonTexto,
                tachado = variante == VarianteTicket.TACHADA,
                brillo = variante == VarianteTicket.OSCURA
            )
            if (fecha.isNotBlank()) {
                EtiquetaMono(fecha, talonTexto.copy(alpha = 0.85f))
            }
        }
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp)
                .drawBehind {
                    val margen = 9.dp.toPx()
                    drawLine(
                        color = linea,
                        start = Offset(0f, margen),
                        end = Offset(0f, size.height - margen),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    )
                }
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .alpha(if (apagada) 0.6f else 1f)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            CompositionLocalProvider(LocalContentColor provides textoCuerpo) {
                contenido()
            }
        }
    }
}

// =====================================================================
// 5 · FichaTactica
// =====================================================================

enum class ColorFicha { LIMA, BLANCA, AMARILLA, VERDE, NAVY }

/**
 * Ficha redonda de pizarra táctica (44 a 58 dp) con ícono, sombra inferior "de ficha"
 * y etiqueta debajo. Puede llevar un contador naranja.
 * colorEtiqueta: blanco sobre la pizarra; textoPrincipal fuera de ella.
 */
@Composable
fun FichaTactica(
    icono: ImageVector,
    etiqueta: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: ColorFicha = ColorFicha.LIMA,
    tamano: Dp = 52.dp,
    contador: Int? = null,
    colorEtiqueta: Color = Blanco,
    mostrarEtiqueta: Boolean = true
) {
    val c = LineaDeCal.colores
    val diametro = tamano.coerceIn(44.dp, 58.dp)
    val (fondo, contenido) = when (color) {
        ColorFicha.LIMA -> c.lima to c.navy
        ColorFicha.BLANCA -> Blanco to c.navy
        ColorFicha.AMARILLA -> c.amarillo to c.navy
        ColorFicha.VERDE -> c.verde to Blanco
        ColorFicha.NAVY -> c.navy to c.lima
    }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .semantics(mergeDescendants = true) {
                if (!mostrarEtiqueta) contentDescription = etiqueta
            }
            .padding(vertical = 4.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.size(diametro).padding(bottom = 0.dp)) {
            // Sombra inferior de ficha
            Box(
                Modifier
                    .size(diametro)
                    .offset(y = 4.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.32f))
            )
            Box(
                modifier = Modifier
                    .size(diametro)
                    .clip(CircleShape)
                    .background(fondo)
                    .border(1.5.dp, Blanco.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icono, contentDescription = null, tint = contenido, modifier = Modifier.size(diametro * 0.46f))
            }
            if (contador != null && contador > 0) {
                InsigniaContador(contador, Modifier.align(Alignment.TopEnd).offset(x = 6.dp, y = (-4).dp))
            }
        }
        if (mostrarEtiqueta) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = etiqueta,
                modifier = Modifier.width(diametro + 34.dp),
                color = colorEtiqueta,
                fontFamily = FamiliaCuerpo,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                lineHeight = 15.sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// =====================================================================
// 6 · PizarraFichas
// =====================================================================

/** Datos de una ficha de la pizarra. */
data class DatoFicha(
    val icono: ImageVector,
    val etiqueta: String,
    val onClick: () -> Unit,
    val color: ColorFicha = ColorFicha.LIMA,
    val contador: Int? = null
)

/** Pizarra táctica navy con borde discontinuo interior y cuadrícula de fichas (3 o 4 columnas). */
@Composable
fun PizarraFichas(
    fichas: List<DatoFicha>,
    modifier: Modifier = Modifier,
    columnas: Int = 4
) {
    val c = LineaDeCal.colores
    val forma = RoundedCornerShape(18.dp)
    val fondo = if (c.esOscura) c.navy2 else c.navy
    val n = columnas.coerceIn(3, 4)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(forma)
            .background(fondo)
            .drawBehind {
                val inset = 6.dp.toPx()
                drawRoundRect(
                    color = Blanco.copy(alpha = 0.22f),
                    topLeft = Offset(inset, inset),
                    size = Size(size.width - inset * 2, size.height - inset * 2),
                    cornerRadius = CornerRadius(13.dp.toPx()),
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 7f), 0f)
                    )
                )
            }
            .padding(horizontal = 10.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        fichas.chunked(n).forEach { fila ->
            Row(modifier = Modifier.fillMaxWidth()) {
                fila.forEach { ficha ->
                    Box(Modifier.weight(1f), contentAlignment = Alignment.TopCenter) {
                        FichaTactica(
                            icono = ficha.icono,
                            etiqueta = ficha.etiqueta,
                            onClick = ficha.onClick,
                            color = ficha.color,
                            contador = ficha.contador
                        )
                    }
                }
                repeat(n - fila.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

// =====================================================================
// 7 · CromoJugador
// =====================================================================

enum class TamanoCromo { GRANDE, MINI }

/**
 * Cromo coleccionable: borde amarillo, interior navy con franjas verdes, número gigante
 * translúcido, foto (fotoUrl) o silueta lima con el número, nombre, posición y pie con
 * equipo y estado. `Jugador` no tiene dorsal, por eso el número es opcional.
 * insignia: contenido opcional arriba a la izquierda (por ejemplo un ChipEstado).
 */
@Composable
fun CromoJugador(
    nombre: String,
    posicion: String,
    modifier: Modifier = Modifier,
    numero: String? = null,
    equipo: String = "",
    estado: String = "",
    fotoUrl: String = "",
    tamano: TamanoCromo = TamanoCromo.GRANDE,
    insignia: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val c = LineaDeCal.colores
    val grande = tamano == TamanoCromo.GRANDE
    val formaExterior = RoundedCornerShape(if (grande) 18.dp else 14.dp)
    val formaInterior = RoundedCornerShape(if (grande) 13.dp else 10.dp)
    val iniciales = nombre.split(" ").filter { it.isNotBlank() }.take(2)
        .joinToString("") { it.first().uppercase() }.ifBlank { "?" }

    Column(
        modifier = modifier
            .clip(formaExterior)
            .background(c.amarillo)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .semantics(mergeDescendants = true) {}
            .padding(if (grande) 6.dp else 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (grande) 200.dp else 124.dp)
                .clip(formaInterior)
                .background(c.navy)
                .drawBehind {
                    val franja = (if (grande) 22.dp else 16.dp).toPx()
                    var y = 0f
                    var i = 0
                    while (y < size.height) {
                        if (i % 2 == 1) {
                            drawRect(
                                color = c.verde.copy(alpha = 0.22f),
                                topLeft = Offset(0f, y),
                                size = Size(size.width, franja)
                            )
                        }
                        y += franja
                        i++
                    }
                }
        ) {
            if (!numero.isNullOrBlank()) {
                Text(
                    text = numero,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 8.dp)
                        .offset(y = if (grande) (-18).dp else (-10).dp),
                    color = c.verde.copy(alpha = 0.45f),
                    fontFamily = FamiliaTitulos,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = if (grande) 150.sp else 88.sp,
                    lineHeight = if (grande) 150.sp else 88.sp,
                    maxLines = 1
                )
            }
            if (insignia != null) {
                Box(Modifier.align(Alignment.TopStart).padding(8.dp)) { insignia() }
            }
            if (grande) {
                Box(Modifier.align(Alignment.BottomStart).padding(start = 18.dp)) {
                    SiluetaOFoto(nombre, numero, iniciales, fotoUrl, ancho = 96.dp)
                }
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxWidth(0.55f)
                        .padding(end = 14.dp, top = 30.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = nombre,
                        color = Blanco,
                        fontFamily = FamiliaTitulos,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp,
                        lineHeight = 28.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (posicion.isNotBlank()) {
                        EtiquetaMono(
                            posicion,
                            c.navy,
                            Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(c.lima)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            } else {
                Box(Modifier.align(Alignment.CenterStart).padding(start = 12.dp)) {
                    if (fotoUrl.isNotBlank()) {
                        AvatarRemoto(fotoUrl, "Foto de $nombre", iniciales, Modifier.size(40.dp))
                    } else {
                        Box(Modifier.size(34.dp).clip(CircleShape).background(c.lima))
                    }
                }
                Text(
                    text = nombre,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 10.dp, end = 10.dp, bottom = 8.dp),
                    color = Blanco,
                    fontFamily = FamiliaTitulos,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    lineHeight = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = if (grande) 8.dp else 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val izquierda = if (grande) equipo else posicion
            val derecha = if (grande) estado else numero?.let { "#$it" } ?: ""
            EtiquetaMono(izquierda, c.navy, Modifier.weight(1f))
            if (derecha.isNotBlank()) {
                EtiquetaMono(derecha, c.navy)
            }
        }
    }
}

/** Foto del jugador o silueta lima (cabeza y torso) con el número o las iniciales. */
@Composable
private fun SiluetaOFoto(
    nombre: String,
    numero: String?,
    iniciales: String,
    fotoUrl: String,
    ancho: Dp
) {
    val c = LineaDeCal.colores
    if (fotoUrl.isNotBlank()) {
        AvatarRemoto(
            url = fotoUrl,
            descripcion = "Foto de $nombre",
            iniciales = iniciales,
            modifier = Modifier.padding(bottom = 16.dp).size(ancho)
        )
        return
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(ancho * 0.48f).clip(CircleShape).background(c.lima))
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .width(ancho)
                .height(ancho * 0.62f)
                .clip(RoundedCornerShape(topStart = ancho / 2, topEnd = ancho / 2))
                .background(c.lima),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = numero?.takeIf { it.isNotBlank() } ?: iniciales,
                color = c.navy,
                fontFamily = FamiliaTitulos,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 26.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

// =====================================================================
// 8 · ChipEstado
// =====================================================================

enum class TipoEstado { OK, PENDIENTE, ERROR, APAGADO, AMARILLO, NEUTRO }

/** Un solo chip para todos los estados (colores de la sección 3.1 y 3.2). */
@Composable
fun ChipEstado(
    texto: String,
    tipo: TipoEstado,
    modifier: Modifier = Modifier,
    conPunto: Boolean = false,
    icono: ImageVector? = null
) {
    val c = LineaDeCal.colores
    val (fondo, color) = when (tipo) {
        TipoEstado.OK -> c.okFondo to c.okTexto
        TipoEstado.PENDIENTE -> c.pendienteFondo to c.pendienteTexto
        TipoEstado.ERROR -> c.errorFondo to c.errorTexto
        TipoEstado.APAGADO -> c.apagadoFondo to c.apagadoTexto
        TipoEstado.AMARILLO -> c.amarilloFondo to c.amarilloTexto
        TipoEstado.NEUTRO -> c.neutroFondo to c.neutroTexto
    }
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(fondo)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        if (conPunto) {
            Box(Modifier.size(6.dp).clip(CircleShape).background(color))
        }
        if (icono != null) {
            Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        }
        Text(
            text = texto,
            color = color,
            fontFamily = FamiliaCuerpo,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            maxLines = 1
        )
    }
}

// =====================================================================
// 9 · BotonPrincipal
// =====================================================================

enum class VarianteBoton {
    /** Acción principal: fondo lima, texto navy. */
    LIMA,

    /** Navy con texto lima. */
    NAVY
}

/** Botón ancho (52 dp, radio 14 dp). Muestra un indicador mientras `cargando`. */
@Composable
fun BotonPrincipal(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variante: VarianteBoton = VarianteBoton.LIMA,
    icono: ImageVector? = null,
    habilitado: Boolean = true,
    cargando: Boolean = false
) {
    val c = LineaDeCal.colores
    val (fondo, contenido) = when (variante) {
        VarianteBoton.LIMA -> c.lima to c.navy
        VarianteBoton.NAVY -> c.superficieOscura to c.lima
    }
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().heightIn(min = 52.dp),
        enabled = habilitado && !cargando,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = fondo,
            contentColor = contenido,
            disabledContainerColor = fondo.copy(alpha = 0.45f),
            disabledContentColor = contenido.copy(alpha = 0.7f)
        )
    ) {
        if (cargando) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = contenido,
                strokeWidth = 2.dp
            )
            Spacer(Modifier.width(10.dp))
        } else if (icono != null) {
            Icon(icono, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = texto,
            fontFamily = FamiliaCuerpo,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

// =====================================================================
// 10 · CampoSportPro
// =====================================================================

/**
 * OutlinedTextField con etiqueta mono arriba, fondo de superficie y borde verde al enfocar.
 * esContrasena agrega el botón para mostrar u ocultar la contraseña (ícono visibility, 7.5).
 */
@Composable
fun CampoSportPro(
    valor: String,
    onValorChange: (String) -> Unit,
    etiqueta: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    esContrasena: Boolean = false,
    error: String? = null,
    ayuda: String? = null,
    habilitado: Boolean = true,
    soloLectura: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    icono: (@Composable () -> Unit)? = null
) {
    val c = LineaDeCal.colores
    var visible by rememberSaveable { mutableStateOf(false) }
    val transformacion = when {
        esContrasena && !visible -> PasswordVisualTransformation()
        esContrasena -> VisualTransformation.None
        else -> visualTransformation
    }
    val opcionesTeclado =
        if (esContrasena && keyboardOptions == KeyboardOptions.Default) {
            KeyboardOptions(keyboardType = KeyboardType.Password)
        } else {
            keyboardOptions
        }
    val iconoFinal: (@Composable () -> Unit)? = if (esContrasena) {
        @Composable {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    imageVector = if (visible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                    contentDescription = if (visible) "Ocultar contraseña" else "Mostrar contraseña"
                )
            }
        }
    } else {
        icono
    }
    val textoInferior = error ?: ayuda
    val placeholderFinal: (@Composable () -> Unit)? =
        if (placeholder != null) @Composable { Text(placeholder, fontFamily = FamiliaCuerpo) } else null
    val inferiorFinal: (@Composable () -> Unit)? =
        if (textoInferior != null) @Composable { Text(textoInferior, fontSize = 12.sp) } else null

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        EtiquetaMono(etiqueta, if (error != null) c.errorTexto else c.textoSecundario)
        OutlinedTextField(
            value = valor,
            onValueChange = onValorChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = habilitado,
            readOnly = soloLectura,
            textStyle = TextStyle(fontFamily = FamiliaCuerpo, fontSize = 16.sp, color = c.textoPrincipal),
            placeholder = placeholderFinal,
            trailingIcon = iconoFinal,
            supportingText = inferiorFinal,
            isError = error != null,
            visualTransformation = transformacion,
            keyboardOptions = opcionesTeclado,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            minLines = minLines,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = c.superficie,
                unfocusedContainerColor = c.superficie,
                disabledContainerColor = c.apagadoFondo,
                errorContainerColor = c.superficie,
                focusedBorderColor = c.verde,
                unfocusedBorderColor = c.borde,
                errorBorderColor = c.rojo,
                cursorColor = c.verde,
                focusedTextColor = c.textoPrincipal,
                unfocusedTextColor = c.textoPrincipal,
                focusedPlaceholderColor = c.textoSecundario,
                unfocusedPlaceholderColor = c.textoSecundario,
                focusedTrailingIconColor = c.textoSecundario,
                unfocusedTrailingIconColor = c.textoSecundario,
                focusedSupportingTextColor = c.textoSecundario,
                unfocusedSupportingTextColor = c.textoSecundario,
                errorSupportingTextColor = c.errorTexto
            )
        )
    }
}

// =====================================================================
// 11 · BarraSuperior
// =====================================================================

/**
 * Barra de las pantallas internas: botón atrás circular, título Bricolage centrado y
 * hasta 2 acciones (usar AccionBarra).
 */
@Composable
fun BarraSuperior(
    titulo: String,
    modifier: Modifier = Modifier,
    onVolver: (() -> Unit)? = null,
    descripcionVolver: String = "Volver",
    acciones: @Composable RowScope.() -> Unit = {}
) {
    val c = LineaDeCal.colores
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        if (onVolver != null) {
            BotonRedondo(
                icono = Icons.AutoMirrored.Rounded.ArrowBack,
                descripcion = descripcionVolver,
                onClick = onVolver,
                fondo = c.superficie,
                contenido = c.textoPrincipal,
                modifier = Modifier.align(Alignment.CenterStart)
            )
        }
        Text(
            text = titulo,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 96.dp),
            color = c.textoPrincipal,
            fontFamily = FamiliaTitulos,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            lineHeight = 24.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = acciones
        )
    }
}

/** Acción circular para BarraSuperior. destacada = fondo oscuro con ícono lima (Moderar). */
@Composable
fun AccionBarra(
    icono: ImageVector,
    descripcion: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contador: Int? = null,
    destacada: Boolean = false
) {
    val c = LineaDeCal.colores
    BotonRedondo(
        icono = icono,
        descripcion = descripcion,
        onClick = onClick,
        fondo = if (destacada) c.superficieOscura else c.superficie,
        contenido = if (destacada) c.lima else c.textoPrincipal,
        modifier = modifier,
        contador = contador
    )
}

// =====================================================================
// 12 · FondoCancha
// =====================================================================

/** Versión grande de la cabecera de césped para la bienvenida y el login. */
@Composable
fun FondoCancha(
    modifier: Modifier = Modifier,
    alturaMinima: Dp = 300.dp,
    contenido: @Composable ColumnScope.() -> Unit
) {
    val c = LineaDeCal.colores
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = alturaMinima)
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .cespedConLineas(
                cesped = c.cesped,
                alterno = c.cespedAlterno,
                linea = Blanco.copy(alpha = 0.85f),
                franja = 34.dp,
                margen = 12.dp,
                radioCirculo = 84.dp
            )
            .padding(horizontal = 26.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        content = contenido
    )
}

// =====================================================================
// Extra · FilaContadores (7.4: contadores LED de las cabeceras)
// =====================================================================

/** Fila de ContadorLed del mismo ancho. Cada par es (valor, etiqueta). */
@Composable
fun FilaContadores(contadores: List<Pair<String, String>>, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        contadores.forEach { (valor, etiqueta) ->
            ContadorLed(valor = valor, etiqueta = etiqueta, modifier = Modifier.weight(1f))
        }
    }
}

/** 7.4: filtro en chips Todos / Activos / Inactivos (se filtra en el cliente). */
enum class FiltroActivos(val texto: String) {
    TODOS("Todos"), ACTIVOS("Activos"), INACTIVOS("Inactivos");

    fun incluye(activo: Boolean): Boolean = when (this) {
        TODOS -> true
        ACTIVOS -> activo
        INACTIVOS -> !activo
    }
}

@Composable
fun FiltroChipsActivos(seleccionado: FiltroActivos, onElegir: (FiltroActivos) -> Unit) {
    FilaDeChipsSportPro {
        FiltroActivos.entries.forEach { filtro ->
            ChipSportPro(filtro.texto, filtro == seleccionado) { onElegir(filtro) }
        }
    }
}

// =====================================================================
// Extra · anchoCompleto
// =====================================================================

/**
 * Deja que un elemento (por ejemplo CabeceraCancha) ocupe todo el ancho aunque su contenedor
 * tenga un margen lateral de `margen`. Evita reestructurar pantallas que ya tienen padding.
 */
fun Modifier.anchoCompleto(margen: Dp): Modifier = layout { medible, restricciones ->
    val extra = margen.roundToPx() * 2
    if (!restricciones.hasBoundedWidth) {
        val p = medible.measure(restricciones)
        return@layout layout(p.width, p.height) { p.place(0, 0) }
    }
    val ancho = restricciones.maxWidth + extra
    val p = medible.measure(restricciones.copy(minWidth = ancho, maxWidth = ancho))
    layout(restricciones.maxWidth, p.height) { p.place(-extra / 2, 0) }
}

// =====================================================================
// Extra · EscudoIniciales (img 08: equipos de la academia)
// =====================================================================

/**
 * Escudo cuadrado con iniciales. "Halcones Sub-12" → "S12"; otros nombres → iniciales.
 * El color se elige por el texto (siempre el mismo para el mismo nombre). Inactivo: apagado.
 */
@Composable
fun EscudoIniciales(
    nombre: String,
    modifier: Modifier = Modifier,
    activo: Boolean = true,
    tamano: Dp = 44.dp
) {
    val c = LineaDeCal.colores
    val sub = Regex("(?i)sub\\s*-?\\s*(\\d{1,2})").find(nombre)?.groupValues?.get(1)
    val texto = if (sub != null) {
        "S$sub"
    } else {
        nombre.split(" ").filter { it.isNotBlank() }.take(2)
            .joinToString("") { it.first().uppercase() }.ifBlank { "?" }
    }
    val opciones = listOf(c.navy to c.lima, c.verde to Blanco, c.amarillo to c.navy, c.superficie to c.textoPrincipal)
    val (fondo, contenido) = if (activo) {
        opciones[(nombre.hashCode() and Int.MAX_VALUE) % opciones.size]
    } else {
        c.apagadoFondo to c.apagadoTexto
    }
    Box(
        modifier = modifier
            .size(tamano)
            .clip(RoundedCornerShape(12.dp))
            .background(fondo)
            .border(1.dp, c.borde, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = texto,
            color = contenido,
            fontFamily = FamiliaTitulos,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 15.sp,
            maxLines = 1
        )
    }
}

// =====================================================================
// Previews · claro y oscuro
// =====================================================================

@Preview(name = "Claro", showBackground = true, backgroundColor = 0xFFF7F7F2, widthDp = 380)
@Preview(
    name = "Oscuro",
    showBackground = true,
    backgroundColor = 0xFF0D1E2D,
    widthDp = 380,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
annotation class VistaClaroOscuro

@Composable
private fun VistaKit(contenido: @Composable ColumnScope.() -> Unit) {
    ProjectSportPro2026_2Theme {
        Column(
            modifier = Modifier
                .background(LineaDeCal.colores.fondo)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = contenido
        )
    }
}

@VistaClaroOscuro
@Composable
private fun VistaCabeceraCancha() = VistaKit {
    CabeceraCancha(
        titulo = "Entrenamientos",
        subtitulo = "Planificación semanal del equipo",
        onVolver = {},
        acciones = { PildoraCabecera("Halcones Sub-12", onClick = {}) }
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ContadorLed("3", "Esta semana", Modifier.weight(1f))
            ContadorLed("255", "Minutos", Modifier.weight(1f))
            ContadorLed("1", "Cancelado", Modifier.weight(1f))
        }
    }
}

@VistaClaroOscuro
@Composable
private fun VistaMarcadorLed() = VistaKit {
    MarcadorLed(
        local = "Halcones",
        rival = "R. Norte",
        golesLocal = 2,
        golesRival = 1,
        etiquetaSuperior = "En vivo · Sub-12",
        enVivo = true,
        onClick = {}
    )
    MarcadorLed(
        local = "Halcones",
        rival = "R. Norte",
        golesLocal = 2,
        golesRival = 1,
        etiquetaInferior = "36:18",
        provisional = true,
        compacto = true
    )
}

@VistaClaroOscuro
@Composable
private fun VistaContadorLed() = VistaKit {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ContadorLed("12", "Presentes", Modifier.weight(1f))
        ContadorLed("92%", "Asistencia", Modifier.weight(1f))
        ContadorLed("Al día", "Mensualidad", Modifier.weight(1f))
    }
}

@VistaClaroOscuro
@Composable
private fun VistaTarjetaTicket() = VistaKit {
    TarjetaTicket(hora = "16:00", fecha = "Mar 24", variante = VarianteTicket.VERDE) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Técnica de pase", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            ChipEstado("Publicado", TipoEstado.OK)
        }
        Text("90 min · 6 ejercicios", fontSize = 13.sp, color = LineaDeCal.colores.textoSecundario)
    }
    TarjetaTicket(hora = "10:00", fecha = "Sáb 28", variante = VarianteTicket.AMARILLA) {
        Text("vs Deportivo Sur", fontWeight = FontWeight.Bold)
        Text("Estadio Municipal · convocatoria 14/18", fontSize = 13.sp, color = LineaDeCal.colores.textoSecundario)
    }
    TarjetaTicket(hora = "09:00", fecha = "Sáb 28", variante = VarianteTicket.TACHADA) {
        Text("Físico y coordinación", fontWeight = FontWeight.Bold)
        Text("Se conserva en el historial", fontSize = 13.sp)
    }
    TarjetaTicket(hora = "En vivo", fecha = "", variante = VarianteTicket.OSCURA) {
        Text("vs Real Norte", fontWeight = FontWeight.Bold)
        Text("Cancha Halcones", fontSize = 13.sp)
    }
}

@VistaClaroOscuro
@Composable
private fun VistaFichaTactica() = VistaKit {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FichaTactica(Icons.Rounded.HowToReg, "Asistencia", {}, colorEtiqueta = LineaDeCal.colores.textoPrincipal)
        FichaTactica(Icons.Rounded.Groups, "Convocar", {}, color = ColorFicha.BLANCA, colorEtiqueta = LineaDeCal.colores.textoPrincipal)
        FichaTactica(Icons.Rounded.Link, "Vinculaciones", {}, color = ColorFicha.AMARILLA, contador = 3, colorEtiqueta = LineaDeCal.colores.textoPrincipal)
        FichaTactica(Icons.Rounded.Campaign, "Anuncios", {}, color = ColorFicha.VERDE, colorEtiqueta = LineaDeCal.colores.textoPrincipal)
    }
}

@VistaClaroOscuro
@Composable
private fun VistaPizarraFichas() = VistaKit {
    PizarraFichas(
        fichas = listOf(
            DatoFicha(Icons.Rounded.HowToReg, "Asistencias", {}),
            DatoFicha(Icons.Rounded.Groups, "Cuentas de jugadores", {}, ColorFicha.BLANCA, contador = 2),
            DatoFicha(Icons.Rounded.Link, "Vinculaciones", {}, ColorFicha.AMARILLA, contador = 3),
            DatoFicha(Icons.Rounded.Payments, "Mensualidades", {}, ColorFicha.VERDE),
            DatoFicha(Icons.Rounded.BarChart, "Estadísticas", {}, ColorFicha.AMARILLA),
            DatoFicha(Icons.Rounded.Forum, "Comunidad", {}, ColorFicha.BLANCA)
        ),
        columnas = 3
    )
}

@VistaClaroOscuro
@Composable
private fun VistaCromoJugador() = VistaKit {
    CromoJugador(
        nombre = "Mateo Ríos",
        posicion = "Delantero",
        numero = "9",
        equipo = "Halcones Sub-12",
        estado = "Temporada 2026"
    )
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        CromoJugador(
            nombre = "Lucas Paredes",
            posicion = "Volante",
            numero = "10",
            tamano = TamanoCromo.MINI,
            modifier = Modifier.weight(1f)
        )
        CromoJugador(
            nombre = "Joaquín Castro",
            posicion = "Volante",
            tamano = TamanoCromo.MINI,
            insignia = { ChipEstado("Inactivo", TipoEstado.APAGADO) },
            modifier = Modifier.weight(1f)
        )
    }
}

@VistaClaroOscuro
@Composable
private fun VistaChipEstado() = VistaKit {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        ChipEstado("Sincronizado", TipoEstado.OK)
        ChipEstado("Pendiente", TipoEstado.PENDIENTE)
        ChipEstado("Vencido", TipoEstado.ERROR)
    }
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        ChipEstado("Cerrada", TipoEstado.APAGADO)
        ChipEstado("Justificado", TipoEstado.AMARILLO)
        ChipEstado("Programado", TipoEstado.NEUTRO)
        ChipEstado("Abierta", TipoEstado.OK, conPunto = true)
    }
}

@VistaClaroOscuro
@Composable
private fun VistaBotonPrincipal() = VistaKit {
    BotonPrincipal("Publicar entrenamiento", {}, icono = Icons.Rounded.Check)
    BotonPrincipal("Guardar asistencia", {}, variante = VarianteBoton.NAVY, icono = Icons.Rounded.Check)
    BotonPrincipal("Guardando...", {}, cargando = true)
}

@VistaClaroOscuro
@Composable
private fun VistaCampoSportPro() = VistaKit {
    CampoSportPro(valor = "daniel@halcones.pe", onValorChange = {}, etiqueta = "Correo")
    CampoSportPro(valor = "secreto", onValorChange = {}, etiqueta = "Contraseña", esContrasena = true)
    CampoSportPro(
        valor = "",
        onValorChange = {},
        etiqueta = "Objetivo",
        placeholder = "Técnica de pase",
        error = "Escribe el objetivo de la sesión"
    )
}

@VistaClaroOscuro
@Composable
private fun VistaBarraSuperior() = VistaKit {
    BarraSuperior(titulo = "Ejercicios", onVolver = {})
    BarraSuperior(
        titulo = "Comunidad",
        onVolver = {},
        acciones = {
            AccionBarra(Icons.Rounded.Block, "Usuarios bloqueados", {})
            AccionBarra(Icons.Rounded.Shield, "Moderar", {}, contador = 2, destacada = true)
        }
    )
}

@VistaClaroOscuro
@Composable
private fun VistaFondoCancha() = VistaKit {
    FondoCancha {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(LineaDeCal.colores.lima),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.SportsSoccer, contentDescription = null, tint = LineaDeCal.colores.navy)
            }
            Text("SportPro", color = Color.White, fontFamily = FamiliaTitulos, fontWeight = FontWeight.ExtraBold, fontSize = 30.sp)
        }
        Text(
            "La operación de tu academia,\nsiempre en juego.",
            color = Color.White,
            fontFamily = FamiliaTitulos,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 28.sp,
            lineHeight = 32.sp
        )
    }
}
