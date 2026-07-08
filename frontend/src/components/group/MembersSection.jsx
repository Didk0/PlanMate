import { useState } from "react";
import Alert from "@/components/shared/Alert";
import Button from "@/components/shared/Button";
import CollapsibleSection from "@/components/shared/CollapsibleSection";
import EmptyState from "@/components/shared/EmptyState";
import TextInput from "@/components/shared/TextInput";

const initials = (member) =>
  `${member.firstName?.[0] ?? ""}${member.lastName?.[0] ?? ""}`.toUpperCase();

const MembersSection = ({ members, onAddMember, onRemoveMember }) => {
  const [showAddForm, setShowAddForm] = useState(false);
  const [username, setUsername] = useState("");
  const [validationError, setValidationError] = useState(null);

  const handleAdd = () => {
    if (!username.trim()) {
      setValidationError("Please enter a name");
      return;
    }
    setValidationError(null);
    onAddMember(username);
    setUsername("");
    setShowAddForm(false);
  };

  return (
    <section>
      <h2 className="text-xl font-semibold text-slate-100 mb-4">Members</h2>
      {members.length === 0 ? (
        <EmptyState title="No members yet" description="Add someone to get started." />
      ) : (
        <ul className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-3">
          {members.map((member) => (
            <li
              key={member.id}
              className="flex items-center justify-between gap-3 rounded-xl border border-slate-700 bg-surface p-3 shadow-sm"
            >
              <div className="flex items-center gap-3 min-w-0">
                <span className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-primary-900/50 text-sm font-semibold text-primary-300">
                  {initials(member)}
                </span>
                <span className="truncate font-medium text-slate-100">
                  {member.firstName} {member.lastName}
                </span>
              </div>
              <button
                onClick={() => onRemoveMember(member.id)}
                className="flex h-7 w-7 shrink-0 items-center justify-center rounded-full text-slate-500 transition hover:bg-danger-900/40 hover:text-danger-400"
                aria-label={`Remove member ${member.username}`}
              >
                &times;
              </button>
            </li>
          ))}
        </ul>
      )}

      {/* Toggle Add Member form */}
      <Button variant="secondary" onClick={() => setShowAddForm(!showAddForm)} className="mt-6">
        {showAddForm ? "Cancel" : "Add Member"}
      </Button>

      {/* Animated Add Member form */}
      <CollapsibleSection show={showAddForm} className="mt-4">
        <div className="flex flex-col gap-4 md:flex-row md:items-start">
          <TextInput
            type="text"
            placeholder="Username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            className="w-full"
          />
          <Button onClick={handleAdd} className="shrink-0">
            Add Member
          </Button>
        </div>

        {validationError && (
          <Alert variant="error" className="mt-4">
            {validationError}
          </Alert>
        )}
      </CollapsibleSection>
    </section>
  );
};

export default MembersSection;
