package com.example.projectsportpro2026_2.ui.resumen

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
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
 *
 * Para que suene fluido:
 * - se pide el motor "Servicios de voz de Google" (en celulares
 *   Samsung el motor por defecto suele ser el de Samsung, que
 *   pronuncia peor); si no está instalado se usa el del sistema;
 * - se elige la mejor voz en español instalada (calidad alta,
 *   preferencia por español latino);
 * - el texto se adapta para oírse (TextoParaVoz) y se habla por
 *   oraciones, con pausas breves entre oraciones y párrafos.
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
    private val motor: TextToSpeech = TextToSpeech(
        contexto.applicationContext,
        { estado -> if (estado == TextToSpeech.SUCCESS) configurarIdioma() },
        MOTOR_GOOGLE
    )

    // Voz elegida (para mostrarla o depurar)
    var nombreVoz by mutableStateOf("")
        private set

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
        mejorVozEspanol()?.let { voz ->
            motor.voice = voz
            nombreVoz = voz.name
        }
        motor.setSpeechRate(1.0f)
        motor.setPitch(1.0f)
        motor.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(id: String?) {}

            override fun onDone(id: String?) = terminar(id)

            @Deprecated("Requerido por versiones antiguas de la API")
            override fun onError(id: String?) = terminar(id)

            override fun onError(id: String?, codigo: Int) = terminar(id)
        })
        disponible = true
    }

    /*
     * Mejor voz en español ya instalada: primero la calidad, luego
     * que no necesite internet y por último el acento (latino antes
     * que España).
     */
    private fun mejorVozEspanol(): Voice? {
        val voces = try {
            motor.voices.orEmpty()
        } catch (e: Exception) {
            emptySet()
        }
        val paises = listOf("PE", "US", "MX", "CO", "AR", "CL", "ES")
        return voces
            .filter { it.locale.language == "es" }
            .filter { TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED !in it.features.orEmpty() }
            .maxWithOrNull(
                compareBy<Voice> { it.quality }
                    .thenBy { !it.isNetworkConnectionRequired }
                    .thenBy { voz ->
                        val i = paises.indexOf(voz.locale.country)
                        if (i < 0) 0 else paises.size - i
                    }
            )
    }

    // Terminó el último párrafo (o falló) de la lectura vigente
    private fun terminar(id: String?) {
        if (id != null && id == ultimoId) hablando = false
    }

    /*
     * Lee la narración oración por oración, con una pausa corta
     * entre oraciones y una más larga entre párrafos.
     */
    fun leer(texto: String) {
        if (!disponible) return
        motor.stop()
        val limite = TextToSpeech.getMaxSpeechInputLength() - 1
        val parrafos = TextoParaVoz.preparar(texto)
            .map { oraciones -> oraciones.flatMap { it.chunked(limite) } }
        val total = parrafos.sumOf { it.size }
        if (total == 0) return

        sesion++
        ultimoId = "narracion_${sesion}_${total - 1}"
        hablando = true
        var n = 0
        parrafos.forEachIndexed { p, oraciones ->
            oraciones.forEachIndexed { o, oracion ->
                motor.speak(oracion, TextToSpeech.QUEUE_ADD, null, "narracion_${sesion}_$n")
                n++
                val ultima = p == parrafos.lastIndex && o == oraciones.lastIndex
                if (!ultima) {
                    val pausa = if (o == oraciones.lastIndex) PAUSA_PARRAFO_MS else PAUSA_ORACION_MS
                    motor.playSilentUtterance(pausa, TextToSpeech.QUEUE_ADD, "pausa_${sesion}_$n")
                }
            }
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

    private companion object {
        const val MOTOR_GOOGLE = "com.google.android.tts"
        const val PAUSA_ORACION_MS = 250L
        const val PAUSA_PARRAFO_MS = 550L
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
