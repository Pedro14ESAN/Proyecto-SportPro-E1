package com.example.projectsportpro2026_2.datos.sincronizacion

import com.google.android.gms.tasks.Task
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

/*
 * HU16 - Escritura "local primero".
 *
 * Firestore guarda cada escritura en su caché local de
 * inmediato y la envía al servidor cuando hay conexión.
 * La Task que devuelve set()/update()/commit() solo
 * termina cuando el SERVIDOR confirma, así que sin
 * conexión quedaría esperando indefinidamente.
 *
 * Esta función espera la confirmación un tiempo corto:
 *  - Si el servidor confirma → la escritura está sincronizada.
 *  - Si el servidor la rechaza dentro de la espera → se
 *    lanza la excepción para mostrar el error.
 *  - Si no responde a tiempo → la escritura queda en la cola
 *    local ("Pendiente de sync") y se enviará sola al
 *    recuperar la conexión. Devuelve false.
 *
 * alFallarDespues: si la escritura encolada es rechazada
 * más tarde (al resincronizar), se avisa para que el
 * evento pase a "Requiere revisión" y NO se pierda en
 * silencio.
 */
suspend fun confirmarOEncolar(
    tarea: Task<*>,
    esperaMaximaMs: Long = 2_500L,
    alFallarDespues: ((Exception) -> Unit)? = null
): Boolean {
    val confirmada =
        withTimeoutOrNull(esperaMaximaMs) {
            tarea.await()
            true
        }

    if (confirmada == true) {
        return true
    }

    alFallarDespues?.let { accion ->
        tarea.addOnFailureListener { e ->
            accion(e)
        }
    }

    return false
}
