import { motion } from "framer-motion";
import { useCallback } from "react";
import { useParams } from "react-router-dom";
import expenseService from "@/api/expenseService";
import BackButton from "@/components/shared/BackButton";
import EmptyState from "@/components/shared/EmptyState";
import ErrorScreen from "@/components/shared/ErrorScreen";
import PageShell from "@/components/shared/PageShell";
import Skeleton from "@/components/shared/Skeleton";
import { useAsyncData } from "@/hooks/useAsyncData";
import { useGroupWebSocket } from "@/hooks/useGroupWebSocket";
import { itemVariants, listVariants } from "@/lib/motion";

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
    return <ErrorScreen message={error} onRetry={reload} />;
  }

  return (
    <PageShell maxWidth="max-w-lg">
      <h1 className="text-3xl font-bold text-slate-100 mb-6">Settlements</h1>

      {isLoading ? (
        <div className="space-y-3">
          <Skeleton className="h-16 w-full" />
          <Skeleton className="h-16 w-full" />
        </div>
      ) : settlements.length === 0 ? (
        <EmptyState
          title="No settlements yet"
          description="Once expenses are added, we'll calculate who owes whom."
        />
      ) : (
        <motion.ul className="space-y-3" variants={listVariants} initial="hidden" animate="visible">
          {settlements.map((settlement) => (
            <motion.li
              key={settlement.id}
              variants={itemVariants}
              className="flex items-center justify-between gap-4 rounded-xl border border-slate-700 bg-surface p-4 shadow-sm"
            >
              <span className="text-slate-100">
                <span className="font-semibold">
                  {settlement.fromUserFirstName} {settlement.fromUserLastName}
                </span>{" "}
                <span className="text-slate-300">&rarr;</span>{" "}
                <span className="font-semibold">
                  {settlement.toUserFirstName} {settlement.toUserLastName}
                </span>
              </span>
              <span className="font-semibold text-primary-400">
                ${settlement.amount.toFixed(2)}
              </span>
            </motion.li>
          ))}
        </motion.ul>
      )}

      <BackButton to={`/groups/${groupId}`} className="mt-6" />
    </PageShell>
  );
};

export default SettlementsPage;
