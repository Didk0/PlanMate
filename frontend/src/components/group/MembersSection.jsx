import { useState } from "react";
import Button from "@/components/shared/Button";
import CollapsibleSection from "@/components/shared/CollapsibleSection";
import TextInput from "@/components/shared/TextInput";

const MembersSection = ({ members, onAddMember, onRemoveMember }) => {
  const [showAddForm, setShowAddForm] = useState(false);
  const [username, setUsername] = useState("");

  const handleAdd = () => {
    if (!username.trim()) {
      alert("Please enter a name");
      return;
    }
    onAddMember(username);
    setUsername("");
    setShowAddForm(false);
  };

  return (
    <section>
      <h2 className="text-2xl font-semibold text-yellow-900 mb-6 drop-shadow-sm">Members</h2>
      {members.length === 0 ? (
        <p className="text-yellow-900 text-lg">No members yet.</p>
      ) : (
        <ul className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4">
          {members.map((member) => (
            <li
              key={member.id}
              className="flex justify-between items-center p-4 rounded-md bg-yellow-200 shadow hover:shadow-lg transition cursor-default"
            >
              <span className="text-yellow-900 font-medium">
                {member.firstName} {member.lastName}
              </span>
              <button
                onClick={() => onRemoveMember(member.id)}
                className="text-white bg-red-600 hover:bg-red-700 transition px-3 py-1 rounded-md text-sm font-semibold shadow"
                aria-label={`Remove member ${member.username}`}
              >
                &times;
              </button>
            </li>
          ))}
        </ul>
      )}

      {/* Toggle Add Member form */}
      <Button onClick={() => setShowAddForm(!showAddForm)} className="mt-8 w-34">
        {showAddForm ? "Cancel" : "Add Member"}
      </Button>

      {/* Animated Add Member form */}
      <CollapsibleSection show={showAddForm} className="mt-6">
        <TextInput
          type="text"
          placeholder="Username"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          className="w-full md:w-auto mb-4 md:mb-0 md:mr-4"
        />
        <Button onClick={handleAdd} className="mt-2 md:mt-0 inline-block">
          Add Member
        </Button>
      </CollapsibleSection>
    </section>
  );
};

export default MembersSection;
