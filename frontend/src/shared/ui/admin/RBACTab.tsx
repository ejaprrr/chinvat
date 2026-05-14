import RoleManagementModal from './RoleManagementModal';

type Props = {
  t: (k: string, opts?: Record<string, unknown>) => string;
  users: unknown[];
  usersLoading: boolean;
  userRolesCache: Record<string, string[]>;
  setUserRoleManagementId: (id: string | null) => void;
  userRoleManagementId: string | null;
  handleAssignRole: (userId: string, roleName: string) => Promise<void> | void;
  onRolesChanged: (uid: string, roles: string[]) => void;
  isOperating: boolean;
  roleInspectorValue: string;
  setRoleInspectorValue: (v: string) => void;
  handleInspectRole: () => Promise<void> | void;
  inspectedRole: { roleName: string; permissions: string[] } | null;
  selectedPermissionToAdd: string;
  setSelectedPermissionToAdd: (v: string) => void;
  catalogPermissions: { code: string; description?: string }[];
  catalogPermissionsLoading: boolean;
  handleAssignPermissionToRole: (roleName: string, permissionCode: string) => Promise<void> | void;
  handleRemovePermissionFromRole: (
    roleName: string,
    permissionCode: string,
  ) => Promise<void> | void;
};

export default function RBACTab({
  t,
  users,
  usersLoading,
  userRolesCache,
  setUserRoleManagementId,
  userRoleManagementId,
  handleAssignRole,
  onRolesChanged,
  isOperating,
  roleInspectorValue,
  setRoleInspectorValue,
  handleInspectRole,
  inspectedRole,
  selectedPermissionToAdd,
  setSelectedPermissionToAdd,
  catalogPermissions,
  catalogPermissionsLoading,
  handleAssignPermissionToRole,
  handleRemovePermissionFromRole,
}: Props) {
  return (
    <div className="flex h-full min-h-0 flex-col gap-4 overflow-y-auto">
      <div className="rounded-md border border-border-subtle bg-surface-subtle p-3 shrink-0">
        <p className="mb-2 text-xs font-semibold text-ink">{t('admin.rolePermissions')}</p>
        <div className="flex items-center gap-2">
          <input
            value={roleInspectorValue}
            onChange={(e) => setRoleInspectorValue(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === 'Enter') void handleInspectRole();
            }}
            placeholder={t('admin.roleNamePlaceholder')}
            className="min-w-0 flex-1 rounded-md border border-border-subtle bg-panel px-3 py-1.5 text-sm outline-none focus:border-brand-500"
          />
          <button
            type="button"
            onClick={() => void handleInspectRole()}
            disabled={!roleInspectorValue.trim()}
            className="rounded-md bg-brand-500 px-3 py-1.5 text-xs font-medium text-white hover:bg-brand-600 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {t('admin.inspect')}
          </button>
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
                      onClick={() =>
                        void handleRemovePermissionFromRole(inspectedRole.roleName, perm)
                      }
                      className="ml-0.5 rounded-full hover:bg-red-100 hover:text-red-600 disabled:opacity-50"
                    >
                      ×
                    </button>
                  </span>
                ))
              )}
            </div>
            <div className="flex items-center gap-2">
              <select
                value={selectedPermissionToAdd}
                onChange={(e) => setSelectedPermissionToAdd(e.target.value)}
                disabled={isOperating || catalogPermissionsLoading}
                className="min-w-0 flex-1 rounded-md border border-border-subtle bg-panel px-2 py-1.5 text-xs outline-none focus:border-brand-500"
              >
                <option value="">
                  {catalogPermissionsLoading
                    ? t('admin.loadingPermissions')
                    : t('admin.addPermissionPlaceholder')}
                </option>
                {catalogPermissions
                  .filter((p) => !inspectedRole.permissions.includes(p.code))
                  .map((p) => (
                    <option key={p.code} value={p.code}>
                      {p.code}
                      {p.description ? ` — ${p.description}` : ''}
                    </option>
                  ))}
              </select>
              <button
                type="button"
                disabled={isOperating || !selectedPermissionToAdd}
                onClick={() =>
                  void handleAssignPermissionToRole(inspectedRole.roleName, selectedPermissionToAdd)
                }
                className="rounded-md bg-brand-500 px-3 py-1.5 text-xs font-medium text-white hover:bg-brand-600 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {t('admin.addPermission')}
              </button>
            </div>
          </div>
        )}
      </div>

      {usersLoading ? (
        <div className="py-8 text-center text-muted-soft">{t('admin.loading')}</div>
      ) : (
        <div className="min-h-0 flex-1 overflow-x-auto overflow-y-auto rounded-md border border-border-subtle bg-panel">
          <table className="w-full text-sm">
            <thead className="border-b border-border-subtle bg-surface-subtle">
              <tr>
                <th className="px-4 py-3 text-left font-semibold text-ink">{t('admin.email')}</th>
                <th className="px-4 py-3 text-left font-semibold text-ink">
                  {t('admin.fullName')}
                </th>
                <th className="px-4 py-3 text-left font-semibold text-ink">{t('admin.roles')}</th>
                <th className="px-4 py-3 text-left font-semibold text-ink">{t('admin.actions')}</th>
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
                  <tr key={userData.id} className="transition-colors hover:bg-surface-subtle">
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
                        onClick={() => setUserRoleManagementId(userData.id.toString())}
                        disabled={isOperating}
                        className="rounded bg-surface-subtle px-3 py-2 text-xs font-medium text-ink transition-colors hover:bg-surface-hover disabled:cursor-not-allowed disabled:opacity-50"
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

      {userRoleManagementId && (
        <RoleManagementModal
          userId={userRoleManagementId}
          onClose={() => setUserRoleManagementId(null)}
          onAssignRole={handleAssignRole}
          onRolesChanged={onRolesChanged}
          isOperating={isOperating}
          t={t}
        />
      )}
    </div>
  );
}
