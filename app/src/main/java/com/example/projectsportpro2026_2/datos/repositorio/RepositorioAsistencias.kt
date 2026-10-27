package com.example.projectsportpro2026_2.datos.repositorio

import com.example.projectsportpro2026_2.datos.modelo.Academia
import com.example.projectsportpro2026_2.datos.modelo.Asistencia
import com.example.projectsportpro2026_2.datos.modelo.Entrenamiento
import com.example.projectsportpro2026_2.datos.modelo.EstadoAsistencia
import com.example.projectsportpro2026_2.datos.modelo.EstadoEntrenamiento
import com.example.projectsportpro2026_2.datos.modelo.EstadoVinculacion
import com.example.projectsportpro2026_2.datos.modelo.Jugador
import com.example.projectsportpro2026_2.datos.modelo.PerfilUsuario
import com.example.projectsportpro2026_2.datos.modelo.Rol
import com.example.projectsportpro2026_2.datos.modelo.Vinculacion
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class RepositorioAsistencias(
    private val bd: FirebaseFirestore =
        FirebaseFirestore.getInstance()
) {

    private val asistencias =
        bd.collection("asistencias")

    private val entrenamientos =
        bd.collection("entrenamientos")

    private val jugadores =
        bd.collection("jugadores")

    private val academias =
        bd.collection("academias")

    private val usuarios =
        bd.collection("usuarios")

    private val vinculaciones =
        bd.collection("vinculaciones")

    private suspend fun <T> intentar(
        bloque: suspend () -> T
    ): Result<T> {
        return try {
            Result.success(bloque())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun obtenerPerfil(
        uid: String
    ): PerfilUsuario {

        return usuarios
            .document(uid)
            .get()
            .await()
            .toObject(PerfilUsuario::class.java)
            ?: throw IllegalStateException(
                "Tu cuenta no tiene perfil"
            )
    }

    private fun convertirEntrenamiento(
        documento: DocumentSnapshot
    ): Entrenamiento? {

        return documento
            .toObject(Entrenamiento::class.java)
            ?.copy(
                id = documento.id
            )
    }

    private fun convertirJugador(
        documento: DocumentSnapshot
    ): Jugador? {

        return documento
            .toObject(Jugador::class.java)
            ?.copy(
                id = documento.id
            )
    }

    private fun convertirAsistencia(
        documento: DocumentSnapshot
    ): Asistencia? {

        return documento
            .toObject(Asistencia::class.java)
            ?.copy(
                id = documento.id
            )
    }

    private suspend fun academiasAdministrables(
        uid: String
    ): List<Academia> {

        val principal =
            academias
                .whereEqualTo(
                    "idAdministrador",
                    uid
                )
                .get()
                .await()
                .documents
                .mapNotNull { documento ->

                    documento
                        .toObject(Academia::class.java)
                        ?.copy(
                            id = documento.id
                        )
                }

        val autorizadas =
            academias
                .whereArrayContains(
                    "idsAutorizados",
                    uid
                )
                .get()
                .await()
                .documents
                .mapNotNull { documento ->

                    documento
                        .toObject(Academia::class.java)
                        ?.copy(
                            id = documento.id
                        )
                }

        return (
                principal +
                        autorizadas
                )
            .distinctBy {
                it.id
            }
    }

    private suspend fun verificarSesion(
        idEntrenamiento: String,
        uid: String
    ): Entrenamiento {

        val perfil =
            obtenerPerfil(uid)

        if (
            perfil.rol != Rol.DT &&
            perfil.rol != Rol.ADM
        ) {
            throw IllegalStateException(
                "No tienes permisos para administrar asistencia"
            )
        }

        val entrenamiento =
            entrenamientos
                .document(idEntrenamiento)
                .get()
                .await()
                .let {
                    convertirEntrenamiento(it)
                }
                ?: throw IllegalStateException(
                    "El entrenamiento no existe"
                )

        if (
            perfil.rol ==
            Rol.DT
        ) {

            if (
                entrenamiento.idEntrenador !=
                uid
            ) {
                throw IllegalStateException(
                    "No administras esta sesión"
                )
            }

        } else {

            val idsAcademias =
                academiasAdministrables(uid)
                    .map {
                        it.id
                    }

            if (
                entrenamiento.idAcademia !in
                idsAcademias
            ) {
                throw IllegalStateException(
                    "No administras esta academia"
                )
            }
        }

        return entrenamiento
    }

    suspend fun listarSesionesGestionables(
        uid: String
    ): Result<List<Entrenamiento>> =
        intentar {

            val perfil =
                obtenerPerfil(uid)

            val lista =
                when (
                    perfil.rol
                ) {

                    Rol.DT -> {

                        entrenamientos
                            .whereEqualTo(
                                "idEntrenador",
                                uid
                            )
                            .get()
                            .await()
                            .documents
                            .mapNotNull {
                                convertirEntrenamiento(it)
                            }
                    }

                    Rol.ADM -> {

                        val idsAcademias =
                            academiasAdministrables(uid)
                                .map {
                                    it.id
                                }

                        if (
                            idsAcademias.isEmpty()
                        ) {

                            emptyList()

                        } else {

                            idsAcademias
                                .chunked(10)
                                .flatMap { grupo ->

                                    entrenamientos
                                        .whereIn(
                                            "idAcademia",
                                            grupo
                                        )
                                        .get()
                                        .await()
                                        .documents
                                        .mapNotNull {
                                            convertirEntrenamiento(it)
                                        }
                                }
                        }
                    }

                    else ->
                        throw IllegalStateException(
                            "No tienes acceso al registro de asistencia"
                        )
                }

            lista
                .distinctBy {
                    it.id
                }
                .sortedByDescending {
                    it.fechaHoraEpochMillis
                }
        }

    suspend fun listarJugadoresSesion(
        idEntrenamiento: String,
        uid: String
    ): Result<List<Jugador>> =
        intentar {

            val entrenamiento =
                verificarSesion(
                    idEntrenamiento,
                    uid
                )

            jugadores
                .whereEqualTo(
                    "idEquipo",
                    entrenamiento.idEquipo
                )
                .get()
                .await()
                .documents
                .mapNotNull {
                    convertirJugador(it)
                }
                .filter {
                    it.activo
                }
                .sortedBy {
                    it.nombreCompleto()
                        .lowercase()
                }
        }

    suspend fun listarAsistenciasSesion(
        idEntrenamiento: String,
        uid: String
    ): Result<List<Asistencia>> =
        intentar {

            verificarSesion(
                idEntrenamiento,
                uid
            )

            asistencias
                .whereEqualTo(
                    "idEntrenamiento",
                    idEntrenamiento
                )
                .get()
                .await()
                .documents
                .mapNotNull {
                    convertirAsistencia(it)
                }
        }

    suspend fun guardarAsistencias(
        idEntrenamiento: String,
        estados: Map<String, EstadoAsistencia>,
        uid: String,
        permitirCorreccion: Boolean
    ): Result<Unit> =
        intentar {

            val entrenamiento =
                verificarSesion(
                    idEntrenamiento,
                    uid
                )

            if (
                entrenamiento.estado ==
                EstadoEntrenamiento.CANCELADO
            ) {
                throw IllegalStateException(
                    "No se puede registrar asistencia en una sesión cancelada"
                )
            }

            if (
                estados.isEmpty()
            ) {
                throw IllegalArgumentException(
                    "Registra la asistencia de los jugadores"
                )
            }

            val existentes =
                asistencias
                    .whereEqualTo(
                        "idEntrenamiento",
                        idEntrenamiento
                    )
                    .get()
                    .await()
                    .documents

            if (
                existentes.isNotEmpty() &&
                !permitirCorreccion
            ) {
                throw IllegalStateException(
                    "La asistencia de esta sesión ya fue registrada. Usa Corregir asistencia."
                )
            }

            if (
                existentes.isEmpty() &&
                permitirCorreccion
            ) {
                throw IllegalStateException(
                    "Todavía no existe una asistencia registrada para corregir"
                )
            }

            val lote =
                bd.batch()

            estados.forEach { entrada ->

                val idJugador =
                    entrada.key

                val estado =
                    entrada.value

                val documentoJugador =
                    jugadores
                        .document(idJugador)
                        .get()
                        .await()

                val jugador =
                    convertirJugador(
                        documentoJugador
                    )
                        ?: throw IllegalStateException(
                            "Uno de los jugadores no existe"
                        )

                if (
                    jugador.idEquipo !=
                    entrenamiento.idEquipo
                ) {
                    throw IllegalStateException(
                        "${jugador.nombreCompleto()} no pertenece al equipo de esta sesión"
                    )
                }

                val id =
                    "${idEntrenamiento}_$idJugador"

                val referencia =
                    asistencias
                        .document(id)

                val existente =
                    referencia
                        .get()
                        .await()
                        .let {
                            convertirAsistencia(it)
                        }

                val ahora =
                    System.currentTimeMillis()

                val asistencia =
                    Asistencia(
                        id =
                            id,
                        idEntrenamiento =
                            entrenamiento.id,
                        idJugador =
                            jugador.id,
                        nombreJugador =
                            jugador.nombreCompleto(),
                        idEquipo =
                            entrenamiento.idEquipo,
                        nombreEquipo =
                            entrenamiento.nombreEquipo,
                        fechaEntrenamiento =
                            entrenamiento.fecha,
                        estado =
                            estado,
                        registradoPor =
                            existente
                                ?.registradoPor
                                ?: uid,
                        fechaRegistro =
                            existente
                                ?.fechaRegistro
                                ?: ahora,
                        actualizadoPor =
                            uid,
                        fechaActualizacion =
                            ahora
                    )

                lote.set(
                    referencia,
                    asistencia
                )
            }

            lote.commit()
                .await()
        }

    suspend fun listarJugadoresConsulta(
        uid: String
    ): Result<List<Jugador>> =
        intentar {

            val perfil =
                obtenerPerfil(uid)

            when (
                perfil.rol
            ) {

                Rol.JUG -> {

                    jugadores
                        .whereEqualTo(
                            "idUsuario",
                            uid
                        )
                        .get()
                        .await()
                        .documents
                        .mapNotNull {
                            convertirJugador(it)
                        }
                }

                Rol.PAD -> {

                    val relaciones =
                        vinculaciones
                            .whereEqualTo(
                                "idApoderado",
                                uid
                            )
                            .get()
                            .await()
                            .toObjects(
                                Vinculacion::class.java
                            )
                            .filter {
                                it.estado ==
                                        EstadoVinculacion.ACTIVA
                            }

                    val ids =
                        relaciones
                            .map {
                                it.idJugador
                            }
                            .distinct()

                    if (
                        ids.isEmpty()
                    ) {

                        emptyList()

                    } else {

                        ids
                            .chunked(10)
                            .flatMap { grupo ->

                                jugadores
                                    .whereIn(
                                        "id",
                                        grupo
                                    )
                                    .get()
                                    .await()
                                    .documents
                                    .mapNotNull {
                                        convertirJugador(it)
                                    }
                            }
                    }
                }

                else ->
                    emptyList()
            }
        }

    suspend fun listarHistorialJugador(
        idJugador: String,
        uid: String
    ): Result<List<Asistencia>> =
        intentar {

            val perfil =
                obtenerPerfil(uid)

            when (
                perfil.rol
            ) {

                Rol.JUG -> {

                    val ficha =
                        jugadores
                            .document(idJugador)
                            .get()
                            .await()
                            .let {
                                convertirJugador(it)
                            }

                    if (
                        ficha?.idUsuario !=
                        uid
                    ) {
                        throw IllegalStateException(
                            "Solo puedes consultar tu propio historial"
                        )
                    }
                }

                Rol.PAD -> {

                    val permitido =
                        vinculaciones
                            .whereEqualTo(
                                "idApoderado",
                                uid
                            )
                            .get()
                            .await()
                            .toObjects(
                                Vinculacion::class.java
                            )
                            .any {
                                it.idJugador ==
                                        idJugador &&
                                        it.estado ==
                                        EstadoVinculacion.ACTIVA
                            }

                    if (!permitido) {
                        throw IllegalStateException(
                            "No estás vinculado a este jugador"
                        )
                    }
                }

                Rol.DT,
                Rol.ADM -> Unit
            }

            asistencias
                .whereEqualTo(
                    "idJugador",
                    idJugador
                )
                .get()
                .await()
                .documents
                .mapNotNull {
                    convertirAsistencia(it)
                }
                .sortedByDescending {
                    it.fechaEntrenamiento
                }
        }
}