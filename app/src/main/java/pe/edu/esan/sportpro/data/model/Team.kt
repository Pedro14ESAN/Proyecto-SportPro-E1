package pe.edu.esan.sportpro.data.model

import com.google.firebase.Timestamp

data class Team(
    val id: String = "",
    val academyId: String = "",
    val name: String = "",
    val category: String = "",
    val coachUid: String = "",
    val season: String = "",
    val active: Boolean = true,
    val createdAt: Timestamp? = null,

    // HU-03 CA-07: trazabilidad de cambios (valores por defecto, no rompe código existente)
    val createdBy: String = "",
    val updatedBy: String = "",
    val updatedAt: Timestamp? = null
)

/** Categorías sugeridas para el selector (HU-03: sub-10, sub-15, primera...). Ajústalas con el grupo. */
object TeamCategories {
    val defaults = listOf("Sub-8", "Sub-10", "Sub-12", "Sub-14", "Sub-15", "Sub-17", "Primera")
}