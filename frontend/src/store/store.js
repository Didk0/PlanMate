import { configureStore } from "@reduxjs/toolkit";
import { authReducer } from "./reducers/authReducer";
import { errorReducer } from "./reducers/errorReducer";

const storedAuth = localStorage.getItem("auth")
  ? JSON.parse(localStorage.getItem("auth"))
  : { user: null, token: null };

const preloadedState = {
  auth: storedAuth,
};

export const store = configureStore({
  reducer: {
    errors: errorReducer,
    auth: authReducer,
  },
  preloadedState,
});
