import { useNavigate } from "react-router-dom";

const BackButton = ({ to, className = "" }) => {
  const navigate = useNavigate();

  return (
    <button
      type="button"
      onClick={() => navigate(to)}
      className={`inline-flex items-center gap-1 text-sm font-medium text-slate-400 hover:text-slate-100 transition ${className}`}
    >
      &larr; Back
    </button>
  );
};

export default BackButton;
