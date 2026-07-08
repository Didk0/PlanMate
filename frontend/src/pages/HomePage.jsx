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
      <h1 className="text-4xl md:text-5xl font-bold text-slate-100 mb-6">Welcome to PlanMate</h1>
      <p className="text-lg md:text-xl text-slate-400 mb-8 max-w-xl mx-auto">
        Track group expenses, view settlements, and keep everything balanced easily.
      </p>
      <Button size="lg" onClick={handleGetStarted}>
        Get Started
      </Button>
    </PageShell>
  );
};

export default HomePage;
