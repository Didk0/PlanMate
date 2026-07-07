const Button = ({ type = "button", className = "", children, ...rest }) => {
  return (
    <button
      type={type}
      className={`bg-yellow-600 hover:bg-yellow-700 text-yellow-50 px-6 py-3 font-semibold rounded-md shadow-md transition ${className}`}
      {...rest}
    >
      {children}
    </button>
  );
};

export default Button;
