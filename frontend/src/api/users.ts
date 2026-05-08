import api from "./client";
import type {
  UserResponse,
  UpdateUserRequest,
  UserRolesResponse,
  RoleResponse,
} from "../types/user";

/**
 * GET /api/v1/users/{id}
 * Get user profile by ID
 * Requires bearer token
 */
export async function getUserById(id: number): Promise<UserResponse> {
  const response = await api.get<UserResponse>(`/users/${id}`);
  return response.data;
}

/**
 * PUT /api/v1/users/{id}
 * Update user profile
 * Requires bearer token
 */
export async function updateUser(
  id: number,
  payload: UpdateUserRequest,
): Promise<UserResponse> {
  const response = await api.put<UserResponse>(`/users/${id}`, payload);
  return response.data;
}

/**
 * DELETE /api/v1/users/{id}
 * Delete user account
 * Requires bearer token
 */
export async function deleteUser(id: number): Promise<void> {
  await api.delete(`/users/${id}`);
}

/**
 * GET /api/v1/users
 * Get all users (admin only)
 * Requires bearer token
 */
export async function getAllUsers(): Promise<UserResponse[]> {
  const response = await api.get<UserResponse[]>("/users");
  return response.data;
}

/**
 * GET /api/v1/rbac/users/{userId}/roles
 * Get roles assigned to a user
 * Requires bearer token
 */
export async function getUserRoles(userId: number): Promise<UserRolesResponse> {
  const response = await api.get<UserRolesResponse>(
    `/rbac/users/${userId}/roles`,
  );
  return response.data;
}

/**
 * POST /api/v1/rbac/users/{userId}/roles/{roleName}
 * Assign a role to a user
 * Requires bearer token + USERS:MANAGE permission
 */
export async function assignRoleToUser(
  userId: number,
  roleName: string,
): Promise<void> {
  await api.post(`/rbac/users/${userId}/roles/${roleName}`, {});
}

/**
 * DELETE /api/v1/rbac/users/{userId}/roles/{roleName}
 * Remove a role from a user
 * Requires bearer token + USERS:MANAGE permission
 */
export async function removeRoleFromUser(
  userId: number,
  roleName: string,
): Promise<void> {
  await api.delete(`/rbac/users/${userId}/roles/${roleName}`);
}

/**
 * GET /api/v1/rbac/roles/{roleName}
 * Get role definition with permissions
 * Requires bearer token
 */
export async function getRole(roleName: string): Promise<RoleResponse> {
  const response = await api.get<RoleResponse>(`/rbac/roles/${roleName}`);
  return response.data;
}
