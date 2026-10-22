package com.example.projectsportpro2026_2.datos.repositorio

import com.example.projectsportpro2026_2.datos.modelo.Academia
import com.example.projectsportpro2026_2.datos.modelo.DatosPrivadosJugador
import com.example.projectsportpro2026_2.datos.modelo.EstadoVinculacion
import com.example.projectsportpro2026_2.datos.modelo.Jugador
import com.example.projectsportpro2026_2.datos.modelo.PerfilUsuario
import com.example.projectsportpro2026_2.datos.modelo.Rol
import com.example.projectsportpro2026_2.datos.modelo.Vinculacion
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// Lo que ve el apoderado en una vinculación activa
data class DatosSensiblesVinculo(
    val fechaNacimiento: String = "",
    val posicion: String = "",
    val telefono: String = ""
)

class RepositorioVinculaciones(
    private val bd: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val vinculaciones = bd.collection("vinculaciones")
    private val jugadores = bd.collection("jugadores")
    private val usuarios = bd.collection("usuarios")
    private val academias = bd.collection("academias")

    private suspend fun <T> intentar(bloque: suspend () -> T): Result<T> =
        try {
            Result.success(bloque())
        } catch (e: Exception) {
            Result.failure(e)
        }

    private suspend fun perfil(uid: String): PerfilUsuario =
        usuarios.document(uid).get().await().toObject(PerfilUsuario::class.java)
            ?: throw IllegalStateException("Tu cuenta no tiene perfil")

    private suspend fun obtener(id: String): Vinculacion =
        vinculaciones.document(id).get().await().toObject(Vinculacion::class.java)
            ?: throw IllegalStateException("La vinculación no existe")

    private suspend fun verificarAdministrador(idAcademia: String, uid: String) {
        val academia = academias.document(idAcademia).get().await()
            .toObject(Academia::class.java)
        if (academia == null || uid !in academia.idsAutorizados) {
            throw IllegalStateException("No tienes permiso para gestionar esta academia")
        }
    }

    // ---------- Apoderado ----------

    suspend fun solicitar(documentoJugador: String, uid: String): Result<Vinculacion> = intentar {
        if (documentoJugador.isBlank()) {
            throw IllegalArgumentException("Escribe el documento de identidad del jugador")
        }
        val apoderado = perfil(uid)
        if (apoderado.rol != Rol.PAD) {
            throw IllegalStateException("Solo un apoderado puede solicitar una vinculación")
        }

        val coincidencias = jugadores.whereEqualTo("documento", documentoJugador.trim())
            .get().await().toObjects(Jugador::class.java).filter { it.activo }
        if (coincidencias.isEmpty()) {
            throw IllegalStateException("No se encontró un jugador activo con ese documento")
        }
        if (coincidencias.size > 1) {
            throw IllegalStateException("Hay más de un jugador con ese documento. Contacta a la academia")
        }
        val jugador = coincidencias.first()

        val propias = vinculaciones.whereEqualTo("idApoderado", uid)
            .get().await().toObjects(Vinculacion::class.java)
        val duplicada = propias.any {
            it.idJugador == jugador.id && it.estado != EstadoVinculacion.INACTIVA
        }
        if (duplicada) {
            throw IllegalStateException("Ya existe una solicitud pendiente o activa para este jugador")
        }

        val ahora = System.currentTimeMillis()
        val documento = vinculaciones.document()
        val vinculacion = Vinculacion(
            id = documento.id,
            idApoderado = uid,
            nombreApoderado = apoderado.nombre,
            idJugador = jugador.id,
            nombreJugador = jugador.nombreCompleto(),
            idAcademia = jugador.idAcademia,
            estado = EstadoVinculacion.PENDIENTE,
            fechaSolicitud = ahora,
            actualizadoPor = uid,
            fechaActualizacion = ahora
        )
        documento.set(vinculacion).await()
        vinculacion
    }

    suspend fun listarMias(uid: String): Result<List<Vinculacion>> = intentar {
        vinculaciones.whereEqualTo("idApoderado", uid)
            .get().await().toObjects(Vinculacion::class.java)
            .sortedByDescending { it.fechaSolicitud }
    }

    // Solo se entregan si la vinculación es del apoderado y está ACTIVA en este momento
    suspend fun datosSensibles(
        idVinculacion: String, uid: String
    ): Result<DatosSensiblesVinculo> = intentar {
        val vinculacion = obtener(idVinculacion)
        if (vinculacion.idApoderado != uid) {
            throw IllegalStateException("No tienes acceso a esta vinculación")
        }
        if (vinculacion.estado != EstadoVinculacion.ACTIVA) {
            throw IllegalStateException("La vinculación no está activa")
        }
        val documento = jugadores.document(vinculacion.idJugador)
        val jugador = documento.get().await().toObject(Jugador::class.java)
            ?: throw IllegalStateException("El jugador no existe")
        val privados = documento.collection("privado").document("datos").get().await()
            .toObject(DatosPrivadosJugador::class.java) ?: DatosPrivadosJugador()
        DatosSensiblesVinculo(
            fechaNacimiento = jugador.fechaNacimiento,
            posicion = jugador.posicion,
            telefono = privados.telefono
        )
    }

    suspend fun solicitarDesvinculacion(idVinculacion: String, uid: String): Result<Unit> = intentar {
        val vinculacion = obtener(idVinculacion)
        if (vinculacion.idApoderado != uid) {
            throw IllegalStateException("No tienes acceso a esta vinculación")
        }
        if (vinculacion.estado != EstadoVinculacion.ACTIVA) {
            throw IllegalStateException("Solo se puede desvincular una relación activa")
        }
        vinculaciones.document(idVinculacion).update(
            mapOf(
                "desvinculacionSolicitada" to true,
                "actualizadoPor" to uid,
                "fechaActualizacion" to System.currentTimeMillis()
            )
        ).await()
    }

    // ---------- Administrador ----------

    // Todas las vinculaciones de las academias que el usuario administra
    suspend fun listarParaAdministrador(uid: String): Result<List<Vinculacion>> = intentar {
        val ids = academias.whereArrayContains("idsAutorizados", uid)
            .get().await().documents.map { it.id }
        if (ids.isEmpty()) {
            emptyList()
        } else {
            ids.chunked(10).flatMap { grupo ->
                vinculaciones.whereIn("idAcademia", grupo)
                    .get().await().toObjects(Vinculacion::class.java)
            }.sortedByDescending { it.fechaSolicitud }
        }
    }

    suspend fun aprobar(idVinculacion: String, uid: String): Result<Unit> = intentar {
        val vinculacion = obtener(idVinculacion)
        verificarAdministrador(vinculacion.idAcademia, uid)
        if (vinculacion.estado != EstadoVinculacion.PENDIENTE) {
            throw IllegalStateException("Solo se pueden autorizar solicitudes pendientes")
        }
        val aprobador = perfil(uid)
        val ahora = System.currentTimeMillis()
        val lote = bd.batch()
        lote.update(
            vinculaciones.document(idVinculacion),
            mapOf(
                "estado" to EstadoVinculacion.ACTIVA.name,
                "idAprobador" to uid,
                "nombreAprobador" to aprobador.nombre,
                "fechaAprobacion" to ahora,
                "actualizadoPor" to uid,
                "fechaActualizacion" to ahora
            )
        )
        lote.update(
            jugadores.document(vinculacion.idJugador),
            "idsApoderados", FieldValue.arrayUnion(vinculacion.idApoderado)
        )
        lote.commit().await()
    }

    suspend fun rechazar(idVinculacion: String, uid: String): Result<Unit> = intentar {
        val vinculacion = obtener(idVinculacion)
        verificarAdministrador(vinculacion.idAcademia, uid)
        if (vinculacion.estado != EstadoVinculacion.PENDIENTE) {
            throw IllegalStateException("Solo se pueden rechazar solicitudes pendientes")
        }
        vinculaciones.document(idVinculacion).update(
            mapOf(
                "estado" to EstadoVinculacion.INACTIVA.name,
                "actualizadoPor" to uid,
                "fechaActualizacion" to System.currentTimeMillis()
            )
        ).await()
    }

    // Revoca el acceso de inmediato; el historial se conserva
    suspend fun desvincular(idVinculacion: String, uid: String): Result<Unit> = intentar {
        val vinculacion = obtener(idVinculacion)
        verificarAdministrador(vinculacion.idAcademia, uid)
        if (vinculacion.estado != EstadoVinculacion.ACTIVA) {
            throw IllegalStateException("Solo se puede desvincular una relación activa")
        }
        val lote = bd.batch()
        lote.update(
            vinculaciones.document(idVinculacion),
            mapOf(
                "estado" to EstadoVinculacion.INACTIVA.name,
                "desvinculacionSolicitada" to false,
                "actualizadoPor" to uid,
                "fechaActualizacion" to System.currentTimeMillis()
            )
        )
        lote.update(
            jugadores.document(vinculacion.idJugador),
            "idsApoderados", FieldValue.arrayRemove(vinculacion.idApoderado)
        )
        lote.commit().await()
    }
}