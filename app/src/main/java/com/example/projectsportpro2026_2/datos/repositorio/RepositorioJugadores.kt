package com.example.projectsportpro2026_2.datos.repositorio

import com.example.projectsportpro2026_2.datos.modelo.Academia
import com.example.projectsportpro2026_2.datos.modelo.DatosPrivadosJugador
import com.example.projectsportpro2026_2.datos.modelo.Equipo
import com.example.projectsportpro2026_2.datos.modelo.Jugador
import com.example.projectsportpro2026_2.datos.modelo.PiesHabiles
import com.example.projectsportpro2026_2.datos.modelo.Posiciones
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeParseException
import com.example.projectsportpro2026_2.datos.modelo.Categorias
import com.example.projectsportpro2026_2.datos.modelo.PoliticaEdad

// Todo lo que se captura en el formulario del jugador
data class DatosJugador(
    val nombres: String = "",
    val apellidos: String = "",
    val fechaNacimiento: String = "",
    val documento: String = "",
    val posicion: String = "",
    val piePreferido: String = "",
    val estaturaCm: Double? = null,
    val pesoKg: Double? = null,
    val telefono: String = "",
    val contactoEmergenciaNombre: String = "",
    val contactoEmergenciaTelefono: String = ""
)

class RepositorioJugadores(
    private val bd: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val academias = bd.collection("academias")
    private val equipos = bd.collection("equipos")
    private val jugadores = bd.collection("jugadores")

    private suspend fun <T> intentar(bloque: suspend () -> T): Result<T> =
        try {
            Result.success(bloque())
        } catch (e: Exception) {
            Result.failure(e)
        }

    private suspend fun academiaAutorizada(idAcademia: String, uid: String): Academia {
        val academia = academias.document(idAcademia).get().await()
            .toObject(Academia::class.java)
        if (academia == null || uid !in academia.idsAutorizados) {
            throw IllegalStateException("No tienes permiso sobre esta academia")
        }
        return academia
    }

    private fun validar(d: DatosJugador) {
        if (d.nombres.isBlank()) throw IllegalArgumentException("Los nombres son obligatorios")
        if (d.apellidos.isBlank()) throw IllegalArgumentException("Los apellidos son obligatorios")

        if (d.fechaNacimiento.isNotBlank()) {
            val fecha = try {
                LocalDate.parse(d.fechaNacimiento.trim())
            } catch (e: DateTimeParseException) {
                throw IllegalArgumentException("La fecha de nacimiento no es válida (use aaaa-MM-dd)")
            }
            if (fecha.isAfter(LocalDate.now())) {
                throw IllegalArgumentException("La fecha de nacimiento no puede ser futura")
            }
        }
        if (d.posicion.isNotBlank() && d.posicion !in Posiciones.lista) {
            throw IllegalArgumentException("Elige una posición válida")
        }
        if (d.piePreferido.isNotBlank() && d.piePreferido !in PiesHabiles.lista) {
            throw IllegalArgumentException("Elige un pie hábil válido")
        }
        d.estaturaCm?.let {
            if (it < 50 || it > 250) throw IllegalArgumentException("La estatura debe estar entre 50 y 250 cm")
        }
        d.pesoKg?.let {
            if (it < 10 || it > 250) throw IllegalArgumentException("El peso debe estar entre 10 y 250 kg")
        }
    }

    private fun validarEdadParaCategoria(fechaNacimiento: String, categoria: String) {
        if (fechaNacimiento.isBlank()) return
        val rango = Categorias.rangoDeEdad(categoria) ?: return
        val edad = PoliticaEdad.calcularEdad(LocalDate.parse(fechaNacimiento.trim()))
        if (edad !in rango) {
            throw IllegalArgumentException(
                "Un jugador de $edad años no corresponde a la categoría $categoria " +
                        "(edades permitidas: ${rango.first} a ${rango.last})"
            )
        }
    }
    private fun privados(d: DatosJugador) = DatosPrivadosJugador(
        estaturaCm = d.estaturaCm,
        pesoKg = d.pesoKg,
        telefono = d.telefono.trim(),
        contactoEmergenciaNombre = d.contactoEmergenciaNombre.trim(),
        contactoEmergenciaTelefono = d.contactoEmergenciaTelefono.trim()
    )

    suspend fun crearJugador(
        idEquipo: String, datos: DatosJugador, uid: String
    ): Result<Jugador> = intentar {
        validar(datos)
        val equipo = equipos.document(idEquipo).get().await().toObject(Equipo::class.java)
            ?: throw IllegalStateException("El equipo no existe")
        if (!equipo.activo) throw IllegalStateException("El equipo está inactivo")
        validarEdadParaCategoria(datos.fechaNacimiento, equipo.categoria)
        val academia = academiaAutorizada(equipo.idAcademia, uid)
        if (!academia.activa) throw IllegalStateException("La academia está inactiva")

        val documento = jugadores.document()
        val jugador = Jugador(
            id = documento.id,
            nombres = datos.nombres.trim(),
            apellidos = datos.apellidos.trim(),
            fechaNacimiento = datos.fechaNacimiento.trim(),
            documento = datos.documento.trim(),
            posicion = datos.posicion,
            piePreferido = datos.piePreferido,
            idAcademia = equipo.idAcademia,
            idEquipo = equipo.id,
            categoria = equipo.categoria,
            actualizadoPor = uid,
            fechaActualizacion = System.currentTimeMillis()
        )
        // Perfil y datos sensibles se guardan juntos o no se guarda nada
        val lote = bd.batch()
        lote.set(documento, jugador)
        lote.set(documento.collection("privado").document("datos"), privados(datos))
        lote.commit().await()
        jugador
    }

    suspend fun editarJugador(
        idJugador: String, datos: DatosJugador, uid: String
    ): Result<Unit> = intentar {
        validar(datos)
        val documento = jugadores.document(idJugador)
        val existente = documento.get().await().toObject(Jugador::class.java)
            ?: throw IllegalStateException("El jugador no existe")
        academiaAutorizada(existente.idAcademia, uid)
        validarEdadParaCategoria(datos.fechaNacimiento, existente.categoria)

        val lote = bd.batch()
        lote.update(
            documento,
            mapOf(
                "nombres" to datos.nombres.trim(),
                "apellidos" to datos.apellidos.trim(),
                "fechaNacimiento" to datos.fechaNacimiento.trim(),
                "documento" to datos.documento.trim(),
                "posicion" to datos.posicion,
                "piePreferido" to datos.piePreferido,
                "actualizadoPor" to uid,
                "fechaActualizacion" to System.currentTimeMillis()
            )
        )
        lote.set(documento.collection("privado").document("datos"), privados(datos))
        lote.commit().await()
    }

    // Activos e inactivos; primero los activos
    suspend fun listarJugadores(idEquipo: String): Result<List<Jugador>> = intentar {
        jugadores.whereEqualTo("idEquipo", idEquipo)
            .get().await().toObjects(Jugador::class.java)
            .sortedWith(compareByDescending<Jugador> { it.activo }.thenBy { it.apellidos })
    }

    // Datos sensibles: solo para quienes están autorizados en la academia
    suspend fun obtenerPrivados(
        idJugador: String, uid: String
    ): Result<DatosPrivadosJugador> = intentar {
        val documento = jugadores.document(idJugador)
        val jugador = documento.get().await().toObject(Jugador::class.java)
            ?: throw IllegalStateException("El jugador no existe")
        academiaAutorizada(jugador.idAcademia, uid)
        documento.collection("privado").document("datos").get().await()
            .toObject(DatosPrivadosJugador::class.java) ?: DatosPrivadosJugador()
    }

    // Nunca se borra: solo se activa o inactiva
    suspend fun cambiarEstadoJugador(
        idJugador: String, activo: Boolean, uid: String
    ): Result<Unit> = intentar {
        val documento = jugadores.document(idJugador)
        val jugador = documento.get().await().toObject(Jugador::class.java)
            ?: throw IllegalStateException("El jugador no existe")
        academiaAutorizada(jugador.idAcademia, uid)
        documento.update(
            mapOf(
                "activo" to activo,
                "actualizadoPor" to uid,
                "fechaActualizacion" to System.currentTimeMillis()
            )
        ).await()
    }
}