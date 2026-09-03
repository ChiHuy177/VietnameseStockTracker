import { httpClient } from './httpClient';
import type { HealthStatus } from '../models/health';

export async function getHealth(): Promise<HealthStatus> {
  const response = await httpClient.get<HealthStatus>('/health');
  return response.data;
}
