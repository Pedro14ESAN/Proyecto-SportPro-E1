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
