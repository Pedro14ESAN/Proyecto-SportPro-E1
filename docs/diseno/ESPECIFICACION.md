# SportPro · Rediseño "Línea de cal": especificación para implementar

Este documento es la fuente de verdad del rediseño. Las imágenes de `docs/diseno/img/` muestran cómo debe verse la app. **Si una imagen y este texto no coinciden, manda este texto.**

Rama de trabajo: `feature/nahim-diseno-linea-de-cal` (creada desde `main` en `f45daba`).

---

## 1. Reglas de oro (no negociables)

1. **No se quita ninguna funcionalidad.** Todo botón, pantalla, acción, validación, mensaje de error, diálogo, filtro y regla de negocio que existe hoy debe seguir existiendo y funcionando igual. El rediseño solo cambia el aspecto y **agrega** lo listado en la sección 7.
2. **No se renombran** funciones públicas, ViewModels, repositorios, rutas (`Rutas.kt`), modelos ni campos de Firestore. Tampoco se cambia la estructura de las colecciones.
3. **No se tocan** `firestore.rules`, `functions/`, la lógica de sincronización offline (`datos/sincronizacion`, `RepositorioSincronizacion`), la generación del resumen con IA ni las reglas de negocio de los repositorios, salvo lo indicado de forma explícita en la sección 7.
4. **Los textos de los flujos se conservan**: títulos de pantalla, etiquetas de botones y mensajes. Solo pueden cambiar si la sección 6 o la 7 lo indica.
5. **Barra de navegación inferior: solo como atajo adicional** (cambio de alcance aprobado el 2026-11-25). Estilo de las imágenes 01 y 05 a 09: navy, esquinas redondeadas, flotante con margen; el ítem activo en lima con una rayita debajo; íconos de Material. Pestañas: ADM y DT: Inicio · Academias · Partidos (`MIS_PARTIDOS`) · Comunidad · Perfil. JUG: Inicio · Entrenamientos · Partidos · Comunidad · Perfil. PAD: Inicio · Mis jugadores · Partidos · Comunidad · Perfil. Solo se muestra en esas pantallas principales; se oculta en bienvenida, login, registro, recuperar, cuenta pendiente y en todas las pantallas internas. Al tocar una pestaña se navega con `launchSingleTop`, `restoreState` y `popUpTo` del inicio con `saveState`. No se quita ningún botón del inicio ni ninguna ruta. Se agrega la pantalla Perfil (`Rutas.PERFIL = "perfil"`), de solo lectura, con "Cambiar contraseña" (mismo envío de correo que Recuperar) y "Cerrar sesión" (misma lógica que el inicio).
6. **Commits pequeños por etapa y por módulo**, con mensajes del tipo `style(HU-08): ...` o `feat(HU-14): ...`. El proyecto debe compilar después de cada commit. No hacer push sin que el usuario lo pida.
7. **Pruebas**: los tests existentes en `app/src/test` y `app/src/androidTest` deben seguir pasando. Toda lógica pura nueva (minuto sugerido, minutos jugados, contadores) lleva su test unitario.
8. Si algo de este documento obligara a romper una regla de oro, **no se hace**: se deja anotado en `docs/diseno/PENDIENTES.md` y se sigue.

---

## 2. Imágenes de referencia

| Archivo | Qué muestra |
|---|---|
| `01_estilo_claro.png` | Concepto: inicio, partido en vivo y cromo del jugador (modo claro) |
| `02_estilo_oscuro.png` | Las mismas pantallas en modo oscuro |
| `03_inicio_roles.png` | Inicio de ADM, DT, JUG y PAD (sin barra inferior) |
| `04_inicio_adm_tablet.png` | Panel del ADM en tablet y las 3 ideas de mejora. **La vista de tablet ya no se implementa** (ver 6.2); la imagen queda solo como referencia de estilo |
| `05_entrenamientos_ejercicios.png` | Entrenamientos, planificar entrenamiento y ejercicios |
| `06_asistencia_mensualidades.png` | Registrar asistencia, historial del jugador y mensualidades |
| `07_partidos_convocatoria_alineacion.png` | Partidos, convocatoria y alineación |
| `08_academia_jugadores_vinculacion.png` | Equipos de la academia, jugadores y mis jugadores (PAD) |
| `09_acceso_comunidad_resumen.png` | Login, comunidad (versión anterior) y resumen con IA |
| `10_comunidad_detalle.png` | Comunidad vista como ADM (versión más reciente; ver correcciones en 6.10) |

Los nombres, números y datos de las imágenes son de ejemplo. En la app se muestran los datos reales.

---

## 3. Tokens de diseño

### 3.1 Colores · modo claro

| Token | Hex | Uso |
|---|---|---|
| `navy` | `#0F2438` | Texto principal, pizarra, botones oscuros |
| `navy2` | `#16354D` | Superficies oscuras secundarias |
| `verde` | `#1B9A5C` | Color primario, césped, estados OK |
| `verdeCesped` | `#178A52` | Franja alterna del césped |
| `lima` | `#A8F06A` | Acento: LED, botón principal, selección |
| `crema` | `#F7F7F2` | Fondo de pantalla |
| `blanco` | `#FFFFFF` | Tarjetas |
| `grisTexto` | `#5B6B73` | Texto secundario |
| `amarillo` | `#F2C94C` | Tarjeta amarilla, talón de partidos, borde del cromo |
| `rojo` | `#D9534F` | Error, ausente, vencido, tarjeta roja |
| `naranja` | `#E8913A` | Pendiente de sincronización, advertencias |
| `ledFondo` | `#0B1A28` | Fondo del marcador LED |

Chips de estado (fondo / texto): OK `#E1F3E8` / `#16794A` · pendiente `#FDE9D4` / `#A55A12` · error `#FBE3E2` / `#B23A36` · apagado `#ECEEEA` / `#6B7A80` · amarillo `#FFF3CC` / `#8A6A00` · neutro `#E3EAF0` / `navy`.

### 3.2 Colores · modo oscuro

| Token | Hex |
|---|---|
| fondo | `#0D1E2D` |
| superficie | `#16354D` |
| borde | `#2B4D66` |
| césped noche | `#146B42` / `#115C39` |
| texto | `#E8EEF2` |
| texto 2 | `#9FB3C1` |
| lima, amarillo, naranja, rojo | Iguales que en claro |

Chips en oscuro: OK `#1E4A3A` / `#A8F06A` · pendiente `#4A3420` / `#FFC58A`. No se usa negro puro. El LED lleva un resplandor lima suave.

### 3.3 Tipografías (ya están en `app/src/main/res/font/`, licencia OFL)

| Rol | Archivos | Uso |
|---|---|---|
| Títulos | `bricolage_medium`, `bricolage_bold`, `bricolage_extrabold` | Títulos de pantalla y de sección, nombres en el cromo |
| Marcador y números | `doto_bold`, `doto_black` | **Solo números grandes (18 sp o más)**: marcador, minutos, horas de los talones, contadores, porcentajes. Nunca en texto corrido |
| Cuerpo | `instrument_sans_regular`, `_medium`, `_semibold`, `_bold` | Todo el texto normal |
| Etiquetas | `dm_mono_regular`, `dm_mono_medium` | Etiquetas en MAYÚSCULAS con espaciado (por ejemplo "MINUTO", "EN VIVO") |

Ningún texto debe medir menos de **12 sp** (hoy hay 38 textos de 9 a 10 sp). Se reemplazan todos los `FontFamily.Serif` y `FontFamily.Monospace` por las familias del tema.

### 3.4 Formas y espaciado

Radios: tarjetas 14–16 dp, pizarra 18 dp, chips y botones píldora 999 dp, botón principal 14 dp, cabecera de césped 24 dp en las esquinas inferiores. Márgenes laterales de 16 dp. Las sombras de las tarjetas son suaves y navy.

### 3.5 Íconos

Se usan íconos de Material (Rounded). Se agrega la dependencia `androidx.compose.material:material-icons-extended` (si no resuelve con el BOM, fijar la versión 1.7.8). **Se reemplazan todos los emojis y flechas de texto que hacen de ícono** (hoy hay unos 174: ← → ⚽ 🔔 ✓ ⚠ 📊 👥 🌐 🛡 📅 🏛 🏆 🏃 🚫 🗑 🔄 📍 💬 💎 🏟 ✨). Excepciones: las reacciones ❤️ 👏 💪 (`TipoReaccion.emoji`), que son contenido y se quedan como emoji. El campo `TipoNotificacion.emoji` se conserva en el modelo, pero en la UI se muestra un ícono.

---

## 4. Tema (etapa 1)

- `ui/theme/Color.kt`: todos los tokens de la sección 3, en dos objetos (claro y oscuro) o en un `CompositionLocal` con la paleta extendida.
- `ui/theme/Type.kt`: `FontFamily` para las 4 familias y una `Typography` completa.
- `ui/theme/Theme.kt`: **`dynamicColor = false`**. `lightColorScheme` y `darkColorScheme` con los tokens; el esquema se elige con `isSystemInDarkTheme()`. También se estilizan `OutlinedTextField`, `DatePickerDialog`, `FilterChip`, `Switch`, `Checkbox`, `AlertDialog` y `CircularProgressIndicator` mediante el esquema, para que ninguno tome colores del fondo de pantalla del celular.
- Las paletas privadas que existen hoy (`EstiloPartido.kt`, `ComponentesSportPro.kt`, `EstiloSincronizacion.kt` y los `private val ... = Color(0x...)` de unos 17 archivos) **se redirigen a los tokens** cambiando solo la línea de la declaración. Así cada pantalla cambia de color sin tocar su lógica.

## 5. Kit de componentes (etapa 2)

Se crea en `ui/comun/` (por ejemplo `LineaDeCal.kt`), con `@Preview` en claro y en oscuro:

| Componente | Descripción |
|---|---|
| `CabeceraCancha` | Franjas de césped horizontales, rectángulo y círculo central con "líneas de cal" (blanco 85 %), esquinas inferiores redondeadas. Recibe título, subtítulo y contenido extra |
| `MarcadorLed` | Fondo `ledFondo` con trama de puntos, números en Doto lima con resplandor, nombres de equipos en Doto blanco y etiquetas en Doto amarillo. Muestra la etiqueta "PROVISIONAL" en naranja cuando corresponde |
| `ContadorLed` | Caja LED pequeña con un número y una etiqueta mono (fila de contadores de las cabeceras) |
| `TarjetaTicket` | Tarjeta blanca con talón izquierdo separado por línea discontinua y muescas circulares. El talón muestra hora y fecha (Doto + mono). Variantes: normal, verde (entrenamiento), amarilla (partido), apagada y tachada (cancelado) |
| `FichaTactica` | Botón redondo de 44–58 dp con ícono, sombra inferior "de ficha" y etiqueta debajo. Puede llevar un contador naranja |
| `PizarraFichas` | Contenedor navy con borde discontinuo interior y cuadrícula de `FichaTactica` (3 o 4 columnas) |
| `CromoJugador` | Borde amarillo, interior navy con franjas verdes, número gigante translúcido, foto (`fotoUrl`) o silueta lima con el número, nombre, posición y pie con equipo y estado. Tamaños grande y mini |
| `ChipEstado` | Un solo componente para todos los estados (colores de 3.1) |
| `BotonPrincipal` | Botón ancho; variante lima (acción principal) y variante navy con texto lima |
| `CampoSportPro` | `OutlinedTextField` con etiqueta mono arriba, fondo blanco y borde verde al enfocar |
| `BarraSuperior` | Botón atrás circular, título Bricolage centrado y hasta 2 acciones |
| `FondoCancha` | Versión de `CabeceraCancha` para el login y la bienvenida |

---

## 6. Pantallas: cómo se ve cada una

Para cada pantalla se mantiene **todo** lo que tiene hoy y se aplican el kit y los tokens. Se siguen las imágenes indicadas.

### 6.1 Bienvenida, login, registro y recuperar (img 09)
Se usa `FondoCancha` arriba con el logo y el lema; los campos usan `CampoSportPro` y el botón principal es lima. El registro y la recuperación siguen el mismo estilo, con todos sus campos y validaciones actuales.

### 6.2 Inicio de los 4 roles (img 03)
- Cabecera de césped con el rol en mono, el saludo con el **nombre real** del perfil, la campana (Notificaciones) y el avatar con las iniciales.
- Debajo, el `MarcadorLed` del partido en curso (ver 7.1). Al tocarlo se abre la misma acción que hoy tiene "Partidos en vivo".
- **Pizarra**: **todos** los botones actuales del rol, como fichas, con el mismo destino. ADM: 12 destinos; DT: 9; JUG: 9; PAD: 10 (ver `PantallasInicio.kt`). "Notificaciones" pasa a la campana y "Partidos en vivo" al marcador; "Planes y ofertas" (ADM) queda como tarjeta debajo de la pizarra.
- **Cerrar sesión**: se abre desde el avatar (menú con el nombre y "Cerrar sesión"). Además se mantiene un botón "Cerrar sesión" discreto al final de la pantalla, para no perderlo.
- `PantallaCuentaPendiente`: mismo estilo, mismo texto.
- **Pantallas anchas**: **no hay vista de tablet**. En cualquier tamaño se usa el mismo diseño que en celular. En pantallas anchas, el contenido va centrado con un ancho máximo de unos 600 dp (`Modifier.widthIn(max = 600.dp)`), para que no se estire.

### 6.3 Academias y equipos (img 08)
`CabeceraCancha` con contadores; cada equipo es una tarjeta con escudo de iniciales y su categoría. Los equipos inactivos se ven apagados. Se mantienen crear, editar, inactivar y la gestión de entrenadores.

### 6.4 Jugadores, cuentas de jugadores, mis jugadores y vinculaciones (img 08)
- Jugadores: cuadrícula de `CromoJugador` mini. Se mantienen la búsqueda, el alta, la edición y los datos privados según permisos.
- Mis jugadores (PAD): un `CromoJugador` grande por jugador vinculado y sus solicitudes como tickets (Pendiente, Activa, Inactiva). Se mantiene el botón de solicitar vinculación con su flujo actual.
- Cuentas de jugadores y vinculaciones (ADM): tickets con chips de estado y los mismos botones de aprobar y rechazar.

### 6.5 Mensualidades (img 06)
Un ticket por mensualidad, con el periodo en el talón y el `ChipEstado` Pagado, Pendiente o Vencido. **No se muestran montos** (el modelo no los tiene y RN-03 lo prohíbe). Se mantienen las acciones actuales.

### 6.6 Ejercicios y entrenamientos (img 05)
- Ejercicios: lista con una `FichaTactica` pequeña según el objetivo, el chip del objetivo y la duración en Doto. Se mantiene la búsqueda actual.
- Entrenamientos: tickets con hora y fecha en el talón. Los cancelados se ven apagados y tachados y se conservan en el historial. El cambio de horario se muestra como nota naranja usando el historial que ya existe.
- Planificar: campos con `CampoSportPro`; caja LED "EJERCICIOS 95/90" con barra y la advertencia actual cuando se excede la duración; los ejercicios en orden con número en Doto y los botones de subir, bajar y quitar actuales.

### 6.7 Asistencia (img 06)
Ticket de la sesión, `ContadorLed` de presentes, ausentes y justificados, y por jugador tres fichas **P / A / J** (PRESENTE, AUSENTE y JUSTIFICADO, que son los estados reales). El historial usa un cromo mini, el porcentaje en LED y filas con un borde de color por estado.

### 6.8 Partidos, crear, editar, detalle, convocatoria, mis convocatorias y alineación (img 07)
- Partidos: tickets con talón amarillo; el partido en curso va en un ticket oscuro con mini LED. Se mantienen todos los estados (Programado, En curso, Finalizado y Cancelado).
- Convocatoria: ticket del partido, caja LED "RESPONDIERON x/y" con barra y los jugadores con check de convocado y chip de disponibilidad (Disponible, No disponible y Pendiente).
- Alineación: cancha vertical con líneas de cal y jugadores como fichas con número. El arquero va en amarillo y el jugador no disponible en naranja. Se mantienen los formatos (11, 9, 7 y 5 vs.), la banca de suplentes, la alerta de no disponible y "Confirmar alineación".

### 6.9 Partido en vivo y seguimiento (img 01 y 02)
Se usa el `MarcadorLed` grande, el aviso naranja de "Sin conexión" con "Reintentar", la cronología con línea discontinua, el minuto en Doto y el chip de sincronización por evento. Todos los estados de la HU-16 y la HU-17 se mantienen exactamente como están. Las funcionalidades nuevas de esta pantalla están en 7.2.

### 6.10 Comunidad (img 10, con estas correcciones obligatorias)
1. Las pestañas son las actuales: **Para ti, Academias y Pruebas**.
2. Arriba van **Bloqueados** (ícono `block`) y **Moderar** (ícono `shield`, solo ADM). El ícono de bandera se elimina. El número sobre el escudo **no existe hoy**: se agrega con una consulta de reportes pendientes de su academia. Si no se llega a implementar, el escudo va sin número.
3. Las publicaciones y las pruebas usan **el mismo molde de tarjeta**: avatar o logo, nombre y "Aviso oficial" (ícono `verified`) en las pruebas, y el menú ⋮.
4. **Título de la prueba**: se usa `AvisoPrueba.titulo()`, que arma "Pruebas abiertas Sub-10" si la posición es abierta o "Buscamos Arquero Sub-10". No existe un título libre.
5. **Estados de la prueba** según `estadoEfectivo()`: "● PRUEBA ABIERTA" (verde), "CERRADA" (gris) y **"VENCIDA"** (gris, con su propia etiqueta). Se muestran el chip amarillo con la fecha, los datos actuales (categoría, posición y ubicación) y el botón "Ver detalles".
6. **Reacciones**: un solo botón por tarjeta. Si el usuario reaccionó, se resalta **su** emoji y el total; si no, se muestra ♡ y el total. Junto al total se muestran los emojis presentes (por ejemplo ❤️👏💪 37), para que no parezca que son 37 corazones. Al tocarlo se abre el selector actual con ❤️ 👏 💪, el conteo de cada uno y el texto "Toca tu reacción otra vez para quitarla". No hay ícono de compartir.
7. **Menú ⋮** en cada tarjeta, que abre la hoja actual (`HojaAccionesContenido`): Reportar, Bloquear y Moderar directamente (ADM). En los contenidos propios: Editar y Eliminar. En las pruebas: Cerrar y Reabrir convocatoria (autor o ADM).
8. Los filtros de Pruebas son los actuales (Categoría, Posición, Ubicación y Fecha) y el interruptor "Mostrar cerradas y vencidas".
9. El FAB dice "+ Publicar" en Para ti y Academias, y **"+ Aviso" en Pruebas**.
10. **Espacio al final de la lista de unos 80 dp**, para que el FAB no tape el último "Ver detalles".
11. Se conservan el mensaje para quien no pertenece a una academia ("Ver pruebas"), los comentarios, la hoja de bloqueados y el panel de moderación.

### 6.11 Anuncios, notificaciones, moderación, planes, resumen y estadísticas
- Anuncios: tarjetas con ícono de megáfono y chip de audiencia, archivar igual que hoy.
- Notificaciones: lista con el ícono de cada tipo (en lugar del emoji) y las no leídas resaltadas.
- Panel de moderación: tickets por reporte con chip Pendiente o Resuelto y las mismas decisiones.
- Planes: cada plan como una "entrada" (ticket). Sigue siendo una demo, sin pagos reales.
- Resumen con IA (img 09): `MarcadorLed` final, chips de estado (Borrador, Aprobado o Rechazado), origen (IA o plantilla) y versión; el texto en una tarjeta blanca y los botones Rechazar, Editar y Aprobar con el flujo actual. Se mantienen el historial de versiones y la evaluación.
- Estadísticas (img 01): `CromoJugador` grande, contadores en Doto y lo de 7.3.

---

## 7. Funcionalidades nuevas (se agregan, no reemplazan nada)

Las marcadas con ⚠️ cambian el flujo de un compañero: se implementan en un commit aparte, para que el dueño de la HU lo revise.

### 7.1 Inicio (HU-02, HU-08, HU-10, HU-11, HU-17, HU-19)
- **Tarjeta EN VIVO**: busca el partido EN_CURSO de los equipos del usuario y escucha su marcador con la lógica existente. Si no hay partido en curso, muestra "Sin partidos en vivo · próximo: \<fecha y hora\>" (o solo "Sin partidos en vivo") y sigue abriendo Partidos en vivo.
- **Contadores en la campana**: notificaciones con `leida == false`.
- **ADM · "Requiere tu atención"**: cuentas de 12 a 17 años por aprobar, vinculaciones PENDIENTE y mensualidades VENCIDO de su academia. Cada tarjeta abre la pantalla que ya existe. Los mismos números aparecen sobre las fichas.
- **DT · "Tu jornada"**: el próximo entrenamiento PUBLICADO (usar `listarParaUsuario()`: objetivo, hora, `duracionTotalMinutos` y número de ejercicios; **no mostrar lugar**, porque `Entrenamiento` no lo guarda) y el próximo partido con "respondieron x/y" de su convocatoria.
- **JUG · "Para ti"**: convocatoria pendiente de responder (abre Mis convocatorias), porcentaje de asistencia, goles de la temporada y estado de la mensualidad.
- **PAD · "Mis jugadores"**: cromo mini de cada jugador vinculado con el estado de su mensualidad.
- Todo esto **solo lee** datos existentes. Si una consulta falla o no hay datos, esa sección se oculta y el inicio sigue funcionando.

### 7.2 Partido en vivo (HU-14, HU-16)
- ⚠️ **Minuto sugerido**: al abrir el formulario de un evento, el campo minuto viene **precargado** con `((ahora - fechaCaptura del INICIO) / 60000) + 1`, calculado en el celular sin red. Lleva botones − / + y el campo sigue siendo editable. El operador **confirma** el minuto, como pide la HU-14. Si no hay INICIO en caché, el campo queda vacío, como hoy. Debe tener test unitario.
- ⚠️ **Reloj del partido** en el LED ("1T 12:34" o "2T 36:18"): se calcula desde el INICIO. Para distinguir el segundo tiempo, se agrega el tipo **"Inicio 2T"** (código `INICIO_2T`) al catálogo por defecto. Se registra como cualquier evento (respeta la cola offline de la HU-16), se agrega a `CODIGOS_CONTROL` en `FuenteResumen` y se excluye de estadísticas, conflictos y marcador. Si no se agrega, el reloj muestra el tiempo desde el INICIO sin "1T" ni "2T".
- ⚠️ **Panel rápido sobre la mini cancha**: las fichas (Gol, Tarjeta, Falta, Cambio, Córner) abren el formulario actual con el tipo ya elegido (`abrirEvento(tipo)`). Las fichas **se leen del catálogo activo**, no se escriben fijas. Se agrega la ficha **"Más tipos"**, que lista todos los demás tipos activos, incluidos FIN y los personalizados.
- **Mensaje honesto offline**: si `confirmarOEncolar` devuelve `false`, el mensaje dice "\<Evento\> guardado en este dispositivo · se enviará al reconectar", en lugar de "registrado correctamente". Para eso, `registrarEvento` debe devolver si quedó sincronizado.
- No se cambia nada más de la HU-16: cola, pendientes, reintento, "Requiere revisión" y marcador provisional siguen igual.

### 7.3 Estadísticas del jugador (HU-18)
- **Minutos jugados**: titular desde el minuto 1 o desde que entra por un Cambio, hasta que sale por un Cambio o hasta el minuto del FIN. Solo partidos FINALIZADOS. Debe tener test unitario. Cumple RN-18 porque sale de datos registrados.
- **Goles por fecha**: un balón por gol en cada partido finalizado y "—" cuando no hubo.
- **Último partido**: resultado, goles, minutos y si fue titular.
- **Foto**: se usa `fotoUrl` si existe; si no, la silueta con el número.
- **Compartir el cromo como imagen** (opcional, al final): se genera un bitmap del cromo y se abre `Intent.ACTION_SEND` con un `FileProvider`. Solo lo ve quien ya puede ver a ese jugador.

### 7.4 Listas y cabeceras
- **Contadores LED en las cabeceras**, calculados con los datos ya cargados en el ViewModel. Por ejemplo: entrenamientos de la semana, mensualidades por estado, partidos jugados, ganados y próximos, equipos y jugadores.
- **Filtros nuevos en chips**, solo donde hoy no existen, filtrando en el cliente lo que ya se cargó: Jugadores (Todos, Activos, Pendiente), Convocatoria (Todos, Disponibles, Pendientes), Historial de asistencia (Todas, Ausencias, Justificadas), Mensualidades (Todos, Pendientes, Vencidos), Entrenamientos (Próximos, Historial), Partidos (Próximos, Finalizados) y Equipos (Activos, Inactivos). Si un filtro equivalente ya existe, se conserva el actual y solo se restiliza.
- **Selector de equipo de las cabeceras** ("Halcones Sub-12 ▾"): solo donde la pantalla ya permite elegir equipo. Donde no, se muestra el nombre del equipo como etiqueta fija.

### 7.5 Login (HU-01 y HU-02)
- Botón para **mostrar u ocultar la contraseña** (ícono `visibility`) en el login y en el registro. Es un criterio de la HU-01 que hoy falta.

### 7.6 Lo que NO se hace en este rediseño
- ~~Barra de navegación inferior~~: se agregó como atajo adicional (ver la regla de oro 5, cambio aprobado el 2026-11-25).
- Campos nuevos de lugar del entrenamiento o de competición del partido. En su lugar se muestran la fecha y la ubicación que ya existen.
- Cambios en las reglas de Firestore, la Cloud Function o la lógica de sincronización.

---

## 8. Verificación al cerrar cada etapa

1. `./gradlew assembleDebug` y `./gradlew testDebugUnitTest` sin errores.
2. Revisar la pantalla tocada en modo claro y en modo oscuro.
3. Recorrer el flujo de la HU de esa pantalla y confirmar que todas sus acciones siguen ahí.
4. Anotar en `docs/diseno/PROGRESO.md` qué se hizo, qué HU se tocó y qué quedó pendiente.
