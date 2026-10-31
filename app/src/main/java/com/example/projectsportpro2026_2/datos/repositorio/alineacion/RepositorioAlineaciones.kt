package com.example.projectsportpro2026_2.datos.repositorio.alineacion

import com.example.projectsportpro2026_2.datos.modelo.Academia
import com.example.projectsportpro2026_2.datos.modelo.Jugador
import com.example.projectsportpro2026_2.datos.modelo.PerfilUsuario
import com.example.projectsportpro2026_2.datos.modelo.Rol
import com.example.projectsportpro2026_2.datos.modelo.alineacion.Alineacion
import com.example.projectsportpro2026_2.datos.modelo.alineacion.FormatoPartido
import com.example.projectsportpro2026_2.datos.modelo.alineacion.JugadorAlineacion
import com.example.projectsportpro2026_2.datos.modelo.alineacion.TipoJugadorAlineacion
import com.example.projectsportpro2026_2.datos.modelo.convocatoria.Convocatoria
import com.example.projectsportpro2026_2.datos.modelo.convocatoria.EstadoDisponibilidad
import com.example.projectsportpro2026_2.datos.modelo.convocatoria.JugadorConvocado
import com.example.projectsportpro2026_2.datos.modelo.partido.EstadoPartido
import com.example.projectsportpro2026_2.datos.modelo.partido.Partido
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class RepositorioAlineaciones(
    private val bd: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val alineaciones = bd.collection("alineaciones")
    private val partidos = bd.collection("partidos")
    private val convocatorias = bd.collection("convocatorias")
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

    private suspend fun verificarGestor(
        partido: Partido,
        uid: String
    ) {
        val perfil =
            usuarios.document(uid)
                .get()
                .await()
                .toObject(PerfilUsuario::class.java)
                ?: throw IllegalStateException(
                    "El usuario no tiene un perfil válido"
                )

        if (
            perfil.rol != Rol.ADM &&
            perfil.rol != Rol.DT
        ) {
            throw IllegalStateException(
                "Solo un administrador o entrenador puede gestionar la alineación"
            )
        }

        val academia =
            academias.document(partido.idAcademia)
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

    suspend fun listarJugadoresDisponibles(
        idPartido: String,
        uid: String
    ): Result<List<Jugador>> = intentar {

        val partido = obtenerPartido(idPartido)

        verificarGestor(
            partido,
            uid
        )

        val convocatoria =
            convocatorias.document(idPartido)
                .get()
                .await()
                .toObject(Convocatoria::class.java)
                ?: throw IllegalStateException(
                    "El partido todavía no tiene una convocatoria"
                )

        if (!convocatoria.publicada) {
            throw IllegalStateException(
                "La convocatoria todavía no ha sido publicada"
            )
        }

        val convocados =
            convocatorias.document(idPartido)
                .collection("jugadores")
                .get()
                .await()
                .toObjects(JugadorConvocado::class.java)

        val idsDisponibles =
            convocados
                .filter {
                    it.estado == EstadoDisponibilidad.DISPONIBLE
                }
                .map {
                    it.idJugador
                }
                .toSet()

        if (idsDisponibles.isEmpty()) {
            return@intentar emptyList()
        }

        val resultado =
            idsDisponibles.mapNotNull { idJugador ->

                jugadores.document(idJugador)
                    .get()
                    .await()
                    .toObject(Jugador::class.java)
            }
                .filter {
                    it.activo &&
                            it.idEquipo == partido.idEquipo
                }
                .sortedWith(
                    compareBy<Jugador> {
                        it.posicion
                    }.thenBy {
                        it.apellidos
                    }
                )

        resultado
    }

    suspend fun obtenerAlineacion(
        idPartido: String,
        uid: String
    ): Result<Alineacion?> = intentar {

        val partido = obtenerPartido(idPartido)

        verificarGestor(
            partido,
            uid
        )

        alineaciones.document(idPartido)
            .get()
            .await()
            .toObject(Alineacion::class.java)
    }

    suspend fun listarJugadoresAlineacion(
        idPartido: String,
        uid: String
    ): Result<List<JugadorAlineacion>> = intentar {

        val partido = obtenerPartido(idPartido)

        verificarGestor(
            partido,
            uid
        )

        alineaciones.document(idPartido)
            .collection("jugadores")
            .get()
            .await()
            .toObjects(JugadorAlineacion::class.java)
            .sortedBy {
                it.orden
            }
    }

    suspend fun guardarAlineacion(
        idPartido: String,
        formato: FormatoPartido,
        jugadoresAlineacion: List<JugadorAlineacion>,
        confirmada: Boolean,
        uid: String
    ): Result<Alineacion> = intentar {

        val partido = obtenerPartido(idPartido)

        verificarGestor(
            partido,
            uid
        )

        if (partido.estado != EstadoPartido.PROGRAMADO) {
            throw IllegalStateException(
                "La alineación solo puede modificarse antes de iniciar el partido"
            )
        }

        val ids =
            jugadoresAlineacion.map {
                it.idJugador
            }

        if (ids.size != ids.distinct().size) {
            throw IllegalArgumentException(
                "Un jugador no puede aparecer más de una vez en la alineación"
            )
        }

        val titulares =
            jugadoresAlineacion.filter {
                it.tipo == TipoJugadorAlineacion.TITULAR
            }

        if (
            titulares.size >
            formato.maximoTitulares
        ) {
            throw IllegalArgumentException(
                "El formato ${formato.etiqueta} permite como máximo " +
                        "${formato.maximoTitulares} titulares"
            )
        }
        if (
            confirmada &&
            titulares.size != formato.maximoTitulares
        ) {
            throw IllegalArgumentException(
                "Para confirmar la alineación debes completar " +
                        "${formato.maximoTitulares} titulares"
            )
        }
        if (confirmada) {

            val titularSinPosicion =
                titulares.firstOrNull {
                    it.posicionCampo.isBlank()
                }

            if (titularSinPosicion != null) {
                throw IllegalArgumentException(
                    "Todos los titulares deben ocupar una posición en el campo"
                )
            }

            val posiciones =
                titulares.map {
                    it.posicionCampo
                }

            if (
                posiciones.size !=
                posiciones.distinct().size
            ) {
                throw IllegalArgumentException(
                    "Dos titulares no pueden ocupar la misma posición en el campo"
                )
            }
        }

        val convocatoria =
            convocatorias.document(idPartido)
                .get()
                .await()
                .toObject(Convocatoria::class.java)
                ?: throw IllegalStateException(
                    "El partido todavía no tiene una convocatoria"
                )

        if (!convocatoria.publicada) {
            throw IllegalStateException(
                "La convocatoria todavía no ha sido publicada"
            )
        }

        val convocados =
            convocatorias.document(idPartido)
                .collection("jugadores")
                .get()
                .await()
                .toObjects(JugadorConvocado::class.java)

        val convocadosPorId =
            convocados.associateBy {
                it.idJugador
            }

        jugadoresAlineacion.forEach { jugadorAlineacion ->

            val convocado =
                convocadosPorId[
                    jugadorAlineacion.idJugador
                ]
                    ?: throw IllegalArgumentException(
                        "Solo puedes incluir jugadores convocados"
                    )

            if (
                convocado.estado !=
                EstadoDisponibilidad.DISPONIBLE
            ) {
                throw IllegalStateException(
                    "Solo los jugadores disponibles pueden formar parte de la alineación"
                )
            }

            val jugador =
                jugadores.document(
                    jugadorAlineacion.idJugador
                )
                    .get()
                    .await()
                    .toObject(Jugador::class.java)
                    ?: throw IllegalStateException(
                        "Uno de los jugadores ya no existe"
                    )

            if (!jugador.activo) {
                throw IllegalStateException(
                    "${jugador.nombreCompleto()} está inactivo"
                )
            }

            if (jugador.idEquipo != partido.idEquipo) {
                throw IllegalStateException(
                    "${jugador.nombreCompleto()} ya no pertenece al equipo del partido"
                )
            }
        }

        val documentoAlineacion =
            alineaciones.document(idPartido)

        val anterior =
            documentoAlineacion
                .get()
                .await()
                .toObject(Alineacion::class.java)

        val jugadoresAnteriores =
            documentoAlineacion
                .collection("jugadores")
                .get()
                .await()
                .toObjects(JugadorAlineacion::class.java)

        val ahora =
            System.currentTimeMillis()

        val alineacion =
            Alineacion(
                idPartido = partido.id,
                idEquipo = partido.idEquipo,
                idAcademia = partido.idAcademia,
                formato = formato,
                confirmada = confirmada,
                creadoPor =
                    anterior
                        ?.creadoPor
                        ?.takeIf {
                            it.isNotBlank()
                        }
                        ?: uid,
                fechaCreacion =
                    anterior
                        ?.fechaCreacion
                        ?.takeIf {
                            it > 0L
                        }
                        ?: ahora,
                actualizadoPor = uid,
                fechaActualizacion = ahora
            )

        val lote =
            bd.batch()

        lote.set(
            documentoAlineacion,
            alineacion
        )

        jugadoresAlineacion.forEach { jugador ->

            lote.set(
                documentoAlineacion
                    .collection("jugadores")
                    .document(jugador.idJugador),
                jugador
            )
        }

        jugadoresAnteriores
            .filter { anteriorJugador ->
                anteriorJugador.idJugador !in ids
            }
            .forEach { eliminado ->

                lote.delete(
                    documentoAlineacion
                        .collection("jugadores")
                        .document(eliminado.idJugador)
                )
            }

        lote.commit().await()

        alineacion
    }
}