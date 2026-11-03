package com.example.projectsportpro2026_2.datos.repositorio

import com.example.projectsportpro2026_2.datos.modelo.Academia
import com.example.projectsportpro2026_2.datos.modelo.Equipo
import com.example.projectsportpro2026_2.datos.modelo.EstadoPublicacion
import com.example.projectsportpro2026_2.datos.modelo.PerfilUsuario
import com.example.projectsportpro2026_2.datos.modelo.Publicacion
import com.example.projectsportpro2026_2.datos.modelo.VisibilidadPublicacion
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// Dónde puede publicar el usuario (además de Comunidad SportPro, que siempre está disponible)
data class OpcionesComunidad(
    val academias: List<Academia> = emptyList(),
    val equipos: List<Equipo> = emptyList()
)
// Regla de visibilidad de una publicación (HU21 / RC-03).
// La usan el feed (HU21) y las interacciones (HU22).
fun puedeVerPublicacion(publicacion: Publicacion, p: Pertenencia): Boolean {
    // Eliminadas por el autor u ocultas por moderación no son accesibles
    if (publicacion.estado != EstadoPublicacion.VISIBLE) return false

    return when (publicacion.visibilidad) {
        VisibilidadPublicacion.COMUNIDAD -> true
        VisibilidadPublicacion.ACADEMIA -> publicacion.idAcademia in p.idsAcademias
        VisibilidadPublicacion.EQUIPO ->
            publicacion.idEquipo in p.idsEquipos ||
                    publicacion.idAcademia in p.idsAcademiasGestionadas
    }
}
class RepositorioPublicaciones(
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

    // Regla de visibilidad del feed (HU21 / RC-03)
    private fun puedeVer(publicacion: Publicacion, p: Pertenencia): Boolean {
        // Eliminadas por el autor u ocultas por moderación no aparecen en el feed
        if (publicacion.estado != EstadoPublicacion.VISIBLE) return false

        return when (publicacion.visibilidad) {
            VisibilidadPublicacion.COMUNIDAD -> true
            VisibilidadPublicacion.ACADEMIA -> publicacion.idAcademia in p.idsAcademias
            VisibilidadPublicacion.EQUIPO ->
                publicacion.idEquipo in p.idsEquipos ||
                        publicacion.idAcademia in p.idsAcademiasGestionadas  // el ADM puede moderarlas
        }
    }

    // ---------- Lectura ----------

    suspend fun listarFeed(uid: String): Result<List<Publicacion>> = intentar {
        val perfil = obtenerPerfil(uid)
        val p = resolutor.obtener(uid, perfil.rol)

        // 1) Publicaciones abiertas a toda la Comunidad SportPro (de cualquier academia)
        val deComunidad = publicaciones
            .whereEqualTo("visibilidad", VisibilidadPublicacion.COMUNIDAD.name)
            .get().await().documents
            .mapNotNull { convertirPublicacion(it) }

        // 2) Publicaciones internas de las academias a las que pertenece
        val internas = p.idsAcademias.toList().chunked(10).flatMap { grupo ->
            publicaciones.whereIn("idAcademia", grupo)
                .get().await().documents
                .mapNotNull { convertirPublicacion(it) }
        }

        (deComunidad + internas)
            .distinctBy { it.id }                  // una pública de mi academia aparece en ambas listas
            .filter { puedeVerPublicacion(it, p) }
            .sortedByDescending { it.fechaCreacion }
    }

    suspend fun opcionesPublicacion(uid: String): Result<OpcionesComunidad> = intentar {
        val perfil = obtenerPerfil(uid)
        val p = resolutor.obtener(uid, perfil.rol)
        OpcionesComunidad(
            academias = p.academias.sortedBy { it.nombre.lowercase() },
            equipos = p.equipos.sortedBy { it.nombre.lowercase() }
        )
    }

    // ---------- Publicar ----------

    private fun validarTexto(texto: String) {
        if (texto.isBlank()) throw IllegalArgumentException("Escribe algo antes de publicar")
        if (texto.trim().length > 500) throw IllegalArgumentException("La publicación admite máximo 500 caracteres")
    }

    suspend fun crear(
        uid: String,
        texto: String,
        visibilidad: VisibilidadPublicacion,
        idAcademia: String = "",
        idEquipo: String = ""
    ): Result<Publicacion> = intentar {
        validarTexto(texto)
        val perfil = obtenerPerfil(uid)
        val p = resolutor.obtener(uid, perfil.rol)

        // Verifica que el autor realmente pertenezca a donde quiere publicar
        val equipo = p.equipos.find { it.id == idEquipo }
        val academia: Academia? = when (visibilidad) {
            VisibilidadPublicacion.EQUIPO -> {
                if (equipo == null) throw IllegalStateException("Selecciona uno de tus equipos")
                p.academias.find { it.id == equipo.idAcademia }
            }
            VisibilidadPublicacion.ACADEMIA ->
                p.academias.find { it.id == idAcademia }
                    ?: throw IllegalStateException("Selecciona una de tus academias")
            VisibilidadPublicacion.COMUNIDAD ->
                p.academias.find { it.id == idAcademia }   // opcional: solo para mostrar el club
        }

        val documento = publicaciones.document()
        val ahora = System.currentTimeMillis()
        val publicacion = Publicacion(
            id = documento.id,
            texto = texto.trim(),
            visibilidad = visibilidad,
            idAcademia = academia?.id.orEmpty(),
            nombreAcademia = academia?.nombre.orEmpty(),
            idEquipo = if (visibilidad == VisibilidadPublicacion.EQUIPO) equipo?.id.orEmpty() else "",
            nombreEquipo = if (visibilidad == VisibilidadPublicacion.EQUIPO) equipo?.nombre.orEmpty() else "",
            idAutor = uid,
            nombreAutor = perfil.nombre,
            rolAutor = perfil.rol,
            estado = EstadoPublicacion.VISIBLE,
            fechaCreacion = ahora,
            fechaActualizacion = ahora
        )

        documento.set(publicacion).await()
        publicacion
    }

    // ---------- Editar y eliminar (solo el autor) ----------

    private suspend fun obtenerPropia(idPublicacion: String, uid: String): Publicacion {
        val publicacion = convertirPublicacion(publicaciones.document(idPublicacion).get().await())
            ?: throw IllegalStateException("La publicación no existe")

        if (publicacion.idAutor != uid) {
            throw IllegalStateException("Solo el autor puede modificar su publicación")
        }

        when (publicacion.estado) {
            EstadoPublicacion.OCULTA ->
                throw IllegalStateException("Esta publicación fue ocultada por moderación y no se puede modificar")
            EstadoPublicacion.ELIMINADA ->
                throw IllegalStateException("La publicación ya fue eliminada")
            EstadoPublicacion.VISIBLE -> Unit
        }
        return publicacion
    }

    suspend fun editar(idPublicacion: String, texto: String, uid: String): Result<Unit> = intentar {
        validarTexto(texto)
        obtenerPropia(idPublicacion, uid)

        publicaciones.document(idPublicacion).update(
            mapOf(
                "texto" to texto.trim(),
                "editada" to true,
                "fechaActualizacion" to System.currentTimeMillis()
            )
        ).await()
    }

    suspend fun eliminar(idPublicacion: String, uid: String): Result<Unit> = intentar {
        obtenerPropia(idPublicacion, uid)

        // Eliminación lógica: el documento se conserva (criterio HU21)
        publicaciones.document(idPublicacion).update(
            mapOf(
                "estado" to EstadoPublicacion.ELIMINADA.name,
                "fechaActualizacion" to System.currentTimeMillis()
            )
        ).await()
    }
}