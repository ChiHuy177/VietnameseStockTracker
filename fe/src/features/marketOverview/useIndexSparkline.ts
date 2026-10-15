import { isoDateDaysAgo } from '@/lib/date';
import { getIndexOhlcvHistory } from '@/services/marketIndexService';
import { useQuery } from '@tanstack/react-query';

const SPARKLINE_LOOKBACK_DAYS = 30;

/** Sibling của `useSparklineData` (watchlist), chỉ khác endpoint `/indices/{symbol}/ohlcv`. */
export function useIndexSparkline(symbol: string) {
  const start = isoDateDaysAgo(SPARKLINE_LOOKBACK_DAYS);
  const end = isoDateDaysAgo(0);

  return useQuery({
    queryKey: ['indices', symbol, 'sparkline', start, end],
    queryFn: () => getIndexOhlcvHistory(symbol, start, end),
    enabled: symbol.trim().length > 0,
    select: (data) => data.bars.map((bar) => bar.close),
  });
}
