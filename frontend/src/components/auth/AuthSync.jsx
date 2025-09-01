import { useContext, useEffect } from "react";
import { AuthContext } from "react-oauth2-code-pkce";
import { useDispatch } from "react-redux";
import { clearAuthData, setAuthData } from "../../store/actions/index";

const AuthSync = () => {
  const { isAuthenticated, tokenData, token } = useContext(AuthContext);
  const dispatch = useDispatch();

  useEffect(() => {
    if (token && tokenData) {
      const user = {
        name: tokenData.name,
        email: tokenData.email,
      };
      dispatch(setAuthData(user, token));
    } else {
      dispatch(clearAuthData());
    }
  }, [isAuthenticated, token, tokenData, dispatch]);

  return null;
};

export default AuthSync;
