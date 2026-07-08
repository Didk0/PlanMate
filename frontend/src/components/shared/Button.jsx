import Spinner from "@/components/shared/Spinner";
import { buttonClasses } from "@/lib/buttonClasses";

const Button = ({
  type = "button",
  variant = "primary",
  size = "md",
  isLoading = false,
  disabled,
  className = "",
  children,
  ...rest
}) => {
  return (
    <button
      type={type}
      disabled={isLoading || disabled}
      className={`${buttonClasses(variant, size)} ${className}`}
      {...rest}
    >
      {isLoading && <Spinner size="sm" />}
      {children}
    </button>
  );
};

export default Button;
