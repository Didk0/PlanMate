const ErrorScreen = ({ message }) => {
  return (
    <div className="min-h-screen flex items-center justify-center text-red-700 font-semibold text-lg px-4">
      {message}
    </div>
  );
};

export default ErrorScreen;
