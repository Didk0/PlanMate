import { motion } from "framer-motion";
import { Link } from "react-router-dom";

const listVariants = {
  hidden: { opacity: 0 },
  visible: {
    opacity: 1,
    transition: {
      staggerChildren: 0.12,
      delayChildren: 0.1,
    },
  },
};

const itemVariants = {
  hidden: { opacity: 0, y: 20 },
  visible: {
    opacity: 1,
    y: 0,
    transition: { type: "spring", stiffness: 300, damping: 24 },
  },
};

const ExpensesSection = ({ expenses, groupId }) => {
  return (
    <section>
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-2xl font-extrabold text-yellow-900 drop-shadow-md">
          Expenses
        </h2>
        <Link
          to={`/groups/${groupId}/expense`}
          className="px-5 py-2 mb-2 bg-yellow-600 text-yellow-50 rounded-md shadow hover:bg-yellow-700 transition"
        >
          Create Expense
        </Link>
      </div>
      {expenses.length === 0 ? (
        <p className="text-yellow-900 text-lg">No expenses yet</p>
      ) : (
        <motion.ul
          className="space-y-4"
          variants={listVariants}
          initial="hidden"
          animate="visible"
        >
          {expenses.map((expense) => (
            <motion.li
              key={expense.id}
              className="bg-yellow-100 rounded-md p-4 shadow hover:shadow-lg transition cursor-default"
              variants={itemVariants}
              whileHover={{ scale: 1.03 }}
            >
              <div className="flex flex-col md:flex-row md:justify-between md:items-start gap-4">
                {/* Expense main info */}
                <div className="flex-1">
                  <div className="font-semibold text-yellow-900 text-lg">
                    {expense.description}
                  </div>
                  <div className="flex flex-wrap gap-6 text-yellow-800 text-sm mt-2">
                    <div>Amount: ${expense.amount.toFixed(2)}</div>
                    <div>Paid by: {expense.paidByFirstName} {expense.paidByLastName}</div>
                  </div>
                </div>
                {/* Participants list */}
                {expense.participants?.length > 0 && (
                  <div className="mt-4 md:mt-0 md:ml-6 min-w-[180px] bg-yellow-200 rounded p-3">
                    <h4 className="font-semibold mb-2 text-yellow-900">
                      Participants:
                    </h4>
                    <ul className="list-disc list-inside space-y-1 text-yellow-900 text-sm">
                      {expense.participants
                        .filter((participant) => participant.shareAmount > 0)
                        .map((participant) => (
                          <li
                            key={participant.id}
                            className="flex justify-between"
                          >
                            <span>{participant.firstName} {participant.lastName}</span>
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
      <Link
        to={`/groups/${groupId}/settlements`}
        className="mt-10 inline-block bg-yellow-600 text-yellow-50 px-6 py-3 rounded-md shadow hover:bg-yellow-700 transition font-semibold"
      >
        Settle Debts
      </Link>
    </section>
  );
};

export default ExpensesSection;
