package com.example.projectsportpro2026_2.ui.inicio

import com.example.projectsportpro2026_2.datos.modelo.Publicacion
import com.example.projectsportpro2026_2.datos.modelo.TipoReaccion
import com.example.projectsportpro2026_2.datos.estadisticas.ConteoAsistencia
import com.example.projectsportpro2026_2.datos.modelo.Asistencia
import com.example.projectsportpro2026_2.datos.modelo.Entrenamiento
import com.example.projectsportpro2026_2.datos.modelo.EstadoAsistencia
import com.example.projectsportpro2026_2.datos.modelo.EstadoEntrenamiento
import com.example.projectsportpro2026_2.datos.modelo.EstadoMensualidad
import com.example.projectsportpro2026_2.datos.modelo.EstadoVinculacion
import com.example.projectsportpro2026_2.datos.modelo.Mensualidad
import com.example.projectsportpro2026_2.datos.modelo.Notificacion
import com.example.projectsportpro2026_2.datos.modelo.Vinculacion
import com.example.projectsportpro2026_2.datos.modelo.convocatoria.EstadoDisponibilidad
import com.example.projectsportpro2026_2.datos.modelo.convocatoria.JugadorConvocado
import com.example.projectsportpro2026_2.datos.modelo.partido.EstadoPartido
import com.example.projectsportpro2026_2.datos.modelo.partido.Partido
import com.example.projectsportpro2026_2.datos.repositorio.CuentaPorVincular
import com.example.projectsportpro2026_2.datos.repositorio.convocatoria.ConvocatoriaParaResponder
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth

/*
 * Rediseño "Línea de cal" · HU-02 / 7.1: lógica pura del inicio.
 *
 * Solo cuenta y elige sobre datos que ya se leyeron; no toca Firestore.
 * Está separada del InicioViewModel para probarla con tests unitarios
 * (app/src/test/.../inicio/LogicaInicioTest.kt).
 */
object LogicaInicio {

    // ---------- Contadores ----------

    /**
     * Comunidad (7.1): las [cantidad] publicaciones más recientes de una lista ya filtrada con
     * filtrarFeedComunidad (el mismo filtro del feed "Para ti").
     */
    fun publicacionesRecientes(publicaciones: List<Publicacion>, cantidad: Int = 3): List<Publicacion> =
        publicaciones.sortedByDescending { it.fechaCreacion }.take(cantidad)

    /** Comunidad (7.1): emojis de las reacciones presentes, en el orden de TipoReaccion. */
    fun emojisReacciones(publicacion: Publicacion): String =
        TipoReaccion.entries
            .filter { publicacion.conteoDe(it) > 0 }
            .joinToString("") { it.emoji }

    /** Campana: notificaciones con leida == false. */
    fun noLeidas(notificaciones: List<Notificacion>): Int =
        notificaciones.count { !it.leida }

    /**
     * ADM · "Cuentas por aprobar" (decisión D-10): las cuentas con pendiente == true
     * de RepositorioCuentasJugador.listar(), igual que la pantalla Cuentas de jugadores.
     */
    fun cuentasPorAprobar(cuentas: List<CuentaPorVincular>): Int =
        cuentas.count { it.pendiente }

    /** ADM · vinculaciones en estado PENDIENTE. */
    fun vinculacionesPendientes(vinculaciones: List<Vinculacion>): Int =
        vinculaciones.count { it.estado == EstadoVinculacion.PENDIENTE }

    /** ADM / PAD · mensualidades en estado VENCIDO. */
    fun mensualidadesVencidas(mensualidades: List<Mensualidad>): Int =
        mensualidades.count { it.estado == EstadoMensualidad.VENCIDO }

    /** JUG / PAD · convocatorias que todavía esperan respuesta. */
    fun convocatoriasPorResponder(convocatorias: List<ConvocatoriaParaResponder>): Int =
        convocatorias.count { esPorResponder(it) }

    /** DT · "respondieron x/y": x = respuestas distintas de PENDIENTE, y = convocados. */
    fun respuestasConvocatoria(convocados: List<JugadorConvocado>): Respuestas =
        Respuestas(
            respondieron = convocados.count { it.estado != EstadoDisponibilidad.PENDIENTE },
            total = convocados.size
        )

    /** JUG · porcentaje de asistencia (mismo criterio que Estadísticas: presentes / registros). */
    fun porcentajeAsistencia(asistencias: List<Asistencia>): Int? =
        ConteoAsistencia(
            presente = asistencias.count { it.estado == EstadoAsistencia.PRESENTE },
            ausente = asistencias.count { it.estado == EstadoAsistencia.AUSENTE },
            justificado = asistencias.count { it.estado == EstadoAsistencia.JUSTIFICADO }
        ).porcentaje

    // ---------- Selección ----------

    /** Partido EN_CURSO que se muestra en el marcador (el primero, si hay varios). */
    fun <T> partidoEnCurso(partidos: List<T>, partidoDe: (T) -> Partido): T? =
        partidos.firstOrNull { partidoDe(it).estado == EstadoPartido.EN_CURSO }

    /** Próximo partido PROGRAMADO desde `ahora` (fecha aaaa-MM-dd + hora HH:mm). */
    fun <T> proximoPartido(partidos: List<T>, ahora: LocalDateTime, partidoDe: (T) -> Partido): T? =
        partidos
            .filter { partidoDe(it).estado == EstadoPartido.PROGRAMADO }
            .mapNotNull { item -> fechaHora(partidoDe(item))?.let { item to it } }
            .filter { (_, cuando) -> !cuando.isBefore(ahora) }
            .minByOrNull { (_, cuando) -> cuando }
            ?.first

    /** DT · próximo entrenamiento PUBLICADO desde `ahoraMillis`. */
    fun proximoEntrenamiento(entrenamientos: List<Entrenamiento>, ahoraMillis: Long): Entrenamiento? =
        entrenamientos
            .filter { it.estado == EstadoEntrenamiento.PUBLICADO && it.fechaHoraEpochMillis >= ahoraMillis }
            .minByOrNull { it.fechaHoraEpochMillis }

    /** JUG · la convocatoria por responder del partido más cercano. */
    fun convocatoriaPorResponder(convocatorias: List<ConvocatoriaParaResponder>): ConvocatoriaParaResponder? =
        convocatorias
            .filter { esPorResponder(it) }
            .minWithOrNull(compareBy<ConvocatoriaParaResponder> { it.partido.fecha }.thenBy { it.partido.hora })

    /**
     * Estado de la mensualidad del periodo actual de un jugador.
     * null si todavía no hay mensualidad registrada para ese periodo.
     */
    fun estadoMensualidad(
        mensualidades: List<Mensualidad>,
        idJugador: String,
        periodo: String = YearMonth.now().toString()
    ): EstadoMensualidad? =
        mensualidades
            .filter { it.idJugador == idJugador && it.periodo == periodo }
            .maxByOrNull { it.fechaActualizacion }
            ?.estado

    // ---------- Apoyo ----------

    private fun esPorResponder(c: ConvocatoriaParaResponder): Boolean =
        c.convocado.estado == EstadoDisponibilidad.PENDIENTE &&
                c.partido.estado == EstadoPartido.PROGRAMADO

    /** Fecha y hora del partido; null si los textos no tienen el formato esperado. */
    fun fechaHora(partido: Partido): LocalDateTime? =
        try {
            val fecha = LocalDate.parse(partido.fecha.trim())
            val hora = if (partido.hora.isBlank()) LocalTime.MIDNIGHT else LocalTime.parse(partido.hora.trim())
            LocalDateTime.of(fecha, hora)
        } catch (e: Exception) {
            null
        }
}

data class Respuestas(val respondieron: Int, val total: Int)
