import { Edit2, Trash2, ChevronLeft, ChevronRight, X } from 'lucide-react';
import type { Dispatch, FormEvent, SetStateAction } from 'react';
import type { UserResponse, UpdateUserRequest } from '@/shared/types/user';
import type { CreateUserFormState } from './types';

type Props = {
  t: (k: string, opts?: Record<string, unknown>) => string;
  usersLoading: boolean;
  filteredUsers: UserResponse[];
  userSearchQuery: string;
  setUserSearchQuery: (v: string) => void;
  setIsUserToolsOpen: (v: boolean) => void;
  handleEditUser: (u: UserResponse) => void;
  handleDeleteUser: (id: string) => void;
  usersPagination: { totalPages: number } | null;
  usersPage: number;
  loadUsers: (page: number) => void;
  editingUserId: string | null;
  editingUserData: UpdateUserRequest | null;
  setEditingUserId: (v: string | null) => void;
  editingUserEmail: string;
  setEditingUserData: (d: UpdateUserRequest | null) => void;
  handleSaveUser: (e: FormEvent) => Promise<void> | void;
  isOperating: boolean;
  isCreateUserOpen: boolean;
  setIsCreateUserOpen: (v: boolean) => void;
  createUserForm: CreateUserFormState;
  setCreateUserForm: Dispatch<SetStateAction<CreateUserFormState>>;
  handleCreateUser: (e: FormEvent) => Promise<void> | void;
};

export default function UsersTab({
  t,
  usersLoading,
  filteredUsers,
  userSearchQuery,
  setUserSearchQuery,
  setIsUserToolsOpen,
  handleEditUser,
  handleDeleteUser,
  usersPagination,
  usersPage,
  loadUsers,
  editingUserId,
  editingUserData,
  setEditingUserId,
  editingUserEmail,
  setEditingUserData,
  handleSaveUser,
  isOperating,
  isCreateUserOpen,
  setIsCreateUserOpen,
  createUserForm,
  setCreateUserForm,
  handleCreateUser,
}: Props) {
  return (
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
                <th className="px-4 py-3 text-left font-semibold text-ink">{t('admin.email')}</th>
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
                <th className="px-4 py-3 text-left font-semibold text-ink">{t('admin.actions')}</th>
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
                  <tr key={userData.id} className="hover:bg-surface-subtle transition-colors">
                    <td className="px-4 py-3 text-ink">{userData.email}</td>
                    <td className="px-4 py-3 text-muted font-mono text-xs">{userData.username}</td>
                    <td className="px-4 py-3 text-ink">{userData.fullName}</td>
                    <td className="px-4 py-3 text-muted">{userData.userType}</td>
                    <td className="px-4 py-3">
                      <span
                        className={`inline-block px-3 py-1 text-xs font-semibold rounded-full ${userData.accessLevel === 'SUPERADMIN' ? 'bg-danger-50 text-danger-700' : userData.accessLevel === 'ADMIN' ? 'bg-warning-surface text-warning-ink' : userData.accessLevel === 'GOLD' ? 'bg-brand-50 text-brand-700' : 'bg-surface-subtle text-muted'}`}
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

      {editingUserId && editingUserData && (
        <div className="admin-modal-overlay" onClick={() => setEditingUserId(null)}>
          <div className="admin-modal" onClick={(e) => e.stopPropagation()}>
            <div className="admin-modal-header">
              <h3>{t('admin.editUser')}</h3>
              <button className="admin-modal-close" onClick={() => setEditingUserId(null)}>
                <X size={20} />
              </button>
            </div>
            <form onSubmit={handleSaveUser} className="admin-form">
              <div className="admin-form-group">
                <label>{t('admin.email')}</label>
                <input type="email" value={editingUserEmail} disabled className="admin-input" />
              </div>
              <div className="admin-form-group">
                <label>{t('admin.username')}</label>
                <input
                  type="text"
                  value={editingUserData.username}
                  onChange={(e) =>
                    setEditingUserData({ ...editingUserData, username: e.target.value })
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
                    setEditingUserData({ ...editingUserData, fullName: e.target.value })
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
                    setEditingUserData({ ...editingUserData, phoneNumber: e.target.value })
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
                <button type="submit" className="admin-btn-primary" disabled={isOperating}>
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

      {isCreateUserOpen && (
        <div className="admin-modal-overlay" onClick={() => setIsCreateUserOpen(false)}>
          <div className="admin-modal" onClick={(event) => event.stopPropagation()}>
            <div className="admin-modal-header">
              <h3>{t('admin.createUser')}</h3>
              <button className="admin-modal-close" onClick={() => setIsCreateUserOpen(false)}>
                <X size={20} />
              </button>
            </div>
            <form onSubmit={handleCreateUser} className="admin-form">
              <div className="admin-form-group">
                <label>{t('admin.username')}</label>
                <input
                  value={createUserForm.username}
                  onChange={(event) =>
                    setCreateUserForm((c) => ({ ...c, username: event.target.value }))
                  }
                  className="admin-input"
                />
              </div>
              <div className="admin-form-group">
                <label>{t('admin.fullName')}</label>
                <input
                  value={createUserForm.fullName}
                  onChange={(event) =>
                    setCreateUserForm((c) => ({ ...c, fullName: event.target.value }))
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
                    setCreateUserForm((c) => ({ ...c, email: event.target.value }))
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
                    setCreateUserForm((c) => ({ ...c, password: event.target.value }))
                  }
                  className="admin-input"
                />
              </div>
              <div className="admin-form-group">
                <label>{t('admin.userType')}</label>
                <select
                  value={createUserForm.userType}
                  onChange={(event) =>
                    setCreateUserForm((c) => ({
                      ...c,
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
                    setCreateUserForm((c) => ({ ...c, defaultLanguage: event.target.value }))
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
                    setCreateUserForm((c) => ({ ...c, phoneNumber: event.target.value }))
                  }
                  className="admin-input"
                />
              </div>
              <div className="admin-form-group">
                <label>{t('admin.accessLevel')}</label>
                <select
                  value={createUserForm.accessLevel}
                  onChange={(event) =>
                    setCreateUserForm((c) => ({
                      ...c,
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
                <button type="submit" className="admin-btn-primary" disabled={isOperating}>
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
    </div>
  );
}
