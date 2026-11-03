package com.example.projectsportpro2026_2.datos.repositorio

import com.example.projectsportpro2026_2.datos.modelo.Academia
import com.example.projectsportpro2026_2.datos.modelo.AvisoPrueba
import com.example.projectsportpro2026_2.datos.modelo.Categorias
import com.example.projectsportpro2026_2.datos.modelo.EstadoAviso
import com.example.projectsportpro2026_2.datos.modelo.PerfilUsuario
import com.example.projectsportpro2026_2.datos.modelo.Posiciones
import com.example.projectsportpro2026_2.datos.modelo.Rol
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// Avisos oficiales de pruebas abiertas para captar jugadores (HU23)
class RepositorioAvisosPrueba(
    private val bd: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val resolutor: ResolutorPertenencia = ResolutorPertenencia(bd)
) {

    private val avisos = bd.collection("avisosPrueba")
    private val usuarios = bd.collection("usuarios")

    // ---------- Utilidades ----------

    private suspend fun <T> intentar(bloque: suspend () -> T): Result<T> =
        try {
            Result.success(bloque())
        } catch (e: Exception) {
            Result.failure(e)
        }

    private suspend fun obtenerPerfil(uid: String): PerfilUsuario =
        usuarios.document(uid).get().await()
            .toObject(PerfilUsuario::class.java)
            ?: throw IllegalStateException("Tu cuenta no tiene perfil")

    private fun convertirAviso(d: DocumentSnapshot) =
        d.toObject(AvisoPrueba::class.java)?.copy(id = d.id)

    // ---------- Lectura ----------

    // Cualquier usuario con sesión puede ver los avisos; no se le pide ningún dato sensible
    suspend fun listar(): Result<List<AvisoPrueba>> = intentar {
        avisos.get().await().documents
            .mapNotNull { convertirAviso(it) }
            .filter { it.estado != EstadoAviso.OCULTO }   // los ocultos por moderación no se muestran
            .sortedBy { it.fechaPrueba }                    // las pruebas más próximas primero
    }

    // Academias en nombre de las cuales puede publicar (RC-05: solo DT o ADM)
    suspend fun academiasParaPublicar(uid: String): Result<List<Academia>> = intentar {
        val perfil = obtenerPerfil(uid)
        if (perfil.rol != Rol.DT && perfil.rol != Rol.ADM) {
            emptyList()
        } else {
            // DT: academias de sus equipos. ADM: academias que gestiona.
            resolutor.obtener(uid, perfil.rol).academias.sortedBy { it.nombre.lowercase() }
        }
    }

    // ---------- Publicar ----------

    suspend fun crear(
        uid: String,
        idAcademia: String,
        categoria: String,
        posiciones: List<String>,
        fechaPrueba: Long,
        ubicacion: String,
        descripcion: String
    ): Result<AvisoPrueba> = intentar {

        // Validaciones de contenido
        if (categoria !in Categorias.lista) throw IllegalArgumentException("Selecciona una categoría válida")
        if (posiciones.any { it !in Posiciones.lista }) throw IllegalArgumentException("Hay una posición no válida")
        if (ubicacion.isBlank()) throw IllegalArgumentException("La ubicación es obligatoria")
        if (descripcion.isBlank()) throw IllegalArgumentException("La descripción es obligatoria")
        if (ubicacion.trim().length > 120) throw IllegalArgumentException("La ubicación admite máximo 120 caracteres")
        if (descripcion.trim().length > 600) throw IllegalArgumentException("La descripción admite máximo 600 caracteres")

        // Caso alterno HU23: no se puede publicar como Abierto una prueba con fecha pasada
        if (fechaPrueba <= System.currentTimeMillis()) {
            throw IllegalArgumentException("La fecha de la prueba debe ser posterior a este momento")
        }

        // RC-05: el autor debe estar autorizado para esa academia
        val perfil = obtenerPerfil(uid)
        val academia = academiasParaPublicar(uid).getOrThrow()
            .find { it.id == idAcademia }
            ?: throw IllegalStateException("No tienes permiso para publicar avisos de esta academia")

        val documento = avisos.document()
        val ahora = System.currentTimeMillis()
        val aviso = AvisoPrueba(
            id = documento.id,
            idAcademia = academia.id,
            nombreAcademia = academia.nombre,
            categoria = categoria,
            posiciones = Posiciones.lista.filter { it in posiciones },   // orden consistente y sin repetidos
            fechaPrueba = fechaPrueba,
            ubicacion = ubicacion.trim(),
            descripcion = descripcion.trim(),
            idAutor = uid,
            nombreAutor = perfil.nombre,
            rolAutor = perfil.rol,
            estado = EstadoAviso.ABIERTO,
            fechaCreacion = ahora,
            actualizadoPor = uid,
            fechaActualizacion = ahora
        )

        documento.set(aviso).await()
        aviso
    }

    // ---------- Cerrar / reabrir ----------

    suspend fun cambiarEstado(
        idAviso: String,
        nuevo: EstadoAviso,
        uid: String
    ): Result<Unit> = intentar {
        if (nuevo != EstadoAviso.ABIERTO && nuevo != EstadoAviso.CERRADO) {
            throw IllegalArgumentException("Solo se puede abrir o cerrar un aviso")
        }

        val aviso = convertirAviso(avisos.document(idAviso).get().await())
            ?: throw IllegalStateException("El aviso no existe")

        // Solo el autor o un administrador de esa academia
        val esGestor = resolutor.academiasQueGestiona(uid).any { it.id == aviso.idAcademia }
        if (aviso.idAutor != uid && !esGestor) {
            throw IllegalStateException("Solo el autor o un administrador puede modificar este aviso")
        }

        if (aviso.estado == EstadoAviso.OCULTO) {
            throw IllegalStateException("Este aviso fue ocultado por moderación")
        }
        if (nuevo == EstadoAviso.ABIERTO && aviso.fechaPrueba <= System.currentTimeMillis()) {
            throw IllegalStateException("No se puede reabrir: la fecha de la prueba ya pasó")
        }

        avisos.document(idAviso).update(
            mapOf(
                "estado" to nuevo.name,
                "actualizadoPor" to uid,
                "fechaActualizacion" to System.currentTimeMillis()
            )
        ).await()
    }
}