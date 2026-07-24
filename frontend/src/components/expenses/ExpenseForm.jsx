import { useState } from "react";
import Alert from "@/components/shared/Alert";
import Button from "@/components/shared/Button";
import TextInput, { inputClasses } from "@/components/shared/TextInput";
import { fromCents, splitEqually, sumCents, toCents } from "@/lib/splitExpense";

const SPLIT_MODES = { EQUAL: "equal", EXACT: "exact" };

/** Detects whether a set of participant shares matches an equal split of the total. */
function detectSplitMode(totalCents, participantSharesCents) {
  const expected = splitEqually(totalCents, participantSharesCents.length)
    .slice()
    .sort((a, b) => a - b);
  const actual = participantSharesCents.slice().sort((a, b) => a - b);
  const matchesEqualSplit =
    expected.length === actual.length && expected.every((cents, i) => cents === actual[i]);
  return matchesEqualSplit ? SPLIT_MODES.EQUAL : SPLIT_MODES.EXACT;
}

/**
 * Shared add/edit expense form. Supports splitting the total equally among the
 * checked participants, or entering exact per-person amounts with a live
 * "remaining to assign" reconciliation against the total.
 */
const ExpenseForm = ({ members, initialValues, submitLabel, onSubmit, isSubmitting }) => {
  const memberList = members ?? [];

  const initialParticipants = useState(() => {
    if (!initialValues?.participants?.length) return null;
    return new Map(initialValues.participants.map((p) => [p.userName, p.shareAmount]));
  })[0];

  const [description, setDescription] = useState(initialValues?.description ?? "");
  const [amount, setAmount] = useState(
    initialValues?.amount != null ? String(initialValues.amount) : ""
  );
  const [paidByUsername, setPaidByUsername] = useState(
    initialValues?.paidByUsername || memberList[0]?.username || ""
  );
  const [checked, setChecked] = useState(
    () =>
      new Set(initialParticipants ? initialParticipants.keys() : memberList.map((m) => m.username))
  );
  const [exactShares, setExactShares] = useState(() =>
    initialParticipants ? Object.fromEntries(initialParticipants) : {}
  );
  const [splitMode, setSplitMode] = useState(() => {
    if (!initialParticipants) return SPLIT_MODES.EQUAL;
    return detectSplitMode(
      toCents(initialValues.amount),
      [...initialParticipants.values()].map(toCents)
    );
  });

  const checkedUsernames = memberList
    .filter((member) => checked.has(member.username))
    .map((member) => member.username);

  const totalCents = toCents(amount);
  const equalSharesCents =
    splitMode === SPLIT_MODES.EQUAL ? splitEqually(totalCents, checkedUsernames.length) : [];
  const equalSharesByUsername = Object.fromEntries(
    checkedUsernames.map((username, i) => [username, equalSharesCents[i] ?? 0])
  );

  const remainingCents =
    splitMode === SPLIT_MODES.EXACT
      ? totalCents - sumCents(checkedUsernames.map((username) => exactShares[username] ?? ""))
      : 0;

  const canSubmit =
    description.trim().length > 0 &&
    totalCents > 0 &&
    checkedUsernames.length > 0 &&
    Boolean(paidByUsername) &&
    remainingCents === 0;

  const toggleParticipant = (username) => {
    setChecked((prev) => {
      const next = new Set(prev);
      if (next.has(username)) {
        next.delete(username);
      } else {
        next.add(username);
      }
      return next;
    });
  };

  const handleExactShareChange = (username, value) => {
    setExactShares((prev) => ({ ...prev, [username]: value }));
  };

  const handleSubmit = (event) => {
    event.preventDefault();
    if (!canSubmit) return;

    const participants = checkedUsernames.map((username, i) => ({
      userName: username,
      shareAmount:
        splitMode === SPLIT_MODES.EQUAL
          ? fromCents(equalSharesCents[i])
          : fromCents(toCents(exactShares[username])),
    }));

    onSubmit({
      description,
      amount: fromCents(totalCents),
      paidByUsername,
      participants,
    });
  };

  return (
    <form onSubmit={handleSubmit}>
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
        value={paidByUsername}
        onChange={(event) => setPaidByUsername(event.target.value)}
        className={`${inputClasses} w-full mb-6`}
        required
      >
        {memberList.map((member) => (
          <option key={member.id} value={member.username}>
            {member.username}
          </option>
        ))}
      </select>

      <div className="flex items-center justify-between mb-3">
        <h3 className="text-xl font-semibold text-slate-100">Participants</h3>
        <div className="flex rounded-lg border border-slate-700 overflow-hidden">
          <button
            type="button"
            onClick={() => setSplitMode(SPLIT_MODES.EQUAL)}
            className={`px-3 py-1.5 text-sm font-medium transition ${
              splitMode === SPLIT_MODES.EQUAL
                ? "bg-primary-500 text-white"
                : "bg-slate-800 text-slate-300 hover:bg-slate-700"
            }`}
          >
            Split equally
          </button>
          <button
            type="button"
            onClick={() => setSplitMode(SPLIT_MODES.EXACT)}
            className={`px-3 py-1.5 text-sm font-medium transition ${
              splitMode === SPLIT_MODES.EXACT
                ? "bg-primary-500 text-white"
                : "bg-slate-800 text-slate-300 hover:bg-slate-700"
            }`}
          >
            Exact amounts
          </button>
        </div>
      </div>

      <div className="space-y-3 mb-4 rounded-lg border border-slate-700 bg-slate-900 p-4">
        {memberList.map((member) => {
          const isChecked = checked.has(member.username);
          return (
            <div key={member.id} className="flex items-center gap-4 flex-wrap md:flex-nowrap">
              <label className="flex items-center gap-2 w-40 shrink-0">
                <input
                  type="checkbox"
                  checked={isChecked}
                  onChange={() => toggleParticipant(member.username)}
                  className="h-4 w-4 rounded border-slate-600 bg-slate-900 text-primary-500 focus:ring-primary-500"
                />
                <span className="font-medium text-slate-300 text-sm">{member.username}</span>
              </label>
              {isChecked &&
                (splitMode === SPLIT_MODES.EQUAL ? (
                  <span className="w-full max-w-[150px] text-slate-100 font-medium">
                    ${fromCents(equalSharesByUsername[member.username] ?? 0)}
                  </span>
                ) : (
                  <TextInput
                    type="number"
                    placeholder="Share Amount"
                    value={exactShares[member.username] ?? ""}
                    onChange={(event) =>
                      handleExactShareChange(member.username, event.target.value)
                    }
                    className="w-full max-w-[150px]"
                    min="0"
                    step="0.01"
                  />
                ))}
            </div>
          );
        })}
      </div>

      {splitMode === SPLIT_MODES.EXACT && (
        <Alert variant={remainingCents === 0 ? "success" : "warning"} className="mb-6">
          Remaining to assign: ${fromCents(remainingCents)}
        </Alert>
      )}

      <Button type="submit" isLoading={isSubmitting} disabled={!canSubmit} className="w-full">
        {submitLabel}
      </Button>
    </form>
  );
};

export default ExpenseForm;
