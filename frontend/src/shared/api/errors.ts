import { AxiosError } from 'axios';
import i18n from '@/shared/i18n';
import { getErrorMessageKey } from './errorCodes';

export type AppErrorCode = string;

export interface ErrorDisplay {
  code: string;
  message: string;
  messageKey?: string;
}

type ApiErrorShape = {
  code?: unknown;
  errorCode?: unknown;
  message?: unknown;
  messageKey?: unknown;
  errors?: Array<{ code?: unknown }>;
};

export class AppHttpError extends Error {
  readonly isAppHttpError = true;
  readonly code: AppErrorCode;
  readonly status?: number;
  readonly backendMessage?: string;
  readonly messageKey?: string;

  constructor(code: AppErrorCode, status?: number, backendMessage?: string, messageKey?: string) {
    super(backendMessage || code);
    this.name = 'AppHttpError';
    this.code = code;
    this.status = status;
    this.backendMessage = backendMessage;
    // Fallback to derived messageKey if not explicitly provided
    this.messageKey = messageKey || getErrorMessageKey(code) || undefined;
  }
}

function isApiErrorShape(value: unknown): value is ApiErrorShape {
  return Boolean(value) && typeof value === 'object';
}

function extractCodeFromPayload(data: unknown): string | null {
  if (!isApiErrorShape(data)) {
    return null;
  }

  if (typeof data.code === 'string' && data.code.trim()) {
    return data.code;
  }

  if (typeof data.errorCode === 'string' && data.errorCode.trim()) {
    return data.errorCode;
  }

  const firstError = Array.isArray(data.errors) ? data.errors[0] : null;
  if (firstError && typeof firstError.code === 'string' && firstError.code) {
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

  if (typeof data.message === 'string' && data.message.trim()) {
    return data.message;
  }

  return null;
}

function extractMessageKeyFromPayload(data: unknown): string | null {
  if (!isApiErrorShape(data)) {
    return null;
  }

  if (typeof data.messageKey === 'string' && data.messageKey.trim()) {
    return data.messageKey;
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
    const payloadMessageKey = extractMessageKeyFromPayload(error.response?.data);

    if (payloadCode) {
      return new AppHttpError(
        payloadCode,
        status,
        payloadMessage || undefined,
        payloadMessageKey || undefined,
      );
    }

    if (error.code === 'ERR_NETWORK') {
      return new AppHttpError('NETWORK_ERROR');
    }

    if (error.code === 'ECONNABORTED') {
      return new AppHttpError('REQUEST_TIMEOUT');
    }

    const statusCode = codeFromHttpStatus(status);
    if (statusCode) {
      return new AppHttpError(
        statusCode,
        status,
        payloadMessage || undefined,
        payloadMessageKey || undefined,
      );
    }

    return new AppHttpError('REQUEST_FAILED');
  }

  return new AppHttpError('UNKNOWN_ERROR');
}

export function getErrorCode(error: unknown, fallback = 'UNKNOWN_ERROR'): string {
  return normalizeHttpError(error).code || fallback;
}

export function getErrorDisplay(
  error: unknown,
  options: { fallbackCode: string; fallbackMessage: string },
): ErrorDisplay {
  const normalized = normalizeHttpError(error);
  const code = normalized.code || options.fallbackCode;
  const messageKey = normalized.messageKey;

  if (messageKey) {
    const translated = i18n.t(messageKey, {
      defaultValue: normalized.backendMessage || options.fallbackMessage,
    });

    if (translated && translated !== messageKey) {
      return { code, message: translated, messageKey };
    }
  }

  if (normalized.backendMessage) {
    return { code, message: normalized.backendMessage, messageKey };
  }

  return {
    code,
    message: `${options.fallbackMessage} [${code}]`,
    messageKey,
  };
}
