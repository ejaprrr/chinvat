<<<<<<< HEAD
import api from '../http/client';
=======
import api from "../http/client";
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
import type {
  RoleResponse,
  UpdateUserRequest,
  UserResponse,
  UserRolesResponse,
<<<<<<< HEAD
} from '../../types/user';
=======
} from "../../types/user";
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0

export async function getUserById(id: number): Promise<UserResponse> {
  const response = await api.get<UserResponse>(`/users/${id}`);
  return response.data;
}

<<<<<<< HEAD
export async function updateUser(id: number, payload: UpdateUserRequest): Promise<UserResponse> {
=======
export async function updateUser(
  id: number,
  payload: UpdateUserRequest,
): Promise<UserResponse> {
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
  const response = await api.put<UserResponse>(`/users/${id}`, payload);
  return response.data;
}

export async function deleteUser(id: number): Promise<void> {
  await api.delete(`/users/${id}`);
}

export async function getAllUsers(): Promise<UserResponse[]> {
<<<<<<< HEAD
  const response = await api.get<UserResponse[]>('/users');
=======
  const response = await api.get<UserResponse[]>("/users");
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
  return response.data;
}

export async function getUserRoles(userId: number): Promise<UserRolesResponse> {
<<<<<<< HEAD
  const response = await api.get<UserRolesResponse>(`/rbac/users/${userId}/roles`);
  return response.data;
}

export async function assignRoleToUser(userId: number, roleName: string): Promise<void> {
  await api.post(`/rbac/users/${userId}/roles/${roleName}`, {});
}

export async function removeRoleFromUser(userId: number, roleName: string): Promise<void> {
=======
  const response = await api.get<UserRolesResponse>(
    `/rbac/users/${userId}/roles`,
  );
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
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
  await api.delete(`/rbac/users/${userId}/roles/${roleName}`);
}

export async function getRole(roleName: string): Promise<RoleResponse> {
  const response = await api.get<RoleResponse>(`/rbac/roles/${roleName}`);
  return response.data;
}
