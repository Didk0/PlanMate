import Button from "@/components/shared/Button";
import LinkButton from "@/components/shared/LinkButton";

const ErrorScreen = ({ message, status, onRetry }) => {
  const isForbidden = status === 403;

  return (
    <div className="min-h-screen bg-canvas flex items-center justify-center px-4">
      <div className="w-full max-w-sm bg-surface rounded-xl border border-slate-700 shadow-sm p-8 text-center">
        <div className="mx-auto mb-4 flex h-12 w-12 items-center justify-center rounded-full bg-danger-900/40 text-danger-400">
          <svg
            className="h-6 w-6"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
            aria-hidden="true"
          >
            <path d="M12 9v4" />
            <path d="M12 17h.01" />
            <path d="M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z" />
          </svg>
        </div>
        {isForbidden ? (
          <>
            <h2 className="text-slate-100 font-semibold">You don&apos;t have access</h2>
            <p className="text-slate-400 text-sm mt-2">
              You may not have permission to view this, or it may have been removed.
            </p>
            <div className="mt-6">
              <LinkButton to="/groups" variant="secondary">
                Back to groups
              </LinkButton>
            </div>
          </>
        ) : (
          <>
            <h2 className="text-slate-100 font-semibold">Something went wrong</h2>
            {message && <p className="text-slate-400 text-sm mt-2">{message}</p>}
            {onRetry && (
              <div className="mt-6">
                <Button variant="secondary" onClick={onRetry}>
                  Try again
                </Button>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
};

export default ErrorScreen;
