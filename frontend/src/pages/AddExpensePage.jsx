import { useCallback, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import expenseService from "@/api/expenseService";
import userService from "@/api/userService";
import BackButton from "@/components/shared/BackButton";
import Button from "@/components/shared/Button";
import ErrorScreen from "@/components/shared/ErrorScreen";
import PageShell from "@/components/shared/PageShell";
import Skeleton from "@/components/shared/Skeleton";
import TextInput, { inputClasses } from "@/components/shared/TextInput";
import { useAsyncData } from "@/hooks/useAsyncData";
import { notifyError, notifySuccess } from "@/lib/notify";

const AddExpensePage = () => {
  const { id } = useParams();
  const groupId = id;
  const navigate = useNavigate();

  const fetchMembers = useCallback(() => userService.getGroupMembers(groupId), [groupId]);
  const {
    data: members,
    isLoading,
    error,
    errorStatus,
    reload,
  } = useAsyncData(fetchMembers, [groupId]);

  const [description, setDescription] = useState("");
  const [amount, setAmount] = useState("");
  const [paidByUsername, setPaidByUsername] = useState("");
  const [shareAmounts, setShareAmounts] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  const effectivePaidBy = paidByUsername || members?.[0]?.username || "";

  const participants = (members ?? [])
    .filter((member) => member.username !== effectivePaidBy)
    .map((member) => ({
      memberId: member.id,
      userName: member.username,
      shareAmount: shareAmounts[member.id] ?? "",
    }));

  const handleAmountChange = (memberId, value) => {
    setShareAmounts((prev) => ({ ...prev, [memberId]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const expenseData = {
      description,
      amount,
      paidByUsername: effectivePaidBy,
      participants: participants
        .filter((p) => parseFloat(p.shareAmount) > 0)
        .map((p) => ({
          userName: p.userName,
          shareAmount: p.shareAmount,
        })),
    };
    setIsSubmitting(true);
    try {
      await expenseService.createExpense(groupId, expenseData);
      notifySuccess("Expense created");
      navigate(`/groups/${groupId}`);
    } catch (err) {
      notifyError(err, "Failed to create expense");
      setIsSubmitting(false);
    }
  };

  if (error) {
    return <ErrorScreen message={error} status={errorStatus} onRetry={reload} />;
  }

  if (isLoading) {
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

  return (
    <PageShell as="form" onSubmit={handleSubmit} maxWidth="max-w-lg">
      {/* Back Button */}
      <BackButton to={`/groups/${groupId}`} className="mb-6" />

      <h1 className="text-3xl font-bold text-slate-100 mb-6">Add Expense</h1>

      <label htmlFor="description" className="block mb-2 font-medium text-slate-300 text-sm">
        Description
      </label>
      <TextInput
        id="description"
        type="text"
        placeholder="Description"
        value={description}
        onChange={(event) => setDescription(event.target.value)}
        className="w-full mb-5"
        required
      />

      <label htmlFor="amount" className="block mb-2 font-medium text-slate-300 text-sm">
        Amount
      </label>
      <TextInput
        id="amount"
        type="number"
        placeholder="Amount"
        value={amount}
        onChange={(event) => setAmount(event.target.value)}
        className="w-full mb-5"
        min="0"
        step="0.01"
        required
      />

      <label htmlFor="paidBy" className="block mb-2 font-medium text-slate-300 text-sm">
        Paid by
      </label>
      <select
        id="paidBy"
        value={effectivePaidBy}
        onChange={(event) => setPaidByUsername(event.target.value)}
        className={`${inputClasses} w-full mb-6`}
        required
      >
        {(members ?? []).map((member) => (
          <option key={member.id} value={member.username}>
            {member.username}
          </option>
        ))}
      </select>

      <h3 className="text-xl font-semibold mb-3 text-slate-100">Participants</h3>

      <div className="space-y-3 mb-6 rounded-lg border border-slate-700 bg-slate-900 p-4">
        {participants.map((participant) => (
          <div
            key={participant.memberId}
            className="flex items-center gap-4 flex-wrap md:flex-nowrap"
          >
            <span className="w-32 font-medium text-slate-300 text-sm">{participant.userName}</span>
            <TextInput
              type="number"
              placeholder="Share Amount"
              value={participant.shareAmount}
              onChange={(event) => handleAmountChange(participant.memberId, event.target.value)}
              className="w-full max-w-[150px]"
              min="0"
              step="0.01"
            />
          </div>
        ))}
      </div>

      <Button type="submit" isLoading={isSubmitting} className="w-full">
        Save Expense
      </Button>
    </PageShell>
  );
};

export default AddExpensePage;
