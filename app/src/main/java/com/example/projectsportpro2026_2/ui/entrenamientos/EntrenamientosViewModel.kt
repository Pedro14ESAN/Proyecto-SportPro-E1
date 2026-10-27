package com.example.projectsportpro2026_2.ui.entrenamientos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectsportpro2026_2.datos.modelo.Ejercicio
import com.example.projectsportpro2026_2.datos.modelo.Entrenamiento
import com.example.projectsportpro2026_2.datos.modelo.Equipo
import com.example.projectsportpro2026_2.datos.modelo.Rol
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioAutenticacion
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioEntrenamientos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EstadoEntrenamientos(
    val rol: Rol? = null,
    val equipos: List<Equipo> = emptyList(),
    val idEquipoSeleccionado: String = "",
    val ejerciciosActivos: List<Ejercicio> = emptyList(),
    val entrenamientos: List<Entrenamiento> = emptyList(),
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null
)

class EntrenamientosViewModel(
    private val repositorio:
    RepositorioEntrenamientos =
        RepositorioEntrenamientos(),

    private val autenticacion:
    RepositorioAutenticacion =
        RepositorioAutenticacion()
) : ViewModel() {

    private val _estado =
        MutableStateFlow(
            EstadoEntrenamientos()
        )

    val estado:
            StateFlow<EstadoEntrenamientos> =
        _estado.asStateFlow()

    private var iniciado =
        false

    fun iniciar() {

        if (iniciado) return

        iniciado =
            true

        viewModelScope.launch {

            val uid =
                autenticacion
                    .uidActual()

            if (
                uid.isNullOrBlank()
            ) {

                _estado.update {
                    it.copy(
                        error =
                            "No existe una sesión activa"
                    )
                }

                return@launch
            }

            _estado.update {
                it.copy(
                    cargando =
                        true,
                    error =
                        null
                )
            }

            try {

                val perfil =
                    autenticacion
                        .obtenerPerfil(
                            uid
                        )
                        ?: throw IllegalStateException(
                            "Tu cuenta no tiene perfil"
                        )

                if (
                    perfil.rol ==
                    Rol.DT
                ) {

                    val equipos =
                        repositorio
                            .listarEquiposDt(
                                uid
                            )
                            .getOrThrow()

                    val entrenamientos =
                        repositorio
                            .listarParaDt(
                                uid
                            )
                            .getOrThrow()

                    val primerEquipo =
                        equipos
                            .firstOrNull()
                            ?.id
                            .orEmpty()

                    _estado.update {
                        it.copy(
                            rol =
                                perfil.rol,
                            equipos =
                                equipos,
                            idEquipoSeleccionado =
                                primerEquipo,
                            entrenamientos =
                                entrenamientos,
                            cargando =
                                false
                        )
                    }

                    if (
                        primerEquipo.isNotBlank()
                    ) {

                        cargarEjercicios(
                            primerEquipo
                        )
                    }

                } else {

                    val entrenamientos =
                        repositorio
                            .listarParaUsuario(
                                uid
                            )
                            .getOrThrow()

                    _estado.update {
                        it.copy(
                            rol =
                                perfil.rol,
                            entrenamientos =
                                entrenamientos,
                            cargando =
                                false
                        )
                    }
                }

            } catch (
                e: Exception
            ) {

                _estado.update {
                    it.copy(
                        cargando =
                            false,
                        error =
                            e.message
                                ?: "No se pudo cargar la información"
                    )
                }
            }
        }
    }

    fun seleccionarEquipo(
        idEquipo: String
    ) {

        _estado.update {
            it.copy(
                idEquipoSeleccionado =
                    idEquipo
            )
        }

        cargarEjercicios(
            idEquipo
        )
    }

    private fun cargarEjercicios(
        idEquipo: String
    ) {

        viewModelScope.launch {

            val uid =
                autenticacion
                    .uidActual()
                    ?: return@launch

            repositorio
                .listarEjerciciosActivos(
                    idEquipo =
                        idEquipo,
                    uid =
                        uid
                )
                .onSuccess {
                        ejercicios ->

                    _estado.update {
                        it.copy(
                            ejerciciosActivos =
                                ejercicios
                        )
                    }
                }
                .onFailure {
                        error ->

                    _estado.update {
                        it.copy(
                            error =
                                error.message
                                    ?: "No se pudieron cargar los ejercicios"
                        )
                    }
                }
        }
    }

    fun guardar(
        entrenamientoActual:
        Entrenamiento?,
        fecha: String,
        hora: String,
        objetivo: String,
        duracionTexto: String,
        idsEjercicios: List<String>
    ) {

        val duracion =
            duracionTexto
                .toIntOrNull()

        if (
            duracion ==
            null ||
            duracion <= 0
        ) {

            _estado.update {
                it.copy(
                    error =
                        "Ingresa una duración válida"
                )
            }

            return
        }

        val idEquipo =
            entrenamientoActual
                ?.idEquipo
                ?: _estado.value
                    .idEquipoSeleccionado

        if (
            idEquipo.isBlank()
        ) {

            _estado.update {
                it.copy(
                    error =
                        "Selecciona un equipo"
                )
            }

            return
        }

        viewModelScope.launch {

            val uid =
                autenticacion
                    .uidActual()

            if (
                uid.isNullOrBlank()
            ) {
                return@launch
            }

            _estado.update {
                it.copy(
                    guardando =
                        true,
                    error =
                        null,
                    mensaje =
                        null
                )
            }

            val resultado =
                if (
                    entrenamientoActual ==
                    null
                ) {

                    repositorio
                        .crearEntrenamiento(
                            idEquipo =
                                idEquipo,
                            fecha =
                                fecha,
                            hora =
                                hora,
                            objetivo =
                                objetivo,
                            duracionTotalMinutos =
                                duracion,
                            idsEjercicios =
                                idsEjercicios,
                            uid =
                                uid
                        )
                        .map {
                            Unit
                        }

                } else {

                    repositorio
                        .editarEntrenamiento(
                            idEntrenamiento =
                                entrenamientoActual.id,
                            fecha =
                                fecha,
                            hora =
                                hora,
                            objetivo =
                                objetivo,
                            duracionTotalMinutos =
                                duracion,
                            idsEjercicios =
                                idsEjercicios,
                            uid =
                                uid
                        )
                }

            resultado
                .onSuccess {

                    _estado.update {
                        it.copy(
                            guardando =
                                false,
                            mensaje =
                                if (
                                    entrenamientoActual ==
                                    null
                                ) {
                                    "Entrenamiento publicado"
                                } else {
                                    "Entrenamiento actualizado"
                                }
                        )
                    }

                    recargar()
                }
                .onFailure {
                        error ->

                    _estado.update {
                        it.copy(
                            guardando =
                                false,
                            error =
                                error.message
                                    ?: "No se pudo guardar el entrenamiento"
                        )
                    }
                }
        }
    }

    fun cancelar(
        entrenamiento:
        Entrenamiento
    ) {

        viewModelScope.launch {

            val uid =
                autenticacion
                    .uidActual()
                    ?: return@launch

            repositorio
                .cancelarEntrenamiento(
                    idEntrenamiento =
                        entrenamiento.id,
                    uid =
                        uid
                )
                .onSuccess {

                    _estado.update {
                        it.copy(
                            mensaje =
                                "Entrenamiento cancelado"
                        )
                    }

                    recargar()
                }
                .onFailure {
                        error ->

                    _estado.update {
                        it.copy(
                            error =
                                error.message
                                    ?: "No se pudo cancelar"
                        )
                    }
                }
        }
    }

    private fun recargar() {

        viewModelScope.launch {

            val uid =
                autenticacion
                    .uidActual()
                    ?: return@launch

            repositorio
                .listarParaUsuario(
                    uid
                )
                .onSuccess {
                        entrenamientos ->

                    _estado.update {
                        it.copy(
                            entrenamientos =
                                entrenamientos
                        )
                    }
                }
        }
    }

    fun limpiarError() {

        _estado.update {
            it.copy(
                error =
                    null
            )
        }
    }
}