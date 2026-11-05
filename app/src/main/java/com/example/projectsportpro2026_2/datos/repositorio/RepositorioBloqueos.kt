package com.example.projectsportpro2026_2.datos.repositorio

import com.example.projectsportpro2026_2.datos.modelo.Bloqueo
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// Política de bloqueo de HU24: bidireccional y sin borrar contenido (RC-09)
class RepositorioBloqueos(
    bd: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val bloqueos = bd.collection("bloqueos")

    private suspend fun <T> intentar(bloque: suspend () -> T): Result<T> =
        try {
            Result.success(bloque())
        } catch (e: Exception) {
            Result.failure(e)
        }

    private fun idBloqueo(bloqueador: String, bloqueado: String) = "${bloqueador}_$bloqueado"

    // Usuarios con los que hay un bloqueo activo en CUALQUIER dirección.
    // Lo usan el feed y los comentarios para ocultar contenido. No devuelve Result:
    // si falla, el error lo maneja el repositorio que lo llamó.
    suspend fun usuariosBloqueadosCon(uid: String): Set<String> {
        val queBloquee = bloqueos.whereEqualTo("idBloqueador", uid).get().await()
            .toObjects(Bloqueo::class.java)
            .filter { it.activo }
            .map { it.idBloqueado }

        val queMeBloquearon = bloqueos.whereEqualTo("idBloqueado", uid).get().await()
            .toObjects(Bloqueo::class.java)
            .filter { it.activo }
            .map { it.idBloqueador }

        return (queBloquee + queMeBloquearon).toSet()
    }

    // Para la lista "Usuarios bloqueados" (solo los que YO bloqueé)
    suspend fun listarBloqueados(uid: String): Result<List<Bloqueo>> = intentar {
        bloqueos.whereEqualTo("idBloqueador", uid).get().await()
            .toObjects(Bloqueo::class.java)
            .filter { it.activo }
            .sortedByDescending { it.fecha }
    }

    suspend fun bloquear(uid: String, idOtro: String, nombreOtro: String): Result<Unit> = intentar {
        if (uid == idOtro) throw IllegalArgumentException("No puedes bloquearte a ti misma/o")

        val ahora = System.currentTimeMillis()
        bloqueos.document(idBloqueo(uid, idOtro)).set(
            Bloqueo(
                idBloqueador = uid,
                idBloqueado = idOtro,
                nombreBloqueado = nombreOtro,
                activo = true,
                fecha = ahora,
                fechaActualizacion = ahora
            )
        ).await()
    }

    // Desbloquear no borra el registro: lo marca inactivo
    suspend fun desbloquear(uid: String, idOtro: String): Result<Unit> = intentar {
        bloqueos.document(idBloqueo(uid, idOtro)).update(
            mapOf(
                "activo" to false,
                "fechaActualizacion" to System.currentTimeMillis()
            )
        ).await()
    }
}