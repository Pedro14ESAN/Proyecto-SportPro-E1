package com.example.projectsportpro2026_2.datos.repositorio

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.util.Log
import com.example.projectsportpro2026_2.datos.remoto.ConfigSupabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import kotlin.math.max

/*
 * Imágenes compartidas del proyecto (Supabase Storage).
 * Uso: HU21 (publicaciones) y HU04 (fotos de jugadores).
 *
 * Ruta en el bucket: {carpeta}/{uid}/{idAleatorio}.jpg
 * La URL pública resultante se guarda en Firestore (imagenUrl / fotoUrl).
 */
class ErrorSubidaImagenException(mensaje: String) : IllegalStateException(mensaje)

// Compresión tomada de la rama de Pedro (feature/pedro-hu21-imagenes):
// reduce a 1280 px y recomprime en JPEG, lo que además borra los
// metadatos EXIF (ubicación GPS, modelo del celular). Importante por los menores (RC-04).
object ProcesadorImagen {

    const val LADO_MAXIMO = 1280
    const val CALIDAD_JPEG = 80
    const val TAMANO_MAXIMO_BYTES = 2 * 1024 * 1024

    suspend fun comprimir(contexto: Context, uri: Uri): ByteArray = withContext(Dispatchers.IO) {
        val fuente = ImageDecoder.createSource(contexto.contentResolver, uri)
        val bitmap = ImageDecoder.decodeBitmap(fuente) { decodificador, info, _ ->
            val ancho = info.size.width
            val alto = info.size.height
            val mayor = max(ancho, alto)
            if (mayor > LADO_MAXIMO) {
                val escala = LADO_MAXIMO.toFloat() / mayor
                decodificador.setTargetSize(
                    (ancho * escala).toInt().coerceAtLeast(1),
                    (alto * escala).toInt().coerceAtLeast(1)
                )
            }
            decodificador.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }

        val salida = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, CALIDAD_JPEG, salida)
        bitmap.recycle()
        val bytes = salida.toByteArray()

        if (bytes.size > TAMANO_MAXIMO_BYTES) {
            throw IllegalArgumentException("La imagen es demasiado grande. Elige otra.")
        }
        bytes
    }
}

class RepositorioImagenes {

    /*
     * Sube la imagen ya comprimida y devuelve su URL pública.
     * carpeta: ConfigSupabase.CARPETA_PUBLICACIONES o CARPETA_JUGADORES
     * alProgreso recibe valores de 0f a 1f.
     */
    suspend fun subirImagen(
        carpeta: String,
        uid: String,
        bytes: ByteArray,
        alProgreso: (Float) -> Unit = {}
    ): String = withContext(Dispatchers.IO) {
        if (uid.isBlank()) throw IllegalStateException("No existe una sesión activa")

        val ruta = "$carpeta/$uid/${UUID.randomUUID()}.jpg"
        val conexion = URL(
            "${ConfigSupabase.URL}/storage/v1/object/${ConfigSupabase.BUCKET}/$ruta"
        ).openConnection() as HttpURLConnection

        try {
            conexion.requestMethod = "POST"
            conexion.doOutput = true
            conexion.connectTimeout = 15_000
            conexion.readTimeout = 30_000
            conexion.setRequestProperty("apikey", ConfigSupabase.CLAVE_PUBLICA)
            conexion.setRequestProperty("Content-Type", "image/jpeg")
            conexion.setRequestProperty("x-upsert", "false")   // nunca sobrescribir
            conexion.setFixedLengthStreamingMode(bytes.size)

            // Envía en partes para poder mostrar el progreso
            conexion.outputStream.use { salida ->
                val parte = 32 * 1024
                var enviados = 0
                while (enviados < bytes.size) {
                    val n = minOf(parte, bytes.size - enviados)
                    salida.write(bytes, enviados, n)
                    enviados += n
                    alProgreso(enviados.toFloat() / bytes.size)
                }
            }

            val codigo = conexion.responseCode
            if (codigo !in 200..299) {
                val detalle = conexion.errorStream?.bufferedReader()?.readText().orEmpty()
                Log.e("SportProImagenes", "Supabase respondió $codigo: $detalle")
                throw ErrorSubidaImagenException(
                    "No se pudo subir la imagen. Puedes reintentar o publicar solo el texto."
                )
            }

            ConfigSupabase.urlPublica(ruta)
        } catch (e: ErrorSubidaImagenException) {
            throw e
        } catch (e: Exception) {
            Log.e("SportProImagenes", "Error de red al subir", e)
            throw ErrorSubidaImagenException(
                "No se pudo subir la imagen. Revisa tu conexión y vuelve a intentar."
            )
        } finally {
            conexion.disconnect()
        }
    }

    // Descarga la imagen para mostrarla (máx. 3 MB)
    suspend fun descargar(url: String): ByteArray = withContext(Dispatchers.IO) {
        val conexion = URL(url).openConnection() as HttpURLConnection
        try {
            conexion.connectTimeout = 15_000
            conexion.readTimeout = 30_000
            if (conexion.responseCode !in 200..299) {
                throw IllegalStateException("No se pudo descargar la imagen")
            }
            val bytes = conexion.inputStream.use { it.readBytes() }
            if (bytes.size > 3 * 1024 * 1024) throw IllegalStateException("Imagen demasiado grande")
            bytes
        } finally {
            conexion.disconnect()
        }
    }
}