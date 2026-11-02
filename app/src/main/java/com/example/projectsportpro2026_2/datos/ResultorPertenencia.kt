package com.example.projectsportpro2026_2.datos.repositorio

import com.example.projectsportpro2026_2.datos.modelo.Academia
import com.example.projectsportpro2026_2.datos.modelo.Equipo
import com.example.projectsportpro2026_2.datos.modelo.EstadoVinculacion
import com.example.projectsportpro2026_2.datos.modelo.Jugador
import com.example.projectsportpro2026_2.datos.modelo.Rol
import com.example.projectsportpro2026_2.datos.modelo.Vinculacion
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// A qué academias y equipos pertenece un usuario.
// Lo comparten Anuncios (HU19), Comunidad (HU21-HU23) y Notificaciones (HU20).
data class Pertenencia(
    val academias: List<Academia> = emptyList(),
    val equipos: List<Equipo> = emptyList(),
    val idsAcademiasGestionadas: Set<String> = emptySet()   // donde es ADM o autorizado
) {
    val idsAcademias: Set<String>
        get() = academias.map { it.id }.toSet()

    val idsEquipos: Set<String>
        get() = equipos.map { it.id }.toSet()
}

class ResolutorPertenencia(
    bd: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val academias = bd.collection("academias")
    private val equipos = bd.collection("equipos")
    private val jugadores = bd.collection("jugadores")
    private val vinculaciones = bd.collection("vinculaciones")

    private fun convertirAcademia(d: DocumentSnapshot) = d.toObject(Academia::class.java)?.copy(id = d.id)
    private fun convertirEquipo(d: DocumentSnapshot) = d.toObject(Equipo::class.java)?.copy(id = d.id)
    private fun convertirJugador(d: DocumentSnapshot) = d.toObject(Jugador::class.java)?.copy(id = d.id)

    // Firestore solo acepta hasta 10 valores en whereIn, por eso se parte en grupos
    private suspend fun buscarPorIds(
        coleccion: CollectionReference,
        ids: Collection<String>
    ): List<DocumentSnapshot> =
        ids.filter { it.isNotBlank() }.distinct().chunked(10).flatMap { grupo ->
            coleccion.whereIn(FieldPath.documentId(), grupo).get().await().documents
        }

    // Academias donde el usuario es administrador o está autorizado
    suspend fun academiasQueGestiona(uid: String): List<Academia> {
        val comoAdmin = academias.whereEqualTo("idAdministrador", uid).get().await().documents
        val comoAutorizado = academias.whereArrayContains("idsAutorizados", uid).get().await().documents
        return (comoAdmin + comoAutorizado)
            .mapNotNull { convertirAcademia(it) }
            .distinctBy { it.id }
            .filter { it.activa }
    }

    suspend fun equiposDelDt(uid: String): List<Equipo> =
        equipos.whereEqualTo("idEntrenador", uid).get().await().documents
            .mapNotNull { convertirEquipo(it) }
            .filter { it.activo }

    suspend fun obtener(uid: String, rol: Rol): Pertenencia {

        // Fichas de jugador: la propia (JUG) o las de sus hijos vinculados (PAD)
        val fichas: List<Jugador> = when (rol) {
            Rol.JUG -> jugadores.whereEqualTo("idUsuario", uid).get().await().documents
                .mapNotNull { convertirJugador(it) }

            Rol.PAD -> {
                val idsJugadores = vinculaciones.whereEqualTo("idApoderado", uid).get().await()
                    .toObjects(Vinculacion::class.java)
                    .filter { it.estado == EstadoVinculacion.ACTIVA }
                    .map { it.idJugador }
                buscarPorIds(jugadores, idsJugadores).mapNotNull { convertirJugador(it) }
            }

            else -> emptyList()
        }

        // Equipos: los que dirige (DT) + los de sus fichas (JUG / PAD)
        val equiposDt = if (rol == Rol.DT) equiposDelDt(uid) else emptyList()
        val idsEquiposDt = equiposDt.map { it.id }.toSet()
        val equiposFichas = buscarPorIds(
            equipos,
            fichas.map { it.idEquipo }.filter { it !in idsEquiposDt }
        ).mapNotNull { convertirEquipo(it) }.filter { it.activo }
        val todosLosEquipos = equiposDt + equiposFichas

        // Academias: las que gestiona + las de sus equipos y fichas
        val gestionadas = academiasQueGestiona(uid)
        val idsGestionadas = gestionadas.map { it.id }.toSet()
        val otrasAcademias = buscarPorIds(
            academias,
            (fichas.map { it.idAcademia } + todosLosEquipos.map { it.idAcademia })
                .filter { it.isNotBlank() && it !in idsGestionadas }
        ).mapNotNull { convertirAcademia(it) }.filter { it.activa }

        return Pertenencia(
            academias = gestionadas + otrasAcademias,
            equipos = todosLosEquipos,
            idsAcademiasGestionadas = idsGestionadas
        )
    }
}