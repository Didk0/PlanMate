import ExpensesSection from "@/components/group/ExpensesSection";
import MembersSection from "@/components/group/MembersSection";
import { AnimatePresence } from "framer-motion";
import { motion } from "framer-motion";
import { useCallback, useContext, useState } from "react";
import { AuthContext } from "react-oauth2-code-pkce";
import { useNavigate, useParams } from "react-router-dom";
import groupService from "@/api/groupService";
import userService from "@/api/userService";
import BackButton from "@/components/shared/BackButton";
import Button from "@/components/shared/Button";
import ConfirmDialog from "@/components/shared/ConfirmDialog";
import ErrorScreen from "@/components/shared/ErrorScreen";
import PageShell from "@/components/shared/PageShell";
import Skeleton from "@/components/shared/Skeleton";
import { useAsyncData } from "@/hooks/useAsyncData";
import { useGroupWebSocket } from "@/hooks/useGroupWebSocket";
import { notifyError, notifySuccess } from "@/lib/notify";

const GroupDetailsPage = () => {
  const { id } = useParams();
  const groupId = id;
  const navigate = useNavigate();
  const { tokenData } = useContext(AuthContext);

  const fetchDetails = useCallback(() => groupService.getGroupDetails(groupId), [groupId]);
  const { data, isLoading, error, reload } = useAsyncData(fetchDetails, [groupId]);

  const [loadedData, setLoadedData] = useState(null);
  const [members, setMembers] = useState([]);
  const [expenses, setExpenses] = useState([]);

  const [showExpenses, setShowExpenses] = useState(false);
  const [showLeaveConfirm, setShowLeaveConfirm] = useState(false);

  if (data && data !== loadedData) {
    setLoadedData(data);
    setMembers(data.membersData);
    setExpenses(data.expensesData);
  }

  useGroupWebSocket(groupId, (topic) => {
    if (topic.endsWith("/users") || topic.endsWith("/expenses")) {
      reload();
    }
  });

  const handleAddMember = async (username) => {
    try {
      const addedMember = await userService.addMemberToGroup(groupId, { username });
      setMembers((prev) =>
        prev.some((m) => m.id === addedMember.id) ? prev : [...prev, addedMember]
      );
      notifySuccess("Member added");
    } catch (err) {
      notifyError(err, "Failed to add member");
    }
  };

  const handleRemoveMember = async (memberId) => {
    try {
      await userService.removeMemberFromGroup(groupId, memberId);
      setMembers((prev) => prev.filter((m) => m.id !== memberId));
      notifySuccess("Member removed");
    } catch (err) {
      notifyError(err, "Failed to remove member");
    }
  };

  const currentMember = members.find((m) => m.username === tokenData?.preferred_username);

  const handleConfirmLeaveGroup = async () => {
    if (!currentMember) return;
    setShowLeaveConfirm(false);
    try {
      await userService.removeMemberFromGroup(groupId, currentMember.id);
      notifySuccess("You left the group");
      navigate("/groups");
    } catch (err) {
      notifyError(err, "Failed to leave group");
    }
  };

  if (error) {
    return <ErrorScreen message={error} onRetry={reload} />;
  }

  if (isLoading || !data) {
    return (
      <PageShell maxWidth="max-w-5xl" cardClassName="flex flex-col">
        <Skeleton className="h-4 w-24 mb-6" />
        <Skeleton className="h-9 w-2/3 mb-3" />
        <Skeleton className="h-5 w-1/2 mb-8" />
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4 mb-8">
          <Skeleton className="h-16 w-full" />
          <Skeleton className="h-16 w-full" />
          <Skeleton className="h-16 w-full" />
        </div>
        <Skeleton className="h-10 w-40 mx-auto" />
      </PageShell>
    );
  }

  const group = data.groupData;

  return (
    <PageShell maxWidth="max-w-5xl" cardClassName="flex flex-col">
      {/* Back Button */}
      <div className="flex justify-start mb-3">
        <BackButton to="/groups" />
      </div>

      {/* Header */}
      <header className="space-y-2 mb-6">
        <h1 className="text-3xl font-bold text-slate-100">{group.name}</h1>
        {group.description && <p className="text-slate-400">{group.description}</p>}
      </header>

      {/* Members Section */}
      <section className="mb-8">
        <MembersSection
          members={members}
          onAddMember={handleAddMember}
          onRemoveMember={handleRemoveMember}
        />
      </section>

      {/* Toggle Expenses Button */}
      <div className="flex justify-center">
        <Button variant="secondary" onClick={() => setShowExpenses(!showExpenses)} className="mb-6">
          {showExpenses ? "Hide Expenses" : "Show Expenses"}
        </Button>
      </div>

      {/* Expenses Section */}
      <AnimatePresence initial={false}>
        {showExpenses && (
          <motion.div
            key="expenses"
            initial={{ opacity: 0, height: 0, overflow: "hidden" }}
            animate={{ opacity: 1, height: "auto" }}
            exit={{ opacity: 0, height: 0, overflow: "hidden" }}
            transition={{ duration: 0.3, ease: "easeInOut" }}
            className="overflow-hidden"
          >
            <ExpensesSection expenses={expenses} groupId={groupId} />
          </motion.div>
        )}
      </AnimatePresence>

      {/* Leave Group */}
      {currentMember && (
        <div className="mt-8 pt-6 border-t border-slate-700 flex justify-end">
          <Button variant="danger" onClick={() => setShowLeaveConfirm(true)}>
            Leave Group
          </Button>
        </div>
      )}

      <ConfirmDialog
        open={showLeaveConfirm}
        variant="danger"
        title="Leave group?"
        message="Are you sure you want to leave this group?"
        confirmLabel="Leave"
        onConfirm={handleConfirmLeaveGroup}
        onCancel={() => setShowLeaveConfirm(false)}
      />
    </PageShell>
  );
};

export default GroupDetailsPage;
