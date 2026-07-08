const VARIANTS = {
  error: "bg-danger-900/40 border border-danger-800 text-danger-300",
  warning: "bg-warning-900/40 border border-warning-800 text-warning-300",
  info: "bg-primary-900/40 border border-primary-800 text-primary-300",
  success: "bg-success-900/40 border border-success-800 text-success-300",
};

const Alert = ({ variant = "error", children, className = "" }) => {
  return (
    <div
      role="alert"
      className={`rounded-lg px-4 py-3 text-sm font-medium ${VARIANTS[variant]} ${className}`}
    >
      {children}
    </div>
  );
};

export default Alert;
