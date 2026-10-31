package com.example.projectsportpro2026_2.datos.repositorio.evento

import com.example.projectsportpro2026_2.datos.modelo.Academia
import com.example.projectsportpro2026_2.datos.modelo.PerfilUsuario
import com.example.projectsportpro2026_2.datos.modelo.Rol
import com.example.projectsportpro2026_2.datos.modelo.evento.TipoEventoCatalogo
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

data class ActualizacionCatalogo(
    val tipos: List<TipoEventoCatalogo> = emptyList(),

    // true = Firestore está mostrando la copia local/cache.
    val desdeCache: Boolean = false,

    // true = existen cambios locales todavía pendientes de subir.
    val tieneEscriturasPendientes: Boolean = false
)

class RepositorioCatalogoEventos(
    private val bd: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val academias =
        bd.collection("academias")

    private val usuarios =
        bd.collection("usuarios")

    private suspend fun <T> intentar(
        bloque: suspend () -> T
    ): Result<T> =
        try {
            Result.success(
                bloque()
            )
        } catch (e: Exception) {
            Result.failure(e)
        }

    private suspend fun verificarPermisoConfiguracion(
        idAcademia: String,
        uid: String
    ): Academia {

        if (idAcademia.isBlank()) {
            throw IllegalArgumentException(
                "La academia no es válida"
            )
        }

        if (uid.isBlank()) {
            throw IllegalStateException(
                "No existe una sesión activa"
            )
        }

        val perfil =
            usuarios.document(uid)
                .get()
                .await()
                .toObject(PerfilUsuario::class.java)
                ?: throw IllegalStateException(
                    "El usuario no tiene un perfil válido"
                )

        if (perfil.rol != Rol.ADM) {
            throw IllegalStateException(
                "Solo un administrador autorizado puede configurar el catálogo"
            )
        }

        val academia =
            academias.document(idAcademia)
                .get()
                .await()
                .toObject(Academia::class.java)
                ?: throw IllegalStateException(
                    "La academia no existe"
                )

        if (!academia.activa) {
            throw IllegalStateException(
                "La academia está inactiva"
            )
        }

        val autorizado =
            academia.idAdministrador == uid ||
                    uid in academia.idsAutorizados

        if (!autorizado) {
            throw IllegalStateException(
                "No tienes permiso para configurar el catálogo de esta academia"
            )
        }

        return academia
    }

    private fun referenciaCatalogo(
        idAcademia: String
    ) =
        academias.document(idAcademia)
            .collection("catalogoEventos")

    /*
     * Crea los eventos mínimos de HU13.
     *
     * Utilizamos el código como ID del documento para que
     * dos dispositivos no puedan crear dos veces el mismo
     * evento base.
     */
    suspend fun inicializarCatalogoBase(
        idAcademia: String,
        uid: String
    ): Result<Unit> = intentar {

        verificarPermisoConfiguracion(
            idAcademia = idAcademia,
            uid = uid
        )

        val catalogo =
            referenciaCatalogo(idAcademia)

        val documentosActuales =
            catalogo.get()
                .await()

        val idsExistentes =
            documentosActuales.documents
                .map {
                    it.id
                }
                .toSet()

        val ahora =
            System.currentTimeMillis()

        val faltantes =
            tiposBase(
                uid = uid,
                ahora = ahora
            ).filter {
                it.id !in idsExistentes
            }

        if (faltantes.isEmpty()) {
            return@intentar Unit
        }

        val lote =
            bd.batch()

        faltantes.forEach { tipo ->

            lote.set(
                catalogo.document(tipo.id),
                tipo
            )
        }

        lote.commit().await()

        Unit
    }

    /*
     * Listener en tiempo real.
     *
     * La UI no necesita estar preguntando a Firestore cada
     * pocos segundos. Cuando otro dispositivo modifica el
     * catálogo, Firestore notifica automáticamente.
     */
    fun observarCatalogo(
        idAcademia: String,
        uid: String
    ): Flow<Result<ActualizacionCatalogo>> =
        flow {

            try {

                verificarPermisoConfiguracion(
                    idAcademia = idAcademia,
                    uid = uid
                )

            } catch (e: Exception) {

                emit(
                    Result.failure(e)
                )

                return@flow
            }

            emitAll(
                callbackFlow {

                    val consulta =
                        referenciaCatalogo(idAcademia)
                            .orderBy("orden")

                    val registro =
                        consulta.addSnapshotListener(
                            MetadataChanges.INCLUDE
                        ) { snapshot, error ->

                            if (error != null) {

                                trySend(
                                    Result.failure(error)
                                )

                                close()
                                return@addSnapshotListener
                            }

                            if (snapshot == null) {
                                return@addSnapshotListener
                            }

                            val tipos =
                                snapshot.documents
                                    .mapNotNull {
                                            documento ->

                                        documento
                                            .toObject(
                                                TipoEventoCatalogo::class.java
                                            )
                                            ?.copy(
                                                id = documento.id
                                            )
                                    }
                                    .sortedWith(
                                        compareBy<TipoEventoCatalogo> {
                                            it.orden
                                        }.thenBy {
                                            it.nombre.lowercase()
                                        }
                                    )

                            val actualizacion =
                                ActualizacionCatalogo(
                                    tipos = tipos,

                                    desdeCache =
                                        snapshot.metadata
                                            .isFromCache,

                                    tieneEscriturasPendientes =
                                        snapshot.metadata
                                            .hasPendingWrites()
                                )

                            trySend(
                                Result.success(
                                    actualizacion
                                )
                            )
                        }

                    awaitClose {
                        registro.remove()
                    }
                }
            )
        }

    suspend fun crearTipo(
        idAcademia: String,
        nombre: String,
        activo: Boolean,
        requiereEquipo: Boolean,
        requiereJugador: Boolean,
        requiereObservacion: Boolean,
        afectaMarcador: Boolean,
        afectaEstadoPartido: Boolean,
        uid: String
    ): Result<TipoEventoCatalogo> = intentar {

        verificarPermisoConfiguracion(
            idAcademia = idAcademia,
            uid = uid
        )

        val nombreLimpio =
            nombre.trim()

        if (nombreLimpio.isBlank()) {
            throw IllegalArgumentException(
                "El nombre del evento es obligatorio"
            )
        }

        validarNombreActivoUnico(
            idAcademia = idAcademia,
            nombre = nombreLimpio,
            activo = activo,
            idIgnorado = null
        )

        val catalogo =
            referenciaCatalogo(idAcademia)

        val documento =
            catalogo.document()

        val ahora =
            System.currentTimeMillis()

        val ordenSiguiente =
            obtenerSiguienteOrden(
                idAcademia
            )

        /*
         * El código técnico no depende del nombre.
         * Si luego cambiamos "Córner corto" por otro nombre,
         * el identificador histórico permanece estable.
         */
        val codigo =
            "CUSTOM_${documento.id.uppercase()}"

        val tipo =
            TipoEventoCatalogo(
                id = documento.id,
                codigo = codigo,
                nombre = nombreLimpio,
                activo = activo,

                requiereEquipo = requiereEquipo,
                requiereJugador = requiereJugador,
                requiereObservacion = requiereObservacion,

                afectaMarcador = afectaMarcador,
                afectaEstadoPartido =
                    afectaEstadoPartido,

                esTipoBase = false,
                orden = ordenSiguiente,

                creadoPor = uid,
                fechaCreacion = ahora,
                actualizadoPor = uid,
                fechaActualizacion = ahora
            )

        documento.set(tipo).await()

        tipo
    }

    suspend fun editarTipo(
        idAcademia: String,
        idTipo: String,
        nombre: String,
        activo: Boolean,
        requiereEquipo: Boolean,
        requiereJugador: Boolean,
        requiereObservacion: Boolean,
        afectaMarcador: Boolean,
        afectaEstadoPartido: Boolean,
        uid: String
    ): Result<TipoEventoCatalogo> = intentar {

        verificarPermisoConfiguracion(
            idAcademia = idAcademia,
            uid = uid
        )

        if (idTipo.isBlank()) {
            throw IllegalArgumentException(
                "El tipo de evento no es válido"
            )
        }

        val referencia =
            referenciaCatalogo(idAcademia)
                .document(idTipo)

        val anterior =
            referencia.get()
                .await()
                .toObject(
                    TipoEventoCatalogo::class.java
                )
                ?: throw IllegalStateException(
                    "El tipo de evento ya no existe"
                )

        val nombreLimpio =
            nombre.trim()

        if (nombreLimpio.isBlank()) {
            throw IllegalArgumentException(
                "El nombre del evento es obligatorio"
            )
        }

        /*
         * Los tipos mínimos conservan su nombre para seguir
         * cumpliendo CA-01. Sí pueden cambiar campos,
         * efectos o estado activo/inactivo.
         */
        if (
            anterior.esTipoBase &&
            !anterior.nombre.equals(
                nombreLimpio,
                ignoreCase = true
            )
        ) {
            throw IllegalArgumentException(
                "Los eventos base conservan su nombre. " +
                        "Puedes modificar sus reglas o activarlos/desactivarlos."
            )
        }

        validarNombreActivoUnico(
            idAcademia = idAcademia,
            nombre = nombreLimpio,
            activo = activo,
            idIgnorado = idTipo
        )

        val actualizado =
            anterior.copy(
                id = idTipo,
                nombre = nombreLimpio,
                activo = activo,

                requiereEquipo = requiereEquipo,
                requiereJugador = requiereJugador,
                requiereObservacion =
                    requiereObservacion,

                afectaMarcador =
                    afectaMarcador,

                afectaEstadoPartido =
                    afectaEstadoPartido,

                actualizadoPor = uid,
                fechaActualizacion =
                    System.currentTimeMillis()
            )

        referencia.set(actualizado).await()

        actualizado
    }

    /*
     * Activar/desactivar es una operación frecuente, así
     * que tiene su propia función.
     *
     * NO existe eliminarTipo().
     * Esa ausencia es intencional: CA-04 y CA-06.
     */
    suspend fun cambiarEstado(
        idAcademia: String,
        idTipo: String,
        activo: Boolean,
        uid: String
    ): Result<TipoEventoCatalogo> = intentar {

        verificarPermisoConfiguracion(
            idAcademia = idAcademia,
            uid = uid
        )

        val referencia =
            referenciaCatalogo(idAcademia)
                .document(idTipo)

        val anterior =
            referencia.get()
                .await()
                .toObject(
                    TipoEventoCatalogo::class.java
                )
                ?: throw IllegalStateException(
                    "El tipo de evento ya no existe"
                )

        if (activo) {

            validarNombreActivoUnico(
                idAcademia = idAcademia,
                nombre = anterior.nombre,
                activo = true,
                idIgnorado = idTipo
            )
        }

        val actualizado =
            anterior.copy(
                id = idTipo,
                activo = activo,
                actualizadoPor = uid,
                fechaActualizacion =
                    System.currentTimeMillis()
            )

        referencia.set(actualizado).await()

        actualizado
    }

    private suspend fun validarNombreActivoUnico(
        idAcademia: String,
        nombre: String,
        activo: Boolean,
        idIgnorado: String?
    ) {
        /*
         * CA-03 solamente prohíbe nombres duplicados ACTIVOS.
         * Dos históricos inactivos pueden conservar nombres
         * repetidos sin afectar el panel de registro.
         */
        if (!activo) {
            return
        }

        val nombreNormalizado =
            nombre.normalizarNombreEvento()

        val existentes =
            referenciaCatalogo(idAcademia)
                .get()
                .await()
                .documents
                .mapNotNull {
                        documento ->

                    documento
                        .toObject(
                            TipoEventoCatalogo::class.java
                        )
                        ?.copy(
                            id = documento.id
                        )
                }

        val duplicado =
            existentes.any { tipo ->

                tipo.activo &&
                        tipo.id != idIgnorado &&
                        tipo.nombre
                            .normalizarNombreEvento() ==
                        nombreNormalizado
            }

        if (duplicado) {
            throw IllegalArgumentException(
                "Ya existe un evento activo con el nombre \"$nombre\""
            )
        }
    }

    private suspend fun obtenerSiguienteOrden(
        idAcademia: String
    ): Int {

        val documentos =
            referenciaCatalogo(idAcademia)
                .get()
                .await()
                .documents

        val mayor =
            documentos
                .mapNotNull {
                    it.getLong("orden")
                        ?.toInt()
                }
                .maxOrNull()
                ?: 0

        return mayor + 10
    }

    private fun tiposBase(
        uid: String,
        ahora: Long
    ): List<TipoEventoCatalogo> =
        listOf(

            tipoBase(
                codigo = "INICIO",
                nombre = "Inicio",
                orden = 10,
                afectaEstadoPartido = true,
                uid = uid,
                ahora = ahora
            ),

            tipoBase(
                codigo = "FIN",
                nombre = "Fin",
                orden = 20,
                afectaEstadoPartido = true,
                uid = uid,
                ahora = ahora
            ),

            tipoBase(
                codigo = "SAQUE_META",
                nombre = "Saque de meta",
                orden = 30,
                requiereEquipo = true,
                requiereJugador = true,
                uid = uid,
                ahora = ahora
            ),

            tipoBase(
                codigo = "SAQUE_LATERAL",
                nombre = "Saque lateral",
                orden = 40,
                requiereEquipo = true,
                requiereJugador = true,
                uid = uid,
                ahora = ahora
            ),

            tipoBase(
                codigo = "TIRO_ESQUINA",
                nombre = "Tiro de esquina",
                orden = 50,
                requiereEquipo = true,
                requiereJugador = true,
                uid = uid,
                ahora = ahora
            ),

            tipoBase(
                codigo = "FALTA",
                nombre = "Falta",
                orden = 60,
                requiereEquipo = true,
                requiereJugador = true,
                requiereObservacion = true,
                uid = uid,
                ahora = ahora
            ),

            tipoBase(
                codigo = "PENAL",
                nombre = "Penal",
                orden = 70,
                requiereEquipo = true,
                requiereJugador = true,
                requiereObservacion = true,
                uid = uid,
                ahora = ahora
            ),

            tipoBase(
                codigo = "GOL",
                nombre = "Gol",
                orden = 80,
                requiereEquipo = true,
                requiereJugador = true,
                afectaMarcador = true,
                uid = uid,
                ahora = ahora
            ),

            tipoBase(
                codigo = "TARJETA",
                nombre = "Tarjeta",
                orden = 90,
                requiereEquipo = true,
                requiereJugador = true,
                requiereObservacion = true,
                uid = uid,
                ahora = ahora
            ),

            tipoBase(
                codigo = "FUERA_JUEGO",
                nombre = "Fuera de juego",
                orden = 100,
                requiereEquipo = true,
                requiereJugador = true,
                uid = uid,
                ahora = ahora
            ),

            tipoBase(
                codigo = "CAMBIO",
                nombre = "Cambio",
                orden = 110,
                requiereEquipo = true,
                requiereJugador = true,
                requiereObservacion = true,
                uid = uid,
                ahora = ahora
            )
        )

    private fun tipoBase(
        codigo: String,
        nombre: String,
        orden: Int,

        requiereEquipo: Boolean = false,
        requiereJugador: Boolean = false,
        requiereObservacion: Boolean = false,

        afectaMarcador: Boolean = false,
        afectaEstadoPartido: Boolean = false,

        uid: String,
        ahora: Long
    ): TipoEventoCatalogo =
        TipoEventoCatalogo(
            id = codigo,
            codigo = codigo,
            nombre = nombre,
            activo = true,

            requiereEquipo = requiereEquipo,
            requiereJugador = requiereJugador,
            requiereObservacion =
                requiereObservacion,

            afectaMarcador =
                afectaMarcador,

            afectaEstadoPartido =
                afectaEstadoPartido,

            esTipoBase = true,
            orden = orden,

            creadoPor = uid,
            fechaCreacion = ahora,
            actualizadoPor = uid,
            fechaActualizacion = ahora
        )

    private fun String.normalizarNombreEvento(): String =
        trim()
            .lowercase()
            .replace(
                Regex("\\s+"),
                " "
            )
}