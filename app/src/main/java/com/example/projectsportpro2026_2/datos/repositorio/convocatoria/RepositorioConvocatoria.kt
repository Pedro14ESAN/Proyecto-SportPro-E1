package com.example.projectsportpro2026_2.datos.repositorio.convocatoria

import com.example.projectsportpro2026_2.datos.modelo.Academia
import com.example.projectsportpro2026_2.datos.modelo.Jugador
import com.example.projectsportpro2026_2.datos.modelo.PerfilUsuario
import com.example.projectsportpro2026_2.datos.modelo.Rol
import com.example.projectsportpro2026_2.datos.modelo.convocatoria.Convocatoria
import com.example.projectsportpro2026_2.datos.modelo.convocatoria.EstadoDisponibilidad
import com.example.projectsportpro2026_2.datos.modelo.convocatoria.JugadorConvocado
import com.example.projectsportpro2026_2.datos.modelo.partido.EstadoPartido
import com.example.projectsportpro2026_2.datos.modelo.partido.Partido
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class RepositorioConvocatorias(
    private val bd: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val convocatorias = bd.collection("convocatorias")
    private val partidos = bd.collection("partidos")
    private val jugadores = bd.collection("jugadores")
    private val academias = bd.collection("academias")
    private val usuarios = bd.collection("usuarios")

    private suspend fun <T> intentar(
        bloque: suspend () -> T
    ): Result<T> =
        try {
            Result.success(bloque())
        } catch (e: Exception) {
            Result.failure(e)
        }

    private suspend fun obtenerPartido(
        idPartido: String
    ): Partido =
        partidos.document(idPartido)
            .get()
            .await()
            .toObject(Partido::class.java)
            ?: throw IllegalStateException(
                "El partido no existe"
            )

    private suspend fun obtenerPerfil(
        uid: String
    ): PerfilUsuario =
        usuarios.document(uid)
            .get()
            .await()
            .toObject(PerfilUsuario::class.java)
            ?: throw IllegalStateException(
                "El usuario no tiene un perfil válido"
            )

    private suspend fun verificarGestor(
        partido: Partido,
        uid: String
    ) {
        val perfil = obtenerPerfil(uid)

        if (
            perfil.rol != Rol.ADM &&
            perfil.rol != Rol.DT
        ) {
            throw IllegalStateException(
                "Solo un administrador o entrenador puede gestionar la convocatoria"
            )
        }

        val academia = academias
            .document(partido.idAcademia)
            .get()
            .await()
            .toObject(Academia::class.java)
            ?: throw IllegalStateException(
                "La academia no existe"
            )

        if (!academia.activa) {
            throw IllegalStateException(
                "La academia está inactiva"
            )
        }

        if (uid !in academia.idsAutorizados) {
            throw IllegalStateException(
                "No tienes permiso para gestionar esta academia"
            )
        }
    }

    suspend fun obtenerConvocatoria(
        idPartido: String,
        uid: String
    ): Result<Convocatoria?> = intentar {

        val partido = obtenerPartido(idPartido)

        verificarGestor(
            partido,
            uid
        )

        convocatorias
            .document(idPartido)
            .get()
            .await()
            .toObject(Convocatoria::class.java)
    }

    suspend fun listarConvocados(
        idPartido: String,
        uid: String
    ): Result<List<JugadorConvocado>> = intentar {

        val partido = obtenerPartido(idPartido)

        verificarGestor(
            partido,
            uid
        )

        convocatorias
            .document(idPartido)
            .collection("jugadores")
            .get()
            .await()
            .toObjects(JugadorConvocado::class.java)
    }

    suspend fun publicarConvocatoria(
        idPartido: String,
        idsJugadores: List<String>,
        uid: String
    ): Result<Convocatoria> = intentar {

        val partido = obtenerPartido(idPartido)

        verificarGestor(
            partido,
            uid
        )

        if (partido.estado != EstadoPartido.PROGRAMADO) {
            throw IllegalStateException(
                "La convocatoria solo puede modificarse antes de iniciar el partido"
            )
        }

        if (idsJugadores.isEmpty()) {
            throw IllegalArgumentException(
                "Selecciona al menos un jugador"
            )
        }

        val idsUnicos = idsJugadores.distinct()

        if (idsUnicos.size != idsJugadores.size) {
            throw IllegalArgumentException(
                "Un jugador no puede aparecer dos veces en la convocatoria"
            )
        }

        val plantillaEquipo = jugadores
            .whereEqualTo("idEquipo", partido.idEquipo)
            .get()
            .await()
            .toObjects(Jugador::class.java)

        val idsPlantilla = plantillaEquipo
            .map { it.id }
            .toSet()

        val idFueraDelEquipo = idsUnicos.firstOrNull {
            it !in idsPlantilla
        }

        if (idFueraDelEquipo != null) {
            throw IllegalArgumentException(
                "Solo puedes convocar jugadores del equipo del partido"
            )
        }

        val seleccionados = plantillaEquipo.filter {
            it.id in idsUnicos
        }

        val inactivo = seleccionados.firstOrNull {
            !it.activo
        }

        if (inactivo != null) {
            throw IllegalStateException(
                "${inactivo.nombreCompleto()} está inactivo y no puede ser convocado"
            )
        }

        val documentoConvocatoria =
            convocatorias.document(idPartido)

        val convocatoriaAnterior =
            documentoConvocatoria
                .get()
                .await()
                .toObject(Convocatoria::class.java)

        val convocadosAnteriores =
            documentoConvocatoria
                .collection("jugadores")
                .get()
                .await()
                .toObjects(JugadorConvocado::class.java)

        val anterioresPorId =
            convocadosAnteriores.associateBy {
                it.idJugador
            }

        val ahora = System.currentTimeMillis()

        val convocatoria = Convocatoria(
            idPartido = partido.id,
            idEquipo = partido.idEquipo,
            idAcademia = partido.idAcademia,
            publicada = true,
            creadoPor = convocatoriaAnterior
                ?.creadoPor
                ?.takeIf { it.isNotBlank() }
                ?: uid,
            fechaPublicacion = convocatoriaAnterior
                ?.fechaPublicacion
                ?.takeIf { it > 0L }
                ?: ahora,
            actualizadoPor = uid,
            fechaActualizacion = ahora
        )

        val lote = bd.batch()

        lote.set(
            documentoConvocatoria,
            convocatoria
        )

        idsUnicos.forEach { idJugador ->

            val documentoJugador =
                documentoConvocatoria
                    .collection("jugadores")
                    .document(idJugador)

            val existente =
                anterioresPorId[idJugador]

            // Si ya estaba convocado, conservamos su respuesta.
            // Si es nuevo, empieza PENDIENTE.
            val convocado = existente ?: JugadorConvocado(
                idJugador = idJugador,
                estado = EstadoDisponibilidad.PENDIENTE
            )

            lote.set(
                documentoJugador,
                convocado
            )
        }

        // Si el DT quitó a alguien al actualizar la convocatoria,
        // se elimina de la convocatoria mientras el partido siga PROGRAMADO.
        convocadosAnteriores
            .filter {
                it.idJugador !in idsUnicos
            }
            .forEach { eliminado ->

                lote.delete(
                    documentoConvocatoria
                        .collection("jugadores")
                        .document(eliminado.idJugador)
                )
            }

        lote.commit().await()

        convocatoria
    }

    suspend fun responderDisponibilidad(
        idPartido: String,
        idJugador: String,
        nuevoEstado: EstadoDisponibilidad,
        uid: String
    ): Result<Unit> = intentar {

        if (
            nuevoEstado != EstadoDisponibilidad.DISPONIBLE &&
            nuevoEstado != EstadoDisponibilidad.NO_DISPONIBLE
        ) {
            throw IllegalArgumentException(
                "La respuesta debe ser Disponible o No disponible"
            )
        }

        val partido = obtenerPartido(idPartido)

        if (partido.estado != EstadoPartido.PROGRAMADO) {
            throw IllegalStateException(
                "La convocatoria ya es histórica porque el partido comenzó"
            )
        }

        val documentoConvocatoria =
            convocatorias.document(idPartido)

        val convocatoria =
            documentoConvocatoria
                .get()
                .await()
                .toObject(Convocatoria::class.java)
                ?: throw IllegalStateException(
                    "La convocatoria no existe"
                )

        if (!convocatoria.publicada) {
            throw IllegalStateException(
                "La convocatoria todavía no ha sido publicada"
            )
        }

        val documentoConvocado =
            documentoConvocatoria
                .collection("jugadores")
                .document(idJugador)

        val convocado =
            documentoConvocado
                .get()
                .await()
                .toObject(JugadorConvocado::class.java)
                ?: throw IllegalStateException(
                    "El jugador no está convocado para este partido"
                )

        val jugador =
            jugadores.document(idJugador)
                .get()
                .await()
                .toObject(Jugador::class.java)
                ?: throw IllegalStateException(
                    "El jugador no existe"
                )

        val esElJugador =
            jugador.idUsuario.isNotBlank() &&
                    jugador.idUsuario == uid

        val esApoderado =
            uid in jugador.idsApoderados

        if (!esElJugador && !esApoderado) {
            throw IllegalStateException(
                "No tienes permiso para responder por este jugador"
            )
        }

        documentoConvocado.update(
            mapOf(
                "estado" to nuevoEstado.name,
                "respondidoPor" to uid,
                "fechaRespuesta" to System.currentTimeMillis()
            )
        ).await()
    }

    private suspend fun buscarConvocatoriasDeJugador(
        jugador: Jugador
    ): List<ConvocatoriaParaResponder> {

        val documentos = bd
            .collectionGroup("jugadores")
            .whereEqualTo(
                "idJugador",
                jugador.id
            )
            .get()
            .await()
            .documents

        return documentos.mapNotNull { documento ->

            val convocado =
                documento.toObject(
                    JugadorConvocado::class.java
                )
                    ?: return@mapNotNull null

            /*
             * Estructura:
             *
             * convocatorias/{idPartido}/jugadores/{idJugador}
             *
             * documento.parent      -> jugadores
             * documento.parent.parent -> convocatoria / idPartido
             */

            val documentoConvocatoria =
                documento.reference.parent.parent
                    ?: return@mapNotNull null

            val convocatoria =
                documentoConvocatoria
                    .get()
                    .await()
                    .toObject(
                        Convocatoria::class.java
                    )
                    ?: return@mapNotNull null

            if (!convocatoria.publicada) {
                return@mapNotNull null
            }

            val partido =
                partidos.document(
                    convocatoria.idPartido
                )
                    .get()
                    .await()
                    .toObject(
                        Partido::class.java
                    )
                    ?: return@mapNotNull null

            ConvocatoriaParaResponder(
                partido = partido,
                jugador = jugador,
                convocado = convocado
            )
        }
    }

    suspend fun listarParaJugador(
        uid: String
    ): Result<List<ConvocatoriaParaResponder>> =
        intentar {

            val perfil = obtenerPerfil(uid)

            if (perfil.rol != Rol.JUG) {
                throw IllegalStateException(
                    "Solo un jugador puede consultar sus convocatorias con esta opción"
                )
            }

            val jugador =
                jugadores
                    .whereEqualTo(
                        "idUsuario",
                        uid
                    )
                    .get()
                    .await()
                    .toObjects(
                        Jugador::class.java
                    )
                    .firstOrNull {
                        it.activo
                    }
                    ?: throw IllegalStateException(
                        "Tu cuenta todavía no está asociada a un jugador activo"
                    )

            buscarConvocatoriasDeJugador(
                jugador
            ).sortedWith(
                compareBy<ConvocatoriaParaResponder> {
                    it.partido.fecha
                }.thenBy {
                    it.partido.hora
                }
            )
        }
    suspend fun listarParaApoderado(
        uid: String
    ): Result<List<ConvocatoriaParaResponder>> =
        intentar {

            val perfil = obtenerPerfil(uid)

            if (perfil.rol != Rol.PAD) {
                throw IllegalStateException(
                    "Solo un apoderado puede consultar convocatorias de sus jugadores"
                )
            }

            val jugadoresVinculados =
                jugadores
                    .whereArrayContains(
                        "idsApoderados",
                        uid
                    )
                    .get()
                    .await()
                    .toObjects(
                        Jugador::class.java
                    )
                    .filter {
                        it.activo
                    }

            jugadoresVinculados
                .flatMap { jugador ->
                    buscarConvocatoriasDeJugador(
                        jugador
                    )
                }
                .distinctBy {
                    "${it.partido.id}-${it.jugador.id}"
                }
                .sortedWith(
                    compareBy<ConvocatoriaParaResponder> {
                        it.partido.fecha
                    }.thenBy {
                        it.partido.hora
                    }
                )
        }
}
data class ConvocatoriaParaResponder(
    val partido: Partido = Partido(),
    val jugador: Jugador = Jugador(),
    val convocado: JugadorConvocado = JugadorConvocado()
)