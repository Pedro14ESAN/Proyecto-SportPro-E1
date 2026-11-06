package com.example.projectsportpro2026_2.ui.comun

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
import com.example.projectsportpro2026_2.ui.partidos.SportProTextoSecundario
import com.example.projectsportpro2026_2.ui.partidos.SportProVerde
import com.example.projectsportpro2026_2.ui.partidos.SportProVerdeClaro
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// HU21: caché en memoria para no volver a descargar al desplazar el feed
private object CacheImagenes {
    val memoria = object : LruCache<String, Bitmap>(24 * 1024 * 1024) {
        override fun sizeOf(key: String, value: Bitmap) = value.byteCount
    }
}

// Muestra una imagen guardada en Cloud Storage (sin librerías extra)
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
                Text("No se pudo cargar la imagen", color = SportProTextoSecundario, fontSize = 12.sp)
            } else {
                CircularProgressIndicator(color = SportProVerde, modifier = Modifier.size(28.dp))
            }
        }
    }
}
