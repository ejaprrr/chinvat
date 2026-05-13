<<<<<<< HEAD
import { AxiosError } from 'axios';
=======
import { AxiosError } from "axios";
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0

export type AppErrorCode = string;

type ApiErrorShape = {
  code?: unknown;
  errorCode?: unknown;
  message?: unknown;
  errors?: Array<{ code?: unknown }>;
};

export class AppHttpError extends Error {
  readonly isAppHttpError = true;
  readonly code: AppErrorCode;
  readonly status?: number;
  readonly backendMessage?: string;

  constructor(code: AppErrorCode, status?: number, backendMessage?: string) {
    super(backendMessage || code);
<<<<<<< HEAD
    this.name = 'AppHttpError';
=======
    this.name = "AppHttpError";
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
    this.code = code;
    this.status = status;
    this.backendMessage = backendMessage;
  }
}

function isApiErrorShape(value: unknown): value is ApiErrorShape {
<<<<<<< HEAD
  return Boolean(value) && typeof value === 'object';
=======
  return Boolean(value) && typeof value === "object";
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
}

function extractCodeFromPayload(data: unknown): string | null {
  if (!isApiErrorShape(data)) {
    return null;
  }

<<<<<<< HEAD
  if (typeof data.code === 'string' && data.code.trim()) {
    return data.code;
  }

  if (typeof data.errorCode === 'string' && data.errorCode.trim()) {
=======
  if (typeof data.code === "string" && data.code.trim()) {
    return data.code;
  }

  if (typeof data.errorCode === "string" && data.errorCode.trim()) {
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
    return data.errorCode;
  }

  const firstError = Array.isArray(data.errors) ? data.errors[0] : null;
<<<<<<< HEAD
  if (firstError && typeof firstError.code === 'string' && firstError.code) {
=======
  if (firstError && typeof firstError.code === "string" && firstError.code) {
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
    return firstError.code;
  }

  return null;
}

function codeFromHttpStatus(status?: number): string | null {
  if (!status) {
    return null;
  }
  return `HTTP_${status}`;
}

function extractMessageFromPayload(data: unknown): string | null {
  if (!isApiErrorShape(data)) {
    return null;
  }

<<<<<<< HEAD
  if (typeof data.message === 'string' && data.message.trim()) {
=======
  if (typeof data.message === "string" && data.message.trim()) {
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
    return data.message;
  }

  return null;
}

export function normalizeHttpError(error: unknown): AppHttpError {
  if (error instanceof AppHttpError) {
    return error;
  }

  if (error instanceof AxiosError) {
    const status = error.response?.status;
    const payloadMessage = extractMessageFromPayload(error.response?.data);
    const payloadCode = extractCodeFromPayload(error.response?.data);
    if (payloadCode) {
      return new AppHttpError(payloadCode, status, payloadMessage || undefined);
    }

<<<<<<< HEAD
    if (error.code === 'ERR_NETWORK') {
      return new AppHttpError('NETWORK_ERROR');
    }

    if (error.code === 'ECONNABORTED') {
      return new AppHttpError('REQUEST_TIMEOUT');
=======
    if (error.code === "ERR_NETWORK") {
      return new AppHttpError("NETWORK_ERROR");
    }

    if (error.code === "ECONNABORTED") {
      return new AppHttpError("REQUEST_TIMEOUT");
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
    }

    const statusCode = codeFromHttpStatus(status);
    if (statusCode) {
      return new AppHttpError(statusCode, status, payloadMessage || undefined);
    }

<<<<<<< HEAD
    return new AppHttpError('REQUEST_FAILED');
  }

  return new AppHttpError('UNKNOWN_ERROR');
}

export function getErrorCode(error: unknown, fallback = 'UNKNOWN_ERROR'): string {
=======
    return new AppHttpError("REQUEST_FAILED");
  }

  return new AppHttpError("UNKNOWN_ERROR");
}

export function getErrorCode(
  error: unknown,
  fallback = "UNKNOWN_ERROR",
): string {
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
  return normalizeHttpError(error).code || fallback;
}

export function getErrorDisplay(
  error: unknown,
  options: { fallbackCode: string; fallbackMessage: string },
): { code: string; message: string } {
  const normalized = normalizeHttpError(error);
  const code = normalized.code || options.fallbackCode;

  if (normalized.backendMessage) {
    return { code, message: normalized.backendMessage };
  }

  return {
    code,
    message: `${options.fallbackMessage} [${code}]`,
  };
}
