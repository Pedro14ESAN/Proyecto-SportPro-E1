package com.example.projectsportpro2026_2.datos.modelo.resumen

/*
 * HU26 / HU27 - Resumen narrativo del partido.
 *
 * Documento: resumenesPartido/{idPartido}
 * Historial: resumenesPartido/{idPartido}/versiones/{id}
 *
 * Se conservan SIEMPRE la versión original de la IA
 * (textoOriginalIA) y la versión final editada (textoFinal).
 */
enum class EstadoResumen {
    // Generado, pendiente de revisión del DT (nunca se publica así)
    BORRADOR,

    // Revisado y aprobado por un DT autorizado
    APROBADO,

    // El DT lo rechazó y pidió una nueva generación
    RECHAZADO,

    // RN-13: se corrigió o anuló un evento después de aprobar
    DESACTUALIZADO
}

enum class OrigenResumen {
    // Generado por el proveedor de IA desde la Cloud Function
    IA,

    // Respaldo sin IA (plantilla con los mismos eventos) cuando el
    // servicio de IA no está disponible. Igual requiere revisión.
    PLANTILLA
}

// Formulario de evaluación del DT (HU27)
data class EvaluacionResumen(
    val utilidad: Int = 0,                 // 1 a 5
    val omisiones: String = "",
    val erroresOInvenciones: String = "",
    val correccionesRealizadas: String = ""
)

data class ResumenPartido(
    val idPartido: String = "",
    val idAcademia: String = "",
    val idEquipo: String = "",

    val estado: EstadoResumen = EstadoResumen.BORRADOR,
    val origen: OrigenResumen = OrigenResumen.IA,
    val modeloIA: String = "",

    // Número de generación (1, 2, 3…) dentro del historial
    val version: Int = 1,

    val textoOriginalIA: String = "",
    val textoEditado: String = "",
    val textoFinal: String = "",

    // Conjunto de eventos usado: ids y huella "id:versión"
    val idsEventosUsados: List<String> = emptyList(),
    val huellaEventos: String = "",
    val totalEventos: Int = 0,

    val generadoPor: String = "",
    val fechaGeneracion: Long = 0L,

    val editadoPor: String = "",
    val fechaEdicion: Long = 0L,

    val aprobadoPor: String = "",
    val nombreAprobador: String = "",
    val fechaAprobacion: Long = 0L,

    val evaluacion: EvaluacionResumen = EvaluacionResumen(),

    val rechazadoPor: String = "",
    val motivoRechazo: String = "",
    val fechaRechazo: Long = 0L,

    val motivoDesactualizado: String = "",
    val fechaDesactualizado: Long = 0L
)

enum class TipoVersionResumen { GENERADA, EDITADA, APROBADA, RECHAZADA, DESACTUALIZADA }

data class VersionResumen(
    val id: String = "",
    val version: Int = 1,
    val tipo: TipoVersionResumen = TipoVersionResumen.GENERADA,
    val origen: OrigenResumen = OrigenResumen.IA,
    val texto: String = "",
    val huellaEventos: String = "",
    val totalEventos: Int = 0,
    val autor: String = "",
    val fecha: Long = 0L,
    val nota: String = ""
)

/*
 * Nota de evaluación de cada narración aprobada (HU27).
 *
 * Colección: evaluacionesResumen/{idPartido}_v{version}
 * Un documento por narración aprobada de cada partido. A diferencia
 * del campo "evaluacion" de resumenesPartido (que se reemplaza al
 * generar una nueva versión), aquí la nota queda guardada para siempre.
 */
data class EvaluacionNarracion(
    val id: String = "",
    val idPartido: String = "",
    val idAcademia: String = "",
    val idEquipo: String = "",
    val nombreRival: String = "",

    // Narración evaluada
    val version: Int = 1,
    val origen: OrigenResumen = OrigenResumen.IA,
    val modeloIA: String = "",
    val textoFinal: String = "",
    val totalEventos: Int = 0,

    // Nota del DT
    val utilidad: Int = 0,                 // 1 a 5
    val omisiones: String = "",
    val erroresOInvenciones: String = "",
    val correccionesRealizadas: String = "",
    // true si el DT cambió el texto de la IA antes de aprobar
    val textoModificado: Boolean = false,

    val evaluadoPor: String = "",
    val nombreEvaluador: String = "",
    val fechaEvaluacion: Long = 0L
)

/*
 * Comentario de un DT sobre la narración aprobada (HU27).
 *
 * Colección: comentariosResumen/{id}
 * Solo lo escriben y lo ven los DT autorizados del equipo o de la
 * academia del partido (ni jugadores, ni padres, ni ADM).
 */
data class ComentarioNarracion(
    val id: String = "",
    val idPartido: String = "",
    val idAcademia: String = "",
    val idEquipo: String = "",
    // Versión de la narración aprobada que se comenta
    val version: Int = 1,
    val texto: String = "",
    val autor: String = "",
    val nombreAutor: String = "",
    val fecha: Long = 0L
)
