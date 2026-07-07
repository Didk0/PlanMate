import axios from "axios";

const api = axios.create({
  baseURL: `${import.meta.env.VITE_BACK_END_URL}/api`,
  timeout: 10000,
});

let authToken = null;

export const setAuthToken = (token) => {
  authToken = token;
};

api.interceptors.request.use((config) => {
  if (authToken) {
    config.headers.Authorization = `Bearer ${authToken}`;
  } else {
    delete config.headers.Authorization;
  }
  return config;
});

export default api;
