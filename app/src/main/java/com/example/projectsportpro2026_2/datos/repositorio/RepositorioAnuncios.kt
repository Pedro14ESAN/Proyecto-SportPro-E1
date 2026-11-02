package com.example.projectsportpro2026_2.datos.repositorio

import com.example.projectsportpro2026_2.datos.modelo.Academia
import com.example.projectsportpro2026_2.datos.modelo.Anuncio
import com.example.projectsportpro2026_2.datos.modelo.Equipo
import com.example.projectsportpro2026_2.datos.modelo.EstadoAnuncio
import com.example.projectsportpro2026_2.datos.modelo.EstadoVinculacion
import com.example.projectsportpro2026_2.datos.modelo.Jugador
import com.example.projectsportpro2026_2.datos.modelo.PerfilUsuario
import com.example.projectsportpro2026_2.datos.modelo.Rol
import com.example.projectsportpro2026_2.datos.modelo.TipoAudiencia
import com.example.projectsportpro2026_2.datos.modelo.Vinculacion
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class OpcionesPublicacion(
    val academia: Academia,
    val puedePublicarATodos: Boolean,   // ADM o autorizado: toda la academia o por rol
    val equipos: List<Equipo>           // equipos a los que puede dirigir un anuncio
)

// A qué academias y equipos pertenece el usuario (para saber qué puede leer)
private data class Pertenencia(
    val idsAcademias: Set<String>,
    val idsEquipos: Set<String>,
    val idsAcademiasGestionadas: Set<String>
)

class RepositorioAnuncios(
    private val bd: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val anuncios = bd.collection("anuncios")
    private val academias = bd.collection("academias")
    private val equipos = bd.collection("equipos")
    private val jugadores = bd.collection("jugadores")
    private val usuarios = bd.collection("usuarios")
    private val vinculaciones = bd.collection("vinculaciones")

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

    private fun convertirAcademia(d: DocumentSnapshot) = d.toObject(Academia::class.java)?.copy(id = d.id)
    private fun convertirEquipo(d: DocumentSnapshot) = d.toObject(Equipo::class.java)?.copy(id = d.id)
    private fun convertirJugador(d: DocumentSnapshot) = d.toObject(Jugador::class.java)?.copy(id = d.id)
    private fun convertirAnuncio(d: DocumentSnapshot) = d.toObject(Anuncio::class.java)?.copy(id = d.id)

    // Firestore solo acepta hasta 10 valores en whereIn, por eso se parte en grupos
    private suspend fun buscarPorIds(
        coleccion: CollectionReference,
        ids: List<String>
    ): List<DocumentSnapshot> =
        ids.filter { it.isNotBlank() }.distinct().chunked(10).flatMap { grupo ->
            coleccion.whereIn(FieldPath.documentId(), grupo).get().await().documents
        }

    // ---------- ¿A qué pertenece el usuario? ----------

    // Academias donde el usuario es administrador o está autorizado
    private suspend fun academiasQueGestiona(uid: String): List<Academia> {
        val comoAdmin = academias.whereEqualTo("idAdministrador", uid).get().await().documents
        val comoAutorizado = academias.whereArrayContains("idsAutorizados", uid).get().await().documents
        return (comoAdmin + comoAutorizado)
            .mapNotNull { convertirAcademia(it) }
            .distinctBy { it.id }
            .filter { it.activa }
    }

    private suspend fun equiposDelDt(uid: String): List<Equipo> =
        equipos.whereEqualTo("idEntrenador", uid).get().await().documents
            .mapNotNull { convertirEquipo(it) }
            .filter { it.activo }

    private suspend fun pertenencia(uid: String, rol: Rol): Pertenencia {

        // Fichas de jugador: la propia (JUG) o las de sus hijos vinculados (PAD)
        val fichas: List<Jugador> = when (rol) {
            Rol.JUG -> jugadores.whereEqualTo("idUsuario", uid).get().await().documents
                .mapNotNull { convertirJugador(it) }

            Rol.PAD -> {
                val idsJugadores = vinculaciones.whereEqualTo("idApoderado", uid).get().await()
                    .toObjects(Vinculacion::class.java)
                    .filter { it.estado == EstadoVinculacion.ACTIVA }
                    .map { it.idJugador }
                buscarPorIds(jugadores, idsJugadores).mapNotNull { convertirJugador(it) }
            }

            else -> emptyList()
        }

        val equiposDt = if (rol == Rol.DT) equiposDelDt(uid) else emptyList()
        val gestionadas = academiasQueGestiona(uid)

        return Pertenencia(
            idsAcademias = (fichas.map { it.idAcademia } +
                    equiposDt.map { it.idAcademia } +
                    gestionadas.map { it.id })
                .filter { it.isNotBlank() }.toSet(),
            idsEquipos = (fichas.map { it.idEquipo } + equiposDt.map { it.id })
                .filter { it.isNotBlank() }.toSet(),
            idsAcademiasGestionadas = gestionadas.map { it.id }.toSet()
        )
    }

    // Regla central de visibilidad (criterio HU19: nadie ve anuncios de grupos ajenos)
    private fun puedeVer(anuncio: Anuncio, uid: String, rol: Rol, p: Pertenencia): Boolean {
        if (anuncio.idAcademia in p.idsAcademiasGestionadas) return true // el ADM ve todo, incluso archivados
        if (anuncio.idAutor == uid) return true                         // el autor ve los suyos
        if (anuncio.estado != EstadoAnuncio.ACTIVO) return false          // los demás solo ven activos
        if (anuncio.idAcademia !in p.idsAcademias) return false

        return when (anuncio.tipoAudiencia) {
            TipoAudiencia.ACADEMIA -> true
            TipoAudiencia.EQUIPO -> anuncio.idEquipo in p.idsEquipos
            TipoAudiencia.ROL -> anuncio.rolDestino == rol
        }
    }

    // ---------- Lectura ----------

    suspend fun listarParaUsuario(uid: String): Result<List<Anuncio>> = intentar {
        val perfil = obtenerPerfil(uid)
        val p = pertenencia(uid, perfil.rol)

        p.idsAcademias.toList().chunked(10)
            .flatMap { grupo ->
                anuncios.whereIn("idAcademia", grupo).get().await().documents
                    .mapNotNull { convertirAnuncio(it) }
            }
            .filter { puedeVer(it, uid, perfil.rol, p) }
            .sortedByDescending { it.fechaCreacion }
    }

    // ---------- Publicación ----------

    suspend fun opcionesPublicacion(uid: String): Result<List<OpcionesPublicacion>> = intentar {
        val perfil = obtenerPerfil(uid)
        if (perfil.rol != Rol.ADM && perfil.rol != Rol.DT) {
            throw IllegalStateException("Solo un DT o administrador puede publicar anuncios")
        }

        // Academias que gestiona: puede publicar a toda la academia, por rol o a cualquier equipo
        val gestionadas = academiasQueGestiona(uid)
        val deGestion = gestionadas.map { academia ->
            val equiposAcademia = equipos.whereEqualTo("idAcademia", academia.id).get().await()
                .documents.mapNotNull { convertirEquipo(it) }.filter { it.activo }
            OpcionesPublicacion(academia, puedePublicarATodos = true, equipos = equiposAcademia)
        }

        // DT sin permisos de gestión: solo puede publicar para sus propios equipos
        val idsGestionadas = gestionadas.map { it.id }.toSet()
        val deDt = if (perfil.rol == Rol.DT) {
            equiposDelDt(uid)
                .filter { it.idAcademia !in idsGestionadas }
                .groupBy { it.idAcademia }
                .mapNotNull { (idAcademia, suyos) ->
                    val academia = convertirAcademia(academias.document(idAcademia).get().await())
                    if (academia == null || !academia.activa) null
                    else OpcionesPublicacion(academia, puedePublicarATodos = false, equipos = suyos)
                }
        } else {
            emptyList()
        }

        (deGestion + deDt).sortedBy { it.academia.nombre.lowercase() }
    }

    private fun validarTexto(titulo: String, contenido: String) {
        if (titulo.isBlank()) throw IllegalArgumentException("El título es obligatorio")
        if (contenido.isBlank()) throw IllegalArgumentException("El contenido es obligatorio")
        if (titulo.trim().length > 80) throw IllegalArgumentException("El título admite máximo 80 caracteres")
        if (contenido.trim().length > 1000) throw IllegalArgumentException("El contenido admite máximo 1000 caracteres")
    }

    suspend fun crear(
        uid: String,
        titulo: String,
        contenido: String,
        idAcademia: String,
        tipoAudiencia: TipoAudiencia,
        idEquipo: String = "",
        rolDestino: Rol = Rol.DT
    ): Result<Anuncio> = intentar {
        validarTexto(titulo, contenido)
        val perfil = obtenerPerfil(uid)

        // Verifica que el autor tenga permiso para esa audiencia
        val opciones = opcionesPublicacion(uid).getOrThrow()
            .find { it.academia.id == idAcademia }
            ?: throw IllegalStateException("No tienes permiso para publicar en esta academia")

        val equipo = opciones.equipos.find { it.id == idEquipo }
        when (tipoAudiencia) {
            TipoAudiencia.ACADEMIA, TipoAudiencia.ROL ->
                if (!opciones.puedePublicarATodos)
                    throw IllegalStateException("Solo puedes publicar anuncios para tus equipos")
            TipoAudiencia.EQUIPO ->
                if (equipo == null) throw IllegalStateException("Selecciona un equipo válido")
        }

        val documento = anuncios.document()
        val ahora = System.currentTimeMillis()
        val anuncio = Anuncio(
            id = documento.id,
            titulo = titulo.trim(),
            contenido = contenido.trim(),
            idAcademia = idAcademia,
            tipoAudiencia = tipoAudiencia,
            idEquipo = if (tipoAudiencia == TipoAudiencia.EQUIPO) idEquipo else "",
            nombreEquipo = if (tipoAudiencia == TipoAudiencia.EQUIPO) equipo?.nombre.orEmpty() else "",
            rolDestino = rolDestino,
            idAutor = uid,
            nombreAutor = perfil.nombre,
            rolAutor = perfil.rol,
            estado = EstadoAnuncio.ACTIVO,
            fechaCreacion = ahora,
            actualizadoPor = uid,
            fechaActualizacion = ahora
        )

        documento.set(anuncio).await()
        anuncio
    }

    // ---------- Edición y archivo ----------

    private suspend fun obtenerAnuncioGestionable(idAnuncio: String, uid: String): Anuncio {
        val anuncio = convertirAnuncio(anuncios.document(idAnuncio).get().await())
            ?: throw IllegalStateException("El anuncio no existe")

        val esGestor = academiasQueGestiona(uid).any { it.id == anuncio.idAcademia }
        if (anuncio.idAutor != uid && !esGestor) {
            throw IllegalStateException("Solo el autor o un administrador puede modificar este anuncio")
        }
        return anuncio
    }

    suspend fun editar(
        idAnuncio: String,
        titulo: String,
        contenido: String,
        uid: String
    ): Result<Unit> = intentar {
        validarTexto(titulo, contenido)
        val anuncio = obtenerAnuncioGestionable(idAnuncio, uid)
        if (anuncio.estado == EstadoAnuncio.ARCHIVADO) {
            throw IllegalStateException("No se puede editar un anuncio archivado")
        }

        anuncios.document(idAnuncio).update(
            mapOf(
                "titulo" to titulo.trim(),
                "contenido" to contenido.trim(),
                "editado" to true,
                "actualizadoPor" to uid,
                "fechaActualizacion" to System.currentTimeMillis()
            )
        ).await()
    }

    suspend fun cambiarEstado(
        idAnuncio: String,
        estado: EstadoAnuncio,
        uid: String
    ): Result<Unit> = intentar {
        obtenerAnuncioGestionable(idAnuncio, uid)

        anuncios.document(idAnuncio).update(
            mapOf(
                "estado" to estado.name,   // los enum se guardan como texto: "ACTIVO" / "ARCHIVADO"
                "actualizadoPor" to uid,
                "fechaActualizacion" to System.currentTimeMillis()
            )
        ).await()
    }
}