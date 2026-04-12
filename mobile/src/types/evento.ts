export type TipoEvento = "temperatura" | "bateria" | "conexion";

export type CrearEventoPayload = {
  dispositivoId: string;
  tipoEvento: TipoEvento;
  valor: number;
};

export type Evento = {
  id: number;
  dispositivoId: string;
  tipoEvento: string;
  valor: number;
  severidad: string;
  fechaHora: string;
};
