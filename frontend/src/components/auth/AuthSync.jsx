import { useContext, useEffect } from "react";
import { AuthContext } from "react-oauth2-code-pkce";
import { useDispatch } from "react-redux";
import { clearAuthData, setAuthData } from "../../store/actions/index";

const AuthSync = () => {
  const { tokenData, token } = useContext(AuthContext);
  const dispatch = useDispatch();

  useEffect(() => {
    if (token && tokenData) {
      const user = {
        name: tokenData.preferred_username,
        email: tokenData.email,
        firstName: tokenData.given_name,
        lastName: tokenData.family_name,
      };
      dispatch(setAuthData(user, token));
    } else {
      dispatch(clearAuthData());
    }
  }, [token, tokenData, dispatch]);

  return null;
};

export default AuthSync;
