import type { MarketBreadthSnapshot } from '@/models/marketBreadth';
import type { SectorSnapshot } from '@/models/sector';
import { httpClient } from './httpClient';

export async function getMarketBreadth(): Promise<MarketBreadthSnapshot> {
  const response = await httpClient.get<MarketBreadthSnapshot>('/market/breadth');
  return response.data;
}

export async function getSectorSnapshots(): Promise<SectorSnapshot[]> {
  const response = await httpClient.get<SectorSnapshot[]>('/market/sectors');
  return response.data;
}
