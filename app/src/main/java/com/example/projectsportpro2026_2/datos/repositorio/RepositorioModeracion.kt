package com.example.projectsportpro2026_2.datos.repositorio

import com.example.projectsportpro2026_2.datos.modelo.AvisoPrueba
import com.example.projectsportpro2026_2.datos.modelo.Comentario
import com.example.projectsportpro2026_2.datos.modelo.DecisionModeracion
import com.example.projectsportpro2026_2.datos.modelo.Denuncia
import com.example.projectsportpro2026_2.datos.modelo.EstadoAviso
import com.example.projectsportpro2026_2.datos.modelo.EstadoComentario
import com.example.projectsportpro2026_2.datos.modelo.EstadoPublicacion
import com.example.projectsportpro2026_2.datos.modelo.EstadoReporte
import com.example.projectsportpro2026_2.datos.modelo.MotivoReporte
import com.example.projectsportpro2026_2.datos.modelo.ObjetivoReporte
import com.example.projectsportpro2026_2.datos.modelo.PerfilUsuario
import com.example.projectsportpro2026_2.datos.modelo.Publicacion
import com.example.projectsportpro2026_2.datos.modelo.RegistroModeracion
import com.example.projectsportpro2026_2.datos.modelo.Reporte
import com.example.projectsportpro2026_2.datos.modelo.Rol
import com.example.projectsportpro2026_2.datos.modelo.TipoContenido
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// Reportes y moderación de contenido de la comunidad (HU24)
class RepositorioModeracion(
    private val bd: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val resolutor: ResolutorPertenencia = ResolutorPertenencia(bd)
) {

    private val reportes = bd.collection("reportes")
    private val moderaciones = bd.collection("moderaciones")
    private val publicaciones = bd.collection("publicaciones")
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

    // Dónde vive cada tipo de contenido en Firestore
    private fun referenciaContenido(o: ObjetivoReporte): DocumentReference = when (o.tipo) {
        TipoContenido.PUBLICACION -> publicaciones.document(o.idContenido)
        TipoContenido.COMENTARIO -> publicaciones.document(o.idPublicacion)
            .collection("comentarios").document(o.idContenido)
        TipoContenido.AVISO -> avisos.document(o.idContenido)
    }

    private fun referenciaPublicacionDeComentario(o: ObjetivoReporte): DocumentReference? =
        if (o.tipo == TipoContenido.COMENTARIO) publicaciones.document(o.idPublicacion) else null

    // Lo que el reporte necesita saber del contenido: academia, autor y copia del texto
    private data class DatosContenido(
        val idAcademia: String,
        val idAutor: String,
        val nombreAutor: String,
        val resumen: String
    )

    private fun leerDatos(
        o: ObjetivoReporte,
        doc: DocumentSnapshot,
        docPublicacion: DocumentSnapshot?
    ): DatosContenido = when (o.tipo) {
        TipoContenido.PUBLICACION -> {
            val p = doc.toObject(Publicacion::class.java)
                ?: throw IllegalStateException("La publicación no existe")
            DatosContenido(p.idAcademia, p.idAutor, p.nombreAutor, p.texto)
        }
        TipoContenido.COMENTARIO -> {
            val c = doc.toObject(Comentario::class.java)
                ?: throw IllegalStateException("El comentario no existe")
            val p = docPublicacion?.toObject(Publicacion::class.java)
            DatosContenido(p?.idAcademia.orEmpty(), c.idAutor, c.nombreAutor, c.texto)
        }
        TipoContenido.AVISO -> {
            val a = doc.toObject(AvisoPrueba::class.java)
                ?: throw IllegalStateException("El aviso no existe")
            DatosContenido(a.idAcademia, a.idAutor, a.nombreAutor, "${a.titulo()} — ${a.descripcion}")
        }
    }

    // ---------- Reportar (cualquier usuario) ----------

    suspend fun reportar(
        objetivo: ObjetivoReporte,
        motivo: MotivoReporte,
        detalle: String,
        uid: String
    ): Result<Unit> = intentar {
        if (detalle.trim().length > 300) throw IllegalArgumentException("El detalle admite máximo 300 caracteres")

        val refContenido = referenciaContenido(objetivo)
        val refPublicacion = referenciaPublicacionDeComentario(objetivo)
        val refReporte = reportes.document(objetivo.idReporte())
        val refDenuncia = refReporte.collection("denuncias").document(uid)   // id = uid
        val ahora = System.currentTimeMillis()

        bd.runTransaction { t ->
            // 1) Leer todo primero
            val docContenido = t.get(refContenido)
            if (!docContenido.exists()) throw IllegalStateException("El contenido ya no existe")
            val docPublicacion = refPublicacion?.let { t.get(it) }
            val docReporte = t.get(refReporte)
            val docDenuncia = t.get(refDenuncia)

            val datos = leerDatos(objetivo, docContenido, docPublicacion)
            if (datos.idAutor == uid) {
                throw IllegalStateException("No puedes reportar tu propio contenido")
            }
            if (docDenuncia.exists()) {
                throw IllegalStateException("Ya reportaste este contenido. Un moderador lo revisará.")
            }

            // 2) Escribir
            t.set(refDenuncia, Denuncia(uid, motivo, detalle.trim(), ahora))

            if (!docReporte.exists()) {
                // Primer reporte de este contenido: se crea el caso con la evidencia
                t.set(
                    refReporte,
                    Reporte(
                        id = refReporte.id,
                        tipoContenido = objetivo.tipo,
                        idContenido = objetivo.idContenido,
                        idPublicacion = objetivo.idPublicacion,
                        idAcademia = datos.idAcademia,
                        resumenContenido = datos.resumen.take(500),
                        idAutorContenido = datos.idAutor,
                        nombreAutorContenido = datos.nombreAutor,
                        cantidadReportes = 1,
                        motivos = mapOf(motivo.name to 1L),
                        estado = EstadoReporte.PENDIENTE,
                        fechaPrimerReporte = ahora,
                        fechaUltimoReporte = ahora
                    )
                )
            } else {
                // Ya existía: se agrupa (se suma al contador) y vuelve a quedar pendiente
                t.update(
                    refReporte,
                    mapOf(
                        "cantidadReportes" to FieldValue.increment(1),
                        "motivos.${motivo.name}" to FieldValue.increment(1),
                        "estado" to EstadoReporte.PENDIENTE.name,
                        "fechaUltimoReporte" to ahora
                    )
                )
            }
            Unit
        }.await()
    }

    // ---------- Moderar (solo ADM) ----------

    // ¿Puede este usuario moderar? (para mostrar el botón "Moderar" en la pantalla)
    suspend fun puedeModerar(uid: String): Result<Boolean> = intentar {
        obtenerPerfil(uid).rol == Rol.ADM && resolutor.academiasQueGestiona(uid).isNotEmpty()
    }

    // Reportes que le corresponden a este ADM: pendientes primero, luego los más reportados
    suspend fun listarReportes(uid: String): Result<List<Reporte>> = intentar {
        val perfil = obtenerPerfil(uid)
        if (perfil.rol != Rol.ADM) throw IllegalStateException("Solo un administrador puede moderar")

        val gestionadas = resolutor.academiasQueGestiona(uid).map { it.id }.toSet()
        if (gestionadas.isEmpty()) return@intentar emptyList<Reporte>()

        reportes.get().await().documents
            .mapNotNull { it.toObject(Reporte::class.java)?.copy(id = it.id) }
            .filter { it.idAcademia in gestionadas || it.idAcademia.isBlank() }
            .sortedWith(
                compareBy<Reporte> { it.estado != EstadoReporte.PENDIENTE }
                    .thenByDescending { it.cantidadReportes }
                    .thenByDescending { it.fechaUltimoReporte }
            )
    }

    // Sirve para resolver un reporte Y para "Moderar directamente" (sin reportes previos)
    suspend fun moderar(
        objetivo: ObjetivoReporte,
        decision: DecisionModeracion,
        motivo: String,
        uid: String
    ): Result<Unit> = intentar {
        if (motivo.isBlank()) throw IllegalArgumentException("Escribe el motivo de la decisión")
        if (decision == DecisionModeracion.CERRADO && objetivo.tipo != TipoContenido.AVISO) {
            throw IllegalArgumentException("Solo los avisos de prueba se pueden cerrar")
        }

        val perfil = obtenerPerfil(uid)
        if (perfil.rol != Rol.ADM) throw IllegalStateException("Solo un administrador puede moderar")
        val gestionadas = resolutor.academiasQueGestiona(uid).map { it.id }.toSet()

        val refContenido = referenciaContenido(objetivo)
        val refPublicacion = referenciaPublicacionDeComentario(objetivo)
        val refReporte = reportes.document(objetivo.idReporte())
        val refRegistro = moderaciones.document()
        val ahora = System.currentTimeMillis()

        bd.runTransaction { t ->
            // 1) Leer
            val docContenido = t.get(refContenido)
            if (!docContenido.exists()) throw IllegalStateException("El contenido ya no existe")
            val docPublicacion = refPublicacion?.let { t.get(it) }
            val docReporte = t.get(refReporte)

            val datos = leerDatos(objetivo, docContenido, docPublicacion)
            if (datos.idAcademia.isNotBlank() && datos.idAcademia !in gestionadas) {
                throw IllegalStateException("Este contenido pertenece a otra academia")
            }
            val reporteActual = docReporte.toObject(Reporte::class.java)

            // 2) Aplicar la decisión sobre el contenido, sin borrarlo (RC-10)
            when (decision) {
                DecisionModeracion.DESCARTADO -> Unit

                DecisionModeracion.OCULTADO -> {
                    // Si se oculta un comentario visible, se resta del contador de la publicación
                    if (objetivo.tipo == TipoContenido.COMENTARIO && refPublicacion != null) {
                        val comentario = docContenido.toObject(Comentario::class.java)
                        if (comentario?.estado == EstadoComentario.VISIBLE) {
                            t.update(refPublicacion, "cantidadComentarios", FieldValue.increment(-1))
                        }
                    }
                    val estadoOculto = when (objetivo.tipo) {
                        TipoContenido.PUBLICACION -> EstadoPublicacion.OCULTA.name
                        TipoContenido.COMENTARIO -> EstadoComentario.OCULTO.name
                        TipoContenido.AVISO -> EstadoAviso.OCULTO.name
                    }
                    t.update(refContenido, mapOf("estado" to estadoOculto, "fechaActualizacion" to ahora))
                }

                DecisionModeracion.CERRADO ->
                    t.update(
                        refContenido,
                        mapOf(
                            "estado" to EstadoAviso.CERRADO.name,
                            "actualizadoPor" to uid,
                            "fechaActualizacion" to ahora
                        )
                    )
            }

            // 3) Cerrar el caso con la última decisión
            if (docReporte.exists()) {
                t.update(
                    refReporte,
                    mapOf(
                        "estado" to EstadoReporte.RESUELTO.name,
                        "decision" to decision.name,
                        "motivoDecision" to motivo.trim(),
                        "idModerador" to uid,
                        "nombreModerador" to perfil.nombre,
                        "fechaDecision" to ahora
                    )
                )
            } else {
                // Moderación directa: se crea el caso ya resuelto, para que quede registrado
                t.set(
                    refReporte,
                    Reporte(
                        id = refReporte.id,
                        tipoContenido = objetivo.tipo,
                        idContenido = objetivo.idContenido,
                        idPublicacion = objetivo.idPublicacion,
                        idAcademia = datos.idAcademia,
                        resumenContenido = datos.resumen.take(500),
                        idAutorContenido = datos.idAutor,
                        nombreAutorContenido = datos.nombreAutor,
                        cantidadReportes = 0,
                        estado = EstadoReporte.RESUELTO,
                        fechaPrimerReporte = ahora,
                        fechaUltimoReporte = ahora,
                        decision = decision,
                        motivoDecision = motivo.trim(),
                        idModerador = uid,
                        nombreModerador = perfil.nombre,
                        fechaDecision = ahora
                    )
                )
            }

            // 4) Auditoría: registro nuevo que nunca se modifica
            t.set(
                refRegistro,
                RegistroModeracion(
                    id = refRegistro.id,
                    idReporte = refReporte.id,
                    tipoContenido = objetivo.tipo,
                    idContenido = objetivo.idContenido,
                    decision = decision,
                    motivo = motivo.trim(),
                    idModerador = uid,
                    nombreModerador = perfil.nombre,
                    cantidadReportesAlDecidir = reporteActual?.cantidadReportes ?: 0,
                    fecha = ahora
                )
            )
            Unit
        }.await()
    }
}