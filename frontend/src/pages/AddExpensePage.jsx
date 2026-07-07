import { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useNavigate, useParams } from "react-router-dom";
import BackButton from "@/components/shared/BackButton";
import Button from "@/components/shared/Button";
import ErrorScreen from "@/components/shared/ErrorScreen";
import LoadingScreen from "@/components/shared/LoadingScreen";
import PageShell from "@/components/shared/PageShell";
import TextInput, { inputClasses } from "@/components/shared/TextInput";
import { createExpense, getGroupMembers } from "@/store/actions";

const AddExpensePage = () => {
  const { id } = useParams();
  const groupId = id;
  const navigate = useNavigate();

  const { isLoading, errorMessage } = useSelector((state) => state.errors);

  const [description, setDescription] = useState("");
  const [amount, setAmount] = useState("");
  const [paidByUsername, setPaidByUsername] = useState("");
  const [shareAmounts, setShareAmounts] = useState({});
  const [members, setMembers] = useState([]);

  const dispatch = useDispatch();

  useEffect(() => {
    dispatch(getGroupMembers(groupId)).then((membersData) => {
      setMembers(membersData ?? []);
      if (membersData?.length > 0) {
        setPaidByUsername(membersData[0].username);
      }
    });
  }, [dispatch, groupId]);

  const participants = members
    .filter((member) => member.username !== paidByUsername)
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
      amount: parseFloat(amount),
      paidByUsername,
      participants: participants.map((p) => ({
        userName: p.userName,
        shareAmount: parseFloat(p.shareAmount || 0),
      })),
    };
    await dispatch(createExpense(groupId, expenseData));
    navigate(`/groups/${groupId}`);
  };

  if (errorMessage) {
    return <ErrorScreen message={errorMessage} />;
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
        value={paidByUsername}
        onChange={(event) => setPaidByUsername(event.target.value)}
        className={`${inputClasses} w-full mb-6`}
        required
      >
        {members.map((member) => (
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

      <Button type="submit" className="w-full">
        Save Expense
      </Button>
    </PageShell>
  );
};

export default AddExpensePage;
