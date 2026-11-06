package com.example.projectsportpro2026_2.datos.seguridad

import com.example.projectsportpro2026_2.datos.modelo.EstadoCuenta
import com.example.projectsportpro2026_2.datos.modelo.EstadoVinculacion
import com.example.projectsportpro2026_2.datos.modelo.Jugador
import com.example.projectsportpro2026_2.datos.modelo.PerfilUsuario
import com.example.projectsportpro2026_2.datos.modelo.Rol
import com.example.projectsportpro2026_2.datos.modelo.Vinculacion
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/*
 * HU25 - Flujo principal, paso 2: la app verifica sesión,
 * rol y relación del usuario antes de pedir un dato protegido.
 *
 * Las relaciones se leen siempre del servidor/caché actual,
 * nunca se guardan "para siempre": si la academia revoca un
 * vínculo PAD o retira al DT de un equipo, en la siguiente
 * consulta el permiso ya no existe.
 */
class RepositorioPermisos(
    private val bd: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val usuarios = bd.collection("usuarios")
    private val academias = bd.collection("academias")
    private val equipos = bd.collection("equipos")
    private val jugadores = bd.collection("jugadores")
    private val vinculaciones = bd.collection("vinculaciones")

    suspend fun perfil(uid: String): PerfilUsuario {
        if (uid.isBlank()) {
            throw AccesoDenegadoException(MensajesPermiso.SIN_SESION)
        }

        val perfil =
            usuarios.document(uid).get().await()
                .toObject(PerfilUsuario::class.java)
                ?.copy(uid = uid)
                ?: throw AccesoDenegadoException()

        // Una cuenta pendiente de autorización no ve datos protegidos
        if (perfil.estado == EstadoCuenta.PENDIENTE) {
            throw AccesoDenegadoException(
                "Tu cuenta aún no fue autorizada por la academia."
            )
        }

        return perfil
    }

    suspend fun contexto(uid: String): ContextoAcceso {
        val perfil = perfil(uid)

        return when (perfil.rol) {

            Rol.ADM -> {
                val ids = academiasGestionadas(uid)
                ContextoAcceso(
                    perfil = perfil,
                    idsAcademiasAdministradas = ids
                )
            }

            Rol.DT -> {
                val dirigidos =
                    equipos.whereEqualTo("idEntrenador", uid).get().await()
                        .documents
                        .filter { it.getBoolean("activo") != false }
                        .map { it.id }
                        .toSet()

                ContextoAcceso(
                    perfil = perfil,
                    idsEquiposDirigidos = dirigidos,
                    idsAcademiasComoDt = academiasGestionadas(uid)
                )
            }

            Rol.JUG -> {
                val fichas =
                    jugadores.whereEqualTo("idUsuario", uid).get().await()
                        .documents
                        .mapNotNull { convertirJugador(it) }
                        .filter { it.activo }

                ContextoAcceso(
                    perfil = perfil,
                    idsFichasPropias = fichas.map { it.id }.toSet(),
                    idsEquiposDeFichas = fichas.map { it.idEquipo }
                        .filter { it.isNotBlank() }
                        .toSet()
                )
            }

            Rol.PAD -> {
                // Solo vínculos ACTIVOS: perder el vínculo revoca permisos
                val idsVinculados =
                    vinculaciones.whereEqualTo("idApoderado", uid).get().await()
                        .toObjects(Vinculacion::class.java)
                        .filter { it.estado == EstadoVinculacion.ACTIVA }
                        .map { it.idJugador }
                        .filter { it.isNotBlank() }
                        .toSet()

                val fichas =
                    idsVinculados.chunked(10).flatMap { grupo ->
                        jugadores.whereIn(FieldPath.documentId(), grupo)
                            .get().await().documents
                            .mapNotNull { convertirJugador(it) }
                    }.filter { it.activo }

                ContextoAcceso(
                    perfil = perfil,
                    idsJugadoresVinculados = fichas.map { it.id }.toSet(),
                    idsEquiposDeFichas = fichas.map { it.idEquipo }
                        .filter { it.isNotBlank() }
                        .toSet()
                )
            }
        }
    }

    // Academias activas donde el usuario es administrador o autorizado
    private suspend fun academiasGestionadas(uid: String): Set<String> {
        val comoAdmin =
            academias.whereEqualTo("idAdministrador", uid).get().await().documents
        val comoAutorizado =
            academias.whereArrayContains("idsAutorizados", uid).get().await().documents

        return (comoAdmin + comoAutorizado)
            .filter { it.getBoolean("activa") != false }
            .map { it.id }
            .toSet()
    }

    private fun convertirJugador(d: DocumentSnapshot): Jugador? =
        d.toObject(Jugador::class.java)?.copy(id = d.id)
}
