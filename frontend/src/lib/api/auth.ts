<<<<<<< HEAD
import api from '../http/client';
=======
import api from "../http/client";
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
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
<<<<<<< HEAD
} from '../../types/auth';

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
=======
} from "../../types/auth";

export async function login(payload: LoginRequest): Promise<AuthResponse> {
  const response = await api.post<AuthResponse>("/auth/login", payload);
  return response.data;
}

export async function register(
  payload: RegisterRequest,
): Promise<AuthResponse> {
  const response = await api.post<AuthResponse>("/auth/register", payload);
  return response.data;
}

export async function refreshTokens(
  payload: RefreshRequest,
): Promise<AuthResponse> {
  const response = await api.post<AuthResponse>("/auth/refresh", payload);
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
  return response.data;
}

export async function eidasLogin(): Promise<AuthResponse> {
<<<<<<< HEAD
  const response = await api.post<AuthResponse>('/auth/certificates/login');
=======
  const response = await api.post<AuthResponse>("/auth/certificates/login");
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
  return response.data;
}

// Backward-compatible alias; prefer eidasLogin.
export async function certificateLogin(): Promise<AuthResponse> {
  return eidasLogin();
}

export async function getCurrentUser(): Promise<AuthMeResponse> {
<<<<<<< HEAD
  const response = await api.get<AuthMeResponse>('/auth/me');
=======
  const response = await api.get<AuthMeResponse>("/auth/me");
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
  return response.data;
}

export async function logout(payload: LogoutRequest): Promise<void> {
<<<<<<< HEAD
  await api.post('/auth/logout', payload);
}

export async function listSessions(): Promise<AuthSessionResponse[]> {
  const response = await api.get<AuthSessionResponse[]>('/auth/sessions');
=======
  await api.post("/auth/logout", payload);
}

export async function listSessions(): Promise<AuthSessionResponse[]> {
  const response = await api.get<AuthSessionResponse[]>("/auth/sessions");
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
  return response.data;
}

export async function revokeSession(sessionId: string): Promise<void> {
  await api.delete(`/auth/sessions/${sessionId}`);
}

export async function revokeAllSessions(): Promise<void> {
<<<<<<< HEAD
  await api.delete('/auth/sessions');
}

export async function changePassword(payload: PasswordChangeRequest): Promise<void> {
  await api.post('/auth/password/change', payload);
=======
  await api.delete("/auth/sessions");
}

export async function changePassword(
  payload: PasswordChangeRequest,
): Promise<void> {
  await api.post("/auth/password/change", payload);
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
}

export async function requestPasswordReset(
  payload: PasswordResetRequest,
): Promise<PasswordResetRequestResponse> {
  const response = await api.post<PasswordResetRequestResponse>(
<<<<<<< HEAD
    '/auth/password-reset/request',
=======
    "/auth/password-reset/request",
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
    payload,
  );
  return response.data;
}

<<<<<<< HEAD
export async function confirmPasswordReset(payload: PasswordResetConfirmRequest): Promise<void> {
  await api.post('/auth/password-reset/confirm', payload);
=======
export async function confirmPasswordReset(
  payload: PasswordResetConfirmRequest,
): Promise<void> {
  await api.post("/auth/password-reset/confirm", payload);
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
}
