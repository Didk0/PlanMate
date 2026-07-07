import { useCallback } from "react";
import { useParams } from "react-router-dom";
import expenseService from "@/api/expenseService";
import BackButton from "@/components/shared/BackButton";
import ErrorScreen from "@/components/shared/ErrorScreen";
import LoadingScreen from "@/components/shared/LoadingScreen";
import PageShell from "@/components/shared/PageShell";
import { useAsyncData } from "@/hooks/useAsyncData";
import { useGroupWebSocket } from "@/hooks/useGroupWebSocket";

const SettlementsPage = () => {
  const { id } = useParams();
  const groupId = id;

  const fetchSettlements = useCallback(
    () => expenseService.calculateSettlements(groupId),
    [groupId]
  );
  const { data: settlements, isLoading, error, reload } = useAsyncData(fetchSettlements, [groupId]);

  useGroupWebSocket(groupId, (topic, message) => {
    if (!topic.endsWith("/settlements")) return;
    const payload = JSON.parse(message.body);
    if (payload.changeType === "SETTLEMENTS_INVALIDATED") {
      reload();
    }
  });

  if (error) {
    return <ErrorScreen message={error} />;
  }

  if (isLoading) {
    return <LoadingScreen message="Loading settlements..." />;
  }

  return (
    <PageShell maxWidth="max-w-lg">
      <h1 className="text-3xl font-extrabold text-yellow-900 mb-6 drop-shadow-md">Settlements</h1>

      {settlements.length === 0 ? (
        <p className="text-yellow-900 text-lg">No settlements calculated yet.</p>
      ) : (
        <ul className="space-y-3">
          {settlements.map((settlement) => (
            <li
              key={settlement.id}
              className="p-4 border border-yellow-300 rounded-md bg-yellow-200 shadow-sm text-yellow-900 font-semibold"
            >
              <span className="font-bold">
                {settlement.fromUserFirstName} {settlement.fromUserLastName}
              </span>{" "}
              pays{" "}
              <span className="font-bold">
                {settlement.toUserFirstName} {settlement.toUserLastName}
              </span>{" "}
              <span className="text-yellow-700">${settlement.amount.toFixed(2)}</span>
            </li>
          ))}
        </ul>
      )}

      <BackButton to={`/groups/${groupId}`} className="mt-6" />
    </PageShell>
  );
};

export default SettlementsPage;
