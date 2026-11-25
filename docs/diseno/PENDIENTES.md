# Pendientes del rediseño "Línea de cal"

Regla 8 de la espec: si algo obligara a romper una regla de oro, no se hace y se anota aquí.

## Cambios que habrían roto una regla de oro

**Ninguno.** En la verificación final (2026-11-25) todos los errores de compilación se corrigieron sin quitar funcionalidad, y ningún test se borró ni se debilitó.

## Pendientes visuales u opcionales (no rompen ninguna regla)

| # | Pendiente | Dónde | Por qué quedó |
|---|---|---|---|
| 1 | Selector de equipo en las cabeceras ("Equipo ▾", 7.4) | Entrenamientos, Estadísticas | Las pantallas conservan su selector actual. `PildoraCabecera` ya está en el kit |
| 2 | Caja LED "RESPONDIERON x/y" (6.8) | Convocatoria | Detalle visual. Los chips con los conteos (7.4) ya muestran disponibles y pendientes |
| 3 | Colores del arquero (amarillo) y del jugador no disponible (naranja) en la cancha (6.8) | Alineación | Detalle visual; la alerta de no disponible sigue igual |
| 4 | Un `CromoJugador` grande por jugador vinculado (6.4) | Mis jugadores | Se conservó la tarjeta con los datos sensibles y la desvinculación, restilizada |
| 5 | Tickets en lugar de tarjetas (6.4) | Cuentas de jugadores, Vinculaciones | Quedaron como tarjetas con `ChipEstado` y las mismas acciones |
| 6 | El calendario de `CampoFecha` es el `DatePickerDialog` nativo de Android | Registro, Jugadores | Toma los colores de `themes.xml`, no del tema de Compose. Se arregla con un tema de diálogo en `res/values/themes.xml` |
| 7 | Duración del 1er tiempo fija en 45 min para el minuto sugerido y el reloj del 2T (7.2) | Partido en vivo | `RelojPartido.MINUTOS_PRIMER_TIEMPO`; en categorías con tiempos cortos el operador corrige el minuto. Para revisión de Italo |
| 8 | Conteo de jugadores por equipo (D-07) | Equipos | No se carga hoy; agregarlo requiere una lectura nueva |
| 9 | Revisión manual en claro y en oscuro de cada pantalla, y tests instrumentados en un emulador | Toda la app | Necesitan un dispositivo |

## Textos decorativos que ya no aparecen (no son acciones, filtros, diálogos ni mensajes)

Se vaciaron las cabeceras decorativas (logo ⚽ + "SPORTPRO" + campana 🔔 **sin acción**). Con ellas dejan de verse las líneas de marca "SPORTPRO", "CLUB DEPORTIVO" (Anuncios), "ADMINISTRACIÓN" (Catálogo), "PANEL PRINCIPAL" (inicio de los roles; se mantiene en Cuenta pendiente) y la campana falsa "🔔 (editar)". Cada pantalla conserva su título, su etiqueta y su botón de volver. Si se quiere recuperar alguna, basta con pasarla como `etiqueta` de su cabecera.
