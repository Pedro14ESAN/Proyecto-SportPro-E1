package com.example.projectsportpro2026_2.datos.modelo.sincronizacion

import com.example.projectsportpro2026_2.datos.modelo.evento.EventoPartido

/*
 * HU16 - Estado de sincronización de un evento.
 *
 * SINCRONIZADO: el servidor ya confirmó el evento y lo ven
 *               todos los dispositivos.
 * PENDIENTE:    está guardado en este dispositivo y se
 *               enviará al recuperar la conexión.
 * CONFLICTO:    al sincronizar se detectó una situación que
 *               el operador debe revisar (duplicado o
 *               registrado después del FIN).
 */
enum class EstadoSincronizacionEvento {
    SINCRONIZADO,
    PENDIENTE,
    CONFLICTO
}

data class EventoSincronizado(
    val evento: EventoPartido,
    val estado: EstadoSincronizacionEvento =
        EstadoSincronizacionEvento.SINCRONIZADO,
    val motivoConflicto: String = ""
)

/*
 * Lo que entrega el listener de Firestore en cada cambio.
 *
 * pendientePorId: ids de eventos que todavía tienen
 *                 escrituras locales sin confirmar.
 * desdeCache:     true si los datos aún no fueron
 *                 confirmados con el servidor.
 */
data class InstantaneaEventos(
    val eventos: List<EventoPartido> = emptyList(),
    val pendientePorId: Set<String> = emptySet(),
    val desdeCache: Boolean = false,
    val escriturasPendientes: Boolean = false
)

/*
 * Estado general mostrado en el banner superior.
 */
enum class EstadoConexionPartido {
    SINCRONIZADO,
    SINCRONIZANDO,
    SIN_CONEXION
}
