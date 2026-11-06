package com.example.projectsportpro2026_2.datos.repositorio

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.UUID
import kotlin.math.max

/*
 * HU21 - Imágenes de publicaciones en Cloud Storage.
 *
 * Ruta: publicaciones/{uidAutor}/{idAleatorio}.jpg
 * - Solo el autor escribe en su carpeta (storage.rules).
 * - La imagen se reduce a 1280 px y se recomprime en JPEG: pesa
 *   poco y, al volver a codificarla, se pierden los metadatos EXIF
 *   (ubicación GPS, modelo del celular...). Importante porque hay
 *   menores de edad en la comunidad (RC-04).
 */
class ErrorSubidaImagenException(mensaje: String) : IllegalStateException(mensaje)

object ProcesadorImagen {

    const val LADO_MAXIMO = 1280
    const val CALIDAD_JPEG = 80
    const val TAMANO_MAXIMO_BYTES = 2 * 1024 * 1024

    // Lee la imagen elegida, la reduce y la devuelve como JPEG sin metadatos
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
            // Bitmap en software para poder comprimirlo
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

class RepositorioImagenes(
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {

    /*
     * Sube la imagen y devuelve su URL de descarga.
     * alProgreso recibe valores de 0f a 1f.
     */
    suspend fun subirImagenPublicacion(
        uid: String,
        bytes: ByteArray,
        alProgreso: (Float) -> Unit = {}
    ): String {
        if (uid.isBlank()) throw IllegalStateException("No existe una sesión activa")

        val referencia = storage.reference
            .child("publicaciones")
            .child(uid)
            .child("${UUID.randomUUID()}.jpg")

        val metadatos = StorageMetadata.Builder()
            .setContentType("image/jpeg")
            .build()

        return try {
            val tarea = referencia.putBytes(bytes, metadatos)
            tarea.addOnProgressListener { foto ->
                if (foto.totalByteCount > 0) {
                    alProgreso(foto.bytesTransferred.toFloat() / foto.totalByteCount)
                }
            }
            tarea.await()
            referencia.downloadUrl.await().toString()
        } catch (e: Exception) {
            throw ErrorSubidaImagenException(
                "No se pudo subir la imagen. Puedes reintentar o publicar solo el texto."
            )
        }
    }

    // Descarga para mostrar en el feed (máx. 3 MB)
    suspend fun descargar(url: String): ByteArray =
        storage.getReferenceFromUrl(url).getBytes(3L * 1024 * 1024).await()
}
