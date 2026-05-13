import api from '@/shared/api/client';
import type {
  RoleResponse,
  UpdateUserRequest,
  UserResponse,
  UserRolesResponse,
} from '@/shared/types/user';
export async function getUserById(id: number): Promise<UserResponse> {
  const response = await api.get<UserResponse>(`/users/${id}`);
  return response.data;
}

export async function updateUser(id: number, payload: UpdateUserRequest): Promise<UserResponse> {
  const response = await api.put<UserResponse>(`/users/${id}`, payload);
  return response.data;
}

export async function deleteUser(id: number): Promise<void> {
  await api.delete(`/users/${id}`);
}

export async function getAllUsers(): Promise<UserResponse[]> {
  const response = await api.get<UserResponse[]>('/users');
  return response.data;
}

export async function getUserRoles(userId: number): Promise<UserRolesResponse> {
  const response = await api.get<UserRolesResponse>(`/rbac/users/${userId}/roles`);
  return response.data;
}

export async function assignRoleToUser(userId: number, roleName: string): Promise<void> {
  await api.post(`/rbac/users/${userId}/roles/${roleName}`, {});
}

export async function removeRoleFromUser(userId: number, roleName: string): Promise<void> {
  await api.delete(`/rbac/users/${userId}/roles/${roleName}`);
}

export async function getRole(roleName: string): Promise<RoleResponse> {
  const response = await api.get<RoleResponse>(`/rbac/roles/${roleName}`);
  return response.data;
}
