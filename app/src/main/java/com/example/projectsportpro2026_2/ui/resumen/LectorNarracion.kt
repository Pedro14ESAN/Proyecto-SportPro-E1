package com.example.projectsportpro2026_2.ui.resumen

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

/*
 * Escuchar la narración aprobada en audio.
 *
 * Usa el TextToSpeech que ya trae Android (motor de voz de Google
 * del celular): es gratis, no usa Firebase ni APIs de pago y
 * funciona sin conexión si la voz en español está descargada.
 */
class LectorNarracion(contexto: Context) {

    // El motor de voz está listo para hablar en español
    var disponible by mutableStateOf(false)
        private set

    // Se está reproduciendo la narración
    var hablando by mutableStateOf(false)
        private set

    // El motor existe pero no tiene voz en español instalada
    var faltaVozEspanol by mutableStateOf(false)
        private set

    // Cada lectura usa ids propios para ignorar avisos de una lectura anterior
    private var sesion = 0
    @Volatile private var ultimoId = ""
    private val motor: TextToSpeech = TextToSpeech(contexto.applicationContext) { estado ->
        if (estado == TextToSpeech.SUCCESS) configurarIdioma()
    }

    private fun configurarIdioma() {
        // Español de Perú; si no está, cualquier español
        val idiomas = listOf(Locale.forLanguageTag("es-PE"), Locale.forLanguageTag("es-US"),
            Locale.forLanguageTag("es-ES"), Locale.forLanguageTag("es"))
        val elegido = idiomas.firstOrNull { idioma ->
            motor.isLanguageAvailable(idioma) >= TextToSpeech.LANG_AVAILABLE
        }
        if (elegido == null) {
            faltaVozEspanol = true
            return
        }
        motor.setLanguage(elegido)
        motor.setSpeechRate(0.95f)
        motor.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(id: String?) {}

            override fun onDone(id: String?) = terminar(id)

            @Deprecated("Requerido por versiones antiguas de la API")
            override fun onError(id: String?) = terminar(id)

            override fun onError(id: String?, codigo: Int) = terminar(id)
        })
        disponible = true
    }

    // Terminó el último párrafo (o falló) de la lectura vigente
    private fun terminar(id: String?) {
        if (id != null && id == ultimoId) hablando = false
    }

    /*
     * Lee el texto por párrafos (el motor tiene un límite de
     * caracteres por llamada). Las viñetas "•" se omiten.
     */
    fun leer(texto: String) {
        if (!disponible) return
        motor.stop()
        val partes = texto
            .split("\n")
            .map { it.replace("•", "").trim() }
            .filter { it.isNotBlank() }
            .flatMap { it.chunked(TextToSpeech.getMaxSpeechInputLength() - 1) }
        if (partes.isEmpty()) return
        sesion++
        ultimoId = "narracion_${sesion}_${partes.size - 1}"
        hablando = true
        partes.forEachIndexed { i, parte ->
            motor.speak(parte, TextToSpeech.QUEUE_ADD, null, "narracion_${sesion}_$i")
        }
    }

    fun detener() {
        ultimoId = ""
        motor.stop()
        hablando = false
    }

    fun liberar() {
        motor.stop()
        motor.shutdown()
    }
}

// Crea el lector y lo apaga al salir de la pantalla
@Composable
fun rememberLectorNarracion(): LectorNarracion {
    val contexto = LocalContext.current
    val lector = remember { LectorNarracion(contexto) }
    DisposableEffect(lector) {
        onDispose { lector.liberar() }
    }
    return lector
}
