package com.example.projectsportpro2026_2.datos.estadisticas

import com.example.projectsportpro2026_2.datos.modelo.Asistencia
import com.example.projectsportpro2026_2.datos.modelo.Entrenamiento
import com.example.projectsportpro2026_2.datos.modelo.EstadoAsistencia
import com.example.projectsportpro2026_2.datos.modelo.EstadoEntrenamiento
import com.example.projectsportpro2026_2.datos.modelo.evento.EquipoEvento
import com.example.projectsportpro2026_2.datos.modelo.evento.EstadoEventoPartido
import com.example.projectsportpro2026_2.datos.modelo.evento.EventoPartido
import com.example.projectsportpro2026_2.datos.modelo.partido.EstadoPartido
import com.example.projectsportpro2026_2.datos.modelo.partido.Partido
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/*
 * HU18 - Estadísticas acumuladas por jugador y equipo.
 *
 * Reglas de cálculo (criterios de aceptación):
 * - Solo se usa lo registrado en SportPro: partidos, eventos
 *   (HU14/HU15), entrenamientos y asistencias (HU08/HU09).
 * - No se calcula posesión, distancia ni tiros: no hay fuente.
 * - Solo eventos válidos: los ANULADOS se excluyen y de los
 *   corregidos se usa la versión vigente (el documento del
 *   evento ya guarda la última versión), así una corrección
 *   actualiza los acumulados automáticamente.
 * - Partido jugado = partido FINALIZADO.
 *
 * Es código puro (sin Firebase) para poder probarlo con JUnit.
 */

// ---------- Periodo ----------

data class PeriodoEstadisticas(
    val clave: String,
    val etiqueta: String,
    val desde: LocalDate? = null,     // null = sin límite
    val hasta: LocalDate? = null
) {
    fun incluye(fechaTexto: String): Boolean {
        if (desde == null && hasta == null) return true
        val fecha = parsearFecha(fechaTexto) ?: return false
        if (desde != null && fecha.isBefore(desde)) return false
        if (hasta != null && fecha.isAfter(hasta)) return false
        return true
    }

    companion object {
        val TODO = PeriodoEstadisticas("todo", "Todo el historial")

        fun temporada(anio: Int) = PeriodoEstadisticas(
            clave = "anio-$anio",
            etiqueta = "$anio · Temporada completa",
            desde = LocalDate.of(anio, 1, 1),
            hasta = LocalDate.of(anio, 12, 31)
        )

        fun mes(mes: YearMonth): PeriodoEstadisticas {
            val nombre = mes.month
                .getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("es-PE"))
                .replace(".", "")
                .replaceFirstChar { it.uppercase() }
            return PeriodoEstadisticas(
                clave = "mes-$mes",
                etiqueta = "$nombre ${mes.year}",
                desde = mes.atDay(1),
                hasta = mes.atEndOfMonth()
            )
        }
    }
}

fun parsearFecha(texto: String): LocalDate? =
    try {
        LocalDate.parse(texto.trim())
    } catch (e: Exception) {
        null
    }

// ---------- Resultados ----------

data class EstadisticasEquipo(
    val partidosJugados: Int = 0,
    val ganados: Int = 0,
    val empatados: Int = 0,
    val perdidos: Int = 0,
    val golesFavor: Int = 0,
    val golesContra: Int = 0,
    val tarjetasAmarillas: Int = 0,
    val tarjetasRojas: Int = 0,
    val cambios: Int = 0,
    val faltas: Int = 0,
    val sesionesEntrenamiento: Int = 0,
    val eventosRegistrados: Int = 0,
    val eventosAnuladosExcluidos: Int = 0,
    // null = no hay asistencias registradas (se muestra "Sin datos")
    val porcentajeAsistencia: Int? = null
) {
    val hayPartidos: Boolean get() = partidosJugados > 0
    val balanceGoles: Int get() = golesFavor - golesContra
}

data class ConteoAsistencia(
    val presente: Int = 0,
    val ausente: Int = 0,
    val justificado: Int = 0
) {
    val total: Int get() = presente + ausente + justificado

    // null cuando no hay registros: "Sin datos" en lugar de 0 %
    val porcentaje: Int?
        get() = if (total == 0) null else Math.round(presente * 100f / total)
}

data class EstadisticasJugador(
    val idJugador: String,
    val participaciones: Int = 0,
    val goles: Int = 0,
    val tarjetasAmarillas: Int = 0,
    val tarjetasRojas: Int = 0,
    val faltas: Int = 0,
    val asistencia: ConteoAsistencia = ConteoAsistencia()
)

data class SesionPeriodo(
    val idEntrenamiento: String,
    val fecha: String,
    val objetivo: String,
    val asistencia: ConteoAsistencia
)

// Todo lo que se leyó de Firestore para un equipo
data class DatosFuenteEquipo(
    val partidos: List<Partido> = emptyList(),
    // Todos los eventos (activos y anulados) por id de partido
    val eventosPorPartido: Map<String, List<EventoPartido>> = emptyMap(),
    // Jugadores alineados (titulares y suplentes) por id de partido
    val alineadosPorPartido: Map<String, Set<String>> = emptyMap(),
    val entrenamientos: List<Entrenamiento> = emptyList(),
    val asistencias: List<Asistencia> = emptyList()
)

object CalculadoraEstadisticas {

    private const val TARJETA = "TARJETA"
    private const val FALTA = "FALTA"
    private const val CAMBIO = "CAMBIO"

    fun esTarjetaRoja(e: EventoPartido): Boolean =
        e.codigoTipoEvento.contains("ROJA", ignoreCase = true) ||
                (
                        e.codigoTipoEvento == TARJETA &&
                                (
                                        e.nombreTipoEvento.contains("roja", ignoreCase = true) ||
                                                e.observacion.contains("roja", ignoreCase = true)
                                        )
                        )

    fun esTarjetaAmarilla(e: EventoPartido): Boolean =
        !esTarjetaRoja(e) &&
                (
                        e.codigoTipoEvento == TARJETA ||
                                e.codigoTipoEvento.contains("AMARILLA", ignoreCase = true)
                        )

    fun esFalta(e: EventoPartido): Boolean = e.codigoTipoEvento == FALTA

    fun esCambio(e: EventoPartido): Boolean = e.codigoTipoEvento == CAMBIO

    fun esGol(e: EventoPartido): Boolean = e.afectaMarcador

    // Solo la versión vigente y no anulada
    fun eventosValidos(eventos: List<EventoPartido>): List<EventoPartido> =
        eventos.filter { it.estado == EstadoEventoPartido.ACTIVO }

    fun partidosJugados(
        fuente: DatosFuenteEquipo,
        periodo: PeriodoEstadisticas
    ): List<Partido> =
        fuente.partidos.filter {
            it.estado == EstadoPartido.FINALIZADO && periodo.incluye(it.fecha)
        }

    // Sesiones ya realizadas (publicadas y con fecha hasta hoy)
    fun sesionesRealizadas(
        fuente: DatosFuenteEquipo,
        periodo: PeriodoEstadisticas,
        hoy: LocalDate = LocalDate.now()
    ): List<Entrenamiento> =
        fuente.entrenamientos.filter { sesion ->
            val fecha = parsearFecha(sesion.fecha)
            sesion.estado == EstadoEntrenamiento.PUBLICADO &&
                    fecha != null &&
                    !fecha.isAfter(hoy) &&
                    periodo.incluye(sesion.fecha)
        }

    fun calcularEquipo(
        fuente: DatosFuenteEquipo,
        periodo: PeriodoEstadisticas,
        hoy: LocalDate = LocalDate.now()
    ): EstadisticasEquipo {

        val jugados = partidosJugados(fuente, periodo)

        var ganados = 0
        var empatados = 0
        var perdidos = 0
        var favor = 0
        var contra = 0
        var amarillas = 0
        var rojas = 0
        var cambios = 0
        var faltas = 0
        var registrados = 0
        var anulados = 0

        jugados.forEach { partido ->
            val todos = fuente.eventosPorPartido[partido.id].orEmpty()
            val validos = eventosValidos(todos)

            anulados += todos.size - validos.size
            registrados += validos.size

            val golesPropios = validos.count { esGol(it) && it.equipoEvento == EquipoEvento.PROPIO }
            val golesRival = validos.count { esGol(it) && it.equipoEvento == EquipoEvento.RIVAL }

            favor += golesPropios
            contra += golesRival

            when {
                golesPropios > golesRival -> ganados++
                golesPropios == golesRival -> empatados++
                else -> perdidos++
            }

            // Disciplina y cambios del equipo propio (o sin equipo indicado)
            val propios = validos.filter { it.equipoEvento != EquipoEvento.RIVAL }
            amarillas += propios.count { esTarjetaAmarilla(it) }
            rojas += propios.count { esTarjetaRoja(it) }
            cambios += propios.count { esCambio(it) }
            faltas += propios.count { esFalta(it) }
        }

        val sesiones = sesionesRealizadas(fuente, periodo, hoy)
        val idsSesiones = sesiones.map { it.id }.toSet()
        val conteo = contar(fuente.asistencias.filter { it.idEntrenamiento in idsSesiones })

        return EstadisticasEquipo(
            partidosJugados = jugados.size,
            ganados = ganados,
            empatados = empatados,
            perdidos = perdidos,
            golesFavor = favor,
            golesContra = contra,
            tarjetasAmarillas = amarillas,
            tarjetasRojas = rojas,
            cambios = cambios,
            faltas = faltas,
            sesionesEntrenamiento = sesiones.size,
            eventosRegistrados = registrados,
            eventosAnuladosExcluidos = anulados,
            porcentajeAsistencia = conteo.porcentaje
        )
    }

    fun calcularJugador(
        idJugador: String,
        fuente: DatosFuenteEquipo,
        periodo: PeriodoEstadisticas,
        hoy: LocalDate = LocalDate.now()
    ): EstadisticasJugador {

        val jugados = partidosJugados(fuente, periodo)

        var participaciones = 0
        var goles = 0
        var amarillas = 0
        var rojas = 0
        var faltas = 0

        jugados.forEach { partido ->
            val validos = eventosValidos(fuente.eventosPorPartido[partido.id].orEmpty())
            val suyos = validos.filter {
                it.idJugador == idJugador && it.equipoEvento != EquipoEvento.RIVAL
            }

            // Participó si estaba en la alineación o figura en algún evento
            val alineado = idJugador in fuente.alineadosPorPartido[partido.id].orEmpty()
            if (alineado || suyos.isNotEmpty()) participaciones++

            goles += suyos.count { esGol(it) }
            amarillas += suyos.count { esTarjetaAmarilla(it) }
            rojas += suyos.count { esTarjetaRoja(it) }
            faltas += suyos.count { esFalta(it) }
        }

        val idsSesiones = sesionesRealizadas(fuente, periodo, hoy).map { it.id }.toSet()
        val asistencia = contar(
            fuente.asistencias.filter {
                it.idJugador == idJugador && it.idEntrenamiento in idsSesiones
            }
        )

        return EstadisticasJugador(
            idJugador = idJugador,
            participaciones = participaciones,
            goles = goles,
            tarjetasAmarillas = amarillas,
            tarjetasRojas = rojas,
            faltas = faltas,
            asistencia = asistencia
        )
    }

    fun sesionesDelPeriodo(
        fuente: DatosFuenteEquipo,
        periodo: PeriodoEstadisticas,
        hoy: LocalDate = LocalDate.now()
    ): List<SesionPeriodo> =
        sesionesRealizadas(fuente, periodo, hoy)
            .sortedByDescending { it.fecha }
            .map { sesion ->
                SesionPeriodo(
                    idEntrenamiento = sesion.id,
                    fecha = sesion.fecha,
                    objetivo = sesion.objetivo,
                    asistencia = contar(
                        fuente.asistencias.filter { it.idEntrenamiento == sesion.id }
                    )
                )
            }

    /*
     * Periodos que tienen sentido para el equipo: todo el
     * historial, cada temporada (año) y cada mes con datos,
     * del más reciente al más antiguo.
     */
    fun periodosDisponibles(fuente: DatosFuenteEquipo): List<PeriodoEstadisticas> {
        val fechas =
            (fuente.partidos.map { it.fecha } + fuente.entrenamientos.map { it.fecha })
                .mapNotNull { parsearFecha(it) }

        val anios = fechas.map { it.year }.distinct().sortedDescending()
        val meses = fechas.map { YearMonth.from(it) }.distinct().sortedDescending()

        return buildList {
            anios.forEach { add(PeriodoEstadisticas.temporada(it)) }
            meses.forEach { add(PeriodoEstadisticas.mes(it)) }
            add(PeriodoEstadisticas.TODO)
        }
    }

    private fun contar(asistencias: List<Asistencia>) =
        ConteoAsistencia(
            presente = asistencias.count { it.estado == EstadoAsistencia.PRESENTE },
            ausente = asistencias.count { it.estado == EstadoAsistencia.AUSENTE },
            justificado = asistencias.count { it.estado == EstadoAsistencia.JUSTIFICADO }
        )
}
