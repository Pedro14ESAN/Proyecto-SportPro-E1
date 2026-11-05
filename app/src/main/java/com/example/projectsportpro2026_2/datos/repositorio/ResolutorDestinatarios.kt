package com.example.projectsportpro2026_2.datos.repositorio

import com.example.projectsportpro2026_2.datos.modelo.Academia
import com.example.projectsportpro2026_2.datos.modelo.Equipo
import com.example.projectsportpro2026_2.datos.modelo.EstadoVinculacion
import com.example.projectsportpro2026_2.datos.modelo.Jugador
import com.example.projectsportpro2026_2.datos.modelo.Rol
import com.example.projectsportpro2026_2.datos.modelo.Vinculacion
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// Calcula QUIÉN debe recibir una notificación según equipo, vínculo y rol (criterio HU20).
// Así se evitan envíos masivos innecesarios.
class ResolutorDestinatarios(
    bd: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val academias = bd.collection("academias")
    private val equipos = bd.collection("equipos")
    private val jugadores = bd.collection("jugadores")
    private val vinculaciones = bd.collection("vinculaciones")

    // Cuentas de usuario de esas fichas (solo jugadores activos que tienen cuenta)
    private suspend fun cuentasDeJugadores(idsFichas: List<String>): Set<String> =
        idsFichas.filter { it.isNotBlank() }.distinct().chunked(10).flatMap { grupo ->
            jugadores.whereIn(FieldPath.documentId(), grupo).get().await()
                .toObjects(Jugador::class.java)
        }
            .filter { it.activo }
            .map { it.idUsuario }
            .filter { it.isNotBlank() }
            .toSet()

    // Apoderados con vínculo ACTIVO con esas fichas
    private suspend fun apoderadosDe(idsFichas: List<String>): Set<String> =
        idsFichas.filter { it.isNotBlank() }.distinct().chunked(10).flatMap { grupo ->
            vinculaciones.whereIn("idJugador", grupo).get().await()
                .toObjects(Vinculacion::class.java)
        }
            .filter { it.estado == EstadoVinculacion.ACTIVA }
            .map { it.idApoderado }
            .filter { it.isNotBlank() }
            .toSet()

    // Para convocatorias: el jugador convocado y sus apoderados
    suspend fun deJugadores(idsFichas: Collection<String>): Set<String> {
        val ids = idsFichas.toList()
        return cuentasDeJugadores(ids) + apoderadosDe(ids)
    }

    // Para entrenamientos y anuncios de equipo: jugadores, apoderados y DT del equipo
    suspend fun deEquipo(idEquipo: String): Set<String> {
        if (idEquipo.isBlank()) return emptySet()

        val equipo = equipos.document(idEquipo).get().await().toObject(Equipo::class.java)
        val idsFichas = jugadores.whereEqualTo("idEquipo", idEquipo).get().await()
            .documents.map { it.id }

        return deJugadores(idsFichas) +
                listOfNotNull(equipo?.idEntrenador?.takeIf { it.isNotBlank() })
    }

    // Para anuncios de academia (rol = null) o por rol (solo DT, JUG, PAD o ADM)
    suspend fun deAcademia(idAcademia: String, rol: Rol? = null): Set<String> {
        if (idAcademia.isBlank()) return emptySet()
        val resultado = mutableSetOf<String>()

        if (rol == null || rol == Rol.JUG || rol == Rol.PAD) {
            val idsFichas = jugadores.whereEqualTo("idAcademia", idAcademia).get().await()
                .documents.map { it.id }
            when (rol) {
                Rol.JUG -> resultado += cuentasDeJugadores(idsFichas)
                Rol.PAD -> resultado += apoderadosDe(idsFichas)
                else -> resultado += deJugadores(idsFichas)
            }
        }

        if (rol == null || rol == Rol.DT) {
            resultado += equipos.whereEqualTo("idAcademia", idAcademia).get().await()
                .toObjects(Equipo::class.java)
                .map { it.idEntrenador }
        }

        if (rol == null || rol == Rol.ADM) {
            academias.document(idAcademia).get().await()
                .toObject(Academia::class.java)
                ?.let { academia ->
                    resultado += academia.idAdministrador
                    resultado += academia.idsAutorizados
                }
        }

        return resultado.filter { it.isNotBlank() }.toSet()
    }
}