const BASE =
  "inline-flex items-center justify-center gap-2 font-semibold rounded-lg transition active:scale-[0.98] disabled:opacity-50 disabled:pointer-events-none focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary-400 focus-visible:ring-offset-2 focus-visible:ring-offset-slate-900";

const VARIANTS = {
  primary: "bg-primary-500 hover:bg-primary-400 text-white shadow-sm",
  secondary: "bg-slate-800 border border-slate-600 text-slate-200 hover:bg-slate-700 shadow-sm",
  ghost: "text-slate-300 hover:bg-slate-800 hover:text-white",
  danger: "bg-danger-600 hover:bg-danger-500 text-white shadow-sm",
};

const SIZES = {
  sm: "px-3 py-1.5 text-sm",
  md: "px-4 py-2",
  lg: "px-6 py-3 text-base",
};

export const buttonClasses = (variant = "primary", size = "md") =>
  `${BASE} ${VARIANTS[variant]} ${SIZES[size]}`;
