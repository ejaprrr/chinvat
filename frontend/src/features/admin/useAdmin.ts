import { useCallback, useEffect, useState } from 'react';
import { getAllUsersPaginated, getCertificateCredentials, getPermissions } from './api';
import { getErrorCode } from '@/shared/api/errors';
import type { UserResponse, PermissionResponse } from '@/shared/types/user';
import type { CertificateCredential, PageResponse } from './api';

export interface AdminPageState {
  users: UserResponse[];
  credentials: CertificateCredential[];
  permissions: PermissionResponse[];
  usersLoading: boolean;
  credentialsLoading: boolean;
  permissionsLoading: boolean;
  error: string | null;
  usersPagination: PageResponse<UserResponse>['pagination'] | null;
  credentialsPagination: PageResponse<CertificateCredential>['pagination'] | null;
  permissionsPagination: PageResponse<PermissionResponse>['pagination'] | null;
  usersPage: number;
  credentialsPage: number;
  permissionsPage: number;
}

export function useAdmin() {
  const [state, setState] = useState<AdminPageState>({
    users: [],
    credentials: [],
    permissions: [],
    usersLoading: false,
    credentialsLoading: false,
    permissionsLoading: false,
    error: null,
    usersPagination: null,
    credentialsPagination: null,
    permissionsPagination: null,
    usersPage: 0,
    credentialsPage: 0,
    permissionsPage: 0,
  });

  const loadUsers = useCallback(async (page: number = 0) => {
    try {
      setState((s) => ({ ...s, usersLoading: true, error: null }));
      const result = await getAllUsersPaginated(page, 20);
      setState((s) => ({
        ...s,
        users: result.data,
        usersPagination: result.pagination,
        usersPage: page,
        usersLoading: false,
      }));
    } catch (error) {
      const errorCode = getErrorCode(error, 'USERS_LOAD_FAILED');
      setState((s) => ({ ...s, error: errorCode, usersLoading: false }));
    }
  }, []);

  const loadCredentials = useCallback(async (page: number = 0, userId?: string) => {
    try {
      setState((s) => ({ ...s, credentialsLoading: true, error: null }));
      const result = await getCertificateCredentials(page, 20, userId);
      setState((s) => ({
        ...s,
        credentials: result.data,
        credentialsPagination: result.pagination,
        credentialsPage: page,
        credentialsLoading: false,
      }));
    } catch (error) {
      const errorCode = getErrorCode(error, 'CREDENTIALS_LOAD_FAILED');
      setState((s) => ({ ...s, error: errorCode, credentialsLoading: false }));
    }
  }, []);

  const loadPermissions = useCallback(async (page: number = 0) => {
    try {
      setState((s) => ({ ...s, permissionsLoading: true, error: null }));
      const result = await getPermissions(page, 20);
      setState((s) => ({
        ...s,
        permissions: result.data,
        permissionsPagination: result.pagination,
        permissionsPage: page,
        permissionsLoading: false,
      }));
    } catch (error) {
      const errorCode = getErrorCode(error, 'PERMISSIONS_LOAD_FAILED');
      setState((s) => ({ ...s, error: errorCode, permissionsLoading: false }));
    }
  }, []);

  // Load all data on mount
  useEffect(() => {
    Promise.all([loadUsers(0), loadCredentials(0), loadPermissions(0)]);
  }, [loadUsers, loadCredentials, loadPermissions]);

  return {
    ...state,
    loadUsers,
    loadCredentials,
    loadPermissions,
  };
}
