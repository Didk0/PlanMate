import { useContext } from "react";
import { AuthContext } from "react-oauth2-code-pkce";
import { Navigate, Outlet } from "react-router-dom";

const RequireAuth = () => {
  const { token } = useContext(AuthContext);

  if (!token) {
    return <Navigate to="/" replace />;
  }

  return <Outlet />;
};

export default RequireAuth;
