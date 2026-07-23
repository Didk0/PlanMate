import { useSelector } from "react-redux";
import { Navigate, Outlet } from "react-router-dom";

const RequireRole = ({ role }) => {
  const roles = useSelector((state) => state.auth.user?.roles ?? []);
  const hasRole = roles.includes(role);

  return hasRole ? <Outlet /> : <Navigate to="/groups" replace />;
};

export default RequireRole;
