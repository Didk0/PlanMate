import Spinner from "@/components/shared/Spinner";
import { inputClasses } from "@/components/shared/TextInput";

const SearchInput = ({
  value,
  onChange,
  placeholder = "Search...",
  isSearching = false,
  className = "",
  ...rest
}) => {
  return (
    <div className={`relative ${className}`}>
      <input
        type="search"
        value={value}
        onChange={onChange}
        placeholder={placeholder}
        className={`${inputClasses} w-full pr-9`}
        {...rest}
      />
      {isSearching && (
        <Spinner size="sm" className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400" />
      )}
    </div>
  );
};

export default SearchInput;
