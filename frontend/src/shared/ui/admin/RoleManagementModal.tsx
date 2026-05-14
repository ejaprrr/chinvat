import { useEffect, useState } from 'react';
import { X } from 'lucide-react';
import { getUserRoles, removeRoleFromUser } from '@/features/admin/api';

const KNOWN_ROLES = ['USER', 'ADMIN', 'EMPLOYEE', 'SUPERADMIN'];

interface RoleManagementModalProps {
  userId: string;
  onClose: () => void;
  onAssignRole: (userId: string, roleName: string) => Promise<void>;
  onRolesChanged?: (userId: string, roles: string[]) => void;
  isOperating: boolean;
  t: (key: string) => string;
}

export default function RoleManagementModal({
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
        if (!cancelled) setCurrentRoles([]);
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
      // ignore
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
