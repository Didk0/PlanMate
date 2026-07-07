import { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { Link } from "react-router-dom";
import Button from "@/components/shared/Button";
import CollapsibleSection from "@/components/shared/CollapsibleSection";
import ErrorScreen from "@/components/shared/ErrorScreen";
import LoadingScreen from "@/components/shared/LoadingScreen";
import PageShell from "@/components/shared/PageShell";
import TextInput from "@/components/shared/TextInput";
import { createGroup, loadAllGroups } from "@/store/actions";

const GroupsPage = () => {
  const { isLoading, errorMessage } = useSelector((state) => state.errors);

  const [groups, setGroups] = useState([]);

  const dispatch = useDispatch();

  useEffect(() => {
    const fetchGroups = async () => {
      try {
        const data = await dispatch(loadAllGroups());
        if (data) {
          setGroups(data);
        }
      } catch (error) {
        console.error("Failed to refresh token or load groups", error);
      }
    };
    fetchGroups();
  }, [dispatch]);

  const [showCreateForm, setShowCreateForm] = useState(false);
  const [newGroup, setNewGroup] = useState({ name: "", description: "" });

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setNewGroup((prev) => ({ ...prev, [name]: value }));
  };

  const handleCreateGroup = async () => {
    const { name, description } = newGroup;
    if (!name.trim() || !description.trim()) {
      alert("Please enter both a group name and description.");
      return;
    }
    try {
      const createdGroup = await dispatch(createGroup(newGroup));
      if (createdGroup) {
        setGroups((prev) => [...prev, createdGroup]);
        setNewGroup({ name: "", description: "" });
        setShowCreateForm(false);
      }
    } catch (error) {
      console.error("Create group failed", error);
    }
  };

  if (errorMessage) {
    return <ErrorScreen message={errorMessage} />;
  }

  if (isLoading || !groups) {
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

        <Button onClick={handleCreateGroup} className="mt-2 md:mt-0 inline-block">
          Create Group
        </Button>
      </CollapsibleSection>
    </PageShell>
  );
};

export default GroupsPage;
