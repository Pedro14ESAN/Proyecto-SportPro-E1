"use strict";

/*
 * SportPro - Cloud Functions
 *
 * HU26: generación del resumen narrativo del partido con IA.
 *
 * Seguridad (HU25 / RN-20):
 * - La API key del proveedor de IA NO está en el APK: es un secreto
 *   de Firebase (ANTHROPIC_API_KEY) que solo esta función lee.
 * - La función vuelve a validar sesión, rol y relación con el equipo
 *   aunque la app ya lo haya hecho.
 * - El texto se guarda SIEMPRE como BORRADOR; solo el DT lo aprueba (HU27).
 *
 * Despliegue:
 *   firebase functions:secrets:set ANTHROPIC_API_KEY
 *   firebase deploy --only functions
 */

const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { defineSecret } = require("firebase-functions/params");
const logger = require("firebase-functions/logger");
const { initializeApp } = require("firebase-admin/app");
const { getFirestore } = require("firebase-admin/firestore");
const Anthropic = require("@anthropic-ai/sdk");

const {
  MINIMO_EVENTOS_JUEGO,
  eventosValidos,
  eventosDeJuego,
  huella,
  construirEntrada,
  INSTRUCCIONES,
  mensajeUsuario,
} = require("./resumen");

initializeApp();
const db = getFirestore();

const ANTHROPIC_API_KEY = defineSecret("ANTHROPIC_API_KEY");
const MODELO = "claude-opus-5-5";

// Solo un DT del equipo o un DT autorizado en la academia del partido
async function verificarDt(uid, partido) {
  const perfil = (await db.collection("usuarios").doc(uid).get()).data();
  if (!perfil || perfil.rol !== "DT" || perfil.estado === "PENDIENTE") {
    throw new HttpsError("permission-denied", "Solo el director técnico autorizado puede generar el resumen.");
  }

  const [equipoDoc, academiaDoc] = await Promise.all([
    db.collection("equipos").doc(partido.idEquipo || "-").get(),
    db.collection("academias").doc(partido.idAcademia || "-").get(),
  ]);
  const equipo = equipoDoc.data() || {};
  const academia = academiaDoc.data() || {};

  const esDtDelEquipo = equipo.idEntrenador === uid;
  const autorizadoEnAcademia =
    academia.activa !== false && Array.isArray(academia.idsAutorizados) && academia.idsAutorizados.includes(uid);

  if (!esDtDelEquipo && !autorizadoEnAcademia) {
    throw new HttpsError("permission-denied", "No tienes acceso a este partido.");
  }
  return { perfil, nombreEquipo: equipo.nombre || "Mi equipo" };
}

exports.generarResumenPartido = onCall(
  {
    secrets: [ANTHROPIC_API_KEY],
    timeoutSeconds: 120,
    memory: "512MiB",
    region: "us-central1",
  },
  async (request) => {
    const uid = request.auth && request.auth.uid;
    if (!uid) {
      throw new HttpsError("unauthenticated", "Tu sesión terminó. Vuelve a iniciar sesión.");
    }

    const idPartido = request.data && String(request.data.idPartido || "");
    if (!idPartido) {
      throw new HttpsError("invalid-argument", "El partido no es válido.");
    }

    // Precondición: partido finalizado
    const partidoDoc = await db.collection("partidos").doc(idPartido).get();
    if (!partidoDoc.exists) {
      // Mismo mensaje que sin permiso: no se revela si existe (HU25)
      throw new HttpsError("permission-denied", "No tienes acceso a este partido.");
    }
    const partido = { id: partidoDoc.id, ...partidoDoc.data() };

    const { nombreEquipo } = await verificarDt(uid, partido);

    if (partido.estado !== "FINALIZADO") {
      throw new HttpsError("failed-precondition", "El resumen solo se genera cuando el partido está finalizado.");
    }

    // Eventos tal como están en el servidor (los pendientes de sync aún no llegaron)
    const eventosSnap = await db.collection("partidos").doc(idPartido).collection("eventos").get();
    const eventos = eventosSnap.docs.map((d) => ({ id: d.id, ...d.data() }));
    const huellaServidor = huella(eventos);

    const huellaEsperada = request.data.huellaEsperada;
    if (huellaEsperada && huellaEsperada !== huellaServidor) {
      throw new HttpsError(
        "failed-precondition",
        "Hay eventos que aún no llegan al servidor o cambiaron. Espera la sincronización y vuelve a intentar."
      );
    }

    if (eventosDeJuego(eventos).length < MINIMO_EVENTOS_JUEGO) {
      throw new HttpsError(
        "failed-precondition",
        "No hay suficientes eventos registrados para generar un resumen fiable."
      );
    }

    const validos = eventosValidos(eventos);
    const entrada = construirEntrada({ partido, nombreEquipo, eventos });

    // Llamada al proveedor de IA
    let texto;
    let modeloUsado = MODELO;
    try {
      const client = new Anthropic({ apiKey: ANTHROPIC_API_KEY.value() });
      const respuesta = await client.beta.messages.create({
        model: MODELO,
        max_tokens: 4000,
        thinking: { type: "adaptive" },
        output_config: { effort: "medium" },
        betas: ["server-side-fallback-2026-07-01"],
        fallbacks: "default",
        system: INSTRUCCIONES,
        messages: [{ role: "user", content: mensajeUsuario(entrada) }],
      });

      if (respuesta.stop_reason === "refusal") {
        throw new Error("El proveedor de IA rechazó la solicitud");
      }
      modeloUsado = respuesta.model || MODELO;
      texto = respuesta.content
        .filter((b) => b.type === "text")
        .map((b) => b.text)
        .join("\n")
        .trim();
      if (!texto) throw new Error("Respuesta vacía del proveedor de IA");
    } catch (e) {
      // Si falla la IA, los eventos se conservan y el DT puede reintentar
      logger.error("Fallo al generar el resumen", { idPartido, error: e && e.message });
      throw new HttpsError("unavailable", "El servicio de IA no respondió. Puedes reintentar.");
    }

    // Guardar como BORRADOR con su historial de versiones
    const ref = db.collection("resumenesPartido").doc(idPartido);
    const ahora = Date.now();

    const version = await db.runTransaction(async (tx) => {
      const actual = await tx.get(ref);
      const siguiente = ((actual.exists && actual.data().version) || 0) + 1;

      tx.set(ref, {
        idPartido,
        idAcademia: partido.idAcademia || "",
        idEquipo: partido.idEquipo || "",
        estado: "BORRADOR",
        origen: "IA",
        modeloIA: modeloUsado,
        version: siguiente,
        textoOriginalIA: texto,
        textoEditado: texto,
        textoFinal: "",
        idsEventosUsados: validos.map((e) => e.id),
        huellaEventos: huellaServidor,
        totalEventos: validos.length,
        generadoPor: uid,
        fechaGeneracion: ahora,
        editadoPor: "",
        fechaEdicion: 0,
        aprobadoPor: "",
        nombreAprobador: "",
        fechaAprobacion: 0,
        evaluacion: { utilidad: 0, omisiones: "", erroresOInvenciones: "", correccionesRealizadas: "" },
        rechazadoPor: "",
        motivoRechazo: "",
        fechaRechazo: 0,
        motivoDesactualizado: "",
        fechaDesactualizado: 0,
      });

      tx.set(ref.collection("versiones").doc(), {
        version: siguiente,
        tipo: "GENERADA",
        origen: "IA",
        texto,
        huellaEventos: huellaServidor,
        totalEventos: validos.length,
        autor: uid,
        fecha: ahora,
        nota: `Generado con ${modeloUsado}`,
      });

      // HU25: operación crítica con usuario ejecutor
      tx.set(db.collection("auditoria").doc(), {
        accion: "RESUMEN_GENERADO",
        uid,
        rol: "DT",
        recurso: "resumenesPartido",
        idRecurso: idPartido,
        idAcademia: partido.idAcademia || "",
        detalle: `versión ${siguiente} · ${validos.length} eventos · ${modeloUsado}`,
        fecha: ahora,
      });

      return siguiente;
    });

    return { version, totalEventos: validos.length, huella: huellaServidor };
  }
);
