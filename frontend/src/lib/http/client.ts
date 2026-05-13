<<<<<<< HEAD
import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios';
import { API_BASE_URL, API_PREFIX } from '../../config';
import { normalizeHttpError } from './errors';
import { clearTokens, getAccessToken, getRefreshToken, setTokens } from '../auth/tokenStorage';

const baseURL = API_BASE_URL
  ? `${API_BASE_URL.replace(/\/$/, '')}${API_PREFIX}`
=======
import axios, { type AxiosError, type InternalAxiosRequestConfig } from "axios";
import { API_BASE_URL, API_PREFIX } from "../../config";
import { normalizeHttpError } from "./errors";
import {
  clearTokens,
  getAccessToken,
  getRefreshToken,
  setTokens,
} from "../auth/tokenStorage";

const baseURL = API_BASE_URL
  ? `${API_BASE_URL.replace(/\/$/, "")}${API_PREFIX}`
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
  : `http://localhost:8080${API_PREFIX}`;

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
<<<<<<< HEAD
    const response = await refreshClient.post('/auth/refresh', {
=======
    const response = await refreshClient.post("/auth/refresh", {
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
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
      return Promise.reject(normalizeHttpError(error));
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
<<<<<<< HEAD
      if (typeof window !== 'undefined' && window.location.pathname !== '/login') {
        window.location.assign('/login');
=======
      if (
        typeof window !== "undefined" &&
        window.location.pathname !== "/login"
      ) {
        window.location.assign("/login");
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
      }
      return Promise.reject(normalizeHttpError(error));
    }

    originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
    return api(originalRequest);
  },
);

export default api;
