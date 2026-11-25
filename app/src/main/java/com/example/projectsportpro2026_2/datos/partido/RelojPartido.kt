package com.example.projectsportpro2026_2.datos.partido

import com.example.projectsportpro2026_2.datos.modelo.evento.EstadoEventoPartido
import com.example.projectsportpro2026_2.datos.modelo.evento.EventoPartido
import java.util.Locale

/*
 * Rediseño "Línea de cal" · 7.2 (HU-14): minuto sugerido y reloj del partido.
 *
 * Lógica pura, sin red: se calcula en el celular con la fechaCaptura del INICIO que ya
 * está en la caché y, si el operador lo marcó, con la hora del inicio del 2T guardada
 * solo en el dispositivo (decisión D-02: no existe el tipo INICIO_2T en el catálogo).
 *
 *  - 1er tiempo: minuto = ((ahora - INICIO) / 60000) + 1
 *  - 2do tiempo: minuto = MINUTOS_PRIMER_TIEMPO + ((ahora - marca 2T) / 60000) + 1
 *  - Sin INICIO: no hay sugerencia (el campo queda vacío, como antes).
 *
 * El minuto es solo una sugerencia: el operador lo confirma o lo corrige (HU-14).
 */
object RelojPartido {

    /** Duración nominal del 1er tiempo para continuar el reloj en el 2T (el operador puede corregir el minuto). */
    const val MINUTOS_PRIMER_TIEMPO = 45

    private const val MS_POR_MINUTO = 60_000L

    /** fechaCaptura del INICIO válido (no anulado) más antiguo; null si no hay. */
    fun capturaInicio(eventos: List<EventoPartido>): Long? =
        eventos
            .filter {
                it.codigoTipoEvento == "INICIO" &&
                        it.estado == EstadoEventoPartido.ACTIVO &&
                        it.fechaCaptura > 0L
            }
            .minOfOrNull { it.fechaCaptura }

    /** La marca del 2T solo vale si es posterior al INICIO. */
    private fun marcaValida(inicio: Long, inicio2T: Long?): Long? =
        inicio2T?.takeIf { it > 0L && it >= inicio }

    /** Minuto sugerido para un evento nuevo; null si no hay INICIO. */
    fun minutoSugerido(
        inicio: Long?,
        inicio2T: Long?,
        ahora: Long,
        minutosPrimerTiempo: Int = MINUTOS_PRIMER_TIEMPO
    ): Int? {
        if (inicio == null || inicio <= 0L) return null
        val segundo = marcaValida(inicio, inicio2T)
        return if (segundo != null) {
            minutosPrimerTiempo + (maxOf(0L, ahora - segundo) / MS_POR_MINUTO).toInt() + 1
        } else {
            (maxOf(0L, ahora - inicio) / MS_POR_MINUTO).toInt() + 1
        }
    }

    /** Segundos de juego para el reloj del LED; null si no hay INICIO. */
    fun segundosDeJuego(
        inicio: Long?,
        inicio2T: Long?,
        ahora: Long,
        minutosPrimerTiempo: Int = MINUTOS_PRIMER_TIEMPO
    ): Long? {
        if (inicio == null || inicio <= 0L) return null
        val segundo = marcaValida(inicio, inicio2T)
        return if (segundo != null) {
            minutosPrimerTiempo * 60L + maxOf(0L, ahora - segundo) / 1000L
        } else {
            maxOf(0L, ahora - inicio) / 1000L
        }
    }

    /** "mm:ss" (los minutos pueden pasar de 99). */
    fun textoReloj(segundos: Long): String =
        String.format(Locale.ROOT, "%02d:%02d", segundos / 60, segundos % 60)
}
