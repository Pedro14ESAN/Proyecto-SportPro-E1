package com.example.projectsportpro2026_2.datos.seguridad

import com.example.projectsportpro2026_2.datos.modelo.Jugador
import com.example.projectsportpro2026_2.datos.modelo.PerfilUsuario
import com.example.projectsportpro2026_2.datos.modelo.Rol
import com.google.firebase.firestore.FirebaseFirestoreException

/*
 * HU25 - Control de acceso y privacidad según rol.
 *
 * Matriz de permisos de la app (el backend la repite en
 * firestore.rules, RN-20). La app nunca decide sola: si una
 * regla del servidor rechaza la lectura, se muestra un error
 * comprensible sin revelar si el dato existe.
 *
 * RN-02: denegar por defecto. Si no hay una relación que
 * justifique el acceso, el nivel es NINGUNO.
 */

// Qué puede ver un usuario de un jugador concreto
enum class NivelAcceso {
    // Datos sensibles: contacto, emergencia, datos físicos,
    // edad y estadísticas completas (disciplina, asistencia).
    COMPLETO,

    // Solo lo deportivo y público: nombre, posición, goles,
    // participaciones. Nunca contacto ni datos físicos.
    BASICO,

    // Nada: no tiene relación con el jugador.
    NINGUNO
}

/*
 * Relaciones del usuario con academias, equipos y jugadores.
 * Se calcula con RepositorioPermisos.contexto(uid).
 */
data class ContextoAcceso(
    val perfil: PerfilUsuario,

    // Academias donde es administrador (idAdministrador) o autorizado
    val idsAcademiasAdministradas: Set<String> = emptySet(),

    // Equipos que dirige como DT (equipo.idEntrenador == uid)
    val idsEquiposDirigidos: Set<String> = emptySet(),

    // Academias donde el DT figura como autorizado
    val idsAcademiasComoDt: Set<String> = emptySet(),

    // JUG: su propia ficha
    val idsFichasPropias: Set<String> = emptySet(),

    // PAD: jugadores con vínculo ACTIVO (perder el vínculo revoca)
    val idsJugadoresVinculados: Set<String> = emptySet(),

    // Equipos donde está alguna ficha propia o vinculada
    val idsEquiposDeFichas: Set<String> = emptySet()
) {
    val uid: String get() = perfil.uid
    val rol: Rol get() = perfil.rol
}

object ControlAcceso {

    /*
     * Criterios HU25:
     * - ADM: solo jugadores de SU academia.
     * - DT: información deportiva y de contacto/emergencia
     *   solo de los jugadores de SUS equipos.
     * - JUG: sus propios datos sensibles; de los demás solo lo básico.
     * - PAD: datos sensibles solo de jugadores con vínculo activo.
     * - Resto (otro equipo, comunidad): básico solo si comparten
     *   equipo; si el jugador es menor de edad, nada sensible.
     */
    fun nivelSobreJugador(
        contexto: ContextoAcceso,
        jugador: Jugador
    ): NivelAcceso {

        val completo =
            when (contexto.rol) {
                Rol.ADM ->
                    jugador.idAcademia in contexto.idsAcademiasAdministradas

                Rol.DT ->
                    jugador.idEquipo in contexto.idsEquiposDirigidos

                Rol.JUG ->
                    jugador.id in contexto.idsFichasPropias

                Rol.PAD ->
                    jugador.id in contexto.idsJugadoresVinculados
            }

        if (completo) {
            return NivelAcceso.COMPLETO
        }

        val compartenEquipo =
            jugador.idEquipo.isNotBlank() &&
                    (
                            jugador.idEquipo in contexto.idsEquiposDeFichas ||
                                    jugador.idEquipo in contexto.idsEquiposDirigidos
                            )

        val mismaAcademiaGestion =
            jugador.idAcademia in contexto.idsAcademiasAdministradas ||
                    jugador.idAcademia in contexto.idsAcademiasComoDt

        return if (compartenEquipo || mismaAcademiaGestion) {
            NivelAcceso.BASICO
        } else {
            NivelAcceso.NINGUNO
        }
    }

    // Contacto, emergencia, teléfono y datos físicos
    fun puedeVerDatosSensibles(
        contexto: ContextoAcceso,
        jugador: Jugador
    ): Boolean =
        nivelSobreJugador(contexto, jugador) == NivelAcceso.COMPLETO

    /*
     * Estadísticas sensibles (disciplina, asistencia, edad):
     * igual que los datos sensibles. Para un menor, ni siquiera
     * los compañeros ven estos datos (aviso "Datos protegidos").
     */
    fun puedeVerEstadisticasCompletas(
        contexto: ContextoAcceso,
        jugador: Jugador
    ): Boolean =
        puedeVerDatosSensibles(contexto, jugador)

    /*
     * Equipos cuyas estadísticas de conjunto puede ver.
     */
    fun puedeVerEquipo(
        contexto: ContextoAcceso,
        idEquipo: String,
        idAcademia: String
    ): Boolean =
        idEquipo in contexto.idsEquiposDirigidos ||
                idEquipo in contexto.idsEquiposDeFichas ||
                idAcademia in contexto.idsAcademiasAdministradas ||
                idAcademia in contexto.idsAcademiasComoDt

    /*
     * HU26/HU27: solo un DT autorizado genera, edita y aprueba
     * el resumen: el DT del equipo o un DT autorizado en la
     * academia del partido.
     */
    fun puedeRevisarResumen(
        contexto: ContextoAcceso,
        idEquipo: String,
        idAcademia: String
    ): Boolean =
        contexto.rol == Rol.DT &&
                (
                        idEquipo in contexto.idsEquiposDirigidos ||
                                idAcademia in contexto.idsAcademiasComoDt
                        )

    /*
     * El resumen aprobado lo pueden leer quienes ven el partido.
     * Un borrador solo lo ven el DT y el ADM de la academia.
     */
    fun puedeLeerBorradorResumen(
        contexto: ContextoAcceso,
        idEquipo: String,
        idAcademia: String
    ): Boolean =
        puedeRevisarResumen(contexto, idEquipo, idAcademia) ||
                idAcademia in contexto.idsAcademiasAdministradas
}

/*
 * Excepción HU25: mensaje de permisos comprensible que NO
 * revela si el dato existe ("no tienes acceso" en lugar de
 * "el jugador X existe pero no puedes verlo").
 */
class AccesoDenegadoException(
    mensaje: String = MensajesPermiso.SIN_ACCESO
) : IllegalStateException(mensaje)

object MensajesPermiso {

    const val SIN_ACCESO =
        "No tienes acceso a esta información. Si crees que es un error, contacta a tu academia."

    const val SIN_SESION =
        "Tu sesión terminó. Vuelve a iniciar sesión."

    fun traducir(
        error: Throwable,
        porDefecto: String = "No se pudo completar la operación. Revisa tu conexión."
    ): String =
        when {
            error is AccesoDenegadoException ->
                error.message ?: SIN_ACCESO

            error is FirebaseFirestoreException &&
                    error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                SIN_ACCESO

            error is FirebaseFirestoreException &&
                    error.code == FirebaseFirestoreException.Code.UNAUTHENTICATED ->
                SIN_SESION

            error is IllegalArgumentException ||
                    error is IllegalStateException ->
                error.message ?: porDefecto

            else -> porDefecto
        }
}
