package com.example.projectsportpro2026_2.datos.modelo.evento

data class EventoPartido(
    val id: String = "",

    // Partido al que pertenece el evento.
    val idPartido: String = "",
    val idAcademia: String = "",

    /*
     * Copia estable del tipo del catálogo.
     *
     * Ejemplo:
     * idTipoEvento = "GOL"
     * codigoTipoEvento = "GOL"
     * nombreTipoEvento = "Gol"
     *
     * Guardamos estos datos también en el evento para que
     * el histórico no dependa de futuras modificaciones
     * realizadas en HU13.
     */
    val idTipoEvento: String = "",
    val codigoTipoEvento: String = "",
    val nombreTipoEvento: String = "",

    // Minuto futbolístico ingresado por el operador.
    val minuto: Int = 0,

    /*
     * Algunos eventos requieren indicar qué equipo realizó
     * la acción.
     *
     * PROPIO = el equipo SportPro del partido.
     * RIVAL = el rival escrito al crear el partido.
     *
     * NINGUNO = evento que no requiere equipo.
     */
    val equipoEvento: EquipoEvento = EquipoEvento.NINGUNO,

    /*
     * Para un jugador propio guardamos su ID real.
     * Para un jugador rival puede quedar vacío porque el
     * rival no necesariamente existe como equipo/jugador
     * registrado dentro de SportPro.
     */
    val idJugador: String = "",

    /*
     * Guardamos también el nombre mostrado en el momento
     * del evento.
     *
     * Esto permite conservar la cronología aunque después
     * el perfil del jugador cambie de nombre o deje de
     * pertenecer al equipo.
     */
    val nombreJugador: String = "",

    val observacion: String = "",

    /*
     * Copia de las reglas importantes del catálogo.
     * Así HU14/HU15 no dependen de que el catálogo siga
     * teniendo exactamente la misma configuración.
     */
    val afectaMarcador: Boolean = false,
    val afectaEstadoPartido: Boolean = false,

    /*
     * Usuario que registró la jugada y momento real en que
     * la aplicación recibió el registro.
     */
    val creadoPor: String = "",
    val fechaCaptura: Long = 0L,

    val estado: EstadoEventoPartido =
        EstadoEventoPartido.ACTIVO,

    val version: Int = 1,

    val modificadoPor: String = "",
    val fechaModificacion: Long = 0L,

    val anuladoPor: String = "",
    val fechaAnulacion: Long = 0L
)

enum class EquipoEvento {
    NINGUNO,
    PROPIO,
    RIVAL
}
enum class EstadoEventoPartido {
    ACTIVO,
    ANULADO
}