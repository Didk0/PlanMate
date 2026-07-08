import LinkButton from "@/components/shared/LinkButton";
import PageShell from "@/components/shared/PageShell";

const NotFoundPage = () => {
  return (
    <PageShell center maxWidth="max-w-xl" cardClassName="text-center">
      <h1 className="text-4xl font-bold text-slate-100 mb-4">404</h1>
      <p className="text-slate-400 text-lg mb-8">This page doesn&apos;t exist.</p>
      <LinkButton to="/">Back to Home</LinkButton>
    </PageShell>
  );
};

export default NotFoundPage;
