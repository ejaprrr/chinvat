// The app is served behind the container nginx proxy by default.
// An absolute backend URL can still be provided at build time for local development.
declare global {
  interface Window {
    CONFIG?: {
      API_BASE_URL?: string;
    };
  }
}

export const API_BASE_URL =
  window.CONFIG?.API_BASE_URL ||
  (typeof import.meta.env !== 'undefined' ? import.meta.env.VITE_API_BASE_URL : undefined) ||
  '';

export const API_PREFIX = '/api/v1';
export const AUTH_API_PREFIX = `${API_PREFIX}/auth`;

export default {
  API_BASE_URL,
  API_PREFIX,
  AUTH_API_PREFIX,
};
