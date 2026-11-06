package com.example.projectsportpro2026_2.datos.resumen

import com.example.projectsportpro2026_2.datos.modelo.evento.EquipoEvento
import com.example.projectsportpro2026_2.datos.modelo.evento.EventoPartido
import com.example.projectsportpro2026_2.datos.modelo.partido.Partido
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.RequestOptions
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

/*
 * HU26 - Llamada al proveedor de IA mediante Firebase AI Logic.
 *
 * Por qué es una "capa segura de backend":
 * - La app NO contiene ninguna API key del proveedor. Firebase AI
 *   Logic actúa de proxy en los servidores de Google y usa la
 *   configuración del proyecto (google-services.json identifica el
 *   proyecto, no da acceso a Gemini por sí mismo).
 * - Funciona en el plan gratuito Spark (Gemini Developer API).
 * - Recomendado para producción: activar App Check para que solo
 *   la app oficial pueda usar el proxy.
 *
 * La entrada se construye SOLO con eventos válidos y contexto
 * mínimo del partido, sin datos sensibles (sin teléfonos,
 * contactos, datos físicos, fechas de nacimiento ni ids internos).
 *
 * Mismo prompt y misma entrada que functions/resumen.js (alternativa
 * con Cloud Functions si algún día se activa el plan Blaze).
 */
object GeneradorResumenIA {

    // Se prueban en orden; el alias "latest" sigue al modelo Flash vigente.
    // (gemini-2.5-flash ya no está disponible para proyectos nuevos)
    val MODELOS = listOf("gemini-3.8-flash", "gemini-flash-latest")

    const val INSTRUCCIONES = """Eres el redactor de resúmenes de partidos de fútbol formativo de la app SportPro.
Escribes en español neutro, en tono sobrio y respetuoso: muchos jugadores son menores de edad.

Reglas obligatorias:
1. Usa ÚNICAMENTE los eventos del JSON que recibes. No agregues jugadas, causas, asistencias, ocasiones, estadísticas, clima, público ni declaraciones que no estén en los eventos.
2. Si un dato no está (por ejemplo, el jugador figura como "no identificado"), dilo así o no lo menciones; nunca lo completes ni lo adivines.
3. El marcador que menciones debe coincidir exactamente con "marcadorFinal".
4. Respeta los minutos tal como vienen.
5. No califiques ni critiques a jugadores individuales; no incluyas datos personales.
6. Separa hechos de interpretación con este formato exacto:

HECHOS
(uno o dos párrafos que narran en orden cronológico solo lo registrado)

LECTURA DEL PARTIDO (interpretación)
(máximo dos frases generales que se deduzcan directamente de los hechos, por ejemplo cuándo se marcaron los goles; si no hay base suficiente escribe "Sin interpretación: los eventos registrados no permiten sacar conclusiones adicionales.")

Escribe solo el resumen, sin títulos adicionales ni comentarios sobre estas reglas. Máximo 220 palabras."""

    // Entrada estructurada (JSON) con los eventos válidos
    fun construirEntrada(
        partido: Partido,
        nombreEquipo: String,
        eventos: List<EventoPartido>
    ): JSONObject {
        val validos = FuenteResumen.eventosValidos(eventos)
        val m = FuenteResumen.marcador(eventos)
        val nombreRival = partido.nombreRival.ifBlank { "Rival" }

        val lista = JSONArray()
        validos.forEachIndexed { i, e ->
            val equipo = when (e.equipoEvento) {
                EquipoEvento.PROPIO -> nombreEquipo
                EquipoEvento.RIVAL -> nombreRival
                EquipoEvento.NINGUNO -> null
            }
            val jugador = when {
                e.nombreJugador.isNotBlank() -> e.nombreJugador
                e.equipoEvento == EquipoEvento.NINGUNO -> null
                else -> "no identificado"
            }
            lista.put(
                JSONObject()
                    .put("n", i + 1)
                    .put("minuto", e.minuto)
                    .put("tipo", e.nombreTipoEvento.ifBlank { e.codigoTipoEvento })
                    .put("codigo", e.codigoTipoEvento)
                    .put("equipo", equipo ?: JSONObject.NULL)
                    .put("jugador", jugador ?: JSONObject.NULL)
                    .put(
                        "observacion",
                        e.observacion.takeIf { it.isNotBlank() }?.take(200) ?: JSONObject.NULL
                    )
                    .put("corregido", e.version > 1)
            )
        }

        return JSONObject()
            .put(
                "partido",
                JSONObject()
                    .put("equipoLocal", nombreEquipo)
                    .put("rival", nombreRival)
                    .put("fecha", partido.fecha.ifBlank { null } ?: JSONObject.NULL)
                    .put("lugar", partido.ubicacion.ifBlank { null } ?: JSONObject.NULL)
                    .put("marcadorFinal", "${m.propio}-${m.rival}")
            )
            .put("eventos", lista)
    }

    fun mensajeUsuario(entrada: JSONObject): String =
        "Eventos registrados del partido (JSON):\n${entrada.toString(2)}\n\n" +
                "Redacta el resumen siguiendo las reglas."

    data class Resultado(val texto: String, val modelo: String)

    /*
     * Llama a Gemini. Si un modelo no está disponible prueba el
     * siguiente; si todos fallan lanza la última excepción.
     */
    suspend fun generar(
        partido: Partido,
        nombreEquipo: String,
        eventos: List<EventoPartido>
    ): Resultado {
        val mensaje = mensajeUsuario(construirEntrada(partido, nombreEquipo, eventos))
        var ultimoError: Exception? = null

        for (nombreModelo in MODELOS) {
            try {
                val modelo = Firebase.ai(backend = GenerativeBackend.googleAI())
                    .generativeModel(
                        modelName = nombreModelo,
                        generationConfig = generationConfig {
                            // Baja creatividad: narrar, no inventar
                            temperature = 0.2f
                            maxOutputTokens = 2048
                        },
                        systemInstruction = content { text(INSTRUCCIONES) },
                        // Los modelos con razonamiento pueden tardar más de 30 s
                        requestOptions = RequestOptions(timeoutInMillis = 120_000L)
                    )

                val texto = modelo.generateContent(mensaje).text?.trim().orEmpty()
                if (texto.isBlank()) {
                    throw IllegalStateException("La IA devolvió una respuesta vacía")
                }
                return Resultado(texto = texto, modelo = nombreModelo)
            } catch (e: Exception) {
                // Queda en Logcat (filtro "SportProIA") para diagnosticar
                Log.w("SportProIA", "Falló $nombreModelo: ${e.javaClass.simpleName}: ${e.message}", e)
                ultimoError = e
            }
        }
        throw ultimoError ?: IllegalStateException("No hay modelo de IA disponible")
    }
}
