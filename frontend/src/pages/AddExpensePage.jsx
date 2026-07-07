import { useCallback, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import expenseService from "@/api/expenseService";
import userService from "@/api/userService";
import BackButton from "@/components/shared/BackButton";
import Button from "@/components/shared/Button";
import ErrorScreen from "@/components/shared/ErrorScreen";
import LoadingScreen from "@/components/shared/LoadingScreen";
import PageShell from "@/components/shared/PageShell";
import TextInput, { inputClasses } from "@/components/shared/TextInput";
import { useAsyncData } from "@/hooks/useAsyncData";

const AddExpensePage = () => {
  const { id } = useParams();
  const groupId = id;
  const navigate = useNavigate();

  const fetchMembers = useCallback(() => userService.getGroupMembers(groupId), [groupId]);
  const { data: members, isLoading, error } = useAsyncData(fetchMembers, [groupId]);

  const [description, setDescription] = useState("");
  const [amount, setAmount] = useState("");
  const [paidByUsername, setPaidByUsername] = useState("");
  const [shareAmounts, setShareAmounts] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState(null);

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
    setSubmitError(null);
    try {
      await expenseService.createExpense(groupId, expenseData);
      navigate(`/groups/${groupId}`);
    } catch (err) {
      setSubmitError(err.response?.data?.message || err.message || "Failed to create expense");
      setIsSubmitting(false);
    }
  };

  if (error) {
    return <ErrorScreen message={error} />;
  }

  if (isLoading) {
    return <LoadingScreen />;
  }

  return (
    <PageShell as="form" onSubmit={handleSubmit} maxWidth="max-w-lg">
      {/* Back Button */}
      <BackButton to={`/groups/${groupId}`} className="mb-6" />

      <h1 className="text-3xl font-extrabold text-yellow-900 mb-6 drop-shadow-md">Add Expense</h1>

      <TextInput
        type="text"
        placeholder="Description"
        value={description}
        onChange={(event) => setDescription(event.target.value)}
        className="w-full mb-5"
        required
      />

      <TextInput
        type="number"
        placeholder="Amount"
        value={amount}
        onChange={(event) => setAmount(event.target.value)}
        className="w-full mb-5"
        min="0"
        step="0.01"
        required
      />

      <label htmlFor="paidBy" className="block mb-2 font-semibold text-yellow-900">
        Select payer
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

      <h3 className="text-xl font-semibold mb-4 text-yellow-900 drop-shadow-sm">Participants:</h3>

      <div className="space-y-4 mb-6">
        {participants.map((participant) => (
          <div
            key={participant.memberId}
            className="flex items-center gap-4 flex-wrap md:flex-nowrap"
          >
            <span className="w-32 font-medium text-yellow-900">{participant.userName}</span>
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

      {submitError && <p className="text-red-700 font-semibold mb-4">{submitError}</p>}

      <Button type="submit" disabled={isSubmitting} className="w-full">
        {isSubmitting ? "Saving..." : "Save Expense"}
      </Button>
    </PageShell>
  );
};

export default AddExpensePage;
