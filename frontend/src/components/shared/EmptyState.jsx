const EmptyState = ({ icon = null, title, description = null, action = null, className = "" }) => {
  return (
    <div
      className={`border border-dashed border-slate-700 rounded-xl py-10 px-6 text-center ${className}`}
    >
      {icon && <div className="mx-auto mb-3 flex justify-center text-slate-500">{icon}</div>}
      <p className="text-slate-100 font-semibold">{title}</p>
      {description && <p className="text-slate-400 text-sm mt-1">{description}</p>}
      {action && <div className="mt-4 flex justify-center">{action}</div>}
    </div>
  );
};

export default EmptyState;
