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

export type UserType = "individual" | "library";

export type RegistrationPayload = {
  username: string;
  fullName: string;
  phoneNumber?: string;
  email: string;
  userType: UserType;
  level: "normal";
  address?: string;
  postalCode?: string;
  city?: string;
  country?: string;
  defaultLanguage: string;
  password: string;
  certificateFile?: File | null;
};

export type LoginResponse = {
  token?: string;
  user?: {
    id?: string;
    displayName?: string;
  };
};

export type RegistrationResponse = {
  user?: {
    id?: string;
    username?: string;
    email?: string;
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

export async function registerUser(
  payload: RegistrationPayload,
): Promise<RequestHandlerResult<RegistrationResponse | null>> {
  const formData = new FormData();

  formData.set("username", payload.username);
  formData.set("fullName", payload.fullName);
  formData.set("email", payload.email);
  formData.set("userType", payload.userType);
  formData.set("level", payload.level);
  formData.set("defaultLanguage", payload.defaultLanguage);
  formData.set("password", payload.password);

  if (payload.phoneNumber) {
    formData.set("phoneNumber", payload.phoneNumber);
  }

  if (payload.address) {
    formData.set("address", payload.address);
  }

  if (payload.postalCode) {
    formData.set("postalCode", payload.postalCode);
  }

  if (payload.city) {
    formData.set("city", payload.city);
  }

  if (payload.country) {
    formData.set("country", payload.country);
  }

  if (payload.certificateFile) {
    formData.set("fnmtCertificate", payload.certificateFile);
  }

  try {
    const response = await executeBackendRequest("registerUser", {
      body: formData,
    });

    return handleJsonResponse<RegistrationResponse | null>(response, {
      successMessageKey: "auth.register.status.success",
      errorMessageKey: "auth.register.status.error",
      parseData: (payloadData) =>
        payloadData && typeof payloadData === "object"
          ? (payloadData as RegistrationResponse)
          : null,
    });
  } catch (error) {
    return handleRequestError(error, {
      messageKey: "auth.register.status.networkError",
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

export function buildResetPasswordRecoveryUrl(
  recoveryUrl: string,
  email: string,
) {
  try {
    const resolvedUrl = new URL(
      recoveryUrl,
      typeof window !== "undefined"
        ? window.location.origin
        : "http://localhost",
    );

    resolvedUrl.searchParams.set("email", email.trim());

    return resolvedUrl.toString();
  } catch {
    return recoveryUrl;
  }
}

export function handleCertificateLoginStart(): RequestHandlerResult<null> {
  return {
    ok: true,
    data: null,
    status: 0,
    messageKey: "auth.status.certificateOpening",
  };
}
