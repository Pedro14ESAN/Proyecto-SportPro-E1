package com.example.projectsportpro2026_2.datos.repositorio

import com.example.projectsportpro2026_2.datos.modelo.Anuncio
import com.example.projectsportpro2026_2.datos.modelo.Entrenamiento
import com.example.projectsportpro2026_2.datos.modelo.EstadoAnuncio
import com.example.projectsportpro2026_2.datos.modelo.Notificacion
import com.example.projectsportpro2026_2.datos.modelo.PerfilUsuario
import com.example.projectsportpro2026_2.datos.modelo.TipoAudiencia
import com.example.projectsportpro2026_2.datos.modelo.TipoNotificacion
import com.example.projectsportpro2026_2.datos.modelo.convocatoria.Convocatoria
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// Avisos internos de HU20 (convocatorias, entrenamientos y anuncios)
class RepositorioNotificaciones(
    private val bd: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val destinatarios: ResolutorDestinatarios = ResolutorDestinatarios(bd),
    private val resolutor: ResolutorPertenencia = ResolutorPertenencia(bd),
    private val anuncios: RepositorioAnuncios = RepositorioAnuncios(bd)
) {

    private val notificaciones = bd.collection("notificaciones")
    private val entrenamientos = bd.collection("entrenamientos")
    private val convocatorias = bd.collection("convocatorias")
    private val usuarios = bd.collection("usuarios")

    private suspend fun <T> intentar(bloque: suspend () -> T): Result<T> =
        try {
            Result.success(bloque())
        } catch (e: Exception) {
            Result.failure(e)
        }

    private suspend fun obtenerPerfil(uid: String): PerfilUsuario =
        usuarios.document(uid).get().await()
            .toObject(PerfilUsuario::class.java)
            ?: throw IllegalStateException("Tu cuenta no tiene perfil")

    private fun noDisponible() =
        IllegalStateException("Este contenido ya no está disponible o ya no tienes acceso a él")

    // ---------- Envío ----------

    // Nunca lanza error: si falla, la acción principal (publicar, convocar...) ya se hizo
    // y no debe deshacerse por culpa de la notificación. Devuelve cuántas se crearon.
    suspend fun enviar(
        destinatarios: Set<String>,
        tipo: TipoNotificacion,
        titulo: String,
        mensaje: String,
        idReferencia: String,
        idEmisor: String
    ): Int {
        val ids = destinatarios.filter { it.isNotBlank() && it != idEmisor }
        if (ids.isEmpty()) return 0

        return try {
            val ahora = System.currentTimeMillis()
            ids.chunked(400).forEach { grupo ->
                val lote = bd.batch()
                grupo.forEach { uidDestino ->
                    val ref = notificaciones.document()
                    lote.set(
                        ref,
                        Notificacion(
                            id = ref.id,
                            idDestinatario = uidDestino,
                            tipo = tipo,
                            titulo = titulo.take(80),
                            mensaje = mensaje.take(200),
                            idReferencia = idReferencia,
                            idEmisor = idEmisor,
                            fechaCreacion = ahora
                        )
                    )
                }
                lote.commit().await()
            }
            ids.size
        } catch (e: Exception) {
            0
        }
    }

    // Anuncio publicado (HU19)
    suspend fun notificarAnuncio(anuncio: Anuncio) {
        val lista = runCatching {
            when (anuncio.tipoAudiencia) {
                TipoAudiencia.ACADEMIA -> destinatarios.deAcademia(anuncio.idAcademia)
                TipoAudiencia.EQUIPO -> destinatarios.deEquipo(anuncio.idEquipo)
                TipoAudiencia.ROL -> destinatarios.deAcademia(anuncio.idAcademia, anuncio.rolDestino)
            }
        }.getOrDefault(emptySet())

        enviar(
            destinatarios = lista,
            tipo = TipoNotificacion.ANUNCIO,
            titulo = "Nuevo anuncio: ${anuncio.titulo}",
            mensaje = "Toca para leer el comunicado completo.",
            idReferencia = anuncio.id,
            idEmisor = anuncio.idAutor
        )
    }

    // Convocatoria publicada o modificada (HU11). Los textos no nombran al jugador.
    suspend fun notificarConvocatoria(
        idPartido: String,
        nombreRival: String,
        idsFichasNuevas: Collection<String>,
        idsFichasRetiradas: Collection<String>,
        idEmisor: String
    ) {
        enviar(
            destinatarios = runCatching { destinatarios.deJugadores(idsFichasNuevas) }.getOrDefault(emptySet()),
            tipo = TipoNotificacion.CONVOCATORIA,
            titulo = "Convocatoria: partido vs. $nombreRival",
            mensaje = "Confirma la disponibilidad en SportPro.",
            idReferencia = idPartido,
            idEmisor = idEmisor
        )
        enviar(
            destinatarios = runCatching { destinatarios.deJugadores(idsFichasRetiradas) }.getOrDefault(emptySet()),
            tipo = TipoNotificacion.CONVOCATORIA_RETIRADA,
            titulo = "Cambio en la convocatoria vs. $nombreRival",
            mensaje = "Revisa el estado de la convocatoria en SportPro.",
            idReferencia = idPartido,
            idEmisor = idEmisor
        )
    }

    // Entrenamiento publicado, con cambio de horario o cancelado (HU08)
    suspend fun notificarEntrenamiento(
        entrenamiento: Entrenamiento,
        tipo: TipoNotificacion,
        idEmisor: String
    ) {
        val (titulo, mensaje) = when (tipo) {
            TipoNotificacion.ENTRENAMIENTO_PUBLICADO ->
                "Nuevo entrenamiento: ${entrenamiento.nombreEquipo}" to
                        "${entrenamiento.fecha} a las ${entrenamiento.hora}"
            TipoNotificacion.ENTRENAMIENTO_CAMBIADO ->
                "Cambio de horario: ${entrenamiento.nombreEquipo}" to
                        "Ahora es el ${entrenamiento.fecha} a las ${entrenamiento.hora}"
            TipoNotificacion.ENTRENAMIENTO_CANCELADO ->
                "Entrenamiento cancelado: ${entrenamiento.nombreEquipo}" to
                        "La sesión del ${entrenamiento.fecha} fue cancelada"
            else -> return
        }

        enviar(
            destinatarios = runCatching { destinatarios.deEquipo(entrenamiento.idEquipo) }.getOrDefault(emptySet()),
            tipo = tipo,
            titulo = titulo,
            mensaje = mensaje,
            idReferencia = entrenamiento.id,
            idEmisor = idEmisor
        )
    }

    // ---------- Lectura ----------

    suspend fun listar(uid: String): Result<List<Notificacion>> = intentar {
        notificaciones.whereEqualTo("idDestinatario", uid).get().await()
            .documents
            .mapNotNull { it.toObject(Notificacion::class.java)?.copy(id = it.id) }
            .sortedByDescending { it.fechaCreacion }
            .take(100)
    }

    suspend fun marcarLeida(idNotificacion: String): Result<Unit> = intentar {
        notificaciones.document(idNotificacion).update("leida", true).await()
    }

    suspend fun marcarTodasLeidas(uid: String): Result<Unit> = intentar {
        val pendientes = notificaciones
            .whereEqualTo("idDestinatario", uid)
            .whereEqualTo("leida", false)
            .get().await().documents

        pendientes.chunked(400).forEach { grupo ->
            val lote = bd.batch()
            grupo.forEach { lote.update(it.reference, "leida", true) }
            lote.commit().await()
        }
    }

    // ---------- Acceso al tocar ----------

    // Criterio HU20: solo se abre si el contenido sigue existiendo y el usuario mantiene el permiso
    suspend fun verificarAcceso(notificacion: Notificacion, uid: String): Result<Unit> = intentar {
        when (notificacion.tipo) {

            TipoNotificacion.ANUNCIO -> {
                val visibles = anuncios.listarParaUsuario(uid).getOrThrow()
                val sigueActivo = visibles.any {
                    it.id == notificacion.idReferencia && it.estado == EstadoAnuncio.ACTIVO
                }
                if (!sigueActivo) throw noDisponible()
            }

            TipoNotificacion.ENTRENAMIENTO_PUBLICADO,
            TipoNotificacion.ENTRENAMIENTO_CAMBIADO,
            TipoNotificacion.ENTRENAMIENTO_CANCELADO -> {
                val entrenamiento = entrenamientos.document(notificacion.idReferencia).get().await()
                    .toObject(Entrenamiento::class.java) ?: throw noDisponible()
                val perfil = obtenerPerfil(uid)
                val p = resolutor.obtener(uid, perfil.rol)
                if (entrenamiento.idEquipo !in p.idsEquipos) throw noDisponible()
            }

            TipoNotificacion.CONVOCATORIA,
            TipoNotificacion.CONVOCATORIA_RETIRADA -> {
                val convocatoria = convocatorias.document(notificacion.idReferencia).get().await()
                    .toObject(Convocatoria::class.java) ?: throw noDisponible()
                val perfil = obtenerPerfil(uid)
                val p = resolutor.obtener(uid, perfil.rol)
                if (convocatoria.idEquipo !in p.idsEquipos) throw noDisponible()
            }
        }
    }
}