import api from '@/shared/api/client';

type PaginationMetadata = {
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  isFirst: boolean;
  isLast: boolean;
  hasNext: boolean;
  offset: number;
};

type PageResponse<T> = {
  data: T[];
  pagination: PaginationMetadata;
};

export interface EidasProviderResponse {
  code: string;
  displayName: string;
  countryCode: string;
  enabled: boolean;
}

export interface EidasLoginRequest {
  providerCode: string;
  redirectUri: string;
}

export interface EidasLoginResponse {
  providerCode: string;
  state: string;
  authorizationUrl: string;
  expiresAt: string;
}

export interface EidasCallbackRequest {
  providerCode: string;
  state: string;
  authorizationCode: string;
  externalSubjectId: string;
  levelOfAssurance: string;
}

export interface EidasCallbackResponse {
  providerCode: string;
  externalSubjectId: string;
  levelOfAssurance: string;
  currentStatus: string;
  linkedUserId?: string;
  profileCompletionRequired: boolean;
  processedAt: string;
}

export async function listEidasProviders(): Promise<EidasProviderResponse[]> {
  const response = await api.get<EidasProviderResponse[]>('/auth/eidas/providers');
  return response.data;
}

export async function listEidasProvidersPaged(
  page: number = 0,
  size: number = 20,
  sort?: string,
): Promise<PageResponse<EidasProviderResponse>> {
  const params = new URLSearchParams();
  params.append('page', page.toString());
  params.append('size', size.toString());
  if (sort) params.append('sort', sort);

  const response = await api.get<PageResponse<EidasProviderResponse>>(
    `/auth/eidas/providers/paged?${params.toString()}`,
  );
  return response.data;
}

export async function initiateEidasLogin(payload: EidasLoginRequest): Promise<EidasLoginResponse> {
  const response = await api.post<EidasLoginResponse>('/auth/eidas/login', payload);
  return response.data;
}

export async function handleEidasCallback(
  payload: EidasCallbackRequest,
): Promise<EidasCallbackResponse> {
  const response = await api.post<EidasCallbackResponse>('/auth/eidas/callback', payload);
  return response.data;
}
