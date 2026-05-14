import { X } from 'lucide-react';
import type { Dispatch, FormEvent, SetStateAction } from 'react';
import type { BindCredentialFormState } from './types';

type Props = {
  t: (k: string, opts?: Record<string, unknown>) => string;
  credentials: unknown[];
  credentialsLoading: boolean;
  loadCredentials: (page: number, userId?: string) => void;
  selectedUserForCerts: string | null;
  setSelectedUserForCerts: (v: string | null) => void;
  selectedCredentialId: string | null;
  setSelectedCredentialId: (id: string | null) => void;
  revokeReason: string;
  setRevokeReason: (v: string) => void;
  handleRevokeCertificate: (id: string) => Promise<void> | void;
  isOperating: boolean;
  isBindCredentialOpen: boolean;
  setIsBindCredentialOpen: (v: boolean) => void;
  bindCredentialForm: BindCredentialFormState;
  setBindCredentialForm: Dispatch<SetStateAction<BindCredentialFormState>>;
  handleBindCredential: (e: FormEvent) => Promise<void> | void;
};

export default function CertificatesTab({
  t,
  credentials,
  credentialsLoading,
  loadCredentials,
  selectedUserForCerts,
  setSelectedUserForCerts,
  selectedCredentialId,
  setSelectedCredentialId,
  revokeReason,
  setRevokeReason,
  handleRevokeCertificate,
  isOperating,
  isBindCredentialOpen,
  setIsBindCredentialOpen,
  bindCredentialForm,
  setBindCredentialForm,
  handleBindCredential,
}: Props) {
  return (
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
                <th className="px-4 py-3 text-left font-semibold text-ink">{t('admin.userId')}</th>
                <th className="px-4 py-3 text-left font-semibold text-ink">
                  {t('admin.subjectDn')}
                </th>
                <th className="px-4 py-3 text-left font-semibold text-ink">{t('admin.status')}</th>
                <th className="px-4 py-3 text-left font-semibold text-ink">
                  {t('admin.notAfter')}
                </th>
                <th className="px-4 py-3 text-left font-semibold text-ink">{t('admin.actions')}</th>
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
                  <tr key={cert.id} className="hover:bg-surface-subtle transition-colors">
                    <td className="px-4 py-3 text-ink font-mono text-xs">
                      {cert.userId.slice(0, 8)}...
                    </td>
                    <td className="px-4 py-3 text-ink max-w-xs truncate text-xs">
                      {cert.subjectDn}
                    </td>
                    <td className="px-4 py-3">
                      <span
                        className={`inline-block px-3 py-1 text-xs font-semibold rounded-full ${cert.revocationStatus === 'REVOKED' ? 'bg-danger-50 text-danger-700' : 'bg-surface-subtle text-ink'}`}
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

      {selectedCredentialId && (
        <div className="admin-modal-overlay" onClick={() => setSelectedCredentialId(null)}>
          <div className="admin-modal" onClick={(e) => e.stopPropagation()}>
            <div className="admin-modal-header">
              <h3>{t('admin.revokeCertificate')}</h3>
              <button className="admin-modal-close" onClick={() => setSelectedCredentialId(null)}>
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
                  onClick={() => void handleRevokeCertificate(selectedCredentialId)}
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

      {isBindCredentialOpen && (
        <div className="admin-modal-overlay" onClick={() => setIsBindCredentialOpen(false)}>
          <div className="admin-modal" onClick={(e) => e.stopPropagation()}>
            <div className="admin-modal-header">
              <h3>{t('admin.bindCredential')}</h3>
              <button className="admin-modal-close" onClick={() => setIsBindCredentialOpen(false)}>
                <X size={20} />
              </button>
            </div>
            <form onSubmit={handleBindCredential} className="admin-form">
              <div className="admin-form-group">
                <label>{t('admin.userId')}</label>
                <input
                  value={bindCredentialForm.userId}
                  onChange={(event) =>
                    setBindCredentialForm((c) => ({ ...c, userId: event.target.value }))
                  }
                  className="admin-input"
                />
              </div>
              <div className="admin-form-group">
                <label>{t('admin.providerCode')}</label>
                <input
                  value={bindCredentialForm.providerCode}
                  onChange={(event) =>
                    setBindCredentialForm((c) => ({ ...c, providerCode: event.target.value }))
                  }
                  className="admin-input"
                />
              </div>
              <div className="admin-form-group">
                <label>{t('admin.registrationSource')}</label>
                <input
                  value={bindCredentialForm.registrationSource}
                  onChange={(event) =>
                    setBindCredentialForm((c) => ({ ...c, registrationSource: event.target.value }))
                  }
                  className="admin-input"
                />
              </div>
              <div className="admin-form-group">
                <label>{t('admin.assuranceLevel')}</label>
                <input
                  value={bindCredentialForm.assuranceLevel}
                  onChange={(event) =>
                    setBindCredentialForm((c) => ({ ...c, assuranceLevel: event.target.value }))
                  }
                  className="admin-input"
                />
              </div>
              <div className="admin-form-group">
                <label>{t('admin.certificatePem')}</label>
                <textarea
                  value={bindCredentialForm.certificatePem}
                  onChange={(event) =>
                    setBindCredentialForm((c) => ({ ...c, certificatePem: event.target.value }))
                  }
                  rows={4}
                  className="admin-textarea"
                />
              </div>
              <div className="admin-form-actions">
                <button type="submit" className="admin-btn-primary" disabled={isOperating}>
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
    </div>
  );
}
