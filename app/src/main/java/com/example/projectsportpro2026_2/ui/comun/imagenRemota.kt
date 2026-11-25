package com.example.projectsportpro2026_2.ui.comun

import com.example.projectsportpro2026_2.ui.partidos.SportProNavyFondo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioImagenes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.projectsportpro2026_2.ui.partidos.SportProNavy
import com.example.projectsportpro2026_2.ui.partidos.SportProTextoSecundario
import com.example.projectsportpro2026_2.ui.partidos.SportProVerde
import com.example.projectsportpro2026_2.ui.partidos.SportProVerdeClaro
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Tomado de la rama de Pedro (feature/pedro-hu21-imagenes).
// Caché en memoria (hasta 24 MB) para no volver a descargar al hacer scroll.
private object CacheImagenes {
    val memoria = object : LruCache<String, Bitmap>(24 * 1024 * 1024) {
        override fun sizeOf(key: String, value: Bitmap) = value.byteCount
    }
}

// Muestra una imagen guardada en Supabase Storage (sin librerías extra)
@Composable
fun ImagenRemota(
    url: String,
    descripcion: String,
    modifier: Modifier = Modifier
) {
    var imagen by remember(url) { mutableStateOf(CacheImagenes.memoria.get(url)) }
    var fallo by remember(url) { mutableStateOf(false) }

    LaunchedEffect(url) {
        if (imagen != null || url.isBlank()) return@LaunchedEffect
        try {
            val bytes = RepositorioImagenes().descargar(url)
            val bitmap = withContext(Dispatchers.Default) {
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            }
            if (bitmap != null) {
                CacheImagenes.memoria.put(url, bitmap)
                imagen = bitmap
            } else {
                fallo = true
            }
        } catch (e: Exception) {
            fallo = true
        }
    }

    val forma = RoundedCornerShape(14.dp)
    val actual = imagen
    if (actual != null) {
        Image(
            bitmap = actual.asImageBitmap(),
            contentDescription = descripcion,
            contentScale = ContentScale.Crop,
            modifier = modifier
                .fillMaxWidth()
                .heightIn(max = 320.dp)
                .clip(forma)
        )
    } else {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = 160.dp)
                .clip(forma)
                .background(SportProVerdeClaro),
            contentAlignment = Alignment.Center
        ) {
            if (fallo) {
                Text(
                    "No se pudo cargar la imagen",
                    color = SportProTextoSecundario,
                    fontSize = 12.sp
                )
            } else {
                CircularProgressIndicator(color = SportProVerde, modifier = Modifier.size(28.dp))
            }
        }
    }
}

    @Composable
    fun AvatarRemoto(
        url: String,
        descripcion: String,
        iniciales: String,
        modifier: Modifier = Modifier
    ) {
        var imagen by remember(url) {
            mutableStateOf(
                CacheImagenes.memoria.get(url)
            )
        }

        var fallo by remember(url) {
            mutableStateOf(false)
        }

        LaunchedEffect(url) {

            if (
                imagen != null ||
                url.isBlank()
            ) {
                return@LaunchedEffect
            }

            try {

                val bytes =
                    RepositorioImagenes()
                        .descargar(url)

                val bitmap =
                    withContext(
                        Dispatchers.Default
                    ) {
                        BitmapFactory.decodeByteArray(
                            bytes,
                            0,
                            bytes.size
                        )
                    }

                if (bitmap != null) {

                    CacheImagenes.memoria.put(
                        url,
                        bitmap
                    )

                    imagen = bitmap

                } else {
                    fallo = true
                }

            } catch (_: Exception) {
                fallo = true
            }
        }

        Box(
            modifier = modifier
                .clip(CircleShape)
                .background(SportProNavyFondo),
            contentAlignment =
                Alignment.Center
        ) {

            val actual = imagen

            when {

                actual != null -> {

                    Image(
                        bitmap =
                            actual.asImageBitmap(),
                        contentDescription =
                            descripcion,
                        contentScale =
                            ContentScale.Crop,
                        modifier =
                            Modifier.fillMaxSize()
                    )
                }

                url.isBlank() || fallo -> {

                    Text(
                        text = iniciales,
                        color = Color.White,
                        fontWeight =
                            FontWeight.Bold
                    )
                }

                else -> {

                    CircularProgressIndicator(
                        color = Color.White,
                        modifier =
                            Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                }
            }
        }
    }
