package com.example.projectsportpro2026_2.datos.repositorio

import com.example.projectsportpro2026_2.datos.modelo.Academia
import com.example.projectsportpro2026_2.datos.modelo.EstadoCuenta
import com.example.projectsportpro2026_2.datos.modelo.Jugador
import com.example.projectsportpro2026_2.datos.modelo.PerfilUsuario
import com.example.projectsportpro2026_2.datos.modelo.Rol
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// Cuenta de jugador que coincide, por documento, con una ficha que aún no tiene cuenta
data class CuentaPorVincular(
    val perfil: PerfilUsuario,
    val ficha: Jugador
) {
    val pendiente: Boolean get() = perfil.estado == EstadoCuenta.PENDIENTE
    val tieneApoderado: Boolean get() = ficha.idsApoderados.isNotEmpty()

    // Se activa al vincular solo si ya hay un apoderado con vinculación activa
    val seActivara: Boolean get() = pendiente && tieneApoderado
}

enum class ResultadoVinculo { VINCULADA, VINCULADA_Y_ACTIVADA, VINCULADA_PENDIENTE }

class RepositorioCuentasJugador(
    private val bd: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val usuarios = bd.collection("usuarios")
    private val jugadores = bd.collection("jugadores")
    private val academias = bd.collection("academias")

    private suspend fun <T> intentar(bloque: suspend () -> T): Result<T> =
        try {
            Result.success(bloque())
        } catch (e: Exception) {
            Result.failure(e)
        }

    private suspend fun verificarAdministrador(uid: String) {
        val perfil = usuarios.document(uid).get().await().toObject(PerfilUsuario::class.java)
        if (perfil == null || perfil.rol != Rol.ADM) {
            throw IllegalStateException("Solo el administrador puede vincular cuentas de jugadores")
        }
    }

    private suspend fun buscar(uid: String): List<CuentaPorVincular> {
        verificarAdministrador(uid)

        val idsAcademias = academias.whereArrayContains("idsAutorizados", uid)
            .get().await().documents.map { it.id }.toSet()
        if (idsAcademias.isEmpty()) return emptyList()

        val cuentas = usuarios.whereEqualTo("rol", Rol.JUG.name)
            .get().await().toObjects(PerfilUsuario::class.java)
            .filter { it.documento.isNotBlank() }
        if (cuentas.isEmpty()) return emptyList()

        val cuentasPorDocumento = cuentas.groupBy { it.documento.trim() }

        val fichas = cuentasPorDocumento.keys.toList().chunked(10).flatMap { grupo ->
            jugadores.whereIn("documento", grupo).get().await().documents.mapNotNull { d ->
                d.toObject(Jugador::class.java)?.copy(id = d.id)
            }
        }.filter { it.activo && it.idUsuario.isBlank() && it.idAcademia in idsAcademias }

        return fichas.flatMap { ficha ->
            cuentasPorDocumento[ficha.documento.trim()].orEmpty()
                .map { perfil -> CuentaPorVincular(perfil, ficha) }
        }.sortedWith(
            compareByDescending<CuentaPorVincular> { it.pendiente }.thenBy { it.perfil.nombre }
        )
    }

    suspend fun listar(uid: String): Result<List<CuentaPorVincular>> = intentar { buscar(uid) }

    // Enlaza la cuenta con la ficha (jugadores.idUsuario) y, si corresponde, la activa
    suspend fun vincular(
        idCuenta: String, idFicha: String, uid: String
    ): Result<ResultadoVinculo> = intentar {
        verificarAdministrador(uid)

        val documentoFicha = jugadores.document(idFicha)
        val ficha = documentoFicha.get().await().toObject(Jugador::class.java)
            ?: throw IllegalStateException("La ficha no existe")
        val academia = academias.document(ficha.idAcademia).get().await()
            .toObject(Academia::class.java)
        if (academia == null || uid !in academia.idsAutorizados) {
            throw IllegalStateException("No tienes permiso sobre la academia de este jugador")
        }
        if (!ficha.activo) throw IllegalStateException("La ficha del jugador está inactiva")
        if (ficha.idUsuario.isNotBlank()) {
            throw IllegalStateException("Esa ficha ya tiene una cuenta vinculada")
        }

        val documentoCuenta = usuarios.document(idCuenta)
        val cuenta = documentoCuenta.get().await().toObject(PerfilUsuario::class.java)
            ?: throw IllegalStateException("La cuenta no existe")
        if (cuenta.rol != Rol.JUG) throw IllegalStateException("La cuenta no es de un jugador")
        if (cuenta.documento.trim() != ficha.documento.trim()) {
            throw IllegalStateException("El documento de la cuenta no coincide con el de la ficha")
        }

        val otras = jugadores.whereEqualTo("idUsuario", idCuenta).limit(1).get().await()
        if (!otras.isEmpty) throw IllegalStateException("Esa cuenta ya está vinculada a otra ficha")

        val activar = cuenta.estado == EstadoCuenta.PENDIENTE && ficha.idsApoderados.isNotEmpty()

        val lote = bd.batch()
        lote.update(
            documentoFicha,
            mapOf(
                "idUsuario" to idCuenta,
                "actualizadoPor" to uid,
                "fechaActualizacion" to System.currentTimeMillis()
            )
        )
        if (activar) {
            lote.update(documentoCuenta, "estado", EstadoCuenta.ACTIVA.name)
        }
        lote.commit().await()

        when {
            activar -> ResultadoVinculo.VINCULADA_Y_ACTIVADA
            cuenta.estado == EstadoCuenta.PENDIENTE -> ResultadoVinculo.VINCULADA_PENDIENTE
            else -> ResultadoVinculo.VINCULADA
        }
    }
}