package com.example.projectsportpro2026_2.datos.sincronizacion

import android.content.Context
import java.util.UUID

/*
 * HU16 - Identificador anónimo del dispositivo.
 *
 * Se genera una sola vez por instalación y se guarda en
 * SharedPreferences. Se copia en cada evento registrado
 * para saber desde qué dispositivo se capturó y así
 * detectar registros duplicados hechos por dos
 * operadores distintos mientras alguno estaba sin conexión.
 *
 * No contiene datos personales.
 */
object IdentificadorDispositivo {

    private const val PREFERENCIAS = "sportpro_sincronizacion"
    private const val CLAVE_ID = "id_dispositivo"

    @Volatile
    var id: String = ""
        private set

    fun inicializar(contexto: Context) {
        if (id.isNotBlank()) {
            return
        }

        val preferencias =
            contexto.applicationContext.getSharedPreferences(
                PREFERENCIAS,
                Context.MODE_PRIVATE
            )

        val guardado =
            preferencias.getString(CLAVE_ID, null)

        id = if (guardado.isNullOrBlank()) {
            val nuevo = UUID.randomUUID().toString()

            preferencias
                .edit()
                .putString(CLAVE_ID, nuevo)
                .apply()

            nuevo
        } else {
            guardado
        }
    }
}
