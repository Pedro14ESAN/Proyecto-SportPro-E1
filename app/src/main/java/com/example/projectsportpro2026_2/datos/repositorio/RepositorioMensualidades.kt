package com.example.projectsportpro2026_2.datos.repositorio

import com.example.projectsportpro2026_2.datos.modelo.Academia
import com.example.projectsportpro2026_2.datos.modelo.EstadoMensualidad
import com.example.projectsportpro2026_2.datos.modelo.EstadoVinculacion
import com.example.projectsportpro2026_2.datos.modelo.Equipo
import com.example.projectsportpro2026_2.datos.modelo.Jugador
import com.example.projectsportpro2026_2.datos.modelo.Mensualidad
import com.example.projectsportpro2026_2.datos.modelo.PerfilUsuario
import com.example.projectsportpro2026_2.datos.modelo.Rol
import com.example.projectsportpro2026_2.datos.modelo.Vinculacion
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class RepositorioMensualidades(
    private val bd: FirebaseFirestore =
        FirebaseFirestore.getInstance()
) {

    private val mensualidades =
        bd.collection("mensualidades")

    private val jugadores =
        bd.collection("jugadores")

    private val equipos =
        bd.collection("equipos")

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

    private fun convertirJugador(
        documento: DocumentSnapshot
    ): Jugador? {

        val jugador =
            documento.toObject(
                Jugador::class.java
            ) ?: return null

        return jugador.copy(
            id = documento.id
        )
    }

    private suspend fun obtenerPerfil(
        uid: String
    ): PerfilUsuario {

        return usuarios
            .document(uid)
            .get()
            .await()
            .toObject(
                PerfilUsuario::class.java
            )
            ?: throw IllegalStateException(
                "Tu cuenta no tiene perfil"
            )
    }

    private suspend fun obtenerAcademiasAdministrables(
        uid: String
    ): List<Academia> {

        val comoAdministrador =
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
                        .toObject(
                            Academia::class.java
                        )
                        ?.copy(
                            id = documento.id
                        )
                }

        val comoAutorizado =
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
                        .toObject(
                            Academia::class.java
                        )
                        ?.copy(
                            id = documento.id
                        )
                }

        return (
                comoAdministrador +
                        comoAutorizado
                )
            .distinctBy {
                it.id
            }
    }

    private suspend fun verificarAdministrador(
        idAcademia: String,
        uid: String
    ): Academia {

        val perfil =
            obtenerPerfil(uid)

        if (perfil.rol != Rol.ADM) {
            throw IllegalStateException(
                "Solo un administrador puede modificar mensualidades"
            )
        }

        val documento =
            academias
                .document(idAcademia)
                .get()
                .await()

        val academia =
            documento
                .toObject(
                    Academia::class.java
                )
                ?.copy(
                    id = documento.id
                )
                ?: throw IllegalStateException(
                    "La academia no existe"
                )

        val tienePermiso =
            academia.idAdministrador ==
                    uid ||
                    uid in academia.idsAutorizados

        if (!tienePermiso) {
            throw IllegalStateException(
                "No tienes permiso sobre esta academia"
            )
        }

        if (!academia.activa) {
            throw IllegalStateException(
                "La academia está inactiva"
            )
        }

        return academia
    }

    private fun validarPeriodo(
        periodo: String
    ) {

        val formato =
            Regex(
                """^\d{4}-(0[1-9]|1[0-2])$"""
            )

        if (!formato.matches(periodo)) {
            throw IllegalArgumentException(
                "El periodo debe tener formato yyyy-MM"
            )
        }
    }

    private fun obtenerIdMensualidad(
        idJugador: String,
        periodo: String
    ): String {

        return "${idJugador}_$periodo"
    }

    suspend fun registrarOActualizar(
        idJugador: String,
        periodo: String,
        estado: EstadoMensualidad,
        uid: String
    ): Result<Mensualidad> = intentar {

        validarPeriodo(periodo)

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
                    "El jugador no existe"
                )

        if (!jugador.activo) {
            throw IllegalStateException(
                "El jugador está inactivo"
            )
        }

        var idAcademia =
            jugador.idAcademia

        if (idAcademia.isBlank() &&
            jugador.idEquipo.isNotBlank()
        ) {

            val equipo =
                equipos
                    .document(
                        jugador.idEquipo
                    )
                    .get()
                    .await()
                    .toObject(
                        Equipo::class.java
                    )

            idAcademia =
                equipo
                    ?.idAcademia
                    .orEmpty()
        }

        if (idAcademia.isBlank()) {
            throw IllegalStateException(
                "El jugador no pertenece a una academia"
            )
        }

        val academia =
            verificarAdministrador(
                idAcademia =
                    idAcademia,
                uid =
                    uid
            )

        val id =
            obtenerIdMensualidad(
                idJugador =
                    jugador.id,
                periodo =
                    periodo
            )

        val mensualidad =
            Mensualidad(
                id =
                    id,
                idJugador =
                    jugador.id,
                nombreJugador =
                    jugador.nombreCompleto(),
                idAcademia =
                    idAcademia,
                nombreAcademia =
                    academia.nombre,
                idEquipo =
                    jugador.idEquipo,
                periodo =
                    periodo,
                estado =
                    estado,
                actualizadoPor =
                    uid,
                fechaActualizacion =
                    System.currentTimeMillis()
            )

        mensualidades
            .document(id)
            .set(mensualidad)
            .await()

        mensualidad
    }

    suspend fun listarJugadoresAdministrables(
        uid: String
    ): Result<List<Jugador>> = intentar {

        val perfil =
            obtenerPerfil(uid)

        if (perfil.rol != Rol.ADM) {
            throw IllegalStateException(
                "Solo el administrador puede gestionar mensualidades"
            )
        }

        val academiasPermitidas =
            obtenerAcademiasAdministrables(
                uid
            )
                .filter {
                    it.activa
                }

        val idsAcademias =
            academiasPermitidas
                .map {
                    it.id
                }
                .filter {
                    it.isNotBlank()
                }
                .distinct()

        if (idsAcademias.isEmpty()) {
            return@intentar emptyList()
        }

        val jugadoresPorAcademia =
            idsAcademias
                .chunked(10)
                .flatMap { grupo ->

                    jugadores
                        .whereIn(
                            "idAcademia",
                            grupo
                        )
                        .get()
                        .await()
                        .documents
                        .mapNotNull {
                            convertirJugador(it)
                        }
                }

        val equiposPermitidos =
            idsAcademias
                .chunked(10)
                .flatMap { grupo ->

                    equipos
                        .whereIn(
                            "idAcademia",
                            grupo
                        )
                        .get()
                        .await()
                        .documents
                        .mapNotNull { documento ->

                            documento
                                .toObject(
                                    Equipo::class.java
                                )
                                ?.copy(
                                    id = documento.id
                                )
                        }
                }

        val idsEquipos =
            equiposPermitidos
                .map {
                    it.id
                }
                .filter {
                    it.isNotBlank()
                }
                .distinct()

        val jugadoresPorEquipo =
            if (idsEquipos.isEmpty()) {

                emptyList()

            } else {

                idsEquipos
                    .chunked(10)
                    .flatMap { grupo ->

                        jugadores
                            .whereIn(
                                "idEquipo",
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

        (
                jugadoresPorAcademia +
                        jugadoresPorEquipo
                )
            .distinctBy {
                it.id
            }
            .filter {
                it.activo
            }
            .sortedBy {
                it.nombreCompleto()
                    .lowercase()
            }
    }

    suspend fun listarParaUsuario(
        uid: String
    ): Result<List<Mensualidad>> = intentar {

        val perfil =
            obtenerPerfil(uid)

        val resultado =
            when (perfil.rol) {

                Rol.ADM ->
                    listarParaAdministrador(
                        uid
                    )

                Rol.JUG ->
                    listarParaJugador(
                        uid
                    )

                Rol.PAD ->
                    listarParaApoderado(
                        uid
                    )

                Rol.DT ->
                    throw IllegalStateException(
                        "El director técnico no tiene acceso a mensualidades"
                    )
            }

        resultado
            .distinctBy {
                it.id
            }
            .sortedWith(
                compareByDescending<Mensualidad> {
                    it.periodo
                }.thenBy {
                    it.nombreJugador
                }
            )
    }

    private suspend fun listarParaAdministrador(
        uid: String
    ): List<Mensualidad> {

        val idsAcademias =
            obtenerAcademiasAdministrables(
                uid
            )
                .map {
                    it.id
                }
                .filter {
                    it.isNotBlank()
                }
                .distinct()

        if (idsAcademias.isEmpty()) {
            return emptyList()
        }

        return idsAcademias
            .chunked(10)
            .flatMap { grupo ->

                mensualidades
                    .whereIn(
                        "idAcademia",
                        grupo
                    )
                    .get()
                    .await()
                    .toObjects(
                        Mensualidad::class.java
                    )
            }
            .distinctBy {
                it.id
            }
    }

    private suspend fun listarParaJugador(
        uid: String
    ): List<Mensualidad> {

        val fichas =
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

        val idsJugadores =
            fichas
                .map {
                    it.id
                }
                .distinct()

        return listarPorJugadores(
            idsJugadores
        )
    }

    private suspend fun listarParaApoderado(
        uid: String
    ): List<Mensualidad> {

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

        val idsJugadores =
            relaciones
                .map {
                    it.idJugador
                }
                .filter {
                    it.isNotBlank()
                }
                .distinct()

        return listarPorJugadores(
            idsJugadores
        )
    }

    private suspend fun listarPorJugadores(
        idsJugadores: List<String>
    ): List<Mensualidad> {

        if (idsJugadores.isEmpty()) {
            return emptyList()
        }

        return idsJugadores
            .chunked(10)
            .flatMap { grupo ->

                mensualidades
                    .whereIn(
                        "idJugador",
                        grupo
                    )
                    .get()
                    .await()
                    .toObjects(
                        Mensualidad::class.java
                    )
            }
            .distinctBy {
                it.id
            }
    }
}