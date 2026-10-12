import type { IndexQuote } from '@/models/marketIndex';
import type { OhlcvHistory } from '@/models/ohlcvBar';
import { httpClient } from './httpClient';

export async function getMarketIndices(): Promise<IndexQuote[]> {
  const response = await httpClient.get<IndexQuote[]>('/indices');
  return response.data;
}

// Chỉ số dùng chung shape OHLCV với mã cổ phiếu (backend tái dùng `price_history_ohlcv`), nhưng
// khác endpoint — index không search/watchlist được nên không nằm trong `/stocks`.
export async function getIndexOhlcvHistory(symbol: string, start: string, end: string): Promise<OhlcvHistory> {
  const response = await httpClient.get<OhlcvHistory>(`/indices/${symbol}/ohlcv`, { params: { start, end } });
  return response.data;
}
