package com.example.projectsportpro2026_2.ui.navegacion

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
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
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.projectsportpro2026_2.datos.repositorio.RepositorioAutenticacion
import com.example.projectsportpro2026_2.ui.academias.PantallaAcademias
import com.example.projectsportpro2026_2.ui.alineacion.PantallaAlineacion
import com.example.projectsportpro2026_2.ui.asistencias.PantallaAsistencias
import com.example.projectsportpro2026_2.ui.autenticacion.PantallaBienvenida
import com.example.projectsportpro2026_2.ui.autenticacion.PantallaIntroduccion
import com.example.projectsportpro2026_2.ui.autenticacion.PantallaLogin
import com.example.projectsportpro2026_2.ui.planes.PantallaPlanes
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
import com.example.projectsportpro2026_2.ui.seguimiento.PantallaMisPartidos
import com.example.projectsportpro2026_2.ui.seguimiento.PantallaSeguimientoPartido
import com.example.projectsportpro2026_2.ui.vinculaciones.PantallaMisJugadores
import com.example.projectsportpro2026_2.ui.vinculaciones.PantallaVinculaciones
import com.example.projectsportpro2026_2.ui.anuncios.PantallaAnuncios
import com.example.projectsportpro2026_2.ui.comunidad.PantallaComunidad
import com.example.projectsportpro2026_2.ui.moderacion.PantallaPanelModeracion
import androidx.compose.ui.platform.LocalContext
import com.example.projectsportpro2026_2.ui.notificaciones.MonitorNotificaciones
import com.example.projectsportpro2026_2.ui.notificaciones.PantallaNotificaciones
import com.example.projectsportpro2026_2.ui.notificaciones.DestinoNotificacion
import kotlinx.coroutines.delay
import com.example.projectsportpro2026_2.ui.autenticacion.PantallaRecuperar
import com.example.projectsportpro2026_2.ui.estadisticas.PantallaEstadisticas
import com.example.projectsportpro2026_2.ui.resumen.PantallaResumenPartido
import com.example.projectsportpro2026_2.ui.vinculaciones.PantallaCuentasJugadores
import com.example.projectsportpro2026_2.datos.modelo.PerfilUsuario
import com.example.projectsportpro2026_2.ui.perfil.PantallaPerfil
import com.example.projectsportpro2026_2.ui.theme.LineaDeCal

@OptIn(ExperimentalLayoutApi::class) // WindowInsets.isImeVisible (ocultar la barra con el teclado)
@Composable
fun NavegacionApp() {

    val repositorio =
        remember {
            RepositorioAutenticacion()
        }

    val navController =
        rememberNavController()

    // HU20: notificaciones del celular mientras la app está abierta
    val contextoApp = LocalContext.current.applicationContext
    LaunchedEffect(Unit) {
        MonitorNotificaciones.iniciar(contextoApp)
    }

    var destinoInicial by
    remember {
        mutableStateOf<String?>(
            null
        )
    }

    // Línea de cal: perfil de la sesión, para saber qué pestañas lleva la barra inferior
    var perfilActual by
    remember {
        mutableStateOf<PerfilUsuario?>(
            null
        )
    }

    LaunchedEffect(Unit) {

        val uid =
            repositorio.uidActual()

        destinoInicial =
            if (uid == null) {

                // Sin sesión: portada → introducción → login
                Rutas.BIENVENIDA

            } else {

                val perfil =
                    try {
                        repositorio.obtenerPerfil(uid)
                    } catch (e: Exception) {
                        null
                    }

                if (perfil != null) {
                    perfilActual = perfil
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

        perfilActual = null

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

    // HU17: partidos para seguir en tiempo real
    val abrirPartidosEnVivo: () -> Unit = {
        navController.navigate(
            Rutas.MIS_PARTIDOS
        )
    }
    val abrirAnuncios: () -> Unit = {
        navController.navigate(
            Rutas.ANUNCIOS
        )
    }
    val abrirComunidad: () -> Unit = {
        navController.navigate(
            Rutas.COMUNIDAD
        )
    }
    val abrirNotificaciones: () -> Unit = {
        navController.navigate(
            Rutas.NOTIFICACIONES
        )
    }

    // HU18: estadísticas
    val abrirEstadisticas: () -> Unit = {
        navController.navigate(
            Rutas.ESTADISTICAS
        )
    }

    // HU26/HU27: resumen narrativo del partido
    val abrirResumen: (String) -> Unit = { idPartido ->
        navController.navigate(
            rutaResumenPartido(idPartido)
        )
    }

    // Línea de cal: barra de navegación inferior. Es un atajo adicional: los botones del
    // inicio y todas las rutas siguen igual. Va en todas las pantallas de la sesión, salvo
    // autenticación y cuenta pendiente, y se oculta con el teclado abierto.
    val entradaActual by
    navController.currentBackStackEntryAsState()

    val rutaActual =
        entradaActual?.destination?.route

    val pestanas =
        pestanasPara(perfilActual)

    val tecladoAbierto =
        WindowInsets.isImeVisible

    val mostrarBarra =
        barraVisibleEn(rutaActual, pestanas.isNotEmpty()) && !tecladoAbierto

    // En las pantallas internas se resalta la pestaña de su sección
    val rutaActiva =
        rutaPestanaActiva(rutaActual, pestanas.map { it.ruta })

    val irAPestana: (PestanaNavegacion) -> Unit = irAPestana@{ pestana ->
        val perfil = perfilActual ?: return@irAPestana
        val rutaInicio = rutaInicioPara(perfil)

        // Ya está en esa pestaña: no se recrea la pantalla
        if (navController.currentDestination?.route == pestana.ruta) return@irAPestana

        if (pestana.ruta == rutaInicio) {
            // Inicio: se vuelve al inicio que ya está en la pila (sin saveState/restoreState,
            // que restauraban la pantalla anterior en lugar del inicio)
            val volvio = navController.popBackStack(rutaInicio, inclusive = false)
            if (!volvio) {
                navController.navigate(rutaInicio) {
                    popUpTo(navController.graph.id) { inclusive = true }
                    launchSingleTop = true
                }
            }
            return@irAPestana
        }

        val inicioEnPila =
            runCatching { navController.getBackStackEntry(rutaInicio) }.isSuccess

        if (!inicioEnPila) {
            // Caso raro: se rehace inicio → pestaña, para que atrás desde la pestaña vuelva al inicio
            navController.navigate(rutaInicio) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        }

        // Otras pestañas: la pila queda [inicio, pestaña], sin apilar pantallas
        navController.navigate(pestana.ruta) {
            popUpTo(rutaInicio)
            launchSingleTop = true
        }
    }

    Scaffold(
        containerColor =
            LineaDeCal.colores.fondo,
        // Los insets del sistema los sigue manejando cada pantalla; aquí solo se reserva la barra
        contentWindowInsets =
            WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (mostrarBarra) {
                BarraNavegacionInferior(
                    pestanas = pestanas,
                    rutaActiva = rutaActiva,
                    onElegir = irAPestana
                )
            }
        }
    ) { relleno ->

        NavHost(
            navController =
                navController,
            startDestination =
                inicio,
            modifier =
                Modifier
                    .fillMaxSize()
                    // Ninguna pantalla queda tapada: su contenido, sus botones inferiores y
                    // sus FAB van por encima de la barra
                    .padding(relleno)
                    // La barra ya cubre la zona de navegación del sistema: no se repite ese margen
                    .consumeWindowInsets(relleno)
        ) {

            composable(
                Rutas.BIENVENIDA
            ) {
                PantallaBienvenida(
                    onComenzar = {
                        navController.navigate(Rutas.INTRODUCCION)
                    }
                )
            }

            composable(
                Rutas.INTRODUCCION
            ) {
                PantallaIntroduccion(
                    onVolver = {
                        navController.popBackStack()
                    },
                    // Al terminar o saltar, el login queda como única pantalla
                    onTerminar = {
                        irA(Rutas.LOGIN)
                    }
                )
            }

            composable(
                Rutas.LOGIN
            ) {

                PantallaLogin(
                    onSesionIniciada = { perfil ->

                        perfilActual = perfil

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
                    },
                    onIrARecuperar = {
                        navController.navigate(
                            Rutas.RECUPERAR
                        )
                    },
                    // Flecha para regresar a la portada
                    onVolverAlInicio = {
                        irA(Rutas.BIENVENIDA)
                    }
                )
            }

            composable(
                Rutas.REGISTRO
            ) {

                PantallaRegistro(
                    onRegistrado = { perfil ->

                        perfilActual = perfil

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
                    onCuentasJugadores = {
                        navController.navigate(
                            Rutas.CUENTAS_JUGADORES
                        )
                    },
                    onMensualidades =
                        abrirMensualidades,
                    onEjercicios =
                        abrirEjercicios,
                    onAsistencias =
                        abrirAsistencias,
                    onPartidosEnVivo =
                        abrirPartidosEnVivo,
                    onAnuncios =
                        abrirAnuncios,
                    onComunidad =
                        abrirComunidad,
                    onNotificaciones =
                        abrirNotificaciones,
                    onEstadisticas =
                        abrirEstadisticas,
                    onPlanes = {
                        navController.navigate(Rutas.PLANES)
                    },
                    onCerrarSesion =
                        cerrarSesion
                )
            }

            composable(
                Rutas.PLANES
            ) {
                PantallaPlanes(
                    onVolver = { navController.popBackStack() }
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
                    onPartidosEnVivo =
                        abrirPartidosEnVivo,
                    onAnuncios =
                        abrirAnuncios,
                    onComunidad =
                        abrirComunidad,
                    onNotificaciones =
                        abrirNotificaciones,
                    onEstadisticas =
                        abrirEstadisticas,
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
                    onPartidosEnVivo =
                        abrirPartidosEnVivo,
                    onAnuncios =
                        abrirAnuncios,
                    onComunidad =
                        abrirComunidad,
                    onNotificaciones =
                        abrirNotificaciones,
                    onEstadisticas =
                        abrirEstadisticas,
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
                    onPartidosEnVivo =
                        abrirPartidosEnVivo,
                    onAnuncios =
                        abrirAnuncios,
                    onComunidad =
                        abrirComunidad,
                    onNotificaciones =
                        abrirNotificaciones,
                    onEstadisticas =
                        abrirEstadisticas,
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
                    },
                    onAbrirResumen =
                        abrirResumen
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
                Rutas.CUENTAS_JUGADORES
            ) {

                PantallaCuentasJugadores(
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

            composable(Rutas.RECUPERAR) {
                PantallaRecuperar(onVolver = { navController.popBackStack() })
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

            // HU17: lista de partidos y seguimiento en tiempo real
            composable(
                Rutas.MIS_PARTIDOS
            ) {
                PantallaMisPartidos(
                    onVolver = {
                        navController.popBackStack()
                    },
                    onAbrirPartido = { idPartido ->
                        navController.navigate(
                            rutaSeguimientoPartido(idPartido)
                        )
                    }
                )
            }

            composable(
                route = Rutas.SEGUIMIENTO_PARTIDO,
                arguments = listOf(
                    navArgument("idPartido") {
                        type = NavType.StringType
                    }
                )
            ) { entrada ->
                PantallaSeguimientoPartido(
                    idPartido = entrada.arguments
                        ?.getString("idPartido")
                        .orEmpty(),
                    onVolver = {
                        navController.popBackStack()
                    },
                    onVerResumen =
                        abrirResumen
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
            // HU19: anuncios internos
            composable(
                Rutas.ANUNCIOS
            ) {
                PantallaAnuncios(
                    onVolver = {
                        navController.popBackStack()
                    }
                )
            }
            // HU21: comunidad
            // HU21: comunidad
            composable(
                Rutas.COMUNIDAD
            ) {
                PantallaComunidad(
                    onVolver = {
                        navController.popBackStack()
                    },
                    onAbrirModeracion = {
                        navController.navigate(Rutas.MODERACION)
                    }
                )
            }

            // HU24: panel de moderación (solo ADM)
            composable(
                Rutas.MODERACION
            ) {
                PantallaPanelModeracion(
                    onVolver = {
                        navController.popBackStack()
                    }
                )
            }
            // HU18: estadísticas acumuladas por jugador y equipo
            composable(
                Rutas.ESTADISTICAS
            ) {
                PantallaEstadisticas(
                    onVolver = {
                        navController.popBackStack()
                    }
                )
            }

            // HU26/HU27: resumen narrativo, revisión y aprobación del DT
            composable(
                route = Rutas.RESUMEN_PARTIDO,
                arguments = listOf(
                    navArgument("idPartido") {
                        type = NavType.StringType
                    }
                )
            ) { entrada ->
                PantallaResumenPartido(
                    idPartido = entrada.arguments
                        ?.getString("idPartido")
                        .orEmpty(),
                    onVolver = {
                        navController.popBackStack()
                    }
                )
            }

            // HU20: centro de notificaciones
            composable(
                Rutas.NOTIFICACIONES_ABRIR,
                arguments = listOf(
                    navArgument("idNotificacion") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { entrada ->
                PantallaNotificaciones(
                    // HU20 (deep link): aviso tocado en la barra del celular (null = entrada normal)
                    abrirAlIniciar = entrada.arguments?.getString("idNotificacion"),
                    onVolver = {
                        navController.popBackStack()
                    },
                    onAbrirAnuncios = {
                        navController.navigate(Rutas.ANUNCIOS)
                    },
                    onAbrirEntrenamientos = {
                        navController.navigate(Rutas.ENTRENAMIENTOS)
                    },
                    onAbrirConvocatorias = {
                        navController.navigate(Rutas.MIS_CONVOCATORIAS)
                    }
                )
            }

            // Línea de cal: perfil de solo lectura (pestaña "Perfil")
            composable(
                Rutas.PERFIL
            ) {
                PantallaPerfil(
                    onVolver = {
                        navController.popBackStack()
                    },
                    onCerrarSesion =
                        cerrarSesion
                )
            }
        }
    }

    // HU20 (deep link): al tocar una notificación de la barra, abre el centro de
    // notificaciones con ese aviso. Espera a que haya sesión y a que el usuario
    // no esté en login / registro / cuenta pendiente.
    LaunchedEffect(Unit) {
        val pantallasPublicas = setOf(
            Rutas.BIENVENIDA, Rutas.INTRODUCCION,
            Rutas.LOGIN, Rutas.REGISTRO, Rutas.RECUPERAR, Rutas.CUENTA_PENDIENTE
        )
        DestinoNotificacion.pendiente.collect { id ->
            if (id == null) return@collect
            while (
                repositorio.uidActual() == null ||
                navController.currentDestination?.route in pantallasPublicas
            ) {
                delay(500)
            }
            DestinoNotificacion.consumir()?.let { idNotificacion ->
                navController.navigate("notificaciones?abrir=$idNotificacion") {
                    launchSingleTop = true
                }
            }
        }
    }
}
