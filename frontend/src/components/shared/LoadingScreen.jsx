const LoadingScreen = ({ message = "Loading..." }) => {
  return (
    <div className="min-h-screen flex items-center justify-center text-yellow-900 font-semibold text-xl">
      {message}
    </div>
  );
};

export default LoadingScreen;
