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
