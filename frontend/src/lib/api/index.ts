import api from "../http/client";
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
} from "../../types/auth";
import type {
  UserResponse,
  UpdateUserRequest,
  UserRolesResponse,
  RoleResponse,
} from "../../types/user";

// --- Auth endpoints ---
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
  return response.data;
}

export async function getCurrentUser(): Promise<AuthMeResponse> {
  const response = await api.get<AuthMeResponse>("/auth/me");
  return response.data;
}

export async function logout(payload: LogoutRequest): Promise<void> {
  await api.post("/auth/logout", payload);
}

export async function listSessions(): Promise<AuthSessionResponse[]> {
  const response = await api.get<AuthSessionResponse[]>("/auth/sessions");
  return response.data;
}

export async function revokeSession(sessionId: string): Promise<void> {
  await api.delete(`/auth/sessions/${sessionId}`);
}

export async function revokeAllSessions(): Promise<void> {
  await api.delete("/auth/sessions");
}

export async function changePassword(
  payload: PasswordChangeRequest,
): Promise<void> {
  await api.post("/auth/password/change", payload);
}

export async function requestPasswordReset(
  payload: PasswordResetRequest,
): Promise<PasswordResetRequestResponse> {
  const response = await api.post<PasswordResetRequestResponse>(
    "/auth/password-reset/request",
    payload,
  );
  return response.data;
}

export async function confirmPasswordReset(
  payload: PasswordResetConfirmRequest,
): Promise<void> {
  await api.post("/auth/password-reset/confirm", payload);
}

// --- User endpoints ---
export async function getUserById(id: number): Promise<UserResponse> {
  const response = await api.get<UserResponse>(`/users/${id}`);
  return response.data;
}

export async function updateUser(
  id: number,
  payload: UpdateUserRequest,
): Promise<UserResponse> {
  const response = await api.put<UserResponse>(`/users/${id}`, payload);
  return response.data;
}

export async function deleteUser(id: number): Promise<void> {
  await api.delete(`/users/${id}`);
}

export async function getAllUsers(): Promise<UserResponse[]> {
  const response = await api.get<UserResponse[]>("/users");
  return response.data;
}

export async function getUserRoles(userId: number): Promise<UserRolesResponse> {
  const response = await api.get<UserRolesResponse>(`/rbac/users/${userId}/roles`);
  return response.data;
}

export async function assignRoleToUser(
  userId: number,
  roleName: string,
): Promise<void> {
  await api.post(`/rbac/users/${userId}/roles/${roleName}`, {});
}

export async function removeRoleFromUser(
  userId: number,
  roleName: string,
): Promise<void> {
  await api.delete(`/rbac/users/${userId}/roles/${roleName}`);
}

export async function getRole(roleName: string): Promise<RoleResponse> {
  const response = await api.get<RoleResponse>(`/rbac/roles/${roleName}`);
  return response.data;
}

export default {
  login,
  register,
  refreshTokens,
  getCurrentUser,
  logout,
  listSessions,
  revokeSession,
  revokeAllSessions,
  changePassword,
  requestPasswordReset,
  confirmPasswordReset,
  getUserById,
  updateUser,
  deleteUser,
  getAllUsers,
  getUserRoles,
  assignRoleToUser,
  removeRoleFromUser,
  getRole,
};
