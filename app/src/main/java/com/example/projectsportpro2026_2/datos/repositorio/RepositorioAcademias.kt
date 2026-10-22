package com.example.projectsportpro2026_2.datos.repositorio

import com.example.projectsportpro2026_2.datos.modelo.Academia
import com.example.projectsportpro2026_2.datos.modelo.Categorias
import com.example.projectsportpro2026_2.datos.modelo.Equipo
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class RepositorioAcademias(
    bd: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val academias = bd.collection("academias")
    private val equipos = bd.collection("equipos")

    private suspend fun <T> intentar(bloque: suspend () -> T): Result<T> =
        try {
            Result.success(bloque())
        } catch (e: Exception) {
            Result.failure(e)
        }

    private suspend fun verificarPermiso(idAcademia: String, uid: String) {
        val academia = academias.document(idAcademia).get().await()
            .toObject(Academia::class.java)
        if (academia == null || uid !in academia.idsAutorizados) {
            throw IllegalStateException("No tienes permiso para gestionar esta academia")
        }
    }

    private suspend fun verificarSinDuplicado(
        idAcademia: String, nombre: String, categoria: String, ignorarId: String = ""
    ) {
        val existentes = equipos.whereEqualTo("idAcademia", idAcademia)
            .get().await().toObjects(Equipo::class.java)
        val repetido = existentes.any {
            it.id != ignorarId &&
                    it.nombre.trim().equals(nombre.trim(), ignoreCase = true) &&
                    it.categoria == categoria
        }
        if (repetido) {
            throw IllegalArgumentException("Ya existe un equipo con ese nombre en esa categoría")
        }
    }

    private fun validarEquipo(nombre: String, categoria: String) {
        if (nombre.isBlank()) throw IllegalArgumentException("El nombre del equipo es obligatorio")
        if (categoria !in Categorias.lista) throw IllegalArgumentException("Elige una categoría válida")
    }

    // ---------- Academias ----------

    suspend fun crearAcademia(
        nombre: String, direccion: String, descripcion: String, uid: String
    ): Result<Academia> = intentar {
        if (nombre.isBlank()) throw IllegalArgumentException("El nombre de la academia es obligatorio")
        val documento = academias.document()
        val academia = Academia(
            id = documento.id,
            nombre = nombre.trim(),
            direccion = direccion.trim(),
            descripcion = descripcion.trim(),
            idAdministrador = uid,
            idsAutorizados = listOf(uid),
            actualizadoPor = uid,
            fechaActualizacion = System.currentTimeMillis()
        )
        documento.set(academia).await()
        academia
    }

    // Solo las academias donde el usuario está autorizado (activas e inactivas)
    suspend fun listarAcademias(uid: String): Result<List<Academia>> = intentar {
        academias.whereArrayContains("idsAutorizados", uid)
            .get().await().toObjects(Academia::class.java)
            .sortedWith(compareByDescending<Academia> { it.activa }.thenBy { it.nombre })
    }

    suspend fun editarAcademia(
        idAcademia: String, nombre: String, direccion: String, descripcion: String, uid: String
    ): Result<Unit> = intentar {
        if (nombre.isBlank()) throw IllegalArgumentException("El nombre de la academia es obligatorio")
        verificarPermiso(idAcademia, uid)
        academias.document(idAcademia).update(
            mapOf(
                "nombre" to nombre.trim(),
                "direccion" to direccion.trim(),
                "descripcion" to descripcion.trim(),
                "actualizadoPor" to uid,
                "fechaActualizacion" to System.currentTimeMillis()
            )
        ).await()
    }

    suspend fun cambiarEstadoAcademia(
        idAcademia: String, activa: Boolean, uid: String
    ): Result<Unit> = intentar {
        verificarPermiso(idAcademia, uid)
        academias.document(idAcademia).update(
            mapOf(
                "activa" to activa,
                "actualizadoPor" to uid,
                "fechaActualizacion" to System.currentTimeMillis()
            )
        ).await()
    }

    // Autoriza a otro usuario (por ejemplo un DT) a gestionar la academia
    suspend fun autorizarUsuario(
        idAcademia: String, uidNuevo: String, uid: String
    ): Result<Unit> = intentar {
        verificarPermiso(idAcademia, uid)
        academias.document(idAcademia).update(
            "idsAutorizados", FieldValue.arrayUnion(uidNuevo)
        ).await()
    }

    // ---------- Equipos ----------

    suspend fun crearEquipo(
        nombre: String, categoria: String, idAcademia: String, idEntrenador: String, uid: String
    ): Result<Equipo> = intentar {
        validarEquipo(nombre, categoria)
        verificarPermiso(idAcademia, uid)
        verificarSinDuplicado(idAcademia, nombre, categoria)
        val documento = equipos.document()
        val equipo = Equipo(
            id = documento.id,
            nombre = nombre.trim(),
            categoria = categoria,
            idAcademia = idAcademia,
            idEntrenador = idEntrenador,
            actualizadoPor = uid,
            fechaActualizacion = System.currentTimeMillis()
        )
        documento.set(equipo).await()
        equipo
    }

    // Activos e inactivos; primero los activos
    suspend fun listarEquipos(idAcademia: String): Result<List<Equipo>> = intentar {
        equipos.whereEqualTo("idAcademia", idAcademia)
            .get().await().toObjects(Equipo::class.java)
            .sortedWith(compareByDescending<Equipo> { it.activo }.thenBy { it.nombre })
    }

    suspend fun editarEquipo(
        idEquipo: String, nombre: String, categoria: String, idEntrenador: String, uid: String
    ): Result<Unit> = intentar {
        validarEquipo(nombre, categoria)
        val equipo = equipos.document(idEquipo).get().await().toObject(Equipo::class.java)
            ?: throw IllegalStateException("El equipo no existe")
        verificarPermiso(equipo.idAcademia, uid)
        verificarSinDuplicado(equipo.idAcademia, nombre, categoria, ignorarId = idEquipo)
        equipos.document(idEquipo).update(
            mapOf(
                "nombre" to nombre.trim(),
                "categoria" to categoria,
                "idEntrenador" to idEntrenador,
                "actualizadoPor" to uid,
                "fechaActualizacion" to System.currentTimeMillis()
            )
        ).await()
    }

    // Nunca se borra: solo se activa o inactiva, para conservar el historial
    suspend fun cambiarEstadoEquipo(
        idEquipo: String, activo: Boolean, uid: String
    ): Result<Unit> = intentar {
        val equipo = equipos.document(idEquipo).get().await().toObject(Equipo::class.java)
            ?: throw IllegalStateException("El equipo no existe")
        verificarPermiso(equipo.idAcademia, uid)
        equipos.document(idEquipo).update(
            mapOf(
                "activo" to activo,
                "actualizadoPor" to uid,
                "fechaActualizacion" to System.currentTimeMillis()
            )
        ).await()
    }
}