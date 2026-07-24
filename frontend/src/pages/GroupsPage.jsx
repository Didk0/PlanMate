import { motion } from "framer-motion";
import { useCallback, useState } from "react";
import { Link } from "react-router-dom";
import groupService from "@/api/groupService";
import Alert from "@/components/shared/Alert";
import Button from "@/components/shared/Button";
import CollapsibleSection from "@/components/shared/CollapsibleSection";
import EmptyState from "@/components/shared/EmptyState";
import ErrorScreen from "@/components/shared/ErrorScreen";
import PageShell from "@/components/shared/PageShell";
import Pagination from "@/components/shared/Pagination";
import Skeleton from "@/components/shared/Skeleton";
import TextInput from "@/components/shared/TextInput";
import { useAsyncData } from "@/hooks/useAsyncData";
import { itemVariants, listVariants } from "@/lib/motion";
import { notifyError, notifySuccess } from "@/lib/notify";

const GROUPS_PAGE_SIZE = 5;

const GroupsPage = () => {
  const [page, setPage] = useState(0);
  const fetchGroups = useCallback(() => groupService.getAllGroups(page, GROUPS_PAGE_SIZE), [page]);
  const { data, isLoading, error, errorStatus, reload } = useAsyncData(fetchGroups, [page]);
  const groups = data?.content ?? [];

  const [showCreateForm, setShowCreateForm] = useState(false);
  const [newGroup, setNewGroup] = useState({ name: "", description: "" });
  const [isCreating, setIsCreating] = useState(false);
  const [createError, setCreateError] = useState(null);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setNewGroup((prev) => ({ ...prev, [name]: value }));
  };

  const handleCreateGroup = async () => {
    const { name, description } = newGroup;
    if (!name.trim() || !description.trim()) {
      setCreateError("Please enter both a group name and description.");
      return;
    }
    setIsCreating(true);
    setCreateError(null);
    try {
      await groupService.createGroup(newGroup);
      setNewGroup({ name: "", description: "" });
      setShowCreateForm(false);
      if (page === 0) {
        reload();
      } else {
        setPage(0);
      }
      notifySuccess("Group created");
    } catch (err) {
      notifyError(err, "Failed to create group");
    } finally {
      setIsCreating(false);
    }
  };

  if (error) {
    return <ErrorScreen message={error} status={errorStatus} onRetry={reload} />;
  }

  return (
    <PageShell maxWidth="max-w-4xl">
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-3xl font-bold text-slate-100">Groups</h1>
        <Button onClick={() => setShowCreateForm((show) => !show)}>
          {showCreateForm ? "Cancel" : "Create Group"}
        </Button>
      </div>

      {isLoading ? (
        <div className="space-y-3">
          <Skeleton className="h-16 w-full" />
          <Skeleton className="h-16 w-full" />
          <Skeleton className="h-16 w-full" />
        </div>
      ) : groups.length === 0 ? (
        <EmptyState
          title="No groups yet"
          description="Create a group to start tracking shared expenses."
        />
      ) : (
        <>
          <motion.ul
            className="space-y-3"
            variants={listVariants}
            initial="hidden"
            animate="visible"
          >
            {groups.map((group) => (
              <motion.li key={group.id} variants={itemVariants}>
                <Link
                  to={`/groups/${group.id}`}
                  className="block rounded-xl border border-slate-700 bg-surface p-4 shadow-sm transition hover:border-primary-500 hover:shadow-md"
                >
                  <span className="font-semibold text-slate-100">{group.name}</span>
                </Link>
              </motion.li>
            ))}
          </motion.ul>
          <Pagination page={page} totalPages={data.totalPages} onPageChange={setPage} />
        </>
      )}

      {/* Create Group form */}
      <CollapsibleSection show={showCreateForm} className="mt-6">
        <div className="flex flex-col gap-4 md:flex-row md:items-start">
          <TextInput
            name="name"
            type="text"
            placeholder="Group Name"
            value={newGroup.name}
            onChange={handleInputChange}
            className="w-full"
          />

          <TextInput
            name="description"
            type="text"
            placeholder="Group Description"
            value={newGroup.description}
            onChange={handleInputChange}
            className="w-full"
          />

          <Button onClick={handleCreateGroup} isLoading={isCreating} className="shrink-0">
            Create Group
          </Button>
        </div>

        {createError && (
          <Alert variant="error" className="mt-4">
            {createError}
          </Alert>
        )}
      </CollapsibleSection>
    </PageShell>
  );
};

export default GroupsPage;
