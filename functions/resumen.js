"use strict";

/*
 * HU26 - Lógica pura del resumen narrativo (sin Firebase ni red),
 * para poder probarla con `npm test`.
 */

const crypto = require("node:crypto");

const CODIGOS_CONTROL = new Set(["INICIO", "FIN"]);
const MINIMO_EVENTOS_JUEGO = 2;

// Solo eventos válidos: sin anulados; el documento ya es la versión vigente.
function eventosValidos(eventos) {
  return eventos
    .filter((e) => (e.estado || "ACTIVO") === "ACTIVO")
    .sort((a, b) => (a.minuto - b.minuto) || ((a.fechaCaptura || 0) - (b.fechaCaptura || 0)));
}

function eventosDeJuego(eventos) {
  return eventosValidos(eventos).filter((e) => !CODIGOS_CONTROL.has(e.codigoTipoEvento));
}

/*
 * Misma huella que FuenteResumen.huella() en la app:
 * sha256("id:version|...") de los eventos ACTIVOS ordenados por id,
 * primeros 16 caracteres hex.
 */
function huella(eventos) {
  const base = eventos
    .filter((e) => (e.estado || "ACTIVO") === "ACTIVO")
    .map((e) => ({ id: e.id, version: e.version || 1 }))
    .sort((a, b) => (a.id < b.id ? -1 : a.id > b.id ? 1 : 0))
    .map((e) => `${e.id}:${e.version}`)
    .join("|");
  return crypto.createHash("sha256").update(base, "utf8").digest("hex").slice(0, 16);
}

function marcador(eventos) {
  const goles = eventosValidos(eventos).filter((e) => e.afectaMarcador === true);
  return {
    propio: goles.filter((e) => e.equipoEvento === "PROPIO").length,
    rival: goles.filter((e) => e.equipoEvento === "RIVAL").length,
  };
}

/*
 * Entrada estructurada SIN datos sensibles: solo minuto, tipo,
 * equipo, nombre visible del jugador y observación del evento.
 * Nunca se envían teléfonos, contactos, datos físicos, fechas de
 * nacimiento, documentos ni ids internos.
 */
function construirEntrada({ partido, nombreEquipo, eventos }) {
  const validos = eventosValidos(eventos);
  const m = marcador(eventos);
  const nombreRival = partido.nombreRival || "Rival";

  const equipoDe = (e) =>
    e.equipoEvento === "PROPIO" ? nombreEquipo : e.equipoEvento === "RIVAL" ? nombreRival : null;

  return {
    partido: {
      equipoLocal: nombreEquipo,
      rival: nombreRival,
      fecha: partido.fecha || null,
      lugar: partido.ubicacion || null,
      marcadorFinal: `${m.propio}-${m.rival}`,
    },
    eventos: validos.map((e, i) => ({
      n: i + 1,
      minuto: e.minuto,
      tipo: e.nombreTipoEvento || e.codigoTipoEvento,
      codigo: e.codigoTipoEvento,
      equipo: equipoDe(e),
      jugador: e.nombreJugador ? e.nombreJugador : (e.equipoEvento === "NINGUNO" ? null : "no identificado"),
      observacion: e.observacion ? String(e.observacion).slice(0, 200) : null,
      corregido: (e.version || 1) > 1,
    })),
  };
}

const INSTRUCCIONES = `Eres el redactor de resúmenes de partidos de fútbol formativo de la app SportPro.
Escribes en español neutro, en tono sobrio y respetuoso: muchos jugadores son menores de edad.

Reglas obligatorias:
1. Usa ÚNICAMENTE los eventos del JSON que recibes. No agregues jugadas, causas, asistencias, ocasiones, estadísticas, clima, público ni declaraciones que no estén en los eventos.
2. Si un dato no está (por ejemplo, el jugador figura como "no identificado"), dilo así o no lo menciones; nunca lo completes ni lo adivines.
3. El marcador que menciones debe coincidir exactamente con "marcadorFinal".
4. Respeta los minutos tal como vienen.
5. No califiques ni critiques a jugadores individuales; no incluyas datos personales.
6. Separa hechos de interpretación con este formato exacto:

HECHOS
(uno o dos párrafos que narran en orden cronológico solo lo registrado)

LECTURA DEL PARTIDO (interpretación)
(máximo dos frases generales que se deduzcan directamente de los hechos, por ejemplo cuándo se marcaron los goles; si no hay base suficiente escribe "Sin interpretación: los eventos registrados no permiten sacar conclusiones adicionales.")

Escribe solo el resumen, sin títulos adicionales ni comentarios sobre estas reglas. Máximo 220 palabras.`;

function mensajeUsuario(entrada) {
  return `Eventos registrados del partido (JSON):\n${JSON.stringify(entrada, null, 2)}\n\nRedacta el resumen siguiendo las reglas.`;
}

module.exports = {
  CODIGOS_CONTROL,
  MINIMO_EVENTOS_JUEGO,
  eventosValidos,
  eventosDeJuego,
  huella,
  marcador,
  construirEntrada,
  INSTRUCCIONES,
  mensajeUsuario,
};
