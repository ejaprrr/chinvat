export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL?.trim() ?? "";
export const RESET_PASSWORD_URL =
  import.meta.env.VITE_RESET_PASSWORD_URL?.trim() || null;

export const API_PREFIX = "/api/v1";
export const AUTH_API_PREFIX = `${API_PREFIX}/auth`;
