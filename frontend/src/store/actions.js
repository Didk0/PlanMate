export const setAuthData = (user, token) => (dispatch) => {
  dispatch({
    type: "LOGIN_USER",
    payload: { user, token },
  });
  localStorage.setItem("auth", JSON.stringify({ user, token }));
};

export const clearAuthData = () => (dispatch) => {
  dispatch({ type: "LOGOUT_USER" });
  localStorage.removeItem("auth");
  localStorage.removeItem("userCreated");
};
