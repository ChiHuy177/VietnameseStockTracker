import type { WatchlistItem } from '@/models/watchlist';
import { httpClient } from './httpClient';

export async function getWatchlist(): Promise<WatchlistItem[]> {
  const response = await httpClient.get<WatchlistItem[]>('/watchlist');
  return response.data;
}

export async function addToWatchlist(symbol: string): Promise<WatchlistItem> {
  const response = await httpClient.post<WatchlistItem>('/watchlist', { symbol });
  return response.data;
}

export async function removeFromWatchlist(symbol: string): Promise<void> {
  await httpClient.delete(`/watchlist/${symbol}`);
}
