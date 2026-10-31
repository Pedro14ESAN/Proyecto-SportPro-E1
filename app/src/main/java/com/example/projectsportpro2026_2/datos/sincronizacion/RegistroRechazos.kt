package com.example.projectsportpro2026_2.datos.sincronizacion

import android.content.Context
import android.content.SharedPreferences
import com.example.projectsportpro2026_2.datos.modelo.evento.EquipoEvento
import com.example.projectsportpro2026_2.datos.modelo.evento.EventoPartido
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import org.json.JSONObject

/*
 * HU16 - Eventos que el servidor rechazó al resincronizar.
 *
 * Cuando Firestore rechaza una escritura que estaba en la
 * cola offline, el documento desaparece de la caché local.
 * Para que el evento NO se descarte silenciosamente, se
 * guarda aquí (persistido en el dispositivo) y la pantalla
 * lo muestra como "Requiere revisión" con la opción de
 * reintentar con el MISMO id (idempotente) o descartarlo
 * de forma explícita.
 */
data class EventoRechazado(
    val evento: EventoPartido,
    val motivo: String,
    val fecha: Long
)

object RegistroRechazos {

    private const val PREFERENCIAS = "sportpro_rechazos"
    private const val CLAVE = "eventos"

    private var preferencias: SharedPreferences? = null

    private val _rechazados =
        MutableStateFlow<List<EventoRechazado>>(emptyList())

    val rechazados: StateFlow<List<EventoRechazado>> =
        _rechazados.asStateFlow()

    fun inicializar(contexto: Context) {
        if (preferencias != null) {
            return
        }

        val prefs =
            contexto.applicationContext.getSharedPreferences(
                PREFERENCIAS,
                Context.MODE_PRIVATE
            )

        preferencias = prefs

        _rechazados.value =
            leer(prefs.getString(CLAVE, null))
    }

    fun agregar(evento: EventoPartido, motivo: String) {
        _rechazados.update { actual ->
            actual.filterNot { it.evento.id == evento.id } +
                    EventoRechazado(
                        evento = evento,
                        motivo = motivo,
                        fecha = System.currentTimeMillis()
                    )
        }
        guardar()
    }

    fun quitar(idEvento: String) {
        _rechazados.update { actual ->
            actual.filterNot { it.evento.id == idEvento }
        }
        guardar()
    }

    fun delPartido(
        lista: List<EventoRechazado>,
        idPartido: String
    ): List<EventoRechazado> =
        lista.filter { it.evento.idPartido == idPartido }

    private fun guardar() {
        val arreglo = JSONArray()

        _rechazados.value.forEach { rechazado ->
            val e = rechazado.evento
            arreglo.put(
                JSONObject()
                    .put("id", e.id)
                    .put("idPartido", e.idPartido)
                    .put("idAcademia", e.idAcademia)
                    .put("idTipoEvento", e.idTipoEvento)
                    .put("codigoTipoEvento", e.codigoTipoEvento)
                    .put("nombreTipoEvento", e.nombreTipoEvento)
                    .put("minuto", e.minuto)
                    .put("equipoEvento", e.equipoEvento.name)
                    .put("idJugador", e.idJugador)
                    .put("nombreJugador", e.nombreJugador)
                    .put("observacion", e.observacion)
                    .put("afectaMarcador", e.afectaMarcador)
                    .put("afectaEstadoPartido", e.afectaEstadoPartido)
                    .put("creadoPor", e.creadoPor)
                    .put("fechaCaptura", e.fechaCaptura)
                    .put("idDispositivo", e.idDispositivo)
                    .put("motivo", rechazado.motivo)
                    .put("fecha", rechazado.fecha)
            )
        }

        preferencias
            ?.edit()
            ?.putString(CLAVE, arreglo.toString())
            ?.apply()
    }

    private fun leer(texto: String?): List<EventoRechazado> {
        if (texto.isNullOrBlank()) {
            return emptyList()
        }

        return try {
            val arreglo = JSONArray(texto)

            (0 until arreglo.length()).map { i ->
                val o = arreglo.getJSONObject(i)

                EventoRechazado(
                    evento = EventoPartido(
                        id = o.optString("id"),
                        idPartido = o.optString("idPartido"),
                        idAcademia = o.optString("idAcademia"),
                        idTipoEvento = o.optString("idTipoEvento"),
                        codigoTipoEvento = o.optString("codigoTipoEvento"),
                        nombreTipoEvento = o.optString("nombreTipoEvento"),
                        minuto = o.optInt("minuto"),
                        equipoEvento = runCatching {
                            EquipoEvento.valueOf(o.optString("equipoEvento"))
                        }.getOrDefault(EquipoEvento.NINGUNO),
                        idJugador = o.optString("idJugador"),
                        nombreJugador = o.optString("nombreJugador"),
                        observacion = o.optString("observacion"),
                        afectaMarcador = o.optBoolean("afectaMarcador"),
                        afectaEstadoPartido = o.optBoolean("afectaEstadoPartido"),
                        creadoPor = o.optString("creadoPor"),
                        fechaCaptura = o.optLong("fechaCaptura"),
                        idDispositivo = o.optString("idDispositivo")
                    ),
                    motivo = o.optString("motivo"),
                    fecha = o.optLong("fecha")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
