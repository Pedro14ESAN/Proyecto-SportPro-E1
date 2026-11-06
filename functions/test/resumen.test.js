"use strict";

const test = require("node:test");
const assert = require("node:assert");
const r = require("../resumen");

const eventos = [
  { id: "b", minuto: 0, codigoTipoEvento: "INICIO", estado: "ACTIVO", version: 1 },
  { id: "a", minuto: 12, codigoTipoEvento: "GOL", nombreTipoEvento: "Gol", afectaMarcador: true,
    equipoEvento: "PROPIO", nombreJugador: "Luis", estado: "ACTIVO", version: 2 },
  { id: "c", minuto: 30, codigoTipoEvento: "GOL", afectaMarcador: true, equipoEvento: "RIVAL",
    estado: "ANULADO", version: 2 },
  { id: "d", minuto: 40, codigoTipoEvento: "TARJETA", equipoEvento: "RIVAL", estado: "ACTIVO", version: 1 },
];

test("excluye anulados y ordena por minuto", () => {
  assert.deepStrictEqual(r.eventosValidos(eventos).map((e) => e.id), ["b", "a", "d"]);
});

test("marcador solo con eventos válidos", () => {
  assert.deepStrictEqual(r.marcador(eventos), { propio: 1, rival: 0 });
});

test("la huella cambia si se corrige un evento", () => {
  const h1 = r.huella(eventos);
  const corregidos = eventos.map((e) => (e.id === "d" ? { ...e, version: 2 } : e));
  assert.notStrictEqual(h1, r.huella(corregidos));
  assert.strictEqual(h1.length, 16);
});

test("la entrada no incluye ids ni datos sensibles", () => {
  const entrada = r.construirEntrada({ partido: { nombreRival: "Sur" }, nombreEquipo: "Norte", eventos });
  const texto = JSON.stringify(entrada);
  assert.ok(!texto.includes("idJugador"));
  assert.strictEqual(entrada.partido.marcadorFinal, "1-0");
  assert.strictEqual(entrada.eventos.length, 3);
  assert.strictEqual(entrada.eventos[2].jugador, "no identificado");
});
