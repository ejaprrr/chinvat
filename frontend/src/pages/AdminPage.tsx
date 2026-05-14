import { useEffect, useId, useState, type FormEvent } from 'react';
import { useTranslation } from 'react-i18next';
import { AlertCircle, Edit2, Trash2, ChevronLeft, ChevronRight, Plus, X } from 'lucide-react';
import { useDocumentTitle } from '@/shared/lib/documentTitle';
import { useAuth } from '@/shared/auth';
import FormPage from '@/shared/ui/FormPage';
import { ActionButton } from '@/shared/ui/Action';
import { useAdmin } from '@/features/admin';
import LanguageSwitcher from '@/shared/ui/LanguageSwitcher';
import {
  assignPermissionToRole,
  bindCertificateCredential,
  createPermission,
  deletePermission,
  createUser,
  getAllPermissions,
  getRole,
  getUserRoles,
  removePermissionFromRole,
  removeRoleFromUser,
  updateUser,
  deleteUser,
  permanentlyDeleteUser,
  restoreUser,
  revokeCertificateCredential,
  assignRoleToUser,
  updatePermission,
} from '@/features/admin/api';
import { getErrorDisplay } from '@/shared/api/errors';
import type { RegisterRequest } from '@/shared/types/auth';
import type { UserResponse, UpdateUserRequest } from '@/shared/types/user';
import '@/shared/ui/admin.css';

type Tab = 'users' | 'certificates' | 'rbac' | 'roles' | 'permissions';

type PermissionFormState = {
  code: string;
  description: string;
};

type BindCredentialFormState = {
  userId: string;
  providerCode: string;
  registrationSource: string;
  assuranceLevel: string;
  certificatePem: string;
};

type CreateUserFormState = RegisterRequest & {
  accessLevel: 'SUPERADMIN' | 'ADMIN' | 'GOLD' | 'PREMIUM' | 'NORMAL';
};

const initialBindCredentialForm: BindCredentialFormState = {
  userId: '',
  providerCode: '',
  registrationSource: '',
  assuranceLevel: '',
  certificatePem: '',
};

const initialCreateUserForm: CreateUserFormState = {
  username: '',
  fullName: '',
  phoneNumber: '',
  email: '',
  password: '',
  userType: 'INDIVIDUAL',
  addressLine: '',
  postalCode: '',
  city: '',
  country: '',
  defaultLanguage: 'en',
  accessLevel: 'NORMAL',
};

const initialPermissionForm: PermissionFormState = {
  code: '',
  description: '',
};

const KNOWN_ROLES = ['USER', 'ADMIN', 'EMPLOYEE', 'SUPERADMIN'];

function AdminPage() {
  useDocumentTitle('meta.adminPageTitle');
  const { t } = useTranslation();
  const { hasRole } = useAuth();
  const {
    users,
    credentials,
    permissions,
    usersLoading,
    credentialsLoading,
    permissionsLoading,
    error,
    usersPagination,
    usersPage,
    permissionsPagination,
    permissionsPage,
    credentialsPage,
    loadUsers,
    loadCredentials,
    loadPermissions,
  } = useAdmin();

  const [activeTab, setActiveTab] = useState<Tab>('users');
  const [editingUserId, setEditingUserId] = useState<string | null>(null);
  const [editingUserData, setEditingUserData] = useState<UpdateUserRequest | null>(null);
  const [statusMessage, setStatusMessage] = useState<string | null>(null);
  const [selectedUserForCerts, setSelectedUserForCerts] = useState<string | null>(null);
  const [revokeReason, setRevokeReason] = useState('');
  const [selectedCredentialId, setSelectedCredentialId] = useState<string | null>(null);
  const [userRoleManagementId, setUserRoleManagementId] = useState<string | null>(null);
  const [bindCredentialForm, setBindCredentialForm] =
    useState<BindCredentialFormState>(initialBindCredentialForm);
  const [createUserForm, setCreateUserForm] = useState<CreateUserFormState>(initialCreateUserForm);
  const [isCreateUserOpen, setIsCreateUserOpen] = useState(false);
  const [roleInspectorValue, setRoleInspectorValue] = useState('');
  const [inspectedRole, setInspectedRole] = useState<{
    roleName: string;
    permissions: string[];
  } | null>(null);
  const [catalogPermissions, setCatalogPermissions] = useState<
    { code: string; description?: string }[]
  >([]);
  const [catalogPermissionsLoading, setCatalogPermissionsLoading] = useState(false);
  const [isRoleInspectorOpen, setIsRoleInspectorOpen] = useState(false);
  const [selectedPermissionToAdd, setSelectedPermissionToAdd] = useState('');
  const [isUserToolsOpen, setIsUserToolsOpen] = useState(false);
  const [isPermissionCatalogOpen, setIsPermissionCatalogOpen] = useState(false);
  const [permissionModalMode, setPermissionModalMode] = useState<'create' | 'edit' | null>(null);
  const [permissionForm, setPermissionForm] = useState<PermissionFormState>(initialPermissionForm);
  const [isBindCredentialOpen, setIsBindCredentialOpen] = useState(false);
  const [restoreUserId, setRestoreUserId] = useState('');
  const [permanentDeleteUserId, setPermanentDeleteUserId] = useState('');
  const [isOperating, setIsOperating] = useState(false);
  const [userRolesCache, setUserRolesCache] = useState<Record<string, string[]>>({});
  const [userSearchQuery, setUserSearchQuery] = useState('');
  const [editingUserEmail, setEditingUserEmail] = useState('');
  const [selectedRoleForMatrix, setSelectedRoleForMatrix] = useState<string | null>(null);
  const [roleMatrixData, setRoleMatrixData] = useState<Record<string, string[]>>({});
  const [roleMatrixLoading, setRoleMatrixLoading] = useState(false);
  const [matrixPermissionToAdd, setMatrixPermissionToAdd] = useState('');

  const errorId = useId();

  // Check if user is admin
  if (!hasRole('ADMIN') && !hasRole('SUPERADMIN')) {
    return (
      <FormPage
        title={t('admin.accessDenied')}
        intro=""
        aria-labelledby="admin-denied-title"
        titleId="admin-denied-title"
      >
        <div className="flex flex-col gap-4 items-center text-center py-8">
          <AlertCircle size={48} className="text-danger-700" />
          <p className="text-muted">{t('admin.accessDeniedMessage')}</p>
          <ActionButton onClick={() => window.history.back()}>
            {t('admin.backToProfile')}
          </ActionButton>
        </div>
      </FormPage>
    );
  }

  const handleEditUser = (userData: UserResponse) => {
    setEditingUserId(userData.id.toString());
    setEditingUserEmail(userData.email);
    setEditingUserData({
      username: userData.username,
      fullName: userData.fullName,
      phoneNumber: userData.phoneNumber,
      userType: userData.userType as 'INDIVIDUAL' | 'LIBRARY',
      accessLevel: userData.accessLevel as 'SUPERADMIN' | 'ADMIN' | 'GOLD' | 'PREMIUM' | 'NORMAL',
      defaultLanguage: userData.defaultLanguage,
      addressLine: userData.addressLine || '',
      postalCode: userData.postalCode || '',
      city: userData.city || '',
      country: userData.country || '',
    });
  };

  const handleCreateUser = async (e: FormEvent) => {
    e.preventDefault();

    if (
      !createUserForm.username.trim() ||
      !createUserForm.fullName.trim() ||
      !createUserForm.email.trim()
    ) {
      setStatusMessage(t('admin.usernameEmailRequired'));
      return;
    }

    try {
      setIsOperating(true);
      const createdUser = await createUser({
        username: createUserForm.username.trim(),
        fullName: createUserForm.fullName.trim(),
        email: createUserForm.email.trim(),
        password: createUserForm.password,
        userType: createUserForm.userType,
        phoneNumber: createUserForm.phoneNumber?.trim() || undefined,
        addressLine: createUserForm.addressLine?.trim() || undefined,
        postalCode: createUserForm.postalCode?.trim() || undefined,
        city: createUserForm.city?.trim() || undefined,
        country: createUserForm.country?.trim() || undefined,
        defaultLanguage: createUserForm.defaultLanguage,
      });
      if (createUserForm.accessLevel !== 'NORMAL') {
        await updateUser(createdUser.id.toString(), {
          username: createdUser.username,
          fullName: createdUser.fullName,
          phoneNumber: createdUser.phoneNumber,
          userType: createdUser.userType as 'INDIVIDUAL' | 'LIBRARY',
          accessLevel: createUserForm.accessLevel,
          defaultLanguage: createdUser.defaultLanguage,
          addressLine: createdUser.addressLine,
          postalCode: createdUser.postalCode,
          city: createdUser.city,
          country: createdUser.country,
        });
      }
      setStatusMessage(t('admin.userCreated'));
      setCreateUserForm(initialCreateUserForm);
      setIsCreateUserOpen(false);
      await loadUsers(0);
    } catch (err) {
      const errorMsg = getErrorDisplay(err, {
        fallbackCode: 'ADMIN_CREATE_USER_FAILED',
        fallbackMessage: 'Failed to create user',
      }).message;
      setStatusMessage(errorMsg);
    } finally {
      setIsOperating(false);
    }
  };

  const handleSaveUser = async (e: FormEvent) => {
    e.preventDefault();
    if (!editingUserId || !editingUserData) return;

    try {
      setIsOperating(true);
      await updateUser(editingUserId, editingUserData);
      setStatusMessage(t('admin.userUpdated'));
      setEditingUserId(null);
      setEditingUserData(null);
      await loadUsers(usersPage);
    } catch (err) {
      const errorMsg = getErrorDisplay(err, {
        fallbackCode: 'ADMIN_UPDATE_FAILED',
        fallbackMessage: 'Failed to update user',
      }).message;
      setStatusMessage(errorMsg);
    } finally {
      setIsOperating(false);
    }
  };

  const handleDeleteUser = async (userId: string) => {
    if (!confirm(t('admin.confirmDelete'))) return;

    try {
      setIsOperating(true);
      await deleteUser(userId);
      setStatusMessage(t('admin.userDeleted'));
      await loadUsers(usersPage);
    } catch (err) {
      const errorMsg = getErrorDisplay(err, {
        fallbackCode: 'ADMIN_DELETE_FAILED',
        fallbackMessage: 'Failed to delete user',
      }).message;
      setStatusMessage(errorMsg);
    } finally {
      setIsOperating(false);
    }
  };

  const handleRevokeCertificate = async (credentialId: string) => {
    if (!revokeReason.trim()) {
      setStatusMessage(t('admin.revocationReasonRequired'));
      return;
    }

    try {
      setIsOperating(true);
      await revokeCertificateCredential(credentialId, revokeReason);
      setStatusMessage(t('admin.certificateRevoked'));
      setSelectedCredentialId(null);
      setRevokeReason('');
      await loadCredentials(credentialsPage, selectedUserForCerts || undefined);
    } catch (err) {
      const errorMsg = getErrorDisplay(err, {
        fallbackCode: 'ADMIN_REVOKE_FAILED',
        fallbackMessage: 'Failed to revoke certificate',
      }).message;
      setStatusMessage(errorMsg);
    } finally {
      setIsOperating(false);
    }
  };

  const handleAssignRole = async (userId: string, roleName: string) => {
    try {
      setIsOperating(true);
      await assignRoleToUser(userId, roleName);
      setStatusMessage(t('admin.roleAssigned'));
      try {
        const updatedRoles = await getUserRoles(userId);
        setUserRolesCache((prev) => ({ ...prev, [userId]: updatedRoles.roles }));
      } catch {
        // ignore cache refresh failure
      }
      await loadUsers(usersPage);
    } catch (err) {
      const errorMsg = getErrorDisplay(err, {
        fallbackCode: 'ADMIN_ASSIGN_ROLE_FAILED',
        fallbackMessage: 'Failed to assign role',
      }).message;
      setStatusMessage(errorMsg);
    } finally {
      setIsOperating(false);
    }
  };

  const handleRestoreUser = async () => {
    if (!restoreUserId.trim()) return;

    try {
      setIsOperating(true);
      await restoreUser(restoreUserId.trim());
      setStatusMessage(t('admin.userRestored'));
      setRestoreUserId('');
      await loadUsers(usersPage);
    } catch (err) {
      const errorMsg = getErrorDisplay(err, {
        fallbackCode: 'ADMIN_RESTORE_FAILED',
        fallbackMessage: 'Failed to restore user',
      }).message;
      setStatusMessage(errorMsg);
    } finally {
      setIsOperating(false);
    }
  };

  const handlePermanentDeleteUser = async () => {
    if (!permanentDeleteUserId.trim()) return;
    if (!confirm(t('admin.confirmDelete'))) {
      return;
    }

    try {
      setIsOperating(true);
      await permanentlyDeleteUser(permanentDeleteUserId.trim());
      setStatusMessage(t('admin.userDeleted'));
      setPermanentDeleteUserId('');
      await loadUsers(usersPage);
    } catch (err) {
      const errorMsg = getErrorDisplay(err, {
        fallbackCode: 'ADMIN_PERMANENT_DELETE_FAILED',
        fallbackMessage: 'Failed to permanently delete user',
      }).message;
      setStatusMessage(errorMsg);
    } finally {
      setIsOperating(false);
    }
  };

  const handleBindCredential = async (e: FormEvent) => {
    e.preventDefault();

    if (
      !bindCredentialForm.userId.trim() ||
      !bindCredentialForm.providerCode.trim() ||
      !bindCredentialForm.registrationSource.trim() ||
      !bindCredentialForm.certificatePem.trim()
    ) {
      setStatusMessage(t('admin.certificateFieldsRequired'));
      return;
    }

    try {
      setIsOperating(true);
      await bindCertificateCredential({
        userId: bindCredentialForm.userId.trim(),
        providerCode: bindCredentialForm.providerCode.trim(),
        registrationSource: bindCredentialForm.registrationSource.trim(),
        assuranceLevel: bindCredentialForm.assuranceLevel.trim() || undefined,
        certificatePem: bindCredentialForm.certificatePem.trim(),
      });
      setStatusMessage(t('admin.certificateBound'));
      setBindCredentialForm(initialBindCredentialForm);
      setIsBindCredentialOpen(false);
      await loadCredentials(credentialsPage, selectedUserForCerts || undefined);
    } catch (err) {
      const errorMsg = getErrorDisplay(err, {
        fallbackCode: 'ADMIN_BIND_CREDENTIAL_FAILED',
        fallbackMessage: 'Failed to bind certificate credential',
      }).message;
      setStatusMessage(errorMsg);
    } finally {
      setIsOperating(false);
    }
  };

  const openPermissionEditor = (
    mode: 'create' | 'edit',
    permission?: { code: string; description?: string },
  ) => {
    setPermissionModalMode(mode);
    setPermissionForm({
      code: permission?.code ?? '',
      description: permission?.description ?? '',
    });
  };

  const handleSavePermission = async (e: FormEvent) => {
    e.preventDefault();

    if (!permissionForm.code.trim()) {
      setStatusMessage(t('admin.permissionCodeRequired'));
      return;
    }

    try {
      setIsOperating(true);
      if (permissionModalMode === 'edit') {
        await updatePermission(permissionForm.code.trim(), {
          description: permissionForm.description.trim() || undefined,
        });
        setStatusMessage(t('admin.permissionUpdated'));
      } else {
        await createPermission({
          code: permissionForm.code.trim(),
          description: permissionForm.description.trim() || undefined,
        });
        setStatusMessage(t('admin.permissionCreated'));
      }
      setPermissionModalMode(null);
      setPermissionForm(initialPermissionForm);
      await loadPermissions(permissionsPage);
    } catch (err) {
      const errorMsg = getErrorDisplay(err, {
        fallbackCode:
          permissionModalMode === 'edit'
            ? 'ADMIN_PERMISSION_UPDATE_FAILED'
            : 'ADMIN_PERMISSION_CREATE_FAILED',
        fallbackMessage: 'Failed to save permission',
      }).message;
      setStatusMessage(errorMsg);
    } finally {
      setIsOperating(false);
    }
  };

  const handleDeletePermission = async (code: string) => {
    if (!confirm(t('admin.confirmDelete'))) return;

    try {
      setIsOperating(true);
      await deletePermission(code);
      setStatusMessage(t('admin.permissionDeleted'));
      await loadPermissions(permissionsPage);
    } catch (err) {
      const errorMsg = getErrorDisplay(err, {
        fallbackCode: 'ADMIN_PERMISSION_DELETE_FAILED',
        fallbackMessage: 'Failed to delete permission',
      }).message;
      setStatusMessage(errorMsg);
    } finally {
      setIsOperating(false);
    }
  };

  const handleInspectRole = async () => {
    if (!roleInspectorValue.trim()) {
      setStatusMessage(t('admin.roleNameRequired'));
      return;
    }

    try {
      setIsOperating(true);
      const result = await getRole(roleInspectorValue.trim());
      setInspectedRole(result);
      setSelectedPermissionToAdd('');
    } catch (err) {
      const errorMsg = getErrorDisplay(err, {
        fallbackCode: 'ADMIN_ROLE_LOOKUP_FAILED',
        fallbackMessage: 'Failed to load role',
      }).message;
      setStatusMessage(errorMsg);
    } finally {
      setIsOperating(false);
    }
  };

  const handleAssignPermissionToRole = async (roleName: string, permissionCode: string) => {
    if (!permissionCode) return;
    try {
      setIsOperating(true);
      await assignPermissionToRole(roleName, permissionCode);
      const updated = await getRole(roleName);
      setInspectedRole(updated);
      setSelectedPermissionToAdd('');
    } catch (err) {
      const errorMsg = getErrorDisplay(err, {
        fallbackCode: 'ADMIN_PERMISSION_ASSIGN_FAILED',
        fallbackMessage: 'Failed to assign permission',
      }).message;
      setStatusMessage(errorMsg);
    } finally {
      setIsOperating(false);
    }
  };

  const handleRemovePermissionFromRole = async (roleName: string, permissionCode: string) => {
    try {
      setIsOperating(true);
      await removePermissionFromRole(roleName, permissionCode);
      const updated = await getRole(roleName);
      setInspectedRole(updated);
    } catch (err) {
      const errorMsg = getErrorDisplay(err, {
        fallbackCode: 'ADMIN_PERMISSION_REMOVE_FAILED',
        fallbackMessage: 'Failed to remove permission',
      }).message;
      setStatusMessage(errorMsg);
    } finally {
      setIsOperating(false);
    }
  };

  const handleMatrixAssignPermission = async (roleName: string, permCode: string) => {
    if (!permCode) return;
    try {
      setIsOperating(true);
      await assignPermissionToRole(roleName, permCode);
      const updated = await getRole(roleName);
      setRoleMatrixData((prev) => ({ ...prev, [roleName]: updated.permissions }));
      setMatrixPermissionToAdd('');
    } catch (err) {
      setStatusMessage(
        getErrorDisplay(err, {
          fallbackCode: 'ADMIN_PERMISSION_ASSIGN_FAILED',
          fallbackMessage: 'Failed to assign permission',
        }).message,
      );
    } finally {
      setIsOperating(false);
    }
  };

  const handleMatrixRemovePermission = async (roleName: string, permCode: string) => {
    try {
      setIsOperating(true);
      await removePermissionFromRole(roleName, permCode);
      const updated = await getRole(roleName);
      setRoleMatrixData((prev) => ({ ...prev, [roleName]: updated.permissions }));
    } catch (err) {
      setStatusMessage(
        getErrorDisplay(err, {
          fallbackCode: 'ADMIN_PERMISSION_REMOVE_FAILED',
          fallbackMessage: 'Failed to remove permission',
        }).message,
      );
    } finally {
      setIsOperating(false);
    }
  };

  const handlePrimaryAction = () => {
    if (activeTab === 'users') {
      setIsCreateUserOpen(true);
      return;
    }

    if (activeTab === 'certificates') {
      setIsBindCredentialOpen(true);
      return;
    }

    if (activeTab === 'rbac') {
      setIsRoleInspectorOpen(true);
      return;
    }

    if (activeTab === 'roles') {
      return;
    }

    openPermissionEditor('create');
  };

  const primaryActionLabel =
    activeTab === 'users'
      ? t('admin.addUser')
      : activeTab === 'certificates'
        ? t('admin.bindCertificate')
        : activeTab === 'rbac'
          ? t('admin.inspectRole')
          : activeTab === 'roles'
            ? t('admin.manageRoles')
            : t('admin.addPermission');

  useEffect(() => {
    if (!isPermissionCatalogOpen && !isRoleInspectorOpen && activeTab !== 'roles') {
      return;
    }

    if (catalogPermissions.length > 0) {
      return;
    }

    let cancelled = false;
    setCatalogPermissionsLoading(true);

    void (async () => {
      try {
        const result = await getAllPermissions();
        if (!cancelled) {
          setCatalogPermissions(result);
        }
      } catch {
        if (!cancelled) {
          setCatalogPermissions([]);
        }
      } finally {
        if (!cancelled) {
          setCatalogPermissionsLoading(false);
        }
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [isPermissionCatalogOpen, isRoleInspectorOpen, activeTab]);

  useEffect(() => {
    if (activeTab !== 'roles') return;

    let cancelled = false;
    setRoleMatrixLoading(true);

    void (async () => {
      const results = await Promise.allSettled(KNOWN_ROLES.map((r) => getRole(r)));
      if (cancelled) return;
      const data: Record<string, string[]> = {};
      results.forEach((result, i) => {
        data[KNOWN_ROLES[i]] = result.status === 'fulfilled' ? result.value.permissions : [];
      });
      setRoleMatrixData(data);
      setRoleMatrixLoading(false);
    })();

    return () => {
      cancelled = true;
    };
  }, [activeTab]);

  // Load roles for all visible users when RBAC tab is active
  useEffect(() => {
    if (activeTab !== 'rbac' || users.length === 0) return;

    let cancelled = false;

    void (async () => {
      const results = await Promise.allSettled(
        users.map((u) => getUserRoles(u.id.toString())),
      );
      if (cancelled) return;
      const newCache: Record<string, string[]> = {};
      results.forEach((result, index) => {
        if (result.status === 'fulfilled') {
          newCache[users[index].id.toString()] = result.value.roles;
        }
      });
      setUserRolesCache((prev) => ({ ...prev, ...newCache }));
    })();

    return () => {
      cancelled = true;
    };
  }, [activeTab, users]);

  const filteredUsers = userSearchQuery
    ? users.filter(
        (u) =>
          u.email.toLowerCase().includes(userSearchQuery.toLowerCase()) ||
          u.fullName.toLowerCase().includes(userSearchQuery.toLowerCase()) ||
          u.username.toLowerCase().includes(userSearchQuery.toLowerCase()),
      )
    : users;

  return (
    <main className="auth-popup-shell items-center bg-canvas py-6 lg:py-8">
      <section className="auth-popup max-w-6xl p-4 sm:p-5 lg:p-6">
        <FormPage
          title={t('admin.title')}
          intro={t('admin.subtitle')}
          aria-labelledby="admin-title"
          titleId="admin-title"
          action={<LanguageSwitcher />}
        >
          <div className="flex flex-col gap-6">
            {/* Status Messages */}
            {error && (
              <div
                role="alert"
                className="flex items-start gap-3 p-4 bg-danger-50 border border-danger-200 rounded-md"
              >
                <AlertCircle size={20} className="text-danger-700 flex-shrink-0 mt-0.5" />
                <span id={errorId} className="text-danger-700 text-sm flex-1">
                  {error}
                </span>
                <button
                  onClick={() => setStatusMessage(null)}
                  className="text-danger-700 hover:text-danger-900 transition-colors flex-shrink-0"
                  aria-label={t('accessibility.closeError')}
                >
                  <X size={18} />
                </button>
              </div>
            )}

            {statusMessage && (
              <div className="flex items-start gap-3 p-4 bg-surface-subtle border border-border-subtle rounded-md">
                <span className="text-ink text-sm flex-1">{statusMessage}</span>
                <button
                  onClick={() => setStatusMessage(null)}
                  className="text-muted hover:text-ink transition-colors flex-shrink-0"
                  aria-label={t('accessibility.closeError')}
                >
                  <X size={18} />
                </button>
              </div>
            )}

            <div className="flex h-[min(72dvh,40rem)] flex-col rounded-2xl border border-border-subtle bg-panel p-4 shadow-sm">
              <div className="flex items-center justify-between gap-3">
                <p className="auth-progress-text">{t('admin.title')}</p>
                <button
                  type="button"
                  onClick={handlePrimaryAction}
                  aria-label={primaryActionLabel}
                  title={primaryActionLabel}
                  className="inline-flex h-9 w-9 items-center justify-center rounded-full bg-brand-500 text-white hover:bg-brand-600"
                >
                  <Plus size={16} />
                </button>
              </div>
              {/* Tab Navigation */}
              <div className="mt-4 flex border-b border-border-subtle -mx-4 px-4">
                <button
                  onClick={() => {
                    setActiveTab('users');
                    setSelectedUserForCerts(null);
                  }}
                  className={`px-4 py-3 font-medium text-sm border-b-2 transition-colors -mb-px ${
                    activeTab === 'users'
                      ? 'border-brand-500 text-ink'
                      : 'border-transparent text-muted hover:text-ink'
                  }`}
                >
                  {t('admin.userManagement')}
                </button>
                <button
                  onClick={() => setActiveTab('certificates')}
                  className={`px-4 py-3 font-medium text-sm border-b-2 transition-colors -mb-px ${
                    activeTab === 'certificates'
                      ? 'border-brand-500 text-ink'
                      : 'border-transparent text-muted hover:text-ink'
                  }`}
                >
                  {t('admin.certificateManagement')}
                </button>
                <button
                  onClick={() => setActiveTab('rbac')}
                  className={`px-4 py-3 font-medium text-sm border-b-2 transition-colors -mb-px ${
                    activeTab === 'rbac'
                      ? 'border-brand-500 text-ink'
                      : 'border-transparent text-muted hover:text-ink'
                  }`}
                >
                  {t('admin.roleManagement')}
                </button>
                <button
                  onClick={() => {
                    setActiveTab('roles');
                    setSelectedRoleForMatrix(null);
                    setMatrixPermissionToAdd('');
                  }}
                  className={`px-4 py-3 font-medium text-sm border-b-2 transition-colors -mb-px ${
                    activeTab === 'roles'
                      ? 'border-brand-500 text-ink'
                      : 'border-transparent text-muted hover:text-ink'
                  }`}
                >
                  {t('admin.rolesSection')}
                </button>
                <button
                  onClick={() => setActiveTab('permissions')}
                  className={`px-4 py-3 font-medium text-sm border-b-2 transition-colors -mb-px ${
                    activeTab === 'permissions'
                      ? 'border-brand-500 text-ink'
                      : 'border-transparent text-muted hover:text-ink'
                  }`}
                >
                  {t('admin.permissionsSection')}
                </button>
              </div>

              {/* Tab Content */}
              <div className="min-h-0 flex-1 pt-4">
                {/* Users Tab */}
                {activeTab === 'users' && (
                  <div className="flex h-full min-h-0 flex-col gap-4">
                    <div className="flex items-center gap-2 justify-between">
                      <input
                        type="search"
                        placeholder={t('admin.searchUsers')}
                        value={userSearchQuery}
                        onChange={(e) => setUserSearchQuery(e.target.value)}
                        className="min-w-0 flex-1 rounded-md border border-border-subtle bg-panel px-3 py-2 text-sm outline-none focus:border-brand-500"
                      />
                      <button
                        type="button"
                        onClick={() => setIsUserToolsOpen(true)}
                        className="rounded-md bg-surface-subtle px-3 py-2 text-xs font-medium text-ink hover:bg-surface-hover"
                      >
                        {t('admin.userTools')}
                      </button>
                    </div>

                    {usersLoading ? (
                      <div className="text-center py-8 text-muted-soft">{t('admin.loading')}</div>
                    ) : (
                      <div className="min-h-0 flex-1 overflow-y-auto overflow-x-auto rounded-md border border-border-subtle bg-panel">
                        <table className="w-full text-sm">
                          <thead className="bg-surface-subtle border-b border-border-subtle">
                            <tr>
                              <th className="px-4 py-3 text-left font-semibold text-ink">
                                {t('admin.email')}
                              </th>
                              <th className="px-4 py-3 text-left font-semibold text-ink">
                                {t('admin.username')}
                              </th>
                              <th className="px-4 py-3 text-left font-semibold text-ink">
                                {t('admin.fullName')}
                              </th>
                              <th className="px-4 py-3 text-left font-semibold text-ink">
                                {t('admin.userType')}
                              </th>
                              <th className="px-4 py-3 text-left font-semibold text-ink">
                                {t('admin.accessLevel')}
                              </th>
                              <th className="px-4 py-3 text-left font-semibold text-ink">
                                {t('admin.actions')}
                              </th>
                            </tr>
                          </thead>
                          <tbody className="divide-y divide-border-subtle">
                            {filteredUsers.length === 0 ? (
                              <tr>
                                <td colSpan={6} className="px-4 py-8 text-center text-muted-soft">
                                  {t('admin.noUsers')}
                                </td>
                              </tr>
                            ) : (
                              filteredUsers.map((userData) => (
                                <tr
                                  key={userData.id}
                                  className="hover:bg-surface-subtle transition-colors"
                                >
                                  <td className="px-4 py-3 text-ink">{userData.email}</td>
                                  <td className="px-4 py-3 text-muted font-mono text-xs">{userData.username}</td>
                                  <td className="px-4 py-3 text-ink">{userData.fullName}</td>
                                  <td className="px-4 py-3 text-muted">{userData.userType}</td>
                                  <td className="px-4 py-3">
                                    <span
                                      className={`inline-block px-3 py-1 text-xs font-semibold rounded-full ${
                                        userData.accessLevel === 'SUPERADMIN'
                                          ? 'bg-danger-50 text-danger-700'
                                          : userData.accessLevel === 'ADMIN'
                                            ? 'bg-warning-surface text-warning-ink'
                                            : userData.accessLevel === 'GOLD'
                                              ? 'bg-brand-50 text-brand-700'
                                              : 'bg-surface-subtle text-muted'
                                      }`}
                                    >
                                      {userData.accessLevel}
                                    </span>
                                  </td>
                                  <td className="px-4 py-3">
                                    <div className="flex gap-2">
                                      <button
                                        onClick={() => handleEditUser(userData)}
                                        title={t('admin.editUser')}
                                        disabled={isOperating}
                                        className="p-2 hover:bg-surface-hover rounded text-muted hover:text-ink disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                                      >
                                        <Edit2 size={16} />
                                      </button>
                                      <button
                                        onClick={() => handleDeleteUser(userData.id.toString())}
                                        title={t('admin.deleteUser')}
                                        disabled={isOperating}
                                        className="p-2 hover:bg-surface-hover rounded text-muted hover:text-danger-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                                      >
                                        <Trash2 size={16} />
                                      </button>
                                    </div>
                                  </td>
                                </tr>
                              ))
                            )}
                          </tbody>
                        </table>

                        {/* Pagination */}
                        {usersPagination && usersPagination.totalPages > 1 && (
                          <div className="flex items-center justify-center gap-4 px-4 py-4 border-t border-border-subtle">
                            <button
                              onClick={() => loadUsers(usersPage - 1)}
                              disabled={usersPage === 0}
                              className="p-2 hover:bg-surface-hover rounded disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                            >
                              <ChevronLeft size={18} />
                            </button>
                            <span className="text-sm text-muted">
                              {t('admin.page')} {usersPage + 1} / {usersPagination.totalPages}
                            </span>
                            <button
                              onClick={() => loadUsers(usersPage + 1)}
                              disabled={usersPage >= usersPagination.totalPages - 1}
                              className="p-2 hover:bg-surface-hover rounded disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                            >
                              <ChevronRight size={18} />
                            </button>
                          </div>
                        )}
                      </div>
                    )}

                    {/* Edit User Modal */}
                    {editingUserId && editingUserData && (
                      <div className="admin-modal-overlay" onClick={() => setEditingUserId(null)}>
                        <div className="admin-modal" onClick={(e) => e.stopPropagation()}>
                          <div className="admin-modal-header">
                            <h3>{t('admin.editUser')}</h3>
                            <button
                              className="admin-modal-close"
                              onClick={() => setEditingUserId(null)}
                            >
                              <X size={20} />
                            </button>
                          </div>

                          <form onSubmit={handleSaveUser} className="admin-form">
                            <div className="admin-form-group">
                              <label>{t('admin.email')}</label>
                              <input
                                type="email"
                                value={editingUserEmail}
                                disabled
                                className="admin-input"
                              />
                            </div>

                            <div className="admin-form-group">
                              <label>{t('admin.username')}</label>
                              <input
                                type="text"
                                value={editingUserData.username}
                                onChange={(e) =>
                                  setEditingUserData({
                                    ...editingUserData,
                                    username: e.target.value,
                                  })
                                }
                                className="admin-input"
                              />
                            </div>

                            <div className="admin-form-group">
                              <label>{t('admin.fullName')}</label>
                              <input
                                type="text"
                                value={editingUserData.fullName}
                                onChange={(e) =>
                                  setEditingUserData({
                                    ...editingUserData,
                                    fullName: e.target.value,
                                  })
                                }
                                className="admin-input"
                              />
                            </div>

                            <div className="admin-form-group">
                              <label>{t('admin.phoneNumber')}</label>
                              <input
                                type="tel"
                                value={editingUserData.phoneNumber || ''}
                                onChange={(e) =>
                                  setEditingUserData({
                                    ...editingUserData,
                                    phoneNumber: e.target.value,
                                  })
                                }
                                className="admin-input"
                              />
                            </div>

                            <div className="admin-form-group">
                              <label>{t('admin.userType')}</label>
                              <select
                                value={editingUserData.userType}
                                onChange={(e) =>
                                  setEditingUserData({
                                    ...editingUserData,
                                    userType: e.target.value as 'INDIVIDUAL' | 'LIBRARY',
                                  })
                                }
                                className="admin-input"
                              >
                                <option value="INDIVIDUAL">INDIVIDUAL</option>
                                <option value="LIBRARY">LIBRARY</option>
                              </select>
                            </div>

                            <div className="admin-form-group">
                              <label>{t('admin.accessLevel')}</label>
                              <select
                                value={editingUserData.accessLevel}
                                onChange={(e) =>
                                  setEditingUserData({
                                    ...editingUserData,
                                    accessLevel: e.target.value as
                                      | 'SUPERADMIN'
                                      | 'ADMIN'
                                      | 'GOLD'
                                      | 'PREMIUM'
                                      | 'NORMAL',
                                  })
                                }
                                className="admin-input"
                              >
                                <option value="NORMAL">NORMAL</option>
                                <option value="PREMIUM">PREMIUM</option>
                                <option value="GOLD">GOLD</option>
                                <option value="ADMIN">ADMIN</option>
                                <option value="SUPERADMIN">SUPERADMIN</option>
                              </select>
                            </div>

                            <div className="admin-form-actions">
                              <button
                                type="submit"
                                className="admin-btn-primary"
                                disabled={isOperating}
                              >
                                {t('admin.save')}
                              </button>
                              <button
                                type="button"
                                className="admin-btn-secondary"
                                onClick={() => setEditingUserId(null)}
                                disabled={isOperating}
                              >
                                {t('admin.cancel')}
                              </button>
                            </div>
                          </form>
                        </div>
                      </div>
                    )}
                  </div>
                )}

                {/* Certificates Tab */}
                {activeTab === 'certificates' && (
                  <div className="flex h-full min-h-0 flex-col gap-4">
                    <div className="rounded-md border border-border-subtle bg-surface-subtle p-4">
                      <p className="text-sm font-semibold text-ink">{t('admin.filterCredentials')}</p>
                      <div className="mt-3 flex gap-2">
                        <input
                          type="text"
                          placeholder={t('admin.filterByUserId')}
                          value={selectedUserForCerts || ''}
                          onChange={(e) => {
                            setSelectedUserForCerts(e.target.value || null);
                            loadCredentials(0, e.target.value || undefined);
                          }}
                          className="min-w-0 flex-1 rounded-md border border-border-subtle bg-panel px-3 py-2 text-sm outline-none focus:border-brand-500"
                        />
                      </div>
                    </div>

                    {credentialsLoading ? (
                      <div className="text-center py-8 text-muted-soft">{t('admin.loading')}</div>
                    ) : (
                      <div className="min-h-0 flex-1 overflow-y-auto overflow-x-auto rounded-md border border-border-subtle bg-panel">
                        <table className="w-full text-sm">
                          <thead className="bg-surface-subtle border-b border-border-subtle">
                            <tr>
                              <th className="px-4 py-3 text-left font-semibold text-ink">
                                {t('admin.userId')}
                              </th>
                              <th className="px-4 py-3 text-left font-semibold text-ink">
                                {t('admin.subjectDn')}
                              </th>
                              <th className="px-4 py-3 text-left font-semibold text-ink">
                                {t('admin.status')}
                              </th>
                              <th className="px-4 py-3 text-left font-semibold text-ink">
                                {t('admin.notAfter')}
                              </th>
                              <th className="px-4 py-3 text-left font-semibold text-ink">
                                {t('admin.actions')}
                              </th>
                            </tr>
                          </thead>
                          <tbody className="divide-y divide-border-subtle">
                            {credentials.length === 0 ? (
                              <tr>
                                <td colSpan={5} className="px-4 py-8 text-center text-muted-soft">
                                  {t('admin.noCertificates')}
                                </td>
                              </tr>
                            ) : (
                              credentials.map((cert) => (
                                <tr
                                  key={cert.id}
                                  className="hover:bg-surface-subtle transition-colors"
                                >
                                  <td className="px-4 py-3 text-ink font-mono text-xs">
                                    {cert.userId.slice(0, 8)}...
                                  </td>
                                  <td className="px-4 py-3 text-ink max-w-xs truncate text-xs">
                                    {cert.subjectDn}
                                  </td>
                                  <td className="px-4 py-3">
                                    <span
                                      className={`inline-block px-3 py-1 text-xs font-semibold rounded-full ${
                                        cert.revocationStatus === 'REVOKED'
                                          ? 'bg-danger-50 text-danger-700'
                                          : 'bg-surface-subtle text-ink'
                                      }`}
                                    >
                                      {cert.revocationStatus}
                                    </span>
                                  </td>
                                  <td className="px-4 py-3 text-muted text-sm">
                                    {new Date(cert.notAfter).toLocaleDateString()}
                                  </td>
                                  <td className="px-4 py-3">
                                    <button
                                      onClick={() => setSelectedCredentialId(cert.id)}
                                      disabled={isOperating || cert.revocationStatus === 'REVOKED'}
                                      className="px-3 py-2 text-xs font-medium bg-surface-subtle hover:bg-surface-hover text-ink rounded disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                                    >
                                      {t('admin.revoke')}
                                    </button>
                                  </td>
                                </tr>
                              ))
                            )}
                          </tbody>
                        </table>
                      </div>
                    )}

                    {/* Revoke Modal */}
                    {selectedCredentialId && (
                      <div
                        className="admin-modal-overlay"
                        onClick={() => setSelectedCredentialId(null)}
                      >
                        <div className="admin-modal" onClick={(e) => e.stopPropagation()}>
                          <div className="admin-modal-header">
                            <h3>{t('admin.revokeCertificate')}</h3>
                            <button
                              className="admin-modal-close"
                              onClick={() => setSelectedCredentialId(null)}
                            >
                              <X size={20} />
                            </button>
                          </div>

                          <div className="admin-form">
                            <div className="admin-form-group">
                              <label>{t('admin.revocationReason')}</label>
                              <textarea
                                value={revokeReason}
                                onChange={(e) => setRevokeReason(e.target.value)}
                                placeholder={t('admin.enterReason')}
                                className="admin-textarea"
                                rows={4}
                              />
                            </div>

                            <div className="admin-form-actions">
                              <button
                                type="button"
                                className="admin-btn-primary"
                                onClick={() => handleRevokeCertificate(selectedCredentialId)}
                                disabled={isOperating}
                              >
                                {t('admin.confirmRevoke')}
                              </button>
                              <button
                                type="button"
                                className="admin-btn-secondary"
                                onClick={() => setSelectedCredentialId(null)}
                                disabled={isOperating}
                              >
                                {t('admin.cancel')}
                              </button>
                            </div>
                          </div>
                        </div>
                      </div>
                    )}
                  </div>
                )}

                {/* RBAC Tab */}
                {activeTab === 'rbac' && (
                  <div className="flex h-full min-h-0 flex-col gap-4 overflow-y-auto">
                    {/* Role permissions panel */}
                    <div className="rounded-md border border-border-subtle bg-surface-subtle p-3 shrink-0">
                      <p className="text-xs font-semibold text-ink mb-2">{t('admin.rolePermissions')}</p>
                      <div className="flex gap-2 items-center">
                        <input
                          value={roleInspectorValue}
                          onChange={(e) => setRoleInspectorValue(e.target.value)}
                          onKeyDown={(e) => { if (e.key === 'Enter') void handleInspectRole(); }}
                          placeholder={t('admin.roleNamePlaceholder')}
                          className="min-w-0 flex-1 rounded-md border border-border-subtle bg-panel px-3 py-1.5 text-sm outline-none focus:border-brand-500"
                        />
                        <button
                          type="button"
                          onClick={() => void handleInspectRole()}
                          disabled={isOperating || !roleInspectorValue.trim()}
                          className="rounded-md bg-brand-500 px-3 py-1.5 text-xs font-medium text-white hover:bg-brand-600 disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                          {t('admin.inspect')}
                        </button>
                        {inspectedRole && (
                          <button
                            type="button"
                            onClick={() => setInspectedRole(null)}
                            className="rounded-md bg-surface-subtle px-2 py-1.5 text-xs text-muted hover:text-ink"
                          >
                            <X size={14} />
                          </button>
                        )}
                      </div>

                      {inspectedRole && (
                        <div className="mt-3 flex flex-col gap-2">
                          <div className="flex flex-wrap gap-1.5">
                            {inspectedRole.permissions.length === 0 ? (
                              <span className="text-xs text-muted">{t('admin.noPermissionsAssigned')}</span>
                            ) : (
                              inspectedRole.permissions.map((perm) => (
                                <span
                                  key={perm}
                                  className="inline-flex items-center gap-1 rounded-full bg-brand-50 px-2.5 py-0.5 text-xs font-medium text-brand-700"
                                >
                                  {perm}
                                  <button
                                    type="button"
                                    title={`Remove ${perm}`}
                                    disabled={isOperating}
                                    onClick={() => void handleRemovePermissionFromRole(inspectedRole.roleName, perm)}
                                    className="ml-0.5 rounded-full hover:bg-red-100 hover:text-red-600 disabled:opacity-50"
                                  >
                                    <X size={10} />
                                  </button>
                                </span>
                              ))
                            )}
                          </div>
                          <div className="flex gap-2 items-center">
                            <select
                              value={selectedPermissionToAdd}
                              onChange={(e) => setSelectedPermissionToAdd(e.target.value)}
                              disabled={isOperating || catalogPermissionsLoading}
                              className="min-w-0 flex-1 rounded-md border border-border-subtle bg-panel px-2 py-1.5 text-xs outline-none focus:border-brand-500"
                            >
                              <option value="">
                                {catalogPermissionsLoading ? t('admin.loadingPermissions') : t('admin.addPermissionPlaceholder')}
                              </option>
                              {catalogPermissions
                                .filter((p) => !inspectedRole.permissions.includes(p.code))
                                .map((p) => (
                                  <option key={p.code} value={p.code}>
                                    {p.code}{p.description ? ` — ${p.description}` : ''}
                                  </option>
                                ))}
                            </select>
                            <button
                              type="button"
                              disabled={isOperating || !selectedPermissionToAdd}
                              onClick={() => void handleAssignPermissionToRole(inspectedRole.roleName, selectedPermissionToAdd)}
                              className="rounded-md bg-brand-500 px-3 py-1.5 text-xs font-medium text-white hover:bg-brand-600 disabled:opacity-50 disabled:cursor-not-allowed"
                            >
                              {t('admin.addPermission')}
                            </button>
                          </div>
                        </div>
                      )}
                    </div>

                    {/* Users / roles table */}
                    {usersLoading ? (
                      <div className="text-center py-8 text-muted-soft">{t('admin.loading')}</div>
                    ) : (
                      <div className="min-h-0 flex-1 overflow-y-auto overflow-x-auto rounded-md border border-border-subtle bg-panel">
                        <table className="w-full text-sm">
                          <thead className="bg-surface-subtle border-b border-border-subtle">
                            <tr>
                              <th className="px-4 py-3 text-left font-semibold text-ink">
                                {t('admin.email')}
                              </th>
                              <th className="px-4 py-3 text-left font-semibold text-ink">
                                {t('admin.fullName')}
                              </th>
                              <th className="px-4 py-3 text-left font-semibold text-ink">
                                {t('admin.roles')}
                              </th>
                              <th className="px-4 py-3 text-left font-semibold text-ink">
                                {t('admin.actions')}
                              </th>
                            </tr>
                          </thead>
                          <tbody className="divide-y divide-border-subtle">
                            {users.length === 0 ? (
                              <tr>
                                <td colSpan={4} className="px-4 py-8 text-center text-muted-soft">
                                  {t('admin.noUsers')}
                                </td>
                              </tr>
                            ) : (
                              users.map((userData) => (
                                <tr
                                  key={userData.id}
                                  className="hover:bg-surface-subtle transition-colors"
                                >
                                  <td className="px-4 py-3 text-ink">{userData.email}</td>
                                  <td className="px-4 py-3 text-ink">{userData.fullName}</td>
                                  <td className="px-4 py-3">
                                    <div className="flex flex-wrap gap-1">
                                      {userRolesCache[userData.id.toString()] ? (
                                        userRolesCache[userData.id.toString()].length === 0 ? (
                                          <span className="text-xs text-muted">—</span>
                                        ) : (
                                          userRolesCache[userData.id.toString()].map((role) => (
                                            <span
                                              key={role}
                                              className="inline-block rounded-full bg-surface-subtle px-2 py-0.5 text-xs font-medium text-ink"
                                            >
                                              {role}
                                            </span>
                                          ))
                                        )
                                      ) : (
                                        <span className="text-xs text-muted">…</span>
                                      )}
                                    </div>
                                  </td>
                                  <td className="px-4 py-3">
                                    <button
                                      onClick={() =>
                                        setUserRoleManagementId(userData.id.toString())
                                      }
                                      disabled={isOperating}
                                      className="px-3 py-2 text-xs font-medium bg-surface-subtle hover:bg-surface-hover text-ink rounded disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                                    >
                                      {t('admin.manage')}
                                    </button>
                                  </td>
                                </tr>
                              ))
                            )}
                          </tbody>
                        </table>
                      </div>
                    )}

                    {/* Role Management Modal */}
                    {userRoleManagementId && (
                      <RoleManagementModal
                        userId={userRoleManagementId}
                        onClose={() => setUserRoleManagementId(null)}
                        onAssignRole={handleAssignRole}
                        onRolesChanged={(uid, roles) =>
                          setUserRolesCache((prev) => ({ ...prev, [uid]: roles }))
                        }
                        isOperating={isOperating}
                        t={t}
                      />
                    )}
                  </div>
                )}

                {/* Roles Tab */}
                {activeTab === 'roles' && (
                  <div className="flex h-full min-h-0 gap-3">
                    {/* Left panel — role list */}
                    <div className="flex w-44 shrink-0 flex-col gap-1 overflow-y-auto">
                      <p className="mb-1 px-1 text-xs font-semibold uppercase tracking-wide text-muted">
                        {t('admin.rolesSection')}
                      </p>
                      {KNOWN_ROLES.map((role) => {
                        const count = roleMatrixData[role]?.length ?? null;
                        const isSelected = selectedRoleForMatrix === role;
                        return (
                          <button
                            key={role}
                            type="button"
                            onClick={() => {
                              setSelectedRoleForMatrix(role);
                              setMatrixPermissionToAdd('');
                            }}
                            className={`flex items-center justify-between rounded-md px-3 py-2.5 text-left text-sm font-medium transition-colors ${
                              isSelected
                                ? 'bg-brand-500 text-white'
                                : 'bg-surface-subtle text-ink hover:bg-surface-hover'
                            }`}
                          >
                            <span>{role}</span>
                            {count !== null && (
                              <span
                                className={`ml-2 rounded-full px-1.5 py-0.5 text-xs font-semibold tabular-nums ${
                                  isSelected
                                    ? 'bg-white/20 text-white'
                                    : 'bg-panel text-muted'
                                }`}
                              >
                                {roleMatrixLoading ? '…' : count}
                              </span>
                            )}
                          </button>
                        );
                      })}
                    </div>

                    {/* Right panel — permissions for selected role */}
                    <div className="flex min-w-0 flex-1 flex-col gap-3 overflow-y-auto rounded-md border border-border-subtle bg-panel p-4">
                      {roleMatrixLoading ? (
                        <div className="flex h-full items-center justify-center text-sm text-muted-soft">
                          {t('admin.loadingRoles')}
                        </div>
                      ) : !selectedRoleForMatrix ? (
                        <div className="flex h-full flex-col items-center justify-center gap-2 text-center">
                          <p className="text-sm font-medium text-ink">{t('admin.selectRole')}</p>
                          <p className="text-xs text-muted">
                            {t('admin.selectRoleHint')}
                          </p>
                        </div>
                      ) : (
                        <>
                          <div className="flex items-center justify-between border-b border-border-subtle pb-3">
                            <div>
                              <h3 className="text-sm font-semibold text-ink">
                                {selectedRoleForMatrix}
                              </h3>
                              <p className="text-xs text-muted">
                                {t('admin.permissionsAssigned', { count: roleMatrixData[selectedRoleForMatrix]?.length ?? 0 })}
                              </p>
                            </div>
                          </div>

                          {/* Assigned permissions */}
                          <div className="flex flex-wrap gap-1.5 rounded-md border border-border-subtle bg-surface-subtle p-3 min-h-[4rem]">
                            {(roleMatrixData[selectedRoleForMatrix]?.length ?? 0) === 0 ? (
                              <span className="text-xs text-muted">
                                {t('admin.noPermissionsAssigned')}
                              </span>
                            ) : (
                              roleMatrixData[selectedRoleForMatrix].map((perm) => (
                                <span
                                  key={perm}
                                  className="inline-flex items-center gap-1 rounded-full bg-brand-50 px-2.5 py-1 text-xs font-medium text-brand-700"
                                >
                                  {perm}
                                  <button
                                    type="button"
                                    disabled={isOperating}
                                    onClick={() =>
                                      void handleMatrixRemovePermission(
                                        selectedRoleForMatrix,
                                        perm,
                                      )
                                    }
                                    className="ml-0.5 rounded-full p-0.5 hover:bg-red-100 hover:text-red-600 disabled:opacity-50 transition-colors"
                                    title={`Remove ${perm}`}
                                  >
                                    <X size={10} />
                                  </button>
                                </span>
                              ))
                            )}
                          </div>

                          {/* Add permission */}
                          <div className="flex shrink-0 gap-2 pt-1">
                            <select
                              value={matrixPermissionToAdd}
                              onChange={(e) => setMatrixPermissionToAdd(e.target.value)}
                              disabled={isOperating || catalogPermissionsLoading}
                              className="min-w-0 flex-1 rounded-md border border-border-subtle bg-panel px-3 py-2 text-sm outline-none focus:border-brand-500 disabled:opacity-50"
                            >
                              <option value="">
                                {catalogPermissionsLoading ? t('admin.loadingPermissions') : t('admin.addPermissionPlaceholder')}
                              </option>
                              {catalogPermissions
                                .filter(
                                  (p) =>
                                    !(
                                      roleMatrixData[selectedRoleForMatrix] ?? []
                                    ).includes(p.code),
                                )
                                .map((p) => (
                                  <option key={p.code} value={p.code}>
                                    {p.code}
                                    {p.description ? ` — ${p.description}` : ''}
                                  </option>
                                ))}
                            </select>
                            <button
                              type="button"
                              disabled={isOperating || !matrixPermissionToAdd}
                              onClick={() =>
                                void handleMatrixAssignPermission(
                                  selectedRoleForMatrix,
                                  matrixPermissionToAdd,
                                )
                              }
                              className="shrink-0 rounded-md bg-brand-500 px-4 py-2 text-sm font-medium text-white hover:bg-brand-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                            >
                              {t('admin.addPermission')}
                            </button>
                          </div>
                        </>
                      )}
                    </div>
                  </div>
                )}

                {activeTab === 'permissions' && (
                  <div className="flex h-full min-h-0 flex-col gap-4">
                    <div className="flex justify-end">
                      <button
                        type="button"
                        onClick={() => setIsPermissionCatalogOpen(true)}
                        className="rounded-md bg-surface-subtle px-3 py-2 text-xs font-medium text-ink hover:bg-surface-hover"
                      >
                        {t('admin.viewCatalog')}
                      </button>
                    </div>

                    {permissionsLoading ? (
                      <div className="text-center py-8 text-muted-soft">{t('admin.loading')}</div>
                    ) : (
                      <div className="min-h-0 flex-1 overflow-y-auto overflow-x-auto rounded-md border border-border-subtle bg-panel">
                        <table className="w-full text-sm">
                          <thead className="bg-surface-subtle border-b border-border-subtle">
                            <tr>
                              <th className="px-4 py-3 text-left font-semibold text-ink">{t('admin.permissionCode')}</th>
                              <th className="px-4 py-3 text-left font-semibold text-ink">
                                {t('admin.permissionDescription')}
                              </th>
                              <th className="px-4 py-3 text-left font-semibold text-ink">
                                {t('admin.actions')}
                              </th>
                            </tr>
                          </thead>
                          <tbody className="divide-y divide-border-subtle">
                            {permissions.length === 0 ? (
                              <tr>
                                <td colSpan={3} className="px-4 py-8 text-center text-muted-soft">
                                  {t('admin.noPermissionsAvailable')}
                                </td>
                              </tr>
                            ) : (
                              permissions.map((permission) => (
                                <tr
                                  key={permission.code}
                                  className="hover:bg-surface-subtle transition-colors"
                                >
                                  <td className="px-4 py-3 text-ink font-medium">
                                    {permission.code}
                                  </td>
                                  <td className="px-4 py-3 text-muted">
                                    {permission.description || '—'}
                                  </td>
                                  <td className="px-4 py-3">
                                    <div className="flex gap-2">
                                      <button
                                        type="button"
                                        onClick={() => openPermissionEditor('edit', permission)}
                                        className="rounded-md bg-surface-subtle px-3 py-2 text-xs font-medium text-ink hover:bg-surface-hover"
                                      >
                                        {t('admin.edit')}
                                      </button>
                                      <button
                                        type="button"
                                        onClick={() => handleDeletePermission(permission.code)}
                                        className="rounded-md bg-danger-50 px-3 py-2 text-xs font-medium text-danger-700 hover:bg-danger-200"
                                      >
                                        {t('admin.delete')}
                                      </button>
                                    </div>
                                  </td>
                                </tr>
                              ))
                            )}
                          </tbody>
                        </table>
                      </div>
                    )}

                    {permissionsPagination && permissionsPagination.totalPages > 1 && (
                      <div className="flex items-center justify-center gap-4 px-4 py-4 border-t border-border-subtle">
                        <button
                          onClick={() => loadPermissions(permissionsPage - 1)}
                          disabled={permissionsPage === 0}
                          className="p-2 hover:bg-surface-hover rounded disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                        >
                          <ChevronLeft size={18} />
                        </button>
                        <span className="text-sm text-muted">
                          {t('admin.page')} {permissionsPage + 1} /{' '}
                          {permissionsPagination.totalPages}
                        </span>
                        <button
                          onClick={() => loadPermissions(permissionsPage + 1)}
                          disabled={permissionsPage >= permissionsPagination.totalPages - 1}
                          className="p-2 hover:bg-surface-hover rounded disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                        >
                          <ChevronRight size={18} />
                        </button>
                      </div>
                    )}

                    {permissionModalMode && (
                      <div
                        className="admin-modal-overlay"
                        onClick={() => setPermissionModalMode(null)}
                      >
                        <div className="admin-modal" onClick={(event) => event.stopPropagation()}>
                          <div className="admin-modal-header">
                            <h3>
                              {permissionModalMode === 'edit'
                                ? t('admin.editPermission')
                                : t('admin.createPermission')}
                            </h3>
                            <button
                              className="admin-modal-close"
                              onClick={() => setPermissionModalMode(null)}
                            >
                              <X size={20} />
                            </button>
                          </div>

                          <form className="admin-form" onSubmit={handleSavePermission}>
                            <div className="admin-form-group">
                              <label>{t('admin.permissionCode')}</label>
                              <input
                                value={permissionForm.code}
                                onChange={(event) =>
                                  setPermissionForm((current) => ({
                                    ...current,
                                    code: event.target.value,
                                  }))
                                }
                                disabled={permissionModalMode === 'edit'}
                                className="admin-input"
                              />
                            </div>
                            <div className="admin-form-group">
                              <label>{t('admin.permissionDescription')}</label>
                              <input
                                value={permissionForm.description}
                                onChange={(event) =>
                                  setPermissionForm((current) => ({
                                    ...current,
                                    description: event.target.value,
                                  }))
                                }
                                className="admin-input"
                              />
                            </div>
                            <div className="admin-form-actions">
                              <button
                                type="submit"
                                className="admin-btn-primary"
                                disabled={isOperating}
                              >
                                {t('admin.save')}
                              </button>
                              <button
                                type="button"
                                className="admin-btn-secondary"
                                onClick={() => setPermissionModalMode(null)}
                                disabled={isOperating}
                              >
                                {t('admin.cancel')}
                              </button>
                            </div>
                          </form>
                        </div>
                      </div>
                    )}
                  </div>
                )}

                {isCreateUserOpen && (
                  <div className="admin-modal-overlay" onClick={() => setIsCreateUserOpen(false)}>
                    <div className="admin-modal" onClick={(event) => event.stopPropagation()}>
                      <div className="admin-modal-header">
                        <h3>{t('admin.createUser')}</h3>
                        <button
                          className="admin-modal-close"
                          onClick={() => setIsCreateUserOpen(false)}
                        >
                          <X size={20} />
                        </button>
                      </div>

                      <form onSubmit={handleCreateUser} className="admin-form">
                        <div className="admin-form-group">
                          <label>{t('admin.username')}</label>
                          <input
                            value={createUserForm.username}
                            onChange={(event) =>
                              setCreateUserForm((current) => ({
                                ...current,
                                username: event.target.value,
                              }))
                            }
                            className="admin-input"
                          />
                        </div>
                        <div className="admin-form-group">
                          <label>{t('admin.fullName')}</label>
                          <input
                            value={createUserForm.fullName}
                            onChange={(event) =>
                              setCreateUserForm((current) => ({
                                ...current,
                                fullName: event.target.value,
                              }))
                            }
                            className="admin-input"
                          />
                        </div>
                        <div className="admin-form-group">
                          <label>{t('admin.email')}</label>
                          <input
                            type="email"
                            value={createUserForm.email}
                            onChange={(event) =>
                              setCreateUserForm((current) => ({
                                ...current,
                                email: event.target.value,
                              }))
                            }
                            className="admin-input"
                          />
                        </div>
                        <div className="admin-form-group">
                          <label>{t('admin.password')}</label>
                          <input
                            type="password"
                            value={createUserForm.password}
                            onChange={(event) =>
                              setCreateUserForm((current) => ({
                                ...current,
                                password: event.target.value,
                              }))
                            }
                            className="admin-input"
                          />
                        </div>
                        <div className="admin-form-group">
                          <label>{t('admin.userType')}</label>
                          <select
                            value={createUserForm.userType}
                            onChange={(event) =>
                              setCreateUserForm((current) => ({
                                ...current,
                                userType: event.target.value as 'INDIVIDUAL' | 'LIBRARY',
                              }))
                            }
                            className="admin-input"
                          >
                            <option value="INDIVIDUAL">INDIVIDUAL</option>
                            <option value="LIBRARY">LIBRARY</option>
                          </select>
                        </div>
                        <div className="admin-form-group">
                          <label>{t('admin.defaultLanguage')}</label>
                          <select
                            value={createUserForm.defaultLanguage}
                            onChange={(event) =>
                              setCreateUserForm((current) => ({
                                ...current,
                                defaultLanguage: event.target.value,
                              }))
                            }
                            className="admin-input"
                          >
                            <option value="en">en</option>
                            <option value="es">es</option>
                            <option value="ca">ca</option>
                            <option value="gl">gl</option>
                            <option value="eu">eu</option>
                          </select>
                        </div>
                        <div className="admin-form-group">
                          <label>{t('admin.phoneNumber')}</label>
                          <input
                            value={createUserForm.phoneNumber || ''}
                            onChange={(event) =>
                              setCreateUserForm((current) => ({
                                ...current,
                                phoneNumber: event.target.value,
                              }))
                            }
                            className="admin-input"
                          />
                        </div>
                        <div className="admin-form-group">
                          <label>{t('admin.accessLevel')}</label>
                          <select
                            value={createUserForm.accessLevel}
                            onChange={(event) =>
                              setCreateUserForm((current) => ({
                                ...current,
                                accessLevel: event.target.value as
                                  | 'SUPERADMIN'
                                  | 'ADMIN'
                                  | 'GOLD'
                                  | 'PREMIUM'
                                  | 'NORMAL',
                              }))
                            }
                            className="admin-input"
                          >
                            <option value="NORMAL">NORMAL</option>
                            <option value="PREMIUM">PREMIUM</option>
                            <option value="GOLD">GOLD</option>
                            <option value="ADMIN">ADMIN</option>
                            <option value="SUPERADMIN">SUPERADMIN</option>
                          </select>
                        </div>
                        <div className="admin-form-actions">
                          <button
                            type="submit"
                            className="admin-btn-primary"
                            disabled={isOperating}
                          >
                            {t('admin.save')}
                          </button>
                          <button
                            type="button"
                            className="admin-btn-secondary"
                            onClick={() => setIsCreateUserOpen(false)}
                            disabled={isOperating}
                          >
                            {t('admin.cancel')}
                          </button>
                        </div>
                      </form>
                    </div>
                  </div>
                )}

                {isUserToolsOpen && (
                  <div className="admin-modal-overlay" onClick={() => setIsUserToolsOpen(false)}>
                    <div className="admin-modal" onClick={(event) => event.stopPropagation()}>
                      <div className="admin-modal-header">
                        <h3>{t('admin.userTools')}</h3>
                        <button
                          className="admin-modal-close"
                          onClick={() => setIsUserToolsOpen(false)}
                        >
                          <X size={20} />
                        </button>
                      </div>

                      <div className="admin-form">
                        <div className="admin-form-group">
                          <label>{t('admin.userIdToRestore')}</label>
                          <div className="admin-role-assign">
                            <input
                              value={restoreUserId}
                              onChange={(event) => setRestoreUserId(event.target.value)}
                              className="admin-input"
                            />
                            <button
                              type="button"
                              className="admin-btn-primary"
                              onClick={() => void handleRestoreUser()}
                              disabled={isOperating}
                            >
                              {t('admin.restore')}
                            </button>
                          </div>
                        </div>
                        <div className="admin-form-group">
                          <label>{t('admin.userIdToDelete')}</label>
                          <div className="admin-role-assign">
                            <input
                              value={permanentDeleteUserId}
                              onChange={(event) => setPermanentDeleteUserId(event.target.value)}
                              className="admin-input"
                            />
                            <button
                              type="button"
                              className="admin-btn-primary"
                              onClick={() => void handlePermanentDeleteUser()}
                              disabled={isOperating}
                            >
                              {t('admin.delete')}
                            </button>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                )}

                {isBindCredentialOpen && (
                  <div
                    className="admin-modal-overlay"
                    onClick={() => setIsBindCredentialOpen(false)}
                  >
                    <div className="admin-modal" onClick={(event) => event.stopPropagation()}>
                      <div className="admin-modal-header">
                        <h3>{t('admin.bindCredential')}</h3>
                        <button
                          className="admin-modal-close"
                          onClick={() => setIsBindCredentialOpen(false)}
                        >
                          <X size={20} />
                        </button>
                      </div>

                      <form onSubmit={handleBindCredential} className="admin-form">
                        <div className="admin-form-group">
                          <label>{t('admin.userId')}</label>
                          <input
                            value={bindCredentialForm.userId}
                            onChange={(event) =>
                              setBindCredentialForm((current) => ({
                                ...current,
                                userId: event.target.value,
                              }))
                            }
                            className="admin-input"
                          />
                        </div>
                        <div className="admin-form-group">
                          <label>{t('admin.providerCode')}</label>
                          <input
                            value={bindCredentialForm.providerCode}
                            onChange={(event) =>
                              setBindCredentialForm((current) => ({
                                ...current,
                                providerCode: event.target.value,
                              }))
                            }
                            className="admin-input"
                          />
                        </div>
                        <div className="admin-form-group">
                          <label>{t('admin.registrationSource')}</label>
                          <input
                            value={bindCredentialForm.registrationSource}
                            onChange={(event) =>
                              setBindCredentialForm((current) => ({
                                ...current,
                                registrationSource: event.target.value,
                              }))
                            }
                            className="admin-input"
                          />
                        </div>
                        <div className="admin-form-group">
                          <label>{t('admin.assuranceLevel')}</label>
                          <input
                            value={bindCredentialForm.assuranceLevel}
                            onChange={(event) =>
                              setBindCredentialForm((current) => ({
                                ...current,
                                assuranceLevel: event.target.value,
                              }))
                            }
                            className="admin-input"
                          />
                        </div>
                        <div className="admin-form-group">
                          <label>{t('admin.certificatePem')}</label>
                          <textarea
                            value={bindCredentialForm.certificatePem}
                            onChange={(event) =>
                              setBindCredentialForm((current) => ({
                                ...current,
                                certificatePem: event.target.value,
                              }))
                            }
                            rows={4}
                            className="admin-textarea"
                          />
                        </div>
                        <div className="admin-form-actions">
                          <button
                            type="submit"
                            className="admin-btn-primary"
                            disabled={isOperating}
                          >
                            {t('admin.bind')}
                          </button>
                          <button
                            type="button"
                            className="admin-btn-secondary"
                            onClick={() => setIsBindCredentialOpen(false)}
                            disabled={isOperating}
                          >
                            {t('admin.cancel')}
                          </button>
                        </div>
                      </form>
                    </div>
                  </div>
                )}

                {isRoleInspectorOpen && (
                  <div
                    className="admin-modal-overlay"
                    onClick={() => {
                      setIsRoleInspectorOpen(false);
                      setInspectedRole(null);
                    }}
                  >
                    <div className="admin-modal" onClick={(event) => event.stopPropagation()}>
                      <div className="admin-modal-header">
                        <h3>{t('admin.roleDetails')}</h3>
                        <button
                          className="admin-modal-close"
                          onClick={() => {
                            setIsRoleInspectorOpen(false);
                            setInspectedRole(null);
                          }}
                        >
                          <X size={20} />
                        </button>
                      </div>

                      <div className="admin-form">
                        <div className="admin-form-group">
                          <label>{t('admin.roleName')}</label>
                          <div className="admin-role-assign">
                            <input
                              value={roleInspectorValue}
                              onChange={(event) => setRoleInspectorValue(event.target.value)}
                              placeholder="ADMIN"
                              className="admin-input"
                            />
                            <button
                              type="button"
                              className="admin-btn-primary"
                              onClick={() => void handleInspectRole()}
                              disabled={isOperating}
                            >
                              {t('admin.search')}
                            </button>
                          </div>
                        </div>

                        {inspectedRole && (
                          <>
                            <div className="admin-form-group">
                              <label>{t('admin.currentPermissions')}</label>
                              <div className="flex flex-wrap gap-2">
                                {inspectedRole.permissions.length === 0 ? (
                                  <span className="text-sm text-muted">{t('admin.noPermissionsAssigned')}</span>
                                ) : (
                                  inspectedRole.permissions.map((permission) => (
                                    <span
                                      key={permission}
                                      className="inline-flex items-center gap-1 rounded-full bg-surface-subtle px-3 py-1 text-xs font-medium text-ink"
                                    >
                                      {permission}
                                      <button
                                        type="button"
                                        title={`Remove ${permission}`}
                                        className="ml-1 rounded-full p-0.5 hover:bg-red-100 hover:text-red-600"
                                        disabled={isOperating}
                                        onClick={() =>
                                          void handleRemovePermissionFromRole(
                                            inspectedRole.roleName,
                                            permission,
                                          )
                                        }
                                      >
                                        <X size={11} />
                                      </button>
                                    </span>
                                  ))
                                )}
                              </div>
                            </div>

                            <div className="admin-form-group">
                              <label>{t('admin.selectPermission')}</label>
                              <div className="admin-role-assign">
                                <select
                                  className="admin-input"
                                  value={selectedPermissionToAdd}
                                  onChange={(e) => setSelectedPermissionToAdd(e.target.value)}
                                  disabled={isOperating || catalogPermissionsLoading}
                                >
                                  <option value="">
                                    {catalogPermissionsLoading
                                      ? t('admin.loadingPermissions')
                                      : t('admin.selectPermission')}
                                  </option>
                                  {catalogPermissions
                                    .filter(
                                      (p) => !inspectedRole.permissions.includes(p.code),
                                    )
                                    .map((p) => (
                                      <option key={p.code} value={p.code}>
                                        {p.code}
                                        {p.description ? ` — ${p.description}` : ''}
                                      </option>
                                    ))}
                                </select>
                                <button
                                  type="button"
                                  className="admin-btn-primary"
                                  disabled={isOperating || !selectedPermissionToAdd}
                                  onClick={() =>
                                    void handleAssignPermissionToRole(
                                      inspectedRole.roleName,
                                      selectedPermissionToAdd,
                                    )
                                  }
                                >
                                  {t('admin.addPermission')}
                                </button>
                              </div>
                            </div>
                          </>
                        )}
                      </div>
                    </div>
                  </div>
                )}

                {isPermissionCatalogOpen && (
                  <div
                    className="admin-modal-overlay"
                    onClick={() => setIsPermissionCatalogOpen(false)}
                  >
                    <div className="admin-modal" onClick={(event) => event.stopPropagation()}>
                      <div className="admin-modal-header">
                        <h3>{t('admin.permissionCatalog')}</h3>
                        <button
                          className="admin-modal-close"
                          onClick={() => setIsPermissionCatalogOpen(false)}
                        >
                          <X size={20} />
                        </button>
                      </div>

                      <div className="admin-form">
                        <div className="admin-form-group">
                          <label>{t('admin.availablePermissions')}</label>
                          <div className="flex max-h-64 flex-wrap gap-2 overflow-y-auto">
                            {catalogPermissionsLoading ? (
                              <span className="text-sm text-muted">{t('admin.loadingPermissions')}</span>
                            ) : catalogPermissions.length === 0 ? (
                              <span className="text-sm text-muted">{t('admin.noPermissionsAvailable')}</span>
                            ) : (
                              catalogPermissions.map((permission) => (
                                <span
                                  key={permission.code}
                                  className="rounded-full bg-surface-subtle px-3 py-1 text-xs font-medium text-ink"
                                >
                                  {permission.code}
                                </span>
                              ))
                            )}
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                )}
              </div>
            </div>
          </div>
        </FormPage>
      </section>
    </main>
  );
}

interface RoleManagementModalProps {
  userId: string;
  onClose: () => void;
  onAssignRole: (userId: string, roleName: string) => Promise<void>;
  onRolesChanged?: (userId: string, roles: string[]) => void;
  isOperating: boolean;
  t: (key: string) => string;
}

function RoleManagementModal({
  userId,
  onClose,
  onAssignRole,
  onRolesChanged,
  isOperating,
  t,
}: RoleManagementModalProps) {
  const [selectedRole, setSelectedRole] = useState('USER');

  const [currentRoles, setCurrentRoles] = useState<string[]>([]);

  useEffect(() => {
    let cancelled = false;

    void (async () => {
      try {
        const result = await getUserRoles(userId);
        if (!cancelled) {
          setCurrentRoles(result.roles);
        }
      } catch {
        if (!cancelled) {
          setCurrentRoles([]);
        }
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [userId]);

  const handleRemoveRole = async (roleName: string) => {
    try {
      await removeRoleFromUser(userId, roleName);
      const newRoles = currentRoles.filter((role) => role !== roleName);
      setCurrentRoles(newRoles);
      onRolesChanged?.(userId, newRoles);
    } catch {
      // ignore local UI removal failure; server state is the source of truth
    }
  };

  return (
    <div className="admin-modal-overlay" onClick={onClose}>
      <div className="admin-modal" onClick={(e) => e.stopPropagation()}>
        <div className="admin-modal-header">
          <h3>{t('admin.manageUserRoles')}</h3>
          <button className="admin-modal-close" onClick={onClose}>
            <X size={20} />
          </button>
        </div>

        <div className="admin-form">
          <div className="admin-form-group">
            <label>{t('admin.roles')}</label>
            <div className="flex flex-wrap gap-2">
              {currentRoles.length === 0 ? (
                <span className="text-sm text-muted">{t('admin.noRolesLoaded')}</span>
              ) : (
                currentRoles.map((role) => (
                  <button
                    key={role}
                    type="button"
                    onClick={() => void handleRemoveRole(role)}
                    className="rounded-full bg-surface-subtle px-3 py-1 text-xs font-medium text-ink hover:bg-danger-50 hover:text-danger-700"
                  >
                    {role} ×
                  </button>
                ))
              )}
            </div>
          </div>

          <div className="admin-form-group">
            <label>{t('admin.assignRole')}</label>
            <div className="admin-role-assign">
              <select
                value={selectedRole}
                onChange={(e) => setSelectedRole(e.target.value)}
                className="admin-input"
              >
                {KNOWN_ROLES.map((role) => (
                  <option key={role} value={role}>
                    {role}
                  </option>
                ))}
              </select>
              <button
                type="button"
                className="admin-btn-primary"
                onClick={() => onAssignRole(userId, selectedRole)}
                disabled={isOperating}
              >
                {t('admin.assign')}
              </button>
            </div>
          </div>

          <div className="admin-form-actions">
            <button
              type="button"
              className="admin-btn-secondary"
              onClick={onClose}
              disabled={isOperating}
            >
              {t('admin.close')}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

export default AdminPage;
