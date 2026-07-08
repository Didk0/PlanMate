const Skeleton = ({ className = "" }) => {
  return <div className={`animate-pulse rounded-md bg-slate-700 ${className}`} />;
};

export default Skeleton;
