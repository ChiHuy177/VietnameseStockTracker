import { getOhlcvHistory } from '@/services/stockService';
import { useQuery } from '@tanstack/react-query';

export function useOhlcvHistory(symbol: string, start: string, end: string) {
  return useQuery({
    queryKey: ['stocks', symbol, 'ohlcv', start, end],
    queryFn: () => getOhlcvHistory(symbol, start, end),
    enabled: symbol.trim().length > 0,
  });
}
