import { Link } from "react-router-dom";
import { buttonClasses } from "@/lib/buttonClasses";

const LinkButton = ({
  to,
  variant = "primary",
  size = "md",
  className = "",
  children,
  ...rest
}) => {
  return (
    <Link to={to} className={`${buttonClasses(variant, size)} ${className}`} {...rest}>
      {children}
    </Link>
  );
};

export default LinkButton;
