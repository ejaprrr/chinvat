import api from '@/shared/api/client';

export interface SyncTrustedProvidersRequest {
  onlineRefresh?: boolean;
}

export interface SyncTrustedProvidersResponse {
  sourceUrl: string;
  processedLotlCount: number;
  processedTlCount: number;
  trustedCertificates: number;
  synchronizedAt: string;
}

export interface ValidateCertificateRequest {
  certificatePem: string;
  refreshTrustedProvidersBeforeValidation?: boolean;
}

export interface ValidateCertificateResponse {
  thumbprintSha256: string;
  subjectDn: string;
  issuerDn: string;
  serialNumber: string;
  notBefore: string;
  notAfter: string;
  validNow: boolean;
  trustedIssuer: boolean;
  trustSource: string;
  keyUsageFlags: string[];
  validatedAt: string;
}

// Trust API - TSL/LOTL Synchronization
export async function synchronizeTrustedProviders(
  onlineRefresh: boolean = true,
): Promise<SyncTrustedProvidersResponse> {
  const response = await api.post<SyncTrustedProvidersResponse>('/trust/tsl/sync', {
    onlineRefresh,
  });
  return response.data;
}

// Trust API - Certificate Validation
export async function validateCertificate(
  certificatePem: string,
  refreshTrustedProvidersBeforeValidation: boolean = false,
): Promise<ValidateCertificateResponse> {
  const response = await api.post<ValidateCertificateResponse>('/trust/certificates/validate', {
    certificatePem,
    refreshTrustedProvidersBeforeValidation,
  });
  return response.data;
}
