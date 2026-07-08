import Spinner from "@/components/shared/Spinner";

const LoadingScreen = ({ message = "Loading..." }) => {
  return (
    <div className="min-h-screen bg-canvas flex flex-col items-center justify-center gap-4">
      <Spinner size="lg" className="text-primary-400" />
      <p className="text-slate-400 font-medium">{message}</p>
    </div>
  );
};

export default LoadingScreen;
