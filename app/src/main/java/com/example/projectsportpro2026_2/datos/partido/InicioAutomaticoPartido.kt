package com.example.projectsportpro2026_2.datos.partido

import com.example.projectsportpro2026_2.datos.modelo.evento.EventoPartido
import com.example.projectsportpro2026_2.datos.modelo.evento.TipoEventoCatalogo
import com.example.projectsportpro2026_2.datos.modelo.partido.EstadoPartido
import com.example.projectsportpro2026_2.datos.modelo.partido.Partido

internal const val ID_INICIO_AUTOMATICO = "INICIO_AUTOMATICO"

internal data class CambioEstadoPartido(
    val eventoInicio: EventoPartido?
)

/** Null significa que una solicitud de inicio ya fue aplicada. */
internal fun planificarCambioEstado(
    partido: Partido,
    nuevoEstado: EstadoPartido,
    inicioExistente: Boolean,
    tipoInicio: TipoEventoCatalogo?,
    uid: String,
    ahora: Long,
    idDispositivo: String
): CambioEstadoPartido? {
    if (partido.estado == EstadoPartido.EN_CURSO && nuevoEstado == EstadoPartido.EN_CURSO) {
        return null
    }
    val valido = when (partido.estado) {
        EstadoPartido.PROGRAMADO ->
            nuevoEstado == EstadoPartido.EN_CURSO || nuevoEstado == EstadoPartido.CANCELADO
        EstadoPartido.EN_CURSO ->
            nuevoEstado == EstadoPartido.FINALIZADO || nuevoEstado == EstadoPartido.CANCELADO
        EstadoPartido.FINALIZADO, EstadoPartido.CANCELADO -> false
    }
    check(valido) { "No se puede cambiar de ${partido.estado} a $nuevoEstado" }

    val evento = if (
        partido.estado == EstadoPartido.PROGRAMADO &&
        nuevoEstado == EstadoPartido.EN_CURSO && !inicioExistente
    ) {
        val tipo = checkNotNull(tipoInicio) { "No se pudo obtener el tipo de evento de inicio" }
        EventoPartido(
            id = ID_INICIO_AUTOMATICO,
            idPartido = partido.id,
            idAcademia = partido.idAcademia,
            idTipoEvento = tipo.id,
            codigoTipoEvento = "INICIO",
            nombreTipoEvento = "Inicio del partido",
            minuto = 1,
            afectaEstadoPartido = true,
            creadoPor = uid,
            fechaCaptura = ahora,
            idDispositivo = idDispositivo
        )
    } else null

    return CambioEstadoPartido(evento)
}
