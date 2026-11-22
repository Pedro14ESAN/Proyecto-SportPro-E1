package com.example.projectsportpro2026_2.datos.alineacion

import com.example.projectsportpro2026_2.datos.modelo.alineacion.JugadorAlineacion

/**
 * Compatibility with coordinates written before explicit Firestore property names:
 * getXCampo/getYCampo were serialized as "xcampo"/"ycampo".
 * Reading never changes the stored document or invents coordinates for old lineups.
 */
fun JugadorAlineacion.recuperarCoordenadasGuardadas(campos: Map<String, Any?>): JugadorAlineacion =
    copy(
        xCampo = xCampo ?: (campos["xcampo"] as? Number)?.toDouble(),
        yCampo = yCampo ?: (campos["ycampo"] as? Number)?.toDouble()
    )