import { useContext } from "react";
import { AuthContext } from "react-oauth2-code-pkce";
import { useNavigate } from "react-router-dom";
import Button from "@/components/shared/Button";
import PageShell from "@/components/shared/PageShell";

const HomePage = () => {
  const { token, logIn } = useContext(AuthContext);
  const navigate = useNavigate();

  const handleGetStarted = () => {
    if (token) {
      navigate("/groups");
    } else {
      logIn();
    }
  };

  return (
    <PageShell center maxWidth="max-w-3xl" padding="p-20" cardClassName="text-center">
      <h1 className="text-4xl md:text-5xl font-extrabold text-yellow-900 mb-8 drop-shadow-md">
        Welcome To PlanMate
      </h1>
      <p className="text-lg md:text-xl text-yellow-900 mb-8 max-w-xl mx-auto">
        Track group expenses, view settlements, and keep everything balanced easily.
      </p>
      <Button onClick={handleGetStarted} className="mt-6">
        Get Started
      </Button>
    </PageShell>
  );
};

export default HomePage;
