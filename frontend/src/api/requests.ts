type HttpMethod = "GET" | "POST" | "PUT" | "PATCH" | "DELETE";

type BackendRequestDefinition = {
  method: HttpMethod;
  path: string;
  description: string;
};

export const backendRequests = {
  loginWithCredentials: {
    method: "POST",
    path: "/api/auth/login",
    description: "Primary username/password sign-in endpoint.",
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

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL?.trim() ?? "";

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
