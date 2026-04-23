import {
  backendRequests,
  buildBackendUrl,
  executeBackendRequest,
} from "./requests";
import {
  handleJsonResponse,
  handleRequestError,
  type RequestHandlerResult,
} from "./handlers";

export type LoginPayload = {
  username: string;
  password: string;
};

export type LoginResponse = {
  token?: string;
  user?: {
    id?: string;
    displayName?: string;
  };
};

export async function loginWithCredentials(
  payload: LoginPayload,
): Promise<RequestHandlerResult<LoginResponse | null>> {
  try {
    const response = await executeBackendRequest("loginWithCredentials", {
      body: payload,
    });

    return handleLoginResponse(response);
  } catch (error) {
    return handleRequestError(
      error,
      "The login request could not reach the backend. Replace this placeholder handler once the API is available.",
    );
  }
}

// Placeholder handler: tighten the response contract once the backend auth
// payload is defined.
export async function handleLoginResponse(
  response: Response,
): Promise<RequestHandlerResult<LoginResponse | null>> {
  return handleJsonResponse<LoginResponse | null>(response, {
    successMessage:
      "Login request sent. Replace this placeholder success handler with the real authentication flow.",
    errorMessage:
      "Login failed. Replace this placeholder error handler with the backend-specific response mapping.",
    parseData: (payload) =>
      payload && typeof payload === "object"
        ? (payload as LoginResponse)
        : null,
  });
}

export function getCertificateLoginUrl() {
  return buildBackendUrl(backendRequests.certificateLogin.path);
}

// Placeholder handler: keep certificate navigation feedback centralized even
// while the flow is still a direct browser redirect.
export function handleCertificateLoginStart(): RequestHandlerResult<null> {
  return {
    ok: true,
    data: null,
    status: 0,
    message: "Redirecting to the certificate login route.",
  };
}
