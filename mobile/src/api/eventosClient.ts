import type { CrearEventoPayload, Evento } from "../types/evento";

/** Base URL del backend. En emulador Android (Windows) el host suele ser 10.0.2.2. */
const baseUrl = (
  process.env.EXPO_PUBLIC_API_URL ?? "http://10.0.2.2:8080"
).replace(/\/$/, "");

async function parseErrorMessage(res: Response): Promise<string> {
  const text = await res.text();
  try {
    const data = JSON.parse(text) as { error?: string };
    if (data.error) {
      return data.error;
    }
  } catch {
    /* ignorar JSON inválido */
  }
  return text || `HTTP ${res.status}`;
}

export async function crearEvento(payload: CrearEventoPayload): Promise<Evento> {
  const res = await fetch(`${baseUrl}/eventos`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  if (!res.ok) {
    throw new Error(await parseErrorMessage(res));
  }
  return (await res.json()) as Evento;
}

export async function listarEventos(): Promise<Evento[]> {
  const res = await fetch(`${baseUrl}/eventos`);
  if (!res.ok) {
    throw new Error(await parseErrorMessage(res));
  }
  return (await res.json()) as Evento[];
}
