import type { OhlcvHistory } from '@/models/ohlcvBar';
import type { StockSummary } from '@/models/stock';
import { httpClient } from './httpClient';

export async function searchStocks(query: string): Promise<StockSummary[]> {
  const response = await httpClient.get<StockSummary[]>('/stocks', { params: { q: query } });
  return response.data;
}

export async function getOhlcvHistory(symbol: string, start: string, end: string): Promise<OhlcvHistory> {
  const response = await httpClient.get<OhlcvHistory>(`/stocks/${symbol}/ohlcv`, { params: { start, end } });
  return response.data;
}
