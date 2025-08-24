import { configureStore } from "@reduxjs/toolkit";
import { errorReducer } from "./reducers/errorReducer";

export const store = configureStore({
  reducer: {
    errors: errorReducer,
  },
})