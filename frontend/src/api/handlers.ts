export type RequestHandlerResult<T> =
  | {
      ok: true;
      data: T;
      message: string;
      status: number;
    }
  | {
      ok: false;
      message: string;
      status?: number;
    };

type JsonResponseHandlerOptions<T> = {
  successMessage: string;
  errorMessage: string;
  parseData?: (payload: unknown) => T;
};

type ApiErrorPayload = {
  message?: unknown;
  detail?: unknown;
  error?: unknown;
};

// Placeholder parser: replace these generic helpers with endpoint-specific
// contracts once the backend response format is finalized.
export async function handleJsonResponse<T>(
  response: Response,
  options: JsonResponseHandlerOptions<T>,
): Promise<RequestHandlerResult<T>> {
  const payload = await readJsonPayload(response);
  const message = extractPayloadMessage(payload);

  if (!response.ok) {
    return {
      ok: false,
      status: response.status,
      message: message ?? options.errorMessage,
    };
  }

  return {
    ok: true,
    status: response.status,
    data: options.parseData ? options.parseData(payload) : (payload as T),
    message: message ?? options.successMessage,
  };
}

export function handleRequestError<T>(
  error: unknown,
  fallbackMessage: string,
): RequestHandlerResult<T> {
  if (error instanceof Error && error.message) {
    return {
      ok: false,
      message: error.message,
    };
  }

  return {
    ok: false,
    message: fallbackMessage,
  };
}

async function readJsonPayload(response: Response): Promise<unknown> {
  const contentType = response.headers.get("content-type") ?? "";

  if (!contentType.includes("application/json")) {
    return null;
  }

  try {
    return await response.json();
  } catch {
    return null;
  }
}

function extractPayloadMessage(payload: unknown) {
  if (!payload || typeof payload !== "object") {
    return null;
  }

  const candidatePayload = payload as ApiErrorPayload;
  const candidates = [
    candidatePayload.message,
    candidatePayload.detail,
    candidatePayload.error,
  ];

  for (const candidate of candidates) {
    if (typeof candidate === "string" && candidate.trim()) {
      return candidate;
    }
  }

  return null;
}
