package com.example.projectsportpro2026_2.datos.partido

import android.content.Context

/*
 * Rediseño "Línea de cal" · 7.2 con la decisión D-02.
 *
 * Hora en que el operador marcó el inicio del 2do tiempo. Se guarda SOLO en este
 * dispositivo (SharedPreferences, una clave por partido): no se escribe en Firestore,
 * no pasa por la cola offline de la HU-16 y no agrega ningún tipo al catálogo.
 */
class AlmacenSegundoTiempo(contexto: Context) {

    private val preferencias =
        contexto.applicationContext.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)

    fun leer(idPartido: String): Long? =
        preferencias.getLong(clave(idPartido), 0L).takeIf { it > 0L }

    fun guardar(idPartido: String, milisegundos: Long) {
        preferencias.edit().putLong(clave(idPartido), milisegundos).apply()
    }

    fun borrar(idPartido: String) {
        preferencias.edit().remove(clave(idPartido)).apply()
    }

    private fun clave(idPartido: String) = "inicio_2t_$idPartido"

    private companion object {
        const val ARCHIVO = "sportpro_segundo_tiempo"
    }
}
