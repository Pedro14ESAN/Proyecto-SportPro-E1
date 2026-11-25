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
