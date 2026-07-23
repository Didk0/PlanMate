import { useSelector } from "react-redux";
import { Navigate, Outlet } from "react-router-dom";
import { selectIsAdmin } from "@/store/authSlice";

const RequireRole = ({ role }) => {
  const isAdmin = useSelector(selectIsAdmin);
  const hasRole = role === "ADMIN" ? isAdmin : false;

  return hasRole ? <Outlet /> : <Navigate to="/groups" replace />;
};

export default RequireRole;
