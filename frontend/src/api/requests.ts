import { API_BASE_URL } from "../config";

type HttpMethod = "GET" | "POST" | "PUT" | "PATCH" | "DELETE";

type BackendRequestDefinition = {
  method: HttpMethod;
  path: string;
  description: string;
};

export const backendRequests = {
  loginWithCredentials: {
    method: "POST",
    path: "/api/v1/auth/login",
    description:
      "Authenticate user credentials and return access and refresh tokens.",
  },
  registerUser: {
    method: "POST",
    path: "/api/v1/auth/register",
    description: "Register a new user and issue access and refresh tokens.",
  },
  refreshTokens: {
    method: "POST",
    path: "/api/v1/auth/refresh",
    description: "Issue a new access + refresh token pair.",
  },
  logout: {
    method: "POST",
    path: "/api/v1/auth/logout",
    description: "Revoke the current session.",
  },
  requestPasswordReset: {
    method: "POST",
    path: "/api/v1/auth/password-reset/request",
    description: "Request a password reset code for a given email.",
  },
  confirmPasswordReset: {
    method: "POST",
    path: "/api/v1/auth/password-reset/confirm",
    description: "Consume a password reset code and update the user password.",
  },
  changePassword: {
    method: "POST",
    path: "/api/v1/auth/password/change",
    description:
      "Change the current user's password using the current password.",
  },
  getCurrentUser: {
    method: "GET",
    path: "/api/v1/auth/me",
    description: "Return profile information for the authenticated user.",
  },
  getHealth: {
    method: "GET",
    path: "/api/v1/health",
    description: "Health check endpoint.",
  },
  certificateLogin: {
    method: "GET",
    path: "/cert-login",
    description: "Browser redirect entrypoint for certificate-based sign-in.",
  },
} as const satisfies Record<string, BackendRequestDefinition>;

export type BackendRequestKey = keyof typeof backendRequests;

type RequestPayload = FormData | Record<string, unknown> | undefined;

type ExecuteBackendRequestOptions = {
  body?: RequestPayload;
  headers?: HeadersInit;
  signal?: AbortSignal;
};

const apiBaseUrl = API_BASE_URL;

export function buildBackendUrl(path: string): string {
  if (/^https?:\/\//.test(path)) {
    return path;
  }

  if (!apiBaseUrl) {
    return path;
  }

  const normalizedBaseUrl = apiBaseUrl.endsWith("/")
    ? apiBaseUrl.slice(0, -1)
    : apiBaseUrl;
  const normalizedPath = path.startsWith("/") ? path : `/${path}`;

  return `${normalizedBaseUrl}${normalizedPath}`;
}

export async function executeBackendRequest(
  requestKey: BackendRequestKey,
  options: ExecuteBackendRequestOptions = {},
) {
  const request = backendRequests[requestKey];
  const headers = new Headers(options.headers);

  let body: BodyInit | undefined;

  if (options.body instanceof FormData) {
    body = options.body;
  } else if (options.body !== undefined) {
    headers.set("Content-Type", "application/json");
    body = JSON.stringify(options.body);
  }

  return fetch(buildBackendUrl(request.path), {
    method: request.method,
    headers,
    body,
    signal: options.signal,
    credentials: "include",
  });
}
