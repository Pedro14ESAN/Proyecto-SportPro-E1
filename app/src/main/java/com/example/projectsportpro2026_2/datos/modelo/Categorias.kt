package com.example.projectsportpro2026_2.datos.modelo

object Categorias {
    val lista = listOf(
        "Sub-8", "Sub-10", "Sub-12", "Sub-14",
        "Sub-15", "Sub-16", "Sub-18", "Primera"
    )

    // Edades permitidas por categoría. Ajustar según el reglamento de la academia.
    private val rangos = mapOf(
        "Sub-8" to (5..8),
        "Sub-10" to (9..10),
        "Sub-12" to (11..12),
        "Sub-14" to (13..14),
        "Sub-15" to (15..15),
        "Sub-16" to (16..16),
        "Sub-18" to (17..18),
        "Primera" to (19..60)
    )

    fun rangoDeEdad(categoria: String): IntRange? = rangos[categoria]
}