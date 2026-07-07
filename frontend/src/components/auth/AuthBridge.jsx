import { useContext, useEffect } from "react";
import { AuthContext } from "react-oauth2-code-pkce";
import { useDispatch } from "react-redux";
import { setAuthToken } from "@/api/client";
import { loginUser, logoutUser } from "@/store/authSlice";
import { useProvisionUser } from "./useProvisionUser";

const AuthBridge = () => {
  const { token, tokenData } = useContext(AuthContext);
  const dispatch = useDispatch();

  useProvisionUser(token, tokenData);

  useEffect(() => {
    setAuthToken(token);

    if (token && tokenData) {
      const user = {
        name: tokenData.preferred_username,
        email: tokenData.email,
        firstName: tokenData.given_name,
        lastName: tokenData.family_name,
      };
      dispatch(loginUser({ user, token }));
    } else {
      dispatch(logoutUser());
    }
  }, [token, tokenData, dispatch]);

  return null;
};

export default AuthBridge;
