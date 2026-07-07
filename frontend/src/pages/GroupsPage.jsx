import { useCallback, useState } from "react";
import { Link } from "react-router-dom";
import groupService from "@/api/groupService";
import Button from "@/components/shared/Button";
import CollapsibleSection from "@/components/shared/CollapsibleSection";
import ErrorScreen from "@/components/shared/ErrorScreen";
import LoadingScreen from "@/components/shared/LoadingScreen";
import PageShell from "@/components/shared/PageShell";
import TextInput from "@/components/shared/TextInput";
import { useAsyncData } from "@/hooks/useAsyncData";

const GroupsPage = () => {
  const fetchGroups = useCallback(() => groupService.getAllGroups(), []);
  const { data: groups, setData: setGroups, isLoading, error } = useAsyncData(fetchGroups, []);

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
      const createdGroup = await groupService.createGroup(newGroup);
      setGroups((prev) => [...(prev ?? []), createdGroup]);
      setNewGroup({ name: "", description: "" });
      setShowCreateForm(false);
    } catch (err) {
      setCreateError(err.response?.data?.message || err.message || "Failed to create group");
    } finally {
      setIsCreating(false);
    }
  };

  if (error) {
    return <ErrorScreen message={error} />;
  }

  if (isLoading) {
    return <LoadingScreen message="Loading groups..." />;
  }

  return (
    <PageShell maxWidth="max-w-4xl">
      <h1 className="text-3xl font-extrabold text-yellow-900 mb-6 drop-shadow-md">Groups</h1>

      {groups.length === 0 ? (
        <p className="text-yellow-900 text-lg">You are not member of any groups.</p>
      ) : (
        <ul className="space-y-4">
          {groups.map((group) => (
            <li
              key={group.id}
              className="bg-yellow-200 rounded-md p-4 shadow hover:shadow-lg transition cursor-pointer"
            >
              <Link
                to={`/groups/${group.id}`}
                className="text-yellow-900 font-semibold text-lg hover:underline"
              >
                {group.name}
              </Link>
            </li>
          ))}
        </ul>
      )}

      {/* Create Group button */}
      <Button onClick={() => setShowCreateForm((show) => !show)} className="mt-8 mb-6 w-35">
        {showCreateForm ? "Cancel" : "Create Group"}
      </Button>

      {/* Create Group form */}
      <CollapsibleSection show={showCreateForm} className="mb-8">
        <TextInput
          name="name"
          type="text"
          placeholder="Group Name"
          value={newGroup.name}
          onChange={handleInputChange}
          className="w-full md:w-auto mb-4 md:mb-0 md:mr-4"
        />

        <TextInput
          name="description"
          type="text"
          placeholder="Group Description"
          value={newGroup.description}
          onChange={handleInputChange}
          className="w-full md:w-auto mb-4 md:mb-0 md:mr-4"
        />

        <Button
          onClick={handleCreateGroup}
          disabled={isCreating}
          className="mt-2 md:mt-0 inline-block"
        >
          {isCreating ? "Creating..." : "Create Group"}
        </Button>

        {createError && <p className="text-red-700 font-semibold mt-4">{createError}</p>}
      </CollapsibleSection>
    </PageShell>
  );
};

export default GroupsPage;
