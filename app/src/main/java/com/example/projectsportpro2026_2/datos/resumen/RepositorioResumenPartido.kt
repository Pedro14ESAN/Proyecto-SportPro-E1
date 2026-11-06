package com.example.projectsportpro2026_2.datos.resumen

import com.example.projectsportpro2026_2.datos.modelo.Equipo
import com.example.projectsportpro2026_2.datos.modelo.Rol
import com.example.projectsportpro2026_2.datos.modelo.evento.EventoPartido
import com.example.projectsportpro2026_2.datos.modelo.partido.EstadoPartido
import com.example.projectsportpro2026_2.datos.modelo.partido.Partido
import com.example.projectsportpro2026_2.datos.modelo.resumen.EstadoResumen
import com.example.projectsportpro2026_2.datos.modelo.resumen.EvaluacionResumen
import com.example.projectsportpro2026_2.datos.modelo.resumen.OrigenResumen
import com.example.projectsportpro2026_2.datos.modelo.resumen.ResumenPartido
import com.example.projectsportpro2026_2.datos.modelo.resumen.TipoVersionResumen
import com.example.projectsportpro2026_2.datos.modelo.resumen.VersionResumen
import com.example.projectsportpro2026_2.datos.seguridad.AccesoDenegadoException
import com.example.projectsportpro2026_2.datos.seguridad.AccionesAuditoria
import com.example.projectsportpro2026_2.datos.seguridad.ContextoAcceso
import com.example.projectsportpro2026_2.datos.seguridad.ControlAcceso
import com.example.projectsportpro2026_2.datos.seguridad.RegistroAuditoria
import com.example.projectsportpro2026_2.datos.seguridad.RepositorioPermisos
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/*
 * HU26 (generación) y HU27 (revisión, edición y aprobación).
 *
 * La API key del proveedor de IA NUNCA está en el APK: la llamada
 * pasa por Firebase AI Logic (ver GeneradorResumenIA). Como
 * alternativa con plan Blaze queda la Cloud Function de functions/.
 * El texto siempre se guarda como BORRADOR.
 */

data class ContextoResumen(
    val partido: Partido,
    val equipo: Equipo,
    val acceso: ContextoAcceso,
    val puedeRevisar: Boolean,       // DT autorizado: genera, edita, aprueba
    val puedeVerBorrador: Boolean    // DT o ADM de la academia
)

// El proveedor de IA no respondió (sin red, cuota agotada, modelo caído)
class ServicioIANoDisponibleException(mensaje: String) : IllegalStateException(mensaje)

class RepositorioResumenPartido(
    private val bd: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val permisos: RepositorioPermisos = RepositorioPermisos(bd),
    private val auditoria: RegistroAuditoria = RegistroAuditoria(bd)
) {

    private val partidos = bd.collection("partidos")
    private val equipos = bd.collection("equipos")
    private val resumenes = bd.collection("resumenesPartido")

    private suspend fun <T> intentar(bloque: suspend () -> T): Result<T> =
        try {
            Result.success(bloque())
        } catch (e: Exception) {
            Result.failure(e)
        }

    suspend fun prepararContexto(idPartido: String, uid: String): Result<ContextoResumen> =
        intentar {
            val acceso = permisos.contexto(uid)

            val docPartido = partidos.document(idPartido).get().await()
            val partido = docPartido.toObject(Partido::class.java)?.copy(id = docPartido.id)
                ?: throw AccesoDenegadoException()

            val docEquipo = equipos.document(partido.idEquipo).get().await()
            val equipo = docEquipo.toObject(Equipo::class.java)?.copy(id = docEquipo.id)
                ?: Equipo(id = partido.idEquipo, nombre = "Mi equipo", idAcademia = partido.idAcademia)

            // HU25: sin relación con el equipo no se revela nada del partido
            if (!ControlAcceso.puedeVerEquipo(acceso, equipo.id, partido.idAcademia)) {
                throw AccesoDenegadoException()
            }

            ContextoResumen(
                partido = partido,
                equipo = equipo,
                acceso = acceso,
                puedeRevisar = ControlAcceso.puedeRevisarResumen(acceso, equipo.id, partido.idAcademia),
                puedeVerBorrador = ControlAcceso.puedeLeerBorradorResumen(
                    acceso, equipo.id, partido.idAcademia
                )
            )
        }

    // Resumen en tiempo real (null si aún no existe)
    fun escuchar(idPartido: String): Flow<ResumenPartido?> = callbackFlow {
        val registro = resumenes.document(idPartido).addSnapshotListener { doc, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(doc?.takeIf { it.exists() }?.toObject(ResumenPartido::class.java))
        }
        awaitClose { registro.remove() }
    }

    suspend fun historial(idPartido: String): Result<List<VersionResumen>> = intentar {
        resumenes.document(idPartido).collection("versiones").get().await()
            .documents.mapNotNull { d -> d.toObject(VersionResumen::class.java)?.copy(id = d.id) }
            .sortedByDescending { it.fecha }
    }

    private fun exigirRevisor(contexto: ContextoResumen) {
        if (!contexto.puedeRevisar) {
            throw AccesoDenegadoException(
                "Solo el director técnico autorizado del equipo puede gestionar el resumen."
            )
        }
    }

    private fun validarPrecondiciones(
        contexto: ContextoResumen,
        eventos: List<EventoPartido>,
        hayPendientesSync: Boolean
    ) {
        exigirRevisor(contexto)

        if (contexto.partido.estado != EstadoPartido.FINALIZADO) {
            throw IllegalStateException("El resumen solo se genera cuando el partido está finalizado.")
        }
        if (hayPendientesSync) {
            throw IllegalStateException(
                "Hay eventos pendientes de sincronizar. Espera a que se sincronicen para generar un resumen confiable."
            )
        }
        if (!FuenteResumen.suficientes(eventos)) {
            throw IllegalStateException(
                "No hay suficientes eventos registrados para generar un resumen fiable " +
                        "(mínimo ${FuenteResumen.MINIMO_EVENTOS_JUEGO} eventos de juego válidos)."
            )
        }
    }

    /*
     * Flujo principal HU26: el DT pulsa "Generar resumen".
     * 1) se validan precondiciones, 2) se toman los eventos válidos,
     * 3) se arma la entrada estructurada sin datos sensibles,
     * 4) Firebase AI Logic la envía a Gemini (sin API key en el APK),
     * 5) el texto se guarda como BORRADOR. Devuelve la versión creada.
     */
    suspend fun generarConIA(
        contexto: ContextoResumen,
        eventos: List<EventoPartido>,
        hayPendientesSync: Boolean,
        actual: ResumenPartido?
    ): Result<Int> = intentar {
        validarPrecondiciones(contexto, eventos, hayPendientesSync)

        val resultado = try {
            GeneradorResumenIA.generar(
                partido = contexto.partido,
                nombreEquipo = contexto.equipo.nombre.ifBlank { "Mi equipo" },
                eventos = eventos
            )
        } catch (e: Exception) {
            // Si falla la IA no se toca el partido ni los eventos: se puede reintentar
            throw ServicioIANoDisponibleException(
                "El servicio de IA no respondió. Los eventos se conservan: puedes reintentar."
            )
        }

        guardarBorradorNuevo(
            contexto = contexto,
            eventos = eventos,
            actual = actual,
            texto = resultado.texto,
            origen = OrigenResumen.IA,
            modelo = resultado.modelo,
            nota = "Generado con ${resultado.modelo}"
        )
    }

    /*
     * Respaldo: borrador sin IA con los mismos eventos válidos.
     * Queda como BORRADOR (origen PLANTILLA) y exige revisión.
     */
    suspend fun generarPlantilla(
        contexto: ContextoResumen,
        eventos: List<EventoPartido>,
        hayPendientesSync: Boolean,
        actual: ResumenPartido?
    ): Result<Int> = intentar {
        validarPrecondiciones(contexto, eventos, hayPendientesSync)

        val texto = FuenteResumen.redactarPlantilla(
            eventos = eventos,
            nombreEquipo = contexto.equipo.nombre.ifBlank { "Mi equipo" },
            nombreRival = contexto.partido.nombreRival.ifBlank { "Rival" }
        )

        guardarBorradorNuevo(
            contexto = contexto,
            eventos = eventos,
            actual = actual,
            texto = texto,
            origen = OrigenResumen.PLANTILLA,
            modelo = "",
            nota = "Borrador sin IA (servicio IA no disponible)"
        )
    }

    /*
     * Guarda una nueva versión como BORRADOR, con fecha de
     * generación y el conjunto de eventos usado (ids + huella),
     * más su entrada en el historial y la auditoría.
     */
    private suspend fun guardarBorradorNuevo(
        contexto: ContextoResumen,
        eventos: List<EventoPartido>,
        actual: ResumenPartido?,
        texto: String,
        origen: OrigenResumen,
        modelo: String,
        nota: String
    ): Int {
        val uid = contexto.acceso.uid
        val ahora = System.currentTimeMillis()
        val version = (actual?.version ?: 0) + 1
        val validos = FuenteResumen.eventosValidos(eventos)
        val huella = FuenteResumen.huella(eventos)

        val resumen = ResumenPartido(
            idPartido = contexto.partido.id,
            idAcademia = contexto.partido.idAcademia,
            idEquipo = contexto.equipo.id,
            estado = EstadoResumen.BORRADOR,
            origen = origen,
            modeloIA = modelo,
            version = version,
            textoOriginalIA = texto,
            textoEditado = texto,
            idsEventosUsados = validos.map { it.id },
            huellaEventos = huella,
            totalEventos = validos.size,
            generadoPor = uid,
            fechaGeneracion = ahora
        )

        val lote = bd.batch()
        val referencia = resumenes.document(contexto.partido.id)
        lote.set(referencia, resumen)
        lote.set(
            referencia.collection("versiones").document(),
            VersionResumen(
                version = version,
                tipo = TipoVersionResumen.GENERADA,
                origen = origen,
                texto = texto,
                huellaEventos = huella,
                totalEventos = validos.size,
                autor = uid,
                fecha = ahora,
                nota = nota
            )
        )
        auditoria.agregarA(
            lote, AccionesAuditoria.RESUMEN_GENERADO, uid, contexto.acceso.rol,
            "resumenesPartido", contexto.partido.id, contexto.partido.idAcademia,
            "versión $version · ${origen.name} ${modelo} · ${validos.size} eventos"
        )
        lote.commit().await()
        return version
    }

    // HU27: el borrador es editable antes de aprobar
    suspend fun guardarEdicion(
        contexto: ContextoResumen,
        actual: ResumenPartido,
        texto: String
    ): Result<Unit> = intentar {
        exigirRevisor(contexto)
        if (actual.estado != EstadoResumen.BORRADOR) {
            throw IllegalStateException("Solo se puede editar un borrador.")
        }
        val limpio = texto.trim()
        if (limpio.isBlank()) throw IllegalArgumentException("El resumen no puede quedar vacío.")

        resumenes.document(contexto.partido.id).update(
            mapOf(
                "textoEditado" to limpio,
                "editadoPor" to contexto.acceso.uid,
                "fechaEdicion" to System.currentTimeMillis()
            )
        ).await()
    }

    /*
     * HU27: aprobación. Guarda responsable, fecha/hora, versión
     * final y evaluación; conserva el texto original de la IA.
     * Si los eventos cambiaron desde la generación, no se aprueba.
     */
    suspend fun aprobar(
        contexto: ContextoResumen,
        actual: ResumenPartido,
        textoFinal: String,
        evaluacion: EvaluacionResumen,
        eventosActuales: List<EventoPartido>
    ): Result<Unit> = intentar {
        exigirRevisor(contexto)

        if (actual.estado != EstadoResumen.BORRADOR) {
            throw IllegalStateException("Solo se puede aprobar un borrador.")
        }
        val limpio = textoFinal.trim()
        if (limpio.isBlank()) throw IllegalArgumentException("El resumen no puede quedar vacío.")
        if (evaluacion.utilidad !in 1..5) {
            throw IllegalArgumentException("Califica la utilidad del resumen (1 a 5) antes de aprobar.")
        }
        if (FuenteResumen.huella(eventosActuales) != actual.huellaEventos) {
            throw IllegalStateException(
                "Los eventos del partido cambiaron desde que se generó este borrador. Genera uno nuevo."
            )
        }

        val uid = contexto.acceso.uid
        val ahora = System.currentTimeMillis()
        val referencia = resumenes.document(contexto.partido.id)

        val lote = bd.batch()
        lote.update(
            referencia,
            mapOf(
                "estado" to EstadoResumen.APROBADO,
                "textoEditado" to limpio,
                "textoFinal" to limpio,
                "aprobadoPor" to uid,
                "nombreAprobador" to contexto.acceso.perfil.nombre,
                "fechaAprobacion" to ahora,
                "evaluacion" to evaluacion
            )
        )
        lote.set(
            referencia.collection("versiones").document(),
            VersionResumen(
                version = actual.version,
                tipo = TipoVersionResumen.APROBADA,
                origen = actual.origen,
                texto = limpio,
                huellaEventos = actual.huellaEventos,
                totalEventos = actual.totalEventos,
                autor = uid,
                fecha = ahora,
                nota = "Utilidad ${evaluacion.utilidad}/5"
            )
        )
        auditoria.agregarA(
            lote, AccionesAuditoria.RESUMEN_APROBADO, uid, contexto.acceso.rol,
            "resumenesPartido", contexto.partido.id, contexto.partido.idAcademia,
            "versión ${actual.version}"
        )
        lote.commit().await()
    }

    // Excepción HU27: rechazar y pedir nueva generación, conservando historial
    suspend fun rechazar(
        contexto: ContextoResumen,
        actual: ResumenPartido,
        motivo: String
    ): Result<Unit> = intentar {
        exigirRevisor(contexto)
        if (actual.estado != EstadoResumen.BORRADOR) {
            throw IllegalStateException("Solo se puede rechazar un borrador.")
        }
        val razon = motivo.trim()
        if (razon.length < 5) throw IllegalArgumentException("Indica el motivo del rechazo.")

        val uid = contexto.acceso.uid
        val ahora = System.currentTimeMillis()
        val referencia = resumenes.document(contexto.partido.id)

        val lote = bd.batch()
        lote.update(
            referencia,
            mapOf(
                "estado" to EstadoResumen.RECHAZADO,
                "rechazadoPor" to uid,
                "motivoRechazo" to razon,
                "fechaRechazo" to ahora
            )
        )
        lote.set(
            referencia.collection("versiones").document(),
            VersionResumen(
                version = actual.version,
                tipo = TipoVersionResumen.RECHAZADA,
                origen = actual.origen,
                texto = actual.textoEditado.ifBlank { actual.textoOriginalIA },
                huellaEventos = actual.huellaEventos,
                totalEventos = actual.totalEventos,
                autor = uid,
                fecha = ahora,
                nota = razon
            )
        )
        auditoria.agregarA(
            lote, AccionesAuditoria.RESUMEN_RECHAZADO, uid, contexto.acceso.rol,
            "resumenesPartido", contexto.partido.id, contexto.partido.idAcademia, razon
        )
        lote.commit().await()
    }

    /*
     * RN-13: si se corrige o anula un evento después de aprobar,
     * el resumen queda DESACTUALIZADO y requiere nueva revisión.
     * Se llama desde HU15 (corrección/anulación) y al abrir la
     * pantalla si la huella ya no coincide.
     */
    suspend fun marcarDesactualizadoSiCorresponde(
        idPartido: String,
        uid: String,
        rol: Rol,
        motivo: String,
        huellaActual: String? = null
    ): Result<Boolean> = intentar {
        val referencia = resumenes.document(idPartido)
        val actual = referencia.get().await().toObject(ResumenPartido::class.java)
            ?: return@intentar false

        if (actual.estado != EstadoResumen.APROBADO) return@intentar false
        if (huellaActual != null && huellaActual == actual.huellaEventos) return@intentar false

        val ahora = System.currentTimeMillis()
        val lote = bd.batch()
        lote.update(
            referencia,
            mapOf(
                "estado" to EstadoResumen.DESACTUALIZADO,
                "motivoDesactualizado" to motivo,
                "fechaDesactualizado" to ahora
            )
        )
        lote.set(
            referencia.collection("versiones").document(),
            VersionResumen(
                version = actual.version,
                tipo = TipoVersionResumen.DESACTUALIZADA,
                origen = actual.origen,
                texto = actual.textoFinal,
                huellaEventos = actual.huellaEventos,
                totalEventos = actual.totalEventos,
                autor = uid,
                fecha = ahora,
                nota = motivo
            )
        )
        auditoria.agregarA(
            lote, AccionesAuditoria.RESUMEN_DESACTUALIZADO, uid, rol,
            "resumenesPartido", idPartido, actual.idAcademia, motivo
        )
        lote.commit().await()
        true
    }
}
