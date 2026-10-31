package com.example.projectsportpro2026_2.ui.navegacion

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioAutenticacion
import com.example.projectsportpro2026_2.ui.academias.PantallaAcademias
import com.example.projectsportpro2026_2.ui.alineacion.PantallaAlineacion
import com.example.projectsportpro2026_2.ui.asistencias.PantallaAsistencias
import com.example.projectsportpro2026_2.ui.autenticacion.PantallaLogin
import com.example.projectsportpro2026_2.ui.autenticacion.PantallaRegistro
import com.example.projectsportpro2026_2.ui.catalogoeventos.PantallaCatalogoEventos
import com.example.projectsportpro2026_2.ui.convocatoria.PantallaConvocatoria
import com.example.projectsportpro2026_2.ui.convocatoria.PantallaMisConvocatorias
import com.example.projectsportpro2026_2.ui.ejercicios.PantallaEjercicios
import com.example.projectsportpro2026_2.ui.entrenamientos.PantallaEntrenamientos
import com.example.projectsportpro2026_2.ui.equipos.PantallaEquipos
import com.example.projectsportpro2026_2.ui.eventospartido.PantallaPartidoEnVivo
import com.example.projectsportpro2026_2.ui.inicio.InicioAdministrador
import com.example.projectsportpro2026_2.ui.inicio.InicioApoderado
import com.example.projectsportpro2026_2.ui.inicio.InicioEntrenador
import com.example.projectsportpro2026_2.ui.inicio.InicioJugador
import com.example.projectsportpro2026_2.ui.inicio.PantallaCuentaPendiente
import com.example.projectsportpro2026_2.ui.jugadores.PantallaJugadores
import com.example.projectsportpro2026_2.ui.mensualidades.PantallaMensualidades
import com.example.projectsportpro2026_2.ui.partidos.PantallaCrearPartido
import com.example.projectsportpro2026_2.ui.partidos.PantallaDetallePartido
import com.example.projectsportpro2026_2.ui.partidos.PantallaEditarPartido
import com.example.projectsportpro2026_2.ui.partidos.PantallaPartidos
import com.example.projectsportpro2026_2.ui.vinculaciones.PantallaMisJugadores
import com.example.projectsportpro2026_2.ui.vinculaciones.PantallaVinculaciones

@Composable
fun NavegacionApp() {

    val repositorio =
        remember {
            RepositorioAutenticacion()
        }

    val navController =
        rememberNavController()

    var destinoInicial by
    remember {
        mutableStateOf<String?>(
            null
        )
    }

    LaunchedEffect(Unit) {

        val uid =
            repositorio.uidActual()

        destinoInicial =
            if (uid == null) {

                Rutas.LOGIN

            } else {

                val perfil =
                    try {
                        repositorio.obtenerPerfil(uid)
                    } catch (e: Exception) {
                        null
                    }

                if (perfil != null) {
                    rutaInicioPara(perfil)
                } else {
                    Rutas.LOGIN
                }
            }
    }

    val inicio =
        destinoInicial

    if (inicio == null) {

        Box(
            modifier =
                Modifier.fillMaxSize(),
            contentAlignment =
                Alignment.Center
        ) {

            CircularProgressIndicator()
        }

        return
    }

    val irA: (String) -> Unit = { ruta ->

        navController.navigate(ruta) {
            popUpTo(
                navController.graph.id
            ) {
                inclusive = true
            }
        }
    }

    val cerrarSesion: () -> Unit = {

        repositorio.cerrarSesion()

        irA(
            Rutas.LOGIN
        )
    }

    val abrirAcademias: () -> Unit = {
        navController.navigate(
            Rutas.ACADEMIAS
        )
    }

    val abrirMensualidades: () -> Unit = {
        navController.navigate(
            Rutas.MENSUALIDADES
        )
    }

    val abrirEjercicios: () -> Unit = {
        navController.navigate(
            Rutas.EJERCICIOS
        )
    }

    val abrirEntrenamientos: () -> Unit = {
        navController.navigate(
            Rutas.ENTRENAMIENTOS
        )
    }

    val abrirAsistencias: () -> Unit = {
        navController.navigate(
            Rutas.ASISTENCIAS
        )
    }

    NavHost(
        navController =
            navController,
        startDestination =
            inicio
    ) {

        composable(
            Rutas.LOGIN
        ) {

            PantallaLogin(
                onSesionIniciada = { perfil ->

                    irA(
                        rutaInicioPara(
                            perfil
                        )
                    )
                },
                onIrARegistro = {
                    navController.navigate(
                        Rutas.REGISTRO
                    )
                }
            )
        }

        composable(
            Rutas.REGISTRO
        ) {

            PantallaRegistro(
                onRegistrado = { perfil ->

                    irA(
                        rutaInicioPara(
                            perfil
                        )
                    )
                },
                onIrALogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            Rutas.INICIO_ADM
        ) {

            InicioAdministrador(
                onGestionarAcademias =
                    abrirAcademias,
                onVinculaciones = {
                    navController.navigate(
                        Rutas.VINCULACIONES
                    )
                },
                onMensualidades =
                    abrirMensualidades,
                onEjercicios =
                    abrirEjercicios,
                onAsistencias =
                    abrirAsistencias,
                onCerrarSesion =
                    cerrarSesion
            )
        }

        composable(
            Rutas.INICIO_DT
        ) {

            InicioEntrenador(
                onGestionarAcademias =
                    abrirAcademias,
                onEjercicios =
                    abrirEjercicios,
                onEntrenamientos =
                    abrirEntrenamientos,
                onAsistencias =
                    abrirAsistencias,
                onCerrarSesion =
                    cerrarSesion
            )
        }

        composable(
            Rutas.INICIO_JUG
        ) {

            InicioJugador(
                onMensualidades =
                    abrirMensualidades,
                onEntrenamientos =
                    abrirEntrenamientos,
                onAsistencias =
                    abrirAsistencias,
                onConvocatorias = {
                    navController.navigate(
                        Rutas.MIS_CONVOCATORIAS
                    )
                },
                onCerrarSesion =
                    cerrarSesion
            )
        }

        composable(
            Rutas.INICIO_PAD
        ) {

            InicioApoderado(
                onMisJugadores = {
                    navController.navigate(
                        Rutas.MIS_JUGADORES
                    )
                },
                onMensualidades =
                    abrirMensualidades,
                onEntrenamientos =
                    abrirEntrenamientos,
                onAsistencias =
                    abrirAsistencias,
                onConvocatorias = {
                    navController.navigate(
                        Rutas.MIS_CONVOCATORIAS
                    )
                },
                onCerrarSesion =
                    cerrarSesion
            )
        }

        composable(
            Rutas.CUENTA_PENDIENTE
        ) {

            PantallaCuentaPendiente(
                onCerrarSesion =
                    cerrarSesion
            )
        }

        composable(
            Rutas.ACADEMIAS
        ) {

            PantallaAcademias(
                onVolver = {
                    navController.popBackStack()
                },
                onAbrirEquipos = {
                        id,
                        nombre ->

                    navController.navigate(
                        rutaEquipos(
                            id,
                            nombre
                        )
                    )
                },
                onAbrirPartidos = {
                        id,
                        nombre ->

                    navController.navigate(
                        rutaPartidos(
                            id,
                            nombre
                        )
                    )
                },
                onAbrirCatalogo = {
                        idAcademia ->

                    navController.navigate(
                        rutaCatalogoEventos(
                            idAcademia
                        )
                    )
                }
            )
        }

        composable(
            route =
                Rutas.EQUIPOS,
            arguments =
                listOf(
                    navArgument(
                        "idAcademia"
                    ) {
                        type =
                            NavType.StringType
                    },
                    navArgument(
                        "nombre"
                    ) {
                        type =
                            NavType.StringType
                    }
                )
        ) { entrada ->

            PantallaEquipos(
                idAcademia =
                    entrada.arguments
                        ?.getString(
                            "idAcademia"
                        )
                        .orEmpty(),
                nombreAcademia =
                    entrada.arguments
                        ?.getString(
                            "nombre"
                        )
                        .orEmpty(),
                onVolver = {
                    navController.popBackStack()
                },
                onAbrirJugadores = {
                        id,
                        nombre ->

                    navController.navigate(
                        rutaJugadores(
                            id,
                            nombre
                        )
                    )
                }
            )
        }

        composable(
            route =
                Rutas.PARTIDOS,
            arguments =
                listOf(
                    navArgument(
                        "idAcademia"
                    ) {
                        type =
                            NavType.StringType
                    },
                    navArgument(
                        "nombre"
                    ) {
                        type =
                            NavType.StringType
                    }
                )
        ) { entrada ->

            val idAcademia =
                entrada.arguments
                    ?.getString(
                        "idAcademia"
                    )
                    .orEmpty()

            val nombreAcademia =
                entrada.arguments
                    ?.getString(
                        "nombre"
                    )
                    .orEmpty()

            PantallaPartidos(
                idAcademia =
                    idAcademia,
                nombreAcademia =
                    nombreAcademia,
                onVolver = {
                    navController.popBackStack()
                },
                onCrearPartido = {
                    navController.navigate(
                        rutaCrearPartido(
                            idAcademia,
                            nombreAcademia
                        )
                    )
                },
                onAbrirPartido = {
                        idPartido ->

                    navController.navigate(
                        rutaDetallePartido(
                            idPartido
                        )
                    )
                }
            )
        }

        composable(
            route =
                Rutas.CREAR_PARTIDO,
            arguments =
                listOf(
                    navArgument(
                        "idAcademia"
                    ) {
                        type =
                            NavType.StringType
                    },
                    navArgument(
                        "nombre"
                    ) {
                        type =
                            NavType.StringType
                    }
                )
        ) { entrada ->

            val idAcademia =
                entrada.arguments
                    ?.getString(
                        "idAcademia"
                    )
                    .orEmpty()

            PantallaCrearPartido(
                idAcademia =
                    idAcademia,
                onVolver = {
                    navController.popBackStack()
                },
                onPartidoCreado = {
                        idPartido ->

                    navController.popBackStack()

                    navController.navigate(
                        rutaDetallePartido(
                            idPartido
                        )
                    )
                }
            )
        }

        composable(
            route =
                Rutas.DETALLE_PARTIDO,
            arguments =
                listOf(
                    navArgument(
                        "idPartido"
                    ) {
                        type =
                            NavType.StringType
                    }
                )
        ) { entrada ->

            val idPartido =
                entrada.arguments
                    ?.getString(
                        "idPartido"
                    )
                    .orEmpty()

            PantallaDetallePartido(
                idPartido =
                    idPartido,
                onVolver = {
                    navController.popBackStack()
                },
                onEditar = {
                        partidoId ->

                    navController.navigate(
                        rutaEditarPartido(
                            partidoId
                        )
                    )
                },
                onGestionarConvocatoria = {
                    navController.navigate(
                        rutaConvocatoria(
                            idPartido
                        )
                    )
                },
                onVerAlineacion = {
                    navController.navigate(
                        rutaAlineacion(
                            idPartido
                        )
                    )
                },
                onAbrirPartidoEnVivo = {
                    navController.navigate(
                        rutaPartidoEnVivo(
                            idPartido
                        )
                    )
                }
            )
        }

        composable(
            route =
                Rutas.CONVOCATORIA,
            arguments =
                listOf(
                    navArgument(
                        "idPartido"
                    ) {
                        type =
                            NavType.StringType
                    }
                )
        ) { entrada ->

            val idPartido =
                entrada.arguments
                    ?.getString(
                        "idPartido"
                    )
                    .orEmpty()

            PantallaConvocatoria(
                idPartido =
                    idPartido,
                onVolver = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route =
                Rutas.ALINEACION,
            arguments =
                listOf(
                    navArgument(
                        "idPartido"
                    ) {
                        type =
                            NavType.StringType
                    }
                )
        ) { entrada ->

            val idPartido =
                entrada.arguments
                    ?.getString(
                        "idPartido"
                    )
                    .orEmpty()

            PantallaAlineacion(
                idPartido =
                    idPartido,
                onVolver = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route =
                Rutas.EDITAR_PARTIDO,
            arguments =
                listOf(
                    navArgument(
                        "idPartido"
                    ) {
                        type =
                            NavType.StringType
                    }
                )
        ) { entrada ->

            val idPartido =
                entrada.arguments
                    ?.getString(
                        "idPartido"
                    )
                    .orEmpty()

            PantallaEditarPartido(
                idPartido =
                    idPartido,
                onVolver = {
                    navController.popBackStack()
                },
                onPartidoEditado = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route =
                Rutas.JUGADORES,
            arguments =
                listOf(
                    navArgument(
                        "idEquipo"
                    ) {
                        type =
                            NavType.StringType
                    },
                    navArgument(
                        "nombre"
                    ) {
                        type =
                            NavType.StringType
                    }
                )
        ) { entrada ->

            PantallaJugadores(
                idEquipo =
                    entrada.arguments
                        ?.getString(
                            "idEquipo"
                        )
                        .orEmpty(),
                nombreEquipo =
                    entrada.arguments
                        ?.getString(
                            "nombre"
                        )
                        .orEmpty(),
                onVolver = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            Rutas.MIS_CONVOCATORIAS
        ) {

            PantallaMisConvocatorias(
                onVolver = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            Rutas.MIS_JUGADORES
        ) {

            PantallaMisJugadores(
                onVolver = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            Rutas.VINCULACIONES
        ) {

            PantallaVinculaciones(
                onVolver = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            Rutas.MENSUALIDADES
        ) {

            PantallaMensualidades(
                onVolver = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            Rutas.EJERCICIOS
        ) {

            PantallaEjercicios(
                onVolver = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            Rutas.ENTRENAMIENTOS
        ) {

            PantallaEntrenamientos(
                onVolver = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            Rutas.ASISTENCIAS
        ) {

            PantallaAsistencias(
                onVolver = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route =
                Rutas.CATALOGO_EVENTOS,
            arguments =
                listOf(
                    navArgument(
                        "idAcademia"
                    ) {
                        type =
                            NavType.StringType
                    }
                )
        ) { entrada ->

            val idAcademia =
                entrada.arguments
                    ?.getString(
                        "idAcademia"
                    )
                    .orEmpty()

            PantallaCatalogoEventos(
                idAcademia =
                    idAcademia,
                onVolver = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route =
                Rutas.PARTIDO_EN_VIVO,
            arguments =
                listOf(
                    navArgument(
                        "idPartido"
                    ) {
                        type =
                            NavType.StringType
                    }
                )
        ) { entrada ->

            val idPartido =
                entrada.arguments
                    ?.getString(
                        "idPartido"
                    )
                    .orEmpty()

            PantallaPartidoEnVivo(
                idPartido =
                    idPartido,
                onVolver = {
                    navController.popBackStack()
                }
            )
        }
    }
}