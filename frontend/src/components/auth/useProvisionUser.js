import { useEffect, useRef } from "react";
import userService from "@/api/userService";

export function useProvisionUser(token, tokenData) {
  const provisionedSubRef = useRef(null);

  useEffect(() => {
    if (!token || !tokenData) return;

    const sub = tokenData.sub;
    const sessionKey = `userCreated:${sub}`;

    if (provisionedSubRef.current === sub || sessionStorage.getItem(sessionKey)) return;

    const userData = {
      username: tokenData.preferred_username,
      email: tokenData.email,
      firstName: tokenData.given_name,
      lastName: tokenData.family_name,
      keycloakId: sub,
    };

    userService
      .createUser(userData)
      .then(() => {
        provisionedSubRef.current = sub;
        sessionStorage.setItem(sessionKey, "true");
      })
      .catch((err) => {
        if (err.response?.status === 409) {
          provisionedSubRef.current = sub;
          sessionStorage.setItem(sessionKey, "true");
          return;
        }
        console.error("Failed to provision user", err);
      });
  }, [token, tokenData]);
}
