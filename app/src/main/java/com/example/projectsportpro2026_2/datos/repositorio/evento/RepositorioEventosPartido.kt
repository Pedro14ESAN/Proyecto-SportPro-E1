package com.example.projectsportpro2026_2.datos.repositorio.evento

import com.example.projectsportpro2026_2.datos.modelo.Academia
import com.example.projectsportpro2026_2.datos.modelo.Equipo
import com.example.projectsportpro2026_2.datos.modelo.Jugador
import com.example.projectsportpro2026_2.datos.modelo.PerfilUsuario
import com.example.projectsportpro2026_2.datos.modelo.Rol
import com.example.projectsportpro2026_2.datos.modelo.alineacion.Alineacion
import com.example.projectsportpro2026_2.datos.modelo.alineacion.JugadorAlineacion
import com.example.projectsportpro2026_2.datos.modelo.evento.EquipoEvento
import com.example.projectsportpro2026_2.datos.modelo.evento.EventoPartido
import com.example.projectsportpro2026_2.datos.modelo.evento.TipoEventoCatalogo
import com.example.projectsportpro2026_2.datos.modelo.partido.EstadoPartido
import com.example.projectsportpro2026_2.datos.modelo.partido.Partido
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class ContextoPartidoEnVivo(
    val partido: Partido,
    val equipo: Equipo,
    val tiposEvento: List<TipoEventoCatalogo>,
    val jugadoresAlineados: List<Jugador>
)

data class MarcadorPartido(
    val propio: Int = 0,
    val rival: Int = 0
)

class RepositorioEventosPartido(
    private val bd: FirebaseFirestore =
        FirebaseFirestore.getInstance()
) {

    private val partidos =
        bd.collection("partidos")

    private val academias =
        bd.collection("academias")

    private val equipos =
        bd.collection("equipos")

    private val jugadores =
        bd.collection("jugadores")

    private val usuarios =
        bd.collection("usuarios")

    private val alineaciones =
        bd.collection("alineaciones")

    private suspend fun <T> intentar(
        bloque: suspend () -> T
    ): Result<T> =
        try {
            Result.success(
                bloque()
            )
        } catch (e: Exception) {
            Result.failure(e)
        }

    private suspend fun obtenerPartido(
        idPartido: String
    ): Partido {

        if (idPartido.isBlank()) {
            throw IllegalArgumentException(
                "El partido no es válido"
            )
        }

        return partidos
            .document(idPartido)
            .get()
            .await()
            .toObject(Partido::class.java)
            ?: throw IllegalStateException(
                "El partido no existe"
            )
    }

    /*
     * HU14 habla de un operador autorizado.
     *
     * El modelo actual del proyecto todavía no tiene un rol
     * OPERADOR independiente, por lo que utilizamos los roles
     * disponibles que pueden operar partidos: ADM y DT.
     */
    private suspend fun verificarOperador(
        partido: Partido,
        uid: String
    ) {

        if (uid.isBlank()) {
            throw IllegalStateException(
                "No existe una sesión activa"
            )
        }

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
                "No tienes permiso para registrar eventos del partido"
            )
        }

        val academia =
            academias.document(
                partido.idAcademia
            )
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

        val autorizado =
            academia.idAdministrador == uid ||
                    uid in academia.idsAutorizados

        if (!autorizado) {
            throw IllegalStateException(
                "No estás autorizado para operar partidos de esta academia"
            )
        }
    }

    private suspend fun obtenerAlineacionConfirmada(
        partido: Partido
    ): Alineacion {

        val alineacion =
            alineaciones.document(
                partido.id
            )
                .get()
                .await()
                .toObject(Alineacion::class.java)
                ?: throw IllegalStateException(
                    "El partido todavía no tiene una alineación"
                )

        if (!alineacion.confirmada) {
            throw IllegalStateException(
                "La alineación debe estar confirmada antes de registrar eventos"
            )
        }

        if (
            alineacion.idEquipo !=
            partido.idEquipo
        ) {
            throw IllegalStateException(
                "La alineación no corresponde al equipo actual del partido"
            )
        }

        return alineacion
    }

    private suspend fun obtenerIdsAlineados(
        idPartido: String
    ): Set<String> =
        alineaciones.document(idPartido)
            .collection("jugadores")
            .get()
            .await()
            .toObjects(
                JugadorAlineacion::class.java
            )
            .map {
                it.idJugador
            }
            .filter {
                it.isNotBlank()
            }
            .toSet()

    suspend fun obtenerContexto(
        idPartido: String,
        uid: String
    ): Result<ContextoPartidoEnVivo> =
        intentar {

            val partido =
                obtenerPartido(idPartido)

            verificarOperador(
                partido = partido,
                uid = uid
            )

            if (
                partido.estado !=
                EstadoPartido.EN_CURSO
            ) {
                throw IllegalStateException(
                    "El partido debe estar en curso para registrar eventos"
                )
            }

            obtenerAlineacionConfirmada(
                partido
            )

            val equipo =
                equipos.document(
                    partido.idEquipo
                )
                    .get()
                    .await()
                    .toObject(Equipo::class.java)
                    ?: throw IllegalStateException(
                        "El equipo del partido no existe"
                    )

            val tiposEvento =
                academias.document(
                    partido.idAcademia
                )
                    .collection(
                        "catalogoEventos"
                    )
                    .get()
                    .await()
                    .documents
                    .mapNotNull {
                            documento ->

                        documento
                            .toObject(
                                TipoEventoCatalogo::class.java
                            )
                            ?.copy(
                                id = documento.id
                            )
                    }
                    .filter {
                        it.activo
                    }
                    .sortedWith(
                        compareBy<TipoEventoCatalogo> {
                            it.orden
                        }.thenBy {
                            it.nombre.lowercase()
                        }
                    )

            if (tiposEvento.isEmpty()) {
                throw IllegalStateException(
                    "La academia no tiene eventos activos en su catálogo"
                )
            }

            val idsAlineados =
                obtenerIdsAlineados(
                    idPartido
                )

            val jugadoresAlineados =
                idsAlineados
                    .mapNotNull {
                            idJugador ->

                        jugadores.document(
                            idJugador
                        )
                            .get()
                            .await()
                            .toObject(
                                Jugador::class.java
                            )
                    }
                    .filter {
                        it.activo &&
                                it.idEquipo ==
                                partido.idEquipo
                    }
                    .sortedBy {
                        it.nombreCompleto()
                            .lowercase()
                    }

            ContextoPartidoEnVivo(
                partido = partido,
                equipo = equipo,
                tiposEvento = tiposEvento,
                jugadoresAlineados =
                    jugadoresAlineados
            )
        }

    suspend fun registrarEvento(
        idPartido: String,
        idTipoEvento: String,
        minuto: Int,
        equipoEvento: EquipoEvento,
        idJugador: String,
        nombreJugadorRival: String,
        observacion: String,
        uid: String
    ): Result<EventoPartido> =
        intentar {

            val partido =
                obtenerPartido(idPartido)

            verificarOperador(
                partido = partido,
                uid = uid
            )

            /*
             * Regla principal de HU14.
             */
            if (
                partido.estado !=
                EstadoPartido.EN_CURSO
            ) {
                throw IllegalStateException(
                    "Solo se pueden registrar eventos mientras el partido está en curso"
                )
            }

            obtenerAlineacionConfirmada(
                partido
            )

            if (minuto < 0) {
                throw IllegalArgumentException(
                    "El minuto no puede ser negativo"
                )
            }

            if (idTipoEvento.isBlank()) {
                throw IllegalArgumentException(
                    "Debes seleccionar un tipo de evento"
                )
            }

            /*
             * No confiamos solamente en lo que envíe la UI.
             * Volvemos a leer HU13 desde Firestore.
             */
            val referenciaTipo =
                academias.document(
                    partido.idAcademia
                )
                    .collection(
                        "catalogoEventos"
                    )
                    .document(
                        idTipoEvento
                    )

            val tipo =
                referenciaTipo
                    .get()
                    .await()
                    .toObject(
                        TipoEventoCatalogo::class.java
                    )
                    ?: throw IllegalStateException(
                        "El tipo de evento ya no existe"
                    )

            if (!tipo.activo) {
                throw IllegalStateException(
                    "El tipo de evento ${tipo.nombre} ya no está activo"
                )
            }

            /*
             * INICIO no tiene sentido dentro de HU14 porque
             * la precondición exige que el partido ya esté
             * EN_CURSO.
             */
            if (
                tipo.codigo ==
                "INICIO"
            ) {
                throw IllegalStateException(
                    "El partido ya se encuentra en curso"
                )
            }

            val equipoFinal =
                validarEquipo(
                    tipo = tipo,
                    equipoEvento = equipoEvento
                )

            val jugadorFinal =
                validarJugador(
                    partido = partido,
                    tipo = tipo,
                    equipoEvento = equipoFinal,
                    idJugador = idJugador,
                    nombreJugadorRival =
                        nombreJugadorRival
                )

            val observacionFinal =
                validarObservacion(
                    tipo = tipo,
                    observacion = observacion
                )

            val documento =
                partidos.document(
                    idPartido
                )
                    .collection(
                        "eventos"
                    )
                    .document()

            val ahora =
                System.currentTimeMillis()

            val evento =
                EventoPartido(
                    id = documento.id,

                    idPartido = partido.id,
                    idAcademia =
                        partido.idAcademia,

                    idTipoEvento =
                        tipo.id,

                    codigoTipoEvento =
                        tipo.codigo,

                    nombreTipoEvento =
                        tipo.nombre,

                    minuto = minuto,

                    equipoEvento =
                        equipoFinal,

                    idJugador =
                        jugadorFinal.first,

                    nombreJugador =
                        jugadorFinal.second,

                    observacion =
                        observacionFinal,

                    afectaMarcador =
                        tipo.afectaMarcador,

                    afectaEstadoPartido =
                        tipo.afectaEstadoPartido,

                    creadoPor = uid,
                    fechaCaptura = ahora
                )

            /*
             * Normalmente solo guardamos el evento.
             *
             * Si el evento es FIN, además finalizamos el
             * partido en el mismo batch para que ambas
             * escrituras ocurran juntas.
             */
            val lote =
                bd.batch()

            lote.set(
                documento,
                evento
            )

            if (
                tipo.codigo == "FIN" &&
                tipo.afectaEstadoPartido
            ) {

                lote.update(
                    partidos.document(
                        idPartido
                    ),
                    mapOf(
                        "estado" to
                                EstadoPartido.FINALIZADO,

                        "actualizadoPor" to
                                uid,

                        "fechaActualizacion" to
                                ahora
                    )
                )
            }

            lote.commit().await()

            evento
        }

    private fun validarEquipo(
        tipo: TipoEventoCatalogo,
        equipoEvento: EquipoEvento
    ): EquipoEvento {

        if (!tipo.requiereEquipo) {
            return EquipoEvento.NINGUNO
        }

        if (
            equipoEvento ==
            EquipoEvento.NINGUNO
        ) {
            throw IllegalArgumentException(
                "${tipo.nombre} requiere seleccionar un equipo"
            )
        }

        return equipoEvento
    }

    private suspend fun validarJugador(
        partido: Partido,
        tipo: TipoEventoCatalogo,
        equipoEvento: EquipoEvento,
        idJugador: String,
        nombreJugadorRival: String
    ): Pair<String, String> {

        if (!tipo.requiereJugador) {
            return "" to ""
        }

        /*
         * Si el evento corresponde al rival, actualmente
         * SportPro no tiene fichas de sus jugadores.
         *
         * Por ello conservamos únicamente el nombre
         * ingresado por el operador.
         */
        if (
            equipoEvento ==
            EquipoEvento.RIVAL
        ) {

            val nombre =
                nombreJugadorRival
                    .trim()

            if (nombre.isBlank()) {
                throw IllegalArgumentException(
                    "${tipo.nombre} requiere indicar el jugador rival"
                )
            }

            return "" to nombre
        }

        /*
         * Para nuestro equipo sí exigimos un jugador real
         * de la alineación confirmada.
         */
        if (idJugador.isBlank()) {
            throw IllegalArgumentException(
                "${tipo.nombre} requiere seleccionar un jugador"
            )
        }

        val idsAlineados =
            obtenerIdsAlineados(
                partido.id
            )

        if (
            idJugador !in
            idsAlineados
        ) {
            throw IllegalArgumentException(
                "El jugador seleccionado no pertenece a la alineación del partido"
            )
        }

        val jugador =
            jugadores.document(
                idJugador
            )
                .get()
                .await()
                .toObject(
                    Jugador::class.java
                )
                ?: throw IllegalStateException(
                    "El jugador seleccionado ya no existe"
                )

        if (!jugador.activo) {
            throw IllegalStateException(
                "El jugador seleccionado está inactivo"
            )
        }

        if (
            jugador.idEquipo !=
            partido.idEquipo
        ) {
            throw IllegalStateException(
                "El jugador ya no pertenece al equipo del partido"
            )
        }

        return jugador.id to
                jugador.nombreCompleto()
    }

    private fun validarObservacion(
        tipo: TipoEventoCatalogo,
        observacion: String
    ): String {

        if (!tipo.requiereObservacion) {
            return ""
        }

        val texto =
            observacion.trim()

        if (texto.isBlank()) {
            throw IllegalArgumentException(
                "${tipo.nombre} requiere una observación"
            )
        }

        return texto
    }

    suspend fun listarEventos(
        idPartido: String,
        uid: String
    ): Result<List<EventoPartido>> =
        intentar {

            val partido =
                obtenerPartido(idPartido)

            verificarOperador(
                partido = partido,
                uid = uid
            )

            /*
             * No usamos dos orderBy en Firestore para no
             * obligarnos innecesariamente a crear un índice.
             *
             * Para HU14 la cantidad de eventos de un partido
             * será pequeña, así que ordenamos en Kotlin.
             */
            partidos.document(
                idPartido
            )
                .collection(
                    "eventos"
                )
                .get()
                .await()
                .toObjects(
                    EventoPartido::class.java
                )
                .sortedWith(
                    compareBy<EventoPartido> {
                        it.minuto
                    }.thenBy {
                        it.fechaCaptura
                    }
                )
        }

    fun calcularMarcador(
        eventos: List<EventoPartido>
    ): MarcadorPartido {

        /*
         * Con el catálogo actual, cada evento marcado como
         * afectaMarcador representa una anotación.
         *
         * No dependemos del texto "Gol": usamos la regla
         * persistida en HU13/HU14.
         */
        val propio =
            eventos.count {
                it.afectaMarcador &&
                        it.equipoEvento ==
                        EquipoEvento.PROPIO
            }

        val rival =
            eventos.count {
                it.afectaMarcador &&
                        it.equipoEvento ==
                        EquipoEvento.RIVAL
            }

        return MarcadorPartido(
            propio = propio,
            rival = rival
        )
    }
}