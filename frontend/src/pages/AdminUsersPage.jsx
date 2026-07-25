import { useCallback, useState } from "react";
import { useSelector } from "react-redux";
import userService from "@/api/userService";
import Button from "@/components/shared/Button";
import ConfirmDialog from "@/components/shared/ConfirmDialog";
import EmptyState from "@/components/shared/EmptyState";
import ErrorScreen from "@/components/shared/ErrorScreen";
import PageShell from "@/components/shared/PageShell";
import Pagination from "@/components/shared/Pagination";
import SearchInput from "@/components/shared/SearchInput";
import Skeleton from "@/components/shared/Skeleton";
import { useAsyncData } from "@/hooks/useAsyncData";
import { useSearchablePagination } from "@/hooks/useSearchablePagination";
import { notifyError, notifySuccess } from "@/lib/notify";
import { selectAppUserId } from "@/store/authSlice";

const USERS_PAGE_SIZE = 10;

const AdminUsersPage = () => {
  const appUserId = useSelector(selectAppUserId);

  const { page, setPage, searchTerm, setSearchTerm, debouncedSearch, isSearching } =
    useSearchablePagination();

  const fetchUsers = useCallback(
    () => userService.getAllUsers(page, USERS_PAGE_SIZE, debouncedSearch),
    [page, debouncedSearch]
  );
  const { data, isLoading, error, errorStatus, reload } = useAsyncData(fetchUsers, [
    page,
    debouncedSearch,
  ]);
  const users = data?.content ?? [];

  const [userToDelete, setUserToDelete] = useState(null);

  const handleConfirmDelete = async () => {
    const userId = userToDelete;
    setUserToDelete(null);
    try {
      await userService.deleteUser(userId);
      if (users.length === 1 && page > 0) {
        setPage((prev) => prev - 1);
      } else {
        reload();
      }
      notifySuccess("User deleted");
    } catch (err) {
      notifyError(err, "Failed to delete user");
    }
  };

  if (error) {
    return <ErrorScreen message={error} status={errorStatus} onRetry={reload} />;
  }

  return (
    <PageShell maxWidth="max-w-4xl">
      <h1 className="text-3xl font-bold text-slate-100 mb-6">Users</h1>

      <SearchInput
        value={searchTerm}
        onChange={(e) => setSearchTerm(e.target.value)}
        placeholder="Search by username..."
        isSearching={isSearching}
        className="mb-6"
      />

      {isLoading ? (
        <div className="space-y-3">
          <Skeleton className="h-16 w-full" />
          <Skeleton className="h-16 w-full" />
          <Skeleton className="h-16 w-full" />
        </div>
      ) : users.length === 0 ? (
        <EmptyState title={debouncedSearch ? "No matching users" : "No users yet"} />
      ) : (
        <div className="overflow-x-auto">
          <table className="w-full text-left">
            <thead>
              <tr className="border-b border-slate-700 text-sm text-slate-400">
                <th className="py-2 pr-4 font-medium">Username</th>
                <th className="py-2 pr-4 font-medium">Name</th>
                <th className="py-2 pr-4 font-medium">Email</th>
                <th className="py-2 pr-4 font-medium"></th>
              </tr>
            </thead>
            <tbody>
              {users.map((user) => (
                <tr key={user.id} className="border-b border-slate-800">
                  <td className="py-3 pr-4 font-medium text-slate-100">{user.username}</td>
                  <td className="py-3 pr-4 text-slate-300">
                    {user.firstName} {user.lastName}
                  </td>
                  <td className="py-3 pr-4 text-slate-300">{user.email}</td>
                  <td className="py-3 pr-4 text-right">
                    <Button
                      variant="danger"
                      size="sm"
                      disabled={user.id === appUserId}
                      onClick={() => setUserToDelete(user.id)}
                    >
                      Delete
                    </Button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          <Pagination page={page} totalPages={data.totalPages} onPageChange={setPage} />
        </div>
      )}

      <ConfirmDialog
        open={userToDelete !== null}
        variant="danger"
        title="Delete user?"
        message="This will permanently delete the user. This action cannot be undone."
        confirmLabel="Delete"
        onConfirm={handleConfirmDelete}
        onCancel={() => setUserToDelete(null)}
      />
    </PageShell>
  );
};

export default AdminUsersPage;
