import { motion } from "framer-motion";

const MOTION_TAGS = {
  div: motion.div,
  form: motion.form,
};

const PageShell = ({
  as: Tag = "div",
  maxWidth = "max-w-4xl",
  center = false,
  padding = "p-8",
  cardClassName = "",
  children,
  ...rest
}) => {
  const MotionTag = MOTION_TAGS[Tag] ?? motion.div;

  return (
    <div
      className={`min-h-screen bg-canvas px-4 sm:px-6 py-10 flex flex-col items-center ${
        center ? "justify-center" : ""
      }`}
    >
      <MotionTag
        initial={{ opacity: 0, y: 8 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.25, ease: "easeOut" }}
        className={`w-full ${maxWidth} bg-surface rounded-xl border border-slate-700 shadow-sm ${padding} ${cardClassName}`}
        {...rest}
      >
        {children}
      </MotionTag>
    </div>
  );
};

export default PageShell;
