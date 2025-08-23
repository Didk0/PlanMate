import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";
import { useRef } from "react";

export function createStompClient({ onConnect, onDisconnect, onError } = {}) {
  const wsUrl = import.meta.env.VITE_WS_URL;

  const client = new Client({
    webSocketFactory: () => new SockJS(wsUrl),
    reconnectDelay: 5000,
    debug: (str) => console.log("[STOMP]", str),
    onConnect: (frame) => {
      console.log("[STOMP] connected", frame.headers);
      onConnect?.(client, frame);
    },
    onStompError: (frame) => {
      console.error(
        "[STOMP] broker error",
        frame.headers["message"],
        frame.body
      );
      onError?.(frame);
    },
    onWebSocketClose: (event) => {
      console.warn("[STOMP] websocket closed", event);
      onDisconnect?.();
    },
  });

  client.activate();
  return client;
}
