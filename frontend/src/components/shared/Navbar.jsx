import { useContext } from "react";
import { AuthContext } from "react-oauth2-code-pkce";
import { useDispatch, useSelector } from "react-redux";
import { Link, useNavigate } from "react-router-dom";
import { clearAuthData } from "../../store/actions";

const Navbar = () => {
  const { token, logIn, logOut } = useContext(AuthContext);
  const dispatch = useDispatch();
  const { user } = useSelector((state) => state.auth);
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
    dispatch(clearAuthData());
    navigate("/");
  };

  return (
    <nav className="bg-yellow-600 text-white p-4 shadow-md">
      <div className="max-w-6xl mx-auto flex space-x-15">
        <Link
          to="/"
          className="text-lg font-medium hover:text-yellow-300 hover:underline transition-colors duration-200"
        >
          Home
        </Link>

        {token && (
          <Link
            to="/groups"
            className="text-lg font-medium hover:text-yellow-300 hover:underline transition-colors duration-200"
          >
            Groups
          </Link>
        )}

        <div className="ml-auto">
          {token ? (
            <button
              onClick={handleLogout}
              className="text-lg font-medium hover:text-yellow-300 hover:underline transition-colors duration-200 bg-transparent border-none cursor-pointer"
            >
              Logout ({user?.name})
            </button>
          ) : (
            <button
              onClick={handleLogin}
              className="text-lg font-medium hover:text-yellow-300 hover:underline transition-colors duration-200 bg-transparent border-none cursor-pointer"
            >
              Login
            </button>
          )}
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
