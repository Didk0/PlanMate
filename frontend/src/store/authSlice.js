import { createSlice } from "@reduxjs/toolkit";

const initialState = {
  user: null,
  token: null,
  appUserId: null,
};

const authSlice = createSlice({
  name: "auth",
  initialState,
  reducers: {
    loginUser: (state, action) => {
      state.user = action.payload.user;
      state.token = action.payload.token;
    },
    logoutUser: (state) => {
      state.user = null;
      state.token = null;
      state.appUserId = null;
    },
    setAppUserId: (state, action) => {
      state.appUserId = action.payload;
    },
  },
});

export const { loginUser, logoutUser, setAppUserId } = authSlice.actions;

export const selectAppUserId = (state) => state.auth.appUserId;
export const selectIsAdmin = (state) => (state.auth.user?.roles ?? []).includes("ADMIN");

export default authSlice.reducer;
