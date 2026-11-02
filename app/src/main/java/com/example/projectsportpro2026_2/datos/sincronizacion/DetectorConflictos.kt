package com.example.projectsportpro2026_2.datos.sincronizacion

import com.example.projectsportpro2026_2.datos.modelo.evento.EstadoEventoPartido
import com.example.projectsportpro2026_2.datos.modelo.evento.EventoPartido
import kotlin.math.abs

/*
 * HU16 - Detección de conflictos de sincronización.
 *
 * Cuando dos dispositivos registran el mismo partido y
 * alguno estuvo sin conexión, al reconectar pueden
 * aparecer situaciones que el operador debe revisar:
 *
 *  1. POSIBLE DUPLICADO
 *     Dos eventos activos del mismo tipo, mismo equipo y
 *     mismo jugador, con una diferencia de minuto menor o
 *     igual a la tolerancia, capturados en dispositivos
 *     distintos. Se marca el que llegó después.
 *
 *  2. POSTERIOR AL FIN
 *     Un evento activo capturado después del evento FIN
 *     (otro dispositivo finalizó el partido mientras este
 *     seguía registrando sin conexión).
 *
 * Un evento que el operador ya revisó (conflictoRevisado)
 * no vuelve a marcarse. Los eventos anulados se ignoran.
 *
 * Es lógica pura (sin Firebase) para poder probarla con
 * pruebas unitarias.
 */
object DetectorConflictos {

    const val TOLERANCIA_MINUTOS = 1

    fun detectar(
        eventos: List<EventoPartido>
    ): Map<String, String> {

        val activos =
            eventos
                .filter {
                    it.estado == EstadoEventoPartido.ACTIVO
                }
                .sortedWith(
                    compareBy<EventoPartido> {
                        it.fechaCaptura
                    }.thenBy {
                        it.id
                    }
                )

        val conflictos =
            linkedMapOf<String, String>()

        detectarDuplicados(activos, conflictos)
        detectarPosterioresAlFin(activos, conflictos)

        return conflictos
    }

    private fun detectarDuplicados(
        activos: List<EventoPartido>,
        conflictos: MutableMap<String, String>
    ) {
        activos
            .groupBy { claveEvento(it) }
            .values
            .forEach { grupo ->

                // grupo ya está ordenado por fechaCaptura
                for (i in grupo.indices) {
                    val posterior = grupo[i]

                    if (
                        posterior.conflictoRevisado ||
                        posterior.id in conflictos
                    ) {
                        continue
                    }

                    val original =
                        grupo
                            .subList(0, i)
                            .firstOrNull { anterior ->
                                abs(anterior.minuto - posterior.minuto) <=
                                        TOLERANCIA_MINUTOS &&
                                        !mismoOrigen(anterior, posterior)
                            }
                            ?: continue

                    conflictos[posterior.id] =
                        "Posible duplicado: otro dispositivo ya registró " +
                                "${original.nombreTipoEvento} en el minuto " +
                                "${original.minuto}'"
                }
            }
    }

    private fun detectarPosterioresAlFin(
        activos: List<EventoPartido>,
        conflictos: MutableMap<String, String>
    ) {
        val fin =
            activos.firstOrNull {
                it.codigoTipoEvento == "FIN"
            } ?: return

        activos
            .filter { evento ->
                evento.id != fin.id &&
                        !evento.conflictoRevisado &&
                        evento.id !in conflictos &&
                        (
                                evento.fechaCaptura > fin.fechaCaptura ||
                                        evento.minuto > fin.minuto
                                )
            }
            .forEach { evento ->
                conflictos[evento.id] =
                    "Registrado después del FIN del partido " +
                            "(minuto ${fin.minuto}')"
            }
    }

    /*
     * Mismo tipo + mismo equipo + mismo jugador.
     * Para el rival se compara el nombre escrito.
     */
    private fun claveEvento(
        evento: EventoPartido
    ): String {
        val jugador =
            evento.idJugador.ifBlank {
                evento.nombreJugador.trim().lowercase()
            }

        return listOf(
            evento.codigoTipoEvento.ifBlank { evento.idTipoEvento },
            evento.equipoEvento.name,
            jugador
        ).joinToString("|")
    }

    /*
     * Dos eventos del mismo dispositivo no son un conflicto
     * de sincronización (el operador lo registró dos veces
     * a propósito o lo corregirá con HU15).
     *
     * Para eventos antiguos sin idDispositivo se usa el
     * usuario que los creó.
     */
    private fun mismoOrigen(
        a: EventoPartido,
        b: EventoPartido
    ): Boolean =
        if (
            a.idDispositivo.isNotBlank() &&
            b.idDispositivo.isNotBlank()
        ) {
            a.idDispositivo == b.idDispositivo
        } else {
            a.creadoPor == b.creadoPor
        }
}
