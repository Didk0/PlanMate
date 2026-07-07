import { useNavigate } from "react-router-dom";

const BackButton = ({ to, className = "" }) => {
  const navigate = useNavigate();

  return (
    <button
      type="button"
      onClick={() => navigate(to)}
      className={`font-semibold text-yellow-700 hover:text-yellow-900 transition ${className}`}
    >
      &larr; Back
    </button>
  );
};

export default BackButton;
