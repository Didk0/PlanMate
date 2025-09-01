import "./index.css";
import App from "./App.jsx";
import { createRoot } from "react-dom/client";
import { AuthProvider } from "react-oauth2-code-pkce";
import { Provider } from "react-redux";
import { authConfig } from "./auth/authConfig.js";
import { store } from "./store/store.js";

createRoot(document.getElementById("root")).render(
  <AuthProvider
    authConfig={authConfig}
    loadingComponent={<div>Loading...</div>}
  >
    <Provider store={store}>
      <App />
    </Provider>
  </AuthProvider>
);
