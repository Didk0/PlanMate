const PageShell = ({
  as: Tag = "div",
  maxWidth = "max-w-4xl",
  center = false,
  padding = "p-8",
  cardClassName = "",
  children,
  ...rest
}) => {
  return (
    <div
      className={`min-h-screen bg-gradient-to-r from-yellow-400 via-yellow-300 to-yellow-200 px-6 py-12 flex flex-col items-center ${
        center ? "justify-center" : ""
      }`}
    >
      <Tag
        className={`w-full ${maxWidth} bg-yellow-100 bg-opacity-80 rounded-lg shadow-lg ${padding} ${cardClassName}`}
        {...rest}
      >
        {children}
      </Tag>
    </div>
  );
};

export default PageShell;
