import ExpensesSection from "@/components/group/ExpensesSection";
import MembersSection from "@/components/group/MembersSection";
import { AnimatePresence } from "framer-motion";
import { motion } from "framer-motion";
import { useCallback, useContext, useState } from "react";
import { AuthContext } from "react-oauth2-code-pkce";
import { useSelector } from "react-redux";
import { useNavigate, useParams } from "react-router-dom";
import expenseService from "@/api/expenseService";
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
import { selectIsAdmin } from "@/store/authSlice";

const EXPENSES_PAGE_SIZE = 10;

const GroupDetailsPage = () => {
  const { id } = useParams();
  const groupId = id;
  const navigate = useNavigate();
  const { tokenData } = useContext(AuthContext);
  const isAdmin = useSelector(selectIsAdmin);

  const fetchDetails = useCallback(() => groupService.getGroupDetails(groupId), [groupId]);
  const { data, isLoading, error, errorStatus, reload } = useAsyncData(fetchDetails, [groupId]);

  const fetchFirstExpensesPage = useCallback(
    () => expenseService.getGroupExpenses(groupId, 0, EXPENSES_PAGE_SIZE),
    [groupId]
  );
  const { data: expensesPageData } = useAsyncData(fetchFirstExpensesPage, [groupId]);

  const [loadedData, setLoadedData] = useState(null);
  const [members, setMembers] = useState([]);

  const [loadedExpensesPageData, setLoadedExpensesPageData] = useState(null);
  const [expenses, setExpenses] = useState([]);
  const [expensePage, setExpensePage] = useState(0);
  const [expenseTotalPages, setExpenseTotalPages] = useState(0);
  const [isLoadingMoreExpenses, setIsLoadingMoreExpenses] = useState(false);

  const [showExpenses, setShowExpenses] = useState(false);
  const [showLeaveConfirm, setShowLeaveConfirm] = useState(false);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [expenseIdToDelete, setExpenseIdToDelete] = useState(null);

  if (data && data !== loadedData) {
    setLoadedData(data);
    setMembers(data.membersData);
  }

  if (expensesPageData && expensesPageData !== loadedExpensesPageData) {
    setLoadedExpensesPageData(expensesPageData);
    setExpenses(expensesPageData.content);
    setExpensePage(0);
    setExpenseTotalPages(expensesPageData.totalPages);
  }

  const handleLoadMoreExpenses = async () => {
    setIsLoadingMoreExpenses(true);
    try {
      const nextPage = expensePage + 1;
      const expensesPage = await expenseService.getGroupExpenses(
        groupId,
        nextPage,
        EXPENSES_PAGE_SIZE
      );
      setExpenses((prev) => [...prev, ...expensesPage.content]);
      setExpensePage(nextPage);
      setExpenseTotalPages(expensesPage.totalPages);
    } catch (err) {
      notifyError(err, "Failed to load more expenses");
    } finally {
      setIsLoadingMoreExpenses(false);
    }
  };

  const refreshExpensesPreservingProgress = async () => {
    const loadedCount = (expensePage + 1) * EXPENSES_PAGE_SIZE;
    const expensesPage = await expenseService.getGroupExpenses(groupId, 0, loadedCount);
    setExpenses(expensesPage.content);
    setExpenseTotalPages(Math.ceil(expensesPage.totalElements / EXPENSES_PAGE_SIZE));
    setExpensePage(Math.max(0, Math.ceil(expensesPage.content.length / EXPENSES_PAGE_SIZE) - 1));
  };

  useGroupWebSocket(groupId, (topic) => {
    if (topic.endsWith("/users")) {
      reload();
    }
    if (topic.endsWith("/expenses")) {
      refreshExpensesPreservingProgress();
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
  const canManageGroup = isAdmin || currentMember?.role === "OWNER";
  const canAddMembers = isAdmin || Boolean(currentMember);

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

  const handleConfirmDeleteExpense = async () => {
    const targetExpenseId = expenseIdToDelete;
    setExpenseIdToDelete(null);
    try {
      await expenseService.deleteExpense(groupId, targetExpenseId);
      setExpenses((prev) => prev.filter((expense) => expense.id !== targetExpenseId));
      notifySuccess("Expense deleted");
    } catch (err) {
      notifyError(err, "Failed to delete expense");
    }
  };

  const handleConfirmDeleteGroup = async () => {
    setShowDeleteConfirm(false);
    try {
      await groupService.deleteGroup(groupId);
      notifySuccess("Group deleted");
      navigate("/groups");
    } catch (err) {
      notifyError(err, "Failed to delete group");
    }
  };

  if (error) {
    return <ErrorScreen message={error} status={errorStatus} onRetry={reload} />;
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
          canManage={canManageGroup}
          canAddMember={canAddMembers}
          currentMemberId={currentMember?.id}
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
            <ExpensesSection
              expenses={expenses}
              groupId={groupId}
              onDeleteExpense={setExpenseIdToDelete}
              hasMoreExpenses={expensePage + 1 < expenseTotalPages}
              isLoadingMoreExpenses={isLoadingMoreExpenses}
              onLoadMoreExpenses={handleLoadMoreExpenses}
            />
          </motion.div>
        )}
      </AnimatePresence>

      {/* Leave / Delete Group */}
      {(currentMember || canManageGroup) && (
        <div className="mt-8 pt-6 border-t border-slate-700 flex justify-end gap-3">
          {canManageGroup && (
            <Button variant="danger" onClick={() => setShowDeleteConfirm(true)}>
              Delete Group
            </Button>
          )}
          {currentMember && (
            <Button variant="danger" onClick={() => setShowLeaveConfirm(true)}>
              Leave Group
            </Button>
          )}
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

      <ConfirmDialog
        open={showDeleteConfirm}
        variant="danger"
        title="Delete group?"
        message="This will permanently delete the group and all its data. This action cannot be undone."
        confirmLabel="Delete"
        onConfirm={handleConfirmDeleteGroup}
        onCancel={() => setShowDeleteConfirm(false)}
      />

      <ConfirmDialog
        open={expenseIdToDelete !== null}
        variant="danger"
        title="Delete expense?"
        message="This will permanently delete this expense and recompute balances. This action cannot be undone."
        confirmLabel="Delete"
        onConfirm={handleConfirmDeleteExpense}
        onCancel={() => setExpenseIdToDelete(null)}
      />
    </PageShell>
  );
};

export default GroupDetailsPage;
