import ExpensesSection from "../expenses/ExpensesSection";
import MembersSection from "../members/MembersSection";
import expenseService from "../../api/services/expenseService";
import groupService from "../../api/services/groupService";
import userService from "../../api/services/userService";
import { AnimatePresence } from "framer-motion";
import { motion } from "framer-motion";
import { useCallback, useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useGroupWebSocket } from "../../websocket/useGroupWebSocket";

const GroupDetails = () => {
  const { id } = useParams();
  const groupId = id;

  const [group, setGroup] = useState(null);
  const [members, setMembers] = useState([]);
  const [expenses, setExpenses] = useState([]);
  const [showExpenses, setShowExpenses] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const navigate = useNavigate();

  useEffect(() => {
    (async () => {
      setLoading(true);
      setError(null);
      try {
        const [groupData, membersData, expensesData] = await Promise.all([
          groupService.getGroupById(groupId),
          userService.getGroupMembers(groupId),
          expenseService.getGroupExpenses(groupId),
        ]);
        setGroup(groupData);
        setMembers(membersData);
        setExpenses(expensesData);
      } catch (e) {
        setError(e.message || "Failed to load group details");
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  useGroupWebSocket(groupId, (_, message) => {
    const { changeType, data } = JSON.parse(message.body);
    switch (changeType) {
      case "ADD_MEMBER":
        setMembers((prevMembers) => [...prevMembers, data]);
        break;
      case "REMOVE_MEMBER":
        setMembers((prevMembers) =>
          prevMembers.filter((m) => m.id !== data.id)
        );
        break;
      case "ADD_EXPENSE":
        setExpenses((prevExpenses) => [...prevExpenses, data]);
        break;
      default:
        break;
    }
  });

  const handleAddMember = async ({ name }) => {
    await userService.addMemberToGroup(groupId, { name });
  };

  const handleRemoveMember = async (userId) => {
    await userService.removeMemberFromGroup(groupId, userId);
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center text-yellow-900 font-semibold text-xl">
        Loading group...
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center text-red-700 font-semibold text-lg px-4">
        {error}
      </div>
    );
  }

  if (!group) {
    return (
      <div className="min-h-screen flex items-center justify-center text-yellow-900 font-semibold text-xl px-4">
        Group not found
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-r from-yellow-400 via-yellow-300 to-yellow-200 px-6 py-12 flex justify-center">
      <div className="max-w-5xl w-full bg-yellow-100 bg-opacity-80 rounded-lg shadow-lg p-8 flex flex-col relative">
        {/* Back Button */}
        <div className="flex justify-start mb-3">
          <button
            type="button"
            onClick={() => navigate(`/groups`)}
            className="font-semibold text-yellow-700 hover:text-yellow-900 transition"
          >
            &larr; Back
          </button>
        </div>

        {/* Header */}
        <header className="space-y-4 mb-6">
          <h1 className="text-4xl font-extrabold text-yellow-900 drop-shadow-md">
            {group.name}
          </h1>
          {group.description && (
            <p className="text-yellow-900 text-lg">{group.description}</p>
          )}
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
          <button
            onClick={() => setShowExpenses(!showExpenses)}
            className="px-6 py-3 mb-6 bg-yellow-600 hover:bg-yellow-700 text-yellow-50 font-semibold rounded-lg shadow-md transition"
          >
            {showExpenses ? "Hide Expenses" : "Show Expenses"}
          </button>
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
        <div className="absolute bottom-6 right-6">
          <button
            className={
              "mt-6 px-6 py-3 bg-yellow-600 hover:bg-yellow-700 text-yellow-50 rounded-lg font-semibold shadow-md transition"
            }
          >
            Leave Group
          </button>
        </div>
      </div>
    </div>
  );
};

export default GroupDetails;
