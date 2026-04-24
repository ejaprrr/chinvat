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
    return handleRequestError(error, {
      messageKey: "auth.status.networkError",
    });
  }
}

export async function handleLoginResponse(
  response: Response,
): Promise<RequestHandlerResult<LoginResponse | null>> {
  return handleJsonResponse<LoginResponse | null>(response, {
    successMessageKey: "auth.status.loginSuccess",
    errorMessageKey: "auth.status.loginError",
    parseData: (payload) =>
      payload && typeof payload === "object"
        ? (payload as LoginResponse)
        : null,
  });
}

export function getCertificateLoginUrl() {
  return buildBackendUrl(backendRequests.certificateLogin.path);
}

export function getConfiguredResetPasswordUrl() {
  return import.meta.env.VITE_RESET_PASSWORD_URL?.trim() || null;
}

export function handleCertificateLoginStart(): RequestHandlerResult<null> {
  return {
    ok: true,
    data: null,
    status: 0,
    messageKey: "auth.status.certificateOpening",
  };
}
