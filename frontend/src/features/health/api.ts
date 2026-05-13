import api from '@/shared/api/client';

export interface HealthResponse {
  status: 'UP' | 'DOWN';
  version: string;
  checkedAt: string;
  healthy: boolean;
}

// Health API - System Health Check
export async function getHealthStatus(): Promise<HealthResponse> {
  const response = await api.get<HealthResponse>('/health', {
    headers: { Authorization: undefined }, // No auth required for health check
  });
  return response.data;
}
