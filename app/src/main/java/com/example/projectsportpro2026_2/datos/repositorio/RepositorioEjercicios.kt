package com.example.projectsportpro2026_2.datos.repositorio

import com.example.projectsportpro2026_2.datos.modelo.Academia
import com.example.projectsportpro2026_2.datos.modelo.Ejercicio
import com.example.projectsportpro2026_2.datos.modelo.PerfilUsuario
import com.example.projectsportpro2026_2.datos.modelo.Rol
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class RepositorioEjercicios(
    private val bd: FirebaseFirestore =
        FirebaseFirestore.getInstance()
) {

    private val ejercicios =
        bd.collection("ejercicios")

    private val academias =
        bd.collection("academias")

    private val usuarios =
        bd.collection("usuarios")

    private suspend fun <T> intentar(
        bloque: suspend () -> T
    ): Result<T> {
        return try {
            Result.success(bloque())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun obtenerPerfil(
        uid: String
    ): PerfilUsuario {

        return usuarios
            .document(uid)
            .get()
            .await()
            .toObject(
                PerfilUsuario::class.java
            )
            ?: throw IllegalStateException(
                "Tu cuenta no tiene perfil"
            )
    }

    private fun convertirAcademia(
        documento: DocumentSnapshot
    ): Academia? {

        val academia =
            documento.toObject(
                Academia::class.java
            ) ?: return null

        return academia.copy(
            id = documento.id
        )
    }

    private fun convertirEjercicio(
        documento: DocumentSnapshot
    ): Ejercicio? {

        val ejercicio =
            documento.toObject(
                Ejercicio::class.java
            ) ?: return null

        return ejercicio.copy(
            id = documento.id
        )
    }

    suspend fun listarAcademiasDisponibles(
        uid: String
    ): Result<List<Academia>> = intentar {

        val perfil =
            obtenerPerfil(uid)

        if (
            perfil.rol != Rol.ADM &&
            perfil.rol != Rol.DT
        ) {
            throw IllegalStateException(
                "No tienes acceso a la biblioteca de ejercicios"
            )
        }

        val comoAdministrador =
            academias
                .whereEqualTo(
                    "idAdministrador",
                    uid
                )
                .get()
                .await()
                .documents
                .mapNotNull {
                    convertirAcademia(it)
                }

        val comoAutorizado =
            academias
                .whereArrayContains(
                    "idsAutorizados",
                    uid
                )
                .get()
                .await()
                .documents
                .mapNotNull {
                    convertirAcademia(it)
                }

        (
                comoAdministrador +
                        comoAutorizado
                )
            .distinctBy {
                it.id
            }
            .filter {
                it.activa
            }
            .sortedBy {
                it.nombre.lowercase()
            }
    }

    private suspend fun verificarPermiso(
        idAcademia: String,
        uid: String
    ): Academia {

        val perfil =
            obtenerPerfil(uid)

        if (
            perfil.rol != Rol.ADM &&
            perfil.rol != Rol.DT
        ) {
            throw IllegalStateException(
                "Solo un administrador o director técnico puede administrar ejercicios"
            )
        }

        val documento =
            academias
                .document(idAcademia)
                .get()
                .await()

        val academia =
            convertirAcademia(
                documento
            )
                ?: throw IllegalStateException(
                    "La academia no existe"
                )

        val permitido =
            academia.idAdministrador == uid ||
                    uid in academia.idsAutorizados

        if (!permitido) {
            throw IllegalStateException(
                "No tienes permiso para gestionar esta academia"
            )
        }

        if (!academia.activa) {
            throw IllegalStateException(
                "La academia está inactiva"
            )
        }

        return academia
    }

    private fun validarEjercicio(
        nombre: String,
        objetivo: String,
        duracionMinutos: Int
    ) {

        if (nombre.isBlank()) {
            throw IllegalArgumentException(
                "El nombre es obligatorio"
            )
        }

        if (objetivo.isBlank()) {
            throw IllegalArgumentException(
                "El objetivo es obligatorio"
            )
        }

        if (duracionMinutos <= 0) {
            throw IllegalArgumentException(
                "La duración debe ser mayor a cero"
            )
        }
    }

    suspend fun crearEjercicio(
        nombre: String,
        objetivo: String,
        descripcion: String,
        duracionMinutos: Int,
        idAcademia: String,
        uid: String
    ): Result<Ejercicio> = intentar {

        validarEjercicio(
            nombre,
            objetivo,
            duracionMinutos
        )

        verificarPermiso(
            idAcademia,
            uid
        )

        val documento =
            ejercicios.document()

        val ahora =
            System.currentTimeMillis()

        val ejercicio =
            Ejercicio(
                id =
                    documento.id,
                nombre =
                    nombre.trim(),
                objetivo =
                    objetivo.trim(),
                descripcion =
                    descripcion.trim(),
                duracionMinutos =
                    duracionMinutos,
                idAcademia =
                    idAcademia,
                activo =
                    true,
                creadoPor =
                    uid,
                fechaCreacion =
                    ahora,
                actualizadoPor =
                    uid,
                fechaActualizacion =
                    ahora
            )

        documento
            .set(ejercicio)
            .await()

        ejercicio
    }

    suspend fun listarEjercicios(
        idAcademia: String,
        uid: String
    ): Result<List<Ejercicio>> = intentar {

        verificarPermiso(
            idAcademia,
            uid
        )

        ejercicios
            .whereEqualTo(
                "idAcademia",
                idAcademia
            )
            .get()
            .await()
            .documents
            .mapNotNull {
                convertirEjercicio(it)
            }
            .sortedWith(
                compareByDescending<Ejercicio> {
                    it.activo
                }.thenBy {
                    it.nombre.lowercase()
                }
            )
    }

    suspend fun listarEjerciciosActivos(
        idAcademia: String,
        uid: String
    ): Result<List<Ejercicio>> = intentar {

        verificarPermiso(
            idAcademia,
            uid
        )

        ejercicios
            .whereEqualTo(
                "idAcademia",
                idAcademia
            )
            .whereEqualTo(
                "activo",
                true
            )
            .get()
            .await()
            .documents
            .mapNotNull {
                convertirEjercicio(it)
            }
            .sortedBy {
                it.nombre.lowercase()
            }
    }

    suspend fun editarEjercicio(
        idEjercicio: String,
        nombre: String,
        objetivo: String,
        descripcion: String,
        duracionMinutos: Int,
        uid: String
    ): Result<Unit> = intentar {

        validarEjercicio(
            nombre,
            objetivo,
            duracionMinutos
        )

        val referencia =
            ejercicios.document(
                idEjercicio
            )

        val documento =
            referencia
                .get()
                .await()

        val ejercicio =
            convertirEjercicio(
                documento
            )
                ?: throw IllegalStateException(
                    "El ejercicio no existe"
                )

        verificarPermiso(
            ejercicio.idAcademia,
            uid
        )

        referencia
            .update(
                mapOf(
                    "nombre" to
                            nombre.trim(),
                    "objetivo" to
                            objetivo.trim(),
                    "descripcion" to
                            descripcion.trim(),
                    "duracionMinutos" to
                            duracionMinutos,
                    "actualizadoPor" to
                            uid,
                    "fechaActualizacion" to
                            System.currentTimeMillis()
                )
            )
            .await()
    }

    suspend fun cambiarEstado(
        idEjercicio: String,
        activo: Boolean,
        uid: String
    ): Result<Unit> = intentar {

        val referencia =
            ejercicios.document(
                idEjercicio
            )

        val documento =
            referencia
                .get()
                .await()

        val ejercicio =
            convertirEjercicio(
                documento
            )
                ?: throw IllegalStateException(
                    "El ejercicio no existe"
                )

        verificarPermiso(
            ejercicio.idAcademia,
            uid
        )

        referencia
            .update(
                mapOf(
                    "activo" to
                            activo,
                    "actualizadoPor" to
                            uid,
                    "fechaActualizacion" to
                            System.currentTimeMillis()
                )
            )
            .await()
    }
}