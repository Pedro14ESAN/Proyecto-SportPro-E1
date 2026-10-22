package com.example.projectsportpro2026_2.ui.vinculaciones

import com.example.projectsportpro2026_2.datos.modelo.EstadoVinculacion
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatoFecha(milisegundos: Long): String =
    if (milisegundos <= 0L) "—"
    else SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(milisegundos))

fun etiquetaEstado(estado: EstadoVinculacion): String = when (estado) {
    EstadoVinculacion.PENDIENTE -> "Pendiente"
    EstadoVinculacion.ACTIVA -> "Activo"
    EstadoVinculacion.INACTIVA -> "Inactivo"
}