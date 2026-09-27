import axios from "axios";
import { useAuthStore } from "../features/auth/store/authStore";

const gatewayBaseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080/api/v1";
const api = axios.create({ baseURL: gatewayBaseUrl });
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("accessToken");
  if (token) config.headers["Authorization"] = "Bearer " + token;
  return config;
});

let isRefreshing = false;
let failedQueue: Array<{ resolve: (token: string) => void; reject: (error: unknown) => void }> = [];
const processQueue = (error: unknown, token: string | null = null) => {
  failedQueue.forEach((promise) => error ? promise.reject(error) : promise.resolve(token as string));
  isRefreshing = false;
  failedQueue = [];
};

api.interceptors.response.use((response) => response, async (error) => {
  const originalRequest = error.config;
  if (error.response?.status === 401 && !originalRequest._retry) {
    if (isRefreshing) return new Promise((resolve, reject) => failedQueue.push({ resolve, reject })).then(() => api(originalRequest));
    originalRequest._retry = true;
    isRefreshing = true;
    const refreshToken = localStorage.getItem("refreshToken");
    if (!refreshToken) {
      processQueue(error);
      useAuthStore.getState().clearTokens();
      window.location.href = "/login";
      return Promise.reject(error);
    }
    try {
      const response = await axios.post(`${gatewayBaseUrl}/auth/token/refresh`, { refreshToken });
      const newAccessToken = response.data?.data?.accessToken || response.data?.accessToken;
      const newRefreshToken = response.data?.data?.refreshToken || response.data?.refreshToken;
      if (newAccessToken && newRefreshToken) {
        useAuthStore.getState().setTokens(newAccessToken, newRefreshToken);
        api.defaults.headers.common["Authorization"] = "Bearer " + newAccessToken;
        processQueue(null, newAccessToken);
        return api(originalRequest);
      }
    } catch (refreshError) {
      processQueue(refreshError);
      useAuthStore.getState().clearTokens();
      window.location.href = "/login";
      return Promise.reject(refreshError);
    }
  }
  return Promise.reject(error);
});

export default api;
