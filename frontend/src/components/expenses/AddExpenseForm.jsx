import React, { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useNavigate, useParams } from "react-router-dom";
import { createExpense, getGroupMembers } from "../../store/actions";

const AddExpenseForm = () => {
  const { id } = useParams();
  const groupId = id;
  const navigate = useNavigate();

  const { isLoading, errorMessage } = useSelector((state) => state.errors);

  const [description, setDescription] = useState("");
  const [amount, setAmount] = useState("");
  const [paidByUserName, setPaidByUserName] = useState("");
  const [participants, setParticipants] = useState([]);
  const [members, setMembers] = useState([]);

  const dispatch = useDispatch();

  useEffect(() => {
    dispatch(getGroupMembers(groupId)).then((membersData) => {
      setMembers(membersData);

      if (membersData.length > 0) {
        const defaultPayer = membersData[0].username;

        setPaidByUserName(defaultPayer);

        setParticipants(
          membersData
            .filter((m) => m.username !== (paidByUserName || defaultPayer))
            .map((m) => ({
              memberId: m.id,
              shareAmount: "",
              userName: m.username,
            }))
        );
      }
    });
  }, [dispatch, groupId]);

  useEffect(() => {
    if (!paidByUserName || members.length === 0) return;
    setParticipants((prevParticipants) =>
      members
        .filter((m) => m.username !== paidByUserName)
        .map((member) => {
          const existing = prevParticipants.find(
            (p) => p.memberId === member.id
          );
          return {
            memberId: member.id,
            userName: member.username,
            shareAmount: existing ? existing.shareAmount : "",
          };
        })
    );
  }, [paidByUserName, members]);

  const handleAmountChange = (memberId, value) => {
    setParticipants((prev) =>
      prev.map((p) =>
        p.memberId === memberId ? { ...p, shareAmount: value } : p
      )
    );
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const expenseData = {
      description,
      amount: parseFloat(amount),
      paidByUserName,
      participants: participants.map((p) => ({
        userName: p.userName,
        shareAmount: parseFloat(p.shareAmount || 0),
      })),
    };
    await dispatch(createExpense(groupId, expenseData));
    navigate(`/groups/${groupId}`);
  };

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center text-yellow-900 font-semibold text-xl">
        Loading...
      </div>
    );
  }

  if (errorMessage) {
    return (
      <div className="min-h-screen flex items-center justify-center text-red-700 font-semibold text-lg px-4">
        {errorMessage}
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-r from-yellow-400 via-yellow-300 to-yellow-200 p-6 flex flex-col items-center">
      <form
        onSubmit={handleSubmit}
        className="max-w-lg mx-auto p-8 bg-yellow-100 rounded-lg shadow-lg"
      >
        {/* Back Button */}
        <button
          type="button"
          onClick={() => navigate(`/groups/${groupId}`)}
          className="mb-6 text-yellow-700 font-semibold hover:text-yellow-900 transition"
        >
          &larr; Back
        </button>

        <h1 className="text-3xl font-extrabold text-yellow-900 mb-6 drop-shadow-md">
          Add Expense
        </h1>

        <input
          type="text"
          placeholder="Description"
          value={description}
          onChange={(event) => setDescription(event.target.value)}
          className="w-full p-3 mb-5 rounded-md border border-yellow-400 focus:outline-yellow-500 focus:ring-2 focus:ring-yellow-400 transition text-yellow-900 font-semibold"
          required
        />

        <input
          type="number"
          placeholder="Amount"
          value={amount}
          onChange={(event) => setAmount(event.target.value)}
          className="w-full p-3 mb-5 rounded-md border border-yellow-400 focus:outline-yellow-500 focus:ring-2 focus:ring-yellow-400 transition text-yellow-900 font-semibold"
          min="0"
          step="0.01"
          required
        />

        <label
          htmlFor="paidBy"
          className="block mb-2 font-semibold text-yellow-900"
        >
          Select payer
        </label>
        <select
          id="paidBy"
          value={paidByUserName}
          onChange={(event) => setPaidByUserName(event.target.value)}
          className="w-full p-3 mb-6 rounded-md border border-yellow-400 focus:outline-yellow-500 focus:ring-2 focus:ring-yellow-400 transition text-yellow-900 font-semibold"
          required
        >
          {members.map((member) => (
            <option key={member.id} value={member.username}>
              {member.username}
            </option>
          ))}
        </select>

        <h3 className="text-xl font-semibold mb-4 text-yellow-900 drop-shadow-sm">
          Participants:
        </h3>

        <div className="space-y-4 mb-6">
          {members
            .filter((member) => member.username !== paidByUserName)
            .map((member) => (
              <div
                key={member.id}
                className="flex items-center gap-4 flex-wrap md:flex-nowrap"
              >
                <span className="w-32 font-medium text-yellow-900">
                  {member.username}
                </span>
                <input
                  type="number"
                  placeholder="Share Amount"
                  value={
                    participants.find((p) => p.memberId === member.id)
                      ?.shareAmount || ""
                  }
                  onChange={(event) =>
                    handleAmountChange(member.id, event.target.value)
                  }
                  className="w-full max-w-[150px] border border-yellow-400 rounded-md p-3 focus:outline-yellow-500 focus:ring-2 focus:ring-yellow-400 transition text-yellow-900 font-semibold"
                  min="0"
                  step="0.01"
                />
              </div>
            ))}
        </div>

        <button
          type="submit"
          className="w-full bg-yellow-600 hover:bg-yellow-700 text-yellow-50 py-3 rounded-md font-bold shadow-md transition"
        >
          Save Expense
        </button>
      </form>
    </div>
  );
};

export default AddExpenseForm;
