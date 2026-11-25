# Progreso del rediseño "Línea de cal"

Regla del usuario (2026-11-24): **Claude no hace commits ni push.** Los cambios quedan en el árbol de trabajo y el usuario los confirma a mano. Al final de cada etapa se deja un mensaje de commit sugerido.

---

## Etapa 0 · Revisión y plan (2026-11-24)

- `docs/diseno/PLAN.md`: inventario de las 33 pantallas, mapa del kit, funcionalidades nuevas con riesgos, dudas D-01 a D-18 y la sección **Decisiones**.
- Estado inicial: `assembleDebug` OK, `testDebugUnitTest` OK (46 tests).

---

## Etapa 1 · Tema, colores, fuentes y modo oscuro (2026-11-24)

HU tocadas: ninguna en lógica (solo aspecto, sección 4 de la espec).

### Qué se hizo

1. **Dependencia** `androidx.compose.material:material-icons-extended:1.7.8` (versión fija; ya no viene en el BOM). Archivos: `gradle/libs.versions.toml`, `app/build.gradle.kts`.
2. **`ui/theme/Color.kt`**: todos los tokens de la sección 3 (`Token*`) y dos paletas con los mismos campos, `PaletaClara` y `PaletaOscura` (`PaletaLineaDeCal`), expuestas con `LocalPaletaLineaDeCal` y `LineaDeCal.colores`. Incluye fondo, superficie, superficie oscura, borde, textos, césped de día y de noche y los pares fondo/texto de los chips (OK, pendiente, error, apagado, amarillo, neutro, más azul y lila para las etiquetas de visibilidad que ya existían). Los chips de error, apagado, amarillo y neutro en oscuro son derivados (la espec solo fija OK y pendiente).
3. **`ui/theme/Type.kt`**: `FamiliaTitulos` (Bricolage), `FamiliaMarcador` (Doto), `FamiliaCuerpo` (Instrument Sans), `FamiliaEtiquetas` (DM Mono), `EstiloMarcador`, `EstiloEtiqueta` y una `Typography` completa. Ningún estilo baja de 12 sp. Instrument Sans queda como fuente por defecto de todos los `Text`.
4. **`ui/theme/Theme.kt`**: sin color dinámico (el parámetro `dynamicColor` se conserva por compatibilidad, pero no se usa), `lightColorScheme` y `darkColorScheme` con los tokens según `isSystemInDarkTheme()`, y la paleta extendida provista con `CompositionLocalProvider`. TextField, DatePicker, TimePicker, FilterChip, Switch, Checkbox, RadioButton, AlertDialog, menús, hojas y CircularProgressIndicator toman sus colores del esquema (ver el comentario del archivo).
5. **Paletas existentes → tokens (decisión D-08)**: las 161 declaraciones `val X = Color(0x…)` de 21 archivos son ahora `val X: Color @Composable get() = LineaDeCal.colores.…`, con el mismo nombre y el valor anterior en un comentario `// antes …`. Archivos: `EstiloPartido`, `EstiloSincronizacion`, `ComponentesSportPro`, `ComponentesEstadisticas` y las pantallas de academias, alineación, anuncios, asistencias, autenticación (4), ejercicios, entrenamientos, equipos, inicio, jugadores, mensualidades y vinculaciones (3).
6. **Fondos que necesitaban un token distinto** (solo cambia el argumento de color, no la lógica):
   - `SportProNavy` es color de **texto** y en modo oscuro se vuelve claro. Se agregó `SportProNavyFondo` (superficie oscura) y se usa en los 42 sitios donde navy es **fondo** (botones oscuros, cabeceras con el logo, chip seleccionado, snackbar, tarjeta de partido EN CURSO, plan recomendado, marcador).
   - Del mismo modo, los fondos azul oscuro de Asistencias, Ejercicios y Entrenamientos, el botón de Registro y el botón de Notificaciones usan `LineaDeCal.colores.superficieOscura`. El botón "Cerrar" de Moderación y el contador de Notificaciones usan `naranja`.
   - `colorSimboloEvento` pasó a ser `@Composable` (mismo nombre y firma; sus dos llamadas ya estaban en composables) para que los colores de tarjeta, gol y falta salgan del tema.
7. **`Color.White` como fondo o superficie → `LineaDeCal.colores.superficie`** en 39 sitios: tarjetas, campos de texto, menús desplegables, botones claros y la insignia sobre la cancha. Se dejaron en blanco el texto, los íconos y los indicadores de progreso sobre fondos oscuros, el LED, y las líneas y bordes blancos de la cancha.

### Verificación

- Primera compilación: **falló** con 58 errores "@Composable invocations can only happen from the context of a @Composable function". Pasaba en las funciones normales que leían las paletas (ahora getters `@Composable`).
- Corrección: `estiloAudiencia` (Anuncios), `estiloVisibilidad` (Comunidad), `colorEstadoClaro` (Mensualidades), `coloresGenerales` (ComponentesSincronizacion), `estiloEventoEnVivo` (VisualesEvento) y `colorSimboloEvento` pasaron a ser `@Composable` (todos sus llamadores ya eran composables). `ResaltadoNarracion.resaltar(texto, paleta = PaletaClara)` recibe la paleta como parámetro con valor por defecto, porque se usa dentro de `remember` y de una `VisualTransformation`. `PantallaResumenPartido` le pasa `LineaDeCal.colores`.
- **Sin verificar**: el usuario pidió no compilar. La segunda compilación se detuvo antes de terminar y los tests no se corrieron. **Antes de confirmar esta etapa hay que correr** `./gradlew assembleDebug` y `./gradlew testDebugUnitTest`.

### Cambio de alcance (2026-11-24): sin vista de tablet

- Se actualizaron la espec (6.2, 7.1 y la fila de la img 04), `PROMPTS.md` (prompt 4) y `PLAN.md` ("Reglas de trabajo" y las filas de tablet).
- En el código no había nada de tablet que quitar. El único `BoxWithConstraints` que existe (`PantallaAlineacion.kt`) es para el tamaño de la cancha, no para cambiar el diseño, y se deja como está.

### Pendiente para las etapas siguientes (no es parte de la etapa 1)

- Quedan unos 100 `Color(0x…)` escritos **dentro** del código de las pantallas (por ejemplo en `VisualesEvento.kt` y en los botones de filtro sin seleccionar). Siguen con su color claro en modo oscuro y se reemplazan en la etapa 4, módulo por módulo.
- `FontFamily.Serif` y `Monospace` (81 usos), los textos de menos de 12 sp (100) y los emojis que hacen de ícono: etapa 4, por módulo.
- Revisión visual en claro y oscuro de cada pantalla: se hará al rediseñar cada módulo (etapas 3 a 6).

### Commit sugerido

`style: tema Línea de cal (claro y oscuro)`

---

## Etapa 2 · Kit de componentes (2026-11-25)

HU tocadas: ninguna. Todavía no cambia ninguna pantalla.

### Qué se hizo

Nuevo archivo `app/src/main/java/com/example/projectsportpro2026_2/ui/comun/LineaDeCal.kt` con los 12 componentes de la sección 5. Todos leen los tokens del tema (`LineaDeCal.colores` y las familias de `Type.kt`) y tienen preview en claro y en oscuro con la anotación `@VistaClaroOscuro`.

| Componente | Notas |
|---|---|
| `CabeceraCancha` | Franjas de césped, rectángulo y círculo central en blanco al 85 %, esquinas inferiores de 24 dp. Parámetros: título, subtítulo, etiqueta mono, botón atrás opcional, acciones y contenido extra. Incluye `PildoraCabecera` (selector "Equipo ▾" o etiqueta fija, 7.4) |
| `MarcadorLed` | Fondo `ledFondo` con trama de puntos y goles en Doto lima con resplandor (más intenso en oscuro). Nombres en mono blanco, etiquetas en mono amarillo y "PROVISIONAL" en naranja. Tiene punto rojo de EN VIVO, variante compacta y `onClick` con flecha. Describe el marcador completo para TalkBack |
| `ContadorLed` | Número en Doto (si no es numérico, por ejemplo "Al día", va en mono) más una etiqueta mono |
| `TarjetaTicket` | Talón con hora (Doto) y fecha (mono), línea discontinua y muescas. Variantes: `NORMAL`, `VERDE`, `AMARILLA`, `APAGADA`, `TACHADA` y `OSCURA` (partido en curso, 6.8). `colorMuesca` = color del fondo donde va la tarjeta |
| `FichaTactica` | De 44 a 58 dp, con sombra inferior "de ficha", 5 colores (`ColorFicha`), contador naranja y etiqueta de 2 líneas como máximo |
| `PizarraFichas` | Navy (navy2 en oscuro) con borde discontinuo interior y cuadrícula de 3 o 4 columnas a partir de una lista de `DatoFicha` |
| `CromoJugador` | Versiones `GRANDE` y `MINI`: borde amarillo, franjas verdes, número gigante translúcido, foto (`AvatarRemoto` si hay `fotoUrl`) o silueta lima, posición en chip lima, pie con equipo y estado, e insignia opcional. El número es opcional porque `Jugador` no tiene dorsal |
| `ChipEstado` | `TipoEstado`: OK, PENDIENTE, ERROR, APAGADO, AMARILLO y NEUTRO; con punto o ícono opcional |
| `BotonPrincipal` | `VarianteBoton.LIMA` y `NAVY`, con ícono opcional e indicador de carga |
| `CampoSportPro` | `OutlinedTextField` con etiqueta mono arriba, fondo de superficie y borde verde al enfocar. Muestra error o ayuda y tiene `esContrasena` con el botón visibility / visibility_off (deja lista la 7.5) |
| `BarraSuperior` | Botón atrás circular, título Bricolage centrado y hasta 2 acciones. Incluye `AccionBarra` (botón circular con contador y variante destacada para Moderar) |
| `FondoCancha` | Versión grande de la cabecera para la bienvenida y el login |

### Decisiones de esta etapa

- **Doto solo en números de 18 sp o más.** La sección 5 pedía Doto en los nombres de equipo y en las etiquetas del LED, pero la 3.3 y el prompt lo prohíben en texto. Por eso van en DM Mono (blanco y amarillo). La función interna `TextoNumero` usa Doto solo si el texto es numérico y mide 18 sp o más; si no, cae a DM Mono.
- Ningún texto del kit mide menos de 12 sp. Los íconos son Material Rounded (`material-icons-extended`).
- Se agregaron tres ayudas que no están en la sección 5, porque las imágenes las usan: `PildoraCabecera`, `AccionBarra` y la variante `OSCURA` del ticket.

### Verificación

- **No se compiló ni se corrieron tests**: el usuario pidió no compilar. Se revisó el código a mano (APIs, imports, que no haya textos de menos de 12 sp). Los previews tampoco se revisaron en Android Studio.
- Antes de confirmar las etapas 1 y 2 hay que correr `./gradlew assembleDebug` y `./gradlew testDebugUnitTest`, y abrir los previews de `LineaDeCal.kt` en claro y en oscuro.

### Commit sugerido

`feat(ui): kit de componentes Línea de cal`

---

## Etapa 3 · Inicio de los 4 roles (2026-11-25)

HU tocadas: HU-02 (inicio por rol) y lo nuevo de la 7.1 (HU-08, HU-10, HU-11, HU-17, HU-19, solo lectura). Referencia: img 03. Sin vista de tablet.

### Qué se hizo

- **`ui/inicio/PantallasInicio.kt`** (rediseño): `InicioAdministrador`, `InicioEntrenador`, `InicioJugador`, `InicioApoderado` y `PantallaCuentaPendiente` conservan nombre y parámetros. Solo se agregó `viewModel: InicioViewModel = viewModel()` con valor por defecto, así que `NavegacionApp.kt` no cambia.
  - Estructura: `CabeceraCancha` (etiqueta con el título de antes, por ejemplo "Panel del administrador"; saludo "Hola, {nombre}." con el nombre real del perfil; la descripción de antes como subtítulo), campana con no leídas, avatar con iniciales y menú (nombre + "Cerrar sesión"), tarjeta EN VIVO, sección propia del rol, "Pizarra" con `PizarraFichas` y un botón discreto "Cerrar sesión" al final.
  - En pantallas anchas, el contenido va centrado con `widthIn(max = 600.dp)`. No hay barra inferior.
  - `PantallaCuentaPendiente`: `CabeceraCancha` con el mismo título y texto, chip "Pendiente" y "Cerrar sesión".
- **`ui/inicio/InicioViewModel.kt`** (nuevo, **solo lectura**): cada sección se carga por separado con `Result`. Si una falla, queda en `null` y la pantalla la oculta. No llama a `generarMensualidadesMesActual` ni a nada que escriba. Se revisó el cuerpo de cada función usada (y de las privadas que llama): ninguna hace `set`, `update`, `add`, `delete`, `batch` ni `commit`.
- **`ui/inicio/LogicaInicio.kt`** (nuevo): lógica pura de contadores y selecciones, separada para poder probarla.
- **`app/src/test/.../inicio/LogicaInicioTest.kt`** (nuevo): 21 tests de la lógica nueva (**no se corrieron**, por pedido del usuario).

### Destinos conservados (lista de control del PLAN 1.5)

| Rol | Campana | Marcador LED | Fichas de la pizarra | Otros | Total |
|---|---|---|---|---|---|
| ADM | Notificaciones | Partidos en vivo | Gestionar academias, Cuentas de jugadores, Vinculaciones, Mensualidades, Biblioteca de ejercicios, Asistencias, Estadísticas, Anuncios del club, Comunidad (9) | Tarjeta "Planes y ofertas" | **12** ✅ |
| DT | Notificaciones | Partidos en vivo | Gestionar academias, Entrenamientos, Asistencias, Biblioteca de ejercicios, Estadísticas, Anuncios del club, Comunidad (7) | — | **9** ✅ |
| JUG | Notificaciones | Partidos en vivo | Mis convocatorias, Mis entrenamientos, Mi asistencia, Mis mensualidades, Estadísticas, Anuncios del club, Comunidad (7) | — | **9** ✅ |
| PAD | Notificaciones | Partidos en vivo | Mis jugadores, Mensualidades, Entrenamientos, Historial de asistencia, Convocatorias, Estadísticas, Anuncios del club, Comunidad (8) | — | **10** ✅ |

"Cerrar sesión" está en el menú del avatar y en el botón del final, en los 4 roles y en Cuenta pendiente. Los textos de los botones son los de antes, sin emojis ("📊 Estadísticas" → "Estadísticas", "💎 Planes y ofertas" → "Planes y ofertas", "🔔 Notificaciones" → campana con `contentDescription` "Notificaciones").

### Lo nuevo de la 7.1 y de dónde salen los datos

| Sección | Datos (todas son lecturas) |
|---|---|
| Nombre del saludo | `RepositorioAutenticacion.obtenerPerfil(uid)` |
| Campana | `RepositorioNotificaciones.listar(uid)` → `leida == false` |
| Tarjeta EN VIVO | `RepositorioSeguimientoPartido.equiposVisibles` + `escucharPartidos` (la misma lógica de "Partidos en vivo"); marcador con `RepositorioSincronizacion.escucharEventos` + `calcularMarcador`. Sin partido en curso muestra "Sin partidos en vivo · próximo: {día} · {hora}" (o solo "Sin partidos en vivo") y sigue abriendo Partidos en vivo |
| ADM · Requiere tu atención | Cuentas: `RepositorioCuentasJugador.listar` con `pendiente == true` (D-10). Vinculaciones: `listarParaAdministrador` en PENDIENTE. Mensualidades: `listarParaUsuario` en VENCIDO. Los mismos números aparecen sobre las fichas, y cada tarjeta abre la pantalla que ya existe |
| DT · Tu jornada | Próximo entrenamiento PUBLICADO de `RepositorioEntrenamientos.listarParaUsuario` (objetivo, hora, `duracionTotalMinutos`, número de ejercicios; **sin lugar**). Próximo partido PROGRAMADO con "respondieron x/y" de `RepositorioConvocatorias.listarConvocados` |
| JUG · Para ti | Convocatoria por responder (`listarParaJugador`, abre Mis convocatorias), % de asistencia (`listarHistorialJugador`, mismo criterio que Estadísticas), goles de la temporada (`RepositorioEstadisticas.cargarEquipo` + `CalculadoraEstadisticas.calcularJugador`, se carga al final) y mensualidad del mes (`listarParaUsuario`) |
| PAD · Mis jugadores | `RepositorioAsistencias.listarJugadoresConsulta` (fichas visibles para el apoderado) más la mensualidad del mes de cada uno. Cromos mini, "Ver todos" abre Mis jugadores. Contadores en Mensualidades (vencidas) y Convocatorias (por responder, `listarParaApoderado`) |

### Notas

- La marca "⚽ SportPro · PANEL PRINCIPAL" de la cabecera anterior se reemplazó por la cabecera de césped con el título de cada panel, como en la img 03. En Cuenta pendiente se mantiene "Panel principal" como etiqueta.
- El partido del DT y la convocatoria del JUG abren pantallas que ya existen (Partidos en vivo y Mis convocatorias). No se agregaron rutas.

### Verificación

- **No se compiló ni se corrieron tests** (pedido del usuario). Se revisó a mano: firmas de los repositorios, paquetes, lambdas `@Composable` y que no haya textos de menos de 12 sp.
- Pendiente antes de confirmar: `./gradlew assembleDebug`, `./gradlew testDebugUnitTest` (incluye `LogicaInicioTest`) y revisar el inicio de los 4 roles en claro y en oscuro.

### Commits sugeridos

- `style(HU-02): inicio de los 4 roles Línea de cal` (`PantallasInicio.kt`)
- `feat(HU-02): InicioViewModel de solo lectura y contadores del inicio` (`InicioViewModel.kt`, `LogicaInicio.kt`, `LogicaInicioTest.kt`)

---

## Etapa 4 · Resto de pantallas, módulo por módulo (2026-11-25)

Sección 6 de la espec. **No se compiló ni se corrieron tests** (pedido del usuario). Se revisó a mano y con scripts que llaves y paréntesis estén balanceados en cada archivo tocado.

### En todos los módulos

- Textos de 9 a 11 sp llevados a 12 sp (95 casos); `FontFamily.Serif` → `FamiliaTitulos` y `FontFamily.Monospace` → `FamiliaEtiquetas` (78 casos).
- 81 colores escritos a mano dentro del código pasados a tokens del tema (`LineaDeCal.colores.*`, con el valor anterior en un comentario `/* antes … */`). Se dejó fijo el texto claro sobre la tarjeta oscura del partido en curso (`0xFFD7E5EC`), porque ese fondo es oscuro en los dos modos.
- Emojis y flechas de texto que hacían de ícono → íconos Material Rounded (se conservan las reacciones ❤️ 👏 💪, que son de Comunidad, etapa 5).
- "← Volver / ← Inicio / ← Partidos…" → ícono de flecha más el mismo texto (o el mismo texto como descripción de TalkBack en las cabeceras).
- Las cabeceras decorativas propias (logo ⚽ + "SPORTPRO" + campana 🔔 **sin acción**) quedaron vacías: `Cabecera*SportPro()` de Crear/Editar/Detalle de partido, Convocatoria, Mis convocatorias, Alineación, Catálogo y Anuncios, y `CabeceraSportPro()` de `ComponentesSportPro`. No tenían ninguna acción; cada pantalla conserva su título y su botón de volver.
- Kit: se agregaron `EscudoIniciales` (img 08) y `Modifier.anchoCompleto(margen)`, que deja la cabecera de césped a todo el ancho dentro de pantallas que ya tienen margen lateral, sin reestructurarlas.

### Por módulo (comparado con el inventario de `PLAN.md`, sección 1)

| # | Módulo | Qué cambió | Acciones del inventario |
|---|---|---|---|
| 1 | Autenticación | Bienvenida, Login, Registro y Recuperar con `FondoCancha`, `CampoSportPro` y `BotonPrincipal`. Íconos en la introducción y en las tarjetas de rol. `CampoFecha`: ícono de calendario | ✅ todas (Comenzar, Volver, Continuar / Ir a iniciar sesión, Saltar; Volver al inicio, Iniciar sesión, ¿Olvidaste…?, Crear cuenta; Enviar enlace; los 3 roles, aviso ADM, todos los campos y validaciones) |
| 2 | Academias y equipos | `CabeceraCancha`, FAB lima con ícono, escudo de iniciales, `ChipEstado`, equipos inactivos apagados | ✅ todas (crear, editar, activar/inactivar, Equipos, Partidos, Catálogo, Directores técnicos, Jugadores, formularios) |
| 3 | Jugadores, cuentas, mis jugadores, vinculaciones | Jugadores en cuadrícula de `CromoJugador` mini (foto, Inactivo, Menor de edad) con Editar e Inactivar debajo. Las demás pantallas: `CabeceraCancha` y `ChipEstado` | ✅ todas (alta, edición con foto y datos sensibles, Vincular, Solicitar, Solicitar desvinculación, pestañas, Autorizar, Rechazar, Desvincular) |
| 4 | Mensualidades | `CabeceraCancha`, contadores como `ContadorLed`, un `TarjetaTicket` por mensualidad (periodo en el talón; verde = pagado, amarillo = pendiente), `ChipEstado`, `BotonPrincipal` "Registrar mensualidad". Sin montos | ✅ todas (búsqueda, chips de periodo y estado, detalle, Modificar estado, formulario, nota de solo consulta) |
| 5 | Ejercicios y entrenamientos | `CabeceraCancha`. Ejercicios: `FichaTactica` según el objetivo, chip del objetivo, duración en Doto. Entrenamientos: `TarjetaTicket` (cancelados tachados), nota naranja del último cambio de horario (historial existente), caja LED "EJERCICIOS x/y" con barra en Planificar, ↑ ↓ como íconos | ✅ todas (selector, búsqueda, filtros, crear, editar, activar; editar y cancelar sesión, subir, bajar, quitar, agregar, publicar) |
| 6 | Asistencias | `CabeceraCancha`, fichas redondas **P / A / J** (TalkBack lee "Presente", "Ausente" y "Justificado"), contadores como `ContadorLed`, `ChipEstado` en el historial | ✅ todas (registro rápido, guardar, corregir, cancelar corrección, historial por jugador) |
| 7 | Partidos, convocatoria, alineación | Lista: `CabeceraCancha` y tickets (amarillo programado, oscuro en curso, tachado cancelado) con `ChipEstado`. Crear/Editar: íconos de calendario, hora y desplegable. Detalle: `MarcadorLed` e íconos en Editar, Abrir en vivo y Resumen | ✅ todas (todas las transiciones de estado y sus diálogos, convocar, responder, alineación completa) |
| 8 | Catálogo de eventos | Menú "⋮" como `IconButton` con ícono | ✅ todas |
| 9 | Partido en vivo y seguimiento | `MarcadorLed` grande (PROVISIONAL en naranja + aviso con ícono), aviso de sincronización con fondo naranja e íconos de nube y Reintentar, íconos Material por tipo de evento (`iconoEvento`) en la cronología, el panel, el resumen y la cancha. Seguimiento: marcador en Doto | ✅ todas. **HU-16 sin cambios de lógica** |
| 10 | Anuncios, notificaciones, moderación, planes | Megáfono en anuncios, ícono por tipo de notificación (D-17), íconos en ofertas y en lo que incluye cada plan | ✅ todas |
| 11 | Resumen con IA | `CabeceraCancha`, `MarcadorLed` del partido, íconos en los plegables, avisos, Generar, Aprobado, comentarios y audio | ✅ todas (generar con o sin IA, guardar, aprobar, rechazar, escuchar, comentarios, notas e historial) |
| 12 | Estadísticas | `CabeceraCancha`, `CromoJugador` grande con edad y "MENOR" como insignia, métricas en Doto con íconos | ✅ todas (pestañas, selectores y aviso de datos protegidos) |

### Decisiones y notas

- **D-01**: `IndicadoresJugadorEnCancha` muestra un ícono de balón más "×N". `AlineacionReactivaTest` ahora comprueba el texto "×2". Las descripciones ("Ana Uno, 2 goles", etc.) no cambiaron.
- **7.5 adelantada**: `CampoSportPro` ya trae el botón de mostrar u ocultar la contraseña, así que Login y Registro lo tienen desde esta etapa.
- Textos: se quitaron solo los glifos ("Comenzar →" → "Comenzar" + ícono, "✓ Disponible" → ícono + "Disponible", "+ Nuevo" → ícono + "Nuevo"). El resto se conserva (D-09).
- `simboloEvento()` (pública) se conserva sin cambios, pero la UI ya no la usa. La reemplaza `iconoEvento()`.
- Quedan glifos que son contenido y no íconos: "Versión 1 → 2" en la auditoría, "Ajustes › Sistema › …" (ruta de ajustes), las estrellas ★☆ de la evaluación y "● EN VIVO" en Mis partidos. También queda `simbolo = "▰"`, un argumento que ya no se dibuja.

### Pendiente (no es una regla rota; queda para las próximas etapas o la verificación final)

- Detalles visuales de la sección 6 que no se hicieron: caja LED "RESPONDIERON x/y" en Convocatoria; colores del arquero y del jugador no disponible en la cancha de Alineación; cromo grande por jugador en Mis jugadores; tickets en Cuentas de jugadores y Vinculaciones (quedaron como tarjetas con `ChipEstado`).
- `CampoFecha` abre el `DatePickerDialog` de Android (no el de Compose), que toma los colores de `themes.xml`, no del tema de Compose.
- Comunidad (etapa 5) también usaba `CabeceraSportPro()`: hasta que se rediseñe, no muestra el logo arriba.
- Compilar, correr los tests y revisar las pantallas en claro y en oscuro.

### Commits sugeridos (uno por módulo)

`style(HU-02): autenticación Línea de cal` · `style(HU-03): academias y equipos` · `style(HU-04/05): jugadores y vinculaciones` · `style(HU-06): mensualidades` · `style(HU-07/08): ejercicios y entrenamientos` · `style(HU-09): asistencias` · `style(HU-10/11/12): partidos, convocatoria y alineación` · `style(HU-13): catálogo de eventos` · `style(HU-14/16/17): partido en vivo y seguimiento` · `style(HU-19/20/24): anuncios, notificaciones, moderación y planes` · `style(HU-26/27): resumen con IA` · `style(HU-18): estadísticas`

---

## Etapa 5 · Comunidad (2026-11-25)

HU-21 a HU-24. Sección 6.10 con las 11 correcciones; referencia img 10 (el texto manda). **No se compiló ni se corrieron tests** (pedido del usuario); se revisó a mano y con scripts que llaves y paréntesis estén balanceados.

### Las 11 correcciones de la 6.10

| # | Corrección | Cómo quedó |
|---|---|---|
| 1 | Pestañas actuales | Para ti, Academias y Pruebas (sin cambios de lógica) |
| 2 | Bloqueados y Moderar arriba, sin bandera | `BarraSuperior` "Comunidad" con `AccionBarra` **block** ("Bloqueados") y **shield** destacado ("Moderar", solo ADM). El número sobre el escudo son los reportes **PENDIENTES**: `AccionesModeracionViewModel.reportesPendientes`, que reutiliza `RepositorioModeracion.listarReportes(uid)` (la misma consulta del panel, solo lectura). Si falla, el escudo va sin número. Se recarga al volver a la pantalla |
| 3 | Mismo molde de tarjeta | Publicaciones y pruebas: avatar o logo con iniciales, nombre, chip de rol y menú ⋮. Las pruebas muestran "Aviso oficial" con el ícono **verified** |
| 4 | Título de la prueba | `AvisoPrueba.titulo()` (no hay título libre) |
| 5 | Estados según `estadoEfectivo()` | "● PRUEBA ABIERTA" (verde), "CERRADA" (gris) y "VENCIDA" (gris; antes era naranja), chip **amarillo** con la fecha, datos de categoría, posición, fecha y ubicación con íconos, y botón "Ver detalles" (D-13: despliega la descripción en la misma tarjeta; vuelve a ocultarla con "Ocultar detalles") |
| 6 | Reacciones | Un solo botón: si reaccionaste, tu emoji resaltado, los demás emojis presentes y el total; si no, ♡ (ícono `FavoriteBorder`), los emojis presentes y el total (por ejemplo ❤️👏💪 37). Al tocarlo se abre el selector actual con ❤️ 👏 💪, el conteo de cada uno y "Toca tu reacción otra vez para quitarla". Sin botón de compartir |
| 7 | Menú ⋮ en cada tarjeta | Ajenas: abre `HojaAccionesContenido` (Reportar, Bloquear y, para el ADM, Moderar directamente). Propias: Editar y Eliminar. Pruebas: Cerrar y Reabrir convocatoria (autor o ADM) |
| 8 | Filtros de Pruebas | Categoría, Posición, Ubicación, Fecha, el interruptor "Mostrar cerradas y vencidas" y "Limpiar" (sin cambios; ▾ como ícono) |
| 9 | FAB | `ExtendedFloatingActionButton` lima: ícono "+" con "Publicar" en Para ti y Academias, y con "Aviso" en Pruebas (mismas condiciones que antes) |
| 10 | Espacio al final | 96 dp de margen inferior en la lista, para que el FAB no tape el último "Ver detalles" |
| 11 | Se conserva | Mensaje para quien no pertenece a una academia ("Ver pruebas"), comentarios, hoja de bloqueados y panel de moderación |

### Otros cambios

- `HojaAccionesContenido`: opciones con ícono en círculo de color (flag, block, shield) y notas con ícono (lock, info).
- `HojaComentarios`: ♡ como ícono; eliminar (ícono delete) y ⋮ como `IconButton` con descripción para TalkBack.
- `FormularioPublicacion`: íconos en la visibilidad (groups, account_balance, public), "Añadir imagen" (add_photo_alternate) y "Quitar" (close).
- `FormularioAvisoPrueba`: insignia oficial con ícono verified, "Volver" con flecha y el aviso de fecha sin "⚠".
- Snackbars sin el "✓" delante del texto.
- Pasada mecánica también en Comunidad: 11 textos de menos de 12 sp llevados a 12 sp.

### Inventario (`PLAN.md` 1.27)

✅ No falta ninguna acción: Inicio, Bloqueados (hoja con desbloquear), Moderar, Publicar/Aviso, pestañas, reaccionar y quitar la reacción, comentar y eliminar o reportar comentarios, Editar y Eliminar con su confirmación, Reportar, Bloquear, Moderar directamente, Cerrar y Reabrir convocatoria, filtros y Limpiar, "Ver pruebas", formularios con imagen y reintento o "Solo texto", y todos los mensajes.

### Commits sugeridos

- `style(HU-21..24): comunidad Línea de cal` (pantalla, pruebas, hojas y formularios)
- `feat(HU-24): número de reportes pendientes en el escudo de Moderar` (`AccionesModeracionViewModel.kt` y su uso en la pantalla)

---

## Etapa 6 · Estadísticas, listas y login (2026-11-25)

Secciones 7.3, 7.4 y 7.5 y las decisiones de `PLAN.md`. **No se compiló ni se corrieron tests** (pedido del usuario); se revisó a mano y con scripts que llaves y paréntesis estén balanceados.

### 1 · Estadísticas del jugador (7.3, HU-18)

- **`CalculadoraEstadisticas.kt`** (lógica pura):
  - `minutosJugados(idJugador, eventos, titulares)`: un titular cuenta desde el minuto 0; si entra por un CAMBIO, desde el minuto del cambio; termina cuando sale por un CAMBIO o en el minuto del FIN. **Sin FIN** (D-12): hasta el último evento válido (sin eventos, 0). **Sin alineación confirmada**: `null`, que se muestra como "—". Solo cuenta eventos válidos y cambios del equipo propio (ignora los anulados y los del rival). Admite entrar y volver a salir.
  - `partidosDelJugador(...)`: un `PartidoJugador` por partido FINALIZADO del periodo, con resultado, goles del jugador, minutos y si fue titular.
  - `EstadisticasJugador` suma `minutosJugados` (solo de partidos con alineación confirmada; `null` si ninguno la tiene) y `partidos`.
  - `DatosFuenteEquipo` suma `titularesPorPartido`. Es un dato en memoria, **no un campo de Firestore** (decisión "no agregues campos").
- **`RepositorioEstadisticas.cargarEquipo`**: en la misma lectura de la alineación de cada partido, ahora también lee el documento `alineaciones/{partido}` para saber si está `confirmada`, y toma sus titulares (`tipo == TITULAR`). `alineadosPorPartido` (participaciones) sigue igual que antes. Si no se puede leer, el partido queda sin minutos ("—").
- **`PantallaEstadisticas`**:
  - `CromoJugador` con la **foto** (`fotoUrl`) o la silueta, el equipo y el periodo.
  - Métrica **Minutos** ("—" si no hay alineación confirmada).
  - **Goles por fecha**: un balón por gol en cada partido (F1, F2…) y "—" cuando no hubo.
  - **Último partido**: un ticket con el resultado, los goles, los minutos y si fue titular, suplente o no tiene alineación confirmada.
- **Compartir el cromo como imagen** (al final, opcional): el cromo se dibuja en una `GraphicsLayer`. "Compartir cromo" lo guarda en `cache/compartidos/cromo.png` y abre `Intent.ACTION_SEND` mediante un `FileProvider` (`${applicationId}.archivos`, `res/xml/rutas_compartidas.xml`, declarado en `AndroidManifest.xml`). Solo aparece en Estadísticas, que ya muestra únicamente a los jugadores que el usuario puede ver.
- **Tests** (no se corrieron): `app/src/test/.../estadisticas/MinutosJugadosTest.kt`, con 11 casos: titular hasta el FIN, entra por cambio, sale por cambio, sin FIN, sin FIN ni eventos, sin alineación (null), suplente que no entra, cambios anulados y del rival, entra y vuelve a salir, goles por fecha y último partido, y la suma en `calcularJugador`.

### 2 · Contadores LED y filtros en chips (7.4)

Todo se calcula en el cliente con los datos que ya carga cada ViewModel. Nuevos en el kit: `FilaContadores`, `FiltroActivos` y `FiltroChipsActivos`.

| Pantalla | Contadores en la cabecera | Filtro nuevo |
|---|---|---|
| Academias | Academias, Activas | — |
| Equipos | Equipos, Activos, DT (D-07: sin jugadores, no están cargados) | Todos / Activos / Inactivos (con "Todos" para no ocultar equipos por defecto) |
| Jugadores | Jugadores, Activos, Inactivos | **Búsqueda por nombre** y chips Todos / Activos / Inactivos (D-04) |
| Entrenamientos | Esta semana (publicados de lunes a domingo), Minutos, Cancelados | Ya existía (Todos / Publicados / Cancelados, D-05) |
| Partidos | Jugados, Ganados, Próximos | Ya existían las secciones (D-06) |
| Mensualidades | Ya existían (Pagadas / Pendientes / Vencidas) | Ya existía |
| Convocatoria | — | Todos N / Disponibles N / Pendientes N |
| Historial de asistencia | — | Todas / Ausencias / Justificadas |

- El selector de equipo en las cabeceras ("Equipo ▾") no se agregó: las pantallas que ya permiten elegir equipo conservan su selector actual. Queda como pendiente visual opcional (`PildoraCabecera` ya está en el kit).

### 3 · Mostrar y ocultar la contraseña (7.5, HU-01)

Ya quedó en la etapa 4: Login y Registro usan `CampoSportPro(esContrasena = true)`, con el botón visibility / visibility_off ("Mostrar contraseña" / "Ocultar contraseña" para TalkBack). En esta etapa no hubo cambios.

### Commits sugeridos

- `feat(HU-18): minutos jugados, goles por fecha y último partido` (calculadora, repositorio, pantalla y `MinutosJugadosTest`)
- `feat(HU-18): compartir el cromo del jugador como imagen` (`PantallaEstadisticas` "Compartir cromo", `AndroidManifest.xml` y `rutas_compartidas.xml`)
- `feat(ui): contadores LED y filtros en chips (7.4)` (kit, academias, equipos, jugadores, entrenamientos, partidos, convocatoria y asistencias)

---

## Etapa 7 · Partido en vivo (2026-11-25) — para revisión de Italo (HU-14)

Sección 7.2 con la decisión D-02 (**no se agregó INICIO_2T al catálogo**). No se tocó nada de la HU-16 fuera de lo indicado: la cola offline, los pendientes, el reintento, "Requiere revisión" y el marcador provisional siguen igual. **No se compiló ni se corrieron tests** (pedido del usuario); se revisó a mano y con scripts que llaves y paréntesis estén balanceados.

### 1 · Minuto sugerido y "Marcar inicio del 2T"

- `datos/partido/RelojPartido.kt` (lógica pura, sin red):
  - `capturaInicio(eventos)`: la `fechaCaptura` del INICIO válido (no anulado) más antiguo de la cronología que ya está en caché.
  - `minutoSugerido(inicio, inicio2T, ahora)`: en el 1T, `((ahora − INICIO) / 60000) + 1`; con la marca del 2T, `45 + ((ahora − marca) / 60000) + 1`. Sin INICIO devuelve `null` y el campo queda vacío, como antes.
- `datos/partido/AlmacenSegundoTiempo.kt`: guarda la hora del 2T **solo en el dispositivo** (SharedPreferences `sportpro_segundo_tiempo`, clave `inicio_2t_<idPartido>`). No escribe en Firestore ni pasa por la cola de la HU-16.
- `PartidoEnVivoViewModel.abrirEvento(tipo, minutoSugerido: Int? = null)`: el parámetro nuevo tiene valor por defecto, así que las llamadas existentes no cambian. Precarga `formulario.minuto`.
- Formulario de evento: botones **−** y **+** a los lados del campo Minuto, que sigue siendo editable. El operador confirma el minuto al registrar (HU-14).
- Pantalla: botón **"Marcar inicio del 2T"** (solo EN CURSO y con INICIO en caché). Una vez marcado muestra "2T marcado a las HH:mm · solo en este dispositivo" y el botón "Quitar marca", por si se marcó por error.
- **Para revisar**: el 2T continúa desde el minuto 45 (`MINUTOS_PRIMER_TIEMPO`). En categorías con tiempos más cortos la sugerencia hay que corregirla a mano (la constante y el parámetro `minutosPrimerTiempo` permiten cambiarlo).
- Tests (no se corrieron): `app/src/test/.../partido/RelojPartidoTest.kt`, con 13 casos (sin INICIO, minuto 1, 37', reloj del celular atrasado, 2T en 46 y 56, marca inválida, duración ajustable, reloj 12:34 y 46:18, más de 99 minutos e INICIO válido más antiguo).

### 2 · Reloj en el MarcadorLed

`TarjetaMarcador` recibe `reloj`. El LED muestra "EN CURSO · mm:ss", que se actualiza cada segundo mientras el partido está EN CURSO. Antes de marcar el 2T se cuenta desde el INICIO; después, 45:00 más el tiempo desde la marca. Sin etiqueta 1T/2T (D-02). Sin INICIO en caché solo dice "EN CURSO".

### 3 · Panel rápido sobre la mini cancha

`PanelEventosRapidos` dibuja las fichas sobre `MiniCancha` (nueva en el kit): Tarjeta, Falta, **Gol** al centro, Cambio y Tiro de esquina. Se toman del **catálogo activo** por su código: si un tipo no está activo, su ficha no aparece, y la etiqueta es el nombre del catálogo. La ficha **"Más tipos"** lista todos los demás tipos activos, incluidos FIN y los personalizados; INICIO aparece deshabilitado, como antes. Todas abren el formulario actual con `abrirEvento(tipo)`. Se quitó `BotonEventoRapido`, que ya no se usaba.

### 4 · Mensaje honesto sin conexión

- `RepositorioEventosPartido.registrarEventoConEstado(...)` devuelve `RegistroEvento(evento, sincronizado)`, con el valor de `confirmarOEncolar`. **`registrarEvento(...)` conserva su firma pública** y delega en la variante nueva (`.map { it.evento }`).
- `PartidoEnVivoViewModel.registrarEvento()` usa la variante. Si `sincronizado == false`, el mensaje es "**{Evento} guardado en este dispositivo · se enviará al reconectar**" en lugar de "registrado correctamente" (también para FIN).

### Además

- Se corrigió un `@Composable` duplicado sobre `TarjetaMarcador` que había quedado de la etapa 4 y no habría compilado.

### Archivos tocados en este prompt (revisión de Italo)

| Archivo | Cambio |
|---|---|
| `app/src/main/java/com/example/projectsportpro2026_2/datos/partido/RelojPartido.kt` | **Nuevo**: minuto sugerido, reloj e INICIO en caché |
| `app/src/main/java/com/example/projectsportpro2026_2/datos/partido/AlmacenSegundoTiempo.kt` | **Nuevo**: hora del 2T en SharedPreferences |
| `app/src/main/java/com/example/projectsportpro2026_2/datos/repositorio/evento/RepositorioEventosPartido.kt` | `registrarEventoConEstado` y `RegistroEvento`; `registrarEvento` delega (misma firma) |
| `app/src/main/java/com/example/projectsportpro2026_2/ui/eventospartido/PartidoEnVivoViewModel.kt` | `abrirEvento(tipo, minutoSugerido)` y mensaje "guardado en este dispositivo" |
| `app/src/main/java/com/example/projectsportpro2026_2/ui/eventospartido/PantallaPartidoEnVivo.kt` | Reloj en el LED, "Marcar inicio del 2T", − / + en el minuto, panel sobre la mini cancha con "Más tipos" |
| `app/src/main/java/com/example/projectsportpro2026_2/ui/comun/LineaDeCal.kt` | Kit: `MiniCancha` |
| `app/src/test/java/com/example/projectsportpro2026_2/partido/RelojPartidoTest.kt` | **Nuevo**: tests del minuto sugerido y del reloj |

### Commits sugeridos (aparte, para que los revise Italo)

- `feat(HU-14): minuto sugerido con − / + y marca local del 2T` (RelojPartido, AlmacenSegundoTiempo, ViewModel, formulario, RelojPartidoTest)
- `feat(HU-14): reloj del partido en el marcador LED`
- `feat(HU-14): panel rápido sobre la mini cancha con "Más tipos"` (PanelEventosRapidos y MiniCancha)
- `feat(HU-14): mensaje "guardado en este dispositivo" sin conexión` (RepositorioEventosPartido y ViewModel)

---

## Verificación final (2026-11-25)

Revisión contra `PLAN.md` y `ESPECIFICACION.md`, esta vez compilando y corriendo los tests. Sin commits ni push.

### 1 · Compilación (`./gradlew assembleDebug`)

| Ronda | Resultado | Errores |
|---|---|---|
| 1 | ❌ BUILD FAILED | 11 errores en 3 causas: (a) `InicioViewModel` importaba `RepositorioNotificaciones` de `datos.repositorio.sincronizacion`, pero su paquete es `datos.repositorio`; (b) `@Composable` repetido en `PantallaRecuperar`; (c) en `CampoSportPro`, `if (x) @Composable { … } else null` se leía como bloque del `if` y no como lambda |
| 2 | ✅ **BUILD SUCCESSFUL** | 0. Corregido: import con el paquete real, anotación duplicada quitada (y búsqueda de otras en todo `ui/`) y lambdas envueltas en `{ @Composable { … } }` |
| 3 (después de la limpieza del punto 4) | ✅ **BUILD SUCCESSFUL** | 0 |

Además, `./gradlew compileDebugAndroidTestKotlin`: ✅ **BUILD SUCCESSFUL**. Los tests instrumentados compilan, incluido `AlineacionReactivaTest` adaptado (D-01); para ejecutarlos hace falta un emulador.

Ningún error obligó a quitar funcionalidad.

### 2 · Tests unitarios (`./gradlew testDebugUnitTest`)

✅ **91 tests, 91 pasan, 0 fallos, 0 omitidos.** No hubo que arreglar ni adaptar ningún test.

| Clase | Tests | Origen |
|---|---|---|
| ExampleUnitTest | 1 | existente |
| DerivarAlineacionEnVivoTest | 11 | existente |
| FormacionesTest | 7 | existente |
| IndicadoresJugadorEnVivoTest | 6 | existente |
| CalculadoraEstadisticasTest | 5 | existente |
| EventosValidosTest | 1 | existente |
| InicioAutomaticoPartidoTest | 8 | existente |
| DetectorConflictosTest | 7 | existente |
| LogicaInicioTest | 21 | **nuevo** (etapa 3, contadores del inicio) |
| MinutosJugadosTest | 11 | **nuevo** (etapa 6, minutos jugados) |
| RelojPartidoTest | 13 | **nuevo** (etapa 7, minuto sugerido y reloj) |

Los 46 tests que había al empezar siguen pasando.

### 3 · Inventario de las 33 pantallas

Método: además de revisar cada pantalla contra `PLAN.md` 1.1–1.33, un script extrajo **todos los textos** de las 33 `Pantalla*.kt` originales (commit `bd4c423`) y comprobó que cada uno siga en el código actual del módulo (1.008 textos revisados). Los que no aparecen tal cual se revisaron uno por uno (columna "Notas").

| # | Pantalla | Textos revisados | Acciones, filtros, diálogos y mensajes | Notas |
|---|---|---|---|---|
| 1 | PantallaBienvenida (+ Introducción) | 18 | ✅ completo | — |
| 2 | PantallaLogin | 10 | ✅ completo | Las etiquetas "CORREO ELECTRÓNICO" y "CONTRASEÑA" las pone en mayúsculas `CampoSportPro`. Nuevo: mostrar u ocultar la contraseña (7.5) |
| 3 | PantallaRecuperar | 7 | ✅ completo | Etiqueta en mayúsculas por `CampoSportPro` |
| 4 | PantallaRegistro | 30 | ✅ completo | Etiquetas en mayúsculas por `CampoSportPro`. Nuevo: mostrar u ocultar la contraseña |
| 5 | PantallasInicio (4 roles + Cuenta pendiente) | 33 | ✅ completo: ADM 12, DT 9, JUG 9, PAD 10 destinos | "PANEL PRINCIPAL" se reemplazó por el título del panel de cada rol (se mantiene en Cuenta pendiente) |
| 6 | PantallaAcademias | 20 | ✅ completo | — |
| 7 | PantallaEquipos | 29 | ✅ completo | Nuevo: filtro Todos / Activos / Inactivos |
| 8 | PantallaJugadores | 37 | ✅ completo | "Foto elegida ✓ (cambiar)" → ícono + "Foto elegida (cambiar)". Nuevo: búsqueda y filtro |
| 9 | PantallaCuentasJugadores | 13 | ✅ completo | — |
| 10 | PantallaMisJugadores | 22 | ✅ completo | — |
| 11 | PantallaVinculaciones | 15 | ✅ completo | — |
| 12 | PantallaMensualidades | 37 | ✅ completo | "+ Registrar mensualidad" → ícono + "Registrar mensualidad" |
| 13 | PantallaEjercicios | 26 | ✅ completo | — |
| 14 | PantallaEntrenamientos | 39 | ✅ completo | — |
| 15 | PantallaAsistencias | 31 | ✅ completo | Nuevo: filtro del historial |
| 16 | PantallaPartidos | 17 | ✅ completo | — |
| 17 | PantallaCrearPartido | 18 | ✅ completo | — |
| 18 | PantallaEditarPartido | 19 | ✅ completo | — |
| 19 | PantallaDetallePartido | 44 | ✅ completo | El marcador "a–b" ahora es `MarcadorLed`. Se quitó la campana decorativa "🔔 (editar)", que no tenía acción |
| 20 | PantallaConvocatoria | 27 | ✅ completo | Nuevo: filtro Todos / Disponibles / Pendientes |
| 21 | PantallaMisConvocatorias | 23 | ✅ completo | — |
| 22 | PantallaAlineacion | 56 | ✅ completo | — |
| 23 | PantallaPartidoEnVivo | 53 | ✅ completo, HU-16 intacta | El marcador "a–b" ahora es `MarcadorLed`. Nuevo (7.2): minuto sugerido, − / +, 2T local, reloj, mini cancha, "Más tipos" y mensaje honesto offline |
| 24 | PantallaMisPartidos | 22 | ✅ completo | — |
| 25 | PantallaSeguimientoPartido | 34 | ✅ completo | — |
| 26 | PantallaEstadisticas | 54 | ✅ completo | Nuevo (7.3): minutos, goles por fecha, último partido, foto y compartir cromo |
| 27 | PantallaComunidad (+ pruebas, hojas, formularios) | 29 | ✅ completo | La reacción y los chips de visibilidad se rearmaron (6.10.6) con los mismos datos. Nuevo: número de reportes pendientes en el escudo |
| 28 | PantallaPanelModeracion | 28 | ✅ completo | — |
| 29 | PantallaAnuncios | 27 | ✅ completo | "CLUB DEPORTIVO" era la línea de marca de la cabecera decorativa (ver PENDIENTES) |
| 30 | PantallaNotificaciones | 10 | ✅ completo | Ícono por tipo (D-17) |
| 31 | PantallaPlanes | 38 | ✅ completo | — |
| 32 | PantallaResumenPartido | 103 | ✅ completo | — |
| 33 | PantallaCatalogoEventos | 39 | ✅ completo | "ADMINISTRACIÓN" era la línea de marca de la cabecera decorativa (ver PENDIENTES) |

**Resultado: en las 33 pantallas no falta ninguna acción, filtro, diálogo ni mensaje.** Lo único que ya no se ve son textos de las cabeceras decorativas sin acción (detallados en `PENDIENTES.md`).

### 4 · Limpieza

Barrido de todo `ui/`:

- **Emojis y flechas como íconos**: se corrigieron los últimos 2: el "✓" de un mensaje de `ResumenPartidoViewModel` y el parámetro `simbolo = "▰"` que ya no se dibujaba en `BotonTipoTarjeta` (quitado). Aparte, el color `0xFFD7E5EC` de `PantallaPartidos` pasó al token `TokenOscuroTexto2`. Quedan a propósito, porque son contenido y no íconos: "● PRUEBA ABIERTA" (texto exigido por la 6.10.5), "● EN VIVO", "Versión 1 → 2" en la auditoría, "Ajustes › Sistema › …", las estrellas ★☆ de la evaluación y "motivo ×3" en moderación. `simboloEvento()` (pública, con emojis) se conserva por la regla 2, pero ninguna pantalla la usa. Las reacciones ❤️ 👏 💪 se mantienen.
- **Textos de menos de 12 sp**: 0.
- **`FontFamily.Serif` / `FontFamily.Monospace`**: 0.
- **`Color(0x…)` fuera del tema**: 0 en el código de las pantallas. Solo quedan las declaraciones redirigidas a tokens (con su comentario `// antes …`) y `colorEstadoOscuro` de Mensualidades, que ya no se usa.
- **Restos de tablet**: 0 (`screenWidthDp`, `WindowSizeClass` y similares). El único `BoxWithConstraints` es el de la cancha de Alineación (tamaño, no diseño).

### 5 · Verificación después de corregir

`./gradlew assembleDebug testDebugUnitTest`: ✅ BUILD SUCCESSFUL, **91/91 tests**. `compileDebugAndroidTestKotlin`: ✅.

### Pendientes

Ver `docs/diseno/PENDIENTES.md`. **Ninguno rompe una regla de oro.** Hay 9 pendientes visuales u opcionales: el selector de equipo en las cabeceras, la caja "RESPONDIERON x/y", los colores en la cancha de Alineación, el cromo grande en Mis jugadores, tickets en Cuentas y Vinculaciones, el tema del `DatePickerDialog` nativo, la duración del 1er tiempo (45 min) para revisar con Italo, el conteo de jugadores por equipo, y la revisión visual en claro y oscuro y los tests instrumentados en un emulador.


---

## Etapa 8 · Barra de navegación inferior y Perfil (2026-11-25)

Cambio de alcance aprobado: se actualizaron la regla de oro 5 y la 7.6 de `ESPECIFICACION.md` y las "Reglas de trabajo" de `PLAN.md`. Sin commits.

- **Barra** (`ui/navegacion/BarraNavegacionInferior.kt`, nuevo): estilo de las imágenes 01 y 05 a 09, con fondo navy (`superficieOscura`), esquinas de 22 dp y margen de 16 dp (flotante y con sombra). El ítem activo va en lima con una rayita de 18×3 dp; los demás, en blanco al 62 %. Íconos de Material, etiquetas de 12 sp, `Role.Tab` y `selectableGroup` para accesibilidad. Las etiquetas largas reciben más ancho, porque el peso depende del largo del texto.
  - ADM y DT: Inicio · Academias · Partidos · Comunidad · Perfil.
  - JUG: Inicio · Entrenamientos · Partidos · Comunidad · Perfil.
  - PAD: Inicio · Mis jugadores · Partidos · Comunidad · Perfil.
  - Sin barra si no hay sesión o si la cuenta está pendiente.
- **NavegacionApp**: guarda el perfil de la sesión (`perfilActual`), que se carga al abrir la app, al iniciar sesión y al registrarse, y se borra al cerrar sesión, para saber qué pestañas lleva la barra.
- **Perfil** (`ui/perfil/PantallaPerfil.kt` y `PerfilViewModel.kt`, nuevos; `Rutas.PERFIL = "perfil"`):
  - Es de solo lectura: muestra nombre, correo, rol (`nombreRol`), academia y estado de la cuenta (`ChipEstado`), con `CabeceraCancha` y un avatar con las iniciales.
  - La academia se obtiene con lecturas que ya existen: para ADM y DT, `listarAcademias`; para JUG y PAD, la academia de los jugadores de `listarJugadoresConsulta`.
  - "Cambiar contraseña" usa `RecuperarViewModel.enviar()` con el correo de la cuenta, el mismo envío de Recuperar, con sus mensajes.
  - "Cerrar sesión" recibe el mismo callback `cerrarSesion` que el inicio.
- Compilación (`./gradlew assembleDebug`): ✅ BUILD SUCCESSFUL al primer intento.

## Etapa 8b · Barra en toda la sesión, corrección de Inicio y pizarra sin duplicados (2026-11-25)

Cambios aprobados sobre la barra y el inicio. Se actualizaron la regla 5 y el punto de Cerrar sesión (6.2) de `ESPECIFICACION.md` y las "Reglas de trabajo" de `PLAN.md`. **No se compiló ni se corrieron tests**, como se pidió. Sin commits.

### 1 · Cerrar sesión

Se quitó el botón "Cerrar sesión" del final del inicio en los 4 roles. Queda en el menú del avatar y en la pantalla Perfil. El de Cuenta pendiente se mantiene, porque esa pantalla no tiene barra ni avatar.

### 2 · Bug: "Inicio" en la barra no volvía al inicio

- **Causa**: todas las pestañas usaban `popUpTo(inicio) { saveState = true }` + `restoreState = true`. Al tocar Inicio, Navigation restauraba el estado guardado de la "pestaña" inicio, que incluía la pantalla de la que se venía (por ejemplo, Partidos en vivo), así que no se veía el inicio.
- **Corrección** (`NavegacionApp.irAPestana`):
  - Si ya se está en esa pestaña, no se hace nada (no se recrea la pantalla).
  - **Inicio**: `popBackStack(rutaInicio, inclusive = false)`. Si devuelve `false` porque el inicio no está en la pila, navega a `rutaInicio` limpiando toda la pila. Para limpiarla se usa `popUpTo(navController.graph.id) { inclusive = true }`, igual que el `irA` que ya existía: con `popUpTo(0)`, Navigation no encuentra el destino 0 y no saca nada de la pila.
  - **Otras pestañas**: `popUpTo(rutaInicio)` + `launchSingleTop = true`, sin `saveState` ni `restoreState`. Si el inicio no estuviera en la pila (caso raro), primero se navega al inicio limpiando la pila y después a la pestaña.
- **Casos revisados en el código** (I = inicio; A y B = pestañas; X e Y = pantallas internas; MP = Partidos; S = seguimiento):

| Desde | Toca | Pila resultante | Atrás del celular |
|---|---|---|---|
| Inicio [I] | Inicio | [I] (no hace nada) | sale de la app |
| Inicio [I] | Pestaña B | [I, B] | vuelve a [I] |
| Pestaña A [I, A] | Pestaña B | [I, B] (A se saca) | vuelve a [I] |
| Pestaña A [I, A] | A otra vez | [I, A] (no hace nada) | vuelve a [I] |
| Pestaña A [I, A] | Inicio | [I] | sale de la app |
| Interna [I, X, Y] (por ejemplo, Academias → Partidos → Detalle) | Inicio | [I] | sale de la app |
| Interna [I, X, Y] | Pestaña B | [I, B] | vuelve a [I] |
| Interna de la misma sección [I, A, Y] | A | [I, A] (A desde arriba) | vuelve a [I] |
| Partido en vivo abierto desde el inicio [I, MP, S] | Inicio | [I] (este era el bug) | sale de la app |

Las flechas de volver de cada pantalla (`popBackStack`) no cambian.

### 3 · Pizarra sin las fichas de la barra

| Rol | Se quitaron | Fichas que quedan | Columnas |
|---|---|---|---|
| ADM | Gestionar academias, Comunidad | Cuentas de jugadores, Vinculaciones, Mensualidades, Biblioteca de ejercicios, Asistencias, Estadísticas, Anuncios del club (7) | 4 |
| DT | Gestionar academias, Comunidad | Entrenamientos, Asistencias, Biblioteca de ejercicios, Estadísticas, Anuncios del club (5) | 3 |
| JUG | Mis entrenamientos, Comunidad | Mis convocatorias, Mi asistencia, Mis mensualidades, Estadísticas, Anuncios del club (5) | 3 |
| PAD | Mis jugadores, Comunidad | Mensualidades, Entrenamientos, Historial de asistencia, Convocatorias, Estadísticas, Anuncios del club (6) | 3 |

Se cambió la cantidad de columnas para que las filas queden parejas. La tarjeta EN VIVO, la campana y la tarjeta de Planes (ADM) se quedan. Los callbacks públicos de `InicioAdministrador`, `InicioEntrenador`, `InicioJugador` e `InicioApoderado` no cambian de firma; `onGestionarAcademias` y `onComunidad` siguen como parámetros (regla 2). En PAD, la sección "Mis jugadores" del inicio, con "Ver todos" y los cromos, sigue abriendo Mis jugadores.

**Todos los destinos siguen accesibles:**

| Rol | Destinos | Desde |
|---|---|---|
| ADM (12) | Academias · Comunidad | barra |
| | Partidos en vivo | marcador EN VIVO y barra (Partidos) |
| | Notificaciones | campana |
| | Planes | tarjeta bajo la pizarra |
| | Cuentas, Vinculaciones, Mensualidades, Ejercicios, Asistencias, Estadísticas, Anuncios | pizarra |
| DT (9) | Academias · Comunidad | barra |
| | Partidos en vivo | marcador y barra |
| | Notificaciones | campana |
| | Entrenamientos, Asistencias, Ejercicios, Estadísticas, Anuncios | pizarra |
| JUG (9) | Entrenamientos · Comunidad | barra |
| | Partidos en vivo | marcador y barra |
| | Notificaciones | campana |
| | Convocatorias, Asistencia, Mensualidades, Estadísticas, Anuncios | pizarra |
| PAD (10) | Mis jugadores · Comunidad | barra (y la sección "Mis jugadores") |
| | Partidos en vivo | marcador y barra |
| | Notificaciones | campana |
| | Mensualidades, Entrenamientos, Asistencia, Convocatorias, Estadísticas, Anuncios | pizarra |

Además, todos los roles tienen Perfil en la barra.

### 4 · Barra en todas las pantallas de la sesión

- **Un único `Scaffold`** alrededor del `NavHost`, con la barra en `bottomBar`. El `NavHost` recibe `padding(relleno)` y `consumeWindowInsets(relleno)`:
  - Ninguna pantalla queda tapada: el contenido, los botones de acción inferiores (Guardar, Confirmar alineación, Publicar, etc.) y los FAB de los `Scaffold` de cada pantalla quedan por encima de la barra.
  - Las pantallas no repiten el margen de la barra de gestos del sistema, porque la barra ya lo cubre.
  - `contentWindowInsets = WindowInsets(0, 0, 0, 0)`: arriba no cambia nada (cada pantalla sigue manejando la barra de estado).
  - Las hojas inferiores (`ModalBottomSheet`) y los diálogos se dibujan encima de todo, como antes.
- **Se oculta** en bienvenida, introducción, login, registro, recuperar y cuenta pendiente (`RUTAS_SIN_BARRA`), y **mientras el teclado está abierto** (`WindowInsets.isImeVisible`). Así no tapa los campos y la pantalla recupera ese espacio.
- **Pestaña resaltada** (`rutaPestanaActiva`, función pura):
  - partido/* → Partidos.
  - equipos, jugadores, partidos de una academia (partidos/…) y catálogo (academias/…) → Academias.
  - comunidad y moderación → Comunidad.
  - perfil → Perfil.
  - el resto (mensualidades, ejercicios, asistencias, anuncios, estadísticas, notificaciones, planes, vinculaciones, etc.) → Inicio.
  - Para JUG, entrenamientos → Entrenamientos; para PAD, mis jugadores → Mis jugadores. Si el rol no tiene la pestaña de la sección, se resalta Inicio.
- **Test nuevo** (no se corrió): `app/src/test/.../navegacion/NavegacionBarraTest.kt`, con 11 casos sobre dónde se muestra la barra y qué pestaña se resalta en cada ruta y rol.

### Archivos de esta etapa (8 y 8b)

- **Nuevos**: `ui/navegacion/BarraNavegacionInferior.kt`, `ui/perfil/PantallaPerfil.kt`, `ui/perfil/PerfilViewModel.kt` y `test/.../navegacion/NavegacionBarraTest.kt`.
- **Modificados**: `ui/navegacion/NavegacionApp.kt`, `ui/navegacion/Rutas.kt` (+ `PERFIL`), `ui/inicio/PantallasInicio.kt`, `docs/diseno/ESPECIFICACION.md` y `docs/diseno/PLAN.md`.

### Para verificar en un dispositivo

- Correr `./gradlew assembleDebug testDebugUnitTest`: esta etapa no se compiló.
- Probar en un emulador la tabla de navegación de arriba, que la barra desaparece al abrir el teclado y que los botones inferiores de Alineación, Convocatoria, formularios y Partido en vivo quedan por encima de la barra.


---

## Etapa 9 · Vista previa de Comunidad en el inicio (2026-11-25)

Funcionalidad aprobada. Quedó anotada en la sección 7.1 de `ESPECIFICACION.md`. Sin commits.

### Qué se agregó

- **Sección "Comunidad"** en el inicio de los 4 roles (no en Cuenta pendiente), debajo de la pizarra; en el ADM, debajo de "Planes y ofertas" (`ui/inicio/SeccionComunidadInicio.kt`, nuevo):
  - Encabezado "Comunidad" con "Ver todo" y una flecha como ícono de Material (no como texto, por la regla de los íconos), que abre `Rutas.COMUNIDAD`.
  - Hasta **3 tarjetas compactas** con el kit: avatar con iniciales, nombre, chip de rol (ADM/DT/JUG/PAD, el mismo texto que en Comunidad), tiempo relativo (`tiempoRelativo`, el mismo de Comunidad: "Hace 5 min"), texto en máximo 2 líneas, miniatura de 56 dp si hay imagen (`ImagenRemota`), total de reacciones con los emojis presentes y número de comentarios.
  - Tocar una tarjeta abre Comunidad. Desde el inicio no se reacciona ni se comenta.
  - Las tarjetas nuevas entran con `fadeIn` + `expandVertically` de 300 ms. Las que ya estaban no vuelven a animarse, porque cada tarjeta lleva como clave el id de su publicación.
  - **Vacío**: "Aún no hay publicaciones" con el botón "Publicar", que abre Comunidad.
  - **Error**: la sección no se muestra y el resto del inicio sigue igual.

### Tiempo real

- `RepositorioPublicaciones.escucharRecientes(uid)` (función nueva, solo lectura): `addSnapshotListener` sobre `publicaciones` con `orderBy("fechaCreacion", DESCENDING).limit(20)`. Cada instantánea pasa por el mismo filtro del feed "Para ti". El listener se quita al cancelar el `Flow` (`awaitClose { registro.remove() }`).
  - **Ventana de 20**: si en las 20 más recientes de toda la app quedan menos de 3 visibles para el usuario (por ejemplo, porque otras academias publicaron mucho), se completa **una sola vez** con `listarFeed`, sin tiempo real. Así no aparece "Aún no hay publicaciones" cuando sí hay publicaciones más antiguas. Si una publicación está en las dos listas, vale la versión en tiempo real.
  - Las reglas de Firestore no cambian: `publicaciones` ya se puede leer con `activo()`, y ordenar por un solo campo no necesita un índice nuevo.
- `InicioViewModel.comunidad`: `StateFlow<ComunidadInicio>` (Cargando / Oculta / Lista) con `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Cargando)`. Si el `Flow` falla, `.catch` emite `Oculta`.
- La pantalla lo recoge con `collectAsStateWithLifecycle`. Al salir del inicio o pasar la app a segundo plano deja de haber suscriptores y, a los 5 s, se cierra el listener. El margen evita cerrarlo y reabrirlo al girar la pantalla. Cuenta pendiente no usa la sección, así que nunca abre el listener.

### Privacidad: el mismo filtro de "Para ti"

- El filtro estaba dentro de `RepositorioPublicaciones.listarFeed`, no en `ComunidadViewModel`. Se extrajo **tal cual** a la función pura `filtrarFeedComunidad(publicaciones, pertenencia, bloqueados)` (`datos/repositorio/FiltroFeedComunidad.kt`, nuevo):
  - quita las repetidas;
  - aplica `puedeVerPublicacion`: visibilidad Equipo, Academia o Comunidad, sin publicaciones ELIMINADAS ni OCULTAS;
  - quita las de usuarios bloqueados en ambos sentidos;
  - ordena de la más reciente a la más antigua.
- `listarFeed` ahora llama a esa función con los mismos datos. **El feed de Comunidad no cambia.** Las dos consultas siguen igual, y también la pertenencia y los bloqueos.
- `LogicaInicio.publicacionesRecientes` (las 3 primeras) y `LogicaInicio.emojisReacciones` son funciones puras nuevas.

### Tests

`app/src/test/.../comunidad/FiltroFeedComunidadTest.kt`, nuevo, con 12 casos:

- Comunidad de otra academia sí se ve.
- Academia solo si el usuario pertenece a ella.
- Equipo si es su equipo o si administra la academia.
- Sin eliminadas ni ocultas.
- Sin bloqueados.
- Una repetida queda una vez, con la primera versión.
- Una repetida oculta en la versión nueva no aparece.
- El orden.
- Lista vacía.
- Las 3 más recientes.
- Con menos de 3 se muestran las que hay.
- Los emojis presentes.

### Compilación y tests

- `./gradlew assembleDebug testDebugUnitTest`: ✅ **BUILD SUCCESSFUL**, **114 tests, 114 pasan, 0 fallos**.
- Los 114 tests son: los 91 anteriores, los 11 de `NavegacionBarraTest` de la etapa 8b y los 12 nuevos.
- Esta ejecución también **verifica la etapa 8b** (barra en toda la sesión), que se había dejado sin compilar.

### Archivos

- **Nuevos**: `datos/repositorio/FiltroFeedComunidad.kt`, `ui/inicio/SeccionComunidadInicio.kt` y `test/.../comunidad/FiltroFeedComunidadTest.kt`.
- **Modificados**:
  - `datos/repositorio/RepositorioPublicaciones.kt`: `listarFeed` usa el filtro compartido; nueva `escucharRecientes`.
  - `ui/inicio/InicioViewModel.kt`: `ComunidadInicio` y `comunidad`; nuevo parámetro con valor por defecto en el constructor.
  - `ui/inicio/LogicaInicio.kt`.
  - `ui/inicio/PantallasInicio.kt`: la sección va debajo de la pizarra; `onComunidad` llega a `PantallaInicioRol`.
  - `docs/diseno/ESPECIFICACION.md` (7.1).

### Para verificar en un dispositivo

- Que una publicación nueva aparezca sola en el inicio con la animación.
- Que, desde la consola de Firebase, el listener se cierre al salir del inicio.
- La miniatura de imagen.
