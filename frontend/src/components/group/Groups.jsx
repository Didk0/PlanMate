import React, { useEffect, useState } from "react";
import { AnimatePresence } from "framer-motion";
import { motion } from "motion/react";
import { useDispatch, useSelector } from "react-redux";
import { Link } from "react-router-dom";
import { createGroup, loadAllGroups } from "../../store/actions";

const Groups = () => {

  const { isLoading, errorMessage } = useSelector((state) => state.errors);

  const [groups, setGroups] = useState([]);

  const dispatch = useDispatch();

  useEffect(() => {
    const fetchGroups = async () => {
      
        try {
          const data = await dispatch(loadAllGroups());
          if (data) {
            setGroups(data);
          }
        } catch (error) {
          console.error("Failed to refresh token or load groups", error);
        }
    };
    fetchGroups();
  }, [dispatch]);

  const [showCreateForm, setShowCreateForm] = useState(false);
  const [newGroup, setNewGroup] = useState({ name: "", description: "" });

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setNewGroup((prev) => ({ ...prev, [name]: value }));
  };

  const handleCreateGroup = async () => {
    const { name, description } = newGroup;
    if (!name.trim() || !description.trim()) {
      alert("Please enter both a group name and description.");
      return;
    }
      try {
        const createdGroup = await dispatch(createGroup(newGroup));
        if (createdGroup) {
          setGroups((prev) => [...prev, createdGroup]);
          setNewGroup({ name: "", description: "" });
          setShowCreateForm(false);
        }
      } catch (error) {
        console.error("Create group failed", error);
      }
  };

  if (isLoading || !groups) {
    return (
      <div className="min-h-screen flex items-center justify-center text-yellow-900 font-semibold text-xl">
        Loading groups...
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
    <div className="min-h-screen bg-gradient-to-r from-yellow-400 via-yellow-300 to-yellow-200 flex flex-col items-center px-6 py-16">
      <div className="w-full max-w-4xl bg-yellow-100 bg-opacity-80 rounded-lg shadow-lg p-8">
        <h1 className="text-3xl font-extrabold text-yellow-900 mb-6 drop-shadow-md">
          Groups
        </h1>

        {groups.length === 0 ? (
          <p className="text-yellow-900 text-lg">
            You are not member of any groups.
          </p>
        ) : (
          <ul className="space-y-4">
            {groups.map((group) => (
              <li
                key={group.id}
                className="bg-yellow-200 rounded-md p-4 shadow hover:shadow-lg transition cursor-pointer"
              >
                <Link
                  to={`/groups/${group.id}`}
                  className="text-yellow-900 font-semibold text-lg hover:underline"
                >
                  {group.name}
                </Link>
              </li>
            ))}
          </ul>
        )}

        {/* Create Group button */}
        <button
          onClick={() => setShowCreateForm((show) => !show)}
          className="mt-8 mb-6 bg-yellow-600 hover:bg-yellow-700 text-yellow-50 px-5 py-3 font-semibold rounded-md shadow-md transition w-35"
        >
          {showCreateForm ? "Cancel" : "Create Group"}
        </button>

        {/* Create Group form */}
        <AnimatePresence>
          {showCreateForm && (
            <motion.div
              key="createGroupForm"
              initial={{ opacity: 0, scaleY: 0, height: 0 }}
              animate={{ opacity: 1, scaleY: 1, height: "auto" }}
              exit={{ opacity: 0, scaleY: 0, height: 0 }}
              transition={{ duration: 0.4 }}
              style={{ originY: 0 }}
              className="mb-8 p-6 rounded-md bg-yellow-100 border border-yellow-300 shadow-inner overflow-hidden"
            >
              <input
                name="name"
                type="text"
                placeholder="Group Name"
                value={newGroup.name}
                onChange={handleInputChange}
                className="w-full md:w-auto border border-yellow-400 rounded-md p-3 mb-4 md:mb-0 md:mr-4 text-yellow-900 font-semibold focus:outline-yellow-500 focus:ring-2 focus:ring-yellow-400 transition"
              />

              <input
                name="description"
                type="text"
                placeholder="Group Description"
                value={newGroup.description}
                onChange={handleInputChange}
                className="w-full md:w-auto border border-yellow-400 rounded-md p-3 mb-4 md:mb-0 md:mr-4 text-yellow-900 font-semibold focus:outline-yellow-500 focus:ring-2 focus:ring-yellow-400 transition"
              />

              <button
                onClick={handleCreateGroup}
                className="mt-2 md:mt-0 inline-block bg-yellow-600 hover:bg-yellow-700 text-yellow-50 px-6 py-3 rounded-md font-semibold shadow-md transition"
              >
                Create Group
              </button>
            </motion.div>
          )}
        </AnimatePresence>
      </div>
    </div>
  );
};

export default Groups;
