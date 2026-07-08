export const inputClasses =
  "border border-slate-600 rounded-lg p-2.5 bg-slate-900 text-slate-100 placeholder:text-slate-500 focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500 transition";

const TextInput = ({ className = "", ...rest }) => {
  return <input className={`${inputClasses} ${className}`} {...rest} />;
};

export default TextInput;
