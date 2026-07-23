import { useContext } from "react";
import { AuthContext } from "react-oauth2-code-pkce";
import { useDispatch, useSelector } from "react-redux";
import { Link, useNavigate } from "react-router-dom";
import Button from "@/components/shared/Button";
import { logoutUser, selectIsAdmin } from "@/store/authSlice";

const Navbar = () => {
  const { token, logIn, logOut } = useContext(AuthContext);
  const dispatch = useDispatch();
  const { user } = useSelector((state) => state.auth);
  const isAdmin = useSelector(selectIsAdmin);
  const navigate = useNavigate();

  const handleLogin = () => {
    logIn();
  };

  const handleLogout = async () => {
    await logOut({
      additionalParameters: {
        returnTo: "/",
      },
    });
    dispatch(logoutUser());
    navigate("/");
  };

  return (
    <nav className="sticky top-0 z-40 border-b border-slate-800 bg-slate-900">
      <div className="mx-auto flex max-w-6xl items-center gap-6 px-4 py-3 sm:px-6">
        <Link to="/" className="text-lg font-bold text-slate-100">
          Plan<span className="text-primary-400">Mate</span>
        </Link>

        {token && (
          <Link
            to="/groups"
            className="text-sm font-medium text-slate-400 transition hover:text-slate-100"
          >
            Groups
          </Link>
        )}

        {token && isAdmin && (
          <Link
            to="/admin/users"
            className="text-sm font-medium text-slate-400 transition hover:text-slate-100"
          >
            Users
          </Link>
        )}

        <div className="ml-auto">
          {token ? (
            <Button variant="ghost" size="sm" onClick={handleLogout}>
              Logout ({user?.name})
            </Button>
          ) : (
            <Button size="sm" onClick={handleLogin}>
              Login
            </Button>
          )}
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
