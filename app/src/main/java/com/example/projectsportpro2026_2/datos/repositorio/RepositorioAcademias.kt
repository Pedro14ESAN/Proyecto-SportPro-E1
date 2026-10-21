package com.example.projectsportpro2026_2.datos.repositorio

import com.example.projectsportpro2026_2.datos.modelo.Academia
import com.example.projectsportpro2026_2.datos.modelo.Equipo
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class RepositorioAcademias(
    bd: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val academias = bd.collection("academias")
    private val equipos = bd.collection("equipos")

    suspend fun crearAcademia(
        nombre: String,
        direccion: String,
        descripcion: String,
        idAdministrador: String
    ): Result<Academia> = try {
        val documento = academias.document()
        val academia = Academia(
            id = documento.id,
            nombre = nombre.trim(),
            direccion = direccion.trim(),
            descripcion = descripcion.trim(),
            idAdministrador = idAdministrador
        )
        documento.set(academia).await()
        Result.success(academia)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun listarAcademias(): Result<List<Academia>> = try {
        val lista = academias.get().await().toObjects(Academia::class.java)
        Result.success(lista.filter { it.activa }.sortedBy { it.nombre })
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun crearEquipo(
        nombre: String,
        categoria: String,
        idAcademia: String,
        idEntrenador: String
    ): Result<Equipo> = try {
        val documento = equipos.document()
        val equipo = Equipo(
            id = documento.id,
            nombre = nombre.trim(),
            categoria = categoria.trim(),
            idAcademia = idAcademia,
            idEntrenador = idEntrenador
        )
        documento.set(equipo).await()
        Result.success(equipo)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun listarEquipos(idAcademia: String): Result<List<Equipo>> = try {
        val lista = equipos.whereEqualTo("idAcademia", idAcademia)
            .get().await().toObjects(Equipo::class.java)
        Result.success(lista.filter { it.activo }.sortedBy { it.nombre })
    } catch (e: Exception) {
        Result.failure(e)
    }
}