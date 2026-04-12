import "./global.css";
import { StatusBar } from "expo-status-bar";
import { useCallback, useState } from "react";
import {
  ActivityIndicator,
  KeyboardAvoidingView,
  Platform,
  Pressable,
  ScrollView,
  Text,
  TextInput,
  View,
} from "react-native";
import { SafeAreaProvider, SafeAreaView } from "react-native-safe-area-context";
import { crearEvento, listarEventos } from "./src/api/eventosClient";
import { TIPOS_EVENTO } from "./src/constants/tiposEvento";
import type { Evento, TipoEvento } from "./src/types/evento";

export default function App() {
  const [dispositivoId, setDispositivoId] = useState("");
  const [tipoEvento, setTipoEvento] = useState<TipoEvento>("temperatura");
  const [valorTexto, setValorTexto] = useState("");
  const [mensaje, setMensaje] = useState<string | null>(null);
  const [mensajeOk, setMensajeOk] = useState<boolean | null>(null);
  const [enviando, setEnviando] = useState(false);
  const [refrescando, setRefrescando] = useState(false);
  const [eventos, setEventos] = useState<Evento[]>([]);

  const mostrarMensaje = useCallback((texto: string, ok: boolean) => {
    setMensaje(texto);
    setMensajeOk(ok);
  }, []);

  const onEnviar = useCallback(async () => {
    const id = dispositivoId.trim();
    if (!id) {
      mostrarMensaje("Ingresá un dispositivoId.", false);
      return;
    }
    const valor = Number(valorTexto.replace(",", "."));
    if (!Number.isFinite(valor)) {
      mostrarMensaje("El valor debe ser un número válido.", false);
      return;
    }

    setEnviando(true);
    setMensaje(null);
    setMensajeOk(null);
    try {
      const guardado = await crearEvento({
        dispositivoId: id,
        tipoEvento,
        valor,
      });
      setEventos((prev) => [guardado, ...prev]);
      mostrarMensaje("Evento enviado correctamente.", true);
    } catch (e) {
      const texto = e instanceof Error ? e.message : "Error desconocido";
      mostrarMensaje(texto, false);
    } finally {
      setEnviando(false);
    }
  }, [dispositivoId, tipoEvento, valorTexto, mostrarMensaje]);

  const onRefrescar = useCallback(async () => {
    setRefrescando(true);
    setMensaje(null);
    setMensajeOk(null);
    try {
      const lista = await listarEventos();
      setEventos(lista);
      mostrarMensaje("Lista actualizada desde el servidor.", true);
    } catch (e) {
      const texto = e instanceof Error ? e.message : "Error desconocido";
      mostrarMensaje(texto, false);
    } finally {
      setRefrescando(false);
    }
  }, [mostrarMensaje]);

  return (
    <SafeAreaProvider>
      <SafeAreaView className="flex-1 bg-white">
        <StatusBar style="dark" />
        <KeyboardAvoidingView
          behavior={Platform.OS === "ios" ? "padding" : undefined}
          className="flex-1"
        >
          <ScrollView
            className="flex-1 px-4 pt-2"
            keyboardShouldPersistTaps="handled"
            contentContainerStyle={{ paddingBottom: 24 }}
          >
            <Text className="mb-4 text-2xl font-bold text-neutral-900">
              AgroMonitor Mini
            </Text>

            <Text className="mb-1 text-sm font-medium text-neutral-700">
              Dispositivo ID
            </Text>
            <TextInput
              className="mb-4 rounded-lg border border-neutral-300 bg-neutral-50 px-3 py-2 text-base text-neutral-900"
              placeholder="ej. sensor-campo-1"
              placeholderTextColor="#9ca3af"
              value={dispositivoId}
              onChangeText={setDispositivoId}
              autoCapitalize="none"
              autoCorrect={false}
            />

            <Text className="mb-2 text-sm font-medium text-neutral-700">
              Tipo de evento
            </Text>
            <View className="mb-4 flex-row flex-wrap gap-2">
              {TIPOS_EVENTO.map((t) => {
                const seleccionado = tipoEvento === t;
                return (
                  <Pressable
                    key={t}
                    onPress={() => setTipoEvento(t)}
                    className={`rounded-full border px-3 py-2 ${
                      seleccionado
                        ? "border-emerald-600 bg-emerald-600"
                        : "border-neutral-300 bg-neutral-100"
                    }`}
                  >
                    <Text
                      className={`text-sm font-medium ${
                        seleccionado ? "text-white" : "text-neutral-800"
                      }`}
                    >
                      {t}
                    </Text>
                  </Pressable>
                );
              })}
            </View>

            <Text className="mb-1 text-sm font-medium text-neutral-700">
              Valor
            </Text>
            <TextInput
              className="mb-4 rounded-lg border border-neutral-300 bg-neutral-50 px-3 py-2 text-base text-neutral-900"
              placeholder="ej. 23.5"
              placeholderTextColor="#9ca3af"
              value={valorTexto}
              onChangeText={setValorTexto}
              keyboardType="decimal-pad"
            />

            <Pressable
              onPress={onEnviar}
              disabled={enviando}
              className={`mb-3 items-center rounded-lg py-3 ${
                enviando ? "bg-emerald-300" : "bg-emerald-600"
              }`}
            >
              {enviando ? (
                <ActivityIndicator color="#fff" />
              ) : (
                <Text className="text-base font-semibold text-white">
                  Enviar evento
                </Text>
              )}
            </Pressable>

            <Pressable
              onPress={onRefrescar}
              disabled={refrescando}
              className={`mb-4 items-center rounded-lg border border-neutral-400 py-3 ${
                refrescando ? "opacity-60" : ""
              }`}
            >
              {refrescando ? (
                <ActivityIndicator color="#374151" />
              ) : (
                <Text className="text-base font-semibold text-neutral-800">
                  Refrescar desde servidor
                </Text>
              )}
            </Pressable>

            {mensaje != null && mensajeOk != null && (
              <View
                className={`mb-4 rounded-lg border px-3 py-2 ${
                  mensajeOk
                    ? "border-emerald-200 bg-emerald-50"
                    : "border-red-200 bg-red-50"
                }`}
              >
                <Text
                  className={`text-sm ${
                    mensajeOk ? "text-emerald-800" : "text-red-800"
                  }`}
                >
                  {mensaje}
                </Text>
              </View>
            )}

            <Text className="mb-2 text-lg font-semibold text-neutral-900">
              Eventos
            </Text>
            {eventos.length === 0 ? (
              <Text className="text-sm text-neutral-500">
                Todavía no hay eventos en la lista. Enviá uno o refrescá desde
                el servidor.
              </Text>
            ) : (
              eventos.map((e) => (
                <View
                  key={e.id}
                  className="mb-2 rounded-lg border border-neutral-200 bg-neutral-50 px-3 py-2"
                >
                  <Text className="text-sm font-medium text-neutral-900">
                    #{e.id} · {e.dispositivoId} · {e.tipoEvento}
                  </Text>
                  <Text className="text-xs text-neutral-600">
                    valor {e.valor} · {e.severidad} · {e.fechaHora}
                  </Text>
                </View>
              ))
            )}
          </ScrollView>
        </KeyboardAvoidingView>
      </SafeAreaView>
    </SafeAreaProvider>
  );
}
