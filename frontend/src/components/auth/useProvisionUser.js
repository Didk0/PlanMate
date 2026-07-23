import { useCallback, useEffect, useRef, useState } from "react";
import userService from "@/api/userService";
import { getErrorMessage } from "@/lib/getErrorMessage";

export function useProvisionUser(token, tokenData) {
  const provisionedSubRef = useRef(null);
  const [attempt, setAttempt] = useState(0);
  const [state, setState] = useState({ status: "pending", error: null });

  const retry = useCallback(() => setAttempt((a) => a + 1), []);

  useEffect(() => {
    if (!token || !tokenData) return;

    const sub = tokenData.sub;
    const sessionKey = `userCreated:${sub}`;

    if (provisionedSubRef.current === sub || sessionStorage.getItem(sessionKey)) {
      setState({ status: "ready", error: null });
      return;
    }

    let cancelled = false;
    setState({ status: "pending", error: null });

    userService
      .createUser()
      .then(() => {
        if (cancelled) return;
        provisionedSubRef.current = sub;
        sessionStorage.setItem(sessionKey, "true");
        setState({ status: "ready", error: null });
      })
      .catch((err) => {
        if (cancelled) return;
        if (err.response?.status === 409) {
          provisionedSubRef.current = sub;
          sessionStorage.setItem(sessionKey, "true");
          setState({ status: "ready", error: null });
          return;
        }
        setState({ status: "error", error: getErrorMessage(err, "Failed to set up your account") });
      });

    return () => {
      cancelled = true;
    };
  }, [token, tokenData, attempt]);

  return { ...state, retry };
}
