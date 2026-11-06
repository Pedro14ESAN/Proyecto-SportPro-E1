package com.example.projectsportpro2026_2.datos.seguridad

import com.example.projectsportpro2026_2.datos.modelo.Rol
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch

/*
 * HU25 - Las operaciones críticas registran al usuario
 * ejecutor. Documento: auditoria/{id}
 *
 * Las correcciones de eventos (HU15) y la moderación (HU24)
 * ya guardan su propia traza; aquí se registran las demás
 * operaciones críticas (aprobación/rechazo del resumen IA,
 * accesos a datos sensibles).
 *
 * Las reglas de Firestore solo permiten crear registros con
 * uid == request.auth.uid y nunca modificarlos ni borrarlos.
 */
data class RegistroAuditoriaDoc(
    val id: String = "",
    val accion: String = "",
    val uid: String = "",
    val rol: Rol = Rol.JUG,
    val recurso: String = "",          // p. ej. "resumenesPartido"
    val idRecurso: String = "",
    val idAcademia: String = "",
    val detalle: String = "",
    val fecha: Long = 0L
)

object AccionesAuditoria {
    const val RESUMEN_GENERADO = "RESUMEN_GENERADO"
    const val RESUMEN_EDITADO = "RESUMEN_EDITADO"
    const val RESUMEN_APROBADO = "RESUMEN_APROBADO"
    const val RESUMEN_RECHAZADO = "RESUMEN_RECHAZADO"
    const val RESUMEN_DESACTUALIZADO = "RESUMEN_DESACTUALIZADO"
    const val LECTURA_DATOS_SENSIBLES = "LECTURA_DATOS_SENSIBLES"
}

class RegistroAuditoria(
    private val bd: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val auditoria = bd.collection("auditoria")

    // Agrega el registro al mismo lote de la operación crítica
    fun agregarA(
        lote: WriteBatch,
        accion: String,
        uid: String,
        rol: Rol,
        recurso: String,
        idRecurso: String,
        idAcademia: String,
        detalle: String = ""
    ) {
        val documento = auditoria.document()
        lote.set(
            documento,
            RegistroAuditoriaDoc(
                id = documento.id,
                accion = accion,
                uid = uid,
                rol = rol,
                recurso = recurso,
                idRecurso = idRecurso,
                idAcademia = idAcademia,
                detalle = detalle.take(500),
                fecha = System.currentTimeMillis()
            )
        )
    }

    // Registro suelto: si falla no bloquea la operación del usuario
    fun registrar(
        accion: String,
        uid: String,
        rol: Rol,
        recurso: String,
        idRecurso: String,
        idAcademia: String,
        detalle: String = ""
    ) {
        val lote = bd.batch()
        agregarA(lote, accion, uid, rol, recurso, idRecurso, idAcademia, detalle)
        lote.commit()
    }
}
