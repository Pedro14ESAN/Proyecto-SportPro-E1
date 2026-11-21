package com.example.projectsportpro2026_2.datos.repositorio

import com.example.projectsportpro2026_2.datos.modelo.Academia
import com.example.projectsportpro2026_2.datos.modelo.Equipo
import com.example.projectsportpro2026_2.datos.modelo.Jugador
import com.example.projectsportpro2026_2.datos.modelo.Rol
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

    private suspend fun fichasActivas(idsFichas: Collection<String>): List<Jugador> =
        idsFichas.filter { it.isNotBlank() }.distinct().chunked(10).flatMap { grupo ->
            jugadores.whereIn(FieldPath.documentId(), grupo).get().await()
                .toObjects(Jugador::class.java)
        }.filter { it.activo }

    // Cuenta propia del jugador + apoderados con vínculo activo.
    // Los apoderados se leen de la ficha (idsApoderados, lo mantiene HU05 al aprobar
    // o desvincular) y no de "vinculaciones": las reglas de HU25 solo dejan leer
    // vinculaciones al propio apoderado o al gestor de la academia, y un DT o JUG
    // que dispara la notificación no siempre lo es.
    private fun cuentasDe(fichas: List<Jugador>): Set<String> =
        fichas.flatMap { listOf(it.idUsuario) + it.idsApoderados }
            .filter { it.isNotBlank() }
            .toSet()

    // Para convocatorias: el jugador convocado y sus apoderados
    suspend fun deJugadores(idsFichas: Collection<String>): Set<String> =
        cuentasDe(fichasActivas(idsFichas))

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
            val fichas = jugadores.whereEqualTo("idAcademia", idAcademia).get().await()
                .toObjects(Jugador::class.java)
                .filter { it.activo }
            when (rol) {
                Rol.JUG -> resultado += fichas.map { it.idUsuario }
                Rol.PAD -> resultado += fichas.flatMap { it.idsApoderados }
                else -> resultado += cuentasDe(fichas)
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