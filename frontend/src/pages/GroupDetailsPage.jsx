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
import LoadingScreen from "@/components/shared/LoadingScreen";
import PageShell from "@/components/shared/PageShell";
import { useAsyncData } from "@/hooks/useAsyncData";
import { useGroupWebSocket } from "@/hooks/useGroupWebSocket";

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
  const [memberError, setMemberError] = useState(null);

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
    setMemberError(null);
    try {
      const addedMember = await userService.addMemberToGroup(groupId, { username });
      setMembers((prev) =>
        prev.some((m) => m.id === addedMember.id) ? prev : [...prev, addedMember]
      );
    } catch (err) {
      setMemberError(err.response?.data?.message || err.message || "Failed to add member");
    }
  };

  const handleRemoveMember = async (memberId) => {
    setMemberError(null);
    try {
      await userService.removeMemberFromGroup(groupId, memberId);
      setMembers((prev) => prev.filter((m) => m.id !== memberId));
    } catch (err) {
      setMemberError(err.response?.data?.message || err.message || "Failed to remove member");
    }
  };

  const currentMember = members.find((m) => m.username === tokenData?.preferred_username);

  const handleConfirmLeaveGroup = async () => {
    if (!currentMember) return;
    setShowLeaveConfirm(false);
    try {
      await userService.removeMemberFromGroup(groupId, currentMember.id);
      navigate("/groups");
    } catch (err) {
      setMemberError(err.response?.data?.message || err.message || "Failed to leave group");
    }
  };

  if (error) {
    return <ErrorScreen message={error} />;
  }

  if (isLoading || !data) {
    return <LoadingScreen message="Loading group..." />;
  }

  const group = data.groupData;

  return (
    <PageShell maxWidth="max-w-5xl" cardClassName="flex flex-col relative">
      {/* Back Button */}
      <div className="flex justify-start mb-3">
        <BackButton to="/groups" />
      </div>

      {/* Header */}
      <header className="space-y-4 mb-6">
        <h1 className="text-4xl font-extrabold text-yellow-900 drop-shadow-md">{group.name}</h1>
        {group.description && <p className="text-yellow-900 text-lg">{group.description}</p>}
      </header>

      {/* Members Section */}
      <section className="mb-8">
        <MembersSection
          members={members}
          onAddMember={handleAddMember}
          onRemoveMember={handleRemoveMember}
          error={memberError}
        />
      </section>

      {/* Toggle Expenses Button */}
      <div className="flex justify-center">
        <Button onClick={() => setShowExpenses(!showExpenses)} className="mb-6">
          {showExpenses ? "Hide Expenses" : "Show Expenses"}
        </Button>
      </div>

      {/* Expenses Section */}
      <AnimatePresence initial={false}>
        {showExpenses && (
          <motion.div
            key="expenses"
            initial={{ opacity: 0, height: 0, scaleY: 0, overflow: "hidden" }}
            animate={{ opacity: 1, height: "auto", scaleY: 1 }}
            exit={{ opacity: 0, height: 0, scaleY: 0, overflow: "hidden" }}
            transition={{ duration: 0.4, ease: "easeInOut" }}
            style={{ transformOrigin: "top" }}
            className="overflow-hidden"
          >
            <ExpensesSection expenses={expenses} groupId={groupId} />
          </motion.div>
        )}
      </AnimatePresence>

      {/* Leave Group Button */}
      {currentMember && (
        <div className="absolute bottom-6 right-6">
          <Button onClick={() => setShowLeaveConfirm(true)} className="mt-6">
            Leave Group
          </Button>
        </div>
      )}

      <ConfirmDialog
        open={showLeaveConfirm}
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
