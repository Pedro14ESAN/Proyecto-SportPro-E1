package com.example.projectsportpro2026_2.datos.repositorio.sincronizacion

import com.example.projectsportpro2026_2.datos.modelo.evento.EstadoEventoPartido
import com.example.projectsportpro2026_2.datos.modelo.evento.EventoPartido
import com.example.projectsportpro2026_2.datos.modelo.partido.Partido
import com.example.projectsportpro2026_2.datos.modelo.sincronizacion.EstadoSincronizacionEvento
import com.example.projectsportpro2026_2.datos.modelo.sincronizacion.EventoSincronizado
import com.example.projectsportpro2026_2.datos.modelo.sincronizacion.InstantaneaEventos
import com.example.projectsportpro2026_2.datos.sincronizacion.DetectorConflictos
import com.example.projectsportpro2026_2.datos.sincronizacion.RegistroRechazos
import com.example.projectsportpro2026_2.datos.sincronizacion.confirmarOEncolar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Source
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/*
 * HU16 - Sincronización del partido entre dispositivos.
 *
 * Usa listeners en tiempo real de Firestore con
 * MetadataChanges.INCLUDE para saber, evento por evento,
 * si ya fue confirmado por el servidor o sigue pendiente
 * en la cola local del dispositivo.
 *
 * También lo usa HU17 (actualización en tiempo real) para recibir
 * los eventos en tiempo real.
 */
class RepositorioSincronizacion(
    private val bd: FirebaseFirestore =
        FirebaseFirestore.getInstance()
) {

    private val partidos =
        bd.collection("partidos")

    /*
     * Escucha TODOS los eventos del partido (activos y
     * anulados). Cada pantalla filtra lo que necesita.
     */
    fun escucharEventos(
        idPartido: String
    ): Flow<InstantaneaEventos> =
        callbackFlow {

            val registro =
                partidos
                    .document(idPartido)
                    .collection("eventos")
                    .addSnapshotListener(
                        MetadataChanges.INCLUDE
                    ) { instantanea, error ->

                        if (error != null) {
                            close(error)
                            return@addSnapshotListener
                        }

                        if (instantanea == null) {
                            return@addSnapshotListener
                        }

                        val eventos =
                            instantanea.documents.mapNotNull { documento ->
                                documento
                                    .toObject(EventoPartido::class.java)
                                    ?.copy(id = documento.id)
                            }

                        val pendientes =
                            instantanea.documents
                                .filter {
                                    it.metadata.hasPendingWrites()
                                }
                                .map { it.id }
                                .toSet()

                        trySend(
                            InstantaneaEventos(
                                eventos = eventos,
                                pendientePorId = pendientes,
                                desdeCache =
                                    instantanea.metadata.isFromCache,
                                escriturasPendientes =
                                    instantanea.metadata.hasPendingWrites()
                            )
                        )
                    }

            awaitClose {
                registro.remove()
            }
        }

    /*
     * Escucha el documento del partido para enterarse en
     * vivo si otro dispositivo lo finalizó.
     */
    fun escucharPartido(
        idPartido: String
    ): Flow<Partido> =
        callbackFlow {

            val registro =
                partidos
                    .document(idPartido)
                    .addSnapshotListener { documento, error ->

                        if (error != null) {
                            close(error)
                            return@addSnapshotListener
                        }

                        if (documento == null) {
                            return@addSnapshotListener
                        }

                        documento
                            .toObject(Partido::class.java)
                            ?.copy(id = documento.id)
                            ?.let { trySend(it) }
                    }

            awaitClose {
                registro.remove()
            }
        }

    /*
     * Combina la instantánea con el detector de conflictos.
     * Devuelve solo los eventos ACTIVOS, ordenados como la
     * cronología de HU14.
     */
    fun clasificar(
        instantanea: InstantaneaEventos
    ): List<EventoSincronizado> {

        val conflictos =
            DetectorConflictos.detectar(
                instantanea.eventos
            )

        return instantanea.eventos
            .filter {
                it.estado == EstadoEventoPartido.ACTIVO
            }
            .sortedWith(
                compareBy<EventoPartido> {
                    it.minuto
                }.thenBy {
                    it.fechaCaptura
                }
            )
            .map { evento ->
                val motivo =
                    conflictos[evento.id]

                val estado =
                    when {
                        motivo != null ->
                            EstadoSincronizacionEvento.CONFLICTO

                        evento.id in instantanea.pendientePorId ->
                            EstadoSincronizacionEvento.PENDIENTE

                        else ->
                            EstadoSincronizacionEvento.SINCRONIZADO
                    }

                EventoSincronizado(
                    evento = evento,
                    estado = estado,
                    motivoConflicto = motivo.orEmpty()
                )
            }
    }

    /*
     * "Simular sin conexión" del diseño.
     *
     * disableNetwork() corta la comunicación de Firestore
     * con el servidor: las lecturas salen de la caché y las
     * escrituras se encolan, igual que sin señal real.
     */
    suspend fun cambiarConexionSimulada(
        sinConexion: Boolean
    ) {
        if (sinConexion) {
            bd.disableNetwork().await()
        } else {
            bd.enableNetwork().await()
        }
    }

    /*
     * Restaura la red sin esperar (al salir de la pantalla).
     */
    fun restaurarConexion() {
        bd.enableNetwork()
    }

    /*
     * Acción clara de "Reintentar": vuelve a habilitar la
     * red de Firestore, que reenvía la cola local.
     * Los eventos conservan su id, así que el reenvío no
     * crea duplicados (idempotencia).
     */
    suspend fun reintentarSincronizacion() {
        bd.enableNetwork().await()
    }

    /*
     * Caso alterno HU16: si el dispositivo nunca cargó el
     * partido o el catálogo de eventos, no hay datos locales
     * para registrar offline de forma confiable.
     *
     * Consulta SOLO la caché local (Source.CACHE).
     */
    suspend fun registroOfflineDisponible(
        idPartido: String
    ): Boolean =
        try {
            val partido =
                partidos
                    .document(idPartido)
                    .get(Source.CACHE)
                    .await()
                    .toObject(Partido::class.java)

            if (partido == null) {
                false
            } else {
                val catalogo =
                    bd.collection("academias")
                        .document(partido.idAcademia)
                        .collection("catalogoEventos")
                        .get(Source.CACHE)
                        .await()

                val alineacion =
                    bd.collection("alineaciones")
                        .document(idPartido)
                        .get(Source.CACHE)
                        .await()

                !catalogo.isEmpty && alineacion.exists()
            }
        } catch (e: Exception) {
            // FirebaseFirestoreException UNAVAILABLE:
            // el documento no está en la caché
            false
        }

    /*
     * Reenvía un evento que el servidor rechazó, usando el
     * MISMO id de documento: si ya existía, se sobrescribe
     * con los mismos datos y no se duplica.
     */
    suspend fun reintentarEventoRechazado(
        evento: EventoPartido
    ): Result<Boolean> =
        try {
            val tarea =
                partidos
                    .document(evento.idPartido)
                    .collection("eventos")
                    .document(evento.id)
                    .set(evento)

            val confirmado =
                confirmarOEncolar(
                    tarea = tarea,
                    alFallarDespues = { e ->
                        RegistroRechazos.agregar(
                            evento = evento,
                            motivo = e.message
                                ?: "El servidor rechazó el evento"
                        )
                    }
                )

            RegistroRechazos.quitar(evento.id)

            Result.success(confirmado)
        } catch (e: Exception) {
            Result.failure(e)
        }

    /*
     * El operador revisó el conflicto y decide conservar
     * el evento. Funciona también sin conexión.
     */
    suspend fun marcarConflictoRevisado(
        idPartido: String,
        idEvento: String,
        uid: String
    ): Result<Boolean> =
        try {
            if (uid.isBlank()) {
                throw IllegalStateException(
                    "No existe una sesión activa"
                )
            }

            val tarea =
                partidos
                    .document(idPartido)
                    .collection("eventos")
                    .document(idEvento)
                    .update(
                        mapOf(
                            "conflictoRevisado" to true,
                            "revisadoPor" to uid,
                            "fechaRevision" to
                                    System.currentTimeMillis()
                        )
                    )

            Result.success(
                confirmarOEncolar(tarea)
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
}
