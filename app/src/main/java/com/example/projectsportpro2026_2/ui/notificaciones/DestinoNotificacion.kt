package com.example.projectsportpro2026_2.ui.notificaciones

import android.content.Intent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// HU20: guarda el aviso que se tocó en la barra del celular hasta que la app
// esté lista (sesión cargada) para abrirlo.
object DestinoNotificacion {

    // Nombre de la "etiqueta" que viaja dentro de la notificación
    const val EXTRA_ID_NOTIFICACION = "sportpro_id_notificacion"

    private val _pendiente = MutableStateFlow<String?>(null)
    val pendiente: StateFlow<String?> = _pendiente.asStateFlow()

    // Lo llama MainActivity al abrirse desde una notificación
    fun desdeIntent(intent: Intent?) {
        val id = intent?.getStringExtra(EXTRA_ID_NOTIFICACION)
        if (!id.isNullOrBlank()) {
            _pendiente.value = id
            intent.removeExtra(EXTRA_ID_NOTIFICACION)   // para no reabrirlo al girar la pantalla
        }
    }

    // Lo llama la navegación cuando ya usó el destino
    fun consumir(): String? {
        val id = _pendiente.value
        _pendiente.value = null
        return id
    }
}
