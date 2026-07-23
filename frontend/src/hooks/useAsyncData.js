import { useCallback, useEffect, useState } from "react";
import { getErrorMessage } from "@/lib/getErrorMessage";

export function useAsyncData(fetcher, deps) {
  const [reloadIndex, setReloadIndex] = useState(0);
  const reload = useCallback(() => setReloadIndex((i) => i + 1), []);

  const key = JSON.stringify([...deps, reloadIndex]);

  const [state, setState] = useState({
    key,
    data: null,
    error: null,
    errorStatus: null,
    isLoading: true,
  });

  if (state.key !== key) {
    setState({ key, data: null, error: null, errorStatus: null, isLoading: true });
  }

  useEffect(() => {
    let cancelled = false;

    fetcher()
      .then((data) => {
        if (cancelled) return;
        setState((prev) =>
          prev.key === key
            ? { ...prev, data, error: null, errorStatus: null, isLoading: false }
            : prev
        );
      })
      .catch((err) => {
        if (cancelled) return;
        setState((prev) =>
          prev.key === key
            ? {
                ...prev,
                data: null,
                error: getErrorMessage(err),
                errorStatus: err.response?.status ?? null,
                isLoading: false,
              }
            : prev
        );
      });

    return () => {
      cancelled = true;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [key]);

  const setData = useCallback((updater) => {
    setState((prev) => ({
      ...prev,
      data: typeof updater === "function" ? updater(prev.data) : updater,
    }));
  }, []);

  return {
    data: state.data,
    setData,
    isLoading: state.isLoading,
    error: state.error,
    errorStatus: state.errorStatus,
    reload,
  };
}
