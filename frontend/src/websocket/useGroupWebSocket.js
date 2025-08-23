import { useEffect, useRef } from "react";
import { createStompClient } from "./stompClient";

export function useGroupWebSocket(groupId, onMessageReceived) {
  const clientRef = useRef(null);
  const subscriptionsRef = useRef([]);
  const onMessageReceivedRef = useRef(onMessageReceived);

  useEffect(() => {
    onMessageReceivedRef.current = onMessageReceived;
  }, [onMessageReceived]);

  useEffect(() => {
    if (!groupId) return;

    function handleConnect(client) {
      const topics = [
        `/topic/groups/${groupId}/users`,
        `/topic/expenses/groups/${groupId}`,
      ];
      subscriptionsRef.current = topics.map((topic) =>
        client.subscribe(topic, (message) => {
          onMessageReceivedRef.current?.(topic, message);
        })
      );
    }

    clientRef.current = createStompClient({
      onConnect: handleConnect,
      onDisconnect: () => console.log("[WS] Disconnected"),
      onError: (frame) => console.error("[WS] Error", frame),
    });

    return () => {
      subscriptionsRef.current.forEach((sub) => sub.unsubscribe());
      subscriptionsRef.current = [];
      if (clientRef.current) {
        clientRef.current.deactivate();
      }
    };
  }, [groupId]);
}
