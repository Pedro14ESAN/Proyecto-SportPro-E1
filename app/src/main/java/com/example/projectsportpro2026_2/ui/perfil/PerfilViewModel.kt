package com.example.projectsportpro2026_2.ui.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectsportpro2026_2.datos.modelo.PerfilUsuario
import com.example.projectsportpro2026_2.datos.modelo.Rol
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioAcademias
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioAsistencias
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioAutenticacion
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Perfil de solo lectura (Línea de cal). [academias] es null mientras carga o si no se pudo
 * leer, y una lista vacía si el usuario no tiene academia.
 */
data class EstadoPerfil(
    val cargando: Boolean = true,
    val perfil: PerfilUsuario? = null,
    val academias: List<String>? = null,
    val academiasCargadas: Boolean = false,
    val error: String? = null
)

/**
 * Solo lee datos que ya existen; no escribe nada. La academia se obtiene igual que en el
 * resto de la app: ADM y DT por las academias donde están autorizados; JUG y PAD por la
 * academia de los jugadores que pueden consultar.
 */
class PerfilViewModel(
    private val autenticacion: RepositorioAutenticacion = RepositorioAutenticacion(),
    private val academias: RepositorioAcademias = RepositorioAcademias(),
    private val asistencias: RepositorioAsistencias = RepositorioAsistencias(),
    private val bd: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _estado = MutableStateFlow(EstadoPerfil())
    val estado: StateFlow<EstadoPerfil> = _estado.asStateFlow()

    init {
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }

            val uid = autenticacion.uidActual()
            if (uid == null) {
                _estado.update { it.copy(cargando = false, error = "No hay una sesión activa") }
                return@launch
            }

            val perfil = try {
                autenticacion.obtenerPerfil(uid)
            } catch (e: Exception) {
                null
            }
            if (perfil == null) {
                _estado.update {
                    it.copy(cargando = false, error = "No se pudo cargar tu perfil. Revisa tu conexión e inténtalo de nuevo")
                }
                return@launch
            }

            _estado.update { it.copy(cargando = false, perfil = perfil) }

            val nombres = nombresAcademias(uid, perfil.rol)
            _estado.update { it.copy(academias = nombres, academiasCargadas = true) }
        }
    }

    private suspend fun nombresAcademias(uid: String, rol: Rol): List<String>? =
        when (rol) {
            Rol.ADM, Rol.DT ->
                academias.listarAcademias(uid).getOrNull()?.map { it.nombre }

            Rol.JUG, Rol.PAD ->
                asistencias.listarJugadoresConsulta(uid).getOrNull()
                    ?.map { it.idAcademia }
                    ?.filter { it.isNotBlank() }
                    ?.distinct()
                    ?.mapNotNull { idAcademia ->
                        try {
                            bd.collection("academias").document(idAcademia)
                                .get().await().getString("nombre")
                        } catch (e: Exception) {
                            null
                        }
                    }
        }?.filter { it.isNotBlank() }?.distinct()
}
