package com.example.projectsportpro2026_2.datos.modelo

import java.time.LocalDate
import java.time.Period

enum class ReglaEdad {
    SIN_CUENTA,             // menor de 12: ficha administrada por academia y apoderado
    REQUIERE_AUTORIZACION,  // 12 a 17: cuenta propia con autorización del apoderado
    LIBRE                   // 18 o más: se registra sin apoderado
}

object PoliticaEdad {
    const val EDAD_MINIMA_CUENTA = 12
    const val EDAD_MAYOR = 18

    fun calcularEdad(fechaNacimiento: LocalDate, hoy: LocalDate = LocalDate.now()): Int =
        Period.between(fechaNacimiento, hoy).years

    fun reglaPara(edad: Int): ReglaEdad = when {
        edad < EDAD_MINIMA_CUENTA -> ReglaEdad.SIN_CUENTA
        edad < EDAD_MAYOR -> ReglaEdad.REQUIERE_AUTORIZACION
        else -> ReglaEdad.LIBRE
    }
}