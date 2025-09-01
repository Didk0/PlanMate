import { useEffect } from "react";
import { useSelector } from "react-redux";
import { setAuthToken } from "../../api/api";

const ApiTokenSetter = () => {
  const token = useSelector((state) => state.auth.token);

  useEffect(() => {
    setAuthToken(token);
  }, [token]);

  return null;
};

export default ApiTokenSetter;
