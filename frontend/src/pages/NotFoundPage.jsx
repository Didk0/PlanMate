import { Link } from "react-router-dom";
import Button from "@/components/shared/Button";
import PageShell from "@/components/shared/PageShell";

const NotFoundPage = () => {
  return (
    <PageShell center maxWidth="max-w-xl" cardClassName="text-center">
      <h1 className="text-4xl font-extrabold text-yellow-900 mb-4 drop-shadow-md">404</h1>
      <p className="text-yellow-900 text-lg mb-8">This page doesn&apos;t exist.</p>
      <Link to="/">
        <Button>Back to Home</Button>
      </Link>
    </PageShell>
  );
};

export default NotFoundPage;
