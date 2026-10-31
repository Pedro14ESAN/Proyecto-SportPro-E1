package com.example.projectsportpro2026_2.datos.modelo.alineacion

data class Alineacion(
    val idPartido: String = "",
    val idEquipo: String = "",
    val idAcademia: String = "",
    val formato: FormatoPartido = FormatoPartido.ONCE_VS_ONCE,
    val confirmada: Boolean = false,
    val creadoPor: String = "",
    val fechaCreacion: Long = 0L,
    val actualizadoPor: String = "",
    val fechaActualizacion: Long = 0L
)

data class JugadorAlineacion(
    val idJugador: String = "",
    val tipo: TipoJugadorAlineacion = TipoJugadorAlineacion.SUPLENTE,

    // Identifica dónde se muestra el titular dentro de la cancha.
    // Ejemplos: "ARQ", "DEF_IZQ", "MED_CENTRO", "DEL_DER".
    // Para suplentes queda vacío.
    val posicionCampo: String = "",

    // Sirve para mantener un orden estable en pantalla.
    val orden: Int = 0
)

enum class TipoJugadorAlineacion {
    TITULAR,
    SUPLENTE
}

enum class FormatoPartido(
    val maximoTitulares: Int,
    val etiqueta: String
) {
    ONCE_VS_ONCE(
        maximoTitulares = 11,
        etiqueta = "11 vs 11"
    ),

    OCHO_VS_OCHO(
        maximoTitulares = 8,
        etiqueta = "8 vs 8"
    ),

    SIETE_VS_SIETE(
        maximoTitulares = 7,
        etiqueta = "7 vs 7"
    ),

    CINCO_VS_CINCO(
        maximoTitulares = 5,
        etiqueta = "5 vs 5"
    )
}