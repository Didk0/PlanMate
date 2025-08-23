import { AnimatePresence } from "framer-motion";
import { motion } from "motion/react";
import { useState } from "react";

const MembersSection = ({ members, onAddMember, onRemoveMember }) => {
  const [showAddForm, setShowAddForm] = useState(false);
  const [name, setName] = useState("");

  const handleAdd = () => {
    if (!name.trim()) {
      alert("Please enter a name");
      return;
    }
    onAddMember({ name });
    setName("");
    setShowAddForm(false);
  };

  return (
    <section>
      <h2 className="text-2xl font-semibold text-yellow-900 mb-6 drop-shadow-sm">
        Members
      </h2>
      {members.length === 0 ? (
        <p className="text-yellow-900 text-lg">No members yet.</p>
      ) : (
        <ul className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4">
          {members.map((member) => (
            <li
              key={member.id}
              className="flex justify-between items-center p-4 rounded-md bg-yellow-200 shadow hover:shadow-lg transition cursor-default"
            >
              <span className="text-yellow-900 font-medium">
                {member.user.name}
              </span>
              <button
                onClick={() => onRemoveMember(member.user.id)}
                className="text-white bg-red-600 hover:bg-red-700 transition px-3 py-1 rounded-md text-sm font-semibold shadow"
                aria-label={`Remove member ${member.user.name}`}
              >
                &times;
              </button>
            </li>
          ))}
        </ul>
      )}

      {/* Toggle Add Member form */}
      <button
        onClick={() => setShowAddForm(!showAddForm)}
        className="mt-8 bg-yellow-600 hover:bg-yellow-700 text-yellow-50 px-5 py-3 font-semibold rounded-md shadow-md transition w-34"
      >
        {showAddForm ? "Cancel" : "Add Member"}
      </button>

      {/* Animated Add Member form */}
      <AnimatePresence>
        {showAddForm && (
          <motion.div
            key="addForm"
            initial={{ opacity: 0, scaleY: 0, height: 0 }}
            animate={{ opacity: 1, scaleY: 1, height: "auto" }}
            exit={{ opacity: 0, scaleY: 0, height: 0 }}
            transition={{ duration: 0.4 }}
            style={{ originY: 0 }}
            className="mt-6 p-6 rounded-md bg-yellow-100 border border-yellow-300 shadow-inner overflow-hidden"
          >
            <input
              type="text"
              placeholder="Name"
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="w-full md:w-auto border border-yellow-400 rounded-md p-3 mb-4 md:mb-0 md:mr-4 text-yellow-900 font-semibold focus:outline-yellow-500 focus:ring-2 focus:ring-yellow-400 transition"
            />
            <button
              onClick={handleAdd}
              className="mt-2 md:mt-0 inline-block bg-yellow-600 hover:bg-yellow-700 text-yellow-50 px-6 py-3 rounded-md font-semibold shadow-md transition"
            >
              Add Member
            </button>
          </motion.div>
        )}
      </AnimatePresence>
    </section>
  );
};

export default MembersSection;
