package com.example.projectsportpro2026_2.datos.repositorio

import com.example.projectsportpro2026_2.datos.modelo.Comentario
import com.example.projectsportpro2026_2.datos.modelo.EstadoComentario
import com.example.projectsportpro2026_2.datos.modelo.EstadoPublicacion
import com.example.projectsportpro2026_2.datos.modelo.PerfilUsuario
import com.example.projectsportpro2026_2.datos.modelo.Publicacion
import com.example.projectsportpro2026_2.datos.modelo.TipoReaccion
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// Comentarios y reacciones sobre publicaciones de la comunidad (HU22)
class RepositorioInteracciones(
    private val bd: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val resolutor: ResolutorPertenencia = ResolutorPertenencia(bd)
) {

    private val publicaciones = bd.collection("publicaciones")
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

    private fun convertirPublicacion(d: DocumentSnapshot) =
        d.toObject(Publicacion::class.java)?.copy(id = d.id)

    private fun convertirComentario(d: DocumentSnapshot) =
        d.toObject(Comentario::class.java)?.copy(id = d.id)

    private fun comentariosDe(idPublicacion: String) =
        publicaciones.document(idPublicacion).collection("comentarios")

    // Criterio HU22: solo usuarios con acceso a la publicación pueden interactuar
    private suspend fun verificarAcceso(idPublicacion: String, uid: String): PerfilUsuario {
        val perfil = obtenerPerfil(uid)
        val publicacion = convertirPublicacion(publicaciones.document(idPublicacion).get().await())
            ?: throw IllegalStateException("La publicación no existe")

        val p = resolutor.obtener(uid, perfil.rol)
        if (!puedeVerPublicacion(publicacion, p)) {
            throw IllegalStateException("Esta publicación ya no está disponible")
        }
        return perfil
    }

    // ---------- Reacciones ----------

    // tipo = null significa "quitar mi reacción"
    suspend fun reaccionar(
        idPublicacion: String,
        uid: String,
        tipo: TipoReaccion?
    ): Result<TipoReaccion?> = intentar {
        verificarAcceso(idPublicacion, uid)

        val referencia = publicaciones.document(idPublicacion)
        val miCampo = FieldPath.of("reacciones", uid)   // reacciones.{uid}

        bd.runTransaction { transaccion ->
            val actual = convertirPublicacion(transaccion.get(referencia))
                ?: throw IllegalStateException("La publicación no existe")
            if (actual.estado != EstadoPublicacion.VISIBLE) {
                throw IllegalStateException("Esta publicación ya no está disponible")
            }

            if (tipo == null) {
                transaccion.update(referencia, miCampo, FieldValue.delete())
            } else {
                transaccion.update(referencia, miCampo, tipo.name)
            }
            Unit
        }.await()

        tipo
    }

    // ---------- Comentarios ----------

    suspend fun listarComentarios(
        idPublicacion: String,
        uid: String
    ): Result<List<Comentario>> = intentar {
        verificarAcceso(idPublicacion, uid)

        comentariosDe(idPublicacion).get().await().documents
            .mapNotNull { convertirComentario(it) }
            .filter { it.estado == EstadoComentario.VISIBLE }
            .sortedBy { it.fechaCreacion }        // los más antiguos arriba, como un chat
    }

    suspend fun comentar(
        idPublicacion: String,
        texto: String,
        uid: String
    ): Result<Comentario> = intentar {
        if (texto.isBlank()) throw IllegalArgumentException("Escribe un comentario")
        if (texto.trim().length > 300) throw IllegalArgumentException("El comentario admite máximo 300 caracteres")

        val perfil = verificarAcceso(idPublicacion, uid)

        val refPublicacion = publicaciones.document(idPublicacion)
        val refComentario = comentariosDe(idPublicacion).document()
        val ahora = System.currentTimeMillis()

        val comentario = Comentario(
            id = refComentario.id,
            idPublicacion = idPublicacion,
            texto = texto.trim(),
            idAutor = uid,
            nombreAutor = perfil.nombre,
            rolAutor = perfil.rol,
            estado = EstadoComentario.VISIBLE,
            fechaCreacion = ahora,
            fechaActualizacion = ahora
        )

        // Crear el comentario y sumar el contador ocurren juntos, o no ocurre ninguno
        bd.runTransaction { transaccion ->
            val actual = convertirPublicacion(transaccion.get(refPublicacion))
                ?: throw IllegalStateException("La publicación no existe")
            if (actual.estado != EstadoPublicacion.VISIBLE) {
                throw IllegalStateException("Esta publicación ya no está disponible")
            }

            transaccion.set(refComentario, comentario)
            transaccion.update(refPublicacion, "cantidadComentarios", FieldValue.increment(1))
            Unit
        }.await()

        comentario
    }

    suspend fun eliminarComentario(
        idPublicacion: String,
        idComentario: String,
        uid: String
    ): Result<Unit> = intentar {
        val refPublicacion = publicaciones.document(idPublicacion)
        val refComentario = comentariosDe(idPublicacion).document(idComentario)

        bd.runTransaction { transaccion ->
            val comentario = convertirComentario(transaccion.get(refComentario))
                ?: throw IllegalStateException("El comentario no existe")

            if (comentario.idAutor != uid) {
                throw IllegalStateException("Solo puedes eliminar tus propios comentarios")
            }
            if (comentario.estado != EstadoComentario.VISIBLE) {
                throw IllegalStateException("Este comentario ya no está disponible")
            }

            // Eliminación lógica + restar el contador
            transaccion.update(
                refComentario,
                mapOf(
                    "estado" to EstadoComentario.ELIMINADO.name,
                    "fechaActualizacion" to System.currentTimeMillis()
                )
            )
            transaccion.update(refPublicacion, "cantidadComentarios", FieldValue.increment(-1))
            Unit
        }.await()
    }
}