package com.example.projectsportpro2026_2.datos.repositorio

import com.example.projectsportpro2026_2.datos.modelo.Academia
import com.example.projectsportpro2026_2.datos.modelo.Categorias
import com.example.projectsportpro2026_2.datos.modelo.EntrenadorAcademia
import com.example.projectsportpro2026_2.datos.modelo.Equipo
import com.example.projectsportpro2026_2.datos.modelo.PerfilUsuario
import com.example.projectsportpro2026_2.datos.modelo.Rol
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class RepositorioAcademias(
    bd: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val academias = bd.collection("academias")
    private val equipos = bd.collection("equipos")
    private val usuarios = bd.collection("usuarios")

    private suspend fun <T> intentar(bloque: suspend () -> T): Result<T> =
        try {
            Result.success(bloque())
        } catch (e: Exception) {
            Result.failure(e)
        }

    private suspend fun verificarPermiso(idAcademia: String, uid: String): Academia {
        val academia = academias.document(idAcademia).get().await()
            .toObject(Academia::class.java)
        if (academia == null || uid !in academia.idsAutorizados) {
            throw IllegalStateException("No tienes permiso para gestionar esta academia")
        }
        return academia
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

    // El DT es opcional, pero si se elige debe ser un DT autorizado en la academia
    private suspend fun validarEntrenador(academia: Academia, idEntrenador: String) {
        if (idEntrenador.isBlank()) return
        if (idEntrenador !in academia.idsAutorizados) {
            throw IllegalArgumentException("El director técnico no pertenece a esta academia")
        }
        val perfil = usuarios.document(idEntrenador).get().await()
            .toObject(PerfilUsuario::class.java)
        if (perfil == null || perfil.rol != Rol.DT) {
            throw IllegalArgumentException("La persona elegida no es un director técnico")
        }
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

    // ---------- Directores técnicos de la academia ----------

    suspend fun listarEntrenadores(
        idAcademia: String, uid: String
    ): Result<List<EntrenadorAcademia>> = intentar {
        val academia = verificarPermiso(idAcademia, uid)
        academia.idsAutorizados.chunked(10).flatMap { grupo ->
            usuarios.whereIn(FieldPath.documentId(), grupo).get().await().documents
        }.mapNotNull { documento ->
            val perfil = documento.toObject(PerfilUsuario::class.java)
            if (perfil != null && perfil.rol == Rol.DT) {
                EntrenadorAcademia(documento.id, perfil.nombre, perfil.correo)
            } else {
                null
            }
        }.sortedBy { it.nombre }
    }

    // Solo el administrador de la academia agrega directores técnicos, por su correo
    suspend fun agregarEntrenador(
        idAcademia: String, correo: String, uid: String
    ): Result<Unit> = intentar {
        if (correo.isBlank()) throw IllegalArgumentException("Escribe el correo del director técnico")
        val academia = academias.document(idAcademia).get().await()
            .toObject(Academia::class.java)
            ?: throw IllegalStateException("La academia no existe")
        if (academia.idAdministrador != uid) {
            throw IllegalStateException("Solo el administrador de la academia puede agregar directores técnicos")
        }
        val limpio = correo.trim()
        val encontrados = usuarios.whereIn("correo", listOf(limpio, limpio.lowercase()))
            .get().await().documents
        val documento = encontrados.firstOrNull()
            ?: throw IllegalStateException("No existe una cuenta con ese correo")
        val perfil = documento.toObject(PerfilUsuario::class.java)
        if (perfil == null || perfil.rol != Rol.DT) {
            throw IllegalStateException("Esa cuenta no es de un director técnico")
        }
        if (documento.id in academia.idsAutorizados) {
            throw IllegalStateException("Ese director técnico ya pertenece a la academia")
        }
        academias.document(idAcademia).update(
            mapOf(
                "idsAutorizados" to FieldValue.arrayUnion(documento.id),
                "actualizadoPor" to uid,
                "fechaActualizacion" to System.currentTimeMillis()
            )
        ).await()
    }

    // ---------- Equipos ----------

    suspend fun crearEquipo(
        nombre: String, categoria: String, idAcademia: String, idEntrenador: String, uid: String
    ): Result<Equipo> = intentar {
        validarEquipo(nombre, categoria)
        val academia = verificarPermiso(idAcademia, uid)
        validarEntrenador(academia, idEntrenador)
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
        val academia = verificarPermiso(equipo.idAcademia, uid)
        validarEntrenador(academia, idEntrenador)
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