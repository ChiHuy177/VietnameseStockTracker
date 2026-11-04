import { isoDateDaysAgo } from '@/lib/date';
import { getOhlcvHistory } from '@/services/stockService';
import { useQuery } from '@tanstack/react-query';

const SPARKLINE_LOOKBACK_DAYS = 30;

export function useSparklineData(symbol: string) {
  const start = isoDateDaysAgo(SPARKLINE_LOOKBACK_DAYS);
  const end = isoDateDaysAgo(0);

  return useQuery({
    queryKey: ['stocks', symbol, 'sparkline'],
    queryFn: () => getOhlcvHistory(symbol, start, end),
    enabled: symbol.trim().length > 0,
    select: (data) => data.bars.map((bar) => bar.close),
  });
}
