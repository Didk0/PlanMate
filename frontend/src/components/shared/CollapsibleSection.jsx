import { AnimatePresence, motion } from "framer-motion";

const CollapsibleSection = ({ show, className = "", children }) => {
  return (
    <AnimatePresence>
      {show && (
        <motion.div
          initial={{ opacity: 0, scaleY: 0, height: 0 }}
          animate={{ opacity: 1, scaleY: 1, height: "auto" }}
          exit={{ opacity: 0, scaleY: 0, height: 0 }}
          transition={{ duration: 0.4 }}
          style={{ originY: 0 }}
          className={`p-6 rounded-md bg-yellow-100 border border-yellow-300 shadow-inner overflow-hidden ${className}`}
        >
          {children}
        </motion.div>
      )}
    </AnimatePresence>
  );
};

export default CollapsibleSection;
