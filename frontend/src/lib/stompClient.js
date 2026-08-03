import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";
import { getAuthToken } from "@/api/client";

export function createStompClient({ onConnect, onDisconnect, onError } = {}) {
  const wsUrl = import.meta.env.VITE_WS_URL;
  if (!wsUrl) {
    throw new Error("VITE_WS_URL is not set - SockJS cannot connect without a WebSocket URL");
  }

  const log = import.meta.env.DEV ? console.log : () => {};
  const warn = import.meta.env.DEV ? console.warn : () => {};

  const client = new Client({
    webSocketFactory: () => new SockJS(wsUrl),
    reconnectDelay: 5000,
    debug: (str) => log("[STOMP]", str),
    beforeConnect: () => {
      client.connectHeaders = { Authorization: `Bearer ${getAuthToken() ?? ""}` };
    },
    onConnect: (frame) => {
      log("[STOMP] connected", frame.headers);
      onConnect?.(client, frame);
    },
    onStompError: (frame) => {
      console.error("[STOMP] broker error", frame.headers["message"], frame.body);
      onError?.(frame);
    },
    onWebSocketClose: (event) => {
      warn("[STOMP] websocket closed", event);
      onDisconnect?.();
    },
  });

  client.activate();
  return client;
}
