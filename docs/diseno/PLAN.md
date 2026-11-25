# Plan de implementación · Rediseño "Línea de cal"

Rama: `feature/nahim-diseno-linea-de-cal` · Fuente de verdad: `docs/diseno/ESPECIFICACION.md` (en adelante "la espec").
Este plan **no modifica código**. Es la lista de control para no perder ninguna funcionalidad.

---

## 0. Estado de partida (verificado el 2026-11-24, sin cambios en el código)

| Comando | Resultado |
|---|---|
| `./gradlew assembleDebug` | ✅ BUILD SUCCESSFUL (2 min 55 s) |
| `./gradlew testDebugUnitTest` | ✅ BUILD SUCCESSFUL · **46 tests, 0 fallos, 0 omitidos** |

Detalle de los tests unitarios: `ExampleUnitTest` 1 · `DerivarAlineacionEnVivoTest` 11 · `FormacionesTest` 7 · `IndicadoresJugadorEnVivoTest` 6 · `CalculadoraEstadisticasTest` 5 · `EventosValidosTest` 1 · `InicioAutomaticoPartidoTest` 8 · `DetectorConflictosTest` 7.

Tests instrumentados (`androidTest`, no se ejecutaron porque necesitan un dispositivo): `AlineacionReactivaTest`, `FormacionPersonalizadaTest`, `PersistenciaAlineacionTest`, `SincronizacionCompactaTest`, `ExampleInstrumentedTest`. **Ojo:** buscan textos concretos (ver la duda D-01).

Datos del punto de partida que afectan al plan:

- `Theme.kt` usa `dynamicColor = true` y la paleta morada de la plantilla (Purple40/80). `Type.kt` es la plantilla por defecto. Solo `MainActivity` y 3 previews usan el tema.
- Paletas privadas: 17 archivos con `val X = Color(0x…)` a nivel de archivo (EstiloPartido 11, EstiloSincronizacion 16, ComponentesSportPro 8, PantallaRegistro 10, PantallaJugadores 10, PantallaMisJugadores 11, PantallaVinculaciones 10, etc.). En total hay **259** `Color(0x…)` en `ui/` (muchos van dentro del código, no en declaraciones) y **155** `Color.White`.
- `FontFamily.Serif`/`Monospace`: 81 usos. Textos con 9 a 10 sp: 38 (coincide con la espec). Textos con 11 sp: 62 más (la regla de mínimo 12 sp también les aplica).
- No hay `material-icons-extended`. El BOM de Compose es `2026.02.01`, así que hay que fijar la versión `1.7.8`, como prevé la espec.
- No hay `FileProvider` en el `AndroidManifest.xml` (se necesita para 7.3, compartir el cromo).
- Las pantallas de inicio no reciben el perfil: `NavegacionApp` solo les pasa callbacks. No existe `InicioViewModel`.

---

## 1. Inventario de las 33 pantallas (lista de control)

Leyenda: **A** = acciones y botones · **D** = diálogos y hojas · **F** = filtros y selectores · **E** = estados de la pantalla · **M** = mensajes (pantalla o ViewModel).
Cada línea debe seguir existiendo y funcionando después del rediseño.

### 1.1 `autenticacion/PantallaBienvenida.kt` (incluye `PantallaBienvenida` y `PantallaIntroduccion`)
- **A** Bienvenida: "Comenzar →" (va a la introducción). Introducción: "← Volver", "Continuar" / "Ir a iniciar sesión" en la última página, "Saltar introducción".
- **E** 3 páginas de introducción con indicador "01 / 03 · ETIQUETA": ORGANIZACIÓN ("Todo tu equipo, en la misma cancha."), PARTIDO EN VIVO ("Cada jugada, al minuto."), COMUNIDAD ("Familias y jugadores siempre al tanto.").
- **M** Lema: "La operación de tu academia,\nsiempre en juego." y marca "SPORTPRO / 2026".

### 1.2 `autenticacion/PantallaLogin.kt`
- **A** "← Volver al inicio", "Iniciar sesión" (muestra un progreso mientras carga), "¿Olvidaste tu contraseña?" (va a Recuperar), "Crear cuenta" (va a Registro).
- **F** Campos: CORREO ELECTRÓNICO ("nombre@correo.com") y CONTRASEÑA (oculta, **sin botón para mostrarla** → 7.5).
- **E** cargando · error.
- **M** "Escribe un correo válido", "Escribe tu contraseña", "Correo o contraseña incorrectos", "Demasiados intentos. Espera un momento", "Sin conexión a internet", "No se pudo iniciar sesión", "No se pudo iniciar sesión. Inténtalo de nuevo". Títulos: "Sport PRO" y "Bienvenido".

### 1.3 `autenticacion/PantallaRecuperar.kt`
- **A** "← Volver", "Enviar enlace" (con progreso).
- **F** Campo CORREO ELECTRÓNICO.
- **E** cargando · enviado · error.
- **M** "Escribe tu correo electrónico", "El correo no tiene un formato válido", "Si el correo está registrado, te enviamos un enlace… Revisa también spam.", "Sin conexión. Revisa tu internet e inténtalo de nuevo", "No se pudo enviar el correo. Inténtalo de nuevo". Textos: "RECUPERAR ACCESO", "¿Olvidaste tu contraseña?".

### 1.4 `autenticacion/PantallaRegistro.kt`
- **A** "← Volver", "Registrarme como {jugador|apoderado|entrenador} →" (con progreso), "Iniciar sesión".
- **F** Tarjetas de rol con `RadioButton`: Jugador, Padre / Apoderado, Entrenador / DT. Campos: NOMBRE COMPLETO, CORREO ELECTRÓNICO, CONTRASEÑA (MÍNIMO 6 CARACTERES) (**sin mostrar/ocultar** → 7.5), FECHA DE NACIMIENTO (`CampoFecha` con ayuda "Menores de 12 años no crean cuenta. De 12 a 17 requieren autorización del apoderado."), DOCUMENTO DE IDENTIDAD ("Sirve para que la academia vincule tu cuenta con tu ficha.").
- **E** cargando · error. Aviso fijo: "El rol Administrador es asignado por la academia y no está disponible para registro público."
- **M** "Escribe tu nombre completo", "Escribe un correo válido", "El correo no es válido", "La contraseña debe tener al menos 6 caracteres", "La contraseña es muy débil", "Escribe tu fecha de nacimiento", "Escribe tu documento de identidad", "Ya existe una cuenta con ese correo", "Sin conexión a internet", "No se pudo completar el registro", "No se pudo registrar. Inténtalo de nuevo".

### 1.5 `inicio/PantallasInicio.kt` (`InicioAdministrador`, `InicioEntrenador`, `InicioJugador`, `InicioApoderado`, `PantallaCuentaPendiente`)
- **A, ADM (12)**: Partidos en vivo (destacado), 📊 Estadísticas, Gestionar academias, Vinculaciones, Cuentas de jugadores, Mensualidades, Biblioteca de ejercicios, Asistencias, Anuncios del club, Comunidad, 🔔 Notificaciones, 💎 Planes y ofertas.
- **A, DT (9)**: Partidos en vivo, Estadísticas, Gestionar academias, Biblioteca de ejercicios, Entrenamientos, Asistencias, Anuncios del club, Comunidad, Notificaciones.
- **A, JUG (9)**: Partidos en vivo, Estadísticas, Mis mensualidades, Mis entrenamientos, Mi asistencia, Mis convocatorias, Anuncios del club, Comunidad, Notificaciones.
- **A, PAD (10)**: Partidos en vivo, Estadísticas, Mis jugadores, Mensualidades, Entrenamientos, Historial de asistencia, Convocatorias, Anuncios del club, Comunidad, Notificaciones.
- **A, todos**: "Cerrar sesión" (`OutlinedButton`, llama a `cerrarSesion` en `NavegacionApp`).
- **E** Textos por rol: "Panel del administrador" / "Gestiona tus academias, equipos, jugadores y módulos administrativos."; "Panel del director técnico" / "Gestiona tus equipos, biblioteca, entrenamientos y asistencia."; "Panel del jugador" / "Consulta tu información deportiva y administrativa."; "Panel del apoderado" / "Consulta a los jugadores bajo tu responsabilidad." Cabecera "⚽ SportPro · PANEL PRINCIPAL".
- **CuentaPendiente**: "Cuenta pendiente" / "Tu cuenta debe ser autorizada por tu apoderado y validada por la academia antes de usarla." + "Cerrar sesión".

### 1.6 `academias/PantallaAcademias.kt`
- **A** FAB "+" (nueva academia), "← Volver". En cada tarjeta: Equipos, Partidos, Catálogo, Editar, Inactivar/Activar.
- **D** `FormularioAcademia` (AlertDialog "Nueva academia" / "Editar academia"): Nombre *, Dirección, Descripción · Guardar / Cancelar.
- **E** cargando · vacío ("Aún no tienes academias. Pulsa + para crear una.") · chip Activa/Inactiva.
- **M** "No se pudo completar la operación", "No se pudo completar la operación. Revisa tu conexión e inténtalo de nuevo". Encabezado: "GESTIÓN" / "Mis academias" / "Administra tus academias, equipos y partidos."

### 1.7 `equipos/PantallaEquipos.kt`
- **A** FAB "+", "← Volver", botón "Directores técnicos de la academia". En cada tarjeta: Jugadores, Editar, Inactivar/Activar.
- **D** `FormularioEquipo` ("Nuevo equipo" / "Editar equipo"): Nombre del equipo *, `SelectorCategoria` (DropdownMenu "Elige la categoría *"), `SelectorEntrenador` (DropdownMenu, "(Sin asignar)") · Guardar / Cancelar. `DialogoEntrenadores` ("Directores técnicos"): lista, "Correo del director técnico", Agregar / Cerrar; vacío "Aún no hay directores técnicos en esta academia."
- **E** cargando · vacío ("Esta academia aún no tiene equipos. Pulsa + para crear uno.") · Activo/Inactivo · "Categoría: …" · "Director técnico: … / Sin asignar".
- **M** los mismos de error genérico que Academias.

### 1.8 `jugadores/PantallaJugadores.kt`
- **A** FAB "+", "← Volver". En cada tarjeta: Editar, Inactivar/Activar. Foto (`imagenRemota`).
- **D** `FormularioJugador` ("Nuevo jugador" / "Editar jugador"), en secciones: *Datos personales* (Nombres *, Apellidos *, Fecha de nacimiento, Documento de identidad, foto "Elegir foto (opcional)" / "Cambiar foto" / "Foto elegida ✓ (cambiar)"); *Datos deportivos* (Posición, Pie hábil, desplegables con "(Sin definir)"); *Datos sensibles (solo personal autorizado)* (Estatura (cm), Peso (kg), Teléfono del jugador, Contacto de emergencia, Teléfono de emergencia) · Guardar / Cancelar.
- **E** cargando · vacío ("Este equipo aún no tiene jugadores. Pulsa + para registrar uno.") · Activo/Inactivo · chip "Menor de edad".
- **F** **No hay búsqueda ni filtros hoy** (ver D-04).
- **M** "No se pudo leer la foto", "No se pudo subir la foto", "La estatura / El peso debe ser un número", "No tienes permiso para realizar esta operación", "Sin conexión. Revisa tu internet e inténtalo de nuevo", "No se pudo completar la operación (código)".

### 1.9 `vinculaciones/PantallaCuentasJugadores.kt`
- **A** "← Volver"; por cuenta: "Vincular y activar" / "Vincular".
- **E** cargando · vacío ("No hay cuentas de jugadores por vincular.") · chip Pendiente/Activa · "Ficha: … · categoría", "Documento: …" · nota según `seActivara` (si se activará o quedará pendiente hasta autorizar a un apoderado).
- **M** "Cuenta de {nombre} vinculada y activada" / "… vinculada" / "… vinculada. Seguirá pendiente hasta que autorices a un apoderado", más los errores genéricos.

### 1.10 `vinculaciones/PantallaMisJugadores.kt` (PAD)
- **A** FAB "+ Solicitar", "← Volver"; en la tarjeta activa: "Solicitar desvinculación".
- **F** Pestañas `FiltroVinculacion`: Activo, Pendiente, Inactivo, Todos (ya existen).
- **D** `DialogoSolicitud` ("Solicitar vinculación", "Documento del jugador") · Solicitar / Cancelar.
- **E** cargando · vacío ("No hay jugadores en esta vista. Usa + Solicitar para vincular uno.") · chip de estado · "Solicitud: fecha" · "Esperando la aprobación de la academia" · "Aprobado por X el fecha" · datos (Fecha de nacimiento, Posición, Teléfono; "Pendiente" si faltan) · "Desvinculación solicitada. Esperando a la academia." · "Vinculación inactiva".
- **M** errores genéricos.

### 1.11 `vinculaciones/PantallaVinculaciones.kt` (ADM)
- **A** "← Volver"; por solicitud: Autorizar, Rechazar, Desvincular.
- **E** cargando · vacío ("Aún no hay solicitudes de vinculación en tus academias.") · banner "N solicitud(es) esperando aprobación" · "Apoderado: …", "Solicitud: …", "Aprobado por …", "El apoderado solicitó la desvinculación".
- **M** errores genéricos.

### 1.12 `mensualidades/PantallaMensualidades.kt`
- **A** "← Inicio", "+ Registrar mensualidad" (solo ADM), tocar tarjeta → detalle, "Modificar estado" (ADM).
- **F** Búsqueda "Buscar jugador..."; chips de periodo (TODOS + los 2 primeros periodos); chips de estado Todos / Pagado / Pendiente / Vencido (**ya existe** el filtro que pide 7.4).
- **D** `ModalBottomSheet` `DetalleMensualidad` (Academia, Periodo, Última actualización, Estado actual). `FormularioMensualidad` ("Registrar mensualidad" / "Actualizar mensualidad"): Jugador * (DropdownMenu, "No hay jugadores disponibles"), Periodo * (DropdownMenu), Estado * (FilterChip) · Guardar ("Guardando...") / Cancelar.
- **E** contadores Pagadas / Pendientes / Vencidas (según el periodo elegido) · cargando · vacío ("No hay mensualidades para mostrar.") · nota de solo consulta para quien no es ADM ("Solo consulta · Solo el administrador autorizado puede modificar mensualidades.").
- **M** "Mensualidad guardada correctamente", "Selecciona un jugador", "No existe una sesión activa", "Tu cuenta no tiene perfil", errores genéricos.
- **Regla**: el ViewModel llama a `generarMensualidadesMesActual` (escritura) al iniciar. Hay que conservarlo aquí y **no** llamarlo desde el inicio.

### 1.13 `ejercicios/PantallaEjercicios.kt`
- **A** FAB "+", "← Volver"; por ejercicio: Editar, Inactivar/Activar.
- **F** Selector de academia (DropdownMenu "Seleccionar academia"), búsqueda "Buscar por nombre u objetivo", filtro Todos / Activos / Inactivos.
- **D** `FormularioEjercicio` ("Crear ejercicio" / "Editar ejercicio"): Nombre, Objetivo, Descripción, Duración en minutos · Guardar ("Guardando...") / Cancelar.
- **E** cargando · vacío ("No hay ejercicios para mostrar.") · numeración "1. Nombre" · Activo/Inactivo · "N minutos".
- **M** "Ejercicio creado correctamente", "Ejercicio actualizado correctamente", "Ejercicio inactivado", "Ejercicio activado", "Ingresa una duración válida", "Selecciona una academia", "No existe una sesión activa".

### 1.14 `entrenamientos/PantallaEntrenamientos.kt`
- **A** FAB "+" (solo DT), "← Volver"; por sesión (DT): "Editar sesión", "Cancelar sesión".
- **F** Selector de equipo (DT, "Seleccionar equipo"); filtro Todos / Publicados / Cancelados (ya existe; ver D-05).
- **D** `FormularioEntrenamiento` ("Planificar entrenamiento" / "Editar entrenamiento"): Fecha (`DatePickerDialog`), Hora (`TimePickerDialog`), Objetivo de la sesión, Duración total en minutos, "Ejercicios seleccionados" ("X min de Y min"), advertencia "Los ejercicios superan la duración total.", lista ordenada con ↑, ↓ y Quitar, "Agregar ejercicios" con "Agregar ejercicio" · Publicar / Guardar cambios ("Guardando...") / Cerrar.
- **E** título "Entrenamientos" (DT) / "Mis entrenamientos" · cargando · vacío ("No hay entrenamientos para mostrar.") · chip Publicado/Cancelado · "fecha · hora", "N min · M ejercicios" · `historialCambiosHorario` disponible en el modelo.
- **M** "Entrenamiento publicado", "Entrenamiento actualizado", "Entrenamiento cancelado", "Ingresa una duración válida", "Selecciona un equipo", "No se pudo guardar el entrenamiento", "No se pudo cancelar", "No se pudieron cargar los ejercicios".

### 1.15 `asistencias/PantallaAsistencias.kt`
- **Modo registro (DT/ADM, `puedeGestionar`)**: selector de entrenamiento ("Seleccionar entrenamiento"); "Registro rápido" con "Marcar todos presentes" y "Marcar todos ausentes"; por jugador: Presente / Ausente / Justificado; "Guardar asistencia"; si ya hay registro: "ASISTENCIA REGISTRADA", contadores Presentes / Ausentes / Justificados, "Corregir asistencia" → "Modo corrección: modifica únicamente los jugadores necesarios.", "Guardar corrección", "Cancelar corrección" ("Guardando...").
- **Modo historial (JUG/PAD)**: título "Historial de asistencia", selector de jugador ("Seleccionar jugador"), tarjetas del historial con etiqueta Presente / Ausente / Justificado / Sin registrar.
- **E** "Selecciona un entrenamiento." · "No hay jugadores en este equipo." · "No hay registros de asistencia." · cargando.
- **M** "Asistencia registrada correctamente", "Asistencia corregida correctamente", "La asistencia ya fue registrada. Usa Corregir asistencia.", "Registra la asistencia de todos los jugadores", "No se pudo guardar la asistencia", "No se pudo cargar el historial", "Tu cuenta no tiene perfil".

### 1.16 `partidos/PantallaPartidos.kt` (por academia)
- **A** FAB "+" (crear), "← Volver"; por partido: "Ver detalles →" / "Ver partido →".
- **E** Secciones: EN VIVO, Próximos partidos, Partidos anteriores, CANCELADOS (equivalen a un filtro; ver D-06) · vacío por sección ("No hay partidos en esta sección.") · marcador en vivo o "VS" · chip PROGRAMADO / EN CURSO / FINALIZADO / CANCELADO · "fecha · hora · ubicación" · cabecera "TEMPORADA 2026 · Partidos".
- **M** errores genéricos.

### 1.17 `partidos/PantallaCrearPartido.kt`
- **A** "← Partidos", "Crear partido" (con progreso).
- **F** EQUIPO * (DropdownMenu), RIVAL * (texto libre; "No es obligatorio que esté registrado en SportPro."), FECHA * (`DatePickerDialog`), HORA * (`TimePickerDialog`), UBICACIÓN *.
- **E** "El partido se creará con estado Programado".
- **M** errores del ViewModel de partidos.

### 1.18 `partidos/PantallaEditarPartido.kt`
- **A** "← Volver al partido", "Guardar cambios".
- **F** Los mismos 5 campos que Crear.
- **E** cargando · "No se pudo cargar el partido." · "Solo los partidos en estado Programado pueden editarse."

### 1.19 `partidos/PantallaDetallePartido.kt`
- **A** "← Partidos", "Gestionar convocatoria" / "Ver convocatoria", "Ver alineación", "✎ Editar partido". PROGRAMADO: "Iniciar partido". EN CURSO: "⚽ Abrir partido en vivo", "Finalizar partido". PROGRAMADO o EN CURSO: "Cancelar partido". FINALIZADO: "Ver cronología del partido", "✨ Resumen del partido".
- **D** `DialogoCambioEstadoSportPro`: "¿Iniciar antes de lo previsto?" (con fecha y hora programadas), "¿Iniciar partido?", "¿Finalizar partido?", "¿Cancelar partido?" · Confirmar / Volver.
- **E** cargando · error · `TarjetaMarcadorDetalle` (RESULTADO FINAL / MARCADOR ACTUAL) · información (Rival, Fecha, Hora, Ubicación, Estado) · textos de estado ("El partido está listo para iniciarse…", "…actualmente EN CURSO.", "Este partido ya fue finalizado.", "…fue cancelado y se conserva en el historial.").

### 1.20 `convocatoria/PantallaConvocatoria.kt` (DT/ADM)
- **A** "← Partidos", check por jugador, "Publicar convocatoria" / "Actualizar convocatoria · N jugadores" (con progreso).
- **E** cargando · histórica ("La convocatoria es histórica porque el partido ya comenzó.") · sin jugadores ("Este equipo todavía no tiene jugadores registrados.") · chip INACTIVO · chip de disponibilidad PENDIENTE / DISPONIBLE / NO DISP. · posición abreviada y edad.
- **F** **No hay filtro hoy** (7.4 agrega Todos / Disponibles / Pendientes).
- **M** "Convocatoria publicada correctamente", "Selecciona al menos un jugador", "{jugador} está inactivo y no puede ser convocado", "La convocatoria ya no puede modificarse porque el partido comenzó".

### 1.21 `convocatoria/PantallaMisConvocatorias.kt` (JUG/PAD)
- **A** "← Inicio"; por convocatoria: "✓ Disponible", "No disponible" (con progreso).
- **E** Encabezado según el rol ("CONVOCATORIAS DE TUS JUGADORES" / "MIS PARTIDOS"; texto de ayuda por rol) · cargando · vacío ("No tienes convocatorias") · "¿Podrás asistir a este partido?" · "Puedes cambiar tu respuesta mientras el partido no haya comenzado." · histórico ("Esta convocatoria ya forma parte del historial del partido.") · chip PENDIENTE / DISPONIBLE / NO DISP.
- **M** "Disponibilidad actualizada: Disponible/No disponible", "Este usuario no puede responder convocatorias", "No hay una sesión activa", "El usuario no tiene un perfil válido".

### 1.22 `alineacion/PantallaAlineacion.kt`
- **A** "← Partidos", `SelectorFormato` (11, 9, 7 y 5 vs.), formación (incluye "Personalizado" y "4-4-2"), arrastre de fichas en la cancha (tag `posicion-*`), "+" en suplente (pasa a titular), "−" en titular, DropdownMenu de posición ("… · ocupado"), "Confirmar alineación" / "Confirmar cambios".
- **D** "¿Cambiar formato?" ("Se reemplazará la distribución personalizada de la cancha.") · Redistribuir / Cancelar. `DialogoJugadorNoDisponible` ("JUGADOR NO DISPONIBLE") · "Quitar de titulares" / "Ignorar".
- **E** cargando · "x/y titulares" · histórica ("La alineación es histórica porque el partido ya comenzó.") · SUPLENTES / TITULARES con sus textos de ayuda · "No hay suplentes disponibles." · "Todavía no has agregado titulares." · "No hay jugadores con disponibilidad confirmada para este partido." · "Completa los N titulares y todas las posiciones para confirmar."
- **M** "Alineación confirmada correctamente", "Primero quita titulares. El formato X permite máximo N.", "{jugador} ya no está disponible para el partido", "Ya completaste los N titulares…", "No hay una posición libre en el campo", "Esa posición ya está ocupada", "Debes completar N titulares para confirmar…", "Hay un titular que ya no está disponible", "Debes completar todas las posiciones del campo antes de confirmar", "La alineación ya no puede modificarse porque el partido comenzó".

### 1.23 `eventospartido/PantallaPartidoEnVivo.kt` (operador; incluye `ModalGestionEvento`, `ModalAuditoriaGeneral` y `ComponentesSincronizacion`)
- **A** "← Partidos"; panel "REGISTRO RÁPIDO" (`PanelEventosRapidos`: **ya lee el catálogo activo**, en filas de 4, con INICIO deshabilitado) → `abrirEvento(tipo)`; "Auditoría"; tocar evento → detalle o conflicto; "↻ Reintentar sincronización"; en los rechazados: Reintentar / Descartar.
- **D** `ModalRegistroEvento` ("NUEVO EVENTO", ×): Minuto ("Ej. 37", **vacío hoy**), equipo ("EQUIPO QUE ANOTA" / "EQUIPO DEL EVENTO"), JUGADOR (selector) o "Jugador rival", SUSTITUCIÓN (↓ sale / ↑ entra, propio o rival), TIPO DE TARJETA (Amarilla / Roja), Observación · "Registrar {tipo}" / "Registrar y finalizar" (FIN).
  `ModalGestionEvento` ("DETALLE DEL EVENTO"): Corregir (formulario con "Motivo de corrección *", "Guardar corrección"), Anular ("Motivo de anulación *", "Confirmar anulación"), auditoría por versión, Cerrar.
  `ModalAuditoriaGeneral` ("Historial de eventos", ACTIVO / ANULADO).
  `ModalConflictoSincronizacion` ("REQUIERE REVISIÓN"): "Conservar evento" / "Anular evento".
- **E** cargando · sin contexto ("No se pudo abrir el partido sin conexión: este dispositivo nunca lo había cargado." / "No se pudo cargar el partido en vivo.") · `AvisoRegistroOfflineNoDisponible` · `TarjetaMarcador` (PARTIDO EN CURSO, aviso de marcador provisional) · `AlineacionPartidoEnVivo` (si está EN CURSO) · `ResumenEventosEnVivo` · registro cerrado ("El registro de eventos está cerrado porque el partido ya no está en curso.") · `ContadoresSincronizacion` (pendientes / con conflicto) · `TarjetaEventoSincronizado` con chip Sincronizado / Pendiente de sync / Requiere revisión · `BannerSincronizacion` abajo ("✓ Sincronizado", "Sincronizando · …", "Sin conexión · …", "Conectando con el servidor…", "Última confirmación …").
- **M** "{Evento} registrado correctamente", "Partido finalizado correctamente", "Evento corregido correctamente", "Evento anulado correctamente", más todas las validaciones (minuto obligatorio, no negativo, equipo, jugador, jugador rival, tipo de tarjeta, observación obligatoria, sale ≠ entra, motivo obligatorio, "El partido ya no está en curso", "El partido ya se encuentra en curso") y los de `SincronizacionViewModel` ("Conexión restablecida: enviando eventos pendientes", "Modo sin conexión: …", "Revisión guardada en el dispositivo…", etc.).

### 1.24 `seguimiento/PantallaMisPartidos.kt` ("Partidos en vivo" del inicio)
- **A** "← Inicio"; por partido: "Seguir en vivo →" / "Ver partido →".
- **E** subtítulo por rol ("Partidos de tus jugadores" / "de tu equipo" / "de tus academias") · banner "Sin conexión · la lista puede estar desactualizada" · cargando · vacío ("Todavía no hay partidos para mostrar.") · secciones En vivo / Próximos partidos / Partidos anteriores / Cancelados · chip ● EN VIVO / PROGRAMADO / FINALIZADO / CANCELADO · marcador en tiempo real ("Cargando marcador…") · "Juega: …".
- **M** "No se pudieron cargar tus partidos", "No se pudieron escuchar los partidos".

### 1.25 `seguimiento/PantallaSeguimientoPartido.kt` (solo lectura)
- **A** "← Partido", "✨ Ver resumen del partido →" (si está finalizado), "Reintentar" (en el error).
- **E** `CabeceraVivo` (marcador, MARCADOR PROVISIONAL / MARCADOR FINAL / "MIN n'"), chip EN VIVO · n' / FINALIZADO / CANCELADO / POR INICIAR / CARGANDO con punto pulsante · `FranjaActualizacion` ("Sin conexión · último estado conocido…", "Partido finalizado · sincronizado", "Actualizado en vivo") · avisos de por iniciar o cancelado · "CRONOLOGÍA DE EVENTOS" con conteo de anulados · insignias NUEVO / ✎ CORREGIDO / ANULADO · motivo · "Todavía no hay eventos registrados." · pie "SOLO LECTURA · ACTUALIZACIÓN AUTOMÁTICA".
- **M** "No se pudo abrir el partido", "No se pudo abrir el partido. Revisa tu conexión."

### 1.26 `estadisticas/PantallaEstadisticas.kt`
- **F** Pestañas Equipo / Jugador / Periodo; selector EQUIPO; selector PERIODO (Todo el historial, temporada, meses); selector de jugador.
- **E** cargando · "Sin equipos" · "Sin jugadores visibles" · equipo: G·E·P, eventos anulados excluidos, métricas (Partidos jugados, Sesiones de entrenamiento, Goles a favor, en contra, tarjetas, cambios), BALANCE DE GOLES · jugador: edad / MENOR, Participaciones, Goles, ASISTENCIA A ENTRENAMIENTOS (% o "Sin datos", Justificado), T. amarillas, T. rojas, Faltas, aviso "Datos protegidos" según el acceso · periodo: Sesiones, Partidos, % asistencia, Eventos registrados, SESIONES DEL PERIODO · pie "SOLO LECTURA · DATOS BASADOS EN EVENTOS VÁLIDOS".
- **M** "No se pudieron calcular las estadísticas. Revisa tu conexión."

### 1.27 `comunidad/PantallaComunidad.kt` (incluye SeccionPruebas, hojas y formularios)
- **A** "← Inicio", "🚫 Bloqueados" (hoja), "🛡 Moderar" (solo ADM → panel), botón "+ Publicar" / "+ Aviso" (en Pruebas), reaccionar (`BotonReaccion` con DropdownMenu ❤️ 👏 💪, conteo y "Toca tu reacción otra vez para quitarla"), "💬 N comentarios" (hoja), "•••" propio (DropdownMenu Editar / Eliminar), "•••" ajeno (abre `HojaAccionesContenido`), "Ver pruebas" (sin academia).
- **F** Pestañas Para ti / Academias / Pruebas. Pruebas: Categoría, Posición, Ubicación, Fecha (Cualquier fecha / Próximos 7 días / Próximos 30 días), interruptor "Mostrar cerradas y vencidas", "Limpiar".
- **D** "¿Eliminar publicación?" · Eliminar / Cancelar. `HojaAccionesContenido`: "🚩 Reportar {tipo}" (MOTIVO *, detalle, "Enviar reporte"), "⊘ Bloquear a X" ("Bloquear"), "🛡 Moderar directamente" (Ocultar contenido / Cerrar aviso, MOTIVO *, "Aplicar"), Volver. `HojaComentarios` (lista, "Escribe un comentario…", Publicar, 🗑 / •••). `HojaUsuariosBloqueados` (lista, "Desde fecha", desbloquear; "No has bloqueado a nadie."). `FormularioPublicacion` (PUBLICANDO COMO, texto 0/500, MULTIMEDIA con subida y progreso, Reintentar / Solo texto, VISIBILIDAD Equipo / Academia / Comunidad, Cancelar / Publicar o Guardar). `FormularioAvisoPrueba` (ACADEMIA *, CATEGORÍA *, POSICIÓN * Abierta (todas) / Específica, FECHA Y HORA *, UBICACIÓN *, DESCRIPCIÓN * 0/600, "Publicar aviso", "← Volver").
- **E** cargando · vacío ("Todavía no hay publicaciones…" / "No hay pruebas con esos filtros." / "Aún no hay pruebas abiertas publicadas.") · sin academia ("Aún no perteneces a una academia…") · chip de visibilidad (🌐 / 🏛 / 👥) · " · editada" · tarjeta de prueba: "Publicación oficial verificada", estado ● PRUEBA ABIERTA / CERRADA / VENCIDA (hoy en naranja), datos 🏆 Categoría, ⚽ Posición, 📅 Fecha, 📍 Ubicación, descripción completa, menú "•••" con Cerrar / Reabrir convocatoria.
- **M** Snackbars "✓ …": "Publicación compartida con …", "Publicación actualizada", "Publicación eliminada", "Aviso publicado: {titulo}", "Aviso cerrado", "Aviso reabierto", "Reporte enviado. Un moderador lo revisará.", "Bloqueaste a X", "Desbloqueaste a X".

### 1.28 `moderacion/PantallaPanelModeracion.kt` (ADM)
- **A** "← Comunidad"; por reporte: Descartar, Ocultar, Cerrar (aviso).
- **F** Chips Pendientes / Resueltos / Todos.
- **D** AlertDialog con motivo ("Escribe el motivo. Quedará registrado con tu nombre y la fecha.") · Confirmar / Cancelar.
- **E** "N pendientes" · cargando · vacío ("No hay reportes pendientes 🎉" / "No hay reportes en esta sección.") · chip PENDIENTE / RESUELTO · "🚩 N reportes" / "Moderación directa" · motivos agrupados "×n" · autor · fechas del primer y del último reporte · decisión y moderador.
- **M** Snackbar "✓ …", "No se pudieron cargar los reportes".

### 1.29 `anuncios/PantallaAnuncios.kt` (incluye `FormularioAnuncio`)
- **A** "← Inicio", "+ Nuevo"; por anuncio: Editar, Archivar, Reactivar; "Reintentar" en el error.
- **F** Chips Todos / Activos / Archivados.
- **D** `FormularioAnuncio` (hoja "NUEVO/EDITAR ANUNCIO"): ACADEMIA, AUDIENCIA (Toda la academia / Por rol / Público / Un equipo; no se puede cambiar al editar), EQUIPO, ROL DESTINO, TÍTULO 0/80, CONTENIDO 0/1000 · Cancelar / Publicar o Guardar.
- **E** cargando · vacío ("No hay anuncios por ahora.") · chip de audiencia (🏟️ / 👥 / 🎯 / 🌐) · ARCHIVADO / ACTUALIZADO · autor, rol, fecha.
- **M** "Anuncio publicado", "Anuncio actualizado", "Anuncio archivado", "Anuncio reactivado".

### 1.30 `notificaciones/PantallaNotificaciones.kt`
- **A** "← Inicio", "Marcar todas leídas", tocar una notificación (marca leída y navega con `DestinoNotificacion`), "Activar notificaciones" (permiso).
- **E** contador de no leídas · `BannerPermiso` (pedir / rechazado) · cargando · vacío ("No tienes notificaciones todavía.") · fila con `TipoNotificacion.emoji` y estilo de no leída.
- **M** "No se pudieron cargar las notificaciones".

### 1.31 `planes/PantallaPlanes.kt`
- **A** "Inicio" (volver), "Elegir {plan}".
- **D** "Plan X" ("¡Gracias por tu interés! Esta es una versión de demostración…") · Entendido.
- **E** OFERTAS DE LANZAMIENTO (3) · PLANES MENSUALES (Básico, Pro con RECOMENDADO, Élite; precios "S/ …") · "Versión demo: los precios son referenciales y no se realiza ningún cobro."

### 1.32 `resumen/PantallaResumenPartido.kt`
- **A** "Partido" (volver), "Generar borrador sin IA (solo hechos)", "Generar con IA" (`BloqueGenerar`, "Generando…"), "Guardar cambios del borrador", "Aprobar resumen", "Rechazar y pedir nueva generación", "🔊 Escuchar narración" / "⏹ Detener audio", Comentar / Eliminar comentario (DT), plegables "Notas de evaluación por narración (n)" e "Historial de versiones (n)", selector de utilidad 1 a 5 con sus campos de evaluación.
- **D** "Rechazar borrador" (motivo) · Rechazar / Cancelar. "Eliminar comentario" / "Eliminar comentario de otro DT" (motivo).
- **E** cargando · no disponible ("Resumen aún no disponible", que explica el estado) · "Resumen desactualizado" · "Borrador vN rechazado" con el motivo · "Los eventos cambiaron" · `TarjetaBorrador` (BORRADOR · REQUIERE REVISIÓN DT, "IA · vN" / "SIN IA · vN", eventos usados, modelo) · `TarjetaAprobado` ("✓ Resumen aprobado", aprobado por…) · CRONOLOGÍA FUENTE · TEXTO FINAL (EDITABLE) · avisos "⚠ Hay eventos pendientes de sincronizar…" y "⚠ Eventos insuficientes…" · "El resumen se habilita cuando el partido está FINALIZADO." · voz no instalada.
- **M** "Borrador vN generado. Revísalo antes de aprobar.", "Borrador vN (sin IA) generado…", "Borrador rechazado. Puedes generar uno nuevo.", "Cambios guardados", "Comentario eliminado", "Los eventos del partido cambiaron después de la aprobación".

### 1.33 `catalogoeventos/PantallaCatalogoEventos.kt`
- **A** "← Inicio", "+  Nuevo evento", Switch de activo por evento, menú "⋮" (Editar / Desactivar o Activar).
- **D** `DialogoFormularioEvento` ("NUEVO/EDITAR EVENTO" · "Configurar evento"): Nombre del evento ("Los eventos base conservan su nombre."), Evento activo, METADATOS (Requiere equipo, Requiere jugador, Requiere observación, Afecta marcador, Afecta estado del partido) · Guardar evento / Guardar cambios / Cancelar.
- **E** "EVENTOS CONFIGURADOS · N activos" · cargando · vacío ("Todavía no existen eventos configurados.") · chips "marcador" / "estado" · campos requeridos o "Sin campos adicionales" · INACTIVO · `IndicadorSincronizacion` (Sincronizado / Sincronizando... / Datos locales).
- **M** "Evento creado correctamente", "Evento actualizado correctamente", "El nombre del evento es obligatorio", "Ya existe un evento activo con el nombre …".
- **Regla**: al abrir, `inicializarCatalogoBase` agrega los tipos base que falten (importante para INICIO_2T, ver 3.2).

---

## 2. Kit de componentes e imagen de referencia por pantalla

Kit (`ui/comun/LineaDeCal.kt`): CabeceraCancha (CC), MarcadorLed (ML), ContadorLed (CL), TarjetaTicket (TT), FichaTactica (FT), PizarraFichas (PF), CromoJugador (CJ), ChipEstado (CE), BotonPrincipal (BP), CampoSportPro (CS), BarraSuperior (BS), FondoCancha (FC).
Todas usan además los tokens del tema, íconos Material Rounded y BS donde hoy hay "← Volver".

| # | Pantalla | Componentes | Imagen |
|---|---|---|---|
| 1.1 | Bienvenida + Introducción | FC, BP (lima), CE (etiqueta de página) | 09 (login) |
| 1.2 | Login | FC, CS (+ visibility), BP lima | 09 |
| 1.3 | Recuperar | FC, CS, BP | 09 (estilo) |
| 1.4 | Registro | FC, CS (+ visibility), tarjetas de rol con FT, BP | 09 (estilo) |
| 1.5 | Inicio ×4 + CuentaPendiente | CC, ML, CL, PF+FT (con contador), TT, CJ mini (PAD), CE, BP | 03 · 01/02 (estilo) · sin tablet: centrado con `widthIn(max = 600.dp)` |
| 1.6 | Academias | CC + CL, TT o tarjeta con escudo, CE, BS | 08 (izq.) |
| 1.7 | Equipos | CC + CL, tarjeta con escudo de iniciales, CE, chips Activos/Inactivos | 08 (izq.) |
| 1.8 | Jugadores | BS, CJ mini en cuadrícula, chips de filtro, CS en el formulario | 08 (centro) |
| 1.9 | Cuentas de jugadores | BS, TT, CE, BP | 08 (estilo) |
| 1.10 | Mis jugadores (PAD) | BS, CJ grande, TT (solicitudes), CE, BP navy "Solicitar vinculación" | 08 (der.) |
| 1.11 | Vinculaciones | BS, TT, CE, BP | 08 (estilo) |
| 1.12 | Mensualidades | CC + CL (Pagado/Pendiente/Vencido), TT (periodo en el talón), CE, chips | 06 (der.) |
| 1.13 | Ejercicios | BS, búsqueda con CS, FT pequeña por objetivo, CE, número Doto | 05 (der.) |
| 1.14 | Entrenamientos + Planificar | CC + CL, TT (verde / tachada), chips, CS, caja LED "EJERCICIOS x/y" con barra, BP lima | 05 (izq. y centro) |
| 1.15 | Asistencias (registro e historial) | BS, TT (sesión), CL ×3, FT P/A/J, BP navy; historial: CJ mini + LED %, filas con borde de color | 06 (izq. y centro) |
| 1.16 | Partidos | CC + CL, TT amarilla, ticket oscuro con mini ML, CE, chips | 07 (izq.) |
| 1.17 | Crear partido | BS, CS, BP | 07 (estilo) |
| 1.18 | Editar partido | BS, CS, BP | 07 (estilo) |
| 1.19 | Detalle de partido | BS, ML, TT, CE, BP (acciones por estado) | 07 / 01 (estilo) |
| 1.20 | Convocatoria | BS, TT, caja LED "RESPONDIERON x/y", chips, CE, BP navy | 07 (centro) |
| 1.21 | Mis convocatorias | BS, TT amarilla, CE, BP | 07 (estilo) |
| 1.22 | Alineación | BS, chips de formato, cancha con líneas de cal, FT numeradas, banca, aviso naranja, BP lima | 07 (der.) |
| 1.23 | Partido en vivo | BS, ML grande (reloj, PROVISIONAL), aviso naranja Sin conexión/Reintentar, minuto Doto con − / +, mini cancha con FT, cronología discontinua, CE de sync | 01 / 02 (centro) |
| 1.24 | Mis partidos (en vivo) | BS, TT + mini ML, CE | 07 (izq.) / 03 |
| 1.25 | Seguimiento | BS, ML, cronología discontinua, CE | 01 / 02 (centro) |
| 1.26 | Estadísticas | BS, CJ grande, CL, goles por fecha, TT último partido | 01 / 02 (der.) |
| 1.27 | Comunidad | BS (Bloqueados `block`, Moderar `shield` + contador), chips, tarjeta común (avatar, ⋮), CE, FAB | 10 (con 6.10) · 09 (versión anterior) |
| 1.28 | Panel de moderación | BS, TT por reporte, CE Pendiente/Resuelto, chips | 10 (estilo) |
| 1.29 | Anuncios | BS, tarjeta con megáfono, CE de audiencia, chips | 01 (tarjeta de anuncio) |
| 1.30 | Notificaciones | BS, filas con ícono por tipo, no leídas resaltadas | sin imagen (estilo general) |
| 1.31 | Planes | BS, TT ("entrada") por plan, BP | 03 (tarjeta "Planes y ofertas") |
| 1.32 | Resumen con IA | BS, ML final, CE (Borrador/Aprobado/Rechazado, origen, versión), tarjeta blanca, BP | 09 (der.) |
| 1.33 | Catálogo de eventos | BS, tarjetas, CE, Switch del tema | sin imagen (estilo general) |

---

## 3. Funcionalidades nuevas (sección 7)

Riesgo: 🟢 bajo · 🟡 medio · 🔴 alto. ⚠️ = cambia el flujo de un compañero (commit aparte).

### 3.1 Inicio (7.1)

| Funcionalidad | Archivos | De dónde salen los datos | Riesgo |
|---|---|---|---|
| Nombre real en el saludo + avatar con iniciales | **nuevo** `ui/inicio/InicioViewModel.kt`; `PantallasInicio.kt` (parámetros opcionales con valor por defecto); `NavegacionApp.kt` (solo pasar el ViewModel) | `RepositorioAutenticacion.uidActual()` + `obtenerPerfil(uid)` | 🟢 |
| Tarjeta EN VIVO | `InicioViewModel`, `PantallasInicio.kt` (ML) | `RepositorioSeguimientoPartido.equiposVisibles()` + `escucharPartidos()` + `RepositorioSincronizacion().escucharEventos()` y `calcularMarcador()` (la misma lógica de `MisPartidosViewModel`). Próximo: el partido PROGRAMADO más cercano de esa misma lista | 🟡 Listeners: se cancelan en `onCleared`. Si varios están EN_CURSO, se muestra el primero |
| Contador en la campana | `InicioViewModel` | `RepositorioNotificaciones.listar(uid)` → `count { !leida }` (o reutilizar `NotificacionesViewModel.noLeidas`) | 🟢 |
| ADM "Requiere tu atención" + números en las fichas | `InicioViewModel`, `PantallasInicio.kt` | Cuentas: `RepositorioCuentasJugador.listar(uid)` con `pendiente`. Vinculaciones: `RepositorioVinculaciones.listarParaAdministrador(uid)` con PENDIENTE. Vencidas: `RepositorioMensualidades.listarParaUsuario(...)` con VENCIDO. **No llamar a `generarMensualidadesMesActual`** | 🟡 3 lecturas más al abrir el inicio. Ver D-10 sobre qué cuenta como "por aprobar" |
| DT "Tu jornada" | `InicioViewModel` | `RepositorioEntrenamientos.listarParaUsuario()` → próximo PUBLICADO (`fechaHoraEpochMillis`, objetivo, hora, `duracionTotalMinutos`, `ejercicios.size`; sin lugar). Partido: próximo PROGRAMADO de `equiposVisibles`; "x/y" con `RepositorioConvocatoria.listarConvocados()` | 🟡 |
| JUG "Para ti" | `InicioViewModel` | Convocatoria pendiente: `RepositorioConvocatoria.listarParaJugador()`. Asistencia %: `RepositorioAsistencias.listarHistorialJugador()` → `ConteoAsistencia.porcentaje`. Goles de la temporada: `RepositorioEstadisticas.cargarEquipo()` + `CalculadoraEstadisticas.calcularJugador()` con `PeriodoEstadisticas.temporada(año)`. Mensualidad: `listarParaUsuario()` del periodo actual | 🟡 `cargarEquipo` es una lectura pesada (eventos de todos los partidos). Se carga después del resto y se oculta si falla |
| PAD "Mis jugadores" | `InicioViewModel`, CJ mini | `RepositorioVinculaciones.listarMias(uid)` (ACTIVA) + `RepositorioMensualidades.listarParaUsuario()` | 🟢 |
| ~~Tablet: agenda de 7 días + pruebas abiertas~~ | **Eliminado** (sin vista de tablet, ver "Reglas de trabajo") | — | — |
| Cerrar sesión desde el avatar + botón discreto | `PantallasInicio.kt` | Callback `onCerrarSesion` que ya existe | 🟢 |
| Ocultar sección si falla | `InicioViewModel` | Cada bloque con su propio `Result`; un fallo se queda en `null` y la sección no se muestra | 🟢 |

### 3.2 Partido en vivo (7.2)

| Funcionalidad | Archivos | Datos | Riesgo |
|---|---|---|---|
| ⚠️ Minuto sugerido + botones − / + | **nuevo** `datos/partido/MinutoSugerido.kt` (función pura `minutoSugerido(inicioCaptura: Long?, ahora: Long): Int?`) + **test** `app/src/test/.../partido/MinutoSugeridoTest.kt`; `PartidoEnVivoViewModel.abrirEvento()` (precargar `formulario.minuto`); `PantallaPartidoEnVivo.kt` (`ModalRegistroEvento`: − / + llaman a `cambiarMinuto`) | `fechaCaptura` del evento INICIO válido en la lista local (`eventosActuales`, que sale de la caché de Firestore o de `estado.eventos`). Fórmula `((ahora − inicio) / 60000) + 1` | 🟡 El ViewModel no ve `sync.eventos`. Hay que pasarle el INICIO desde la pantalla o leerlo de `estado.eventos`. Sin INICIO el campo queda vacío, como hoy |
| ⚠️ Reloj "1T 12:34" / "2T 36:18" + tipo INICIO_2T | `RepositorioCatalogoEventos.tiposBase()` (nuevo tipo base `INICIO_2T` "Inicio 2T", sin equipo ni jugador); `FuenteResumen.CODIGOS_CONTROL` += INICIO_2T; `PantallaPartidoEnVivo.kt`, `PantallaSeguimientoPartido.kt` (reloj en ML con `LaunchedEffect` cada segundo); **nuevo** `datos/partido/RelojPartido.kt` (función pura) + **test** | INICIO y INICIO_2T válidos (no anulados) | 🔴 Ver D-02: `functions/resumen.js` tiene su propio `CODIGOS_CONTROL` y no se puede tocar. Las academias existentes solo reciben el tipo cuando alguien abre el catálogo (`inicializarCatalogoBase`). `ResumenPartido.BloqueGenerar` cuenta eventos "de juego" excluyendo solo INICIO y FIN. Plan B: reloj sin 1T/2T |
| ⚠️ Panel rápido sobre la mini cancha + "Más tipos" | `PantallaPartidoEnVivo.kt` (`PanelEventosRapidos` → mini cancha con FT de GOL, TARJETA, FALTA, CAMBIO, TIRO_ESQUINA si están activos; ficha "Más tipos" con DropdownMenu u hoja con el resto, incluidos FIN y los personalizados) | `contexto.tiposEvento` (catálogo activo, ya cargado) | 🟢 Hoy el panel ya lee el catálogo y llama a `abrirEvento(tipo)`. Solo cambia el diseño y se agrega el agrupador. "Córner" = código `TIRO_ESQUINA` (nombre "Tiro de esquina") |
| Mensaje honesto offline | `RepositorioEventosPartido.registrarEvento()` (devolver si quedó sincronizado: el valor de `confirmarOEncolar`); `PartidoEnVivoViewModel.registrarEvento()` (mensaje "{Evento} guardado en este dispositivo · se enviará al reconectar") | Booleano de `confirmarOEncolar` | 🟡 Cambia el tipo de retorno de una función pública de un repositorio (regla 2: no se renombra, pero cambia la firma). Propuesta sin romper a nadie: `Result<EventoPartido>` se mantiene y se agrega `registrarEventoConEstado(): Result<Pair<EventoPartido, Boolean>>`, o un campo transitorio. Ver D-03 |

### 3.3 Estadísticas del jugador (7.3)

| Funcionalidad | Archivos | Datos | Riesgo |
|---|---|---|---|
| Minutos jugados | **nuevo** cálculo puro en `CalculadoraEstadisticas.kt` (`minutosJugados(...)`) + **test** en `CalculadoraEstadisticasTest` o archivo nuevo; `DatosFuenteEquipo` += `titularesPorPartido: Map<String, Set<String>> = emptyMap()` (campo nuevo con valor por defecto); `RepositorioEstadisticas.cargarEquipo()` (llenarlo filtrando `JugadorAlineacion.tipo == TITULAR`); `EstadisticasJugador` += `minutos` | Alineación (titulares), eventos CAMBIO válidos (`idJugadorSale` / `idJugadorEntra`, minuto), minuto del FIN. Solo partidos FINALIZADOS | 🟡 Hoy `alineadosPorPartido` mezcla titulares y suplentes, así que no sirve para saber quién fue titular. Sin FIN: ver D-12 |
| Goles por fecha | `CalculadoraEstadisticas` (lista por partido), `PantallaEstadisticas.kt` | `eventosPorPartido` + `esGol` | 🟢 |
| Último partido | Ídem | Último partido FINALIZADO: marcador (`calcularMarcador`), goles, minutos y si fue titular | 🟢 |
| Foto | `PantallaEstadisticas.kt` (CJ) | `Jugador.fotoUrl` vía `JugadorEstadistico` | 🟢 |
| Compartir el cromo (opcional) | `AndroidManifest.xml` (`<provider>` FileProvider), **nuevo** `res/xml/file_paths.xml`, captura del cromo (`GraphicsLayer` / `rememberGraphicsLayer`), `Intent.ACTION_SEND` | Cromo ya visible (respeta `nivel` de `ControlAcceso`) | 🟡 Toca el manifest. Se deja para el final |

### 3.4 Listas y cabeceras (7.4)

| Pantalla | Contadores LED (datos ya cargados) | Filtro nuevo | Selector de equipo | Riesgo |
|---|---|---|---|---|
| Entrenamientos | esta semana, minutos, cancelados (`estado.entrenamientos`) | Próximos / Historial (ver D-05: ya existe Todos/Publicados/Cancelados) | **Sí** (DT ya elige equipo) → "Equipo ▾" | 🟢 |
| Mensualidades | Pagado, Pendiente, Vencido (ya se calculan) | **Ya existe** (Todos/Pagado/Pendiente/Vencido) → solo restilizar | No hay selector → etiqueta fija | 🟢 |
| Partidos | jugados, ganados (`marcadoresPorPartido`), próximos | Próximos / Finalizados (ver D-06: hoy hay secciones) | Pantalla por academia → nombre de la academia fijo | 🟢 |
| Equipos | equipos, DT (`entrenadores.size`) | Activos / Inactivos | Academia fija | 🟡 "Jugadores" y "18 JUG." por equipo **no están cargados** (ver D-07) |
| Jugadores | total, activos | Todos / Activos / Pendiente (ver D-04) | Equipo fijo | 🟡 |
| Convocatoria | caja "RESPONDIERON x/y" (`convocados`) | Todos / Disponibles / Pendientes | Partido fijo | 🟢 |
| Historial de asistencia | % y "x de y sesiones" (`historial`) | Todas / Ausencias / Justificadas | Ya existe el selector de jugador | 🟢 |
| Academias | academias, activas | — | — | 🟢 |

Archivos: solo las `Pantalla*.kt` indicadas (estado local `remember` para el filtro y un `filter` sobre la lista del ViewModel). No se tocan los ViewModels salvo en Equipos (D-07).

### 3.5 Login (7.5)

| Funcionalidad | Archivos | Riesgo |
|---|---|---|
| Mostrar u ocultar la contraseña (`visibility` / `visibility_off`) | `PantallaLogin.kt`, `PantallaRegistro.kt` (o dentro de `CampoSportPro` con un parámetro `esContrasena`) | 🟢 |

### 3.6 Base técnica (etapas 1 y 2, no son de la sección 7, pero hacen falta)

| Tarea | Archivos | Riesgo |
|---|---|---|
| Íconos | `gradle/libs.versions.toml` + `app/build.gradle.kts` (`material-icons-extended:1.7.8`) | 🟡 Biblioteca grande. Sube el tamaño del APK debug y el tiempo de compilación |
| Tema | `ui/theme/Color.kt`, `Type.kt` (4 `FontFamily` con los .ttf), `Theme.kt` (`dynamicColor = false`) | 🟢 |
| Redirigir paletas | `EstiloPartido.kt`, `EstiloSincronizacion.kt`, `ComponentesSportPro.kt` y los 14 `private val` de pantallas | 🔴 Ver D-08: los `val X = Color(…)` a nivel de archivo no pueden cambiar con el modo oscuro solo cambiando la línea |
| Kit | **nuevo** `ui/comun/LineaDeCal.kt` con `@Preview` claro y oscuro | 🟢 |

---

## 4. Dudas y conflictos con las reglas de oro

Cada punto lleva una **propuesta**. Si no hay respuesta, se aplica la propuesta y el caso se anota en `docs/diseno/PENDIENTES.md`, como dice la regla 8.

**D-01 · Emojis vs. tests instrumentados (reglas 7 y 3.5).** `AlineacionReactivaTest` busca el texto `"⚽×2"` (insignia de `IndicadoresJugadorEnCancha`). `FormacionPersonalizadaTest` busca "Personalizado", "4-4-2", "Cancelar" y "Redistribuir", y el tag `posicion-A`. Si se cambia ⚽ por un ícono, ese test falla.
→ Propuesta: en `IndicadoresJugadorEnCancha` mantener el texto "⚽×N" (o un `Text` con esa semántica) y no tocar esos textos ni tags. Se anota como excepción.

**D-02 · INICIO_2T y la Cloud Function (reglas 3 y 7.2).** `functions/resumen.js` define `CODIGOS_CONTROL = {INICIO, FIN}` y no se puede tocar. Si se agrega INICIO_2T solo en Android, el resumen generado en el servidor contaría "Inicio 2T" como una jugada. También `PantallaResumenPartido.BloqueGenerar` excluye solo INICIO y FIN al contar eventos "de juego".
→ Propuesta: implementar primero el reloj **sin** 1T/2T (el plan B que permite la espec). Agregar INICIO_2T solo si se autoriza tocar `functions/resumen.js` (una línea) o si se confirma que el resumen con IA solo se genera en el cliente. Además, las academias existentes no tienen el tipo hasta que alguien abra el Catálogo.

**D-03 · Firma de `registrarEvento` (regla 2 y 7.2).** Para el mensaje honesto, `registrarEvento` "debe devolver si quedó sincronizado". Hoy devuelve `Result<EventoPartido>`.
→ Propuesta: no cambiar la firma pública. Agregar un parámetro de callback opcional (`alQuedarPendiente: (() -> Unit)? = null`) o devolver el estado mediante un campo transitorio no persistido. Se hace en un commit ⚠️ aparte.

**D-04 · Jugadores: "búsqueda" y filtro "Pendiente" (6.4 y 7.4).** La espec dice "se mantienen la búsqueda…", pero `PantallaJugadores` **no tiene búsqueda**. Además, `Jugador` solo tiene `activo` y no hay un estado "Pendiente".
→ Propuesta: agregar la búsqueda por nombre en el cliente (es un filtro local, se puede considerar parte de 7.4) **o** omitirla. Para "Pendiente", usar "Inactivos" (que es lo que existe) o "Sin cuenta" (`idUsuario` vacío). Hay que decidirlo.

**D-05 · Entrenamientos: filtro equivalente (7.4).** Ya existe Todos / Publicados / Cancelados. La espec pide Próximos / Historial y la regla dice "si existe uno equivalente, se conserva". No es equivalente (filtra por estado, no por fecha).
→ Propuesta: conservar el filtro actual y agregar Próximos / Historial como un segundo control (pestañas arriba como en la img 05, chips de estado debajo).

**D-06 · Partidos: secciones vs. pestañas (7.4).** Hoy se muestran cuatro secciones a la vez (EN VIVO, Próximos, Anteriores, CANCELADOS). Las pestañas Próximos / Finalizados de la img 07 ocultarían secciones.
→ Propuesta: pestaña "Próximos" = EN VIVO + Próximos; "Finalizados" = Anteriores + CANCELADOS (los cancelados se ven apagados y siguen visibles). No se pierde ningún partido.

**D-07 · Equipos: número de jugadores (7.4 / img 08).** "62 JUGADORES" y "18 JUG." por equipo no se cargan hoy (`EstadoEquipos` solo tiene equipos y entrenadores). La regla 7.4 pide usar datos ya cargados.
→ Propuesta: contadores EQUIPOS y DT solamente. Mostrar la categoría en lugar de "18 JUG." Si se quiere el conteo, hace falta una lectura nueva (anotar en PENDIENTES).

**D-08 · Modo oscuro con las paletas actuales (sección 4).** `SportProNavy`, `SyncVerde`, `EtiquetaAzul`, etc. son `val` de nivel superior (constantes). "Redirigirlas cambiando solo la declaración" da el color claro, pero **no** puede cambiar con `isSystemInDarkTheme()`. A esto se suman 155 `Color.White` sueltos (las tarjetas quedarían blancas en modo oscuro).
→ Propuesta: convertir cada una en `val X: Color @Composable get() = LocalPaleta.current.y` (mismo nombre, nadie cambia su llamada). Revisar con el compilador los usos fuera de `@Composable` (por ejemplo, `Triple(...)` en funciones normales o valores por defecto de parámetros que no son composables) y moverlos. Los `Color.White` de tarjetas y superficies se cambian a `MaterialTheme.colorScheme.surface` pantalla por pantalla. Es la tarea con más riesgo de regresiones visuales.

**D-09 · Textos que cambian según las imágenes (regla 4).** Las imágenes muestran textos distintos a los actuales: login "Ingresar" (hoy "Bienvenido" / "Iniciar sesión"), "SportPro" (hoy "Sport PRO"), "Guardar convocatoria" (hoy "Publicar/Actualizar convocatoria"), "Nuevo equipo", "Crear partido", "Planificar", "Marcar pagado", "Responder", "Aviso oficial" (hoy "Publicación oficial verificada"), etc.
→ Propuesta: conservar los textos actuales salvo los que la sección 6 o la 7 nombran (6.10: "Aviso oficial", "+ Aviso", "VENCIDA" en gris, "Ver detalles"; 7.2: mensaje offline; 7.1: "Sin partidos en vivo…"). Los FAB con texto ("+ Publicar") solo donde hoy ya hay texto. Donde hoy hay un "+" solo, el FAB lleva ícono `add` y el mismo texto accesible.

**D-10 · "Cuentas de 12 a 17 años por aprobar" (7.1).** `RepositorioCuentasJugador.listar()` devuelve las "cuentas cuyo documento coincide con una ficha que aún no tiene cuenta", con el campo `pendiente`. No es exactamente "cuentas de 12 a 17 por aprobar".
→ Propuesta: usar el mismo conteo que muestra hoy la pantalla Cuentas de jugadores (`listar().size`), para que el número del inicio coincida con lo que el ADM ve al tocar. Hay que confirmar.

**D-11 · Elementos de las imágenes que no están en la sección 7.** Img 04: bloque "Esta semana" (EQUIPOS, JUGADORES, ENTRENOS, ASISTENCIA). Img 01: "Uniforme blanco este sábado" (último anuncio) bajo la pizarra del DT. Img 08: "Asistencia 92 % / Mensualidad Al día" en Mis jugadores. Img 07: "Lluvia · se reprogramará" (motivo de cancelación, que no existe en el modelo).
→ Propuesta: no implementarlos salvo el último anuncio y la asistencia/mensualidad en Mis jugadores, si se aprueban como "solo lectura de datos existentes". El motivo de cancelación no se hace (requiere un campo nuevo, regla 2 / 7.6).

**D-12 · Minutos jugados sin FIN o con alineación sin confirmar (7.3).** Un partido FINALIZADO desde Detalle ("Finalizar partido") puede no tener un evento FIN. Tampoco se sabe la duración si el formato es 7 vs. 7 o 5 vs. 5.
→ Propuesta: si no hay FIN, usar el minuto del último evento válido y, si no hay eventos, contar 0 minutos para ese partido y marcarlo como "sin datos". Se documenta en el test.

**D-13 · "Ver detalles" en las pruebas (6.10.5).** No existe una pantalla de detalle de la prueba. Hoy la tarjeta muestra la descripción completa.
→ Propuesta: "Ver detalles" despliega o pliega la descripción dentro de la misma tarjeta (estado local, sin pantalla ni ruta nuevas).

**D-14 · Resumen con IA: botón "Editar" (6.11, img 09).** Hoy el texto final es un campo siempre editable y hay "Guardar cambios del borrador". No existe un modo "Editar".
→ Propuesta: el botón Editar enfoca el campo de texto (o alterna solo lectura/edición en la UI). Se conservan "Guardar cambios del borrador", "Aprobar resumen" y "Rechazar y pedir nueva generación" con sus textos (regla 4).

**D-15 · Pestañas de la imagen 09 de Comunidad.** La imagen 09 muestra Todo / Mi equipo / Academia / Avisos y el ícono de bandera. La 6.10 las corrige (Para ti / Academias / Pruebas, sin bandera). Se sigue la 6.10 y la img 10.

**D-16 · Números Doto ≥ 18 sp y textos ≥ 12 sp.** El "talón" de los tickets en las imágenes lleva la fecha ("MAR 24") en tamaño pequeño, y el mínimo es 12 sp. Las etiquetas de la pizarra ("Cuentas de jugadores") tienen que caber en 2 líneas con 12 sp.
→ Propuesta: talón con hora en Doto 18 sp y fecha en DM Mono 12 sp. Las etiquetas de las fichas con `maxLines = 2` y ancho fijo. En celulares angostos, la pizarra pasa a 3 columnas.

**D-17 · `TipoNotificacion.emoji` y las reacciones.** Se conservan en el modelo, como pide la espec. En la UI de notificaciones se usa un mapeo `TipoNotificacion → ImageVector` en `ui/notificaciones` (sin tocar el enum).

**D-18 · Simulación offline sin uso.** `SincronizacionViewModel` tiene textos "Simular sin conexión" / "Reconectar", pero no aparecen en ninguna pantalla. No se agrega nada; solo se anota.

---

## Decisiones (2026-11-24, tomadas por el usuario; mandan sobre las propuestas de la sección 4)

- **D-01** · Se adapta `AlineacionReactivaTest` a la nueva UI (`testTag` o `contentDescription`, por ejemplo "2 goles"), comprobando el mismo comportamiento.
- **D-02** · **No se agrega INICIO_2T.** El reloj del LED va sin 1T/2T. Se agrega un botón "Marcar inicio del 2T" en el partido en vivo que guarda la hora **solo en el dispositivo** (SharedPreferences, una clave por partido). El minuto sugerido usa esa hora en el segundo tiempo. No se tocan el catálogo, Firestore ni `functions/`. (Reemplaza el punto 2 del prompt 8 de `PROMPTS.md`).
- **D-03** · No se rompe la firma pública de `registrarEvento`. Se agrega una variante que devuelve también si quedó sincronizado.
- **D-04** · Jugadores: búsqueda por nombre filtrada en el cliente. Los chips son **Todos / Activos / Inactivos** (campo `activo`), no "Pendiente".
- **D-05 / D-06** · Se conservan los filtros existentes de Entrenamientos (Todos / Publicados / Cancelados) y las secciones de Partidos, con el nuevo estilo. No se duplican con pestañas nuevas.
- **D-08** · Las paletas existentes pasan a ser getters `@Composable` con el mismo nombre. `Color.White` se cambia solo cuando es fondo o superficie; el texto blanco sobre fondos oscuros o en el LED se queda.
- **D-09** · Se conservan los textos actuales, salvo los que la sección 6 o la 7 indican.
- **D-10** · "Cuentas por aprobar" = las cuentas con `pendiente == true` de `RepositorioCuentasJugador.listar()`, para que coincida con la pantalla Cuentas de jugadores.
- **Minutos jugados** · Se toman los titulares de la alineación **confirmada** de cada partido. Si no hay alineación confirmada, ese partido muestra "—". No se agregan campos (D-12 queda resuelta así).
- **InicioViewModel** · Solo lectura: nunca llama a `generarMensualidadesMesActual` ni a nada que escriba en Firestore.
- **Resto de dudas** (D-07, D-11, D-12 a D-18) · Se aceptan las propuestas, siempre que no quiten funcionalidad ni cambien las reglas de Firestore, `functions/` o la sincronización. Si alguna lo hiciera, se anota en `docs/diseno/PENDIENTES.md`.

---

## Reglas de trabajo (cambios de alcance pedidos por el usuario)

- **Sin commits ni push** (2026-11-24): Claude no hace commits ni push en ninguna etapa. Los cambios quedan en el árbol de trabajo y el usuario los confirma a mano. Las instrucciones "haz un commit" de la especificación y de los prompts se ignoran.
- **Sin vista de tablet** (2026-11-24): en cualquier tamaño de pantalla se usa el mismo diseño que en celular. En pantallas anchas, el contenido va centrado con `Modifier.widthIn(max = 600.dp)`. Se eliminan la cabecera ancha, la pizarra en grupos de 4 columnas, la columna derecha y la agenda de 7 días (6.2 y 7.1 de la espec, prompt 4).

---

## 5. Orden de trabajo propuesto (commits pequeños)

1. `build: material-icons-extended 1.7.8` → compilar.
2. `style(tema): tokens, tipografías y esquemas claro/oscuro` (Color/Type/Theme, `dynamicColor = false`).
3. `style(tema): paletas existentes apuntan a los tokens` (D-08), un commit por archivo de estilo.
4. `feat(kit): LineaDeCal.kt con previews`.
5. Pantallas por módulo, un commit `style(HU-xx)` por pantalla, en este orden: autenticación (+ `feat(HU-01)` contraseña visible) → inicio (+ `feat(HU-02…)` InicioViewModel en commits separados por rol) → academias/equipos/jugadores → vinculaciones → mensualidades → ejercicios/entrenamientos → asistencias → partidos/convocatoria/alineación → comunidad/moderación → anuncios/notificaciones/planes → resumen → estadísticas (+ `feat(HU-18)` minutos y goles, con tests) → catálogo.
6. Commits ⚠️ aparte: minuto sugerido (con test), panel rápido + "Más tipos", mensaje offline, reloj (y, si se aprueba, INICIO_2T).
7. Al cerrar cada etapa: `assembleDebug` + `testDebugUnitTest`, revisión en claro y en oscuro, y anotación en `docs/diseno/PROGRESO.md`.
