import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";

export function createStompClient({ onConnect, onDisconnect, onError } = {}) {
  const wsUrl = import.meta.env.VITE_WS_URL;
  const log = import.meta.env.DEV ? console.log : () => {};
  const warn = import.meta.env.DEV ? console.warn : () => {};

  const client = new Client({
    webSocketFactory: () => new SockJS(wsUrl),
    reconnectDelay: 5000,
    debug: (str) => log("[STOMP]", str),
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
