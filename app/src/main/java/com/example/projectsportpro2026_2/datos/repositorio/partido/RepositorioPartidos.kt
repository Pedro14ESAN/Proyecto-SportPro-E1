package com.example.projectsportpro2026_2.datos.repositorio

import com.example.projectsportpro2026_2.datos.modelo.Academia
import com.example.projectsportpro2026_2.datos.modelo.Equipo
import com.example.projectsportpro2026_2.datos.modelo.partido.EstadoPartido
import com.example.projectsportpro2026_2.datos.modelo.partido.Partido
import com.example.projectsportpro2026_2.datos.modelo.PerfilUsuario
import com.example.projectsportpro2026_2.datos.modelo.Rol
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeParseException

class RepositorioPartidos(
    bd: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val partidos = bd.collection("partidos")
    private val equipos = bd.collection("equipos")
    private val academias = bd.collection("academias")
    private val usuarios = bd.collection("usuarios")

    private suspend fun <T> intentar(bloque: suspend () -> T): Result<T> =
        try {
            Result.success(bloque())
        } catch (e: Exception) {
            Result.failure(e)
        }

    private suspend fun verificarPermiso(idAcademia: String, uid: String) {
        val perfil = usuarios.document(uid).get().await()
            .toObject(PerfilUsuario::class.java)
            ?: throw IllegalStateException("El usuario no tiene un perfil válido")

        if (perfil.rol != Rol.ADM && perfil.rol != Rol.DT) {
            throw IllegalStateException("Solo un administrador o entrenador puede gestionar partidos")
        }

        val academia = academias.document(idAcademia).get().await()
            .toObject(Academia::class.java)
            ?: throw IllegalStateException("La academia no existe")

        if (!academia.activa) {
            throw IllegalStateException("La academia está inactiva")
        }

        if (uid !in academia.idsAutorizados) {
            throw IllegalStateException("No tienes permiso para gestionar esta academia")
        }
    }

    private fun validarDatos(
        nombreRival: String,
        fecha: String,
        hora: String,
        ubicacion: String
    ) {
        if (nombreRival.isBlank()) {
            throw IllegalArgumentException("El rival es obligatorio")
        }

        if (fecha.isBlank()) {
            throw IllegalArgumentException("La fecha es obligatoria")
        }

        if (hora.isBlank()) {
            throw IllegalArgumentException("La hora es obligatoria")
        }

        if (ubicacion.isBlank()) {
            throw IllegalArgumentException("La ubicación es obligatoria")
        }

        try {
            LocalDate.parse(fecha)
        } catch (e: DateTimeParseException) {
            throw IllegalArgumentException("La fecha debe tener formato aaaa-MM-dd")
        }

        try {
            LocalTime.parse(hora)
        } catch (e: DateTimeParseException) {
            throw IllegalArgumentException("La hora no es válida")
        }
    }

    suspend fun crearPartido(
        idEquipo: String,
        nombreRival: String,
        fecha: String,
        hora: String,
        ubicacion: String,
        uid: String
    ): Result<Partido> = intentar {

        validarDatos(nombreRival, fecha, hora, ubicacion)

        val equipo = equipos.document(idEquipo).get().await()
            .toObject(Equipo::class.java)
            ?: throw IllegalStateException("El equipo no existe")

        if (!equipo.activo) {
            throw IllegalStateException("No se puede crear un partido para un equipo inactivo")
        }

        verificarPermiso(equipo.idAcademia, uid)

        val documento = partidos.document()
        val ahora = System.currentTimeMillis()

        val partido = Partido(
            id = documento.id,
            idEquipo = equipo.id,
            idAcademia = equipo.idAcademia,
            nombreRival = nombreRival.trim(),
            fecha = fecha,
            hora = hora,
            ubicacion = ubicacion.trim(),
            estado = EstadoPartido.PROGRAMADO,
            creadoPor = uid,
            fechaCreacion = ahora,
            actualizadoPor = uid,
            fechaActualizacion = ahora
        )

        documento.set(partido).await()

        partido
    }

    suspend fun listarPartidos(
        idAcademia: String,
        uid: String
    ): Result<List<Partido>> = intentar {

        verificarPermiso(idAcademia, uid)

        partidos
            .whereEqualTo("idAcademia", idAcademia)
            .get()
            .await()
            .toObjects(Partido::class.java)
            .sortedWith(
                compareBy<Partido> { it.fecha }
                    .thenBy { it.hora }
            )
    }

    suspend fun obtenerPartido(
        idPartido: String,
        uid: String
    ): Result<Partido> = intentar {

        val partido = partidos.document(idPartido).get().await()
            .toObject(Partido::class.java)
            ?: throw IllegalStateException("El partido no existe")

        verificarPermiso(partido.idAcademia, uid)

        partido
    }

    suspend fun editarPartido(
        idPartido: String,
        idEquipo: String,
        nombreRival: String,
        fecha: String,
        hora: String,
        ubicacion: String,
        uid: String
    ): Result<Unit> = intentar {

        validarDatos(
            nombreRival,
            fecha,
            hora,
            ubicacion
        )

        val partido = partidos.document(idPartido)
            .get()
            .await()
            .toObject(Partido::class.java)
            ?: throw IllegalStateException("El partido no existe")

        verificarPermiso(
            partido.idAcademia,
            uid
        )

        if (partido.estado != EstadoPartido.PROGRAMADO) {
            throw IllegalStateException(
                "Solo se puede editar un partido que todavía está programado"
            )
        }

        val equipo = equipos.document(idEquipo)
            .get()
            .await()
            .toObject(Equipo::class.java)
            ?: throw IllegalStateException("El equipo no existe")

        if (!equipo.activo) {
            throw IllegalStateException(
                "No se puede asignar un equipo inactivo"
            )
        }

        if (equipo.idAcademia != partido.idAcademia) {
            throw IllegalStateException(
                "El equipo debe pertenecer a la misma academia del partido"
            )
        }

        partidos.document(idPartido).update(
            mapOf(
                "idEquipo" to equipo.id,
                "nombreRival" to nombreRival.trim(),
                "fecha" to fecha,
                "hora" to hora,
                "ubicacion" to ubicacion.trim(),
                "actualizadoPor" to uid,
                "fechaActualizacion" to System.currentTimeMillis()
            )
        ).await()
    }

    suspend fun cambiarEstadoPartido(
        idPartido: String,
        nuevoEstado: EstadoPartido,
        uid: String
    ): Result<Unit> = intentar {

        val partido = partidos.document(idPartido).get().await()
            .toObject(Partido::class.java)
            ?: throw IllegalStateException("El partido no existe")

        verificarPermiso(partido.idAcademia, uid)

        val cambioValido = when (partido.estado) {
            EstadoPartido.PROGRAMADO ->
                nuevoEstado == EstadoPartido.EN_CURSO ||
                        nuevoEstado == EstadoPartido.CANCELADO

            EstadoPartido.EN_CURSO ->
                nuevoEstado == EstadoPartido.FINALIZADO ||
                        nuevoEstado == EstadoPartido.CANCELADO

            EstadoPartido.FINALIZADO,
            EstadoPartido.CANCELADO -> false
        }

        if (!cambioValido) {
            throw IllegalStateException(
                "No se puede cambiar de ${partido.estado} a $nuevoEstado"
            )
        }

        partidos.document(idPartido).update(
            mapOf(
                "estado" to nuevoEstado,
                "actualizadoPor" to uid,
                "fechaActualizacion" to System.currentTimeMillis()
            )
        ).await()
    }
}