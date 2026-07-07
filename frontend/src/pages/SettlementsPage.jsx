import { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useParams } from "react-router-dom";
import BackButton from "@/components/shared/BackButton";
import ErrorScreen from "@/components/shared/ErrorScreen";
import LoadingScreen from "@/components/shared/LoadingScreen";
import PageShell from "@/components/shared/PageShell";
import { calculateSettlements } from "@/store/actions";
import { useGroupWebSocket } from "@/hooks/useGroupWebSocket";

const SettlementsPage = () => {
  const { id } = useParams();
  const groupId = id;
  const [settlements, setSettlements] = useState([]);

  const { isLoading, errorMessage } = useSelector((state) => state.errors);

  const dispatch = useDispatch();

  useEffect(() => {
    dispatch(calculateSettlements(groupId)).then((data) => setSettlements(data ?? []));
  }, [dispatch, groupId]);

  useGroupWebSocket(groupId, (topic, message) => {
    if (!topic.endsWith("/settlements")) return;
    const payload = JSON.parse(message.body);
    if (payload.changeType === "SETTLEMENTS_INVALIDATED") {
      dispatch(calculateSettlements(groupId)).then((data) => setSettlements(data ?? []));
    }
  });

  if (errorMessage) {
    return <ErrorScreen message={errorMessage} />;
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
          {settlements.map((settlement, index) => (
            <li
              key={index}
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
