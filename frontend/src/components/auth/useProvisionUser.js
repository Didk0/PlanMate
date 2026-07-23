import { useCallback, useEffect, useRef, useState } from "react";
import { useDispatch } from "react-redux";
import userService from "@/api/userService";
import { getErrorMessage } from "@/lib/getErrorMessage";
import { setAppUserId } from "@/store/authSlice";

export function useProvisionUser(token, tokenData) {
  const dispatch = useDispatch();
  const provisionedSubRef = useRef(null);
  const [attempt, setAttempt] = useState(0);
  const [state, setState] = useState({ status: "pending", error: null });

  const retry = useCallback(() => setAttempt((a) => a + 1), []);

  useEffect(() => {
    if (!token || !tokenData) return;

    const sub = tokenData.sub;

    if (provisionedSubRef.current === sub) {
      setState({ status: "ready", error: null });
      return;
    }

    let cancelled = false;
    setState({ status: "pending", error: null });

    userService
      .createUser()
      .then((dto) => {
        if (cancelled) return;
        provisionedSubRef.current = sub;
        dispatch(setAppUserId(dto.id));
        setState({ status: "ready", error: null });
      })
      .catch((err) => {
        if (cancelled) return;
        setState({ status: "error", error: getErrorMessage(err, "Failed to set up your account") });
      });

    return () => {
      cancelled = true;
    };
  }, [token, tokenData, attempt, dispatch]);

  return { ...state, retry };
}
