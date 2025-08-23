import { Link } from "react-router-dom";

const Navbar = () => {
  return (
    <nav className="bg-yellow-600 text-white p-4 shadow-md">
      <div className="max-w-6xl mx-auto flex space-x-15">
        <Link
          to="/"
          className="text-lg font-medium hover:text-yellow-300 hover:underline transition-colors duration-200"
        >
          Home
        </Link>
        <Link
          to="/groups"
          className="text-lg font-medium hover:text-yellow-300 hover:underline transition-colors duration-200"
        >
          Groups
        </Link>
        <Link
          to="/login"
          className="ml-auto text-lg font-medium hover:text-yellow-300 hover:underline transition-colors duration-200"
        >
          Login
        </Link>
      </div>
    </nav>
  );
};

export default Navbar;
