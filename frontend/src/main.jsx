import "./index.css";
import App from "./App.jsx";
import { createRoot } from "react-dom/client";
import { AuthProvider } from "react-oauth2-code-pkce";
import { Provider } from "react-redux";
import LoadingScreen from "@/components/shared/LoadingScreen";
import { authConfig } from "@/lib/authConfig.js";
import { store } from "@/store/store.js";

createRoot(document.getElementById("root")).render(
  <AuthProvider authConfig={authConfig} loadingComponent={<LoadingScreen />}>
    <Provider store={store}>
      <App />
    </Provider>
  </AuthProvider>
);
