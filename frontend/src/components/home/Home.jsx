import { useNavigate } from "react-router-dom";

const Home = () => {
  const navigate = useNavigate();

  const handleGetStarted = () => {
    navigate("/groups");
  };

  return (
    <div className="min-h-screen bg-gradient-to-r from-yellow-400 via-yellow-300 to-yellow-200 flex items-center justify-center px-6">
      <div className="max-w-3xl w-full text-center rounded-lg shadow-lg bg-yellow-100 bg-opacity-80 p-20">
        <h1 className="text-4xl md:text-5xl font-extrabold text-yellow-900 mb-8 drop-shadow-md">
          Welcome To PlanMate
        </h1>
        <p className="text-lg md:text-xl text-yellow-900 mb-8 max-w-xl mx-auto">
          Track group expenses, view settlements, and keep everything balanced
          easily.
        </p>
        <button
          onClick={handleGetStarted}
          className="bg-yellow-600 text-yellow-50 font-semibold rounded-lg px-8 py-3 mt-6 shadow-md hover:bg-yellow-700 transition"
        >
          Get Started
        </button>
      </div>
    </div>
  );
};

export default Home;
