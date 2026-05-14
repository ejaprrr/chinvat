import api from '@/shared/api/client';
import type {
  AuthMeResponse,
  AuthResponse,
  AuthSessionResponse,
  LoginRequest,
  LogoutRequest,
  PasswordChangeRequest,
  PasswordResetConfirmRequest,
  PasswordResetRequest,
  PasswordResetRequestResponse,
  RefreshRequest,
  RegisterRequest,
} from '@/shared/types/auth';

type PaginationMetadata = {
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  isFirst: boolean;
  isLast: boolean;
  hasNext: boolean;
  offset: number;
};

type PageResponse<T> = {
  data: T[];
  pagination: PaginationMetadata;
};

export async function login(payload: LoginRequest): Promise<AuthResponse> {
  const response = await api.post<AuthResponse>('/auth/login', payload);
  return response.data;
}

export async function register(payload: RegisterRequest): Promise<AuthResponse> {
  const response = await api.post<AuthResponse>('/auth/register', payload);
  return response.data;
}

export async function refreshTokens(payload: RefreshRequest): Promise<AuthResponse> {
  const response = await api.post<AuthResponse>('/auth/refresh', payload);
  return response.data;
}

export async function eidasLogin(): Promise<AuthResponse> {
  const response = await api.post<AuthResponse>('/auth/certificates/login');
  return response.data;
}

export async function fnmtLogin(): Promise<AuthResponse> {
  const response = await api.post<AuthResponse>('/auth/fnmt/login');
  return response.data;
}

// Backward-compatible alias; prefer eidasLogin.
export async function certificateLogin(): Promise<AuthResponse> {
  return eidasLogin();
}

export async function getCurrentUser(): Promise<AuthMeResponse> {
  const response = await api.get<AuthMeResponse>('/auth/me');
  return response.data;
}

export async function logout(payload: LogoutRequest): Promise<void> {
  await api.post('/auth/logout', payload);
}

export async function listSessions(): Promise<AuthSessionResponse[]> {
  const response = await api.get<AuthSessionResponse[]>('/auth/sessions');
  return response.data;
}

export async function listSessionsPaged(
  page: number = 0,
  size: number = 10,
  sort?: string,
): Promise<PageResponse<AuthSessionResponse>> {
  const params = new URLSearchParams();
  params.append('page', page.toString());
  params.append('size', size.toString());
  if (sort) params.append('sort', sort);

  const response = await api.get<PageResponse<AuthSessionResponse>>(
    `/auth/sessions/paged?${params.toString()}`,
  );
  return response.data;
}

export async function revokeSession(sessionId: string): Promise<void> {
  await api.delete(`/auth/sessions/${sessionId}`);
}

export async function revokeAllSessions(): Promise<void> {
  await api.delete('/auth/sessions');
}

export async function changePassword(payload: PasswordChangeRequest): Promise<void> {
  await api.post('/auth/password/change', payload);
}

export async function requestPasswordReset(
  payload: PasswordResetRequest,
): Promise<PasswordResetRequestResponse> {
  const response = await api.post<PasswordResetRequestResponse>(
    '/auth/password-reset/request',
    payload,
  );
  return response.data;
}

export async function confirmPasswordReset(payload: PasswordResetConfirmRequest): Promise<void> {
  await api.post('/auth/password-reset/confirm', payload);
}
