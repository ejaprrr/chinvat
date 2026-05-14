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

export interface ProfileCertificateResponse {
  id: string;
  providerCode: string;
  trustStatus: string;
  revocationStatus: string;
  assuranceLevel?: string;
  thumbprintSha256: string;
  subjectDn: string;
  issuerDn: string;
  serialNumber: string;
  notBefore: string;
  notAfter: string;
  primary: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface AddProfileCertificateRequest {
  certificatePem: string;
  providerCode?: string;
  assuranceLevel?: string;
}

export interface CompleteEidasProfileRequest {
  providerCode: string;
  externalSubjectId: string;
  username: string;
  fullName: string;
  phoneNumber?: string;
  email: string;
  password: string;
  userType?: string;
  addressLine?: string;
  postalCode?: string;
  city?: string;
  country?: string;
  defaultLanguage: string;
  certificatePem?: string;
  assuranceLevel?: string;
  certificateProviderCode?: string;
  identityReference?: string;
  nationality?: string;
}

export interface CompleteEidasProfileResponse {
  userId: string;
  providerCode: string;
  externalSubjectId: string;
  currentStatus: string;
  linkedAt: string;
  completedAt: string;
}

export async function listProfileCertificates(): Promise<ProfileCertificateResponse[]> {
  const response = await api.get<ProfileCertificateResponse[]>('/profile/certificates');
  return response.data;
}

export async function addProfileCertificate(
  payload: AddProfileCertificateRequest,
): Promise<ProfileCertificateResponse> {
  const response = await api.post<ProfileCertificateResponse>('/profile/certificates', payload);
  return response.data;
}

export async function setPrimaryProfileCertificate(
  credentialId: string,
): Promise<ProfileCertificateResponse> {
  const response = await api.post<ProfileCertificateResponse>(
    `/profile/certificates/${credentialId}/primary`,
  );
  return response.data;
}

export async function removeProfileCertificate(
  credentialId: string,
  reason: string = 'USER_REQUEST',
): Promise<void> {
  await api.delete(`/profile/certificates/${credentialId}?reason=${encodeURIComponent(reason)}`);
}

export async function completeEidasProfile(
  payload: CompleteEidasProfileRequest,
): Promise<CompleteEidasProfileResponse> {
  const response = await api.post<CompleteEidasProfileResponse>('/profile/eidas/complete', payload);
  return response.data;
}
