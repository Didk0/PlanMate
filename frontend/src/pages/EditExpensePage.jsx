import { useCallback, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import expenseService from "@/api/expenseService";
import userService from "@/api/userService";
import ExpenseForm from "@/components/expenses/ExpenseForm";
import BackButton from "@/components/shared/BackButton";
import ErrorScreen from "@/components/shared/ErrorScreen";
import PageShell from "@/components/shared/PageShell";
import Skeleton from "@/components/shared/Skeleton";
import { useAsyncData } from "@/hooks/useAsyncData";
import { notifyError, notifySuccess } from "@/lib/notify";

const EditExpensePage = () => {
  const { id, expenseId } = useParams();
  const groupId = id;
  const navigate = useNavigate();

  const fetchData = useCallback(
    () =>
      Promise.all([userService.getGroupMembers(groupId), expenseService.getGroupExpenses(groupId)]),
    [groupId]
  );
  const { data, isLoading, error, errorStatus, reload } = useAsyncData(fetchData, [groupId]);

  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (expenseData) => {
    setIsSubmitting(true);
    try {
      await expenseService.updateExpense(groupId, expenseId, expenseData);
      notifySuccess("Expense updated");
      navigate(`/groups/${groupId}`);
    } catch (err) {
      notifyError(err, "Failed to update expense");
      setIsSubmitting(false);
    }
  };

  if (error) {
    return <ErrorScreen message={error} status={errorStatus} onRetry={reload} />;
  }

  if (isLoading || !data) {
    return (
      <PageShell maxWidth="max-w-lg">
        <Skeleton className="h-4 w-24 mb-6" />
        <Skeleton className="h-9 w-1/2 mb-6" />
        <Skeleton className="h-11 w-full mb-5" />
        <Skeleton className="h-11 w-full mb-5" />
        <Skeleton className="h-11 w-full mb-6" />
        <Skeleton className="h-24 w-full mb-6" />
        <Skeleton className="h-11 w-full" />
      </PageShell>
    );
  }

  const [members, expenses] = data;
  const expense = expenses.find((e) => String(e.id) === expenseId);

  if (!expense) {
    return <ErrorScreen message="Expense not found" status={404} onRetry={reload} />;
  }

  const initialValues = {
    description: expense.description,
    amount: expense.amount,
    paidByUsername: expense.paidByUsername,
    participants: expense.participants.map((p) => ({
      userName: p.username,
      shareAmount: p.shareAmount,
    })),
  };

  return (
    <PageShell maxWidth="max-w-lg">
      {/* Back Button */}
      <BackButton to={`/groups/${groupId}`} className="mb-6" />

      <h1 className="text-3xl font-bold text-slate-100 mb-6">Edit Expense</h1>

      <ExpenseForm
        members={members}
        initialValues={initialValues}
        submitLabel="Save Changes"
        onSubmit={handleSubmit}
        isSubmitting={isSubmitting}
      />
    </PageShell>
  );
};

export default EditExpensePage;
