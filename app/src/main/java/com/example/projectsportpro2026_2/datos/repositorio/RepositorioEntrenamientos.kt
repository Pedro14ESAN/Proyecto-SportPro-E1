package com.example.projectsportpro2026_2.datos.repositorio

import com.example.projectsportpro2026_2.datos.modelo.CambioHorarioEntrenamiento
import com.example.projectsportpro2026_2.datos.modelo.Ejercicio
import com.example.projectsportpro2026_2.datos.modelo.EjercicioEntrenamiento
import com.example.projectsportpro2026_2.datos.modelo.Entrenamiento
import com.example.projectsportpro2026_2.datos.modelo.Equipo
import com.example.projectsportpro2026_2.datos.modelo.EstadoEntrenamiento
import com.example.projectsportpro2026_2.datos.modelo.EstadoVinculacion
import com.example.projectsportpro2026_2.datos.modelo.Jugador
import com.example.projectsportpro2026_2.datos.modelo.PerfilUsuario
import com.example.projectsportpro2026_2.datos.modelo.Rol
import com.example.projectsportpro2026_2.datos.modelo.Vinculacion
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class RepositorioEntrenamientos(
    private val bd: FirebaseFirestore =
        FirebaseFirestore.getInstance()
) {

    private val entrenamientos =
        bd.collection("entrenamientos")

    private val equipos =
        bd.collection("equipos")

    private val ejercicios =
        bd.collection("ejercicios")

    private val jugadores =
        bd.collection("jugadores")

    private val usuarios =
        bd.collection("usuarios")

    private val vinculaciones =
        bd.collection("vinculaciones")

    private suspend fun <T> intentar(
        bloque: suspend () -> T
    ): Result<T> {
        return try {
            Result.success(bloque())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun obtenerPerfil(
        uid: String
    ): PerfilUsuario {

        return usuarios
            .document(uid)
            .get()
            .await()
            .toObject(
                PerfilUsuario::class.java
            )
            ?: throw IllegalStateException(
                "Tu cuenta no tiene perfil"
            )
    }

    private fun convertirEquipo(
        documento: DocumentSnapshot
    ): Equipo? {

        return documento
            .toObject(
                Equipo::class.java
            )
            ?.copy(
                id = documento.id
            )
    }

    private fun convertirEjercicio(
        documento: DocumentSnapshot
    ): Ejercicio? {

        return documento
            .toObject(
                Ejercicio::class.java
            )
            ?.copy(
                id = documento.id
            )
    }

    private fun convertirJugador(
        documento: DocumentSnapshot
    ): Jugador? {

        return documento
            .toObject(
                Jugador::class.java
            )
            ?.copy(
                id = documento.id
            )
    }

    private fun convertirEntrenamiento(
        documento: DocumentSnapshot
    ): Entrenamiento? {

        return documento
            .toObject(
                Entrenamiento::class.java
            )
            ?.copy(
                id = documento.id
            )
    }

    private suspend fun verificarDt() :
            Pair<String, PerfilUsuario> {

        val uid =
            com.google.firebase.auth.FirebaseAuth
                .getInstance()
                .currentUser
                ?.uid
                ?: throw IllegalStateException(
                    "No existe una sesión activa"
                )

        val perfil =
            obtenerPerfil(uid)

        if (perfil.rol != Rol.DT) {
            throw IllegalStateException(
                "Solo el director técnico puede planificar entrenamientos"
            )
        }

        return uid to perfil
    }

    private suspend fun obtenerEquipoDt(
        idEquipo: String,
        uid: String,
        exigirActivo: Boolean
    ): Equipo {

        val documento =
            equipos
                .document(idEquipo)
                .get()
                .await()

        val equipo =
            convertirEquipo(
                documento
            )
                ?: throw IllegalStateException(
                    "El equipo no existe"
                )

        if (
            equipo.idEntrenador != uid
        ) {
            throw IllegalStateException(
                "No estás asignado a este equipo"
            )
        }

        if (
            exigirActivo &&
            !equipo.activo
        ) {
            throw IllegalStateException(
                "No puedes crear entrenamientos para un equipo inactivo"
            )
        }

        return equipo
    }

    private fun validarDatos(
        fecha: String,
        hora: String,
        objetivo: String,
        duracionTotalMinutos: Int,
        idsEjercicios: List<String>
    ) {

        try {
            LocalDate.parse(fecha)
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "La fecha no es válida"
            )
        }

        try {
            LocalTime.parse(hora)
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "La hora no es válida"
            )
        }

        if (objetivo.isBlank()) {
            throw IllegalArgumentException(
                "El objetivo es obligatorio"
            )
        }

        if (duracionTotalMinutos <= 0) {
            throw IllegalArgumentException(
                "La duración debe ser mayor a cero"
            )
        }

        if (idsEjercicios.isEmpty()) {
            throw IllegalArgumentException(
                "Agrega al menos un ejercicio"
            )
        }
    }

    private fun obtenerEpoch(
        fecha: String,
        hora: String
    ): Long {

        return LocalDate
            .parse(fecha)
            .atTime(
                LocalTime.parse(hora)
            )
            .atZone(
                ZoneId.systemDefault()
            )
            .toInstant()
            .toEpochMilli()
    }

    suspend fun listarEquiposDt(
        uid: String
    ): Result<List<Equipo>> = intentar {

        val perfil =
            obtenerPerfil(uid)

        if (perfil.rol != Rol.DT) {
            throw IllegalStateException(
                "Solo el director técnico puede acceder a entrenamientos"
            )
        }

        equipos
            .whereEqualTo(
                "idEntrenador",
                uid
            )
            .get()
            .await()
            .documents
            .mapNotNull {
                convertirEquipo(it)
            }
            .filter {
                it.activo
            }
            .sortedBy {
                it.nombre.lowercase()
            }
    }

    suspend fun listarEjerciciosActivos(
        idEquipo: String,
        uid: String
    ): Result<List<Ejercicio>> = intentar {

        val equipo =
            obtenerEquipoDt(
                idEquipo =
                    idEquipo,
                uid =
                    uid,
                exigirActivo =
                    true
            )

        ejercicios
            .whereEqualTo(
                "idAcademia",
                equipo.idAcademia
            )
            .get()
            .await()
            .documents
            .mapNotNull {
                convertirEjercicio(it)
            }
            .filter {
                it.activo
            }
            .sortedBy {
                it.nombre.lowercase()
            }
    }

    private suspend fun obtenerEjerciciosParaSesion(
        idsEjercicios: List<String>,
        idAcademia: String,
        historicos: List<EjercicioEntrenamiento> =
            emptyList()
    ): List<EjercicioEntrenamiento> {

        val resultado =
            mutableListOf<EjercicioEntrenamiento>()

        idsEjercicios
            .distinct()
            .forEachIndexed {
                    indice,
                    id ->

                val documento =
                    ejercicios
                        .document(id)
                        .get()
                        .await()

                val ejercicio =
                    convertirEjercicio(
                        documento
                    )

                if (
                    ejercicio != null &&
                    ejercicio.idAcademia ==
                    idAcademia &&
                    ejercicio.activo
                ) {

                    resultado.add(
                        EjercicioEntrenamiento(
                            idEjercicio =
                                ejercicio.id,
                            nombre =
                                ejercicio.nombre,
                            objetivo =
                                ejercicio.objetivo,
                            duracionMinutos =
                                ejercicio.duracionMinutos,
                            orden =
                                indice
                        )
                    )

                } else {

                    val historico =
                        historicos
                            .firstOrNull {
                                it.idEjercicio ==
                                        id
                            }

                    if (historico != null) {

                        resultado.add(
                            historico.copy(
                                orden =
                                    indice
                            )
                        )

                    } else {

                        throw IllegalStateException(
                            "Uno de los ejercicios seleccionados ya no está disponible"
                        )
                    }
                }
            }

        return resultado
    }

    suspend fun crearEntrenamiento(
        idEquipo: String,
        fecha: String,
        hora: String,
        objetivo: String,
        duracionTotalMinutos: Int,
        idsEjercicios: List<String>,
        uid: String
    ): Result<Entrenamiento> = intentar {

        val perfil =
            obtenerPerfil(uid)

        if (perfil.rol != Rol.DT) {
            throw IllegalStateException(
                "Solo el director técnico puede crear entrenamientos"
            )
        }

        validarDatos(
            fecha,
            hora,
            objetivo,
            duracionTotalMinutos,
            idsEjercicios
        )

        val equipo =
            obtenerEquipoDt(
                idEquipo =
                    idEquipo,
                uid =
                    uid,
                exigirActivo =
                    true
            )

        val ejerciciosSesion =
            obtenerEjerciciosParaSesion(
                idsEjercicios =
                    idsEjercicios,
                idAcademia =
                    equipo.idAcademia
            )

        val documento =
            entrenamientos.document()

        val ahora =
            System.currentTimeMillis()

        val entrenamiento =
            Entrenamiento(
                id =
                    documento.id,
                idAcademia =
                    equipo.idAcademia,
                idEquipo =
                    equipo.id,
                nombreEquipo =
                    equipo.nombre,
                idEntrenador =
                    uid,
                fecha =
                    fecha,
                hora =
                    hora,
                fechaHoraEpochMillis =
                    obtenerEpoch(
                        fecha,
                        hora
                    ),
                objetivo =
                    objetivo.trim(),
                duracionTotalMinutos =
                    duracionTotalMinutos,
                ejercicios =
                    ejerciciosSesion,
                estado =
                    EstadoEntrenamiento.PUBLICADO,
                fechaCreacion =
                    ahora,
                creadoPor =
                    uid,
                actualizadoPor =
                    uid,
                fechaActualizacion =
                    ahora
            )

        documento
            .set(
                entrenamiento
            )
            .await()

        entrenamiento
    }

    suspend fun editarEntrenamiento(
        idEntrenamiento: String,
        fecha: String,
        hora: String,
        objetivo: String,
        duracionTotalMinutos: Int,
        idsEjercicios: List<String>,
        uid: String
    ): Result<Unit> = intentar {

        val perfil =
            obtenerPerfil(uid)

        if (perfil.rol != Rol.DT) {
            throw IllegalStateException(
                "Solo el director técnico puede editar entrenamientos"
            )
        }

        validarDatos(
            fecha,
            hora,
            objetivo,
            duracionTotalMinutos,
            idsEjercicios
        )

        val referencia =
            entrenamientos
                .document(
                    idEntrenamiento
                )

        val documento =
            referencia
                .get()
                .await()

        val actual =
            convertirEntrenamiento(
                documento
            )
                ?: throw IllegalStateException(
                    "El entrenamiento no existe"
                )

        if (
            actual.estado ==
            EstadoEntrenamiento.CANCELADO
        ) {
            throw IllegalStateException(
                "No puedes editar un entrenamiento cancelado"
            )
        }

        val equipo =
            obtenerEquipoDt(
                idEquipo =
                    actual.idEquipo,
                uid =
                    uid,
                exigirActivo =
                    false
            )

        val ejerciciosSesion =
            obtenerEjerciciosParaSesion(
                idsEjercicios =
                    idsEjercicios,
                idAcademia =
                    equipo.idAcademia,
                historicos =
                    actual.ejercicios
            )

        val cambioHorario =
            actual.fecha != fecha ||
                    actual.hora != hora

        val historial =
            if (cambioHorario) {

                actual
                    .historialCambiosHorario +
                        CambioHorarioEntrenamiento(
                            fechaAnterior =
                                actual.fecha,
                            horaAnterior =
                                actual.hora,
                            fechaNueva =
                                fecha,
                            horaNueva =
                                hora,
                            actualizadoPor =
                                uid,
                            fechaCambio =
                                System.currentTimeMillis()
                        )

            } else {

                actual
                    .historialCambiosHorario
            }

        val actualizado =
            actual.copy(
                fecha =
                    fecha,
                hora =
                    hora,
                fechaHoraEpochMillis =
                    obtenerEpoch(
                        fecha,
                        hora
                    ),
                objetivo =
                    objetivo.trim(),
                duracionTotalMinutos =
                    duracionTotalMinutos,
                ejercicios =
                    ejerciciosSesion,
                actualizadoPor =
                    uid,
                fechaActualizacion =
                    System.currentTimeMillis(),
                historialCambiosHorario =
                    historial
            )

        referencia
            .set(
                actualizado
            )
            .await()
    }

    suspend fun cancelarEntrenamiento(
        idEntrenamiento: String,
        uid: String
    ): Result<Unit> = intentar {

        val perfil =
            obtenerPerfil(uid)

        if (perfil.rol != Rol.DT) {
            throw IllegalStateException(
                "Solo el director técnico puede cancelar entrenamientos"
            )
        }

        val referencia =
            entrenamientos
                .document(
                    idEntrenamiento
                )

        val entrenamiento =
            referencia
                .get()
                .await()
                .let {
                    convertirEntrenamiento(
                        it
                    )
                }
                ?: throw IllegalStateException(
                    "El entrenamiento no existe"
                )

        obtenerEquipoDt(
            idEquipo =
                entrenamiento.idEquipo,
            uid =
                uid,
            exigirActivo =
                false
        )

        referencia
            .update(
                mapOf(
                    "estado" to
                            EstadoEntrenamiento.CANCELADO,
                    "actualizadoPor" to
                            uid,
                    "fechaActualizacion" to
                            System.currentTimeMillis()
                )
            )
            .await()
    }

    suspend fun listarParaDt(
        uid: String
    ): Result<List<Entrenamiento>> = intentar {

        val perfil =
            obtenerPerfil(uid)

        if (perfil.rol != Rol.DT) {
            throw IllegalStateException(
                "No tienes acceso a esta información"
            )
        }

        entrenamientos
            .whereEqualTo(
                "idEntrenador",
                uid
            )
            .get()
            .await()
            .documents
            .mapNotNull {
                convertirEntrenamiento(it)
            }
            .sortedByDescending {
                it.fechaHoraEpochMillis
            }
    }

    private suspend fun listarPorEquipos(
        idsEquipos: List<String>
    ): List<Entrenamiento> {

        if (
            idsEquipos.isEmpty()
        ) {
            return emptyList()
        }

        return idsEquipos
            .distinct()
            .chunked(10)
            .flatMap {
                    grupo ->

                entrenamientos
                    .whereIn(
                        "idEquipo",
                        grupo
                    )
                    .get()
                    .await()
                    .documents
                    .mapNotNull {
                        convertirEntrenamiento(it)
                    }
            }
            .distinctBy {
                it.id
            }
            .sortedByDescending {
                it.fechaHoraEpochMillis
            }
    }

    private suspend fun listarParaJugador(
        uid: String
    ): List<Entrenamiento> {

        val fichas =
            jugadores
                .whereEqualTo(
                    "idUsuario",
                    uid
                )
                .get()
                .await()
                .documents
                .mapNotNull {
                    convertirJugador(it)
                }

        return listarPorEquipos(
            fichas
                .map {
                    it.idEquipo
                }
                .filter {
                    it.isNotBlank()
                }
        )
    }

    private suspend fun listarParaApoderado(
        uid: String
    ): List<Entrenamiento> {

        val relaciones =
            vinculaciones
                .whereEqualTo(
                    "idApoderado",
                    uid
                )
                .get()
                .await()
                .toObjects(
                    Vinculacion::class.java
                )
                .filter {
                    it.estado ==
                            EstadoVinculacion.ACTIVA
                }

        val idsJugadores =
            relaciones
                .map {
                    it.idJugador
                }
                .filter {
                    it.isNotBlank()
                }
                .distinct()

        if (
            idsJugadores.isEmpty()
        ) {
            return emptyList()
        }

        val fichas =
            idsJugadores
                .chunked(10)
                .flatMap {
                        grupo ->

                    jugadores
                        .whereIn(
                            "id",
                            grupo
                        )
                        .get()
                        .await()
                        .documents
                        .mapNotNull {
                            convertirJugador(it)
                        }
                }

        return listarPorEquipos(
            fichas
                .map {
                    it.idEquipo
                }
                .filter {
                    it.isNotBlank()
                }
        )
    }

    suspend fun listarParaUsuario(
        uid: String
    ): Result<List<Entrenamiento>> =
        intentar {

            val perfil =
                obtenerPerfil(uid)

            when (
                perfil.rol
            ) {

                Rol.DT -> {

                    entrenamientos
                        .whereEqualTo(
                            "idEntrenador",
                            uid
                        )
                        .get()
                        .await()
                        .documents
                        .mapNotNull {
                            convertirEntrenamiento(it)
                        }
                        .sortedByDescending {
                            it.fechaHoraEpochMillis
                        }
                }

                Rol.JUG ->
                    listarParaJugador(
                        uid
                    )

                Rol.PAD ->
                    listarParaApoderado(
                        uid
                    )

                Rol.ADM ->
                    throw IllegalStateException(
                        "El administrador no gestiona este módulo"
                    )
            }
        }
}