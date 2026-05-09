import axios, { type AxiosError, type InternalAxiosRequestConfig } from "axios";
import { API_BASE_URL } from "../config";
import {
  clearTokens,
  getAccessToken,
  getRefreshToken,
  setTokens,
} from "../auth/tokenStorage";

const baseURL = API_BASE_URL
  ? `${API_BASE_URL.replace(/\/$/, "")}/api/v1`
  : "http://192.168.181.152/api/v1";

const api = axios.create({
  baseURL,
  withCredentials: false,
});

const refreshClient = axios.create({
  baseURL,
  withCredentials: false,
});

let refreshPromise: Promise<string | null> | null = null;

api.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = getAccessToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

async function refreshAccessToken(): Promise<string | null> {
  const refreshToken = getRefreshToken();
  if (!refreshToken) {
    return null;
  }

  try {
    const response = await refreshClient.post("/auth/refresh", {
      refreshToken,
    });
    const tokens = response.data?.tokens;

    if (!tokens?.accessToken || !tokens?.refreshToken) {
      return null;
    }

    setTokens(tokens.accessToken, tokens.refreshToken);
    return tokens.accessToken;
  } catch {
    return null;
  }
}

api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as
      | (InternalAxiosRequestConfig & { _retry?: boolean })
      | undefined;

    if (
      error.response?.status !== 401 ||
      !originalRequest ||
      originalRequest._retry ||
      !getRefreshToken()
    ) {
      return Promise.reject(error);
    }

    originalRequest._retry = true;

    if (!refreshPromise) {
      refreshPromise = refreshAccessToken().finally(() => {
        refreshPromise = null;
      });
    }

    const newAccessToken = await refreshPromise;
    if (!newAccessToken) {
      clearTokens();
      if (
        typeof window !== "undefined" &&
        window.location.pathname !== "/login"
      ) {
        window.location.assign("/login");
      }
      return Promise.reject(error);
    }

    originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
    return api(originalRequest);
  },
);

export default api;
