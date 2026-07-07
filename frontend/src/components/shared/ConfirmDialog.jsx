import { AnimatePresence, motion } from "framer-motion";
import Button from "@/components/shared/Button";

const ConfirmDialog = ({
  open,
  title = "Are you sure?",
  message,
  confirmLabel = "Confirm",
  cancelLabel = "Cancel",
  onConfirm,
  onCancel,
}) => {
  return (
    <AnimatePresence>
      {open && (
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          transition={{ duration: 0.2 }}
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 px-4"
          onClick={onCancel}
        >
          <motion.div
            initial={{ opacity: 0, scale: 0.95, y: 10 }}
            animate={{ opacity: 1, scale: 1, y: 0 }}
            exit={{ opacity: 0, scale: 0.95, y: 10 }}
            transition={{ duration: 0.2 }}
            className="w-full max-w-sm bg-yellow-100 rounded-lg shadow-lg p-6"
            onClick={(event) => event.stopPropagation()}
          >
            <h2 className="text-xl font-extrabold text-yellow-900 mb-3 drop-shadow-sm">{title}</h2>
            <p className="text-yellow-900 mb-6">{message}</p>
            <div className="flex justify-end items-center gap-4">
              <button
                type="button"
                onClick={onCancel}
                className="font-semibold text-yellow-700 hover:text-yellow-900 transition"
              >
                {cancelLabel}
              </button>
              <Button onClick={onConfirm}>{confirmLabel}</Button>
            </div>
          </motion.div>
        </motion.div>
      )}
    </AnimatePresence>
  );
};

export default ConfirmDialog;
