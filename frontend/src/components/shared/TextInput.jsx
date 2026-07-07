export const inputClasses =
  "border border-yellow-400 rounded-md p-3 text-yellow-900 font-semibold focus:outline-yellow-500 focus:ring-2 focus:ring-yellow-400 transition";

const TextInput = ({ className = "", ...rest }) => {
  return <input className={`${inputClasses} ${className}`} {...rest} />;
};

export default TextInput;
