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
