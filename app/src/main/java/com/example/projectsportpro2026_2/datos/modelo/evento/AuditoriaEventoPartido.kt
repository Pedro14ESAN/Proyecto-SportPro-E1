package com.example.projectsportpro2026_2.datos.modelo.evento

data class DatosEventoAuditoria(
    val idTipoEvento: String = "",
    val codigoTipoEvento: String = "",
    val nombreTipoEvento: String = "",

    val minuto: Int = 0,

    val equipoEvento: EquipoEvento =
        EquipoEvento.NINGUNO,

    val idJugador: String = "",
    val nombreJugador: String = "",

    val observacion: String = "",

    val afectaMarcador: Boolean = false,
    val afectaEstadoPartido: Boolean = false,

    val estado: EstadoEventoPartido =
        EstadoEventoPartido.ACTIVO
)

data class AuditoriaEventoPartido(
    val id: String = "",

    val idEvento: String = "",
    val idPartido: String = "",

    val tipoCambio: TipoCambioEvento =
        TipoCambioEvento.CORRECCION,

    val versionAnterior: Int = 1,
    val versionNueva: Int = 1,

    val motivo: String = "",

    val autor: String = "",
    val fechaCambio: Long = 0L,

    val valoresAnteriores:
    DatosEventoAuditoria =
        DatosEventoAuditoria(),

    val valoresNuevos:
    DatosEventoAuditoria =
        DatosEventoAuditoria()
)

enum class TipoCambioEvento {
    CORRECCION,
    ANULACION
}