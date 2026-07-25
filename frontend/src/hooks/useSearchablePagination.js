import { useState } from "react";
import { useDebounce } from "@/hooks/useDebounce";

export function useSearchablePagination() {
  const [page, setPage] = useState(0);
  const [searchTerm, setSearchTerm] = useState("");
  const debouncedSearch = useDebounce(searchTerm);

  const [appliedSearch, setAppliedSearch] = useState(debouncedSearch);
  if (appliedSearch !== debouncedSearch) {
    setAppliedSearch(debouncedSearch);
    setPage(0);
  }

  return {
    page,
    setPage,
    searchTerm,
    setSearchTerm,
    debouncedSearch,
    isSearching: searchTerm !== debouncedSearch,
  };
}
