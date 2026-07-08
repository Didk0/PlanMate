import { AnimatePresence, motion } from "framer-motion";

const CollapsibleSection = ({ show, className = "", children }) => {
  return (
    <AnimatePresence>
      {show && (
        <motion.div
          initial={{ opacity: 0, height: 0 }}
          animate={{ opacity: 1, height: "auto" }}
          exit={{ opacity: 0, height: 0 }}
          transition={{ duration: 0.3 }}
          className={`bg-slate-900 border border-slate-700 rounded-lg p-6 overflow-hidden ${className}`}
        >
          {children}
        </motion.div>
      )}
    </AnimatePresence>
  );
};

export default CollapsibleSection;
