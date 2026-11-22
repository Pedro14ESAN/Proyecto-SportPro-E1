package com.example.projectsportpro2026_2.datos.partido

import com.example.projectsportpro2026_2.datos.modelo.evento.EstadoEventoPartido
import com.example.projectsportpro2026_2.datos.modelo.evento.EventoPartido
import com.example.projectsportpro2026_2.datos.sincronizacion.DetectorConflictos

fun eventosValidos(eventos: List<EventoPartido>): List<EventoPartido> {
    val conflictos = DetectorConflictos.detectar(eventos)
    return eventos.filter { it.estado == EstadoEventoPartido.ACTIVO && it.id !in conflictos }
}