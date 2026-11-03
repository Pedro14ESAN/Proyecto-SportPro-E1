package com.example.projectsportpro2026_2.datos.modelo

// ABIERTO y CERRADO se guardan en Firestore.
// VENCIDO no se guarda: se calcula comparando la fecha de la prueba con la fecha actual.
// OCULTO lo asigna un moderador (HU24).
enum class EstadoAviso { ABIERTO, CERRADO, VENCIDO, OCULTO }

// Documento de Firestore: avisosPrueba/{id}
data class AvisoPrueba(
    val id: String = "",

    // Academia que publica (RC-05: aviso oficial)
    val idAcademia: String = "",
    val nombreAcademia: String = "",

    // Datos de la prueba (criterio HU23)
    val categoria: String = "",                    // un valor de Categorias.lista
    val posiciones: List<String> = emptyList(),    // vacía = posición abierta (todas)
    val fechaPrueba: Long = 0L,                    // fecha y hora, en milisegundos
    val ubicacion: String = "",
    val descripcion: String = "",

    // Publicador (solo DT o ADM)
    val idAutor: String = "",
    val nombreAutor: String = "",
    val rolAutor: Rol = Rol.DT,

    // Estado y trazabilidad
    val estado: EstadoAviso = EstadoAviso.ABIERTO,
    val fechaCreacion: Long = 0L,
    val actualizadoPor: String = "",
    val fechaActualizacion: Long = 0L
) {
    // El estado que realmente ve el usuario: si sigue "ABIERTO" pero la fecha ya pasó, está VENCIDO
    fun estadoEfectivo(ahora: Long = System.currentTimeMillis()): EstadoAviso =
        if (estado == EstadoAviso.ABIERTO && fechaPrueba < ahora) EstadoAviso.VENCIDO
        else estado

    fun posicionAbierta(): Boolean = posiciones.isEmpty()

    // "Buscamos Portero y Defensa Sub-15" o "Pruebas abiertas Sub-15"
    fun titulo(): String {
        if (posicionAbierta()) return "Pruebas abiertas $categoria"

        val lista = if (posiciones.size == 1) {
            posiciones.first()
        } else {
            posiciones.dropLast(1).joinToString(", ") + " y " + posiciones.last()
        }
        return "Buscamos $lista $categoria"
    }
}