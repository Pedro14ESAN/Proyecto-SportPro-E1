package com.example.projectsportpro2026_2.ui.ejercicios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectsportpro2026_2.datos.modelo.Academia
import com.example.projectsportpro2026_2.datos.modelo.Ejercicio
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioAutenticacion
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioEjercicios
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EstadoEjercicios(
    val academias: List<Academia> = emptyList(),
    val idAcademiaSeleccionada: String = "",
    val ejercicios: List<Ejercicio> = emptyList(),
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null
)

class EjerciciosViewModel(
    private val repositorio:
    RepositorioEjercicios =
        RepositorioEjercicios(),

    private val autenticacion:
    RepositorioAutenticacion =
        RepositorioAutenticacion()
) : ViewModel() {

    private val _estado =
        MutableStateFlow(
            EstadoEjercicios()
        )

    val estado:
            StateFlow<EstadoEjercicios> =
        _estado.asStateFlow()

    private var iniciado =
        false

    fun iniciar() {

        if (iniciado) return

        iniciado =
            true

        viewModelScope.launch {

            _estado.update {
                it.copy(
                    cargando = true,
                    error = null
                )
            }

            val uid =
                autenticacion.uidActual()

            if (uid.isNullOrBlank()) {

                _estado.update {
                    it.copy(
                        cargando = false,
                        error =
                            "No existe una sesión activa"
                    )
                }

                return@launch
            }

            repositorio
                .listarAcademiasDisponibles(
                    uid
                )
                .onSuccess { academias ->

                    val primera =
                        academias
                            .firstOrNull()
                            ?.id
                            .orEmpty()

                    _estado.update {
                        it.copy(
                            academias =
                                academias,
                            idAcademiaSeleccionada =
                                primera,
                            cargando =
                                false
                        )
                    }

                    if (
                        primera.isNotBlank()
                    ) {
                        cargarEjercicios(
                            primera
                        )
                    }
                }
                .onFailure { e ->

                    _estado.update {
                        it.copy(
                            cargando = false,
                            error =
                                mensajeError(e)
                        )
                    }
                }
        }
    }

    fun seleccionarAcademia(
        idAcademia: String
    ) {

        _estado.update {
            it.copy(
                idAcademiaSeleccionada =
                    idAcademia
            )
        }

        cargarEjercicios(
            idAcademia
        )
    }

    fun cargarEjercicios(
        idAcademia: String =
            _estado.value
                .idAcademiaSeleccionada
    ) {

        if (
            idAcademia.isBlank()
        ) {
            return
        }

        viewModelScope.launch {

            val uid =
                autenticacion.uidActual()

            if (uid.isNullOrBlank()) {
                return@launch
            }

            _estado.update {
                it.copy(
                    cargando = true,
                    error = null
                )
            }

            repositorio
                .listarEjercicios(
                    idAcademia,
                    uid
                )
                .onSuccess { lista ->

                    _estado.update {
                        it.copy(
                            ejercicios =
                                lista,
                            cargando =
                                false
                        )
                    }
                }
                .onFailure { e ->

                    _estado.update {
                        it.copy(
                            cargando =
                                false,
                            error =
                                mensajeError(e)
                        )
                    }
                }
        }
    }

    fun guardarEjercicio(
        ejercicioActual:
        Ejercicio?,
        nombre: String,
        objetivo: String,
        descripcion: String,
        duracionTexto: String
    ) {

        val duracion =
            duracionTexto
                .trim()
                .toIntOrNull()

        if (
            duracion == null ||
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

        val idAcademia =
            _estado.value
                .idAcademiaSeleccionada

        if (
            idAcademia.isBlank()
        ) {

            _estado.update {
                it.copy(
                    error =
                        "Selecciona una academia"
                )
            }

            return
        }

        viewModelScope.launch {

            val uid =
                autenticacion.uidActual()

            if (uid.isNullOrBlank()) {

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
                    guardando = true,
                    error = null,
                    mensaje = null
                )
            }

            val resultado =
                if (
                    ejercicioActual ==
                    null
                ) {

                    repositorio
                        .crearEjercicio(
                            nombre =
                                nombre,
                            objetivo =
                                objetivo,
                            descripcion =
                                descripcion,
                            duracionMinutos =
                                duracion,
                            idAcademia =
                                idAcademia,
                            uid =
                                uid
                        )
                        .map {
                            Unit
                        }

                } else {

                    repositorio
                        .editarEjercicio(
                            idEjercicio =
                                ejercicioActual.id,
                            nombre =
                                nombre,
                            objetivo =
                                objetivo,
                            descripcion =
                                descripcion,
                            duracionMinutos =
                                duracion,
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
                                    ejercicioActual ==
                                    null
                                ) {
                                    "Ejercicio creado correctamente"
                                } else {
                                    "Ejercicio actualizado correctamente"
                                }
                        )
                    }

                    cargarEjercicios()
                }
                .onFailure { e ->

                    _estado.update {
                        it.copy(
                            guardando =
                                false,
                            error =
                                mensajeError(e)
                        )
                    }
                }
        }
    }

    fun cambiarEstado(
        ejercicio: Ejercicio
    ) {

        viewModelScope.launch {

            val uid =
                autenticacion.uidActual()

            if (uid.isNullOrBlank()) {
                return@launch
            }

            repositorio
                .cambiarEstado(
                    idEjercicio =
                        ejercicio.id,
                    activo =
                        !ejercicio.activo,
                    uid =
                        uid
                )
                .onSuccess {

                    _estado.update {
                        it.copy(
                            mensaje =
                                if (
                                    ejercicio.activo
                                ) {
                                    "Ejercicio inactivado"
                                } else {
                                    "Ejercicio activado"
                                },
                            error =
                                null
                        )
                    }

                    cargarEjercicios()
                }
                .onFailure { e ->

                    _estado.update {
                        it.copy(
                            error =
                                mensajeError(e)
                        )
                    }
                }
        }
    }

    fun limpiarError() {

        _estado.update {
            it.copy(
                error = null
            )
        }
    }

    fun limpiarMensaje() {

        _estado.update {
            it.copy(
                mensaje = null
            )
        }
    }

    private fun mensajeError(
        e: Throwable
    ): String {

        return e.message
            ?: "No se pudo completar la operación"
    }
}