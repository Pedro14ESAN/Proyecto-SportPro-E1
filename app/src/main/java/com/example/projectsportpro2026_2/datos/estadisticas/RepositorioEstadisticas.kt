package com.example.projectsportpro2026_2.datos.estadisticas

import com.example.projectsportpro2026_2.datos.modelo.Asistencia
import com.example.projectsportpro2026_2.datos.modelo.Entrenamiento
import com.example.projectsportpro2026_2.datos.modelo.Equipo
import com.example.projectsportpro2026_2.datos.modelo.Jugador
import com.example.projectsportpro2026_2.datos.modelo.Rol
import com.example.projectsportpro2026_2.datos.modelo.alineacion.JugadorAlineacion
import com.example.projectsportpro2026_2.datos.modelo.evento.EventoPartido
import com.example.projectsportpro2026_2.datos.modelo.partido.EstadoPartido
import com.example.projectsportpro2026_2.datos.modelo.partido.Partido
import com.example.projectsportpro2026_2.datos.seguridad.AccesoDenegadoException
import com.example.projectsportpro2026_2.datos.seguridad.ContextoAcceso
import com.example.projectsportpro2026_2.datos.seguridad.ControlAcceso
import com.example.projectsportpro2026_2.datos.seguridad.NivelAcceso
import com.example.projectsportpro2026_2.datos.seguridad.RepositorioPermisos
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await

/*
 * HU18 - Lectura de la fuente de las estadísticas.
 *
 * Primero se verifica la relación del usuario con el equipo
 * (HU25) y solo después se leen partidos, eventos,
 * entrenamientos y asistencias.
 */

// Un jugador del plantel con el nivel de acceso que tiene quien consulta
data class JugadorEstadistico(
    val jugador: Jugador,
    val nivel: NivelAcceso
)

data class DatosEstadisticasEquipo(
    val equipo: Equipo,
    val fuente: DatosFuenteEquipo,
    val jugadores: List<JugadorEstadistico>,
    // false si las reglas no dejaron leer la asistencia de todo el equipo
    val asistenciaCompleta: Boolean
)

class RepositorioEstadisticas(
    private val bd: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val permisos: RepositorioPermisos = RepositorioPermisos(bd)
) {

    private val equipos = bd.collection("equipos")
    private val partidos = bd.collection("partidos")
    private val jugadores = bd.collection("jugadores")
    private val alineaciones = bd.collection("alineaciones")
    private val entrenamientos = bd.collection("entrenamientos")
    private val asistencias = bd.collection("asistencias")

    private suspend fun <T> intentar(bloque: suspend () -> T): Result<T> =
        try {
            Result.success(bloque())
        } catch (e: Exception) {
            Result.failure(e)
        }

    private fun convertirEquipo(d: DocumentSnapshot) =
        d.toObject(Equipo::class.java)?.copy(id = d.id)

    // Equipos cuyas estadísticas puede consultar el usuario
    suspend fun equiposVisibles(uid: String): Result<Pair<ContextoAcceso, List<Equipo>>> =
        intentar {
            val contexto = permisos.contexto(uid)

            val idsAcademias =
                contexto.idsAcademiasAdministradas + contexto.idsAcademiasComoDt

            val porAcademia =
                idsAcademias.chunked(10).flatMap { grupo ->
                    equipos.whereIn("idAcademia", grupo).get().await()
                        .documents.mapNotNull { convertirEquipo(it) }
                }

            val idsDirectos =
                (contexto.idsEquiposDirigidos + contexto.idsEquiposDeFichas) -
                        porAcademia.map { it.id }.toSet()

            val directos =
                idsDirectos.chunked(10).flatMap { grupo ->
                    equipos.whereIn(FieldPath.documentId(), grupo).get().await()
                        .documents.mapNotNull { convertirEquipo(it) }
                }

            val lista = (porAcademia + directos)
                .filter { it.activo }
                .distinctBy { it.id }
                .sortedBy { it.nombre }

            contexto to lista
        }

    suspend fun cargarEquipo(
        contexto: ContextoAcceso,
        equipo: Equipo
    ): Result<DatosEstadisticasEquipo> = intentar {

        // HU25: se valida la relación antes de leer nada
        if (!ControlAcceso.puedeVerEquipo(contexto, equipo.id, equipo.idAcademia)) {
            throw AccesoDenegadoException()
        }

        coroutineScope {

            val partidosEquipo = async {
                partidos.whereEqualTo("idEquipo", equipo.id).get().await()
                    .documents.mapNotNull { d ->
                        d.toObject(Partido::class.java)?.copy(id = d.id)
                    }
            }

            val sesiones = async {
                entrenamientos.whereEqualTo("idEquipo", equipo.id).get().await()
                    .documents.mapNotNull { d ->
                        d.toObject(Entrenamiento::class.java)?.copy(id = d.id)
                    }
            }

            val plantel = async {
                jugadores.whereEqualTo("idEquipo", equipo.id).get().await()
                    .documents.mapNotNull { d ->
                        d.toObject(Jugador::class.java)?.copy(id = d.id)
                    }
                    .filter { it.activo }
                    .sortedBy { it.apellidos }
            }

            val listaPartidos = partidosEquipo.await()
            val jugados = listaPartidos.filter { it.estado == EstadoPartido.FINALIZADO }

            // Eventos y alineación de cada partido jugado, en paralelo
            val eventosPorPartido = jugados.map { partido ->
                async {
                    partido.id to partidos.document(partido.id).collection("eventos")
                        .get().await()
                        .documents.mapNotNull { d ->
                            d.toObject(EventoPartido::class.java)?.copy(id = d.id)
                        }
                }
            }.awaitAll().toMap()

            val alineados = jugados.map { partido ->
                async {
                    partido.id to try {
                        alineaciones.document(partido.id).collection("jugadores")
                            .get().await()
                            .toObjects(JugadorAlineacion::class.java)
                            .map { it.idJugador }
                            .toSet()
                    } catch (e: Exception) {
                        emptySet()
                    }
                }
            }.awaitAll().toMap()

            val listaJugadores = plantel.await()
                .map { JugadorEstadistico(it, ControlAcceso.nivelSobreJugador(contexto, it)) }
                .filter { it.nivel != NivelAcceso.NINGUNO }

            val (listaAsistencias, completa) = leerAsistencias(contexto, equipo, listaJugadores)

            DatosEstadisticasEquipo(
                equipo = equipo,
                fuente = DatosFuenteEquipo(
                    partidos = listaPartidos,
                    eventosPorPartido = eventosPorPartido,
                    alineadosPorPartido = alineados,
                    entrenamientos = sesiones.await(),
                    asistencias = listaAsistencias
                ),
                jugadores = listaJugadores,
                asistenciaCompleta = completa
            )
        }
    }

    /*
     * La asistencia es dato sensible: ADM y DT leen la de todo el
     * equipo; JUG y PAD solo la de sus fichas (las reglas del
     * servidor rechazan cualquier otra consulta).
     */
    private suspend fun leerAsistencias(
        contexto: ContextoAcceso,
        equipo: Equipo,
        plantel: List<JugadorEstadistico>
    ): Pair<List<Asistencia>, Boolean> {

        val gestiona =
            equipo.id in contexto.idsEquiposDirigidos ||
                    equipo.idAcademia in contexto.idsAcademiasAdministradas ||
                    (contexto.rol == Rol.DT && equipo.idAcademia in contexto.idsAcademiasComoDt)

        return try {
            if (gestiona) {
                asistencias.whereEqualTo("idEquipo", equipo.id).get().await()
                    .toObjects(Asistencia::class.java) to true
            } else {
                val propios = plantel
                    .filter { it.nivel == NivelAcceso.COMPLETO }
                    .map { it.jugador.id }

                propios.flatMap { idJugador ->
                    asistencias.whereEqualTo("idJugador", idJugador).get().await()
                        .toObjects(Asistencia::class.java)
                        .filter { it.idEquipo == equipo.id }
                } to false
            }
        } catch (e: Exception) {
            // Sin permiso o sin conexión: la asistencia se muestra "Sin datos"
            emptyList<Asistencia>() to false
        }
    }
}
