import api from '@/shared/api/client';
import type {
  UserResponse,
  UpdateUserRequest,
  PermissionResponse,
  UserRolesResponse,
  RoleResponse,
} from '@/shared/types/user';
import type { RegisterRequest } from '@/shared/types/auth';

export interface PageResponse<T> {
  data: T[];
  pagination: {
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
    isFirst: boolean;
    isLast: boolean;
    hasNext: boolean;
    offset: number;
  };
}

export interface CertificateCredential {
  id: string;
  userId: string;
  providerCode: string;
  credentialType: string;
  trustStatus: string;
  revocationStatus: string;
  assuranceLevel: string;
  registrationSource: string;
  thumbprintSha256: string;
  subjectDn: string;
  issuerDn: string;
  serialNumber: string;
  notBefore: string;
  notAfter: string;
  approvedBy: string;
  approvedAt: string;
  revokedBy: string;
  revokedAt: string;
  primary: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreatePermissionRequest {
  code: string;
  description?: string;
}

export interface UpdatePermissionRequest {
  description?: string;
}

export interface BindCertificateCredentialRequest {
  userId: string;
  providerCode: string;
  registrationSource: string;
  certificatePem: string;
  assuranceLevel?: string;
}

// Users API
export async function getAllUsersPaginated(
  page: number = 0,
  size: number = 20,
  sort?: string,
): Promise<PageResponse<UserResponse>> {
  const params = new URLSearchParams();
  params.append('page', page.toString());
  params.append('size', size.toString());
  if (sort) params.append('sort', sort);

  const response = await api.get<PageResponse<UserResponse>>(`/users?${params.toString()}`);
  return response.data;
}

export async function getUserById(id: string): Promise<UserResponse> {
  const response = await api.get<UserResponse>(`/users/${id}`);
  return response.data;
}

export async function createUser(payload: RegisterRequest): Promise<UserResponse> {
  const response = await api.post<UserResponse>('/users', payload);
  return response.data;
}

export async function updateUser(id: string, payload: UpdateUserRequest): Promise<UserResponse> {
  const response = await api.put<UserResponse>(`/users/${id}`, payload);
  return response.data;
}

export async function deleteUser(id: string): Promise<void> {
  await api.delete(`/users/${id}`);
}

export async function restoreUser(id: string): Promise<UserResponse> {
  const response = await api.post<UserResponse>(`/users/${id}/restore`, {});
  return response.data;
}

export async function permanentlyDeleteUser(id: string): Promise<void> {
  await api.delete(`/users/${id}/permanent`);
}

// Certificate Credentials API
export async function getCertificateCredentials(
  page: number = 0,
  size: number = 20,
  userId?: string,
): Promise<PageResponse<CertificateCredential>> {
  const params = new URLSearchParams();
  params.append('page', page.toString());
  params.append('size', size.toString());
  if (userId) params.append('userId', userId);

  const response = await api.get<PageResponse<CertificateCredential>>(
    `/admin/credentials?${params.toString()}`,
  );
  return response.data;
}

export async function revokeCertificateCredential(
  credentialId: string,
  reason: string,
): Promise<void> {
  await api.post(`/admin/credentials/${credentialId}/revoke`, { reason });
}

export async function bindCertificateCredential(
  payload: BindCertificateCredentialRequest,
): Promise<CertificateCredential> {
  const response = await api.post<CertificateCredential>('/admin/credentials', payload);
  return response.data;
}

// RBAC API
export async function getPermissions(
  page: number = 0,
  size: number = 20,
): Promise<PageResponse<PermissionResponse>> {
  const params = new URLSearchParams();
  params.append('page', page.toString());
  params.append('size', size.toString());

  const response = await api.get<PageResponse<PermissionResponse>>(
    `/rbac/permissions/paged?${params.toString()}`,
  );
  return response.data;
}

export async function getAllPermissions(): Promise<PermissionResponse[]> {
  const response = await api.get<PermissionResponse[]>('/rbac/permissions');
  return response.data;
}

export async function createPermission(
  payload: CreatePermissionRequest,
): Promise<PermissionResponse> {
  const response = await api.post<PermissionResponse>('/rbac/permissions', payload);
  return response.data;
}

export async function updatePermission(
  code: string,
  payload: UpdatePermissionRequest,
): Promise<PermissionResponse> {
  const response = await api.put<PermissionResponse>(`/rbac/permissions/${code}`, payload);
  return response.data;
}

export async function deletePermission(code: string): Promise<void> {
  await api.delete(`/rbac/permissions/${code}`);
}

export async function getUserRoles(userId: string): Promise<UserRolesResponse> {
  const response = await api.get<UserRolesResponse>(`/rbac/users/${userId}/roles`);
  return response.data;
}

export async function assignRoleToUser(userId: string, roleName: string): Promise<void> {
  await api.post(`/rbac/users/${userId}/roles/${roleName}`, {});
}

export async function removeRoleFromUser(userId: string, roleName: string): Promise<void> {
  await api.delete(`/rbac/users/${userId}/roles/${roleName}`);
}

export async function getRole(roleName: string): Promise<RoleResponse> {
  const response = await api.get<RoleResponse>(`/rbac/roles/${roleName}`);
  return response.data;
}
