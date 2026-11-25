# Prompts para Claude Code (en orden)

Pega un prompt a la vez. Cuando una etapa termine, revisa los cambios, prueba la app y recién entonces pasa a la siguiente.

---

## Prompt 1 · Revisión y plan (sin tocar código)

```
Lee completo docs/diseno/ESPECIFICACION.md y mira todas las imágenes de docs/diseno/img/. Después revisa todo el proyecto: ui/, datos/, navegación, tema y tests.

No modifiques código todavía. Crea docs/diseno/PLAN.md con:
1. Inventario de las 33 pantallas (archivos Pantalla*.kt): por cada una, todas sus acciones, botones, diálogos, filtros, estados y mensajes actuales. Será la lista de control para no perder ninguna funcionalidad.
2. Para cada pantalla: qué componentes del kit (sección 5) usará y qué imagen de referencia le corresponde.
3. Para cada funcionalidad nueva de la sección 7: los archivos que hay que tocar, de dónde salen los datos y su riesgo.
4. Las dudas o conflictos que encuentres con las reglas de oro (sección 1).

Antes de terminar, corre ./gradlew assembleDebug y ./gradlew testDebugUnitTest y anota el resultado en el plan. Muéstrame un resumen del plan.
```

## Prompt 2 · Etapa 1: tema, colores, fuentes y modo oscuro

```
Sigue docs/diseno/ESPECIFICACION.md (las reglas de oro de la sección 1 son obligatorias) y docs/diseno/PLAN.md.

Implementa la sección 4 (tema), usando los tokens de la sección 3 y las fuentes que ya están en app/src/main/res/font/:
- Color.kt con la paleta clara y la oscura; Type.kt con las 4 familias y una Typography completa; Theme.kt con dynamicColor = false, lightColorScheme y darkColorScheme según isSystemInDarkTheme().
- Que TextField, DatePicker, FilterChip, Switch, Checkbox, AlertDialog y CircularProgressIndicator tomen los colores del tema.
- Redirige las paletas privadas existentes (EstiloPartido.kt, ComponentesSportPro.kt, EstiloSincronizacion.kt y los private val Color(0x...) de las pantallas) a los tokens, cambiando solo la línea de cada declaración.
- Agrega la dependencia material-icons-extended (sección 3.5).

No cambies la lógica de ninguna pantalla. Compila, corre los tests, haz un commit "style: tema Línea de cal (claro y oscuro)" y actualiza docs/diseno/PROGRESO.md.
```

## Prompt 3 · Etapa 2: kit de componentes

```
Sigue docs/diseno/ESPECIFICACION.md y docs/diseno/PLAN.md.

Crea el kit de la sección 5 en ui/comun/LineaDeCal.kt: CabeceraCancha, MarcadorLed, ContadorLed, TarjetaTicket, FichaTactica, PizarraFichas, CromoJugador, ChipEstado, BotonPrincipal, CampoSportPro, BarraSuperior y FondoCancha. Cada uno con @Preview en claro y en oscuro, fiel a las imágenes 01, 02 y 05 a 10. Doto se usa solo para números de 18 sp o más.

Todavía no cambies ninguna pantalla. Compila, haz un commit "feat(ui): kit de componentes Línea de cal" y actualiza PROGRESO.md.
```

## Prompt 4 · Etapa 3: inicio de los 4 roles

```
Sigue docs/diseno/ESPECIFICACION.md (secciones 6.2 y 7.1) y docs/diseno/PLAN.md. Referencia: img/03_inicio_roles.png.

Rediseña PantallasInicio.kt para ADM, DT, JUG y PAD, y también PantallaCuentaPendiente. Conserva TODOS los destinos actuales de cada rol (ADM 12, DT 9, JUG 9, PAD 10): Notificaciones pasa a la campana, Partidos en vivo al MarcadorLed, y Cerrar sesión va en el menú del avatar y también como botón discreto al final. Agrega lo de la sección 7.1 (tarjeta EN VIVO, contadores, "Requiere tu atención", "Tu jornada", "Para ti" y "Mis jugadores"). Todo eso solo lee datos; si algo falla, esa sección se oculta. No hay vista de tablet: en cualquier tamaño se usa el diseño de celular, centrado con widthIn(max = 600.dp). No agregues la barra inferior.

Agrega tests para la lógica nueva de los contadores. Compila, corre los tests, haz commits separados para el estilo y para lo nuevo, y actualiza PROGRESO.md.
```

## Prompt 5 · Etapa 4: resto de pantallas, módulo por módulo

```
Sigue docs/diseno/ESPECIFICACION.md (sección 6) y el inventario de docs/diseno/PLAN.md.

Aplica el kit y el tema a estos módulos, en este orden, con un commit por módulo:
1) autenticación (bienvenida, login, registro, recuperar) · 2) academias y equipos · 3) jugadores, cuentas de jugadores, mis jugadores y vinculaciones · 4) mensualidades · 5) ejercicios y entrenamientos · 6) asistencias · 7) partidos (lista, crear, editar, detalle), convocatoria, mis convocatorias y alineación · 8) catálogo de eventos · 9) partido en vivo y seguimiento (solo lo visual de 6.9) · 10) anuncios, notificaciones, moderación, planes · 11) resumen con IA · 12) estadísticas (solo lo visual).

En cada módulo reemplaza los emojis y flechas de texto por íconos de Material (salvo las reacciones ❤️ 👏 💪), elimina los textos de menos de 12 sp y usa las familias del tema. Al terminar cada módulo, compara contra su inventario en PLAN.md y confirma que no falta ninguna acción. Compila y corre los tests después de cada commit. Si un cambio obligara a quitar algo, anótalo en docs/diseno/PENDIENTES.md y no lo hagas.
```

## Prompt 6 · Etapa 5: comunidad

```
Sigue docs/diseno/ESPECIFICACION.md, sección 6.10, con las 11 correcciones obligatorias. Referencia: img/10_comunidad_detalle.png (el texto manda sobre la imagen).

Rediseña PantallaComunidad.kt, SeccionPruebas.kt, HojaAccionesContenido.kt, HojaComentarios.kt, HojaUsuariosBloqueados.kt y los formularios, sin quitar ninguna acción. Puntos clave: el título de la prueba sale de AvisoPrueba.titulo(); los estados ABIERTA, CERRADA y VENCIDA salen de estadoEfectivo(); el botón de reacción muestra la reacción propia o los emojis presentes junto al total; menú ⋮ en cada tarjeta; FAB "+ Aviso" en Pruebas; 80 dp de espacio al final de la lista; escudo de moderar con el número de reportes pendientes solo si la consulta es simple (si no, sin número).

Compila, corre los tests, haz un commit "style(HU-21..24): comunidad Línea de cal" y actualiza PROGRESO.md.
```

## Prompt 7 · Etapa 6: funcionalidades nuevas de estadísticas, listas y login

```
Sigue docs/diseno/ESPECIFICACION.md, secciones 7.3, 7.4 y 7.5.

1) Estadísticas del jugador: minutos jugados, goles por fecha, último partido y foto en el CromoJugador. Agrega tests unitarios para el cálculo de minutos (titular, entra por cambio, sale por cambio, sin FIN). "Compartir el cromo como imagen" va al final y en un commit aparte.
2) Contadores LED en las cabeceras y los filtros en chips de la sección 7.4, solo donde hoy no existen, filtrando en el cliente.
3) Mostrar y ocultar la contraseña en el login y el registro.

Un commit por punto. Compila y corre los tests en cada uno. Actualiza PROGRESO.md.
```

## Prompt 8 · Etapa 7 (aparte, para que lo revise Italo): partido en vivo

```
Sigue docs/diseno/ESPECIFICACION.md, sección 7.2. Estos cambios afectan la HU-14 de Italo: hazlos en commits separados y no toques nada de la HU-16 fuera de lo indicado.

1) Minuto sugerido: precargado desde la fechaCaptura del INICIO en caché, con botones − / + y siempre editable; si no hay INICIO, el campo queda vacío. Agrega tests unitarios.
2) Reloj del partido en el MarcadorLed. Agrega el tipo "Inicio 2T" (código INICIO_2T) al catálogo por defecto, inclúyelo en CODIGOS_CONTROL de FuenteResumen y confirma que no afecta estadísticas, conflictos ni marcador.
3) Panel rápido sobre la mini cancha: fichas tomadas del catálogo activo más la ficha "Más tipos", que abren el formulario actual con abrirEvento(tipo).
4) Mensaje "guardado en este dispositivo" cuando confirmarOEncolar devuelva false: haz que registrarEvento informe si quedó sincronizado.

Compila, corre todos los tests y actualiza PROGRESO.md.
```

## Prompt 9 · Verificación final

```
Revisa todo el trabajo de la rama contra docs/diseno/PLAN.md y docs/diseno/ESPECIFICACION.md:
1) Recorre el inventario de las 33 pantallas y confirma, una por una, que no falta ninguna acción, filtro, diálogo ni mensaje. Haz una tabla con el resultado.
2) Busca emojis o flechas de texto que hayan quedado como íconos, textos de menos de 12 sp, FontFamily.Serif o Monospace y colores Color(0x...) fuera del tema.
3) Corre ./gradlew assembleDebug, ./gradlew testDebugUnitTest y, si hay un emulador, los tests instrumentados.
4) Revisa el modo oscuro de las pantallas principales.
Corrige lo que encuentres en commits pequeños y deja el resumen final en PROGRESO.md.
```
