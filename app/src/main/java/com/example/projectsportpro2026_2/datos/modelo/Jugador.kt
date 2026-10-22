package com.example.projectsportpro2026_2.datos.modelo

import java.time.LocalDate

// Documento de Firestore: jugadores/{id}  (información básica, no sensible)
data class Jugador(
    val id: String = "",
    val nombres: String = "",
    val apellidos: String = "",
    val fechaNacimiento: String = "",          // aaaa-MM-dd (opcional)
    val documento: String = "",
    val posicion: String = "",
    val piePreferido: String = "",
    val idAcademia: String = "",
    val idEquipo: String = "",
    val categoria: String = "",
    val idUsuario: String = "",                // cuenta propia, si la tiene
    val idsApoderados: List<String> = emptyList(),   // los llena US-05
    val fotoUrl: String = "",
    val activo: Boolean = true,
    val actualizadoPor: String = "",
    val fechaActualizacion: Long = 0L
) {
    fun nombreCompleto(): String = "$nombres $apellidos".trim()

    fun esMenorDeEdad(): Boolean = try {
        PoliticaEdad.calcularEdad(LocalDate.parse(fechaNacimiento)) < PoliticaEdad.EDAD_MAYOR
    } catch (e: Exception) {
        false
    }
}