import { motion } from "framer-motion";
import EmptyState from "@/components/shared/EmptyState";
import LinkButton from "@/components/shared/LinkButton";
import { itemVariants, listVariants } from "@/lib/motion";

const ExpensesSection = ({ expenses, groupId }) => {
  return (
    <section>
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-xl font-semibold text-slate-100">Expenses</h2>
        <LinkButton to={`/groups/${groupId}/expense`} size="sm">
          Create Expense
        </LinkButton>
      </div>
      {expenses.length === 0 ? (
        <EmptyState
          title="No expenses yet"
          description="Add an expense to start splitting costs."
          action={
            <LinkButton to={`/groups/${groupId}/expense`} size="sm">
              Create Expense
            </LinkButton>
          }
        />
      ) : (
        <motion.ul className="space-y-3" variants={listVariants} initial="hidden" animate="visible">
          {expenses.map((expense) => (
            <motion.li
              key={expense.id}
              className="rounded-xl border border-slate-700 bg-surface p-4 shadow-sm transition hover:shadow-md"
              variants={itemVariants}
              whileHover={{ scale: 1.01 }}
            >
              <div className="flex flex-col md:flex-row md:justify-between md:items-start gap-4">
                {/* Expense main info */}
                <div className="flex-1">
                  <div className="font-semibold text-slate-100 text-lg">{expense.description}</div>
                  <div className="flex flex-wrap gap-x-6 gap-y-1 text-slate-400 text-sm mt-1">
                    <div className="font-semibold text-slate-100">${expense.amount.toFixed(2)}</div>
                    <div>
                      Paid by {expense.paidByFirstName} {expense.paidByLastName}
                    </div>
                  </div>
                </div>
                {/* Participants list */}
                {expense.participants?.length > 0 && (
                  <div className="mt-4 md:mt-0 md:ml-6 min-w-[180px] bg-slate-900 rounded-lg p-3">
                    <h4 className="font-semibold mb-2 text-slate-300 text-sm">Participants</h4>
                    <ul className="space-y-1 text-slate-300 text-sm">
                      {expense.participants
                        .filter((participant) => participant.shareAmount > 0)
                        .map((participant) => (
                          <li key={participant.id} className="flex justify-between gap-4">
                            <span>
                              {participant.firstName} {participant.lastName}
                            </span>
                            <span className="font-semibold">
                              ${participant.shareAmount.toFixed(2)}
                            </span>
                          </li>
                        ))}
                    </ul>
                  </div>
                )}
              </div>
            </motion.li>
          ))}
        </motion.ul>
      )}

      {/* Settle Debts button */}
      <LinkButton to={`/groups/${groupId}/settlements`} variant="secondary" className="mt-8">
        Settle Debts
      </LinkButton>
    </section>
  );
};

export default ExpensesSection;
