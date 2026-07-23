import { useContext } from "react";
import { AuthContext } from "react-oauth2-code-pkce";
import { Navigate, Outlet } from "react-router-dom";
import ErrorScreen from "@/components/shared/ErrorScreen";
import LoadingScreen from "@/components/shared/LoadingScreen";
import { useProvisionUser } from "./useProvisionUser";

const RequireAuth = () => {
  const { token, tokenData } = useContext(AuthContext);
  const { status, error, retry } = useProvisionUser(token, tokenData);

  if (!token) {
    return <Navigate to="/" replace />;
  }

  if (status === "pending") {
    return <LoadingScreen message="Setting up your account..." />;
  }

  if (status === "error") {
    return <ErrorScreen message={error} onRetry={retry} />;
  }

  return <Outlet />;
};

export default RequireAuth;
