package com.example.projectsportpro2026_2.datos.repositorio.seguimiento

import com.example.projectsportpro2026_2.datos.modelo.Academia
import com.example.projectsportpro2026_2.datos.modelo.Equipo
import com.example.projectsportpro2026_2.datos.modelo.Jugador
import com.example.projectsportpro2026_2.datos.modelo.PerfilUsuario
import com.example.projectsportpro2026_2.datos.modelo.Rol
import com.example.projectsportpro2026_2.datos.modelo.partido.EstadoPartido
import com.example.projectsportpro2026_2.datos.modelo.partido.Partido
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await

/*
 * HU17 - Actualización en tiempo real de marcador y cronología.
 *
 * Lectura (solo lectura) del partido para DT / JUG / PAD / ADM.
 * Valida que el usuario tenga relación con el equipo del
 * partido antes de suscribirse a sus datos (HU25).
 */
data class InfoSeguimiento(
    val partido: Partido,
    val nombreEquipo: String,
    val categoria: String,
    val nombreAcademia: String
)

data class EquipoVisible(
    val id: String,
    val nombre: String,
    val categoria: String,
    // Jugadores del usuario en ese equipo (JUG/PAD)
    val jugadores: List<String> = emptyList()
)

data class PartidoSeguible(
    val partido: Partido,
    val equipo: EquipoVisible
)

data class PartidosSeguibles(
    val partidos: List<PartidoSeguible> = emptyList(),
    val desdeCache: Boolean = false
)

class RepositorioSeguimientoPartido(
    private val bd: FirebaseFirestore =
        FirebaseFirestore.getInstance()
) {

    private val partidos = bd.collection("partidos")
    private val equipos = bd.collection("equipos")
    private val academias = bd.collection("academias")
    private val jugadores = bd.collection("jugadores")
    private val usuarios = bd.collection("usuarios")

    private suspend fun <T> intentar(
        bloque: suspend () -> T
    ): Result<T> =
        try {
            Result.success(bloque())
        } catch (e: Exception) {
            Result.failure(e)
        }

    private suspend fun obtenerPerfil(uid: String): PerfilUsuario {
        if (uid.isBlank()) {
            throw IllegalStateException("No existe una sesión activa")
        }

        return usuarios
            .document(uid)
            .get()
            .await()
            .toObject(PerfilUsuario::class.java)
            ?.copy(uid = uid)
            ?: throw IllegalStateException(
                "El usuario no tiene un perfil válido"
            )
    }

    /*
     * Jugadores relacionados con el usuario:
     * JUG → su propia ficha; PAD → sus jugadores vinculados.
     */
    private suspend fun jugadoresDelUsuario(
        perfil: PerfilUsuario
    ): List<Jugador> {
        val consulta =
            when (perfil.rol) {
                Rol.JUG ->
                    jugadores.whereEqualTo("idUsuario", perfil.uid)

                Rol.PAD ->
                    jugadores.whereArrayContains("idsApoderados", perfil.uid)

                else -> return emptyList()
            }

        return consulta
            .get()
            .await()
            .documents
            .mapNotNull { documento ->
                documento
                    .toObject(Jugador::class.java)
                    ?.copy(id = documento.id)
            }
            .filter { it.activo }
    }

    /*
     * Academias que el ADM/DT puede gestionar.
     */
    private suspend fun academiasGestionadas(
        uid: String
    ): List<Academia> {
        val comoAutorizado =
            academias
                .whereArrayContains("idsAutorizados", uid)
                .get()
                .await()
                .documents

        val comoAdministrador =
            academias
                .whereEqualTo("idAdministrador", uid)
                .get()
                .await()
                .documents

        return (comoAutorizado + comoAdministrador)
            .distinctBy { it.id }
            .mapNotNull { documento ->
                documento
                    .toObject(Academia::class.java)
                    ?.copy(id = documento.id)
            }
            .filter { it.activa }
    }

    /*
     * Control de acceso (HU25): solo se sigue un partido del
     * equipo del jugador / de los jugadores del apoderado /
     * de una academia que el ADM/DT gestiona.
     */
    private suspend fun verificarEspectador(
        partido: Partido,
        perfil: PerfilUsuario
    ) {
        val autorizado =
            when (perfil.rol) {
                Rol.ADM, Rol.DT -> {
                    val academia =
                        academias
                            .document(partido.idAcademia)
                            .get()
                            .await()
                            .toObject(Academia::class.java)

                    academia != null &&
                            (
                                    academia.idAdministrador == perfil.uid ||
                                            perfil.uid in academia.idsAutorizados
                                    )
                }

                Rol.JUG, Rol.PAD ->
                    jugadoresDelUsuario(perfil).any {
                        it.idEquipo == partido.idEquipo
                    }
            }

        if (!autorizado) {
            throw IllegalStateException(
                "Solo puedes seguir partidos de tu equipo o del equipo de tus jugadores"
            )
        }
    }

    suspend fun prepararSeguimiento(
        idPartido: String,
        uid: String
    ): Result<InfoSeguimiento> =
        intentar {
            if (idPartido.isBlank()) {
                throw IllegalArgumentException("El partido no es válido")
            }

            val documentoPartido =
                partidos.document(idPartido).get().await()

            val partido =
                documentoPartido
                    .toObject(Partido::class.java)
                    ?.copy(id = documentoPartido.id)
                    ?: throw IllegalStateException("El partido no existe")

            val perfil = obtenerPerfil(uid)

            verificarEspectador(partido, perfil)

            val equipo =
                equipos
                    .document(partido.idEquipo)
                    .get()
                    .await()
                    .toObject(Equipo::class.java)

            val academia =
                academias
                    .document(partido.idAcademia)
                    .get()
                    .await()
                    .toObject(Academia::class.java)

            InfoSeguimiento(
                partido = partido,
                nombreEquipo = equipo?.nombre ?: "Mi equipo",
                categoria = equipo?.categoria.orEmpty(),
                nombreAcademia = academia?.nombre.orEmpty()
            )
        }

    /*
     * Equipos cuyos partidos puede seguir el usuario.
     */
    suspend fun equiposVisibles(
        uid: String
    ): Result<Pair<Rol, List<EquipoVisible>>> =
        intentar {
            val perfil = obtenerPerfil(uid)

            val lista =
                when (perfil.rol) {
                    Rol.JUG, Rol.PAD -> {
                        val propios = jugadoresDelUsuario(perfil)

                        propios
                            .filter { it.idEquipo.isNotBlank() }
                            .groupBy { it.idEquipo }
                            .map { (idEquipo, jugadoresEquipo) ->
                                val equipo =
                                    equipos
                                        .document(idEquipo)
                                        .get()
                                        .await()
                                        .toObject(Equipo::class.java)

                                EquipoVisible(
                                    id = idEquipo,
                                    nombre = equipo?.nombre ?: "Equipo",
                                    categoria = equipo?.categoria.orEmpty(),
                                    jugadores = jugadoresEquipo.map {
                                        it.nombreCompleto()
                                    }
                                )
                            }
                    }

                    Rol.ADM, Rol.DT -> {
                        val idsAcademias =
                            academiasGestionadas(perfil.uid).map { it.id }

                        idsAcademias
                            .chunked(10)
                            .flatMap { grupo ->
                                equipos
                                    .whereIn("idAcademia", grupo)
                                    .get()
                                    .await()
                                    .documents
                                    .mapNotNull { documento ->
                                        documento
                                            .toObject(Equipo::class.java)
                                            ?.copy(id = documento.id)
                                    }
                            }
                            .filter { it.activo }
                            .map {
                                EquipoVisible(
                                    id = it.id,
                                    nombre = it.nombre,
                                    categoria = it.categoria
                                )
                            }
                    }
                }

            perfil.rol to lista
        }

    /*
     * Lista de partidos en tiempo real (se reordena sola
     * cuando un partido pasa a En curso / Finalizado).
     */
    fun escucharPartidos(
        equiposVisibles: List<EquipoVisible>
    ): Flow<PartidosSeguibles> {

        if (equiposVisibles.isEmpty()) {
            return flowOf(PartidosSeguibles())
        }

        val porId = equiposVisibles.associateBy { it.id }

        // whereIn admite como máximo 10 valores por consulta
        val flujos =
            equiposVisibles
                .map { it.id }
                .chunked(10)
                .map { grupo -> escucharGrupo(grupo, porId) }

        return combine(flujos) { partes ->
            PartidosSeguibles(
                partidos = partes
                    .flatMap { it.partidos }
                    .sortedWith(ordenLista),
                desdeCache = partes.any { it.desdeCache }
            )
        }
    }

    private fun escucharGrupo(
        idsEquipo: List<String>,
        porId: Map<String, EquipoVisible>
    ): Flow<PartidosSeguibles> =
        callbackFlow {
            val registro =
                partidos
                    .whereIn("idEquipo", idsEquipo)
                    .addSnapshotListener(
                        MetadataChanges.INCLUDE
                    ) { instantanea, error ->

                        if (error != null) {
                            close(error)
                            return@addSnapshotListener
                        }

                        if (instantanea == null) {
                            return@addSnapshotListener
                        }

                        val lista =
                            instantanea.documents.mapNotNull { documento ->
                                val partido =
                                    documento
                                        .toObject(Partido::class.java)
                                        ?.copy(id = documento.id)
                                        ?: return@mapNotNull null

                                val equipo =
                                    porId[partido.idEquipo]
                                        ?: return@mapNotNull null

                                PartidoSeguible(partido, equipo)
                            }

                        trySend(
                            PartidosSeguibles(
                                partidos = lista,
                                desdeCache = instantanea.metadata.isFromCache
                            )
                        )
                    }

            awaitClose { registro.remove() }
        }

    /*
     * En curso primero; luego programados (más cercano
     * primero); luego finalizados y cancelados (más reciente
     * primero).
     */
    private val ordenLista =
        Comparator<PartidoSeguible> { a, b ->
            val grupoA = grupo(a.partido.estado)
            val grupoB = grupo(b.partido.estado)

            if (grupoA != grupoB) {
                return@Comparator grupoA - grupoB
            }

            val fechaA = "${a.partido.fecha} ${a.partido.hora}"
            val fechaB = "${b.partido.fecha} ${b.partido.hora}"

            if (a.partido.estado == EstadoPartido.PROGRAMADO) {
                fechaA.compareTo(fechaB)
            } else {
                fechaB.compareTo(fechaA)
            }
        }

    private fun grupo(estado: EstadoPartido): Int =
        when (estado) {
            EstadoPartido.EN_CURSO -> 0
            EstadoPartido.PROGRAMADO -> 1
            EstadoPartido.FINALIZADO -> 2
            EstadoPartido.CANCELADO -> 3
        }
}
