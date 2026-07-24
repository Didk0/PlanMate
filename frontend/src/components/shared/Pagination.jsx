import Button from "@/components/shared/Button";

const MAX_VISIBLE_PAGES = 5;

const getVisiblePages = (page, totalPages) => {
  const half = Math.floor(MAX_VISIBLE_PAGES / 2);
  let start = Math.max(0, page - half);
  const end = Math.min(totalPages, start + MAX_VISIBLE_PAGES);
  start = Math.max(0, end - MAX_VISIBLE_PAGES);

  return Array.from({ length: end - start }, (_, i) => start + i);
};

const Pagination = ({ page, totalPages, onPageChange }) => {
  if (totalPages <= 1) return null;

  const visiblePages = getVisiblePages(page, totalPages);

  return (
    <nav className="mt-6 flex items-center justify-center gap-2" aria-label="Pagination">
      <Button
        variant="secondary"
        size="sm"
        disabled={page === 0}
        onClick={() => onPageChange(page - 1)}
      >
        Prev
      </Button>

      {visiblePages.map((p) => (
        <Button
          key={p}
          variant={p === page ? "primary" : "secondary"}
          size="sm"
          onClick={() => onPageChange(p)}
          aria-current={p === page ? "page" : undefined}
        >
          {p + 1}
        </Button>
      ))}

      <Button
        variant="secondary"
        size="sm"
        disabled={page + 1 >= totalPages}
        onClick={() => onPageChange(page + 1)}
      >
        Next
      </Button>
    </nav>
  );
};

export default Pagination;
